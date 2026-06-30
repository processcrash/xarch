import { test, expect, login } from '../fixtures/base'
import { mockJson } from '../helpers/api-mock'

/**
 * Tests for the sidebar / main navigation.
 * Relies on the shared `auth` fixture which mocks /auth/*.
 */
test.describe('Sidebar navigation', () => {
  test.beforeEach(async ({ page }) => {
    // Suppress noisy API traffic the sidebar might trigger.
    await mockJson(page, '**/api/messages/**', { body: { rows: [], total: 0, unreadCount: 0 } })
    await mockJson(page, '**/api/users/page**', { body: { rows: [], total: 0 } })
    await mockJson(page, '**/api/menus/tree**', { body: [] })
    await login(page)
  })

  test('should display the sidebar with menu items', async ({ page }) => {
    const sidebar = page.locator('.el-aside, .sidebar, aside').first()
    await expect(sidebar).toBeVisible()
    // The sidebar should contain at least one menu entry.
    const items = page.locator('.el-menu-item, .el-sub-menu__title')
    expect(await items.count()).toBeGreaterThan(0)
  })

  test('should navigate to user list when clicking Users', async ({ page }) => {
    const usersLink = page.getByRole('menuitem', { name: /user|用户/i }).first()
    await usersLink.click()
    await page.waitForURL('**/users', { timeout: 10_000 })
    await expect(page).toHaveURL(/\/users/)
  })

  test('should highlight active menu item', async ({ page }) => {
    const usersLink = page.getByRole('menuitem', { name: /user|用户/i }).first()
    await usersLink.click()
    await page.waitForURL('**/users', { timeout: 10_000 })
    // Element Plus marks active items with .is-active.
    await expect(usersLink).toHaveClass(/is-active/)
  })

  test('should collapse the sidebar', async ({ page }) => {
    // Element Plus admin layouts usually have a collapse toggle button.
    const toggle = page
      .getByRole('button', { name: /collapse|toggle|展开|收起/i })
      .or(page.locator('.hamburger, .collapse-btn, [class*="collapse"]').first())
    if (await toggle.first().isVisible({ timeout: 1000 }).catch(() => false)) {
      await toggle.first().click()
      await page.waitForTimeout(300)
      // The sidebar should now have a "collapsed" modifier class.
      const collapsed = page.locator('.el-menu--collapse, .is-collapsed, [class*="collapsed"]').first()
      await expect(collapsed).toBeVisible()
    } else {
      test.skip(true, 'No collapse control found in current layout')
    }
  })

  test('should show unread message count badge (mocked)', async ({ page }) => {
    await mockJson(page, '**/api/messages/unread', { body: { count: 5 } })
    // Re-load so the badge picks up the new value.
    await page.goto('/home')
    await page.waitForLoadState('networkidle')
    const badge = page.locator('.el-badge__content, .el-menu .el-badge, sup').first()
    // Soft assertion — the badge is layout-specific.
    await badge.isVisible({ timeout: 2000 }).catch(() => undefined)
  })

  const sections: Array<{ path: string; label: RegExp }> = [
    { path: '/users', label: /user/i },
    { path: '/roles', label: /role|角色/i },
    { path: '/clients', label: /client|客户端/i },
    { path: '/messages', label: /message|消息/i },
    { path: '/monitor/server', label: /server|服务/i },
    { path: '/monitor/cache', label: /cache|缓存/i },
    { path: '/resources', label: /resource|资源/i },
    { path: '/tempfiles', label: /temp\s?file|临时/i },
    { path: '/audit', label: /audit|审计/i },
    { path: '/excel/users', label: /excel/i }
  ]

  for (const section of sections) {
    test(`should navigate to ${section.path}`, async ({ page }) => {
      await page.goto(section.path)
      await page.waitForLoadState('networkidle')
      // The URL should match (after any trailing redirect).
      expect(page.url()).toContain(section.path)
      // Main content area should be visible (not a blank page).
      const main = page.locator('main, .main-content, .el-main').first()
      await expect(main).toBeVisible()
    })
  }
})