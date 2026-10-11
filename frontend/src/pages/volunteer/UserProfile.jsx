import React from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { IconButton } from '../../design-system';
import ProfileOverview from '../../components/ProfileOverview.jsx';
import { Async } from '../../components/AsyncState.jsx';
import { api } from '../../api/index.js';
import { useApi } from '../../hooks/useApi.js';
import { useAuth } from '../../auth/AuthContext.jsx';

/** Another member's profile. */
export default function UserProfile() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const state = useApi(() => api.users.profile(id), [id]);

  if (user && String(user.id) === id) return <Navigate to="/app/profile" replace />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <IconButton icon="chevron-left" label="Back" onClick={() => navigate(-1)} style={{ alignSelf: 'flex-start' }} />
      <Async state={state}>{(profile) => <ProfileOverview profile={profile} />}</Async>
    </div>
  );
}
