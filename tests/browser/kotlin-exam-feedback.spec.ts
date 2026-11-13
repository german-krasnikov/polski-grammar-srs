import { expect, test, type Page } from '@playwright/test';

async function forceJsCompatibilityFallback(page: Page) {
  await page.addInitScript(() => {
    Object.defineProperty(WebAssembly, 'validate', { configurable: true, value: () => false });
  });
}

test('practice choice reveals chosen and source answer, freezes first tap, then advances once', async ({ page }) => {
  await page.clock.install();
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await expect(page.locator('.exam-progress-text')).toContainText('1 / 50');
  await page.clock.pauseAt(new Date((await page.evaluate(() => Date.now())) + 10_000));
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await expect(page.getByRole('status')).toContainText('Неверно');
  await expect(page.getByRole('status')).toContainText('Ваш ответ: dobry teatr');
  await expect(page.getByRole('status')).toContainText('dobrego teatru');
  await expect(page.locator('.exam-option.feedback-wrong')).toContainText('dobry teatr');
  await expect(page.locator('.exam-option.feedback-correct')).toContainText('dobrego teatru');
  const lockedOption = await page.locator('.exam-option').filter({ hasText: 'dobrego teatru' }).boundingBox();
  if (!lockedOption) throw new Error('Correct option has no pointer target');
  await page.mouse.click(lockedOption.x + lockedOption.width / 2, lockedOption.y + lockedOption.height / 2);
  await expect(page.locator('.exam-option.feedback-wrong')).toContainText('dobry teatr');
  await page.clock.runFor(1_999);
  await expect(page.locator('.exam-progress-text')).toContainText('1 / 50');
  await page.clock.runFor(1);
  await expect(page.locator('.exam-progress-text')).toContainText('2 / 50');
  await expect(page.getByRole('status')).toHaveCount(0);
  await page.clock.runFor(2_100);
  await expect(page.locator('.exam-progress-text')).toContainText('2 / 50');
  for (let i = 0; i < 48; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  await expect(page.locator('.exam-score-big')).toContainText('0%');
  await expect(page.locator('.exam-review-item').first()).toContainText('Ваш ответ: dobry teatr');
  await expect(page.locator('.exam-review-item').first()).toContainText('dobrego teatru');
  await expect(page.locator('.exam-review-item').first()).toHaveClass(/wrong/);
});

test('manual next cancels feedback timer and preference OFF keeps choices editable', async ({ page }) => {
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await page.getByRole('button', { name: 'Далее →' }).click();
  await page.waitForTimeout(2_100);
  await expect(page.locator('.exam-progress-text')).toContainText('2 / 50');
  await expect(page.getByRole('status')).toHaveCount(0);
  await page.goto('/#/settings');
  await page.locator('#settings-exam-immediate-feedback').uncheck();
  await page.reload();
  await expect(page.locator('#settings-exam-immediate-feedback')).not.toBeChecked();
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobrego teatru' }).click();
  await expect(page.locator('.exam-option.selected')).toContainText('dobrego teatru');
  await expect(page.getByRole('status')).toHaveCount(0);
});

test('imported OFF survives reload and an older profile defaults feedback back ON', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') await forceJsCompatibilityFallback(page);
  await page.goto('/#/settings');
  const feedback = page.locator('#settings-exam-immediate-feedback');
  await expect(feedback).toBeChecked();
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await page.getByLabel('Файл JSON настроек для импорта').setInputFiles({
    name: 'feedback-off.json', mimeType: 'application/json',
    buffer: Buffer.from('{"schemaVersion":3,"immediateExamFeedbackEnabled":false}'),
  });
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await expect(feedback).not.toBeChecked();
  await page.reload();
  await expect(feedback).not.toBeChecked();
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobrego teatru' }).click();
  await expect(page.locator('.exam-option.selected')).toContainText('dobrego teatru');
  await expect(page.getByRole('status')).toHaveCount(0);

  await page.goto('/#/settings');
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await page.getByLabel('Файл JSON настроек для импорта').setInputFiles({
    name: 'legacy-settings.json', mimeType: 'application/json',
    buffer: Buffer.from('{"schemaVersion":1,"appearance":"Dark"}'),
  });
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await expect(feedback).toBeChecked();
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await expect(page.getByRole('status')).toContainText('Ваш ответ: dobry teatr');
  await expect(page.getByRole('status')).toContainText('dobrego teatru');
});

test('timed entry keeps early choices editable and hides the source key', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') await forceJsCompatibilityFallback(page);
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.getByRole('button', { name: 'Полный экзамен по времени' }).click();
  await expect(page.locator('.exam-progress').first()).toHaveText('Часть 1 / 4');
  const first = page.locator('.exam-reading-q').first();
  const options = first.locator('.exam-option');
  await expect(options.nth(1)).toBeVisible();
  await options.first().click();
  await options.nth(1).click();
  await expect(options.nth(1)).toHaveClass(/selected/);
  await expect(options.first()).not.toHaveClass(/selected/);
  await expect(first.getByRole('status')).toHaveCount(0);
  await expect(first.locator('.exam-option.feedback-wrong, .exam-option.feedback-correct')).toHaveCount(0);
});

test('route teardown and reduced motion keep feedback semantic and cancel stale advance', async ({ page }) => {
  await page.goto('/#/settings');
  await page.locator('#settings-motion').selectOption('Reduced');
  await page.locator('#settings-animations').uncheck();
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await expect(page.getByRole('status')).toContainText('Неверно');
  await page.goto('/#/settings');
  await page.waitForTimeout(2_100);
  await page.goto('/#/exam');
  await expect(page.locator('.exam-list')).toBeVisible();
  await expect(page.getByRole('status')).toHaveCount(0);
});

test('JS fallback shows practice feedback and advances after two seconds', async ({ page }) => {
  await forceJsCompatibilityFallback(page);
  await page.clock.install();
  await page.goto('/#/exam');
  await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await expect(page.getByRole('status')).toContainText('Ваш ответ: dobry teatr');
  await page.clock.runFor(2_000);
  await expect(page.locator('.exam-progress-text')).toContainText('2 / 50');
});

test('real hidden tab clears feedback and prevents stale advance', async ({ page }) => {
  test.skip(process.env.P14_VISIBILITY_TEST !== '1', 'Run the real tab visibility lane with P14_VISIBILITY_TEST=1');
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await expect(page.getByRole('status')).toContainText('Неверно');
  const emulation = await page.context().newCDPSession(page);
  const otherTab = await page.context().newPage();
  const otherEmulation = await page.context().newCDPSession(otherTab);
  try {
    await emulation.send('Emulation.setFocusEmulationEnabled', { enabled: false });
    await otherEmulation.send('Emulation.setFocusEmulationEnabled', { enabled: false });
    await otherTab.bringToFront();
    await expect.poll(() => page.evaluate(() => document.hidden)).toBe(true);
    await page.waitForTimeout(2_100);
    await page.bringToFront();
    await expect(page.getByRole('status')).toHaveCount(0);
    await expect(page.locator('.exam-progress-text')).toContainText('1 / 50');
  } finally {
    await otherEmulation.detach();
    await otherTab.close();
    await emulation.detach();
  }
});

test('returned committed choice stays visibly and accessibly locked without stale verdict', async ({ page }) => {
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2017' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-option').filter({ hasText: 'dobry teatr' }).click();
  await expect(page.getByRole('status')).toContainText('Неверно');
  await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: '← Назад' }).click();
  await expect(page.locator('.exam-progress-text')).toContainText('1 / 50');
  await expect(page.getByRole('status')).toHaveCount(0);
  await expect(page.getByRole('note')).toContainText('Ответ зафиксирован');
  await expect(page.getByRole('note')).toContainText('Ваш ответ: dobry teatr');
  const options = page.locator('.exam-option');
  await expect(options).toHaveCount(3);
  for (const option of await options.all()) {
    await expect(option).toBeDisabled();
    await expect(option).toHaveAttribute('aria-disabled', 'true');
  }
  await expect(page.locator('.exam-option.selected')).toContainText('dobry teatr');
  await expect(page.locator('.exam-option.feedback-wrong, .exam-option.feedback-correct')).toHaveCount(0);
  const correct = await options.filter({ hasText: 'dobrego teatru' }).boundingBox();
  if (!correct) throw new Error('Correct option has no pointer target');
  await page.mouse.click(correct.x + correct.width / 2, correct.y + correct.height / 2);
  await expect(page.locator('.exam-option.selected')).toContainText('dobry teatr');
  await page.getByRole('button', { name: 'Далее →' }).focus();
  await page.keyboard.press('Shift+Tab');
  await expect(page.locator('.exam-option:focus')).toHaveCount(0);
  await page.waitForTimeout(2_100);
  await expect(page.locator('.exam-progress-text')).toContainText('1 / 50');
  await expect(page.getByRole('status')).toHaveCount(0);
  for (let i = 0; i < 49; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  await expect(page.locator('.exam-score-big')).toContainText('0%');
  await expect(page.locator('.exam-review-item').first()).toContainText('Ваш ответ: dobry teatr');
  await expect(page.locator('.exam-review-item').first()).toHaveClass(/wrong/);
});

test('grammar text commits on Check, locks on return, and advances after two seconds', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') await forceJsCompatibilityFallback(page);
  await page.clock.install();
  await page.goto('/#/exam');
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await expect(page.locator('script[src="originJsComposeApp.js"]')).toHaveCount(1);
  }
  await page.locator('.exam-card').filter({ hasText: 'C1 Certyfikat 2024-04' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await expect(page.locator('.exam-context')).toContainText('(9)');
  const input = page.locator('#exam-grammar-input');
  await input.fill('   ');
  await page.getByRole('button', { name: 'Проверить', exact: true }).click();
  await expect(input).toBeEnabled();
  await expect(page.getByRole('status')).toHaveCount(0);
  await input.fill('próba');
  await expect(input).toHaveValue('próba');
  await expect(page.getByRole('status')).toHaveCount(0);
  await input.fill('zły');
  await expect(page.getByRole('status')).toHaveCount(0);
  await expect(input).toBeEnabled();
  await page.clock.pauseAt(new Date((await page.evaluate(() => Date.now())) + 10_000));
  await page.getByRole('button', { name: 'Проверить', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Ваш ответ: zły');
  await expect(page.getByRole('status')).toContainText('dziewięciu');
  await expect(input).toBeDisabled();
  await page.clock.runFor(1_999);
  await expect(page.locator('.exam-progress-text')).toContainText('1 /');
  await page.clock.runFor(1);
  await expect(page.locator('.exam-progress-text')).toContainText('2 /');
  await page.getByRole('button', { name: 'Назад' }).click();
  await expect(page.locator('#exam-grammar-input')).toHaveValue('zły');
  await expect(page.locator('#exam-grammar-input')).toBeDisabled();
  await expect(page.getByRole('note')).toContainText('Ответ зафиксирован');
  await expect(page.getByRole('status')).toHaveCount(0);
});

test('grammar bank commits token identity, freezes its controls, and keeps source answer visible', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') await forceJsCompatibilityFallback(page);
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'C1 Certyfikat 2024-04' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  for (let i = 0; i < 35; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await expect(page.locator('.exam-context')).toContainText('Luka 1');
  const bank = page.locator('.exam-word-bank .exam-option');
  await bank.filter({ hasText: 'Jeszcze' }).first().click();
  await expect(page.getByRole('status')).toContainText('Ваш ответ: jeszcze');
  await expect(bank.first()).toBeDisabled();
  const selectedId = await page.locator('.exam-word-bank .exam-option.selected').getAttribute('data-bank-token-id');
  expect(selectedId).toBeTruthy();
  await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Назад' }).click();
  await expect(page.locator(`.exam-word-bank .exam-option[data-bank-token-id="${selectedId}"]`)).toHaveClass(/selected/);
  await expect(page.locator('.exam-word-bank .exam-option').first()).toBeDisabled();
  await expect(page.getByRole('note')).toContainText('Ответ зафиксирован');
});

test('practice bank keeps the second printed w token distinct after navigation', async ({ page }) => {
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2020' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' })
    .getByRole('button', { name: 'Начать' }).click();
  for (let i = 0; i < 50; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  const bank = page.locator('.exam-word-bank .exam-option');
  await expect.poll(() => bank.allTextContents()).toEqual(['do', 'w', 'nad', 'przed', 'z', 'na', 'w']);
  await page.getByRole('button', { name: 'w, 2' }).click();
  await expect(page.getByRole('status')).toContainText('Ваш ответ: w');
  const second = page.locator('.exam-word-bank .exam-option[data-bank-token-id="certyfikat-polski-B1-2020-grammar-8-bank-7"]');
  await expect(second).toHaveClass(/selected/);
  await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Назад' }).click();
  await expect(second).toHaveClass(/selected/);
  await expect(page.locator('.exam-word-bank .exam-option[data-bank-token-id="certyfikat-polski-B1-2020-grammar-8-bank-2"]')).not.toHaveClass(/selected/);
  for (const option of await bank.all()) await expect(option).toBeDisabled();
  await expect(page.getByRole('note')).toContainText('Ответ зафиксирован · Ваш ответ: w');
  await expect(page.getByRole('status')).toHaveCount(0);
});

test('paired grammar waits for partner and then confirms source tuple', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') await forceJsCompatibilityFallback(page);
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'C1 Certyfikat 2024-04' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  for (let i = 0; i < 7; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await expect(page.locator('.exam-context')).toContainText('(kilka)');
  await page.locator('#exam-grammar-input').fill('kilkorgiem');
  await page.getByRole('button', { name: 'Проверить', exact: true }).click();
  await expect(page.getByRole('status')).toHaveCount(0);
  await expect(page.locator('#exam-grammar-input')).toBeDisabled();
  await page.waitForTimeout(2_100);
  await expect(page.locator('.exam-context')).toContainText('(kilka)');
  await page.getByRole('button', { name: 'Далее →' }).click();
  await expect(page.locator('.exam-context')).toContainText('(przyjaciel)');
  await page.locator('#exam-grammar-input').fill('przyjaciół');
  await page.getByRole('button', { name: 'Проверить', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Ваш ответ: przyjaciół');
  await expect(page.getByRole('status')).not.toContainText('Неверно');
});

test('open manual grammar shows pending review and never auto advances', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') await forceJsCompatibilityFallback(page);
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'C1 Certyfikat 2024-04' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  for (let i = 0; i < 10; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.locator('#exam-grammar-input').fill('Мой ответ');
  await page.getByRole('button', { name: 'Проверить', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Требуется ручная проверка');
  await expect(page.getByRole('status')).not.toContainText('Неверно');
  await page.waitForTimeout(2_100);
  await expect(page.locator('.exam-progress-text')).toContainText('11 /');
  await expect(page.locator('#exam-grammar-input')).toBeDisabled();
});

test('grammar submitted review freezes wrong, blank, and pending answers with source examples', async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') await forceJsCompatibilityFallback(page);
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'C1 Certyfikat 2024-04' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Poprawność gramatyczna' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('#exam-grammar-input').fill('zły');
  await page.getByRole('button', { name: 'Проверить', exact: true }).click();
  await page.getByRole('button', { name: 'Далее →' }).click();
  for (let i = 0; i < 9; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.locator('#exam-grammar-input').fill('Mój opis');
  await page.getByRole('button', { name: 'Проверить', exact: true }).click();
  for (let i = 0; i < 64; i++) await page.getByRole('button', { name: 'Далее →' }).click();
  await page.getByRole('button', { name: 'Завершить' }).click();
  const review = page.locator('.exam-review-item');
  await expect(review.first()).toHaveClass(/wrong/);
  await expect(review.first()).toContainText('Ваш ответ: zły');
  await expect(review.first()).toContainText('dziewięciu');
  await expect(review.nth(1)).toContainText('Ответ не введён');
  await expect(review.nth(10)).toHaveClass(/pending/);
  await expect(review.nth(10)).toContainText('Ваш ответ: Mój opis');
  await expect(review.nth(10)).toContainText('Требуется ручная проверка');
});
