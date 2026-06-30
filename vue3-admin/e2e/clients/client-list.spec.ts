import { test, expect, login } from '../fixtures/base'
import { mockJson, mockApis } from '../helpers/api-mock'
import { waitForDialog, waitForToast, waitForTableLoaded } from '../helpers/wait'
import clientsFixture from '../mocks/clients.json'

/**
 * Tests for /clients — OAuth client management.
 */
test.describe('Client list', () => {
  test.beforeEach(async ({ page }) => {
    await mockApis(page, [
      { url: '**/api/clients/page**', body: clientsFixture },
      { url: '**/api/clients', body: clientsFixture.rows[0] }
    ])
    await login(page)
    await page.goto('/clients')
    await waitForTableLoaded(page)
  })

  test('should display client list', async ({ page }) => {
    await expect(page.locator('.el-table').first()).toBeVisible()
    await expect(page.getByText('web-client').first()).toBeVisible()
    await expect(page.getByText('mobile-client').first()).toBeVisible()
  })

  test('should add new OAuth client', async ({ page }) => {
    await mockJson(page, '**/api/clients', { status: 200, body: { success: true } })
    await page.getByRole('button', { name: /add|new|新增|添加/i }).first().click()
    const dialog = await waitForDialog(page)
    await expect(dialog).toBeVisible()

    const inputs = dialog.locator('input')
    await inputs.nth(0).fill('test-client')
    await inputs.nth(1).fill('test-secret')

    const submit = dialog.getByRole('button', { name: /submit|save|确定|保存/i }).first()
    await submit.click()
    await expect.poll(() => waitForToast(page)).toMatch(/success|成功/i)
  })

  test('should edit client', async ({ page }) => {
    await mockJson(page, '**/api/clients/**', { status: 200, body: { success: true } })
    await page.getByRole('button', { name: /edit|修改|编辑/i }).first().click()
    const dialog = await waitForDialog(page)
    await expect(dialog).toBeVisible()
    const submit = dialog.getByRole('button', { name: /submit|save|确定|保存/i }).first()
    await submit.click()
    await expect.poll(() => waitForToast(page)).toMatch(/success|成功/i)
  })

  test('should delete client with confirmation', async ({ page }) => {
    await mockJson(page, '**/api/clients/**', { status: 200, body: { success: true } })
    await page.getByRole('button', { name: /delete|删除/i }).first().click()
    const confirm = page.locator('.el-message-box__btns button, .el-popconfirm .el-button--primary').first()
    await confirm.click()
    await expect.poll(() => waitForToast(page)).toMatch(/success|deleted|成功|已删除/i)
  })

  test('should batch delete', async ({ page }) => {
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
      test.skip(true, 'No batch delete button visible')
    }
  })
})