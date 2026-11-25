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

        // In-page search bar
        sb.append("<div style=\"margin-bottom: 28px;\">\n");
        sb.append("  <form action=\"/notes\" method=\"GET\" class=\"search-bar\">\n");
        sb.append("    <svg class=\"search-icon\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><circle cx=\"11\" cy=\"11\" r=\"8\"/><line x1=\"21\" y1=\"21\" x2=\"16.65\" y2=\"16.65\"/></svg>\n");
        sb.append("    <input type=\"text\" name=\"q\" class=\"search-input\" placeholder=\"Search across all notes...\" />\n");
        sb.append("    <kbd class=\"search-kbd-hint\" title=\"Press Ctrl+K or / to search\"><span class=\"search-kbd-key\">Ctrl</span><span class=\"search-kbd-key\">K</span></kbd>\n");
        sb.append("  </form>\n");
        sb.append("</div>\n");

        // Pinned notes section
        sb.append("<div style=\"margin-bottom: 32px;\">\n");
        sb.append("  <div style=\"font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 10px;\">Pinned</div>\n");
        if (pinned.isEmpty()) {
            sb.append("  <div class=\"empty-state\">\n");
            sb.append("    <svg class=\"empty-state-icon\" width=\"48\" height=\"48\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6\"/></svg>\n");
            sb.append("    <p class=\"empty-state-text\">No pinned notes yet. Pin important notes to keep them here.</p>\n");
            sb.append("  </div>\n");
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
            sb.append("  <div class=\"empty-state\">\n");
            sb.append("    <svg class=\"empty-state-icon\" width=\"48\" height=\"48\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\"><path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"/><polyline points=\"14 2 14 8 20 8\"/><line x1=\"16\" y1=\"13\" x2=\"8\" y2=\"13\"/><line x1=\"16\" y1=\"17\" x2=\"8\" y2=\"17\"/><line x1=\"10\" y1=\"9\" x2=\"8\" y2=\"9\"/></svg>\n");
            sb.append("    <p class=\"empty-state-text\">Nothing here yet. Create your first note.</p>\n");
            sb.append("    <a href=\"/notes/new\" class=\"btn btn-primary btn-sm empty-state-btn\">Create your first note</a>\n");
            sb.append("  </div>\n");
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
