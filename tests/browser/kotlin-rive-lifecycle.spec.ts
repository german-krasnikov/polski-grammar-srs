import { expect, test, type Page } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

async function rateTrainingCard(page: Page): Promise<void> {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
}

test('P16 rating overlay is cleared when the learner leaves the card for Matrix', async ({ page }) => {
  await rateTrainingCard(page);
  await expect(page.locator('#polski-rive-overlay')).toBeVisible();
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  await expect(page.locator('.matrix-page')).toBeVisible();
  // Inspect the new route immediately; waiting for the old animation to expire
  // would conceal the stale overlay this scenario reproduces.
  expect(await page.locator('#polski-rive-overlay').isVisible()).toBe(false);
});

test('P16 failed effect metadata loading cannot leave a permanent rating overlay', async ({ page }) => {
  await page.route('**/rive/rive-effects.generated.js', route => route.abort('failed'));
  await rateTrainingCard(page);
  // A network error must clean up independently of Rive's completion callback.
  await expect(page.locator('#polski-rive-overlay')).toBeHidden({ timeout: 4_000 });
});
