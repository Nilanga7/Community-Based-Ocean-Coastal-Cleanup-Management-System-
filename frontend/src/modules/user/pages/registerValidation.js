// Client-side mirror of RegisterRequest's Bean Validation + AuthService.validateRoleDetails on
// the backend (see backend/.../user/dto/RegisterRequest.java and AuthService.java). Duplicated
// deliberately, not derived from a shared schema: catching obviously-invalid input before a round
// trip is a UX nicety, not the source of truth — the backend re-validates everything regardless.
export function validateRegisterForm(form) {
  const errors = {}

  if (!form.firstName.trim()) {
    errors.firstName = 'First name is required'
  }
  if (!form.lastName.trim()) {
    errors.lastName = 'Last name is required'
  }
  if (!form.email.trim()) {
    errors.email = 'Email is required'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Enter a valid email address'
  }
  if (!form.password) {
    errors.password = 'Password is required'
  } else if (form.password.length < 8) {
    errors.password = 'Password must be at least 8 characters'
  }
  if (!form.role) {
    errors.role = 'Role is required'
  }
  if (form.role === 'ORGANIZATION' && !form.organizationName.trim()) {
    errors.organizationName = 'Organization name is required for organization accounts'
  }

  return errors
}
