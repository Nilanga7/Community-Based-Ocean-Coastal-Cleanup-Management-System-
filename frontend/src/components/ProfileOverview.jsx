import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Avatar, Badge, Card, Icon, Tag } from '../design-system';
import {
  CERTIFICATION_LABEL,
  ORGANIZATION_LABEL,
  PROJECT_STATUS_LABEL,
  PROJECT_STATUS_TONE,
  ROLE_LABEL,
  formatDate,
  plural,
} from '../lib/format.js';
import { mediaUrl } from '../api/client.js';

export function DetailRow({ icon, label, children }) {
  return (
    <div style={{ display: 'flex', gap: 'var(--space-3)', alignItems: 'flex-start', padding: 'var(--space-3) 0', borderTop: '1px solid var(--border-subtle)' }}>
      <Icon name={icon} size="sm" color="var(--text-muted)" style={{ marginTop: 2 }} />
      <span style={{ width: 150, flex: '0 0 auto', font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>{label}</span>
      <span style={{ flex: 1, minWidth: 0, font: 'var(--text-body-sm)', color: 'var(--text-heading)', overflowWrap: 'anywhere' }}>{children}</span>
    </div>
  );
}

export function DetailSection({ title, children }) {
  return (
    <section style={{ display: 'flex', flexDirection: 'column' }}>
      <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)', paddingBottom: 'var(--space-2)' }}>{title}</span>
      <div style={{ borderBottom: '1px solid var(--border-subtle)' }}>{children}</div>
    </section>
  );
}

function Stat({ value, label }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 2, padding: 'var(--space-3) var(--space-4)', borderRadius: 'var(--radius-md)', background: 'var(--surface-sunken)' }}>
      <span style={{ font: '700 22px/1.1 var(--font-display)', color: 'var(--text-strong)' }}>{value}</span>
      <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{label}</span>
    </div>
  );
}

/** Everything anyone can see on a member's profile; `actions` sits beside the name (e.g. Edit). */
export default function ProfileOverview({ profile, actions, avatar, children }) {
  const navigate = useNavigate();
  const isDiver = profile.role === 'DIVER';
  const isOrganisation = profile.role === 'ORGANIZATION';
  const owned = profile.ownedProjects || [];
  const place = [profile.city, profile.province].filter(Boolean).join(', ');
  const displayName = isOrganisation && profile.organizationName ? profile.organizationName : profile.fullName;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 'var(--space-4)', flexWrap: 'wrap' }}>
        {avatar ?? <Avatar name={displayName} src={mediaUrl(profile.avatarUrl)} role={isDiver ? 'diver' : undefined} size={88} />}
        <div style={{ flex: 1, minWidth: 200 }}>
          <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>{displayName}</h1>
          <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
            {[place, profile.memberSince ? `Member since ${formatDate(profile.memberSince)}` : null].filter(Boolean).join(' · ')}
          </span>
          <div style={{ display: 'flex', gap: 6, marginTop: 8, flexWrap: 'wrap' }}>
            <Badge tone="accent" icon={isDiver ? 'anchor' : 'user'}>{ROLE_LABEL[profile.role] || profile.role}</Badge>
            {(isDiver || isOrganisation) && profile.verified ? <Badge tone="success" icon="shield-check">Verified</Badge> : null}
            {owned.length ? <Badge tone="success" icon="flag">Project owner</Badge> : null}
            {profile.averageMark != null ? <Badge tone="success" icon="badge-check">Rated {profile.averageMark} / 5</Badge> : null}
          </div>
        </div>
        {actions}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: 'var(--space-3)' }}>
        <Stat value={profile.reportsSubmitted ?? 0} label={profile.reportsSubmitted === 1 ? 'Report submitted' : 'Reports submitted'} />
        <Stat value={owned.length} label={owned.length === 1 ? 'Project owned' : 'Projects owned'} />
        {isDiver ? <Stat value={profile.completedProjects ?? 0} label="Cleanups completed" /> : null}
        {profile.averageMark != null ? <Stat value={`${profile.averageMark} / 5`} label={`From ${plural(profile.markedCleanups, 'rating')}`} /> : null}
      </div>

      {isOrganisation ? (
        <DetailSection title="Organisation">
          <DetailRow icon="building-2" label="Name">{profile.organizationName || '—'}</DetailRow>
          <DetailRow icon="flag" label="Type">{ORGANIZATION_LABEL[profile.organizationType] || '—'}</DetailRow>
          <DetailRow icon="arrow-up-right" label="Website">
            {profile.websiteUrl ? (
              <a href={profile.websiteUrl} target="_blank" rel="noopener noreferrer">{profile.websiteUrl.replace(/^https?:\/\//, '')}</a>
            ) : '—'}
          </DetailRow>
        </DetailSection>
      ) : null}

      {isDiver ? (
        <DetailSection title="Diving">
          <DetailRow icon="badge-check" label="Certification">{CERTIFICATION_LABEL[profile.certificationLevel] || '—'}</DetailRow>
          <DetailRow icon="anchor" label="Experience">{profile.experienceYears != null ? plural(profile.experienceYears, 'year') : '—'}</DetailRow>
          <DetailRow icon="map-pin" label="Preferred regions">
            {profile.preferredRegions?.length ? (
              <span style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                {profile.preferredRegions.map((region) => <Tag key={region}>{region}</Tag>)}
              </span>
            ) : 'Any region'}
          </DetailRow>
        </DetailSection>
      ) : null}

      {children}

      <section style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
        <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>Cleanup projects as project owner</span>
        {owned.length ? (
          owned.map((project) => (
            <Card key={project.id} padding="md" interactive onClick={() => navigate(`/app/cleanups/${project.id}`)} style={{ cursor: 'pointer' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                <span style={{ width: 40, height: 40, flex: '0 0 auto', borderRadius: '50%', background: 'var(--status-verified-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Icon name="flag" size="sm" color="var(--status-verified)" />
                </span>
                <div style={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <span style={{ font: '600 12px/1.4 var(--font-mono)', color: 'var(--text-muted)' }}>{project.reference}</span>
                  <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {project.title}
                  </span>
                </div>
                <Badge tone={PROJECT_STATUS_TONE[project.status]}>{PROJECT_STATUS_LABEL[project.status]}</Badge>
                <Icon name="chevron-right" size="sm" color="var(--text-muted)" />
              </div>
            </Card>
          ))
        ) : (
          <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>No cleanup projects yet.</span>
        )}
      </section>
    </div>
  );
}
