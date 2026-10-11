import React from 'react';

export function ProgressBar({ value = 0, max = 100, label, valueLabel, tone = 'accent', size = 'md', style, ...rest }) {
  const pct = Math.max(0, Math.min(100, (value / max) * 100));
  const fills = {
    accent: 'var(--accent)',
    verified: 'var(--status-verified)',
    warning: 'var(--warning)',
    danger: 'var(--danger)',
    sea: 'var(--sea-600)',
  };
  const h = size === 'sm' ? 4 : size === 'lg' ? 12 : 8;
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6, ...style }} {...rest}>
      {label || valueLabel ? (
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
          <span style={{ font: 'var(--text-caption)', color: 'var(--text-body-color)' }}>{label}</span>
          <span style={{ font: '600 13px/1.45 var(--font-mono)', color: 'var(--text-strong)' }}>{valueLabel}</span>
        </div>
      ) : null}
      <div role="progressbar" aria-valuenow={value} aria-valuemax={max} style={{ height: h, borderRadius: 'var(--radius-pill)', background: 'var(--gray-200)', overflow: 'hidden' }}>
        <div style={{ width: pct + '%', height: '100%', borderRadius: 'var(--radius-pill)', background: fills[tone], transition: 'width var(--duration-slow) var(--ease-out)' }} />
      </div>
    </div>
  );
}
