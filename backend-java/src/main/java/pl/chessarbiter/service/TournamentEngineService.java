package pl.chessarbiter.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.dto.engine.ManualPairingRequest;
import pl.chessarbiter.dto.engine.ManualParticipantRequest;
import pl.chessarbiter.entity.Game;
import pl.chessarbiter.entity.GameResult;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.Round;
import pl.chessarbiter.entity.RoundStatus;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentRegistration;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.exception.BadRequestException;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.GameRepository;
import pl.chessarbiter.repository.RoundRepository;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.repository.TournamentRepository;
import pl.chessarbiter.repository.UserRepository;
import pl.chessarbiter.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class TournamentEngineService {

    private final TournamentService tournamentService;
    private final StandingService standingService;
    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @Transactional
    public Tournament startTournament(SecurityUser currentUser, String tournamentId) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        if (tournament.getStatus() != TournamentStatus.PUBLISHED && tournament.getStatus() != TournamentStatus.REGISTRATION_CLOSED) {
            throw new BadRequestException("Tournament can be started only after publishing or closing registration.");
        }
        if (roundRepository.existsByTournament_Id(tournament.getId())) {
            throw new BadRequestException("Rounds already exist for this tournament.");
        }

        List<TournamentRegistration> players = registeredPlayers(tournament.getId());
        if (players.size() < 2) {
            throw new BadRequestException("At least two active players are required.");
        }

        for (int index = 0; index < players.size(); index += 1) {
            TournamentRegistration player = players.get(index);
            player.setSeedNumber(index + 1);
            player.setStartNumber(index + 1);
            registrationRepository.save(player);
        }

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament.setRegistrationOpen(false);
        tournamentRepository.save(tournament);

        if (tournament.getTournamentType() == TournamentType.ROUND_ROBIN) {
            createRoundRobinSchedule(tournament, players);
        } else if (tournament.getTournamentType() == TournamentType.SWISS) {
            generateSwissRoundInternal(tournament);
        }

        standingService.recalculate(tournament);
        return tournament;
    }

    @Transactional
    public Round generateNextSwissRound(SecurityUser currentUser, String tournamentId) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        if (tournament.getTournamentType() != TournamentType.SWISS) {
            throw new BadRequestException("Swiss round generation is available only for Swiss tournaments.");
        }
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new BadRequestException("Rounds can be generated only while tournament is in progress.");
        }

        Round round = generateSwissRoundInternal(tournament);
        standingService.recalculate(tournament);
        return round;
    }

    @Transactional
    public Game enterResult(SecurityUser currentUser, String tournamentId, String gameId, GameResult result) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new BadRequestException("Results can be entered only while tournament is in progress.");
        }
        if (result == GameResult.NOT_PLAYED) {
            throw new BadRequestException("Choose a playable result.");
        }

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NotFoundException("Game not found."));
        if (!game.getTournament().getId().equals(tournament.getId())) {
            throw new NotFoundException("Game not found.");
        }
        boolean roundRobin = tournament.getTournamentType() == TournamentType.ROUND_ROBIN;
        // Round-robin schedules exist from the start, so individual round status must not block entering results.
        if (!roundRobin && game.getRound().getStatus() == RoundStatus.NOT_STARTED) {
            throw new BadRequestException("Round is not open yet.");
        }
        if (!roundRobin && game.getRound().getStatus() == RoundStatus.COMPLETED) {
            throw new BadRequestException("Completed round cannot be edited.");
        }

        double[] score = scoreForResult(result);
        User enteredBy = userRepository.findByIdAndDeletedAtIsNull(currentUser.getId())
            .orElseThrow(() -> new NotFoundException("User not found."));
        game.setResult(result);
        game.setWhitePoints(score[0]);
        game.setBlackPoints(score[1]);
        game.setResultEnteredBy(enteredBy);
        game.setResultEnteredAt(Instant.now());
        game.getRound().setStatus(RoundStatus.IN_PROGRESS);
        gameRepository.save(game);
        roundRepository.save(game.getRound());
        standingService.recalculate(tournament);
        return game;
    }

    @Transactional
    public Round completeRound(SecurityUser currentUser, String tournamentId, String roundId) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        if (tournament.getTournamentType() == TournamentType.ROUND_ROBIN) {
            throw new BadRequestException("Round robin rounds do not need to be completed.");
        }
        Round round = roundRepository.findById(roundId).orElseThrow(() -> new NotFoundException("Round not found."));
        if (!round.getTournament().getId().equals(tournament.getId())) {
            throw new NotFoundException("Round not found.");
        }

        List<Game> games = gameRepository.findByRound_IdOrderByBoardNumberAsc(round.getId());
        if (games.isEmpty() || games.stream().anyMatch(game -> game.getResult() == GameResult.NOT_PLAYED)) {
            throw new BadRequestException("Round can be completed only after all results are entered.");
        }
        round.setStatus(RoundStatus.COMPLETED);
        Round saved = roundRepository.save(round);
        standingService.recalculate(tournament);
        return saved;
    }

    @Transactional
    public Tournament finishTournament(SecurityUser currentUser, String tournamentId, boolean early) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new BadRequestException("Only in-progress tournament can be finished.");
        }

        if (tournament.getTournamentType() == TournamentType.ROUND_ROBIN) {
            if (early) {
                throw new BadRequestException("Round robin tournaments are finished from the main finish action.");
            }
            if (!allRoundRobinGamesFinished(tournament.getId())) {
                throw new BadRequestException("All games must have results before finishing the tournament.");
            }
        } else {
            long completed = roundRepository.countByTournament_IdAndStatus(tournament.getId(), RoundStatus.COMPLETED);
            if (early) {
                if (completed == 0) {
                    throw new BadRequestException("At least one completed round is required.");
                }
            } else {
                roundRepository.findFirstByTournament_IdAndStatusNotOrderByRoundNumberAsc(tournament.getId(), RoundStatus.COMPLETED)
                    .ifPresent(round -> {
                        throw new BadRequestException("All rounds must be completed first.");
                    });
                if (tournament.getTournamentType() == TournamentType.SWISS && completed < tournament.getRounds()) {
                    throw new BadRequestException("Swiss tournament requires all planned rounds.");
                }
            }
        }

        standingService.recalculate(tournament);
        tournament.setStatus(TournamentStatus.FINISHED);
        tournament.setRegistrationOpen(false);
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public TournamentRegistration addManualParticipant(SecurityUser currentUser, String tournamentId, ManualParticipantRequest request) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        int nextStartNumber = registrationRepository.findByTournament_IdOrderByStatusAscCreatedAtAsc(tournament.getId()).stream()
            .map(TournamentRegistration::getStartNumber)
            .filter(value -> value != null)
            .max(Integer::compareTo)
            .orElse(0) + 1;

        TournamentRegistration registration = new TournamentRegistration();
        registration.setTournament(tournament);
        registration.setFirstName(request.firstName().trim());
        registration.setLastName(request.lastName().trim());
        registration.setEmail("manual-" + UUID.randomUUID() + "@pairing.local");
        registration.setClubOrCity(request.clubOrCity().trim());
        registration.setRating(request.rating());
        registration.setChessCategory(request.chessCategory() == null || request.chessCategory().isBlank() ? "NONE" : request.chessCategory().trim());
        registration.setBirthYear(request.birthYear());
        registration.setStatus(RegistrationStatus.REGISTERED);
        registration.setStartNumber(nextStartNumber);
        registration.setNotes("Added manually in pairing module.");
        return registrationRepository.save(registration);
    }

    @Transactional
    public Game addManualPairing(SecurityUser currentUser, String tournamentId, ManualPairingRequest request) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new BadRequestException("Manual pairings can be added only while tournament is in progress.");
        }
        String blackRegistrationId = request.blackRegistrationId() == null || request.blackRegistrationId().isBlank()
            ? null
            : request.blackRegistrationId();
        if (blackRegistrationId != null && blackRegistrationId.equals(request.whiteRegistrationId())) {
            throw new BadRequestException("Player cannot be paired with themselves.");
        }

        Round round = roundRepository.findByTournament_IdAndRoundNumber(tournament.getId(), request.roundNumber()).orElseGet(() -> {
            Round created = new Round();
            created.setTournament(tournament);
            created.setRoundNumber(request.roundNumber());
            created.setStatus(RoundStatus.PAIRINGS_PUBLISHED);
            return roundRepository.save(created);
        });

        TournamentRegistration white = registrationRepository.findByIdAndTournament_Id(request.whiteRegistrationId(), tournament.getId())
            .orElseThrow(() -> new NotFoundException("White player not found."));
        TournamentRegistration black = blackRegistrationId == null
            ? null
            : registrationRepository.findByIdAndTournament_Id(blackRegistrationId, tournament.getId())
                .orElseThrow(() -> new NotFoundException("Black player not found."));

        List<String> playerIds = new ArrayList<>();
        playerIds.add(white.getId());
        if (black != null) {
            playerIds.add(black.getId());
        }
        gameRepository.findFirstByRoundIdAndAnyRegistrationId(round.getId(), playerIds).ifPresent(game -> {
            throw new BadRequestException("Player is already paired in this round.");
        });
        gameRepository.findByRound_IdAndBoardNumber(round.getId(), request.boardNumber()).ifPresent(game -> {
            throw new BadRequestException("Board number is already taken in this round.");
        });

        Game game = new Game();
        game.setTournament(tournament);
        game.setRound(round);
        game.setBoardNumber(request.boardNumber());
        game.setWhiteRegistration(white);
        game.setBlackRegistration(black);
        game.setResult(black == null ? GameResult.BYE : GameResult.NOT_PLAYED);
        game.setWhitePoints(black == null ? 1.0 : 0.0);
        game.setBlackPoints(0.0);
        Game saved = gameRepository.save(game);
        standingService.recalculate(tournament);
        return saved;
    }

    private Round generateSwissRoundInternal(Tournament tournament) {
        standingService.recalculate(tournament);
        Round lastRound = roundRepository.findFirstByTournament_IdOrderByRoundNumberDesc(tournament.getId()).orElse(null);
        // Swiss pairings depend on completed results from the previous round.
        if (lastRound != null && lastRound.getStatus() != RoundStatus.COMPLETED) {
            throw new BadRequestException("Complete previous round first.");
        }

        int roundNumber = lastRound == null ? 1 : lastRound.getRoundNumber() + 1;
        if (roundNumber > tournament.getRounds()) {
            throw new BadRequestException("All planned rounds have already been generated.");
        }

        List<TournamentRegistration> players = registeredPlayers(tournament.getId());
        SwissContext context = swissContext(tournament.getId());
        List<Pairing> pairings = roundNumber == 1
            ? firstSwissRound(players, context)
            : laterSwissRound(players, context);

        Round round = new Round();
        round.setTournament(tournament);
        round.setRoundNumber(roundNumber);
        round.setStatus(RoundStatus.PAIRINGS_PUBLISHED);
        Round savedRound = roundRepository.save(round);

        savePairings(tournament, savedRound, pairings);

        return savedRound;
    }

    private List<Pairing> firstSwissRound(List<TournamentRegistration> players, SwissContext context) {
        List<TournamentRegistration> sorted = new ArrayList<>(players);
        sorted.sort(Comparator.comparing(TournamentRegistration::getStartNumber, Comparator.nullsLast(Integer::compareTo)));
        TournamentRegistration bye = sorted.size() % 2 == 1 ? chooseBye(sorted, context) : null;
        if (bye != null) {
            sorted.remove(bye);
        }
        int half = sorted.size() / 2;
        List<Pairing> pairings = new ArrayList<>();

        for (int index = 0; index < half; index += 1) {
            TournamentRegistration top = sorted.get(index);
            TournamentRegistration bottom = sorted.get(index + half);
            TournamentRegistration preferredWhite = index % 2 == 0 ? top : bottom;
            TournamentRegistration preferredBlack = index % 2 == 0 ? bottom : top;
            pairings.add(bestColorPairing(preferredWhite, preferredBlack, context));
        }
        return withByeLast(pairings, bye);
    }

    private List<Pairing> laterSwissRound(List<TournamentRegistration> players, SwissContext context) {
        List<TournamentRegistration> sorted = new ArrayList<>(players);
        sorted.sort(swissOrder(context.points()));

        TournamentRegistration bye = null;
        if (sorted.size() % 2 == 1) {
            bye = chooseBye(sorted, context);
            sorted.remove(bye);
        }

        List<Pairing> pairings = pairSwissPlayers(sorted, context, false);
        if (pairings == null) {
            pairings = pairSwissPlayers(sorted, context, true);
        }
        return withByeLast(pairings, bye);
    }

    private SwissContext swissContext(String tournamentId) {
        Map<String, Double> points = currentPoints(tournamentId);
        Set<String> previousByeIds = new HashSet<>();
        Set<String> playedPairs = new HashSet<>();
        Map<String, ColorStats> colors = new HashMap<>();

        for (Game game : gameRepository.findByTournament_IdOrderByRound_RoundNumberAscBoardNumberAsc(tournamentId)) {
            TournamentRegistration white = game.getWhiteRegistration();
            TournamentRegistration black = game.getBlackRegistration();
            if (game.getResult() == GameResult.BYE) {
                TournamentRegistration byePlayer = white != null ? white : black;
                if (byePlayer != null) {
                    previousByeIds.add(byePlayer.getId());
                }
                continue;
            }
            if (white == null || black == null) {
                continue;
            }
            playedPairs.add(pairKey(white, black));
            colors.computeIfAbsent(white.getId(), id -> new ColorStats()).add(Color.WHITE);
            colors.computeIfAbsent(black.getId(), id -> new ColorStats()).add(Color.BLACK);
        }

        return new SwissContext(points, previousByeIds, playedPairs, colors);
    }

    private TournamentRegistration chooseBye(List<TournamentRegistration> players, SwissContext context) {
        List<TournamentRegistration> eligible = players.stream()
            .filter(player -> !context.previousByeIds().contains(player.getId()))
            .toList();
        List<TournamentRegistration> pool = eligible.isEmpty() ? players : eligible;
        double lowestScore = pool.stream()
            .mapToDouble(player -> context.points().getOrDefault(player.getId(), 0.0))
            .min()
            .orElse(0.0);

        return pool.stream()
            .filter(player -> Double.compare(context.points().getOrDefault(player.getId(), 0.0), lowestScore) == 0)
            .min(Comparator
                .comparing(TournamentRegistration::getRating)
                .thenComparing(TournamentRegistration::getStartNumber, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(TournamentRegistration::getLastName)
                .thenComparing(TournamentRegistration::getFirstName))
            .orElse(players.get(players.size() - 1));
    }

    private List<Pairing> pairSwissPlayers(List<TournamentRegistration> players, SwissContext context, boolean allowRematches) {
        List<Pairing> pairings = new ArrayList<>();
        PairingSearch search = new PairingSearch();
        if (pairSwissPlayers(players, context, allowRematches, pairings, search)) {
            return pairings;
        }
        return allowRematches ? greedySwissPairings(players, context, true) : null;
    }

    private boolean pairSwissPlayers(
        List<TournamentRegistration> remaining,
        SwissContext context,
        boolean allowRematches,
        List<Pairing> pairings,
        PairingSearch search
    ) {
        if (remaining.isEmpty()) {
            return true;
        }
        search.visited += 1;
        if (search.visited > 20000) {
            return false;
        }

        TournamentRegistration player = remaining.get(0);
        List<TournamentRegistration> rest = remaining.subList(1, remaining.size());
        for (PairingCandidate candidate : pairingCandidates(player, rest, context, allowRematches)) {
            List<TournamentRegistration> nextRemaining = new ArrayList<>(rest);
            nextRemaining.remove(candidate.opponent());
            pairings.add(candidate.pairing());
            if (pairSwissPlayers(nextRemaining, context, allowRematches, pairings, search)) {
                return true;
            }
            pairings.remove(pairings.size() - 1);
        }
        return false;
    }

    private List<Pairing> greedySwissPairings(
        List<TournamentRegistration> players,
        SwissContext context,
        boolean allowRematches
    ) {
        List<TournamentRegistration> remaining = new ArrayList<>(players);
        List<Pairing> pairings = new ArrayList<>();
        while (!remaining.isEmpty()) {
            TournamentRegistration player = remaining.remove(0);
            PairingCandidate candidate = pairingCandidates(player, remaining, context, allowRematches).stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unable to generate Swiss pairings."));
            remaining.remove(candidate.opponent());
            pairings.add(candidate.pairing());
        }
        return pairings;
    }

    private List<PairingCandidate> pairingCandidates(
        TournamentRegistration player,
        List<TournamentRegistration> opponents,
        SwissContext context,
        boolean allowRematches
    ) {
        return opponents.stream()
            .filter(opponent -> allowRematches || !hasPlayed(context, player, opponent))
            .map(opponent -> {
                Pairing pairing = bestColorPairing(player, opponent, context);
                return new PairingCandidate(opponent, pairing, pairingScore(player, opponent, pairing, context));
            })
            .sorted(Comparator.comparingInt(PairingCandidate::score))
            .toList();
    }

    private Pairing bestColorPairing(TournamentRegistration first, TournamentRegistration second, SwissContext context) {
        Pairing firstWhite = new Pairing(first, second);
        Pairing secondWhite = new Pairing(second, first);
        int firstWhitePenalty = colorPenalty(first, Color.WHITE, context) + colorPenalty(second, Color.BLACK, context);
        int secondWhitePenalty = colorPenalty(second, Color.WHITE, context) + colorPenalty(first, Color.BLACK, context);
        return secondWhitePenalty < firstWhitePenalty ? secondWhite : firstWhite;
    }

    private int pairingScore(
        TournamentRegistration player,
        TournamentRegistration opponent,
        Pairing pairing,
        SwissContext context
    ) {
        int pointGap = (int) Math.round(Math.abs(
            context.points().getOrDefault(player.getId(), 0.0) - context.points().getOrDefault(opponent.getId(), 0.0)
        ) * 100);
        int colorScore = colorPenalty(pairing.white(), Color.WHITE, context) + colorPenalty(pairing.black(), Color.BLACK, context);
        int rematchScore = hasPlayed(context, player, opponent) ? 10000 : 0;
        return rematchScore + pointGap + colorScore;
    }

    private int colorPenalty(TournamentRegistration player, Color color, SwissContext context) {
        ColorStats stats = context.colors().get(player.getId());
        if (stats == null) {
            return 0;
        }
        int balanceAfter = stats.balance() + (color == Color.WHITE ? 1 : -1);
        int penalty = Math.abs(balanceAfter) * 5;
        if (stats.lastColor == color) {
            penalty += stats.sameColorStreak >= 2 ? 100 : 15;
        }
        return penalty;
    }

    private boolean hasPlayed(SwissContext context, TournamentRegistration first, TournamentRegistration second) {
        return context.playedPairs().contains(pairKey(first, second));
    }

    private String pairKey(TournamentRegistration first, TournamentRegistration second) {
        return first.getId().compareTo(second.getId()) < 0
            ? first.getId() + ":" + second.getId()
            : second.getId() + ":" + first.getId();
    }

    private List<Pairing> withByeLast(List<Pairing> pairings, TournamentRegistration bye) {
        List<Pairing> ordered = new ArrayList<>(pairings);
        if (bye != null) {
            ordered.add(new Pairing(bye, null));
        }
        return ordered;
    }

    private Comparator<TournamentRegistration> swissOrder(Map<String, Double> points) {
        return Comparator
            .<TournamentRegistration>comparingDouble(player -> points.getOrDefault(player.getId(), 0.0)).reversed()
            .thenComparing(TournamentRegistration::getRating, Comparator.reverseOrder())
            .thenComparing(TournamentRegistration::getStartNumber, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(TournamentRegistration::getLastName)
            .thenComparing(TournamentRegistration::getFirstName);
    }

    private void savePairings(Tournament tournament, Round savedRound, List<Pairing> pairings) {
        for (int index = 0; index < pairings.size(); index += 1) {
            Pairing pairing = pairings.get(index);
            Game game = new Game();
            game.setTournament(tournament);
            game.setRound(savedRound);
            game.setBoardNumber(index + 1);
            game.setWhiteRegistration(pairing.white());
            game.setBlackRegistration(pairing.black());
            game.setResult(pairing.black() == null ? GameResult.BYE : GameResult.NOT_PLAYED);
            game.setWhitePoints(pairing.black() == null ? 1.0 : 0.0);
            game.setBlackPoints(0.0);
            savedRound.getGames().add(gameRepository.save(game));
        }
    }

    private Map<String, Double> currentPoints(String tournamentId) {
        Map<String, Double> points = new HashMap<>();
        standingService.standings(tournamentId).forEach(standing -> points.put(standing.getRegistration().getId(), standing.getPoints()));
        return points;
    }

    private void createRoundRobinSchedule(Tournament tournament, List<TournamentRegistration> players) {
        List<TournamentRegistration> ordered = new ArrayList<>(players);
        ordered.sort(Comparator.comparing(TournamentRegistration::getStartNumber, Comparator.nullsLast(Integer::compareTo)));
        List<TournamentRegistration> slots = new ArrayList<>(ordered);
        // A null slot represents a BYE and lets the rotation algorithm work for odd player counts.
        if (slots.size() % 2 == 1) {
            slots.add(null);
        }

        int roundsCount = slots.size() - 1;
        int gamesPerRound = slots.size() / 2;
        for (int roundNumber = 1; roundNumber <= roundsCount; roundNumber += 1) {
            Round round = new Round();
            round.setTournament(tournament);
            round.setRoundNumber(roundNumber);
            round.setStatus(roundNumber == 1 ? RoundStatus.PAIRINGS_PUBLISHED : RoundStatus.NOT_STARTED);
            Round savedRound = roundRepository.save(round);
            List<Pairing> roundPairings = new ArrayList<>();
            TournamentRegistration bye = null;

            for (int boardIndex = 0; boardIndex < gamesPerRound; boardIndex += 1) {
                TournamentRegistration first = slots.get(boardIndex);
                TournamentRegistration second = slots.get(slots.size() - 1 - boardIndex);

                if (first == null || second == null) {
                    bye = first == null ? second : first;
                } else {
                    boolean swapColors = ((roundNumber - 1) + boardIndex) % 2 == 1;
                    boolean fixedBoardSwap = boardIndex == 0 && (roundNumber - 1) % 2 == 1;
                    boolean whiteIsSecond = swapColors != fixedBoardSwap;
                    roundPairings.add(new Pairing(whiteIsSecond ? second : first, whiteIsSecond ? first : second));
                }
            }
            savePairings(tournament, savedRound, withByeLast(roundPairings, bye));

            TournamentRegistration fixed = slots.get(0);
            List<TournamentRegistration> rotating = new ArrayList<>(slots.subList(1, slots.size()));
            TournamentRegistration last = rotating.remove(rotating.size() - 1);
            rotating.add(0, last);
            slots.clear();
            slots.add(fixed);
            slots.addAll(rotating);
        }
    }

    private List<TournamentRegistration> registeredPlayers(String tournamentId) {
        return registrationRepository.findByTournament_IdAndStatusOrderByStartNumberAscRatingDescLastNameAscFirstNameAsc(
            tournamentId,
            RegistrationStatus.REGISTERED
        );
    }

    private boolean allRoundRobinGamesFinished(String tournamentId) {
        List<Game> games = gameRepository.findByTournament_IdOrderByRound_RoundNumberAscBoardNumberAsc(tournamentId);
        return !games.isEmpty() && games.stream().noneMatch(game -> game.getResult() == GameResult.NOT_PLAYED);
    }

    private double[] scoreForResult(GameResult result) {
        return switch (result) {
            case WHITE_WIN, BLACK_FORFEIT, BYE -> new double[] {1.0, 0.0};
            case BLACK_WIN, WHITE_FORFEIT -> new double[] {0.0, 1.0};
            case DRAW -> new double[] {0.5, 0.5};
            case DOUBLE_FORFEIT, NOT_PLAYED -> new double[] {0.0, 0.0};
        };
    }

    private enum Color {
        WHITE,
        BLACK
    }

    private record SwissContext(
        Map<String, Double> points,
        Set<String> previousByeIds,
        Set<String> playedPairs,
        Map<String, ColorStats> colors
    ) {
    }

    private record PairingCandidate(TournamentRegistration opponent, Pairing pairing, int score) {
    }

    private static final class PairingSearch {
        private int visited;
    }

    private static final class ColorStats {
        private int whiteCount;
        private int blackCount;
        private Color lastColor;
        private int sameColorStreak;

        private void add(Color color) {
            if (color == Color.WHITE) {
                whiteCount += 1;
            } else {
                blackCount += 1;
            }
            sameColorStreak = lastColor == color ? sameColorStreak + 1 : 1;
            lastColor = color;
        }

        private int balance() {
            return whiteCount - blackCount;
        }
    }

    private record Pairing(TournamentRegistration white, TournamentRegistration black) {
    }
}
