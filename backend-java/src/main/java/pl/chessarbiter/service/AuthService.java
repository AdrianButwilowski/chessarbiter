package pl.chessarbiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.chessarbiter.dto.auth.*;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.exception.BadRequestException;
import pl.chessarbiter.exception.ConflictException;
import pl.chessarbiter.exception.UnauthorizedException;
import pl.chessarbiter.repository.UserRepository;
import pl.chessarbiter.security.JwtService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already registered");
        }
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(request.email())
                .name(request.name())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.PLAYER)
                .createdAt(Instant.now())
                .build();
        user = userRepository.save(user);
        return new AuthResponse(UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return new AuthResponse(UserResponse.from(user));
    }

    public String generateToken(User user) {
        return jwtService.generateToken(user.getId(), user.getRole().name());
    }
}
