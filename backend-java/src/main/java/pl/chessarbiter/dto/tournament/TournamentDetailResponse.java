package pl.chessarbiter.dto.tournament;

import java.time.Instant;
import pl.chessarbiter.entity.TimeControlType;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;

public record TournamentDetailResponse(
    String id,
    String title,
    String slug,
    String description,
    String location,
    String city,
    Instant startDate,
    Instant endDate,
    Instant registrationDeadline,
    String organizer,
    String contactEmail,
    String contactPhone,
    TournamentType tournamentType,
    TimeControlType timeControlType,
    String timeControlDescription,
    Integer rounds,
    Integer maxPlayers,
    String entryFee,
    String regulationsUrl,
    TournamentStatus status,
    TournamentDisplayStatus displayStatus,
    boolean registrationOpen,
    boolean allowPlayerCancellation,
    boolean showRegisteredPlayers,
    String createdById
) {

    public static TournamentDetailResponse from(Tournament tournament) {
        return new TournamentDetailResponse(
            tournament.getId(),
            tournament.getTitle(),
            tournament.getSlug(),
            tournament.getDescription(),
            tournament.getLocation(),
            tournament.getCity(),
            tournament.getStartDate(),
            tournament.getEndDate(),
            tournament.getRegistrationDeadline(),
            tournament.getOrganizer(),
            tournament.getContactEmail(),
            tournament.getContactPhone(),
            tournament.getTournamentType(),
            tournament.getTimeControlType(),
            tournament.getTimeControlDescription(),
            tournament.getRounds(),
            tournament.getMaxPlayers(),
            tournament.getEntryFee(),
            tournament.getRegulationsUrl(),
            tournament.getStatus(),
            TournamentDisplayStatusResolver.resolve(
                tournament.getStatus(),
                tournament.getStartDate(),
                tournament.getEndDate()
            ),
            tournament.isRegistrationOpen(),
            tournament.isAllowPlayerCancellation(),
            tournament.isShowRegisteredPlayers(),
            tournament.getCreatedBy().getId()
        );
    }
}
