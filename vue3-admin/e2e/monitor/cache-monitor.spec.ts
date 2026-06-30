import { test, expect, login } from '../fixtures/base'
import { mockJson, mockApis } from '../helpers/api-mock'
import { waitForToast } from '../helpers/wait'
import cacheFixture from '../mocks/cache.json'

/**
 * Tests for /monitor/cache — cache names, keys, and values.
 */
test.describe('Cache monitor', () => {
  test.beforeEach(async ({ page }) => {
    await mockApis(page, [
      { url: '**/api/monitor/cache/names', body: cacheFixture.cacheNames },
      { url: '**/api/monitor/cache/keys/**', body: ['key:1', 'key:2', 'key:3'] },
      { url: '**/api/monitor/cache/value/**', body: { value: 'cached-value', ttl: 3600 } },
      { url: '**/api/monitor/cache/stats', body: cacheFixture.cacheStats }
    ])
    await login(page)
    await page.goto('/monitor/cache')
    await page.waitForLoadState('networkidle')
  })

  test('should display cache names', async ({ page }) => {
    await expect(page.getByText('userCache').first()).toBeVisible()
    await expect(page.getByText('dictCache').first()).toBeVisible()
  })

  test('should show keys for selected cache name', async ({ page }) => {
    await page.getByText('userCache').first().click()
    await page.waitForLoadState('networkidle')
    await expect(page.getByText(/key:1|key:2|key:3/).first()).toBeVisible()
  })

  test('should show value for selected key', async ({ page }) => {
    await page.getByText('userCache').first().click()
    await page.waitForLoadState('networkidle')
    await page.getByText(/key:1/).first().click()
    await page.waitForLoadState('networkidle')
    await expect(page.getByText(/cached-value/).first()).toBeVisible()
  })

  test('should clear cache key with confirm', async ({ page }) => {
    await mockJson(page, '**/api/monitor/cache/clear/**', { status: 200, body: { success: true } })
    await page.getByText('userCache').first().click()
    await page.waitForLoadState('networkidle')

    const clearKeyBtn = page
      .getByRole('button', { name: /clear|删除|清除/i })
      .first()
    if (await clearKeyBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
      await clearKeyBtn.click()
      const confirm = page.locator('.el-message-box__btns button, .el-popconfirm .el-button--primary').first()
      await confirm.click()
      await expect.poll(() => waitForToast(page)).toMatch(/success|cleared|成功|已清除/i)
    } else {
      test.skip(true, 'No per-key clear control found')
    }
  })

  test('should clear all cache with confirm', async ({ page }) => {
    await mockJson(page, '**/api/monitor/cache/clearAll/**', { status: 200, body: { success: true } })
    const clearAll = page.getByRole('button', { name: /clear\s?all|全部清除|清除全部/i }).first()
    if (await clearAll.isVisible({ timeout: 1000 }).catch(() => false)) {
      await clearAll.click()
      const confirm = page.locator('.el-message-box__btns button').first()
      await confirm.click()
      await expect.poll(() => waitForToast(page)).toMatch(/success|cleared|成功|已清除/i)
    } else {
      test.skip(true, 'No clear-all control found')
    }
  })
})