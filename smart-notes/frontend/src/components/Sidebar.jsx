import React from 'react';
import { NavLink, Link, useLocation } from 'react-router-dom';

/**
 * Sidebar navigation displaying brand, primary links, folders, and tags.
 */
export default function Sidebar({ folders = [], tags = [], totalNotes = 0 }) {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const activeFolderId = searchParams.get('folderId');
  const activeTagId = searchParams.get('tag');

  return (
    <aside className="sidebar">
      <Link to="/" className="brand">
        Smart Notes
        <span className="brand-sub">Personal Knowledge Base</span>
      </Link>

      <ul className="nav-list">
        <li>
          <NavLink
            to="/"
            end
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            Dashboard
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/notes"
            end={location.pathname === '/notes' && !location.search}
            className={({ isActive }) => `nav-item ${isActive && !activeFolderId && !activeTagId ? 'active' : ''}`}
          >
            <span>All Notes</span>
            <span className="count-badge">{totalNotes}</span>
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/notes/new"
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            New Note
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/folders"
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <span>Folders</span>
            <span className="count-badge">{folders.length}</span>
          </NavLink>
        </li>
      </ul>

      {/* Folders List */}
      <div className="sidebar-heading">Folders</div>
      <ul className="nav-list">
        {folders.length === 0 ? (
          <li style={{ padding: '4px 10px', fontSize: '12px', color: 'var(--text-dim)' }}>
            No folders yet
          </li>
        ) : (
          folders.map((folder) => {
            const isFolderActive = location.pathname === '/notes' && activeFolderId === String(folder.id);
            return (
              <li key={folder.id}>
                <Link
                  to={`/notes?folderId=${folder.id}`}
                  className={`nav-item ${isFolderActive ? 'active' : ''}`}
                >
                  <span>{folder.name}</span>
                  <span className="count-badge">{folder.noteCount || 0}</span>
                </Link>
              </li>
            );
          })
        )}
      </ul>

      {/* Tags List */}
      <div className="sidebar-heading">Tags</div>
      <div className="sidebar-tags">
        {tags.length === 0 ? (
          <span style={{ fontSize: '12px', color: 'var(--text-dim)' }}>No tags yet</span>
        ) : (
          tags.map((tag) => {
            const isTagActive = location.pathname === '/notes' && activeTagId === String(tag.id);
            return (
              <Link
                key={tag.id}
                to={`/notes?tag=${tag.id}`}
                className={`tag-chip ${isTagActive ? 'active' : ''}`}
              >
                #{tag.name}
              </Link>
            );
          })
        )}
      </div>

      <div className="sidebar-footer">
        Notes project, CSE
      </div>
    </aside>
  );
}
