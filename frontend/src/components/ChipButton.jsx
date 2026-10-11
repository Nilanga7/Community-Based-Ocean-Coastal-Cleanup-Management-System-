import React from 'react';
import { Tag } from '../design-system';

// Tag renders a <span>, which keyboards and screen readers can't operate. Selectable chips
// (filters, ratings, the role picker) wrap it in a real button instead.
export default function ChipButton({ selected = false, icon, onClick, children, style, ...rest }) {
  return (
    <button
      type="button"
      aria-pressed={selected}
      onClick={onClick}
      style={{ background: 'none', border: 0, padding: 0, cursor: 'pointer', borderRadius: 'var(--radius-pill)', ...style }}
      {...rest}
    >
      <Tag selected={selected} icon={icon} style={{ cursor: 'pointer' }}>
        {children}
      </Tag>
    </button>
  );
}
