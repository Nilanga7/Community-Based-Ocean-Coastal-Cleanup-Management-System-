import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './useAuth.js'

// Redirects an unauthenticated visitor to /login, remembering where they were headed (so login
// can send them back) rather than just dropping them on a fixed landing page.
export function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return children
}
