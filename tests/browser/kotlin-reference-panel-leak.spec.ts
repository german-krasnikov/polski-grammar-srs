import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// EmphasisUXAudit-2026-09-27.md E8 / ContrastHighlightPlan.md §5 ("Запрет утечки ответа"):
// "Таблица под рукой" is a reference table for the whole case system, so every OTHER row may
// show its form freely — but the row that matches this exercise's own target case is the
// answer, and before reveal that row must stay unhighlighted with its form hidden, for every
// skill (not just the ones whose exercises happen to be case drills).
test('reference panel never leaks this exercise\'s own answer before reveal, for every skill', async ({ page }) => {
  test.setTimeout(90_000);
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
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  const picker = page.getByRole('region', { name: 'Выбор навыка' });
  await expect(picker).toBeVisible();
  const skillCount = await picker.getByRole('button').count();
  expect(skillCount).toBeGreaterThan(0);

  for (let index = 0; index < skillCount; index += 1) {
    if (!(await picker.isVisible())) await page.getByRole('button', { name: 'Отдельный навык' }).click();
    await picker.getByRole('button').nth(index).click();
    await continueIntroductionIfPresent(page);

    const referenceButton = page.getByRole('button', { name: /^(Таблица под рукой|Скрыть таблицу)$/ });
    if ((await referenceButton.textContent())?.trim() === 'Таблица под рукой') await referenceButton.click();
    const panel = page.locator('.reference-panel');
    await expect(panel).toBeVisible();

    // No row is highlighted as the answer before reveal.
    await expect(panel.locator('.highlight-row')).toHaveCount(0);
    const beforeReveal = await panel.innerText();

    await page.locator('#training-reveal').click();
    await expect(page.locator('.answer-sentence')).toBeVisible();

    // If this skill's exercise has a matching case row, it becomes the highlighted answer only
    // now — and the pre-reveal text must have shown the masked placeholder for it, not its form.
    // (Comparing full "Было/Стало" text instead would false-fail on Nom=Acc syncretism, e.g.
    // neuter "okno": the revealed form legitimately repeats the already-visible "Было" text.)
    if (await panel.locator('.highlight-row').count()) {
      expect(beforeReveal).toContain('Стало: ?');
    }
  }
});
