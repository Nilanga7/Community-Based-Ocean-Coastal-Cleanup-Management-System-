import React from 'react';
import { Icon } from './Icon.jsx';

const BOX = { xs: 24, sm: 32, md: 40, lg: 56 };

export function Avatar({ name = '', src, size = 'md', role, style, ...rest }) {
  const px = BOX[size] || size;
  const initials = name.split(' ').filter(Boolean).slice(0, 2).map((w) => w[0]).join('').toUpperCase();
  return (
    <span style={{ position: 'relative', display: 'inline-flex', flex: '0 0 auto', ...style }} {...rest}>
      <span
        style={{
          width: px,
          height: px,
          borderRadius: '50%',
          overflow: 'hidden',
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: src ? 'var(--gray-200)' : 'var(--sea-100)',
          color: 'var(--sea-800)',
          font: '600 ' + Math.round(px * 0.36) + 'px/1 var(--font-body)',
          border: '1px solid var(--border-subtle)',
        }}
      >
        {src ? <img src={src} alt={name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : initials || <Icon name="user" size="sm" />}
      </span>
      {role ? (
        <span
          title={role}
          style={{
            position: 'absolute',
            right: -2,
            bottom: -2,
            width: Math.max(12, px * 0.38),
            height: Math.max(12, px * 0.38),
            borderRadius: '50%',
            background: role === 'authority' ? 'var(--sea-700)' : role === 'organization' ? 'var(--buoy-600)' : 'var(--tide-600)',
            border: '2px solid var(--surface-card)',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Icon name={role === 'authority' ? 'shield-check' : role === 'organization' ? 'building-2' : 'anchor'} size={10} color="var(--white)" />
        </span>
      ) : null}
    </span>
  );
}
