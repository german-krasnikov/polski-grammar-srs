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
