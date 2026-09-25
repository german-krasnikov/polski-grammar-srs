import { expect, test } from '@playwright/test';
import { resolve } from 'node:path';
import { createServer, type ViteDevServer } from 'vite';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const kotlinKey = 'polski-grammar-srs-kmp-preview-v1';
const reactKey = 'polski-grammar-srs-v1';
const at = new Date('2026-09-23T12:00:00.000Z');
let reactServer: ViteDevServer;
let reactBaseUrl: string;

test.beforeAll(async () => {
  reactServer = await createServer({ configFile: resolve('vite.config.ts'), server: { host: '127.0.0.1', port: 0 } });
  await reactServer.listen();
  reactBaseUrl = reactServer.resolvedUrls!.local[0];
});

test.afterAll(async () => {
  await reactServer?.close();
});

for (const label of ['Повторить', 'Вспомнил']) {
  test(`P05 React and Kotlin persist the same first oral ${label} review`, async ({ page, browser }) => {
    if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
      await page.addInitScript(() => {
        const validate = WebAssembly.validate;
        WebAssembly.validate = function (bytes: BufferSource): boolean {
          if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
          return validate.call(WebAssembly, bytes);
        };
      });
    }
    const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
    try {
      const react = await reactContext.newPage();
      await Promise.all([page.clock.install({ time: at }), react.clock.install({ time: at })]);
      await Promise.all([page.clock.setFixedTime(at), react.clock.setFixedTime(at)]);
      await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
      for (const current of [page, react]) {
        await continueIntroductionIfPresent(current);
        await current.getByRole('button', { name: /Показать ответ/ }).click();
        await current.getByRole('button', { name: new RegExp(label) }).click();
      }
      await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, kotlinKey)).toBe(1);
      await expect.poll(() => react.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, reactKey)).toBe(1);
      const kotlinProgress = await page.evaluate(key => JSON.parse(localStorage.getItem(key)!), kotlinKey);
      const reactProgress = await react.evaluate(key => JSON.parse(localStorage.getItem(key)!), reactKey);
      expect(kotlinProgress).toEqual(reactProgress);
    } finally {
      await reactContext.close();
    }
  });
}

test('P05 a second rating shortcut cannot review the next hidden card', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.keyboard.press('2');
  await page.keyboard.press('2');
  await expect(page.locator('.card-meta')).toContainText('Цепочка · 2 / 5');
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, kotlinKey)).toBe(1);
});
