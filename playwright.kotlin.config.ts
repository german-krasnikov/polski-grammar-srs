import { defineConfig, devices } from '@playwright/test';
import { existsSync } from 'node:fs';
import { resolve } from 'node:path';

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
  testDir: './tests/browser',
  outputDir: `./test-results/kotlin-${branch}`,
  testMatch: ['kotlin-native-web.spec.ts', 'kotlin-preferences-settings.spec.ts', 'kotlin-method-cycle.spec.ts', 'kotlin-training.spec.ts', 'kotlin-matrix-progress.spec.ts', 'kotlin-progress-migration.spec.ts', 'kotlin-progress-local-day.spec.ts', 'kotlin-parity-matrix.spec.ts', 'kotlin-parity-chain.spec.ts', 'kotlin-parity-skill-picker.spec.ts', 'kotlin-parity-ratings.spec.ts', 'kotlin-parity-progress-review.spec.ts', 'kotlin-parity-progress-load.spec.ts', 'kotlin-parity-progress-recovery.spec.ts', 'kotlin-binary-rating.spec.ts', 'kotlin-emphasis-underline.spec.ts', 'kotlin-style-blocks.spec.ts', 'kotlin-vocabulary.spec.ts', 'kotlin-inventory-content.spec.ts', 'kotlin-flip-card.spec.ts', 'flip-rive-perf.spec.ts', 'kotlin-ux4.spec.ts', 'kotlin-reference-panel-leak.spec.ts', 'kotlin-lifehack-block.spec.ts'],
  retries: 0,
  workers: 1,
  timeout: 45_000,
  expect: { timeout: 10_000 },
  forbidOnly: !!process.env.CI,
  reporter: 'list',
  use: {
    baseURL: `http://127.0.0.1:${port}`,
    locale: 'pl-PL',
    timezoneId: 'Europe/Warsaw',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
  ],
  webServer: {
    command: `python3 -m http.server ${port} --bind 127.0.0.1`,
    cwd: distributionPath,
    url: `http://127.0.0.1:${port}`,
    reuseExistingServer: false,
    timeout: 30_000,
  },
});
