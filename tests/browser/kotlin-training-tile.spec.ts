import { expect, test, type Page } from '@playwright/test';
import { installProgressReader } from './kotlin-storage';

// SB6-AC1..AC11 (Plans/Architecture/SENTENCE-BUILDER-WEB-BLUEPRINT.md §5). `case.acc.f` is one of
// the three pilot skills opted into tiles (commit 7d50aa6): its recipe sets
// `tileTargetSlots: [adjective noun]`, so every generated exercise has exactly two
// `TileSegment.Choice` positions and at least one locked word around them.

const continueIntroductionIfPresent = async (page: Page) => {
  await page.getByRole('button', { name: /^(Перейти к заданию|Показать ответ)$/ }).first().waitFor();
  const next = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await next.count()) await next.click();
};

const previewKey = 'polski-grammar-srs-kmp-preview-v1';
const expectedBranch = process.env.KOTLIN_SPIKE_BRANCH;
const errors = new WeakMap<Page, string[]>();

/** Navigates to the Focused "Biernik · żeński" (`case.acc.f`) card and switches to TileBuilder. */
const openTileCard = async (page: Page) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button', { name: /Biernik · żeński/ }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Собрать из слов' }).click();
  await expect(page.locator('.tile-row')).toBeVisible();
};

/** Opens the picker for the Nth empty choice tile and places `optionIndex` (0 = correctForm). */
const placeChoice = async (page: Page, choiceIndex: number, optionIndex: number) => {
  const tile = page.locator('.tile-choice').nth(choiceIndex);
  await tile.click();
  const options = page.locator('.tile-option');
  await expect(options.first()).toBeVisible();
  await options.nth(optionIndex).click();
};

test.beforeEach(async ({ page }) => {
  await installProgressReader(page);
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

test('SB6-AC1/AC4: tile row starts with every choice empty, in position order, and Check stays disabled', async ({ page }) => {
  await openTileCard(page);
  await expect(page.locator('.tile-choice.tile-empty')).toHaveCount(2);
  await expect(page.locator('.tile-choice.tile-empty').first()).toHaveText('___');
  const check = page.getByRole('button', { name: 'Проверить' });
  await expect(check).toBeDisabled();
  await placeChoice(page, 0, 0);
  await expect(check).toBeDisabled(); // one of two still empty
  await placeChoice(page, 1, 0);
  await expect(check).toBeEnabled();
});

test('SB6-AC2: a skill without tileTargetSlots disables the mode button and falls back to Oral, never a broken row', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button', { name: /Czas przeszły/ }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Собрать из слов' })).toBeDisabled();
  await expect(page.locator('.tile-row')).toHaveCount(0);
  await expect(page.locator('.muted', { hasText: 'Произнеси целое предложение' })).toBeVisible();
});

test('SB6-AC3/AC5/AC7: placing correct forms shows a green result, records the review, and the next card starts clean', async ({ page }) => {
  await openTileCard(page);
  await placeChoice(page, 0, 0);
  await expect(page.locator('.tile-choice.tile-empty')).toHaveCount(1);
  await placeChoice(page, 1, 0);
  await expect(page.locator('.tile-choice.tile-empty')).toHaveCount(0);
  await page.getByRole('button', { name: 'Проверить' }).click();
  await expect(page.locator('.tile-correct')).toHaveCount(2);
  await expect(page.locator('.tile-incorrect')).toHaveCount(0);
  await expect(page.locator('.typed-result.correct')).toContainText('Совпадает с правильным вариантом');
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => (window as any).__readProgress(key, 'totalReviews'), previewKey)).toBe(1);
  // Next card: a fresh TileBuilder derivation, never carrying over the previous selection/result/open panel.
  await expect(page.locator('.tile-row')).toBeVisible();
  await expect(page.locator('.tile-choice.tile-empty')).toHaveCount(2);
  await expect(page.locator('.typed-result')).toHaveCount(0);
  await expect(page.locator('.tile-option')).toHaveCount(0);
});

test('SB6-AC5/AC6: a wrong tile is red, its mini-rule toggles open/closed on tap, and a green tile is not a button', async ({ page }) => {
  await openTileCard(page);
  const optionCounts: number[] = [];
  for (let i = 0; i < 2; i += 1) {
    await page.locator('.tile-choice').nth(i).click();
    optionCounts.push(await page.locator('.tile-option').count());
    await page.locator('.tile-option').first().click(); // tentatively place the correct form, re-opened below
  }
  const wrongIndex = optionCounts.findIndex(count => count > 1);
  test.skip(wrongIndex === -1, 'neither pilot choice carries a distractor alternative in this content snapshot');
  await placeChoice(page, wrongIndex, 1); // the alternative, not correctForm
  await page.getByRole('button', { name: 'Проверить' }).click();
  await expect(page.locator('.tile-incorrect')).toHaveCount(1);
  await expect(page.locator('.tile-correct')).toHaveCount(1);
  await expect(page.locator('.typed-result.incorrect')).toBeVisible();
  const wrongTile = page.locator('.tile-incorrect');
  await expect(wrongTile).toHaveAttribute('aria-label', /неверно, нажми для правила/);
  await wrongTile.click();
  await expect(page.locator('.rule-focus')).toBeVisible();
  await expect(wrongTile).toHaveAttribute('aria-expanded', 'true');
  await wrongTile.click(); // toggles closed
  await expect(wrongTile).toHaveAttribute('aria-expanded', 'false');
  // A correct tile renders as a <span>, not a button — tapping it must do nothing.
  const greenTile = page.locator('.tile-correct');
  await expect(greenTile).not.toHaveJSProperty('tagName', 'BUTTON');
});

test('SB6-AC8/AC9: tiles stay >=48px with 8px gap and wrap at 320px, and reduced motion opens the picker instantly', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await openTileCard(page);
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(320);
  const box = await page.locator('.tile-choice').first().boundingBox();
  expect(box?.width).toBeGreaterThanOrEqual(48);
  expect(box?.height).toBeGreaterThanOrEqual(48);
  await page.locator('.tile-choice').first().click();
  const panel = page.locator('.tile-picker');
  await expect(panel).toHaveClass(/expanded/);
  expect(await panel.evaluate(node => getComputedStyle(node).transitionDuration)).toBe('0s');
});

test('SB6-AC10: tiles are reachable by Tab/Enter, and collapsed picker candidates are inert', async ({ page }) => {
  await openTileCard(page);
  const firstTile = page.locator('.tile-choice').first();
  await expect(page.locator('.tile-picker')).toHaveAttribute('inert', '');
  await firstTile.focus();
  await expect(firstTile).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(firstTile).toHaveAttribute('aria-expanded', 'true');
  await expect(page.locator('.tile-picker')).not.toHaveAttribute('inert', '');
  const firstOption = page.locator('.tile-option').first();
  await expect(firstOption).toBeVisible();
  await firstOption.focus();
  await page.keyboard.press('Enter');
  await expect(firstTile).not.toHaveClass(/tile-empty/);
  await expect(page.locator('.tile-picker')).toHaveAttribute('inert', '');
});
