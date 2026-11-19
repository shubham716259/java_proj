package com.notes.web;

import com.notes.dao.NoteDAO;
import com.notes.dao.UserDAO;
import com.notes.helper.RequestHelper;
import com.notes.helper.SessionHelper;
import com.notes.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class NotePinHandler implements HttpHandler {

    private final NoteDAO noteDAO;
    private final UserDAO userDAO;

    public NotePinHandler(NoteDAO noteDAO, UserDAO userDAO) {
        this.noteDAO = noteDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = SessionHelper.getUser(exchange, userDAO);
        if (user == null) {
            RequestHelper.redirect(exchange, "/login");
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            RequestHelper.redirect(exchange, "/notes");
            return;
        }

        Map<String, String> form = RequestHelper.getFormData(exchange);
        String idStr = form.get("id");
        String returnTo = form.getOrDefault("from", "/notes");

        if (idStr != null && !idStr.isEmpty()) {
            try {
                noteDAO.togglePin(Integer.parseInt(idStr), user.getId());
            } catch (NumberFormatException ignored) {}
        }

        RequestHelper.redirect(exchange, returnTo);
    }
}
