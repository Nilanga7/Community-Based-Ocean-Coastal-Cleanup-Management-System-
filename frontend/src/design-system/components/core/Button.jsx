import React from 'react';
import { Icon } from './Icon.jsx';

const PAD = {
  sm: '0 12px',
  md: '0 16px',
  lg: '0 22px',
};
const HEIGHT = { sm: 32, md: 40, lg: 48 };
const FONT = { sm: '600 13px/1 var(--font-body)', md: '600 15px/1 var(--font-body)', lg: '600 16px/1 var(--font-body)' };

function skin(variant, state) {
  const map = {
    primary: {
      rest: { background: 'var(--accent)', color: 'var(--text-on-accent)', border: '1px solid transparent', boxShadow: 'var(--shadow-xs)' },
      hover: { background: 'var(--accent-hover)' },
      press: { background: 'var(--accent-press)' },
    },
    secondary: {
      rest: { background: 'var(--surface-card)', color: 'var(--text-heading)', border: '1px solid var(--border-default)', boxShadow: 'var(--shadow-xs)' },
      hover: { background: 'var(--gray-50)', border: '1px solid var(--border-strong)' },
      press: { background: 'var(--gray-100)' },
    },
    ghost: {
      rest: { background: 'transparent', color: 'var(--text-heading)', border: '1px solid transparent' },
      hover: { background: 'var(--gray-100)' },
      press: { background: 'var(--gray-200)' },
    },
    soft: {
      rest: { background: 'var(--accent-soft)', color: 'var(--accent-on-soft)', border: '1px solid transparent' },
      hover: { background: 'var(--tide-200)' },
      press: { background: 'var(--tide-300)' },
    },
    danger: {
      rest: { background: 'var(--danger)', color: 'var(--white)', border: '1px solid transparent', boxShadow: 'var(--shadow-xs)' },
      hover: { background: 'var(--coral-700)' },
      press: { background: 'var(--coral-700)' },
    },
    inverse: {
      rest: { background: 'var(--white)', color: 'var(--sea-900)', border: '1px solid transparent', boxShadow: 'var(--shadow-sm)' },
      hover: { background: 'var(--sea-50)' },
      press: { background: 'var(--sea-100)' },
    },
  };
  const v = map[variant] || map.primary;
  return { ...v.rest, ...(state === 'hover' ? v.hover : null), ...(state === 'press' ? v.press : null) };
}

export function Button({
  variant = 'primary',
  size = 'md',
  iconLeft,
  iconRight,
  fullWidth = false,
  disabled = false,
  loading = false,
  as = 'button',
  children,
  style,
  ...rest
}) {
  const [hover, setHover] = React.useState(false);
  const [press, setPress] = React.useState(false);
  const [focus, setFocus] = React.useState(false);
  const state = disabled || loading ? 'rest' : press ? 'press' : hover ? 'hover' : 'rest';
  const Tag = as;
  return (
    <Tag
      disabled={Tag === 'button' ? disabled || loading : undefined}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => { setHover(false); setPress(false); }}
      onMouseDown={() => setPress(true)}
      onMouseUp={() => setPress(false)}
      onFocus={() => setFocus(true)}
      onBlur={() => setFocus(false)}
      style={{
        display: fullWidth ? 'flex' : 'inline-flex',
        width: fullWidth ? '100%' : undefined,
        alignItems: 'center',
        justifyContent: 'center',
        gap: 'var(--space-2)',
        height: HEIGHT[size],
        padding: PAD[size],
        font: FONT[size],
        letterSpacing: '0.005em',
        borderRadius: 'var(--radius-md)',
        textDecoration: 'none',
        cursor: disabled || loading ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.45 : 1,
        transform: press && !disabled ? 'translateY(1px)' : 'none',
        transition: 'var(--transition-control), transform var(--duration-instant) var(--ease-standard)',
        outline: 'none',
        boxShadow: focus ? 'var(--shadow-focus)' : undefined,
        ...skin(variant, state),
        ...style,
      }}
      {...rest}
    >
      {loading ? <Icon name="loader-circle" size={size === 'lg' ? 'md' : 'sm'} /> : iconLeft ? <Icon name={iconLeft} size={size === 'lg' ? 'md' : 'sm'} /> : null}
      {children}
      {iconRight ? <Icon name={iconRight} size={size === 'lg' ? 'md' : 'sm'} /> : null}
    </Tag>
  );
}
