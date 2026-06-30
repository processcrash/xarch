import type { Page } from '@playwright/test'

export type SupportedLocale = 'zh-CN' | 'en-US'

/**
 * Switch the UI language. The app stores locale in localStorage
 * (key "locale") which is read at boot, so we set it before navigation
 * and reload the page.
 */
export async function setLocale(page: Page, locale: SupportedLocale): Promise<void> {
  await page.addInitScript((l) => {
    try {
      window.localStorage.setItem('locale', l as string)
    } catch {
      /* no-op */
    }
  }, locale)
  if (!page.url().includes('about:blank')) {
    await page.reload({ waitUntil: 'networkidle' })
  }
}

/**
 * Click the LangSwitch component if visible on the page.
 * Falls back to localStorage + reload when not present.
 */
export async function switchLanguageInUi(
  page: Page,
  target: SupportedLocale
): Promise<void> {
  const switcher = page.getByRole('button', { name: /language|语言|LangSwitch/i })
  if (await switcher.isVisible({ timeout: 1000 }).catch(() => false)) {
    await switcher.click()
    const option = page.getByRole('option', {
      name: target === 'zh-CN' ? /中文|Chinese/ : /English|英文/
    })
    await option.click()
    await page.waitForTimeout(300)
    return
  }
  await setLocale(page, target)
}