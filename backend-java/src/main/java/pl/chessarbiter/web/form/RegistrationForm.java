package pl.chessarbiter.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pl.chessarbiter.dto.registration.RegistrationRequest;

@Getter
@Setter
public class RegistrationForm {

    @NotBlank(message = "Podaj imię.")
    @Size(max = 80, message = "Imię może mieć maksymalnie 80 znaków.")
    private String firstName;

    @NotBlank(message = "Podaj nazwisko.")
    @Size(max = 80, message = "Nazwisko może mieć maksymalnie 80 znaków.")
    private String lastName;

    @NotBlank(message = "Podaj adres e-mail.")
    @Email(message = "Podaj poprawny adres e-mail.")
    private String email;

    @NotBlank(message = "Podaj klub lub miasto.")
    @Size(max = 120, message = "Wartość może mieć maksymalnie 120 znaków.")
    private String clubOrCity;

    private String federation;

    @NotNull(message = "Podaj ranking.")
    @Min(value = 0, message = "Ranking nie może być ujemny.")
    @Max(value = 4000, message = "Ranking nie może przekraczać 4000.")
    private Integer rating;

    @Size(max = 30, message = "Kategoria może mieć maksymalnie 30 znaków.")
    private String chessCategory;

    private String phoneNumber;

    @Min(value = 1900, message = "Podaj poprawny rok urodzenia.")
    @Max(value = 2100, message = "Podaj poprawny rok urodzenia.")
    private Integer birthYear;

    private String notes;

    public RegistrationRequest toRequest() {
        return new RegistrationRequest(
            firstName,
            lastName,
            email,
            clubOrCity,
            federation,
            rating,
            chessCategory,
            phoneNumber,
            birthYear,
            notes
        );
    }
}
