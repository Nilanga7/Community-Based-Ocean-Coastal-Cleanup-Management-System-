import React from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { Alert, Button, Icon, IconButton, Textarea } from '../../design-system';
import EvidencePicker from '../../components/EvidencePicker.jsx';
import { Async } from '../../components/AsyncState.jsx';
import { api } from '../../api/index.js';
import { useApi } from '../../hooks/useApi.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { ALERTS_CHANGED } from '../../hooks/useUnreadAlerts.js';
import { formatDate } from '../../lib/format.js';

const who = (user) => (user?.role === 'AUTHORITY' ? 'The government officer' : 'An administrator');

/** Where the reporter answers a reviewer's request for more information. */
export default function MoreInfo() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const state = useApi(() => Promise.all([api.reports.get(id), api.reports.infoRequests(id)]), [id]);

  const [description, setDescription] = React.useState('');
  const [photos, setPhotos] = React.useState([]);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [sent, setSent] = React.useState(false);

  async function submit(request) {
    setBusy(true);
    setError(null);
    try {
      await api.reports.respondInfo(id, request.id, description, photos);
      window.dispatchEvent(new Event(ALERTS_CHANGED));
      setSent(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (sent) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-4)', textAlign: 'center', padding: 'var(--space-16) var(--space-4)' }}>
        <span style={{ width: 64, height: 64, borderRadius: '50%', background: 'var(--status-verified-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="badge-check" size="xl" color="var(--status-verified)" />
        </span>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Information sent</h1>
        <p style={{ font: 'var(--text-body)', color: 'var(--text-body-color)', maxWidth: 360 }}>
          Thanks — it's attached to your report for the reviewers. You'll get an alert when they decide.
        </p>
        <Button onClick={() => navigate(`/app/report/${id}`)}>Back to your report</Button>
      </div>
    );
  }

  return (
    <Async state={state}>
      {([report, requests]) => {
        if (user?.id !== report.reporter?.id) return <Navigate to={`/app/report/${id}`} replace />;
        const open = requests.find((r) => r.status === 'OPEN');

        return (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <IconButton icon="chevron-left" label="Back" onClick={() => navigate(`/app/report/${id}`)} />
              <div>
                <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Additional information</h1>
                <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{report.reference} · {report.title}</span>
              </div>
            </div>

            {!open ? (
              <Alert tone="success" title="Nothing to answer">
                There's no open request for more information on this report.
              </Alert>
            ) : (
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  submit(open);
                }}
                style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}
              >
                <Alert tone="danger" title={`${who(open.requestedBy)} needs more information`}>
                  <span style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <span style={{ whiteSpace: 'pre-wrap' }}>{open.message}</span>
                    <span style={{ font: 'var(--text-caption)', opacity: 0.8 }}>Requested {formatDate(open.createdAt)}</span>
                  </span>
                </Alert>

                {error ? <Alert tone="danger" title="Couldn't send">{error}</Alert> : null}

                <Textarea
                  label="Description"
                  required
                  rows={5}
                  maxLength={2000}
                  placeholder="Answer the request — what you saw, where exactly, how much, anything that changed."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />

                <EvidencePicker
                  label="Photos"
                  photosOnly
                  files={photos}
                  onChange={setPhotos}
                />

                <Button type="submit" size="lg" fullWidth iconLeft="upload" loading={busy} disabled={busy || !description.trim()}>
                  {busy ? 'Sending…' : 'Send information'}
                </Button>
              </form>
            )}
          </div>
        );
      }}
    </Async>
  );
}
