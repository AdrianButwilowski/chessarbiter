package pl.chessarbiter.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email,
    @Size(max = 120) String name,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String confirmPassword
) {
}
