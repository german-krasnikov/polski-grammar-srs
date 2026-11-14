import { expect, test } from '@playwright/test';

// M10 blueprint: `dsl/exam-skill-links.data-contract` links B1-2017-g1-2/g1-5 to the course skill
// `case.loc`, and B1-2017-g1-3 to `case.inst` (only one linked item, the AC4 degrade case).

async function finishGrammarModule(page: import('@playwright/test').Page) {
  while (await page.getByRole('button', { name: 'Далее →' }).isVisible()) {
    await page.getByRole('button', { name: 'Далее →' }).click();
  }
  await page.getByRole('button', { name: 'Завершить' }).click();
}

test('M10 AC2: only a wrong+linked review row offers "Потренировать навык"', async ({ page }) => {
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await expect(page.locator('.exam-progress-text')).toContainText('1 / 50');
  // Item 1 (g1-1) is left unanswered and unlinked.
  await page.getByRole('button', { name: 'Далее →' }).click();
  await expect(page.locator('.exam-progress-text')).toContainText('2 / 50');
  // Item 2 (g1-2, linked to case.loc) answered wrong.
  await page.locator('.exam-option').filter({ hasText: 'późnego lata' }).click();
  await finishGrammarModule(page);

  const unlinkedRow = page.locator('.exam-review-item').first();
  await expect(unlinkedRow.getByRole('button', { name: 'Потренировать навык' })).toHaveCount(0);

  const linkedRow = page.locator('.exam-review-item').nth(1);
  await expect(linkedRow).toHaveClass(/wrong/);
  await expect(linkedRow.getByRole('button', { name: 'Потренировать навык' })).toBeVisible();
});

test('M10 AC3: training a mistake to a Good rating opens a different verification question', async ({ page }) => {
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.getByRole('button', { name: 'Далее →' }).click();
  await page.locator('.exam-option').filter({ hasText: 'późnego lata' }).click();
  await finishGrammarModule(page);

  const linkedRow = page.locator('.exam-review-item').nth(1);
  await linkedRow.getByRole('button', { name: 'Потренировать навык' }).click();

  await expect(page).toHaveURL(/#\/training$/);
  // case.loc has no authored prediction question — straight to the introduction.
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /Вспомнил/ }).click();

  // AC3: back on the Exam route, on a DIFFERENT item (B1-2017-g1-5) than the one that triggered
  // it (B1-2017-g1-2) — never the original mistake, per the Learning Plan KPI note.
  await expect(page).toHaveURL(/#\/exam$/);
  await expect(page.locator('.exam-context')).toContainText('najnowocześniejszych ogrodów zoologicznych');
});

test('M10 AC4: a skill with only one linked item degrades to a "not enough questions" notice', async ({ page }) => {
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.getByRole('button', { name: 'Далее →' }).click(); // -> g1-2
  await page.getByRole('button', { name: 'Далее →' }).click(); // -> g1-3 (case.inst, only one linked item)
  await page.locator('.exam-option', { hasText: /^A\. ich$/ }).click(); // wrong (correct is "nimi")
  await finishGrammarModule(page);

  const linkedRow = page.locator('.exam-review-item').nth(2);
  await linkedRow.getByRole('button', { name: 'Потренировать навык' }).click();
  await expect(page).toHaveURL(/#\/training$/);
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /Вспомнил/ }).click();

  await expect(page).toHaveURL(/#\/exam$/);
  await expect(page.getByRole('status').filter({ hasText: 'не хватает связанных вопросов' })).toBeVisible();
});
