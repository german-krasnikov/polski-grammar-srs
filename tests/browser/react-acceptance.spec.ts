import { expect, test, type Page } from '@playwright/test';

const continueIntroductionIfPresent = async (page: Page) => {
  const next = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await next.count()) await next.click();
};
const revealGrammarAnswer = async (page: Page) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: /Показать ответ/ }).click();
};

const key = 'polski-grammar-srs-v1';
const errorsByPage = new WeakMap<Page, string[]>();

test.beforeEach(async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
  errorsByPage.set(page, errors);
  await page.clock.setFixedTime(new Date('2026-02-03T12:00:00.000Z'));
});
test.afterEach(async ({ page }) => {
  expect(errorsByPage.get(page)).toEqual([]);
});

test('standard keyboard activation grades once and advances the card', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  const reveal = page.getByRole('button', { name: /Показать ответ/ });
  await reveal.focus();
  await page.keyboard.press('Enter');
  await expect(page.locator('.answer-sentence')).toHaveText('Widzę moją piękną żonę.');
  const rating = page.getByRole('button', { name: /2 Вспомнил/ });
  await rating.focus();
  await page.keyboard.press('Space');
  await expect(page.locator('.source-sentence')).toHaveText('Widzę moją piękną żonę.');
  await expect.poll(() => page.evaluate(storageKey => JSON.parse(localStorage.getItem(storageKey)!).totalReviews, key)).toBe(1);
  await page.keyboard.press('Space');
  expect(await page.evaluate(storageKey => JSON.parse(localStorage.getItem(storageKey)!).totalReviews, key)).toBe(1);
});

test('failed save preserves the reviewed state for export in the open session', async ({ page }) => {
  await page.addInitScript(storageKey => {
    const original = Storage.prototype.setItem;
    Storage.prototype.setItem = function (name, value) {
      if (name === storageKey) throw new DOMException('quota', 'QuotaExceededError');
      return original.call(this, name, value);
    };
  }, key);
  await page.goto('/');
  await revealGrammarAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.getByRole('alert')).toContainText('Экспортируй JSON');
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await expect(page.locator('.bigstats b').first()).toHaveText('1');
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Экспорт JSON' }).click();
  const download = await downloadPromise;
  const stream = await download.createReadStream();
  const chunks: Buffer[] = [];
  for await (const chunk of stream) chunks.push(Buffer.from(chunk));
  const exported = JSON.parse(Buffer.concat(chunks).toString());
  expect(exported).toMatchObject({ totalReviews: 1, reviewsToday: 1 });
  expect(exported.stats['case.acc.f']).toMatchObject({ reviews: 1, correct: 1 });
  expect(await page.evaluate(storageKey => localStorage.getItem(storageKey), key)).toBeNull();
});

test('a pointer wheel scrolls the narrow comparison table to its last column', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await page.goto('/');
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  await page.getByRole('button', { name: 'Падежи и окончания' }).click();
  const scroller = page.locator('.comparison-table').locator('..');
  await scroller.scrollIntoViewIfNeeded();
  const box = await scroller.boundingBox();
  expect(box).not.toBeNull();
  const maximumScroll = await scroller.evaluate(element => element.scrollWidth - element.clientWidth);
  expect(maximumScroll).toBeGreaterThan(0);
  await page.mouse.move(box!.x + box!.width / 2, box!.y + box!.height / 2);
  await page.mouse.wheel(maximumScroll + 1000, 0);
  await expect.poll(() => scroller.evaluate(element => element.scrollLeft)).toBeGreaterThanOrEqual(maximumScroll - 1);
  const lastCell = await page.locator('.comparison-table tbody tr').first().locator('td').last().boundingBox();
  const visibleScroller = await scroller.boundingBox();
  expect(lastCell).not.toBeNull();
  expect(visibleScroller).not.toBeNull();
  expect(lastCell!.x).toBeGreaterThanOrEqual(visibleScroller!.x - 1);
  expect(lastCell!.x + lastCell!.width).toBeLessThanOrEqual(visibleScroller!.x + visibleScroller!.width + 1);
});
