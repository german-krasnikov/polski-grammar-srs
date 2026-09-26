import { defineConfig, devices } from '@playwright/test';
import { existsSync } from 'node:fs';
import { resolve } from 'node:path';

// Tester-added config, mirrors playwright.kotlin.config.ts but points at tests/perf so the
// §5 measurement spec runs without touching the developer's testMatch list. Test-only.
const distribution = process.env.KOTLIN_SPIKE_DIST;
if (!distribution) throw new Error('Set KOTLIN_SPIKE_DIST to the Kotlin production distribution directory.');
if (!['wasm', 'js'].includes(process.env.KOTLIN_SPIKE_BRANCH ?? '')) {
  throw new Error('Set KOTLIN_SPIKE_BRANCH to wasm or js.');
}
const distributionPath = resolve(distribution);
const branch = process.env.KOTLIN_SPIKE_BRANCH;
const port = Number(process.env.KOTLIN_SPIKE_PORT ?? '4174');
if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('Invalid KOTLIN_SPIKE_PORT.');
if (!existsSync(resolve(distributionPath, 'index.html'))) {
  throw new Error(`No index.html in Kotlin distribution: ${distributionPath}`);
}

export default defineConfig({
  testDir: './tests/perf',
  outputDir: `./test-results/perf-${branch}`,
  retries: 0,
  workers: 1,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: 'list',
  use: {
    baseURL: `http://127.0.0.1:${port}`,
    locale: 'pl-PL',
    timezoneId: 'Europe/Warsaw',
    trace: 'off',
    screenshot: 'off',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: `python3 -m http.server ${port} --bind 127.0.0.1`,
    cwd: distributionPath,
    url: `http://127.0.0.1:${port}`,
    reuseExistingServer: false,
    timeout: 30_000,
  },
});
