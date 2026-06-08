<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Map" %>
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
    if (errors == null) errors = Collections.emptyMap();
    if (values == null) values = Collections.emptyMap();

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
    <title>Logowanie | ChessArbiter Polska</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/styles.css">
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

<main class="auth-page chess-board-bg">
    <section class="hero-copy">
        <p class="eyebrow">ChessArbiter Polska</p>
        <h1>Panel turniejowy dla sędziów i zawodników</h1>
        <p>
            Logowanie zostało przygotowane jako klasyczna podstrona Java/JSP uruchamiana w Tomcacie.
            Wygląd bazuje na kolorystyce i układzie z przesłanego projektu.
        </p>
    </section>

    <section class="card auth-card" aria-labelledby="login-title">
        <div class="card__header">
            <h2 id="login-title" class="card__title">Logowanie</h2>
            <p class="card__description">Zaloguj się do panelu turniejowego.</p>
        </div>
        <div class="card__content">
            <% if (!error(errors, "form").isEmpty()) { %>
                <p class="alert alert--error"><%= h(error(errors, "form")) %></p>
            <% } %>
            <% if (flashSuccess != null) { %>
                <p class="alert alert--success"><%= h(flashSuccess) %></p>
            <% } %>

            <form method="post" action="<%= request.getContextPath() %>/logowanie" class="form" novalidate>
                <div class="field">
                    <label class="label" for="email">E-mail</label>
                    <input
                            class="input <%= error(errors, "email").isEmpty() ? "" : "input--error" %>"
                            id="email"
                            name="email"
                            type="email"
                            autocomplete="email"
                            value="<%= h(v(values, "email")) %>"
                            required>
                    <% if (!error(errors, "email").isEmpty()) { %>
                        <p class="field-error"><%= h(error(errors, "email")) %></p>
                    <% } %>
                </div>

                <div class="field">
                    <label class="label" for="password">Hasło</label>
                    <input
                            class="input <%= error(errors, "password").isEmpty() ? "" : "input--error" %>"
                            id="password"
                            name="password"
                            type="password"
                            autocomplete="current-password"
                            required>
                    <% if (!error(errors, "password").isEmpty()) { %>
                        <p class="field-error"><%= h(error(errors, "password")) %></p>
                    <% } %>
                </div>

                <button class="button button--primary button--full" type="submit">Zaloguj</button>
            </form>

            <div class="demo-box">
                <p class="demo-box__title">Konto demo</p>
                <p><strong>admin@chessarbiter.pl</strong> / <strong>admin123</strong></p>
                <p>Możesz też użyć: sedzia@chessarbiter.pl / sedzia123</p>
            </div>
        </div>
    </section>
</main>
</body>
</html>
