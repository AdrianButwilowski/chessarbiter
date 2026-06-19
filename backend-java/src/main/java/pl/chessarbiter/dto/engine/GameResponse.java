package pl.chessarbiter.dto.engine;

import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.entity.Game;
import pl.chessarbiter.entity.GameResult;

public record GameResponse(
    String id,
    String tournamentId,
    String roundId,
    Integer roundNumber,
    Integer boardNumber,
    RegistrationResponse whiteRegistration,
    RegistrationResponse blackRegistration,
    GameResult result,
    Double whitePoints,
    Double blackPoints
) {

    public static GameResponse from(Game game) {
        return new GameResponse(
            game.getId(),
            game.getTournament().getId(),
            game.getRound().getId(),
            game.getRound().getRoundNumber(),
            game.getBoardNumber(),
            game.getWhiteRegistration() == null ? null : RegistrationResponse.from(game.getWhiteRegistration()),
            game.getBlackRegistration() == null ? null : RegistrationResponse.from(game.getBlackRegistration()),
            game.getResult(),
            game.getWhitePoints(),
            game.getBlackPoints()
        );
    }
}
