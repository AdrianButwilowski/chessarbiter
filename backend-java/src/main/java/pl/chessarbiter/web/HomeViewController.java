package pl.chessarbiter.web;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.chessarbiter.dto.tournament.TournamentDisplayStatus;
import pl.chessarbiter.dto.tournament.TournamentDisplayStatusResolver;
import pl.chessarbiter.dto.tournament.TournamentSummaryResponse;
import pl.chessarbiter.entity.TimeControlType;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;
import pl.chessarbiter.service.TournamentService;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final TournamentService tournamentService;

    @GetMapping("/")
    @Transactional(readOnly = true)
    String home(Model model) {
        List<TournamentSummaryResponse> tournaments = tournamentService.listPublicTournaments().stream()
            .map(this::homeSummary)
            .filter(tournament -> tournament.displayStatus() == TournamentDisplayStatus.SCHEDULED
                || tournament.displayStatus() == TournamentDisplayStatus.IN_PROGRESS)
            .limit(3)
            .toList();
        model.addAttribute("tournaments", tournaments);
        return "home";
    }

    @GetMapping("/turnieje")
    @Transactional(readOnly = true)
    String tournaments(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "all") String filter,
        @RequestParam(required = false) TournamentType type,
        @RequestParam(required = false) TimeControlType time,
        Model model
    ) {
        String query = q.trim().toLowerCase();
        List<TournamentSummaryResponse> tournaments = tournamentService.listPublicTournaments().stream()
            .map(TournamentSummaryResponse::from)
            .filter(tournament -> query.isBlank() || (
                tournament.title() + " " + tournament.city() + " " + tournament.location()
            ).toLowerCase().contains(query))
            .filter(tournament -> type == null || tournament.tournamentType() == type)
            .filter(tournament -> time == null || tournament.timeControlType() == time)
            .filter(tournament -> matchesFilter(tournament, filter))
            .sorted(Comparator.comparing(
                TournamentSummaryResponse::startDate,
                Comparator.nullsLast(Comparator.reverseOrder())
            ))
            .toList();

        model.addAttribute("tournaments", tournaments);
        model.addAttribute("q", q);
        model.addAttribute("filter", filter);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedTime", time);
        return "tournaments/list";
    }

    private TournamentSummaryResponse homeSummary(Tournament tournament) {
        return new TournamentSummaryResponse(
            tournament.getId(),
            tournament.getTitle(),
            tournament.getSlug(),
            tournament.getCity(),
            tournament.getLocation(),
            tournament.getStartDate(),
            tournament.getEndDate(),
            tournament.getTournamentType(),
            tournament.getTimeControlType(),
            tournament.getStatus(),
            TournamentDisplayStatusResolver.resolve(
                tournament.getStatus(),
                tournament.getStartDate(),
                tournament.getEndDate()
            ),
            tournament.isRegistrationOpen(),
            tournament.getMaxPlayers(),
            tournament.isAllowPlayerCancellation(),
            null,
            null,
            null,
            0,
            0,
            0,
            0,
            0
        );
    }

    private boolean matchesFilter(TournamentSummaryResponse tournament, String filter) {
        return switch (filter) {
            case "upcoming" -> tournament.displayStatus() == TournamentDisplayStatus.SCHEDULED;
            case "registration" -> tournament.registrationOpen() && tournament.status() == TournamentStatus.PUBLISHED;
            case "in-progress" -> tournament.displayStatus() == TournamentDisplayStatus.IN_PROGRESS;
            case "finished" -> tournament.displayStatus() == TournamentDisplayStatus.FINISHED;
            case "cancelled" -> tournament.status() == TournamentStatus.CANCELLED;
            default -> true;
        };
    }

}
