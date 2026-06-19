package pl.chessarbiter.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.PlayerProfile;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, String> {

    Optional<PlayerProfile> findByUser_Id(String userId);
}
