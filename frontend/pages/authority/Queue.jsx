import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Icon, Badge, Avatar } from '../../design-system';
import ChipButton from '../../components/ChipButton.jsx';
import ReportStatusBadge from '../../components/ReportStatusBadge.jsx';
import { api } from '../../api/index.js';
import { useApi } from '../../hooks/useApi.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { Async } from '../../components/AsyncState.jsx';
import { formatDate, reviewStage } from '../../lib/format.js';
import { mediaUrl } from '../../api/client.js';

const FILTERS = [
  { value: 'all', label: 'All reports' },
  { value: 'VERIFIED', label: 'Verified' },
  { value: 'ESCALATED', label: 'With government officer' },
  { value: 'REJECTED', label: 'Rejected' },
];

const COLUMNS = '90px minmax(200px, 1fr) 130px 110px 180px 70px 70px';

export default function Queue() {
  const navigate = useNavigate();
  const { user } = useAuth();
  // Officers mostly work on what administrators have sent them.
  const [filter, setFilter] = React.useState(user?.role === 'AUTHORITY' ? 'ESCALATED' : 'all');
  const state = useApi(() => api.reports.list({ status: filter, reviewQueue: true, size: 50 }), [filter]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      <div>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Review queue</h1>
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {FILTERS.map((f) => (
          <ChipButton key={f.value} selected={filter === f.value} onClick={() => setFilter(f.value)}>
            {f.label}
          </ChipButton>
        ))}
      </div>

      <Async
        state={state}
        isEmpty={(data) => !data?.content?.length}
        empty="Nothing in this queue — try another status filter."
        emptyIcon="list-filter"
      >
        {(data) => (
          <div style={{ overflowX: 'auto', background: 'var(--surface-card)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-lg)' }}>
            <div style={{ minWidth: 900 }}>
              <div style={{ display: 'grid', gridTemplateColumns: COLUMNS, gap: 8, padding: '10px var(--space-4)', background: 'var(--surface-sunken)', font: 'var(--text-micro)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 'var(--tracking-micro)' }}>
                <span>Ref</span>
                <span>Report</span>
                <span>Location</span>
                <span>Status</span>
                <span>Review</span>
                <span>Trust</span>
                <span>Reported</span>
              </div>

              {data.content.map((report) => {
                const stage = reviewStage(report);
                return (
                  <div
                    key={report.id}
                    role="link"
                    tabIndex={0}
                    onClick={() => navigate(`/console/reports/${report.id}`)}
                    onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/console/reports/${report.id}`); }}
                    style={{
                      display: 'grid',
                      gridTemplateColumns: COLUMNS,
                      gap: 8,
                      alignItems: 'center',
                      padding: 'var(--space-3) var(--space-4)',
                      borderTop: '1px solid var(--border-subtle)',
                      cursor: 'pointer',
                      font: 'var(--text-body-sm)',
                    }}
                  >
                    <span style={{ font: '600 12px/1.5 var(--font-mono)', color: 'var(--text-muted)' }}>{report.reference}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, minWidth: 0 }}>
                      <Avatar name={report.reporter?.fullName || ''} src={mediaUrl(report.reporter?.avatarUrl)} size="xs" />
                      <span style={{ color: 'var(--text-heading)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {report.title}
                      </span>
                    </div>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: 'var(--text-muted)' }}>
                      <Icon name="map-pin" size="xs" />
                      {report.locationName}
                    </span>
                    <span><ReportStatusBadge status={report.status} size="sm" /></span>
                    <span><Badge tone={stage.tone} size="sm">{stage.label}</Badge></span>
                    <Badge tone={report.trustPercentage >= report.thresholdPercent && report.confirmVotes >= report.minimumConfirmations ? 'success' : 'warning'} size="sm">
                      {report.trustPercentage}%
                    </Badge>
                    <span style={{ color: 'var(--text-muted)', font: 'var(--text-caption)' }}>
                      {formatDate(report.createdAt, false)}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </Async>
    </div>
  );
}
