package handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import dao.FolderDAO;
import model.Folder;

import java.io.IOException;
import java.util.*;

/**
 * REST Handler for Folder CRUD operations.
 */
public class FolderHandler extends BaseHandler {

    private final FolderDAO folderDAO;

    public FolderHandler(FolderDAO folderDAO) {
        this.folderDAO = folderDAO;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        // 1. /api/folders/{id} (PUT, DELETE)
        if (path.matches(".*/api/folders/\\d+/?$")) {
            Integer id = extractIdFromPath(path, "/api/folders");
            if (id == null) {
                sendError(exchange, 400, "Invalid folder ID.");
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

        // 2. /api/folders (GET list, POST create)
        if ("/api/folders".equals(path) || "/api/folders/".equals(path)) {
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
        List<Folder> folders = folderDAO.getAllFolders();
        Map<Integer, Integer> counts = folderDAO.getFolderNoteCounts();

        List<Map<String, Object>> response = new ArrayList<>();
        for (Folder f : folders) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("name", f.getName());
            map.put("noteCount", counts.getOrDefault(f.getId(), 0));
            response.add(map);
        }

        sendJson(exchange, 200, response);
    }

    private void handleGetOne(HttpExchange exchange, int id) throws IOException {
        Folder folder = folderDAO.getFolderById(id);
        if (folder == null) {
            sendError(exchange, 404, "Folder not found with ID: " + id);
            return;
        }
        sendJson(exchange, 200, folder);
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

        String name = json.has("name") ? json.get("name").getAsString().trim() : "";
        if (name.isEmpty()) {
            sendError(exchange, 400, "Folder name is required.");
            return;
        }
        if (name.length() > 100) {
            sendError(exchange, 400, "Folder name cannot exceed 100 characters.");
            return;
        }

        Folder created = folderDAO.createFolder(name);
        if (created != null) {
            sendJson(exchange, 201, created);
        } else {
            sendError(exchange, 409, "Folder with this name already exists or database error.");
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws IOException {
        Folder existing = folderDAO.getFolderById(id);
        if (existing == null) {
            sendError(exchange, 404, "Folder not found with ID: " + id);
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

        String name = json.has("name") ? json.get("name").getAsString().trim() : "";
        if (name.isEmpty()) {
            sendError(exchange, 400, "Folder name cannot be empty.");
            return;
        }

        boolean updated = folderDAO.updateFolder(id, name);
        if (updated) {
            sendJson(exchange, 200, folderDAO.getFolderById(id));
        } else {
            sendError(exchange, 409, "Folder name already taken or update failed.");
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        Folder existing = folderDAO.getFolderById(id);
        if (existing == null) {
            sendError(exchange, 404, "Folder not found with ID: " + id);
            return;
        }

        boolean deleted = folderDAO.deleteFolder(id);
        if (deleted) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Folder deleted. Notes have been moved to unfiled.");
            sendJson(exchange, 200, resp);
        } else {
            sendError(exchange, 500, "Failed to delete folder.");
        }
    }
}
