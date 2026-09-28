import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// EN-21 (Plans/Kotlin/EnRuPackPlan.md §4.2/§4.3/§6): the "Лайфхак" block is not a StyleComposer
// block kind — it must render after the resolved style's own Back blocks for every style, be
// collapsed by default with a visible source-attribution caption (ADR-15's editorial/community
// distinction), and be entirely absent (not an empty frame) for a skill with no authored
// lifehack. courses/pairs/pl-ru/lifehacks.json (EN-20) authored exactly 5 real records, so this
// exercises the real pl-ru pack end to end, not a fixture.

test.beforeEach(async ({ page }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  await page.goto('/');
  await continueIntroductionIfPresent(page);
});

test('a skill with an authored lifehack shows a collapsed block, with the source attribution and citation link visible on expand', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Dopełniacz · negacja · A2' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');

  const toggle = back.getByRole('button', { name: 'Лайфхак · источник: editorial' });
  await expect(toggle).toBeVisible();
  await expect(toggle).toHaveAttribute('aria-expanded', 'false');
  const content = back.locator('.lifehack-content');
  await expect(content).not.toHaveClass(/expanded/);
  await expect(content).toHaveAttribute('inert', '');

  await toggle.click();
  await expect(toggle).toHaveAttribute('aria-expanded', 'true');
  await expect(content).toHaveClass(/expanded/);
  await expect(content).not.toHaveAttribute('inert', '');
  await expect(content.locator('.lifehack-text')).toContainText('падеж при отрицании');
  const link = content.locator('.lifehack-citation a');
  await expect(link).toHaveText('источник');
  await expect(link).toHaveAttribute('href', 'https://www.slavica.com/grammar-of-contemporary-polish.html');
});

// case.inst's authored record (EN-20) has no source.url — the citation still renders, just
// without a link, proving the link is genuinely optional rather than always expected.
test('a lifehack with no source URL still shows its citation, without a link', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Narzędnik · z / być · A1' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');
  await back.getByRole('button', { name: 'Лайфхак · источник: editorial' }).click();
  const citation = back.locator('.lifehack-citation');
  await expect(citation).toContainText('Bielec, D. (1998)');
  await expect(citation.locator('a')).toHaveCount(0);
});

// pronouns ("Zaimki osobowe") has no entry in the 5-record pl-ru lifehacks.json — the block must
// not render at all (no empty frame), on either Front or Back.
test('a skill with no authored lifehack shows no lifehack block at all', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Zaimki osobowe · A2' }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.locator('.card-front .lifehack-block')).toHaveCount(0);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.locator('.card-back .lifehack-block')).toHaveCount(0);
});
