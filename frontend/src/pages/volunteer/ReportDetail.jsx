import React from 'react';
import { useParams, useNavigate, Navigate } from 'react-router-dom';
import { Icon, IconButton, Avatar } from '../../design-system';
import MapLink from '../../components/MapLink.jsx';
import EvidenceGallery from '../../components/EvidenceGallery.jsx';
import ReportStatusBadge from '../../components/ReportStatusBadge.jsx';
import UserLink from '../../components/UserLink.jsx';
import { Async } from '../../components/AsyncState.jsx';
import { api } from '../../api/index.js';
import { useApi } from '../../hooks/useApi.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { formatDate, locationLine } from '../../lib/format.js';
import { mediaUrl } from '../../api/client.js';

const projectHref = (projectId) => `/app/cleanups/${projectId}`;

export default function ReportDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const reportState = useApi(() => api.reports.get(id), [id, user?.id]);

  return (
    <Async state={reportState}>
      {(report) => {
        const isReporter = user?.id === report.reporter?.id;
        // Once the government officer approves, the report lives on only as its project.
        if (report.projectId) return <Navigate to={projectHref(report.projectId)} replace />;

        return (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <IconButton icon="chevron-left" label="Back" onClick={() => navigate(-1)} />
              <span style={{ font: '600 13px/1.5 var(--font-mono)', color: 'var(--text-muted)' }}>{report.reference}</span>
              <ReportStatusBadge status={report.status} size="sm" style={{ marginLeft: 'auto' }} />
            </div>

            <EvidenceGallery report={report} />

            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>{report.title}</h1>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>
                <Icon name="map-pin" size="sm" />
                {locationLine(report)}
              </div>
              <p style={{ font: 'var(--text-body)', color: 'var(--text-body-color)', marginTop: 6 }}>{report.description}</p>
              <div style={{ alignSelf: 'flex-start', marginTop: 4 }}>
                <MapLink latitude={report.latitude} longitude={report.longitude} />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 6 }}>
                <UserLink user={report.reporter}><Avatar name={report.reporter?.fullName || ''} src={mediaUrl(report.reporter?.avatarUrl)} size="sm" /></UserLink>
                <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
                  {isReporter ? 'Reported by you' : <>Reported by <UserLink user={report.reporter} style={{ color: 'var(--text-link)', fontWeight: 600 }} /></>} · {formatDate(report.createdAt)}
                </span>
              </div>
            </div>
          </div>
        );
      }}
    </Async>
  );
}
