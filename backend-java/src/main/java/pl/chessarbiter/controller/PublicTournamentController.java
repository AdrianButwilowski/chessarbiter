package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.dto.engine.RoundResponse;
import pl.chessarbiter.dto.engine.StandingResponse;
import pl.chessarbiter.dto.player.PlayerDetailResponse;
import pl.chessarbiter.dto.player.PlayerGameResponse;
import pl.chessarbiter.dto.registration.RegistrationRequest;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;
import pl.chessarbiter.dto.tournament.TournamentSummaryResponse;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.RoundRepository;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.repository.TournamentStandingRepository;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.RegistrationService;
import pl.chessarbiter.service.StandingService;
import pl.chessarbiter.service.TournamentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/tournaments")
public class PublicTournamentController {

    private final TournamentService tournamentService;
    private final RegistrationService registrationService;
    private final StandingService standingService;
    private final RoundRepository roundRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentStandingRepository standingRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<TournamentSummaryResponse> list() {
        return tournamentService.listPublicTournaments()
            .stream()
            .map(TournamentSummaryResponse::from)
            .toList();
    }

    @GetMapping("/{slug}")
    @Transactional(readOnly = true)
    public TournamentDetailResponse detail(@PathVariable String slug) {
        return TournamentDetailResponse.from(tournamentService.getPublicTournament(slug));
    }

    @GetMapping("/{slug}/registrations")
    @Transactional(readOnly = true)
    public List<RegistrationResponse> registrations(@PathVariable String slug) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        return registrationRepository
            .findByTournament_IdAndStatusNotOrderByStartNumberAscRatingDescLastNameAscFirstNameAsc(
                tournament.getId(),
                RegistrationStatus.CANCELLED
            )
            .stream()
            .map(RegistrationResponse::from)
            .toList();
    }

    @PostMapping("/{tournamentId}/registrations")
    @Transactional
    public RegistrationResponse register(@PathVariable String tournamentId, @Valid @RequestBody RegistrationRequest request) {
        return RegistrationResponse.from(registrationService.register(tournamentId, request, CurrentUser.optional().orElse(null)));
    }

    @GetMapping("/{slug}/rounds")
    @Transactional(readOnly = true)
    public List<RoundResponse> rounds(@PathVariable String slug) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        return roundRepository.findByTournament_IdOrderByRoundNumberAsc(tournament.getId())
            .stream()
            .map(RoundResponse::from)
            .toList();
    }

    @GetMapping("/{slug}/rounds/{roundNumber}")
    @Transactional(readOnly = true)
    public RoundResponse round(@PathVariable String slug, @PathVariable Integer roundNumber) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        return roundRepository.findByTournament_IdAndRoundNumber(tournament.getId(), roundNumber)
            .map(RoundResponse::from)
            .orElseThrow(() -> new NotFoundException("Round not found."));
    }

    @GetMapping("/{slug}/players/{registrationId}")
    @Transactional(readOnly = true)
    public PlayerDetailResponse player(@PathVariable String slug, @PathVariable String registrationId) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        var registration = registrationRepository.findByIdAndTournament_Id(registrationId, tournament.getId())
            .orElseThrow(() -> new NotFoundException("Player not found."));
        var standing = standingRepository.findByTournament_IdAndRegistration_Id(tournament.getId(), registration.getId()).orElse(null);
        var currentUser = CurrentUser.optional().orElse(null);
        boolean canSeePrivate = currentUser != null
            && ("ADMIN".equals(currentUser.getRole()) || tournament.getCreatedBy().getId().equals(currentUser.getId()));
        List<PlayerGameResponse> games = new java.util.ArrayList<PlayerGameResponse>();
        registration.getWhiteGames().forEach(game -> games.add(PlayerGameResponse.asWhite(game)));
        registration.getBlackGames().forEach(game -> games.add(PlayerGameResponse.asBlack(game)));
        games.sort(Comparator.comparing(PlayerGameResponse::roundNumber).thenComparing(PlayerGameResponse::boardNumber));

        return new PlayerDetailResponse(
            registration.getId(),
            registration.getFirstName(),
            registration.getLastName(),
            canSeePrivate ? registration.getEmail() : null,
            registration.getClubOrCity(),
            registration.getFederation(),
            registration.getRating(),
            registration.getChessCategory(),
            registration.getBirthYear(),
            registration.getStatus(),
            registration.getStartNumber(),
            standing == null ? 0.0 : standing.getPoints(),
            standing == null ? null : standing.getRank(),
            canSeePrivate ? registration.getPhoneNumber() : null,
            canSeePrivate ? registration.getNotes() : null,
            games
        );
    }

    @GetMapping("/{slug}/standings")
    @Transactional
    public List<StandingResponse> standings(@PathVariable String slug) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        return standingService.recalculate(tournament)
            .stream()
            .map(StandingResponse::from)
            .toList();
    }
}
