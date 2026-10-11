import React from 'react';

export function Tooltip({ label, placement = 'top', children, style }) {
  const [show, setShow] = React.useState(false);
  const pos =
    placement === 'bottom' ? { top: 'calc(100% + 6px)', left: '50%', transform: 'translateX(-50%)' }
    : placement === 'left' ? { right: 'calc(100% + 6px)', top: '50%', transform: 'translateY(-50%)' }
    : placement === 'right' ? { left: 'calc(100% + 6px)', top: '50%', transform: 'translateY(-50%)' }
    : { bottom: 'calc(100% + 6px)', left: '50%', transform: 'translateX(-50%)' };
  return (
    <span
      style={{ position: 'relative', display: 'inline-flex', ...style }}
      onMouseEnter={() => setShow(true)}
      onMouseLeave={() => setShow(false)}
      onFocus={() => setShow(true)}
      onBlur={() => setShow(false)}
    >
      {children}
      <span
        role="tooltip"
        style={{
          position: 'absolute',
          ...pos,
          padding: '5px 9px',
          borderRadius: 'var(--radius-sm)',
          background: 'var(--sea-950)',
          color: 'var(--white)',
          font: 'var(--text-micro)',
          letterSpacing: 'var(--tracking-micro)',
          whiteSpace: 'nowrap',
          pointerEvents: 'none',
          opacity: show ? 1 : 0,
          transition: 'opacity var(--duration-fast) var(--ease-standard)',
          zIndex: 40,
        }}
      >
        {label}
      </span>
    </span>
  );
}
