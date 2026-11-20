package com.notes.dao;

import com.notes.helper.DBConnection;
import com.notes.model.Folder;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderDAO {

    public List<Folder> getAllFolders(int userId) {
        List<Folder> list = new ArrayList<>();
        String sql = "SELECT id, name, user_id FROM folders WHERE user_id = ? ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Folder(rs.getInt("id"), rs.getString("name"), rs.getInt("user_id")));
                }
            }
        } catch (SQLException e) {
            System.out.println("FolderDAO.getAllFolders: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("user_id")) {
                try (Connection conn = DBConnection.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE folders ADD COLUMN user_id INT NOT NULL DEFAULT 1");
                    return getAllFolders(userId);
                } catch (SQLException ignored) {}
            }
        }
        return list;
    }

    public Folder getFolderById(int id, int userId) {
        String sql = "SELECT id, name, user_id FROM folders WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Folder(rs.getInt("id"), rs.getString("name"), rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.out.println("FolderDAO.getFolderById: " + e.getMessage());
        }
        return null;
    }

    public Folder createFolder(String name, int userId) {
        if (name == null || name.trim().isEmpty()) return null;
        String sql = "INSERT INTO folders (name, user_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setInt(2, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Folder(rs.getInt(1), name.trim(), userId);
                }
            }
        } catch (SQLException e) {
            System.out.println("FolderDAO.createFolder: " + e.getMessage());
        }
        return null;
    }

    public boolean renameFolder(int id, String newName, int userId) {
        if (newName == null || newName.trim().isEmpty()) return false;
        String sql = "UPDATE folders SET name = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName.trim());
            ps.setInt(2, id);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("FolderDAO.renameFolder: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFolder(int id, int userId) {
        String sql = "DELETE FROM folders WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("FolderDAO.deleteFolder: " + e.getMessage());
            return false;
        }
    }

    // gets the number of notes in each folder for the sidebar counts
    public Map<Integer, Integer> getFolderCounts(int userId) {
        Map<Integer, Integer> counts = new HashMap<>();
        String sql = "SELECT folder_id, COUNT(*) AS total FROM notes WHERE folder_id IS NOT NULL AND user_id = ? GROUP BY folder_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getInt("folder_id"), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            System.out.println("FolderDAO.getFolderCounts: " + e.getMessage());
        }
        return counts;
    }
}
