package pl.chessarbiter.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.entity.Game;
import pl.chessarbiter.entity.GameResult;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.Round;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentRegistration;
import pl.chessarbiter.entity.TournamentStanding;
import pl.chessarbiter.repository.GameRepository;
import pl.chessarbiter.repository.RoundRepository;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.repository.TournamentStandingRepository;

@Service
@RequiredArgsConstructor
public class StandingService {

    private final TournamentRegistrationRepository registrationRepository;
    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final TournamentStandingRepository standingRepository;

    @Transactional
    public List<TournamentStanding> recalculate(Tournament tournament) {
        List<TournamentRegistration> registrations = registrationRepository
            .findByTournament_IdAndStatusNotOrderByStartNumberAscRatingDescLastNameAscFirstNameAsc(
                tournament.getId(),
                RegistrationStatus.CANCELLED
            );
        Map<String, StandingAccumulator> standings = new HashMap<>();
        registrations.forEach(registration -> standings.put(registration.getId(), new StandingAccumulator()));

        List<Round> rounds = roundRepository.findByTournament_IdOrderByRoundNumberAsc(tournament.getId());
        for (Round round : rounds) {
            List<Game> games = gameRepository.findByRound_IdOrderByBoardNumberAsc(round.getId());
            games.forEach(game -> applyGame(standings, game));
            standings.values().forEach(standing -> standing.progressiveScore += standing.points);
        }

        applyTiebreaks(standings);
        List<TournamentRegistration> sorted = sortRegistrations(registrations, standings);
        List<TournamentStanding> saved = new ArrayList<>();

        for (int index = 0; index < sorted.size(); index += 1) {
            TournamentRegistration registration = sorted.get(index);
            StandingAccumulator accumulator = standings.get(registration.getId());
            TournamentStanding standing = standingRepository
                .findByTournament_IdAndRegistration_Id(tournament.getId(), registration.getId())
                .orElseGet(TournamentStanding::new);

            standing.setTournament(tournament);
            standing.setRegistration(registration);
            standing.setPoints(accumulator.points);
            standing.setGamesPlayed(accumulator.gamesPlayed);
            standing.setWins(accumulator.wins);
            standing.setDraws(accumulator.draws);
            standing.setLosses(accumulator.losses);
            standing.setForfeits(accumulator.forfeits);
            standing.setBuchholz(accumulator.buchholz);
            standing.setMedianBuchholz(accumulator.medianBuchholz);
            standing.setSonnebornBerger(accumulator.sonnebornBerger);
            standing.setProgressiveScore(accumulator.progressiveScore);
            standing.setRank(index + 1);

            registration.setFinalRank(index + 1);
            saved.add(standingRepository.save(standing));
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<TournamentStanding> standings(String tournamentId) {
        return standingRepository.findByTournament_IdOrderByRankAsc(tournamentId);
    }

    private void applyGame(Map<String, StandingAccumulator> standings, Game game) {
        if (game.getResult() == GameResult.NOT_PLAYED) {
            return;
        }
        if (game.getResult() == GameResult.BYE) {
            TournamentRegistration byePlayer = game.getWhiteRegistration() != null ? game.getWhiteRegistration() : game.getBlackRegistration();
            if (byePlayer != null && standings.containsKey(byePlayer.getId())) {
                StandingAccumulator standing = standings.get(byePlayer.getId());
                standing.points += 1;
                standing.wins += 1;
            }
            return;
        }
        if (game.getWhiteRegistration() == null || game.getBlackRegistration() == null) {
            return;
        }

        StandingAccumulator white = standings.get(game.getWhiteRegistration().getId());
        StandingAccumulator black = standings.get(game.getBlackRegistration().getId());
        if (white == null || black == null) {
            return;
        }

        white.points += game.getWhitePoints();
        black.points += game.getBlackPoints();
        white.gamesPlayed += 1;
        black.gamesPlayed += 1;
        white.opponentIds.add(game.getBlackRegistration().getId());
        black.opponentIds.add(game.getWhiteRegistration().getId());
        white.opponentResults.add(new OpponentResult(game.getBlackRegistration().getId(), game.getWhitePoints()));
        black.opponentResults.add(new OpponentResult(game.getWhiteRegistration().getId(), game.getBlackPoints()));

        if (game.getResult() == GameResult.DRAW) {
            white.draws += 1;
            black.draws += 1;
        } else if (game.getWhitePoints() > game.getBlackPoints()) {
            white.wins += 1;
            black.losses += 1;
        } else if (game.getBlackPoints() > game.getWhitePoints()) {
            black.wins += 1;
            white.losses += 1;
        } else {
            white.losses += 1;
            black.losses += 1;
        }

        if (isForfeit(game.getResult())) {
            white.forfeits += 1;
            black.forfeits += 1;
        }
    }

    private void applyTiebreaks(Map<String, StandingAccumulator> standings) {
        // Tie-breaks are calculated after all raw points are known, because they depend on opponents' final scores.
        standings.values().forEach(standing -> {
            List<Double> opponentScores = standing.opponentIds.stream()
                .map(opponentId -> standings.get(opponentId) == null ? 0.0 : standings.get(opponentId).points)
                .toList();
            standing.buchholz = sum(opponentScores);
            standing.medianBuchholz = medianBuchholz(opponentScores);
            standing.sonnebornBerger = standing.opponentResults.stream()
                .mapToDouble(result -> {
                    double opponentScore = standings.get(result.opponentId()) == null ? 0.0 : standings.get(result.opponentId()).points;
                    if (result.score() == 1.0) {
                        return opponentScore;
                    }
                    if (result.score() == 0.5) {
                        return opponentScore / 2.0;
                    }
                    return 0.0;
                })
                .sum();
        });
    }

    private List<TournamentRegistration> sortRegistrations(
        List<TournamentRegistration> registrations,
        Map<String, StandingAccumulator> standings
    ) {
        return registrations.stream()
            .sorted(Comparator
                .<TournamentRegistration>comparingDouble(registration -> standings.get(registration.getId()).points).reversed()
                .thenComparing(registration -> standings.get(registration.getId()).buchholz, Comparator.reverseOrder())
                .thenComparing(registration -> standings.get(registration.getId()).sonnebornBerger, Comparator.reverseOrder())
                .thenComparing(registration -> standings.get(registration.getId()).progressiveScore, Comparator.reverseOrder())
                .thenComparing(TournamentRegistration::getRating, Comparator.reverseOrder())
                .thenComparing(TournamentRegistration::getLastName)
                .thenComparing(TournamentRegistration::getFirstName))
            .toList();
    }

    private boolean isForfeit(GameResult result) {
        return result == GameResult.WHITE_FORFEIT || result == GameResult.BLACK_FORFEIT || result == GameResult.DOUBLE_FORFEIT;
    }

    private double medianBuchholz(List<Double> scores) {
        if (scores.size() < 3) {
            return sum(scores);
        }
        List<Double> sorted = scores.stream().sorted().toList();
        return sum(sorted.subList(1, sorted.size() - 1));
    }

    private double sum(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }

    private static final class StandingAccumulator {
        private double points;
        private int gamesPlayed;
        private int wins;
        private int draws;
        private int losses;
        private int forfeits;
        private double buchholz;
        private double medianBuchholz;
        private double sonnebornBerger;
        private double progressiveScore;
        private final List<String> opponentIds = new ArrayList<>();
        private final List<OpponentResult> opponentResults = new ArrayList<>();
    }

    private record OpponentResult(String opponentId, double score) {
    }
}
