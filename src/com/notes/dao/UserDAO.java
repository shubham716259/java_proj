package com.notes.dao;

import com.notes.helper.DBConnection;
import com.notes.model.User;

import java.sql.*;

public class UserDAO {

    public User createUser(String username, String passwordHash) {
        if (username == null || passwordHash == null) return null;
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username.trim());
            ps.setString(2, passwordHash);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new User(rs.getInt(1), username.trim(), passwordHash, new Timestamp(System.currentTimeMillis()));
                }
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.createUser: " + e.getMessage());
        }
        return null;
    }

    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findByUsername: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("UserDAO.findById: " + e.getMessage());
        }
        return null;
    }

    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }
}
