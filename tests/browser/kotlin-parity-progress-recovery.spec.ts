import { expect, test } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const previewKey = 'polski-grammar-srs-kmp-preview-v1';
const fixtureFile = JSON.parse(readFileSync(resolve('tests/fixtures/kotlin-parity/progress.json'), 'utf8')) as {
  cases: Array<{ id: string; input: { storedRaw?: string } }>;
};

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

for (const id of ['P-load-malformed', 'P-load-unsupported-version']) {
  const raw = fixtureFile.cases.find(entry => entry.id === id)!.input.storedRaw!;
  test(`${id} keeps unreadable bytes available for export`, async ({ page }) => {
    await page.addInitScript(([key, value]) => localStorage.setItem(key, value), [previewKey, raw] as const);
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Нужна копия прогресса' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Показать ответ' })).toHaveCount(0);
    expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).toBe(raw);
    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('button', { name: 'Экспорт JSON' }).click();
    const download = await downloadPromise;
    expect(readFileSync((await download.path())!, 'utf8')).toBe(raw);
    expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).toBe(raw);
  });
}

test('P-storage-read-error does not create a fresh replacement after getItem fails', async ({ page }) => {
  await page.addInitScript(key => {
    const original = Storage.prototype.getItem;
    Storage.prototype.getItem = function (name: string) {
      if (name === key) throw new DOMException('Injected read failure', 'SecurityError');
      return original.call(this, name);
    };
  }, previewKey);
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Хранилище недоступно' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toHaveCount(0);
  expect(await page.evaluate(() => localStorage.length)).toBe(0);
});
