import React from 'react';
import { Icon } from '../design-system';

export default function PhotoPlaceholder({ ratio = '16/9', count, style }) {
  return (
    <div
      style={{
        position: 'relative',
        width: '100%',
        aspectRatio: ratio,
        background: 'linear-gradient(135deg, var(--sea-100), var(--sea-200))',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
        ...style,
      }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6 }}>
        <Icon name="image" size="lg" color="var(--sea-500)" />
        <span style={{ font: 'var(--text-caption)', color: 'var(--sea-700)' }}>photo placeholder</span>
      </div>
      {count ? (
        <span
          style={{
            position: 'absolute',
            right: 10,
            bottom: 10,
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
            padding: '4px 8px',
            borderRadius: 'var(--radius-sm)',
            background: 'var(--surface-glass)',
            backdropFilter: 'blur(4px)',
            font: 'var(--text-micro)',
            color: 'var(--text-strong)',
          }}
        >
          <Icon name="camera" size="xs" />
          {count}
        </span>
      ) : null}
    </div>
  );
}
