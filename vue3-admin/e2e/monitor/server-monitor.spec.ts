import { test, expect, login } from '../fixtures/base'
import { mockJson } from '../helpers/api-mock'
import serverFixture from '../mocks/server.json'

/**
 * Tests for /monitor/server — runtime metrics dashboard.
 */
test.describe('Server monitor', () => {
  test.beforeEach(async ({ page }) => {
    await mockJson(page, '**/api/monitor/server', { body: serverFixture })
    await login(page)
    await page.goto('/monitor/server')
    await page.waitForLoadState('networkidle')
  })

  test('should display server info dashboard', async ({ page }) => {
    await expect(page.locator('main, .el-main, .main-content').first()).toBeVisible()
    // Either a card or chart should be visible.
    const card = page.locator('.el-card').first()
    await expect(card).toBeVisible()
  })

  test('should show CPU / memory / JVM usage cards', async ({ page }) => {
    await expect(page.getByText(/cpu/i).first()).toBeVisible()
    await expect(page.getByText(/memory|内存/i).first()).toBeVisible()
    await expect(page.getByText(/jvm/i).first()).toBeVisible()
  })

  test('should refresh server info', async ({ page }) => {
    let calls = 0
    await page.route('**/api/monitor/server', async (route) => {
      calls += 1
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(serverFixture)
      })
    })
    const refresh = page
      .getByRole('button', { name: /refresh|reload|刷新/i })
      .first()
    if (await refresh.isVisible({ timeout: 1000 }).catch(() => false)) {
      await refresh.click()
      expect(calls).toBeGreaterThanOrEqual(1)
    } else {
      // Reload the page — endpoint should be hit again.
      await page.reload({ waitUntil: 'networkidle' })
      expect(calls).toBeGreaterThanOrEqual(1)
    }
  })
})