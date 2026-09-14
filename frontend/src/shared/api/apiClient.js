// Base URL of the Spring Boot backend (see backend/src/main/resources/application.properties —
// no server.port override, so it defaults to 8080). Override per-environment via
// VITE_API_BASE_URL in a .env/.env.local file (Vite only exposes VITE_-prefixed vars to client code).
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

// Mirrors the backend's one error shape: { error: { code, message } } (see
// backend/.../common/error/ErrorResponse.java). `code` is one of ErrorCode's names
// (VALIDATION_ERROR, CONFLICT, NOT_FOUND, UNAUTHORIZED, FORBIDDEN, INTERNAL_ERROR) so callers can
// branch on it without string-matching the message.
export class ApiError extends Error {
  constructor(status, code, message) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

/**
 * Thin fetch wrapper: builds the full URL, attaches the Bearer token when one is supplied,
 * JSON-encodes the body (unless it's already FormData, for multipart uploads), and throws
 * ApiError for any non-2xx response using the backend's real error body instead of a generic
 * "request failed" message.
 */
export async function apiRequest(path, { method = 'GET', body, token, signal } = {}) {
  const headers = {}
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const isFormData = typeof FormData !== 'undefined' && body instanceof FormData
  let requestBody
  if (isFormData) {
    // Let the browser set Content-Type (including the multipart boundary) itself.
    requestBody = body
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
    requestBody = JSON.stringify(body)
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: requestBody,
    signal,
  })

  if (response.status === 204) {
    return null
  }

  const text = await response.text()
  const data = text ? JSON.parse(text) : null

  if (!response.ok) {
    const code = data?.error?.code ?? 'UNKNOWN_ERROR'
    const message = data?.error?.message ?? 'Something went wrong. Please try again.'
    throw new ApiError(response.status, code, message)
  }

  return data
}
