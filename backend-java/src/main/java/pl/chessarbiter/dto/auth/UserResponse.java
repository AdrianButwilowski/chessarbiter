package pl.chessarbiter.dto.auth;

import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;

public record UserResponse(
    String id,
    String email,
    String name,
    UserRole role
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
