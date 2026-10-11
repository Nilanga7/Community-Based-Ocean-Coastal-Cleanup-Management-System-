import React from 'react';
import { Alert, Avatar, Button, IconButton } from '../design-system';
import Modal from './Modal.jsx';
import { api } from '../api/index.js';
import { mediaUrl } from '../api/client.js';

const ACCEPT = 'image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp';
const MAX_BYTES = 5 * 1024 * 1024;

/** Your own profile picture, with add, change and remove. */
export default function AvatarEditor({ user, name, role, onChange }) {
  const inputRef = React.useRef(null);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [confirmRemove, setConfirmRemove] = React.useState(false);
  const hasPhoto = Boolean(user.avatarUrl);

  async function upload(file) {
    setError(null);
    if (!/^image\/(jpeg|png|webp)$/.test(file.type) && !/\.(jpe?g|png|webp)$/i.test(file.name)) {
      setError('Choose a JPG, PNG or WebP image.');
      return;
    }
    if (file.size > MAX_BYTES) {
      setError('That photo is larger than 5 MB.');
      return;
    }
    setBusy(true);
    try {
      onChange(await api.users.updateAvatar(file));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function remove() {
    setBusy(true);
    setError(null);
    try {
      onChange(await api.users.removeAvatar());
      setConfirmRemove(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6 }}>
      <div style={{ position: 'relative', opacity: busy ? 0.6 : 1 }}>
        <Avatar name={name} src={mediaUrl(user.avatarUrl)} role={role} size={88} />
        <IconButton
          icon="camera"
          size="sm"
          variant="primary"
          label={hasPhoto ? 'Change profile photo' : 'Add profile photo'}
          disabled={busy}
          onClick={() => inputRef.current?.click()}
          style={{ position: 'absolute', left: -4, bottom: -4, borderRadius: '50%' }}
        />
      </div>
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPT}
        style={{ display: 'none' }}
        onChange={(e) => {
          const file = e.target.files?.[0];
          e.target.value = '';
          if (file) upload(file);
        }}
      />
      <div style={{ display: 'flex', gap: 4 }}>
        <Button size="sm" variant="ghost" disabled={busy} onClick={() => inputRef.current?.click()}>
          {hasPhoto ? 'Change' : 'Add photo'}
        </Button>
        {hasPhoto ? (
          <Button size="sm" variant="ghost" disabled={busy} onClick={() => setConfirmRemove(true)}>
            Remove
          </Button>
        ) : null}
      </div>
      {error ? <Alert tone="danger" title="Photo not saved" onDismiss={() => setError(null)}>{error}</Alert> : null}

      <Modal
        open={confirmRemove}
        title="Remove your profile photo?"
        onClose={() => setConfirmRemove(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setConfirmRemove(false)}>Cancel</Button>
            <Button variant="danger" disabled={busy} onClick={remove}>Remove photo</Button>
          </>
        }
      />
    </div>
  );
}
