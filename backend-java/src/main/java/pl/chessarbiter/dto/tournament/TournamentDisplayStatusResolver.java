package pl.chessarbiter.dto.tournament;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import pl.chessarbiter.entity.TournamentStatus;

public final class TournamentDisplayStatusResolver {

    static final ZoneId WARSAW_ZONE = ZoneId.of("Europe/Warsaw");

    private TournamentDisplayStatusResolver() {
    }

    public static TournamentDisplayStatus resolve(Instant startDate, Instant endDate) {
        return resolve(null, startDate, endDate, Instant.now());
    }

    public static TournamentDisplayStatus resolve(TournamentStatus status, Instant startDate, Instant endDate) {
        return resolve(status, startDate, endDate, Instant.now());
    }

    static TournamentDisplayStatus resolve(TournamentStatus status, Instant startDate, Instant endDate, Instant now) {
        // Manual states win over date-based display, so cancelled and finished tournaments stay stable over time.
        if (status == TournamentStatus.DRAFT) {
            return TournamentDisplayStatus.DRAFT;
        }
        if (status == TournamentStatus.CANCELLED) {
            return TournamentDisplayStatus.CANCELLED;
        }
        if (status == TournamentStatus.FINISHED) {
            return TournamentDisplayStatus.FINISHED;
        }

        if (startDate == null || now.isBefore(startDate)) {
            return TournamentDisplayStatus.SCHEDULED;
        }
        return TournamentDisplayStatus.IN_PROGRESS;
    }

    static TournamentDisplayStatus resolve(Instant startDate, Instant endDate, LocalDate today) {
        LocalDate start = startDate.atZone(WARSAW_ZONE).toLocalDate();

        if (today.isBefore(start)) {
            return TournamentDisplayStatus.SCHEDULED;
        }
        return TournamentDisplayStatus.IN_PROGRESS;
    }
}
