package pl.chessarbiter.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(name = "LoginServlet", urlPatterns = "/logowanie")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "Logowanie");
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String email = FormTools.param(request, "email");
        String password = FormTools.param(request, "password");

        Map<String, String> values = new LinkedHashMap<>();
        values.put("email", email);

        Map<String, String> errors = FormTools.validateLogin(email, password);

        // Prosta demonstracyjna autoryzacja, żeby strona działała od razu w Tomcacie.
        // W prawdziwej aplikacji podmień ten warunek na sprawdzenie w bazie danych.
        boolean demoCredentials = (
                email.equalsIgnoreCase("admin@chessarbiter.pl") && password.equals("admin123")
        ) || (
                email.equalsIgnoreCase("sedzia@chessarbiter.pl") && password.equals("sedzia123")
        ) || (
                email.equalsIgnoreCase("zawodnik@chessarbiter.pl") && password.equals("zawodnik123")
        );

        if (errors.isEmpty() && !demoCredentials) {
            errors.put("form", "Nieprawidłowy e-mail lub hasło. Użyj konta demo: admin@chessarbiter.pl / admin123.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("pageTitle", "Logowanie");
            request.setAttribute("errors", errors);
            request.setAttribute("values", values);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("currentUserEmail", email);
        session.setAttribute("flashSuccess", "Zalogowano poprawnie jako " + email + ".");
        response.sendRedirect(request.getContextPath() + "/zgloszenie");
    }
}
