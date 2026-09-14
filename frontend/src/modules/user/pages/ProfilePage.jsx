import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../../shared/auth/useAuth.js'
import { ApiError } from '../../../shared/api/apiClient.js'
import { getProfile, updateProfile } from '../api/userApi.js'

const EDITABLE_FIELDS = ['phone', 'dateOfBirth', 'addressLine', 'latitude', 'longitude']

function toEditForm(profile) {
  return {
    phone: profile.phone ?? '',
    dateOfBirth: profile.dateOfBirth ?? '',
    addressLine: profile.addressLine ?? '',
    latitude: profile.latitude ?? '',
    longitude: profile.longitude ?? '',
  }
}

// null/'' stay "unset" instead of being sent as the literal string 'null' or '' — UpdateProfileRequest
// treats a present-but-null field as "leave as-is", so omitting untouched blanks avoids accidentally
// clobbering a value that was never edited (see UpdateProfileRequest.java's own doc comment).
function toUpdatePayload(form) {
  const payload = {}
  for (const field of EDITABLE_FIELDS) {
    const value = form[field]
    payload[field] = value === '' ? null : value
  }
  return payload
}

export function ProfilePage() {
  const { auth } = useAuth()
  const [profile, setProfile] = useState(null)
  const [loadError, setLoadError] = useState('')
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState(null)
  const [saveError, setSaveError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let cancelled = false

    getProfile(auth.userId, auth.token)
      .then((data) => {
        if (!cancelled) setProfile(data)
      })
      .catch((error) => {
        if (!cancelled) {
          setLoadError(error instanceof ApiError ? error.message : 'Could not load your profile.')
        }
      })

    return () => {
      cancelled = true
    }
  }, [auth.userId, auth.token])

  function startEditing() {
    setForm(toEditForm(profile))
    setSaveError('')
    setEditing(true)
  }

  function updateField(field) {
    return (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }))
  }

  async function handleSave(event) {
    event.preventDefault()
    setSaving(true)
    setSaveError('')
    try {
      const updated = await updateProfile(auth.userId, auth.token, toUpdatePayload(form))
      setProfile(updated)
      setEditing(false)
    } catch (error) {
      setSaveError(error instanceof ApiError ? error.message : 'Could not save your profile.')
    } finally {
      setSaving(false)
    }
  }

  if (loadError) {
    return <p role="alert">{loadError}</p>
  }

  if (!profile) {
    return <p>Loading profile…</p>
  }

  return (
    <section>
      <h1>My profile</h1>

      {!editing ? (
        <>
          <dl>
            <dt>Name</dt>
            <dd>
              {profile.firstName} {profile.lastName}
            </dd>
            <dt>Email</dt>
            <dd>{profile.email}</dd>
            <dt>Role</dt>
            <dd>{profile.role}</dd>
            <dt>Phone</dt>
            <dd>{profile.phone || '—'}</dd>
            <dt>Address</dt>
            <dd>{profile.addressLine || '—'}</dd>
          </dl>
          <button type="button" onClick={startEditing}>
            Edit profile
          </button>
          {(profile.role === 'VOLUNTEER_DIVER' || profile.role === 'ORGANIZATION') && (
            <p>
              <Link to="/profile/setup">
                {profile.role === 'VOLUNTEER_DIVER' ? 'Manage diver equipment & documents' : 'Upload verification documents'}
              </Link>
            </p>
          )}
        </>
      ) : (
        <form onSubmit={handleSave} noValidate>
          {saveError && (
            <p role="alert" className="form-error">
              {saveError}
            </p>
          )}

          <label htmlFor="phone">Phone</label>
          <input id="phone" value={form.phone} onChange={updateField('phone')} />

          <label htmlFor="dateOfBirth">Date of birth</label>
          <input id="dateOfBirth" type="date" value={form.dateOfBirth} onChange={updateField('dateOfBirth')} />

          <label htmlFor="addressLine">Address</label>
          <input id="addressLine" value={form.addressLine} onChange={updateField('addressLine')} />

          <label htmlFor="latitude">Latitude</label>
          <input id="latitude" value={form.latitude} onChange={updateField('latitude')} />

          <label htmlFor="longitude">Longitude</label>
          <input id="longitude" value={form.longitude} onChange={updateField('longitude')} />

          <button type="submit" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </button>
          <button type="button" onClick={() => setEditing(false)} disabled={saving}>
            Cancel
          </button>
        </form>
      )}
    </section>
  )
}
