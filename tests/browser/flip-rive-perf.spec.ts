import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

/**
 * §5 measurement scaffold for the web host: variants A (no animation, real Motion.Reduced) / B
 * (flip only, Rive disabled via the debug `?riveDisabled=1` query) / C (flip + Rive) — see
 * FlipCardRivePlan.md §5. Numbers here are Chromium-headless-indicative, not device-grade (same
 * caveat the plan records for every host). This does not replace the emulator/simulator-native
 * profiling tools §5 lists for Android/iOS/macOS; it only covers what Playwright can drive.
 */

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

test('variant A (Motion.Reduced): rating never creates the Rive overlay and the flip has no transition', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', motion: 'Reduced' })));
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const transition = await page.locator('.card-flip-inner').evaluate(el => getComputedStyle(el).transitionDuration);
  expect(transition).toMatch(/^0s/);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.locator('#polski-rive-overlay')).toHaveCount(0);
});

test('variant B (?riveDisabled=1): flip still plays, Rive overlay never appears even with normal motion', async ({ page }) => {
  await page.goto('/?riveDisabled=1');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.locator('.card-flip-inner')).toHaveClass(/flipped/);
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.locator('#polski-rive-overlay')).toHaveCount(0);
});

test('variant C (default motion, Rive enabled): rating creates the overlay and fires a bounded number of long tasks', async ({ page }) => {
  await page.goto('/');
  await page.evaluate(() => {
    (window as any).__polskiLongTasks = [];
    new PerformanceObserver(list => {
      for (const entry of list.getEntries()) (window as any).__polskiLongTasks.push(entry.duration);
    }).observe({ type: 'longtask', buffered: true });
  });
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect(page.locator('#polski-rive-overlay')).toHaveAttribute('data-rive-effect', /^remembered:/);
  await page.waitForTimeout(1500); // let the Rive WASM module + confetti burst run
  const longTasks: number[] = await page.evaluate(() => (window as any).__polskiLongTasks ?? []);
  // Indicative-only budget (Chromium headless, not device-grade — see the plan's §5 caveat):
  // the confetti burst must not produce a long task over 200ms (a visible multi-frame stall).
  for (const duration of longTasks) expect(duration).toBeLessThan(200);
});

// FC2-14 (R4): v1's frame-time measurements came back flat (16.7ms/0% janky for every variant,
// see Plans/Kotlin/artifacts/flip-rive/v1-measurements.json) because unthrottled headless
// Chromium never gets close to a frame budget regardless of variant. CDP CPU throttling makes the
// three variants distinguishable by artificially slowing the main thread, the same technique
// Chrome DevTools' own performance panel uses.
for (const rate of [1, 4, 6]) {
  test(`CPU throttle ${rate}x: flip + rating stays responsive and long tasks are bounded (variant C)`, async ({ page }) => {
    const cdp = await page.context().newCDPSession(page);
    await page.goto('/');
    await page.evaluate(() => {
      (window as any).__polskiLongTasks = [];
      new PerformanceObserver(list => {
        for (const entry of list.getEntries()) (window as any).__polskiLongTasks.push(entry.duration);
      }).observe({ type: 'longtask', buffered: true });
    });
    await continueIntroductionIfPresent(page);
    await cdp.send('Emulation.setCPUThrottlingRate', { rate });
    const start = Date.now();
    await page.getByRole('button', { name: 'Показать ответ' }).click();
    await expect(page.locator('.card-flip-inner')).toHaveClass(/flipped/);
    await page.getByRole('button', { name: /2 Вспомнил/ }).click();
    await expect(page.locator('#polski-rive-overlay')).toHaveAttribute('data-rive-effect', /^remembered:/);
    const flipAndRateMs = Date.now() - start;
    await page.waitForTimeout(1500);
    await cdp.send('Emulation.setCPUThrottlingRate', { rate: 1 }); // restore before the next test/teardown
    const longTasks: number[] = await page.evaluate(() => (window as any).__polskiLongTasks ?? []);
    // Indicative-only (Chromium headless + CDP software throttling, not device-grade — §5's
    // caveat applies here too): the flip+rate interaction itself must still complete in a
    // reasonable wall-clock window even at 6x throttle, and no single long task should be so long
    // it would read as a multi-second freeze.
    expect(flipAndRateMs, `flip+rate wall time at ${rate}x throttle`).toBeLessThan(20_000);
    for (const duration of longTasks) expect(duration, `long task at ${rate}x throttle`).toBeLessThan(2_000);
  });
}

test('bundle keeps the Rive assets lazy: index does not eagerly fetch rive.js/.wasm before first reveal', async ({ page }) => {
  const riveRequests: string[] = [];
  page.on('request', request => { if (request.url().includes('/rive/')) riveRequests.push(request.url()); });
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  expect(riveRequests, 'no Rive asset should load before the answer is even revealed').toEqual([]);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => riveRequests.some(u => u.endsWith('rive-bridge.js'))).toBe(true);
});
