package pl.chessarbiter.dto.player;

import java.util.List;
import pl.chessarbiter.entity.RegistrationStatus;

public record PlayerDetailResponse(
    String registrationId,
    String firstName,
    String lastName,
    String email,
    String clubOrCity,
    String federation,
    Integer rating,
    String chessCategory,
    Integer birthYear,
    RegistrationStatus status,
    Integer startNumber,
    Double points,
    Integer rank,
    String phoneNumber,
    String notes,
    List<PlayerGameResponse> games
) {
}
