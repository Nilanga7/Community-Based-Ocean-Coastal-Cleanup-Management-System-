import React from 'react';
import { Icon } from './Icon.jsx';

/* The report lifecycle vocabulary from the SRS: a report is submitted (pending),
   put to the community (verifying), passes the 75% trust threshold (verified),
   sent to a government authority (escalated), closed by an authority (rejected),
   or finished by a cleanup project (cleaned). */
const STATES = {
  pending: { label: 'Pending', color: 'var(--status-pending)', background: 'var(--status-pending-bg)', icon: 'clock' },
  verifying: { label: 'Verifying', color: 'var(--status-verifying)', background: 'var(--status-verifying-bg)', icon: 'users' },
  verified: { label: 'Verified', color: 'var(--status-verified)', background: 'var(--status-verified-bg)', icon: 'badge-check' },
  escalated: { label: 'Escalated', color: 'var(--status-escalated)', background: 'var(--status-escalated-bg)', icon: 'shield-check' },
  rejected: { label: 'Rejected', color: 'var(--status-rejected)', background: 'var(--status-rejected-bg)', icon: 'x' },
  cleaned: { label: 'Cleaned', color: 'var(--tide-800)', background: 'var(--tide-100)', icon: 'check-check' },
};

export function StatusBadge({ status = 'pending', label, size = 'md', showIcon = true, style, ...rest }) {
  const s = STATES[status] || STATES.pending;
  const sm = size === 'sm';
  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: sm ? 4 : 6,
        height: sm ? 22 : 26,
        padding: sm ? '0 8px' : '0 10px',
        borderRadius: 'var(--radius-sm)',
        font: sm ? '600 11px/1 var(--font-body)' : '600 12px/1 var(--font-body)',
        letterSpacing: 'var(--tracking-micro)',
        textTransform: 'uppercase',
        whiteSpace: 'nowrap',
        color: s.color,
        background: s.background,
        ...style,
      }}
      {...rest}
    >
      {showIcon ? <Icon name={s.icon} size={sm ? 12 : 14} /> : null}
      {label || s.label}
    </span>
  );
}
