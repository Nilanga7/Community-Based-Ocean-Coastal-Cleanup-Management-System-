import React from 'react';
import { Icon } from './Icon.jsx';

const TONES = {
  neutral: { color: 'var(--gray-700)', background: 'var(--gray-100)' },
  info: { color: 'var(--info)', background: 'var(--info-bg)' },
  success: { color: 'var(--success)', background: 'var(--success-bg)' },
  warning: { color: 'var(--warning)', background: 'var(--warning-bg)' },
  danger: { color: 'var(--danger)', background: 'var(--danger-bg)' },
  accent: { color: 'var(--accent-on-soft)', background: 'var(--accent-soft)' },
  inverse: { color: 'var(--white)', background: 'rgba(255,255,255,0.16)' },
};

export function Badge({ tone = 'neutral', icon, dot = false, size = 'md', children, style, ...rest }) {
  const s = size === 'sm';
  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: s ? 4 : 6,
        height: s ? 20 : 24,
        padding: s ? '0 7px' : '0 9px',
        borderRadius: 'var(--radius-pill)',
        font: s ? '600 11px/1 var(--font-body)' : '600 12px/1 var(--font-body)',
        letterSpacing: '0.02em',
        whiteSpace: 'nowrap',
        ...TONES[tone],
        ...style,
      }}
      {...rest}
    >
      {dot ? <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'currentColor' }} /> : null}
      {icon ? <Icon name={icon} size="xs" /> : null}
      {children}
    </span>
  );
}
