import type { Page } from '@playwright/test';

/** M7 blueprint: a pilot skill's first exposure shows an authored prediction question ("Что
 *  скажешь? Дальше") before its own introduction ("Перейти к заданию") — dismiss it first, same
 *  "if present" shape as the introduction continue below, so every existing caller keeps working
 *  unchanged whether or not the current skill has a predictionQuestion. */
export async function continuePredictionIfPresent(page: Page): Promise<void> {
  await page.getByRole('button', { name: /^(Что скажешь\? Дальше|Перейти к заданию|Показать ответ|Проверить и показать ответ)/ }).first().waitFor({ timeout: 5_000 });
  const next = page.getByRole('button', { name: 'Что скажешь? Дальше' });
  if (await next.count()) await next.click();
}

export async function continueIntroductionIfPresent(page: Page): Promise<void> {
  await continuePredictionIfPresent(page);
  await page.getByRole('button', { name: /^(Перейти к заданию|Показать ответ|Проверить и показать ответ)/ }).first().waitFor({ timeout: 5_000 });
  const next = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await next.count()) await next.click();
}
