import React from 'react';
import { Button, Select, Textarea, Input, Alert, Card } from '../design-system';
import { api } from '../api/index.js';

const EMPTY = { stage: 'DURING', note: '', completionPercentage: '', debrisRemovedKg: '' };

const STAGES = [
  { value: 'BEFORE', label: 'Before' },
  { value: 'DURING', label: 'During' },
  { value: 'AFTER', label: 'After' },
];

export default function ProgressUpdateForm({ projectId, onUpdated }) {
  const [update, setUpdate] = React.useState(EMPTY);
  const [error, setError] = React.useState(null);
  const [busy, setBusy] = React.useState(false);

  const set = (key) => (event) => setUpdate((u) => ({ ...u, [key]: event.target.value }));

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const updated = await api.projects.addUpdate(projectId, {
        stage: update.stage,
        note: update.note,
        completionPercentage: update.completionPercentage === '' ? null : Number(update.completionPercentage),
        debrisRemovedKg: update.debrisRemovedKg === '' ? null : Number(update.debrisRemovedKg),
      });
      setUpdate(EMPTY);
      onUpdated(updated);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card padding="lg">
      <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
        <div>
          <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>Record progress</span>
          <p style={{ font: 'var(--text-caption)', color: 'var(--text-muted)', marginTop: 2 }}>
            Reaching 100% closes the project, marks the site cleaned, and tells everyone who joined.
          </p>
        </div>
        {error ? <Alert tone="danger" title="That didn't post">{error}</Alert> : null}
        <Select label="Stage" options={STAGES} value={update.stage} onChange={set('stage')} />
        <Textarea label="Note" required rows={2} placeholder="What happened on site?" value={update.note} onChange={set('note')} />
        <div style={{ display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap' }}>
          <Input label="Completion" type="number" min="0" max="100" suffix="%" value={update.completionPercentage} onChange={set('completionPercentage')} style={{ flex: 1, minWidth: 140 }} />
          <Input label="Debris removed" type="number" min="0" step="0.1" suffix="kg" value={update.debrisRemovedKg} onChange={set('debrisRemovedKg')} style={{ flex: 1, minWidth: 140 }} />
        </div>
        <Button type="submit" disabled={busy || !update.note.trim()} style={{ alignSelf: 'flex-start' }}>
          {busy ? 'Posting…' : 'Post update'}
        </Button>
      </form>
    </Card>
  );
}
