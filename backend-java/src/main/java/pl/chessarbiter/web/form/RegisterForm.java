package pl.chessarbiter.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pl.chessarbiter.dto.auth.RegisterRequest;

@Getter
@Setter
public class RegisterForm {

    @NotBlank(message = "Podaj adres e-mail.")
    @Email(message = "Podaj poprawny adres e-mail.")
    private String email;

    @Size(max = 120, message = "Nazwa może mieć maksymalnie 120 znaków.")
    private String name;

    @NotBlank(message = "Podaj hasło.")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków.")
    private String password;

    @NotBlank(message = "Powtórz hasło.")
    private String confirmPassword;

    public RegisterRequest toRequest() {
        return new RegisterRequest(email, name, password, confirmPassword);
    }
}
