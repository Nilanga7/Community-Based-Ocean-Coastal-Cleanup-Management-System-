import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen } from '@testing-library/react'
import { renderWithProviders } from '../../test-utils.jsx'
import App from '../../App.jsx'
import * as userApi from '../../modules/user/api/userApi.js'

vi.mock('../../modules/user/api/userApi.js')

describe('ProtectedRoute (via /profile)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('redirects an unauthenticated visitor away from the profile page to /login', () => {
    renderWithProviders(<App />, { route: '/profile' })

    expect(screen.getByRole('heading', { name: /log in/i })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: /my profile/i })).not.toBeInTheDocument()
    expect(userApi.getProfile).not.toHaveBeenCalled()
  })

  it('lets an authenticated visitor reach the profile page', async () => {
    localStorage.setItem(
      'cocms.auth',
      JSON.stringify({ token: 'fake-jwt-token', expiresIn: 86_400_000, userId: 42, role: 'VOLUNTEER_NON_DIVER' }),
    )
    userApi.getProfile.mockResolvedValue({
      userId: 42,
      firstName: 'Grace',
      lastName: 'Hopper',
      email: 'grace@example.com',
      phone: null,
      role: 'VOLUNTEER_NON_DIVER',
      status: 'ACTIVE',
      addressLine: null,
    })

    renderWithProviders(<App />, { route: '/profile' })

    expect(await screen.findByRole('heading', { name: /my profile/i })).toBeInTheDocument()
  })
})
