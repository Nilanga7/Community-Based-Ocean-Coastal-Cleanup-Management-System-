import React from 'react';
import { Icon } from '../core/Icon.jsx';

const ICONS = { info: 'info', success: 'circle-check-big', warning: 'triangle-alert', danger: 'triangle-alert' };
const ACCENTS = { info: 'var(--sea-400)', success: 'var(--kelp-500)', warning: 'var(--buoy-500)', danger: 'var(--coral-400)' };

export function Toast({ tone = 'info', title, action, onDismiss, children, style, ...rest }) {
  return (
    <div
      role="alert"
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        gap: 'var(--space-3)',
        minWidth: 300,
        maxWidth: 420,
        padding: 'var(--space-4)',
        borderRadius: 'var(--radius-md)',
        background: 'var(--surface-inverse)',
        color: 'var(--text-inverse)',
        boxShadow: 'var(--shadow-lg)',
        borderLeft: '3px solid ' + ACCENTS[tone],
        ...style,
      }}
      {...rest}
    >
      <Icon name={ICONS[tone]} size="sm" color={ACCENTS[tone]} style={{ marginTop: 2 }} />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 2 }}>
        <strong style={{ font: 'var(--text-label)' }}>{title}</strong>
        {children ? <span style={{ font: 'var(--text-caption)', color: 'var(--text-inverse-muted)' }}>{children}</span> : null}
      </div>
      {action ? (
        <button type="button" onClick={action.onClick} style={{ background: 'transparent', color: ACCENTS[tone], font: 'var(--text-label)', cursor: 'pointer' }}>
          {action.label}
        </button>
      ) : null}
      {onDismiss ? (
        <button type="button" aria-label="Dismiss" onClick={onDismiss} style={{ background: 'transparent', cursor: 'pointer', display: 'flex', height: 20 }}>
          <Icon name="x" size="sm" color="var(--text-inverse-muted)" />
        </button>
      ) : null}
    </div>
  );
}
