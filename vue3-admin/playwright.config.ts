import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright configuration for vue3-admin end-to-end tests.
 *
 * Run all tests:
 *   npm run test:e2e
 *
 * Run a single project:
 *   npm run test:e2e -- --project=chromium
 *
 * Debug mode:
 *   npm run test:e2e:debug
 *
 * Codegen mode (records new tests):
 *   npm run test:e2e:codegen
 *
 * UI mode:
 *   npm run test:e2e:ui
 */
export default defineConfig({
  testDir: './e2e',
  // Don't try to run .ts helper files inside testDir
  testIgnore: ['**/fixtures/**', '**/helpers/**', '**/mocks/**'],
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['junit', { outputFile: 'test-results/results.xml' }]
  ],

  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    viewport: { width: 1280, height: 720 },
    locale: 'en-US',
    timezoneId: 'Asia/Shanghai'
  },

  timeout: 30 * 1000,
  expect: { timeout: 5 * 1000 },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] }
    }
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] }
    // }
  ],

  webServer: {
    command: 'npm run dev',
    port: 5173,
    reuseExistingServer: !process.env.CI,
    stdout: 'ignore',
    stderr: 'pipe',
    timeout: 120 * 1000
  }
})