import React from 'react';
import { Avatar, Badge, Alert } from '../design-system';
import ChipButton from './ChipButton.jsx';
import { api } from '../api/index.js';
import UserLink from './UserLink.jsx';
import { mediaUrl } from '../api/client.js';

/** Visible to the project owner and officials; the owner rates each person once the cleanup is done. */
export default function ParticipantList({ project, canRate, onRated }) {
  const [busyId, setBusyId] = React.useState(null);
  const [error, setError] = React.useState(null);

  async function rate(participant, mark) {
    if (busyId) return;
    setBusyId(participant.id);
    setError(null);
    try {
      onRated(await api.projects.mark(project.id, participant.id, mark));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  }

  if (!project.participants?.length) {
    return <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>No one has joined yet.</p>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
      {canRate ? (
        <p style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
          Rate each person's contribution from 1 to 5. It shows on their profile and helps divers find paid work.
        </p>
      ) : null}
      {error ? <Alert tone="danger" title="That didn't save">{error}</Alert> : null}

      {project.participants.map((p) => (
        <div key={p.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <Avatar name={p.user?.fullName || ''} src={mediaUrl(p.user?.avatarUrl)} size="sm" role={p.role === 'DIVER' ? 'diver' : undefined} />
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}><UserLink user={p.user} /></span>
              <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{p.role === 'DIVER' ? 'Diver' : 'Volunteer'}</span>
            </div>
          </div>

          {canRate ? (
            <div role="group" aria-label={`Rate ${p.user?.fullName}`} style={{ display: 'flex', gap: 6, opacity: busyId === p.id ? 0.5 : 1 }}>
              {[1, 2, 3, 4, 5].map((n) => (
                <ChipButton key={n} selected={p.mark === n} onClick={() => rate(p, n)} aria-label={`${n} out of 5`}>
                  {n}
                </ChipButton>
              ))}
            </div>
          ) : p.mark ? (
            <Badge tone="accent">{p.mark} / 5</Badge>
          ) : null}
        </div>
      ))}
    </div>
  );
}
