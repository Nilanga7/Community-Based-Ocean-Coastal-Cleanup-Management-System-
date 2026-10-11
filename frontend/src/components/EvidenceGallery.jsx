import React from 'react';
import { IconButton } from '../design-system';
import PhotoPlaceholder from './PhotoPlaceholder.jsx';
import { mediaUrl } from '../api/client.js';

/** Uploaded evidence only — seeded demo reports reference placeholder URLs that don't resolve. */
function uploadedEvidence(report) {
  return (report?.evidence || []).filter((item) => item.contentType);
}

function Media({ item, cover, style }) {
  const src = mediaUrl(item.url);
  const fill = { width: '100%', height: '100%', objectFit: 'cover', display: 'block', background: 'var(--gray-900, #111)' };
  if (item.contentType.startsWith('video/')) {
    return cover ? (
      <video src={src} muted playsInline preload="metadata" style={{ ...fill, ...style }} />
    ) : (
      <video src={src} controls playsInline preload="metadata" style={{ ...fill, objectFit: 'contain', ...style }} />
    );
  }
  return <img src={src} alt="Evidence" loading="lazy" style={{ ...fill, ...style }} />;
}

/** Cover image for cards: the first uploaded photo or video, or the placeholder. */
export function EvidenceCover({ report, ratio = '16/9' }) {
  const items = uploadedEvidence(report);
  if (!items.length) return <PhotoPlaceholder ratio={ratio} count={report.photoUrls?.length} />;
  return (
    <div style={{ width: '100%', aspectRatio: ratio, overflow: 'hidden' }}>
      <Media item={items[0]} cover />
    </div>
  );
}

/** Full evidence viewer with thumbnails, for report and project pages. */
export default function EvidenceGallery({ report, ratio = '4/3', radius = 'var(--radius-lg)' }) {
  const items = uploadedEvidence(report);
  const [index, setIndex] = React.useState(0);

  if (!items.length) {
    return <PhotoPlaceholder ratio={ratio} count={report.photoUrls?.length} style={{ borderRadius: radius }} />;
  }

  const current = items[Math.min(index, items.length - 1)];
  const step = (delta) => setIndex((i) => (i + delta + items.length) % items.length);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <div style={{ position: 'relative', width: '100%', aspectRatio: ratio, borderRadius: radius, overflow: 'hidden', background: 'var(--surface-sunken)' }}>
        <Media key={current.url} item={current} style={{ objectFit: 'contain' }} />
        {items.length > 1 ? (
          <>
            <IconButton icon="chevron-left" label="Previous" variant="secondary" size="sm" onClick={() => step(-1)} style={{ position: 'absolute', left: 8, top: '50%', transform: 'translateY(-50%)' }} />
            <IconButton icon="chevron-right" label="Next" variant="secondary" size="sm" onClick={() => step(1)} style={{ position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)' }} />
          </>
        ) : null}
      </div>
      {items.length > 1 ? (
        <div style={{ display: 'flex', gap: 6, overflowX: 'auto' }}>
          {items.map((item, i) => (
            <button
              key={item.url}
              type="button"
              aria-label={`Show evidence ${i + 1}`}
              onClick={() => setIndex(i)}
              style={{
                flex: '0 0 64px',
                height: 64,
                borderRadius: 'var(--radius-sm)',
                overflow: 'hidden',
                cursor: 'pointer',
                outline: i === index ? '2px solid var(--accent)' : '1px solid var(--border-subtle)',
                outlineOffset: -1,
                padding: 0,
              }}
            >
              <Media item={item} cover />
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}
