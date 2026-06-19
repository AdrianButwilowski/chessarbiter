package pl.chessarbiter.web;

import jakarta.validation.Valid;
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
import pl.chessarbiter.dto.admin.AdminUserResponse;
import pl.chessarbiter.dto.tournament.TournamentSummaryResponse;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.exception.ApiException;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.AdminService;
import pl.chessarbiter.service.TournamentService;
import pl.chessarbiter.web.form.TournamentForm;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    private final AdminService adminService;
    private final TournamentService tournamentService;

    @GetMapping("/panel-admina")
    String dashboard(Model model) {
        model.addAttribute("stats", adminService.stats());
        return "admin/dashboard";
    }

    @GetMapping("/panel-admina/uzytkownicy")
    @Transactional(readOnly = true)
    String users(Model model) {
        model.addAttribute("users", adminService.users().stream().map(AdminUserResponse::from).toList());
        return "admin/users";
    }

    @PostMapping("/panel-admina/uzytkownicy/{id}/rola")
    String role(
        @PathVariable String id,
        @RequestParam UserRole role,
        RedirectAttributes attributes
    ) {
        try {
            adminService.changeRole(id, role);
            attributes.addFlashAttribute("success", "Rola użytkownika została zmieniona.");
        } catch (ApiException exception) {
            attributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/panel-admina/uzytkownicy";
    }

    @PostMapping("/panel-admina/uzytkownicy/{id}/usun")
    String delete(@PathVariable String id, RedirectAttributes attributes) {
        try {
            adminService.deleteUser(id);
            attributes.addFlashAttribute("success", "Użytkownik został usunięty.");
        } catch (ApiException exception) {
            attributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/panel-admina/uzytkownicy";
    }

    @GetMapping("/panel-admina/turnieje")
    @Transactional(readOnly = true)
    String tournaments(Model model) {
        model.addAttribute("tournaments", tournamentService.listManagedTournaments(CurrentUser.require())
            .stream().map(TournamentSummaryResponse::from).toList());
        return "admin/tournaments";
    }

    @GetMapping("/panel-admina/turnieje/nowy")
    String newTournament(Model model) {
        model.addAttribute("tournamentForm", new TournamentForm());
        model.addAttribute("editing", false);
        model.addAttribute("adminMode", true);
        return "judge/tournament-form";
    }

    @PostMapping("/panel-admina/turnieje/nowy")
    @Transactional
    String createTournament(
        @Valid @ModelAttribute("tournamentForm") TournamentForm form,
        BindingResult bindingResult,
        @RequestParam(defaultValue = "save") String intent,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        form.setStatus("publish".equals(intent) ? TournamentStatus.PUBLISHED : TournamentStatus.DRAFT);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            model.addAttribute("adminMode", true);
            return "judge/tournament-form";
        }
        try {
            tournamentService.createTournament(CurrentUser.require(), form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Turniej został utworzony.");
            return "redirect:/panel-admina/turnieje";
        } catch (ApiException exception) {
            bindingResult.reject("tournament", exception.getMessage());
            model.addAttribute("editing", false);
            model.addAttribute("adminMode", true);
            return "judge/tournament-form";
        }
    }
}
