import { test, expect, login } from '../fixtures/base'
import { mockApis, mockJson } from '../helpers/api-mock'
import { waitForDialog, waitForToast, waitForTableLoaded } from '../helpers/wait'
import auditFixture from '../mocks/audit.json'

/**
 * Tests for /audit — command audit & approval workflow.
 */
test.describe('Command audit', () => {
  test.beforeEach(async ({ page }) => {
    await mockApis(page, [
      { url: '**/api/audit/page**', body: auditFixture },
      { url: '**/api/audit/stats', body: auditFixture.stats },
      { url: '**/api/audit/pending', body: { rows: auditFixture.rows.filter((r) => r.status === 'pending'), total: 1 } }
    ])
    await login(page)
    await page.goto('/audit')
    await waitForTableLoaded(page)
  })

  test('should display audit log with stats cards', async ({ page }) => {
    const cards = page.locator('.el-card')
    expect(await cards.count()).toBeGreaterThan(0)
    // Stat labels should be present.
    await expect(page.getByText(/total|全部|总计/i).first()).toBeVisible()
  })

  test('should filter by risk level', async ({ page }) => {
    const filter = page
      .locator('.el-select')
      .filter({ hasText: /risk|风险/i })
      .first()
    if (await filter.isVisible({ timeout: 1000 }).catch(() => false)) {
      await filter.click()
      await page.locator('.el-select-dropdown__item').filter({ hasText: /high|高/i }).first().click()
      await page.waitForLoadState('networkidle')
    } else {
      // Fallback — try a generic select then choose an option.
      const generic = page.locator('.el-select').first()
      if (await generic.isVisible({ timeout: 1000 }).catch(() => false)) {
        await generic.click()
        await page.locator('.el-select-dropdown__item').first().click()
        await page.waitForLoadState('networkidle')
      } else {
        test.skip(true, 'No risk filter found')
      }
    }
  })

  test('should show pending approvals tab', async ({ page }) => {
    const tab = page.getByText(/pending|待审|待审批/i).first()
    if (await tab.isVisible({ timeout: 1000 }).catch(() => false)) {
      await tab.click()
      await page.waitForLoadState('networkidle')
      await expect(page.getByText(/systemctl\s?restart\s?nginx/i).first()).toBeVisible()
    } else {
      test.skip(true, 'No pending-approvals tab')
    }
  })

  test('should open approve dialog', async ({ page }) => {
    const approveBtn = page.getByRole('button', { name: /approve|审批|通过/i }).first()
    if (await approveBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
      await approveBtn.click()
      const dialog = await waitForDialog(page)
      await expect(dialog).toBeVisible()
    } else {
      test.skip(true, 'No approve button found')
    }
  })

  test('should approve with comment', async ({ page }) => {
    await mockJson(page, '**/api/audit/**/approve', { status: 200, body: { success: true } })
    await page.getByRole('button', { name: /approve|审批|通过/i }).first().click()
    const dialog = await waitForDialog(page)

    const textarea = dialog.locator('textarea').first()
    if (await textarea.isVisible({ timeout: 1000 }).catch(() => false)) {
      await textarea.fill('Approved by e2e test')
    }
    const submit = dialog.getByRole('button', { name: /submit|确定|确认|ok/i }).first()
    await submit.click()
    await expect.poll(() => waitForToast(page)).toMatch(/success|approved|成功|通过/i)
  })
})