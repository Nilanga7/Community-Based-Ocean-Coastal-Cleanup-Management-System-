import React from 'react';

export function Card({ tone = 'default', interactive = false, padding = 'md', children, style, ...rest }) {
  const [hover, setHover] = React.useState(false);
  const pads = { none: 0, sm: 'var(--space-4)', md: 'var(--space-5)', lg: 'var(--space-6)' };
  const tones = {
    default: { background: 'var(--surface-card)', border: '1px solid var(--border-subtle)' },
    sunken: { background: 'var(--surface-sunken)', border: '1px solid transparent' },
    warm: { background: 'var(--surface-page-warm)', border: '1px solid var(--sand-200)' },
    inverse: { background: 'var(--surface-brand)', border: '1px solid var(--border-inverse)', color: 'var(--text-inverse)' },
    accent: { background: 'var(--accent-soft)', border: '1px solid var(--tide-200)' },
  };
  return (
    <div
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      style={{
        borderRadius: 'var(--radius-lg)',
        padding: pads[padding],
        boxShadow: interactive && hover ? 'var(--shadow-md)' : 'var(--shadow-sm)',
        transform: interactive && hover ? 'translateY(-1px)' : 'none',
        transition: 'box-shadow var(--duration-base) var(--ease-standard), transform var(--duration-base) var(--ease-standard)',
        cursor: interactive ? 'pointer' : undefined,
        ...tones[tone],
        ...style,
      }}
      {...rest}
    >
      {children}
    </div>
  );
}
