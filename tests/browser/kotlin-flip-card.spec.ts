import { expect, test, type Page } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const progressKey = 'polski-grammar-srs-kmp-preview-v1';
const vocabularyKey = 'polski-vocabulary-pl-ru-v1';

const revealAnswer = async (page: Page) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
};

const openVocabularyCard = async (page: Page) => {
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('checkbox').first().check();
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

// v3/A: the training ("Карточки") card no longer flips at all — the answer expands downward
// below the still-visible question.

test('reveal expands the answer downward; the question stays visible and the answer was never in the DOM before reveal', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(card.locator('.card-answer-wrap')).toHaveCount(0);
  await expect(card.locator('.card-flip')).toHaveCount(0); // no flip wrapper exists on this card at all
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(card.locator('.card-front')).toBeVisible(); // question stays on screen
  await expect(card.locator('.card-answer-wrap')).toHaveClass(/expanded/);
  await expect(card.locator('.answer-sentence')).toBeVisible();
  await expect(card.locator('.card-flip')).toHaveCount(0); // still no flip, ever
});

test('clicking the question card reveals exactly once, like the button/Space; a second click on the revealed card does nothing extra', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await card.locator('.card-front').click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.answer-sentence')).toBeVisible();
  await card.locator('.card-front').click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.answer-sentence')).toBeVisible(); // still revealed, nothing toggled back
  const progress = await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}'), progressKey);
  expect(progress.totalReviews ?? 0).toBe(0); // a click on the card is never a rating
});

test('Space still reveals the question card', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await page.keyboard.press('Space');
  await expect(card.locator('.answer-sentence')).toBeVisible();
});

test('clicking a rating button rates the revealed card', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
});

test('a mouse swipe on the revealed answer rates exactly once; a short drag rates nothing', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  // P1-8 fix note: the reveal's own expand transition (0fr→1fr, 550ms) is still running right
  // after the click resolves — wait for it to settle before measuring, or the boundingBox below
  // (and every pixel offset computed from it) reads a mid-animation, not-yet-final layout.
  await page.waitForTimeout(700);
  const hint = card.locator('.vocabulary-swipe-zone');
  await hint.scrollIntoViewIfNeeded();
  const hintBounds = await hint.boundingBox();
  expect(hintBounds).not.toBeNull();
  const y = hintBounds!.y + hintBounds!.height / 2;
  const startX = hintBounds!.x + 10;
  await page.mouse.move(startX, y);
  await page.mouse.down();
  await page.mouse.move(startX + 30, y, { steps: 5 });
  await page.mouse.up();
  expect((await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey))).toBe(0);
  await page.waitForTimeout(300); // let the short drag's own snap-back settle first
  await page.mouse.move(startX, y);
  await page.mouse.down();
  await page.mouse.move(startX + 200, y, { steps: 8 });
  await page.mouse.up();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
});

test('reduced motion (app Motion setting): the expand is instant and rating never creates the Rive overlay', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', motion: 'Reduced' })));
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await expect(page.locator('html')).toHaveAttribute('data-motion', 'reduced');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const wrap = page.getByRole('region', { name: 'Учебная карточка' }).locator('.card-answer-wrap');
  await expect(wrap).toHaveCSS('transition-duration', '0s');
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews ?? 0, progressKey)).toBe(1);
  await expect(page.locator('#polski-rive-overlay')).toHaveCount(0);
});

test('normal motion plays the rating Rive effect as a non-interactive, hidden-from-a11y layer', async ({ page }) => {
  await page.goto('/');
  await revealAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  const overlay = page.locator('#polski-rive-overlay');
  await expect(overlay).toHaveAttribute('aria-hidden', 'true');
  await expect(overlay).toHaveCSS('pointer-events', 'none');
  await expect(overlay).toHaveAttribute('data-rive-effect', /^remembered:/);
});

// v3/A/D: the reveal ring accent (reused rings.riv) pulses once, decoratively, behind the card.
// v3/D: the runtime is warmed shortly after the first render (requestIdleCallback, or its
// setTimeout fallback), not held back until the first real effect — this supersedes the old v2
// "never fetched before the first flip" expectation, which the new prewarm makes obsolete.
test('Rive is prewarmed shortly after first render when Animations is on, before any card interaction', async ({ page }) => {
  const riveRequests: string[] = [];
  page.on('request', request => { if (request.url().includes('/rive/')) riveRequests.push(request.url()); });
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await expect.poll(() => riveRequests.some(u => u.endsWith('rive-bridge.js')), { timeout: 5_000 }).toBe(true);
});

// v3/C: the explicit Settings → Animations toggle, off, keeps Rive entirely unloaded — no
// prewarm, no rive.js/rive.wasm/*.riv requests — through reveal, vocabulary flip and rating.
test('Animations off: zero Rive network requests through reveal, flip and rating on either card', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 2, coursePair: 'pl-ru', animationsEnabled: false })));
  const riveRequests: string[] = [];
  page.on('request', request => { if (request.url().includes('/rive/')) riveRequests.push(request.url()); });
  await page.goto('/');
  await expect(page.locator('html')).toHaveAttribute('data-animations', 'off');
  await revealAnswer(page);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: 'Вспомнил' }).click();
  await page.waitForTimeout(500);
  expect(riveRequests).toEqual([]);
});

// v3/B: the vocabulary ("Слова") card gets a real flip — the whole card, including its own
// buttons/panel, rotates as one object, reusing the training card's original tested flip.

test('vocabulary reveal flips the card to face the answer; front stays out of the accessibility tree', async ({ page }) => {
  await page.goto('/');
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Карточка слова' });
  await expect(card.locator('.card-flip-inner')).toHaveClass(/flipped/);
  await expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-front')).toHaveAttribute('aria-hidden', 'true');
});

test('vocabulary flip swaps faces only past the 90° point in both directions (slowed animation, ~40%/~60% samples)', async ({ page }) => {
  await page.goto('/?flipDebugScale=20');
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Карточка слова' });
  await page.waitForTimeout(4_000); // 40% of the slowed 10s total, still first half
  await expect(card.locator('.card-back')).toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-back')).toHaveAttribute('inert', '');
  await page.waitForTimeout(2_000); // 60% of the total duration
  await expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-back')).not.toHaveAttribute('inert');
});

test('tapping the flipped vocabulary card flips it back to the question, purely visually, never a review', async ({ page }) => {
  await page.goto('/');
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Карточка слова' });
  const flip = card.locator('.card-flip');
  await flip.click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.card-front')).not.toHaveAttribute('aria-hidden', 'true');
  await expect(card.locator('.card-back')).toHaveAttribute('aria-hidden', 'true');
  const saved = await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}'), vocabularyKey);
  expect(Object.keys(saved.cards ?? {})).toHaveLength(0);
  await flip.click({ position: { x: 10, y: 10 } });
  await expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden', 'true');
});

test('a touch swipe on the revealed vocabulary card rates exactly once', async ({ page }) => {
  await page.addInitScript(() => {
    const nativeMatchMedia = window.matchMedia.bind(window);
    window.matchMedia = query => query === '(pointer: coarse)' ? ({ matches: true } as MediaQueryList) : nativeMatchMedia(query);
  });
  await page.goto('/');
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  // v4/UX4-08: the gesture listeners live on the stable `.card-flip` (never itself transformed —
  // see installSwipeCard's own doc comment for why), not on the narrower hint label; a real touch
  // event bubbles up to it from any descendant, which this synthetic dispatch (bubbles:false by
  // default) does not, so it targets `.card-flip` directly.
  const zone = page.getByRole('region', { name: 'Карточка слова' }).locator('.card-flip');
  await zone.dispatchEvent('pointerdown', { clientX: 20, clientY: 100, pointerId: 1, pointerType: 'touch', isPrimary: true });
  await zone.dispatchEvent('pointerup', { clientX: 220, clientY: 102, pointerId: 1, pointerType: 'touch', isPrimary: true });
  await expect.poll(async () => {
    const saved = await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}'), vocabularyKey);
    return Object.keys(saved.cards ?? {}).length;
  }).toBe(1);
});

// v4/UX4-05/06/07: the ring Rive accent (rings.riv) is removed at the user's explicit request —
// neither the vocabulary flip nor the training reveal plays it any more.
test('flip and reveal no longer request or render the removed ring Rive accent', async ({ page }) => {
  const riveRequests: string[] = [];
  page.on('request', request => { if (request.url().includes('/rive/')) riveRequests.push(request.url()); });
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.locator('#polski-rive-reveal')).toHaveCount(0);
  await expect(page.locator('.card-flip-rings')).toHaveCount(0);
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.getByRole('region', { name: 'Карточка слова' }).locator('.card-flip-rings')).toHaveCount(0);
  expect(riveRequests.some(u => u.endsWith('rings.riv'))).toBe(false);
});
