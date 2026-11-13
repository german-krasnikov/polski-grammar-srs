import { expect, test, type Page } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';
import { installProgressReader } from './kotlin-storage';

const progressKey = 'polski-grammar-srs-kmp-preview-v1';

test.beforeEach(async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH !== 'js') return;
  await page.addInitScript(() => {
    const validate = WebAssembly.validate;
    WebAssembly.validate = function (bytes: BufferSource): boolean {
      if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
      return validate.call(WebAssembly, bytes);
    };
  });
});

async function rateTrainingCard(page: Page): Promise<void> {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
}

test('P16 rating overlay is cleared when the learner leaves the card for Matrix', async ({ page }) => {
  await rateTrainingCard(page);
  await expect(page.locator('#polski-rive-overlay')).toBeVisible();
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  await expect(page.locator('.matrix-page')).toBeVisible();
  // Inspect the new route immediately; waiting for the old animation to expire
  // would conceal the stale overlay this scenario reproduces.
  expect(await page.locator('#polski-rive-overlay').isVisible()).toBe(false);
});

test('P16 failed effect metadata loading cannot leave a permanent rating overlay', async ({ page }) => {
  await page.route('**/rive/rive-effects.generated.js', route => route.abort('failed'));
  await rateTrainingCard(page);
  // A network error must clean up independently of Rive's completion callback.
  await expect(page.locator('#polski-rive-overlay')).toBeHidden({ timeout: 4_000 });
});

test('P16 failed bridge loading cannot strand a rating cue', async ({ page }) => {
  await page.route('**/rive/rive-bridge.js', route => route.abort('failed'));
  await rateTrainingCard(page);
  await expect(page.locator('#polski-rive-overlay')).toBeHidden({ timeout: 3_200 });
});

test('P16 a stalled bridge request has bounded overlay cleanup', async ({ page }) => {
  let release!: () => void;
  const stalled = new Promise<void>(resolve => { release = resolve; });
  await page.route('**/rive/rive-bridge.js', async route => {
    await stalled;
    await route.continue();
  });
  try {
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    await continueIntroductionIfPresent(page);
    await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
    await page.getByRole('button', { name: /2 Вспомнил/ }).click();
    await expect(page.locator('#polski-rive-overlay')).toBeHidden({ timeout: 3_200 });
  } finally {
    release();
  }
});

test('P16 a later rating can play after a transient effect metadata failure', async ({ page }) => {
  const asset = '**/rive/rive-effects.generated.js';
  await page.route(asset, route => route.abort('failed'));
  await rateTrainingCard(page);
  await expect(page.locator('#polski-rive-overlay')).toBeHidden();
  await page.unroute(asset);
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.locator('#polski-rive-overlay')).toBeVisible();
});

test('P16 a later rating retries a bridge that previously failed to load', async ({ page }) => {
  const asset = '**/rive/rive-bridge.js';
  await page.route(asset, route => route.abort('failed'));
  await rateTrainingCard(page);
  await expect(page.locator('#polski-rive-overlay')).toBeHidden();
  await page.unroute(asset);
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.locator('#polski-rive-overlay')).toBeVisible();
});

test('P16 a delayed effect load cannot revive an overlay after leaving the route', async ({ page }) => {
  let releaseLoad!: () => void;
  const blocked = new Promise<void>(resolve => { releaseLoad = resolve; });
  await page.route('**/rive/rive-effects.generated.js', async route => {
    await blocked;
    await route.continue();
  });
  await rateTrainingCard(page);
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  await expect(page.locator('.matrix-page')).toBeVisible();
  releaseLoad();
  await expect(page.locator('#polski-rive-overlay')).toBeHidden();
});

test('P16 system reduced motion cancels an already active rating cue', async ({ page }) => {
  await rateTrainingCard(page);
  await expect(page.locator('#polski-rive-overlay')).toBeVisible();
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await expect(page.locator('html')).toHaveAttribute('data-motion', 'reduced');
  await expect(page.locator('#polski-rive-overlay')).toBeHidden();
});

test('P16 returning to Cards permits a new cue and each rating records one review', async ({ page }) => {
  await installProgressReader(page);
  await rateTrainingCard(page);
  await expect.poll(() => page.evaluate(key => (window as any).__readProgress(key, 'totalReviews'), progressKey)).toBe(1);
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  await expect(page.locator('#polski-rive-overlay')).toBeHidden();
  await page.getByRole('button', { name: 'Карточки', exact: true }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.locator('#polski-rive-overlay')).toBeVisible();
  await expect.poll(() => page.evaluate(key => (window as any).__readProgress(key, 'totalReviews'), progressKey)).toBe(2);
});

test('P16 an ordinary rating still loads its authored Rive asset and canvas', async ({ page }) => {
  const loadedAssets: string[] = [];
  page.on('response', response => {
    if (response.ok() && response.url().endsWith('/rive/again.riv')) loadedAssets.push(response.url());
  });
  await rateTrainingCard(page);
  await expect.poll(() => loadedAssets.length).toBeGreaterThan(0);
  await expect.poll(() => page.locator('#polski-rive-overlay canvas').first().evaluate(canvas => (canvas as HTMLCanvasElement).width)).toBeGreaterThan(1);
});

test('P16 a second rapid rating is not cut short by the first cue timer', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'The bounded real-time timer window is measured in Chromium.');
  await rateTrainingCard(page);
  const overlay = page.locator('#polski-rive-overlay');
  await expect(overlay).toBeVisible();
  const firstCue = await overlay.getAttribute('data-rive-effect');
  const firstAt = await page.evaluate(() => performance.now());
  await page.waitForTimeout(800);
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(overlay).toBeVisible();
  await expect(overlay).not.toHaveAttribute('data-rive-effect', firstCue ?? '');
  const elapsed = await page.evaluate(start => performance.now() - start, firstAt);
  expect(elapsed).toBeLessThan(2_200);
  await page.waitForTimeout(Math.max(0, 2_850 - elapsed));
  expect(await overlay.isVisible()).toBe(true);
});

test('P16 finishing a real five-card chain still plays its authored celebration', async ({ page }) => {
  const chainAssets: string[] = [];
  page.on('response', response => {
    if (response.ok() && response.url().endsWith('/rive/chain-complete.riv')) chainAssets.push(response.url());
  });
  await page.goto('/');
  for (let step = 0; step < 5; step += 1) {
    await continueIntroductionIfPresent(page);
    await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
    await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  }
  await expect(page.getByRole('heading', { name: 'Цепочка завершена' })).toBeVisible();
  await expect(page.locator('#polski-rive-chain')).toBeVisible();
  await expect.poll(() => chainAssets.length).toBeGreaterThan(0);
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  await expect(page.locator('.matrix-page')).toBeVisible();
  expect(await page.locator('#polski-rive-chain').isVisible()).toBe(false);
});
