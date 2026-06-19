package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.config.AppSecurityProperties;
import pl.chessarbiter.dto.auth.AuthResponse;
import pl.chessarbiter.dto.auth.LoginRequest;
import pl.chessarbiter.dto.auth.RegisterRequest;
import pl.chessarbiter.dto.auth.UserResponse;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.security.JwtService;
import pl.chessarbiter.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AppSecurityProperties securityProperties;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return authenticatedResponse(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        return authenticatedResponse(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, expiredCookie().toString())
            .build();
    }

    @GetMapping("/me")
    public UserResponse me() {
        User user = authService.currentUser(CurrentUser.require());
        return UserResponse.from(user);
    }

    private ResponseEntity<AuthResponse> authenticatedResponse(User user) {
        String token = jwtService.createToken(user);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, sessionCookie(token).toString())
            .body(new AuthResponse(UserResponse.from(user)));
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
