package handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import dao.FolderDAO;
import dao.NoteDAO;
import dao.TagDAO;
import model.Folder;
import model.Note;
import model.Tag;
import service.SearchService;
import service.SuggestionService;

import java.io.IOException;
import java.util.*;

/**
 * REST Handler for Note operations.
 */
public class NoteHandler extends BaseHandler {

    private final NoteDAO noteDAO;
    private final FolderDAO folderDAO;
    private final TagDAO tagDAO;
    private final SearchService searchService;
    private final SuggestionService suggestionService;

    public NoteHandler(NoteDAO noteDAO, FolderDAO folderDAO, TagDAO tagDAO,
                       SearchService searchService, SuggestionService suggestionService) {
        this.noteDAO = noteDAO;
        this.folderDAO = folderDAO;
        this.tagDAO = tagDAO;
        this.searchService = searchService;
        this.suggestionService = suggestionService;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        // 1. GET /api/notes/recent
        if ("GET".equals(method) && "/api/notes/recent".equals(path)) {
            handleGetRecent(exchange);
            return;
        }

        // 2. PATCH /api/notes/{id}/pin
        if (("PATCH".equals(method) || "POST".equals(method)) && path.matches(".*/api/notes/\\d+/pin/?$")) {
            Integer id = extractIdFromPath(path, "/api/notes");
            handleTogglePin(exchange, id);
            return;
        }

        // 3. /api/notes/{id} (GET, PUT, DELETE)
        if (path.matches(".*/api/notes/\\d+/?$")) {
            Integer id = extractIdFromPath(path, "/api/notes");
            if (id == null) {
                sendError(exchange, 400, "Invalid note ID.");
                return;
            }

            switch (method) {
                case "GET":
                    handleGetOne(exchange, id);
                    break;
                case "PUT":
                    handleUpdate(exchange, id);
                    break;
                case "DELETE":
                    handleDelete(exchange, id);
                    break;
                default:
                    sendError(exchange, 405, "Method Not Allowed");
            }
            return;
        }

        // 4. /api/notes (GET list/search, POST create)
        if ("/api/notes".equals(path) || "/api/notes/".equals(path)) {
            switch (method) {
                case "GET":
                    handleList(exchange);
                    break;
                case "POST":
                    handleCreate(exchange);
                    break;
                default:
                    sendError(exchange, 405, "Method Not Allowed");
            }
            return;
        }

        sendError(exchange, 404, "Endpoint not found: " + path);
    }

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryParams(exchange);
        String q = params.getOrDefault("q", "").trim();
        String folderParam = params.getOrDefault("folderId", "").trim();
        String tagParam = params.getOrDefault("tag", "").trim();
        String sortBy = params.getOrDefault("sort", "date").trim();
        String order = params.getOrDefault("order", "desc").trim();

        int folderId = -1;
        if (!folderParam.isEmpty()) {
            try { folderId = Integer.parseInt(folderParam); } catch (NumberFormatException ignored) {}
        }

        int tagId = -1;
        if (!tagParam.isEmpty()) {
            try { tagId = Integer.parseInt(tagParam); } catch (NumberFormatException ignored) {}
        }

        List<Note> allNotes = noteDAO.getAllNotes();
        List<Note> result = searchService.searchAndFilter(allNotes, q, folderId, tagId, sortBy, order);

        Map<String, Object> response = new HashMap<>();
        response.put("notes", result);
        response.put("total", result.size());

        if (!q.isEmpty()) {
            List<String> relatedSearches = searchService.getRelatedSearches(result, q);
            response.put("relatedSearches", relatedSearches);
        }

        sendJson(exchange, 200, response);
    }

    private void handleGetRecent(HttpExchange exchange) throws IOException {
        List<Note> recent = noteDAO.getRecentNotes(10);
        sendJson(exchange, 200, recent);
    }

    private void handleGetOne(HttpExchange exchange, int id) throws IOException {
        Note note = noteDAO.getNoteById(id);
        if (note == null) {
            sendError(exchange, 404, "Note not found with ID: " + id);
            return;
        }

        // Generate keywords and related notes recommendations
        List<String> keywords = suggestionService.extractKeywords(note.getContent());
        List<Note> allNotes = noteDAO.getAllNotes();
        List<Note> relatedNotes = suggestionService.getRelatedNotes(note, allNotes, keywords);

        Map<String, Object> response = new HashMap<>();
        response.put("note", note);
        response.put("keywords", keywords);
        response.put("relatedNotes", relatedNotes);

        sendJson(exchange, 200, response);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        if (body.isEmpty()) {
            sendError(exchange, 400, "Request body cannot be empty.");
            return;
        }

        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            sendError(exchange, 400, "Malformed JSON payload.");
            return;
        }

        String title = json.has("title") ? json.get("title").getAsString().trim() : "";
        String content = json.has("content") ? json.get("content").getAsString().trim() : "";
        boolean isPinned = json.has("isPinned") && json.get("isPinned").getAsBoolean();

        // Validation
        if (title.isEmpty()) {
            sendError(exchange, 400, "Note title is required.");
            return;
        }
        if (title.length() > 255) {
            sendError(exchange, 400, "Title cannot exceed 255 characters.");
            return;
        }

        Folder folder = null;
        if (json.has("folderId") && !json.get("folderId").isJsonNull()) {
            int folderId = json.get("folderId").getAsInt();
            if (folderId > 0) {
                folder = folderDAO.getFolderById(folderId);
            }
        }

        Set<Tag> tags = parseTagsFromJson(json);

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setFolder(folder);
        note.setTags(tags);
        note.setPinned(isPinned);

        Note created = noteDAO.createNote(note);
        if (created != null) {
            sendJson(exchange, 201, created);
        } else {
            sendError(exchange, 500, "Failed to create note in database.");
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws IOException {
        Note existing = noteDAO.getNoteById(id);
        if (existing == null) {
            sendError(exchange, 404, "Note not found with ID: " + id);
            return;
        }

        String body = readRequestBody(exchange);
        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            sendError(exchange, 400, "Malformed JSON payload.");
            return;
        }

        String title = json.has("title") ? json.get("title").getAsString().trim() : existing.getTitle();
        String content = json.has("content") ? json.get("content").getAsString().trim() : existing.getContent();
        boolean isPinned = json.has("isPinned") ? json.get("isPinned").getAsBoolean() : existing.isPinned();

        if (title.isEmpty()) {
            sendError(exchange, 400, "Note title cannot be empty.");
            return;
        }
        if (title.length() > 255) {
            sendError(exchange, 400, "Title cannot exceed 255 characters.");
            return;
        }

        Folder folder = null;
        if (json.has("folderId")) {
            if (!json.get("folderId").isJsonNull()) {
                int folderId = json.get("folderId").getAsInt();
                if (folderId > 0) {
                    folder = folderDAO.getFolderById(folderId);
                }
            }
        } else {
            folder = existing.getFolder();
        }

        Set<Tag> tags = json.has("tags") ? parseTagsFromJson(json) : existing.getTags();

        existing.setTitle(title);
        existing.setContent(content);
        existing.setFolder(folder);
        existing.setTags(tags);
        existing.setPinned(isPinned);

        boolean updated = noteDAO.updateNote(existing);
        if (updated) {
            sendJson(exchange, 200, noteDAO.getNoteById(id));
        } else {
            sendError(exchange, 500, "Failed to update note.");
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        Note existing = noteDAO.getNoteById(id);
        if (existing == null) {
            sendError(exchange, 404, "Note not found with ID: " + id);
            return;
        }

        boolean deleted = noteDAO.deleteNote(id);
        if (deleted) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Note deleted successfully.");
            sendJson(exchange, 200, resp);
        } else {
            sendError(exchange, 500, "Failed to delete note.");
        }
    }

    private void handleTogglePin(HttpExchange exchange, int id) throws IOException {
        Note existing = noteDAO.getNoteById(id);
        if (existing == null) {
            sendError(exchange, 404, "Note not found with ID: " + id);
            return;
        }

        boolean toggled = noteDAO.togglePin(id);
        if (toggled) {
            Note updated = noteDAO.getNoteById(id);
            sendJson(exchange, 200, updated);
        } else {
            sendError(exchange, 500, "Failed to toggle pin state.");
        }
    }

    private Set<Tag> parseTagsFromJson(JsonObject json) {
        Set<Tag> tags = new HashSet<>();
        if (!json.has("tags")) return tags;

        JsonElement elem = json.get("tags");
        if (elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            for (JsonElement t : arr) {
                String tagName = t.getAsString().trim();
                Tag found = tagDAO.findOrCreateTag(tagName);
                if (found != null) tags.add(found);
            }
        } else if (elem.isJsonPrimitive()) {
            String tagStr = elem.getAsString().trim();
            if (!tagStr.isEmpty()) {
                String[] split = tagStr.split("[,;]+");
                for (String s : split) {
                    Tag found = tagDAO.findOrCreateTag(s.trim());
                    if (found != null) tags.add(found);
                }
            }
        }
        return tags;
    }
}
