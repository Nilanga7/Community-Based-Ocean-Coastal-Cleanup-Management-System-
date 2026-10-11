import React from 'react';

export function Radio({ label, hint, checked = false, disabled, name, value, onChange, style, ...rest }) {
  return (
    <label style={{ display: 'flex', gap: 10, alignItems: 'flex-start', cursor: disabled ? 'not-allowed' : 'pointer', opacity: disabled ? 0.5 : 1, ...style }}>
      <input type="radio" name={name} value={value} checked={checked} disabled={disabled} onChange={onChange} style={{ position: 'absolute', opacity: 0, width: 0, height: 0 }} {...rest} />
      <span
        style={{
          width: 20,
          height: 20,
          flex: '0 0 auto',
          marginTop: 1,
          borderRadius: '50%',
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'var(--surface-card)',
          border: '1px solid ' + (checked ? 'var(--accent)' : 'var(--border-default)'),
          boxShadow: checked ? 'inset 0 0 0 5px var(--accent)' : 'none',
          transition: 'var(--transition-control)',
        }}
      />
      {label || hint ? (
        <span style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-heading)' }}>{label}</span>
          {hint ? <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{hint}</span> : null}
        </span>
      ) : null}
    </label>
  );
}
