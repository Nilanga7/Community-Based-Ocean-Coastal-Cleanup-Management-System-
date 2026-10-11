import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Icon, Avatar, Button } from '../design-system';
import { useAuth } from '../auth/AuthContext.jsx';
import { useUnreadAlerts } from '../hooks/useUnreadAlerts.js';
import AccountReviewPopup from '../components/AccountReviewPopup.jsx';
import { mediaUrl } from '../api/client.js';
import { ROLE_LABEL } from '../lib/format.js';
import './volunteer-shell.css';

const TABS = [
  { to: '/app', label: 'Feed', icon: 'map', end: true },
  { to: '/app/cleanups', label: 'Cleanups', icon: 'hand-heart' },
  { to: '/app/alerts', label: 'Alerts', icon: 'bell', showUnread: true },
  { to: '/app/opportunities', label: 'Opportunities', icon: 'anchor' },
  { to: '/app/profile', label: 'Profile', icon: 'user' },
];

function TabIcon({ tab, unread }) {
  return (
    <span className="vs-icon">
      <Icon name={tab.icon} size="md" />
      {tab.showUnread && unread ? <span className="vs-badge">{unread > 9 ? '9+' : unread}</span> : null}
    </span>
  );
}

export default function VolunteerShell() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const unread = useUnreadAlerts();
  const isOfficial = user?.role === 'ADMIN' || user?.role === 'AUTHORITY';
  const label = (tab) => (tab.showUnread && unread ? `${tab.label}, ${unread} unread` : undefined);

  const logo = (
    <button onClick={() => navigate('/')} style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', padding: '0 var(--space-2)' }}>
      <Icon name="waves-horizontal" size="md" color="var(--tide-300)" />
      <span style={{ font: '700 18px/1 var(--font-display)', letterSpacing: '-0.03em', color: 'var(--white)' }}>Tideline</span>
    </button>
  );

  return (
    <div className="vs-root">
      <aside className="vs-sidebar">
        {logo}

        <nav aria-label="Main" style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          {TABS.map((tab) => (
            <NavLink key={tab.to} to={tab.to} end={tab.end} className="vs-side-link" aria-label={label(tab)}>
              <TabIcon tab={tab} unread={unread} />
              {tab.label}
            </NavLink>
          ))}
        </nav>

        <div style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          {isOfficial ? (
            <Button variant="inverse" size="sm" iconLeft="shield-check" fullWidth onClick={() => navigate('/console')}>
              Console
            </Button>
          ) : null}
          {user ? (
            <button type="button" className="vs-user" onClick={() => navigate('/app/profile')}>
              <Avatar name={user.fullName} src={mediaUrl(user.avatarUrl)} role={user.role === 'DIVER' ? 'diver' : undefined} size="sm" />
              <span style={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
                <span style={{ font: 'var(--text-label)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user.fullName}</span>
                <span style={{ font: 'var(--text-caption)', color: 'var(--text-inverse-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {[user.city, user.province].filter(Boolean).join(' · ') || ROLE_LABEL[user.role]}
                </span>
              </span>
            </button>
          ) : (
            <Button variant="inverse" fullWidth onClick={() => navigate('/login')}>
              Sign in
            </Button>
          )}
        </div>
      </aside>

      <div className="vs-body">
        <header className="vs-topbar">
          {logo}
          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, minWidth: 0 }}>
              {isOfficial ? (
                <Button variant="inverse" size="sm" iconLeft="shield-check" onClick={() => navigate('/console')}>
                  Console
                </Button>
              ) : null}
              <button type="button" onClick={() => navigate('/app/profile')} style={{ display: 'flex', cursor: 'pointer' }} aria-label="Your profile">
                <Avatar name={user.fullName} src={mediaUrl(user.avatarUrl)} role={user.role === 'DIVER' ? 'diver' : undefined} size="sm" />
              </button>
            </div>
          ) : (
            <Button variant="inverse" size="sm" onClick={() => navigate('/login')}>
              Sign in
            </Button>
          )}
        </header>

        <main className="vs-main">
          <Outlet />
        </main>
        <AccountReviewPopup />

        <nav className="vs-tabbar" aria-label="Main">
          {TABS.map((tab) => (
            <NavLink key={tab.to} to={tab.to} end={tab.end} className="vs-tab" aria-label={label(tab)}>
              <TabIcon tab={tab} unread={unread} />
              {tab.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </div>
  );
}
