// Mirrors AuthService.isSelfRegisterable on the backend: only these three roles may be chosen on
// the public registration form. ADMIN and GOVERNMENT_OFFICER accounts are provisioned separately
// (POST /admin/users, an admin-only flow with no UI in this public-facing app) and must never
// appear as a selectable option here.
export const SELF_REGISTERABLE_ROLES = [
  { value: 'VOLUNTEER_NON_DIVER', label: 'Volunteer (non-diver)' },
  { value: 'VOLUNTEER_DIVER', label: 'Volunteer diver' },
  { value: 'ORGANIZATION', label: 'Organization' },
]
