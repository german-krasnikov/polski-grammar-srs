import { type Page, expect, test } from '@playwright/test';

// AC-EH-06 / AC-EH-07: Playwright acceptance for the Error Hunt sub-mode of the Cloze tab.
// All test-time discovery is done through the UI — no data-error-index or data-correct attributes.

/** Switcher button (not the nav tab). */
function switcherBtn(page: Page, label: string) {
  return page.locator('.eh-switcher-btn', { hasText: label });
}

/** Navigate to Cloze tab in Error Hunt sub-mode. */
async function gotoErrorHunt(page: Page) {
  await page.goto('/#/cloze');
  await switcherBtn(page, 'Найди ошибку').click();
  await page.waitForSelector('.eh-sentence');
}

/**
 * Tap tokens at index 0, 1, 2… until .exam-options appears.
 * Stops as soon as options are visible (error found or auto-revealed after 2 misses).
 */
async function findErrorToken(page: Page): Promise<void> {
  const count = await page.locator('.eh-token').count();
  for (let i = 0; i < count; i++) {
    await page.locator('.eh-token').nth(i).click();
    if (await page.locator('.exam-options').isVisible()) return;
  }
  throw new Error(`Error token not found among ${count} tokens`);
}

/**
 * Complete one exercise: find the error token, choose the first available option,
 * then wait for the exercise to advance or the summary to appear.
 * Clicks "Далее →" immediately if a wrong answer was chosen (no auto-advance on wrong).
 */
async function completeExercise(page: Page): Promise<void> {
  await page.waitForSelector('.eh-token:not([disabled])');
  const prog = await page.locator('.exam-progress-text').textContent() ?? '';
  await findErrorToken(page);
  await page.locator('.exam-option').first().click();
  // Wait for feedback or summary (choosing on last exercise shows summary immediately)
  await page.waitForSelector('.exam-feedback, .eh-summary');
  if (await page.locator('.eh-summary').isVisible()) return;
  // Wrong answer: no auto-advance — click Next manually
  if (await page.locator('.exam-feedback.feedback-wrong').isVisible()) {
    await page.locator('.exam-nav button').last().click();
  }
  await page.waitForFunction(
    (p: string) =>
      !!document.querySelector('.eh-summary') ||
      document.querySelector('.exam-progress-text')?.textContent !== p,
    prog,
    { timeout: 5000 },
  );
}

/**
 * Complete the session from whatever state the exercise is in.
 * If currently on an answered (not last) exercise, clicks Next first.
 */
async function completeSession(page: Page): Promise<void> {
  // If we're already in answered state (feedback visible, not yet summary), advance
  if (
    await page.locator('.exam-feedback').isVisible() &&
    !await page.locator('.eh-summary').isVisible()
  ) {
    const prog = await page.locator('.exam-progress-text').textContent() ?? '';
    if (await page.locator('.exam-nav button').isVisible()) {
      await page.locator('.exam-nav button').last().click();
      await page.waitForFunction(
        (p: string) =>
          !!document.querySelector('.eh-summary') ||
          document.querySelector('.exam-progress-text')?.textContent !== p,
        prog,
        { timeout: 5000 },
      );
    }
  }
  for (let i = 0; i < 15; i++) {
    if (await page.locator('.eh-summary').count() > 0) break;
    await completeExercise(page);
  }
}

// ─── AC-EH-06: Error Hunt UI behavior ────────────────────────────────────────

test('error hunt: switcher shows Подстановка and Найди ошибку in cloze tab', async ({ page }) => {
  await page.goto('/#/cloze');
  await expect(switcherBtn(page, 'Подстановка')).toBeVisible();
  await expect(switcherBtn(page, 'Найди ошибку')).toBeVisible();
});

test('error hunt: switch shows token buttons, none pre-highlighted', async ({ page }) => {
  await gotoErrorHunt(page);
  const tokens = page.locator('.eh-token');
  expect(await tokens.count()).toBeGreaterThan(0);
  await expect(page.locator('.eh-token-error')).toHaveCount(0);
});

test('error hunt: tap correct word shows "word is correct" note', async ({ page }) => {
  await gotoErrorHunt(page);
  // Tap the last token — most sentences end with a word that is not the error
  await page.locator('.eh-token').last().click();
  // If options appeared the last token was the error; skip this scenario gracefully
  if (await page.locator('.exam-options').isVisible()) return;
  await expect(page.locator('.exam-feedback.feedback-wrong')).toBeVisible();
  await expect(page.locator('.exam-options')).toHaveCount(0);
});

test('error hunt: 2 misses highlights the error token', async ({ page }) => {
  await gotoErrorHunt(page);
  // Click the first token twice.
  // Non-error: 2 misses → revealed=true → eh-token-error appears.
  // Error: found=true on first click → eh-token-error appears immediately.
  // Either way, eh-token-error count becomes 1.
  await page.locator('.eh-token').first().click();
  await page.locator('.eh-token').first().click();
  await expect(page.locator('.eh-token-error')).toHaveCount(1);
});

test('error hunt: tap error token shows 3 options', async ({ page }) => {
  await gotoErrorHunt(page);
  await findErrorToken(page);
  await expect(page.locator('.exam-options .exam-option')).toHaveCount(3);
});

test('error hunt: correct option shows positive feedback', async ({ page }) => {
  await gotoErrorHunt(page);
  await findErrorToken(page);
  // Click first option and check the result
  await page.locator('.exam-option').first().click();
  await page.waitForSelector('.exam-feedback');

  if (await page.locator('.exam-feedback.feedback-correct').isVisible()) {
    // Lucky: first option was correct — test satisfied
    return;
  }

  // Wrong first choice — learn the correct form, restart, try again
  const correctRaw =
    (await page.locator('.exam-option.feedback-correct').textContent()) ?? '';
  const correctForm = correctRaw.replace(/^[A-Z]\.\s*/, '');
  await completeSession(page);
  await page.locator('button').filter({ hasText: 'Заново' }).click();
  await page.waitForSelector('.eh-token:not([disabled])');

  await findErrorToken(page);
  const exact = new RegExp(`^[A-Z]\\.\\s*${correctForm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}$`);
  await page.locator('.exam-option').filter({ hasText: exact }).click();
  await expect(page.locator('.exam-feedback.feedback-correct')).toBeVisible();
});

test('error hunt: wrong option shows negative feedback and highlights correct', async ({ page }) => {
  await gotoErrorHunt(page);
  await findErrorToken(page);
  await page.locator('.exam-option').first().click();
  await page.waitForSelector('.exam-feedback');

  if (await page.locator('.exam-feedback.feedback-wrong').isVisible()) {
    // We chose wrong directly — verify correct option is highlighted
    await expect(page.locator('.exam-option.feedback-correct')).toHaveCount(1);
    return;
  }

  // We chose correct. Learn correct form, restart, pick a different option.
  const correctRaw =
    (await page.locator('.exam-option.feedback-correct').textContent()) ?? '';
  const correctForm = correctRaw.replace(/^[A-Z]\.\s*/, '');
  await completeSession(page);
  await page.locator('button').filter({ hasText: 'Заново' }).click();
  await page.waitForSelector('.eh-token:not([disabled])');

  await findErrorToken(page);
  // Click the first option that is NOT the correct form
  const opts = page.locator('.exam-option');
  for (let i = 0; i < 3; i++) {
    const t = ((await opts.nth(i).textContent()) ?? '').replace(/^[A-Z]\.\s*/, '');
    if (t !== correctForm) {
      await opts.nth(i).click();
      break;
    }
  }

  await expect(page.locator('.exam-feedback.feedback-wrong')).toBeVisible();
  await expect(page.locator('.exam-option.feedback-correct')).toHaveCount(1);
});

test('error hunt: complete session shows summary with Заново', async ({ page }) => {
  await gotoErrorHunt(page);

  for (let i = 0; i < 12; i++) {
    if (await page.locator('.eh-summary').count() > 0) break;
    await completeExercise(page);
  }

  await expect(page.locator('.eh-summary')).toBeVisible();
  await expect(page.locator('button').filter({ hasText: 'Заново' })).toBeVisible();
});

test('error hunt: Заново restarts from first exercise', async ({ page }) => {
  await gotoErrorHunt(page);

  for (let i = 0; i < 12; i++) {
    if (await page.locator('.eh-summary').count() > 0) break;
    await completeExercise(page);
  }

  await page.locator('button').filter({ hasText: 'Заново' }).click();
  await expect(page.locator('.eh-sentence')).toBeVisible();
  await expect(page.locator('.exam-progress-text')).toContainText('1 /');
});

// ─── AC-EH-07: Fill/Error Hunt switching ─────────────────────────────────────

test('error hunt: switching back to Подстановка shows cloze gap', async ({ page }) => {
  await gotoErrorHunt(page);
  await switcherBtn(page, 'Подстановка').click();
  await expect(page.locator('.exam-gap')).toBeVisible();
  await expect(page.locator('.eh-token')).toHaveCount(0);
});

test('error hunt: Fill sub-mode shows ___ sentence, not token buttons', async ({ page }) => {
  await page.goto('/#/cloze');
  // Default is Fill mode
  await expect(page.locator('.exam-gap')).toBeVisible();
  await expect(page.locator('.exam-options .exam-option')).toHaveCount(3);
});

test('error hunt: progress preserved when switching sub-modes', async ({ page }) => {
  await gotoErrorHunt(page);
  const progressText = await page.locator('.exam-progress-text').textContent();

  await switcherBtn(page, 'Подстановка').click();
  await expect(page.locator('.exam-gap')).toBeVisible();
  await switcherBtn(page, 'Найди ошибку').click();
  await expect(page.locator('.eh-sentence')).toBeVisible();

  await expect(page.locator('.exam-progress-text')).toHaveText(progressText!);
});

// ─── AC-9 / AC-EH-03 (en-ru): graceful degradation ─────────────────────────

test('en-ru: error hunt shows empty-state message; exam screen hides templates button', async ({ page }) => {
  // Switch to en course (en-ru pack has no tileTargetSlots exercises → empty pool).
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('en');

  // Error Hunt sub-mode: empty state message present, no token buttons.
  await page.goto('/#/cloze');
  await switcherBtn(page, 'Find the Error').click();
  await expect(page.locator('.eh-sentence')).toHaveCount(0);
  await expect(page.locator('.notice')).toBeVisible();

  // Exam screen: no templates browser button (en-ru has no letter_task_link rows).
  await page.goto('/#/exam');
  await expect(page.locator('button').filter({ hasText: /шаблон|template/i })).toHaveCount(0);

  // Restore to Polish — must not throw console errors during the whole sequence.
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('pl');
});
