package pl.chessarbiter.web;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class FormTools {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+\\d\\s-]{7,20}$");

    private FormTools() {
    }

    static String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    static Map<String, String> formValues(HttpServletRequest request, List<String> fields) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String field : fields) {
            values.put(field, param(request, field));
        }
        values.put("acceptRegulations", request.getParameter("acceptRegulations") == null ? "" : "on");
        return values;
    }

    static String nullable(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static Integer optionalInteger(String value) {
        String trimmed = nullable(value);
        return trimmed == null ? null : Integer.parseInt(trimmed);
    }

    static Integer requiredInteger(String value) {
        return Integer.parseInt(value.trim());
    }

    static Map<String, String> validateLogin(String email, String password) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Podaj adres e-mail.");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.put("email", "Podaj poprawny adres e-mail.");
        }

        if (password == null || password.isBlank()) {
            errors.put("password", "Podaj hasło.");
        } else if (password.length() < 6) {
            errors.put("password", "Hasło musi mieć minimum 6 znaków.");
        }

        return errors;
    }

    static Map<String, String> validateRegistration(Map<String, String> values, boolean accepted) {
        Map<String, String> errors = new LinkedHashMap<>();
        int currentYear = Year.now().getValue();

        String tournamentId = values.getOrDefault("tournamentId", "");
        String firstName = values.getOrDefault("firstName", "");
        String lastName = values.getOrDefault("lastName", "");
        String email = values.getOrDefault("email", "");
        String clubOrCity = values.getOrDefault("clubOrCity", "");
        String rating = values.getOrDefault("rating", "");
        String birthYear = values.getOrDefault("birthYear", "");
        String chessCategory = values.getOrDefault("chessCategory", "");
        String phoneNumber = values.getOrDefault("phoneNumber", "");
        String notes = values.getOrDefault("notes", "");

        if (tournamentId.isBlank()) {
            errors.put("tournamentId", "Wybierz turniej.");
        }

        if (firstName.length() < 2) {
            errors.put("firstName", "Imię musi mieć co najmniej 2 znaki.");
        } else if (firstName.length() > 80) {
            errors.put("firstName", "Imię może mieć maksymalnie 80 znaków.");
        }

        if (lastName.length() < 2) {
            errors.put("lastName", "Nazwisko musi mieć co najmniej 2 znaki.");
        } else if (lastName.length() > 80) {
            errors.put("lastName", "Nazwisko może mieć maksymalnie 80 znaków.");
        }

        if (email.isBlank()) {
            errors.put("email", "Podaj adres e-mail.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Podaj poprawny adres e-mail.");
        }

        if (clubOrCity.length() < 2) {
            errors.put("clubOrCity", "Podaj klub lub miasto.");
        } else if (clubOrCity.length() > 120) {
            errors.put("clubOrCity", "Klub lub miasto może mieć maksymalnie 120 znaków.");
        }

        if (rating.isBlank()) {
            errors.put("rating", "Podaj ranking.");
        } else {
            try {
                int parsedRating = Integer.parseInt(rating);
                if (parsedRating < 0 || parsedRating > 4000) {
                    errors.put("rating", "Ranking musi być liczbą całkowitą od 0 do 4000.");
                }
            } catch (NumberFormatException exception) {
                errors.put("rating", "Ranking musi być liczbą całkowitą od 0 do 4000.");
            }
        }

        if (chessCategory.length() > 30) {
            errors.put("chessCategory", "Kategoria może mieć maksymalnie 30 znaków.");
        }

        if (!birthYear.isBlank()) {
            try {
                int parsedBirthYear = Integer.parseInt(birthYear);
                if (parsedBirthYear < 1900 || parsedBirthYear > currentYear) {
                    errors.put("birthYear", "Rok urodzenia musi być między 1900 a " + currentYear + ".");
                }
            } catch (NumberFormatException exception) {
                errors.put("birthYear", "Rok urodzenia musi być liczbą.");
            }
        }

        if (!phoneNumber.isBlank() && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            errors.put("phoneNumber", "Podaj poprawny numer telefonu.");
        }

        if (notes.length() > 500) {
            errors.put("notes", "Uwagi mogą mieć maksymalnie 500 znaków.");
        }

        if (!accepted) {
            errors.put("acceptRegulations", "Akceptacja regulaminu jest wymagana.");
        }

        return errors;
    }
}
