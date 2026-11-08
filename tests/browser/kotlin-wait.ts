import type { Page } from '@playwright/test';

/**
 * Waits for every CSS transition/animation running on the page to reach `finished`, instead of a
 * fixed `waitForTimeout` guess. Two-phase: first confirms at least one animation has actually
 * started — otherwise `document.getAnimations()` can still be empty right after the triggering
 * click (the style recalc that starts a CSS transition lands a frame later), and `.every()` on an
 * empty array would trivially pass before anything moved. The first phase gets a short timeout and
 * is allowed to fail silently: if the animation already finished (and was removed from
 * `getAnimations()`) before this function ran, the second phase's `.every()` on an empty array is
 * exactly the right "already settled" answer.
 */
export async function waitForAnimationsSettled(page: Page): Promise<void> {
  await page.waitForFunction(() => document.getAnimations().length > 0, undefined, { timeout: 2_000 }).catch(() => {});
  await page.waitForFunction(() => document.getAnimations().every(a => a.playState === 'finished'));
}
