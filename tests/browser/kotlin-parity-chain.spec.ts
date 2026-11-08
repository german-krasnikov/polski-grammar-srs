import { expect, test, type Page } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { continueIntroductionIfPresent } from './kotlin-introduction';
import { installProgressReader } from './kotlin-storage';

type ChainCase = {
  id: string;
  expected: {
    source: string;
    prompt: string;
    expected: string;
    changes: Array<{ from: string; to: string; reason: string }>;
  };
};
const frozenCases = (JSON.parse(readFileSync(resolve('tests/fixtures/kotlin-parity/exercises.json'), 'utf8')) as {
  cases: ChainCase[];
}).cases;
const caseAt = (seed: number, step: number) => frozenCases.find(entry =>
  entry.id === `C-${String(seed + 1).padStart(2, '0')}-${String(step).padStart(2, '0')}`)!;
const normalized = (value: string | null) => (value ?? '').replace(/\s+/g, ' ').trim();

async function revealGrammarAnswer(page: Page): Promise<void> {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: /Показать ответ/ }).click();
}

test('P02 Kotlin shows all 12 five-step chains against frozen exercise cases', async ({ page }) => {
  test.setTimeout(180_000);
  await installProgressReader(page);
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/');
  await page.getByRole('button', { name: 'Цепочка предложений' }).click();
  await expect(page.getByRole('combobox', { name: 'Слова для цепочки' }).locator('option')).toHaveCount(12);

  for (let seed = 0; seed < 12; seed += 1) {
    await page.getByRole('combobox', { name: 'Слова для цепочки' }).selectOption(String(seed));
    for (let step = 1; step <= 5; step += 1) {
      const expected = caseAt(seed, step).expected;
      if (seed === 0) {
        const steps = page.getByRole('list', { name: 'Шаги цепочки' }).locator('li');
        await expect(steps).toHaveCount(5);
        expect((await steps.allTextContents()).map(normalized)).toEqual([
          '1 Вижу', '2 Прошлое', '3 Отрицание', '4 Владелец', '5 Говорю о',
        ]);
        for (let index = 0; index < 5; index += 1) {
          await expect(steps.nth(index)).toHaveAttribute('class', index < step - 1 ? 'done' : index === step - 1 ? 'current' : '');
          if (index === step - 1) await expect(steps.nth(index)).toHaveAttribute('aria-current', 'step');
          else await expect(steps.nth(index)).not.toHaveAttribute('aria-current', 'step');
        }
      }
      await continueIntroductionIfPresent(page);
      expect(normalized(await page.locator('.source-sentence').textContent())).toBe(expected.source);
      await expect(page.locator('.operation h2')).toHaveText(expected.prompt);
      await revealGrammarAnswer(page);
      expect(normalized(await page.locator('.answer-sentence').textContent())).toBe(expected.expected);
      await expect(page.locator('.accepted')).toHaveCount(0);
      const changes = page.locator('.change-list .change-pair');
      await expect(changes).toHaveCount(expected.changes.length);
      for (const [index, change] of expected.changes.entries()) {
        expect(normalized(await changes.nth(index).locator(':scope > span').textContent())).toBe(change.from);
        expect(normalized(await changes.nth(index).locator(':scope > strong').textContent())).toBe(change.to);
        expect(normalized(await changes.nth(index).locator('..').locator('p').textContent())).toBe(change.reason);
      }
      await page.getByRole('button', { name: /2 Вспомнил/ }).click();
      if (seed === 0) {
        await expect.poll(() => page.evaluate(() => (window as any).__readProgress('polski-grammar-srs-kmp-preview-v1', 'totalReviews'))).toBe(step);
      }
    }
    await expect(page.getByRole('heading', { name: 'Цепочка завершена' })).toBeVisible();
    await expect(page.locator('.chain-review > div')).toHaveCount(5);
    for (let step = 1; step <= 5; step += 1) {
      await expect(page.locator('.chain-review > div').nth(step - 1)).toContainText(caseAt(seed, step).expected.expected);
    }
  }
  await expect.poll(() => page.evaluate(() => (window as any).__readProgress('polski-grammar-srs-kmp-preview-v1', 'totalReviews'))).toBe(60);
  await page.reload();
  await page.getByRole('button', { name: 'Прогресс', exact: true }).click();
  await expect(page.locator('.bigstats')).toContainText('60');
  expect(errors).toEqual([]);
});

test('P10 Kotlin shows the pinned first-review rating intervals', async ({ page }) => {
  const at = new Date('2026-09-23T12:00:00.000Z');
  await page.clock.install({ time: at });
  await page.clock.setFixedTime(at);
  await page.goto('/');
  await revealGrammarAnswer(page);
  const intervals = page.locator('.ratings button span');
  await expect(intervals).toHaveCount(2);
  expect((await intervals.allTextContents()).map(normalized)).toEqual(['1 мин', '10 мин']);
});
