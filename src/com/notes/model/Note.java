package com.notes.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class Note {
    private int id;
    private String title;
    private String content;
    private Folder folder;
    private int userId;
    private Set<Tag> tags = new HashSet<>();
    private boolean pinned;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Note() {
    }

    public Note(int id, String title, String content, Folder folder, int userId, boolean pinned,
                Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.folder = folder;
        this.userId = userId;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Note(int id, String title, String content, Folder folder, boolean pinned,
                Timestamp createdAt, Timestamp updatedAt) {
        this(id, title, content, folder, 0, pinned, createdAt, updatedAt);
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
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

    // returns tags as "java, oop" for prefilling the edit input box
    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Tag t : tags) {
            if (count > 0) sb.append(", ");
            sb.append(t.getName());
            count++;
        }
        return sb.toString();
    }

    // 2-line snippet for the note list view
    public String getSnippet(int maxChars) {
        if (content == null || content.isEmpty()) return "";
        if (content.length() <= maxChars) return content;
        return content.substring(0, maxChars) + "...";
    }
}
