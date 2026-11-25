import React from 'react';

/**
 * Toast / Error message notification banner using CSS variables.
 */
export default function Toast({ message, type = 'error', onClose }) {
  if (!message) return null;

  return (
    <div className={`alert ${type === 'error' ? 'alert-error' : 'alert-success'}`}>
      <span>{message}</span>
      {onClose && (
        <button
          type="button"
          onClick={onClose}
          className="btn-text"
          style={{ fontSize: '1.1rem', padding: '0 4px', lineHeight: 1 }}
          aria-label="Dismiss alert"
        >
          &times;
        </button>
      )}
    </div>
  );
}
