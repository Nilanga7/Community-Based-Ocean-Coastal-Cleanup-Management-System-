import React from 'react';
import AlertList from '../../components/AlertList.jsx';
import { consoleAlertLink } from '../../lib/alertLinks.js';

export default function ConsoleAlerts() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)', maxWidth: 760 }}>
      <div>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Alerts</h1>
        <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)', marginTop: 2 }}>
          Reports waiting for a decision, review outcomes, and new accounts to verify.
        </p>
      </div>
      <AlertList linkFor={consoleAlertLink} empty="Nothing needs your attention right now." />
    </div>
  );
}
