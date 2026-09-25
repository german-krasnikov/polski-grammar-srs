import { expect, test, type Locator, type Page } from '@playwright/test';

const storageKey = 'polski-grammar-srs-kmp-spike-v1';
const pageErrors = new WeakMap<Page, string[]>();
const loadedScripts = new WeakMap<Page, string[]>();
const expectedBranch = process.env.KOTLIN_SPIKE_BRANCH;

async function clickAtVisualBounds(page: Page, target: Locator): Promise<void> {
  const bounds = await target.boundingBox();
  expect(bounds).not.toBeNull();
  await page.mouse.click(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
}

test.beforeEach(async ({ page }) => {
  const errors: string[] = [];
  const scripts: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('request', request => {
    if (/origin(?:Wasm|Js)ComposeApp\.js$/.test(request.url())) scripts.push(request.url());
  });
  pageErrors.set(page, errors);
  loadedScripts.set(page, scripts);
  if (expectedBranch === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) {
          return false;
        }
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  await page.goto('/');
});

test.afterEach(async ({ page }) => {
  expect(pageErrors.get(page)).toEqual([]);
});

test('loads the selected production runtime branch', async ({ page }) => {
  const expectedScript = expectedBranch === 'js' ? 'originJsComposeApp.js' : 'originWasmComposeApp.js';
  await expect.poll(() => loadedScripts.get(page)).toEqual([expect.stringContaining(expectedScript)]);
  await expect(page.getByRole('button', { name: 'Pokaż odpowiedź' })).toBeVisible();
});

test('gives the answer textbox an accessible name', async ({ page }) => {
  await expect(page.getByRole('textbox', { name: 'Twoja odpowiedź' })).toBeVisible();
});

test('canvas pointer actions round-trip the temporary state through storage', async ({ page }) => {
  // Compose places accessibility nodes over a canvas. Use their visual bounds to
  // exercise actual pointer targeting, without dispatching an artificial click.
  await clickAtVisualBounds(page, page.getByRole('textbox'));
  await page.keyboard.insertText('Mówię po polsku');
  await expect.poll(() => page.evaluate(key => localStorage.getItem(key), storageKey)).toContain('Mówię po polsku');
  await clickAtVisualBounds(page, page.getByRole('button', { name: 'Pokaż odpowiedź' }));
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toBeVisible();
  await clickAtVisualBounds(page, page.getByRole('button', { name: 'Good', exact: true }));
  await expect(page.getByText('Ocena: GOOD')).toBeVisible();
  await page.reload();
  await expect(page.getByText('Ocena: GOOD')).toBeVisible();
  await expect.poll(() => page.evaluate(key => localStorage.getItem(key), storageKey)).toContain('Mówię po polsku');
});

test('accepts Polish text through the browser input and keeps the answer hidden', async ({ page }) => {
  const answer = page.getByRole('textbox');
  await expect(answer).toBeVisible();
  await answer.click();
  await page.keyboard.insertText('Zażółć gęślą jaźń');
  await expect(answer).toHaveValue('Zażółć gęślą jaźń');
  await expect(page.getByText('Я говорю по-польски')).toBeVisible();
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Pokaż odpowiedź' })).toBeVisible();
});

for (const rating of ['Again', 'Hard', 'Good', 'Easy']) {
  test(`reveals the hidden answer and records ${rating}`, async ({ page }) => {
    const reveal = page.getByRole('button', { name: 'Pokaż odpowiedź' });
    await reveal.click();
    await expect(page.getByText('Odpowiedź: Mówię po polsku')).toBeVisible();
    await expect(reveal).toHaveCount(0);
    for (const choice of ['Again', 'Hard', 'Good', 'Easy']) {
      await expect(page.getByRole('button', { name: choice, exact: true })).toBeVisible();
    }
    await page.getByRole('button', { name: rating, exact: true }).click();
    await expect(page.getByText(`Ocena: ${rating.toUpperCase()}`)).toBeVisible();
  });
}

test('restores typed answer and rating from localStorage after reload', async ({ page }) => {
  const answer = page.getByRole('textbox');
  await answer.click();
  await page.keyboard.insertText('Mówię po polsku');
  await page.getByRole('button', { name: 'Pokaż odpowiedź' }).click();
  await page.getByRole('button', { name: 'Hard', exact: true }).click();
  await expect.poll(() => page.evaluate(key => localStorage.getItem(key), storageKey)).not.toBeNull();
  await page.reload();
  await expect(page.getByRole('textbox')).toHaveValue('Mówię po polsku');
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toBeVisible();
  await expect(page.getByText('Ocena: HARD')).toBeVisible();
});

test('keeps editing keys in the answer and supports keyboard activation', async ({ page }) => {
  const answer = page.getByRole('textbox');
  await answer.focus();
  await page.keyboard.insertText('Mówię');
  await page.keyboard.press('Space');
  await page.keyboard.type('1');
  await expect(answer).toHaveValue('Mówię 1');
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toHaveCount(0);

  const reveal = page.getByRole('button', { name: 'Pokaż odpowiedź' });
  await reveal.focus();
  await expect(reveal).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toBeVisible();
  const good = page.getByRole('button', { name: 'Good', exact: true });
  await good.focus();
  await page.keyboard.press('Space');
  await expect(page.getByText('Ocena: GOOD')).toBeVisible();
});

test('Shift+Enter inserts a line break and Enter completes the typed answer', async ({ page }) => {
  const answer = page.getByRole('textbox');
  await answer.focus();
  await page.keyboard.insertText('Mówię');
  await page.keyboard.press('Shift+Enter');
  await page.keyboard.insertText('po polsku');
  await expect(answer).toHaveValue('Mówię\npo polsku');
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toHaveCount(0);
  await page.keyboard.press('Enter');
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toBeVisible();
});

test('Tab reaches the input, reveal and rating without pointer input', async ({ page }) => {
  const answer = page.getByRole('textbox', { name: 'Twoja odpowiedź' });
  for (let attempt = 0; attempt < 3 && !(await answer.evaluate(element => document.activeElement === element)); attempt += 1) {
    await page.keyboard.press('Tab');
  }
  await expect(answer).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(page.getByRole('button', { name: 'Pokaż odpowiedź' })).toBeFocused();
  await page.keyboard.press('Space');
  await expect(page.getByText('Odpowiedź: Mówię po polsku')).toBeVisible();
  const again = page.getByRole('button', { name: 'Again', exact: true });
  for (let attempt = 0; attempt < 4 && !(await again.evaluate(element => document.activeElement === element)); attempt += 1) {
    await page.keyboard.press('Tab');
  }
  await expect(again).toBeFocused();
  await page.keyboard.press('Space');
  await expect(page.getByText('Ocena: AGAIN')).toBeVisible();
});

test('keeps controls usable at 320 px, in landscape and with reduced motion', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.evaluate(() => { document.documentElement.style.fontSize = '20px'; });
  const answer = page.getByRole('textbox', { name: 'Twoja odpowiedź' });
  await expect(answer).toBeVisible();
  await expect(page.getByRole('button', { name: 'Pokaż odpowiedź' })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(320);
  await page.setViewportSize({ width: 700, height: 320 });
  await page.getByRole('button', { name: 'Pokaż odpowiedź' }).click();
  await page.getByRole('button', { name: 'Easy', exact: true }).click();
  await expect(page.getByText('Ocena: EASY')).toBeVisible();
});

test('exposes seven table rows with row and column headers while scrolling', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await expect(page.getByText('Przypadki / Падежи')).toBeVisible();
  const table = page.getByRole('table');
  await expect(table).toBeVisible();
  await expect(table.getByRole('columnheader', { name: 'Przypadek' })).toBeVisible();
  await expect(table.getByRole('columnheader', { name: 'Forma' })).toBeVisible();
  await expect(table.getByRole('rowheader')).toHaveCount(7);
  await expect(table.getByRole('rowheader', { name: 'Miejscownik' })).toBeVisible();
  await expect(table.getByRole('cell', { name: 'kocie' })).toHaveCount(2);
  const scroller = table.locator('xpath=..');
  const maximumScroll = await scroller.evaluate(element => element.scrollWidth - element.clientWidth);
  expect(maximumScroll).toBeGreaterThan(0);
  const bounds = await scroller.boundingBox();
  expect(bounds).not.toBeNull();
  await page.mouse.move(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
  await page.mouse.wheel(maximumScroll + 100, 0);
  await expect.poll(() => scroller.evaluate(element => element.scrollLeft)).toBeGreaterThan(0);
  await expect(table.getByRole('rowheader', { name: 'Wołacz' })).toBeVisible();
});
