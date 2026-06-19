package pl.chessarbiter.dto.tournament;

import java.time.Instant;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.TimeControlType;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;

public record TournamentSummaryResponse(
    String id,
    String title,
    String slug,
    String city,
    String location,
    Instant startDate,
    Instant endDate,
    TournamentType tournamentType,
    TimeControlType timeControlType,
    TournamentStatus status,
    TournamentDisplayStatus displayStatus,
    boolean registrationOpen,
    Integer maxPlayers,
    boolean allowPlayerCancellation,
    String createdById,
    String createdByName,
    String createdByEmail,
    long registrationCount,
    long activeRegistrationCount,
    long roundCount,
    long gameCount,
    long standingCount
) {

    public static TournamentSummaryResponse from(Tournament tournament) {
        long activeRegistrationCount = tournament.getRegistrations().stream()
            .filter(registration -> registration.getStatus() != RegistrationStatus.CANCELLED)
            .count();

        return new TournamentSummaryResponse(
            tournament.getId(),
            tournament.getTitle(),
            tournament.getSlug(),
            tournament.getCity(),
            tournament.getLocation(),
            tournament.getStartDate(),
            tournament.getEndDate(),
            tournament.getTournamentType(),
            tournament.getTimeControlType(),
            tournament.getStatus(),
            TournamentDisplayStatusResolver.resolve(
                tournament.getStatus(),
                tournament.getStartDate(),
                tournament.getEndDate()
            ),
            tournament.isRegistrationOpen(),
            tournament.getMaxPlayers(),
            tournament.isAllowPlayerCancellation(),
            tournament.getCreatedBy().getId(),
            tournament.getCreatedBy().getName(),
            tournament.getCreatedBy().getEmail(),
            tournament.getRegistrations().size(),
            activeRegistrationCount,
            tournament.getTournamentRounds().size(),
            tournament.getGames().size(),
            tournament.getStandings().size()
        );
    }
}
