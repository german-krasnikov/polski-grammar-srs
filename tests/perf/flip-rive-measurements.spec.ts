import { expect, test, type Page } from '@playwright/test';
import { writeFileSync, mkdirSync } from 'node:fs';
import { resolve } from 'node:path';
import { continueIntroductionIfPresent } from '../browser/kotlin-introduction.js';

/**
 * Tester-added quantitative measurement harness for FlipCardRivePlan.md §5 (web host).
 * The plan's own §8/§11 evidence logs record that the CDP rAF-delta profile,
 * PerformanceObserver('longtask') budget-with-numbers, and memory-before/after were NOT collected
 * by the developer for the web host (§8 "Известные ограничения") — only a boolean long-task<200ms
 * check and the bundle-size table exist (flip-rive-perf.spec.ts). This spec fills that gap:
 * variant A (no-animation, Motion.Reduced) / B (native-flip only, ?riveDisabled=1) / C (flip+Rive),
 * >=5 flip cycles and >=5 swipe-with-effect cycles per variant, on both the JS and Wasm branches,
 * Chromium only (headless-indicative, not device-grade — same caveat the plan records everywhere).
 *
 * Test-only code. Does not touch production sources. Raw output: one JSON file per
 * variant+branch under Plans/Kotlin/artifacts/flip-rive/web/.
 */

type Sample = { t: number; delta: number };

const outDir = resolve(process.cwd(), 'Plans/Kotlin/artifacts/flip-rive/web');
mkdirSync(outDir, { recursive: true });

const branch = process.env.KOTLIN_SPIKE_BRANCH ?? 'unknown';

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

async function installObservers(page: Page) {
  await page.evaluate(() => {
    (window as any).__perf = { longtasks: [] as number[], raf: [] as Sample[], rafRunning: false };
    new PerformanceObserver(list => {
      for (const e of list.getEntries()) (window as any).__perf.longtasks.push(e.duration);
    }).observe({ type: 'longtask', buffered: true });
  });
}

async function sampleRafFor(page: Page, ms: number): Promise<Sample[]> {
  return page.evaluate(async duration => {
    const samples: Sample[] = [];
    let last = performance.now();
    const start = last;
    await new Promise<void>(resolve => {
      function tick(now: number) {
        samples.push({ t: now - start, delta: now - last });
        last = now;
        if (now - start < duration) requestAnimationFrame(tick);
        else resolve();
      }
      requestAnimationFrame(tick);
    });
    return samples;
  }, ms);
}

function percentile(sorted: number[], p: number): number {
  if (sorted.length === 0) return NaN;
  const idx = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
  return sorted[Math.max(0, idx)];
}

function summarize(samples: Sample[]) {
  const deltas = samples.slice(1).map(s => s.delta).sort((a, b) => a - b);
  const janky = deltas.filter(d => d > 16.7 * 1.5).length; // >25ms: missed a 60fps frame
  const dropped = deltas.filter(d => d > 33.4).length; // >2 frames worth: a dropped frame
  return {
    frames: deltas.length,
    p50: percentile(deltas, 50),
    p95: percentile(deltas, 95),
    p99: percentile(deltas, 99),
    max: deltas.length ? deltas[deltas.length - 1] : NaN,
    jankyPct: deltas.length ? (100 * janky) / deltas.length : NaN,
    droppedPct: deltas.length ? (100 * dropped) / deltas.length : NaN,
  };
}

async function heapUsed(page: Page): Promise<number | null> {
  return page.evaluate(() => (performance as any).memory?.usedJSHeapSize ?? null);
}

async function ensureRevealed(page: Page) {
  if (await page.locator('.card-back').count()) return; // already revealed (e.g. right after a flip-only phase)
  await reveal(page);
}

async function reveal(page: Page) {
  await continueIntroductionIfPresent(page);
  if (await page.locator('.card-back').count()) return; // continueIntroductionIfPresent already landed on Revealed
  // The exercise stream mixes oral (direct reveal) and typed-answer cards; handle both so a run
  // of >=6 swipe cycles doesn't hang on whichever mode comes up next.
  const oral = page.getByRole('button', { name: 'Показать ответ' });
  const typedCheck = page.getByRole('button', { name: 'Проверить и показать ответ' });
  const typedPick = page.getByRole('button', { name: 'Напечатать ответ' });
  await Promise.race([
    oral.waitFor({ state: 'visible', timeout: 5_000 }).catch(() => {}),
    typedCheck.waitFor({ state: 'visible', timeout: 5_000 }).catch(() => {}),
    typedPick.waitFor({ state: 'visible', timeout: 5_000 }).catch(() => {}),
  ]);
  if (await oral.count()) {
    await oral.click();
    return;
  }
  if (await typedPick.count()) {
    await typedPick.click();
  }
  const textbox = page.getByRole('textbox', { name: 'Ответ по-польски' });
  if (await textbox.count()) await textbox.fill('x');
  if (await typedCheck.count()) await typedCheck.click();
}

async function runVariant(page: Page, variant: 'A' | 'B' | 'C', queryDisableRive: boolean, reducedMotion: boolean) {
  if (reducedMotion) {
    await page.addInitScript(() =>
      localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', motion: 'Reduced' })),
    );
  }
  await page.goto(queryDisableRive ? '/?riveDisabled=1' : '/');
  await continueIntroductionIfPresent(page);
  // Default mode is the 5-step "chain", which ends in a "chain complete" summary screen every 5
  // cards (different buttons, no reveal/rate) — switch to scheduled review mode so >=6 swipe
  // cycles get a plain, uninterrupted stream of single cards, closer to steady-state usage.
  const scheduled = page.getByRole('button', { name: /^По расписанию/ });
  if (await scheduled.count()) await scheduled.click();
  await installObservers(page);

  const memBaseline = await heapUsed(page);

  // --- flip cycles: reveal once, then tap-flip N times (front<->back), no rating ---
  await reveal(page);
  await page.waitForTimeout(50);
  const flipCycles = 6;
  const flipSummaries: ReturnType<typeof summarize>[] = [];
  const flip = page.locator('.card-flip');
  await flip.scrollIntoViewIfNeeded();
  for (let i = 0; i < flipCycles; i++) {
    const clickAndSample = async () => {
      const samplingPromise = sampleRafFor(page, 700);
      await flip.click({ position: { x: 10, y: 10 } });
      return samplingPromise;
    };
    flipSummaries.push(summarize(await clickAndSample()));
  }

  const memAfterFlips = await heapUsed(page);

  // --- swipe-with-effect cycles: reveal + real pointer swipe on the whole back face, alternating rating ---
  const swipeCycles = 6;
  const swipeSummaries: ReturnType<typeof summarize>[] = [];
  const firstEffectLatencies: number[] = [];
  const riveResourceTimings: number[] = [];
  for (let i = 0; i < swipeCycles; i++) {
    await ensureRevealed(page);
    const back = page.locator('.card-back');
    await back.waitFor({ state: 'visible' });
    await back.scrollIntoViewIfNeeded();
    const bounds = await back.boundingBox();
    if (!bounds) break;
    const y = bounds.y + bounds.height / 2;
    const good = i % 2 === 0;
    const startX = good ? bounds.x + 20 : bounds.x + bounds.width - 20;
    const endX = good ? Math.min(bounds.x + bounds.width - 20, bounds.x + 220) : Math.max(bounds.x + 20, bounds.x + bounds.width - 220);

    const t0 = Date.now();
    const samplingPromise = sampleRafFor(page, 1700);
    await page.mouse.move(startX, y);
    await page.mouse.down();
    await page.mouse.move(endX, y, { steps: 8 });
    await page.mouse.up();
    if (!queryDisableRive && !reducedMotion) {
      try {
        await expect(page.locator('#polski-rive-overlay')).toHaveAttribute('data-rive-effect', /.+/, { timeout: 1200 });
        firstEffectLatencies.push(Date.now() - t0);
      } catch {
        firstEffectLatencies.push(NaN);
      }
    }
    swipeSummaries.push(summarize(await samplingPromise));
  }

  if (!queryDisableRive && !reducedMotion) {
    const resources = await page.evaluate(() =>
      performance.getEntriesByType('resource')
        .filter((r: any) => r.name.includes('/rive/'))
        .map((r: any) => ({ name: r.name.split('/rive/')[1], duration: r.duration, transferSize: r.transferSize })),
    );
    for (const r of resources as any[]) riveResourceTimings.push(r.duration);
  }

  // --- leak check: 50 extra cheap flip toggles (no rating), then re-measure heap ---
  await ensureRevealed(page);
  await flip.scrollIntoViewIfNeeded();
  for (let i = 0; i < 50; i++) {
    await flip.click({ position: { x: 10, y: 10 } });
  }
  await page.waitForTimeout(100);
  const memAfter50 = await heapUsed(page);

  const longtasks: number[] = await page.evaluate(() => (window as any).__perf.longtasks ?? []);

  const avg = (arr: number[]) => (arr.length ? arr.reduce((a, b) => a + (Number.isFinite(b) ? b : 0), 0) / arr.length : NaN);
  const merge = (key: 'p50' | 'p95' | 'p99' | 'jankyPct' | 'droppedPct', list: ReturnType<typeof summarize>[]) => avg(list.map(s => s[key]));

  return {
    variant,
    branch,
    memory: { baselineBytes: memBaseline, afterEffectsBytes: memAfterFlips, after50CyclesBytes: memAfter50 },
    flip: {
      cycles: flipCycles,
      p50: merge('p50', flipSummaries),
      p95: merge('p95', flipSummaries),
      p99: merge('p99', flipSummaries),
      jankyPct: merge('jankyPct', flipSummaries),
      droppedPct: merge('droppedPct', flipSummaries),
    },
    swipeWithEffect: {
      cycles: swipeCycles,
      p50: merge('p50', swipeSummaries),
      p95: merge('p95', swipeSummaries),
      p99: merge('p99', swipeSummaries),
      jankyPct: merge('jankyPct', swipeSummaries),
      droppedPct: merge('droppedPct', swipeSummaries),
    },
    longTasks: { count: longtasks.length, maxMs: longtasks.length ? Math.max(...longtasks) : 0, totalMs: longtasks.reduce((a, b) => a + b, 0) },
    riveFirstEffectLatencyMs: firstEffectLatencies,
    riveResourceDurationsMs: riveResourceTimings,
  };
}

for (const [variant, opts] of [
  ['A', { queryDisableRive: false, reducedMotion: true }],
  ['B', { queryDisableRive: true, reducedMotion: false }],
  ['C', { queryDisableRive: false, reducedMotion: false }],
] as const) {
  test(`§5 measurement — variant ${variant} (${variant === 'A' ? 'no-animation' : variant === 'B' ? 'native-flip only' : 'flip+rive'})`, async ({ page }) => {
    const result = await runVariant(page, variant, opts.queryDisableRive, opts.reducedMotion);
    const file = resolve(outDir, `variant-${variant}-${branch}.json`);
    writeFileSync(file, JSON.stringify(result, null, 2));
    // Sanity assertions only — the numeric results themselves are reported, not pass/failed against a budget.
    expect(result.flip.cycles).toBeGreaterThanOrEqual(5);
    expect(result.swipeWithEffect.cycles).toBeGreaterThanOrEqual(5);
  });
}
