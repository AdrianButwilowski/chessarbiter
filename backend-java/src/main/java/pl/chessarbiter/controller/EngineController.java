package pl.chessarbiter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.chessarbiter.dto.engine.PairingModuleResponse;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;
import pl.chessarbiter.service.TournamentService;
import pl.chessarbiter.service.RegistrationService;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments/{slug}/engine")
@RequiredArgsConstructor
public class EngineController {
    private final TournamentService tournamentService;
    private final RegistrationService registrationService;

    @GetMapping
    public PairingModuleResponse get(@PathVariable String slug) {
        var tournament = tournamentService.getBySlug(slug);
        List<RegistrationResponse> participants = registrationService.getForTournament(tournament.getId());
        return new PairingModuleResponse(TournamentDetailResponse.from(tournament), participants, List.of(), List.of());
    }
}
