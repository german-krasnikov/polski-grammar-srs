import { expect, test, type Locator } from '@playwright/test';

// Flatten the computed background layers before comparing the rendered text color.
// This includes translucent feedback/review tints and the disabled option opacity.
async function textContrast(locator: Locator) {
  return locator.evaluate(element => {
    const color = (value: string) => {
      const parts = (value.match(/[\d.]+/g) ?? []).map(Number);
      return [parts[0] ?? 0, parts[1] ?? 0, parts[2] ?? 0, parts[3] ?? 1];
    };
    const blend = (top: number[], below: number[]) => top.slice(0, 3)
      .map((channel, index) => channel * top[3] + below[index] * (1 - top[3]));
    const chain: Element[] = [];
    for (let current: Element | null = element; current; current = current.parentElement) chain.unshift(current);
    let background = [255, 255, 255];
    for (const node of chain) background = blend(color(getComputedStyle(node).backgroundColor), background);
    const style = getComputedStyle(element);
    const foreground = blend([...color(style.color).slice(0, 3), Number(style.opacity)], background);
    const luminance = (rgb: number[]) => rgb.map(channel => {
      const scaled = channel / 255;
      return scaled <= 0.04045 ? scaled / 12.92 : ((scaled + 0.055) / 1.055) ** 2.4;
    }).reduce((sum, channel, index) => sum + channel * [0.2126, 0.7152, 0.0722][index], 0);
    const a = luminance(foreground);
    const b = luminance(background);
    return { ratio: (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05), foreground, background };
  });
}

async function recordReadability(locator: Locator, label: string, readings: Array<{ label: string; ratio: number; foreground: number[]; background: number[] }>) {
  await expect(locator).toBeVisible();
  const measured = await textContrast(locator);
  readings.push({ label, ...measured });
}

async function openPart(page: import('@playwright/test').Page, exam: string, module: string) {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      Object.defineProperty(WebAssembly, 'validate', { configurable: true, value: () => false });
    });
  }
  await page.goto('/#/exam');
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
  }
  await page.locator('.exam-card').filter({ hasText: exam }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: module }).getByRole('button', { name: 'Начать' }).click();
}

test('default Reading practice explains a wrong choice and focuses another question in its passage', async ({ page }) => {
  await page.clock.install();
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
  const first = page.locator('.exam-reading-q').first();
  await page.clock.pauseAt(new Date((await page.evaluate(() => Date.now())) + 10_000));
  await first.getByRole('button', { name: 'kioski internetowe są popularne w całym kraju' }).click();
  await expect(first.getByRole('status')).toContainText('Ваш ответ: kioski internetowe są popularne w całym kraju');
  await expect(first.getByRole('status')).toContainText('kioski internetowe oferują dostęp do Internetu');
  await expect(first.locator('.exam-option.feedback-wrong')).toContainText('kioski internetowe są popularne');
  await expect(first.locator('.exam-option.feedback-correct')).toContainText('kioski internetowe oferują');
  await page.clock.runFor(1_999);
  await expect(page.locator('.exam-reading-q').nth(1)).not.toHaveClass(/practice-focused/);
  await page.clock.runFor(1);
  await expect(page.locator('.exam-reading-q').nth(1)).toHaveClass(/practice-focused/);
  await expect(page.locator('.exam-reading-q').nth(1)).toBeFocused();
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
});

test('default Listening practice keeps playing the same audio while feedback focuses a question in its task', async ({ page }) => {
  await openPart(page, 'B1 Certyfikat 2019', 'Rozumienie ze słuchu');
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
  const audio = page.locator('audio');
  const audioHandle = await audio.elementHandle();
  if (!audioHandle) throw new Error('No source audio element');
  await page.locator('.player-play-btn').click();
  await expect.poll(() => audio.evaluate(el => (el as HTMLAudioElement).paused)).toBe(false);
  const before = await audio.evaluate(el => (el as HTMLAudioElement).currentTime);
  const first = page.locator('.exam-reading-q').first();
  await first.getByRole('button', { name: 'a) w kinie.' }).click();
  await expect(first.getByRole('status')).toContainText('Ваш ответ: a) w kinie.');
  await expect(first.getByRole('status')).toContainText('b) w bibliotece.');
  await expect(page.locator('.exam-reading-q').nth(1)).toHaveClass(/practice-focused/, { timeout: 5_000 });
  await expect(page.locator('.exam-reading-q').nth(1)).toBeFocused();
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
  expect(await audioHandle.evaluate(el => el.isConnected)).toBe(true);
  expect(await audio.evaluate(el => (el as HTMLAudioElement).paused)).toBe(false);
  expect(await audio.evaluate(el => (el as HTMLAudioElement).currentTime)).toBeGreaterThan(before);
});

test('Reading true-false and matching choices explain source answers', async ({ page }) => {
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  await page.getByRole('button', { name: 'Далее →' }).click();
  const trueFalse = page.locator('.exam-reading-q').first();
  await trueFalse.getByRole('button', { name: 'FAŁSZ' }).click();
  await expect(trueFalse.getByRole('status')).toContainText('Ваш ответ: FAŁSZ');
  await expect(trueFalse.getByRole('status')).toContainText('PRAWDA');
  await page.getByRole('button', { name: 'Далее →' }).click();
  const matching = page.locator('.exam-reading-q').first();
  await matching.getByRole('button', { name: 'B', exact: true }).click();
  await expect(matching.getByRole('status')).toContainText('Ваш ответ: B');
  await expect(matching.getByRole('status')).toContainText('F');
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 3');
});

test('Reading final group answer stays until explicit Next and return keeps neutral locks', async ({ page }) => {
  await page.clock.install();
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  const rows = page.locator('.exam-reading-q');
  await page.clock.pauseAt(new Date((await page.evaluate(() => Date.now())) + 10_000));
  for (let i = 0; i < 5; i++) await rows.nth(i).locator('.exam-option').first().click();
  await expect(rows.nth(4).getByRole('status')).toBeVisible();
  await page.clock.runFor(2_100);
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
  await expect(rows.nth(4).getByRole('status')).toBeVisible();
  await page.getByRole('button', { name: 'Далее →' }).click();
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 2');
  await page.getByRole('button', { name: 'Назад' }).click();
  await expect(page.locator('.exam-reading-q').first().getByRole('note')).toContainText('Ответ зафиксирован');
  await expect(page.locator('.exam-reading-q').first().locator('.exam-option').first()).toBeDisabled();
  await expect(page.locator('.exam-reading-q').first().getByRole('status')).toHaveCount(0);
});

test('Reading focus wraps to the earlier unanswered row without leaving its passage', async ({ page }) => {
  await page.clock.install();
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  await page.clock.pauseAt(new Date((await page.evaluate(() => Date.now())) + 10_000));
  const rows = page.locator('.exam-reading-q');
  await expect(rows).toHaveCount(5);
  for (let i = 1; i < 5; i++) await rows.nth(i).locator('.exam-option').first().click();
  await expect(rows.nth(4).getByRole('status')).toBeVisible();
  await page.clock.runFor(1_999);
  await expect(rows.first()).not.toHaveClass(/practice-focused/);
  await page.clock.runFor(1);
  await expect(rows.first()).toHaveClass(/practice-focused/);
  await expect(rows.first()).toBeFocused();
  await expect(rows.first().locator('.exam-option').first()).toBeEnabled();
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
});

test('Listening unordered choices commit at source slot count with partial credit', async ({ page }) => {
  await openPart(page, 'B2 Certyfikat 2017', 'Rozumienie ze słuchu');
  for (let i = 0; i < 3; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  const row = page.locator('.exam-reading-q').first();
  await expect(row).toContainText('różnych szkolnych pomieszczeniach');
  await row.getByRole('button', { name: '1', exact: true }).click();
  await expect(row.getByRole('status')).toHaveCount(0);
  await expect(row.getByRole('button', { name: '1', exact: true })).toBeEnabled();
  await row.getByRole('button', { name: '3', exact: true }).click();
  await expect(row.getByRole('status')).toContainText('Ваш ответ: 1|3');
  await expect(row.getByRole('status')).toContainText('1|2');
  await expect(row.getByRole('status')).toContainText('+1 / 2');
  await expect(row.getByRole('button', { name: '2', exact: true })).toBeDisabled();
  for (const option of await row.locator('.exam-option').all()) await expect(option).toBeDisabled();
  const chosen = await row.locator('.exam-option.selected').allTextContents();
  const extra = row.getByRole('button', { name: '2', exact: true });
  const bounds = await extra.boundingBox();
  if (!bounds) throw new Error('Printed extra choice has no pointer target');
  await page.mouse.click(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
  await expect(row.locator('.exam-option.selected')).toHaveText(chosen);
});

test('Listening text keeps draft editable until Check and explicit Next changes segment', async ({ page }) => {
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie ze słuchu');
  await page.getByRole('button', { name: 'Далее →' }).click();
  await page.locator('.player-play-btn').click();
  await expect.poll(() => page.locator('audio').evaluate(el => (el as HTMLAudioElement).paused)).toBe(false);
  const first = page.locator('.exam-reading-q').first();
  const input = first.locator('.exam-input');
  await input.fill('zły');
  await expect(first.getByRole('status')).toHaveCount(0);
  await expect(input).toBeEnabled();
  await first.getByRole('button', { name: 'Проверить', exact: true }).click();
  await expect(first.getByRole('status')).toContainText('Ваш ответ: zły');
  await expect(first.getByRole('status')).toContainText('przez');
  await expect(input).toBeDisabled();
  const before = await page.locator('audio').evaluate(el => (el as HTMLAudioElement).currentTime);
  await page.getByRole('button', { name: 'Далее →' }).click();
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 3');
  const after = await page.locator('audio').evaluate(el => (el as HTMLAudioElement).currentTime);
  expect(after).toBeGreaterThan(before);
  expect(await page.locator('audio').evaluate(el => (el as HTMLAudioElement).paused)).toBe(true);
});

test('submitted Reading and Listening reviews name the chosen wrong answer and official source', async ({ page }) => {
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  await page.locator('.exam-reading-q').first().getByRole('button', { name: 'kioski internetowe są popularne w całym kraju' }).click();
  for (let i = 0; i < 4; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  const readingReview = page.locator('.exam-review-item');
  await expect(readingReview.first()).toHaveClass(/wrong/);
  await expect(readingReview.first()).toContainText('Ваш ответ: kioski internetowe są popularne w całym kraju');
  await expect(readingReview.first()).toContainText('Образцы из ключа: kioski internetowe oferują dostęp do Internetu');
  await expect(readingReview.nth(1)).toContainText('Ответ не введён');
  await page.getByRole('button', { name: 'К модулям' }).click();
  await page.goto('/#/exam');
  await page.reload();
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2019' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Rozumienie ze słuchu' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-reading-q').first().getByRole('button', { name: 'w kinie' }).click();
  for (let i = 0; i < 4; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  const listeningReview = page.locator('.exam-review-item');
  await expect(listeningReview.first()).toHaveClass(/wrong/);
  await expect(listeningReview.first()).toContainText('Ваш ответ: a) w kinie.');
  await expect(listeningReview.first()).toContainText('Образцы из ключа: b) w bibliotece.');
  await expect(listeningReview.nth(1)).toContainText('Ответ не введён');
});

test('preference OFF keeps Reading and Listening answers editable without an early key', async ({ page }) => {
  await page.goto('/#/settings');
  await page.locator('#settings-exam-immediate-feedback').uncheck();
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  const row = page.locator('.exam-reading-q').first();
  await row.getByRole('button', { name: 'kioski internetowe są popularne w całym kraju' }).click();
  await row.getByRole('button', { name: 'kioski internetowe oferują dostęp do Internetu' }).click();
  await expect(row.locator('.exam-option.selected')).toContainText('kioski internetowe oferują');
  await expect(row.getByRole('status')).toHaveCount(0);
  await page.goto('/#/exam');
  await page.reload();
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2019' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Rozumienie ze słuchu' }).getByRole('button', { name: 'Начать' }).click();
  const listening = page.locator('.exam-reading-q').first();
  await listening.getByRole('button', { name: 'a) w kinie.' }).click();
  await listening.getByRole('button', { name: 'b) w bibliotece.' }).click();
  await expect(listening.locator('.exam-option.selected')).toContainText('b) w bibliotece.');
  await expect(listening.getByRole('status')).toHaveCount(0);
});

test('Listening keeps the same playing audio beyond 41 seconds while feedback changes question focus', async ({ page }) => {
  test.setTimeout(90_000);
  await openPart(page, 'B1 Certyfikat 2019', 'Rozumienie ze słuchu');
  const audio = page.locator('audio');
  const audioHandle = await audio.elementHandle();
  if (!audioHandle) throw new Error('No source audio element');
  await page.locator('.player-play-btn').click();
  await expect.poll(() => audio.evaluate(el => (el as HTMLAudioElement).paused)).toBe(false);
  const start = await audio.evaluate(el => (el as HTMLAudioElement).currentTime);
  await page.locator('.exam-reading-q').first().getByRole('button', { name: 'a) w kinie.' }).click();
  await expect(page.locator('.exam-reading-q').nth(1)).toHaveClass(/practice-focused/, { timeout: 5_000 });
  await page.waitForTimeout(41_500);
  expect(await audioHandle.evaluate(el => el.isConnected)).toBe(true);
  expect(await audio.evaluate(el => (el as HTMLAudioElement).paused)).toBe(false);
  expect(await audio.evaluate(el => (el as HTMLAudioElement).currentTime)).toBeGreaterThan(start + 41);
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
});

test('new Reading answer cancels the prior question overlay and callback', async ({ page }) => {
  await page.clock.install();
  await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
  await page.clock.pauseAt(new Date((await page.evaluate(() => Date.now())) + 10_000));
  const rows = page.locator('.exam-reading-q');
  await rows.first().locator('.exam-option').first().click();
  await expect(rows.first().getByRole('status')).toBeVisible();
  await page.clock.runFor(500);
  await rows.nth(1).locator('.exam-option').first().click();
  await expect(rows.first().getByRole('status')).toHaveCount(0);
  await expect(rows.first().getByRole('note')).toContainText('Ответ зафиксирован');
  await expect(rows.nth(1).getByRole('status')).toBeVisible();
  await page.clock.runFor(1_500);
  await expect(rows.nth(1)).not.toHaveClass(/practice-focused/);
  await page.clock.runFor(500);
  await expect(rows.nth(2)).toHaveClass(/practice-focused/);
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
});

test('new Listening answer clears stale overlay without replacing audio', async ({ page }) => {
  await page.clock.install();
  await openPart(page, 'B1 Certyfikat 2019', 'Rozumienie ze słuchu');
  await page.clock.pauseAt(new Date((await page.evaluate(() => Date.now())) + 10_000));
  const audio = await page.locator('audio').elementHandle();
  if (!audio) throw new Error('No source audio element');
  const rows = page.locator('.exam-reading-q');
  await rows.first().locator('.exam-option').first().click();
  await page.clock.runFor(500);
  await rows.nth(1).locator('.exam-option').first().click();
  await expect(rows.first().getByRole('status')).toHaveCount(0);
  await expect(rows.first().getByRole('note')).toContainText('Ответ зафиксирован');
  await expect(rows.nth(1).getByRole('status')).toBeVisible();
  expect(await audio.evaluate(el => el.isConnected)).toBe(true);
  await page.clock.runFor(2_000);
  await expect(rows.nth(2)).toHaveClass(/practice-focused/);
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1');
});

for (const theme of ['Dark', 'Light'] as const) {
  test(`grouped feedback and frozen report text remain readable in ${theme.toLowerCase()} theme`, async ({ page }) => {
    const readings: Array<{ label: string; ratio: number; foreground: number[]; background: number[] }> = [];
    if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
      await page.addInitScript(() => {
        Object.defineProperty(WebAssembly, 'validate', { configurable: true, value: () => false });
      });
    }
    await page.goto('/#/settings');
    await page.getByRole('combobox', { name: 'Тема' }).selectOption(theme);
    await expect(page.locator('html')).toHaveAttribute('data-theme', theme.toLowerCase());

    await openPart(page, 'B1 Certyfikat 2017', 'Rozumienie tekstów pisanych');
    const reading = page.locator('.exam-reading-q').first();
    await reading.getByRole('button', { name: 'kioski internetowe są popularne w całym kraju' }).click();
    const wrong = reading.locator('.exam-option.feedback-wrong');
    const correct = reading.locator('.exam-option.feedback-correct');
    for (const option of [wrong, correct]) {
      await expect(option).toBeDisabled();
      await expect(option).toHaveAttribute('aria-disabled', 'true');
      await expect(option).toHaveCSS('cursor', 'not-allowed');
    }
    const wrongColor = await wrong.evaluate(element => getComputedStyle(element).color);
    const correctColor = await correct.evaluate(element => getComputedStyle(element).color);
    const channels = (value: string) => (value.match(/[\d.]+/g) ?? []).slice(0, 3).map(Number);
    const [wrongRed, wrongGreen] = channels(wrongColor);
    const [correctRed, correctGreen] = channels(correctColor);
    expect(wrongRed).toBeGreaterThan(wrongGreen);
    expect(correctGreen).toBeGreaterThan(correctRed);
    await recordReadability(reading.locator('.exam-option.feedback-wrong'), `${theme} chosen wrong`, readings);
    await recordReadability(reading.locator('.exam-option.feedback-correct'), `${theme} source correct`, readings);
    await recordReadability(reading.getByRole('status'), `${theme} chosen and source feedback`, readings);
    for (let i = 0; i < 4; i++) await page.getByRole('button', { name: 'Далее →' }).click();
    await page.getByRole('button', { name: 'Завершить' }).click();
    await recordReadability(page.locator('.exam-review-item.wrong .exam-review-chosen').first(), `${theme} wrong review choice`, readings);
    await recordReadability(page.locator('.exam-review-item.wrong .exam-review-correct').first(), `${theme} review source answer`, readings);

    await page.reload();
    await openPart(page, 'B2 Certyfikat 2017', 'Rozumienie ze słuchu');
    for (let i = 0; i < 3; i++) await page.getByRole('button', { name: 'Далее →' }).click();
    const set = page.locator('.exam-reading-q').first();
    await set.getByRole('button', { name: '1', exact: true }).click();
    await set.getByRole('button', { name: '3', exact: true }).click();
    await expect(set.locator('.exam-option.feedback-partial')).toContainText('3');
    await expect(set.locator('.exam-option.feedback-wrong')).toHaveCount(0);
    await expect(set.locator('.exam-option.feedback-partial')).toHaveCSS('color', await page.locator('body').evaluate(element => getComputedStyle(element).color));
    await recordReadability(set.getByRole('status'), `${theme} partial feedback`, readings);
    await page.getByRole('button', { name: 'Далее →' }).click();
    const manual = page.locator('.exam-reading-q').first();
    await manual.locator('.exam-input').fill('uprzejmą');
    await manual.getByRole('button', { name: 'Проверить', exact: true }).click();
    await expect(manual.locator('.exam-input')).toBeDisabled();
    await expect(manual.locator('.feedback-wrong, .feedback-correct')).toHaveCount(0);
    await recordReadability(manual.getByRole('status'), `${theme} pending feedback`, readings);
    await page.getByRole('button', { name: 'Завершить' }).click();
    await recordReadability(page.locator('.exam-review-item.partial .exam-review-chosen').first(), `${theme} partial review choice`, readings);
    await recordReadability(page.locator('.exam-review-item.partial .exam-review-correct').first(), `${theme} partial review credit`, readings);
    await recordReadability(page.locator('.exam-review-item.pending .exam-review-chosen').first(), `${theme} pending review choice`, readings);
    await recordReadability(page.locator('.exam-review-item.pending .exam-review-manual').first(), `${theme} pending review label`, readings);
    console.log(`Contrast readings: ${JSON.stringify(readings)}`);
    for (const reading of readings) {
      expect(reading.ratio, `${reading.label}: ${JSON.stringify(reading)}`).toBeGreaterThanOrEqual(4.5);
    }
  });

  test(`grammar committed choice and frozen source remain readable in ${theme.toLowerCase()} theme`, async ({ page }) => {
    const readings: Array<{ label: string; ratio: number; foreground: number[]; background: number[] }> = [];
    if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
      await page.addInitScript(() => {
        Object.defineProperty(WebAssembly, 'validate', { configurable: true, value: () => false });
      });
    }
    await page.goto('/#/settings');
    await page.getByRole('combobox', { name: 'Тема' }).selectOption(theme);
    await expect(page.locator('html')).toHaveAttribute('data-theme', theme.toLowerCase());
    await openPart(page, 'B1 Certyfikat 2017', 'Poprawność gramatyczna');
    await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
    await recordReadability(page.locator('.exam-option.feedback-wrong'), `${theme} grammar chosen wrong`, readings);
    await recordReadability(page.locator('.exam-option.feedback-correct'), `${theme} grammar source correct`, readings);
    await page.getByRole('button', { name: 'Далее →' }).click();
    await page.getByRole('button', { name: '← Назад' }).click();
    await expect(page.getByRole('note')).toContainText('Ответ зафиксирован');
    await expect(page.locator('.exam-option.selected')).toBeDisabled();
    await expect(page.locator('.exam-option.selected')).toHaveAttribute('aria-disabled', 'true');
    await expect(page.locator('.exam-option.selected')).toHaveCSS('cursor', 'not-allowed');
    await recordReadability(page.locator('.exam-option.selected'), `${theme} grammar locked choice`, readings);
    await recordReadability(page.getByRole('note'), `${theme} grammar lock note`, readings);
    for (let i = 0; i < 49; i++) await page.getByRole('button', { name: 'Далее →' }).click();
    await page.getByRole('button', { name: 'Завершить' }).click();
    await recordReadability(page.locator('.exam-review-item.wrong .exam-review-correct').first(), `${theme} grammar review source`, readings);
    console.log(`Grammar contrast readings: ${JSON.stringify(readings)}`);
    for (const reading of readings) {
      expect(reading.ratio, `${reading.label}: ${JSON.stringify(reading)}`).toBeGreaterThanOrEqual(4.5);
    }
  });
}
