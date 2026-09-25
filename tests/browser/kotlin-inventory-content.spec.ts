import { expect, test } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { createServer, type ViteDevServer } from 'vite';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const course = JSON.parse(readFileSync(resolve('courses/pl-ru/course.json'), 'utf8')) as {
  reference: {
    matrixIntroduction: string;
    contextHelp: { react: string; compact: string };
    maleAccIntro: string;
    aspectNoPresent: { compact: string };
  };
  vocabulary: {
    instructions: { react: string; web: string };
    unavailableLabel: string;
  };
};

let reactServer: ViteDevServer;
let reactBaseUrl: string;

test.beforeAll(async () => {
  reactServer = await createServer({ configFile: resolve('vite.config.ts'), server: { host: '127.0.0.1', port: 0 } });
  await reactServer.listen();
  reactBaseUrl = reactServer.resolvedUrls!.local[0];
});

test.afterAll(async () => {
  await reactServer?.close();
});

test('React and Kotlin render authored matrix, reference and vocabulary host variants', async ({ page, browser }) => {
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
    for (const current of [react, page]) {
      await current.getByRole('button', { name: 'Таблицы и схема' }).click();
      await expect(current.getByText(course.reference.matrixIntroduction, { exact: true })).toBeVisible();
      await expect(current.getByText(course.reference.maleAccIntro, { exact: true })).toBeVisible();
      await current.getByRole('button', { name: 'Времена и лица' }).click();
      await expect(current.getByText(course.reference.aspectNoPresent.compact, { exact: true }).first()).toBeVisible();
      await current.getByRole('button', { name: 'Карточки' }).click();
      await continueIntroductionIfPresent(current);
      const showTable = current.getByRole('button', { name: 'Таблица под рукой' });
      if (await showTable.count()) await showTable.click();
    }
    await expect(react.getByText(course.reference.contextHelp.react, { exact: true })).toBeVisible();
    await expect(page.getByText(course.reference.contextHelp.compact, { exact: true })).toBeVisible();

    for (const [current, text] of [[react, course.vocabulary.instructions.react], [page, course.vocabulary.instructions.web]] as const) {
      await current.getByRole('button', { name: 'Слова', exact: true }).click();
      const openCatalog = current.getByRole('button', { name: 'Открыть каталог' });
      if (await openCatalog.count()) await openCatalog.click();
      await expect(current.getByText(text, { exact: true })).toBeVisible();
      await current.getByRole('combobox', { name: 'Подборка слов' }).selectOption('100');
      await expect(current.getByText(course.vocabulary.unavailableLabel, { exact: true }).first()).toBeVisible();
    }
  } finally {
    await reactContext.close();
  }
});
