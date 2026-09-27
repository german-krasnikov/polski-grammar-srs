import { expect, test } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const legacyKey = 'polski-grammar-srs-v1';
const previewKey = 'polski-grammar-srs-kmp-preview-v1';
const backupKey = 'polski-grammar-srs-kmp-legacy-backup-v1';
const markerKey = 'polski-grammar-srs-kmp-migrated-v1';
const fixture = JSON.parse(readFileSync(resolve('tests/fixtures/kotlin-parity/progress.json'), 'utf8')) as {
  cases: { id: string; expected: unknown }[];
};
const freshReactProgress = JSON.stringify(fixture.cases.find(entry => entry.id === 'P-fresh')!.expected);

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

test('matrix keeps seven cases, seven comparison nouns and nine verb subjects', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.getByRole('button', { name: 'Падежи и окончания' }).click();
  await expect(page.getByRole('heading', { name: 'Все семь падежей на одной группе слов' })).toBeVisible();
  await expect(page.locator('.matrix-section').first().getByRole('row')).toHaveCount(8);
  await expect(page.locator('.comparison-table').getByRole('columnheader')).toHaveCount(8);
  await page.locator('#matrix-noun').selectOption('friendM');
  await page.locator('#matrix-adjective').selectOption('good');
  await page.locator('#matrix-owner').selectOption('their');
  await page.locator('#matrix-number').selectOption('pl');
  await expect(page.locator('.matrix-section').first().getByRole('rowheader', { name: /Biernik/ })).toBeVisible();
  await expect(page.locator('.comparison-table').getByRole('row')).toHaveCount(8);

  await page.getByRole('button', { name: 'Времена и лица' }).click();
  await expect(page.getByRole('heading', { name: 'Лицо × число × время' })).toBeVisible();
  await expect(page.locator('.matrix-section').first().getByRole('row')).toHaveCount(10);
  await page.locator('#matrix-verb').selectOption('be');
  await expect(page.locator('.matrix-section').first().getByRole('rowheader', { name: /ona/ })).toBeVisible();
  await page.getByRole('button', { name: 'Местоимения' }).click();
  await expect(page.getByRole('heading', { name: 'Владелец меняется независимо от падежа' })).toBeVisible();
  await expect(page.locator('.matrix-section').last().getByRole('row')).toHaveCount(8);
});

test('matrix pipeline keeps both compact lines and opens its chain drill', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  const pipeline = page.locator('.matrix-section').filter({ has: page.getByRole('heading', { name: 'Сначала конструкция, затем формы' }) });
  await expect(pipeline.locator(':scope > p')).toHaveText([
    'Что хочу сказать? → Какой падеж нужен? → Меняю всю группу',
    'widzę → Biernik → moją + piękną + żonę',
  ]);
  await page.getByRole('button', { name: 'Тренировать эту цепочку' }).click();
  await expect(page.getByRole('region', { name: 'Учебная карточка' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Цепочка предложений' })).toHaveAttribute('aria-pressed', 'true');
});

test('matrix drill opens the chosen training card and preserves reference context', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.getByRole('button', { name: 'Тренировать мужской род' }).click();
  await expect(page.getByRole('button', { name: 'Карточки' })).toHaveAttribute('aria-pressed', 'true');
  await expect(page.getByText('Тренировка навыка')).toBeVisible();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Таблица под рукой' }).click();
  await expect(page.getByRole('heading', { name: 'Таблица этого предложения' })).toBeVisible();
  await expect(page.locator('.reference-panel').getByRole('row')).toHaveCount(8);
});

test('progress survives reload, exports the current document and confirms reset', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await expect(page.locator('.bigstats').getByText('1', { exact: true }).first()).toBeVisible();
  await expect(page.locator('.progress-page table').getByRole('row')).toHaveCount(17);
  await expect.poll(() => page.evaluate(key => localStorage.getItem(key), previewKey)).not.toBeNull();
  await page.reload();
  await page.getByRole('button', { name: 'Прогресс', exact: true }).click();
  await expect(page.locator('.bigstats').getByText('1', { exact: true }).first()).toBeVisible();

  const downloadPromise = page.waitForEvent('download');
  await page.locator('#progress-export').click();
  const download = await downloadPromise;
  const exported = JSON.parse(readFileSync((await download.path())!, 'utf8')) as { version: number; totalReviews: number };
  expect(exported.version).toBe(1);
  expect(exported.totalReviews).toBe(1);

  page.once('dialog', dialog => dialog.dismiss());
  await page.locator('#progress-reset').click();
  await expect(page.locator('.bigstats').getByText('1', { exact: true }).first()).toBeVisible();
  page.once('dialog', dialog => dialog.accept());
  await page.locator('#progress-reset').click();
  await expect(page.getByRole('button', { name: 'Карточки' })).toHaveAttribute('aria-pressed', 'true');
  await expect(page.getByText('Цепочка · 1 / 5')).toBeVisible();
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await expect(page.locator('.bigstats').getByText('0', { exact: true }).first()).toBeVisible();
});

test('legacy progress waits for explicit migration and keeps its exact backup', async ({ page }) => {
  await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw), [legacyKey, freshReactProgress] as const);
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Найден прежний прогресс' })).toBeVisible();
  expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).toBeNull();
  await page.getByRole('button', { name: 'Перенести прогресс' }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
  expect(await page.evaluate(key => localStorage.getItem(key), legacyKey)).toBe(freshReactProgress);
  expect(await page.evaluate(key => localStorage.getItem(key), backupKey)).toBe(freshReactProgress);
  expect(await page.evaluate(key => localStorage.getItem(key), markerKey)).not.toBeNull();
  expect(await page.evaluate(key => localStorage.getItem(key), previewKey)).not.toBeNull();
});

test('320px large text keeps the page bounded and lets the comparison table scroll', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto('/');
  await page.evaluate(() => { document.documentElement.style.fontSize = '20px'; });
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.getByRole('button', { name: 'Падежи и окончания' }).click();
  const comparison = page.locator('.comparison-table').locator('..');
  await expect(comparison).toBeVisible();
  await comparison.evaluate(element => { element.scrollLeft = element.scrollWidth; });
  expect(await comparison.evaluate(element => element.scrollLeft)).toBeGreaterThan(0);
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(320);
  await page.locator('#matrix-owner').focus();
  await page.keyboard.press('Tab');
  await expect(page.locator('#matrix-number')).toBeFocused();
  expect(await page.locator('#matrix-number').evaluate(element => getComputedStyle(element).outlineStyle)).not.toBe('none');
});

// W3 (EmphasisUXAudit E6, using shared C2 `ReferenceSystemCard.steps`): the system-map cards
// highlight each step-to-step change from the pack's own explicit steps chain — never re-parsed
// from the joined `example` arrow string — with the same `change-after` token every other "Стало"
// surface uses. The origin step stays plain; each later step marks only its own change from the
// step right before it.
test('system-map cards highlight each step transition from the pack\'s own steps chain', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  const cards = page.locator('.system-grid article');
  await expect(cards).toHaveCount(4);

  const noun = cards.filter({ hasText: 'żona' });
  await expect(noun.locator('code')).toHaveText('żona → żonę → żony');
  const nounHighlights = noun.locator('code .change-after');
  await expect(nounHighlights).toHaveCount(2);
  await expect(nounHighlights.nth(0)).toHaveText('ę');
  await expect(nounHighlights.nth(1)).toHaveText('y');

  const modifiers = cards.nth(3);
  await expect(modifiers.locator('code')).toHaveText('Widzę… → Nie widzę… → Czy widzę…?');
  const modifierHighlights = modifiers.locator('code .change-after');
  await expect(modifierHighlights).toHaveCount(2);
  await expect(modifierHighlights.nth(0)).toHaveText('Nie');
  await expect(modifierHighlights.nth(1)).toHaveText('Czy');
});
