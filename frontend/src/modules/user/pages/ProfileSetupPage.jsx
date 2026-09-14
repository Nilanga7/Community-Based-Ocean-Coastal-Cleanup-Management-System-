import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../../shared/auth/useAuth.js'
import { ApiError } from '../../../shared/api/apiClient.js'
import { addDiverEquipment, listDiverEquipment, uploadVerificationDocument } from '../api/userApi.js'

const DOCUMENT_TYPES = [
  { value: 'ID_CARD', label: 'National ID card' },
  { value: 'PASSPORT', label: 'Passport' },
  { value: 'DIVE_CERTIFICATION', label: 'Diving certification' },
  { value: 'BUSINESS_REGISTRATION', label: 'Business registration' },
]

function DiverEquipmentSection({ userId, token }) {
  const [equipment, setEquipment] = useState([])
  const [loadError, setLoadError] = useState('')
  const [equipmentName, setEquipmentName] = useState('')
  const [addError, setAddError] = useState('')
  const [adding, setAdding] = useState(false)

  useEffect(() => {
    let cancelled = false
    listDiverEquipment(userId, token)
      .then((data) => {
        if (!cancelled) setEquipment(data)
      })
      .catch((error) => {
        if (!cancelled) {
          setLoadError(error instanceof ApiError ? error.message : 'Could not load your equipment.')
        }
      })
    return () => {
      cancelled = true
    }
  }, [userId, token])

  async function handleAdd(event) {
    event.preventDefault()
    if (!equipmentName.trim()) {
      setAddError('Equipment name is required')
      return
    }
    setAdding(true)
    setAddError('')
    try {
      const created = await addDiverEquipment(userId, token, { equipmentName: equipmentName.trim() })
      setEquipment((prev) => [...prev, created])
      setEquipmentName('')
    } catch (error) {
      setAddError(error instanceof ApiError ? error.message : 'Could not add equipment.')
    } finally {
      setAdding(false)
    }
  }

  return (
    <section>
      <h2>Diver equipment</h2>
      {loadError && <p role="alert">{loadError}</p>}
      <ul>
        {equipment.map((item) => (
          <li key={item.equipmentId}>{item.equipmentName}</li>
        ))}
      </ul>
      <form onSubmit={handleAdd} noValidate>
        {addError && <p role="alert">{addError}</p>}
        <label htmlFor="equipmentName">Equipment name</label>
        <input
          id="equipmentName"
          value={equipmentName}
          onChange={(event) => setEquipmentName(event.target.value)}
        />
        <button type="submit" disabled={adding}>
          {adding ? 'Adding…' : 'Add equipment'}
        </button>
      </form>
    </section>
  )
}

function VerificationDocumentSection({ userId, token }) {
  const [documentType, setDocumentType] = useState(DOCUMENT_TYPES[0].value)
  const [file, setFile] = useState(null)
  const [uploadError, setUploadError] = useState('')
  const [uploadSuccess, setUploadSuccess] = useState('')
  const [uploading, setUploading] = useState(false)

  async function handleUpload(event) {
    event.preventDefault()
    setUploadSuccess('')
    if (!file) {
      setUploadError('Choose a file to upload')
      return
    }
    setUploading(true)
    setUploadError('')
    try {
      await uploadVerificationDocument(userId, token, documentType, file)
      setUploadSuccess('Document uploaded — it will be reviewed as part of registration verification.')
      setFile(null)
      event.target.reset()
    } catch (error) {
      setUploadError(error instanceof ApiError ? error.message : 'Could not upload document.')
    } finally {
      setUploading(false)
    }
  }

  return (
    <section>
      <h2>Verification documents</h2>
      <form onSubmit={handleUpload} noValidate>
        {uploadError && <p role="alert">{uploadError}</p>}
        {uploadSuccess && <p role="status">{uploadSuccess}</p>}

        <label htmlFor="documentType">Document type</label>
        <select id="documentType" value={documentType} onChange={(event) => setDocumentType(event.target.value)}>
          {DOCUMENT_TYPES.map((type) => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>

        <label htmlFor="file">File</label>
        <input
          id="file"
          type="file"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
        />

        <button type="submit" disabled={uploading}>
          {uploading ? 'Uploading…' : 'Upload'}
        </button>
      </form>
    </section>
  )
}

export function ProfileSetupPage() {
  const { auth } = useAuth()

  return (
    <section>
      <h1>Profile setup</h1>
      <p>
        <Link to="/profile">Back to profile</Link>
      </p>

      {auth.role === 'VOLUNTEER_DIVER' && <DiverEquipmentSection userId={auth.userId} token={auth.token} />}
      <VerificationDocumentSection userId={auth.userId} token={auth.token} />
    </section>
  )
}
