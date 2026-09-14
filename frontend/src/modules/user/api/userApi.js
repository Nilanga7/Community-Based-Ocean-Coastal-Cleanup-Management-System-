import { apiRequest } from '../../../shared/api/apiClient.js'

export function getProfile(userId, token) {
  return apiRequest(`/users/${userId}/profile`, { token })
}

export function updateProfile(userId, token, payload) {
  return apiRequest(`/users/${userId}/profile`, { method: 'PUT', token, body: payload })
}

export function listDiverEquipment(userId, token) {
  return apiRequest(`/users/${userId}/diver-equipment`, { token })
}

export function addDiverEquipment(userId, token, payload) {
  return apiRequest(`/users/${userId}/diver-equipment`, { method: 'POST', token, body: payload })
}

export function uploadVerificationDocument(userId, token, documentType, file) {
  const formData = new FormData()
  formData.append('documentType', documentType)
  formData.append('file', file)
  return apiRequest(`/users/${userId}/verification-documents`, {
    method: 'POST',
    token,
    body: formData,
  })
}
