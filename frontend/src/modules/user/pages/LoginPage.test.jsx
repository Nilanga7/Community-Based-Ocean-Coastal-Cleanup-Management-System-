import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../../test-utils.jsx'
import App from '../../../App.jsx'
import * as authApi from '../api/authApi.js'
import * as userApi from '../api/userApi.js'

vi.mock('../api/authApi.js')
vi.mock('../api/userApi.js')

const FAKE_AUTH_RESPONSE = {
  token: 'fake-jwt-token',
  expiresIn: 86_400_000,
  userId: 42,
  role: 'VOLUNTEER_NON_DIVER',
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('on success, persists the token to localStorage and navigates into the app', async () => {
    authApi.login.mockResolvedValue(FAKE_AUTH_RESPONSE)
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

    const user = userEvent.setup()
    renderWithProviders(<App />, { route: '/login' })

    await user.type(screen.getByLabelText(/email/i), 'grace@example.com')
    await user.type(screen.getByLabelText(/password/i), 'supersecret')
    await user.click(screen.getByRole('button', { name: /^log in$/i }))

    // Redirected past the login form into the protected profile page.
    expect(await screen.findByRole('heading', { name: /my profile/i })).toBeInTheDocument()

    expect(authApi.login).toHaveBeenCalledWith({ email: 'grace@example.com', password: 'supersecret' })

    const stored = JSON.parse(localStorage.getItem('cocms.auth'))
    expect(stored).toMatchObject({ token: 'fake-jwt-token', userId: 42, role: 'VOLUNTEER_NON_DIVER' })

    // The token that was just stored is the one attached to the very next authenticated request
    // (ProfilePage's getProfile call), proving it's not just saved but actually wired up for use.
    await waitFor(() => expect(userApi.getProfile).toHaveBeenCalledWith(42, 'fake-jwt-token'))
  })

  it('on failure, shows the backend error message and does not store a token', async () => {
    const { ApiError } = await import('../../../shared/api/apiClient.js')
    authApi.login.mockRejectedValue(new ApiError(401, 'UNAUTHORIZED', 'Invalid email or password'))

    const user = userEvent.setup()
    renderWithProviders(<App />, { route: '/login' })

    await user.type(screen.getByLabelText(/email/i), 'grace@example.com')
    await user.type(screen.getByLabelText(/password/i), 'wrong-password')
    await user.click(screen.getByRole('button', { name: /^log in$/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent(/invalid email or password/i)
    expect(localStorage.getItem('cocms.auth')).toBeNull()
  })
})
