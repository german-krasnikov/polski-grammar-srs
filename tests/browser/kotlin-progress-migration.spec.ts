import { expect, test } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { createServer, type ViteDevServer } from 'vite';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const legacyKey = 'polski-grammar-srs-v1';
const previewKey = 'polski-grammar-srs-kmp-preview-v1';
const backupKey = 'polski-grammar-srs-kmp-legacy-backup-v1';
const markerKey = 'polski-grammar-srs-kmp-migrated-v1';
const fixture = JSON.parse(readFileSync(resolve('tests/fixtures/kotlin-parity/progress.json'), 'utf8')) as {
  cases: { id: string; expected: unknown }[];
};
const legacy = JSON.stringify(fixture.cases.find(entry => entry.id === 'P-fresh')!.expected);
let reactServer: ViteDevServer;
let reactBaseUrl: string;

test.beforeAll(async () => {
  reactServer = await createServer({
    configFile: resolve('vite.config.ts'),
    server: { host: '127.0.0.1', port: 0 },
  });
  await reactServer.listen();
  reactBaseUrl = reactServer.resolvedUrls!.local[0];
});

test.afterAll(async () => {
  await reactServer?.close();
});

test.beforeEach(async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
});

test('current Kotlin export loads in React after a migrated review', async ({ page, browser }) => {
  await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [legacyKey, legacy] as const);
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Найден прежний прогресс' })).toBeVisible();
  await page.getByRole('button', { name: 'Перенести прогресс' }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await page.getByRole('button', { name: 'Прогресс' }).click();

  const downloadPromise = page.waitForEvent('download');
  await page.locator('#progress-export').click();
  const download = await downloadPromise;
  const currentRaw = readFileSync((await download.path())!, 'utf8');
  const imported = JSON.parse(currentRaw) as {
    version: number;
    totalReviews: number;
    cards: { card: { last_review?: string } }[];
  };
  expect(imported.version).toBe(1);
  expect(imported.totalReviews).toBe(1);
  expect(imported.cards.length).toBe(16);
  expect(imported.cards.some(entry => entry.card.last_review !== undefined)).toBe(true);
  expect(await page.evaluate(key => localStorage.getItem(key), legacyKey)).toBe(legacy);
  expect(await page.evaluate(key => localStorage.getItem(key), backupKey)).toBe(legacy);
  expect(await page.evaluate(key => localStorage.getItem(key), markerKey)).not.toBeNull();
  expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).not.toBeNull();

  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const reactPage = await reactContext.newPage();
    await reactPage.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [legacyKey, currentRaw] as const);
    await reactPage.goto(reactBaseUrl);
    await reactPage.getByRole('button', { name: 'Прогресс' }).click();
    await expect(reactPage.locator('.bigstats').getByText('1', { exact: true }).first()).toBeVisible();
    expect(await reactPage.evaluate(key => localStorage.getItem(key), legacyKey)).toBe(currentRaw);
  } finally {
    await reactContext.close();
  }
});

test('invalid legacy remains recoverable without a preview or marker', async ({ page }) => {
  await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [legacyKey, '{'] as const);
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Найден прежний прогресс' })).toBeVisible();
  await page.getByRole('button', { name: 'Перенести прогресс' }).click();
  await expect(page.getByRole('heading', { name: 'Нужна копия прогресса' })).toBeVisible();
  await expect(page.getByRole('alert')).toHaveText('Expected JSON object');
  expect(await page.evaluate(key => localStorage.getItem(key), legacyKey)).toBe('{');
  expect(await page.evaluate(key => localStorage.getItem(key), backupKey)).toBe('{');
  expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).toBeNull();
  expect(await page.evaluate(key => localStorage.getItem(key), markerKey)).toBeNull();
});

test('a transient marker write failure offers an in-app migration retry', async ({ page }) => {
  await page.addInitScript(([key, raw, marker]) => {
    localStorage.setItem(key, raw);
    const original = Storage.prototype.setItem;
    let failed = sessionStorage.getItem('kmp-test-marker-write-failed') === '1';
    Storage.prototype.setItem = function (name: string, value: string) {
      if (name === marker && !failed) {
        failed = true;
        sessionStorage.setItem('kmp-test-marker-write-failed', '1');
        throw new DOMException('Injected quota failure', 'QuotaExceededError');
      }
      return original.call(this, name, value);
    };
  }, [legacyKey, legacy, markerKey] as const);
  await page.goto('/');
  await page.getByRole('button', { name: 'Перенести прогресс' }).click();
  await expect(page.getByRole('heading', { name: 'Нужна копия прогресса' })).toBeVisible();
  expect(await page.evaluate(key => localStorage.getItem(key), legacyKey)).toBe(legacy);
  expect(await page.evaluate(key => localStorage.getItem(key), backupKey)).toBe(legacy);
  const partialPreview = await page.evaluate(key => localStorage.getItem(key), previewKey);
  expect(partialPreview).not.toBeNull();
  expect(await page.evaluate(key => localStorage.getItem(key), markerKey)).toBeNull();
  await page.reload();
  await expect(page.getByRole('heading', { name: 'Нужна копия прогресса' })).toBeVisible();
  expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).toBe(partialPreview);
  expect(await page.evaluate(key => localStorage.getItem(key), markerKey)).toBeNull();
  await page.getByRole('button', { name: /повторить перенос|перенести прогресс/i }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
  expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).toBe(partialPreview);
  expect(await page.evaluate(key => localStorage.getItem(key), markerKey)).not.toBeNull();
  expect(await page.evaluate(key => localStorage.getItem(key), backupKey)).toBe(legacy);
  expect(await page.evaluate(key => localStorage.getItem(key), legacyKey)).toBe(legacy);
  await page.reload();
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await expect(page.locator('.bigstats').getByText('0', { exact: true }).first()).toBeVisible();
});
