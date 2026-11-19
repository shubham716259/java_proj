import React, { useState, useEffect, useCallback } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Sidebar from './components/Sidebar';
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
        />

        <div className="main-content">
          <header className="topbar">
            <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              Personal Knowledge Base &bull; CSE Project
            </div>
            <div className="topbar-right">
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
