import { expect, test, type Page } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const progressKey = 'polski-grammar-srs-kmp-preview-v1';

const revealAnswer = async (page: Page) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
};

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

test('reveal auto-flips the card to face the answer, front stays out of the accessibility tree', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(card.locator('.card-flip-inner')).toHaveClass(/flipped/);
  await expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-front')).toHaveAttribute('aria-hidden', 'true');
});

test('tapping the card flips it back to the question, purely visually, then flips again — never a review, never re-hiding the answer data', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  const flip = card.locator('.card-flip');
  await flip.click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.card-front')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-back')).toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.ratings button')).toHaveCount(2); // rating buttons remain available on the visible face's DOM regardless
  await flip.click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.answer-sentence')).toBeVisible();
  const progress = await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}'), progressKey);
  expect(progress.totalReviews ?? 0).toBe(0);
});

test('clicking a rating button rates and does not get swallowed by the flip handler', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
});

test('typed mode: tapping inside the answer field before reveal never flips; checking the answer flips to the back', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  const textbox = page.getByRole('textbox', { name: 'Ответ по-польски' });
  await textbox.click();
  await textbox.fill('proba');
  await expect(page.locator('.card-flip')).toHaveCount(0); // no flip wrapper exists before reveal at all
  await page.getByRole('button', { name: 'Проверить и показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(card.locator('.card-flip-inner')).toHaveClass(/flipped/);
  await expect(card.locator('.typed-result')).toBeVisible();
});

test('a mouse swipe on the revealed back face rates exactly once; a short drag rates nothing and does not flip either', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  const back = page.getByRole('region', { name: 'Учебная карточка' }).locator('.card-back');
  const bounds = await back.boundingBox();
  expect(bounds).not.toBeNull();
  const hint = back.locator('.vocabulary-swipe-zone');
  await hint.scrollIntoViewIfNeeded();
  const hintBounds = await hint.boundingBox();
  expect(hintBounds).not.toBeNull();
  const y = hintBounds!.y + hintBounds!.height / 2;
  // Short drag: below the 75px rating threshold and above the 10px tap threshold — neither fires.
  await page.mouse.move(bounds!.x + bounds!.width / 2, y);
  await page.mouse.down();
  await page.mouse.move(bounds!.x + bounds!.width / 2 + 30, y, { steps: 5 });
  await page.mouse.up();
  await expect(page.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  expect((await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey))).toBe(0);
  // Real swipe: right = Good/"Вспомнил".
  await page.mouse.move(bounds!.x + 20, y);
  await page.mouse.down();
  await page.mouse.move(bounds!.x + Math.min(bounds!.width - 20, 200), y, { steps: 8 });
  await page.mouse.up();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
});

test('reduced motion (app Motion setting) disables the Rive effect overlay entirely on rate', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', motion: 'Reduced' })));
  await page.goto('/');
  await revealAnswer(page);
  await expect(page.locator('html')).toHaveAttribute('data-motion', 'reduced');
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
  await expect(page.locator('#polski-rive-overlay')).toHaveCount(0);
});

test('normal motion plays the Rive effect overlay as a non-interactive, hidden-from-a11y layer on rate', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  const overlay = page.locator('#polski-rive-overlay');
  await expect(overlay).toHaveAttribute('aria-hidden', 'true');
  await expect(overlay).toHaveCSS('pointer-events', 'none');
  await expect(overlay).toHaveAttribute('data-rive-effect', /^remembered:/);
});

// R1/FC2-01/02/03: the auto-flip-on-reveal must swap faces exactly at the 90° edge-on point, not
// instantly. `?flipDebugScale=20` stretches each 250ms half to 5s (same test-only-timing idea as
// the iOS/macOS POLSKI_FLIP_DEBUG_SCALE env var), so 40%/60% of the total (slowed) duration land
// well inside a single half each, with no flakiness from CI timing jitter.
test('reveal auto-flip swaps faces only past the 90° point (slowed animation, ~40%/~60% samples)', async ({ page }) => {
  await page.goto('/?flipDebugScale=20');
  await revealAnswer(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  // Total slowed duration = 2 * 250ms * 20 = 10_000ms; 40% = 4_000ms (still first half, pre-swap),
  // 60% = 6_000ms (into the second half, post-swap).
  await page.waitForTimeout(4_000);
  await expect(card.locator('.card-back'), 'answer must stay out of the a11y tree before the 90° point').toHaveAttribute('aria-hidden', 'true');
  // `inert` is the actual non-visual guarantee: it removes the subtree from focus/hit-testing and
  // (per the HTML spec) is expected to hide it from assistive tech, regardless of what a generic
  // "is this element painted" heuristic reports for a 3D-rotated, backface-hidden face.
  await expect(card.locator('.card-back')).toHaveAttribute('inert', '');
  await page.waitForTimeout(2_000); // now at 60% of the total duration
  await expect(card.locator('.card-back'), 'answer must be in the a11y tree past the 90° point').not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-back')).not.toHaveAttribute('inert');
  await expect(card.locator('.answer-sentence')).toBeVisible();
});

// Symmetric check for the manual flip-back (front swaps into place only past 90° too).
test('tap-flip-back swaps faces only past the 90° point (slowed animation, ~40%/~60% samples)', async ({ page }) => {
  await page.goto('/?flipDebugScale=20');
  await revealAnswer(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await page.waitForTimeout(10_100); // let the (slowed) auto-reveal flip fully settle first
  await card.locator('.card-flip').click({ position: { x: 10, y: 10 } });
  await page.waitForTimeout(4_000); // 40% of the total 10_000ms duration, still first half
  await expect(card.locator('.card-front'), 'question must stay out of the a11y tree before the 90° point').toHaveAttribute('aria-hidden', 'true');
  await page.waitForTimeout(2_000); // 60% of the total duration
  await expect(card.locator('.card-front'), 'question must be in the a11y tree past the 90° point').not.toHaveAttribute('aria-hidden', 'true');
});

// R3/FC2-06/07: the flip-in-progress ring cue and the two new effect assets (Check/Error, Tada)
// are lazy (never fetched before the first flip) and decorative-only.
test('the flip ring cue is lazy, non-interactive and gated by reduced motion, not just the rating overlay', async ({ page }) => {
  const riveRequests: string[] = [];
  page.on('request', request => { if (request.url().includes('/rive/')) riveRequests.push(request.url()); });
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  expect(riveRequests, 'no Rive asset should load before the first flip').toEqual([]);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect.poll(() => riveRequests.some(u => u.endsWith('rings.riv'))).toBe(true);
  const rings = page.locator('.card-flip-rings');
  await expect(rings).toHaveAttribute('aria-hidden', 'true');
  await expect(rings).toHaveCSS('pointer-events', 'none');
});

// Regression (reviewer-found, v2 correction round): the ring cue's canvas is recreated inside
// `.card-flip` on every renderCard() call, which the router-level `.route-content` teardown
// (`content.textContent = ""`) discards wholesale on essentially every card change. Its cached
// Rive instance (`canvas.__polskiRive`) must be disposed when that happens, or its own
// requestAnimationFrame draw loop runs forever against a detached canvas.
test('ring cue Rive instance is disposed, not leaked, when the card is replaced', async ({ page }) => {
  const riveRequests: string[] = [];
  page.on('request', request => { if (request.url().includes('/rive/')) riveRequests.push(request.url()); });
  await page.goto('/');
  await revealAnswer(page);
  await expect.poll(() => riveRequests.some(u => u.endsWith('rings.riv'))).toBe(true);
  await expect.poll(() => page.evaluate(() => Boolean((document.querySelector('.card-flip-rings canvas') as any)?.__polskiRive)), 'ring instance attaches to the first card\'s canvas').toBe(true);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click(); // advances to the next exercise, discarding .route-content
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
  await expect.poll(() => page.evaluate(() => (window as any).__polskiRiveRingDisposals ?? 0), 'the discarded canvas\'s ring instance must be cleaned up, not orphaned').toBeGreaterThan(0);
});

test('reduced motion also suppresses the flip ring cue (no data-rive-rings attribute ever set)', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', motion: 'Reduced' })));
  await page.goto('/');
  await revealAnswer(page);
  await expect(page.locator('.card-flip-rings')).not.toHaveAttribute('data-rive-rings', /.+/);
});
