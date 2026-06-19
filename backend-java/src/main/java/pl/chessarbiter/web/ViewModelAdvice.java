package pl.chessarbiter.web;

import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pl.chessarbiter.entity.GameResult;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.TimeControlType;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.security.SecurityUser;

@ControllerAdvice(basePackages = "pl.chessarbiter.web")
public class ViewModelAdvice {

    @ModelAttribute("currentUser")
    SecurityUser currentUser() {
        return CurrentUser.optional().orElse(null);
    }

    @ModelAttribute("tournamentTypes")
    TournamentType[] tournamentTypes() {
        return TournamentType.values();
    }

    @ModelAttribute("timeControlTypes")
    TimeControlType[] timeControlTypes() {
        return TimeControlType.values();
    }

    @ModelAttribute("tournamentStatuses")
    TournamentStatus[] tournamentStatuses() {
        return TournamentStatus.values();
    }

    @ModelAttribute("registrationStatuses")
    RegistrationStatus[] registrationStatuses() {
        return RegistrationStatus.values();
    }

    @ModelAttribute("gameResults")
    List<GameResult> gameResults() {
        return List.of(
            GameResult.WHITE_WIN,
            GameResult.BLACK_WIN,
            GameResult.DRAW,
            GameResult.WHITE_FORFEIT,
            GameResult.BLACK_FORFEIT,
            GameResult.DOUBLE_FORFEIT
        );
    }
}
