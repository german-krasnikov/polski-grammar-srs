import { devices, expect, test, type Page } from '@playwright/test';
import { mkdirSync } from 'node:fs';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// FlipCardRivePlan.md §17 (v4): whole-panel vocabulary flip, swipe-first rating on both cards,
// animated tab switching and collapsible panels, general UI/UX polish.

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

const openVocabularyCard = async (page: Page) => {
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('checkbox').first().check();
};

test('UX4-01/04: the whole vocabulary panel (background/border/shadow) lives on the flipping face, not the static wrapper', async ({ page }) => {
  await page.goto('/');
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  // `region "Карточка слова"` IS the `.vocabulary-card` element itself (its own aria-label), not
  // an ancestor of it — the static wrapper this test asserts paints nothing of its own.
  const card = page.getByRole('region', { name: 'Карточка слова' });
  const wrapper = await card.evaluate(element => {
    const style = getComputedStyle(element as HTMLElement);
    return { background: style.backgroundColor, border: style.borderStyle, shadow: style.boxShadow };
  });
  expect(wrapper.border).toBe('none');
  expect(wrapper.shadow).toBe('none');
  const activeFace = await card.locator('.card-face:not([aria-hidden="true"])').evaluate(element => {
    const style = getComputedStyle(element as HTMLElement);
    return { background: style.backgroundImage || style.backgroundColor, radius: style.borderRadius, shadow: style.boxShadow };
  });
  expect(activeFace.shadow).not.toBe('none');
  expect(activeFace.radius).not.toBe('0px');
});

// P0-2: before this fix, `renderCard` only ever wrapped the front in `.card-flip`/`.card-face`
// once the card was revealed — the unrevealed prompt/mode-buttons/reveal-button sat loose on the
// page background, and the panel visibly sprang into existence exactly on reveal (the same
// rounded panel the flip then immediately rotates). It must be the same panel object throughout.
test('P0-2: the vocabulary panel already exists (same .card-flip/.card-face object) before reveal, not just after', async ({ page }) => {
  await page.goto('/');
  await openVocabularyCard(page);
  const card = page.getByRole('region', { name: 'Карточка слова' });
  const beforeReveal = await card.locator('.card-face').evaluate(element => {
    const style = getComputedStyle(element as HTMLElement);
    return { shadow: style.boxShadow, radius: style.borderRadius };
  });
  expect(beforeReveal.shadow).not.toBe('none');
  expect(beforeReveal.radius).not.toBe('0px');
  await expect(card.locator('.card-flip')).toHaveCount(1);
  await expect(card.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
});

// P0-1: on a real touch/mobile context, `.card-answer-wrap`'s previous default `touch-action`
// (`auto`) let the browser's own gesture recognizer intercept a horizontal drag as a scroll
// attempt and cancel the pointer sequence (pointerdown → pointermove → pointercancel, never
// pointerup) — nothing rated, and the rating buttons are already visually collapsed on this same
// device (see UX4-11 above), so there was no way at all to rate a card by touch. A synthetic
// `pointerdown`/`pointerup` dispatch (used elsewhere in this file) never exercises the native
// gesture recognizer that `touch-action` governs — only a real device context plus CDP's own
// `Input.dispatchTouchEvent` does.
test('P0-1: a real touch swipe rates the training card on a touch device (not just a synthetic pointer dispatch)', async ({ browser, browserName }) => {
  test.skip(browserName !== 'chromium', 'CDP touch injection is available only in Chromium');
  const context = await browser.newContext({ ...devices['Pixel 7'], viewport: { width: 390, height: 844 } });
  const page = await context.newPage();
  try {
    await page.goto('/#/training');
    await continueIntroductionIfPresent(page);
    await page.getByRole('button', { name: 'Показать ответ' }).click();
    const wrap = page.getByRole('region', { name: 'Учебная карточка' }).locator('.card-answer-wrap');
    await page.waitForTimeout(700); // let the expand-reveal transition settle before measuring
    const bounds = await wrap.boundingBox();
    expect(bounds).not.toBeNull();
    const x1 = bounds!.x + bounds!.width - 30;
    const x2 = x1 - 150;
    const y = bounds!.y + Math.min(60, bounds!.height / 2);
    const session = await context.newCDPSession(page);
    await session.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [{ x: x1, y, id: 1 }] });
    await session.send('Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [{ x: x2, y, id: 1 }] });
    await session.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });
    await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, 'polski-grammar-srs-kmp-preview-v1')).toBe(1);
  } finally {
    await context.close();
  }
});

// P1-8: the drag transform used to land on `.card-back` alone, a child clipped by `.flashcard`'s
// own `overflow:hidden` — it visibly left the question behind and got cut off at the card's own
// edge instead of "the whole card" moving, as requested. It must now land on the `.flashcard`
// element itself (which the question, `.card-meta` and the answer are all siblings/descendants
// inside), so the drag transform is never subject to that element's own overflow clipping.
test('P1-8: the drag transform moves the whole training card (.flashcard), not just the clipped answer face', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  // `region "Учебная карточка"` IS the `.flashcard` element itself (its own aria-label).
  const flashcard = page.getByRole('region', { name: 'Учебная карточка' });
  await page.waitForTimeout(700); // let the expand-reveal transition settle before measuring
  const hint = flashcard.locator('.vocabulary-swipe-zone');
  await hint.scrollIntoViewIfNeeded();
  const hintBounds = await hint.boundingBox();
  expect(hintBounds).not.toBeNull();
  const y = hintBounds!.y + hintBounds!.height / 2;
  await page.mouse.move(hintBounds!.x + 10, y);
  await page.mouse.down();
  await page.mouse.move(hintBounds!.x + 110, y, { steps: 6 });
  const transform = await flashcard.evaluate(el => (el as HTMLElement).style.transform);
  expect(transform).not.toBe('');
  await page.mouse.up();
});

test('UX4-05/07: no ring Rive request or element on vocabulary flip or training reveal', async ({ page }) => {
  const riveRequests: string[] = [];
  page.on('request', request => { if (request.url().includes('/rive/')) riveRequests.push(request.url()); });
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await openVocabularyCard(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.waitForTimeout(300);
  expect(riveRequests.some(u => u.endsWith('rings.riv'))).toBe(false);
  await expect(page.locator('.card-flip-rings')).toHaveCount(0);
});

test('UX4-14: ArrowLeft/ArrowRight rate the revealed vocabulary card, alongside 1/2; nothing before reveal or inside the typed field', async ({ page }) => {
  await page.goto('/');
  await openVocabularyCard(page);
  await page.keyboard.press('ArrowRight'); // before reveal: no-op
  let saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(Object.keys(saved.cards)).toHaveLength(0);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ на карточку слова' }).fill('próba');
  await page.keyboard.press('ArrowRight'); // caret move inside the textarea, never a rating
  saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(Object.keys(saved.cards)).toHaveLength(0);
  await expect(page.getByRole('textbox', { name: 'Ответ на карточку слова' })).toHaveValue('próba');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.locator('body').click({ position: { x: 5, y: 5 } }); // move focus off the (now removed) textarea
  await page.keyboard.press('ArrowRight');
  await expect.poll(async () => {
    saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
    return Object.keys(saved.cards).length;
  }).toBe(1);
});

test('UX4-14: ArrowLeft rates "Again" on the revealed training card, alongside the existing 1/2', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.keyboard.press('ArrowLeft');
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, 'polski-grammar-srs-kmp-preview-v1')).toBe(1);
});

// v4/UX4-11: gated on `(pointer: coarse)` alone (a real touch/stylus device), not also on a
// narrow viewport width — a resizable *desktop* browser window narrower than 480px reports
// `pointer: fine` and must keep its normal, clickable rating buttons (see training.css's own
// deviation note: the plan's literal `,(max-width:480px)` OR-clause broke exactly that case).
// §17.8.4 flagged the exact recipe for a real `pointer: coarse` CSS match in headless Chromium as
// open; a full mobile device descriptor (isMobile + hasTouch), Playwright's own documented way to
// emulate one, is what actually flips the *style engine's* media evaluation — plain `hasTouch`
// alone flips `matchMedia()` for script reads but left the stylesheet's own `@media` block
// unmatched in this same run, which is the resolution recorded here.
test('UX4-11/12: rating buttons stay reachable by keyboard even when visually collapsed on a real coarse-pointer device', async ({ browser }) => {
  const context = await browser.newContext({ ...devices['Pixel 7'], viewport: { width: 390, height: 844 } });
  const page = await context.newPage();
  await page.goto('/');
  expect(await page.evaluate(() => matchMedia('(pointer: coarse)').matches)).toBe(true);
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  const again = card.getByRole('button', { name: /Повторить/ });
  await expect(again).toHaveCount(1);
  // The ancestor `.ratings` container is what's actually clipped to 1x1px+overflow:hidden (the
  // `sr-only` pattern); the button's OWN layout box is unaffected by an ancestor's clipping (per
  // getBoundingClientRect semantics), so the container, not the button, is what to assert on.
  const container = await again.evaluate(el => {
    const style = getComputedStyle(el.closest('.ratings') as Element);
    return { width: style.width, position: style.position, overflow: style.overflow };
  });
  expect(container.width).toBe('1px');
  expect(container.position).toBe('absolute');
  expect(container.overflow).toBe('hidden');
  await again.focus();
  await expect(again).toBeFocused(); // still reachable by Tab/screen reader
  await context.close();
});

test('UX4-11: a narrow but fine-pointer desktop window keeps normal, mouse-clickable rating buttons', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const again = page.getByRole('button', { name: /Повторить/ });
  const box = await again.boundingBox();
  expect(box!.width).toBeGreaterThan(20); // NOT clipped — this is a mouse-driven desktop window
  await again.click();
  await expect.poll(() => page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').totalReviews, 'polski-grammar-srs-kmp-preview-v1')).toBe(1);
});

test('UX4-20/21/22: "Скрыть каталог" collapses with aria-expanded/inert and returns focus to the toggle', async ({ page }) => {
  await page.goto('/#/vocabulary');
  const toggle = page.locator('#vocabulary-catalog-toggle');
  await expect(toggle).toHaveAttribute('aria-expanded', 'true');
  const collapsible = page.locator('#vocabulary-catalog-collapsible');
  await expect(collapsible).not.toHaveAttribute('inert', '');
  const filterSelect = page.getByRole('combobox', { name: 'Подборка слов' });
  await filterSelect.focus();
  await toggle.click();
  await expect(toggle).toHaveAttribute('aria-expanded', 'false');
  await expect(collapsible).toHaveAttribute('inert', '');
  await expect(toggle).toBeFocused(); // moved out before the content became inert, not after
  await toggle.click();
  await expect(toggle).toHaveAttribute('aria-expanded', 'true');
  await expect(collapsible).not.toHaveAttribute('inert', '');
});

test('UX4-16: the nav indicator slides to the active destination', async ({ page }) => {
  await page.goto('/#/training');
  const indicator = page.locator('.nav-indicator');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  // The 250ms CSS transition is still running right after the click; poll until it settles on
  // the new destination's exact position/width, rather than sampling a mid-transition frame.
  await expect.poll(async () => {
    const vocabularyBox = await page.locator('#nav-vocabulary').boundingBox();
    const indicatorBox = await indicator.boundingBox();
    return Math.abs(indicatorBox!.x - vocabularyBox!.x) < 2 && Math.abs(indicatorBox!.width - vocabularyBox!.width) < 2;
  }).toBe(true);
});

test('UX4-17/19: tab navigation still works with View Transitions wired in, on every engine, and is not triggered by the 30s timer', async ({ page }) => {
  await page.goto('/#/training');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click(); // reveal, so state persists visibly across the round trip
  for (const [label, route] of [['Слова', 'vocabulary'], ['Прогресс', 'progress'], ['Карточки', 'training']] as const) {
    await page.getByRole('button', { name: label, exact: true }).click();
    await expect(page).toHaveURL(new RegExp(`#/${route}$`));
  }
  // The introduction was already dismissed above and the card stays revealed — the same exercise
  // is still showing its answer, not a fresh "Перейти к заданию" intro screen.
  await expect(page.locator('.answer-sentence')).toBeVisible();
});

test('capture front/back, mid-swipe and tab-switch screenshots for the v4 evidence log', async ({ page }, testInfo) => {
  const branch = `${process.env.KOTLIN_SPIKE_BRANCH ?? 'unknown'}-${testInfo.project.name}`;
  const directory = 'Plans/Kotlin/artifacts/ux4/web';
  mkdirSync(directory, { recursive: true });
  const shots: string[] = [];
  const shoot = async (viewport: { width: number; height: number }, name: string) => {
    await page.setViewportSize(viewport);
    const path = `${directory}/${branch}-${name}.png`;
    await page.screenshot({ path, fullPage: true });
    shots.push(path);
  };
  for (const [theme, viewport] of [['dark', { width: 390, height: 844 }], ['light', { width: 1280, height: 900 }]] as const) {
    await page.goto('/#/training');
    await page.evaluate(t => localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 2, appearance: t === 'dark' ? 'Dark' : 'Light' })), theme);
    await page.reload();
    await continueIntroductionIfPresent(page);
    await shoot(viewport, `training-front-${theme}`);
    await page.getByRole('button', { name: 'Показать ответ' }).click();
    await shoot(viewport, `training-back-${theme}`);
    await openVocabularyCard(page);
    await shoot(viewport, `vocabulary-front-${theme}`);
    await page.getByRole('button', { name: 'Показать ответ' }).click();
    await shoot(viewport, `vocabulary-back-${theme}`);
    const back = page.getByRole('region', { name: 'Карточка слова' }).locator('.card-face:not([aria-hidden="true"])');
    const bounds = await back.boundingBox();
    if (bounds) {
      const y = bounds.y + bounds.height / 2;
      await page.mouse.move(bounds.x + bounds.width / 2, y);
      await page.mouse.down();
      await page.mouse.move(bounds.x + bounds.width / 2 + 60, y, { steps: 6 });
      await shoot(viewport, `vocabulary-mid-swipe-${theme}`);
      await page.mouse.up();
    }
    await page.getByRole('button', { name: 'Прогресс', exact: true }).click();
    await shoot(viewport, `tabs-progress-${theme}`);
  }
  console.log('UX4 evidence screenshots:', shots.join(', '));
});
