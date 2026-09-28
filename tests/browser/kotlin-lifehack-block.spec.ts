import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

// EN-21 (Plans/Kotlin/EnRuPackPlan.md §4.2/§4.3/§6): the "Лайфхак" block is not a StyleComposer
// block kind — it must render after the resolved style's own Back blocks for every style, be
// collapsed by default with a visible source-attribution caption (ADR-15's editorial/community
// distinction), and be entirely absent (not an empty frame) for a skill with no authored
// lifehack. courses/pairs/pl-ru/lifehacks.json now has full 16-skill coverage (2 records per
// skill, EnRuPackPlan.md §4.4/§7 follow-up), so every skill renders 2 toggles — tests below scope
// to `.first()`/`.nth()` and no longer assume a single record per skill. Genuinely empty-list
// rendering (no real pl-ru skill is empty any more) stays covered by
// `LifehackTest.staticProviderReturnsEmptyForASkillWithNoAuthoredLifehack` (fixture id), not here.

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

  // case.gen.neg now has 2 authored records — this exercises the original one (still first).
  const toggle = back.getByRole('button', { name: 'Лайфхак · источник: editorial' }).first();
  await expect(toggle).toBeVisible();
  await expect(toggle).toHaveAttribute('aria-expanded', 'false');
  const content = back.locator('.lifehack-content').first();
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

// case.inst's original authored record (EN-20, still first) has no source.url — the citation
// still renders, just without a link, proving the link is genuinely optional rather than always
// expected. case.inst now has a 2nd record too, so the toggle/content locators are scoped first().
test('a lifehack with no source URL still shows its citation, without a link', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Narzędnik · z / być · A1' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');
  await back.getByRole('button', { name: 'Лайфхак · источник: editorial' }).first().click();
  const citation = back.locator('.lifehack-citation').first();
  await expect(citation).toContainText('Bielec, D. (1998)');
  await expect(citation.locator('a')).toHaveCount(0);
});

// pronouns ("Zaimki osobowe") gained real coverage in this follow-up (was empty under EN-20's
// deliberately incomplete 5-record set) — now renders 2 independent, independently-collapsible
// entries, proving the block isn't limited to a single lifehack per skill.
test('a skill with two authored lifehacks shows two independent collapsible entries', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Zaimki osobowe · A2' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const back = page.locator('.card-back');
  const toggles = back.getByRole('button', { name: 'Лайфхак · источник: editorial' });
  await expect(toggles).toHaveCount(2);

  const firstContent = back.locator('.lifehack-content').nth(0);
  const secondContent = back.locator('.lifehack-content').nth(1);
  await toggles.nth(0).click();
  await expect(firstContent).toHaveClass(/expanded/);
  await expect(secondContent).not.toHaveClass(/expanded/);
  await expect(firstContent.locator('.lifehack-text')).toContainText('«je»');
});

// This host follow-up: a small "Есть лайфхак" badge on the FRONT of the task card (before reveal),
// naming only that a tip exists — never its text — and not itself triggering the whole front's
// tap-to-reveal gesture.
test('the front of the card shows a non-spoiling "Есть лайфхак" badge, and tapping it does not reveal the card', async ({ page }) => {
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Dopełniacz · negacja · A2' }).click();
  await continueIntroductionIfPresent(page);
  const badge = page.locator('.card-front .lifehack-badge');
  await expect(badge).toBeVisible();
  await expect(badge).toHaveAccessibleName(/Есть лайфхак/);
  // The lifehack's own text (asserted above, "падеж при отрицании") must not leak into the badge.
  await expect(badge).not.toContainText('падеж при отрицании');
  await badge.click();
  await expect(page.locator('.card-back')).toHaveCount(0); // still unrevealed
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
});

// This host follow-up: the "Лайфхаки" sub-section of "Таблицы и схема" — every skill's tips,
// grouped and collapsed by default, in curriculum order.
test('Matrix "Лайфхаки" sub-section groups every skill\'s tips, collapsed by default', async ({ page }) => {
  await page.goto('/#/matrix');
  await page.getByRole('button', { name: 'Лайфхаки', exact: true }).click();
  const groups = page.locator('.matrix-page .lifehack-toggle');
  await expect(groups.first()).toBeVisible();
  const groupCount = await groups.count();
  expect(groupCount).toBeGreaterThanOrEqual(16); // pl-ru has full 16-skill coverage (§4.4 follow-up)

  const firstToggle = groups.first();
  const firstContent = page.locator('.matrix-page .lifehack-content').first();
  await expect(firstToggle).toHaveAttribute('aria-expanded', 'false');
  await expect(firstContent).toHaveAttribute('inert', '');
  await firstToggle.click();
  await expect(firstToggle).toHaveAttribute('aria-expanded', 'true');
  await expect(firstContent).not.toHaveAttribute('inert', '');
  await expect(firstContent.locator('.lifehack-source-label').first()).toContainText('Лайфхак · источник:');
  await expect(firstContent.locator('.lifehack-text').first()).not.toBeEmpty();
});

// EnRuPackPlan.md §4.4: en-ru has its own ~2-per-skill set — the same web code path
// (StaticPackLifehackProvider reads `packRegistry.active`), so switching the active pack in
// Settings must switch both the front badge and the Matrix "Лайфхаки" listing, not just pl-ru's.
test('lifehacks (badge + Matrix section) also render for the en-ru pack once selected in Settings', async ({ page }) => {
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Изучаемый язык' }).selectOption('en');
  await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.locator('.card-front .lifehack-badge')).toBeVisible();

  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.getByRole('button', { name: 'Лайфхаки', exact: true }).click();
  const groups = page.locator('.matrix-page .lifehack-toggle');
  const groupCount = await groups.count();
  expect(groupCount).toBeGreaterThanOrEqual(16);
});
