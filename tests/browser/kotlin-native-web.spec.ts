import { expect, test } from '@playwright/test';

for (const width of [320, 390, 768, 1280]) {
  test(`top navigation stays usable at ${width}px without optical elements`, async ({ page }) => {
    await page.setViewportSize({ width, height: 800 });
    await page.goto('/#/training');
    const nav = page.getByRole('navigation', { name: 'Основные разделы' });
    await expect(nav.getByRole('button')).toHaveCount(5);
    await expect(page.locator('liquid-glass, #primary-nav-backdrop, #primary-nav-lens, nav canvas')).toHaveCount(0);
    expect(await page.locator('canvas').count()).toBeLessThanOrEqual(1); // Compose host canvas only.
    await expect(page.locator('#settings-glass-tint')).toHaveCount(0);
    await expect(page.locator('#nav-training')).toHaveAttribute('aria-current', 'page');
    const buttons = await nav.getByRole('button').evaluateAll(elements => elements.map(element => {
      const rect = element.getBoundingClientRect();
      return { left: rect.left, right: rect.right, width: rect.width, height: rect.height };
    }));
    expect(buttons.every(button => button.left >= 0 && button.right <= width && button.width >= 44 && button.height >= 44)).toBe(true);
    await page.locator('#nav-settings').focus();
    await page.keyboard.press('Enter');
    await expect(page.locator('#nav-settings')).toHaveAttribute('aria-current', 'page');
    await expect(page.getByRole('heading', { name: 'Настройки обучения' })).toBeVisible();
    // UX5: a route change moves focus to the new screen's own heading, not back to the nav
    // button that triggered it (RouteSlider/focusRouteHeading) — see kotlin-ux4.spec.ts's own
    // UX5 tests for the general contract.
    await expect(page.getByRole('heading', { name: 'Настройки обучения' })).toBeFocused();
    await expect(page.locator('#settings-glass-tint')).toHaveCount(0);
    const order = await page.evaluate(() => {
      const header = document.querySelector('header.top')!.getBoundingClientRect();
      const nav = document.querySelector('nav.primary-nav')!.getBoundingClientRect();
      const content = document.querySelector('.route-content')!.getBoundingClientRect();
      return { headerBottom: header.bottom, navTop: nav.top, navBottom: nav.bottom, contentTop: content.top };
    });
    expect(order.navTop).toBeGreaterThanOrEqual(order.headerBottom);
    expect(order.navBottom).toBeLessThanOrEqual(order.contentTop);
  });
}

test('System theme follows OS changes without reload and manual theme wins', async ({ page }) => {
  await page.emulateMedia({ colorScheme: 'light' });
  await page.goto('/#/settings');
  const theme = page.getByRole('combobox', { name: 'Тема' });
  await theme.selectOption('System');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await page.emulateMedia({ colorScheme: 'dark' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await theme.selectOption('Light');
  await page.emulateMedia({ colorScheme: 'dark' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await expect(page.locator('html')).toHaveCSS('color-scheme', 'light');
  await theme.selectOption('Dark');
  await expect(page.locator('html')).toHaveCSS('color-scheme', 'dark');
});
