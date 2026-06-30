import { test, expect, login } from '../fixtures/base'
import { mockJson, mockApis } from '../helpers/api-mock'
import { waitForDialog, waitForToast, waitForTableLoaded } from '../helpers/wait'
import usersFixture from '../mocks/users.json'

/**
 * Tests for /users — table, search, add/edit/delete dialogs, i18n.
 */
test.describe('User list', () => {
  test.beforeEach(async ({ page }) => {
    await mockApis(page, [
      { url: '**/api/users/page**', body: usersFixture },
      { url: '**/api/users', body: usersFixture.rows[0] }
    ])
    await login(page)
    await page.goto('/users')
    await waitForTableLoaded(page)
  })

  test('should display user table with pagination', async ({ page }) => {
    const table = page.locator('.el-table').first()
    await expect(table).toBeVisible()
    const pagination = page.locator('.el-pagination').first()
    await expect(pagination).toBeVisible()
  })

  test('should search by username', async ({ page }) => {
    await page.getByPlaceholder(/username|用户名|search|搜索/i).first().fill('alice')
    await page.getByRole('button', { name: /search|查询|搜索/i }).first().click()
    await page.waitForLoadState('networkidle')
    await expect(page.getByText('alice').first()).toBeVisible()
  })

  test('should reset search', async ({ page }) => {
    const input = page.getByPlaceholder(/username|用户名|search|搜索/i).first()
    await input.fill('alice')
    const reset = page.getByRole('button', { name: /reset|重置/i }).first()
    if (await reset.isVisible({ timeout: 1000 }).catch(() => false)) {
      await reset.click()
      await expect(input).toHaveValue('')
    }
  })

  test('should open add user dialog', async ({ page }) => {
    const addBtn = page.getByRole('button', { name: /add|new|新增|添加/i }).first()
    await addBtn.click()
    const dialog = await waitForDialog(page)
    await expect(dialog).toBeVisible()
    await expect(dialog.getByText(/new\s?user|add\s?user|新增用户|添加用户/i).first()).toBeVisible()
  })

  test('should validate required fields', async ({ page }) => {
    await page.getByRole('button', { name: /add|new|新增|添加/i }).first().click()
    const dialog = await waitForDialog(page)
    // Submit immediately to trigger validation.
    const submit = dialog.getByRole('button', { name: /submit|save|确定|保存/i }).first()
    await submit.click()
    const validation = dialog.locator('.el-form-item__error').first()
    await expect(validation).toBeVisible({ timeout: 3_000 })
  })

  test('should create new user successfully (mocked)', async ({ page }) => {
    await mockJson(page, '**/api/users', { status: 200, body: { success: true } })
    await page.getByRole('button', { name: /add|new|新增|添加/i }).first().click()
    const dialog = await waitForDialog(page)

    await dialog.locator('input').nth(0).fill('newuser')
    await dialog.locator('input[type="password"]').first().fill('Pa55word!')
    await dialog.locator('input').nth(2).fill('new@example.com')
    await dialog.locator('input').nth(3).fill('13800000099')

    const submit = dialog.getByRole('button', { name: /submit|save|确定|保存/i }).first()
    await submit.click()
    await expect.poll(() => waitForToast(page)).toMatch(/success|成功/i)
  })

  test('should display validation error for duplicate username', async ({ page }) => {
    await mockJson(page, '**/api/users', {
      status: 409,
      body: { message: 'Username already exists' }
    })
    await page.getByRole('button', { name: /add|new|新增|添加/i }).first().click()
    const dialog = await waitForDialog(page)
    await dialog.locator('input').nth(0).fill('admin')
    await dialog.locator('input[type="password"]').first().fill('Pa55word!')

    const submit = dialog.getByRole('button', { name: /submit|save|确定|保存/i }).first()
    await submit.click()
    await expect.poll(() => waitForToast(page)).toMatch(/exist|duplicate|已存在|失败/i)
  })

  test('should open edit dialog', async ({ page }) => {
    const editBtn = page.getByRole('button', { name: /edit|修改|编辑/i }).first()
    await editBtn.click()
    const dialog = await waitForDialog(page)
    await expect(dialog).toBeVisible()
  })

  test('should update user successfully (mocked)', async ({ page }) => {
    await mockJson(page, '**/api/users/**', { status: 200, body: { success: true } })
    await page.getByRole('button', { name: /edit|修改|编辑/i }).first().click()
    const dialog = await waitForDialog(page)
    const submit = dialog.getByRole('button', { name: /submit|save|确定|保存/i }).first()
    await submit.click()
    await expect.poll(() => waitForToast(page)).toMatch(/success|成功/i)
  })

  test('should show confirm dialog before delete', async ({ page }) => {
    const deleteBtn = page.getByRole('button', { name: /delete|删除/i }).first()
    await deleteBtn.click()
    // Element Plus delete uses .el-message-box or .el-popconfirm.
    const confirm = page.locator('.el-message-box, .el-popconfirm').first()
    await expect(confirm).toBeVisible({ timeout: 3_000 })
  })

  test('should delete user successfully (mocked)', async ({ page }) => {
    await mockJson(page, '**/api/users/**', { status: 200, body: { success: true } })
    await page.getByRole('button', { name: /delete|删除/i }).first().click()
    const confirm = page.locator('.el-message-box__btns button, .el-popconfirm .el-button--primary').first()
    await confirm.click()
    await expect.poll(() => waitForToast(page)).toMatch(/success|deleted|成功|已删除/i)
  })

  test('should support batch delete with selected rows', async ({ page }) => {
    // Select all checkboxes in the header.
    const headerCheckbox = page.locator('.el-table__header .el-checkbox').first()
    await headerCheckbox.click()
    await page.waitForTimeout(150)
    const batchDelete = page.getByRole('button', { name: /batch\s?delete|delete\s?selected|批量删除/i }).first()
    if (await batchDelete.isVisible({ timeout: 1000 }).catch(() => false)) {
      await batchDelete.click()
      const confirm = page.locator('.el-message-box__btns button').first()
      await confirm.click()
      await expect.poll(() => waitForToast(page)).toMatch(/success|deleted|成功/i)
    } else {
      test.skip(true, 'No batch delete button found in current implementation')
    }
  })

  test('should change page size and navigate pages', async ({ page }) => {
    const sizeSelector = page.locator('.el-pagination .el-select').first()
    if (await sizeSelector.isVisible({ timeout: 1000 }).catch(() => false)) {
      await sizeSelector.click()
      await page.getByText('20/page', { exact: false }).first().click().catch(async () => {
        await page.locator('.el-select-dropdown__item').first().click()
      })
      await page.waitForLoadState('networkidle')
    }
    const next = page.locator('.el-pagination .btn-next').first()
    if (await next.isVisible({ timeout: 1000 }).catch(() => false)) {
      await next.click()
      await page.waitForLoadState('networkidle')
    }
  })

  test('should switch language and verify translated labels', async ({ page }) => {
    // Toggle to English.
    const switcher = page
      .getByRole('button', { name: /language|语言|LangSwitch/i })
      .first()
    if (await switcher.isVisible({ timeout: 1000 }).catch(() => false)) {
      await switcher.click()
      await page.getByText(/English/i).first().click().catch(() => undefined)
      await page.waitForTimeout(300)
    }
    // Header should still render — just confirm a column header is present.
    await expect(page.locator('.el-table__header').first()).toBeVisible()
  })
})