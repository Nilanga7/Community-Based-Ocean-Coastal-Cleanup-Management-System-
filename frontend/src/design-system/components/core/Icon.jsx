import React from 'react';

const SIZES = { xs: 14, sm: 16, md: 20, lg: 24, xl: 32 };

/** Resolve where the icon SVGs live. Override globally with window.__DS_ICON_BASE__. */
export function iconBase() {
  return (typeof window !== 'undefined' && window.__DS_ICON_BASE__) || '/assets/icons/';
}

export function Icon({ name, size = 'md', color = 'currentColor', title, style, ...rest }) {
  const px = SIZES[size] || size;
  const url = 'url("' + iconBase() + name + '.svg")';
  return (
    <span
      role={title ? 'img' : 'presentation'}
      aria-label={title}
      aria-hidden={title ? undefined : true}
      style={{
        display: 'inline-block',
        width: px,
        height: px,
        flex: '0 0 auto',
        background: color,
        WebkitMaskImage: url,
        maskImage: url,
        WebkitMaskRepeat: 'no-repeat',
        maskRepeat: 'no-repeat',
        WebkitMaskPosition: 'center',
        maskPosition: 'center',
        WebkitMaskSize: 'contain',
        maskSize: 'contain',
        ...style,
      }}
      {...rest}
    />
  );
}
