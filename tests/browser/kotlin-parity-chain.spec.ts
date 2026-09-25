import { expect, test } from '@playwright/test';
import type { Page } from '@playwright/test';
import { resolve } from 'node:path';
import { createServer, type ViteDevServer } from 'vite';
import { continueIntroductionIfPresent } from './kotlin-introduction';

let reactServer: ViteDevServer;
let reactBaseUrl: string;

async function revealGrammarAnswer(page: Page): Promise<void> {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: /Показать ответ/ }).click();
}

test.beforeAll(async () => {
  reactServer = await createServer({ configFile: resolve('vite.config.ts'), server: { host: '127.0.0.1', port: 0 } });
  await reactServer.listen();
  reactBaseUrl = reactServer.resolvedUrls!.local[0];
});

test.afterAll(async () => {
  await reactServer?.close();
});

const normalized = (value: string | null) => (value ?? '').replace(/\s+/g, ' ').trim();

test('P02 React and Kotlin show the same 12 five-step chains', async ({ page, browser }) => {
  test.setTimeout(180_000);
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
    for (const current of [page, react]) await current.getByRole('button', { name: 'Цепочка предложений' }).click();
    for (const current of [page, react]) await expect(current.getByRole('combobox', { name: 'Слова для цепочки' }).locator('option')).toHaveCount(12);

    for (let seed = 0; seed < 12; seed += 1) {
      for (const current of [page, react]) await current.getByRole('combobox', { name: 'Слова для цепочки' }).selectOption(String(seed));
      let previousExpected: string | null = null;
      for (let step = 1; step <= 5; step += 1) {
        if (previousExpected !== null) {
          for (const current of [page, react]) {
            await expect(current.locator('.source-sentence')).toHaveText(previousExpected);
          }
        }
        if (seed === 0) {
          for (const current of [page, react]) {
            const steps = current.getByRole('list', { name: 'Шаги цепочки' }).locator('li');
            await expect(steps).toHaveCount(5);
            const labels = ['Вижу', 'Прошлое', 'Отрицание', 'Владелец', 'Говорю о'];
            for (const [index, text] of (await steps.allTextContents()).entries()) {
              expect(normalized(text)).toMatch(new RegExp(`^${index + 1} ?${labels[index]}$`));
            }
            for (let index = 0; index < 5; index += 1) {
              await expect(steps.nth(index)).toHaveAttribute('class', index < step - 1 ? 'done' : index === step - 1 ? 'current' : '');
              if (index === step - 1) await expect(steps.nth(index)).toHaveAttribute('aria-current', 'step');
              else await expect(steps.nth(index)).not.toHaveAttribute('aria-current', 'step');
            }
          }
        }
        for (const current of [page, react]) await continueIntroductionIfPresent(current);
        for (const selector of ['.card-meta', '.source-sentence', '.operation h2']) {
          expect(normalized(await page.locator(selector).textContent())).toBe(normalized(await react.locator(selector).textContent()));
        }
        for (const current of [page, react]) await revealGrammarAnswer(current);
        for (const selector of ['.answer-sentence', '.accepted', '.change-list']) {
          expect((await page.locator(selector).allTextContents()).map(normalized)).toEqual(
            (await react.locator(selector).allTextContents()).map(normalized),
          );
        }
        previousExpected = normalized(await page.locator('.answer-sentence').textContent());
        for (const current of [page, react]) await current.getByRole('button', { name: /2 Вспомнил/ }).click();
        if (seed === 0) {
          for (const [current, key] of [[page, 'polski-grammar-srs-kmp-preview-v1'], [react, 'polski-grammar-srs-v1']] as const) {
            await expect.poll(() => current.evaluate(storageKey =>
              JSON.parse(localStorage.getItem(storageKey) ?? '{}').totalReviews, key)).toBe(step);
          }
        }
      }
      for (const current of [page, react]) await expect(current.getByRole('heading', { name: 'Цепочка завершена' })).toBeVisible();
      expect(normalized(await page.locator('.chain-review').textContent())).toBe(normalized(await react.locator('.chain-review').textContent()));
      if (seed === 0) {
        await expect(react.locator('.session-complete .eyebrow')).toHaveText('5 преобразований');
        await expect(react.locator('.session-complete > p')).toHaveText(
          'Ты изменил время, отрицание, владельца и падеж, сохранив одну мысль. Оценки сохранены в расписании повторений.',
        );
        await expect(page.locator('.session-complete .eyebrow')).toHaveCount(0);
        await expect(page.locator('.session-complete > p')).toHaveText(
          'Пять преобразований завершены. Оценки сохранены в расписании повторений.',
        );
        for (const current of [page, react]) await expect(current.locator('.chain-review > div')).toHaveCount(5);
      }
      if (seed === 11) {
        for (const [current, key] of [[page, 'polski-grammar-srs-kmp-preview-v1'], [react, 'polski-grammar-srs-v1']] as const) {
          await expect.poll(() => current.evaluate(storageKey =>
            JSON.parse(localStorage.getItem(storageKey) ?? '{}').totalReviews, key)).toBe(60);
          await current.reload();
          await current.getByRole('button', { name: 'Прогресс' }).click();
          await expect(current.locator('.bigstats')).toContainText('60');
        }
      }
    }
    expect(errors).toEqual([]);
  } finally {
    await reactContext.close();
  }
});

test('P10 React and Kotlin show the same two visible rating intervals', async ({ page, browser }) => {
  const at = new Date('2026-09-23T12:00:00.000Z');
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
    for (const current of [page, react]) await revealGrammarAnswer(current);
    const intervals = page.locator('.ratings button span');
    const reactIntervals = react.locator('.ratings button span');
    await expect(intervals).toHaveCount(2);
    await expect(reactIntervals).toHaveCount(2);
    const kotlinValues = (await intervals.allTextContents()).map(normalized);
    expect(kotlinValues.every(Boolean)).toBe(true);
    expect(kotlinValues).toEqual((await reactIntervals.allTextContents()).map(normalized));
  } finally {
    await reactContext.close();
  }
});
