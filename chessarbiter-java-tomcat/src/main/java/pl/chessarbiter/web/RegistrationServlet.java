package pl.chessarbiter.web;

import pl.chessarbiter.model.RegistrationEntry;
import pl.chessarbiter.model.TournamentOption;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RegistrationServlet", urlPatterns = "/zgloszenie")
public class RegistrationServlet extends HttpServlet {
    private static final List<String> FIELDS = List.of(
            "tournamentId",
            "firstName",
            "lastName",
            "email",
            "clubOrCity",
            "federation",
            "licenseNumber",
            "rating",
            "chessCategory",
            "phoneNumber",
            "birthYear",
            "notes"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "Formularz zgłoszeniowy");
        request.setAttribute("tournaments", AppData.tournaments());
        request.getRequestDispatcher("/WEB-INF/views/registration.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Map<String, String> values = FormTools.formValues(request, FIELDS);
        boolean accepted = request.getParameter("acceptRegulations") != null;
        Map<String, String> errors = FormTools.validateRegistration(values, accepted);

        TournamentOption selectedTournament = AppData.findTournament(values.get("tournamentId"));
        if (selectedTournament == null) {
            errors.put("tournamentId", "Wybrany turniej nie istnieje.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("pageTitle", "Formularz zgłoszeniowy");
            request.setAttribute("tournaments", AppData.tournaments());
            request.setAttribute("values", values);
            request.setAttribute("errors", errors);
            request.setAttribute("formError", "Popraw błędy w formularzu zgłoszeniowym.");
            request.getRequestDispatcher("/WEB-INF/views/registration.jsp").forward(request, response);
            return;
        }

        RegistrationEntry entry = new RegistrationEntry(
                values.get("tournamentId"),
                values.get("firstName"),
                values.get("lastName"),
                values.get("email"),
                values.get("clubOrCity"),
                FormTools.nullable(values.get("federation")),
                FormTools.nullable(values.get("licenseNumber")),
                FormTools.requiredInteger(values.get("rating")),
                FormTools.nullable(values.get("chessCategory")) == null ? "NONE" : values.get("chessCategory"),
                FormTools.nullable(values.get("phoneNumber")),
                FormTools.optionalInteger(values.get("birthYear")),
                FormTools.nullable(values.get("notes"))
        );

        AppData.registrations(getServletContext()).add(entry);

        request.getSession(true).setAttribute("flashSuccess", "Zgłoszenie zostało zapisane dla turnieju: " + selectedTournament.getTitle() + ".");
        response.sendRedirect(request.getContextPath() + "/zgloszenie");
    }
}
