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

  // ST-03: no course has authored `styleContent.nativeParallel` yet, so native-contrast resolves
  // to its declared rule-first fallback (never an empty/crashing NativeParallel block).
  await method.selectOption('NativeContrast');
  await expect(front.locator('.rule-focus')).toBeVisible();
  await expect(front.locator('.block-table')).toBeVisible();
  await expect(front.locator('.block-native-parallel')).toHaveCount(0);
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
