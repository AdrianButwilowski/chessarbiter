package pl.chessarbiter.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.chessarbiter.config.AppSecurityProperties;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.exception.ApiException;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.security.JwtService;
import pl.chessarbiter.service.AuthService;
import pl.chessarbiter.web.form.LoginForm;
import pl.chessarbiter.web.form.RegisterForm;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AppSecurityProperties securityProperties;

    @GetMapping("/logowanie")
    String login(Model model) {
        var currentUser = CurrentUser.optional().orElse(null);
        if (currentUser != null) {
            return redirectFor(currentUser.getRole());
        }
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/logowanie")
    String login(
        @Valid @ModelAttribute("loginForm") LoginForm form,
        BindingResult bindingResult,
        HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        try {
            User user = authService.login(form.toRequest());
            response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(jwtService.createToken(user)).toString());
            return redirectFor(user);
        } catch (ApiException exception) {
            bindingResult.reject("login", "Nieprawidłowy e-mail lub hasło.");
            return "auth/login";
        }
    }

    @GetMapping("/rejestracja")
    String register(Model model) {
        if (CurrentUser.optional().isPresent()) {
            return "redirect:/profil";
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/rejestracja")
    String register(
        @Valid @ModelAttribute("registerForm") RegisterForm form,
        BindingResult bindingResult,
        HttpServletResponse response
    ) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "passwordMismatch", "Hasła muszą być takie same.");
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            User user = authService.register(form.toRequest());
            response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(jwtService.createToken(user)).toString());
            return "redirect:/profil";
        } catch (ApiException exception) {
            bindingResult.reject("register", exception.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("/wyloguj")
    String logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie().toString());
        return "redirect:/";
    }

    private String redirectFor(User user) {
        return redirectFor(user.getRole().name());
    }

    private String redirectFor(String role) {
        return switch (role) {
            case "ADMIN" -> "redirect:/panel-admina";
            case "ARBITER" -> "redirect:/panel-sedziego";
            default -> "redirect:/profil";
        };
    }

    private ResponseCookie sessionCookie(String token) {
        return ResponseCookie.from(securityProperties.getCookieName(), token)
            .httpOnly(true)
            .secure(securityProperties.isCookieSecure())
            .sameSite("Lax")
            .path("/")
            .maxAge(securityProperties.getJwtExpiration())
            .build();
    }

    private ResponseCookie expiredCookie() {
        return ResponseCookie.from(securityProperties.getCookieName(), "")
            .httpOnly(true)
            .secure(securityProperties.isCookieSecure())
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();
    }
}
