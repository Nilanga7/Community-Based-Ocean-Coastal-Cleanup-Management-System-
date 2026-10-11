import React from 'react';
import { Icon } from '../core/Icon.jsx';

const TONES = {
  info: { color: 'var(--info)', background: 'var(--info-bg)', icon: 'info' },
  success: { color: 'var(--success)', background: 'var(--success-bg)', icon: 'circle-check-big' },
  warning: { color: 'var(--warning)', background: 'var(--warning-bg)', icon: 'triangle-alert' },
  danger: { color: 'var(--danger)', background: 'var(--danger-bg)', icon: 'triangle-alert' },
};

export function Alert({ tone = 'info', title, icon, onDismiss, children, style, ...rest }) {
  const t = TONES[tone];
  return (
    <div
      role="status"
      style={{
        display: 'flex',
        gap: 'var(--space-3)',
        padding: 'var(--space-4)',
        borderRadius: 'var(--radius-md)',
        background: t.background,
        border: '1px solid ' + t.color + '33',
        ...style,
      }}
      {...rest}
    >
      <Icon name={icon || t.icon} size="sm" color={t.color} style={{ marginTop: 2 }} />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 2 }}>
        {title ? <strong style={{ font: 'var(--text-label)', color: 'var(--text-strong)' }}>{title}</strong> : null}
        {children ? <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-body-color)' }}>{children}</span> : null}
      </div>
      {onDismiss ? (
        <button type="button" aria-label="Dismiss" onClick={onDismiss} style={{ background: 'transparent', cursor: 'pointer', display: 'flex', height: 20 }}>
          <Icon name="x" size="sm" color="var(--text-muted)" />
        </button>
      ) : null}
    </div>
  );
}
