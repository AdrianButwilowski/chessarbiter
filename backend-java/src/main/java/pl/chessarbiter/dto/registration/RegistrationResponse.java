package pl.chessarbiter.dto.registration;

import java.time.Instant;
import pl.chessarbiter.dto.tournament.TournamentSummaryResponse;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.TournamentRegistration;

public record RegistrationResponse(
    String id,
    String tournamentId,
    String userId,
    String firstName,
    String lastName,
    String email,
    String clubOrCity,
    String federation,
    Integer rating,
    String chessCategory,
    String phoneNumber,
    Integer birthYear,
    String notes,
    RegistrationStatus status,
    Integer seedNumber,
    Integer startNumber,
    Integer finalRank,
    Instant createdAt,
    TournamentSummaryResponse tournament
) {

    public static RegistrationResponse from(TournamentRegistration registration) {
        return new RegistrationResponse(
            registration.getId(),
            registration.getTournament().getId(),
            registration.getUser() == null ? null : registration.getUser().getId(),
            registration.getFirstName(),
            registration.getLastName(),
            registration.getEmail(),
            registration.getClubOrCity(),
            registration.getFederation(),
            registration.getRating(),
            registration.getChessCategory(),
            registration.getPhoneNumber(),
            registration.getBirthYear(),
            registration.getNotes(),
            registration.getStatus(),
            registration.getSeedNumber(),
            registration.getStartNumber(),
            registration.getFinalRank(),
            registration.getCreatedAt(),
            TournamentSummaryResponse.from(registration.getTournament())
        );
    }
}
