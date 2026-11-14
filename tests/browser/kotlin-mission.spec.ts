import { expect, test } from '@playwright/test';

// M9 blueprint: the "book-shop" pilot mission (книжный магазин) end to end — one "wrong form,
// right meaning" choice along the way, asserting both signals render distinctly, then reaching
// the goal_reached ending and persisting MissionDocument.

test('M9: playing the book-shop mission to its goal, with meaning/form shown independently', async ({ page }) => {
  await page.goto('/#/mission');
  await expect(page.getByRole('heading', { name: 'Мини-миссии' })).toBeVisible();
  const card = page.locator('.mission-card').filter({ hasText: 'В книжном магазине' });
  await expect(card).toBeVisible();
  await card.getByRole('button', { name: 'Начать' }).click();

  await expect(page.locator('.mission-situation')).toContainText('księgarni');
  await page.getByRole('button', { name: 'Спросить про книгу' }).click();
  // Right meaning, deliberately misspelled form.
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('losowy zly tekst');
  await page.getByRole('button', { name: 'Проверить' }).click();

  await expect(page.locator('.mission-result-meaning')).toHaveClass(/correct/);
  await expect(page.locator('.mission-result-meaning')).toContainText('приближает к цели');
  await expect(page.locator('.mission-result-form')).toHaveClass(/incorrect/);
  await expect(page.locator('.mission-result-form')).toContainText('неверно');

  // Second decision, correct meaning and form this time, reaching the goal.
  await page.getByRole('button', { name: 'Спросить про другую книгу' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('Czy możecie polecić inną książkę?');
  await page.getByRole('button', { name: 'Проверить' }).click();

  await expect(page.getByRole('heading', { name: 'Цель достигнута' })).toBeVisible();
  await expect(page.locator('.mission-outcome')).toContainText('Ты добрался до цели.');
  await page.getByRole('button', { name: 'Готово' }).click();

  await expect(page.getByRole('heading', { name: 'Мини-миссии' })).toBeVisible();
  const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-missions-pl-ru-v1')!));
  expect(saved.progress['book-shop'].lastOutcome).toBe('goal_reached');
});

test('M9: a dead-end path shows a distinct, non-success ending', async ({ page }) => {
  await page.goto('/#/mission');
  await page.locator('.mission-card').filter({ hasText: 'В книжном магазине' }).getByRole('button', { name: 'Начать' }).click();
  await page.getByRole('button', { name: 'Промолчать и уйти' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('Nie, dziękuję.');
  await page.getByRole('button', { name: 'Проверить' }).click();

  await expect(page.getByRole('heading', { name: 'Миссия окончена' })).toBeVisible();
  await expect(page.locator('.mission-outcome')).toContainText('Не в этот раз');
});
