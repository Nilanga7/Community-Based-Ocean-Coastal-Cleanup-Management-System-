import React from 'react';
import ChipButton from '../../components/ChipButton.jsx';
import { api } from '../../api/index.js';
import { useApi } from '../../hooks/useApi.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { Async } from '../../components/AsyncState.jsx';
import ProjectCard from '../../components/ProjectCard.jsx';

const FILTERS = [
  { value: 'all', label: 'All' },
  { value: 'PLANNED', label: 'Planned' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'COMPLETED', label: 'Completed' },
];

export default function Cleanups() {
  const { user } = useAuth();
  const [status, setStatus] = React.useState('all');
  const state = useApi(() => api.projects.list({ status }), [status, user?.id]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      <div>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Cleanups</h1>
        <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)', marginTop: 2 }}>
          Reports the government officer approved, now running as cleanup projects. Join one near you.
        </p>
      </div>

      <div style={{ display: 'flex', gap: 8, overflowX: 'auto', paddingBottom: 2 }}>
        {FILTERS.map((f) => (
          <ChipButton key={f.value} selected={status === f.value} onClick={() => setStatus(f.value)}>
            {f.label}
          </ChipButton>
        ))}
      </div>

      <Async
        state={state}
        isEmpty={(list) => !list?.length}
        empty="No cleanup projects here yet — a report becomes a project once the government officer approves it."
        emptyIcon="users"
      >
        {(projects) => (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: 'var(--space-4)' }}>
            {projects.map((project) => (
              <ProjectCard key={project.id} project={project} to={`/app/cleanups/${project.id}`} />
            ))}
          </div>
        )}
      </Async>
    </div>
  );
}
