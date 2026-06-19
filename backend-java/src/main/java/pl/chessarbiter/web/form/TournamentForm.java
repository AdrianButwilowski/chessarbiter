package pl.chessarbiter.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import pl.chessarbiter.dto.tournament.TournamentRequest;
import pl.chessarbiter.entity.TimeControlType;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;
import pl.chessarbiter.web.ViewSupport;

@Getter
@Setter
public class TournamentForm {

    @NotBlank(message = "Podaj nazwę turnieju.")
    @Size(min = 3, max = 180)
    private String title;

    @NotBlank(message = "Podaj opis turnieju.")
    @Size(min = 10, message = "Opis musi mieć co najmniej 10 znaków.")
    private String description;

    @NotBlank(message = "Podaj miejsce.")
    private String location;

    @NotBlank(message = "Podaj miasto.")
    private String city;

    @NotNull(message = "Podaj datę rozpoczęcia.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationDeadline;

    @NotBlank(message = "Podaj organizatora.")
    private String organizer;

    @NotBlank(message = "Podaj kontaktowy adres e-mail.")
    @Email(message = "Podaj poprawny adres e-mail.")
    private String contactEmail;

    private String contactPhone;

    @NotNull
    private TournamentType tournamentType = TournamentType.SWISS;

    @NotNull
    private TimeControlType timeControlType = TimeControlType.RAPID;

    @NotBlank(message = "Opisz tempo gry.")
    private String timeControlDescription;

    @NotNull
    @Min(1)
    @Max(99)
    private Integer rounds = 7;

    @Min(1)
    private Integer maxPlayers;

    private String entryFee;
    private String regulationsUrl;
    private TournamentStatus status = TournamentStatus.DRAFT;
    private boolean registrationOpen;
    private boolean allowPlayerCancellation = true;
    private boolean showRegisteredPlayers = true;

    public TournamentRequest toRequest() {
        return new TournamentRequest(
            title,
            description,
            location,
            city,
            instant(startDate),
            instant(endDate),
            instant(registrationDeadline),
            organizer,
            contactEmail,
            contactPhone,
            tournamentType,
            timeControlType,
            timeControlDescription,
            rounds,
            maxPlayers,
            entryFee,
            regulationsUrl,
            status,
            registrationOpen,
            allowPlayerCancellation,
            showRegisteredPlayers
        );
    }

    public static TournamentForm from(Tournament tournament) {
        TournamentForm form = new TournamentForm();
        form.title = tournament.getTitle();
        form.description = tournament.getDescription();
        form.location = tournament.getLocation();
        form.city = tournament.getCity();
        form.startDate = local(tournament.getStartDate());
        form.endDate = local(tournament.getEndDate());
        form.registrationDeadline = local(tournament.getRegistrationDeadline());
        form.organizer = tournament.getOrganizer();
        form.contactEmail = tournament.getContactEmail();
        form.contactPhone = tournament.getContactPhone();
        form.tournamentType = tournament.getTournamentType();
        form.timeControlType = tournament.getTimeControlType();
        form.timeControlDescription = tournament.getTimeControlDescription();
        form.rounds = tournament.getRounds();
        form.maxPlayers = tournament.getMaxPlayers();
        form.entryFee = tournament.getEntryFee();
        form.regulationsUrl = tournament.getRegulationsUrl();
        form.status = tournament.getStatus();
        form.registrationOpen = tournament.isRegistrationOpen();
        form.allowPlayerCancellation = tournament.isAllowPlayerCancellation();
        form.showRegisteredPlayers = tournament.isShowRegisteredPlayers();
        return form;
    }

    private static Instant instant(LocalDateTime value) {
        return value == null ? null : value.atZone(ViewSupport.WARSAW_ZONE).toInstant();
    }

    private static LocalDateTime local(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ViewSupport.WARSAW_ZONE);
    }
}
