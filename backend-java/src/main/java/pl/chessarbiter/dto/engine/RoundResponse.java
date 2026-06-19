package pl.chessarbiter.dto.engine;

import java.util.List;
import pl.chessarbiter.entity.Round;
import pl.chessarbiter.entity.RoundStatus;

public record RoundResponse(
    String id,
    String tournamentId,
    Integer roundNumber,
    RoundStatus status,
    List<GameResponse> games
) {

    public static RoundResponse from(Round round) {
        return new RoundResponse(
            round.getId(),
            round.getTournament().getId(),
            round.getRoundNumber(),
            round.getStatus(),
            round.getGames().stream()
                .sorted((a, b) -> a.getBoardNumber().compareTo(b.getBoardNumber()))
                .map(GameResponse::from)
                .toList()
        );
    }
}
