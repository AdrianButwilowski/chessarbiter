package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.dto.profile.ProfileRequest;
import pl.chessarbiter.dto.profile.ProfileResponse;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.ProfileService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Transactional(readOnly = true)
    public ProfileResponse get() {
        return ProfileResponse.from(profileService.getProfile(CurrentUser.require()));
    }

    @PutMapping
    @Transactional
    public ProfileResponse update(@Valid @RequestBody ProfileRequest request) {
        return ProfileResponse.from(profileService.upsertProfile(CurrentUser.require(), request));
    }
}
