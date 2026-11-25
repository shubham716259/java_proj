import React from 'react';
import { Link } from 'react-router-dom';
import NoteCard from './NoteCard';

/**
 * Renders a list of NoteCards with loading and empty state messages.
 */
export default function NoteList({
  notes,
  loading,
  emptyMessage = "No notes found.",
  showCreateAction = false,
  onTogglePin,
  onDeleteClick
}) {
  if (loading) {
    return (
      <div className="loading-state">
        <svg
          className="loading-spinner-icon"
          width="28"
          height="28"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M21 12a9 9 0 1 1-6.219-8.56" />
        </svg>
        <span>Loading notes...</span>
      </div>
    );
  }

  if (!notes || notes.length === 0) {
    return (
      <div className="empty-state">
        <svg
          className="empty-state-icon"
          width="48"
          height="48"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <line x1="10" y1="9" x2="8" y2="9" />
        </svg>
        <p className="empty-state-text">{emptyMessage}</p>
        {showCreateAction && (
          <Link to="/notes/new" className="btn btn-primary btn-sm empty-state-btn">
            Create your first note
          </Link>
        )}
      </div>
    );
  }

  return (
    <div className="note-list">
      {notes.map((note) => (
        <NoteCard
          key={note.id}
          note={note}
          onTogglePin={onTogglePin}
          onDeleteClick={onDeleteClick}
        />
      ))}
    </div>
  );
}
