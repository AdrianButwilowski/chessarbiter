package pl.chessarbiter.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.chessarbiter.entity.Game;

public interface GameRepository extends JpaRepository<Game, String> {

    List<Game> findByTournament_IdOrderByRound_RoundNumberAscBoardNumberAsc(String tournamentId);

    List<Game> findByRound_IdOrderByBoardNumberAsc(String roundId);

    Optional<Game> findByRound_IdAndBoardNumber(String roundId, Integer boardNumber);

    @Query("""
        select g from Game g
        where g.round.id = :roundId
          and (
            g.whiteRegistration.id in :registrationIds
            or g.blackRegistration.id in :registrationIds
          )
        """)
    Optional<Game> findFirstByRoundIdAndAnyRegistrationId(
        @Param("roundId") String roundId,
        @Param("registrationIds") List<String> registrationIds
    );
}
