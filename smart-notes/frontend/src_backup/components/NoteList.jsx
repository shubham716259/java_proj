import React from 'react';
import NoteCard from './NoteCard';

/**
 * Renders a list of NoteCards with loading and empty state messages.
 */
export default function NoteList({ notes, loading, emptyMessage = "No notes found.", onTogglePin, onDeleteClick }) {
  if (loading) {
    return <div className="loading-state">Loading notes...</div>;
  }

  if (!notes || notes.length === 0) {
    return <div className="empty-state">{emptyMessage}</div>;
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
