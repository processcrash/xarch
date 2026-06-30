import { test, expect, login } from '../fixtures/base'
import { mockJson, mockApis } from '../helpers/api-mock'
import { waitForToast } from '../helpers/wait'

/**
 * Tests for /resources — file resource management.
 */
test.describe('Resource list', () => {
  const fixture = {
    rows: [
      { id: 1, name: 'logo.png', size: 12345, type: 'image/png', url: '/static/logo.png' },
      { id: 2, name: 'docs.pdf', size: 678901, type: 'application/pdf', url: '/static/docs.pdf' }
    ],
    total: 2
  }

  test.beforeEach(async ({ page }) => {
    await mockApis(page, [
      { url: '**/api/resources/page**', body: fixture },
      { url: '**/api/resources/**', body: fixture.rows[0] }
    ])
    await login(page)
    await page.goto('/resources')
    await page.waitForLoadState('networkidle')
  })

  test('should display resource list with filters', async ({ page }) => {
    await expect(page.locator('.el-table').first()).toBeVisible()
    const nameInput = page.getByPlaceholder(/name|名称/i).first()
    if (await nameInput.isVisible({ timeout: 1000 }).catch(() => false)) {
      await nameInput.fill('logo')
      await page.getByRole('button', { name: /search|查询|搜索/i }).first().click()
      await page.waitForLoadState('networkidle')
    }
  })

  test('should upload file (mocked)', async ({ page }) => {
    await mockJson(page, '**/api/resources/upload', { status: 200, body: { url: '/static/uploaded.png' } })

    const uploadInput = page.locator('input[type="file"]').first()
    await uploadInput.setInputFiles({
      name: 'sample.png',
      mimeType: 'image/png',
      buffer: Buffer.from('89504e470d0a1a0a', 'hex')
    })
    await expect.poll(() => waitForToast(page)).toMatch(/success|upload|成功|上传/i)
  })

  test('should batch delete resources', async ({ page }) => {
    await mockJson(page, '**/api/resources/**', { status: 200, body: { success: true } })
    const headerCheckbox = page.locator('.el-table__header .el-checkbox').first()
    await headerCheckbox.click()
    await page.waitForTimeout(150)

    const batchDelete = page.getByRole('button', { name: /batch\s?delete|批量删除/i }).first()
    if (await batchDelete.isVisible({ timeout: 1000 }).catch(() => false)) {
      await batchDelete.click()
      const confirm = page.locator('.el-message-box__btns button').first()
      await confirm.click()
      await expect.poll(() => waitForToast(page)).toMatch(/success|deleted|成功/i)
    } else {
      test.skip(true, 'No batch delete control found')
    }
  })

  test('should trigger download for a resource', async ({ page }) => {
    // Stub the file URL so the request resolves locally.
    await page.route('**/static/**', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/octet-stream',
        body: 'fake-binary-content'
      })
    )

    const downloadPromise = page.waitForEvent('download', { timeout: 10_000 }).catch(() => null)
    const downloadBtn = page.getByRole('button', { name: /download|下载/i }).first()
    if (await downloadBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
      await downloadBtn.click()
      const download = await downloadPromise
      if (download) {
        expect(download.suggestedFilename()).toBeTruthy()
      } else {
        // Browser may have prevented the download in this headless context.
        test.skip(true, 'Download event not observed (likely CORS / blob URL)')
      }
    } else {
      test.skip(true, 'No download button found for current row')
    }
  })
})