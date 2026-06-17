package pl.chessarbiter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;
import pl.chessarbiter.service.TournamentService;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;

    @GetMapping
    public List<TournamentDetailResponse> list() {
        return tournamentService.findAll().stream().map(TournamentDetailResponse::from).toList();
    }

    @GetMapping("/{slug}")
    public TournamentDetailResponse get(@PathVariable String slug) {
        return TournamentDetailResponse.from(tournamentService.getBySlug(slug));
    }
}
