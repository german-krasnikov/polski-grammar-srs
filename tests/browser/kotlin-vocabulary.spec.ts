import { expect, test } from '@playwright/test';
import { createHash } from 'node:crypto';
import { readFileSync, readdirSync } from 'node:fs';
import { resolve } from 'node:path';

test('serves the selected Kotlin JS or Wasm production artifact', async ({ page }) => {
  const distribution = resolve(process.env.KOTLIN_SPIKE_DIST!);
  const expectedScript = readFileSync(resolve(distribution, 'composeApp.js'));
  const expectedWasm = readdirSync(distribution).filter(name => name.endsWith('.wasm') && name !== 'skiko.wasm').sort();
  const servedFiles = new Set<string>();
  page.on('response', response => servedFiles.add(new URL(response.url()).pathname.split('/').pop()!));
  const scriptResponse = page.waitForResponse(response => response.url().endsWith('/composeApp.js'));
  await page.goto('/');
  await expect(page.getByRole('button', { name: 'Слова', exact: true })).toBeVisible();
  const servedScript = await (await scriptResponse).body();
  expect(createHash('sha256').update(servedScript).digest('hex'))
    .toBe(createHash('sha256').update(expectedScript).digest('hex'));
  expect([...servedFiles].filter(name => name.endsWith('.wasm')).sort()).toEqual(expectedWasm);
});

test('Kotlin word cards keep two schedules and custom content', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('checkbox').first().check();
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('жена');
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('Moja żona czyta książkę.');
  await page.getByRole('button', { name: 'Вспомнил' }).click();
  const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(saved.cards['pl-ru:vocabulary:ru-pl:noun.wife'].reps).toBe(1);
  expect(saved.cards['pl-ru:vocabulary:pl-ru:noun.wife']).toBeUndefined();
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

test('Kotlin frequency catalog reports exact coverage and disables unready ranks', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('100');
  await expect(page.getByRole('status')).toHaveText('Готово 7/100 · недоступно 93');
  await expect(page.locator('.catalog-list input:disabled')).toHaveCount(93);
  await expect(page.getByRole('link', { name: 'Leksjo / NKJP, CC BY 4.0' })).toHaveAttribute('href', /nkjp-frekwencja\.csv/);
  await page.getByRole('combobox', { name: 'Подборка слов' }).selectOption('1000');
  await expect(page.getByRole('status')).toHaveText('Готово 30/1000 · недоступно 970');
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

test('clean Kotlin profile restores a legacy shipped-homonym custom export', async ({ page }) => {
  const legacy = JSON.parse(readFileSync('tests/fixtures/react-vocabulary-sample.json', 'utf8'));
  const customId = 'user.00000000-0000-4000-8000-000000000091';
  legacy.custom.push({ id: customId, lemma: 'żona', translation: 'супруга (личное)', form: 'żona', example: 'To jest moja żona.', level: '—', frequencyRank: null, custom: true });
  legacy.selectedIds.push(customId);
  legacy.cards[`pl-ru:vocabulary:ru-pl:${customId}`] = structuredClone(legacy.cards['pl-ru:vocabulary:ru-pl:noun.wife']);
  await page.goto('/');
  expect(await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'))).toBeNull();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('textbox', { name: 'JSON словаря для импорта' }).fill(JSON.stringify(legacy));
  await page.getByRole('button', { name: 'Добавить данные из JSON' }).click();
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

test('Kotlin four-card JSON round-trips and rejects invalid pair before write', async ({ page }) => {
  const fixture = JSON.parse(readFileSync('tests/fixtures/vocabulary-four-cards.json', 'utf8'));
  await page.goto('/');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('textbox', { name: 'JSON словаря для импорта' }).fill(JSON.stringify(fixture));
  await page.getByRole('button', { name: 'Добавить данные из JSON' }).click();
  await expect(page.getByText('Мой словарь · 2')).toBeVisible();
  const exported = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Экспорт словаря JSON' }).click();
  expect(JSON.parse(readFileSync((await (await exported).path())!, 'utf8'))).toEqual(fixture);
  const before = await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'));
  await page.getByRole('textbox', { name: 'JSON словаря для импорта' }).fill(JSON.stringify({ ...fixture, pair: 'de-ru' }));
  await page.getByRole('button', { name: 'Добавить данные из JSON' }).click();
  await expect(page.getByRole('alert')).toBeVisible();
  expect(await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'))).toBe(before);
  await page.reload();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  expect(JSON.parse((await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1')))!)).toEqual(fixture);
});

test('Kotlin word card stays revealed when local storage rejects a review', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await page.getByRole('checkbox').first().check();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.evaluate(() => {
    const original = Storage.prototype.setItem;
    Storage.prototype.setItem = function(key: string, value: string) {
      if (key === 'polski-vocabulary-pl-ru-v1') throw new Error('Нет места');
      return original.call(this, key, value);
    };
  });
  await page.getByRole('button', { name: 'Вспомнил' }).click();
  await expect(page.getByRole('alert')).toContainText('Нет места');
  await expect(page.getByRole('button', { name: 'Вспомнил' })).toBeVisible();
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('Эталон');
});

test('Kotlin opens a React vocabulary export with its separate review histories', async ({ page }) => {
  const exported = JSON.parse(readFileSync('tests/fixtures/react-vocabulary-sample.json', 'utf8'));
  await page.goto('/');
  await page.evaluate(value => localStorage.setItem('polski-vocabulary-pl-ru-v1', JSON.stringify(value)), exported);
  await page.reload();
  await page.getByRole('button', { name: 'Слова', exact: true }).click();
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('На сейчас всё повторено');
  await page.getByRole('combobox', { name: 'Направление карточки' }).selectOption('pl-ru');
  await expect(page.getByRole('region', { name: 'Карточка слова' })).toContainText('żona');
  const stored = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(stored.cards['pl-ru:vocabulary:ru-pl:noun.wife'].reps).toBe(1);
  expect(stored.cards['pl-ru:vocabulary:pl-ru:noun.wife']).toBeUndefined();
});
