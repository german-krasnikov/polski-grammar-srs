import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const progressKey = 'polski-grammar-srs-kmp-preview-v1';

test.beforeEach(async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  await page.goto('/');
});

test('revealed card offers only Repeat and Remembered, with two keyboard ratings', async ({ page }) => {
  await expect(page.locator('.source-sentence .change-before')).toHaveText(['a', 'a', 'a']);
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.locator('.ratings button')).toHaveCount(2);
  await expect(page.getByRole('region', { name: 'Ключевое правило' }).locator('strong').first()).toBeVisible();
  await expect(page.locator('.change-pair strong').first()).toBeVisible();
  await expect(page.locator('.change-pair .ending-highlight')).toHaveText(['ą', 'ą', 'ę']);
  await expect(page.locator('.answer-sentence .change-after')).toHaveText(['ą', 'ą', 'ę']);
  await expect(page.locator('.change-pair .change-before')).toHaveText(['a', 'a', 'a']);
  const endingStyle = await page.locator('.change-pair strong').first().evaluate(element => {
    const ending = element.querySelector('.ending-highlight')!;
    return { base: getComputedStyle(element).color, ending: getComputedStyle(ending).color,
      decoration: getComputedStyle(ending).textDecorationLine };
  });
  expect(endingStyle.ending).not.toBe(endingStyle.base);
  expect(endingStyle.decoration).toContain('underline');
  await expect(page.getByRole('button', { name: /1 Повторить/ })).toBeVisible();
  await expect(page.getByRole('button', { name: /2 Вспомнил/ })).toBeVisible();
  await page.keyboard.press('3');
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(0);
  await page.keyboard.press('2');
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
});

test('switching explanation style preserves the current typed draft', async ({ page }) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('Moja próba');
  await page.getByRole('combobox', { name: 'Подача объяснений' }).selectOption('situations');
  await expect(page.locator('.method-retrieve')).toContainText('Представь ситуацию и скажи целое предложение самостоятельно.');
  await expect(page.getByRole('textbox', { name: 'Ответ по-польски' })).toHaveValue('Moja próba');
  await page.reload();
  await expect(page.getByRole('combobox', { name: 'Подача объяснений' })).toHaveValue('situations');
});

test('wrong typed answer leaves the two scheduling choices to the learner', async ({ page }) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('wrong');
  await page.getByRole('button', { name: 'Проверить и показать ответ' }).click();
  await expect(page.locator('.typed-result')).toContainText('Сравни свой ответ с эталоном');
  await expect(page.locator('.ratings button')).toHaveCount(2);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  const progress = await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}'), progressKey);
  expect(progress.totalReviews).toBe(1);
  expect(progress.stats['case.acc.f'].correct).toBe(0);
});
