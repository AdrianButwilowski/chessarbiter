package pl.chessarbiter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.PlayerProfile;
import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, String> {
    Optional<PlayerProfile> findByUser_Id(String userId);
}
