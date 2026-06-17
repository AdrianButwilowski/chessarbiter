package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.chessarbiter.dto.registration.RegistrationRequest;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.service.RegistrationService;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments/{slug}/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @GetMapping
    public List<RegistrationResponse> list(@PathVariable String slug) {
        return registrationService.getForTournament(slug);
    }

    @PostMapping
    public RegistrationResponse register(@PathVariable String slug, @Valid @RequestBody RegistrationRequest request) {
        return registrationService.register(slug, request);
    }
}
