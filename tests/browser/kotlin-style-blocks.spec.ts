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
test('situation-first back shows the rule alongside the changes after reveal', async ({ page }) => {
  const method = page.getByRole('combobox', { name: 'Подача объяснений' });
  await method.selectOption('SituationFirst');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');
  await expect(back.locator('.change-list')).toBeVisible();
  await expect(back.locator('.rule-focus')).toBeVisible();
  await expect(back.locator('.rule-focus > p').first()).not.toBeEmpty();
});
