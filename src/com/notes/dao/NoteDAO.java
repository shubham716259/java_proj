package com.notes.dao;

import com.notes.helper.DBConnection;
import com.notes.model.Folder;
import com.notes.model.Note;
import com.notes.model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NoteDAO {

    private final TagDAO tagDAO = new TagDAO();

    private Note mapRow(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("id"));
        note.setTitle(rs.getString("title"));
        note.setContent(rs.getString("content"));
        note.setUserId(rs.getInt("user_id"));
        note.setPinned(rs.getBoolean("is_pinned"));
        note.setCreatedAt(rs.getTimestamp("created_at"));
        note.setUpdatedAt(rs.getTimestamp("updated_at"));

        int folderId = rs.getInt("folder_id");
        if (!rs.wasNull()) {
            note.setFolder(new Folder(folderId, rs.getString("folder_name"), note.getUserId()));
        } else {
            note.setFolder(null);
        }

        Set<Tag> tags = tagDAO.getTagsForNote(note.getId());
        note.setTags(tags);

        return note;
    }

    private static final String SELECT_BASE =
            "SELECT n.id, n.title, n.content, n.folder_id, n.user_id, n.is_pinned, n.created_at, n.updated_at, f.name AS folder_name " +
            "FROM notes n LEFT JOIN folders f ON n.folder_id = f.id ";

    public List<Note> getAllNotes(int userId) {
        List<Note> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE n.user_id = ? ORDER BY n.is_pinned DESC, n.updated_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("NoteDAO.getAllNotes: " + e.getMessage());
        }
        return list;
    }

    public Note getNoteById(int id, int userId) {
        String sql = SELECT_BASE + "WHERE n.id = ? AND n.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("NoteDAO.getNoteById: " + e.getMessage());
        }
        return null;
    }

    public List<Note> getPinnedNotes(int userId) {
        List<Note> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE n.is_pinned = TRUE AND n.user_id = ? ORDER BY n.updated_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("NoteDAO.getPinnedNotes: " + e.getMessage());
        }
        return list;
    }

    public List<Note> getRecentNotes(int userId, int limit) {
        List<Note> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE n.user_id = ? ORDER BY n.updated_at DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("NoteDAO.getRecentNotes: " + e.getMessage());
        }
        return list;
    }

    public List<Note> getNotesByFolder(int folderId, int userId) {
        List<Note> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE n.folder_id = ? AND n.user_id = ? ORDER BY n.is_pinned DESC, n.updated_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, folderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("NoteDAO.getNotesByFolder: " + e.getMessage());
        }
        return list;
    }

    public List<Note> getNotesByTag(int tagId, int userId) {
        List<Note> list = new ArrayList<>();
        String sql = SELECT_BASE + "JOIN note_tags nt ON n.id = nt.note_id " +
                     "WHERE nt.tag_id = ? AND n.user_id = ? ORDER BY n.is_pinned DESC, n.updated_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tagId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("NoteDAO.getNotesByTag: " + e.getMessage());
        }
        return list;
    }

    public Note createNote(Note note) {
        String sql = "INSERT INTO notes (title, content, folder_id, user_id, is_pinned) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, note.getTitle());
            ps.setString(2, note.getContent());
            if (note.getFolder() != null && note.getFolder().getId() > 0) {
                ps.setInt(3, note.getFolder().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, note.getUserId());
            ps.setBoolean(5, note.isPinned());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setId(rs.getInt(1));
                }
            }

            if (note.getTags() != null) {
                tagDAO.syncNoteTags(note.getId(), note.getTags());
            }

            return getNoteById(note.getId(), note.getUserId());
        } catch (SQLException e) {
            System.out.println("NoteDAO.createNote: " + e.getMessage());
            return null;
        }
    }

    public boolean updateNote(Note note) {
        String sql = "UPDATE notes SET title = ?, content = ?, folder_id = ?, is_pinned = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, note.getTitle());
            ps.setString(2, note.getContent());
            if (note.getFolder() != null && note.getFolder().getId() > 0) {
                ps.setInt(3, note.getFolder().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBoolean(4, note.isPinned());
            ps.setInt(5, note.getId());
            ps.setInt(6, note.getUserId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                tagDAO.syncNoteTags(note.getId(), note.getTags());
                return true;
            }
        } catch (SQLException e) {
            System.out.println("NoteDAO.updateNote: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteNote(int id, int userId) {
        String sql = "DELETE FROM notes WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("NoteDAO.deleteNote: " + e.getMessage());
            return false;
        }
    }

    public boolean togglePin(int id, int userId) {
        String sql = "UPDATE notes SET is_pinned = NOT is_pinned WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("NoteDAO.togglePin: " + e.getMessage());
            return false;
        }
    }

    public int countAll(int userId) {
        String sql = "SELECT COUNT(*) FROM notes WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }
}
