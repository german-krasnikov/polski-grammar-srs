import { expect, test, type Page } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const progressKey = 'polski-grammar-srs-kmp-preview-v1';

const revealAnswer = async (page: Page) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
};

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
});

test('reveal auto-flips the card to face the answer, front stays out of the accessibility tree', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(card.locator('.card-flip-inner')).toHaveClass(/flipped/);
  await expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-front')).toHaveAttribute('aria-hidden', 'true');
});

test('tapping the card flips it back to the question, purely visually, then flips again — never a review, never re-hiding the answer data', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  const flip = card.locator('.card-flip');
  await flip.click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.card-front')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-back')).toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.ratings button')).toHaveCount(2); // rating buttons remain available on the visible face's DOM regardless
  await flip.click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.answer-sentence')).toBeVisible();
  const progress = await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}'), progressKey);
  expect(progress.totalReviews ?? 0).toBe(0);
});

test('clicking a rating button rates and does not get swallowed by the flip handler', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
});

test('typed mode: tapping inside the answer field before reveal never flips; checking the answer flips to the back', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  const textbox = page.getByRole('textbox', { name: 'Ответ по-польски' });
  await textbox.click();
  await textbox.fill('proba');
  await expect(page.locator('.card-flip')).toHaveCount(0); // no flip wrapper exists before reveal at all
  await page.getByRole('button', { name: 'Проверить и показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(card.locator('.card-flip-inner')).toHaveClass(/flipped/);
  await expect(card.locator('.typed-result')).toBeVisible();
});

test('a mouse swipe on the revealed back face rates exactly once; a short drag rates nothing and does not flip either', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  const back = page.getByRole('region', { name: 'Учебная карточка' }).locator('.card-back');
  const bounds = await back.boundingBox();
  expect(bounds).not.toBeNull();
  const hint = back.locator('.vocabulary-swipe-zone');
  await hint.scrollIntoViewIfNeeded();
  const hintBounds = await hint.boundingBox();
  expect(hintBounds).not.toBeNull();
  const y = hintBounds!.y + hintBounds!.height / 2;
  // Short drag: below the 75px rating threshold and above the 10px tap threshold — neither fires.
  await page.mouse.move(bounds!.x + bounds!.width / 2, y);
  await page.mouse.down();
  await page.mouse.move(bounds!.x + bounds!.width / 2 + 30, y, { steps: 5 });
  await page.mouse.up();
  await expect(page.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  expect((await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey))).toBe(0);
  // Real swipe: right = Good/"Вспомнил".
  await page.mouse.move(bounds!.x + 20, y);
  await page.mouse.down();
  await page.mouse.move(bounds!.x + Math.min(bounds!.width - 20, 200), y, { steps: 8 });
  await page.mouse.up();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
});

test('reduced motion (app Motion setting) disables the Rive effect overlay entirely on rate', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', motion: 'Reduced' })));
  await page.goto('/');
  await revealAnswer(page);
  await expect(page.locator('html')).toHaveAttribute('data-motion', 'reduced');
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
  await expect(page.locator('#polski-rive-overlay')).toHaveCount(0);
});

test('normal motion plays the Rive effect overlay as a non-interactive, hidden-from-a11y layer on rate', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  const overlay = page.locator('#polski-rive-overlay');
  await expect(overlay).toHaveAttribute('aria-hidden', 'true');
  await expect(overlay).toHaveCSS('pointer-events', 'none');
  await expect(overlay).toHaveAttribute('data-rive-effect', /^remembered:/);
});
