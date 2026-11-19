package com.notes.dao;

import com.notes.helper.DBConnection;
import com.notes.model.Tag;

import java.sql.*;
import java.util.*;

public class TagDAO {

    public List<Tag> getAllTags() {
        List<Tag> list = new ArrayList<>();
        String sql = "SELECT id, name FROM tags ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.out.println("TagDAO.getAllTags: " + e.getMessage());
        }
        return list;
    }

    // gets only the tags that appear on notes belonging to this user
    public List<Tag> getAllTagsForUser(int userId) {
        List<Tag> list = new ArrayList<>();
        String sql = "SELECT DISTINCT t.id, t.name FROM tags t " +
                     "JOIN note_tags nt ON t.id = nt.tag_id " +
                     "JOIN notes n ON nt.note_id = n.id " +
                     "WHERE n.user_id = ? ORDER BY t.name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Tag(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            System.out.println("TagDAO.getAllTagsForUser: " + e.getMessage());
        }
        return list;
    }

    public Tag getTagById(int id) {
        String sql = "SELECT id, name FROM tags WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tag(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("TagDAO.getTagById: " + e.getMessage());
        }
        return null;
    }

    // finds tag by name or inserts it if it is new
    public Tag findOrCreate(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) return null;
        String name = rawName.trim().toLowerCase();

        String findSql = "SELECT id, name FROM tags WHERE LOWER(name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tag(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("TagDAO.findOrCreate (find): " + e.getMessage());
        }

        String insertSql = "INSERT INTO tags (name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Tag(rs.getInt(1), name);
                }
            }
        } catch (SQLException e) {
            // might have been inserted in another thread/request, try finding one more time
            return findByName(name);
        }
        return null;
    }

    private Tag findByName(String name) {
        String sql = "SELECT id, name FROM tags WHERE LOWER(name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tag(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    public Set<Tag> getTagsForNote(int noteId) {
        Set<Tag> tags = new HashSet<>();
        String sql = "SELECT t.id, t.name FROM tags t " +
                     "JOIN note_tags nt ON t.id = nt.tag_id " +
                     "WHERE nt.note_id = ? ORDER BY t.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            System.out.println("TagDAO.getTagsForNote: " + e.getMessage());
        }
        return tags;
    }

    // rewrites the note_tags rows for this note
    public void syncNoteTags(int noteId, Set<Tag> tags) {
        String deleteSql = "DELETE FROM note_tags WHERE note_id = ?";
        String insertSql = "INSERT IGNORE INTO note_tags (note_id, tag_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                    del.setInt(1, noteId);
                    del.executeUpdate();
                }

                if (tags != null && !tags.isEmpty()) {
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        for (Tag t : tags) {
                            ins.setInt(1, noteId);
                            ins.setInt(2, t.getId());
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("TagDAO.syncNoteTags: " + e.getMessage());
        }
    }

    public Map<Integer, Integer> getTagCounts(int userId) {
        Map<Integer, Integer> counts = new HashMap<>();
        String sql = "SELECT nt.tag_id, COUNT(*) AS total FROM note_tags nt " +
                     "JOIN notes n ON nt.note_id = n.id " +
                     "WHERE n.user_id = ? GROUP BY nt.tag_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getInt("tag_id"), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            System.out.println("TagDAO.getTagCounts: " + e.getMessage());
        }
        return counts;
    }
}
