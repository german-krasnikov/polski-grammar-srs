import { resolve } from 'node:path';
import { test } from '@playwright/test';
import { runExamPlaytest } from './exam-playtest-host';

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
