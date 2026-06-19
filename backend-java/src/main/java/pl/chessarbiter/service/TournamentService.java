package pl.chessarbiter.service;

import java.text.Normalizer;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.dto.tournament.TournamentRequest;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.exception.BadRequestException;
import pl.chessarbiter.exception.ForbiddenException;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.TournamentRepository;
import pl.chessarbiter.repository.UserRepository;
import pl.chessarbiter.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private static final List<TournamentStatus> PUBLIC_STATUSES = List.of(
        TournamentStatus.PUBLISHED,
        TournamentStatus.REGISTRATION_CLOSED,
        TournamentStatus.IN_PROGRESS,
        TournamentStatus.FINISHED,
        TournamentStatus.CANCELLED
    );

    private static final Pattern NON_ASCII_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");
    private static final Map<TournamentStatus, List<TournamentStatus>> STATUS_TRANSITIONS = new EnumMap<>(TournamentStatus.class);

    static {
        STATUS_TRANSITIONS.put(TournamentStatus.DRAFT, List.of(TournamentStatus.PUBLISHED, TournamentStatus.CANCELLED));
        STATUS_TRANSITIONS.put(TournamentStatus.PUBLISHED, List.of(TournamentStatus.REGISTRATION_CLOSED, TournamentStatus.CANCELLED));
        STATUS_TRANSITIONS.put(TournamentStatus.REGISTRATION_CLOSED, List.of(TournamentStatus.PUBLISHED, TournamentStatus.CANCELLED));
        STATUS_TRANSITIONS.put(TournamentStatus.IN_PROGRESS, List.of(TournamentStatus.FINISHED, TournamentStatus.CANCELLED));
        STATUS_TRANSITIONS.put(TournamentStatus.FINISHED, List.of());
        STATUS_TRANSITIONS.put(TournamentStatus.CANCELLED, List.of());
    }

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Tournament> listPublicTournaments() {
        return tournamentRepository.findByDeletedAtIsNullAndStatusInOrderByStartDateAscCreatedAtDesc(PUBLIC_STATUSES);
    }

    @Transactional(readOnly = true)
    public Tournament getPublicTournament(String slug) {
        Tournament tournament = tournamentRepository.findBySlugAndDeletedAtIsNull(slug)
            .orElseThrow(() -> new NotFoundException("Tournament not found."));
        if (!isPublic(tournament)) {
            throw new NotFoundException("Tournament not found.");
        }
        return tournament;
    }

    @Transactional(readOnly = true)
    public List<Tournament> listManagedTournaments(SecurityUser currentUser) {
        if (isAdmin(currentUser)) {
            return tournamentRepository.findByDeletedAtIsNullOrderByStartDateAscCreatedAtDesc();
        }
        return tournamentRepository.findByCreatedBy_IdAndDeletedAtIsNullOrderByStartDateAscCreatedAtDesc(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Tournament requireManagedTournament(SecurityUser currentUser, String tournamentId) {
        Tournament tournament = tournamentRepository.findByIdAndDeletedAtIsNull(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found."));
        assertCanManage(currentUser, tournament);
        return tournament;
    }

    @Transactional
    public Tournament createTournament(SecurityUser currentUser, TournamentRequest request) {
        User creator = userRepository.findByIdAndDeletedAtIsNull(currentUser.getId())
            .orElseThrow(() -> new NotFoundException("User not found."));
        TournamentStatus status = request.status() == TournamentStatus.PUBLISHED ? TournamentStatus.PUBLISHED : TournamentStatus.DRAFT;

        Tournament tournament = new Tournament();
        applyRequest(tournament, request);
        tournament.setCreatedBy(creator);
        tournament.setSlug(createUniqueSlug(request.title(), null));
        tournament.setStatus(status);
        tournament.setRegistrationOpen(status == TournamentStatus.PUBLISHED && Boolean.TRUE.equals(request.registrationOpen()));
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public Tournament updateTournament(SecurityUser currentUser, String tournamentId, TournamentRequest request) {
        Tournament tournament = requireManagedTournament(currentUser, tournamentId);
        TournamentStatus nextStatus = request.status() == null ? tournament.getStatus() : request.status();

        applyRequest(tournament, request);
        tournament.setSlug(createUniqueSlug(request.title(), tournament.getId()));
        tournament.setStatus(nextStatus);
        tournament.setRegistrationOpen(nextStatus == TournamentStatus.PUBLISHED && Boolean.TRUE.equals(request.registrationOpen()));
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public Tournament changeStatus(SecurityUser currentUser, String tournamentId, TournamentStatus status) {
        Tournament tournament = requireManagedTournament(currentUser, tournamentId);
        if (!canTransition(tournament.getStatus(), status)) {
            throw new BadRequestException("Tournament status transition is not allowed.");
        }
        tournament.setStatus(status);
        tournament.setRegistrationOpen(status == TournamentStatus.PUBLISHED);
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public void deleteTournament(SecurityUser currentUser, String tournamentId) {
        Tournament tournament = requireManagedTournament(currentUser, tournamentId);
        tournament.setDeletedAt(Instant.now());
        tournament.setRegistrationOpen(false);
        tournamentRepository.save(tournament);
    }

    public boolean isPublic(Tournament tournament) {
        return tournament.getDeletedAt() == null && PUBLIC_STATUSES.contains(tournament.getStatus());
    }

    public void assertCanManage(SecurityUser currentUser, Tournament tournament) {
        if (!isAdmin(currentUser) && !tournament.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You cannot manage this tournament.");
        }
    }

    public boolean isAdmin(SecurityUser currentUser) {
        return UserRole.ADMIN.name().equals(currentUser.getRole());
    }

    private boolean canTransition(TournamentStatus from, TournamentStatus to) {
        if (from == to) {
            return true;
        }
        return STATUS_TRANSITIONS.getOrDefault(from, List.of()).contains(to);
    }

    private void applyRequest(Tournament tournament, TournamentRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("End date cannot be before start date.");
        }

        tournament.setTitle(trim(request.title()));
        tournament.setDescription(trim(request.description()));
        tournament.setLocation(trim(request.location()));
        tournament.setCity(trim(request.city()));
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
        tournament.setRegistrationDeadline(request.registrationDeadline());
        tournament.setOrganizer(trim(request.organizer()));
        tournament.setContactEmail(normalizeEmail(request.contactEmail()));
        tournament.setContactPhone(blankToNull(request.contactPhone()));
        tournament.setTournamentType(request.tournamentType());
        tournament.setTimeControlType(request.timeControlType());
        tournament.setTimeControlDescription(trim(request.timeControlDescription()));
        tournament.setRounds(request.rounds());
        tournament.setMaxPlayers(request.maxPlayers());
        tournament.setEntryFee(blankToNull(request.entryFee()));
        tournament.setRegulationsUrl(blankToNull(request.regulationsUrl()));
        tournament.setAllowPlayerCancellation(request.allowPlayerCancellation() == null || request.allowPlayerCancellation());
        tournament.setShowRegisteredPlayers(request.showRegisteredPlayers() == null || request.showRegisteredPlayers());
    }

    private String createUniqueSlug(String title, String existingId) {
        String base = slugify(title);
        if (base.isBlank()) {
            base = "turniej";
        }
        String slug = base;
        int counter = 2;

        while (existingId == null ? tournamentRepository.existsBySlug(slug) : tournamentRepository.existsBySlugAndIdNot(slug, existingId)) {
            slug = base + "-" + counter;
            counter += 1;
        }
        return slug;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        String withoutMarks = NON_ASCII_MARKS.matcher(normalized).replaceAll("");
        String slug = NON_SLUG_CHARS.matcher(withoutMarks.toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug.length() > 80 ? slug.substring(0, 80) : slug;
    }

    private String normalizeEmail(String email) {
        return trim(email).toLowerCase(Locale.ROOT);
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
