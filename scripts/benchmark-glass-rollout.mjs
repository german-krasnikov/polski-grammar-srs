import { chromium, firefox, webkit } from '@playwright/test';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const urls = {
  baseline: process.env.GLASS_ROLLOUT_BASELINE ?? 'http://127.0.0.1:4181/',
  candidate: process.env.GLASS_ROLLOUT_CANDIDATE ?? 'http://127.0.0.1:4182/',
};
const output = process.env.GLASS_ROLLOUT_OUT ?? 'Plans/Kotlin/artifacts/glass-rollout';
const rounds = Number(process.env.GLASS_ROLLOUT_ROUNDS ?? 5);
const durationMs = Number(process.env.GLASS_ROLLOUT_DURATION_MS ?? 2500);
const available = { chromium, firefox, webkit };
const selected = (process.env.GLASS_ROLLOUT_ENGINES ?? 'chromium,firefox,webkit').split(',');
if (!Number.isInteger(rounds) || rounds < 1 || !Number.isFinite(durationMs) || durationMs < 500 ||
    selected.some(name => !(name in available))) throw new Error('Invalid benchmark settings');
mkdirSync(output, { recursive: true });

const percentile = (samples, fraction) => {
  const sorted = [...samples].sort((a, b) => a - b);
  return sorted[Math.min(sorted.length - 1, Math.ceil(sorted.length * fraction) - 1)];
};
const result = {
  date: new Date().toISOString(), urls, rounds, durationMs,
  profile: { width: 390, height: 844, dpr: 3, colorScheme: 'dark' },
  engines: [],
};

for (const name of selected) {
  const browser = await available[name].launch({ headless: true });
  try {
    const context = await browser.newContext({
      viewport: { width: 390, height: 844 }, deviceScaleFactor: 3,
      colorScheme: 'dark', reducedMotion: 'reduce',
    });
    try {
      const pages = {};
      for (const [variant, url] of Object.entries(urls)) {
        const page = await context.newPage();
        await page.goto(url);
        await page.getByRole('button', { name: 'Таблицы и схема' }).click();
        await page.locator('.matrix-page').waitFor();
        await page.evaluate(() => document.fonts.ready);
        pages[variant] = page;
      }
      const engine = { name, browserVersion: browser.version(), runs: [] };
      for (let round = 0; round < rounds; round += 1) {
        const order = round % 2 === 0 ? ['baseline', 'candidate'] : ['candidate', 'baseline'];
        for (const variant of order) {
          const sample = await pages[variant].evaluate(async duration => {
            const host = document.querySelector('.training-web-host');
            if (!host || host.scrollHeight - host.clientHeight < 200) throw new Error('Matrix has insufficient scroll');
            const maxScroll = host.scrollHeight - host.clientHeight;
            const move = elapsed => { host.scrollTop = (.5 - .5 * Math.cos(elapsed * .0048)) * maxScroll; };
            host.scrollTop = 0;
            await new Promise(resolve => {
              let count = 0;
              function warm(timestamp) {
                move(count * 16.7);
                if (++count < 35) requestAnimationFrame(warm);
                else resolve(timestamp);
              }
              requestAnimationFrame(warm);
            });
            const intervals = [];
            const start = performance.now();
            let previous;
            await new Promise(resolve => {
              function frame(timestamp) {
                if (previous !== undefined) intervals.push(timestamp - previous);
                previous = timestamp;
                move(timestamp - start);
                if (performance.now() - start < duration) requestAnimationFrame(frame);
                else resolve();
              }
              requestAnimationFrame(frame);
            });
            const nav = document.querySelector('nav[aria-label="Основные разделы"]');
            const card = document.querySelector('.card');
            return {
              intervals: intervals.filter(value => value > 0),
              navFilter: nav ? getComputedStyle(nav).backdropFilter : null,
              cardFilter: card ? getComputedStyle(card).backdropFilter : null,
            };
          }, durationMs);
          engine.runs.push({
            round: round + 1, variant,
            frames: sample.intervals.length,
            p50Ms: percentile(sample.intervals, .5),
            p95Ms: percentile(sample.intervals, .95),
            over25Ms: sample.intervals.filter(value => value > 25).length,
            navFilter: sample.navFilter, cardFilter: sample.cardFilter,
          });
          const run = engine.runs.at(-1);
          process.stdout.write(`${name} ${variant} ${round + 1}/${rounds}: p95 ${run.p95Ms.toFixed(2)}ms, >25ms ${run.over25Ms}\n`);
        }
      }
      const paired = [...Array(rounds).keys()].map(index => {
        const pair = engine.runs.filter(run => run.round === index + 1);
        return pair.find(run => run.variant === 'candidate').p95Ms - pair.find(run => run.variant === 'baseline').p95Ms;
      });
      engine.summary = {
        baselineMedianP95Ms: percentile(engine.runs.filter(run => run.variant === 'baseline').map(run => run.p95Ms), .5),
        candidateMedianP95Ms: percentile(engine.runs.filter(run => run.variant === 'candidate').map(run => run.p95Ms), .5),
        pairedMedianDeltaP95Ms: percentile(paired, .5),
        pairedRangeDeltaP95Ms: [Math.min(...paired), Math.max(...paired)],
      };
      result.engines.push(engine);
      writeFileSync(join(output, 'web-matrix-scroll.json'), JSON.stringify(result, null, 2));
    } finally {
      await context.close();
    }
  } finally {
    await browser.close();
  }
}
