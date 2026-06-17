package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.chessarbiter.dto.profile.ProfileRequest;
import pl.chessarbiter.dto.profile.ProfileResponse;
import pl.chessarbiter.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping
    public ProfileResponse get() { return profileService.getMyProfile(); }

    @PutMapping
    public ProfileResponse update(@Valid @RequestBody ProfileRequest request) {
        return profileService.updateMyProfile(request);
    }
}
