import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getNoteById, createNote, updateNote, getFolders } from '../api';
import Toast from '../components/Toast';

export default function NoteFormPage({ onDataChange }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [folderId, setFolderId] = useState('');
  const [tagsInput, setTagsInput] = useState('');
  const [isPinned, setIsPinned] = useState(false);

  const [folders, setFolders] = useState([]);
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [toastMessage, setToastMessage] = useState(null);

  // Load available folders
  useEffect(() => {
    getFolders()
      .then(setFolders)
      .catch((err) => console.error('Error fetching folders:', err));
  }, []);

  // If in edit mode, load note details
  useEffect(() => {
    if (isEdit) {
      setLoading(true);
      getNoteById(id)
        .then((data) => {
          const note = data.note;
          setTitle(note.title || '');
          setContent(note.content || '');
          setFolderId(note.folder ? String(note.folder.id) : '');
          setIsPinned(Boolean(note.isPinned));
          if (note.tags && note.tags.length > 0) {
            setTagsInput(note.tags.map((t) => t.name).join(', '));
          }
        })
        .catch((err) => {
          setToastMessage('Failed to load note for editing: ' + err.message);
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [id, isEdit]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const cleanTitle = title.trim();

    if (!cleanTitle) {
      setToastMessage('Note title is required.');
      return;
    }

    if (cleanTitle.length > 255) {
      setToastMessage('Note title cannot exceed 255 characters.');
      return;
    }

    // Split comma-separated tags
    const tagsArray = tagsInput
      .split(/[,;]+/)
      .map((t) => t.trim())
      .filter((t) => t.length > 0);

    const payload = {
      title: cleanTitle,
      content: content.trim(),
      folderId: folderId ? parseInt(folderId, 10) : null,
      tags: tagsArray,
      isPinned,
    };

    setSaving(true);
    setToastMessage(null);

    try {
      if (isEdit) {
        await updateNote(id, payload);
        if (onDataChange) onDataChange();
        navigate(`/notes/${id}`);
      } else {
        const created = await createNote(payload);
        if (onDataChange) onDataChange();
        navigate(`/notes/${created.id}`);
      }
    } catch (err) {
      setToastMessage(err.message || 'Error saving note.');
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="loading-state">Loading note form...</div>;
  }

  return (
    <div>
      <div className="form-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '20px' }}>
          <h1 className="page-title" style={{ fontSize: '22px' }}>
            {isEdit ? 'Edit Note' : 'New Note'}
          </h1>
          <Link to={isEdit ? `/notes/${id}` : '/notes'} className="btn btn-sm">
            Cancel
          </Link>
        </div>

        <Toast message={toastMessage} onClose={() => setToastMessage(null)} />

        <form onSubmit={handleSubmit}>
          {/* Title */}
          <div className="form-group">
            <label className="form-label" htmlFor="title">
              Title <span style={{ color: 'var(--danger)' }}>*</span>
            </label>
            <input
              type="text"
              id="title"
              className="form-input"
              required
              maxLength={255}
              placeholder="e.g. Core Java Multithreading Concepts"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              autoFocus
            />
          </div>

          {/* Folder */}
          <div className="form-group">
            <label className="form-label" htmlFor="folderId">
              Folder
            </label>
            <select
              id="folderId"
              className="form-input"
              value={folderId}
              onChange={(e) => setFolderId(e.target.value)}
            >
              <option value="">None (Unfiled)</option>
              {folders.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </div>

          {/* Tags */}
          <div className="form-group">
            <label className="form-label" htmlFor="tags">
              Tags
            </label>
            <input
              type="text"
              id="tags"
              className="form-input"
              placeholder="e.g. java, concurrency, threads"
              value={tagsInput}
              onChange={(e) => setTagsInput(e.target.value)}
            />
            <div className="form-help">Separate tags with commas. New tags will be indexed automatically.</div>
          </div>

          {/* Content */}
          <div className="form-group">
            <label className="form-label" htmlFor="content">
              Content
            </label>
            <textarea
              id="content"
              className="form-input"
              placeholder="Write your study notes and explanations here..."
              value={content}
              onChange={(e) => setContent(e.target.value)}
            />
          </div>

          {/* Pinned Checkbox */}
          <div className="form-group">
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '13.5px' }}>
              <input
                type="checkbox"
                checked={isPinned}
                onChange={(e) => setIsPinned(e.target.checked)}
              />
              <span>Pin this note to the top of lists and dashboard</span>
            </label>
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '24px' }}>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : isEdit ? 'Update note' : 'Save note'}
            </button>
            <Link to={isEdit ? `/notes/${id}` : '/notes'} className="btn">
              Cancel
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}
