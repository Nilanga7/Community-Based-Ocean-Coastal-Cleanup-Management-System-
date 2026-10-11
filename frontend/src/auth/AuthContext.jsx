import React from 'react';
import { api } from '../api/index.js';
import { clearToken, getToken, setToken } from '../api/client.js';

const AuthContext = React.createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = React.useState(null);
  // Only a stored token means there is a session to restore before rendering routes.
  const [ready, setReady] = React.useState(() => !getToken());

  React.useEffect(() => {
    if (!getToken()) {
      return;
    }
    api.users
      .me()
      .then(setUser)
      .catch(() => clearToken())
      .finally(() => setReady(true));
  }, []);

  const value = React.useMemo(
    () => ({
      user,
      ready,
      isAuthenticated: Boolean(user),
      async login(email, password, remember = false) {
        const result = await api.auth.login(email, password, remember);
        setToken(result.token, remember);
        setUser(result.user);
        return result.user;
      },
      /** Divers and organisations get no token — they wait for an administrator to verify them. */
      async register(data, certificates, licences) {
        const result = await api.auth.register(data, certificates, licences);
        if (result.token) {
          setToken(result.token);
          setUser(result.user);
        }
        return result;
      },
      logout() {
        clearToken();
        setUser(null);
      },
      setUser,
    }),
    [user, ready],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = React.useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
