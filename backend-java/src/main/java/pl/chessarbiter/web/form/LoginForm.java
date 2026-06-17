package pl.chessarbiter.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import pl.chessarbiter.dto.auth.LoginRequest;

@Getter
@Setter
public class LoginForm {

    @NotBlank(message = "Podaj adres e-mail.")
    @Email(message = "Podaj poprawny adres e-mail.")
    private String email;

    @NotBlank(message = "Podaj hasło.")
    private String password;

    public LoginRequest toRequest() {
        return new LoginRequest(email, password);
    }
}
