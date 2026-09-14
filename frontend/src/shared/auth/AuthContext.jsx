import { useCallback, useMemo, useState } from 'react'
import { login as loginRequest, register as registerRequest } from '../../modules/user/api/authApi'
import { AuthContext } from './authContext.js'

// Single localStorage key holding { token, expiresIn, userId, role } as one JSON blob, so the
// three fields can never desync from a partial write. See CLAUDE.md's "Open technical decisions"
// — JWT storage was decided as localStorage (not an httpOnly cookie): the backend already returns
// the token in the JSON body and JwtAuthenticationFilter only ever reads an Authorization header,
// so this needs no backend changes, at the cost of the token being readable by injected script if
// an XSS bug exists elsewhere in the app.
const STORAGE_KEY = 'cocms.auth'

function readStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    if (!parsed?.token || !parsed?.userId || !parsed?.role) return null
    return parsed
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth)

  const persist = useCallback((authResponse) => {
    const next = {
      token: authResponse.token,
      expiresIn: authResponse.expiresIn,
      userId: authResponse.userId,
      role: authResponse.role,
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
    setAuth(next)
    return next
  }, [])

  const login = useCallback(
    async (email, password) => {
      const response = await loginRequest({ email, password })
      return persist(response)
    },
    [persist],
  )

  const register = useCallback(
    async (payload) => {
      const response = await registerRequest(payload)
      return persist(response)
    },
    [persist],
  )

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setAuth(null)
  }, [])

  const value = useMemo(
    () => ({
      auth,
      isAuthenticated: auth !== null,
      login,
      register,
      logout,
    }),
    [auth, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
