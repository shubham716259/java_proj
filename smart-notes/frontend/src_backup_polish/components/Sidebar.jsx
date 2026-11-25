import React from 'react';
import { NavLink, Link, useLocation } from 'react-router-dom';
import Logo from './Logo';

/**
 * Sidebar navigation displaying brand, primary links, folders, and tags.
 */
export default function Sidebar({ folders = [], tags = [], totalNotes = 0, isOpen = false, onClose }) {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const activeFolderId = searchParams.get('folderId');
  const activeTagId = searchParams.get('tag');

  const handleNavClick = () => {
    if (onClose) onClose();
  };

  return (
    <aside className={`sidebar ${isOpen ? 'mobile-open' : ''}`}>
      <Link to="/" className="brand" onClick={handleNavClick}>
        <div className="logo-container">
          <Logo size={32} showText={false} />
          <div className="brand-text-wrap">
            <span className="logo-text">Smart Notes</span>
            <span className="brand-sub">Personal Knowledge Base</span>
          </div>
        </div>
      </Link>

      <ul className="nav-list">
        <li>
          <NavLink
            to="/"
            end
            onClick={handleNavClick}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            Dashboard
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/notes"
            end={location.pathname === '/notes' && !location.search}
            onClick={handleNavClick}
            className={({ isActive }) => `nav-item ${isActive && !activeFolderId && !activeTagId ? 'active' : ''}`}
          >
            <span>All Notes</span>
            <span className="count-badge">{totalNotes}</span>
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/notes/new"
            onClick={handleNavClick}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            New Note
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/folders"
            onClick={handleNavClick}
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
          <li style={{ padding: '4px 10px', fontSize: '0.8rem', color: 'var(--muted)' }}>
            No folders yet
          </li>
        ) : (
          folders.map((folder) => {
            const isFolderActive = location.pathname === '/notes' && activeFolderId === String(folder.id);
            return (
              <li key={folder.id}>
                <Link
                  to={`/notes?folderId=${folder.id}`}
                  onClick={handleNavClick}
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
          <span style={{ fontSize: '0.8rem', color: 'var(--muted)', padding: '0 4px' }}>No tags yet</span>
        ) : (
          tags.map((tag) => {
            const isTagActive = location.pathname === '/notes' && activeTagId === String(tag.id);
            return (
              <Link
                key={tag.id}
                to={`/notes?tag=${tag.id}`}
                onClick={handleNavClick}
                className={`tag-chip ${isTagActive ? 'active' : ''}`}
              >
                #{tag.name}
              </Link>
            );
          })
        )}
      </div>

      <div className="sidebar-footer">
        Notes project &bull; CSE
      </div>
    </aside>
  );
}
