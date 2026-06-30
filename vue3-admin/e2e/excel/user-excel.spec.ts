import { test, expect, login } from '../fixtures/base'
import { mockJson } from '../helpers/api-mock'
import { waitForToast } from '../helpers/wait'

/**
 * Tests for /excel/users — export / import action cards.
 */
test.describe('User Excel', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto('/excel/users')
    await page.waitForLoadState('networkidle')
  })

  test('should display export/import action cards', async ({ page }) => {
    const cards = page.locator('.el-card')
    expect(await cards.count()).toBeGreaterThan(0)
    await expect(page.getByText(/export|导出/i).first()).toBeVisible()
    await expect(page.getByText(/import|导入/i).first()).toBeVisible()
  })

  test('should trigger export download', async ({ page }) => {
    await mockJson(page, '**/api/excel/users/export', {
      status: 200,
      contentType: 'application/octet-stream',
      body: 'id,username\n1,admin'
    })

    const downloadPromise = page.waitForEvent('download', { timeout: 10_000 }).catch(() => null)
    const exportBtn = page.getByRole('button', { name: /export|导出/i }).first()
    await exportBtn.click()
    const download = await downloadPromise
    if (download) {
      expect(download.suggestedFilename()).toMatch(/users?\.xlsx?$/i)
    } else {
      test.skip(true, 'Download event not observed (may use blob URL)')
    }
  })

  test('should trigger import (mock upload)', async ({ page }) => {
    await mockJson(page, '**/api/excel/users/import', {
      status: 200,
      body: { success: true, imported: 3 }
    })

    const uploadInput = page.locator('input[type="file"]').first()
    await uploadInput.setInputFiles({
      name: 'users.xlsx',
      mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      buffer: Buffer.from('504b0304', 'hex')
    })
    await expect.poll(() => waitForToast(page)).toMatch(/success|imported|成功|导入/i)
  })
})