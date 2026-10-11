import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Icon, Card, Badge, ProgressBar } from '../design-system';
import { PROJECT_STATUS_LABEL, PROJECT_STATUS_TONE, locationLine, plural } from '../lib/format.js';
import ProjectStatusBar from './ProjectStatusBar.jsx';

export default function ProjectCard({ project, to }) {
  const navigate = useNavigate();
  return (
    <Card interactive onClick={() => navigate(to)} style={{ cursor: 'pointer' }}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 }}>
          <span style={{ font: '600 12px/1.5 var(--font-mono)', color: 'var(--text-muted)' }}>{project.reference}</span>
          <Badge tone={PROJECT_STATUS_TONE[project.status]}>{PROJECT_STATUS_LABEL[project.status]}</Badge>
        </div>
        <h3 style={{ font: 'var(--text-label)', fontSize: 15, color: 'var(--text-strong)' }}>{project.title}</h3>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6, font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
          <Icon name="map-pin" size="xs" />
          {locationLine(project)}
        </span>
        <ProjectStatusBar project={project} compact />
        <ProgressBar
          value={project.completionPercentage}
          valueLabel={`${project.completionPercentage}%`}
          tone={project.status === 'COMPLETED' ? 'verified' : 'accent'}
        />
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, font: 'var(--text-caption)', color: 'var(--text-body-color)' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <Icon name="users" size="xs" />
            {project.resources?.volunteersNeeded
              ? `${project.volunteerCount} of ${project.resources.volunteersNeeded} volunteers`
              : plural(project.volunteerCount, 'volunteer')}
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <Icon name="anchor" size="xs" />
            {project.resources?.diversNeeded
              ? `${project.diverCount} of ${project.resources.diversNeeded} divers`
              : plural(project.diverCount, 'diver')}
          </span>
          {project.joined ? (
            <Badge tone="success" size="sm" icon="check" style={{ marginLeft: 'auto' }}>Joined</Badge>
          ) : null}
        </div>
      </div>
    </Card>
  );
}
