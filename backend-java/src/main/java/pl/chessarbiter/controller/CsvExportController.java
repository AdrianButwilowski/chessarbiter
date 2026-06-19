package pl.chessarbiter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.CsvExportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tournaments/{tournamentId}")
@PreAuthorize("hasAnyRole('ADMIN','ARBITER')")
public class CsvExportController {

    private final CsvExportService csvExportService;

    @GetMapping("/registrations/export.csv")
    public ResponseEntity<String> registrations(@PathVariable String tournamentId) {
        CsvExportService.CsvFile file = csvExportService.registrations(CurrentUser.require(), tournamentId);
        return csv(file);
    }

    @GetMapping("/standings/export.csv")
    public ResponseEntity<String> standings(@PathVariable String tournamentId) {
        CsvExportService.CsvFile file = csvExportService.standings(CurrentUser.require(), tournamentId);
        return csv(file);
    }

    private ResponseEntity<String> csv(CsvExportService.CsvFile file) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
            .body(file.content());
    }
}
