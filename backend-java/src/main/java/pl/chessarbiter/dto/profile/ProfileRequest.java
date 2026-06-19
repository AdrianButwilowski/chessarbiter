package pl.chessarbiter.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
    @NotBlank @Size(max = 80) String firstName,
    @NotBlank @Size(max = 80) String lastName,
    @NotBlank @Email String email,
    @NotBlank @Size(max = 120) String clubOrCity,
    String federation,
    @Min(0) @Max(4000) Integer classicalRating,
    @Min(0) @Max(4000) Integer rapidRating,
    @Min(0) @Max(4000) Integer blitzRating,
    @Size(max = 30) String chessCategory,
    String phoneNumber,
    @Min(1900) @Max(2100) Integer birthYear
) {
}
