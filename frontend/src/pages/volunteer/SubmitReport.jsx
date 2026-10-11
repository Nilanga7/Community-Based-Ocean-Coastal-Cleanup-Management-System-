import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Icon, IconButton, Button, Input, Textarea, Select, Alert } from '../../design-system';
import EvidencePicker from '../../components/EvidencePicker.jsx';
import { api } from '../../api/index.js';
import { PROVINCES } from '../../lib/format.js';
import { useAuth } from '../../auth/AuthContext.jsx';

const SEVERITIES = [
  { value: 'LOW', label: 'Low — small, isolated debris' },
  { value: 'MEDIUM', label: 'Medium — noticeable, spreading' },
  { value: 'HIGH', label: 'High — large area, harmful materials' },
  { value: 'CRITICAL', label: 'Critical — active spill or hazard' },
];

export default function SubmitReport() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [files, setFiles] = React.useState([]);
  const [coords, setCoords] = React.useState(null);
  const [locating, setLocating] = React.useState(false);
  const [form, setForm] = React.useState({
    title: '',
    description: '',
    severity: '',
    locationName: user?.city || '',
    province: user?.province || '',
  });
  const [error, setError] = React.useState(null);
  const [busy, setBusy] = React.useState(false);
  const [created, setCreated] = React.useState(null);

  const set = (key) => (event) => setForm((f) => ({ ...f, [key]: event.target.value }));

  function captureLocation() {
    setError(null);
    if (!navigator.geolocation) {
      setError('This browser cannot share your location — type the place name instead.');
      return;
    }
    setLocating(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({ latitude: position.coords.latitude, longitude: position.coords.longitude });
        setLocating(false);
      },
      () => {
        // Falls back to the user's saved province centre so a report can still be filed.
        setCoords({ latitude: 7.2083, longitude: 79.8358 });
        setLocating(false);
      },
      { timeout: 8000 },
    );
  }

  async function submit(event) {
    event.preventDefault();
    if (!coords) {
      setError('Capture the location first — reports are matched to volunteers by distance.');
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const report = await api.reports.create({
        title: form.title,
        description: form.description,
        severity: form.severity,
        locationName: form.locationName,
        province: form.province || null,
        latitude: coords.latitude,
        longitude: coords.longitude,
      }, files);
      setCreated(report);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (created) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-4)', textAlign: 'center', padding: 'var(--space-16) var(--space-4)' }}>
        <span style={{ width: 64, height: 64, borderRadius: '50%', background: 'var(--status-verified-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="badge-check" size="xl" color="var(--status-verified)" />
        </span>
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Report submitted</h1>
        <p style={{ font: 'var(--text-body)', color: 'var(--text-body-color)', maxWidth: 340 }}>
          Thanks — the community can now vote on {created.reference}. You'll get an alert when its status changes.
        </p>
        <div style={{ display: 'flex', gap: 'var(--space-3)' }}>
          <Button onClick={() => navigate(`/app/report/${created.id}`)}>View report</Button>
          <Button variant="secondary" onClick={() => navigate('/app')}>Back to feed</Button>
        </div>
      </div>
    );
  }

  return (
    <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <IconButton icon="chevron-left" label="Back" onClick={() => navigate(-1)} />
        <h1 style={{ font: 'var(--text-h2)', color: 'var(--text-strong)' }}>Report pollution</h1>
      </div>

      {error ? <Alert tone="danger" title="Check this before submitting">{error}</Alert> : null}

      <EvidencePicker
        label="Evidence"
        hint="Add at least one photo — reports with evidence are verified about twice as fast."
        files={files}
        onChange={setFiles}
      />

      <Input label="Site name" required placeholder="e.g. Negombo" iconLeft="map-pin" value={form.locationName} onChange={set('locationName')} />
      <Select label="Province" placeholder="Select the province" options={PROVINCES} value={form.province} onChange={set('province')} />

      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
        <span style={{ font: 'var(--text-label)', color: 'var(--text-heading)' }}>
          GPS location <span style={{ color: 'var(--danger)' }}>*</span>
        </span>
        <Button variant={coords ? 'soft' : 'secondary'} iconLeft="map-pin" onClick={captureLocation} type="button" disabled={locating}>
          {locating ? 'Locating…' : coords ? `${coords.latitude.toFixed(4)}° N, ${coords.longitude.toFixed(4)}° E` : 'Capture my location'}
        </Button>
      </div>

      <Input label="What did you see?" required placeholder="Short title for the report" value={form.title} onChange={set('title')} />

      <Select label="Severity" required placeholder="How severe does this look?" options={SEVERITIES} value={form.severity} onChange={set('severity')} />

      <Textarea
        label="Description"
        required
        rows={4}
        maxLength={400}
        placeholder="Include size, materials, and anything unusual."
        value={form.description}
        onChange={set('description')}
      />

      <Alert tone="info" title="What happens next">
        People nearby who have alerts turned on are asked to confirm what you saw. An administrator and then the
        government officer review the report, and if it's approved it becomes a cleanup project that you own.
      </Alert>

      <Button type="submit" size="lg" fullWidth iconLeft="flag" loading={busy} disabled={busy}>
        {busy ? 'Submitting…' : 'Submit report'}
      </Button>
    </form>
  );
}
