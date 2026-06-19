package pl.chessarbiter.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.dto.auth.LoginRequest;
import pl.chessarbiter.dto.auth.RegisterRequest;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.exception.BadRequestException;
import pl.chessarbiter.exception.ConflictException;
import pl.chessarbiter.exception.UnauthorizedException;
import pl.chessarbiter.repository.UserRepository;
import pl.chessarbiter.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords must match.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Account with this email already exists.");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(blankToNull(request.name()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.PLAYER);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
            .filter(candidate -> candidate.getDeletedAt() == null)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User currentUser(SecurityUser securityUser) {
        return userRepository.findByIdAndDeletedAtIsNull(securityUser.getId())
            .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
