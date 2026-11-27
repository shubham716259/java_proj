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

/**
 * Layout helper class.
 * Generates the master HTML shell (Head, Sidebar, Topbar, Content, Scripts)
 * for all authenticated pages in the Smart Notes application.
 */
public class Layout {

    public static String page(String title, String activeNav, String bodyContent,
                              FolderDAO folderDAO, TagDAO tagDAO, NoteDAO noteDAO, User user) {
        
        // ---------------------------------------------------------
        // 1. Fetch User Data for Sidebar
        // ---------------------------------------------------------
        int userId = user.getId();
        List<Folder> folders = folderDAO.getAllFolders(userId);
        Map<Integer, Integer> folderCounts = folderDAO.getFolderCounts(userId);
        List<Tag> tags = tagDAO.getAllTagsForUser(userId);
        int totalNotes = noteDAO.countAll(userId);

        StringBuilder sb = new StringBuilder();

        // ---------------------------------------------------------
        // 2. HTML HEAD & META CONFIGURATION
        // ---------------------------------------------------------
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("  <title>").append(HtmlUtil.escape(title)).append(" - Smart Notes</title>\n");
        sb.append("  <link rel=\"icon\" type=\"image/svg+xml\" href=\"/static/favicon.svg\">\n");
        sb.append("  <meta name=\"theme-color\" content=\"#6366f1\">\n");
        sb.append("  <link rel=\"stylesheet\" href=\"/static/style.css\">\n");

        // Immediate theme pre-hydration to prevent light/dark white screen flashes
        sb.append("  <script>\n");
        sb.append("    (function() {\n");
        sb.append("      var saved = localStorage.getItem('theme');\n");
        sb.append("      if (saved) {\n");
        sb.append("        document.documentElement.setAttribute('data-theme', saved);\n");
        sb.append("      } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {\n");
        sb.append("        document.documentElement.setAttribute('data-theme', 'dark');\n");
        sb.append("      } else {\n");
        sb.append("        document.documentElement.setAttribute('data-theme', 'light');\n");
        sb.append("      }\n");
        sb.append("    })();\n");
        sb.append("  </script>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");

        // ---------------------------------------------------------
        // 3. SIDEBAR NAVIGATION
        // ---------------------------------------------------------
        sb.append("  <aside class=\"sidebar\">\n");

        // --- Brand & Logo ---
        sb.append("    <a href=\"/\" class=\"brand\">\n");
        sb.append("      <div class=\"logo-container\">\n");
        sb.append("        <svg class=\"logo-icon\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"60 50 200 200\" width=\"40\" height=\"40\" aria-hidden=\"true\">\n");
        sb.append("          <rect x=\"60\" y=\"50\" width=\"200\" height=\"200\" rx=\"44\" fill=\"#6366f1\"/>\n");
        sb.append("          <path d=\"M100 80H196L220 104V220H100Z\" fill=\"#fff\"/>\n");
        sb.append("          <path d=\"M196 80V104H220Z\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <rect x=\"118\" y=\"124\" width=\"84\" height=\"8\" rx=\"4\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <rect x=\"118\" y=\"146\" width=\"64\" height=\"8\" rx=\"4\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <rect x=\"118\" y=\"168\" width=\"76\" height=\"8\" rx=\"4\" fill=\"#c7d2fe\"/>\n");
        sb.append("          <path d=\"M222 186L230 206L250 214L230 222L222 242L214 222L194 214L214 206Z\" fill=\"#fbbf24\" stroke=\"#6366f1\" stroke-width=\"5\" stroke-linejoin=\"round\"/>\n");
        sb.append("        </svg>\n");
        sb.append("        <div class=\"brand-text-wrap\">\n");
        sb.append("          <span class=\"logo-text\">Smart Notes</span>\n");
        sb.append("          <span class=\"brand-sub\">Personal Knowledge Base</span>\n");
        sb.append("        </div>\n");
        sb.append("      </div>\n");
        sb.append("    </a>\n");

        // --- Main Navigation Links ---
        sb.append("    <ul class=\"nav-list\">\n");

        // Link: Dashboard
        sb.append("      <li>\n");
        sb.append("        <a href=\"/\" class=\"nav-item ").append("dashboard".equals(activeNav) ? "active" : "").append("\">\n");
        sb.append("          <div class=\"nav-item-content\">\n");
        sb.append("            <svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"nav-item-icon\"><rect x=\"3\" y=\"3\" width=\"7\" height=\"9\" rx=\"1\"/><rect x=\"14\" y=\"3\" width=\"7\" height=\"5\" rx=\"1\"/><rect x=\"14\" y=\"12\" width=\"7\" height=\"9\" rx=\"1\"/><rect x=\"3\" y=\"16\" width=\"7\" height=\"5\" rx=\"1\"/></svg>\n");
        sb.append("            <span>Dashboard</span>\n");
        sb.append("          </div>\n");
        sb.append("        </a>\n");
        sb.append("      </li>\n");

        // Link: All Notes
        sb.append("      <li>\n");
        sb.append("        <a href=\"/notes\" class=\"nav-item ").append("notes".equals(activeNav) ? "active" : "").append("\">\n");
        sb.append("          <div class=\"nav-item-content\">\n");
        sb.append("            <svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"nav-item-icon\"><path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"/><polyline points=\"14 2 14 8 20 8\"/><line x1=\"16\" y1=\"13\" x2=\"8\" y2=\"13\"/><line x1=\"16\" y1=\"17\" x2=\"8\" y2=\"17\"/><line x1=\"10\" y1=\"9\" x2=\"8\" y2=\"9\"/></svg>\n");
        sb.append("            <span>All notes</span>\n");
        sb.append("          </div>\n");
        if (totalNotes > 0) {
            sb.append("          <span class=\"count-badge\">").append(totalNotes).append("</span>\n");
        }
        sb.append("        </a>\n");
        sb.append("      </li>\n");

        // Link: New Note
        sb.append("      <li>\n");
        sb.append("        <a href=\"/notes/new\" class=\"nav-item ").append("new".equals(activeNav) ? "active" : "").append("\">\n");
        sb.append("          <div class=\"nav-item-content\">\n");
        sb.append("            <svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"nav-item-icon\"><path d=\"M12 20h9\"/><path d=\"M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z\"/></svg>\n");
        sb.append("            <span>New note</span>\n");
        sb.append("          </div>\n");
        sb.append("        </a>\n");
        sb.append("      </li>\n");

        // Link: Folders
        sb.append("      <li>\n");
        sb.append("        <a href=\"/folders\" class=\"nav-item ").append("folders".equals(activeNav) ? "active" : "").append("\">\n");
        sb.append("          <div class=\"nav-item-content\">\n");
        sb.append("            <svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"nav-item-icon\"><path d=\"M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z\"/></svg>\n");
        sb.append("            <span>Folders</span>\n");
        sb.append("          </div>\n");
        if (!folders.isEmpty()) {
            sb.append("          <span class=\"count-badge\">").append(folders.size()).append("</span>\n");
        }
        sb.append("        </a>\n");
        sb.append("      </li>\n");
        sb.append("    </ul>\n");

        // --- Folders Subsection ---
        sb.append("    <div class=\"sidebar-heading\">Folders</div>\n");
        sb.append("    <ul class=\"nav-list\">\n");
        if (folders.isEmpty()) {
            sb.append("      <li style=\"padding: 6px 12px; font-size: 0.8rem; color: var(--muted);\">No folders yet</li>\n");
        } else {
            for (Folder f : folders) {
                int cnt = folderCounts.getOrDefault(f.getId(), 0);
                sb.append("      <li>\n");
                sb.append("        <a href=\"/notes?folder=").append(f.getId()).append("\" class=\"nav-item\">\n");
                sb.append("          <div class=\"nav-item-content\">\n");
                sb.append("            <svg width=\"15\" height=\"15\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"nav-item-icon\" style=\"opacity: 0.7;\"><path d=\"M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z\"/></svg>\n");
                sb.append("            <span>").append(HtmlUtil.escape(f.getName())).append("</span>\n");
                sb.append("          </div>\n");
                if (cnt > 0) {
                    sb.append("          <span class=\"count-badge\">").append(cnt).append("</span>\n");
                }
                sb.append("        </a>\n");
                sb.append("      </li>\n");
            }
        }
        sb.append("    </ul>\n");

        // --- Tags Subsection ---
        sb.append("    <div class=\"sidebar-heading\">Tags</div>\n");
        sb.append("    <div style=\"padding: 4px 6px; display: flex; flex-wrap: wrap; gap: 6px;\">\n");
        if (tags.isEmpty()) {
            sb.append("      <span style=\"font-size: 0.8rem; color: var(--muted); padding: 0 4px;\">No tags yet</span>\n");
        } else {
            for (Tag t : tags) {
                sb.append("      <a href=\"/notes?tag=").append(t.getId()).append("\" class=\"tag-chip\">#")
                  .append(HtmlUtil.escape(t.getName())).append("</a>\n");
            }
        }
        sb.append("    </div>\n");

        // --- User Session Profile & Logout ---
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

        // Sidebar Footer
        sb.append("    <div class=\"sidebar-footer\">Notes project, CSE</div>\n");
        sb.append("  </aside>\n");

        // ---------------------------------------------------------
        // 4. MAIN CONTAINER & TOPBAR
        // ---------------------------------------------------------
        sb.append("  <div class=\"main\">\n");
        sb.append("    <div class=\"topbar\">\n");

        // Topbar Search Form
        sb.append("      <form action=\"/notes\" method=\"GET\" class=\"topbar-search-form\">\n");
        sb.append("        <svg class=\"topbar-search-icon\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"11\" cy=\"11\" r=\"8\"/><line x1=\"21\" y1=\"21\" x2=\"16.65\" y2=\"16.65\"/></svg>\n");
        sb.append("        <input type=\"text\" name=\"q\" id=\"topbar-search-input\" class=\"topbar-search-input\" placeholder=\"Search notes... (Ctrl+K)\" autocomplete=\"off\" />\n");
        sb.append("        <kbd class=\"topbar-search-kbd\" title=\"Press Ctrl+K or / to search\"><span>Ctrl</span> <span>K</span></kbd>\n");
        sb.append("      </form>\n");

        // Topbar Right Actions (Theme Toggle & New Note Button)
        sb.append("      <div class=\"topbar-right\">\n");
        sb.append("        <button type=\"button\" class=\"theme-toggle-btn\" id=\"theme-toggle-btn\" onclick=\"toggleTheme()\" title=\"Toggle theme\" aria-label=\"Toggle theme\">\n");
        sb.append("          <svg class=\"theme-icon-moon\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M12 3a6 6 0 0 0 9 9 9 0 1 1-9-9Z\"/></svg>\n");
        sb.append("          <svg class=\"theme-icon-sun\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"4\"/><path d=\"M12 2v2\"/><path d=\"M12 20v2\"/><path d=\"m4.93 4.93 1.41 1.41\"/><path d=\"m17.66 17.66 1.41 1.41\"/><path d=\"M2 12h2\"/><path d=\"M20 12h2\"/><path d=\"m6.34 17.66-1.41 1.41\"/><path d=\"m19.07 4.93-1.41 1.41\"/></svg>\n");
        sb.append("        </button>\n");
        sb.append("        <a href=\"/notes/new\" class=\"btn btn-primary topbar-new-btn\">\n");
        sb.append("          <svg width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><line x1=\"12\" y1=\"5\" x2=\"12\" y2=\"19\"/><line x1=\"5\" y1=\"12\" x2=\"19\" y2=\"12\"/></svg>\n");
        sb.append("          <span>New note</span>\n");
        sb.append("        </a>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");

        // ---------------------------------------------------------
        // 5. INJECT PAGE BODY CONTENT
        // ---------------------------------------------------------
        sb.append("    <div class=\"content\">\n");
        sb.append(bodyContent);
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // ---------------------------------------------------------
        // 6. CLIENT-SIDE JAVASCRIPT LOGIC
        // ---------------------------------------------------------
        sb.append("  <script>\n");
        
        // Theme Update & Toggle functions
        sb.append("    function updateThemeUI(theme) {\n");
        sb.append("      var btn = document.getElementById('theme-toggle-btn');\n");
        sb.append("      if (btn) {\n");
        sb.append("        var isDark = theme === 'dark';\n");
        sb.append("        btn.setAttribute('title', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');\n");
        sb.append("        btn.setAttribute('aria-label', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    function toggleTheme() {\n");
        sb.append("      var curr = document.documentElement.getAttribute('data-theme') || 'light';\n");
        sb.append("      var next = curr === 'dark' ? 'light' : 'dark';\n");
        sb.append("      document.documentElement.setAttribute('data-theme', next);\n");
        sb.append("      localStorage.setItem('theme', next);\n");
        sb.append("      updateThemeUI(next);\n");
        sb.append("    }\n");
        sb.append("    (function() {\n");
        sb.append("      var curr = document.documentElement.getAttribute('data-theme') || 'light';\n");
        sb.append("      updateThemeUI(curr);\n");
        sb.append("    })();\n");

        // Search query auto-population from URL
        sb.append("    (function() {\n");
        sb.append("      var urlParams = new URLSearchParams(window.location.search);\n");
        sb.append("      var q = urlParams.get('q');\n");
        sb.append("      var inp = document.getElementById('topbar-search-input');\n");
        sb.append("      if (inp && q) {\n");
        sb.append("        inp.value = q;\n");
        sb.append("      }\n");
        sb.append("    })();\n");

        // Global Keyboard Shortcut: Ctrl+K or / to Focus Search
        sb.append("    document.addEventListener('keydown', function(e) {\n");
        sb.append("      if ((e.key === '/' || ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k')) &&\n");
        sb.append("          document.activeElement &&\n");
        sb.append("          document.activeElement.tagName !== 'INPUT' &&\n");
        sb.append("          document.activeElement.tagName !== 'TEXTAREA') {\n");
        sb.append("        e.preventDefault();\n");
        sb.append("        var inp = document.getElementById('topbar-search-input');\n");
        sb.append("        if (inp) {\n");
        sb.append("          inp.focus();\n");
        sb.append("          inp.select();\n");
        sb.append("        }\n");
        sb.append("      }\n");
        sb.append("      if (e.key === 'Escape') {\n");
        sb.append("        var inp = document.getElementById('topbar-search-input');\n");
        sb.append("        if (document.activeElement === inp) {\n");
        sb.append("          inp.blur();\n");
        sb.append("        }\n");
        sb.append("      }\n");
        sb.append("    });\n");
        sb.append("  </script>\n");

        sb.append("</body>\n");
        sb.append("</html>\n");
        
        return sb.toString();
    }
}
