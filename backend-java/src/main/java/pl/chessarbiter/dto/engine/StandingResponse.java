package pl.chessarbiter.dto.engine;

import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.entity.TournamentStanding;

public record StandingResponse(
    String id,
    String tournamentId,
    RegistrationResponse registration,
    Double points,
    Integer gamesPlayed,
    Integer wins,
    Integer draws,
    Integer losses,
    Integer forfeits,
    Double buchholz,
    Double medianBuchholz,
    Double sonnebornBerger,
    Double progressiveScore,
    Double directEncounter,
    Integer rank
) {

    public static StandingResponse from(TournamentStanding standing) {
        return new StandingResponse(
            standing.getId(),
            standing.getTournament().getId(),
            RegistrationResponse.from(standing.getRegistration()),
            standing.getPoints(),
            standing.getGamesPlayed(),
            standing.getWins(),
            standing.getDraws(),
            standing.getLosses(),
            standing.getForfeits(),
            standing.getBuchholz(),
            standing.getMedianBuchholz(),
            standing.getSonnebornBerger(),
            standing.getProgressiveScore(),
            standing.getDirectEncounter(),
            standing.getRank()
        );
    }
}
