<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="pl.chessarbiter.model.TournamentOption" %>
<%!
    private String h(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String v(Map<String, String> values, String key) {
        return values == null ? "" : values.getOrDefault(key, "");
    }

    private String error(Map<String, String> errors, String key) {
        return errors == null ? "" : errors.getOrDefault(key, "");
    }
%>
<%
    Map<String, String> errors = (Map<String, String>) request.getAttribute("errors");
    Map<String, String> values = (Map<String, String>) request.getAttribute("values");
    List<TournamentOption> tournaments = (List<TournamentOption>) request.getAttribute("tournaments");
    String formError = (String) request.getAttribute("formError");

    if (errors == null) errors = Collections.emptyMap();
    if (values == null) values = Collections.emptyMap();
    if (tournaments == null) tournaments = Collections.emptyList();

    String selectedTournamentId = v(values, "tournamentId");
    if (selectedTournamentId.isEmpty() && !tournaments.isEmpty()) {
        selectedTournamentId = tournaments.get(0).getId();
    }

    TournamentOption selectedTournament = null;
    for (TournamentOption tournament : tournaments) {
        if (tournament.getId().equals(selectedTournamentId)) {
            selectedTournament = tournament;
            break;
        }
    }
    if (selectedTournament == null && !tournaments.isEmpty()) {
        selectedTournament = tournaments.get(0);
    }

    String currentUserEmail = (String) session.getAttribute("currentUserEmail");
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    if (flashSuccess != null) {
        session.removeAttribute("flashSuccess");
    }
%>
<!doctype html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formularz zgłoszeniowy | ChessArbiter Polska</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/styles.css">
    <script defer src="<%= request.getContextPath() %>/assets/js/registration.js"></script>
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="brand" href="<%= request.getContextPath() %>/logowanie" aria-label="ChessArbiter Polska">
            <span class="brand__icon">♕</span>
            <span>ChessArbiter Polska</span>
        </a>
        <nav class="nav">
            <a class="nav__link" href="<%= request.getContextPath() %>/logowanie">Logowanie</a>
            <a class="nav__link" href="<%= request.getContextPath() %>/zgloszenie">Zgłoszenie</a>
            <% if (currentUserEmail != null) { %>
                <form method="post" action="<%= request.getContextPath() %>/wyloguj" class="nav__form">
                    <button class="nav__button" type="submit">Wyloguj</button>
                </form>
            <% } %>
        </nav>
    </div>
</header>

<main class="page-shell">
    <div class="page-heading">
        <p class="eyebrow">ChessArbiter Polska</p>
        <h1>Formularz zgłoszeniowy</h1>
        <p>
            Pola oznaczone gwiazdką są wymagane. Walidacja jest wykonywana po stronie Javy w servlecie,
            a błędy wracają bez utraty wpisanych danych.
        </p>
    </div>

    <% if (currentUserEmail != null) { %>
        <p class="alert alert--info">Zalogowany użytkownik: <strong><%= h(currentUserEmail) %></strong></p>
    <% } %>

    <div class="content-grid">
        <section class="card card--accent" aria-labelledby="registration-title">
            <div class="card__header">
                <h2 id="registration-title" class="card__title">Zgłoszenie zawodnika</h2>
                <p class="card__description">Wprowadź dane zawodnika i wybierz turniej.</p>
            </div>
            <div class="card__content">
                <% if (formError != null) { %>
                    <p class="alert alert--error"><%= h(formError) %></p>
                <% } %>
                <% if (flashSuccess != null) { %>
                    <p class="alert alert--success"><%= h(flashSuccess) %></p>
                <% } %>

                <form method="post" action="<%= request.getContextPath() %>/zgloszenie" class="form form-grid" novalidate>
                    <div class="field field--wide">
                        <label class="label" for="tournamentId">Turniej *</label>
                        <select
                                class="input <%= error(errors, "tournamentId").isEmpty() ? "" : "input--error" %>"
                                id="tournamentId"
                                name="tournamentId"
                                data-tournament-select
                                required>
                            <% for (TournamentOption tournament : tournaments) { %>
                                <option
                                        value="<%= h(tournament.getId()) %>"
                                        data-title="<%= h(tournament.getTitle()) %>"
                                        data-city="<%= h(tournament.getCity()) %>"
                                        data-location="<%= h(tournament.getLocation()) %>"
                                        data-date="<%= h(tournament.getDateLabel()) %>"
                                        data-count="<%= tournament.getActiveRegistrationCount() %>"
                                        data-max="<%= tournament.getMaxPlayers() == null ? "" : tournament.getMaxPlayers() %>"
                                        <%= tournament.getId().equals(selectedTournamentId) ? "selected" : "" %>>
                                    <%= h(tournament.getTitle()) %> — <%= h(tournament.getCity()) %>, <%= h(tournament.getDateLabel()) %>
                                </option>
                            <% } %>
                        </select>
                        <% if (!error(errors, "tournamentId").isEmpty()) { %>
                            <p class="field-error"><%= h(error(errors, "tournamentId")) %></p>
                        <% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="firstName">Imię *</label>
                        <input class="input <%= error(errors, "firstName").isEmpty() ? "" : "input--error" %>" id="firstName" name="firstName" value="<%= h(v(values, "firstName")) %>" required minlength="2" maxlength="80">
                        <% if (!error(errors, "firstName").isEmpty()) { %><p class="field-error"><%= h(error(errors, "firstName")) %></p><% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="lastName">Nazwisko *</label>
                        <input class="input <%= error(errors, "lastName").isEmpty() ? "" : "input--error" %>" id="lastName" name="lastName" value="<%= h(v(values, "lastName")) %>" required minlength="2" maxlength="80">
                        <% if (!error(errors, "lastName").isEmpty()) { %><p class="field-error"><%= h(error(errors, "lastName")) %></p><% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="email">E-mail *</label>
                        <input class="input <%= error(errors, "email").isEmpty() ? "" : "input--error" %>" id="email" name="email" type="email" value="<%= h(v(values, "email")) %>" required>
                        <% if (!error(errors, "email").isEmpty()) { %><p class="field-error"><%= h(error(errors, "email")) %></p><% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="clubOrCity">Klub lub miasto *</label>
                        <input class="input <%= error(errors, "clubOrCity").isEmpty() ? "" : "input--error" %>" id="clubOrCity" name="clubOrCity" value="<%= h(v(values, "clubOrCity")) %>" required minlength="2" maxlength="120">
                        <% if (!error(errors, "clubOrCity").isEmpty()) { %><p class="field-error"><%= h(error(errors, "clubOrCity")) %></p><% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="rating">Ranking *</label>
                        <input class="input <%= error(errors, "rating").isEmpty() ? "" : "input--error" %>" id="rating" name="rating" type="number" min="0" max="4000" value="<%= h(v(values, "rating")) %>" required>
                        <% if (!error(errors, "rating").isEmpty()) { %><p class="field-error"><%= h(error(errors, "rating")) %></p><% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="birthYear">Rok urodzenia</label>
                        <input class="input <%= error(errors, "birthYear").isEmpty() ? "" : "input--error" %>" id="birthYear" name="birthYear" type="number" min="1900" value="<%= h(v(values, "birthYear")) %>">
                        <% if (!error(errors, "birthYear").isEmpty()) { %><p class="field-error"><%= h(error(errors, "birthYear")) %></p><% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="federation">Federacja</label>
                        <input class="input" id="federation" name="federation" value="<%= h(v(values, "federation")) %>">
                    </div>

                    <div class="field">
                        <label class="label" for="licenseNumber">Numer licencji</label>
                        <input class="input" id="licenseNumber" name="licenseNumber" value="<%= h(v(values, "licenseNumber")) %>">
                    </div>

                    <div class="field">
                        <label class="label" for="chessCategory">Kategoria szachowa</label>
                        <input class="input <%= error(errors, "chessCategory").isEmpty() ? "" : "input--error" %>" id="chessCategory" name="chessCategory" placeholder="np. III, II, I, K, FM" maxlength="30" value="<%= h(v(values, "chessCategory")) %>">
                        <% if (!error(errors, "chessCategory").isEmpty()) { %><p class="field-error"><%= h(error(errors, "chessCategory")) %></p><% } %>
                    </div>

                    <div class="field">
                        <label class="label" for="phoneNumber">Telefon</label>
                        <input class="input <%= error(errors, "phoneNumber").isEmpty() ? "" : "input--error" %>" id="phoneNumber" name="phoneNumber" value="<%= h(v(values, "phoneNumber")) %>">
                        <% if (!error(errors, "phoneNumber").isEmpty()) { %><p class="field-error"><%= h(error(errors, "phoneNumber")) %></p><% } %>
                    </div>

                    <div class="field field--wide">
                        <label class="label" for="notes">Uwagi</label>
                        <textarea class="textarea <%= error(errors, "notes").isEmpty() ? "" : "input--error" %>" id="notes" name="notes" maxlength="500" data-notes><%= h(v(values, "notes")) %></textarea>
                        <div class="field-row">
                            <% if (!error(errors, "notes").isEmpty()) { %><p class="field-error"><%= h(error(errors, "notes")) %></p><% } %>
                            <p class="counter"><span data-notes-counter><%= h(v(values, "notes")).length() %></span>/500</p>
                        </div>
                    </div>

                    <div class="field field--wide">
                        <label class="checkbox-card">
                            <input type="checkbox" name="acceptRegulations" <%= "on".equals(v(values, "acceptRegulations")) ? "checked" : "" %>>
                            <span>Potwierdzam poprawność danych i akceptuję regulamin wybranego turnieju. *</span>
                        </label>
                        <% if (!error(errors, "acceptRegulations").isEmpty()) { %><p class="field-error"><%= h(error(errors, "acceptRegulations")) %></p><% } %>
                    </div>

                    <div class="field field--wide">
                        <button class="button button--primary" type="submit">Wyślij zgłoszenie</button>
                    </div>
                </form>
            </div>
        </section>

        <aside class="card summary-card" aria-live="polite">
            <div class="card__header">
                <h2 class="card__title">Wybrany turniej</h2>
                <p class="card__description">Podgląd wydarzenia, do którego wysyłasz zgłoszenie.</p>
            </div>
            <div class="card__content summary-list">
                <% if (selectedTournament != null) { %>
                    <p class="summary-title" data-summary-title><%= h(selectedTournament.getTitle()) %></p>
                    <p data-summary-place><%= h(selectedTournament.getCity()) %>, <%= h(selectedTournament.getLocation()) %></p>
                    <p data-summary-date><%= h(selectedTournament.getDateLabel()) %></p>
                    <p data-summary-count>
                        Zgłoszenia: <%= selectedTournament.getActiveRegistrationCount() %><%= selectedTournament.getMaxPlayers() == null ? "" : " / " + selectedTournament.getMaxPlayers() %>
                    </p>
                <% } else { %>
                    <p>Brak dostępnych turniejów.</p>
                <% } %>
            </div>
        </aside>
    </div>
</main>
</body>
</html>
