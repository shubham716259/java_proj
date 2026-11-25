import React, { useState, useEffect, useCallback, useRef } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Logo from './components/Logo';
import ThemeToggle from './components/ThemeToggle';
import DashboardPage from './pages/DashboardPage';
import NotesPage from './pages/NotesPage';
import NoteDetailPage from './pages/NoteDetailPage';
import NoteFormPage from './pages/NoteFormPage';
import FoldersPage from './pages/FoldersPage';
import { getFolders, getTags, getNotes } from './api';
import './App.css';

function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [folders, setFolders] = useState([]);
  const [tags, setTags] = useState([]);
  const [totalNotes, setTotalNotes] = useState(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [topSearch, setTopSearch] = useState('');
  const topSearchRef = useRef(null);
  const isMac = typeof navigator !== 'undefined' && /Mac|iPod|iPhone|iPad/.test(navigator.platform);

  // Sync topSearch with ?q= in URL
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const q = params.get('q') || '';
    setTopSearch(q);
  }, [location.search, location.pathname]);

  const refreshSidebar = useCallback(async () => {
    try {
      const [foldersData, tagsData, notesData] = await Promise.all([
        getFolders(),
        getTags(),
        getNotes(),
      ]);
      setFolders(foldersData || []);
      setTags(tagsData || []);
      setTotalNotes(notesData?.total || 0);
    } catch (err) {
      console.error('Error refreshing sidebar:', err);
    }
  }, []);

  useEffect(() => {
    refreshSidebar();
  }, [refreshSidebar]);

  // Global Ctrl+K / ⌘K / / shortcut to focus topbar search
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (
        (e.key === '/' || ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k')) &&
        document.activeElement?.tagName !== 'INPUT' &&
        document.activeElement?.tagName !== 'TEXTAREA'
      ) {
        e.preventDefault();
        topSearchRef.current?.focus();
        topSearchRef.current?.select();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleTopSearchSubmit = (e) => {
    e.preventDefault();
    if (topSearch.trim()) {
      navigate(`/notes?q=${encodeURIComponent(topSearch.trim())}`);
    } else {
      navigate('/notes');
    }
  };

  const handleClearTopSearch = () => {
    setTopSearch('');
    topSearchRef.current?.focus();
    if (location.pathname === '/notes') {
      navigate('/notes');
    }
  };

  return (
    <div className="app-layout">
      <Sidebar
        folders={folders}
        tags={tags}
        totalNotes={totalNotes}
        isOpen={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
      />

      <div className="main-content">
        <header className="topbar">
          <div className="topbar-left">
            <button
              type="button"
              className="mobile-menu-btn"
              onClick={() => setMobileMenuOpen((prev) => !prev)}
              aria-label="Toggle navigation menu"
            >
              {mobileMenuOpen ? '✕' : '☰'}
            </button>
            <Link to="/" className="topbar-brand-link" aria-label="Smart Notes Home">
              <Logo size={26} showText={false} />
            </Link>
            <div className="topbar-title">
              Personal Knowledge Base &bull; CSE Project
            </div>
          </div>

          <form className="topbar-search-form" onSubmit={handleTopSearchSubmit}>
            <svg
              className="topbar-search-icon"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              ref={topSearchRef}
              type="text"
              className="topbar-search-input"
              placeholder={`Search notes... (${isMac ? '⌘K' : 'Ctrl+K'})`}
              value={topSearch}
              onChange={(e) => setTopSearch(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Escape') {
                  handleClearTopSearch();
                }
              }}
              aria-label="Search notes"
            />
            {topSearch ? (
              <button
                type="button"
                className="topbar-search-clear-btn"
                onClick={handleClearTopSearch}
                title="Clear search (Esc)"
                aria-label="Clear search"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            ) : (
              <kbd className="topbar-search-kbd" title={`Press ${isMac ? '⌘K' : 'Ctrl+K'} or / to search`}>
                <span>{isMac ? '⌘' : 'Ctrl'}</span>
                <span>K</span>
              </kbd>
            )}
          </form>

          <div className="topbar-right">
            <ThemeToggle />
            <Link to="/notes/new" className="btn btn-primary topbar-new-btn">
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              <span>New note</span>
            </Link>
          </div>
        </header>

        <main className="content-area">
          <Routes>
            <Route path="/" element={<DashboardPage onDataChange={refreshSidebar} />} />
            <Route path="/notes" element={<NotesPage onDataChange={refreshSidebar} />} />
            <Route path="/notes/new" element={<NoteFormPage onDataChange={refreshSidebar} />} />
            <Route path="/notes/:id/edit" element={<NoteFormPage onDataChange={refreshSidebar} />} />
            <Route path="/notes/:id" element={<NoteDetailPage onDataChange={refreshSidebar} />} />
            <Route path="/folders" element={<FoldersPage onDataChange={refreshSidebar} />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <AppLayout />
    </Router>
  );
}
