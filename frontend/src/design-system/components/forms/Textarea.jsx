import React from 'react';
import { Field } from './Field.jsx';

function controlStyle({ focus, invalid, disabled, size }) {
  const heights = { sm: 34, md: 42, lg: 48 };
  return {
    width: '100%',
    height: heights[size] || heights.md,
    padding: '0 12px',
    font: 'var(--text-body)',
    color: 'var(--text-strong)',
    background: disabled ? 'var(--surface-sunken)' : 'var(--surface-card)',
    border: '1px solid ' + (invalid ? 'var(--danger)' : focus ? 'var(--border-accent)' : 'var(--border-default)'),
    borderRadius: 'var(--radius-md)',
    boxShadow: focus ? 'var(--shadow-focus)' : 'none',
    outline: 'none',
    transition: 'var(--transition-control)',
    cursor: disabled ? 'not-allowed' : 'text',
  };
}

export function Textarea({ label, hint, error, required, rows = 4, maxLength, value, disabled, id, style, ...rest }) {
  const [focus, setFocus] = React.useState(false);
  const generatedId = React.useId();
  const areaId = id || generatedId;
  return (
    <Field label={label} hint={hint} error={error} required={required} htmlFor={areaId} style={style}>
      <textarea
        id={areaId}
        rows={rows}
        maxLength={maxLength}
        value={value}
        disabled={disabled}
        onFocus={() => setFocus(true)}
        onBlur={() => setFocus(false)}
        style={{
          ...controlStyle({ focus, invalid: Boolean(error), disabled, size: 'md' }),
          height: 'auto',
          minHeight: rows * 24 + 20,
          padding: '10px 12px',
          lineHeight: 1.55,
          resize: 'vertical',
          fontFamily: 'var(--font-body)',
        }}
        {...rest}
      />
      {maxLength ? (
        <span style={{ font: 'var(--text-micro)', color: 'var(--text-muted)', alignSelf: 'flex-end', letterSpacing: 'var(--tracking-micro)' }}>
          {String(value || '').length}/{maxLength}
        </span>
      ) : null}
    </Field>
  );
}
