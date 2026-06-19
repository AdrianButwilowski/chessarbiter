package pl.chessarbiter.dto.engine;

import java.util.List;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;

public record PairingModuleResponse(
    TournamentDetailResponse tournament,
    List<RegistrationResponse> participants,
    List<RoundResponse> rounds,
    List<StandingResponse> standings
) {
}
