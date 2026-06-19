package pl.chessarbiter.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pl.chessarbiter.dto.profile.ProfileRequest;
import pl.chessarbiter.entity.PlayerProfile;

@Getter
@Setter
public class ProfileForm {

    @NotBlank(message = "Podaj imię.")
    @Size(max = 80)
    private String firstName;

    @NotBlank(message = "Podaj nazwisko.")
    @Size(max = 80)
    private String lastName;

    @NotBlank(message = "Podaj adres e-mail.")
    @Email(message = "Podaj poprawny adres e-mail.")
    private String email;

    @NotBlank(message = "Podaj klub lub miasto.")
    @Size(max = 120)
    private String clubOrCity;

    private String federation;

    @Min(0)
    @Max(4000)
    private Integer classicalRating;

    @Min(0)
    @Max(4000)
    private Integer rapidRating;

    @Min(0)
    @Max(4000)
    private Integer blitzRating;

    @Size(max = 30)
    private String chessCategory;

    private String phoneNumber;

    @Min(1900)
    @Max(2100)
    private Integer birthYear;

    public ProfileRequest toRequest() {
        return new ProfileRequest(
            firstName,
            lastName,
            email,
            clubOrCity,
            federation,
            classicalRating,
            rapidRating,
            blitzRating,
            chessCategory,
            phoneNumber,
            birthYear
        );
    }

    public static ProfileForm from(PlayerProfile profile) {
        ProfileForm form = new ProfileForm();
        form.firstName = profile.getFirstName();
        form.lastName = profile.getLastName();
        form.email = profile.getEmail();
        form.clubOrCity = profile.getClubOrCity();
        form.federation = profile.getFederation();
        form.classicalRating = profile.getClassicalRating();
        form.rapidRating = profile.getRapidRating();
        form.blitzRating = profile.getBlitzRating();
        form.chessCategory = profile.getChessCategory();
        form.phoneNumber = profile.getPhoneNumber();
        form.birthYear = profile.getBirthYear();
        return form;
    }
}
