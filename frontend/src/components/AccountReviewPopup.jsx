import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button } from '../design-system';
import Modal from './Modal.jsx';
import { api } from '../api/index.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { ALERTS_CHANGED } from '../hooks/useUnreadAlerts.js';

/**
 * Pops up the administrator's decision on the signed-in person's own registration the first time
 * they sign in after it, and, for administrators, a registration waiting for their approval.
 * The same message stays in their alerts; closing the pop-up marks it read.
 */
export default function AccountReviewPopup() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [alert, setAlert] = React.useState(null);
  const userId = user?.id;
  const needsReview = ['DIVER', 'ORGANIZATION', 'ADMIN', 'AUTHORITY'].includes(user?.role);

  React.useEffect(() => {
    if (!userId || !needsReview) return undefined;
    let cancelled = false;
    api.alerts
      .list()
      .then((alerts) => {
        const unread = alerts.filter((a) => !a.read);
        const next = unread.find((a) => a.type === 'ACCOUNT_REVIEW')
          || (user?.role === 'ADMIN' ? unread.find((a) => a.type === 'ACCOUNT_APPLICATION') : null);
        if (!cancelled && next) setAlert(next);
      })
      .catch(() => {
        /* the pop-up is a courtesy; the alert is still in the alert box */
      });
    return () => {
      cancelled = true;
    };
  }, [userId, needsReview, user?.role]);

  async function close(target) {
    const current = alert;
    setAlert(null);
    try {
      await api.alerts.markRead(current.id);
      window.dispatchEvent(new Event(ALERTS_CHANGED));
    } catch {
      /* marking read is best-effort */
    }
    if (target) navigate(target);
  }

  const verified = user?.accountStatus === 'APPROVED';

  if (alert?.type === 'ACCOUNT_APPLICATION') {
    return (
      <Modal
        open
        title={alert.title}
        onClose={() => close()}
        footer={
          <>
            <Button variant="secondary" onClick={() => close()}>Later</Button>
            <Button iconLeft="badge-check" onClick={() => close('/console/verifications')}>Review application</Button>
          </>
        }
      >
        <Alert tone="warning" title="Approve registration">
          {alert.body}
        </Alert>
      </Modal>
    );
  }

  return (
    <Modal
      open={Boolean(alert)}
      title={alert?.title}
      onClose={() => close()}
      footer={
        <>
          <Button variant="secondary" onClick={() => close('/app/alerts')}>View alerts</Button>
          <Button
            onClick={() => close(!verified ? null
              : user?.role === 'ORGANIZATION' ? '/app/opportunities'
                : user?.role === 'ADMIN' || user?.role === 'AUTHORITY' ? '/console' : null)}
          >
            {verified ? 'Get started' : 'Got it'}
          </Button>
        </>
      }
    >
      <Alert tone={verified ? 'success' : 'danger'} title={verified ? 'Registration approved' : 'Registration not approved'}>
        {alert?.body}
      </Alert>
    </Modal>
  );
}
