import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Icon } from '../design-system';
import { EvidenceCover } from './EvidenceGallery.jsx';
import ReportStatusBadge from './ReportStatusBadge.jsx';
import { SEVERITY_LABEL, SEVERITY_VAR, locationLine } from '../lib/format.js';

export default function ReportCard({ report, basePath = '/app/report' }) {
  const navigate = useNavigate();
  return (
    <Card padding="none" interactive onClick={() => navigate(`${basePath}/${report.id}`)} style={{ cursor: 'pointer', overflow: 'hidden' }}>
      <EvidenceCover report={report} />
      <div style={{ padding: 'var(--space-4)', display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 }}>
          <span style={{ font: '600 13px/1.5 var(--font-mono)', color: 'var(--text-muted)' }}>{report.reference}</span>
          <ReportStatusBadge status={report.status} size="sm" />
        </div>
        <h3 style={{ font: 'var(--text-label)', fontSize: 15, color: 'var(--text-strong)' }}>{report.title}</h3>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
          <Icon name="map-pin" size="xs" />
          {locationLine(report)}
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 4 }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: 6, font: 'var(--text-caption)', color: 'var(--text-body-color)' }}>
            <Icon name="triangle-alert" size="xs" color={`var(${SEVERITY_VAR[report.severity]})`} />
            {SEVERITY_LABEL[report.severity]} severity
          </span>
          <span style={{ font: '600 13px/1 var(--font-mono)', color: 'var(--text-strong)' }}>{report.trustPercentage}% trust</span>
        </div>
      </div>
    </Card>
  );
}
