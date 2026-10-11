import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

/** A member's name (or avatar) that opens their profile — your own opens your profile page. */
export default function UserLink({ user, children, style }) {
  const { user: me } = useAuth();
  if (!user?.id) return <>{children ?? user?.fullName}</>;
  const to = me?.id === user.id ? '/app/profile' : `/app/users/${user.id}`;
  return (
    <Link to={to} onClick={(e) => e.stopPropagation()} style={{ color: 'inherit', textDecoration: 'none', ...style }}>
      {children ?? user.fullName}
    </Link>
  );
}
