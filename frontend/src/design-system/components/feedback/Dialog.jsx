import React from 'react';
import { Icon } from '../core/Icon.jsx';

export function Dialog({ open = true, title, description, width = 480, onClose, footer, children, style, ...rest }) {
  if (!open) return null;
  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 'var(--space-6)',
        background: 'var(--surface-overlay)',
        backdropFilter: 'blur(2px)',
        zIndex: 50,
      }}
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(e) => e.stopPropagation()}
        style={{
          width: '100%',
          maxWidth: width,
          background: 'var(--surface-card)',
          borderRadius: 'var(--radius-xl)',
          boxShadow: 'var(--shadow-xl)',
          overflow: 'hidden',
          ...style,
        }}
        {...rest}
      >
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 'var(--space-4)', padding: 'var(--space-6) var(--space-6) 0' }}>
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6 }}>
            <h2 style={{ font: 'var(--text-h3)', letterSpacing: 'var(--tracking-heading)', color: 'var(--text-strong)', margin: 0 }}>{title}</h2>
            {description ? <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)', margin: 0 }}>{description}</p> : null}
          </div>
          {onClose ? (
            <button type="button" aria-label="Close" onClick={onClose} style={{ background: 'transparent', cursor: 'pointer', display: 'flex', padding: 4 }}>
              <Icon name="x" size="sm" color="var(--text-muted)" />
            </button>
          ) : null}
        </div>
        {children ? <div style={{ padding: 'var(--space-5) var(--space-6)' }}>{children}</div> : <div style={{ height: 'var(--space-5)' }} />}
        {footer ? (
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', padding: 'var(--space-4) var(--space-6)', background: 'var(--surface-page)', borderTop: '1px solid var(--border-subtle)' }}>
            {footer}
          </div>
        ) : null}
      </div>
    </div>
  );
}
