package pl.chessarbiter.dto.player;

import pl.chessarbiter.entity.Game;
import pl.chessarbiter.entity.GameResult;
import pl.chessarbiter.entity.TournamentRegistration;

public record PlayerGameResponse(
    String gameId,
    Integer roundNumber,
    Integer boardNumber,
    String color,
    String opponentId,
    String opponentName,
    Integer opponentRating,
    GameResult result,
    Double points
) {

    public static PlayerGameResponse asWhite(Game game) {
        TournamentRegistration opponent = game.getBlackRegistration();
        return new PlayerGameResponse(
            game.getId(),
            game.getRound().getRoundNumber(),
            game.getBoardNumber(),
            "WHITE",
            opponent == null ? null : opponent.getId(),
            opponent == null ? "BYE" : opponent.getFirstName() + " " + opponent.getLastName(),
            opponent == null ? null : opponent.getRating(),
            game.getResult(),
            game.getWhitePoints()
        );
    }

    public static PlayerGameResponse asBlack(Game game) {
        TournamentRegistration opponent = game.getWhiteRegistration();
        return new PlayerGameResponse(
            game.getId(),
            game.getRound().getRoundNumber(),
            game.getBoardNumber(),
            "BLACK",
            opponent == null ? null : opponent.getId(),
            opponent == null ? "BYE" : opponent.getFirstName() + " " + opponent.getLastName(),
            opponent == null ? null : opponent.getRating(),
            game.getResult(),
            game.getBlackPoints()
        );
    }

    public String outcome() {
        if (result == GameResult.NOT_PLAYED || points == null) {
            return "Nie rozegrano";
        }
        if (Double.compare(points, 1.0) == 0) {
            return "Wygrana";
        }
        if (Double.compare(points, 0.5) == 0) {
            return "Remis";
        }
        return "Przegrana";
    }
}
