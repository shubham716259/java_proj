package com.notes.web;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.dao.UserDAO;
import com.notes.helper.HtmlUtil;
import com.notes.helper.RequestHelper;
import com.notes.helper.SessionHelper;
import com.notes.helper.SuggestionHelper;
import com.notes.model.Note;
import com.notes.model.Tag;
import com.notes.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoteViewHandler implements HttpHandler {

    private final NoteDAO noteDAO;
    private final FolderDAO folderDAO;
    private final TagDAO tagDAO;
    private final UserDAO userDAO;

    public NoteViewHandler(NoteDAO noteDAO, FolderDAO folderDAO, TagDAO tagDAO, UserDAO userDAO) {
        this.noteDAO = noteDAO;
        this.folderDAO = folderDAO;
        this.tagDAO = tagDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = SessionHelper.getUser(exchange, userDAO);
        if (user == null) {
            RequestHelper.redirect(exchange, "/login");
            return;
        }

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

        Note note = noteDAO.getNoteById(id, user.getId());
        if (note == null) {
            String err = "<div class=\"msg-alert msg-error\">Note not found.</div><a href=\"/notes\" class=\"btn\">&larr; Back to notes</a>";
            RequestHelper.sendHtml(exchange, 404, Layout.page("Note Not Found", "notes", err, folderDAO, tagDAO, noteDAO, user));
            return;
        }

        // Suggestions logic: extract keywords with HashMap, find related notes & tags
        List<String> keywords = SuggestionHelper.getKeywords(note);
        List<Note> allNotes = noteDAO.getAllNotes(user.getId());
        List<Note> relatedNotes = SuggestionHelper.getRelatedNotes(note, allNotes, keywords);
        Set<String> relatedTags = SuggestionHelper.getRelatedTags(note, relatedNotes);

        StringBuilder sb = new StringBuilder();

        // Action toolbar
        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;\">\n");
        sb.append("  <a href=\"/notes\" class=\"btn btn-sm\">&larr; Back to notes</a>\n");
        sb.append("  <div style=\"display: flex; gap: 8px;\">\n");

        // Pin form
        sb.append("    <form method=\"POST\" action=\"/notes/pin\" style=\"display: inline;\">\n");
        sb.append("      <input type=\"hidden\" name=\"id\" value=\"").append(note.getId()).append("\" />\n");
        sb.append("      <input type=\"hidden\" name=\"from\" value=\"/notes/view?id=").append(note.getId()).append("\" />\n");
        sb.append("      <button type=\"submit\" class=\"btn btn-sm\">");
        sb.append(note.isPinned() ? "Unpin note" : "Pin note");
        sb.append("</button>\n");
        sb.append("    </form>\n");

        sb.append("    <a href=\"/notes/edit?id=").append(note.getId()).append("\" class=\"btn btn-sm\">Edit</a>\n");
        sb.append("    <a href=\"/notes/delete?id=").append(note.getId()).append("\" class=\"btn btn-sm btn-danger\">Delete</a>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");

        // Note view card
        sb.append("<article class=\"view-panel\">\n");
        sb.append("  <h1 class=\"view-title\">").append(HtmlUtil.escape(note.getTitle())).append("</h1>\n");

        sb.append("  <div class=\"view-meta\">\n");
        if (note.getFolder() != null) {
            sb.append("    <span class=\"folder-label\">").append(HtmlUtil.escape(note.getFolder().getName())).append("</span>\n");
        }
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            for (Tag t : note.getTags()) {
                sb.append("    <a href=\"/notes?tag=").append(t.getId()).append("\" class=\"tag-chip\">#")
                  .append(HtmlUtil.escape(t.getName())).append("</a>\n");
            }
        }
        String updated = note.getUpdatedAt() != null ? note.getUpdatedAt().toString().substring(0, 16) : "";
        sb.append("    <span style=\"margin-left: auto;\">Updated: ").append(updated).append("</span>\n");
        sb.append("  </div>\n");

        // Content
        sb.append("  <div class=\"view-body\">\n");
        sb.append(HtmlUtil.formatContent(note.getContent()));
        sb.append("  </div>\n");

        // Suggestions and keywords section
        sb.append("  <div class=\"view-suggestions\">\n");
        sb.append("    <div class=\"suggestions-heading\">Extracted keywords</div>\n");
        if (keywords.isEmpty()) {
            sb.append("    <div style=\"font-size: 13px; color: var(--text-muted);\">No distinctive keywords extracted.</div>\n");
        } else {
            sb.append("    <div style=\"display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px;\">\n");
            for (String kw : keywords) {
                sb.append("      <a href=\"/notes?q=").append(HtmlUtil.escape(kw)).append("\" class=\"tag-chip\" style=\"background: var(--bg-hover); padding: 2px 8px;\">")
                  .append(HtmlUtil.escape(kw)).append("</a>\n");
            }
            sb.append("    </div>\n");
        }

        // Related notes
        if (!relatedNotes.isEmpty()) {
            sb.append("    <div class=\"suggestions-heading\" style=\"margin-top: 18px;\">Related notes</div>\n");
            sb.append("    <div class=\"note-list\" style=\"margin-bottom: 14px;\">\n");
            for (Note rel : relatedNotes) {
                DashboardHandler.renderNoteRow(sb, rel, null);
            }
            sb.append("    </div>\n");
        }

        // Related tags using HashSet
        if (!relatedTags.isEmpty()) {
            sb.append("    <div class=\"suggestions-heading\" style=\"margin-top: 14px;\">You might also explore tags</div>\n");
            sb.append("    <div style=\"display: flex; gap: 8px; flex-wrap: wrap;\">\n");
            for (String t : relatedTags) {
                sb.append("      <a href=\"/notes?q=").append(HtmlUtil.escape(t)).append("\" class=\"tag-chip\">#")
                  .append(HtmlUtil.escape(t)).append("</a>\n");
            }
            sb.append("    </div>\n");
        }

        sb.append("  </div>\n");
        sb.append("</article>\n");

        String page = Layout.page(note.getTitle(), "notes", sb.toString(), folderDAO, tagDAO, noteDAO, user);
        RequestHelper.sendHtml(exchange, 200, page);
    }
}
