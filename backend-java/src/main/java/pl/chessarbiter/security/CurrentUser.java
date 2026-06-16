package pl.chessarbiter.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.chessarbiter.exception.UnauthorizedException;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static SecurityUser require() {
        return optional().orElseThrow(() -> new UnauthorizedException("Authentication is required."));
    }

    public static Optional<SecurityUser> optional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (principal instanceof SecurityUser user) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}
