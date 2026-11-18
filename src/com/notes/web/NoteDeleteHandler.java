package com.notes.web;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.helper.HtmlUtil;
import com.notes.helper.RequestHelper;
import com.notes.model.Note;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class NoteDeleteHandler implements HttpHandler {

    private final NoteDAO noteDAO;
    private final FolderDAO folderDAO;
    private final TagDAO tagDAO;

    public NoteDeleteHandler(NoteDAO noteDAO, FolderDAO folderDAO, TagDAO tagDAO) {
        this.noteDAO = noteDAO;
        this.folderDAO = folderDAO;
        this.tagDAO = tagDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = RequestHelper.getFormData(exchange);
            String idStr = form.get("id");
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    noteDAO.deleteNote(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            RequestHelper.redirect(exchange, "/notes");
        } else {
            // GET: Confirmation page
            Map<String, String> params = RequestHelper.getQueryParams(exchange);
            String idStr = params.get("id");
            if (idStr == null || idStr.isEmpty()) {
                RequestHelper.redirect(exchange, "/notes");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                RequestHelper.redirect(exchange, "/notes");
                return;
            }

            Note note = noteDAO.getNoteById(id);
            if (note == null) {
                RequestHelper.redirect(exchange, "/notes");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<div class=\"form-box\" style=\"max-width: 500px;\">\n");
            sb.append("  <h1 class=\"page-title\" style=\"font-size: 20px;\">Delete note</h1>\n");
            sb.append("  <p style=\"margin: 14px 0 20px; font-size: 14px;\">\n");
            sb.append("    Are you sure you want to delete &ldquo;<strong>").append(HtmlUtil.escape(note.getTitle())).append("</strong>&rdquo;?\n");
            sb.append("  </p>\n");

            sb.append("  <form method=\"POST\" action=\"/notes/delete\" style=\"display: flex; gap: 10px;\">\n");
            sb.append("    <input type=\"hidden\" name=\"id\" value=\"").append(note.getId()).append("\" />\n");
            sb.append("    <button type=\"submit\" class=\"btn btn-danger\">Yes, delete</button>\n");
            sb.append("    <a href=\"/notes/view?id=").append(note.getId()).append("\" class=\"btn\">Cancel</a>\n");
            sb.append("  </form>\n");
            sb.append("</div>\n");

            String page = Layout.page("Delete Note", "notes", sb.toString(), folderDAO, tagDAO, noteDAO);
            RequestHelper.sendHtml(exchange, 200, page);
        }
    }
}
