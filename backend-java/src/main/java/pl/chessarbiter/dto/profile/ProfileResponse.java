package pl.chessarbiter.dto.profile;

import pl.chessarbiter.entity.PlayerProfile;

public record ProfileResponse(
    String id,
    String userId,
    String firstName,
    String lastName,
    String email,
    String clubOrCity,
    String federation,
    Integer classicalRating,
    Integer rapidRating,
    Integer blitzRating,
    String chessCategory,
    String phoneNumber,
    Integer birthYear
) {

    public static ProfileResponse from(PlayerProfile profile) {
        return new ProfileResponse(
            profile.getId(),
            profile.getUser().getId(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getEmail(),
            profile.getClubOrCity(),
            profile.getFederation(),
            profile.getClassicalRating(),
            profile.getRapidRating(),
            profile.getBlitzRating(),
            profile.getChessCategory(),
            profile.getPhoneNumber(),
            profile.getBirthYear()
        );
    }
}
