import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// UC-10 web S2: the card's Front/Back content comes from StyleComposer.compose's own block list
// for the resolved style (Plans/Kotlin/StylesBlueprint.md §1/§4/§6) — switching the "Подача
// объяснений" picker changes which of these sections render, never a hardcoded style branch.

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

test('switching style changes which blocks the card shows, on the front and after reveal', async ({ page }) => {
  const method = page.getByRole('combobox', { name: 'Подача объяснений' });
  const front = page.locator('.card-front');

  // RuleFirst (default): Formula ("ЗАПОМНИ" + the rule) and a compact endings Table, no Scene.
  await expect(front.locator('.rule-focus small')).toHaveText('ЗАПОМНИ');
  await expect(front.locator('.block-table')).toBeVisible();
  await expect(front.locator('.block-table-wrap tbody tr')).toHaveCount(1);
  await expect(front.locator('.block-scene')).toHaveCount(0);

  await method.selectOption('SituationFirst');
  await expect(front.locator('.block-scene')).toBeVisible();
  await expect(front.locator('.block-scene-quote')).not.toBeEmpty();
  await expect(front.locator('.rule-focus')).toHaveCount(0);
  await expect(front.locator('.block-table')).toHaveCount(0);

  // ST-03/ST-11: CONTENT has authored `styleContent.nativeParallel` for every real skill, so
  // native-contrast shows its own block here, not the rule-first fallback (StyleComposerTest's
  // nativeContrastFallsBackWithoutContentAndComposesWithIt covers the fallback itself with a
  // literal no-content fixture, never an empty/crashing NativeParallel block either way).
  await method.selectOption('NativeContrast');
  await expect(front.locator('.block-native-parallel')).toBeVisible();
  await expect(front.locator('.rule-focus')).toHaveCount(0);
  await expect(front.locator('.block-table')).toHaveCount(0);
  await expect(front.locator('.block-scene')).toHaveCount(0);

  await method.selectOption('MinimalTheory');
  await expect(front.locator('.rule-focus')).toHaveCount(0);
  await expect(front.locator('.block-table')).toHaveCount(0);
  await expect(front.locator('.block-scene')).toHaveCount(0);

  await method.selectOption('RuleFirst');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');
  // Changes is an exercise invariant (ST-05) — present on Back for every style.
  await expect(back.locator('.change-list')).toBeVisible();
  await expect(back.locator('.rule-focus > strong')).toBeVisible();
  await expect(back.locator('.rule-contrast .form-contrast')).toBeVisible();
  await expect(back.locator('.block-why')).toHaveCount(0);
  // Regression (post-df8ade7 correction): the rule-focus box must still show BOTH the skill's
  // rule/theory AND the per-exercise explanation paragraph on reveal, matching pre-UC-10 rule-first
  // byte-for-byte (Plans/Kotlin/StylesBlueprint.md:176) — a silent drop of the 2nd paragraph
  // wouldn't be caught by a mere "some rule-focus text exists" check.
  const ruleParagraphs = back.locator('.rule-focus > p');
  await expect(ruleParagraphs).toHaveCount(2);
  await expect(ruleParagraphs.nth(0)).not.toBeEmpty();
  await expect(ruleParagraphs.nth(1)).not.toBeEmpty();
  await expect(ruleParagraphs.nth(1)).not.toHaveText(await ruleParagraphs.nth(0).textContent() ?? '');

  // Switching style after reveal is a display choice only — same exercise/draft, no new review
  // (ST-04); the Back's Formula/Rule/Contrast box disappears and the "Почему так?" disclosure
  // (collapsed by default, keyboard/ARIA accessible) appears instead.
  await method.selectOption('MinimalTheory');
  await expect(back.locator('.change-list')).toBeVisible();
  await expect(back.locator('.rule-focus')).toHaveCount(0);
  const whyToggle = back.getByRole('button', { name: 'Почему так?' });
  await expect(whyToggle).toHaveAttribute('aria-expanded', 'false');
  await expect(back.locator('.block-why-content')).not.toHaveClass(/expanded/);
  await whyToggle.click();
  await expect(whyToggle).toHaveAttribute('aria-expanded', 'true');
  await expect(back.locator('.block-why-content')).toHaveClass(/expanded/);
  await expect(back.locator('.block-why-content')).not.toHaveAttribute('inert', '');
});

// D1/D3 (StylesIntegrationTest-2026-09-27.md): situation-first.json's own `blocks.back` is
// [changes, rule] — courses/styles/*.json is now the single source of truth StyleRegistry loads
// (kotlin/shared/build.gradle.kts), so the rule must render after reveal, not just the exercise's
// Changes diff (the pre-fix hand-written registry back was [Changes] only, dropping Rule).
// W3 (EmphasisUXAudit E7, using shared S4 `Block.*.parts`/`targetParts`): a style block's own
// prose highlights only a literal, whole-word occurrence of the skill's explicit
// `focus.before`/`focus.after` pair — never the current exercise's own answer. "Zaimki osobowe"
// (pronouns) is the one real skill whose formula ("ona → ją / jej / nią / niej") and first
// nativeParallel target ("Znam ją.") both literally contain its `focus.after` ("ją"), so this
// exercises the real render path end to end instead of only the shared unit fixtures.
test('style blocks highlight a literal occurrence of the skill\'s own focus pair, per block kind', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Zaimki osobowe · A2' }).click();
  await continueIntroductionIfPresent(page);
  const method = page.getByRole('combobox', { name: 'Подача объяснений' });
  const front = page.locator('.card-front');

  // Formula: "ona → ją / jej / nią / niej" — only the standalone word "ją" is marked, the rest
  // of the formula's own text is still present around it (no text lost/duplicated).
  await method.selectOption('RuleFirst');
  const formula = front.locator('.rule-focus strong');
  await expect(formula).toBeVisible();
  await expect(formula.locator('.change-after')).toHaveText('ją');
  await expect(formula).toHaveText('ona → ją / jej / nią / niej');

  // NativeParallel target: "Znam ją." highlights "ją"; the native (L1) side of the same row is
  // never colored (Emphasis contract §4 — родная сторона nativeParallel не красится).
  await method.selectOption('NativeContrast');
  const firstRow = front.locator('.native-parallel-row').first();
  await expect(firstRow.locator('.native-parallel-target')).toHaveText('Znam ją.');
  await expect(firstRow.locator('.native-parallel-target .change-after')).toHaveText('ją');
  await expect(firstRow.locator('.native-parallel-native .change-after')).toHaveCount(0);
  await expect(firstRow.locator('.native-parallel-native .change-before')).toHaveCount(0);

  // Scene: none of this skill's own scene text literally repeats "moja żona" or "ją" (contract
  // rule 6: no highlight beats a wrong one), so it still renders in full with zero invented marks.
  await method.selectOption('SituationFirst');
  await expect(front.locator('.block-scene-quote')).not.toHaveText('');
  await expect(front.locator('.block-scene-quote .change-before, .block-scene-quote .change-after')).toHaveCount(0);

  // Examples: same "no literal match, no invented mark" check, across both list items.
  await method.selectOption('MinimalTheory');
  const examples = front.locator('.block-examples-list li');
  await expect(examples).toHaveCount(2);
  await expect(examples.locator('.change-before, .change-after')).toHaveCount(0);

  // WhyOnDemand and Rule live on the Back; switching style after reveal is a display choice only
  // (no new review), so one reveal covers both without spending a second rating.
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');
  await page.getByRole('button', { name: 'Почему так?' }).click();
  const why = back.locator('.block-why-content p');
  await expect(why).not.toHaveText('');
  await expect(why.locator('.change-before, .change-after')).toHaveCount(0);

  await method.selectOption('RuleFirst');
  const rule = back.locator('.rule-focus > p').first();
  await expect(rule).not.toHaveText('');
  await expect(rule.locator('.change-before, .change-after')).toHaveCount(0);
});

test('situation-first back shows the rule alongside the changes after reveal', async ({ page }) => {
  const method = page.getByRole('combobox', { name: 'Подача объяснений' });
  await method.selectOption('SituationFirst');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');
  await expect(back.locator('.change-list')).toBeVisible();
  await expect(back.locator('.rule-focus')).toBeVisible();
  await expect(back.locator('.rule-focus > p').first()).not.toBeEmpty();
});
