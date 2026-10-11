import React from 'react';
import { Avatar, Badge, Icon } from '../design-system';
import ProtectedImage from './ProtectedImage.jsx';
import { formatDate, plural } from '../lib/format.js';
import { mediaUrl } from '../api/client.js';

const STATUS = {
  OPEN: { label: 'Waiting for reporter', tone: 'warning' },
  ANSWERED: { label: 'Answered', tone: 'success' },
};

const roleName = (user) => (user?.role === 'AUTHORITY' ? 'Government officer' : 'Administrator');

/** Reviewers' requests for more information and the reporter's answers, newest first. */
export default function InfoRequestsPanel({ requests, reporterName }) {
  if (!requests.length) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, padding: 'var(--space-8) var(--space-4)', textAlign: 'center' }}>
        <Icon name="message-square" size="lg" color="var(--text-muted)" />
        <span style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)' }}>
          No additional information yet. Use “Request more info” to ask the reporter for details or photos.
        </span>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      {requests.map((request) => {
        const status = STATUS[request.status];
        return (
          <div
            key={request.id}
            style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)', padding: 'var(--space-5)', borderRadius: 'var(--radius-lg)', background: 'var(--surface-card)', border: '1px solid var(--border-subtle)' }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: 8, font: 'var(--text-caption)', color: 'var(--text-muted)' }}>
                <Avatar name={request.requestedBy?.fullName || ''} src={mediaUrl(request.requestedBy?.avatarUrl)} size="xs" role="authority" />
                {roleName(request.requestedBy)} · {request.requestedBy?.fullName} · {formatDate(request.createdAt)}
              </span>
              <Badge tone={status.tone} size="sm">{status.label}</Badge>
            </div>

            <div>
              <span style={{ font: 'var(--text-micro)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 'var(--tracking-micro)' }}>Requested</span>
              <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-body-color)', paddingLeft: 10, borderLeft: '2px solid var(--border-default)', marginTop: 4 }}>
                {request.message}
              </p>
            </div>

            {request.status === 'ANSWERED' ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', paddingTop: 'var(--space-3)', borderTop: '1px solid var(--border-subtle)' }}>
                <span style={{ font: 'var(--text-micro)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 'var(--tracking-micro)' }}>
                  {reporterName}'s answer · {formatDate(request.respondedAt)}
                </span>
                <p style={{ font: 'var(--text-body)', color: 'var(--text-heading)', whiteSpace: 'pre-wrap' }}>{request.responseText}</p>
                {request.attachments.length ? (
                  <>
                    <span style={{ font: 'var(--text-caption)', color: 'var(--text-muted)' }}>{plural(request.attachments.length, 'photo')}</span>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))', gap: 8 }}>
                      {request.attachments.map((attachment) => (
                        <ProtectedImage key={attachment.url} url={attachment.url} name={attachment.name} />
                      ))}
                    </div>
                  </>
                ) : null}
              </div>
            ) : null}
          </div>
        );
      })}
    </div>
  );
}
