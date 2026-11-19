import com.sun.net.httpserver.HttpServer;
import dao.FolderDAO;
import dao.NoteDAO;
import dao.TagDAO;
import handler.FolderHandler;
import handler.NoteHandler;
import handler.TagHandler;
import service.SearchService;
import service.SuggestionService;
import util.DBConnection;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Main application entrypoint for Personal Knowledge Base and Smart Notes backend.
 * Uses core Java com.sun.net.httpserver.HttpServer to expose REST API.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Smart Notes REST API backend...");

        // Verify MySQL connectivity
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Database connected successfully to MySQL.");
        } catch (Exception e) {
            System.err.println("Warning: Could not connect to MySQL: " + e.getMessage());
            System.err.println("Please check config.properties to verify database URL, port, and credentials.");
        }

        // Determine listening port
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        } else {
            try (FileInputStream in = new FileInputStream("config.properties")) {
                Properties props = new Properties();
                props.load(in);
                String p = props.getProperty("server.port");
                if (p != null) port = Integer.parseInt(p.trim());
            } catch (Exception ignored) {}
        }

        try {
            // Instantiate DAOs and Services
            FolderDAO folderDAO = new FolderDAO();
            TagDAO tagDAO = new TagDAO();
            NoteDAO noteDAO = new NoteDAO();
            SearchService searchService = new SearchService();
            SuggestionService suggestionService = new SuggestionService();

            // Instantiate Handlers
            NoteHandler noteHandler = new NoteHandler(noteDAO, folderDAO, tagDAO, searchService, suggestionService);
            FolderHandler folderHandler = new FolderHandler(folderDAO);
            TagHandler tagHandler = new TagHandler(tagDAO);

            // Create HttpServer
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Register contexts
            server.createContext("/api/notes", noteHandler);
            server.createContext("/api/folders", folderHandler);
            server.createContext("/api/tags", tagHandler);

            // Thread pool executor for handling concurrent requests
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            System.out.println("REST API server is running at http://localhost:" + port + "/api/");
            System.out.println("Endpoints available:");
            System.out.println("  - GET    /api/notes");
            System.out.println("  - GET    /api/notes/recent");
            System.out.println("  - GET    /api/notes/{id}");
            System.out.println("  - POST   /api/notes");
            System.out.println("  - PUT    /api/notes/{id}");
            System.out.println("  - DELETE /api/notes/{id}");
            System.out.println("  - PATCH  /api/notes/{id}/pin");
            System.out.println("  - GET    /api/folders");
            System.out.println("  - POST   /api/folders");
            System.out.println("  - PUT    /api/folders/{id}");
            System.out.println("  - DELETE /api/folders/{id}");
            System.out.println("  - GET    /api/tags");
        } catch (Exception e) {
            System.err.println("Fatal: Server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
