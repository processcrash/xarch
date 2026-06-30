import type { Page, Locator } from '@playwright/test'

/**
 * Common wait patterns for vue3-admin (Element Plus components).
 */

/**
 * Wait for an Element Plus toast (el-message) and return its text.
 */
export async function waitForToast(page: Page): Promise<string> {
  const toast = page.locator('.el-message').first()
  await toast.waitFor({ state: 'visible', timeout: 5_000 })
  const text = (await toast.innerText()).trim()
  // Element Plus auto-removes toasts after ~3s; don't fail if it's already gone.
  return text
}

/**
 * Wait for an Element Plus notification (el-notification).
 */
export async function waitForNotification(page: Page): Promise<string> {
  const n = page.locator('.el-notification').first()
  await n.waitFor({ state: 'visible', timeout: 5_000 })
  return (await n.innerText()).trim()
}

/**
 * Wait for any open Element Plus dialog (.el-dialog) to be visible.
 * Returns the dialog locator for further assertions.
 */
export async function waitForDialog(page: Page): Promise<Locator> {
  const dialog = page.locator('.el-dialog__wrapper, .el-overlay-dialog .el-dialog').first()
  await dialog.waitFor({ state: 'visible', timeout: 5_000 })
  return dialog
}

/**
 * Wait for an Element Plus drawer (.el-drawer) to open.
 */
export async function waitForDrawer(page: Page): Promise<Locator> {
  const drawer = page.locator('.el-drawer').first()
  await drawer.waitFor({ state: 'visible', timeout: 5_000 })
  return drawer
}

/**
 * Wait for an Element Plus table (.el-table) to finish loading.
 * Element Plus renders `.el-table__empty-row` while data is fetching.
 */
export async function waitForTableLoaded(page: Page, tableLocator?: Locator): Promise<void> {
  const table = tableLocator ?? page.locator('.el-table').first()
  await table.waitFor({ state: 'visible', timeout: 10_000 })
  // Wait until the loading mask disappears (if any).
  const loading = page.locator('.el-table .el-loading-mask')
  await loading.waitFor({ state: 'hidden', timeout: 10_000 }).catch(() => {
    /* not all tables show a loading mask */
  })
}

/**
 * Wait until the network is idle for at least 500ms.
 */
export async function waitForNetworkIdle(page: Page, timeout = 10_000): Promise<void> {
  await page.waitForLoadState('networkidle', { timeout })
}