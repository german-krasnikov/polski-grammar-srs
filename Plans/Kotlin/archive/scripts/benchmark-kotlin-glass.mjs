import { chromium, firefox, webkit } from '@playwright/test';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const url = process.env.GLASS_BENCH_URL ?? 'http://127.0.0.1:4178/';
const outputDirectory = process.env.GLASS_BENCH_OUT ?? 'Plans/Kotlin/artifacts/glass-web';
const rounds = Number(process.env.GLASS_BENCH_ROUNDS ?? 5);
const durationMs = Number(process.env.GLASS_BENCH_DURATION_MS ?? 2500);
if (!Number.isInteger(rounds) || rounds < 1 || !Number.isFinite(durationMs) || durationMs < 500) {
  throw new Error('Invalid benchmark rounds or duration');
}
mkdirSync(outputDirectory, { recursive: true });

const variants = {
  opaque: 'background:rgb(24 36 51)!important;backdrop-filter:none!important;-webkit-backdrop-filter:none!important;',
  tint: 'background:rgb(24 36 51 / 72%)!important;backdrop-filter:none!important;-webkit-backdrop-filter:none!important;',
  blur: 'background:rgb(24 36 51 / 72%)!important;backdrop-filter:blur(12px) saturate(1.2)!important;-webkit-backdrop-filter:blur(12px) saturate(1.2)!important;',
};
const sharedStyle = 'nav[aria-label="Основные разделы"]{position:sticky!important;top:0!important;z-index:20!important;padding:8px!important;border:1px solid rgb(120 145 170 / 36%)!important;border-radius:14px!important;box-shadow:0 6px 20px rgb(0 0 0 / 12%)!important;';
const profiles = [
  { name: 'mobile-390-dpr3', viewport: { width: 390, height: 844 }, deviceScaleFactor: 3 },
  { name: 'desktop-1440-dpr1', viewport: { width: 1440, height: 900 }, deviceScaleFactor: 1 },
];
const engines = { chromium, firefox, webkit };
const selectedEngines = (process.env.GLASS_BENCH_ENGINES ?? 'chromium,firefox,webkit').split(',');
const selectedProfiles = (process.env.GLASS_BENCH_PROFILES ?? 'mobile-390-dpr3,desktop-1440-dpr1').split(',');
const percentile = (values, fraction) => {
  const sorted = [...values].sort((a, b) => a - b);
  return sorted[Math.min(sorted.length - 1, Math.ceil(sorted.length * fraction) - 1)];
};
const summary = values => ({ median: percentile(values, 0.5), p95: percentile(values, 0.95), min: Math.min(...values), max: Math.max(...values) });
const results = { date: new Date().toISOString(), url, rounds, durationMs, profiles: [] };

for (const [engineName, engine] of Object.entries(engines).filter(([name]) => selectedEngines.includes(name))) {
  const browser = await engine.launch({ headless: true });
  try {
    for (const profile of profiles.filter(item => selectedProfiles.includes(item.name))) {
      const context = await browser.newContext({
        viewport: profile.viewport,
        deviceScaleFactor: profile.deviceScaleFactor,
        colorScheme: 'dark',
        reducedMotion: 'reduce',
        locale: 'pl-PL',
      });
      const page = await context.newPage();
      try {
        await page.goto(url, { waitUntil: 'load' });
        await page.getByRole('button', { name: 'Таблицы и схема' }).click();
        await page.locator('.matrix-page').waitFor();
        await page.evaluate(() => document.fonts.ready);
        const setup = await page.evaluate(() => {
          const host = document.querySelector('.training-web-host');
          const nav = document.querySelector('nav[aria-label="Основные разделы"]');
          if (!host || !nav) throw new Error('Expected Kotlin DOM host and navigation');
          const style = document.createElement('style');
          style.id = 'glass-benchmark-variant';
          document.head.append(style);
          return {
            maxScroll: host.scrollHeight - host.clientHeight,
            resources: performance.getEntriesByType('resource').map(entry => entry.name.split('/').pop()).filter(name => name?.endsWith('.wasm') || name?.endsWith('.js')),
          };
        });
        if (setup.maxScroll < 200) throw new Error(`Not enough vertical content for benchmark: ${setup.maxScroll}px`);
        const profileResult = { engine: engineName, browserVersion: browser.version(), ...profile, setup, runs: [] };
        for (let round = 0; round < rounds; round += 1) {
          const order = Object.keys(variants);
          order.push(...order.splice(0, round % order.length));
          for (const name of order) {
            const run = await page.evaluate(async ({ css, shared, duration }) => {
              const host = document.querySelector('.training-web-host');
              const nav = document.querySelector('nav[aria-label="Основные разделы"]');
              const style = document.getElementById('glass-benchmark-variant');
              style.textContent = `${shared}${css}}`;
              host.scrollTop = 0;
              const maxScroll = host.scrollHeight - host.clientHeight;
              const move = elapsed => {
                host.scrollTop = (0.5 - 0.5 * Math.cos(elapsed * 0.0048)) * maxScroll;
              };
              await new Promise(resolve => {
                let frames = 0;
                function warmup(timestamp) {
                  move(frames * 16.7);
                  frames += 1;
                  if (frames < 35) requestAnimationFrame(warmup);
                  else resolve();
                }
                requestAnimationFrame(warmup);
              });
              const intervals = [];
              const started = performance.now();
              let previous;
              await new Promise(resolve => {
                function frame(timestamp) {
                  if (previous !== undefined) intervals.push(timestamp - previous);
                  previous = timestamp;
                  move(timestamp - started);
                  if (performance.now() - started < duration) requestAnimationFrame(frame);
                  else resolve();
                }
                requestAnimationFrame(frame);
              });
              return {
                intervals,
                actualDurationMs: performance.now() - started,
                computedBackdropFilter: getComputedStyle(nav).backdropFilter,
                navBounds: { width: nav.getBoundingClientRect().width, height: nav.getBoundingClientRect().height },
              };
            }, { css: variants[name], shared: sharedStyle, duration: durationMs });
            if (name === 'blur' && run.computedBackdropFilter === 'none') throw new Error(`${engineName} did not apply backdrop-filter`);
            const intervals = run.intervals.filter(interval => interval > 0);
            profileResult.runs.push({
              round, variant: name, ...run,
              frameCount: intervals.length,
              p50Ms: percentile(intervals, 0.5),
              p95Ms: percentile(intervals, 0.95),
              p99Ms: percentile(intervals, 0.99),
              over25Ms: intervals.filter(interval => interval > 25).length,
              over33Ms: intervals.filter(interval => interval > 33).length,
            });
            process.stdout.write(`${engineName} ${profile.name} round ${round + 1}/${rounds} ${name}: p95 ${profileResult.runs.at(-1).p95Ms.toFixed(2)}ms, >25ms ${profileResult.runs.at(-1).over25Ms}\n`);
          }
        }
        profileResult.aggregate = Object.fromEntries(Object.keys(variants).map(name => {
          const runs = profileResult.runs.filter(run => run.variant === name);
          return [name, { p95Ms: summary(runs.map(run => run.p95Ms)), over25Ms: summary(runs.map(run => run.over25Ms)), frames: summary(runs.map(run => run.frameCount)) }];
        }));
        profileResult.pairedBlurMinusOpaqueP95Ms = summary([...Array(rounds).keys()].map(round => {
          const matching = profileResult.runs.filter(run => run.round === round);
          return matching.find(run => run.variant === 'blur').p95Ms - matching.find(run => run.variant === 'opaque').p95Ms;
        }));
        results.profiles.push(profileResult);
        writeFileSync(join(outputDirectory, 'raw-results.json'), JSON.stringify(results, null, 2));
      } finally {
        await context.close();
      }
    }
  } finally {
    await browser.close();
  }
}
writeFileSync(join(outputDirectory, 'raw-results.json'), JSON.stringify(results, null, 2));
