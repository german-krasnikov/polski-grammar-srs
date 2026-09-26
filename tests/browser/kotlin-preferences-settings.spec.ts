import { expect, test } from '@playwright/test';
import { mkdirSync } from 'node:fs';

const preferencesKey = 'polski-preferences-v1';
const progressKey = 'polski-grammar-srs-kmp-preview-v1';

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
});

async function prepareOnePreferencesReadFault(page: import('@playwright/test').Page, raw: string) {
  await page.addInitScript(([key, value]) => {
    localStorage.setItem(key, value);
    const getItem = Storage.prototype.getItem;
    let armed = false;
    let faults = 0;
    (window as unknown as { __preferencesReadFault: { arm: () => void; faults: () => number; read: () => string | null } }).__preferencesReadFault = {
      arm: () => { armed = true; },
      faults: () => faults,
      read: () => getItem.call(localStorage, key),
    };
    const faultingGetItem = function (this: Storage, name: string) {
      if (name === key && armed) {
        armed = false;
        faults += 1;
        throw new Error('Чтение временно недоступно');
      }
      return getItem.call(this, name);
    };
    Storage.prototype.getItem = faultingGetItem;
    Object.defineProperty(localStorage, 'getItem', { configurable: true, value: faultingGetItem });
  }, [preferencesKey, raw] as const);
  await page.goto('/#/settings');
  await expect(page.getByRole('heading', { name: 'Настройки обучения' })).toBeVisible();
  await page.evaluate(() => (window as unknown as { __preferencesReadFault: { arm: () => void } }).__preferencesReadFault.arm());
}

async function preferencesReadFaultOutcome(page: import('@playwright/test').Page) {
  return page.evaluate(() => {
    const fault = (window as unknown as { __preferencesReadFault: { faults: () => number; read: () => string | null } }).__preferencesReadFault;
    return { faults: fault.faults(), raw: fault.read() };
  });
}

test('legacy method migrates once and settings route preserves the question draft', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-explanation-method-v1', 'situations'));
  await page.goto('/#/training');
  await expect(page.getByRole('combobox', { name: 'Подача объяснений' })).toHaveValue('situations');
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ по-польски' }).fill('Moja próba');
  const before = await page.evaluate(key => localStorage.getItem(key), progressKey);
  await page.getByRole('button', { name: 'Настройки' }).click();
  await expect(page).toHaveURL(/#\/settings$/);
  await expect(page.getByRole('combobox', { name: 'Подача объяснений в настройках' })).toHaveValue('Situations');
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await page.goBack();
  await expect(page).toHaveURL(/#\/training$/);
  await expect(page.getByRole('textbox', { name: 'Ответ по-польски' })).toHaveValue('Moja próba');
  expect(await page.evaluate(key => localStorage.getItem(key), progressKey)).toBe(before);
  expect(await page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? '{}').explanationMethod, preferencesKey)).toBe('Situations');
  await page.reload();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
});

test('legacy tint is preserved in settings JSON without an optical control', async ({ page }) => {
  const legacy = '{"schemaVersion":1,"appearance":"Dark","answerMode":"Typed"}';
  await page.addInitScript(value => localStorage.setItem('polski-preferences-v1', value), legacy);
  await page.goto('/#/settings');
  await expect(page.locator('#settings-glass-tint')).toHaveCount(0);
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  expect(await page.evaluate(() => localStorage.getItem('polski-preferences-v1-backup'))).toBe(legacy);
  const migrated = await page.evaluate(key => JSON.parse(localStorage.getItem(key)!), preferencesKey);
  expect(migrated).toMatchObject({ schemaVersion: 2, appearance: 'Light', answerMode: 'Typed', glassTintPercent: 50 });
  const custom = { ...migrated, glassTintPercent: 17 };
  await page.addInitScript(([key, value]) => localStorage.setItem(key, JSON.stringify(value)), [preferencesKey, custom] as const);
  await page.reload();
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Dark');
  expect(await page.evaluate(key => JSON.parse(localStorage.getItem(key)!).glassTintPercent, preferencesKey)).toBe(17);
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await expect(page.locator('#settings-glass-tint')).toHaveCount(0);
});

test('direct Settings link and browser history keep four destinations available', async ({ page }) => {
  await page.goto('/#/settings');
  await expect(page.getByRole('heading', { name: 'Настройки обучения' })).toBeVisible();
  await expect(page.getByText('В браузерной версии напоминания после закрытия страницы недоступны', { exact: false })).toBeVisible();
  await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
  await expect(page).toHaveURL(/#\/training$/);
  for (const [label, route] of [['Слова', 'vocabulary'], ['Таблицы и схема', 'matrix'], ['Прогресс', 'progress']] as const) {
    await page.getByRole('button', { name: label, exact: true }).click();
    await expect(page).toHaveURL(new RegExp(`#/${route}$`));
    await page.getByRole('button', { name: 'Настройки' }).click();
    await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
    await expect(page).toHaveURL(new RegExp(`#/${route}$`));
  }
});

test('keyboard focus returns to the active section after leaving Settings', async ({ page }) => {
  await page.goto('/#/training');
  const settingsNav = page.locator('#nav-settings');
  await settingsNav.focus();
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(/#\/settings$/);
  await expect(page.locator('#nav-settings')).toBeFocused();
  const returnButton = page.locator('#settings-return');
  await returnButton.focus();
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(/#\/training$/);
  await expect(page.locator('#nav-training')).toBeFocused();
});

test('unreadable future settings stay exportable and cannot be overwritten by a method change', async ({ page }) => {
  const raw = '{"schemaVersion":2,"future":true}';
  await page.addInitScript(value => localStorage.setItem('polski-preferences-v1', value), raw);
  await page.goto('/#/settings');
  await expect(page.getByRole('status')).toContainText('восстановления');
  await page.getByRole('combobox', { name: 'Подача объяснений в настройках' }).selectOption('Situations');
  expect(await page.evaluate(key => localStorage.getItem(key), preferencesKey)).toBe(raw);
  await expect(page.getByRole('status')).toContainText('Сохраните исходный JSON');
});

test('system palette follows browser changes while manual choice stays fixed', async ({ page }) => {
  await page.emulateMedia({ colorScheme: 'light', reducedMotion: 'no-preference' });
  await page.goto('/#/settings');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await page.emulateMedia({ colorScheme: 'dark', reducedMotion: 'reduce' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await expect(page.locator('html')).toHaveAttribute('data-motion', 'reduced');
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  await page.emulateMedia({ colorScheme: 'dark', reducedMotion: 'no-preference' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await expect(page.locator('html')).toHaveAttribute('data-motion', 'normal');
  await page.getByRole('combobox', { name: 'Движение' }).selectOption('Reduced');
  await expect(page.locator('html')).toHaveAttribute('data-motion', 'reduced');
});

test('file import replaces recoverable preferences and preserves original backup', async ({ page }) => {
  const original = '{"schemaVersion":2,"future":true}';
  await page.addInitScript(value => localStorage.setItem('polski-preferences-v1', value), original);
  await page.goto('/#/settings');
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await expect(page.getByRole('status')).toContainText('Выберите файл');
  await page.getByLabel('Файл JSON настроек для импорта').setInputFiles({
    name: 'preferences.json', mimeType: 'application/json', buffer: Buffer.from('{"schemaVersion":1,"appearance":"Dark"}'),
  });
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await expect(page.getByRole('status')).toContainText('импортированы');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  expect(await page.evaluate(() => localStorage.getItem('polski-preferences-v1-backup'))).toBe(original);
});

test('disabled swipe preference leaves a revealed vocabulary card unrated', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('polski-preferences-v1', JSON.stringify({
    schemaVersion: 1, coursePair: 'pl-ru', swipeRatingEnabled: false,
  })));
  await page.goto('/#/vocabulary');
  await page.getByRole('checkbox').first().check();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Карточка слова' });
  await card.dispatchEvent('pointerdown', { clientX: 250, clientY: 100, pointerId: 1, pointerType: 'touch', isPrimary: true });
  await card.dispatchEvent('pointerup', { clientX: 100, clientY: 102, pointerId: 1, pointerType: 'touch', isPrimary: true });
  await expect(card.getByRole('button', { name: 'Вспомнил' })).toBeVisible();
  const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(Object.keys(saved.cards)).toHaveLength(0);
});

test('grammar swipe is disabled until reveal and remains governed by the shared preference', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', swipeRatingEnabled: true }));
    const nativeMatchMedia = window.matchMedia.bind(window);
    window.matchMedia = query => query === '(pointer: coarse)' ? ({ matches: true } as MediaQueryList) : nativeMatchMedia(query);
  });
  await page.goto('/#/training');
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(card.locator('.vocabulary-swipe-zone')).toHaveCount(0);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const zone = card.locator('.vocabulary-swipe-zone');
  await expect(zone).toHaveCount(1);
  await zone.dispatchEvent('pointerdown', { clientX: 100, clientY: 100, pointerId: 10, pointerType: 'touch', isPrimary: true });
  await zone.dispatchEvent('pointerup', { clientX: 250, clientY: 102, pointerId: 10, pointerType: 'touch', isPrimary: true });
  await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('polski-grammar-srs-kmp-preview-v1') ?? '{}').totalReviews)).toBe(1);
});

test('grammar swipe preference off leaves the revealed answer to rating buttons', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', swipeRatingEnabled: false }));
    const nativeMatchMedia = window.matchMedia.bind(window);
    window.matchMedia = query => query === '(pointer: coarse)' ? ({ matches: true } as MediaQueryList) : nativeMatchMedia(query);
  });
  await page.goto('/#/training');
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(card.locator('.vocabulary-swipe-zone')).toHaveCount(0);
  await card.dispatchEvent('pointerdown', { clientX: 100, clientY: 100, pointerId: 10, pointerType: 'touch', isPrimary: true });
  await card.dispatchEvent('pointerup', { clientX: 250, clientY: 102, pointerId: 10, pointerType: 'touch', isPrimary: true });
  await expect(card.getByRole('button', { name: /Вспомнил/ })).toBeVisible();
  const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-grammar-srs-kmp-preview-v1') ?? '{}'));
  expect(saved.totalReviews ?? 0).toBe(0);
});

test('grammar real touch completes after leaving the swipe zone', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'CDP touch injection is available only in Chromium');
  await page.addInitScript(() => {
    localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', swipeRatingEnabled: true }));
    const nativeMatchMedia = window.matchMedia.bind(window);
    window.matchMedia = query => query === '(pointer: coarse)' ? ({ matches: true } as MediaQueryList) : nativeMatchMedia(query);
  });
  await page.goto('/#/training');
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const zoneElement = page.getByRole('region', { name: 'Учебная карточка' }).locator('.vocabulary-swipe-zone');
  // v3/A: the answer now expands downward with a real transition (plus a short content stagger)
  // instead of appearing instantly — wait for it to fully settle before measuring coordinates,
  // or the bounding box below would be read mid-animation and the CDP touch would miss.
  await page.waitForTimeout(700);
  await zoneElement.scrollIntoViewIfNeeded();
  const zone = await zoneElement.boundingBox();
  expect(zone).not.toBeNull();
  const x1 = zone!.x + zone!.width - 25;
  const x2 = x1 + 110;
  expect(x2).toBeLessThan(page.viewportSize()!.width);
  const y = zone!.y + zone!.height / 2;
  await zoneElement.evaluate(element => {
    // The gesture listener (and setPointerCapture) now lives on the whole revealed back-face
    // (FC-02: the swipe zone was relocated off this narrow hint label onto `.card-back`), so
    // capture is checked on that ancestor, not on the label itself.
    const back = element.closest('.card-back')!;
    document.addEventListener('pointerdown', raw => {
      const pointer = raw as PointerEvent;
      (window as any).__grammarTouchStart = {
        pointerType: pointer.pointerType,
        pointerId: pointer.pointerId,
        isPrimary: pointer.isPrimary,
        zoneTarget: element.contains(pointer.target as Node),
        captured: back.hasPointerCapture(pointer.pointerId),
      };
    }, { once: true });
  });
  const session = await page.context().newCDPSession(page);
  try {
    await session.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [{ x: x1, y, id: 1 }] });
    expect(await page.evaluate(() => (window as any).__grammarTouchStart)).toMatchObject({
      pointerType: 'touch', isPrimary: true, zoneTarget: true, captured: true,
    });
    await session.send('Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [{ x: x2, y, id: 1 }] });
    await session.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });
    await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('polski-grammar-srs-kmp-preview-v1') ?? '{}').totalReviews)).toBe(1);
  } finally {
    await session.detach();
  }
});

test('swipe rating is limited to its dedicated touch affordance, never answer text', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', swipeRatingEnabled: true }));
    const nativeMatchMedia = window.matchMedia.bind(window);
    window.matchMedia = query => query === '(pointer: coarse)' ? ({ matches: true } as MediaQueryList) : nativeMatchMedia(query);
  });
  await page.goto('/#/vocabulary');
  await page.getByRole('checkbox').first().check();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Карточка слова' });
  const answerText = card.locator('.vocabulary-answer p').first();
  await answerText.dispatchEvent('pointerdown', { clientX: 250, clientY: 100, pointerId: 1, pointerType: 'touch', isPrimary: true });
  await answerText.dispatchEvent('pointerup', { clientX: 100, clientY: 102, pointerId: 1, pointerType: 'touch', isPrimary: true });
  await expect(card.getByRole('button', { name: 'Вспомнил' })).toBeVisible();
  const beforeSwipe = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(Object.keys(beforeSwipe.cards)).toHaveLength(0);
  const swipeZone = card.locator('.vocabulary-swipe-zone');
  await swipeZone.dispatchEvent('pointerdown', { clientX: 100, clientY: 100, pointerId: 2, pointerType: 'touch', isPrimary: true });
  await swipeZone.dispatchEvent('pointerup', { clientX: 250, clientY: 102, pointerId: 2, pointerType: 'touch', isPrimary: true });
  await expect(card.getByRole('heading', { name: 'На сейчас всё повторено' })).toBeVisible();
});

test('mouse dragging the swipe affordance does not rate on a coarse-pointer device', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', swipeRatingEnabled: true }));
    const nativeMatchMedia = window.matchMedia.bind(window);
    window.matchMedia = query => query === '(pointer: coarse)' ? ({ matches: true } as MediaQueryList) : nativeMatchMedia(query);
  });
  await page.goto('/#/vocabulary');
  await page.getByRole('checkbox').first().check();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Карточка слова' });
  const zone = card.locator('.vocabulary-swipe-zone');
  const bounds = await zone.boundingBox();
  expect(bounds).not.toBeNull();
  const y = bounds!.y + bounds!.height / 2;
  await page.mouse.move(bounds!.x + 20, y);
  await page.mouse.down();
  await page.mouse.move(bounds!.x + Math.min(bounds!.width - 20, 170), y, { steps: 8 });
  await page.mouse.up();
  await expect(card.getByRole('button', { name: 'Вспомнил' })).toBeVisible();
  const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!));
  expect(Object.keys(saved.cards)).toHaveLength(0);
});

test('a real touch swipe rates only from the dedicated affordance', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'CDP touch injection is available only in Chromium');
  await page.addInitScript(() => {
    localStorage.setItem('polski-preferences-v1', JSON.stringify({ schemaVersion: 1, coursePair: 'pl-ru', swipeRatingEnabled: true }));
    const nativeMatchMedia = window.matchMedia.bind(window);
    window.matchMedia = query => query === '(pointer: coarse)' ? ({ matches: true } as MediaQueryList) : nativeMatchMedia(query);
  });
  await page.goto('/#/vocabulary');
  await page.getByRole('checkbox').first().check();
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  const card = page.getByRole('region', { name: 'Карточка слова' });
  const session = await page.context().newCDPSession(page);
  const swipe = async (x1: number, x2: number, y: number) => {
    await session.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [{ x: x1, y, id: 1 }] });
    await session.send('Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [{ x: x2, y, id: 1 }] });
    await session.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });
  };
  try {
    const answer = await card.locator('.vocabulary-answer p').first().boundingBox();
    expect(answer).not.toBeNull();
    await swipe(answer!.x + Math.min(answer!.width - 20, 170), answer!.x + 20, answer!.y + answer!.height / 2);
    await expect(card.getByRole('button', { name: 'Вспомнил' })).toBeVisible();
    await card.locator('.vocabulary-swipe-zone').scrollIntoViewIfNeeded();
    const zone = await card.locator('.vocabulary-swipe-zone').boundingBox();
    expect(zone).not.toBeNull();
    await swipe(zone!.x + 20, zone!.x + Math.min(zone!.width - 20, 170), zone!.y + zone!.height / 2);
    await expect(card.getByRole('heading', { name: 'На сейчас всё повторено' })).toBeVisible();
  } finally {
    await session.detach();
  }
});

test('settings save failure before the first write preserves the existing value', async ({ page }) => {
  const original = JSON.stringify({ schemaVersion: 1, appearance: 'Dark' });
  await prepareOnePreferencesReadFault(page, original);
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  await expect(page.getByRole('status')).toContainText('Не сохранено');
  expect(await preferencesReadFaultOutcome(page)).toEqual({ faults: 1, raw: original });
});

test('settings import read failure preserves the existing value', async ({ page }) => {
  const original = JSON.stringify({ schemaVersion: 1, appearance: 'Dark' });
  await prepareOnePreferencesReadFault(page, original);
  await page.getByLabel('Файл JSON настроек для импорта').setInputFiles({
    name: 'preferences.json', mimeType: 'application/json', buffer: Buffer.from('{"schemaVersion":1,"appearance":"Light"}'),
  });
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await expect(page.getByRole('status')).toContainText('Чтение временно недоступно');
  expect(await preferencesReadFaultOutcome(page)).toEqual({ faults: 1, raw: original });
});

test('failed import leaves the active preferences and a recoverable backup', async ({ page }) => {
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  const original = await page.evaluate(key => localStorage.getItem(key), preferencesKey);
  expect(original).not.toBeNull();
  await page.evaluate(() => {
    const setItem = Storage.prototype.setItem;
    Storage.prototype.setItem = function (key, value) {
      if (key === 'polski-preferences-v1') throw new DOMException('Storage denied', 'QuotaExceededError');
      return setItem.call(this, key, value);
    };
  });
  await page.getByLabel('Файл JSON настроек для импорта').setInputFiles({
    name: 'preferences.json', mimeType: 'application/json', buffer: Buffer.from('{"schemaVersion":1,"appearance":"Dark"}'),
  });
  await page.getByRole('button', { name: 'Импортировать настройки' }).click();
  await expect(page.getByRole('status')).toContainText('Storage denied');
  expect(await page.evaluate(key => localStorage.getItem(key), preferencesKey)).toBe(original);
  expect(await page.evaluate(() => localStorage.getItem('polski-preferences-v1-backup'))).toBe(original);
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
});

test('a failed preference write keeps the in-memory choice exportable without changing progress', async ({ page }) => {
  await page.goto('/#/training');
  await expect.poll(() => page.evaluate(key => localStorage.getItem(key) !== null, progressKey)).toBe(true);
  const beforeProgress = await page.evaluate(key => localStorage.getItem(key), progressKey);
  await page.evaluate(() => {
    const original = Storage.prototype.setItem;
    Storage.prototype.setItem = function (key, value) {
      if (key === 'polski-preferences-v1') throw new DOMException('Storage denied', 'QuotaExceededError');
      return original.call(this, key, value);
    };
  });
  await page.getByRole('button', { name: 'Настройки' }).click();
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await expect(page.getByRole('status')).toContainText('Не сохранено');
  expect(await page.evaluate(key => localStorage.getItem(key), preferencesKey)).toBeNull();
  expect(await page.evaluate(key => localStorage.getItem(key), progressKey)).toBe(beforeProgress);
  const download = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Сохранить настройки JSON' }).click();
  const file = await download;
  expect(file.suggestedFilename()).toBe('polski-preferences-v2.json');
});

test('settings and theme changes retain the selected vocabulary card and typed draft', async ({ page }) => {
  await page.goto('/#/vocabulary');
  await page.getByRole('checkbox').first().check();
  await page.getByRole('combobox', { name: 'Направление карточки' }).selectOption('pl-ru');
  await page.getByRole('button', { name: 'Напечатать ответ' }).click();
  await page.getByRole('textbox', { name: 'Ответ на карточку слова' }).fill('черновик');
  const beforeVocabulary = await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'));
  const beforeProgress = await page.evaluate(key => localStorage.getItem(key), progressKey);
  await page.getByRole('button', { name: 'Настройки' }).click();
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
  await expect(page.getByRole('combobox', { name: 'Направление карточки' })).toHaveValue('pl-ru');
  await expect(page.getByRole('textbox', { name: 'Ответ на карточку слова' })).toHaveValue('черновик');
  expect(await page.evaluate(() => localStorage.getItem('polski-vocabulary-pl-ru-v1'))).toBe(beforeVocabulary);
  expect(await page.evaluate(key => localStorage.getItem(key), progressKey)).toBe(beforeProgress);
});

test('narrow layout keeps the next training action visible and the document within the viewport', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/training');
  const continueButton = page.getByRole('button', { name: 'Перейти к заданию' });
  await expect(continueButton).toBeVisible();
  const bounds = await continueButton.boundingBox();
  expect(bounds).not.toBeNull();
  expect(bounds!.y + bounds!.height).toBeLessThanOrEqual(844);
  await continueButton.click();
  const revealButton = page.getByRole('button', { name: 'Показать ответ' });
  await expect(revealButton).toBeVisible();
  const revealBounds = await revealButton.boundingBox();
  expect(revealBounds).not.toBeNull();
  expect(revealBounds!.y + revealBounds!.height, '390 px question reveal action should fit the first viewport').toBeLessThanOrEqual(844);
  await page.setViewportSize({ width: 320, height: 700 });
  for (const route of ['training', 'vocabulary', 'matrix', 'progress', 'settings']) {
    await page.goto(`/#/${route}`);
    await expect(page.getByRole('button', { name: 'Настройки', exact: true })).toBeVisible();
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
    expect(overflow, `${route} document overflow`).toBeLessThanOrEqual(1);
  }
});

test('light theme navigation button keeps readable text when hovered', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto('/#/settings');
  await page.getByRole('combobox', { name: 'Тема' }).selectOption('Light');
  const button = page.getByRole('button', { name: 'Прогресс', exact: true });
  await button.hover();
  const colors = await button.evaluate(element => {
    const style = getComputedStyle(element);
    return { foreground: style.color, background: style.backgroundColor };
  });
  const channels = (color: string) => (color.match(/[\d.]+/g) ?? []).slice(0, 3).map(Number);
  const luminance = (color: string) => {
    const [red, green, blue] = channels(color).map(channel => {
      const linear = channel / 255;
      return linear <= 0.04045 ? linear / 12.92 : ((linear + 0.055) / 1.055) ** 2.4;
    });
    return red * 0.2126 + green * 0.7152 + blue * 0.0722;
  };
  const foreground = luminance(colors.foreground);
  const background = luminance(colors.background);
  const ratio = (Math.max(foreground, background) + 0.05) / (Math.min(foreground, background) + 0.05);
  expect(ratio, `hover colors ${colors.foreground} on ${colors.background}`).toBeGreaterThanOrEqual(4.5);
});

test('capture narrow training and settings in both themes for visual review', async ({ page }, testInfo) => {
  const branch = `${process.env.KOTLIN_SPIKE_BRANCH ?? 'unknown'}-${testInfo.project.name}`;
  // Screenshots are run artifacts, not tracked review material: keep them under the
  // gitignored Playwright output directory instead of Plans/Kotlin/artifacts.
  const directory = testInfo.outputPath('ux2-test');
  mkdirSync(directory, { recursive: true });
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/training');
  await page.getByRole('button', { name: 'Перейти к заданию' }).click();
  await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
  for (const theme of ['Dark', 'Light']) {
    await page.getByRole('button', { name: 'Настройки', exact: true }).click();
    await page.getByRole('combobox', { name: 'Тема' }).selectOption(theme);
    await expect(page.locator('html')).toHaveAttribute('data-theme', theme.toLowerCase());
    await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
    await expect(page.getByRole('button', { name: 'Показать ответ' })).toBeVisible();
    await page.screenshot({ path: `${directory}/${branch}-training-390-${theme.toLowerCase()}.png`, fullPage: true });
    await page.setViewportSize({ width: 320, height: 700 });
    await page.getByRole('button', { name: 'Настройки', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Настройки обучения' })).toBeVisible();
    await page.screenshot({ path: `${directory}/${branch}-settings-320-${theme.toLowerCase()}.png`, fullPage: true });
    await page.getByRole('button', { name: 'Вернуться к карточке' }).click();
    await page.setViewportSize({ width: 390, height: 844 });
  }
});
