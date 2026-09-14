import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../../test-utils.jsx'
import { RegisterPage } from './RegisterPage.jsx'
import * as authApi from '../api/authApi.js'

vi.mock('../api/authApi.js')

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('shows a validation error per empty required field and does not submit', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RegisterPage />, { route: '/register' })

    await user.click(screen.getByRole('button', { name: /^register$/i }))

    expect(screen.getByText(/first name is required/i)).toBeInTheDocument()
    expect(screen.getByText(/last name is required/i)).toBeInTheDocument()
    expect(screen.getByText(/email is required/i)).toBeInTheDocument()
    expect(screen.getByText(/password is required/i)).toBeInTheDocument()
    expect(authApi.register).not.toHaveBeenCalled()
  })

  it('shows an error for an invalid email format', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RegisterPage />, { route: '/register' })

    await user.type(screen.getByLabelText(/email/i), 'not-an-email')
    await user.click(screen.getByRole('button', { name: /^register$/i }))

    expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument()
    expect(authApi.register).not.toHaveBeenCalled()
  })

  it('shows an error for a password shorter than 8 characters', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RegisterPage />, { route: '/register' })

    await user.type(screen.getByLabelText(/password/i), 'short')
    await user.click(screen.getByRole('button', { name: /^register$/i }))

    expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument()
    expect(authApi.register).not.toHaveBeenCalled()
  })

  it('requires an organization name once the Organization role is selected', async () => {
    const user = userEvent.setup()
    renderWithProviders(<RegisterPage />, { route: '/register' })

    await user.selectOptions(screen.getByLabelText(/role/i), 'ORGANIZATION')
    await user.click(screen.getByRole('button', { name: /^register$/i }))

    expect(screen.getByText(/organization name is required/i)).toBeInTheDocument()
    expect(authApi.register).not.toHaveBeenCalled()
  })

  it('only offers the three publicly self-registerable roles — never ADMIN or GOVERNMENT_OFFICER', () => {
    renderWithProviders(<RegisterPage />, { route: '/register' })

    const roleSelect = screen.getByLabelText(/role/i)
    const options = within(roleSelect).getAllByRole('option')

    expect(options).toHaveLength(3)
    const values = options.map((option) => option.value)
    expect(values).toEqual(['VOLUNTEER_NON_DIVER', 'VOLUNTEER_DIVER', 'ORGANIZATION'])
    expect(values).not.toContain('ADMIN')
    expect(values).not.toContain('GOVERNMENT_OFFICER')

    for (const option of options) {
      expect(option.textContent).not.toMatch(/admin/i)
      expect(option.textContent).not.toMatch(/government/i)
    }
  })
})
