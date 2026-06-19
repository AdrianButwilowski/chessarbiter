package pl.chessarbiter.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.TournamentStanding;

public interface TournamentStandingRepository extends JpaRepository<TournamentStanding, String> {

    List<TournamentStanding> findByTournament_IdOrderByRankAsc(String tournamentId);

    Optional<TournamentStanding> findByTournament_IdAndRegistration_Id(String tournamentId, String registrationId);

    void deleteByTournament_Id(String tournamentId);
}
