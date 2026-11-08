import { resolve } from 'node:path';
import { test } from '@playwright/test';
import { runExamPlaytest } from './exam-playtest-host';

test('DSL grammar PlayTest: Polish input, caret, return and score', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar.playtest'));
});

test('DSL grammar PlayTest: B1 2017 half point score', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/grammar-weighted.playtest'));
});

test('DSL listening PlayTest: answer, media boundary, replay and cleanup', async ({ page }) => {
  // Use the actual distribution recording through the configured static server.
  await runExamPlaytest(page, resolve('dsl/exam-web/listening.playtest'));
});

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

test('DSL reading PlayTest: B1 2017 truth answers earn two points', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/reading-2017-weights.playtest'));
});

test('DSL reading PlayTest: B1 2017 final task earns half a point', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/reading-2017-half.playtest'));
});

test('DSL writing PlayTest: B1 2017 keeps one set and loads its photo', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/writing-2017-sets.playtest'));
});

test('DSL listening PlayTest: B1 2019 task V loads the image grid', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/listening-2019-image.playtest'));
});
