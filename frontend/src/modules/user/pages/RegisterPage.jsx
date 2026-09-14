import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../../shared/auth/useAuth.js'
import { ApiError } from '../../../shared/api/apiClient.js'
import { SELF_REGISTERABLE_ROLES } from '../roles.js'
import { validateRegisterForm } from './registerValidation.js'

const INITIAL_FORM = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  phone: '',
  role: SELF_REGISTERABLE_ROLES[0].value,
  availability: '',
  experience: '',
  certification: '',
  preferredRegion: '',
  organizationName: '',
  organizationType: '',
  businessRegNum: '',
}

function buildRoleDetails(form) {
  switch (form.role) {
    case 'VOLUNTEER_NON_DIVER':
      return { availability: form.availability || null }
    case 'VOLUNTEER_DIVER':
      return {
        experience: form.experience || null,
        certification: form.certification || null,
        preferredRegion: form.preferredRegion || null,
      }
    case 'ORGANIZATION':
      return {
        organizationName: form.organizationName,
        organizationType: form.organizationType || null,
        businessRegNum: form.businessRegNum || null,
      }
    default:
      return null
  }
}

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(INITIAL_FORM)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function updateField(field) {
    return (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')

    const validationErrors = validateRegisterForm(form)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) {
      return
    }

    setSubmitting(true)
    try {
      await register({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        password: form.password,
        phone: form.phone.trim() || null,
        role: form.role,
        roleDetails: buildRoleDetails(form),
      })
      navigate('/profile', { replace: true })
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : 'Registration failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h1>Register</h1>
      <form onSubmit={handleSubmit} noValidate>
        {formError && (
          <p role="alert" className="form-error">
            {formError}
          </p>
        )}

        <label htmlFor="firstName">First name</label>
        <input id="firstName" value={form.firstName} onChange={updateField('firstName')} />
        {errors.firstName && <span role="alert">{errors.firstName}</span>}

        <label htmlFor="lastName">Last name</label>
        <input id="lastName" value={form.lastName} onChange={updateField('lastName')} />
        {errors.lastName && <span role="alert">{errors.lastName}</span>}

        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={form.email} onChange={updateField('email')} />
        {errors.email && <span role="alert">{errors.email}</span>}

        <label htmlFor="password">Password</label>
        <input id="password" type="password" value={form.password} onChange={updateField('password')} />
        {errors.password && <span role="alert">{errors.password}</span>}

        <label htmlFor="phone">Phone (optional)</label>
        <input id="phone" value={form.phone} onChange={updateField('phone')} />

        <label htmlFor="role">Role</label>
        <select id="role" value={form.role} onChange={updateField('role')}>
          {SELF_REGISTERABLE_ROLES.map((role) => (
            <option key={role.value} value={role.value}>
              {role.label}
            </option>
          ))}
        </select>
        {errors.role && <span role="alert">{errors.role}</span>}

        {form.role === 'VOLUNTEER_NON_DIVER' && (
          <>
            <label htmlFor="availability">Availability (optional)</label>
            <input id="availability" value={form.availability} onChange={updateField('availability')} />
          </>
        )}

        {form.role === 'VOLUNTEER_DIVER' && (
          <>
            <label htmlFor="experience">Diving experience (optional)</label>
            <input id="experience" value={form.experience} onChange={updateField('experience')} />

            <label htmlFor="certification">Certification (optional)</label>
            <input id="certification" value={form.certification} onChange={updateField('certification')} />

            <label htmlFor="preferredRegion">Preferred region (optional)</label>
            <input id="preferredRegion" value={form.preferredRegion} onChange={updateField('preferredRegion')} />
          </>
        )}

        {form.role === 'ORGANIZATION' && (
          <>
            <label htmlFor="organizationName">Organization name</label>
            <input
              id="organizationName"
              value={form.organizationName}
              onChange={updateField('organizationName')}
            />
            {errors.organizationName && <span role="alert">{errors.organizationName}</span>}

            <label htmlFor="organizationType">Organization type (optional)</label>
            <input id="organizationType" value={form.organizationType} onChange={updateField('organizationType')} />

            <label htmlFor="businessRegNum">Business registration number (optional)</label>
            <input id="businessRegNum" value={form.businessRegNum} onChange={updateField('businessRegNum')} />
          </>
        )}

        <button type="submit" disabled={submitting}>
          {submitting ? 'Registering…' : 'Register'}
        </button>
      </form>
      <p>
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </section>
  )
}
