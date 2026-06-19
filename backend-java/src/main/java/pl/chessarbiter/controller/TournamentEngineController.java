package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.dto.engine.GameResponse;
import pl.chessarbiter.dto.engine.GameResultRequest;
import pl.chessarbiter.dto.engine.ManualPairingRequest;
import pl.chessarbiter.dto.engine.ManualParticipantRequest;
import pl.chessarbiter.dto.engine.RoundResponse;
import pl.chessarbiter.dto.engine.StandingResponse;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.StandingService;
import pl.chessarbiter.service.TournamentEngineService;
import pl.chessarbiter.service.TournamentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tournaments/{tournamentId}")
@PreAuthorize("hasAnyRole('ADMIN','ARBITER')")
public class TournamentEngineController {

    private final TournamentEngineService engineService;
    private final TournamentService tournamentService;
    private final StandingService standingService;

    @PostMapping("/start")
    @Transactional
    public TournamentDetailResponse start(@PathVariable String tournamentId) {
        return TournamentDetailResponse.from(engineService.startTournament(CurrentUser.require(), tournamentId));
    }

    @PostMapping("/rounds/swiss/next")
    @Transactional
    public RoundResponse generateSwissRound(@PathVariable String tournamentId) {
        return RoundResponse.from(engineService.generateNextSwissRound(CurrentUser.require(), tournamentId));
    }

    @PatchMapping("/games/{gameId}/result")
    @Transactional
    public GameResponse enterResult(
        @PathVariable String tournamentId,
        @PathVariable String gameId,
        @Valid @RequestBody GameResultRequest request
    ) {
        return GameResponse.from(engineService.enterResult(CurrentUser.require(), tournamentId, gameId, request.result()));
    }

    @PostMapping("/rounds/{roundId}/complete")
    @Transactional
    public RoundResponse completeRound(@PathVariable String tournamentId, @PathVariable String roundId) {
        return RoundResponse.from(engineService.completeRound(CurrentUser.require(), tournamentId, roundId));
    }

    @PostMapping("/finish")
    @Transactional
    public TournamentDetailResponse finish(@PathVariable String tournamentId) {
        return TournamentDetailResponse.from(engineService.finishTournament(CurrentUser.require(), tournamentId, false));
    }

    @PostMapping("/finish-early")
    @Transactional
    public TournamentDetailResponse finishEarly(@PathVariable String tournamentId) {
        return TournamentDetailResponse.from(engineService.finishTournament(CurrentUser.require(), tournamentId, true));
    }

    @PostMapping("/standings/recalculate")
    @Transactional
    public List<StandingResponse> recalculateStandings(@PathVariable String tournamentId) {
        Tournament tournament = tournamentService.requireManagedTournament(CurrentUser.require(), tournamentId);
        return standingService.recalculate(tournament)
            .stream()
            .map(StandingResponse::from)
            .toList();
    }

    @PostMapping("/participants/manual")
    @Transactional
    public RegistrationResponse addManualParticipant(
        @PathVariable String tournamentId,
        @Valid @RequestBody ManualParticipantRequest request
    ) {
        return RegistrationResponse.from(engineService.addManualParticipant(CurrentUser.require(), tournamentId, request));
    }

    @PostMapping("/pairings/manual")
    @Transactional
    public GameResponse addManualPairing(@PathVariable String tournamentId, @Valid @RequestBody ManualPairingRequest request) {
        return GameResponse.from(engineService.addManualPairing(CurrentUser.require(), tournamentId, request));
    }
}
