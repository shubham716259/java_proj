package com.notes.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    // load db.properties once when the class is first referenced
    static {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password", "");
        } catch (IOException e) {
            System.out.println("Could not read db.properties, checking environment variables.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver jar is missing from the classpath.");
        }

        // Allow cloud environments (Render, Railway, Docker) to override via environment variables
        if (System.getenv("DB_URL") != null) {
            String envUrl = System.getenv("DB_URL").trim();
            if (envUrl.startsWith("db.url=")) {
                envUrl = envUrl.substring("db.url=".length()).trim();
            }
            url = envUrl;
        }
        if (System.getenv("DB_USER") != null) {
            user = System.getenv("DB_USER");
        }
        if (System.getenv("DB_PASSWORD") != null) {
            password = System.getenv("DB_PASSWORD");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
