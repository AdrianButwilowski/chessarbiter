package pl.chessarbiter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.Round;
import pl.chessarbiter.entity.RoundStatus;
import java.util.List;
import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, String> {
    List<Round> findByTournament_IdOrderByRoundNumberAsc(String tournamentId);
    Optional<Round> findByTournament_IdAndRoundNumber(String tournamentId, Integer roundNumber);
    Optional<Round> findFirstByTournament_IdOrderByRoundNumberDesc(String tournamentId);
    Optional<Round> findFirstByTournament_IdAndStatusNotOrderByRoundNumberAsc(String tournamentId, RoundStatus status);
    boolean existsByTournament_Id(String tournamentId);
    long countByTournament_IdAndStatus(String tournamentId, RoundStatus status);
}
