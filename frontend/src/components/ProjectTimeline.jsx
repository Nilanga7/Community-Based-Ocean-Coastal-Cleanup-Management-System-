import React from 'react';
import { Badge } from '../design-system';
import { formatDate } from '../lib/format.js';

const STAGE_LABEL = { BEFORE: 'Before', DURING: 'During', AFTER: 'After' };

export default function ProjectTimeline({ updates }) {
  if (!updates?.length) {
    return <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>No progress recorded yet.</p>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column' }}>
      {updates.map((u, i) => (
        <div key={u.id} style={{ display: 'flex', gap: 12 }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <span style={{ width: 10, height: 10, borderRadius: '50%', background: 'var(--accent)', marginTop: 4 }} />
            {i < updates.length - 1 ? <span style={{ width: 2, flex: 1, background: 'var(--border-subtle)' }} /> : null}
          </div>
          <div style={{ paddingBottom: 'var(--space-4)' }}>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <Badge tone="neutral" size="sm">{STAGE_LABEL[u.stage]}</Badge>
              <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{formatDate(u.createdAt, false)}</span>
              {u.completionPercentage != null ? (
                <span style={{ font: '600 12px/1 var(--font-mono)', color: 'var(--text-muted)' }}>{u.completionPercentage}%</span>
              ) : null}
            </div>
            <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-body-color)', marginTop: 4 }}>{u.note}</p>
          </div>
        </div>
      ))}
    </div>
  );
}
