import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';
import { progressAsLegacyShape } from './polski-text';

const kotlinKey = 'polski-grammar-srs-kmp-preview-v1';
const at = new Date('2026-09-23T12:00:00.000Z');

for (const rating of [
  { label: 'Повторить', due: '2026-09-23T12:01:00.000Z', correct: 0, mistakes: 1 },
  { label: 'Вспомнил', due: '2026-09-23T12:10:00.000Z', correct: 1, mistakes: 0 },
]) {
  test(`P05 Kotlin persists the pinned first oral ${rating.label} review`, async ({ page }) => {
    await page.clock.install({ time: at });
    await page.clock.setFixedTime(at);
    await page.goto('/');
    await continueIntroductionIfPresent(page);
    await page.getByRole('button', { name: /Показать ответ/ }).click();
    await page.getByRole('button', { name: new RegExp(rating.label) }).click();

    await expect.poll(async () => progressAsLegacyShape((await page.evaluate(key => localStorage.getItem(key), kotlinKey))!).totalReviews).toBe(1);
    const progress = progressAsLegacyShape((await page.evaluate(key => localStorage.getItem(key), kotlinKey))!);
    expect(progress.reviewsToday).toBe(1);
    expect(progress.lastDay).toBe('2026-09-23');
    const card = (progress.cards as Array<{ skillId: string; card: Record<string, unknown> }>).find(entry => entry.skillId === 'case.acc.f')!;
    expect(card.card).toMatchObject({ due: rating.due, reps: 1, last_review: at.toISOString() });
    expect((progress.stats as Record<string, unknown>)['case.acc.f']).toMatchObject({
      reviews: 1, correct: rating.correct, mistakes: rating.mistakes,
    });
  });
}

test('P05 a second rating shortcut cannot review the next hidden card', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.keyboard.press('2');
  await page.keyboard.press('2');
  await expect(page.locator('.card-meta')).toContainText('Цепочка · 2 / 5');
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  await expect.poll(async () => progressAsLegacyShape((await page.evaluate(key => localStorage.getItem(key), kotlinKey))!).totalReviews).toBe(1);
});
