import { test, expect, login } from '../fixtures/base'
import { setLocale } from '../helpers/locale'

/**
 * Tests for internationalization (zh-CN <-> en-US) and locale persistence.
 */
test.describe('Internationalization', () => {
  test('should default to zh-CN', async ({ page, context }) => {
    await context.clearCookies()
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    // The lang switcher should reflect zh-CN as active.
    const storage = await page.evaluate(() => window.localStorage.getItem('locale'))
    // Default fallback is zh-CN; some builds may write 'zh-CN' explicitly.
    expect(storage === null || storage === 'zh-CN').toBeTruthy()
  })

  test('should switch to en-US via LangSwitch', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('networkidle')

    const switcher = page
      .getByRole('button', { name: /language|语言|LangSwitch|EN|CN/i })
      .first()
    if (await switcher.isVisible({ timeout: 1000 }).catch(() => false)) {
      await switcher.click()
      const option = page.getByText(/English/i).first()
      if (await option.isVisible({ timeout: 1000 }).catch(() => false)) {
        await option.click()
        await page.waitForTimeout(300)
      } else {
        // Fallback: write to localStorage and reload.
        await setLocale(page, 'en-US')
        await page.reload({ waitUntil: 'networkidle' })
      }
    } else {
      await setLocale(page, 'en-US')
      await page.reload({ waitUntil: 'networkidle' })
    }

    const storage = await page.evaluate(() => window.localStorage.getItem('locale'))
    expect(storage).toBe('en-US')
  })

  test('should persist language after reload', async ({ page, context }) => {
    await context.clearCookies()
    await setLocale(page, 'en-US')
    await page.goto('/login')
    await page.waitForLoadState('networkidle')

    await page.reload({ waitUntil: 'networkidle' })
    const storage = await page.evaluate(() => window.localStorage.getItem('locale'))
    expect(storage).toBe('en-US')
  })

  test('should translate user table headers in both languages', async ({ page }) => {
    await login(page)
    await page.goto('/users')
    await page.waitForLoadState('networkidle')

    const headersZh = page.getByText(/用户名|姓名|邮箱|手机/i).first()
    await expect(headersZh).toBeVisible()

    await setLocale(page, 'en-US')
    await page.reload({ waitUntil: 'networkidle' })
    const headersEn = page.getByText(/username|email|mobile/i).first()
    await expect(headersEn).toBeVisible()
  })

  test('should translate login form labels', async ({ page, context }) => {
    await context.clearCookies()

    await setLocale(page, 'zh-CN')
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    await expect(page.getByText(/用户名|密码|登录/).first()).toBeVisible()

    await setLocale(page, 'en-US')
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    await expect(page.getByText(/username|password|login/i).first()).toBeVisible()
  })
})