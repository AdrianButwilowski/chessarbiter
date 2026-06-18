package pl.chessarbiter.dto.player;

import pl.chessarbiter.entity.GameResult;

public record PlayerGameResponse(
    String gameId,
    Integer roundNumber,
    Integer boardNumber,
    String opponentName,
    GameResult result
) {
}
