package com.notes.web;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.dao.UserDAO;
import com.notes.helper.HtmlUtil;
import com.notes.helper.RequestHelper;
import com.notes.helper.SessionHelper;
import com.notes.model.Folder;
import com.notes.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FolderHandler implements HttpHandler {

    private final FolderDAO folderDAO;
    private final TagDAO tagDAO;
    private final NoteDAO noteDAO;
    private final UserDAO userDAO;

    public FolderHandler(FolderDAO folderDAO, TagDAO tagDAO, NoteDAO noteDAO, UserDAO userDAO) {
        this.folderDAO = folderDAO;
        this.tagDAO = tagDAO;
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

        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/delete")) {
            handleDelete(exchange, user);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePost(exchange, user);
        } else {
            handleGet(exchange, null, user);
        }
    }

    private void handlePost(HttpExchange exchange, User user) throws IOException {
        Map<String, String> form = RequestHelper.getFormData(exchange);
        String action = form.getOrDefault("action", "create");

        if ("create".equalsIgnoreCase(action)) {
            String name = form.getOrDefault("name", "").trim();
            if (name.isEmpty()) {
                handleGet(exchange, "Folder name cannot be empty.", user);
                return;
            }
            if (name.length() > 100) {
                handleGet(exchange, "Folder name is too long.", user);
                return;
            }
            Folder created = folderDAO.createFolder(name, user.getId());
            if (created == null) {
                handleGet(exchange, "Folder with this name already exists.", user);
                return;
            }
        } else if ("rename".equalsIgnoreCase(action)) {
            String idStr = form.get("id");
            String newName = form.getOrDefault("new_name", "").trim();
            if (idStr != null && !newName.isEmpty()) {
                try {
                    folderDAO.renameFolder(Integer.parseInt(idStr), newName, user.getId());
                } catch (NumberFormatException ignored) {}
            }
        }

        RequestHelper.redirect(exchange, "/folders");
    }

    private void handleGet(HttpExchange exchange, String errorMessage, User user) throws IOException {
        List<Folder> folders = folderDAO.getAllFolders(user.getId());
        Map<Integer, Integer> counts = folderDAO.getFolderCounts(user.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-header\">\n");
        sb.append("  <h1 class=\"page-title\">Folders</h1>\n");
        sb.append("  <div class=\"page-desc\">Organize notes into folders. Deleting a folder unlinks its notes without removing them.</div>\n");
        sb.append("</div>\n");

        if (errorMessage != null) {
            sb.append("<div class=\"msg-alert msg-error\">").append(HtmlUtil.escape(errorMessage)).append("</div>\n");
        }

        // New folder form
        sb.append("<div class=\"form-box\" style=\"margin-bottom: 24px;\">\n");
        sb.append("  <form method=\"POST\" action=\"/folders\" style=\"display: flex; gap: 8px; align-items: center;\">\n");
        sb.append("    <input type=\"hidden\" name=\"action\" value=\"create\" />\n");
        sb.append("    <input type=\"text\" name=\"name\" class=\"form-input\" placeholder=\"New folder name...\" required maxlength=\"100\" style=\"max-width: 320px;\" />\n");
        sb.append("    <button type=\"submit\" class=\"btn btn-primary\">Create folder</button>\n");
        sb.append("  </form>\n");
        sb.append("</div>\n");

        // Folders list table
        sb.append("<table class=\"simple-table\">\n");
        sb.append("  <thead>\n");
        sb.append("    <tr>\n");
        sb.append("      <th>Folder Name</th>\n");
        sb.append("      <th style=\"width: 120px;\">Notes</th>\n");
        sb.append("      <th style=\"width: 250px;\">Rename</th>\n");
        sb.append("      <th style=\"width: 80px;\">Action</th>\n");
        sb.append("    </tr>\n");
        sb.append("  </thead>\n");
        sb.append("  <tbody>\n");

        if (folders.isEmpty()) {
            sb.append("    <tr><td colspan=\"4\" style=\"text-align: center; color: var(--text-muted); padding: 20px;\">No folders yet.</td></tr>\n");
        } else {
            for (Folder f : folders) {
                int cnt = counts.getOrDefault(f.getId(), 0);
                sb.append("    <tr>\n");
                sb.append("      <td><a href=\"/notes?folder=").append(f.getId()).append("\" style=\"color: var(--text-ink); font-weight: 600; text-decoration: none;\">")
                  .append(HtmlUtil.escape(f.getName())).append("</a></td>\n");
                sb.append("      <td><span class=\"count-badge\">").append(cnt).append(" note(s)</span></td>\n");

                // Rename form
                sb.append("      <td>\n");
                sb.append("        <form method=\"POST\" action=\"/folders\" style=\"display: flex; gap: 6px;\">\n");
                sb.append("          <input type=\"hidden\" name=\"action\" value=\"rename\" />\n");
                sb.append("          <input type=\"hidden\" name=\"id\" value=\"").append(f.getId()).append("\" />\n");
                sb.append("          <input type=\"text\" name=\"new_name\" class=\"form-input\" style=\"padding: 3px 6px; font-size: 12px;\" value=\"").append(HtmlUtil.escape(f.getName())).append("\" required maxlength=\"100\" />\n");
                sb.append("          <button type=\"submit\" class=\"btn btn-sm\">Rename</button>\n");
                sb.append("        </form>\n");
                sb.append("      </td>\n");

                sb.append("      <td><a href=\"/folders/delete?id=").append(f.getId()).append("\" class=\"btn btn-sm btn-danger\">Delete</a></td>\n");
                sb.append("    </tr>\n");
            }
        }
        sb.append("  </tbody>\n");
        sb.append("</table>\n");

        String page = Layout.page("Folders", "folders", sb.toString(), folderDAO, tagDAO, noteDAO, user);
        RequestHelper.sendHtml(exchange, 200, page);
    }

    private void handleDelete(HttpExchange exchange, User user) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = RequestHelper.getFormData(exchange);
            String idStr = form.get("id");
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    folderDAO.deleteFolder(Integer.parseInt(idStr), user.getId());
                } catch (NumberFormatException ignored) {}
            }
            RequestHelper.redirect(exchange, "/folders");
        } else {
            // GET confirm page
            Map<String, String> params = RequestHelper.getQueryParams(exchange);
            String idStr = params.get("id");
            if (idStr == null || idStr.isEmpty()) {
                RequestHelper.redirect(exchange, "/folders");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                RequestHelper.redirect(exchange, "/folders");
                return;
            }

            Folder folder = folderDAO.getFolderById(id, user.getId());
            if (folder == null) {
                RequestHelper.redirect(exchange, "/folders");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<div class=\"form-box\" style=\"max-width: 500px;\">\n");
            sb.append("  <h1 class=\"page-title\" style=\"font-size: 20px;\">Delete folder</h1>\n");
            sb.append("  <p style=\"margin: 14px 0 16px; font-size: 14px;\">\n");
            sb.append("    Delete folder &ldquo;<strong>").append(HtmlUtil.escape(folder.getName())).append("</strong>&rdquo;?\n");
            sb.append("  </p>\n");
            sb.append("  <p style=\"font-size: 13px; color: var(--text-muted); margin-bottom: 20px;\">\n");
            sb.append("    Notes inside this folder will not be deleted. Their folder will simply be set to unfiled.\n");
            sb.append("  </p>\n");

            sb.append("  <form method=\"POST\" action=\"/folders/delete\" style=\"display: flex; gap: 10px;\">\n");
            sb.append("    <input type=\"hidden\" name=\"id\" value=\"").append(folder.getId()).append("\" />\n");
            sb.append("    <button type=\"submit\" class=\"btn btn-danger\">Yes, delete</button>\n");
            sb.append("    <a href=\"/folders\" class=\"btn\">Cancel</a>\n");
            sb.append("  </form>\n");
            sb.append("</div>\n");

            String page = Layout.page("Delete Folder", "folders", sb.toString(), folderDAO, tagDAO, noteDAO, user);
            RequestHelper.sendHtml(exchange, 200, page);
        }
    }
}
