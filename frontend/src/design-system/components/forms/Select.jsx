import React from 'react';
import { Field } from './Field.jsx';
import { Icon } from '../core/Icon.jsx';

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

export function Select({ label, hint, error, required, options = [], placeholder, size = 'md', disabled, id, style, ...rest }) {
  const [focus, setFocus] = React.useState(false);
  const generatedId = React.useId();
  const selId = id || generatedId;
  return (
    <Field label={label} hint={hint} error={error} required={required} htmlFor={selId} style={style}>
      <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
        <select
          id={selId}
          disabled={disabled}
          onFocus={() => setFocus(true)}
          onBlur={() => setFocus(false)}
          style={{
            ...controlStyle({ focus, invalid: Boolean(error), disabled, size }),
            appearance: 'none',
            paddingRight: 38,
            cursor: disabled ? 'not-allowed' : 'pointer',
            fontFamily: 'var(--font-body)',
          }}
          {...rest}
        >
          {placeholder ? <option value="">{placeholder}</option> : null}
          {options.map((o) => {
            const opt = typeof o === 'string' ? { value: o, label: o } : o;
            return <option key={opt.value} value={opt.value}>{opt.label}</option>;
          })}
        </select>
        <span style={{ position: 'absolute', right: 12, display: 'flex', pointerEvents: 'none' }}>
          <Icon name="chevron-down" size="sm" color="var(--text-muted)" />
        </span>
      </div>
    </Field>
  );
}
