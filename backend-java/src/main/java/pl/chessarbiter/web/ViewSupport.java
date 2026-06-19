package pl.chessarbiter.web;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;
import pl.chessarbiter.dto.tournament.TournamentDisplayStatus;
import pl.chessarbiter.entity.GameResult;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.entity.RoundStatus;
import pl.chessarbiter.entity.TimeControlType;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.entity.TournamentType;

@Component("view")
public class ViewSupport {

    public static final ZoneId WARSAW_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Locale POLISH = Locale.forLanguageTag("pl-PL");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy", POLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", POLISH);

    public String dateRange(Instant start, Instant end) {
        String startText = date(start);
        return end == null || end.equals(start) ? startText : startText + " - " + date(end);
    }

    public String date(Instant value) {
        return value == null ? "" : DATE.format(value.atZone(WARSAW_ZONE));
    }

    public String dateTime(Instant value) {
        return value == null ? "" : DATE_TIME.format(value.atZone(WARSAW_ZONE));
    }

    public String displayStatus(TournamentDisplayStatus status) {
        return switch (status) {
            case DRAFT -> "Szkic";
            case CANCELLED -> "Odwołany";
            case SCHEDULED -> "Zaplanowany";
            case IN_PROGRESS -> "W trakcie";
            case FINISHED -> "Zakończony";
        };
    }

    public String tournamentStatus(TournamentStatus status) {
        return switch (status) {
            case DRAFT -> "Szkic";
            case PUBLISHED -> "Opublikowany";
            case REGISTRATION_CLOSED -> "Zapisy zamknięte";
            case IN_PROGRESS -> "W trakcie";
            case FINISHED -> "Zakończony";
            case CANCELLED -> "Odwołany";
        };
    }

    public String registrationStatus(RegistrationStatus status) {
        return switch (status) {
            case REGISTERED -> "Zgłoszony";
            case WAITLIST -> "Lista rezerwowa";
            case CANCELLED -> "Anulowany";
        };
    }

    public String roundStatus(RoundStatus status) {
        return switch (status) {
            case NOT_STARTED -> "Nierozpoczęta";
            case PAIRINGS_PUBLISHED -> "Kojarzenia opublikowane";
            case IN_PROGRESS -> "W trakcie";
            case COMPLETED -> "Zakończona";
        };
    }

    public String gameResult(GameResult result) {
        return switch (result) {
            case NOT_PLAYED -> "Nie rozegrano";
            case WHITE_WIN -> "1-0";
            case BLACK_WIN -> "0-1";
            case DRAW -> "½-½";
            case WHITE_FORFEIT -> "+/-";
            case BLACK_FORFEIT -> "-/+";
            case DOUBLE_FORFEIT -> "-/-";
            case BYE -> "Pauza";
        };
    }

    public String tournamentType(TournamentType type) {
        return type == TournamentType.SWISS ? "System szwajcarski" : "System kołowy";
    }

    public String timeControl(TimeControlType type) {
        return switch (type) {
            case CLASSICAL -> "Klasyczne";
            case RAPID -> "Szybkie";
            case BLITZ -> "Błyskawiczne";
        };
    }

    public String category(String value) {
        return value == null || value.isBlank() || "NONE".equals(value) ? "Brak" : value;
    }

    public String points(Number value) {
        if (value == null) {
            return "0";
        }
        double number = value.doubleValue();
        return number == Math.rint(number) ? String.valueOf((long) number) : String.format(POLISH, "%.1f", number);
    }
}
