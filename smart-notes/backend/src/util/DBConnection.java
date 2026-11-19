package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection helper using pure JDBC.
 * Reads configuration from config.properties with fallback to environment variables.
 */
public class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password", "");
        } catch (IOException e) {
            System.out.println("Notice: config.properties not found, falling back to environment or defaults.");
        }

        // Allow environment variables to override for cloud or container setups
        if (System.getenv("DB_URL") != null) {
            url = System.getenv("DB_URL");
        }
        if (System.getenv("DB_USER") != null) {
            user = System.getenv("DB_USER");
        }
        if (System.getenv("DB_PASSWORD") != null) {
            password = System.getenv("DB_PASSWORD");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found on classpath!");
        }
    }

    /**
     * Establishes a new database connection.
     * Callers must wrap this in a try-with-resources block.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
