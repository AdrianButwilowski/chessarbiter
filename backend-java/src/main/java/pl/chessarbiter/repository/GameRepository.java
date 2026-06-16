package pl.chessarbiter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.Game;

public interface GameRepository extends JpaRepository<Game, String> {}
