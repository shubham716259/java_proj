package com.notes.helper;

import com.notes.dao.UserDAO;
import com.notes.model.User;
import com.sun.net.httpserver.HttpExchange;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionHelper {

    // in-memory session map: token -> userId
    private static final Map<String, Integer> sessions = new ConcurrentHashMap<>();
    private static final String COOKIE_NAME = "session_id";

    // standard SHA-256 hash for password storage
    public static String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // creates a session for the user and sets the HTTP response cookie
    public static String createSession(HttpExchange exchange, int userId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);
        String cookieHeader = COOKIE_NAME + "=" + token + "; Path=/; HttpOnly; SameSite=Lax";
        exchange.getResponseHeaders().add("Set-Cookie", cookieHeader);
        return token;
    }

    // finds the logged-in user id from the request cookies, or returns -1
    public static int getUserId(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return -1;
        }

        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith(COOKIE_NAME + "=")) {
                String token = trimmed.substring((COOKIE_NAME + "=").length()).trim();
                Integer userId = sessions.get(token);
                if (userId != null) {
                    return userId;
                }
            }
        }
        return -1;
    }

    // retrieves full User object for current request, or null if unauthenticated
    public static User getUser(HttpExchange exchange, UserDAO userDAO) {
        int userId = getUserId(exchange);
        if (userId <= 0) return null;
        return userDAO.findById(userId);
    }

    // removes active session and clears the browser cookie
    public static void destroySession(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] parts = cookieHeader.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.startsWith(COOKIE_NAME + "=")) {
                    String token = trimmed.substring((COOKIE_NAME + "=").length()).trim();
                    sessions.remove(token);
                }
            }
        }
        String clearCookie = COOKIE_NAME + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
        exchange.getResponseHeaders().add("Set-Cookie", clearCookie);
    }
}
