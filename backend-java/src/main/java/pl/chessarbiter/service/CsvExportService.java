package pl.chessarbiter.service;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentRegistration;
import pl.chessarbiter.entity.TournamentStanding;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final TournamentService tournamentService;
    private final StandingService standingService;
    private final TournamentRegistrationRepository registrationRepository;

    @Transactional(readOnly = true)
    public CsvFile registrations(SecurityUser currentUser, String tournamentId) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        List<TournamentRegistration> registrations = registrationRepository.findByTournament_IdOrderByStatusAscCreatedAtAsc(tournamentId);
        // UTF-8 BOM helps spreadsheet applications detect Polish characters correctly.
        StringBuilder csv = new StringBuilder("\uFEFF");
        appendRow(csv, List.of(
            "First name",
            "Last name",
            "Email",
            "Club or city",
            "Federation",
            "Rating",
            "Category",
            "Birth year",
            "Phone",
            "Notes",
            "Status",
            "Created at"
        ));
        registrations.forEach(row -> appendRow(csv, Arrays.asList(
            row.getFirstName(),
            row.getLastName(),
            row.getEmail(),
            row.getClubOrCity(),
            row.getFederation(),
            row.getRating(),
            row.getChessCategory(),
            row.getBirthYear(),
            row.getPhoneNumber(),
            row.getNotes(),
            row.getStatus(),
            row.getCreatedAt() == null ? "" : DateTimeFormatter.ISO_INSTANT.format(row.getCreatedAt())
        )));
        return new CsvFile("registrations-" + tournament.getSlug() + ".csv", csv.toString());
    }

    @Transactional
    public CsvFile standings(SecurityUser currentUser, String tournamentId) {
        Tournament tournament = tournamentService.requireManagedTournament(currentUser, tournamentId);
        List<TournamentStanding> standings = standingService.recalculate(tournament);
        // UTF-8 BOM helps spreadsheet applications detect Polish characters correctly.
        StringBuilder csv = new StringBuilder("\uFEFF");
        appendRow(csv, List.of(
            "Rank",
            "First name",
            "Last name",
            "Club or city",
            "Birth year",
            "Rating",
            "Category",
            "Points",
            "Buchholz",
            "Median Buchholz",
            "Sonneborn-Berger",
            "Progressive score"
        ));
        standings.forEach(row -> appendRow(csv, Arrays.asList(
            row.getRank(),
            row.getRegistration().getFirstName(),
            row.getRegistration().getLastName(),
            row.getRegistration().getClubOrCity(),
            row.getRegistration().getBirthYear(),
            row.getRegistration().getRating(),
            row.getRegistration().getChessCategory(),
            row.getPoints(),
            row.getBuchholz(),
            row.getMedianBuchholz(),
            row.getSonnebornBerger(),
            row.getProgressiveScore()
        )));
        return new CsvFile("standings-" + tournament.getSlug() + ".csv", csv.toString());
    }

    private void appendRow(StringBuilder csv, List<?> values) {
        if (csv.length() > 1) {
            csv.append("\r\n");
        }
        for (int index = 0; index < values.size(); index += 1) {
            if (index > 0) {
                csv.append(",");
            }
            csv.append(cell(values.get(index)));
        }
    }

    private String cell(Object value) {
        String stringValue = value == null ? "" : String.valueOf(value);
        return "\"" + stringValue.replace("\"", "\"\"") + "\"";
    }

    public record CsvFile(String filename, String content) {
    }
}
