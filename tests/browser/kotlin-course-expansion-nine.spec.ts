import { expect, test } from '@playwright/test';
import { continueIntroductionIfPresent } from './kotlin-introduction';

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
    ['Po zakończeniu pracy wrócił do domu.', 'Kiedy skończył pracę, wrócił do domu.'],
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

test('numerals and valency give six different tasks before repetition', async ({ page }) => {
  test.setTimeout(120_000);
  for (const topic of [topics[3], topics[7]]) {
    await startTopic(page, topic.title, topic.level);
    const seen = new Set<string>();
    for (let index = 0; index < 6; index += 1) {
      const source = (await page.locator('.source-sentence').innerText()).trim();
      expect(seen.has(source)).toBe(false);
      seen.add(source);
      await page.getByRole('button', { name: 'Показать ответ' }).click();
      await expect(page.locator('.answer-sentence')).toHaveText(topic.pairs.find(([before]) => before === source)![1]);
      await page.getByRole('button', { name: /2 Вспомнил/ }).click();
      await page.getByRole('button', { name: /^(Перейти к заданию|Показать ответ)$/ }).first().waitFor({ timeout: 20_000 });
      await continueIntroductionIfPresent(page);
    }
    expect(seen.size).toBe(6);
    expect((await page.locator('.source-sentence').innerText()).trim()).not.toBe([...seen].at(-1));
  }
});
