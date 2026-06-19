package pl.chessarbiter.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.exception.ApiException;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.security.SecurityUser;
import pl.chessarbiter.service.ProfileService;
import pl.chessarbiter.service.RegistrationService;
import pl.chessarbiter.web.form.ProfileForm;

@Controller
@RequiredArgsConstructor
public class AccountViewController {

    private final ProfileService profileService;
    private final RegistrationService registrationService;

    @GetMapping("/profil")
    @Transactional(readOnly = true)
    String profile(Model model) {
        SecurityUser currentUser = CurrentUser.require();
        ProfileForm form = profileService.findProfile(currentUser)
            .map(ProfileForm::from)
            .orElseGet(() -> emptyProfile(currentUser));
        model.addAttribute("profileForm", form);
        return "account/profile";
    }

    @PostMapping("/profil")
    @Transactional
    String saveProfile(
        @Valid @ModelAttribute("profileForm") ProfileForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "account/profile";
        }
        profileService.upsertProfile(CurrentUser.require(), form.toRequest());
        redirectAttributes.addFlashAttribute("success", "Profil został zapisany.");
        return "redirect:/profil";
    }

    @GetMapping("/moje-zgloszenia")
    @Transactional(readOnly = true)
    String registrations(Model model) {
        model.addAttribute("registrations", registrationService.myRegistrations(CurrentUser.require())
            .stream()
            .map(RegistrationResponse::from)
            .toList());
        return "account/registrations";
    }

    @PostMapping("/moje-zgloszenia/{registrationId}/anuluj")
    String cancel(@PathVariable String registrationId, RedirectAttributes redirectAttributes) {
        try {
            registrationService.cancelOwn(CurrentUser.require(), registrationId);
            redirectAttributes.addFlashAttribute("success", "Zgłoszenie zostało anulowane.");
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/moje-zgloszenia";
    }

    private ProfileForm emptyProfile(SecurityUser currentUser) {
        ProfileForm form = new ProfileForm();
        form.setEmail(currentUser.getEmail());
        return form;
    }
}
