package pl.chessarbiter.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.dto.registration.RegistrationRequest;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentRegistration;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.exception.BadRequestException;
import pl.chessarbiter.exception.ConflictException;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.repository.TournamentRepository;
import pl.chessarbiter.repository.UserRepository;
import pl.chessarbiter.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final TournamentService tournamentService;

    @Transactional
    public TournamentRegistration register(String tournamentId, RegistrationRequest request, SecurityUser currentUser) {
        Tournament tournament = tournamentRepository.findByIdAndDeletedAtIsNull(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found."));
        if (!canCreateRegistration(tournament)) {
            throw new BadRequestException("Registration is closed for this tournament.");
        }

        String email = normalizeEmail(request.email());
        if (registrationRepository.existsByTournament_IdAndEmailIgnoreCase(tournament.getId(), email)) {
            throw new ConflictException("Registration for this email already exists.");
        }
        if (currentUser != null && registrationRepository.existsByTournament_IdAndUser_Id(tournament.getId(), currentUser.getId())) {
            throw new ConflictException("Registration for this account already exists.");
        }

        User user = currentUser == null
            ? null
            : userRepository.findByIdAndDeletedAtIsNull(currentUser.getId()).orElse(null);
        long activeCount = registrationRepository.countByTournament_IdAndStatus(tournament.getId(), RegistrationStatus.REGISTERED);

        TournamentRegistration registration = new TournamentRegistration();
        registration.setTournament(tournament);
        registration.setUser(user);
        registration.setFirstName(trim(request.firstName()));
        registration.setLastName(trim(request.lastName()));
        registration.setEmail(email);
        registration.setClubOrCity(trim(request.clubOrCity()));
        registration.setFederation(blankToNull(request.federation()));
        registration.setRating(request.rating());
        registration.setChessCategory(defaultCategory(request.chessCategory()));
        registration.setPhoneNumber(blankToNull(request.phoneNumber()));
        registration.setBirthYear(request.birthYear());
        registration.setNotes(blankToNull(request.notes()));
        registration.setStatus(initialStatus(tournament, activeCount));

        return registrationRepository.save(registration);
    }

    @Transactional(readOnly = true)
    public List<TournamentRegistration> myRegistrations(SecurityUser currentUser) {
        return registrationRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.getId());
    }

    @Transactional
    public TournamentRegistration cancelOwn(SecurityUser currentUser, String registrationId) {
        TournamentRegistration registration = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new NotFoundException("Registration not found."));
        if (registration.getUser() == null || !registration.getUser().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Registration not found.");
        }
        if (canCancel(registration)) {
            registration.setStatus(RegistrationStatus.CANCELLED);
        }
        return registrationRepository.save(registration);
    }

    @Transactional(readOnly = true)
    public List<TournamentRegistration> managedRegistrations(SecurityUser currentUser, String tournamentId) {
        tournamentService.requireManagedTournament(currentUser, tournamentId);
        return registrationRepository.findByTournament_IdOrderByStatusAscCreatedAtAsc(tournamentId);
    }

    @Transactional(readOnly = true)
    public List<TournamentRegistration> activeTournamentRegistrations(String tournamentId) {
        return registrationRepository.findByTournament_IdAndStatusOrderByStartNumberAscRatingDescLastNameAscFirstNameAsc(
            tournamentId,
            RegistrationStatus.REGISTERED
        );
    }

    @Transactional
    public TournamentRegistration updateStatus(SecurityUser currentUser, String tournamentId, String registrationId, RegistrationStatus status) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        if (tournament.getStatus() == TournamentStatus.FINISHED) {
            throw new BadRequestException("Operations on registrations are disabled for finished tournaments.");
        }
        TournamentRegistration registration = registrationRepository.findByIdAndTournament_Id(registrationId, tournamentId)
            .orElseThrow(() -> new NotFoundException("Registration not found."));
        registration.setStatus(status);
        return registrationRepository.save(registration);
    }

    private boolean canCreateRegistration(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.PUBLISHED || !tournament.isRegistrationOpen()) {
            return false;
        }
        return tournament.getRegistrationDeadline() == null || !tournament.getRegistrationDeadline().isBefore(Instant.now());
    }

    private RegistrationStatus initialStatus(Tournament tournament, long activeCount) {
        if (tournament.getMaxPlayers() != null && activeCount >= tournament.getMaxPlayers()) {
            return RegistrationStatus.WAITLIST;
        }
        return RegistrationStatus.REGISTERED;
    }

    private boolean canCancel(TournamentRegistration registration) {
        Tournament tournament = registration.getTournament();
        return tournament.isAllowPlayerCancellation()
            && tournament.getStartDate().isAfter(Instant.now())
            && (registration.getStatus() == RegistrationStatus.REGISTERED || registration.getStatus() == RegistrationStatus.WAITLIST);
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
