package com.notes.web;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.dao.UserDAO;
import com.notes.helper.HtmlUtil;
import com.notes.helper.RequestHelper;
import com.notes.helper.SessionHelper;
import com.notes.model.Note;
import com.notes.model.Tag;
import com.notes.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

public class DashboardHandler implements HttpHandler {

    private final NoteDAO noteDAO;
    private final FolderDAO folderDAO;
    private final TagDAO tagDAO;
    private final UserDAO userDAO;

    public DashboardHandler(NoteDAO noteDAO, FolderDAO folderDAO, TagDAO tagDAO, UserDAO userDAO) {
        this.noteDAO = noteDAO;
        this.folderDAO = folderDAO;
        this.tagDAO = tagDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!"/".equals(path)) {
            RequestHelper.sendHtml(exchange, 404, "<h1>404 Not Found</h1>");
            return;
        }

        User user = SessionHelper.getUser(exchange, userDAO);
        if (user == null) {
            RequestHelper.redirect(exchange, "/login");
            return;
        }

        List<Note> pinned = noteDAO.getPinnedNotes(user.getId());
        List<Note> recent = noteDAO.getRecentNotes(user.getId(), 10);

        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 20px;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 class=\"page-title\">Dashboard</h1>\n");
        sb.append("    <div class=\"page-desc\">Pinned items and your 10 most recently updated notes</div>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");

        // Pinned notes section
        sb.append("<div style=\"margin-bottom: 32px;\">\n");
        sb.append("  <div style=\"font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 10px;\">Pinned</div>\n");
        if (pinned.isEmpty()) {
            sb.append("  <div class=\"empty-state\">No pinned notes. You can pin important notes to keep them here.</div>\n");
        } else {
            sb.append("  <div class=\"note-list\">\n");
            for (Note n : pinned) {
                renderNoteRow(sb, n, null);
            }
            sb.append("  </div>\n");
        }
        sb.append("</div>\n");

        // Recently updated section
        sb.append("<div>\n");
        sb.append("  <div style=\"font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 10px;\">Recently updated</div>\n");
        if (recent.isEmpty()) {
            sb.append("  <div class=\"empty-state\">Nothing here yet. Create your first note.</div>\n");
        } else {
            sb.append("  <div class=\"note-list\">\n");
            for (Note n : recent) {
                renderNoteRow(sb, n, null);
            }
            sb.append("  </div>\n");
        }
        sb.append("</div>\n");

        String page = Layout.page("Dashboard", "dashboard", sb.toString(), folderDAO, tagDAO, noteDAO, user);
        RequestHelper.sendHtml(exchange, 200, page);
    }

    public static void renderNoteRow(StringBuilder sb, Note note, String searchKeyword) {
        sb.append("    <a href=\"/notes/view?id=").append(note.getId()).append("\" class=\"note-row ")
          .append(note.isPinned() ? "is-pinned" : "").append("\">\n");

        sb.append("      <div class=\"note-row-top\">\n");
        sb.append("        <div class=\"note-row-title\">");
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            sb.append(HtmlUtil.highlight(note.getTitle(), searchKeyword));
        } else {
            sb.append(HtmlUtil.escape(note.getTitle()));
        }
        sb.append("</div>\n");

        if (note.isPinned()) {
            sb.append("        <span class=\"pin-label\">Pinned</span>\n");
        }
        sb.append("      </div>\n");

        sb.append("      <div class=\"note-row-snippet\">");
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            sb.append(HtmlUtil.highlight(note.getSnippet(130), searchKeyword));
        } else {
            sb.append(HtmlUtil.escape(note.getSnippet(130)));
        }
        sb.append("</div>\n");

        sb.append("      <div class=\"note-row-meta\">\n");
        if (note.getFolder() != null) {
            sb.append("        <span class=\"folder-label\">").append(HtmlUtil.escape(note.getFolder().getName())).append("</span>\n");
        }
        if (note.getTags() != null) {
            for (Tag t : note.getTags()) {
                sb.append("        <span class=\"tag-chip\">#").append(HtmlUtil.escape(t.getName())).append("</span>\n");
            }
        }
        String dateStr = note.getUpdatedAt() != null ? note.getUpdatedAt().toString().substring(0, 10) : "";
        sb.append("        <span style=\"margin-left: auto;\">").append(dateStr).append("</span>\n");
        sb.append("      </div>\n");

        sb.append("    </a>\n");
    }
}
