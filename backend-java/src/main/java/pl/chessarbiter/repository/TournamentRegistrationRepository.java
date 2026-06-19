package pl.chessarbiter.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.TournamentRegistration;

public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, String> {

    long countByTournament_IdAndStatus(String tournamentId, RegistrationStatus status);

    Optional<TournamentRegistration> findByIdAndTournament_Id(String id, String tournamentId);

    List<TournamentRegistration> findByUser_IdOrderByCreatedAtDesc(String userId);

    List<TournamentRegistration> findByTournament_IdOrderByStatusAscCreatedAtAsc(String tournamentId);

    List<TournamentRegistration> findByTournament_IdAndStatusOrderByStartNumberAscRatingDescLastNameAscFirstNameAsc(
        String tournamentId,
        RegistrationStatus status
    );

    List<TournamentRegistration> findByTournament_IdAndStatusNotOrderByStartNumberAscRatingDescLastNameAscFirstNameAsc(
        String tournamentId,
        RegistrationStatus status
    );

    boolean existsByTournament_IdAndEmailIgnoreCase(String tournamentId, String email);

    boolean existsByTournament_IdAndUser_Id(String tournamentId, String userId);
}
