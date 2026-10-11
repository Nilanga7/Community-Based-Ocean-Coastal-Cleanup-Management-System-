import React from 'react';
import { Field, Icon, IconButton } from '../design-system';
import { formatBytes } from '../lib/format.js';

const ACCEPT = 'image/jpeg,image/png,image/webp,video/mp4,video/quicktime,video/webm,.jpg,.jpeg,.png,.webp,.mp4,.mov,.webm';
const PHOTO_ACCEPT = 'image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp';
const PHOTO_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const VIDEO_TYPES = ['video/mp4', 'video/quicktime', 'video/webm'];
const MAX_PHOTO_BYTES = 8 * 1024 * 1024;
const MAX_VIDEO_BYTES = 25 * 1024 * 1024;
const MAX_FILES = 6;

const isVideo = (file) => VIDEO_TYPES.includes(file.type) || /\.(mp4|mov|webm)$/i.test(file.name);
const isPhoto = (file) => PHOTO_TYPES.includes(file.type) || /\.(jpe?g|png|webp)$/i.test(file.name);

// Object URLs are an external resource: created and revoked with the file, which also keeps
// previews working under StrictMode double effects.
function Preview({ file, onRemove }) {
  const [url, setUrl] = React.useState(null);
  React.useEffect(() => {
    const objectUrl = URL.createObjectURL(file);
    setUrl(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [file]);

  const video = isVideo(file);
  return (
    <div style={{ position: 'relative', borderRadius: 'var(--radius-md)', overflow: 'hidden', background: 'var(--surface-sunken)', aspectRatio: '1 / 1' }}>
      {url ? (
        video ? (
          <video src={url} muted playsInline preload="metadata" style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} />
        ) : (
          <img src={url} alt={file.name} style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} />
        )
      ) : null}
      <span
        style={{
          position: 'absolute',
          left: 6,
          bottom: 6,
          display: 'inline-flex',
          alignItems: 'center',
          gap: 4,
          padding: '2px 6px',
          borderRadius: 'var(--radius-sm)',
          background: 'var(--surface-glass)',
          backdropFilter: 'blur(4px)',
          font: 'var(--text-micro)',
          color: 'var(--text-strong)',
        }}
      >
        <Icon name={video ? 'camera' : 'image'} size="xs" />
        {video ? 'Video' : 'Photo'} · {formatBytes(file.size)}
      </span>
      <IconButton
        icon="x"
        size="sm"
        variant="secondary"
        label={`Remove ${file.name}`}
        onClick={onRemove}
        style={{ position: 'absolute', top: 6, right: 6 }}
      />
    </div>
  );
}

// The design system's FileDrop has no working file input, so evidence uses this picker with the
// same look, plus previews of what will be uploaded.
export default function EvidencePicker({ label, hint, files, onChange, photosOnly = false }) {
  const inputRef = React.useRef(null);
  const [over, setOver] = React.useState(false);
  const [problem, setProblem] = React.useState(null);

  function add(list) {
    const next = [...files];
    let issue = null;
    for (const file of Array.from(list || [])) {
      if (photosOnly ? !isPhoto(file) : !isPhoto(file) && !isVideo(file)) {
        issue = photosOnly
          ? `${file.name} isn't a photo. Use JPG, PNG or WebP.`
          : `${file.name} isn't a supported photo or video. Use JPG, PNG, WebP, MP4, MOV or WebM.`;
        continue;
      }
      const limit = isVideo(file) ? MAX_VIDEO_BYTES : MAX_PHOTO_BYTES;
      if (file.size > limit) {
        issue = `${file.name} is larger than ${isVideo(file) ? '25' : '8'} MB.`;
        continue;
      }
      if (next.some((f) => f.name === file.name && f.size === file.size)) continue;
      if (next.length >= MAX_FILES) {
        issue = `You can add up to ${MAX_FILES} photos or videos.`;
        break;
      }
      next.push(file);
    }
    setProblem(issue);
    onChange(next);
  }

  return (
    <Field label={label} hint={hint} error={problem}>
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
          gap: 8,
          width: '100%',
          padding: 'var(--space-6)',
          textAlign: 'center',
          borderRadius: 'var(--radius-lg)',
          border: '1px dashed ' + (over ? 'var(--accent)' : problem ? 'var(--danger)' : 'var(--border-default)'),
          background: over ? 'var(--accent-soft)' : 'var(--surface-page-warm)',
          cursor: 'pointer',
          transition: 'var(--transition-control)',
        }}
      >
        <Icon name="camera" size="lg" color="var(--sea-600)" />
        <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>{photosOnly ? 'Add photos' : 'Add photo or video evidence'}</span>
        <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
          Drag files here, or tap to take a photo or choose from your gallery
        </span>
        <span style={{ font: 'var(--text-micro)', color: 'var(--text-muted)' }}>
          {photosOnly ? `Up to ${MAX_FILES} photos · 8 MB each` : `Up to ${MAX_FILES} files · photos 8 MB · videos 25 MB`}
        </span>
      </button>
      <input
        ref={inputRef}
        type="file"
        multiple
        accept={photosOnly ? PHOTO_ACCEPT : ACCEPT}
        style={{ display: 'none' }}
        onChange={(e) => {
          add(e.target.files);
          e.target.value = '';
        }}
      />

      {files.length ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(110px, 1fr))', gap: 8, marginTop: 4 }}>
          {files.map((file) => (
            <Preview key={`${file.name}-${file.size}`} file={file} onRemove={() => onChange(files.filter((f) => f !== file))} />
          ))}
        </div>
      ) : null}
    </Field>
  );
}
