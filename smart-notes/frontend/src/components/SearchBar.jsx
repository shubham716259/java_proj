import React, { useState, useEffect, useRef } from 'react';

/**
 * Completely updated, modern SearchBar with:
 * - Debounced search input (300ms)
 * - Enter key for instant search
 * - Escape key to clear & blur
 * - Left search SVG icon with focus highlight
 * - OS-aware keyboard shortcut hint (Ctrl+K or ⌘K)
 * - Accessible SVG clear button
 */
export default function SearchBar({
  value,
  onChange,
  placeholder = "Search notes by title or content...",
  autoFocus = false,
}) {
  const [searchTerm, setSearchTerm] = useState(value || '');
  const inputRef = useRef(null);
  const isMac = typeof navigator !== 'undefined' && /Mac|iPod|iPhone|iPad/.test(navigator.platform);

  // Keep local state in sync when parent resets value
  useEffect(() => {
    setSearchTerm(value || '');
  }, [value]);

  // Debounce 300 ms
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm !== (value || '')) {
        onChange(searchTerm);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchTerm, value, onChange]);

  // Global shortcut: Ctrl+K or ⌘K or / focuses search input
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (
        (e.key === '/' || ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k')) &&
        document.activeElement?.tagName !== 'INPUT' &&
        document.activeElement?.tagName !== 'TEXTAREA'
      ) {
        e.preventDefault();
        inputRef.current?.focus();
        inputRef.current?.select();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleClear = () => {
    setSearchTerm('');
    onChange('');
    inputRef.current?.focus();
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Escape') {
      if (searchTerm) {
        handleClear();
      } else {
        inputRef.current?.blur();
      }
    } else if (e.key === 'Enter') {
      e.preventDefault();
      onChange(searchTerm);
    }
  };

  return (
    <div className="search-bar">
      <svg
        className="search-icon"
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
        ref={inputRef}
        type="text"
        className="search-input"
        placeholder={placeholder}
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        onKeyDown={handleKeyDown}
        autoFocus={autoFocus}
        aria-label="Search notes"
      />
      {searchTerm ? (
        <button
          type="button"
          className="search-clear-btn"
          onClick={handleClear}
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
        <kbd className="search-kbd-hint" title={`Press ${isMac ? '⌘K' : 'Ctrl+K'} or / to search`}>
          <span className="search-kbd-key">{isMac ? '⌘' : 'Ctrl'}</span>
          <span className="search-kbd-key">K</span>
        </kbd>
      )}
    </div>
  );
}
