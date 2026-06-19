package pl.chessarbiter.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.service.CsvExportService;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','ARBITER')")
public class CsvExportViewController {

    private final CsvExportService csvExportService;

    @GetMapping({
        "/panel-sedziego/turnieje/{id}/zgloszenia.csv",
        "/panel-sedziego/turnieje/{id}/zgloszenia/export.csv"
    })
    public ResponseEntity<String> registrations(@PathVariable String id) {
        return csv(csvExportService.registrations(CurrentUser.require(), id));
    }

    @GetMapping({
        "/panel-sedziego/turnieje/{id}/wyniki.csv",
        "/panel-sedziego/turnieje/{id}/wyniki/export.csv"
    })
    public ResponseEntity<String> standings(@PathVariable String id) {
        return csv(csvExportService.standings(CurrentUser.require(), id));
    }

    private ResponseEntity<String> csv(CsvExportService.CsvFile file) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
            .body(file.content());
    }
}
