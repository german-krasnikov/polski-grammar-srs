import { expect, test } from '@playwright/test';

for (const scheme of ['light', 'dark'] as const) {
  test(`React controls remain readable in system ${scheme}`, async ({ page }, testInfo) => {
    await page.emulateMedia({ colorScheme: scheme });
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/');
    const select = page.getByRole('combobox').first();
    await expect(select).toBeVisible();
    await expect(page.locator('html')).toHaveCSS('color-scheme', scheme);
    const colors = await select.evaluate(element => {
      const style = getComputedStyle(element);
      return { foreground: style.color, background: style.backgroundColor };
    });
    expect(colors.foreground).not.toBe(colors.background);
    await page.screenshot({ path: testInfo.outputPath(`react-${scheme}-390.png`), fullPage: true });
    await page.emulateMedia({ colorScheme: scheme === 'light' ? 'dark' : 'light' });
    await expect(page.locator('html')).toHaveCSS('color-scheme', scheme === 'light' ? 'dark' : 'light');
  });
}
