import React from 'react';
import ChipButton from '../../components/ChipButton.jsx';
import { api } from '../../api/index.js';
import { useApi } from '../../hooks/useApi.js';
import { Async } from '../../components/AsyncState.jsx';
import ProjectCard from '../../components/ProjectCard.jsx';

const FILTERS = [
  { value: 'all', label: 'All' },
  { value: 'PLANNED', label: 'Planned' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'COMPLETED', label: 'Completed' },
];

export default function Projects() {
  const [status, setStatus] = React.useState('all');
  const state = useApi(() => api.projects.list({ status }), [status]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      <div>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Cleanup projects</h1>
        <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)', marginTop: 2 }}>
          Progress tracking from approval to completion.
        </p>
      </div>

      <div style={{ display: 'flex', gap: 8 }}>
        {FILTERS.map((f) => (
          <ChipButton key={f.value} selected={status === f.value} onClick={() => setStatus(f.value)}>
            {f.label}
          </ChipButton>
        ))}
      </div>

      <Async
        state={state}
        isEmpty={(list) => !list?.length}
        empty="No cleanup projects in this state yet."
        emptyIcon="map-pin"
      >
        {(projects) => (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 'var(--space-4)' }}>
            {projects.map((project) => (
              <ProjectCard key={project.id} project={project} to={`/console/projects/${project.id}`} />
            ))}
          </div>
        )}
      </Async>
    </div>
  );
}
