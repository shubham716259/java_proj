import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { getNotes, togglePin, deleteNote, getFolders, getTags } from '../api';
import SearchBar from '../components/SearchBar';
import NoteList from '../components/NoteList';
import ConfirmDialog from '../components/ConfirmDialog';
import Toast from '../components/Toast';

export default function NotesPage({ onDataChange }) {
  const [searchParams, setSearchParams] = useSearchParams();

  const q = searchParams.get('q') || '';
  const folderId = searchParams.get('folderId') || '';
  const tag = searchParams.get('tag') || '';
  const sort = searchParams.get('sort') || 'date';
  const order = searchParams.get('order') || 'desc';

  const [notes, setNotes] = useState([]);
  const [total, setTotal] = useState(0);
  const [relatedSearches, setRelatedSearches] = useState([]);
  const [folders, setFolders] = useState([]);
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toastMessage, setToastMessage] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  // Load filter options
  useEffect(() => {
    getFolders().then(setFolders).catch(console.error);
    getTags().then(setTags).catch(console.error);
  }, []);

  // Fetch notes when query params change
  useEffect(() => {
    let isCurrent = true;
    setLoading(true);

    getNotes({ q, folderId, tag, sort, order })
      .then((res) => {
        if (!isCurrent) return;
        setNotes(res.notes || []);
        setTotal(res.total || 0);
        setRelatedSearches(res.relatedSearches || []);
      })
      .catch((err) => {
        if (!isCurrent) return;
        setToastMessage('Error loading notes: ' + err.message);
      })
      .finally(() => {
        if (isCurrent) setLoading(false);
      });

    return () => {
      isCurrent = false;
    };
  }, [q, folderId, tag, sort, order]);

  const updateParam = (key, value) => {
    const next = new URLSearchParams(searchParams);
    if (value) {
      next.set(key, value);
    } else {
      next.delete(key);
    }
    setSearchParams(next);
  };

  const handleSearchChange = (term) => {
    updateParam('q', term.trim());
  };

  const handleClearFilters = () => {
    setSearchParams(new URLSearchParams());
  };

  const handleTogglePin = async (id) => {
    try {
      await togglePin(id);
      // Refresh list
      const res = await getNotes({ q, folderId, tag, sort, order });
      setNotes(res.notes || []);
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
      const res = await getNotes({ q, folderId, tag, sort, order });
      setNotes(res.notes || []);
      setTotal(res.total || 0);
      if (onDataChange) onDataChange();
    } catch (err) {
      setToastMessage('Failed to delete note: ' + err.message);
    }
  };

  const hasActiveFilters = Boolean(q || folderId || tag || sort !== 'date' || order !== 'desc');

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '20px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 className="page-title">All Notes</h1>
          <div className="page-desc">{total} note(s) found</div>
        </div>
        <Link to="/notes/new" className="btn btn-primary">
          + New note
        </Link>
      </div>

      <Toast message={toastMessage} onClose={() => setToastMessage(null)} />

      {/* Filter & Sort Bar */}
      <div className="filter-bar">
        <SearchBar value={q} onChange={handleSearchChange} placeholder="Filter search..." />

        {/* Folder filter */}
        <select
          value={folderId}
          onChange={(e) => updateParam('folderId', e.target.value)}
          aria-label="Filter by folder"
        >
          <option value="">All Folders</option>
          {folders.map((f) => (
            <option key={f.id} value={f.id}>
              {f.name}
            </option>
          ))}
        </select>

        {/* Tag filter */}
        <select
          value={tag}
          onChange={(e) => updateParam('tag', e.target.value)}
          aria-label="Filter by tag"
        >
          <option value="">All Tags</option>
          {tags.map((t) => (
            <option key={t.id} value={t.id}>
              #{t.name}
            </option>
          ))}
        </select>

        {/* Sort & Order */}
        <select
          value={`${sort}_${order}`}
          onChange={(e) => {
            const [s, o] = e.target.value.split('_');
            const next = new URLSearchParams(searchParams);
            next.set('sort', s);
            next.set('order', o);
            setSearchParams(next);
          }}
          aria-label="Sort notes"
        >
          <option value="date_desc">Newest first</option>
          <option value="date_asc">Oldest first</option>
          <option value="title_asc">Title (A-Z)</option>
          <option value="title_desc">Title (Z-A)</option>
        </select>

        {hasActiveFilters && (
          <button
            type="button"
            className="btn btn-sm btn-text"
            onClick={handleClearFilters}
          >
            Clear filters
          </button>
        )}
      </div>

      {/* Search keyword banner & related searches */}
      {q && (
        <div style={{ marginBottom: '18px' }}>
          <div style={{ fontSize: '13.5px', color: 'var(--text-muted)', marginBottom: '8px' }}>
            Showing keyword matches for &ldquo;<strong>{q}</strong>&rdquo;
          </div>

          {relatedSearches && relatedSearches.length > 0 && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Related searches:
              </span>
              {relatedSearches.map((term) => (
                <button
                  key={term}
                  type="button"
                  className="tag-chip"
                  style={{ background: 'var(--bg-hover)', padding: '2px 8px', borderRadius: '3px' }}
                  onClick={() => updateParam('q', term)}
                >
                  {term}
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Note List */}
      <NoteList
        notes={notes}
        loading={loading}
        emptyMessage={hasActiveFilters ? "Nothing matches your filter or search." : "No notes found. Create your first note!"}
        onTogglePin={handleTogglePin}
        onDeleteClick={(note) => setDeleteTarget(note)}
      />

      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        title="Delete Note"
        message={`Are you sure you want to delete "${deleteTarget?.title}"?`}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
