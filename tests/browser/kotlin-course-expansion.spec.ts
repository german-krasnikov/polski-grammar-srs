import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

const sources = new Set([
  'Ten dom jest stary.', 'To zadanie jest łatwe.', 'Ta książka jest dobra.',
  'Ten film jest interesujący.', 'To jest duży pokój.', 'Ten rower jest szybki.',
]);
const expectedBySource = new Map([
  ['Ten dom jest stary.', 'Ten dom jest starszy.'],
  ['To zadanie jest łatwe.', 'To zadanie jest łatwiejsze.'],
  ['Ta książka jest dobra.', 'Ta książka jest lepsza.'],
  ['Ten film jest interesujący.', 'Ten film jest bardziej interesujący.'],
  ['To jest duży pokój.', 'To jest największy pokój.'],
  ['Ten rower jest szybki.', 'Ten rower jest szybszy od tamtego roweru.'],
]);

test('comparison skill offers six whole-sentence tasks, four presentations and a reachable summary', async ({ page }) => {
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
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: 'Степени сравнения прилагательных · B1' }).click();
  await continueIntroductionIfPresent(page);
  await expect(page.getByRole('button', { name: 'Таблица под рукой' })).toHaveCount(0);
  const seen = new Set<string>();
  for (let index = 0; index < 6; index += 1) {
    const source = (await page.locator('.source-sentence').innerText()).trim();
    expect(sources.has(source)).toBe(true);
    expect(seen.has(source)).toBe(false);
    seen.add(source);
    for (const style of ['RuleFirst', 'SituationFirst', 'NativeContrast', 'MinimalTheory']) {
      await page.locator('#explanation-method').selectOption(style);
      await expect(page.locator('.card-front')).not.toContainText(expectedBySource.get(source)!);
    }
    await page.getByRole('button', { name: 'Показать ответ' }).click();
    await expect(page.locator('.answer-sentence')).toHaveText(expectedBySource.get(source)!);
    await expect(page.getByRole('button', { name: 'Лайфхак · источник: editorial' })).toBeVisible();
    await page.getByRole('button', { name: /2 Вспомнил/ }).click();
    await page.getByRole('button', { name: /^(Перейти к заданию|Показать ответ)$/ }).first().waitFor({ timeout: 20_000 });
    await continueIntroductionIfPresent(page);
  }
  expect(seen).toEqual(sources);
  const seventh = (await page.locator('.source-sentence').innerText()).trim();
  expect(seventh).not.toBe([...seen].at(-1));
  await page.goto('/#/matrix');
  await expect(page.getByRole('heading', { name: 'Степени сравнения' })).toBeVisible();
});
