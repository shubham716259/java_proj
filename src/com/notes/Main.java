
package com.notes;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.dao.UserDAO;
import com.notes.helper.DBConnection;
import com.notes.web.*;
import com.sun.net.httpserver.HttpServer;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Smart Notes application...");

        // quick check to see if database is reachable
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to MySQL database successfully.");

            // Verify and auto-heal schema compatibility if user_id columns are missing
            try (java.sql.Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("ALTER TABLE notes ADD COLUMN user_id INT NOT NULL DEFAULT 1");
                } catch (SQLException ignored) {}
                try {
                    stmt.executeUpdate("ALTER TABLE folders ADD COLUMN user_id INT NOT NULL DEFAULT 1");
                } catch (SQLException ignored) {}
            } catch (SQLException ignored) {}
        } catch (Exception e) {
            System.out.println("Warning: Could not connect to MySQL: " + e.getMessage());
            System.out.println("Check db.properties to make sure credentials and port are correct.");
        }

        // read port from PORT environment variable (cloud hosting) or db.properties
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        } else {
            try (FileInputStream in = new FileInputStream("db.properties")) {
                Properties p = new Properties();
                p.load(in);
                String portStr = p.getProperty("server.port");
                if (portStr != null) {
                    port = Integer.parseInt(portStr.trim());
                }
            } catch (Exception ignored) {
            }
        }

        try {
            UserDAO userDAO = new UserDAO();
            NoteDAO noteDAO = new NoteDAO();
            FolderDAO folderDAO = new FolderDAO();
            TagDAO tagDAO = new TagDAO();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // register authentication routes
            AuthHandler authHandler = new AuthHandler(userDAO);
            server.createContext("/login", authHandler);
            server.createContext("/signup", authHandler);
            server.createContext("/logout", authHandler);

            // register application routes
            server.createContext("/", new DashboardHandler(noteDAO, folderDAO, tagDAO, userDAO));
            server.createContext("/notes", new NoteListHandler(noteDAO, folderDAO, tagDAO, userDAO));
            server.createContext("/notes/view", new NoteViewHandler(noteDAO, folderDAO, tagDAO, userDAO));
            server.createContext("/notes/new", new NoteFormHandler(noteDAO, folderDAO, tagDAO, userDAO));
            server.createContext("/notes/edit", new NoteFormHandler(noteDAO, folderDAO, tagDAO, userDAO));
            server.createContext("/notes/delete", new NoteDeleteHandler(noteDAO, folderDAO, tagDAO, userDAO));
            server.createContext("/notes/pin", new NotePinHandler(noteDAO, userDAO));
            server.createContext("/folders", new FolderHandler(folderDAO, tagDAO, noteDAO, userDAO));
            server.createContext("/static/", new StaticHandler());

            // use a small thread pool to handle multiple requests
            server.setExecutor(Executors.newFixedThreadPool(8));
            server.start();

            System.out.println("Server running at http://localhost:" + port + "/");
        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
