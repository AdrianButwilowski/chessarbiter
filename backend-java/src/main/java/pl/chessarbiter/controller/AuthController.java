package pl.chessarbiter.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.chessarbiter.config.AppSecurityProperties;
import pl.chessarbiter.dto.auth.*;
import pl.chessarbiter.dto.common.MessageResponse;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.UserRepository;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final AppSecurityProperties securityProps;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse res = authService.register(request);
        var user = userRepository.findByEmailIgnoreCase(request.email()).orElseThrow();
        setCookie(response, authService.generateToken(user));
        return res;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse res = authService.login(request);
        var user = userRepository.findByEmailIgnoreCase(request.email()).orElseThrow();
        setCookie(response, authService.generateToken(user));
        return res;
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(securityProps.getCookieName(), "");
        cookie.setHttpOnly(true);
        cookie.setSecure(securityProps.isCookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return new MessageResponse("Logged out");
    }

    @GetMapping("/me")
    public UserResponse me() {
        var user = CurrentUser.require();
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), UserRole.valueOf(user.getRole()));
    }

    private void setCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(securityProps.getCookieName(), token);
        cookie.setHttpOnly(true);
        cookie.setSecure(securityProps.isCookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge((int) securityProps.getJwtExpiration().getSeconds());
        response.addCookie(cookie);
    }
}
