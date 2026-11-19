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
        sb.append("  <link rel=\"stylesheet\" href=\"/static/style.css\">\n");
        sb.append("</head>\n<body class=\"auth-body\">\n");

        sb.append("  <div class=\"auth-container\">\n");
        sb.append("    <div class=\"auth-brand\">\n");
        sb.append("      <a href=\"/\" class=\"brand\" style=\"margin-bottom: 0;\">Smart Notes</a>\n");
        sb.append("      <div class=\"brand-sub\" style=\"margin-top: 4px;\">Personal Knowledge Base</div>\n");
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

        sb.append("</body>\n</html>\n");
        return sb.toString();
    }
}
