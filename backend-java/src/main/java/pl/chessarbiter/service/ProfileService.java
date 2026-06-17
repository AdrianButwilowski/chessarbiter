package pl.chessarbiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.chessarbiter.dto.profile.ProfileRequest;
import pl.chessarbiter.dto.profile.ProfileResponse;
import pl.chessarbiter.entity.PlayerProfile;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.PlayerProfileRepository;
import pl.chessarbiter.security.CurrentUser;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final PlayerProfileRepository profileRepo;

    public ProfileResponse getMyProfile() {
        String userId = CurrentUser.require().getId();
        PlayerProfile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        return ProfileResponse.from(profile);
    }

    public ProfileResponse updateMyProfile(ProfileRequest request) {
        String userId = CurrentUser.require().getId();
        PlayerProfile profile = profileRepo.findByUser_Id(userId)
                .orElseGet(() -> PlayerProfile.builder().id(UUID.randomUUID().toString())
                        .user(User.builder().id(userId).build()).build());
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setEmail(request.email());
        profile.setClubOrCity(request.clubOrCity());
        profile.setFederation(request.federation());
        profile.setClassicalRating(request.classicalRating());
        profile.setRapidRating(request.rapidRating());
        profile.setBlitzRating(request.blitzRating());
        profile.setChessCategory(request.chessCategory());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setBirthYear(request.birthYear());
        profile = profileRepo.save(profile);
        return ProfileResponse.from(profile);
    }
}
