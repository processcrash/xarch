# End-to-end tests (Playwright)

This directory holds the Playwright end-to-end test suite for `vue3-admin`.
Tests exercise the real UI in headless browsers (Chromium & Firefox) and
mock all backend traffic so the suite is fully deterministic and offline.

## Folder layout

```
e2e/
  auth/             Login / logout / auth-store flow
  navigation/       Sidebar, menu items, route navigation
  users/            /users CRUD
  clients/          /clients OAuth client CRUD
  messages/         /messages tabbed message center
  monitor/          /monitor/server and /monitor/cache
  resources/        /resources file management
  audit/            /audit command audit & approval
  excel/            /excel/users export & import
  i18n/             Language switching and persistence
  fixtures/         Custom Playwright test fixtures
  helpers/          Shared utilities (mocking, waits, locale)
  mocks/            Static JSON fixtures reused across suites
```

## Running locally

```bash
# Install browsers once (also runs automatically via pretest:e2e)
npm run pretest:e2e

# Run the full suite headlessly
npm run test:e2e

# Run a single file
npx playwright test e2e/users/user-list.spec.ts

# Run a single project
npx playwright test --project=chromium

# UI mode (interactive runner)
npm run test:e2e:ui

# Step-by-step debug mode
npm run test:e2e:debug

# Record a new test by interacting with the UI
npm run test:e2e:codegen

# Open the HTML report from the last run
npm run test:e2e:report
```

The Playwright config (`playwright.config.ts`) auto-starts `npm run dev`
on port 5173. If a dev server is already running it is reused.

## How to write a new test

1. **Pick or create a fixture.** Most suites import from
   `../fixtures/base` which auto-logs the user in. The base fixture
   extends `fixtures/auth.ts` which mocks `/api/auth/*`.
2. **Mock the API your page hits** before navigation. Reuse data from
   `e2e/mocks/` where possible so the test stays declarative.
3. **Navigate and wait.** Use `page.waitForLoadState('networkidle')`
   after every navigation. Use `waitForTableLoaded(page)` from
   `helpers/wait.ts` for tables and `waitForDialog(page)` for dialogs.
4. **Use stable selectors.** Prefer `getByRole`, `getByLabel`,
   `getByText` over CSS. Use partial regexes (e.g. `/login|登录/`) so
   the test survives i18n changes.
5. **Group related assertions** in `test.describe()` and clean up
   shared state in `test.beforeEach()`.

```ts
import { test, expect } from '../fixtures/base'
import { mockJson } from '../helpers/api-mock'
import { waitForDialog, waitForToast } from '../helpers/wait'

test.describe('My feature', () => {
  test.beforeEach(async ({ page }) => {
    await mockJson(page, '**/api/things', { body: { rows: [], total: 0 } })
  })

  test('opens a dialog', async ({ page }) => {
    await page.goto('/things')
    await page.getByRole('button', { name: /new|新增/i }).click()
    await waitForDialog(page)
    await expect(page.locator('.el-dialog__title')).toBeVisible()
  })
})
```

## Updating mocks

Mocks live under `e2e/mocks/*.json` and are imported directly by spec
files. To change the shape of a fixture:

1. Edit the JSON file.
2. The change is picked up automatically on the next run (no rebuild).

To add a new fixture:

1. Create `e2e/mocks/<feature>.json`.
2. Import it in your spec: `import fixture from '../mocks/<feature>.json'`.
3. Use `mockJson(page, '**/api/<feature>/**', { body: fixture })`.

## Debugging failed tests

- Run the failing file in debug mode:
  `npm run test:e2e:debug -- e2e/users/user-list.spec.ts`
- Inspect the HTML report: `npm run test:e2e:report`
- Open a specific trace from `test-results/<...>/trace.zip` using
  `npx playwright show-trace <file>`.
- Use the `--headed` flag with a single project to see the browser:
  `npx playwright test --project=chromium --headed`

## Best practices

- **Test isolation.** Each test starts on a fresh `page` and never
  depends on order or shared state.
- **Mock everything.** Real backend traffic makes tests flaky. If a
  test requires the backend, tag it `@pending` or `@skip` and explain
  why.
- **Stable selectors only.** Element Plus class names and Vue scoped
  styles change; semantic roles and labels are far more durable.
- **Don't depend on exact text.** Use regexes that match both
  `zh-CN` and `en-US` translations where the label is multilingual.
- **Wait for state, not time.** Prefer `waitForLoadState` and
  `waitFor` over arbitrary `waitForTimeout` calls. The only timeout
  allowed is for animations.
- **Use Page Object Model for repeated flows.** When a flow
  (login, navigation) is repeated in many specs, extract it into
  `fixtures/` or `helpers/` rather than duplicating it.
- **Viewport.** Tests run at 1280×720 by default. If a feature
  requires a different viewport, override `use.viewport` per test.
- **No screenshots / videos in passing runs.** The config retains
  them only on failure to keep the repo lean.

## CI

`.github/workflows/e2e.yml` runs the suite on every push to `main`
and on every PR. It uploads the HTML report, test results, and
per-trace zips as artifacts when a job fails.

Cache strategy:

- Playwright browsers are cached under `~/.cache/ms-playwright`,
  keyed by the lockfile hash.
- Node modules are cached via `actions/setup-node` cache.