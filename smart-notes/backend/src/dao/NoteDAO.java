package dao;

import model.Folder;
import model.Note;
import model.Tag;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for Note entity.
 * Handles composition with Folder and Tag.
 */
public class NoteDAO {

    private final TagDAO tagDAO = new TagDAO();

    private static final String SELECT_BASE =
            "SELECT n.id, n.title, n.content, n.folder_id, n.is_pinned, n.created_at, n.updated_at, " +
            "f.name AS folder_name " +
            "FROM notes n " +
            "LEFT JOIN folders f ON n.folder_id = f.id ";

    private Note mapRow(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("id"));
        note.setTitle(rs.getString("title"));
        note.setContent(rs.getString("content"));
        note.setPinned(rs.getBoolean("is_pinned"));
        note.setCreatedAt(rs.getTimestamp("created_at"));
        note.setUpdatedAt(rs.getTimestamp("updated_at"));

        int folderId = rs.getInt("folder_id");
        if (!rs.wasNull()) {
            note.setFolder(new Folder(folderId, rs.getString("folder_name")));
        } else {
            note.setFolder(null);
        }

        // Fetch associated tags (many-to-many composition)
        Set<Tag> tags = tagDAO.getTagsForNote(note.getId());
        note.setTags(tags);

        return note;
    }

    public List<Note> getAllNotes() {
        List<Note> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY n.is_pinned DESC, n.updated_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("NoteDAO.getAllNotes: " + e.getMessage());
        }
        return list;
    }

    public Note getNoteById(int id) {
        String sql = SELECT_BASE + "WHERE n.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("NoteDAO.getNoteById: " + e.getMessage());
        }
        return null;
    }

    public List<Note> getRecentNotes(int limit) {
        List<Note> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY n.updated_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("NoteDAO.getRecentNotes: " + e.getMessage());
        }
        return list;
    }

    public Note createNote(Note note) {
        String sql = "INSERT INTO notes (title, content, folder_id, is_pinned) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, note.getTitle());
            ps.setString(2, note.getContent());
            if (note.getFolder() != null && note.getFolder().getId() > 0) {
                ps.setInt(3, note.getFolder().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBoolean(4, note.isPinned());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setId(rs.getInt(1));
                }
            }

            // Sync tags to note_tags table
            if (note.getTags() != null) {
                tagDAO.syncNoteTags(note.getId(), note.getTags());
            }

            return getNoteById(note.getId());
        } catch (SQLException e) {
            System.err.println("NoteDAO.createNote: " + e.getMessage());
            return null;
        }
    }

    public boolean updateNote(Note note) {
        String sql = "UPDATE notes SET title = ?, content = ?, folder_id = ?, is_pinned = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
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

            int affected = ps.executeUpdate();
            if (affected > 0) {
                tagDAO.syncNoteTags(note.getId(), note.getTags());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("NoteDAO.updateNote: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteNote(int id) {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NoteDAO.deleteNote: " + e.getMessage());
            return false;
        }
    }

    public boolean togglePin(int id) {
        String sql = "UPDATE notes SET is_pinned = NOT is_pinned WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NoteDAO.togglePin: " + e.getMessage());
            return false;
        }
    }
}
