import React from 'react';
import { Icon } from './Icon.jsx';

const BOX = { sm: 32, md: 40, lg: 48 };
const GLYPH = { sm: 'sm', md: 'md', lg: 'md' };

export function IconButton({ icon, label, variant = 'ghost', size = 'md', disabled = false, style, ...rest }) {
  const [hover, setHover] = React.useState(false);
  const [focus, setFocus] = React.useState(false);
  const skins = {
    ghost: { background: hover ? 'var(--gray-100)' : 'transparent', color: 'var(--text-body-color)', border: '1px solid transparent' },
    secondary: { background: hover ? 'var(--gray-50)' : 'var(--surface-card)', color: 'var(--text-heading)', border: '1px solid var(--border-default)' },
    primary: { background: hover ? 'var(--accent-hover)' : 'var(--accent)', color: 'var(--text-on-accent)', border: '1px solid transparent' },
    inverse: { background: hover ? 'rgba(255,255,255,0.16)' : 'transparent', color: 'var(--white)', border: '1px solid transparent' },
  };
  return (
    <button
      type="button"
      aria-label={label}
      title={label}
      disabled={disabled}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      onFocus={() => setFocus(true)}
      onBlur={() => setFocus(false)}
      style={{
        width: BOX[size],
        height: BOX[size],
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: 'var(--radius-md)',
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.45 : 1,
        transition: 'var(--transition-control)',
        outline: 'none',
        boxShadow: focus ? 'var(--shadow-focus)' : undefined,
        ...skins[variant],
        ...style,
      }}
      {...rest}
    >
      <Icon name={icon} size={GLYPH[size]} />
    </button>
  );
}
