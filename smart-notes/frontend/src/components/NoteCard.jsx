import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Card component for displaying a note summary.
 * Escapes text natively via React JSX (never uses dangerouslySetInnerHTML).
 */
export default function NoteCard({ note, onTogglePin, onDeleteClick }) {
  const snippet = note.content
    ? note.content.length > 140
      ? note.content.substring(0, 140) + '...'
      : note.content
    : '';

  const dateStr = note.updatedAt ? note.updatedAt.substring(0, 10) : '';

  return (
    <article className={`note-card ${note.isPinned ? 'is-pinned' : ''}`}>
      <div className="note-card-top">
        <div className="note-card-title-wrap">
          {note.isPinned && (
            <span className="pin-icon" title="Pinned note">
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <line x1="12" y1="17" x2="12" y2="22" />
                <path d="M5 17h14v-1.76a2 2 0 0 0-1.11-1.79l-1.78-.9A2 2 0 0 1 15 10.76V6h1a1 1 0 0 0 0-2H8a1 1 0 0 0 0 2h1v4.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24Z" />
              </svg>
            </span>
          )}
          <Link to={`/notes/${note.id}`} className="note-card-title">
            {note.title}
          </Link>
        </div>

        {note.isPinned && (
          <span className="pin-badge">
            Pinned
          </span>
        )}
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

        <span style={{ fontSize: '0.8rem', color: 'var(--muted)' }}>{dateStr}</span>

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
