import React, { useState, useEffect } from 'react';

/**
 * SearchBar with 300ms debounce.
 */
export default function SearchBar({ value, onChange, placeholder = "Search notes by title or content..." }) {
  const [searchTerm, setSearchTerm] = useState(value || '');

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

  const handleClear = () => {
    setSearchTerm('');
    onChange('');
  };

  return (
    <div className="search-bar">
      <input
        type="text"
        className="search-input"
        placeholder={placeholder}
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
      />
      {searchTerm && (
        <button
          type="button"
          className="search-clear-btn"
          onClick={handleClear}
          title="Clear search"
        >
          &times;
        </button>
      )}
    </div>
  );
}
