package pl.chessarbiter.web;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.chessarbiter.dto.engine.GameResponse;
import pl.chessarbiter.dto.engine.ManualPairingRequest;
import pl.chessarbiter.dto.engine.ManualParticipantRequest;
import pl.chessarbiter.dto.engine.RoundResponse;
import pl.chessarbiter.dto.engine.StandingResponse;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;
import pl.chessarbiter.dto.tournament.TournamentSummaryResponse;
import pl.chessarbiter.entity.GameResult;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.RoundStatus;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;
import pl.chessarbiter.exception.ApiException;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.RoundRepository;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.RegistrationService;
import pl.chessarbiter.service.StandingService;
import pl.chessarbiter.service.TournamentEngineService;
import pl.chessarbiter.service.TournamentService;
import pl.chessarbiter.web.form.TournamentForm;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','ARBITER')")
public class JudgePanelViewController {

    private final TournamentService tournamentService;
    private final RegistrationService registrationService;
    private final TournamentEngineService engineService;
    private final StandingService standingService;
    private final RoundRepository roundRepository;

    @GetMapping("/panel-sedziego")
    @Transactional(readOnly = true)
    String dashboard(Model model) {
        List<TournamentSummaryResponse> tournaments = tournamentService.listManagedTournaments(CurrentUser.require())
            .stream().map(TournamentSummaryResponse::from).toList();
        model.addAttribute("tournaments", tournaments);
        model.addAttribute("activeRegistrations", tournaments.stream()
            .filter(tournament -> tournament.registrationOpen()
                && tournament.status() != TournamentStatus.FINISHED
                && tournament.status() != TournamentStatus.CANCELLED)
            .mapToLong(tournament -> tournament.activeRegistrationCount()).sum());
        model.addAttribute("upcomingCount", tournaments.stream()
            .filter(tournament -> "SCHEDULED".equals(tournament.displayStatus().name())).count());
        return "judge/dashboard";
    }

    @GetMapping("/panel-sedziego/turnieje/nowy")
    String newTournament(Model model) {
        model.addAttribute("tournamentForm", new TournamentForm());
        model.addAttribute("editing", false);
        model.addAttribute("adminMode", false);
        return "judge/tournament-form";
    }

    @PostMapping("/panel-sedziego/turnieje/nowy")
    @Transactional
    String createTournament(
        @Valid @ModelAttribute("tournamentForm") TournamentForm form,
        BindingResult bindingResult,
        @RequestParam(defaultValue = "save") String intent,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if ("publish".equals(intent)) {
            form.setStatus(TournamentStatus.PUBLISHED);
        } else {
            form.setStatus(TournamentStatus.DRAFT);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            model.addAttribute("adminMode", false);
            return "judge/tournament-form";
        }
        try {
            Tournament tournament = tournamentService.createTournament(CurrentUser.require(), form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Turniej został utworzony.");
            if ("ADMIN".equals(CurrentUser.require().getRole())) {
                return "redirect:/panel-admina/turnieje";
            }
            return "redirect:/panel-sedziego/turnieje/" + tournament.getId() + "/edytuj";
        } catch (ApiException exception) {
            bindingResult.reject("tournament", exception.getMessage());
            model.addAttribute("editing", false);
            model.addAttribute("adminMode", false);
            return "judge/tournament-form";
        }
    }

    @GetMapping("/panel-sedziego/turnieje/{id}/edytuj")
    @Transactional(readOnly = true)
    String editTournament(@PathVariable String id, Model model) {
        Tournament tournament = managed(id);
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("tournamentForm", TournamentForm.from(tournament));
        model.addAttribute("editing", true);
        model.addAttribute("adminMode", false);
        return "judge/tournament-form";
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/edytuj")
    @Transactional
    String updateTournament(
        @PathVariable String id,
        @Valid @ModelAttribute("tournamentForm") TournamentForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Tournament tournament = managed(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
            model.addAttribute("editing", true);
            model.addAttribute("adminMode", false);
            return "judge/tournament-form";
        }
        try {
            tournamentService.updateTournament(CurrentUser.require(), id, form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Turniej został zapisany.");
            return "redirect:/panel-sedziego/turnieje/" + id + "/edytuj";
        } catch (ApiException exception) {
            bindingResult.reject("tournament", exception.getMessage());
            model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
            model.addAttribute("editing", true);
            model.addAttribute("adminMode", false);
            return "judge/tournament-form";
        }
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/status")
    String changeStatus(
        @PathVariable String id,
        @RequestParam TournamentStatus status,
        RedirectAttributes redirectAttributes
    ) {
        return action(
            () -> tournamentService.changeStatus(CurrentUser.require(), id, status),
            "Status turnieju został zmieniony.",
            "/panel-sedziego",
            redirectAttributes
        );
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/usun")
    String deleteTournament(@PathVariable String id, RedirectAttributes redirectAttributes) {
        return action(
            () -> tournamentService.deleteTournament(CurrentUser.require(), id),
            "Turniej został usunięty.",
            "/panel-sedziego",
            redirectAttributes
        );
    }

    @GetMapping("/panel-sedziego/turnieje/{id}/zgloszenia")
    @Transactional(readOnly = true)
    String registrations(
        @PathVariable String id,
        @RequestParam(defaultValue = "") String q,
        @RequestParam(required = false) RegistrationStatus status,
        Model model
    ) {
        Tournament tournament = managed(id);
        String query = q.trim().toLowerCase();
        List<RegistrationResponse> registrations = registrationService.managedRegistrations(CurrentUser.require(), id)
            .stream()
            .map(RegistrationResponse::from)
            .filter(registration -> status == null || registration.status() == status)
            .filter(registration -> query.isBlank() || (
                registration.firstName() + " " + registration.lastName() + " "
                    + registration.email() + " " + registration.clubOrCity()
            ).toLowerCase().contains(query))
            .toList();
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("registrations", registrations);
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("registrationsLocked", tournament.getStatus() == TournamentStatus.FINISHED);
        return "judge/registrations";
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/zgloszenia/{registrationId}/status")
    String registrationStatus(
        @PathVariable String id,
        @PathVariable String registrationId,
        @RequestParam RegistrationStatus status,
        RedirectAttributes redirectAttributes
    ) {
        return action(
            () -> registrationService.updateStatus(CurrentUser.require(), id, registrationId, status),
            "Status zgłoszenia został zmieniony.",
            "/panel-sedziego/turnieje/" + id + "/zgloszenia",
            redirectAttributes
        );
    }

    @GetMapping("/panel-sedziego/turnieje/{id}/rundy")
    @Transactional(readOnly = true)
    String rounds(@PathVariable String id, Model model) {
        Tournament tournament = managed(id);
        List<RoundResponse> rounds = roundRepository.findByTournament_IdOrderByRoundNumberAsc(id)
            .stream().map(RoundResponse::from).toList();
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("rounds", rounds);
        boolean allRoundsCompleted = !rounds.isEmpty()
            && rounds.stream().allMatch(round -> round.status() == RoundStatus.COMPLETED);
        long roundRobinGameCount = rounds.stream()
            .mapToLong(round -> round.games().size())
            .sum();
        boolean allRoundRobinGamesFinished = roundRobinGameCount > 0
            && rounds.stream()
                .flatMap(round -> round.games().stream())
                .noneMatch(game -> game.result() == GameResult.NOT_PLAYED);
        model.addAttribute("allRoundsCompleted", allRoundsCompleted);
        model.addAttribute("canFinishTournament", tournament.getTournamentType() == TournamentType.ROUND_ROBIN
            ? allRoundRobinGamesFinished
            : allRoundsCompleted);
        model.addAttribute("completedRoundExists", rounds.stream()
            .anyMatch(round -> round.status() == RoundStatus.COMPLETED));
        return "judge/rounds";
    }

    @GetMapping("/panel-sedziego/turnieje/{id}/rundy/{roundNumber}")
    @Transactional(readOnly = true)
    String round(@PathVariable String id, @PathVariable Integer roundNumber, Model model) {
        Tournament tournament = managed(id);
        RoundResponse round = roundRepository.findByTournament_IdAndRoundNumber(id, roundNumber)
            .map(RoundResponse::from)
            .orElseThrow(() -> new NotFoundException("Nie znaleziono rundy."));
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("round", round);
        model.addAttribute("editable", tournament.getStatus() == TournamentStatus.IN_PROGRESS);
        model.addAttribute("managementView", true);
        return "tournaments/round";
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/start")
    String start(@PathVariable String id, RedirectAttributes redirectAttributes) {
        return action(
            () -> engineService.startTournament(CurrentUser.require(), id),
            "Turniej został rozpoczęty.",
            "/panel-sedziego/turnieje/" + id + "/rundy",
            redirectAttributes
        );
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/rundy/swiss/nastepna")
    String nextSwissRound(@PathVariable String id, RedirectAttributes redirectAttributes) {
        return action(
            () -> engineService.generateNextSwissRound(CurrentUser.require(), id),
            "Utworzono kolejną rundę.",
            "/panel-sedziego/turnieje/" + id + "/rundy",
            redirectAttributes
        );
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/rundy/{roundId}/zakoncz")
    String completeRound(
        @PathVariable String id,
        @PathVariable String roundId,
        RedirectAttributes redirectAttributes
    ) {
        return action(
            () -> engineService.completeRound(CurrentUser.require(), id, roundId),
            "Runda została zakończona.",
            "/panel-sedziego/turnieje/" + id + "/rundy",
            redirectAttributes
        );
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/partie/{gameId}/wynik")
    String enterResult(
        @PathVariable String id,
        @PathVariable String gameId,
        @RequestParam GameResult result,
        @RequestParam Integer roundNumber,
        RedirectAttributes redirectAttributes
    ) {
        return action(
            () -> engineService.enterResult(CurrentUser.require(), id, gameId, result),
            "Wynik został zapisany.",
            "/panel-sedziego/turnieje/" + id + "/rundy/" + roundNumber,
            redirectAttributes
        );
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/zakoncz")
    String finish(@PathVariable String id, RedirectAttributes redirectAttributes) {
        return action(
            () -> engineService.finishTournament(CurrentUser.require(), id, false),
            "Turniej został zakończony.",
            "/panel-sedziego/turnieje/" + id + "/rundy",
            redirectAttributes
        );
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/zakoncz-wczesniej")
    String finishEarly(@PathVariable String id, RedirectAttributes redirectAttributes) {
        return action(
            () -> engineService.finishTournament(CurrentUser.require(), id, true),
            "Turniej został zakończony.",
            "/panel-sedziego/turnieje/" + id + "/rundy",
            redirectAttributes
        );
    }

    @GetMapping({
        "/panel-sedziego/kojarzenia/{id}",
        "/panel-sedziego/turnieje/{id}/kojarzenia"
    })
    @Transactional
    String pairings(@PathVariable String id, Model model) {
        Tournament tournament = managed(id);
        List<RegistrationResponse> participants = registrationService.activeTournamentRegistrations(id)
            .stream().map(RegistrationResponse::from).toList();
        List<RoundResponse> rounds = roundRepository.findByTournament_IdOrderByRoundNumberAsc(id)
            .stream().map(RoundResponse::from).toList();
        List<StandingResponse> standings = switch (tournament.getStatus()) {
            case IN_PROGRESS, FINISHED -> standingService.recalculate(tournament).stream().map(StandingResponse::from).toList();
            default -> List.of();
        };
        RoundResponse lastRound = rounds.isEmpty() ? null : rounds.get(rounds.size() - 1);
        int nextRoundNumber = lastRound == null ? 1
            : lastRound.roundNumber() + (lastRound.status() == RoundStatus.COMPLETED ? 1 : 0);
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("participants", participants);
        model.addAttribute("rounds", rounds);
        model.addAttribute("standings", standings);
        model.addAttribute("nextRoundNumber", nextRoundNumber);
        return "judge/pairings";
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/uczestnicy/recznie")
    String manualParticipant(
        @PathVariable String id,
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam String clubOrCity,
        @RequestParam Integer rating,
        @RequestParam(required = false) String chessCategory,
        @RequestParam(required = false) Integer birthYear,
        RedirectAttributes redirectAttributes
    ) {
        return action(
            () -> engineService.addManualParticipant(CurrentUser.require(), id, new ManualParticipantRequest(
                firstName, lastName, clubOrCity, rating, chessCategory, birthYear
            )),
            "Zawodnik został dodany.",
            "/panel-sedziego/kojarzenia/" + id,
            redirectAttributes
        );
    }

    @PostMapping("/panel-sedziego/turnieje/{id}/kojarzenia/recznie")
    String manualPairing(
        @PathVariable String id,
        @RequestParam Integer roundNumber,
        @RequestParam Integer boardNumber,
        @RequestParam String whiteRegistrationId,
        @RequestParam(required = false) String blackRegistrationId,
        RedirectAttributes redirectAttributes
    ) {
        return action(
            () -> engineService.addManualPairing(CurrentUser.require(), id, new ManualPairingRequest(
                roundNumber, boardNumber, whiteRegistrationId, blackRegistrationId
            )),
            "Kojarzenie zostało dodane.",
            "/panel-sedziego/kojarzenia/" + id,
            redirectAttributes
        );
    }

    @GetMapping({
        "/panel-sedziego/turnieje/{id}/wyniki",
        "/panel-sedziego/turnieje/{id}/tabela"
    })
    @Transactional
    String results(@PathVariable String id, Model model) {
        Tournament tournament = managed(id);
        List<StandingResponse> standings = standingService.recalculate(tournament).stream()
            .map(StandingResponse::from)
            .toList();
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("standings", standings);
        model.addAttribute("rounds", roundRepository.findByTournament_IdOrderByRoundNumberAsc(id)
            .stream().map(RoundResponse::from).toList());
        return "judge/results";
    }

    private Tournament managed(String id) {
        return tournamentService.requireManagedTournament(CurrentUser.require(), id);
    }

    private String action(Runnable action, String success, String redirect, RedirectAttributes attributes) {
        try {
            action.run();
            attributes.addFlashAttribute("success", success);
        } catch (ApiException exception) {
            attributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:" + redirect;
    }
}
