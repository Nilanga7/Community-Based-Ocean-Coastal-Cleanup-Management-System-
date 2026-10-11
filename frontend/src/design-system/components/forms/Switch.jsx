import React from 'react';

export function Switch({ label, hint, checked = false, disabled, onChange, style, ...rest }) {
  return (
    <label style={{ display: 'flex', gap: 12, alignItems: 'center', justifyContent: 'space-between', cursor: disabled ? 'not-allowed' : 'pointer', opacity: disabled ? 0.5 : 1, ...style }}>
      {label || hint ? (
        <span style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-heading)' }}>{label}</span>
          {hint ? <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{hint}</span> : null}
        </span>
      ) : null}
      <input type="checkbox" role="switch" checked={checked} disabled={disabled} onChange={onChange} style={{ position: 'absolute', opacity: 0, width: 0, height: 0 }} {...rest} />
      <span
        style={{
          width: 44,
          height: 26,
          flex: '0 0 auto',
          borderRadius: 'var(--radius-pill)',
          background: checked ? 'var(--accent)' : 'var(--gray-300)',
          position: 'relative',
          transition: 'background-color var(--duration-base) var(--ease-standard)',
        }}
      >
        <span
          style={{
            position: 'absolute',
            top: 3,
            left: checked ? 21 : 3,
            width: 20,
            height: 20,
            borderRadius: '50%',
            background: 'var(--white)',
            boxShadow: 'var(--shadow-sm)',
            transition: 'left var(--duration-base) var(--ease-standard)',
          }}
        />
      </span>
    </label>
  );
}
