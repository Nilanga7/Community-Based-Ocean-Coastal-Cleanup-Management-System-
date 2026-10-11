import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { Loading } from './AsyncState.jsx';
import { Alert } from '../design-system';

export default function RequireAuth({ roles, children }) {
  const { user, ready } = useAuth();

  if (!ready) {
    return <Loading label="Checking your session…" />;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.includes(user.role)) {
    return (
      <div style={{ padding: 'var(--space-8)' }}>
        <Alert tone="warning" title="This console is for government officers and administrators">
          You are signed in as {user.fullName}. Ask an administrator if you need access.
        </Alert>
      </div>
    );
  }

  return children;
}
