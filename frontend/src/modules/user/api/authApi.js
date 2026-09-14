import { apiRequest } from '../../../shared/api/apiClient.js'

// Both endpoints are permitAll on the backend (see SecurityConfig) — no token to attach here.

export function register(payload) {
  return apiRequest('/auth/register', { method: 'POST', body: payload })
}

export function login(payload) {
  return apiRequest('/auth/login', { method: 'POST', body: payload })
}
