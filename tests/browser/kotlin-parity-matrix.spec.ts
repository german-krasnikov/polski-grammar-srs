import { expect, test, type Page } from '@playwright/test';
import { resolve } from 'node:path';
import { readFileSync } from 'node:fs';
import { createServer, type ViteDevServer } from 'vite';

const course = JSON.parse(readFileSync(resolve('courses/pl-ru/course.json'), 'utf8')) as {
  nouns: Array<{ id: string; lemma: string }>;
  verbs: Array<{ id: string; aspect: string }>;
  reference: {
    verbTeaching: {
      subjects: Array<{ id: string; label: { full: string; compact: string } }>;
      genderControlLabel: { full: string };
      genderOptions: Array<{ id: string; label: { full: string } }>;
      tenseLabels: Record<string, { full: string }>;
      futureExplanation: { react: string; compact: string };
    };
    caseTeaching: { caseNote: { react: string; compact: string }; comparisonReadingHint: string };
    comparisonNounIds: string[];
    chainRows: Array<{ label: string; from: string; to: string; change: string }>;
    tenseRows: Array<{ label: string; from: string; to: string }>;
    aspectRows: Array<{ label: string; from: string; present: string | null; past: string; future: string }>;
    maleAccRows: Array<{ id: string; label: string; title: string; examples: Array<{ from: string; to: string; sentence: string }>; rule: string }>;
    russianSupport: {
      title: { full: string; compact: string };
      columns: string[];
      rows: Array<{ id: string; cue: string; react: { construction: string; check: string }; web: { construction: string; check: string };
        comparisons: Array<{ from: string; to: string }> }>;
    };
  };
};

let reactServer: ViteDevServer;
let reactBaseUrl: string;

/**
 * Mirrors EndingHighlight.kt's `singleWordInsertionOrDeletionParts` alignment check: true only
 * when the two phrases differ by exactly one word AND removing that one word from the longer
 * side lines up (letters only, case-insensitive) with the shorter side. Per the Emphasis
 * contract (ContrastHighlightPlan.md "Контракт выделения" §1), only this specific shape is a
 * pure insertion/deletion with nothing to mark on the shorter side; any other word-count
 * mismatch (e.g. "robić" → "robiłem / robiłam") falls back to a whole-phrase change that marks
 * both sides.
 */
function isSingleWordInsertionOrDeletion(from: string, to: string): boolean {
  const a = from.split(' ');
  const b = to.split(' ');
  if (Math.abs(a.length - b.length) !== 1) return false;
  const [longer, shorter] = a.length > b.length ? [a, b] : [b, a];
  const letterCore = (word: string) => word.replace(/[^\p{L}]+$/u, '').toLowerCase();
  return longer.some((_, i) => {
    const remainder = longer.filter((_, j) => j !== i);
    return remainder.length === shorter.length && remainder.every((word, k) => letterCore(word) === letterCore(shorter[k]));
  });
}

test.beforeAll(async () => {
  reactServer = await createServer({ configFile: resolve('vite.config.ts'), server: { host: '127.0.0.1', port: 0 } });
  await reactServer.listen();
  reactBaseUrl = reactServer.resolvedUrls!.local[0];
});

test.afterAll(async () => {
  await reactServer?.close();
});

async function tableCells(page: Page, selector: string): Promise<string[][]> {
  return page.locator(selector).locator('tbody tr').evaluateAll(rows => rows.map(row =>
    Array.from(row.querySelectorAll('th,td')).map(cell => cell.textContent!.replace(/\s+/g, ' ').trim()),
  ));
}

async function tableResultCells(page: Page, selector: string): Promise<string[][]> {
  return page.locator(selector).locator('tbody tr').evaluateAll(rows => rows.map(row =>
    Array.from(row.querySelectorAll('th,td')).map(cell =>
      (cell.querySelector('.form-contrast-after')?.textContent ?? cell.textContent ?? '').replace(/\s+/g, ' ').trim()),
  ));
}

test('React and Kotlin keep their four authored Russian-support rows and table semantics', async ({ page, browser }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const react = await reactContext.newPage();
    await page.goto('/');
    await react.goto(reactBaseUrl);
    for (const [current, variant] of [[react, 'react'], [page, 'web']] as const) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      const section = current.locator('section.matrix-section').filter({
        has: current.getByRole('heading', { name: course.reference.russianSupport.title.full }),
      });
      await expect(section.getByRole('table')).toBeVisible();
      await expect(section.locator('thead th')).toHaveText(course.reference.russianSupport.columns);
      const rows = section.locator('tbody tr');
      await expect(rows).toHaveCount(4);
      for (const [index, authored] of course.reference.russianSupport.rows.entries()) {
        const cells = rows.nth(index).locator('th,td');
        await expect(cells.nth(0)).toHaveText(authored.cue);
        expect(await cells.nth(1).evaluate(cell => cell.firstChild?.textContent?.trim())).toBe(authored[variant].construction);
        await expect(cells.nth(2)).toHaveText(authored[variant].check);
        const comparisons = cells.nth(1).locator('.form-contrast');
        await expect(comparisons).toHaveCount(authored.comparisons.length);
        for (const [pairIndex, pair] of authored.comparisons.entries()) {
          await expect(comparisons.nth(pairIndex)).toHaveAttribute('aria-label', `Было: ${pair.from}. Стало: ${pair.to}`);
          await expect(comparisons.nth(pairIndex)).toHaveAttribute('role', 'group');
          await expect(comparisons.nth(pairIndex).locator('.form-contrast-before')).toHaveAttribute('lang', 'pl');
          await expect(comparisons.nth(pairIndex).locator('.form-contrast-after')).toHaveAttribute('lang', 'pl');
          await expect(comparisons.nth(pairIndex).locator('.form-contrast-before')).toHaveText(pair.from);
          await expect(comparisons.nth(pairIndex).locator('.form-contrast-after')).toHaveText(pair.to);
          await expect(comparisons.nth(pairIndex).locator('.form-contrast-label')).toHaveText(['Было:', 'Стало:']);
          await expect(comparisons.nth(pairIndex).locator('.change-before').first()).toHaveCSS('text-decoration-style', 'dashed');
          await expect(comparisons.nth(pairIndex).locator('.change-after').first()).toHaveCSS('text-decoration-style', 'solid');
        }
      }
      await current.setViewportSize({ width: 320, height: 800 });
      await expect.poll(() => current.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth))
        .toBeLessThanOrEqual(1);
      for (const colorScheme of ['light', 'dark'] as const) {
        await current.emulateMedia({ colorScheme });
        await expect(rows.first().locator('.form-contrast-label').first()).toBeVisible();
        await expect(rows.first().locator('.change-before').first()).toHaveCSS('text-decoration-style', 'dashed');
      }
    }
  } finally {
    await reactContext.close();
  }
});

test('React and Kotlin show all pronoun constructions and owner phrases with their host copy', async ({ page, browser }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  const pronounRows = [
    ['ja', 'mnie', 'mi', 'mnie', 'ze mną', 'o mnie'],
    ['ty', 'ciebie', 'ci', 'ciebie', 'z tobą', 'o tobie'],
    ['on', 'go', 'mu', 'go', 'z nim', 'o nim'],
    ['ona', 'jej', 'jej', 'ją', 'z nią', 'o niej'],
    ['ono', 'go', 'mu', 'je', 'z nim', 'o nim'],
    ['my', 'nas', 'nam', 'nas', 'z nami', 'o nas'],
    ['wy', 'was', 'wam', 'was', 'z wami', 'o was'],
    ['oni', 'ich', 'im', 'ich', 'z nimi', 'o nich'],
    ['one', 'ich', 'im', 'je', 'z nimi', 'o nich'],
  ];
  const ownerRows = [
    ['mój — мой', 'moja piękna żona', 'moją piękną żonę', 'mojej pięknej żony', 'Согласуется с żona'],
    ['twój — твой', 'twoja piękna żona', 'twoją piękną żonę', 'twojej pięknej żony', 'Согласуется с żona'],
    ['jego — его', 'jego piękna żona', 'jego piękną żonę', 'jego pięknej żony', 'Владелец не склоняется'],
    ['jej — её', 'jej piękna żona', 'jej piękną żonę', 'jej pięknej żony', 'Владелец не склоняется'],
    ['nasz — наш', 'nasza piękna żona', 'naszą piękną żonę', 'naszej pięknej żony', 'Согласуется с żona'],
    ['wasz — ваш', 'wasza piękna żona', 'waszą piękną żonę', 'waszej pięknej żony', 'Согласуется с żona'],
    ['ich — их', 'ich piękna żona', 'ich piękną żonę', 'ich pięknej żony', 'Владелец не склоняется'],
  ];
  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const react = await reactContext.newPage();
    await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
    for (const [current, host] of [[react, 'react'], [page, 'web']] as const) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      await current.getByRole('button', { name: 'Местоимения' }).click();
      const personal = current.locator('section.matrix-section').filter({
        has: current.getByRole('heading', { name: 'Личные местоимения в конструкциях' }),
      });
      await expect(personal.locator('tbody tr')).toHaveCount(9);
      expect(await tableResultCells(current, 'section.matrix-section:has(h3:text-is("Личные местоимения в конструкциях")) table'))
        .toEqual(pronounRows);
      await expect(personal.locator('thead th')).toHaveText(host === 'react'
        ? ['Кто', 'Nie widzę…Dopełniacz', 'Daję prezent…Celownik', 'Widzę…Biernik', 'Idę z…Narzędnik', 'Mówię o…Miejscownik']
        : ['Кто', 'Nie widzę… · Dopełniacz', 'Daję prezent… · Celownik', 'Widzę… · Biernik', 'Idę z… · Narzędnik', 'Mówię o… · Miejscownik']);
      await expect(personal.locator(':scope > p').first()).toHaveText(host === 'react'
        ? 'Вместо отдельной формы запоминай её место в предложении. После предлогов у местоимений третьего лица появляется n-.'
        : 'После предлогов у местоимений третьего лица появляется n-.');
      await expect(personal.locator(':scope > p').last()).toHaveText(host === 'react'
        ? 'Здесь обычные безударные формы. Для ударения или противопоставления: mnie, tobie, jego, jemu. После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.'
        : 'Здесь обычные безударные формы. После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.');
      await expect(personal.locator('tbody tr').first().locator('.form-contrast').first())
        .toHaveAttribute('aria-label', 'Было: ja. Стало: mnie');
      const possessive = current.locator('section.matrix-section').filter({
        has: current.getByRole('heading', { name: 'Владелец меняется независимо от падежа' }),
      });
      await expect(possessive.locator('tbody tr')).toHaveCount(7);
      expect(await tableResultCells(current, 'section.matrix-section:has(h3:text-is("Владелец меняется независимо от падежа")) table'))
        .toEqual(ownerRows);
      await expect(possessive.locator('thead th')).toHaveText([
        'Кому принадлежит', 'Mianownik', 'Widzę… · Biernik', 'Nie widzę… · Dopełniacz', 'Правило',
      ]);
      await expect(possessive.locator('tbody tr').nth(2).locator('.form-contrast').nth(1))
        .toHaveAttribute('aria-label', 'Было: jego piękna żona. Стало: jego piękną żonę');
    }
  } finally {
    await reactContext.close();
  }
});

test('React and Kotlin render every authored chain transition with labelled old and new forms', async ({ page, browser }) => {
  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const react = await reactContext.newPage();
    await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
    for (const current of [page, react]) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      const section = current.locator('section.matrix-section').filter({ has: current.getByRole('heading', { name: 'Одна мысль, пять преобразований' }) });
      const rows = section.locator('tbody tr');
      await expect(rows).toHaveCount(5);
      for (const [index, authored] of course.reference.chainRows.entries()) {
        const row = rows.nth(index);
        await expect(row.locator('th')).toHaveText(authored.label);
        await expect(row.locator('.form-contrast')).toHaveAttribute('aria-label', `Было: ${authored.from}. Стало: ${authored.to}`);
        await expect(row.locator('.change-before').first()).toBeVisible();
        await expect(row.locator('.change-after').first()).toBeVisible();
        await expect(row.locator('td').last()).toHaveText(authored.change);
      }
    }
  } finally {
    await reactContext.close();
  }
});

test('React and Kotlin show authored male accusative decision examples with old and new forms', async ({ page, browser }) => {
  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const react = await reactContext.newPage();
    await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
    for (const current of [page, react]) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      const section = current.locator('section.matrix-section').filter({ has: current.getByRole('heading', { name: 'Мужской Biernik: дерево решений' }) });
      const rows = section.locator('.decision-grid article');
      await expect(rows).toHaveCount(course.reference.maleAccRows.length);
      for (const [index, authored] of course.reference.maleAccRows.entries()) {
        const row = rows.nth(index);
        await expect(row.locator('h4')).toHaveText(authored.title);
        await expect(row.locator('small')).toHaveText(authored.rule);
        const examples = row.locator('.form-contrast');
        await expect(examples).toHaveCount(authored.examples.length);
        for (const [exampleIndex, example] of authored.examples.entries()) {
          await expect(examples.nth(exampleIndex)).toHaveAttribute('aria-label', `Было: ${example.from}. Стало: ${example.to}`);
          await expect(row.locator('p').nth(exampleIndex)).toContainText(example.sentence);
        }
      }
      await current.setViewportSize({ width: 320, height: 800 });
      await expect.poll(() => current.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth))
        .toBeLessThanOrEqual(1);
    }
  } finally {
    await reactContext.close();
  }
});

test('React and Kotlin render every authored tense comparison with labelled contrast', async ({ page, browser }) => {
  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const react = await reactContext.newPage();
    await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
    for (const current of [page, react]) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      await current.getByRole('button', { name: 'Времена и лица' }).click();
      const section = current.locator('section.matrix-section').filter({ has: current.getByRole('heading', { name: 'Время меняется, предложение остаётся целым' }) });
      const rows = section.locator('tbody tr');
      await expect(rows).toHaveCount(course.reference.tenseRows.length);
      for (const [index, authored] of course.reference.tenseRows.entries()) {
        const row = rows.nth(index);
        await expect(row.locator('th')).toHaveText(authored.label);
        await expect(row.locator('.form-contrast')).toHaveAttribute('aria-label', `Было: ${authored.from}. Стало: ${authored.to}`);
        if (index > 0) {
          // Emphasis contract (ContrastHighlightPlan.md "Контракт выделения" §1): a pure
          // word insertion (e.g. "będzie"/"nie"/"Czy") has nothing to mark on the "before"
          // side — only a same-word-count change (index 1: idzie→szła, a whole-word
          // replacement) marks both sides. Kotlin's shared EndingHighlight.kt implements
          // this (EndingHighlightTest.kt); React's src/ui/endingHighlight.ts mirror predates
          // that refinement and still whole-phrase-highlights a word-count mismatch on both
          // sides — a tracked, out-of-scope gap (AI/decisions.md ADR-44), not a contradiction
          // this test should paper over by asserting it on React too.
          const isInsertion = current === page && isSingleWordInsertionOrDeletion(authored.from, authored.to);
          if (isInsertion) await expect(row.locator('.change-before')).toHaveCount(0);
          else await expect(row.locator('.change-before').first()).toBeVisible();
          await expect(row.locator('.change-after').first()).toBeVisible();
        }
      }
    }
  } finally {
    await reactContext.close();
  }
});

test('React and Kotlin render every authored aspect form with its verb baseline', async ({ page, browser }) => {
  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const react = await reactContext.newPage();
    await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
    for (const current of [page, react]) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      await current.getByRole('button', { name: 'Времена и лица' }).click();
      const section = current.locator('section.matrix-section').filter({ has: current.getByRole('heading', { name: 'Вид: процесс или результат' }) });
      const rows = section.locator('tbody tr');
      await expect(rows).toHaveCount(course.reference.aspectRows.length);
      for (const [index, authored] of course.reference.aspectRows.entries()) {
        const row = rows.nth(index);
        await expect(row.locator('th')).toHaveText(authored.label);
        for (const [column, form] of [authored.present, authored.past, authored.future].entries()) {
          const cell = row.locator('td').nth(column);
          if (form === null) await expect(cell).toHaveText('Нет настоящего времени');
          else {
            await expect(cell.locator('.form-contrast')).toHaveAttribute('aria-label', `Было: ${authored.from}. Стало: ${form}`);
            // Emphasis contract §1: a pure word insertion (e.g. "robić" → "będę robić")
            // marks only the "after" side; a same-word-count change (aligned ending, or a
            // multi-word literal fallback) marks both sides. See the tense-comparison test
            // above for why this is checked on Kotlin (`page`) only, not React (ADR-44).
            const isInsertion = current === page && isSingleWordInsertionOrDeletion(authored.from, form);
            if (isInsertion) await expect(cell.locator('.change-before')).toHaveCount(0);
            else await expect(cell.locator('.change-before').first()).toBeVisible();
            await expect(cell.locator('.change-after').first()).toBeVisible();
          }
        }
      }
    }
  } finally {
    await reactContext.close();
  }
});

test('P08 React and Kotlin expose the same case and declension forms', async ({ page, browser }) => {
  if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
  const reactContext = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const react = await reactContext.newPage();
    await Promise.all([page.goto('/'), react.goto(reactBaseUrl)]);
    for (const [current, note] of [[page, course.reference.caseTeaching.caseNote.compact], [react, course.reference.caseTeaching.caseNote.react]] as const) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      await current.getByRole('button', { name: 'Падежи и окончания' }).click();
      await expect(current.locator('.comparison-table tbody tr')).toHaveCount(7);
      await expect(current.getByText(note, { exact: true })).toBeVisible();
      await expect(current.getByText(course.reference.caseTeaching.comparisonReadingHint, { exact: true })).toBeVisible();
      for (const [index, id] of course.reference.comparisonNounIds.entries()) {
        const noun = course.nouns.find(item => item.id === id)!;
        await expect(current.locator('.comparison-table thead th').nth(index + 1)).toContainText(noun.lemma);
      }
      await current.setViewportSize({ width: 320, height: 700 });
      const scroll = current.locator('.comparison-table').locator('..');
      expect(await scroll.evaluate(element => element.scrollWidth - element.clientWidth)).toBeGreaterThan(0);
      await current.setViewportSize({ width: 1280, height: 720 });
    }

    for (const number of ['sg', 'pl']) {
      await page.locator('#matrix-number').selectOption(number);
      await react.locator('.table-controls select').nth(3).selectOption(number, { timeout: 10_000 });
      expect(await tableResultCells(page, '.comparison-table')).toEqual(await tableResultCells(react, '.comparison-table'));
      expect((await tableResultCells(page, '.matrix-section:first-of-type table')).map(row => row.slice(2))).toEqual(
        (await tableResultCells(react, '.matrix-section:first-of-type table')).map(row => row.slice(2)),
      );
    }

    for (const current of [page, react]) await current.getByRole('button', { name: 'Времена и лица' }).click();
    const teaching = course.reference.verbTeaching;
    for (const current of [page, react]) {
      const section = current.locator('.matrix-section').first();
      await expect(section.getByLabel(teaching.genderControlLabel.full)).toBeVisible();
      await expect(section.locator('thead th')).toHaveText([
        'Кто', teaching.tenseLabels.present.full, teaching.tenseLabels.past.full, teaching.tenseLabels.future.full,
      ]);
      await expect(section.locator('tbody tr th')).toHaveText(teaching.subjects.map(subject => subject.label.full));
      await expect(section.locator('tbody tr')).toHaveCount(9);
      await expect(section.getByText(current === react ? teaching.futureExplanation.react : teaching.futureExplanation.compact)).toBeVisible();
      await expect(section.locator('select').nth(1).locator('option')).toHaveText(teaching.genderOptions.map(option => option.label.full));
    }
    for (const verb of course.verbs.filter(item => item.aspect === 'imperfective').map(item => item.id)) {
      await page.locator('#matrix-verb').selectOption(verb);
      await react.locator('.table-controls select').first().selectOption(verb);
      let masculineRows: string[][] = [];
      for (const gender of ['m', 'f']) {
        await page.locator('#matrix-gender').selectOption(gender);
        await react.locator('.table-controls select').nth(1).selectOption(gender);
        const kotlinRows = await tableCells(page, '.matrix-section:first-of-type table');
        expect(kotlinRows).toEqual(await tableCells(react, '.matrix-section:first-of-type table'));
        expect(kotlinRows).toHaveLength(9);
        expect(kotlinRows.every(row => row.length === 4)).toBe(true);
        if (gender === 'm') masculineRows = kotlinRows;
        else {
          for (const index of [2, 3, 4, 7, 8]) expect(kotlinRows[index]).toEqual(masculineRows[index]);
        }
      }
    }
    expect(await tableCells(page, '.matrix-section:nth-of-type(3) table')).toEqual(
      await tableCells(react, '.matrix-section:nth-of-type(3) table'),
    );

    for (const current of [page, react]) await current.getByRole('button', { name: 'Местоимения' }).click();
    for (const section of [1, 2]) {
      expect(await tableCells(page, `.matrix-section:nth-of-type(${section}) table`)).toEqual(
        await tableCells(react, `.matrix-section:nth-of-type(${section}) table`),
      );
    }

    for (const current of [page, react]) await current.getByRole('button', { name: 'Падежи и окончания' }).click();
    await page.locator('#matrix-noun').selectOption('friendM');
    await page.locator('#matrix-adjective').selectOption('good');
    await react.locator('.table-controls select').first().selectOption('friendM');
    await react.locator('.table-controls select').nth(1).selectOption('good');
    for (const current of [page, react]) {
      await current.getByRole('button', { name: 'Тренировать отрицание с этим словом' }).click();
      await expect(current.locator('.source-sentence')).toContainText('kolegę');
    }
  } finally {
    await reactContext.close();
  }
});
