import { expect, test } from '@playwright/test';

// M7 blueprint: a pilot skill's first exposure shows an authored short question ("Что скажешь?
// Дальше") before the skill's own introduction ("Перейти к заданию") — only once per skill, the
// same moment `introPending` already gates. `case.acc.m` is the chain's seed-1 (husband/good)
// skill and carries the brief's own example question.
test('opening case.acc.m for the first time on a fresh profile shows its authored prediction question before the introduction', async ({ page }) => {
  await page.goto('/');
  await page.locator('#training-seed').selectOption('1');
  await expect(page.locator('.card-meta')).toContainText('Biernik · męski');
  await expect(page.locator('.source-sentence')).toBeVisible();

  const predictButton = page.getByRole('button', { name: 'Что скажешь? Дальше' });
  await expect(predictButton).toBeVisible();
  await expect(page.getByText('Почему Widzę dom, но Widzę psa?')).toBeVisible();
  // The existing introduction/answer surfaces are not shown yet.
  await expect(page.getByRole('button', { name: 'Перейти к заданию' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toHaveCount(0);

  await predictButton.click();
  // Continuing reaches the existing first-exposure flow unchanged.
  await expect(page.getByRole('button', { name: 'Перейти к заданию' })).toBeVisible();
  await expect(page.getByText('Почему Widzę dom, но Widzę psa?')).toHaveCount(0);
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
});

// Regression guard: a skill without an authored predictionQuestion (e.g. the chain's own first
// seed, wife/beautiful -> case.acc.f shares one too, so use a non-pilot skill via the picker)
// skips straight to the introduction exactly as before M7.
test('a skill without a prediction question skips straight to the introduction', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button', { name: /Aspekt/ }).click();
  await expect(page.getByRole('button', { name: 'Что скажешь? Дальше' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Перейти к заданию' })).toBeVisible();
});

// M2 blueprint: an error on a Focused skill enqueues a retry; after RETRY_DELAY (2) unrelated
// rated steps, the "Ещё раз: …" badge appears on a substituted card for that same skill.
test('a Typed error on a pilot skill resurfaces it as a retry-badged card after two unrelated steps', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button', { name: /Biernik · żeński/ }).click();
  await page.getByRole('button', { name: 'Что скажешь? Дальше' }).click();
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('wrong');
  await page.getByRole('button', { name: 'Проверить и показать ответ' }).click();
  await page.getByRole('button', { name: /1 Повторить/ }).click();
  await expect(page.getByText(/Ещё раз:/)).toHaveCount(0);

  // Two unrelated steps on a different skill (Oral mode, no diagnosable error).
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button', { name: /Biernik · męski/ }).click();
  await page.getByRole('button', { name: 'Что скажешь? Дальше' }).click();
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await page.getByRole('button', { name: 'Ответ вслух / про себя' }).click();
  for (let step = 0; step < 2; step += 1) {
    await page.getByRole('button', { name: 'Показать ответ' }).click();
    await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  }
  await expect(page.locator('.card-meta')).toContainText('Biernik · żeński');
  await expect(page.getByText('Ещё раз: Biernik · żeński')).toBeVisible();
});
