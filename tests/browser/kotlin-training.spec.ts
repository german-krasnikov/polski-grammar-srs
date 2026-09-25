import { expect, test, type Page } from '@playwright/test';

const continueIntroductionIfPresent = async (page: Page) => {
  await page.getByRole('button', { name: /^(Перейти к заданию|Показать ответ)$/ }).first().waitFor();
  const next = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await next.count()) await next.click();
};
const revealGrammarAnswer = async (page: Page) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
};

const expectedBranch = process.env.KOTLIN_SPIKE_BRANCH;
const previewKey = 'polski-grammar-srs-kmp-preview-v1';
const errors = new WeakMap<Page, string[]>();

test.beforeEach(async ({ page }) => {
  const failures: string[] = [];
  errors.set(page, failures);
  page.on('pageerror', error => failures.push(error.message));
  if (expectedBranch === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
});

test.afterEach(async ({ page }) => {
  expect(errors.get(page)).toEqual([]);
});

test('reveals and rates all five linked cards, then advances the seed', async ({ page }) => {
  const front = page.locator('.source-sentence');
  for (let step = 1; step <= 5; step += 1) {
    await expect(page.locator('.card-meta')).toContainText(`Цепочка · ${step} / 5`);
    await expect(page.locator('.answer-sentence')).toHaveCount(0);
    await expect(page.locator('.rule-contrast')).toHaveCount(0);
    await revealGrammarAnswer(page);
    await expect(page.locator('.rule-contrast .form-contrast')).toBeVisible();
    const answer = await page.locator('.answer-sentence').textContent();
    await page.getByRole('button', { name: /2 Вспомнил/ }).click();
    if (step < 5) await expect(front).toHaveText(answer ?? '');
  }
  await expect(page.getByRole('heading', { name: 'Цепочка завершена' })).toBeVisible();
  await expect(page.locator('.chain-review > div')).toHaveCount(5);
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, previewKey)).toBe(5);
  await page.getByRole('button', { name: 'Следующий набор слов' }).click();
  await expect(page.locator('#training-seed')).toHaveValue('1');
  await expect(page.locator('.card-meta')).toContainText('Цепочка · 1 / 5');
});

test('typed answer stays focused while editing and freezes at reveal', async ({ page }) => {
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  const answer = page.getByRole('textbox', { name: 'Ответ по-польски' });
  await answer.fill('Widzę moją piękną żonę.');
  await expect(answer).toBeFocused();
  await expect(answer).toHaveValue('Widzę moją piękną żonę.');
  await page.keyboard.press('Shift+Enter');
  await expect(answer).toHaveValue('Widzę moją piękną żonę.\n');
  await page.keyboard.press('Backspace');
  await page.keyboard.press('Enter');
  await expect(page.locator('.typed-result')).toContainText('Совпадает с правильным вариантом');
  await expect(page.locator('.typed-result')).toContainText('Widzę moją piękną żonę.');
  await page.getByRole('button', { name: /1 Повторить/ }).click();
  const progress = await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}'), previewKey);
  expect(progress.totalReviews).toBe(1);
  expect(progress.stats['case.acc.f'].correct).toBe(1);
});

test('a periodic time refresh does not disturb caret or selection while typing', async ({ page }) => {
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  const answer = page.getByRole('textbox', { name: 'Ответ по-польски' });
  await answer.fill('Widzę moją piękną żonę.');
  await answer.evaluate(node => (node as HTMLTextAreaElement).setSelectionRange(6, 10));
  // RefreshTime is also dispatched on window focus (and document visibilitychange, and the
  // 30s timer); a synthetic focus event exercises the same rebuild path without a real wait.
  await page.evaluate(() => window.dispatchEvent(new Event('focus')));
  await expect(answer).toBeFocused();
  await expect(answer).toHaveValue('Widzę moją piękną żonę.');
  const selection = await answer.evaluate(node => {
    const textarea = node as HTMLTextAreaElement;
    return [textarea.selectionStart, textarea.selectionEnd];
  });
  expect(selection).toEqual([6, 10]);
});

test('keyboard shortcuts avoid buttons and place focus on the next reveal', async ({ page }) => {
  await page.locator('.source-sentence').click();
  await page.keyboard.press('Space');
  await expect(page.locator('.answer-sentence')).toBeVisible();
  await page.keyboard.press('2');
  await continueIntroductionIfPresent(page);
  const reveal = page.getByRole('button', { name: 'Показать ответ' });
  await expect(reveal).toBeVisible();
  await expect(reveal).toBeFocused();
  await reveal.press('Enter');
  await expect(page.locator('.answer-sentence')).toBeVisible();
  await page.getByRole('button', { name: /2 Вспомнил/ }).focus();
  await page.keyboard.press('Space');
  await expect(page.locator('.card-meta')).toContainText('Цепочка · 3 / 5');
});

test('reload restores saved progress and starts a new ephemeral chain', async ({ page }) => {
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, previewKey)).toBe(1);
  await page.reload();
  await expect(page.locator('.card-meta')).toContainText('Цепочка · 1 / 5');
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await expect(page.locator('.bigstats')).toContainText('1');
});

test('reflows at 320 px and keeps rating actions usable with reduced motion', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.evaluate(() => { document.documentElement.style.fontSize = '20px'; });
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.getByRole('button', { name: /2 Вспомнил/ })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(320);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.locator('.card-meta')).toContainText('Цепочка · 2 / 5');
});

test('schedule and focused picker use semantic controls', async ({ page }) => {
  await page.getByRole('button', { name: /По расписанию/ }).click();
  await expect(page.locator('.card-meta')).toContainText('Повторение по расписанию');
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await expect(page.getByRole('region', { name: 'Выбор навыка' })).toBeVisible();
  await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button', { name: /Aspekt/ }).click();
  await expect(page.locator('.card-meta')).toContainText('Тренировка навыка');
  await expect(page.locator('.accepted')).toHaveCount(0);
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.locator('.accepted')).toContainText('Wczoraj kupiłam');
});
