package pl.chessarbiter.dto.tournament;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import pl.chessarbiter.entity.TimeControlType;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;

public record TournamentRequest(
    @NotBlank @Size(min = 3, max = 180) String title,
    @NotBlank @Size(min = 10) String description,
    @NotBlank @Size(max = 160) String location,
    @NotBlank @Size(max = 120) String city,
    @NotNull Instant startDate,
    Instant endDate,
    Instant registrationDeadline,
    @NotBlank @Size(max = 160) String organizer,
    @NotBlank @Email String contactEmail,
    String contactPhone,
    @NotNull TournamentType tournamentType,
    @NotNull TimeControlType timeControlType,
    @NotBlank @Size(max = 120) String timeControlDescription,
    @NotNull @Min(1) @Max(99) Integer rounds,
    @Min(1) Integer maxPlayers,
    String entryFee,
    String regulationsUrl,
    TournamentStatus status,
    Boolean registrationOpen,
    Boolean allowPlayerCancellation,
    Boolean showRegisteredPlayers
) {
}
