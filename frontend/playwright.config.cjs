// Minimal Playwright Test config for local smoke checks.
/** @type {import('@playwright/test').PlaywrightTestConfig} */
module.exports = {
  testDir: './tests/e2e',
  timeout: 120000,
  expect: { timeout: 10000 },
  reporter: [['list']],
  use: {
    headless: true,
    baseURL: process.env.E2E_BASE_URL || 'http://127.0.0.1:8000',
    ignoreHTTPSErrors: true,
    viewport: { width: 1440, height: 900 },
  },
};
