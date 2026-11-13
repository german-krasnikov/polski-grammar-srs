import { expect, test, type Page } from '@playwright/test';
import { installProgressReader } from './kotlin-storage';

// M3 blueprint (Plans/Architecture/DAILY-SESSION-BLUEPRINT.md §Verification): TrainingMode.Daily
// acceptance. The default mix (reviews=4, new=1, practice=1, planned=6) needs 4 already-known,
// already-due skills seeded up front — a truly fresh profile only ever reaches 2 steps (one New,
// then Practice falling back to that same just-introduced skill) before every kind's cap is either
// hit or has nothing left, per DailySessionPlanner's own backfill rule.
const previewKey = 'polski-grammar-srs-kmp-preview-v1';
const expectedBranch = process.env.KOTLIN_SPIKE_BRANCH;

const allSkillIds = [
  'case.acc.n', 'case.acc.f', 'case.acc.m', 'case.gen.neg', 'case.inst', 'case.loc', 'case.dat',
  'agreement.my', 'verb.present', 'verb.past', 'verb.future', 'aspect', 'pronouns', 'mixed',
  'sentence.question', 'sentence.plural', 'degree.comparison', 'verb.participles', 'voice.impersonal',
  'wordform.nouns', 'numeral.agreement', 'wordform.adjectives', 'sentence.connectives',
  'sentence.rephrase', 'case.valency', 'verb.mood',
];
// case.acc.f is deliberately left reviews=0 — it is the planner's own New pick (first skill in
// list order with satisfied, here empty, prerequisites) once these four already occupy Review.
const reviewedSeedIds = ['case.acc.n', 'case.inst', 'case.dat', 'sentence.question'];
const dueInThePast = '2020-01-01T00:00:00.000Z';

function seedDocumentJson(): string {
  return JSON.stringify({
    version: 1,
    cards: allSkillIds.map(id => ({
      skillId: id,
      card: {
        due: dueInThePast, stability: 0, difficulty: 0, elapsed_days: 0, scheduled_days: 0,
        reps: 0, lapses: 0, learning_steps: 0, state: 0,
      },
    })),
    stats: Object.fromEntries(allSkillIds.map(id => [
      id,
      reviewedSeedIds.includes(id)
        ? { reviews: 1, correct: 1, streak: 1, mistakes: 0 }
        : { reviews: 0, correct: 0, streak: 0, mistakes: 0 },
    ])),
    reviewsToday: 0,
    lastDay: '2020-01-01',
    totalReviews: reviewedSeedIds.length,
  });
}

const seedWasmWorkaround = async (page: Page) => {
  if (expectedBranch !== 'js') return;
  await page.addInitScript(() => {
    const validate = WebAssembly.validate;
    WebAssembly.validate = function (bytes: BufferSource): boolean {
      if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
      return validate.call(WebAssembly, bytes);
    };
  });
};

/** Places the correct form (option 0) for every `.tile-choice` currently on the card — same
 *  pattern as `kotlin-training-tile.spec.ts`'s own `placeChoice` helper. */
const fillAllTileChoices = async (page: Page) => {
  const count = await page.locator('.tile-choice').count();
  for (let index = 0; index < count; index += 1) {
    await page.locator('.tile-choice').nth(index).click();
    const options = page.locator('.tile-option');
    await expect(options.first()).toBeVisible();
    await options.first().click();
  }
};

/** Whichever of: an introduction screen, a plain/typed answer area, or a TileBuilder row shows up
 *  next — `continueIntroductionIfPresent` (kotlin-introduction.ts) only waits for the first two,
 *  so it can't be reused once `dailyAnswerMode` has forced TileBuilder for a Practice step (its
 *  own Check button is named "Проверить", not "Проверить и показать ответ"). */
const waitForAnswerSurface = (page: Page) =>
  page.locator(
    'button:has-text("Перейти к заданию"), button:has-text("Показать ответ"), button:has-text("Проверить и показать ответ"), .tile-row',
  ).first().waitFor();

/** One Daily card, whichever `AnswerMode` the store picked for it (Oral-fallback "Показать
 *  ответ" for Review/New, forced TileBuilder "Проверить" for Practice — `dailyAnswerMode`'s own
 *  documented exception) — then rates it. */
const answerDailyCard = async (page: Page, rating: 'again' | 'good') => {
  await waitForAnswerSurface(page);
  const intro = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await intro.count()) {
    await intro.click();
    await waitForAnswerSurface(page);
  }
  if (await page.locator('.tile-row').count()) {
    await fillAllTileChoices(page);
    await page.getByRole('button', { name: 'Проверить' }).click();
  } else {
    await page.getByRole('button', { name: /^(Показать ответ|Проверить и показать ответ)$/ }).click();
  }
  await page.getByRole('button', { name: rating === 'good' ? /2 Вспомнил/ : /1 Повторить/ }).click();
};

const errors = new WeakMap<Page, string[]>();

test.beforeEach(async ({ page }) => {
  await installProgressReader(page);
  const failures: string[] = [];
  errors.set(page, failures);
  page.on('pageerror', error => failures.push(error.message));
  await seedWasmWorkaround(page);
});

test.afterEach(async ({ page }) => {
  expect(errors.get(page)).toEqual([]);
});

test('Сегодня button is visible next to the other modes and starts a 0/6 session', async ({ page }) => {
  await page.goto('/');
  const dailyButton = page.getByRole('button', { name: /Сегодня · ~5 мин/ });
  await expect(dailyButton).toBeVisible();
  await expect(page.getByRole('button', { name: 'Цепочка предложений' })).toBeVisible();
  await expect(page.getByRole('button', { name: /По расписанию/ })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Отдельный навык' })).toBeVisible();

  await dailyButton.click();
  await expect(page.locator('[role="progressbar"]')).toHaveText('0 / 6');
  await expect(dailyButton).toHaveAttribute('aria-pressed', 'true');
});

test('fresh profile: no due reviews yet, so the first Daily card introduces a new skill', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: /Сегодня · ~5 мин/ }).click();
  // Priority-1 redefinition (blueprint "Decisions"): a never-reviewed skill's freshly scheduled
  // card is technically due too, but that is New's job, not Review's — so a brand-new profile
  // must land here, never on a routine "show answer" review card.
  await expect(page.getByRole('button', { name: 'Перейти к заданию' })).toBeVisible();
  await expect(page.locator('[role="progressbar"]')).toHaveText('0 / 6');
});

test('a seeded session reaches the completion screen after six rated steps, honestly labelled per step', async ({ page }) => {
  await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [previewKey, seedDocumentJson()] as const);
  await page.goto('/');
  await page.getByRole('button', { name: /Сегодня · ~5 мин/ }).click();
  await expect(page.locator('[role="progressbar"]')).toHaveText('0 / 6');

  // Steps 1-4: the four pre-seeded, already-due, already-reviewed skills -> Review, honestly
  // labelled "Сегодня · шаг N / 6" (never a generic "Сегодня" per Plan.md §M3's own "не
  // маскируется под тест памяти").
  for (let step = 1; step <= 4; step += 1) {
    await expect(page.locator('.card-meta')).toContainText(`Сегодня · шаг ${step} / 6`);
    await answerDailyCard(page, step === 1 ? 'again' : 'good');
    await expect(page.locator('[role="progressbar"]')).toHaveText(`${step} / 6`);
  }
  // Step 5: the one never-reviewed, prerequisite-satisfied skill -> New (introduction screen).
  await expect(page.getByRole('button', { name: 'Перейти к заданию' })).toBeVisible();
  await answerDailyCard(page, 'good');
  await expect(page.locator('[role="progressbar"]')).toHaveText('5 / 6');

  // Step 6: Practice, forced into TileBuilder, honestly labelled as a practice check, not a review.
  await expect(page.locator('.card-meta')).toContainText('Короткая проверка применения');
  await answerDailyCard(page, 'good');

  await expect(page.getByRole('heading', { name: 'Сессия на сегодня готова' })).toBeVisible();
  await expect(page.locator('.session-complete p').first()).not.toBeEmpty();
  await expect(page.getByRole('button', { name: 'Ещё одно задание' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'К повторениям по расписанию' })).toBeVisible();
});

test('"Ещё одно задание" continues the session past its planned volume without resetting history', async ({ page }) => {
  await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [previewKey, seedDocumentJson()] as const);
  await page.goto('/');
  await page.getByRole('button', { name: /Сегодня · ~5 мин/ }).click();
  for (let step = 1; step <= 6; step += 1) await answerDailyCard(page, 'good');
  await expect(page.getByRole('heading', { name: 'Сессия на сегодня готова' })).toBeVisible();

  await page.getByRole('button', { name: 'Ещё одно задание' }).click();
  // Back to a real question card — the completed history (6) is preserved, now marked extended;
  // it only grows past "planned" once this bonus card itself gets rated.
  await expect(page.locator('[role="progressbar"]')).toHaveText('6 / 6 · продолжение');
  await answerDailyCard(page, 'good');
  await expect(page.locator('[role="progressbar"]')).toHaveText(/7 \/ 6/);
});

test('switching to Schedule from an in-progress Daily session does not leave stale Daily copy behind', async ({ page }) => {
  await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [previewKey, seedDocumentJson()] as const);
  await page.goto('/');
  await page.getByRole('button', { name: /Сегодня · ~5 мин/ }).click();
  await expect(page.locator('[role="progressbar"]')).toHaveText('0 / 6');

  await page.getByRole('button', { name: /По расписанию/ }).click();
  await expect(page.locator('.card-meta')).toContainText('Повторение по расписанию');
  await expect(page.locator('[role="progressbar"]')).toHaveCount(0);
});
