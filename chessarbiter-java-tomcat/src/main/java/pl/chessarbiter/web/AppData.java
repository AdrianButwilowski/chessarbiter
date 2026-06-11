package pl.chessarbiter.web;

import pl.chessarbiter.model.RegistrationEntry;
import pl.chessarbiter.model.TournamentOption;

import jakarta.servlet.ServletContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AppData {
    private static final String REGISTRATIONS_KEY = "registrations";

    private AppData() {
    }

    static List<TournamentOption> tournaments() {
        return List.of(
                new TournamentOption("rapid-2026", "Otwarte Mistrzostwa Miasta Rapid", "Warszawa", "Dom Kultury Centrum", "20 czerwca 2026", 24, 64),
                new TournamentOption("junior-2026", "Turniej Juniorów U12", "Kraków", "Klub Szachowy Hetman", "27 czerwca 2026", 16, 40),
                new TournamentOption("blitz-2026", "Weekendowy Blitz ChessArbiter", "Gdańsk", "Sala Konferencyjna Amber", "4 lipca 2026", 38, null)
        );
    }

    static TournamentOption findTournament(String id) {
        return tournaments()
                .stream()
                .filter(tournament -> tournament.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    static List<RegistrationEntry> registrations(ServletContext context) {
        synchronized (context) {
            Object existing = context.getAttribute(REGISTRATIONS_KEY);
            if (existing instanceof List<?>) {
                return (List<RegistrationEntry>) existing;
            }

            List<RegistrationEntry> created = Collections.synchronizedList(new ArrayList<>());
            context.setAttribute(REGISTRATIONS_KEY, created);
            return created;
        }
    }
}
