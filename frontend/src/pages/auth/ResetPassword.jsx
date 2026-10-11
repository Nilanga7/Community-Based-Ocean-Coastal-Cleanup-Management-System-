import React from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Alert, Button, Card, Icon } from '../../design-system';
import PasswordInput from '../../components/PasswordInput.jsx';
import { api } from '../../api/index.js';

export default function ResetPassword() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const token = params.get('token') || '';

  const [password, setPassword] = React.useState('');
  const [confirm, setConfirm] = React.useState('');
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [done, setDone] = React.useState(false);

  const mismatch = confirm.length > 0 && password !== confirm;
  const tooShort = password.length > 0 && password.length < 8;

  async function submit(event) {
    event.preventDefault();
    if (password.length < 8 || password !== confirm) return;
    setBusy(true);
    setError(null);
    try {
      await api.auth.resetPassword(token, password);
      setDone(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div style={{ minHeight: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--surface-page-warm)', padding: 'var(--space-6)' }}>
      <div style={{ width: '100%', maxWidth: 400 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, justifyContent: 'center', marginBottom: 'var(--space-8)' }}>
          <Icon name="waves-horizontal" size="lg" color="var(--tide-600)" />
          <span style={{ font: '700 20px/1 var(--font-display)', letterSpacing: '-0.03em', color: 'var(--text-strong)' }}>Tideline</span>
        </div>

        <Card padding="lg">
          {done ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Password changed</h1>
              <Alert tone="success" title="All set">Sign in with your new password.</Alert>
              <Button fullWidth size="lg" onClick={() => navigate('/login', { replace: true })}>Sign in</Button>
            </div>
          ) : !token ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Reset password</h1>
              <Alert tone="danger" title="This link is incomplete">Open the link from your email again, or ask for a new one.</Alert>
              <Button variant="secondary" fullWidth onClick={() => navigate('/forgot-password')}>Get a new link</Button>
            </div>
          ) : (
            <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Choose a new password</h1>
              {error ? (
                <Alert tone="danger" title="Couldn't change the password">
                  {error} <Link to="/forgot-password">Get a new link</Link>
                </Alert>
              ) : null}
              <PasswordInput
                label="New password"
                required
                autoComplete="new-password"
                error={tooShort ? 'Use at least 8 characters.' : undefined}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <PasswordInput
                label="Confirm new password"
                required
                autoComplete="new-password"
                error={mismatch ? "The passwords don't match." : undefined}
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
              />
              <Button type="submit" fullWidth size="lg" loading={busy} disabled={busy || password.length < 8 || password !== confirm}>
                {busy ? 'Saving…' : 'Change password'}
              </Button>
              <p style={{ font: 'var(--text-caption)', color: 'var(--text-muted)', textAlign: 'center' }}>
                <Link to="/login">Back to sign in</Link>
              </p>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}
