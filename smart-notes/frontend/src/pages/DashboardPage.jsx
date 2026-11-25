import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getNotes, getRecentNotes, togglePin, deleteNote } from '../api';
import SearchBar from '../components/SearchBar';
import NoteList from '../components/NoteList';
import ConfirmDialog from '../components/ConfirmDialog';
import Toast from '../components/Toast';

export default function DashboardPage({ onDataChange }) {
  const navigate = useNavigate();
  const [pinnedNotes, setPinnedNotes] = useState([]);
  const [recentNotes, setRecentNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toastMessage, setToastMessage] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      // Pinned notes
      const allRes = await getNotes();
      const all = allRes.notes || [];
      setPinnedNotes(all.filter((n) => n.isPinned));

      // Recent notes
      const recent = await getRecentNotes();
      setRecentNotes(recent || []);
    } catch (err) {
      setToastMessage('Failed to load dashboard: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const handleSearch = (term) => {
    if (term.trim()) {
      navigate(`/notes?q=${encodeURIComponent(term.trim())}`);
    }
  };

  const handleTogglePin = async (id) => {
    try {
      await togglePin(id);
      loadDashboardData();
      if (onDataChange) onDataChange();
    } catch (err) {
      setToastMessage('Error toggling pin: ' + err.message);
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteNote(deleteTarget.id);
      setDeleteTarget(null);
      setToastMessage('Note deleted successfully.');
      loadDashboardData();
      if (onDataChange) onDataChange();
    } catch (err) {
      setToastMessage('Failed to delete note: ' + err.message);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 className="page-title">Dashboard</h1>
          <div className="page-desc">Pinned items and your 10 most recently updated notes</div>
        </div>
        <Link to="/notes/new" className="btn btn-primary">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span>New note</span>
        </Link>
      </div>

      <div style={{ marginBottom: '28px' }}>
        <SearchBar onChange={handleSearch} placeholder="Search across all notes..." />
      </div>

      <Toast message={toastMessage} onClose={() => setToastMessage(null)} />

      {/* Pinned Notes Section */}
      <div style={{ marginBottom: '36px' }}>
        <div className="suggestions-heading" style={{ color: 'var(--accent)', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span>Pinned Notes</span>
          {pinnedNotes.length > 0 && <span className="count-badge">{pinnedNotes.length}</span>}
        </div>

        <NoteList
          notes={pinnedNotes}
          loading={loading}
          emptyMessage="No pinned notes yet. Pin important notes to keep them here."
          onTogglePin={handleTogglePin}
          onDeleteClick={(note) => setDeleteTarget(note)}
        />
      </div>

      {/* Recently Updated Notes Section */}
      <div>
        <div className="suggestions-heading" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span>Recently Updated</span>
          {recentNotes.length > 0 && <span className="count-badge">{recentNotes.length}</span>}
        </div>

        <NoteList
          notes={recentNotes}
          loading={loading}
          emptyMessage="Nothing here yet. Click 'New note' to start taking notes."
          showCreateAction={true}
          onTogglePin={handleTogglePin}
          onDeleteClick={(note) => setDeleteTarget(note)}
        />
      </div>

      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        title="Delete Note"
        message={`Are you sure you want to delete "${deleteTarget?.title}"? This cannot be undone.`}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
