import React from 'react';
import { Icon } from '../core/Icon.jsx';

export function Checkbox({ label, hint, checked = false, indeterminate = false, disabled, onChange, style, ...rest }) {
  const on = checked || indeterminate;
  return (
    <label style={{ display: 'flex', gap: 10, alignItems: 'flex-start', cursor: disabled ? 'not-allowed' : 'pointer', opacity: disabled ? 0.5 : 1, ...style }}>
      <input type="checkbox" checked={checked} disabled={disabled} onChange={onChange} style={{ position: 'absolute', opacity: 0, width: 0, height: 0 }} {...rest} />
      <span
        style={{
          width: 20,
          height: 20,
          flex: '0 0 auto',
          marginTop: 1,
          borderRadius: 'var(--radius-xs)',
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: on ? 'var(--accent)' : 'var(--surface-card)',
          border: '1px solid ' + (on ? 'var(--accent)' : 'var(--border-default)'),
          transition: 'var(--transition-control)',
        }}
      >
        {indeterminate ? (
          <span style={{ width: 10, height: 2, background: 'var(--white)', borderRadius: 1 }} />
        ) : checked ? (
          <Icon name="check" size={14} color="var(--white)" />
        ) : null}
      </span>
      {label || hint ? (
        <span style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-heading)' }}>{label}</span>
          {hint ? <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{hint}</span> : null}
        </span>
      ) : null}
    </label>
  );
}
