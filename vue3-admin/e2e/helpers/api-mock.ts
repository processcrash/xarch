import type { Page, Route } from '@playwright/test'

/**
 * Mock helpers for intercepting network calls. Use these instead of
 * hitting a real backend so tests are deterministic.
 */

export interface MockJson {
  status?: number
  body?: unknown
  delay?: number
}

/**
 * Mock a single endpoint with a JSON response.
 */
export async function mockJson(
  page: Page,
  urlPattern: string | RegExp,
  response: MockJson = {}
): Promise<void> {
  await page.route(urlPattern, async (route: Route) => {
    if (response.delay) {
      await new Promise((r) => setTimeout(r, response.delay))
    }
    await route.fulfill({
      status: response.status ?? 200,
      contentType: 'application/json',
      body: JSON.stringify(response.body ?? {})
    })
  })
}

/**
 * Mock multiple endpoints at once.
 *
 * Example:
 *   await mockApis(page, [
 *     { url: '**/api/users', body: { rows: [], total: 0 } },
 *     { url: '**/api/clients', body: [] }
 *   ])
 */
export async function mockApis(
  page: Page,
  mocks: Array<{ url: string | RegExp; method?: string; status?: number; body?: unknown }>
): Promise<void> {
  for (const m of mocks) {
    await page.route(m.url, async (route) => {
      if (m.method && route.request().method() !== m.method) {
        await route.fallback()
        return
      }
      await route.fulfill({
        status: m.status ?? 200,
        contentType: 'application/json',
        body: JSON.stringify(m.body ?? {})
      })
    })
  }
}

/**
 * Block all calls to a pattern (returns empty success).
 * Useful when you want to suppress noise from a third-party endpoint.
 */
export async function blockApi(page: Page, urlPattern: string | RegExp): Promise<void> {
  await page.route(urlPattern, (route) =>
    route.fulfill({ status: 204, body: '' })
  )
}

/**
 * Shorthand for matching all `/api/...` traffic that wasn't mocked.
 * Returns 200 with an empty payload so the UI doesn't error.
 */
export async function passthroughUnknownApi(page: Page): Promise<void> {
  await page.route('**/api/**', async (route) => {
    if (route.request().method() === 'OPTIONS') {
      await route.fulfill({ status: 204, body: '' })
      return
    }
    // Let already-routed handlers run first
    const handlers = page.handlers || []
    void handlers
    await route.fallback()
  })
}