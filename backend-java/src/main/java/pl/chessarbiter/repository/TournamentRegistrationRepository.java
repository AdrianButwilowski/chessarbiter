package pl.chessarbiter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.TournamentRegistration;
import java.util.List;

public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, String> {
    List<TournamentRegistration> findByTournament_Id(String tournamentId);
}
