import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiRequest, ApiError } from './apiClient.js'

function jsonResponse(status, body) {
  return Promise.resolve(
    new Response(body === null ? '' : JSON.stringify(body), {
      status,
      headers: { 'Content-Type': 'application/json' },
    }),
  )
}

describe('apiRequest', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('attaches the token as a Bearer Authorization header when one is supplied', async () => {
    const fetchMock = vi.fn(() => jsonResponse(200, { ok: true }))
    vi.stubGlobal('fetch', fetchMock)

    await apiRequest('/users/1/profile', { token: 'fake-jwt-token' })

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [, options] = fetchMock.mock.calls[0]
    expect(options.headers.Authorization).toBe('Bearer fake-jwt-token')
  })

  it('sends no Authorization header when no token is supplied', async () => {
    const fetchMock = vi.fn(() => jsonResponse(200, { ok: true }))
    vi.stubGlobal('fetch', fetchMock)

    await apiRequest('/auth/login', { method: 'POST', body: { email: 'a@example.com', password: 'x' } })

    const [, options] = fetchMock.mock.calls[0]
    expect(options.headers.Authorization).toBeUndefined()
  })

  it('throws ApiError built from the backend error shape on a non-2xx response', async () => {
    const fetchMock = vi.fn(() =>
      jsonResponse(403, { error: { code: 'FORBIDDEN', message: 'You do not have permission' } }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(apiRequest('/users/2/profile', { token: 'someone-elses-token' })).rejects.toMatchObject({
      name: 'ApiError',
      status: 403,
      code: 'FORBIDDEN',
      message: 'You do not have permission',
    })
  })

  it('rejected requests are instances of ApiError', async () => {
    const fetchMock = vi.fn(() => jsonResponse(401, { error: { code: 'UNAUTHORIZED', message: 'Authentication required' } }))
    vi.stubGlobal('fetch', fetchMock)

    await expect(apiRequest('/users/1/profile')).rejects.toBeInstanceOf(ApiError)
  })
})
