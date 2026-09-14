import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './shared/auth/ProtectedRoute.jsx'
import { RegisterPage } from './modules/user/pages/RegisterPage.jsx'
import { LoginPage } from './modules/user/pages/LoginPage.jsx'
import { ProfilePage } from './modules/user/pages/ProfilePage.jsx'
import { ProfileSetupPage } from './modules/user/pages/ProfileSetupPage.jsx'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile/setup"
        element={
          <ProtectedRoute>
            <ProfileSetupPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
