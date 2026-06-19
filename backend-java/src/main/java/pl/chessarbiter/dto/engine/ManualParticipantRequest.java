package pl.chessarbiter.dto.engine;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ManualParticipantRequest(
    @NotBlank @Size(max = 80) String firstName,
    @NotBlank @Size(max = 80) String lastName,
    @NotBlank @Size(max = 120) String clubOrCity,
    @NotNull @Min(0) @Max(4000) Integer rating,
    @Size(max = 30) String chessCategory,
    @Min(1900) @Max(2100) Integer birthYear
) {
}
