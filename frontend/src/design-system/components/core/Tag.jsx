import React from 'react';
import { Icon } from './Icon.jsx';

export function Tag({ selected = false, onRemove, icon, children, style, ...rest }) {
  const [hover, setHover] = React.useState(false);
  const clickable = Boolean(rest.onClick);
  return (
    <span
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 6,
        height: 30,
        padding: onRemove ? '0 6px 0 10px' : '0 12px',
        borderRadius: 'var(--radius-pill)',
        font: '500 13px/1 var(--font-body)',
        cursor: clickable ? 'pointer' : 'default',
        transition: 'var(--transition-control)',
        background: selected ? 'var(--sea-900)' : hover && clickable ? 'var(--gray-100)' : 'var(--surface-card)',
        color: selected ? 'var(--white)' : 'var(--text-body-color)',
        border: '1px solid ' + (selected ? 'var(--sea-900)' : 'var(--border-subtle)'),
        ...style,
      }}
      {...rest}
    >
      {icon ? <Icon name={icon} size="xs" /> : null}
      {children}
      {onRemove ? (
        <button
          type="button"
          aria-label="Remove"
          onClick={(e) => { e.stopPropagation(); onRemove(e); }}
          style={{ display: 'inline-flex', width: 18, height: 18, alignItems: 'center', justifyContent: 'center', borderRadius: '50%', background: 'transparent', color: 'inherit', cursor: 'pointer' }}
        >
          <Icon name="x" size="xs" />
        </button>
      ) : null}
    </span>
  );
}
