import { expect, test, type Page } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// Plans/Kotlin/ContrastHighlightPlan.md "Контракт выделения" + EmphasisUXAudit-2026-09-27.md E1:
// on the headline sentence (front "Исходное предложение", back "Эталон") `before` must render
// with a dashed underline and `after` with a solid one, in both themes, through the same
// `.change-before`/`.change-after` classes every other highlighted surface already uses
// (CardBlocksWeb.kt's table rows and "Что изменилось" pair) — one CSS rule, not a second path
// that only the non-headline surfaces got.

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
  await continueIntroductionIfPresent(page);
});

const luminance = (color: string) => {
  const [red, green, blue] = (color.match(/[\d.]+/g) ?? []).slice(0, 3).map(Number).map(channel => {
    const linear = channel / 255;
    return linear <= 0.04045 ? linear / 12.92 : ((linear + 0.055) / 1.055) ** 2.4;
  });
  return red * 0.2126 + green * 0.7152 + blue * 0.0722;
};
const contrastRatio = (foreground: string, background: string) => {
  const a = luminance(foreground);
  const b = luminance(background);
  return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
};

const setTheme = async (page: Page, theme: 'Dark' | 'Light') => {
  await page.getByRole('button', { name: 'Настройки', exact: true }).click();
  await page.getByRole('combobox', { name: 'Тема' }).selectOption(theme);
  await expect(page.locator('html')).toHaveAttribute('data-theme', theme.toLowerCase());
  await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
};

// The card face paints a gradient between these two custom properties (training.css `.card`), so
// the worst-case (lowest-contrast) endpoint is the one that must still clear 4.5:1.
const cardBackgrounds = (page: Page) => page.evaluate(() => {
  const probe = document.createElement('div');
  document.body.appendChild(probe);
  const resolve = (variable: string) => {
    probe.style.background = `var(${variable})`;
    return getComputedStyle(probe).backgroundColor;
  };
  const colors = [resolve('--surface'), resolve('--surface-raised')];
  probe.remove();
  return colors;
});

for (const theme of ['Dark', 'Light'] as const) {
  test(`headline sentence: before is dashed, after is solid, both readable in ${theme.toLowerCase()} theme`, async ({ page }) => {
    await setTheme(page, theme);
    await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();

    const backgrounds = await cardBackgrounds(page);

    const before = page.locator('.source-sentence .change-before').first();
    await expect(before).toBeVisible();
    const beforeStyle = await before.evaluate(element => ({ decorationStyle: getComputedStyle(element).textDecorationStyle, color: getComputedStyle(element).color }));
    expect(beforeStyle.decorationStyle).toBe('dashed');
    for (const background of backgrounds) {
      expect(contrastRatio(beforeStyle.color, background), `before ${beforeStyle.color} on ${background}`).toBeGreaterThanOrEqual(4.5);
    }

    await page.getByRole('button', { name: 'Показать ответ' }).click();
    const after = page.locator('.answer-sentence .change-after').first();
    await expect(after).toBeVisible();
    const afterStyle = await after.evaluate(element => ({ decorationStyle: getComputedStyle(element).textDecorationStyle, color: getComputedStyle(element).color }));
    expect(afterStyle.decorationStyle).toBe('solid');
    for (const background of backgrounds) {
      expect(contrastRatio(afterStyle.color, background), `after ${afterStyle.color} on ${background}`).toBeGreaterThanOrEqual(4.5);
    }

    // WCAG Use of Color: the roles must not rely on hue alone being "warm vs cool" in name only —
    // they must actually differ, and `after` must not drift back into red/orange territory.
    expect(afterStyle.color).not.toBe(beforeStyle.color);
    const [, beforeGreen, beforeBlue] = beforeStyle.color.match(/[\d.]+/g)!.map(Number);
    const [, afterGreen, afterBlue] = afterStyle.color.match(/[\d.]+/g)!.map(Number);
    expect(afterBlue + afterGreen, `after ${afterStyle.color} should read as cool (blue/green), not warm`).toBeGreaterThan(beforeBlue + beforeGreen);
  });
}

test('the same dashed-before/solid-after rule reaches other highlighted surfaces, not only the headline sentence', async ({ page }) => {
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const pairBefore = page.locator('.change-pair .change-before').first();
  const pairAfter = page.locator('.change-pair .change-after').first();
  await expect(pairBefore).toBeVisible();
  await expect(pairAfter).toBeVisible();
  await expect.poll(() => pairBefore.evaluate(el => getComputedStyle(el).textDecorationStyle)).toBe('dashed');
  await expect.poll(() => pairAfter.evaluate(el => getComputedStyle(el).textDecorationStyle)).toBe('solid');
  const headlineBefore = page.locator('.source-sentence .change-before').first();
  const [pairColor, headlineColor] = await Promise.all([
    pairBefore.evaluate(el => getComputedStyle(el).color),
    headlineBefore.evaluate(el => getComputedStyle(el).color),
  ]);
  expect(pairColor).toBe(headlineColor);
});
