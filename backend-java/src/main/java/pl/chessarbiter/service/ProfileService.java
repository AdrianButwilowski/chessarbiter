package pl.chessarbiter.service;

import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.dto.profile.ProfileRequest;
import pl.chessarbiter.entity.PlayerProfile;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.PlayerProfileRepository;
import pl.chessarbiter.repository.UserRepository;
import pl.chessarbiter.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PlayerProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<PlayerProfile> findProfile(SecurityUser currentUser) {
        return profileRepository.findByUser_Id(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public PlayerProfile getProfile(SecurityUser currentUser) {
        return findProfile(currentUser)
            .orElseThrow(() -> new NotFoundException("Profile not found."));
    }

    @Transactional
    public PlayerProfile upsertProfile(SecurityUser currentUser, ProfileRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(currentUser.getId())
            .orElseThrow(() -> new NotFoundException("User not found."));

        PlayerProfile profile = profileRepository.findByUser_Id(user.getId()).orElseGet(PlayerProfile::new);
        profile.setUser(user);
        profile.setFirstName(trim(request.firstName()));
        profile.setLastName(trim(request.lastName()));
        profile.setEmail(normalizeEmail(request.email()));
        profile.setClubOrCity(trim(request.clubOrCity()));
        profile.setFederation(blankToNull(request.federation()));
        profile.setClassicalRating(request.classicalRating());
        profile.setRapidRating(request.rapidRating());
        profile.setBlitzRating(request.blitzRating());
        profile.setChessCategory(defaultCategory(request.chessCategory()));
        profile.setPhoneNumber(blankToNull(request.phoneNumber()));
        profile.setBirthYear(request.birthYear());

        return profileRepository.save(profile);
    }

    private String normalizeEmail(String email) {
        return trim(email).toLowerCase(Locale.ROOT);
    }

    private String defaultCategory(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? "NONE" : normalized;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
