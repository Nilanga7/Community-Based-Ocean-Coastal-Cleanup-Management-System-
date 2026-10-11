import React from 'react';
import { Badge, ProgressBar } from '../design-system';

/** Trust percentage against the threshold; `children` holds the voting controls where they apply. */
export default function CommunityVerificationCard({ report, children }) {
  const total = report.confirmVotes + report.disputeVotes;
  const needed = report.minimumConfirmations ?? 8;
  const passed = report.trustPercentage >= report.thresholdPercent && report.confirmVotes >= needed;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)', padding: 'var(--space-5)', borderRadius: 'var(--radius-lg)', background: 'var(--surface-card)', border: '1px solid var(--border-subtle)' }}>
      <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>Community verification</span>
      <ProgressBar value={report.trustPercentage} tone={passed ? 'verified' : 'warning'} />
      <Badge tone={passed ? 'success' : 'warning'} style={{ whiteSpace: 'normal', height: 'auto', padding: '6px 10px', alignSelf: 'flex-start' }}>
        {total === 0
          ? `No votes yet — needs ${needed} confirmations and ${report.thresholdPercent}% trust`
          : `${report.confirmVotes} of ${needed} confirmations · ${report.trustPercentage}% trust (${report.thresholdPercent}% needed)`}
      </Badge>
      {children}
    </div>
  );
}
