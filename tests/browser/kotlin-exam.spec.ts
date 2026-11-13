import { resolve } from 'node:path';
import { readFileSync } from 'node:fs';
import { expect, test, type Page } from '@playwright/test';
import { runExamPlaytest } from './exam-playtest-host';

async function forceJsCompatibilityFallback(page: Page) {
  await page.addInitScript(() => {
    Object.defineProperty(WebAssembly, 'validate', { configurable: true, value: () => false });
  });
}

test('DSL grammar PlayTest: Polish input, caret, return and score', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar.playtest'));
});

test('DSL catalog PlayTest: six official mapped papers', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/catalog-inventory.playtest'));
});

test('DSL grammar PlayTest: B2 2020 conditional word bank', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-word-bank-2020.playtest'));
});

for (const exam of ['b1-2020', 'b2-2017', 'b2-2019']) {
  test(`DSL grammar PlayTest: ${exam} source key earns credit`, async ({ page }) => {
    await runExamPlaytest(page, resolve(`dsl/exam-web/grammar-restored-${exam}.playtest`));
  });
}

test('DSL grammar PlayTest: open transformation stays pending manual review', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-manual.playtest'));
});

test('DSL grammar PlayTest: B1 2017 key permits other same-meaning sentences', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-b1-2017-open-manual.playtest'));
});

test('DSL grammar PlayTest: official transformation earns confirmed point', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-manual-known.playtest'));
});

test('DSL grammar PlayTest: B1 2017 half point score', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-weighted.playtest'));
});

test('DSL listening PlayTest: answer, media boundary, replay and cleanup', async ({ page }) => {
  // Use the actual distribution recording through the configured static server.
  await runExamPlaytest(page, resolve('dsl/exam-web/listening.playtest'));
});

test('DSL listening PlayTest: real recording replays three times after segment end', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-replay-cycles.playtest'));
});

test('DSL listening PlayTest: B1 2017 all audited segment boundaries', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-boundaries-2017.playtest'));
});

test('DSL listening PlayTest: B1 2019 all audited segment boundaries', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-boundaries-2019.playtest'));
});

for (const exam of ['b1-2020', 'b2-2017', 'b2-2019', 'b2-2020']) {
  test(`DSL listening PlayTest: ${exam} all audited segment boundaries`, async ({ page }) => {
    await runExamPlaytest(page, resolve(`dsl/exam-web/listening-boundaries-${exam}.playtest`));
  });
}

test('DSL listening PlayTest: missing recording is reported', async ({ page }) => {
  await page.route('**/exam-audio/B1-2019.mp3', route => route.fulfill({ status: 404 }));
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-unavailable.playtest'));
});

test('DSL listening PlayTest: real B1 2017 passport task runs beyond 40 seconds', async ({ page }) => {
  test.setTimeout(90_000);
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-passport.playtest'));
});

test('DSL reading PlayTest: B1 2019 has complete passages and questions', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/reading-2019-content.playtest'));
});

for (const exam of ['b1-2020', 'b2-2017', 'b2-2019', 'b2-2020']) {
  test(`DSL reading PlayTest: ${exam} source content and five tasks`, async ({ page }) => {
    await runExamPlaytest(page, resolve(`dsl/exam-web/reading-${exam}-content.playtest`));
  });
}

test('DSL reading PlayTest: B1 2017 truth answers earn two points', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/reading-2017-weights.playtest'));
});

test('DSL reading PlayTest: B1 2017 final task earns half a point', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/reading-2017-half.playtest'));
});

test('DSL writing PlayTest: B1 2017 keeps one set and loads its photo', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/writing-2017-sets.playtest'));
});

for (const [exam, caseName] of [
  ['b1-2020', 'writing-b1-2020-sets'],
  ['b2-2017', 'writing-b2-2017-photo'],
  ['b2-2019', 'writing-b2-2019-sets'],
  ['b2-2020', 'writing-b2-2020-sets'],
]) {
  test(`DSL writing PlayTest: ${exam} two tasks and source prompt`, async ({ page }) => {
    await runExamPlaytest(page, resolve(`dsl/exam-web/${caseName}.playtest`));
  });
}

test('DSL listening PlayTest: B1 2019 task V loads the image grid', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-2019-image.playtest'));
});

test('DSL listening PlayTest: B1 2020 printed TAK NIE earns credit', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b1-2020-true-false.playtest'));
});

test('DSL listening PlayTest: B1 2020 two keyed choices are unordered', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b1-2020-unordered.playtest'));
});

test('DSL listening PlayTest: B2 2017 three keyed choices are unordered', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b2-2017-unordered.playtest'));
});

test('DSL listening PlayTest: B2 2020 keyed word earns half point', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b2-2020-half-point.playtest'));
});

test('DSL listening PlayTest: B2 2017 unknown word awaits review', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b2-2017-manual.playtest'));
});

test('DSL listening PlayTest: B2 2024-02 printed key wording earns full credit', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b2-2024-02-key-variants.playtest'));
});

test('DSL listening PlayTest: B2 2024-02 unlisted wording awaits review', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b2-2024-02-unknown.playtest'));
});

test('DSL listening PlayTest: B2 2019 task IV loads source photographs', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-b2-2019-image.playtest'));
});

for (const exam of ['b1-2017', 'b1-2019', 'b1-2020', 'b2-2017', 'b2-2019', 'b2-2020']) {
  test(`DSL grammar PlayTest: ${exam} printed bank is selectable`, async ({ page }) => {
    await runExamPlaytest(page, resolve(`dsl/exam-web/grammar-bank-${exam}.playtest`));
  });
}

test('DSL grammar PlayTest: B1 2020 question target and answer survive navigation', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-question-target-2020.playtest'));
});

test('DSL grammar PlayTest: B2 2017 derived word survives navigation and scores', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-word-formation-2017.playtest'));
});

test('DSL reading PlayTest: B2 2020 printed fragment labels survive navigation', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/reading-fragment-2020.playtest'));
});

for (const exam of ['b1-2019', 'b1-2020']) {
  test(`DSL grammar PlayTest: ${exam} task VIII printed bank`, async ({ page }) => {
    await runExamPlaytest(page, resolve(`dsl/exam-web/grammar-bank-${exam}-viii.playtest`));
  });
}

test('DSL timed PlayTest: B1 2017 four certified parts and manual writing', async ({ page }) => {
  test.setTimeout(120_000);
  await runExamPlaytest(page, resolve('dsl/exam-web/timed-b1-2017.playtest'));
});

test('certified timed entry is available for sixteen complete source papers', async ({ page }) => {
  await page.goto('/#/exam');
  for (const title of [
    'B1 Certyfikat 2017', 'B1 Certyfikat 2019', 'B1 Certyfikat 2020',
    'B2 Certyfikat 2017', 'B2 Certyfikat 2019', 'B2 Certyfikat 2020',
    'B2 Certyfikat 2022-02', 'B2 Certyfikat 2023-02', 'B2 Certyfikat 2023-06',
    'B2 Certyfikat 2024-02', 'B2 Certyfikat 2024-06',
    'C1 Certyfikat 2022-03', 'C1 Certyfikat 2022-11', 'C1 Certyfikat 2023-04',
    'C1 Certyfikat 2023-11', 'C1 Certyfikat 2024-04',
  ]) {
    await page.locator('.exam-card').filter({ hasText: title }).getByRole('button', { name: 'Начать' }).click();
    await expect(page.getByRole('button', { name: 'Полный экзамен по времени' })).toBeVisible();
    await page.getByRole('button', { name: '← Выберите экзамен' }).click();
  }
  await expect(page.locator('.exam-card').filter({ hasText: 'B2 Certyfikat 2022-06' })).toHaveCount(0);
  await expect(page.locator('.exam-card')).toHaveCount(16);
  await expect(page.locator('.exam-card').filter({ hasText: 'B1 Przykładowy' })).toHaveCount(0);
});

test('Chromium compatibility exam route loads the Wasm production bundle', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium');
  await page.goto('/#/exam');
  await expect(page.locator('script[src="originWasmComposeApp.js"]')).toHaveCount(1);
  await expect(page.locator('.exam-card')).toHaveCount(16);
});

test('all sixteen published recordings support byte-range delivery', async ({ baseURL }) => {
  const source = readFileSync(resolve('dsl/exam-assets/exam-audio.core-data'), 'utf8');
  const paths = [...source.matchAll(/^  webPath: (exam-audio\/[A-Za-z0-9-]+\.mp3)$/gm)]
    .map(match => match[1]);
  expect(paths).toHaveLength(16);
  expect(new Set(paths).size).toBe(16);
  for (const path of paths) {
    const controller = new AbortController();
    const response = await fetch(new URL(path, baseURL), {
      headers: { Range: 'bytes=0-1023' }, signal: controller.signal,
    });
    const status = response.status;
    const range = response.headers.get('content-range');
    const length = response.headers.get('content-length');
    await response.body?.cancel();
    controller.abort();
    expect(status, path).toBe(206);
    expect(range, path).toMatch(/^bytes 0-1023\/\d+$/);
    expect(length, path).toBe('1024');
  }
});

test('compatibility exam catalog remains available through the JS fallback', async ({ page }) => {
  await forceJsCompatibilityFallback(page);
  await page.goto('/#/exam');
  await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
  await expect(page.locator('.exam-card')).toHaveCount(16);
});

test('JS fallback preserves Polish input and caret through the grammar PlayTest', async ({ page }) => {
  await forceJsCompatibilityFallback(page);
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar.playtest'));
  await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
});

test('JS fallback plays the real passport segment beyond 40 seconds', async ({ page }) => {
  test.setTimeout(90_000);
  await forceJsCompatibilityFallback(page);
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-passport.playtest'));
  await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
});

test('JS fallback completes the four certified timed parts', async ({ page }) => {
  test.setTimeout(120_000);
  await forceJsCompatibilityFallback(page);
  await runExamPlaytest(page, resolve('dsl/exam-web/timed-b1-2017.playtest'));
  await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
});

for (const [level, session] of [
  ['B2', '2022-02'], ['B2', '2023-02'], ['B2', '2023-06'],
  ['B2', '2024-02'], ['B2', '2024-06'],
  ['C1', '2022-03'], ['C1', '2022-11'], ['C1', '2023-04'],
  ['C1', '2023-11'], ['C1', '2024-04'],
] as const) {
  test(`${level} ${session} published listening and reading controls use source content`, async ({ page }) => {
    const title = `${level} Certyfikat ${session}`;
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.goto('/#/exam');
    const card = page.locator('.exam-card').filter({ hasText: title });
    await expect(card).toHaveCount(1);
    await card.getByRole('button', { name: 'Начать' }).click();
    await expect(page.locator('.exam-module-card')).toHaveCount(4);
    await page.locator('.exam-module-card').filter({ hasText: 'Rozumienie ze słuchu' })
      .getByRole('button', { name: 'Начать' }).click();
    await expect(page.locator('.exam-progress-text')).toContainText('1 / 5');
    await expect(page.locator('.exam-reading-q').first()).toBeVisible();
    await expect(page.locator('audio')).toHaveAttribute('src', `exam-audio/${level}-${session}.mp3`);
    await page.reload();
    await page.locator('.exam-card').filter({ hasText: title }).getByRole('button', { name: 'Начать' }).click();
    await page.locator('.exam-module-card').filter({ hasText: 'Rozumienie tekstów pisanych' })
      .getByRole('button', { name: 'Начать' }).click();
    await expect(page.locator('.exam-reading-q').first()).toBeVisible();
    await expect.poll(() => page.locator('.exam-reading-text').innerText().then(text => text.length))
      .toBeGreaterThan(100);
    expect(errors).toEqual([]);
  });

  test(`${level} ${session} five published audio windows play and seek`, async ({ page }) => {
    test.setTimeout(120_000);
    await runExamPlaytest(page, resolve(`dsl/exam-web/listening-boundaries-${level.toLowerCase()}-${session}.playtest`));
  });

  test(`${level} ${session} Writing set I preserves official prompts and manual drafts`, async ({ page }) => {
    await runExamPlaytest(page, resolve(`dsl/exam-web/writing-new-${level.toLowerCase()}-${session}.playtest`));
  });
}

test('DSL writing PlayTest: certified maximum remains pending manual assessment', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/writing-certified-manual.playtest'));
});

test('DSL grammar PlayTest: C1 April 2024 valid paired inflections', async ({ page }) => {
  test.setTimeout(120_000);
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-pair-c1-2024-valid.playtest'));
});

test('DSL grammar PlayTest: C1 April 2024 crossed inflections', async ({ page }) => {
  test.setTimeout(120_000);
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-pair-c1-2024-crossed.playtest'));
});

test('DSL grammar PlayTest: C1 November 2022 source rubric appears for manual review', async ({ page }) => {
  test.setTimeout(120_000);
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-rubric-c1-2022-11.playtest'));
});

test('timed session expires on tab focus after a wall-clock jump', async ({ page }) => {
  await page.clock.install({ time: new Date('2026-11-08T12:00:00.000Z') });
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.getByRole('button', { name: 'Полный экзамен по времени' }).click();
  await expect(page.locator('.exam-timed-clock')).toHaveText('25:00');
  await page.clock.setFixedTime(new Date('2026-11-08T12:25:01.000Z'));
  await page.evaluate(() => window.dispatchEvent(new Event('focus')));
  await expect(page.locator('.exam-progress').first()).toHaveText('Часть 2 / 4');
  await expect(page.locator('.exam-timed-clock')).toHaveText('45:00');
  await expect(page.locator('audio')).toHaveCount(0);
  await page.evaluate(() => window.dispatchEvent(new Event('focus')));
  await expect(page.locator('.exam-progress').first()).toHaveText('Часть 2 / 4');
});
