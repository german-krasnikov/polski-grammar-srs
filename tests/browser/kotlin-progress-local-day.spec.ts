import { expect, test, type Browser, type Page } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const previewKey = 'polski-grammar-srs-kmp-preview-v1';
const fixtures = JSON.parse(readFileSync(resolve('tests/fixtures/kotlin-parity/progress.json'), 'utf8')) as {
  cases: Array<{
    id: string;
    input: { instant?: string; zone?: string; progress?: unknown; firstNowIso?: string; secondNowIso?: string };
    expected: string | { first: { lastDay: string; reviewsToday: number; totalReviews: number }; second: { lastDay: string; reviewsToday: number; totalReviews: number } };
  }>;
};

async function openAt(browser: Browser, instant: string, zone: string, progress?: unknown): Promise<Page> {
  const context = await browser.newContext({ locale: 'pl-PL', timezoneId: zone });
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
  if (progress !== undefined) {
    await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [previewKey, JSON.stringify(progress)] as const);
  }
  await page.clock.install({ time: new Date(instant) });
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
  await page.clock.setFixedTime(new Date(instant));
  return page;
}

async function rateCurrentCard(page: Page): Promise<void> {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
}

async function storedProgress(page: Page): Promise<{ lastDay: string; reviewsToday: number; totalReviews: number }> {
  await expect.poll(() => page.evaluate(key => localStorage.getItem(key), previewKey)).not.toBeNull();
  return page.evaluate(key => JSON.parse(localStorage.getItem(key)!), previewKey);
}

for (const fixture of fixtures.cases.filter(entry => entry.id.startsWith('P-day-'))) {
  test(`${fixture.id} records the browser's local day`, async ({ browser }) => {
    const page = await openAt(browser, fixture.input.instant!, fixture.input.zone!);
    try {
      await rateCurrentCard(page);
      const progress = await storedProgress(page);
      expect(progress.lastDay).toBe(fixture.expected);
      expect(progress.reviewsToday).toBe(1);
      expect(progress.totalReviews).toBe(1);
    } finally {
      await page.context().close();
    }
  });
}

for (const fixture of fixtures.cases.filter(entry => entry.id.startsWith('P-review-midnight-'))) {
  test(`${fixture.id} resets only the daily count after midnight`, async ({ browser }) => {
    const page = await openAt(browser, fixture.input.firstNowIso!, 'Europe/Warsaw', fixture.input.progress);
    const expected = fixture.expected as Exclude<typeof fixture.expected, string>;
    try {
      await rateCurrentCard(page);
      await expect.poll(async () => (await storedProgress(page)).totalReviews).toBe(expected.first.totalReviews);
      expect(await storedProgress(page)).toMatchObject(expected.first);

      await page.clock.setFixedTime(new Date(fixture.input.secondNowIso!));
      await rateCurrentCard(page);
      await expect.poll(async () => (await storedProgress(page)).totalReviews).toBe(expected.second.totalReviews);
      const second = await storedProgress(page);
      expect(second.lastDay).toBe(expected.second.lastDay);
      expect(second.reviewsToday).toBe(expected.second.reviewsToday);
      expect(second.totalReviews).toBe(expected.second.totalReviews);
    } finally {
      await page.context().close();
    }
  });
}
