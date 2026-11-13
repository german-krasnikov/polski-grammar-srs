import { expect, test, type Locator, type Page } from '@playwright/test';

async function openPart(page: Page, paper: string, module: string) {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      Object.defineProperty(WebAssembly, 'validate', { configurable: true, value: () => false });
    });
  }
  await page.goto('/#/exam');
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
  }
  await page.locator('.exam-card').filter({ hasText: paper }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: module }).getByRole('button', { name: 'Начать' }).click();
}

async function expectReadableReport(page: Page, rows: Locator) {
  await page.evaluate(() => document.fonts.ready);
  for (const width of [375, 320, 1200]) {
    await page.setViewportSize({ width, height: 667 });
    await expect(page.locator('.exam-review-item').first()).toBeVisible();
    const measured = await rows.evaluateAll(elements => elements.flatMap(row => {
      const rowRect = row.getBoundingClientRect();
      return [...row.querySelectorAll('.exam-review-context, .exam-review-chosen, .exam-review-correct, .exam-review-manual, .exam-review-rubric, .exam-review-rubric-component')]
        .map(element => {
          const rect = element.getBoundingClientRect();
          return {
            text: element.textContent?.trim() ?? '',
            rowLeft: rowRect.left,
            rowRight: rowRect.right,
            left: rect.left,
            right: rect.right,
            width: rect.width,
            clientWidth: element.clientWidth,
            scrollWidth: element.scrollWidth,
            viewportWidth: document.documentElement.clientWidth,
          };
        });
    }));
    expect(measured.length).toBeGreaterThan(0);
    for (const part of measured) {
      expect(part.text).not.toBe('');
      expect(part.left, `${width}px ${part.text}`).toBeGreaterThanOrEqual(part.rowLeft - 1);
      expect(part.right, `${width}px ${part.text}`).toBeLessThanOrEqual(Math.min(part.rowRight, part.viewportWidth) + 1);
      expect(part.scrollWidth, `${width}px ${part.text}`).toBeLessThanOrEqual(part.clientWidth + 1);
      expect(part.width, `${width}px ${part.text}`).toBeGreaterThan(100);
    }
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(width + 1);
  }
}

test('Grammar report keeps wrong choice, source and blank in bounds at 320px', async ({ page }) => {
  test.setTimeout(120_000);
  await openPart(page, 'B1 Certyfikat 2017', 'Poprawność gramatyczna');
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  for (let i = 0; i < 49; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  const rows = page.locator('.exam-review-item');
  await expect(rows.first()).toContainText('Ваш ответ: dobry teatr');
  await expect(rows.first()).toContainText('dobrego teatru');
  await expect(rows.nth(1)).toContainText('Ответ не введён');
  await expectReadableReport(page, rows.filter({ has: page.locator('.exam-review-correct') }).first());
});

test('C1 Grammar source rubric wraps within the review row at 320px', async ({ page }) => {
  test.setTimeout(120_000);
  await openPart(page, 'C1 Certyfikat 2022-11', 'Poprawność gramatyczna');
  for (let i = 0; i < 74; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  const rubricRow = page.locator('.exam-review-item').filter({ has: page.locator('.exam-review-rubric') }).first();
  await expect(rubricRow.locator('.exam-review-rubric-component')).toHaveCount(4);
  await expect(rubricRow).toContainText('zainteresowania');
  await expectReadableReport(page, rubricRow);
});

test('Reading report keeps wrong choice and source in bounds at 320px', async ({ page }) => {
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  await page.locator('.exam-reading-q').first().getByRole('button', { name: 'kioski internetowe są popularne w całym kraju' }).click();
  for (let i = 0; i < 4; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  const rows = page.locator('.exam-review-item');
  await expect(rows.first()).toContainText('Ваш ответ: kioski internetowe są popularne w całym kraju');
  await expect(rows.first()).toContainText('kioski internetowe oferują dostęp do Internetu');
  await expect(rows.nth(1)).toContainText('Ответ не введён');
  await expectReadableReport(page, rows.first());
});

test('Listening partial credit and pending review remain readable at 320px', async ({ page }) => {
  await openPart(page, 'B2 Certyfikat 2017', 'Rozumienie ze słuchu');
  for (let i = 0; i < 3; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  const set = page.locator('.exam-reading-q').first();
  await set.getByRole('button', { name: '1', exact: true }).click();
  await set.getByRole('button', { name: '3', exact: true }).click();
  await page.getByRole('button', { name: 'Далее →' }).click();
  const manual = page.locator('.exam-reading-q').first();
  await manual.locator('.exam-input').fill('uprzejmą');
  await manual.getByRole('button', { name: 'Проверить', exact: true }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  const partial = page.locator('.exam-review-item.partial').first();
  const pending = page.locator('.exam-review-item.pending').first();
  await expect(partial).toContainText('1|3');
  await expect(partial).toContainText('+1 / 2');
  await expect(pending).toContainText('uprzejmą');
  await expect(pending).toContainText('Требуется ручная проверка');
  await expectReadableReport(page, partial);
  await expectReadableReport(page, pending);
});
