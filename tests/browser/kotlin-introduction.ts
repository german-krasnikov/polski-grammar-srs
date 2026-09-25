import type { Page } from '@playwright/test';

export async function continueIntroductionIfPresent(page: Page): Promise<void> {
  await page.getByRole('button', { name: /^(Перейти к заданию|Показать ответ|Проверить и показать ответ)/ }).first().waitFor({ timeout: 5_000 });
  const next = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await next.count()) await next.click();
}
