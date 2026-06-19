package pl.chessarbiter.config;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeedConfig.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Bean
    CommandLineRunner seedAdminUser() {
        return args -> {
            String email = normalize(environment.getProperty("ADMIN_EMAIL"));
            String password = environment.getProperty("ADMIN_PASSWORD");

            if (email == null || password == null || password.isBlank()) {
                logger.info("ADMIN_EMAIL or ADMIN_PASSWORD not set. Skipping administrator seed.");
                return;
            }

            if (userRepository.countByRole(UserRole.ADMIN) > 0) {
                logger.info("Administrator already exists. Skipping administrator seed.");
                return;
            }

            userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
                throw new IllegalStateException("ADMIN_EMAIL already belongs to a non-admin user.");
            });

            User admin = new User();
            admin.setEmail(email);
            admin.setName("Administrator");
            admin.setRole(UserRole.ADMIN);
            admin.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(admin);
            logger.info("Administrator created: {}", email);
        };
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
