package com.notes.web;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.dao.UserDAO;
import com.notes.helper.HtmlUtil;
import com.notes.helper.RequestHelper;
import com.notes.helper.SearchHelper;
import com.notes.helper.SearchHelper.SearchResult;
import com.notes.helper.SessionHelper;
import com.notes.model.Folder;
import com.notes.model.Note;
import com.notes.model.Tag;
import com.notes.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoteListHandler implements HttpHandler {

    private final NoteDAO noteDAO;
    private final FolderDAO folderDAO;
    private final TagDAO tagDAO;
    private final UserDAO userDAO;

    public NoteListHandler(NoteDAO noteDAO, FolderDAO folderDAO, TagDAO tagDAO, UserDAO userDAO) {
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

        String q = params.getOrDefault("q", "").trim();
        String folderParam = params.getOrDefault("folder", "").trim();
        String tagParam = params.getOrDefault("tag", "").trim();
        String sortParam = params.getOrDefault("sort", "date_desc").trim();

        // fetch base list based on folder or tag filter
        List<Note> pool;
        if (!folderParam.isEmpty()) {
            try {
                pool = noteDAO.getNotesByFolder(Integer.parseInt(folderParam), user.getId());
            } catch (NumberFormatException e) {
                pool = noteDAO.getAllNotes(user.getId());
            }
        } else if (!tagParam.isEmpty()) {
            try {
                pool = noteDAO.getNotesByTag(Integer.parseInt(tagParam), user.getId());
            } catch (NumberFormatException e) {
                pool = noteDAO.getAllNotes(user.getId());
            }
        } else {
            pool = noteDAO.getAllNotes(user.getId());
        }

        // if searching, score by matches and sort
        List<Note> displayList = new ArrayList<>();
        if (!q.isEmpty()) {
            List<SearchResult> searchResults = SearchHelper.search(pool, q);
            for (SearchResult sr : searchResults) {
                displayList.add(sr.note);
            }
        } else {
            displayList = pool;
            SearchHelper.sortNotes(displayList, sortParam);
        }

        List<Folder> allFolders = folderDAO.getAllFolders(user.getId());
        List<Tag> allTags = tagDAO.getAllTagsForUser(user.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n");
        sb.append("  <h1 class=\"page-title\">All Notes</h1>\n");
        sb.append("  <div class=\"page-desc\">").append(displayList.size()).append(" note(s) found</div>\n");
        sb.append("</div>\n");

        // Filter / sort bar
        sb.append("<form method=\"GET\" action=\"/notes\" class=\"filter-bar\">\n");
        sb.append("  <input type=\"text\" name=\"q\" class=\"form-input\" style=\"width: 220px; padding: 4px 8px; font-size: 13px;\" placeholder=\"Filter search...\" value=\"").append(HtmlUtil.escape(q)).append("\" />\n");

        // Folder select
        sb.append("  <select name=\"folder\">\n");
        sb.append("    <option value=\"\">All Folders</option>\n");
        for (Folder f : allFolders) {
            boolean sel = String.valueOf(f.getId()).equals(folderParam);
            sb.append("    <option value=\"").append(f.getId()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append(HtmlUtil.escape(f.getName())).append("</option>\n");
        }
        sb.append("  </select>\n");

        // Tag select
        sb.append("  <select name=\"tag\">\n");
        sb.append("    <option value=\"\">All Tags</option>\n");
        for (Tag t : allTags) {
            boolean sel = String.valueOf(t.getId()).equals(tagParam);
            sb.append("    <option value=\"").append(t.getId()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append("#").append(HtmlUtil.escape(t.getName())).append("</option>\n");
        }
        sb.append("  </select>\n");

        // Sort select
        sb.append("  <select name=\"sort\">\n");
        sb.append("    <option value=\"date_desc\" ").append("date_desc".equals(sortParam) ? "selected" : "").append(">Newest first</option>\n");
        sb.append("    <option value=\"date_asc\" ").append("date_asc".equals(sortParam) ? "selected" : "").append(">Oldest first</option>\n");
        sb.append("    <option value=\"title_asc\" ").append("title_asc".equals(sortParam) ? "selected" : "").append(">Title (A-Z)</option>\n");
        sb.append("    <option value=\"title_desc\" ").append("title_desc".equals(sortParam) ? "selected" : "").append(">Title (Z-A)</option>\n");
        sb.append("  </select>\n");

        sb.append("  <button type=\"submit\" class=\"btn btn-sm\">Apply</button>\n");
        if (!q.isEmpty() || !folderParam.isEmpty() || !tagParam.isEmpty()) {
            sb.append("  <a href=\"/notes\" class=\"btn btn-sm\" style=\"border-color: transparent;\">Clear filters</a>\n");
        }
        sb.append("</form>\n");

        if (!q.isEmpty()) {
            sb.append("<div style=\"font-size: 13.5px; margin-bottom: 14px; color: var(--text-muted);\">\n");
            sb.append("  Showing matches for &ldquo;<strong>").append(HtmlUtil.escape(q)).append("</strong>&rdquo;\n");
            sb.append("</div>\n");
        }

        if (displayList.isEmpty()) {
            sb.append("<div class=\"empty-state\">Nothing matches your search or filter.</div>\n");
        } else {
            sb.append("<div class=\"note-list\">\n");
            for (Note n : displayList) {
                DashboardHandler.renderNoteRow(sb, n, q);
            }
            sb.append("</div>\n");
        }

        String page = Layout.page("All Notes", "notes", sb.toString(), folderDAO, tagDAO, noteDAO, user);
        RequestHelper.sendHtml(exchange, 200, page);
    }
}
