import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider } from './shared/auth/AuthContext.jsx'

// Every page under test needs both routing context (useNavigate/useLocation/Link) and auth
// context (useAuth) — this wraps both the way main.jsx does for the real app, so a page rendered
// in isolation behaves the same as it would mounted under App.
export function renderWithProviders(ui, { route = '/' } = {}) {
  return render(<MemoryRouter initialEntries={[route]}>{withAuth(ui)}</MemoryRouter>)
}

function withAuth(ui) {
  return <AuthProvider>{ui}</AuthProvider>
}
