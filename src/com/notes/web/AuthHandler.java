package com.notes.web;

import com.notes.dao.UserDAO;
import com.notes.helper.HtmlUtil;
import com.notes.helper.RequestHelper;
import com.notes.helper.SessionHelper;
import com.notes.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class AuthHandler implements HttpHandler {

    private final UserDAO userDAO;

    public AuthHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if ("/logout".equals(path)) {
            handleLogout(exchange);
            return;
        }

        if ("/signup".equals(path)) {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleSignupPost(exchange);
            } else {
                handleSignupGet(exchange, null);
            }
            return;
        }

        // default is /login
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleLoginPost(exchange);
        } else {
            handleLoginGet(exchange, null);
        }
    }

    private void handleLoginGet(HttpExchange exchange, String errorMessage) throws IOException {
        // If already logged in, redirect to dashboard
        if (SessionHelper.getUser(exchange, userDAO) != null) {
            RequestHelper.redirect(exchange, "/");
            return;
        }

        String html = renderAuthPage(
                "Log In",
                "Sign in to your notebook to access your personal knowledge base.",
                errorMessage,
                "/login",
                "Log In",
                false,
                "<p class=\"auth-switch\">Don't have an account yet? <a href=\"/signup\">Create account</a></p>"
        );

        RequestHelper.sendHtml(exchange, 200, html);
    }

    private void handleLoginPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = RequestHelper.getFormData(exchange);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            handleLoginGet(exchange, "Please enter both username and password.");
            return;
        }

        User user = userDAO.findByUsername(username);
        if (user == null) {
            handleLoginGet(exchange, "Invalid username or password.");
            return;
        }

        String hash = SessionHelper.hashPassword(password);
        if (!hash.equals(user.getPasswordHash())) {
            handleLoginGet(exchange, "Invalid username or password.");
            return;
        }

        // login successful: create session and cookie
        SessionHelper.createSession(exchange, user.getId());
        RequestHelper.redirect(exchange, "/");
    }

    private void handleSignupGet(HttpExchange exchange, String errorMessage) throws IOException {
        if (SessionHelper.getUser(exchange, userDAO) != null) {
            RequestHelper.redirect(exchange, "/");
            return;
        }

        String html = renderAuthPage(
                "Create Account",
                "Start saving notes, organizing folders, and indexing your study material.",
                errorMessage,
                "/signup",
                "Create Account",
                true,
                "<p class=\"auth-switch\">Already have an account? <a href=\"/login\">Log in</a></p>"
        );

        RequestHelper.sendHtml(exchange, 200, html);
    }

    private void handleSignupPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = RequestHelper.getFormData(exchange);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "");
        String confirm = form.getOrDefault("confirm_password", "");

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            handleSignupGet(exchange, "All fields are required.");
            return;
        }

        if (username.length() < 3 || username.length() > 30) {
            handleSignupGet(exchange, "Username must be between 3 and 30 characters.");
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            handleSignupGet(exchange, "Username can only contain letters, numbers, and underscores.");
            return;
        }

        if (password.length() < 6) {
            handleSignupGet(exchange, "Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            handleSignupGet(exchange, "Passwords do not match.");
            return;
        }

        if (userDAO.usernameExists(username)) {
            handleSignupGet(exchange, "Username is already taken. Please choose another.");
            return;
        }

        String hash = SessionHelper.hashPassword(password);
        User newUser = userDAO.createUser(username, hash);
        if (newUser == null) {
            handleSignupGet(exchange, "Failed to create account due to a database error. Please try again.");
            return;
        }

        // automatically sign in new user
        SessionHelper.createSession(exchange, newUser.getId());
        RequestHelper.redirect(exchange, "/");
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        SessionHelper.destroySession(exchange);
        RequestHelper.redirect(exchange, "/login");
    }

    private String renderAuthPage(String title, String subtitle, String error, String formAction,
                                  String buttonText, boolean isSignup, String extraHtml) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("  <title>").append(title).append(" - Smart Notes</title>\n");
        sb.append("  <link rel=\"icon\" type=\"image/svg+xml\" href=\"/static/favicon.svg\">\n");
        sb.append("  <meta name=\"theme-color\" content=\"#6366f1\">\n");
        sb.append("  <link rel=\"stylesheet\" href=\"/static/style.css\">\n");
        sb.append("  <script>\n");
        sb.append("    (function() {\n");
        sb.append("      var saved = localStorage.getItem('theme');\n");
        sb.append("      if (saved) {\n");
        sb.append("        document.documentElement.setAttribute('data-theme', saved);\n");
        sb.append("      } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {\n");
        sb.append("        document.documentElement.setAttribute('data-theme', 'dark');\n");
        sb.append("      } else {\n");
        sb.append("        document.documentElement.setAttribute('data-theme', 'light');\n");
        sb.append("      }\n");
        sb.append("    })();\n");
        sb.append("  </script>\n");
        sb.append("</head>\n<body class=\"auth-body\">\n");
        sb.append("  <div style=\"position: fixed; top: 16px; right: 16px; z-index: 100;\">\n");
        sb.append("    <button type=\"button\" class=\"theme-toggle-btn\" id=\"theme-toggle-btn\" onclick=\"toggleTheme()\" title=\"Toggle theme\" aria-label=\"Toggle theme\">\n");
        sb.append("      <svg class=\"theme-icon-moon\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M12 3a6 6 0 0 0 9 9 9 0 1 1-9-9Z\"/></svg>\n");
        sb.append("      <svg class=\"theme-icon-sun\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"4\"/><path d=\"M12 2v2\"/><path d=\"M12 20v2\"/><path d=\"m4.93 4.93 1.41 1.41\"/><path d=\"m17.66 17.66 1.41 1.41\"/><path d=\"M2 12h2\"/><path d=\"M20 12h2\"/><path d=\"m6.34 17.66-1.41 1.41\"/><path d=\"m19.07 4.93-1.41 1.41\"/></svg>\n");
        sb.append("    </button>\n");
        sb.append("  </div>\n");

        sb.append("  <div class=\"auth-container\">\n");
        sb.append("    <div class=\"auth-brand\">\n");
        sb.append("      <a href=\"/\" class=\"brand auth-brand-link\">\n");
        sb.append("        <svg class=\"logo-icon\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"60 50 200 200\" width=\"40\" height=\"40\" aria-hidden=\"true\">\n");
        sb.append("          <rect x=\"60\" y=\"50\" width=\"200\" height=\"200\" rx=\"44\" fill=\"#6366f1\"/>\n");
        sb.append("          <path d=\"M100 80H196L220 104V220H100Z\" fill=\"#fff\"/>\n");
        sb.append("          <path d=\"M196 80V104H220Z\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <rect x=\"118\" y=\"124\" width=\"84\" height=\"8\" rx=\"4\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <rect x=\"118\" y=\"146\" width=\"64\" height=\"8\" rx=\"4\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <rect x=\"118\" y=\"168\" width=\"76\" height=\"8\" rx=\"4\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <path d=\"M222 186L230 206L250 214L230 222L222 242L214 222L194 214L214 206Z\" fill=\"#fbbf24\" stroke=\"#6366f1\" stroke-width=\"5\" stroke-linejoin=\"round\"/>\n");
        sb.append("        </svg>\n");
        sb.append("        <div class=\"brand-text-wrap\">\n");
        sb.append("          <span class=\"logo-text\" style=\"font-size: 1.35rem; font-weight: 700;\">Smart Notes</span>\n");
        sb.append("          <div class=\"brand-sub\" style=\"margin-top: 2px;\">Personal Knowledge Base</div>\n");
        sb.append("        </div>\n");
        sb.append("      </a>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"auth-card\">\n");
        sb.append("      <h1 class=\"auth-title\">").append(title).append("</h1>\n");
        sb.append("      <p class=\"auth-desc\">").append(subtitle).append("</p>\n");

        if (error != null) {
            sb.append("      <div class=\"msg-alert msg-error\">").append(HtmlUtil.escape(error)).append("</div>\n");
        }

        sb.append("      <form method=\"POST\" action=\"").append(formAction).append("\">\n");

        // Username
        sb.append("        <div class=\"form-group\">\n");
        sb.append("          <label class=\"form-label\" for=\"username\">Username</label>\n");
        sb.append("          <input type=\"text\" id=\"username\" name=\"username\" class=\"form-input\" required autofocus maxlength=\"30\" placeholder=\"Enter your username\" />\n");
        sb.append("        </div>\n");

        // Password
        sb.append("        <div class=\"form-group\">\n");
        sb.append("          <label class=\"form-label\" for=\"password\">Password</label>\n");
        sb.append("          <input type=\"password\" id=\"password\" name=\"password\" class=\"form-input\" required minlength=\"6\" placeholder=\"Enter your password\" />\n");
        sb.append("        </div>\n");

        // Confirm Password for Signup
        if (isSignup) {
            sb.append("        <div class=\"form-group\">\n");
            sb.append("          <label class=\"form-label\" for=\"confirm_password\">Confirm password</label>\n");
            sb.append("          <input type=\"password\" id=\"confirm_password\" name=\"confirm_password\" class=\"form-input\" required minlength=\"6\" placeholder=\"Re-enter password\" />\n");
            sb.append("        </div>\n");
        }

        sb.append("        <button type=\"submit\" class=\"btn btn-primary auth-submit\">").append(buttonText).append("</button>\n");
        sb.append("      </form>\n");

        if (extraHtml != null) {
            sb.append(extraHtml);
        }

        sb.append("    </div>\n");
        sb.append("    <div class=\"auth-footer\">Notes project, CSE</div>\n");
        sb.append("  </div>\n");
        sb.append("  <script>\n");
        sb.append("    function updateThemeUI(theme) {\n");
        sb.append("      var btn = document.getElementById('theme-toggle-btn');\n");
        sb.append("      if (btn) {\n");
        sb.append("        var isDark = theme === 'dark';\n");
        sb.append("        btn.setAttribute('title', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');\n");
        sb.append("        btn.setAttribute('aria-label', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    function toggleTheme() {\n");
        sb.append("      var curr = document.documentElement.getAttribute('data-theme') || 'light';\n");
        sb.append("      var next = curr === 'dark' ? 'light' : 'dark';\n");
        sb.append("      document.documentElement.setAttribute('data-theme', next);\n");
        sb.append("      localStorage.setItem('theme', next);\n");
        sb.append("      updateThemeUI(next);\n");
        sb.append("    }\n");
        sb.append("    (function() {\n");
        sb.append("      var curr = document.documentElement.getAttribute('data-theme') || 'light';\n");
        sb.append("      updateThemeUI(curr);\n");
        sb.append("    })();\n");
        sb.append("  </script>\n");
        sb.append("</body>\n</html>\n");
        return sb.toString();
    }
}
