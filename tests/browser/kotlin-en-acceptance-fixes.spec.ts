import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// EnRuAcceptance-2026-09-28.md §7 correction (blockers on commit 1e5e445): two gaps the original
// en-ru acceptance sweep never exercised.
//
// 1) MatrixWeb.kt's renderVerbs built the pl aspect/tense grid unconditionally, so with en-ru
//    active (an aspect-less pack — `verbs` is honestly empty) and the default matrix-selection
//    verbId "do", MatrixTableEngine.build crashed on `verbById("do")` the instant Matrix's
//    "Времена и лица" section rendered. Proves it no longer crashes and shows a calm placeholder.
// 2) CardBlocksWeb.kt's block renderers (Contrast/Table/Scene/NativeParallel/Examples) and
//    VocabularyWeb.kt's catalog lemma span hardcoded lang="pl" regardless of the active pack, so
//    English content in 3 of the 4 styles (and the frequency catalog) was mistagged. Proves each
//    now carries the active pack's own target-language code.

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

async function switchToEnglish(page: import('@playwright/test').Page) {
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('en');
  await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
  await expect(page).toHaveURL(/#\/training$/);
  await continueIntroductionIfPresent(page);
}

test('Matrix "Времена и лица" skips the empty pl aspect/tense grid for en-ru and shows a calm placeholder', async ({ page }) => {
  await switchToEnglish(page);

  const errors: string[] = [];
  page.on('pageerror', (err) => errors.push(String(err)));

  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.click('#matrix-section-verbs');

  // `verbs` (packRegistry.active.verbs) is honestly empty for en-ru (its verbs aren't
  // aspect-marked), so the pl "person × number × tense" grid must not render at all — not even
  // as a blank/empty table shell — a calm notice takes its place instead.
  await expect(page.getByRole('heading', { name: 'Лицо × число × время' })).toHaveCount(0);
  await expect(page.locator('.matrix-page').getByText('в этом языке нет падежей/рода')).toBeVisible();
  expect(errors).toEqual([]);

  // EN-24's own pack-independent English verb/do-support tables must still render alongside it.
  const enSection = page.locator('section.matrix-section').filter({ has: page.getByRole('heading', { name: /English/ }) });
  await expect(enSection).toBeVisible();
  const doSection = page.locator('section.matrix-section').filter({ has: page.getByRole('heading', { name: /do-support/ }) });
  await expect(doSection).toBeVisible();
});

test('RuleFirst style (Table front, Contrast back) tags en-ru content lang="en"', async ({ page }) => {
  await switchToEnglish(page);
  await page.selectOption('#explanation-method', 'RuleFirst');

  const front = page.locator('.card-blocks-front');
  await expect(front.locator('[lang="pl"]')).toHaveCount(0);
  await expect(front.locator('[lang="en"]').first()).toBeVisible();

  await page.getByRole('button', { name: /^(Показать ответ|Проверить и показать ответ)$/ }).click();
  const back = page.locator('.card-blocks-back');
  await expect(back.locator('[lang="pl"]')).toHaveCount(0);
  await expect(back.locator('.rule-contrast[lang="en"]')).toBeVisible();
});

test('SituationFirst style (Scene block) tags en-ru content lang="en"', async ({ page }) => {
  await switchToEnglish(page);
  await page.selectOption('#explanation-method', 'SituationFirst');

  const scene = page.locator('.block-scene-quote');
  await expect(scene).toBeVisible();
  await expect(scene).toHaveAttribute('lang', 'en');
});

test('MinimalTheory style (Examples block) tags en-ru content lang="en"', async ({ page }) => {
  await switchToEnglish(page);
  await page.selectOption('#explanation-method', 'MinimalTheory');

  const items = page.locator('.block-examples-list li');
  await expect(items.first()).toBeVisible();
  await expect(page.locator('.block-examples-list [lang="pl"]')).toHaveCount(0);
  const langs = await items.evaluateAll((els) => els.map((el) => el.getAttribute('lang')));
  expect(langs.every((l) => l === 'en')).toBe(true);
});

test('NativeContrast style tags any target-language content lang="en", never "pl"', async ({ page }) => {
  await switchToEnglish(page);
  await page.selectOption('#explanation-method', 'NativeContrast');

  await expect(page.locator('.card-blocks-front [lang="pl"]')).toHaveCount(0);
});

test('Vocabulary catalog lemma list tags entries lang="en" for en-ru, not "pl"', async ({ page }) => {
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('en');
  await page.getByRole('button', { name: 'Открыть словарь и импорт или экспорт JSON' }).click();
  await expect(page).toHaveURL(/#\/vocabulary$/);

  const filter = page.getByRole('combobox', { name: 'Подборка слов' });
  await filter.selectOption('100');

  const firstRow = page.locator('.catalog-list b').first();
  await expect(firstRow).toBeVisible();
  await expect(firstRow).toHaveAttribute('lang', 'en');
  await expect(page.locator('.catalog-list b[lang="pl"]')).toHaveCount(0);
});
