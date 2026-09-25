import { expect, test } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const previewKey = 'polski-grammar-srs-kmp-preview-v1';
type ReviewFixture = {
  id: string;
  context: { nowIso: string; timeZone: string };
  input: { progress: unknown; skillId: string; rating: string; typedCorrect?: boolean };
  expected: unknown;
};
const fixtureFile = JSON.parse(readFileSync(resolve('tests/fixtures/kotlin-parity/progress.json'), 'utf8')) as {
  cases: ReviewFixture[];
};

for (const id of ['P-oral-good', 'P-typed-wrong']) {
  const fixture = fixtureFile.cases.find(entry => entry.id === id)!;
  test(`${id} matches the current React progress fixture after one review`, async ({ browser }) => {
    const context = await browser.newContext({ locale: 'pl-PL', timezoneId: fixture.context.timeZone });
    try {
      const page = await context.newPage();
      if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
        await page.addInitScript(() => {
          const validate = WebAssembly.validate;
          WebAssembly.validate = function (bytes: BufferSource): boolean {
            if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
            return validate.call(WebAssembly, bytes);
          };
        });
      }
      await page.addInitScript(([key, raw]) => {
        if (localStorage.getItem(key) === null) localStorage.setItem(key, raw);
      },
        [previewKey, JSON.stringify(fixture.input.progress)] as const);
      await page.clock.install({ time: new Date(fixture.context.nowIso) });
      await page.goto('/');
      await continueIntroductionIfPresent(page);
      await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
      await page.clock.setFixedTime(new Date(fixture.context.nowIso));
      if (fixture.input.typedCorrect === false) {
        await page.getByRole('button', { name: 'Напечатать ответ' }).click();
        await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('Niepoprawna odpowiedź');
      }
      await page.getByRole('button', { name: /Показать ответ|Проверить и показать ответ/ }).click();
      await page.getByRole('button', { name: /2 Вспомнил/ }).click();
      await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, previewKey)).toBe(1);
      const actual = await page.evaluate(key => JSON.parse(localStorage.getItem(key)!), previewKey);
      expect(actual).toEqual(fixture.expected);
      await page.reload();
      await continueIntroductionIfPresent(page);
      await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
      await page.getByRole('button', { name: 'Прогресс' }).click();
      await expect(page.locator('.bigstats')).toContainText('1');
      expect(await page.evaluate(key => JSON.parse(localStorage.getItem(key)!), previewKey)).toEqual(fixture.expected);
    } finally {
      await context.close();
    }
  });
}
