import { test, expect, type Page } from '@playwright/test';
import { mkdirSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import type { Progress } from '../../src/types';

const key = 'polski-grammar-srs-v1';
const fixedTime = new Date('2026-02-03T12:00:00.000Z');
const artifactDir = join(process.cwd(), 'Plans/Kotlin/artifacts/stage1');
const fresh = (): Progress => {
  const file = JSON.parse(readFileSync(join(process.cwd(), 'tests/fixtures/kotlin-parity/progress.json'), 'utf8')) as { cases: { id: string; expected: unknown }[] };
  return structuredClone(file.cases.find(item => item.id === 'P-fresh')!.expected as Progress);
};
const stored = (page: Page) => page.evaluate((storageKey) => JSON.parse(localStorage.getItem(storageKey)!), key);
const continueIntroductionIfPresent = async (page: Page) => {
  const next = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await next.count()) await next.click();
};
const reveal = async (page: Page) => {
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: /Показать ответ/ }).click();
};
const rate = (page: Page, label = /2 Вспомнил/) => page.getByRole('button', { name: label }).click();
const screenshot = async (page: Page, name: string) => {
  mkdirSync(artifactDir, { recursive: true });
  await page.evaluate(() => document.fonts.ready);
  await page.screenshot({ path: join(artifactDir, `${name}.png`), fullPage: true, animations: 'disabled' });
};

test.beforeEach(async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
  (page as Page & { baselineErrors?: string[] }).baselineErrors = errors;
  await page.clock.setFixedTime(fixedTime);
});
test.afterEach(async ({ page }) => {
  expect((page as Page & { baselineErrors?: string[] }).baselineErrors).toEqual([]);
});

test('five linked cards save binary oral ratings and advance to the next seed', async ({ page }) => {
  await page.goto('/');
  expect(await stored(page)).toBeNull();
  await expect(page.getByText('To jest moja piękna żona.')).toBeVisible();
  await expect(page.getByText('Widzę moją piękną żonę.')).toHaveCount(0);
  await expect(page.locator('.source-sentence .change-before')).toHaveText(['a', 'a', 'a']);
  const expected = ['Widzę moją piękną żonę.', 'Widziałem moją piękną żonę.', 'Nie widziałem mojej pięknej żony.', 'Nie widziałem ich pięknej żony.', 'Mówię o ich pięknej żonie.'];
  const gradeLabels = [/1 Повторить/, /2 Вспомнил/, /2 Вспомнил/, /2 Вспомнил/, /2 Вспомнил/];
  for (let i = 0; i < 5; i++) {
    await reveal(page);
    await expect(page.locator('.answer-sentence')).toHaveText(expected[i]);
    if (i === 0) {
      await expect(page.locator('.change-pair .ending-highlight')).toHaveText(['ą', 'ą', 'ę']);
      await expect(page.locator('.answer-sentence .change-after')).toHaveText(['ą', 'ą', 'ę']);
      await expect(page.locator('.change-pair .change-before')).toHaveText(['a', 'a', 'a']);
      const colours = await page.locator('.change-pair strong').first().evaluate(element => {
        const ending = element.querySelector('.ending-highlight')!;
        return [getComputedStyle(element).color, getComputedStyle(ending).color];
      });
      expect(colours[1]).not.toBe(colours[0]);
    }
    await rate(page, gradeLabels[i]);
    await expect.poll(async () => (await stored(page)).totalReviews).toBe(i + 1);
    if (i < 4) await expect(page.locator('.source-sentence')).toHaveText(expected[i]);
  }
  await expect(page.getByRole('heading', { name: 'Цепочка завершена' })).toBeVisible();
  await page.getByRole('button', { name: 'Следующий набор слов' }).click();
  await expect(page.locator('.source-sentence')).toContainText('mąż');
});

test('explanation method switches on the same draft and survives reload', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('Moja próba');
  await page.getByRole('combobox', { name: 'Подача объяснений' }).selectOption('situations');
  await expect(page.locator('.method-retrieve')).toContainText('Представь ситуацию и скажи целое предложение самостоятельно.');
  await expect(page.getByRole('textbox', { name: 'Ответ по-польски' })).toHaveValue('Moja próba');
  await expect(page.locator('.source-sentence')).toHaveText('To jest moja piękna żona.');
  await page.reload();
  await expect(page.getByRole('combobox', { name: 'Подача объяснений' })).toHaveValue('situations');
  await reveal(page);
  await expect(page.locator('.rule-contrast')).toContainText('moja piękna żona');
  await expect(page.locator('.rule-contrast')).toContainText('moją piękną żonę');
});

test('word cards keep both directions and a user entry across reload', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('checkbox').first().check();
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('жена');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('Moja żona czyta książkę.');
  await page.getByRole('button', { name: 'Вспомнил' }).click();
  const wordStore = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(wordStore.cards['pl-ru:vocabulary:ru-pl:noun.wife'].reps).toBe(1);
  expect(wordStore.cards['pl-ru:vocabulary:pl-ru:noun.wife']).toBeUndefined();
  await page.getByRole('combobox', { name: 'Направление карточки' }).selectOption('pl-ru');
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('żona');
  await page.getByLabel('Польское слово').fill('szkoła');
  await page.getByLabel('Перевод', { exact: true }).fill('школа');
  await page.getByLabel('Форма').fill('szkoła · род. szkoły');
  await page.getByLabel('Пример в предложении').fill('To jest moja szkoła.');
  await page.getByRole('button', { name: 'Добавить слово' }).click();
  await page.reload();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('mine');
  await expect(page.getByText('szkoła')).toBeVisible();
});

test('frequency catalog reports exact coverage and disables unready ranks', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('100');
  await expect(page.getByRole('status')).toHaveText('Готово 7/100 · недоступно 93');
  await expect(page.locator('.catalog-list input:disabled')).toHaveCount(93);
  await expect(page.getByRole('link', { name: 'Leksjo / NKJP, CC BY 4.0' })).toHaveAttribute('href', /nkjp-frekwencja\.csv/);
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('500');
  await expect(page.getByRole('status')).toHaveText('Готово 23/500 · недоступно 477');
});

test('imported custom word cannot make a deferred frequency rank selectable', async ({ page }) => {
  const imported = JSON.parse(readFileSync('tests/fixtures/react-vocabulary-sample.json', 'utf8'));
  const customId = 'user.00000000-0000-4000-8000-000000000091';
  imported.custom.push({ id: customId, lemma: 'w', translation: 'в (собственное)', form: 'w', example: 'Jestem w domu.', level: '—', frequencyRank: null, custom: true });
  imported.selectedIds.push(customId);
  imported.cards[`pl-ru:vocabulary:ru-pl:${customId}`] = structuredClone(imported.cards['pl-ru:vocabulary:ru-pl:noun.wife']);
  await page.goto('/');
  await page.evaluate(value => localStorage.setItem('polski-vocabulary-pl-ru-v1', JSON.stringify(value)), imported);
  await page.reload();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('100');
  await expect(page.getByRole('status')).toHaveText('Готово 7/100 · недоступно 93');
  await expect(page.locator('.catalog-list input:disabled')).toHaveCount(93);
  await expect(page.locator('.catalog-row').first()).toContainText('Перевод и пример ещё не проверены');
  await expect(page.locator('.catalog-row').first().getByRole('checkbox')).toBeDisabled();
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('mine');
  await expect(page.locator('.catalog-row').getByRole('checkbox')).toBeChecked();
  const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(saved.selectedIds).toContain(customId);
  expect(saved.cards[`pl-ru:vocabulary:ru-pl:${customId}`]).toEqual(imported.cards[`pl-ru:vocabulary:ru-pl:${customId}`]);
});

test('clean React profile restores a legacy shipped-homonym custom export', async ({ page }) => {
  const legacy = JSON.parse(readFileSync('tests/fixtures/react-vocabulary-sample.json', 'utf8'));
  const customId = 'user.00000000-0000-4000-8000-000000000091';
  legacy.custom.push({ id: customId, lemma: 'żona', translation: 'супруга (личное)', form: 'żona', example: 'To jest moja żona.', level: '—', frequencyRank: null, custom: true });
  legacy.selectedIds.push(customId);
  legacy.cards[`pl-ru:vocabulary:ru-pl:${customId}`] = structuredClone(legacy.cards['pl-ru:vocabulary:ru-pl:noun.wife']);
  await page.goto('/');
  expect(await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'))).toBeNull();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.locator('input[type="file"]').setInputFiles({ name: 'legacy-homonym.json', mimeType: 'application/json', buffer: Buffer.from(JSON.stringify(legacy)) });
  await expect(page.getByRole('alert')).toHaveCount(0);
  const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(saved).toEqual(legacy);
  await page.reload();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('100');
  await expect(page.getByRole('status')).toHaveText('Готово 7/100 · недоступно 93');
  await expect(page.locator('.catalog-row').first().getByRole('checkbox')).toBeDisabled();
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('mine');
  await expect(page.getByText('Мой словарь · 2')).toBeVisible();
  await expect(page.locator('.catalog-row').filter({ hasText: 'супруга (личное)' }).getByRole('checkbox')).toBeChecked();
});

test('four-card vocabulary file round-trips and rejects invalid pair before write', async ({ page }) => {
  const fixture = JSON.parse(readFileSync('tests/fixtures/vocabulary-four-cards.json', 'utf8'));
  await page.goto('/');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.locator('input[type="file"]').setInputFiles({ name: 'four-cards.json', mimeType: 'application/json', buffer: Buffer.from(JSON.stringify(fixture)) });
  await expect(page.getByText('Мой словарь · 2')).toBeVisible();
  const exported = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Экспорт словаря JSON' }).click();
  expect(JSON.parse(readFileSync((await (await exported).path())!, 'utf8'))).toEqual(fixture);
  const before = await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'));
  await page.locator('input[type="file"]').setInputFiles({ name: 'wrong-pair.json', mimeType: 'application/json', buffer: Buffer.from(JSON.stringify({ ...fixture, pair: 'de-ru' })) });
  await expect(page.getByRole('alert')).toBeVisible();
  expect(await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'))).toBe(before);
  await page.reload();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  expect(JSON.parse((await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1')))!)).toEqual(fixture);
});

test('typed answer freezes after Enter, records actual correctness and clears on mode change', async ({ page }) => {
  await page.goto('/');
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  const answer = page.getByRole('textbox', { name: 'Ответ по-польски' });
  await answer.fill('WIDZĘ MOJĄ PIĘKNĄ ŻONĘ!');
  await answer.press('Shift+Enter');
  await expect(answer).toBeVisible();
  await answer.press('Enter');
  await expect(page.getByText('Совпадает с правильным вариантом')).toBeVisible();
  await expect(answer).toHaveCount(0);
  await rate(page);
  expect((await stored(page)).stats['case.acc.f']).toMatchObject({ reviews: 1, correct: 1 });
  await expect(page.locator('.source-sentence')).toHaveText('Widzę moją piękną żonę.');
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: /Смешанные трансформации/ }).click();
  await expect(page.locator('.source-sentence')).toBeVisible();
  await expect(page.getByText('Совпадает с правильным вариантом')).toHaveCount(0);
});

test('accepted typed alternative counts correct; wrong text counts a mistake despite Good rating', async ({ page }) => {
  await page.addInitScript(() => { Math.random = () => 0.01; });
  await page.goto('/');
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: /Смешанные трансформации/ }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('Nie widziałaś mojej pięknej żony.');
  await page.getByRole('button', { name: /Проверить и показать ответ/ }).click();
  await expect(page.getByText('Совпадает с правильным вариантом')).toBeVisible();
  await rate(page);
  expect((await stored(page)).stats.mixed).toMatchObject({ reviews: 1, correct: 1, mistakes: 0 });
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('To jest błędne.');
  await page.getByRole('button', { name: /Проверить и показать ответ/ }).click();
  await expect(page.getByText('Сравни свой ответ с эталоном')).toBeVisible();
  await rate(page);
  expect((await stored(page)).stats.mixed).toMatchObject({ reviews: 2, correct: 1, mistakes: 1, streak: 0 });
});

test('global shortcuts ignore modifiers and focused input controls', async ({ page }) => {
  await page.goto('/');
  await page.locator('body').click({ position: { x: 1, y: 1 } });
  await page.keyboard.press('Control+Space');
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  const field = page.getByRole('textbox', { name: 'Ответ по-польски' });
  await field.focus();
  await page.keyboard.press('Space');
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  await field.fill('Ala');
  await field.press('Shift+Enter');
  await expect(field).toHaveValue('Ala\n');
});

test('characterizes focus after advancing a card', async ({ page }) => {
  await page.goto('/');
  await reveal(page);
  await rate(page);
  await expect(page.locator('.source-sentence')).toHaveText('Widzę moją piękną żonę.');
  await expect(page.getByRole('button', { name: 'Перейти к заданию' })).toBeVisible();
  await continueIntroductionIfPresent(page);
  await expect(page.locator('.reveal-button')).toBeFocused();
});

test('schedule queue, keyboard guard and reference preserve the current card', async ({ page }) => {
  const seed = fresh();
  seed.cards.forEach((card: { card: { due: string } }) => { card.card.due = '2099-01-01T00:00:00.000Z'; });
  await page.addInitScript(({ key, value }) => localStorage.setItem(key, JSON.stringify(value)), { key, value: seed });
  await page.goto('/');
  await page.getByRole('button', { name: /По расписанию/ }).click();
  await expect(page.getByRole('heading', { name: 'Повторения на сейчас завершены' })).toBeVisible();
  await page.getByRole('button', { name: 'Потренировать цепочку' }).click();
  const source = await page.locator('.source-sentence').innerText();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Таблица под рукой' }).click();
  await expect(page.getByRole('table')).toBeVisible();
  await expect(page.locator('.source-sentence')).toHaveText(source);
  await page.getByRole('button', { name: 'Скрыть таблицу' }).click();
  await continueIntroductionIfPresent(page);
  await page.locator('body').click({ position: { x: 1, y: 1 } });
  await page.keyboard.press('Space');
  await expect(page.locator('.answer-sentence')).toBeVisible();
  await page.keyboard.press('2');
  await page.keyboard.press('2');
  expect((await stored(page)).totalReviews).toBe(1);
});

test('matrix selectors, gender forms, comparison and drill work in the browser', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Мужской Biernik: дерево решений' })).toBeVisible();
  await page.getByRole('button', { name: 'Падежи и окончания' }).click();
  await page.getByLabel('Эталонное слово').selectOption('friendM');
  await page.getByLabel('Прилагательное').selectOption('good');
  await page.getByLabel('Владелец').selectOption('their');
  await page.getByLabel('Число').selectOption('pl');
  await expect(page.locator('td .form-contrast-after').filter({ hasText: /^Widzę ich dobrych kolegów\.$/ })).toBeVisible();
  await expect(page.locator('.comparison-table tbody tr')).toHaveCount(7);
  await page.setViewportSize({ width: 320, height: 700 });
  const comparisonScroll = page.locator('.comparison-table').locator('..');
  const horizontalRange = await comparisonScroll.evaluate(element => element.scrollWidth - element.clientWidth);
  expect(horizontalRange).toBeGreaterThan(0);
  await comparisonScroll.evaluate(element => { element.scrollLeft = element.scrollWidth; });
  expect(await comparisonScroll.evaluate(element => element.scrollLeft)).toBeGreaterThan(0);
  await expect(page.locator('.comparison-table tbody tr').first().locator('td').last()).toBeVisible();
  await page.setViewportSize({ width: 1280, height: 720 });
  await page.getByRole('button', { name: 'Времена и лица' }).click();
  await page.getByLabel('Глагол').selectOption('go');
  await expect(page.getByRole('table').first().getByText('szedłem', { exact: true }).first()).toBeVisible();
  await page.getByLabel('Род для ja / ty / my / wy').selectOption('f');
  await expect(page.getByRole('table').first().getByText('szłam', { exact: true }).first()).toBeVisible();
  await expect(page.getByRole('table').first().locator('tbody tr')).toHaveCount(9);
  await page.getByRole('button', { name: 'Местоимения' }).click();
  await expect(page.getByText('ze mną', { exact: true }).first()).toBeVisible();
  await page.getByRole('button', { name: 'Тренировать смену владельца' }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('heading', { name: /Замени «мой/ })).toBeVisible();
});

test('matrix pipeline keeps three authored steps and opens its chain drill', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  const pipeline = page.locator('.matrix-section').filter({ has: page.getByRole('heading', { name: 'Сначала конструкция, затем формы' }) });
  await expect(pipeline.locator('.rule-pipeline > div')).toHaveCount(3);
  await expect(pipeline.locator('.rule-pipeline > b[aria-hidden="true"]')).toHaveText(['→', '→']);
  await expect(pipeline.locator('.rule-pipeline small')).toHaveText(['01 · Смысл', '02 · Операция', '03 · Согласование']);
  await expect(pipeline.locator('.rule-pipeline strong')).toHaveText(['Что хочу сказать?', 'Какой падеж нужен?', 'Меняю всю группу']);
  await expect(pipeline.locator('.rule-pipeline span')).toHaveText(['Вижу / не вижу / говорю о…', 'widzę → Biernik', 'moją + piękną + żonę']);
  await page.getByRole('button', { name: 'Тренировать эту цепочку' }).click();
  await expect(page.getByRole('button', { name: 'Карточки' })).toBeVisible();
});

test('progress survives reload, exports JSON and reset cancel/confirm', async ({ page }) => {
  await page.goto('/');
  await reveal(page);
  await rate(page);
  await page.reload();
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await expect(page.locator('.bigstats b').first()).toHaveText('1');
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Экспорт JSON' }).click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe('polski-srs-progress.json');
  const stream = await download.createReadStream();
  const chunks: Buffer[] = [];
  for await (const chunk of stream) chunks.push(Buffer.from(chunk));
  expect(JSON.parse(Buffer.concat(chunks).toString()).totalReviews).toBe(1);
  page.once('dialog', dialog => dialog.dismiss());
  await page.getByRole('button', { name: 'Сбросить прогресс' }).click();
  expect((await stored(page)).totalReviews).toBe(1);
  page.once('dialog', dialog => dialog.accept());
  await page.getByRole('button', { name: 'Сбросить прогресс' }).click();
  await expect.poll(async () => (await stored(page)).totalReviews).toBe(0);
});

test('storage write failure reports a visible recovery action', async ({ page }) => {
  await page.addInitScript(() => {
    const original = Storage.prototype.setItem;
    Storage.prototype.setItem = function (key, value) {
      if (key === 'polski-grammar-srs-v1') throw new DOMException('quota', 'QuotaExceededError');
      return original.call(this, key, value);
    };
  });
  await page.goto('/');
  await reveal(page);
  await rate(page);
  await expect(page.getByRole('alert')).toContainText('Экспортируй JSON');
  expect(await stored(page)).toBeNull();
});

test('reviewable screenshots cover desktop, 320px, landscape and reduced motion', async ({ page }) => {
  test.setTimeout(60_000);
  await page.goto('/');
  await screenshot(page, 'desktop-front');
  await reveal(page);
  await screenshot(page, 'desktop-revealed');
  await page.getByRole('button', { name: 'Цепочка предложений' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await screenshot(page, 'desktop-typed');
  if (await page.getByRole('button', { name: 'Таблица под рукой' }).isVisible())
    await page.getByRole('button', { name: 'Таблица под рукой' }).click();
  await screenshot(page, 'desktop-reference');
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  for (const [section, label] of [['map', 'Карта системы'], ['cases', 'Падежи и окончания'], ['verbs', 'Времена и лица'], ['pronouns', 'Местоимения']] as const) {
    await page.getByRole('button', { name: label }).click();
    await screenshot(page, `desktop-matrix-${section}`);
  }
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await screenshot(page, 'desktop-progress');
  await page.setViewportSize({ width: 320, height: 700 });
  await page.getByRole('button', { name: 'Карточки' }).click();
  await page.getByRole('button', { name: 'Скрыть таблицу' }).click();
  await page.getByRole('button', { name: 'Цепочка предложений' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Ответ вслух / про себя' }).click();
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  await screenshot(page, '320-front');
  await reveal(page);
  await expect(page.locator('.answer-sentence')).toBeVisible();
  await screenshot(page, '320-revealed');
  await page.getByRole('button', { name: 'Цепочка предложений' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await expect(page.getByRole('textbox', { name: 'Ответ по-польски' })).toBeVisible();
  await screenshot(page, '320-typed');
  await page.getByRole('button', { name: 'Таблица под рукой' }).click();
  await expect(page.getByRole('table')).toBeVisible();
  await screenshot(page, '320-reference');
  await page.getByRole('button', { name: 'Таблицы и схема', exact: true }).click();
  for (const [section, label] of [['map', 'Карта системы'], ['cases', 'Падежи и окончания'], ['verbs', 'Времена и лица'], ['pronouns', 'Местоимения']] as const) {
    await page.getByRole('button', { name: label }).click();
    await screenshot(page, `320-matrix-${section}`);
  }
  await page.getByRole('button', { name: 'Прогресс' }).click();
  await screenshot(page, '320-progress');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await page.setViewportSize({ width: 700, height: 320 });
  await screenshot(page, 'landscape-progress');
  await page.getByRole('button', { name: 'Карточки' }).click();
  await page.getByRole('button', { name: 'Скрыть таблицу' }).click();
  await page.getByRole('button', { name: 'Цепочка предложений' }).click();
  await continueIntroductionIfPresent(page);
  await page.getByRole('button', { name: 'Ответ вслух / про себя' }).click();
  await page.setViewportSize({ width: 320, height: 700 });
  await page.evaluate(() => { document.documentElement.style.fontSize = '20px'; });
  await expect(page.getByRole('button', { name: /Показать ответ/ })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await screenshot(page, 'large-text-front');
  await page.evaluate(() => { document.documentElement.style.fontSize = ''; });
  await page.setViewportSize({ width: 700, height: 320 });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await expect(page.locator('.answer-sentence')).toHaveCount(0);
  expect(await page.locator('.reveal-button').evaluate(element => getComputedStyle(element).transitionDuration)).toBe('0s');
  await screenshot(page, 'reduced-motion-front');
});
