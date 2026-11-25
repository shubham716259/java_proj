import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getFolders, createFolder, updateFolder, deleteFolder } from '../api';
import ConfirmDialog from '../components/ConfirmDialog';
import Toast from '../components/Toast';

export default function FoldersPage({ onDataChange }) {
  const [folders, setFolders] = useState([]);
  const [newFolderName, setNewFolderName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editingName, setEditingName] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [toastMessage, setToastMessage] = useState(null);
  const [toastType, setToastType] = useState('error');
  const [loading, setLoading] = useState(true);

  const loadFolders = async () => {
    setLoading(true);
    try {
      const data = await getFolders();
      setFolders(data || []);
    } catch (err) {
      showToast('Failed to load folders: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFolders();
  }, []);

  const showToast = (msg, type = 'error') => {
    setToastMessage(msg);
    setToastType(type);
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    const name = newFolderName.trim();
    if (!name) {
      showToast('Folder name cannot be empty.');
      return;
    }

    try {
      await createFolder(name);
      setNewFolderName('');
      showToast('Folder created successfully.', 'success');
      loadFolders();
      if (onDataChange) onDataChange();
    } catch (err) {
      showToast(err.message);
    }
  };

  const startEditing = (folder) => {
    setEditingId(folder.id);
    setEditingName(folder.name);
  };

  const cancelEditing = () => {
    setEditingId(null);
    setEditingName('');
  };

  const handleRename = async (id) => {
    const name = editingName.trim();
    if (!name) {
      showToast('Folder name cannot be empty.');
      return;
    }

    try {
      await updateFolder(id, name);
      setEditingId(null);
      showToast('Folder renamed successfully.', 'success');
      loadFolders();
      if (onDataChange) onDataChange();
    } catch (err) {
      showToast(err.message);
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteFolder(deleteTarget.id);
      setDeleteTarget(null);
      showToast('Folder deleted. Its notes have been moved to unfiled.', 'success');
      loadFolders();
      if (onDataChange) onDataChange();
    } catch (err) {
      showToast('Failed to delete folder: ' + err.message);
    }
  };

  return (
    <div>
      <div className="page-header" style={{ marginBottom: '24px' }}>
        <h1 className="page-title">Folders</h1>
        <div className="page-desc">
          Organize your notes into folders. Deleting a folder unlinks its notes without removing them.
        </div>
      </div>

      <Toast
        message={toastMessage}
        type={toastType}
        onClose={() => setToastMessage(null)}
      />

      {/* Create Folder Form */}
      <div className="form-card" style={{ marginBottom: '28px', maxWidth: '600px' }}>
        <form onSubmit={handleCreate} style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <input
            type="text"
            className="form-input"
            placeholder="New folder name..."
            maxLength={100}
            value={newFolderName}
            onChange={(e) => setNewFolderName(e.target.value)}
          />
          <button type="submit" className="btn btn-primary" style={{ whiteSpace: 'nowrap' }}>
            Create folder
          </button>
        </form>
      </div>

      {/* Folders List Table */}
      {loading ? (
        <div className="loading-state">Loading folders...</div>
      ) : folders.length === 0 ? (
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
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
          </svg>
          <p className="empty-state-text">No folders created yet.</p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="simple-table">
            <thead>
              <tr>
                <th>Folder Name</th>
                <th style={{ width: '120px' }}>Notes</th>
                <th style={{ width: '280px' }}>Rename</th>
                <th style={{ width: '90px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {folders.map((folder) => {
                const isEditing = editingId === folder.id;
                return (
                  <tr key={folder.id}>
                    <td>
                      <Link
                        to={`/notes?folderId=${folder.id}`}
                        style={{ color: 'var(--text)', fontWeight: 600, textDecoration: 'none' }}
                      >
                        {folder.name}
                      </Link>
                    </td>
                    <td>
                      <span className="count-badge">{folder.noteCount || 0} note(s)</span>
                    </td>
                    <td>
                      {isEditing ? (
                        <div style={{ display: 'flex', gap: '6px' }}>
                          <input
                            type="text"
                            className="form-input"
                            style={{ padding: '4px 8px', fontSize: '13px' }}
                            value={editingName}
                            onChange={(e) => setEditingName(e.target.value)}
                            autoFocus
                          />
                          <button
                            type="button"
                            className="btn btn-sm btn-primary"
                            onClick={() => handleRename(folder.id)}
                          >
                            Save
                          </button>
                          <button
                            type="button"
                            className="btn btn-sm"
                            onClick={cancelEditing}
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          className="btn btn-sm"
                          onClick={() => startEditing(folder)}
                        >
                          Rename
                        </button>
                      )}
                    </td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-sm btn-danger"
                        onClick={() => setDeleteTarget(folder)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        title="Delete Folder"
        message={`Are you sure you want to delete folder "${deleteTarget?.name}"? Notes inside this folder will remain safe and be moved to unfiled.`}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
