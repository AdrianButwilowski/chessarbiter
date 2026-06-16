package pl.chessarbiter.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private String cookieName = "chessarbiter_session";
    private boolean cookieSecure;
    private String jwtSecret;
    private Duration jwtExpiration = Duration.ofDays(7);
}
