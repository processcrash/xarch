import { test as base, expect, type Page } from '@playwright/test'

/**
 * Pre-authenticated test fixture.
 *
 * Mocks /auth/login and /auth/me, then logs in via the real UI once so
 * subsequent tests can skip the login screen entirely.
 *
 * Usage:
 *   import { test, expect } from '../fixtures/auth'
 *   test('my test', async ({ page }) => { ... })
 */
export const test = base.extend<{ authenticatedPage: Page }>({
  storageState: undefined,

  page: async ({ page, context }, use) => {
    // Mock the auth endpoints for the entire test.
    await context.route('**/api/auth/login', async (route) => {
      const body = route.request().postDataJSON() as { username?: string; password?: string }
      if (body?.username === 'admin' && body?.password === 'admin123') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            token: 'fake-jwt-token-for-e2e',
            expireTime: Date.now() + 3600_000,
            username: 'admin',
            roles: 'ROLE_ADMIN'
          })
        })
      } else {
        await route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({ message: 'Invalid credentials' })
        })
      }
    })

    await context.route('**/api/auth/me', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ username: 'admin', roles: 'ROLE_ADMIN' })
      })
    })

    await context.route('**/api/auth/logout', async (route) => {
      await route.fulfill({ status: 200, body: '{}' })
    })

    await use(page)
  }
})

export { expect }