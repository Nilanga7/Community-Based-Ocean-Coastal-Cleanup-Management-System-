import React from 'react';
import { useLocation } from 'react-router-dom';
import { api } from '../api/index.js';
import { useAuth } from '../auth/AuthContext.jsx';

export const ALERTS_CHANGED = 'tideline:alerts-changed';

/** Unread alert count for the signed-in user; refreshed on navigation and when alerts are read. */
export function useUnreadAlerts() {
  const { user } = useAuth();
  const { pathname } = useLocation();
  const [count, setCount] = React.useState(0);
  const [nonce, setNonce] = React.useState(0);

  React.useEffect(() => {
    const bump = () => setNonce((n) => n + 1);
    window.addEventListener(ALERTS_CHANGED, bump);
    return () => window.removeEventListener(ALERTS_CHANGED, bump);
  }, []);

  React.useEffect(() => {
    if (!user) return undefined;
    let cancelled = false;
    api.alerts
      .unreadCount()
      .then((result) => {
        if (!cancelled) setCount(result.count);
      })
      .catch(() => {
        /* the badge is a convenience; a failed count just leaves it as it was */
      });
    return () => {
      cancelled = true;
    };
  }, [user, pathname, nonce]);

  return user ? count : 0;
}
