import React from 'react';
import { Icon, Alert, Button } from '../design-system';

export function Loading({ label = 'Loading…' }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, padding: 'var(--space-12) 0' }}>
      <Icon name="loader-circle" size="md" color="var(--text-muted)" />
      <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>{label}</span>
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  return (
    <Alert tone="danger" title="That didn't load">
      <span style={{ display: 'flex', flexDirection: 'column', gap: 10, alignItems: 'flex-start' }}>
        {error?.message || 'Something went wrong.'}
        {onRetry ? (
          <Button variant="secondary" size="sm" iconLeft="refresh-cw" onClick={onRetry}>
            Try again
          </Button>
        ) : null}
      </span>
    </Alert>
  );
}

export function EmptyState({ icon = 'waves-horizontal', children }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, padding: 'var(--space-12) 0', textAlign: 'center' }}>
      <Icon name={icon} size="xl" color="var(--gray-300)" />
      <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)', maxWidth: 300 }}>{children}</p>
    </div>
  );
}

/** Renders loading / error / empty for an API call, or the children when data is ready. */
export function Async({ state, empty, emptyIcon, isEmpty, children }) {
  if (state.loading) return <Loading />;
  if (state.error) return <ErrorState error={state.error} onRetry={state.reload} />;
  if (isEmpty && isEmpty(state.data)) return <EmptyState icon={emptyIcon}>{empty}</EmptyState>;
  return children(state.data);
}
