-- Personal Knowledge Base and Smart Notes Database Schema
CREATE DATABASE IF NOT EXISTS notes_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE notes_db;

DROP TABLE IF EXISTS note_tags;
DROP TABLE IF EXISTS notes;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS tags;

CREATE TABLE folders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    folder_id INT,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_folder FOREIGN KEY (folder_id)
        REFERENCES folders(id) ON DELETE SET NULL
);

CREATE TABLE tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE note_tags (
    note_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (note_id, tag_id),
    CONSTRAINT fk_nt_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_nt_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
