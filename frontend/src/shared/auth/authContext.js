import { createContext } from 'react'

// Split into its own file (rather than living alongside AuthProvider in AuthContext.jsx) so that
// file only exports the component, keeping React Fast Refresh happy — see AuthContext.jsx.
export const AuthContext = createContext(null)
