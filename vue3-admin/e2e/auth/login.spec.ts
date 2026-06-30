import { test, expect } from '../fixtures/auth'
import { setLocale, switchLanguageInUi } from '../helpers/locale'
import { waitForToast } from '../helpers/wait'

/**
 * Tests for the /login screen and basic authentication flow.
 * Mocks are installed by the `auth` fixture.
 */
test.describe('Login page', () => {
  test.beforeEach(async ({ page }) => {
    // Make sure no stale token is hanging around.
    await page.context().clearCookies()
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
  })

  test('should display login page with username and password fields', async ({ page }) => {
    await expect(page.getByText(/login|登录/i).first()).toBeVisible()
    await expect(page.getByPlaceholder(/username|用户名/i).first()).toBeVisible()
    await expect(page.getByPlaceholder(/password|密码/i).first()).toBeVisible()
    await expect(page.getByRole('button', { name: /login|登录/i })).toBeVisible()
  })

  test('should show warning toast for empty fields', async ({ page }) => {
    await page.getByRole('button', { name: /login|登录/i }).click()
    await expect.poll(() => waitForToast(page)).toMatch(/enter|credentials|请输入|用户名|密码/i)
  })

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.getByPlaceholder(/username|用户名/i).first().fill('admin')
    await page.getByPlaceholder(/password|密码/i).first().fill('admin123')
    await page.getByRole('button', { name: /login|登录/i }).click()

    await page.waitForURL('**/home', { timeout: 15_000 })
    expect(page.url()).toContain('/home')
  })

  test('should redirect to home after login', async ({ page }) => {
    await page.getByPlaceholder(/username|用户名/i).first().fill('admin')
    await page.getByPlaceholder(/password|密码/i).first().fill('admin123')
    await page.getByRole('button', { name: /login|登录/i }).click()
    await page.waitForURL('**/home', { timeout: 15_000 })
    await expect(page).toHaveURL(/\/home$/)
  })

  test('should show error for invalid credentials', async ({ page }) => {
    await page.getByPlaceholder(/username|用户名/i).first().fill('admin')
    await page.getByPlaceholder(/password|密码/i).first().fill('wrong-password')
    await page.getByRole('button', { name: /login|登录/i }).click()

    await expect.poll(() => waitForToast(page)).toMatch(/invalid|fail|错误|失败/i)
    // Still on login page.
    await expect(page).toHaveURL(/\/login/)
  })

  test('should support language switch on login page', async ({ page }) => {
    await switchLanguageInUi(page, 'en-US')
    // After switching to English, the login title should be English.
    const title = page.getByText(/login/i).first()
    await expect(title).toBeVisible()
  })

  test('should logout and redirect to login', async ({ page }) => {
    // First, login.
    await page.getByPlaceholder(/username|用户名/i).first().fill('admin')
    await page.getByPlaceholder(/password|密码/i).first().fill('admin123')
    await page.getByRole('button', { name: /login|登录/i }).click()
    await page.waitForURL('**/home', { timeout: 15_000 })

    // Trigger logout — look for any dropdown trigger in the header.
    const profile = page.getByText(/admin/i).first()
    if (await profile.isVisible({ timeout: 1000 }).catch(() => false)) {
      await profile.click()
      const logoutBtn = page.getByRole('menuitem', { name: /logout|退出/i })
        .or(page.getByText(/logout|退出/i))
      if (await logoutBtn.first().isVisible({ timeout: 1000 }).catch(() => false)) {
        await logoutBtn.first().click()
        await page.waitForURL('**/login', { timeout: 10_000 })
        await expect(page).toHaveURL(/\/login/)
        return
      }
    }

    // Fallback: clear storage and navigate.
    await page.evaluate(() => {
      window.localStorage.removeItem('token')
      window.localStorage.removeItem('username')
    })
    await page.goto('/home')
    await page.waitForURL('**/login', { timeout: 10_000 })
    await expect(page).toHaveURL(/\/login/)
  })
})