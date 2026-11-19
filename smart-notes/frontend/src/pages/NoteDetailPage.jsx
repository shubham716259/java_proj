import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getNoteById, togglePin, deleteNote } from '../api';
import NoteCard from '../components/NoteCard';
import ConfirmDialog from '../components/ConfirmDialog';
import Toast from '../components/Toast';

export default function NoteDetailPage({ onDataChange }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const [note, setNote] = useState(null);
  const [keywords, setKeywords] = useState([]);
  const [relatedNotes, setRelatedNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toastMessage, setToastMessage] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const loadNoteDetail = async () => {
    setLoading(true);
    try {
      const data = await getNoteById(id);
      setNote(data.note);
      setKeywords(data.keywords || []);
      setRelatedNotes(data.relatedNotes || []);
    } catch (err) {
      setToastMessage('Error loading note: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNoteDetail();
  }, [id]);

  const handleTogglePin = async () => {
    if (!note) return;
    try {
      const updated = await togglePin(note.id);
      setNote(updated);
      if (onDataChange) onDataChange();
    } catch (err) {
      setToastMessage('Failed to toggle pin: ' + err.message);
    }
  };

  const handleDelete = async () => {
    if (!note) return;
    try {
      await deleteNote(note.id);
      if (onDataChange) onDataChange();
      navigate('/notes');
    } catch (err) {
      setToastMessage('Failed to delete note: ' + err.message);
    }
  };

  if (loading) {
    return <div className="loading-state">Loading note details...</div>;
  }

  if (!note) {
    return (
      <div>
        <Toast message={toastMessage || 'Note not found.'} />
        <Link to="/notes" className="btn btn-sm">
          &larr; Back to notes
        </Link>
      </div>
    );
  }

  const updatedDate = note.updatedAt ? note.updatedAt.substring(0, 16) : '';

  return (
    <div>
      {/* Top action bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <Link to="/notes" className="btn btn-sm">
          &larr; Back to notes
        </Link>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button type="button" className="btn btn-sm" onClick={handleTogglePin}>
            {note.isPinned ? "Unpin note" : "Pin note"}
          </button>
          <Link to={`/notes/${note.id}/edit`} className="btn btn-sm">
            Edit
          </Link>
          <button
            type="button"
            className="btn btn-sm btn-danger"
            onClick={() => setShowDeleteConfirm(true)}
          >
            Delete
          </button>
        </div>
      </div>

      <Toast message={toastMessage} onClose={() => setToastMessage(null)} />

      {/* Main Note View Panel */}
      <article className="note-view-panel">
        <h1 className="note-view-title">{note.title}</h1>

        <div className="note-view-meta">
          {note.folder && (
            <span className="folder-badge">{note.folder.name}</span>
          )}

          {note.tags && note.tags.length > 0 && (
            note.tags.map((t) => (
              <Link key={t.id} to={`/notes?tag=${t.id}`} className="tag-chip">
                #{t.name}
              </Link>
            ))
          )}

          <span style={{ marginLeft: 'auto' }}>Updated: {updatedDate}</span>
        </div>

        {/* Note Content (plain string rendering preserves formatting safely) */}
        <div className="note-view-body">
          {note.content}
        </div>

        {/* Suggestions & Keywords Section */}
        <div className="suggestions-section">
          <div className="suggestions-heading">Extracted Keywords (HashMap frequency)</div>
          {keywords.length === 0 ? (
            <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              No distinctive keywords extracted.
            </div>
          ) : (
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '24px' }}>
              {keywords.map((kw) => (
                <Link
                  key={kw}
                  to={`/notes?q=${encodeURIComponent(kw)}`}
                  className="tag-chip"
                  style={{ background: 'var(--bg-hover)', padding: '2px 8px', borderRadius: '3px' }}
                >
                  {kw}
                </Link>
              ))}
            </div>
          )}

          {/* Related Notes */}
          {relatedNotes.length > 0 && (
            <div>
              <div className="suggestions-heading" style={{ marginTop: '20px' }}>
                Related Notes (sharing keywords)
              </div>
              <div className="note-list">
                {relatedNotes.map((rel) => (
                  <NoteCard key={rel.id} note={rel} />
                ))}
              </div>
            </div>
          )}
        </div>
      </article>

      <ConfirmDialog
        isOpen={showDeleteConfirm}
        title="Delete Note"
        message={`Are you sure you want to delete "${note.title}"? This cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    </div>
  );
}
