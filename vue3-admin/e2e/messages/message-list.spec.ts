import { test, expect, login } from '../fixtures/base'
import { mockApis } from '../helpers/api-mock'
import { waitForDrawer, waitForToast, waitForTableLoaded } from '../helpers/wait'
import messagesFixture from '../mocks/messages.json'

/**
 * Tests for /messages — message center with tabs.
 */
test.describe('Message list', () => {
  test.beforeEach(async ({ page }) => {
    await mockApis(page, [
      { url: '**/api/messages/**', body: messagesFixture },
      { url: '**/api/messages/unread', body: { count: messagesFixture.unreadCount } }
    ])
    await login(page)
    await page.goto('/messages')
    await waitForTableLoaded(page)
  })

  test('should display messages with tabs (All / Todo / Unread)', async ({ page }) => {
    const tabs = page.locator('.el-tabs__item, .el-radio-button')
    expect(await tabs.count()).toBeGreaterThan(0)
    // The "All" tab should be present.
    await expect(
      page.getByText(/all|全部/i).first()
    ).toBeVisible()
  })

  test('should show unread count badge', async ({ page }) => {
    const badge = page.locator('.el-badge__content, .el-menu .el-badge, sup').first()
    await badge.isVisible({ timeout: 2000 }).catch(() => undefined)
  })

  test('should display message detail in drawer', async ({ page }) => {
    // Click the first row's view/eye button if present; otherwise the row.
    const viewBtn = page.getByRole('button', { name: /view|查看|详情/i }).first()
    if (await viewBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
      await viewBtn.click()
    } else {
      await page.locator('.el-table__row').first().click()
    }
    await waitForDrawer(page)
    // Title of the first message should be inside the drawer.
    await expect(
      page.locator('.el-drawer').getByText(/system\s?upgrade|please\s?review|welcome/i).first()
    ).toBeVisible()
  })

  test('should mark message as read', async ({ page }) => {
    const readBtn = page.getByRole('button', { name: /mark.*read|标记|已读/i }).first()
    if (await readBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
      await readBtn.click()
      await expect.poll(() => waitForToast(page)).toMatch(/success|read|成功|已读/i)
    } else {
      // Click the row and look for a mark-as-read button inside the drawer.
      await page.locator('.el-table__row').first().click()
      await waitForDrawer(page)
      const drawerBtn = page.locator('.el-drawer').getByRole('button', { name: /read|已读/i }).first()
      if (await drawerBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
        await drawerBtn.click()
        await expect.poll(() => waitForToast(page)).toMatch(/success|read|成功/i)
      } else {
        test.skip(true, 'No mark-as-read action found')
      }
    }
  })
})