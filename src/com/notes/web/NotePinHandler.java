package com.notes.web;

import com.notes.dao.NoteDAO;
import com.notes.helper.RequestHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class NotePinHandler implements HttpHandler {

    private final NoteDAO noteDAO;

    public NotePinHandler(NoteDAO noteDAO) {
        this.noteDAO = noteDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            RequestHelper.redirect(exchange, "/notes");
            return;
        }

        Map<String, String> form = RequestHelper.getFormData(exchange);
        String idStr = form.get("id");
        String returnTo = form.getOrDefault("from", "/notes");

        if (idStr != null && !idStr.isEmpty()) {
            try {
                noteDAO.togglePin(Integer.parseInt(idStr));
            } catch (NumberFormatException ignored) {}
        }

        RequestHelper.redirect(exchange, returnTo);
    }
}
