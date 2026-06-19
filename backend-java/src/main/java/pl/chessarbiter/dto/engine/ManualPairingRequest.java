package pl.chessarbiter.dto.engine;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ManualPairingRequest(
    @NotNull @Min(1) Integer roundNumber,
    @NotNull @Min(1) Integer boardNumber,
    @NotBlank String whiteRegistrationId,
    String blackRegistrationId
) {
}
