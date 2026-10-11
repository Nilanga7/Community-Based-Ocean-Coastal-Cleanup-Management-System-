import React from 'react';
import { Icon } from '../core/Icon.jsx';
import { Field } from './Field.jsx';

export function FileDrop({ label, hint, error, required, files = [], accept = 'image/*,video/*', onPick, style }) {
  const [over, setOver] = React.useState(false);
  return (
    <Field label={label} hint={hint} error={error} required={required} style={style}>
      <div
        onDragOver={(e) => { e.preventDefault(); setOver(true); }}
        onDragLeave={() => setOver(false)}
        onDrop={(e) => { e.preventDefault(); setOver(false); if (onPick) onPick(e); }}
        onClick={onPick}
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 8,
          padding: 'var(--space-6)',
          textAlign: 'center',
          borderRadius: 'var(--radius-lg)',
          border: '1px dashed ' + (over ? 'var(--accent)' : error ? 'var(--danger)' : 'var(--border-default)'),
          background: over ? 'var(--accent-soft)' : 'var(--surface-page-warm)',
          cursor: 'pointer',
          transition: 'var(--transition-control)',
        }}
      >
        <Icon name="camera" size="lg" color="var(--sea-600)" />
        <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>Add photo or video evidence</span>
        <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>Drag files here, or tap to use your camera</span>
        <input type="file" accept={accept} multiple style={{ display: 'none' }} />
      </div>
      {files.length ? (
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 4 }}>
          {files.map((file, i) => (
            <span key={i} style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '5px 10px', borderRadius: 'var(--radius-sm)', background: 'var(--surface-sunken)', font: 'var(--text-caption)', color: 'var(--text-body-color)' }}>
              <Icon name="image" size="xs" color="var(--text-muted)" />
              {typeof file === 'string' ? file : file.name}
            </span>
          ))}
        </div>
      ) : null}
    </Field>
  );
}
