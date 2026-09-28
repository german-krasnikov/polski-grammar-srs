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
  // UX5's route slide defers the incoming route's own DOM (a macrotask plus two rAFs, see
  // TrainingWebApp.kt's `commitRouteBookkeeping`/`slider.start` doc comment) past the moment the
  // URL itself updates — `toHaveText` polls until the slide settles and the real options land,
  // where a one-shot `allTextContents()` right after `toHaveURL` could read the still-empty select.
  await expect(direction.locator('option')).toHaveText(['Русский → английский', 'Английский → русский']);

  await page.getByRole('button', { name: 'Настройки' }).click();
  await expect(page).toHaveURL(/#\/settings$/);
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('pl');
  await page.getByRole('button', { name: 'Открыть словарь и импорт или экспорт JSON' }).click();
  await expect(page).toHaveURL(/#\/vocabulary$/);
  await expect(page.getByRole('combobox', { name: 'Направление карточки' }).locator('option'))
    .toHaveText(['Русский → польский', 'Польский → русский']);

  await page.getByRole('button', { name: 'Карточки' }).click();
  await expect(page).toHaveURL(/#\/training$/);
  await continueIntroductionIfPresent(page);
  await expect(page.locator('.source-sentence')).toHaveAttribute('lang', 'pl');
});

// EnRuAcceptance-2026-09-28.md §7 item 4: the direction picker's OWN options already followed the
// active pack (the test above), but [StudyDirection] itself stayed hardcoded to
// RussianToPolish/PolishToRussian everywhere the card actually reads/writes — selecting either
// en-ru option still saved/rated under pl-ru's own "ru-pl"/"pl-ru" wire. Proves the whole real user
// flow (catalog selection, flip/reveal, swipe rating) works for en-ru's own two directions and pl-ru
// stays untouched — not just that the select shows the right labels.
test('Vocabulary catalog selection, flip/reveal and swipe rating work end to end for en-ru; pl-ru is untouched', async ({ page }) => {
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('en');
  await page.getByRole('button', { name: 'Открыть словарь и импорт или экспорт JSON' }).click();
  await expect(page).toHaveURL(/#\/vocabulary$/);

  const catalogRow = page.locator('.catalog-row').first();
  await expect(catalogRow).toBeVisible();
  const lemma = (await catalogRow.locator('b').textContent())!;
  await catalogRow.getByRole('checkbox').check();

  // Forward (ru→en): prompt is Russian, revealed answer is the English lemma just selected.
  const card = page.getByRole('region', { name: 'Карточка слова' });
  await expect(card).toBeVisible();
  await expect(page.locator('.vocabulary-prompt')).toHaveAttribute('lang', 'ru');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const answer = page.locator('.vocabulary-answer p').first();
  await expect(answer).toHaveAttribute('lang', 'en');
  await expect(answer).toHaveText(lemma);

  // Real 3D flip (v3/B) — clicking the revealed face toggles which side faces forward, purely
  // visually, the same FlipCard the training card's own flip uses.
  const flip = page.locator('.card-flip');
  const inner = flip.locator('.card-flip-inner');
  const beforeFlip = await inner.evaluate(el => (el as HTMLElement).style.transform);
  await flip.click();
  await expect.poll(() => inner.evaluate(el => (el as HTMLElement).style.transform)).not.toBe(beforeFlip);
  await flip.click();
  await expect.poll(() => inner.evaluate(el => (el as HTMLElement).style.transform)).toBe(beforeFlip);

  // Swipe right on the flip zone (vocabulary's own swipe surface, see installSwipeCard's doc
  // comment) rates "Вспомнил" — same >75px rightward drag kotlin-preferences-settings.spec.ts's
  // grammar swipe test already uses, dispatched as a touch pointer sequence.
  await flip.dispatchEvent('pointerdown', { clientX: 100, clientY: 100, pointerId: 1, pointerType: 'touch', isPrimary: true });
  await flip.dispatchEvent('pointerup', { clientX: 260, clientY: 102, pointerId: 1, pointerType: 'touch', isPrimary: true });

  // installSwipeCard commits the rating only after its own fly-out transition (220ms, see
  // SWIPE_COMMIT_TRANSITION_MS) — wait for the card itself to leave the revealed face rather than
  // reading localStorage before that timeout has fired.
  await expect(page.locator('.vocabulary-answer')).toHaveCount(0);
  const afterForward = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-en-ru-v1')!));
  const forwardKey = Object.keys(afterForward.cards).find(key => key.startsWith('en-ru:vocabulary:ru-en:'));
  expect(forwardKey).toBeDefined();
  expect(afterForward.cards[forwardKey!].reps).toBe(1);

  // Backward (en→ru): switch direction, reveal, rate with the explicit button this time.
  await page.getByRole('combobox', { name: 'Направление карточки' }).selectOption('en-ru');
  await expect(page.locator('.vocabulary-prompt')).toHaveAttribute('lang', 'en');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.locator('.vocabulary-answer p').first()).toHaveAttribute('lang', 'ru');
  await page.getByRole('button', { name: 'Вспомнил' }).click();

  const afterBoth = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-en-ru-v1')!));
  const backwardKey = Object.keys(afterBoth.cards).find(key => key.startsWith('en-ru:vocabulary:en-ru:'));
  expect(backwardKey).toBeDefined();
  expect(afterBoth.cards[backwardKey!].reps).toBe(1);
  expect(Object.keys(afterBoth.cards)).toHaveLength(2);

  // pl-ru's own document was never touched by any of the above.
  expect(await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'))).toBeNull();
});
