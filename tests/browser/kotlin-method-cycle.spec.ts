import { expect, test } from '@playwright/test';

const progressKey = 'polski-grammar-srs-kmp-preview-v1';

test('first method cycle keeps the draft and stores exactly one rating', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  await page.setViewportSize({ width: 320, height: 700 });
  await page.goto('/');
  const source = page.locator('.source-sentence');
  await expect(source).toHaveText('To jest moja piękna żona.');
  const method = page.getByRole('combobox', { name: 'Подача объяснений' });
  await expect(page.getByRole('button', { name: 'Перейти к заданию' })).toBeVisible();
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  const reference = page.getByRole('button', { name: 'Таблица под рукой' });
  await expect(reference).toBeDisabled();
  await expect(page.locator('.reference-panel')).toHaveCount(0);
  await method.selectOption('SituationFirst');
  await expect(page.locator('.method-introduce')).toContainText('кого или что видишь');
  await expect(source).toHaveText('To jest moja piękna żona.');
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await expect(reference).toBeEnabled();
  await page.getByRole('button', { name: 'Таблица под рукой' }).click();
  // Emphasis contract §5: before reveal, the reference panel must not leak this exercise's own
  // answer — its target row (Biernik here) stays unhighlighted and its form stays hidden.
  await expect(page.locator('.reference-panel')).not.toContainText('moją piękną żonę');
  await expect(page.locator('.reference-panel .highlight-row')).toHaveCount(0);
  await expect(page.locator('.reference-panel')).toContainText('Стало: ?');
  await page.getByRole('button', { name: 'Скрыть таблицу' }).click();
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  const answer = page.getByRole('textbox', { name: 'Ответ по-польски' });
  await answer.fill('Moja próba');
  await method.selectOption('RuleFirst');
  await expect(answer).toHaveValue('Moja próba');
  await page.getByRole('button', { name: 'Проверить и показать ответ' }).click();
  await expect(page.locator('.answer-sentence')).toHaveText('Widzę moją piękną żonę.');
  await expect(page.locator('.method-feedback h3')).toHaveText('Разбор изменений');
  await method.selectOption('SituationFirst');
  await expect(page.locator('.method-feedback h3')).toHaveText('Сравни смысл и форму');
  await expect(page.locator('.typed-result p')).toHaveText('Moja próba');
  expect(await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, progressKey)).toBe(0);
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(320);
  await page.getByRole('button', { name: 'Таблица под рукой' }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, progressKey)).toBe(1);
  await expect(page.locator('.reference-panel')).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Таблица под рукой' })).toBeDisabled();
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await expect(page.locator('.reference-panel')).toBeVisible();
  await page.reload();
  await expect(page.getByRole('combobox', { name: 'Подача объяснений' })).toHaveValue('SituationFirst');
});
