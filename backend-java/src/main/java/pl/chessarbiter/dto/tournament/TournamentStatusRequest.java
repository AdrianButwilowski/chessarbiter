package pl.chessarbiter.dto.tournament;

import jakarta.validation.constraints.NotNull;
import pl.chessarbiter.entity.TournamentStatus;

public record TournamentStatusRequest(@NotNull TournamentStatus status) {
}
