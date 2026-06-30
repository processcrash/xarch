import { test as authTest, expect } from './auth'

/**
 * Base fixture that auto-logs in before each test.
 *
 * Usage:
 *   import { test, expect } from '../fixtures/base'
 *   test('navigate to users', async ({ page }) => { ... })
 */
export const test = authTest.extend({})

/**
 * Helper that performs the login flow through the UI once.
 * Most tests should prefer this over the storageState approach because
 * it exercises the actual login components.
 */
export async function login(page: import('@playwright/test').Page) {
  await page.goto('/login')
  await page.waitForLoadState('networkidle')
  await page.getByPlaceholder(/username|用户名/i).first().fill('admin')
  await page.getByPlaceholder(/password|密码/i).first().fill('admin123')
  await page.getByRole('button', { name: /login|登录/i }).click()
  await page.waitForURL('**/home', { timeout: 15_000 })
}

export { expect }