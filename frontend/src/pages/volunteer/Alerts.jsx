import React from 'react';
import AlertList from '../../components/AlertList.jsx';
import { useAuth } from '../../auth/AuthContext.jsx';
import { consoleAlertLink, isConsoleRole, volunteerAlertLink } from '../../lib/alertLinks.js';

export default function Alerts() {
  const { user } = useAuth();
  // Administrators and officers can open the volunteer app too; their alerts are about console work.
  const linkFor = isConsoleRole(user) ? consoleAlertLink : volunteerAlertLink;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      <div>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Alerts</h1>
        <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)', marginTop: 2 }}>
          {isConsoleRole(user)
            ? 'Accounts to verify, reports waiting for a decision, and review outcomes.'
            : 'Nearby cleanups, your account and report reviews, and opportunities.'}
        </p>
      </div>
      <AlertList linkFor={linkFor} empty="No alerts yet — you'll hear from us when a cleanup is planned near you." />
    </div>
  );
}
