package com.notes;

import com.notes.dao.FolderDAO;
import com.notes.dao.NoteDAO;
import com.notes.dao.TagDAO;
import com.notes.helper.DBConnection;
import com.notes.web.*;
import com.sun.net.httpserver.HttpServer;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Smart Notes application...");

        // quick check to see if database is reachable
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to MySQL database successfully.");
        } catch (Exception e) {
            System.out.println("Warning: Could not connect to MySQL: " + e.getMessage());
            System.out.println("Check db.properties to make sure credentials and port are correct.");
        }

        // read port from db.properties if specified
        int port = 8080;
        try (FileInputStream in = new FileInputStream("db.properties")) {
            Properties p = new Properties();
            p.load(in);
            String portStr = p.getProperty("server.port");
            if (portStr != null) {
                port = Integer.parseInt(portStr.trim());
            }
        } catch (Exception ignored) {
        }

        try {
            NoteDAO noteDAO = new NoteDAO();
            FolderDAO folderDAO = new FolderDAO();
            TagDAO tagDAO = new TagDAO();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // register application routes
            server.createContext("/", new DashboardHandler(noteDAO, folderDAO, tagDAO));
            server.createContext("/notes", new NoteListHandler(noteDAO, folderDAO, tagDAO));
            server.createContext("/notes/view", new NoteViewHandler(noteDAO, folderDAO, tagDAO));
            server.createContext("/notes/new", new NoteFormHandler(noteDAO, folderDAO, tagDAO));
            server.createContext("/notes/edit", new NoteFormHandler(noteDAO, folderDAO, tagDAO));
            server.createContext("/notes/delete", new NoteDeleteHandler(noteDAO, folderDAO, tagDAO));
            server.createContext("/notes/pin", new NotePinHandler(noteDAO));
            server.createContext("/folders", new FolderHandler(folderDAO, tagDAO, noteDAO));
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
