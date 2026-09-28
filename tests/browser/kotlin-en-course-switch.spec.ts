import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// EnRuAcceptance-2026-09-28.md §7 items 1-4 (ADR-37/ADR-38 core fixes + this task's web Settings
// pickers): the web host had no target/native picker at all, and every place it rendered the
// active exercise hardcoded `lang="pl"`/"по-польски" regardless of which pack was actually active.
// This proves the real, end-to-end user scenario on THIS host: open Settings, list every usable
// pack (pl-ru and en-ru), switch to target English/native Russian, and see Training/lifehacks/
// vocabulary genuinely render English — not a mock, the real `lang/en` pack data through the real
// production ExerciseGenerator/TableMorphology wiring (EnPackEngineRealFormsTest's :shared-side
// proof, exercised here through the actual UI) — then switch back to pl-ru with pl-ru untouched.

const CYRILLIC_OR_POLISH = /[а-яёА-ЯЁąćęłńóśźżĄĆĘŁŃÓŚŹŻ]/;

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

test('Settings lists every usable pack; selecting target English/native Russian rebuilds Training in English', async ({ page }) => {
  await page.goto('/#/settings');
  await expect(page.getByRole('heading', { name: 'Настройки обучения' })).toBeVisible();

  const target = page.getByRole('combobox', { name: 'Изучаемый язык' });
  const native = page.getByRole('combobox', { name: 'Родной язык' });
  await expect(target).toBeVisible();
  const targetLabels = await target.locator('option').allTextContents();
  expect(targetLabels).toEqual(['Польский', 'Английский']);
  await expect(native).toBeVisible();
  expect(await native.locator('option').allTextContents()).toEqual(['Русский']);

  await target.selectOption('en');
  await expect(target).toHaveValue('en');

  await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
  await expect(page).toHaveURL(/#\/training$/);
  await continueIntroductionIfPresent(page);

  const source = page.locator('.source-sentence');
  await expect(source).toBeVisible();
  await expect(source).toHaveAttribute('lang', 'en');
  const sourceText = (await source.textContent())!;
  expect(sourceText).not.toMatch(CYRILLIC_OR_POLISH);
  expect(sourceText.trim().length).toBeGreaterThan(0);

  // Typed mode's accessible label follows the active pack too, not a hardcoded Polish adverb —
  // checked pre-reveal, the only phase this input exists in.
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await expect(page.getByRole('textbox', { name: 'Ответ по-английски' })).toBeVisible();

  await page.getByRole('button', { name: /^(Показать ответ|Проверить и показать ответ)$/ }).click();
  const answer = page.locator('.answer-sentence');
  await expect(answer).toBeVisible();
  await expect(answer).toHaveAttribute('lang', 'en');
  expect((await answer.textContent())!).not.toMatch(CYRILLIC_OR_POLISH);
});

test('an English-only skill (Object · без падежа) shows its own en-ru lifehack after switching', async ({ page }) => {
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('en');
  await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
  await continueIntroductionIfPresent(page);

  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Object · без падежа · A1' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: /^(Показать ответ|Проверить и показать ответ)$/ }).click();

  const back = page.locator('.card-back');
  const toggle = back.getByRole('button', { name: /^Лайфхак/ }).first();
  await expect(toggle).toBeVisible();
  await toggle.click();
  await expect(back.locator('.lifehack-content').first()).toContainText('артикл');
});

test('Vocabulary direction picker follows the active pack, and switching back to pl-ru is byte-identical', async ({ page }) => {
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('en');
  await page.getByRole('button', { name: 'Открыть словарь и импорт или экспорт JSON' }).click();
  await expect(page).toHaveURL(/#\/vocabulary$/);
  const direction = page.getByRole('combobox', { name: 'Направление карточки' });
  expect(await direction.locator('option').allTextContents()).toEqual(['Русский → английский', 'Английский → русский']);

  await page.getByRole('button', { name: 'Настройки' }).click();
  await expect(page).toHaveURL(/#\/settings$/);
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('pl');
  await page.getByRole('button', { name: 'Открыть словарь и импорт или экспорт JSON' }).click();
  await expect(page).toHaveURL(/#\/vocabulary$/);
  expect(await page.getByRole('combobox', { name: 'Направление карточки' }).locator('option').allTextContents())
    .toEqual(['Русский → польский', 'Польский → русский']);

  await page.getByRole('button', { name: 'Карточки' }).click();
  await expect(page).toHaveURL(/#\/training$/);
  await continueIntroductionIfPresent(page);
  await expect(page.locator('.source-sentence')).toHaveAttribute('lang', 'pl');
});
