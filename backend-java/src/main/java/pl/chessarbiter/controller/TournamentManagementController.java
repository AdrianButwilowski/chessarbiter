package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.dto.common.MessageResponse;
import pl.chessarbiter.dto.engine.PairingModuleResponse;
import pl.chessarbiter.dto.engine.RoundResponse;
import pl.chessarbiter.dto.engine.StandingResponse;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.dto.registration.RegistrationStatusRequest;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;
import pl.chessarbiter.dto.tournament.TournamentRequest;
import pl.chessarbiter.dto.tournament.TournamentStatusRequest;
import pl.chessarbiter.dto.tournament.TournamentSummaryResponse;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.RoundRepository;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.RegistrationService;
import pl.chessarbiter.service.StandingService;
import pl.chessarbiter.service.TournamentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tournaments")
@PreAuthorize("hasAnyRole('ADMIN','ARBITER')")
public class TournamentManagementController {

    private final TournamentService tournamentService;
    private final RegistrationService registrationService;
    private final StandingService standingService;
    private final RoundRepository roundRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<TournamentSummaryResponse> list() {
        return tournamentService.listManagedTournaments(CurrentUser.require())
            .stream()
            .map(TournamentSummaryResponse::from)
            .toList();
    }

    @PostMapping
    @Transactional
    public TournamentDetailResponse create(@Valid @RequestBody TournamentRequest request) {
        return TournamentDetailResponse.from(tournamentService.createTournament(CurrentUser.require(), request));
    }

    @GetMapping("/{tournamentId}")
    @Transactional(readOnly = true)
    public TournamentDetailResponse detail(@PathVariable String tournamentId) {
        return TournamentDetailResponse.from(tournamentService.requireManagedTournament(CurrentUser.require(), tournamentId));
    }

    @PutMapping("/{tournamentId}")
    @Transactional
    public TournamentDetailResponse update(@PathVariable String tournamentId, @Valid @RequestBody TournamentRequest request) {
        return TournamentDetailResponse.from(tournamentService.updateTournament(CurrentUser.require(), tournamentId, request));
    }

    @PatchMapping("/{tournamentId}/status")
    @Transactional
    public TournamentDetailResponse changeStatus(@PathVariable String tournamentId, @Valid @RequestBody TournamentStatusRequest request) {
        return TournamentDetailResponse.from(tournamentService.changeStatus(CurrentUser.require(), tournamentId, request.status()));
    }

    @DeleteMapping("/{tournamentId}")
    public MessageResponse delete(@PathVariable String tournamentId) {
        tournamentService.deleteTournament(CurrentUser.require(), tournamentId);
        return new MessageResponse("Tournament deleted.");
    }

    @GetMapping("/{tournamentId}/registrations")
    @Transactional(readOnly = true)
    public List<RegistrationResponse> registrations(@PathVariable String tournamentId) {
        return registrationService.managedRegistrations(CurrentUser.require(), tournamentId)
            .stream()
            .map(RegistrationResponse::from)
            .toList();
    }

    @PatchMapping("/{tournamentId}/registrations/{registrationId}/status")
    @Transactional
    public RegistrationResponse changeRegistrationStatus(
        @PathVariable String tournamentId,
        @PathVariable String registrationId,
        @Valid @RequestBody RegistrationStatusRequest request
    ) {
        return RegistrationResponse.from(registrationService.updateStatus(CurrentUser.require(), tournamentId, registrationId, request.status()));
    }

    @GetMapping("/{tournamentId}/starting-list")
    @Transactional(readOnly = true)
    public List<RegistrationResponse> startingList(@PathVariable String tournamentId) {
        tournamentService.requireManagedTournament(CurrentUser.require(), tournamentId);
        return registrationService.activeTournamentRegistrations(tournamentId)
            .stream()
            .map(RegistrationResponse::from)
            .toList();
    }

    @GetMapping("/{tournamentId}/pairing-module")
    @Transactional
    public PairingModuleResponse pairingModule(@PathVariable String tournamentId) {
        Tournament tournament = tournamentService.requireManagedTournament(CurrentUser.require(), tournamentId);
        List<RegistrationResponse> participants = registrationService.activeTournamentRegistrations(tournamentId)
            .stream()
            .map(RegistrationResponse::from)
            .toList();
        List<RoundResponse> rounds = roundRepository.findByTournament_IdOrderByRoundNumberAsc(tournamentId)
            .stream()
            .map(RoundResponse::from)
            .toList();
        List<StandingResponse> standings = switch (tournament.getStatus()) {
            case IN_PROGRESS, FINISHED -> standingService.recalculate(tournament).stream()
                .map(StandingResponse::from)
                .toList();
            default -> List.of();
        };

        return new PairingModuleResponse(
            TournamentDetailResponse.from(tournament),
            participants,
            rounds,
            standings
        );
    }

    @GetMapping("/{tournamentId}/rounds")
    @Transactional(readOnly = true)
    public List<RoundResponse> rounds(@PathVariable String tournamentId) {
        tournamentService.requireManagedTournament(CurrentUser.require(), tournamentId);
        return roundRepository.findByTournament_IdOrderByRoundNumberAsc(tournamentId)
            .stream()
            .map(RoundResponse::from)
            .toList();
    }

    @GetMapping("/{tournamentId}/rounds/{roundNumber}")
    @Transactional(readOnly = true)
    public RoundResponse round(@PathVariable String tournamentId, @PathVariable Integer roundNumber) {
        tournamentService.requireManagedTournament(CurrentUser.require(), tournamentId);
        return roundRepository.findByTournament_IdAndRoundNumber(tournamentId, roundNumber)
            .map(RoundResponse::from)
            .orElseThrow(() -> new NotFoundException("Round not found."));
    }

    @GetMapping("/{tournamentId}/standings")
    @Transactional
    public List<StandingResponse> standings(@PathVariable String tournamentId) {
        Tournament tournament = tournamentService.requireManagedTournament(CurrentUser.require(), tournamentId);
        standingService.recalculate(tournament);
        return standingService.standings(tournamentId)
            .stream()
            .map(StandingResponse::from)
            .toList();
    }
}
