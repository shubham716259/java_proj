import React from 'react';

/**
 * Toast / Error message notification banner.
 */
export default function Toast({ message, type = 'error', onClose }) {
  if (!message) return null;

  return (
    <div className={`alert ${type === 'error' ? 'alert-error' : 'alert-success'}`} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <span>{message}</span>
      {onClose && (
        <button
          type="button"
          onClick={onClose}
          style={{ background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px', marginLeft: '12px' }}
        >
          &times;
        </button>
      )}
    </div>
  );
}
