package pl.chessarbiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.chessarbiter.dto.registration.RegistrationRequest;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentRegistration;
import pl.chessarbiter.repository.TournamentRepository;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.exception.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final TournamentRegistrationRepository registrationRepo;
    private final TournamentRepository tournamentRepo;

    public List<RegistrationResponse> getForTournament(String tournamentId) {
        return registrationRepo.findByTournament_Id(tournamentId).stream()
                .map(RegistrationResponse::from).toList();
    }

    public RegistrationResponse register(String tournamentSlug, RegistrationRequest request) {
        Tournament tournament = tournamentRepo.findBySlugAndDeletedAtIsNull(tournamentSlug)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));
        TournamentRegistration reg = TournamentRegistration.builder()
                .id(UUID.randomUUID().toString())
                .tournament(tournament)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .clubOrCity(request.clubOrCity())
                .federation(request.federation())
                .rating(request.rating())
                .chessCategory(request.chessCategory())
                .phoneNumber(request.phoneNumber())
                .birthYear(request.birthYear())
                .notes(request.notes())
                .status(RegistrationStatus.REGISTERED)
                .build();
        reg = registrationRepo.save(reg);
        return RegistrationResponse.from(reg);
    }
}
