import { expect, test } from '@playwright/test';
import { resolve } from 'node:path';
import { createServer, type ViteDevServer } from 'vite';

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

test('P03 React and Kotlin expose and select all 16 skills', async ({ page, browser }) => {
  test.setTimeout(90_000);
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(`Kotlin: ${error.message}`));
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
    react.on('pageerror', error => errors.push(`React: ${error.message}`));
    await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
    for (const current of [page, react]) {
      await current.getByRole('button', { name: 'Отдельный навык' }).click();
      await expect(current.getByRole('region', { name: 'Выбор навыка' })).toBeVisible();
    }
    const labels = await react.getByRole('region', { name: 'Выбор навыка' }).getByRole('button').allTextContents();
    const kotlinLabels = await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button').allTextContents();
    const normalize = (value: string) => value.replace(/[·\s]+/g, '');
    expect(labels).toHaveLength(16);
    expect(kotlinLabels.map(normalize)).toEqual(labels.map(normalize));

    for (let index = 0; index < labels.length; index += 1) {
      for (const current of [page, react]) {
        const picker = current.getByRole('region', { name: 'Выбор навыка' });
        if (!(await picker.isVisible())) await current.getByRole('button', { name: 'Отдельный навык' }).click();
        await picker.getByRole('button').nth(index).click();
        await expect(current.locator('.card-meta')).toContainText('Тренировка навыка');
      }
      expect(await page.locator('.card-meta span').nth(1).textContent()).toBe(await react.locator('.card-meta span').nth(1).textContent());
    }
    expect(errors).toEqual([]);
  } finally {
    await reactContext.close();
  }
});
