package dao;

import model.Tag;
import util.DBConnection;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for Tag entity and the note_tags junction table (many-to-many).
 */
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
            System.err.println("TagDAO.getAllTags: " + e.getMessage());
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
            System.err.println("TagDAO.getTagById: " + e.getMessage());
        }
        return null;
    }

    public Tag findOrCreateTag(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) return null;
        String name = rawName.trim().toLowerCase();

        // 1. Try finding existing
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
            System.err.println("TagDAO.findOrCreateTag (find): " + e.getMessage());
        }

        // 2. Insert if new
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
            // In case of race condition or duplicate key, query one more time
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
            System.err.println("TagDAO.getTagsForNote: " + e.getMessage());
        }
        return tags;
    }

    /**
     * Synchronizes tags for a note inside a database transaction.
     */
    public void syncNoteTags(int noteId, Set<Tag> tags) {
        String deleteSql = "DELETE FROM note_tags WHERE note_id = ?";
        String insertSql = "INSERT IGNORE INTO note_tags (note_id, tag_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete previous links
                try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                    del.setInt(1, noteId);
                    del.executeUpdate();
                }

                // Insert new links
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
            System.err.println("TagDAO.syncNoteTags: " + e.getMessage());
        }
    }

    public Map<Integer, Integer> getTagCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        String sql = "SELECT tag_id, COUNT(*) AS count FROM note_tags GROUP BY tag_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getInt("tag_id"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("TagDAO.getTagCounts: " + e.getMessage());
        }
        return counts;
    }
}
