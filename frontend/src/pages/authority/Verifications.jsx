import React from 'react';
import { Badge, Button, Card, Icon, Textarea, Alert, Avatar } from '../../design-system';
import ChipButton from '../../components/ChipButton.jsx';
import Modal from '../../components/Modal.jsx';
import { Async } from '../../components/AsyncState.jsx';
import { api } from '../../api/index.js';
import { useApi } from '../../hooks/useApi.js';
import {
  ACCOUNT_STATUS,
  CERTIFICATION_LABEL,
  ORGANIZATION_LABEL,
  ROLE_LABEL,
  formatBytes,
  formatDate,
  plural,
} from '../../lib/format.js';

const FILTERS = [
  { value: 'PENDING_REVIEW', label: 'Waiting for review' },
  { value: 'REJECTED', label: 'Not approved' },
  { value: 'APPROVED', label: 'Verified' },
];

const DOCUMENT_GROUPS = [
  ['CERTIFICATE', 'Certificates'],
  ['LICENCE', 'Licences'],
  ['APPOINTMENT', 'Proof of appointment'],
];

const EMPTY = {
  PENDING_REVIEW: 'No accounts are waiting for verification.',
  REJECTED: 'No applications have been turned down.',
  APPROVED: 'No volunteer divers or organisations have been verified yet.',
};

function Detail({ label, children }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
      <span style={{ font: 'var(--text-micro)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 'var(--tracking-micro)' }}>{label}</span>
      <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-heading)', overflowWrap: 'anywhere' }}>{children}</span>
    </div>
  );
}

export default function Verifications() {
  const [filter, setFilter] = React.useState('PENDING_REVIEW');
  const state = useApi(() => api.admin.verifications(filter), [filter]);

  const [target, setTarget] = React.useState(null);
  const [reason, setReason] = React.useState('');
  const [busyId, setBusyId] = React.useState(null);
  const [error, setError] = React.useState(null);
  const [notice, setNotice] = React.useState(null);

  async function review(account, approved) {
    setBusyId(account.id);
    setError(null);
    setNotice(null);
    try {
      await api.admin.reviewAccount(account.id, approved, approved ? null : reason.trim());
      setNotice(
        approved
          ? `${account.fullName} is verified and can now sign in.`
          : `${account.fullName}'s application was not approved. They'll see your reason when they try to sign in.`,
      );
      setTarget(null);
      setReason('');
      state.reload();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  }

  async function viewDocument(doc) {
    // Open the window during the click so pop-up blockers allow it, then fill it once the file arrives.
    const win = window.open('', '_blank');
    setError(null);
    try {
      const blob = await api.admin.document(doc.id);
      const url = URL.createObjectURL(blob);
      if (win) win.location.href = url;
      else window.location.assign(url);
      setTimeout(() => URL.revokeObjectURL(url), 60_000);
    } catch (err) {
      if (win) win.close();
      setError(`Could not open ${doc.name}: ${err.message}`);
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      <div>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Account verifications</h1>
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {FILTERS.map((f) => (
          <ChipButton key={f.value} selected={filter === f.value} onClick={() => setFilter(f.value)}>
            {f.label}
          </ChipButton>
        ))}
      </div>

      {notice ? <Alert tone="success" title="Done" onDismiss={() => setNotice(null)}>{notice}</Alert> : null}
      {error && !target ? <Alert tone="danger" title="That didn't work" onDismiss={() => setError(null)}>{error}</Alert> : null}

      <Async state={state} isEmpty={(list) => !list?.length} empty={EMPTY[filter]} emptyIcon="badge-check">
        {(accounts) => (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
            {accounts.map((account) => {
              const status = ACCOUNT_STATUS[account.accountStatus];
              const isDiver = account.role === 'DIVER';
              const isOfficial = account.role === 'ADMIN' || account.role === 'AUTHORITY';
              const hasDocuments = isDiver || isOfficial;
              return (
                <Card key={account.id} padding="lg">
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', gap: 'var(--space-3)', flexWrap: 'wrap' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', minWidth: 0 }}>
                        <Avatar name={account.organizationName || account.fullName} size="md" />
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
                          <span style={{ font: 'var(--text-h4)', color: 'var(--text-strong)' }}>
                            {isDiver || isOfficial ? account.fullName : account.organizationName || account.fullName}
                          </span>
                          <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
                            {isDiver || isOfficial ? account.email : `${account.fullName} · ${account.email}`}
                            {` · applied ${formatDate(account.createdAt, false)}`}
                          </span>
                        </div>
                      </div>
                      <div style={{ display: 'flex', gap: 6, alignItems: 'flex-start', flexWrap: 'wrap' }}>
                        <Badge tone="neutral" size="sm">{ROLE_LABEL[account.role] || account.role}</Badge>
                        <Badge tone={status.tone} size="sm">{status.label}</Badge>
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(170px, 1fr))', gap: 'var(--space-3)' }}>
                      {isOfficial ? (
                        <Detail label="Department or agency">{account.organizationName || 'Not given'}</Detail>
                      ) : isDiver ? (
                        <>
                          <Detail label="Certification">{CERTIFICATION_LABEL[account.certificationLevel] || 'Not given'}</Detail>
                          <Detail label="Experience">{account.experienceYears != null ? plural(account.experienceYears, 'year') : 'Not given'}</Detail>
                        </>
                      ) : (
                        <>
                          <Detail label="Organisation type">{ORGANIZATION_LABEL[account.organizationType] || 'Not given'}</Detail>
                          <Detail label="Website">
                            {account.websiteUrl ? (
                              <a
                                href={account.websiteUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                style={{ color: 'var(--text-link, var(--tide-600))', display: 'inline-flex', alignItems: 'center', gap: 4 }}
                              >
                                {account.websiteUrl.replace(/^https?:\/\//, '')}
                                <Icon name="arrow-up-right" size="xs" />
                              </a>
                            ) : (
                              'Not given'
                            )}
                          </Detail>
                        </>
                      )}
                      <Detail label="Phone">{account.phone || 'Not given'}</Detail>
                      <Detail label="Location">{[account.city, account.province].filter(Boolean).join(', ') || 'Not given'}</Detail>
                    </div>

                    {hasDocuments ? (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                        {account.documents?.length ? (
                          DOCUMENT_GROUPS.map(([kind, title]) => {
                            const group = account.documents.filter((doc) => (doc.kind || 'CERTIFICATE') === kind);
                            if (!group.length) return null;
                            return (
                              <div key={kind} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                                <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>{title}</span>
                                {group.map((doc) => (
                                  <div
                                    key={doc.id}
                                    style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-2) var(--space-3)', background: 'var(--surface-sunken)', borderRadius: 'var(--radius-md)' }}
                                  >
                                    <Icon name={doc.contentType === 'application/pdf' ? 'file-text' : 'image'} size="sm" color="var(--text-muted)" />
                                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
                                      <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-heading)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{doc.name}</span>
                                      <span style={{ font: 'var(--text-micro)', color: 'var(--text-muted)' }}>{formatBytes(doc.sizeBytes)}</span>
                                    </div>
                                    <Button size="sm" variant="secondary" iconLeft="eye" onClick={() => viewDocument(doc)}>View</Button>
                                  </div>
                                ))}
                              </div>
                            );
                          })
                        ) : (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                            <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>{isOfficial ? 'Proof of appointment' : 'Certificates and licences'}</span>
                            <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>No files were attached.</span>
                          </div>
                        )}
                      </div>
                    ) : null}

                    {account.reviewNote ? (
                      <Alert tone="danger" title="Reason given">{account.reviewNote}</Alert>
                    ) : null}

                    {account.accountStatus !== 'APPROVED' ? (
                      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                        {account.accountStatus === 'PENDING_REVIEW' ? (
                          <Button
                            variant="ghost"
                            disabled={busyId === account.id}
                            onClick={() => { setError(null); setReason(''); setTarget(account); }}
                          >
                            Reject
                          </Button>
                        ) : null}
                        <Button iconLeft="badge-check" disabled={busyId === account.id} onClick={() => review(account, true)}>
                          Approve account
                        </Button>
                      </div>
                    ) : null}
                  </div>
                </Card>
              );
            })}
          </div>
        )}
      </Async>

      <Modal
        open={Boolean(target)}
        title={`Reject ${target?.organizationName || target?.fullName}?`}
        description="They won't be able to sign in. The reason is shown to them when they try."
        onClose={() => setTarget(null)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setTarget(null)}>Cancel</Button>
            <Button variant="danger" disabled={!reason.trim() || busyId === target?.id} onClick={() => review(target, false)}>
              Reject application
            </Button>
          </>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          {error ? <Alert tone="danger" title="Could not reject">{error}</Alert> : null}
          <Textarea
            label="Reason"
            required
            rows={3}
            placeholder="e.g. The certificate is expired — please register again with a current card."
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
}
