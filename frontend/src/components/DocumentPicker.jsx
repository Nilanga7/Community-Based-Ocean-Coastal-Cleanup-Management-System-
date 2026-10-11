import React from 'react';
import { Field, Icon, IconButton } from '../design-system';
import { formatBytes } from '../lib/format.js';

const ACCEPT = '.pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png';
const ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];
const MAX_BYTES = 5 * 1024 * 1024;
const MAX_FILES = 5;

// The design system's FileDrop is worded for photo evidence and has no working file input,
// so certificate uploads use this picker with the same visual treatment.
export default function DocumentPicker({ label, hint, required, files, onChange, actionLabel = 'Add certificate files' }) {
  const inputRef = React.useRef(null);
  const [over, setOver] = React.useState(false);
  const [problem, setProblem] = React.useState(null);

  function add(list) {
    const next = [...files];
    let issue = null;
    for (const file of Array.from(list || [])) {
      const allowed = ALLOWED_TYPES.includes(file.type) || /\.(pdf|jpe?g|png)$/i.test(file.name);
      if (!allowed) {
        issue = `${file.name} isn't a PDF, JPG or PNG.`;
        continue;
      }
      if (file.size > MAX_BYTES) {
        issue = `${file.name} is larger than 5 MB.`;
        continue;
      }
      if (next.some((f) => f.name === file.name && f.size === file.size)) continue;
      if (next.length >= MAX_FILES) {
        issue = `You can attach up to ${MAX_FILES} files.`;
        break;
      }
      next.push(file);
    }
    setProblem(issue);
    onChange(next);
  }

  return (
    <Field label={label} hint={hint} error={problem} required={required}>
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => {
          e.preventDefault();
          setOver(true);
        }}
        onDragLeave={() => setOver(false)}
        onDrop={(e) => {
          e.preventDefault();
          setOver(false);
          add(e.dataTransfer.files);
        }}
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 6,
          width: '100%',
          padding: 'var(--space-5)',
          textAlign: 'center',
          borderRadius: 'var(--radius-lg)',
          border: '1px dashed ' + (over ? 'var(--accent)' : 'var(--border-default)'),
          background: over ? 'var(--accent-soft)' : 'var(--surface-page-warm)',
          cursor: 'pointer',
          transition: 'var(--transition-control)',
        }}
      >
        <Icon name="upload" size="lg" color="var(--sea-600)" />
        <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>{actionLabel}</span>
        <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
          PDF, JPG or PNG · up to 5 MB each · drag them here or choose files
        </span>
      </button>
      <input
        ref={inputRef}
        type="file"
        multiple
        accept={ACCEPT}
        style={{ display: 'none' }}
        onChange={(e) => {
          add(e.target.files);
          e.target.value = '';
        }}
      />

      {files.length ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 4 }}>
          {files.map((file) => (
            <div
              key={`${file.name}-${file.size}`}
              style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 4px 4px 10px', borderRadius: 'var(--radius-sm)', background: 'var(--surface-sunken)' }}
            >
              <Icon name="file-text" size="sm" color="var(--text-muted)" />
              <span style={{ flex: 1, minWidth: 0, font: 'var(--text-caption)', color: 'var(--text-body-color)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {file.name}
              </span>
              <span style={{ font: 'var(--text-micro)', color: 'var(--text-muted)' }}>{formatBytes(file.size)}</span>
              <IconButton
                icon="x"
                size="sm"
                label={`Remove ${file.name}`}
                onClick={() => onChange(files.filter((f) => f !== file))}
              />
            </div>
          ))}
        </div>
      ) : null}
    </Field>
  );
}
