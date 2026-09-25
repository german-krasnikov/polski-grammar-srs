import { expect, test } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const previewKey = 'polski-grammar-srs-kmp-preview-v1';
type LoadFixture = {
  id: string;
  context?: { nowIso: string; timeZone: string };
  input: { stored?: unknown; progress?: unknown };
  expected: unknown;
};
const fixtureFile = JSON.parse(readFileSync(resolve('tests/fixtures/kotlin-parity/progress.json'), 'utf8')) as {
  cases: LoadFixture[];
};

for (const id of ['P-fresh', 'P-missing-skills', 'P-save-load', 'P-export-import']) {
  const fixture = fixtureFile.cases.find(entry => entry.id === id)!;
  test(`${id} exports the React-compatible progress document`, async ({ browser }) => {
    const at = fixture.context?.nowIso ?? '2026-02-03T12:00:00.000Z';
    const context = await browser.newContext({ locale: 'pl-PL', timezoneId: fixture.context?.timeZone ?? 'Europe/Warsaw' });
    try {
      const page = await context.newPage();
      if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
        await page.addInitScript(() => {
          const validate = WebAssembly.validate;
          WebAssembly.validate = function (bytes: BufferSource): boolean {
            if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
            return validate.call(WebAssembly, bytes);
          };
        });
      }
      const stored = fixture.input.stored ?? fixture.input.progress;
      if (stored !== undefined) await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw),
        [previewKey, JSON.stringify(stored)] as const);
      await page.clock.install({ time: new Date(at) });
      await page.clock.setFixedTime(new Date(at));
      await page.goto('/');
      await continueIntroductionIfPresent(page);
      await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
      await page.getByRole('button', { name: 'Прогресс' }).click();
      const downloadPromise = page.waitForEvent('download');
      await page.locator('#progress-export').click();
      const download = await downloadPromise;
      expect(JSON.parse(readFileSync((await download.path())!, 'utf8'))).toEqual(fixture.expected);
    } finally {
      await context.close();
    }
  });
}

test('P-queue picks the oldest due skill and still allows a focused future skill', async ({ browser }) => {
  const fixture = fixtureFile.cases.find(entry => entry.id === 'P-queue')!;
  const context = await browser.newContext({ locale: 'pl-PL', timezoneId: 'Europe/Warsaw' });
  try {
    const page = await context.newPage();
    if (process.env.KOTLIN_SPIKE_BRANCH === 'js') {
      await page.addInitScript(() => {
        const validate = WebAssembly.validate;
        WebAssembly.validate = function (bytes: BufferSource): boolean {
          if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
          return validate.call(WebAssembly, bytes);
        };
      });
    }
    await page.addInitScript(([key, raw]) => localStorage.setItem(key, raw),
      [previewKey, JSON.stringify(fixture.input.progress)] as const);
    await page.clock.install({ time: new Date('2026-02-03T12:00:00.000Z') });
    await page.clock.setFixedTime(new Date('2026-02-03T12:00:00.000Z'));
    await page.goto('/');
    await page.getByRole('button', { name: /По расписанию/ }).click();
    await expect(page.locator('.card-meta')).toContainText('Biernik · żeński');
    await page.getByRole('button', { name: 'Отдельный навык' }).click();
    await page.getByRole('region', { name: 'Выбор навыка' }).getByRole('button', { name: /Biernik · средний род/ }).click();
    await expect(page.locator('.card-meta')).toContainText('Biernik · средний род');
  } finally {
    await context.close();
  }
});
