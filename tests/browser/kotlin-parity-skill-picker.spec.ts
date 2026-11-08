import { expect, test } from '@playwright/test';

// Pinned names from the accepted 16-skill course inventory, not from the live DOM.
const skillTitles = [
  'Biernik · средний род', 'Biernik · żeński', 'Biernik · męski',
  'Dopełniacz · negacja', 'Narzędnik · z / być', 'Miejscownik · o',
  'Celownik · komu?', 'Мой, твой, его, наш, их', 'Czas teraźniejszy',
  'Czas przeszły', 'Czas przyszły', 'Aspekt · robić/zrobić',
  'Zaimki osobowe', 'Смешанные трансформации', 'Вопрос с Czy',
  'Единственное → множественное',
];

test('P03 Kotlin exposes and selects all 16 pinned skills', async ({ page }) => {
  test.setTimeout(90_000);
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/');
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  const picker = page.getByRole('region', { name: 'Выбор навыка' });
  await expect(picker.getByRole('button')).toHaveCount(skillTitles.length);
  for (const [index, title] of skillTitles.entries()) {
    if (!(await picker.isVisible())) await page.getByRole('button', { name: 'Отдельный навык' }).click();
    await expect(picker.getByRole('button').nth(index)).toContainText(title);
    await picker.getByRole('button').nth(index).click();
    await expect(page.locator('.card-meta')).toContainText('Тренировка навыка');
    await expect(page.locator('.card-meta')).toContainText(title);
  }
  expect(errors).toEqual([]);
});
