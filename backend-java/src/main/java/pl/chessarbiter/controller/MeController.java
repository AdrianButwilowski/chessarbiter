package pl.chessarbiter.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.RegistrationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MeController {

    private final RegistrationService registrationService;

    @GetMapping("/registrations")
    @Transactional(readOnly = true)
    public List<RegistrationResponse> registrations() {
        return registrationService.myRegistrations(CurrentUser.require())
            .stream()
            .map(RegistrationResponse::from)
            .toList();
    }

    @PostMapping("/registrations/{registrationId}/cancel")
    @Transactional
    public RegistrationResponse cancelRegistration(@PathVariable String registrationId) {
        return RegistrationResponse.from(registrationService.cancelOwn(CurrentUser.require(), registrationId));
    }
}
