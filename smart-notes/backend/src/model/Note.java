package model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Main entity representing a note.
 * Demonstrates OOP composition:
 * - Note HAS-A Folder (references one Folder object)
 * - Note HAS-MANY Tags (references a Set of Tag objects)
 */
public class Note {
    private int id;
    private String title;
    private String content;
    private Folder folder;           // HAS-A Folder (composition)
    private Set<Tag> tags = new HashSet<>(); // HAS-MANY Tags (composition)
    private boolean isPinned;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Note() {
    }

    public Note(int id, String title, String content, Folder folder, boolean isPinned,
                Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.folder = folder;
        this.isPinned = isPinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = (tags != null) ? tags : new HashSet<>();
    }

    public void addTag(Tag tag) {
        if (tag != null) {
            this.tags.add(tag);
        }
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
