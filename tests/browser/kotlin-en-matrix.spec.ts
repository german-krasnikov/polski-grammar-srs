import { expect, test, type Locator } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// EN-24 (UC-09 part 2/2 minimum, Plans/Kotlin/EnRuPackPlan.md §6): the one live English matrix
// table on the web host, read from `lang/en/forms.generated.json` through `MatrixTableViewModel` —
// not a mock; real irregular ("see"→"saw") and do-support ("do"/"does") forms.
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

async function tableRows(section: Locator): Promise<string[][]> {
  return section.locator('tbody tr').evaluateAll((trs) =>
    trs.map((tr) => Array.from(tr.querySelectorAll('th,td')).map((c) => c.textContent!.replace(/\s+/g, ' ').trim())),
  );
}

test('English person × tense table shows real, irregular forms.generated.json(en) values', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.click('#matrix-section-verbs');

  const enSection = page.locator('section.matrix-section').filter({ has: page.getByRole('heading', { name: /English/ }) });
  await expect(enSection).toBeVisible();
  const rows = await tableRows(enSection);
  expect(rows).toHaveLength(7);
  // Each cell's visible text is "Было:X→Стало:Y" (the a11y summary is aria-hidden but still in
  // textContent) — asserting the "→Стало:<form>" suffix reads the real, visible answer only.
  const bySubject = Object.fromEntries(rows.map((r: string[]) => [r[0], r]));
  expect(bySubject['I'][1]).toMatch(/→Стало:see$/);
  expect(bySubject['I'][2]).toMatch(/→Стало:saw$/);
  expect(bySubject['I'][3]).toMatch(/→Стало:will see$/);
  expect(bySubject['he'][1]).toMatch(/→Стало:sees$/);
  expect(bySubject['he'][2]).toMatch(/→Стало:saw$/);
  expect(bySubject['he'][3]).toMatch(/→Стало:will see$/);
  expect(bySubject['we'][1]).toMatch(/→Стало:see$/);

  // English text must not be tagged lang="pl" (ContrastHighlightPlan.md a11y: the same
  // "Было"/"Стало" spans pl's own tables use, but pl's `lang` default would be wrong here).
  await expect(enSection.locator('.form-contrast-after[lang="en"]').first()).toBeVisible();
  await expect(enSection.locator('[lang="pl"]')).toHaveCount(0);
});

test('do-support table shows do/does present split, invariant "did", and no future do-support', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.click('#matrix-section-verbs');

  const doSection = page.locator('section.matrix-section').filter({ has: page.getByRole('heading', { name: /do-support/ }) });
  await expect(doSection).toBeVisible();
  const rows = await tableRows(doSection);
  const bySubject = Object.fromEntries(rows.map((r: string[]) => [r[0], r]));
  expect(bySubject['I'][1]).toMatch(/→Стало:do$/);
  expect(bySubject['he'][1]).toMatch(/→Стало:does$/);
  expect(bySubject['I'][2]).toMatch(/→Стало:did$/);
  expect(bySubject['he'][2]).toMatch(/→Стало:did$/);
  rows.forEach((r: string[]) => expect(r[3]).not.toMatch(/\bdid\b|\bdoes?\b/));
});
