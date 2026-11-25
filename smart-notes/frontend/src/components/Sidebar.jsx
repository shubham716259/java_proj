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
          <Logo size={40} showText={false} />
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
            <div className="nav-item-content">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
                className="nav-item-icon"
              >
                <rect x="3" y="3" width="7" height="9" rx="1" />
                <rect x="14" y="3" width="7" height="5" rx="1" />
                <rect x="14" y="12" width="7" height="9" rx="1" />
                <rect x="3" y="16" width="7" height="5" rx="1" />
              </svg>
              <span>Dashboard</span>
            </div>
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/notes"
            end={location.pathname === '/notes' && !location.search}
            onClick={handleNavClick}
            className={({ isActive }) => `nav-item ${isActive && !activeFolderId && !activeTagId ? 'active' : ''}`}
          >
            <div className="nav-item-content">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
                className="nav-item-icon"
              >
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <line x1="10" y1="9" x2="8" y2="9" />
              </svg>
              <span>All notes</span>
            </div>
            {totalNotes > 0 && <span className="count-badge">{totalNotes}</span>}
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/notes/new"
            onClick={handleNavClick}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <div className="nav-item-content">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
                className="nav-item-icon"
              >
                <path d="M12 20h9" />
                <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
              </svg>
              <span>New note</span>
            </div>
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/folders"
            onClick={handleNavClick}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <div className="nav-item-content">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
                className="nav-item-icon"
              >
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
              </svg>
              <span>Folders</span>
            </div>
            {folders.length > 0 && <span className="count-badge">{folders.length}</span>}
          </NavLink>
        </li>
      </ul>

      {/* Folders List */}
      <div className="sidebar-heading">Folders</div>
      <ul className="nav-list">
        {folders.length === 0 ? (
          <li style={{ padding: '6px 12px', fontSize: '0.8rem', color: 'var(--muted)' }}>
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
                  <div className="nav-item-content">
                    <svg
                      width="15"
                      height="15"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      aria-hidden="true"
                      className="nav-item-icon"
                      style={{ opacity: 0.7 }}
                    >
                      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
                    </svg>
                    <span>{folder.name}</span>
                  </div>
                  {folder.noteCount > 0 && <span className="count-badge">{folder.noteCount}</span>}
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
