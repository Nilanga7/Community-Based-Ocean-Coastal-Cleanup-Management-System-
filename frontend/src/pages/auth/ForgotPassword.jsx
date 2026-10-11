import React from 'react';
import { Link } from 'react-router-dom';
import { Alert, Button, Card, Icon, Input } from '../../design-system';
import { api } from '../../api/index.js';

export default function ForgotPassword() {
  const [email, setEmail] = React.useState('');
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [sent, setSent] = React.useState(false);

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.auth.forgotPassword(email.trim());
      setSent(true);
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
          {sent ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Check your email</h1>
              <Alert tone="success" title="Reset link sent">
                If an account uses {email.trim()}, we've sent a link to reset the password. It expires in 30 minutes.
              </Alert>
              <Button variant="secondary" fullWidth onClick={() => setSent(false)}>Use a different email</Button>
              <p style={{ font: 'var(--text-caption)', color: 'var(--text-muted)', textAlign: 'center' }}>
                <Link to="/login">Back to sign in</Link>
              </p>
            </div>
          ) : (
            <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <div>
                <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Forgot password</h1>
                <p style={{ font: 'var(--text-body-sm)', color: 'var(--text-muted)', marginTop: 4 }}>
                  Enter your account email and we'll send you a link to choose a new password.
                </p>
              </div>
              {error ? <Alert tone="danger" title="Couldn't send the link">{error}</Alert> : null}
              <Input
                label="Email"
                type="email"
                placeholder="you@example.lk"
                iconLeft="user"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              <Button type="submit" fullWidth size="lg" loading={busy} disabled={busy || !email.trim()}>
                {busy ? 'Sending…' : 'Send reset link'}
              </Button>
              <p style={{ font: 'var(--text-caption)', color: 'var(--text-muted)', textAlign: 'center' }}>
                Remembered it? <Link to="/login">Back to sign in</Link>
              </p>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}
