package pl.chessarbiter.dto.admin;

import jakarta.validation.constraints.NotNull;
import pl.chessarbiter.entity.UserRole;

public record ChangeUserRoleRequest(@NotNull UserRole role) {
}
