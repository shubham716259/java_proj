import React, { useState, useEffect, useCallback } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
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

export default function App() {
  const [folders, setFolders] = useState([]);
  const [tags, setTags] = useState([]);
  const [totalNotes, setTotalNotes] = useState(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

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

  return (
    <Router>
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

            <div className="topbar-right">
              <ThemeToggle />
              <Link to="/notes/new" className="btn btn-primary btn-sm">
                + New note
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
    </Router>
  );
}
