package pl.chessarbiter.dto.admin;

import java.time.Instant;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;

public record AdminUserResponse(
    String id,
    String email,
    String name,
    UserRole role,
    Instant createdAt
) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
