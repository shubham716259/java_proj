package com.notes.web;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.helper.HtmlUtil;
import com.notes.helper.RequestHelper;
import com.notes.model.Folder;
import com.notes.model.Note;
import com.notes.model.Tag;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoteFormHandler implements HttpHandler {

    private final NoteDAO noteDAO;
    private final FolderDAO folderDAO;
    private final TagDAO tagDAO;

    public NoteFormHandler(NoteDAO noteDAO, FolderDAO folderDAO, TagDAO tagDAO) {
        this.noteDAO = noteDAO;
        this.folderDAO = folderDAO;
        this.tagDAO = tagDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePost(exchange);
        } else {
            handleGet(exchange, null);
        }
    }

    private void handleGet(HttpExchange exchange, String errorMessage) throws IOException {
        Map<String, String> params = RequestHelper.getQueryParams(exchange);
        String idStr = params.get("id");

        Note note = null;
        if (idStr != null && !idStr.isEmpty()) {
            try {
                note = noteDAO.getNoteById(Integer.parseInt(idStr));
            } catch (NumberFormatException ignored) {}
        }

        renderForm(exchange, note, errorMessage);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> form = RequestHelper.getFormData(exchange);

        int id = 0;
        String idStr = form.get("id");
        if (idStr != null && !idStr.isEmpty()) {
            try { id = Integer.parseInt(idStr); } catch (NumberFormatException ignored) {}
        }

        String title = form.getOrDefault("title", "").trim();
        String content = form.getOrDefault("content", "").trim();
        String folderStr = form.getOrDefault("folder_id", "").trim();
        String tagsInput = form.getOrDefault("tags", "").trim();
        boolean isPinned = "on".equalsIgnoreCase(form.get("is_pinned")) || "true".equalsIgnoreCase(form.get("is_pinned"));

        // Validation
        if (title.isEmpty()) {
            Note temp = new Note(id, title, content, null, isPinned, null, null);
            renderForm(exchange, temp, "Title cannot be empty.");
            return;
        }

        if (title.length() > 255) {
            Note temp = new Note(id, title, content, null, isPinned, null, null);
            renderForm(exchange, temp, "Title is too long (maximum 255 characters).");
            return;
        }

        Folder folder = null;
        if (!folderStr.isEmpty()) {
            try {
                folder = folderDAO.getFolderById(Integer.parseInt(folderStr));
            } catch (NumberFormatException ignored) {}
        }

        // parse comma-separated tags into Tag objects
        Set<Tag> tags = new HashSet<>();
        if (!tagsInput.isEmpty()) {
            String[] split = tagsInput.split("[,;]+");
            for (String s : split) {
                Tag t = tagDAO.findOrCreate(s);
                if (t != null) tags.add(t);
            }
        }

        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setContent(content);
        note.setFolder(folder);
        note.setTags(tags);
        note.setPinned(isPinned);

        if (id > 0) {
            noteDAO.updateNote(note);
            RequestHelper.redirect(exchange, "/notes/view?id=" + id);
        } else {
            Note saved = noteDAO.createNote(note);
            if (saved != null) {
                RequestHelper.redirect(exchange, "/notes/view?id=" + saved.getId());
            } else {
                renderForm(exchange, note, "Error saving note to database.");
            }
        }
    }

    private void renderForm(HttpExchange exchange, Note note, String errorMessage) throws IOException {
        boolean isEdit = (note != null && note.getId() > 0);
        String formTitle = isEdit ? "Edit note" : "New note";
        List<Folder> folders = folderDAO.getAllFolders();

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"form-box\">\n");
        sb.append("  <div style=\"display:flex; justify-content:space-between; align-items:baseline; margin-bottom:16px;\">\n");
        sb.append("    <h1 class=\"page-title\" style=\"font-size:20px;\">").append(formTitle).append("</h1>\n");
        sb.append("    <a href=\"/notes\" class=\"btn btn-sm\">Cancel</a>\n");
        sb.append("  </div>\n");

        if (errorMessage != null) {
            sb.append("  <div class=\"msg-alert msg-error\">").append(HtmlUtil.escape(errorMessage)).append("</div>\n");
        }

        sb.append("  <form method=\"POST\" action=\"").append(isEdit ? "/notes/edit" : "/notes/new").append("\">\n");
        if (isEdit) {
            sb.append("    <input type=\"hidden\" name=\"id\" value=\"").append(note.getId()).append("\" />\n");
        }

        // Title
        sb.append("    <div class=\"form-group\">\n");
        sb.append("      <label class=\"form-label\" for=\"title\">Title</label>\n");
        sb.append("      <input type=\"text\" id=\"title\" name=\"title\" class=\"form-input\" required maxlength=\"255\" placeholder=\"Note title\" value=\"")
          .append(note != null ? HtmlUtil.escape(note.getTitle()) : "").append("\" />\n");
        sb.append("    </div>\n");

        // Folder
        sb.append("    <div class=\"form-group\">\n");
        sb.append("      <label class=\"form-label\" for=\"folder_id\">Folder</label>\n");
        sb.append("      <select id=\"folder_id\" name=\"folder_id\" class=\"form-input\" style=\"padding:6px 10px;\">\n");
        sb.append("        <option value=\"\">None (unfiled)</option>\n");
        int currentFolderId = (note != null && note.getFolder() != null) ? note.getFolder().getId() : -1;
        for (Folder f : folders) {
            boolean sel = (f.getId() == currentFolderId);
            sb.append("        <option value=\"").append(f.getId()).append("\" ").append(sel ? "selected" : "").append(">");
            sb.append(HtmlUtil.escape(f.getName())).append("</option>\n");
        }
        sb.append("      </select>\n");
        sb.append("    </div>\n");

        // Tags
        String tagsStr = (note != null) ? note.getTagsAsString() : "";
        sb.append("    <div class=\"form-group\">\n");
        sb.append("      <label class=\"form-label\" for=\"tags\">Tags</label>\n");
        sb.append("      <input type=\"text\" id=\"tags\" name=\"tags\" class=\"form-input\" placeholder=\"e.g. java, oop, exam\" value=\"")
          .append(HtmlUtil.escape(tagsStr)).append("\" />\n");
        sb.append("      <div class=\"form-help\">Comma-separated. New tags will be added automatically.</div>\n");
        sb.append("    </div>\n");

        // Content
        sb.append("    <div class=\"form-group\">\n");
        sb.append("      <label class=\"form-label\" for=\"content\">Content</label>\n");
        sb.append("      <textarea id=\"content\" name=\"content\" class=\"form-input\" placeholder=\"Write your notes here...\">")
          .append(note != null ? HtmlUtil.escape(note.getContent()) : "").append("</textarea>\n");
        sb.append("    </div>\n");

        // Pinned checkbox
        boolean isPinned = (note != null && note.isPinned());
        sb.append("    <div class=\"form-group\">\n");
        sb.append("      <label style=\"display:flex; align-items:center; gap:8px; font-size:13px; cursor:pointer;\">\n");
        sb.append("        <input type=\"checkbox\" name=\"is_pinned\" ").append(isPinned ? "checked" : "").append(" />\n");
        sb.append("        <span>Pin this note to the top</span>\n");
        sb.append("      </label>\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"display:flex; gap:10px; margin-top:20px;\">\n");
        sb.append("      <button type=\"submit\" class=\"btn btn-primary\">").append(isEdit ? "Update note" : "Save note").append("</button>\n");
        sb.append("      <a href=\"/notes\" class=\"btn\">Cancel</a>\n");
        sb.append("    </div>\n");

        sb.append("  </form>\n");
        sb.append("</div>\n");

        String page = Layout.page(formTitle, isEdit ? "notes" : "new", sb.toString(), folderDAO, tagDAO, noteDAO);
        RequestHelper.sendHtml(exchange, 200, page);
    }
}
