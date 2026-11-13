import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';
import { progressAsLegacyShape } from './polski-text';

const progressKey = 'polski-grammar-srs-kmp-preview-v1';
async function reviewCounts(page: import('@playwright/test').Page, skillId: string) {
  const raw = await page.evaluate(key => localStorage.getItem(key), progressKey);
  if (!raw) return { total: 0, skill: 0 };
  const progress = progressAsLegacyShape(raw);
  const stats = progress.stats as Record<string, { reviews: number }>;
  return { total: progress.totalReviews as number, skill: stats[skillId]?.reviews ?? 0 };
}

const topics = [
  { title: 'Причастия и деепричастия', level: 'B2', summary: 'Причастия и деепричастия', pairs: [
    ['Kobieta, która czyta książkę, siedzi przy oknie.', 'Kobieta czytająca książkę siedzi przy oknie.'],
    ['Mężczyzna, który pracuje w sklepie, zna mojego brata.', 'Mężczyzna pracujący w sklepie zna mojego brata.'],
    ['List, który został napisany wczoraj, leży na stole.', 'List napisany wczoraj leży na stole.'],
    ['Drzwi, które zostały zamknięte rano, prowadzą do piwnicy.', 'Drzwi zamknięte rano prowadzą do piwnicy.'],
    ['Maria czyta gazetę i pije kawę.', 'Czytając gazetę, Maria pije kawę.'],
    ['Piotr słucha muzyki i sprząta pokój.', 'Słuchając muzyki, Piotr sprząta pokój.'],
  ] },
  { title: 'Безличные формы и пассив', level: 'B2', summary: 'Безличные формы и пассив', pairs: [
    ['Ktoś zamknął sklep o szóstej.', 'Zamknięto sklep o szóstej.'],
    ['Ktoś otworzył okno rano.', 'Otwarto okno rano.'],
    ['Ktoś przeczytał list wczoraj.', 'Przeczytano list wczoraj.'],
    ['Robotnicy naprawili drogę.', 'Droga została naprawiona przez robotników.'],
    ['Policja zatrzymała kierowcę.', 'Kierowca został zatrzymany przez policję.'],
    ['Nauczyciel sprawdził testy.', 'Testy zostały sprawdzone przez nauczyciela.'],
  ] },
  { title: 'Образование существительных', level: 'B2', summary: 'Образование существительных', pairs: [
    ['To, że czytam książki, sprawia mi przyjemność.', 'Czytanie książek sprawia mi przyjemność.'],
    ['To, że piszę listy, zajmuje dużo czasu.', 'Pisanie listów zajmuje dużo czasu.'],
    ['To, że wspólnie gotujemy, jest dla nas ważne.', 'Wspólne gotowanie jest dla nas ważne.'],
    ['To, że ten pokój jest czysty, ma znaczenie.', 'Czystość tego pokoju ma znaczenie.'],
    ['To, że ona jest młoda, nie przeszkadza jej w pracy.', 'Jej młodość nie przeszkadza jej w pracy.'],
    ['To, że on jest odważny, pomaga mu w pracy.', 'Jego odwaga pomaga mu w pracy.'],
  ] },
  { title: 'Числительные и согласование', level: 'B2', summary: 'Числительные и согласование', pairs: [
    ['Na stole leży jedna książka.', 'Na stole leżą dwie książki.'],
    ['W pokoju stoją dwa krzesła.', 'W pokoju stoi pięć krzeseł.'],
    ['Dwaj studenci przyszli rano.', 'Pięciu studentów przyszło rano.'],
    ['Mam jednego kota.', 'Mam trzy koty.'],
    ['Widziałem dwie kobiety.', 'Widziałem pięć kobiet.'],
    ['Czekam na jednego kolegę.', 'Czekam na dwóch kolegów.'],
  ] },
  { title: 'Образование прилагательных', level: 'B2', summary: 'Образование прилагательных', pairs: [
    ['To jest stół z drewna.', 'To jest drewniany stół.'],
    ['To jest butelka ze szkła.', 'To jest szklana butelka.'],
    ['To jest sweter z wełny.', 'To jest wełniany sweter.'],
    ['To jest plan na tydzień.', 'To jest tygodniowy plan.'],
    ['To jest podróż zimą.', 'To jest zimowa podróż.'],
    ['To jest obiad z rodziną.', 'To jest rodzinny obiad.'],
  ] },
  { title: 'Союзы и смысловые связи', level: 'B1', summary: 'Союзы и смысловые связи', pairs: [
    ['Pada deszcz. Zostajemy w domu.', 'Zostajemy w domu, ponieważ pada deszcz.'],
    ['Pada deszcz. Idziemy na spacer.', 'Pada deszcz, ale idziemy na spacer.'],
    ['Będziesz miał czas. Przyjdź do mnie.', 'Jeśli będziesz miał czas, przyjdź do mnie.'],
    ['Uczę się polskiego. Chcę rozmawiać z sąsiadami.', 'Uczę się polskiego, żeby rozmawiać z sąsiadami.'],
    ['Jest zmęczona. Pracuje dalej.', 'Chociaż jest zmęczona, pracuje dalej.'],
    ['Skończę pracę. Zadzwonię do ciebie.', 'Kiedy skończę pracę, zadzwonię do ciebie.'],
  ] },
  { title: 'Смысловое преобразование фраз', level: 'B2', summary: 'Смысловое преобразование фраз', pairs: [
    ['Piotr jest wyższy od Jana.', 'Jan jest niższy od Piotra.'],
    ['Tylko Anna zna odpowiedź.', 'Nikt oprócz Anny nie zna odpowiedzi.'],
    ['Chociaż pada deszcz, idziemy na spacer.', 'Mimo deszczu idziemy na spacer.'],
    ['Nie mogę przyjść, bo jestem chory.', 'Z powodu choroby nie mogę przyjść.'],
    ['Piotr skończył pracę, a potem wrócił do domu.', 'Kiedy Piotr skończył pracę, wrócił do domu.'],
    ['Maria napisała list.', 'List został napisany przez Marię.'],
  ] },
  { title: 'Управление польских глаголов', level: 'B2', summary: 'Управление польских глаголов', pairs: [
    ['Я жду автобус.', 'Czekam na autobus.'],
    ['Я слушаю музыку.', 'Słucham muzyki.'],
    ['Я помогаю брату.', 'Pomagam bratu.'],
    ['Я пользуюсь телефоном.', 'Korzystam z telefonu.'],
    ['Я интересуюсь историей.', 'Interesuję się historią.'],
    ['Я ищу ключ.', 'Szukam klucza.'],
  ] },
  { title: 'Условное и повелительное наклонение', level: 'B2', summary: 'Условное и повелительное наклонение', pairs: [
    ['Piszę list.', 'Pisałbym list.'],
    ['Czytam tę książkę.', 'Czytałabym tę książkę.'],
    ['Otwierasz okno.', 'Otwórz okno.'],
    ['Wychodzisz teraz.', 'Nie wychodź teraz.'],
    ['Idziemy do domu.', 'Chodźmy do domu.'],
    ['Czy możesz mi pomóc?', 'Czy mógłbyś mi pomóc?'],
  ] },
] as const;

async function startTopic(page: import('@playwright/test').Page, title: string, level: string) {
  await page.goto('/');
  await page.getByRole('button', { name: 'Отдельный навык' }).click();
  await page.getByRole('button', { name: `${title} · ${level}` }).click();
  await continueIntroductionIfPresent(page);
}

test('nine added families have reachable training, authored styles and Matrix summaries', async ({ page }) => {
  test.setTimeout(120_000);
  for (const topic of topics) {
    await startTopic(page, topic.title, topic.level);
    await expect(page.getByRole('button', { name: 'Таблица под рукой' })).toHaveCount(0);
    const source = (await page.locator('.source-sentence').innerText()).trim();
    const answer = topic.pairs.find(([before]) => before === source)?.[1];
    expect(answer, `${topic.title}: ${source}`).toBeDefined();
    for (const style of ['RuleFirst', 'SituationFirst', 'NativeContrast', 'MinimalTheory']) {
      await page.locator('#explanation-method').selectOption(style);
      await expect(page.locator('.card-front')).not.toContainText(answer!);
    }
    await page.getByRole('button', { name: 'Показать ответ' }).click();
    await expect(page.locator('.answer-sentence')).toHaveText(answer!);
    await expect(page.getByRole('button', { name: 'Лайфхак · источник: editorial' })).toBeVisible();
    await page.goto('/#/matrix');
    await expect(page.getByRole('heading', { name: topic.summary })).toBeVisible();
  }
});

test('numerals and valency persist six reviews per stable skill before repetition', async ({ page }) => {
  test.setTimeout(120_000);
  for (const [topic, id] of [[topics[3], 'numeral.agreement'], [topics[7], 'case.valency']] as const) {
    await startTopic(page, topic.title, topic.level);
    await page.evaluate(() => {
      (window as any).__p18Clicks = [];
      document.addEventListener('click', event => {
        (window as any).__p18Clicks.push((event.target as HTMLElement).closest('button')?.textContent ?? 'outside button');
      }, true);
    });
    const before = await reviewCounts(page, id);
    expect(before.skill).toBe(0);
    const seen = new Set<string>();
    for (let index = 0; index < 6; index += 1) {
      await expect(page.locator('.card-meta')).toContainText(topic.title);
      const source = (await page.locator('.source-sentence').innerText()).trim();
      expect(seen.has(source)).toBe(false);
      seen.add(source);
      await page.getByRole('button', { name: 'Показать ответ' }).click();
      await expect(page.locator('.answer-sentence')).toHaveText(topic.pairs.find(([before]) => before === source)![1]);
      await page.getByRole('button', { name: /2 Вспомнил/ }).click();
      const clicks = await page.evaluate(() => (window as any).__p18Clicks as string[]);
      expect(clicks.filter(label => label.startsWith('2 Вспомнил')).length).toBe(index + 1);
      await expect.poll(async () => reviewCounts(page, id)).toEqual({ total: before.total + index + 1, skill: index + 1 });
      await expect(page.locator('.answer-sentence')).toHaveCount(0);
      await continueIntroductionIfPresent(page);
    }
    expect(seen.size).toBe(6);
    expect((await page.locator('.source-sentence').innerText()).trim()).not.toBe([...seen].at(-1));
    expect(await reviewCounts(page, 'numeral.agreement')).toEqual({ total: before.total + 6, skill: 6 });
  }
  expect(await reviewCounts(page, 'case.valency')).toEqual({ total: 12, skill: 6 });
  await page.reload();
  expect(await reviewCounts(page, 'numeral.agreement')).toEqual({ total: 12, skill: 6 });
  expect(await reviewCounts(page, 'case.valency')).toEqual({ total: 12, skill: 6 });
});

test('rating hit target recovers when motion changes during reveal and after returning to training', async ({ page }) => {
  await startTopic(page, topics[3].title, topics[3].level);
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.evaluate(() => document.documentElement.setAttribute('data-animations', 'off'));
  await expect.poll(() => page.locator('.ratings').evaluate(row => getComputedStyle(row).pointerEvents), { timeout: 600 }).toBe('auto');
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => reviewCounts(page, 'numeral.agreement')).toEqual({ total: 1, skill: 1 });

  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: 'Настройки' }).click();
  await page.locator('#settings-animations').uncheck();
  await page.goBack();
  await expect(page.locator('.answer-sentence')).toBeVisible();
  await expect.poll(() => page.locator('.ratings').evaluate(row => getComputedStyle(row).pointerEvents), { timeout: 600 }).toBe('auto');
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => reviewCounts(page, 'numeral.agreement')).toEqual({ total: 2, skill: 2 });

  await page.getByRole('button', { name: 'Настройки' }).click();
  await page.locator('#settings-animations').check();
  await page.goBack();
  await expect(page.locator('html')).toHaveAttribute('data-animations', 'on');
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await expect(page.locator('.ratings')).toHaveCSS('pointer-events', 'auto', { timeout: 600 });
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => reviewCounts(page, 'numeral.agreement')).toEqual({ total: 3, skill: 3 });

  await page.emulateMedia({ reducedMotion: 'no-preference' });
  await page.getByRole('button', { name: 'Показать ответ' }).click();
  await page.getByRole('button', { name: 'Таблицы и схема' }).click();
  await page.goBack();
  const activeCard = page.getByRole('region', { name: 'Учебная карточка' });
  await expect(activeCard).toHaveCount(1);
  await expect(activeCard.locator('.answer-sentence')).toBeVisible();
  await expect.poll(() => activeCard.locator('.ratings').evaluate(row => getComputedStyle(row).pointerEvents), { timeout: 600 }).toBe('auto');
  await activeCard.getByRole('button', { name: /2 Вспомнил/ }).click();
  await expect.poll(() => reviewCounts(page, 'numeral.agreement')).toEqual({ total: 4, skill: 4 });
});
