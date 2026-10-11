import React from 'react';
import { Icon } from '../design-system';
import { requestBlob } from '../api/client.js';

/**
 * An image the API only serves to signed-in reviewers and the reporter, so it is fetched with the
 * session token and shown from an object URL rather than a plain <img src>.
 */
export default function ProtectedImage({ url, name }) {
  const [src, setSrc] = React.useState(null);
  const [failed, setFailed] = React.useState(false);

  React.useEffect(() => {
    let objectUrl = null;
    let cancelled = false;
    requestBlob(url)
      .then((blob) => {
        if (cancelled) return;
        objectUrl = URL.createObjectURL(blob);
        setSrc(objectUrl);
      })
      .catch(() => {
        if (!cancelled) setFailed(true);
      });
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [url]);

  const box = { position: 'relative', aspectRatio: '1 / 1', borderRadius: 'var(--radius-md)', overflow: 'hidden', background: 'var(--surface-sunken)', display: 'flex', alignItems: 'center', justifyContent: 'center' };

  if (failed) {
    return (
      <div style={box} title={name}>
        <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)', textAlign: 'center', padding: 8 }}>Couldn't load {name}</span>
      </div>
    );
  }
  if (!src) {
    return (
      <div style={box} title={name}>
        <Icon name="image" size="md" color="var(--text-muted)" />
      </div>
    );
  }
  return (
    <a href={src} target="_blank" rel="noopener noreferrer" style={box} title={`Open ${name}`}>
      <img src={src} alt={name} style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} />
    </a>
  );
}
