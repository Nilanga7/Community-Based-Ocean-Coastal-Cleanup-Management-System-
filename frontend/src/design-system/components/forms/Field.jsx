import React from 'react';

export function Field({ label, hint, error, required, htmlFor, children, style }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6, ...style }}>
      {label ? (
        <label htmlFor={htmlFor} style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>
          {label}
          {required ? <span style={{ color: 'var(--danger)' }}> *</span> : null}
        </label>
      ) : null}
      {children}
      {error ? (
        <span style={{ font: 'var(--text-caption)', color: 'var(--danger)' }}>{error}</span>
      ) : hint ? (
        <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{hint}</span>
      ) : null}
    </div>
  );
}
