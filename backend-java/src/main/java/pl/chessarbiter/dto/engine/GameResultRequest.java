package pl.chessarbiter.dto.engine;

import jakarta.validation.constraints.NotNull;
import pl.chessarbiter.entity.GameResult;

public record GameResultRequest(@NotNull GameResult result) {
}
