package pl.chessarbiter.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import pl.chessarbiter.config.AppSecurityProperties;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {
    private final AppSecurityProperties props;

    public JwtService(AppSecurityProperties props) {
        this.props = props;
    }

    public String generateToken(String userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(props.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.getJwtExpiration())))
                .signWith(key)
                .compact();
    }

    public String validateAndGetUserId(String token) {
        SecretKey key = Keys.hmacShaKeyFor(props.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}
