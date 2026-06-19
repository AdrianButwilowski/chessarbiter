package pl.chessarbiter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.chessarbiter.config.AppSecurityProperties;
import pl.chessarbiter.entity.User;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppSecurityProperties properties;
    private SecretKey key;

    @PostConstruct
    void init() {
        String secret = properties.getJwtSecret();
        // Fail fast instead of signing tokens with a predictable local placeholder.
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("AUTH_SECRET or SESSION_SECRET must contain at least 32 characters.");
        }
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getJwtExpiration());

        return Jwts.builder()
            .subject(user.getId())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact();
    }

    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }
}
