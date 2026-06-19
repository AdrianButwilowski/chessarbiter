package pl.chessarbiter.dto.registration;

import jakarta.validation.constraints.NotNull;
import pl.chessarbiter.entity.RegistrationStatus;

public record RegistrationStatusRequest(@NotNull RegistrationStatus status) {
}
