import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Card component for displaying a note summary.
 * Escapes text natively via React JSX (never uses dangerouslySetInnerHTML).
 */
export default function NoteCard({ note, onTogglePin, onDeleteClick }) {
  const snippet = note.content
    ? note.content.length > 130
      ? note.content.substring(0, 130) + '...'
      : note.content
    : '';

  const dateStr = note.updatedAt ? note.updatedAt.substring(0, 10) : '';

  return (
    <article className={`note-card ${note.isPinned ? 'is-pinned' : ''}`}>
      <div className="note-card-top">
        <Link to={`/notes/${note.id}`} className="note-card-title">
          {note.title}
        </Link>
        {note.isPinned && <span className="pin-badge">Pinned</span>}
      </div>

      <p className="note-card-snippet">{snippet}</p>

      <div className="note-card-meta">
        {note.folder && (
          <span className="folder-badge">{note.folder.name}</span>
        )}

        {note.tags && note.tags.length > 0 && (
          note.tags.map((tag) => (
            <Link key={tag.id} to={`/notes?tag=${tag.id}`} className="tag-chip">
              #{tag.name}
            </Link>
          ))
        )}

        <span>{dateStr}</span>

        <div className="note-card-actions">
          {onTogglePin && (
            <button
              type="button"
              className="btn btn-sm"
              onClick={(e) => {
                e.stopPropagation();
                onTogglePin(note.id);
              }}
              title={note.isPinned ? "Unpin note" : "Pin note"}
            >
              {note.isPinned ? "Unpin" : "Pin"}
            </button>
          )}

          <Link
            to={`/notes/${note.id}/edit`}
            className="btn btn-sm"
            onClick={(e) => e.stopPropagation()}
          >
            Edit
          </Link>

          {onDeleteClick && (
            <button
              type="button"
              className="btn btn-sm btn-danger"
              onClick={(e) => {
                e.stopPropagation();
                onDeleteClick(note);
              }}
            >
              Delete
            </button>
          )}
        </div>
      </div>
    </article>
  );
}
