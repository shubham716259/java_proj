package com.notes.web;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.helper.HtmlUtil;
import com.notes.model.Folder;
import com.notes.model.Tag;
import com.notes.model.User;

import java.util.List;
import java.util.Map;

public class Layout {

    public static String page(String title, String activeNav, String bodyContent,
                              FolderDAO folderDAO, TagDAO tagDAO, NoteDAO noteDAO, User user) {
        int userId = user.getId();
        List<Folder> folders = folderDAO.getAllFolders(userId);
        Map<Integer, Integer> folderCounts = folderDAO.getFolderCounts(userId);
        List<Tag> tags = tagDAO.getAllTagsForUser(userId);
        int totalNotes = noteDAO.countAll(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("  <title>").append(HtmlUtil.escape(title)).append(" - Smart Notes</title>\n");
        sb.append("  <link rel=\"stylesheet\" href=\"/static/style.css\">\n");
        sb.append("</head>\n<body>\n");

        // Sidebar
        sb.append("  <aside class=\"sidebar\">\n");
        sb.append("    <a href=\"/\" class=\"brand\">Smart Notes\n");
        sb.append("      <span class=\"brand-sub\">Personal Knowledge Base</span>\n");
        sb.append("    </a>\n");

        sb.append("    <ul class=\"nav-list\">\n");
        sb.append("      <li><a href=\"/\" class=\"nav-item ").append("dashboard".equals(activeNav) ? "active" : "").append("\">Dashboard</a></li>\n");
        sb.append("      <li><a href=\"/notes\" class=\"nav-item ").append("notes".equals(activeNav) ? "active" : "").append("\">All Notes <span class=\"count-badge\">").append(totalNotes).append("</span></a></li>\n");
        sb.append("      <li><a href=\"/notes/new\" class=\"nav-item ").append("new".equals(activeNav) ? "active" : "").append("\">New Note</a></li>\n");
        sb.append("      <li><a href=\"/folders\" class=\"nav-item ").append("folders".equals(activeNav) ? "active" : "").append("\">Folders <span class=\"count-badge\">").append(folders.size()).append("</span></a></li>\n");
        sb.append("    </ul>\n");

        // Folders list in sidebar
        sb.append("    <div class=\"sidebar-heading\">Folders</div>\n");
        sb.append("    <ul class=\"nav-list\">\n");
        if (folders.isEmpty()) {
            sb.append("      <li style=\"padding: 4px 10px; font-size: 12.5px; color: var(--text-muted);\">No folders yet</li>\n");
        } else {
            for (Folder f : folders) {
                int cnt = folderCounts.getOrDefault(f.getId(), 0);
                sb.append("      <li><a href=\"/notes?folder=").append(f.getId()).append("\" class=\"nav-item\">");
                sb.append("<span>").append(HtmlUtil.escape(f.getName())).append("</span>");
                sb.append("<span class=\"count-badge\">").append(cnt).append("</span></a></li>\n");
            }
        }
        sb.append("    </ul>\n");

        // Tags in sidebar
        sb.append("    <div class=\"sidebar-heading\">Tags</div>\n");
        sb.append("    <div style=\"padding: 4px 10px; display: flex; flex-wrap: wrap; gap: 8px;\">\n");
        if (tags.isEmpty()) {
            sb.append("      <span style=\"font-size: 12.5px; color: var(--text-muted);\">No tags yet</span>\n");
        } else {
            for (Tag t : tags) {
                sb.append("      <a href=\"/notes?tag=").append(t.getId()).append("\" class=\"tag-chip\">#")
                  .append(HtmlUtil.escape(t.getName())).append("</a>\n");
            }
        }
        sb.append("    </div>\n");

        // User session section in sidebar
        String initial = (user.getUsername() != null && !user.getUsername().isEmpty())
                ? user.getUsername().substring(0, 1).toUpperCase()
                : "U";

        sb.append("    <div class=\"sidebar-user\">\n");
        sb.append("      <div class=\"user-meta\">\n");
        sb.append("        <span class=\"user-avatar\">").append(initial).append("</span>\n");
        sb.append("        <span class=\"user-name\">").append(HtmlUtil.escape(user.getUsername())).append("</span>\n");
        sb.append("      </div>\n");
        sb.append("      <form method=\"POST\" action=\"/logout\" style=\"margin: 0;\">\n");
        sb.append("        <button type=\"submit\" class=\"logout-link-btn\">Log out</button>\n");
        sb.append("      </form>\n");
        sb.append("    </div>\n");

        // Human student footer line
        sb.append("    <div class=\"sidebar-footer\">\n");
        sb.append("      Notes project, CSE\n");
        sb.append("    </div>\n");
        sb.append("  </aside>\n");

        // Main content
        sb.append("  <div class=\"main\">\n");
        sb.append("    <div class=\"topbar\">\n");
        sb.append("      <form action=\"/notes\" method=\"GET\" class=\"search-box\">\n");
        sb.append("        <input type=\"text\" name=\"q\" placeholder=\"Search notes...\" />\n");
        sb.append("      </form>\n");
        sb.append("      <div style=\"display: flex; align-items: center; gap: 10px;\">\n");
        sb.append("        <a href=\"/notes/new\" class=\"btn btn-primary\">New note</a>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"content\">\n");
        sb.append(bodyContent);
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        sb.append("</body>\n</html>\n");
        return sb.toString();
    }
}
