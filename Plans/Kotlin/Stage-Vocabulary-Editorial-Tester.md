# Step 5 · V2: независимая редакторская проверка исходных 32 карточек

Дата проверки и повторной проверки восьми исправлений: 2026-09-24. Контракт: [VocabularyCatalogBlueprint.md](VocabularyCatalogBlueprint.md), V2 и отдельно V3. Текущий срез: `courses/pl-ru/course.json`, SHA-256 всего файла `4daefc9fd70112e7e38013a62510d2882601c10d5630424dadb6c09283579b6b`, 32 `vocabulary.items`. SHA-256 `JSON.stringify(vocabulary.items)` равен `03a7757d1e34eb5901c108eb692721cd6f6f483423ab900a2350623c966888ad`; он отдельно закрепляет именно проверенные карточки. Прежний whole-file SHA `ccf770d4b202e12a9909e0d86ca3d27a606c72c1266db5bd00a8fa70d6124c7b` относится к срезу до правки UI-copy вне `vocabulary.items` и не является hash текущего файла. Проверены все шесть полей каждой записи (`id`, `lemma`, `translation`, `form`, `example`, `level`) и оба направления карточки. В React направление «Русский → польский» показывает **только перевод** до открытия ответа; пример и форма появляются после него (`src/ui/VocabularyView.tsx`). Поэтому русская подсказка должна помогать выбрать изучаемую лемму без доступа к примеру. Это редакторская проверка, не браузерный/FSRS runtime gate.

Статус в таблице относится к **языковой связке и пригодности двусторонней подсказки**, а не к локальной метке A1/A2. `PASS` означает, что просмотренные словарные статьи подтверждают выбранный смысл и формы, а проектный пример не обнаруживает конфликта; это не «сертифицировано CEFR». `NEEDS-CORRECTION` означает конкретный текстовый дефект или неоднозначность prompt. `UNVERIFIED` оставляется там, где источник/авторство нельзя подтвердить уверенно. Ссылки ведут к статьям [WSJP PAN](https://wsjp.pl/), которые дают значения, сочетаемость и парадигмы; русские формулировки — собственная редакторская оценка соответствия, а не цитаты словаря. Статьи просмотрены 2026-09-24. Примеры из WSJP/NKJP **не перенесены** в курс.

| № | ID · lemma | Текущие translation · form · example · level | Семантика / обе стороны | Основание и первичный источник |
|---:|---|---|---|---|
| 1 | `noun.wife` · żona | жена · `żona · род. żony` · `Moja żona czyta książkę.` · A1 | PASS | Супруга; форма в примере — именительный `żona`. [WSJP: żona](https://wsjp.pl/haslo/podglad/4562/zona). |
| 2 | `noun.woman` · kobieta | женщина · `kobieta · род. kobiety` · `Ta kobieta mówi po polsku.` · A1 | PASS | Значение взрослой женщины, согласование `ta kobieta` верно. [WSJP: kobieta](https://wsjp.pl/haslo/podglad/3950/kobieta/4891319/plec). |
| 3 | `noun.book` · książka | книга · `książka · вин. książkę · род. książki` · `Czytam nową książkę.` · A1 | PASS | Винительный `książkę` из примера теперь явно назван; `książki` — родительный. Русский prompt однозначно указывает предмет. [WSJP: książka](https://wsjp.pl/haslo/podglad/4416/ksiazka/3843714/przedmiot). |
| 4 | `noun.work` · praca | работа · `praca · род. pracy` · `Mam dziś dużo pracy.` · A2 | PASS | После `dużo` стоит родительный `pracy`, прямо показанный в форме; значение «работа/дела» соответствует переводу. [WSJP: praca](https://wsjp.pl/haslo/podglad/16015/praca). |
| 5 | `noun.husband` · mąż | муж · `mąż · род. męża` · `Jej mąż pracuje w domu.` · A2 | PASS | Супруг и именительный в примере; родительный подтверждён парадигмой. [WSJP: mąż](https://wsjp.pl/haslo/podglad/4594/maz/4919307/w-malzenstwie). |
| 6 | `noun.friendM` · kolega | коллега · `kolega · род. kolegi` · `Mój kolega z pracy ma psa.` · A2 | PASS | `z pracy` в проектном примере выбирает рабочее значение; именительный `kolega` и родительный `kolegi` подтверждены. Русский prompt допускает польский `kolega`. [WSJP: kolega](https://wsjp.pl/haslo/podglad/7874/kolega/5071907/z-pracy). |
| 7 | `noun.son` · syn | сын · `syn · род. syna` · `Mój syn lubi koty.` · A1 | PASS | Родственная связь и формы согласованы. [WSJP: syn](https://wsjp.pl/haslo/podglad/5034/syn/5113455/w-rodzinie). |
| 8 | `noun.dog` · pies | собака · `pies · род. psa` · `Ten pies jest mały.` · A1 | PASS | Домашнее животное; польский `pies` может обозначать самца, а русское «собака» родовое, что в этом базовом примере не мешает. [WSJP: pies](https://wsjp.pl/haslo/podglad/16383/pies/4909022/zwierze). |
| 9 | `noun.cat` · kot | кот · `kot · род. kota` · `Mój kot śpi na kanapie.` · A1 | PASS | Значение животного, формы и естественная фраза. Русский «кот» здесь точнее родового «кошка». [WSJP: kot](https://wsjp.pl/haslo/podglad/29368/kot). |
| 10 | `noun.car` · samochód | машина · `samochód · род. samochodu` · `Ten samochód jest nowy.` · A1 | PASS | В контексте русского «машина» обычно автомобиль; более точное «автомобиль» снизило бы редкую обратную неоднозначность с `maszyna`, но обязательной правки не требует. [WSJP: samochód](https://wsjp.pl/haslo/podglad/3991/samochod). |
| 11 | `noun.house` · dom | дом · `dom · род. domu` · `Mój dom jest blisko.` · A1 | PASS | Дом/жилище и формы верны. Фраза грамматична, но `blisko` без точки отсчёта слабо контекстуализовано; улучшение возможно отдельно. [WSJP: dom](https://wsjp.pl/haslo/podglad/25833/dom/4852465/budynek). |
| 12 | `noun.phone` · telefon | телефон · `telefon · род. telefonu` · `Gdzie jest mój telefon?` · A1 | PASS | Устройство, верная форма и естественный вопрос. [WSJP: telefon](https://wsjp.pl/haslo/podglad/7128/telefon/4326358/urzadzenie). |
| 13 | `noun.child` · dziecko | ребёнок · `dziecko · род. dziecka` · `To dziecko lubi czytać.` · A1 | PASS | Смысл ребёнка, именительный в примере; форма родительного подтверждена. [WSJP: dziecko](https://wsjp.pl/haslo/podglad/5040/dziecko/2574901/niedorosly-czlowiek). |
| 14 | `noun.window` · okno | окно · `okno · род. okna` · `Otwórz okno, proszę.` · A1 | PASS | Окно с рамой/стеклом; винительный неодушевлённого среднего рода совпадает с `okno`, просьба естественна. [WSJP: okno](https://wsjp.pl/haslo/podglad/23307/okno/4930418/z-szyba). |
| 15 | `adjective.beautiful` · piękny | прекрасный, очень красивый · `piękny · ж. piękna` · `To piękny dom.` · A2 | PASS | Сочетание `piękny dom` естественно; русская подсказка теперь сильнее по оценке и отличается от № 18. Синонимия остаётся, поэтому самооценка должна допускать разумный вариант. [WSJP: piękny](https://wsjp.pl/haslo/podglad/6013/piekny/4771595/mezczyzna), [ładny](https://wsjp.pl/haslo/podglad/36099/ladny/4948745/dziewczyna). |
| 16 | `adjective.new` · nowy | новый · `nowy · ж. nowa · ж. вин. nową` · `Mam nową książkę.` · A1 | PASS | Женский винительный `nową` в примере теперь назван и согласован с `książkę`; значение «новый» верно. [WSJP: nowy](https://wsjp.pl/haslo/podglad/17360/nowy/5057008/samochod). |
| 17 | `adjective.small` · mały | маленький · `mały · ж. mała` · `To mały pies.` · A1 | PASS | Размер; `mały` в примере мужской именительный. [WSJP: mały](https://wsjp.pl/haslo/podglad/4913/maly). |
| 18 | `adjective.nice` · ładny | симпатичный, приятный на вид · `ładny · ж. ładna` · `To ładny dom.` · A1 | PASS | Выбранное значение отвечает словарному «приятно смотреть»; русский prompt отличается от № 15. `ładny dom` засвидетельствовано как сочетание. Синонимия с `piękny` возможна и при самооценке допустима. [WSJP: ładny](https://wsjp.pl/haslo/podglad/36099/ladny/4948745/dziewczyna), [piękny](https://wsjp.pl/haslo/podglad/6013/piekny/4771595/mezczyzna). |
| 19 | `adjective.good` · dobry | хороший · `dobry · ж. dobra` · `To dobry pomysł.` · A1 | PASS | Оценка идеи естественна; `dobry` согласован с `pomysł`. [WSJP: dobry](https://wsjp.pl/haslo/podglad/775/dobry). |
| 20 | `adjective.old` · stary | старый · `stary · ж. stara` · `To stary samochód.` · A1 | PASS | Возраст/давность предмета; корректная мужская форма. [WSJP: stary](https://wsjp.pl/haslo/podglad/5623/stary). |
| 21 | `adjective.expensive` · drogi | дорогой · `drogi · ж. droga` · `Ten telefon jest drogi.` · A2 | PASS | Пример фиксирует смысл высокой цены, хотя вне примера и польское, и русское слово имеют и смысл «дорогой сердцу». Если нужен полностью однозначный prompt, можно добавить «по цене». [WSJP: drogi — towar](https://wsjp.pl/haslo/podglad/20710/drogi/4552724/towar). |
| 22 | `verb.have` · mieć | иметь · `mieć · я mam` · `Mam książkę.` · A1 | PASS | Владение, 1-е лицо настоящего времени и винительный объекта. [WSJP: mieć](https://wsjp.pl/haslo/podglad/42202/miec/4504388/samochod). |
| 23 | `verb.see` · widzieć | видеть · `widzieć · я widzę` · `Widzę dom.` · A1 | PASS | Зрительное восприятие, 1-е лицо настоящего времени. [WSJP: widzieć](https://wsjp.pl/haslo/podglad/20375/widziec/4938155/kolege). |
| 24 | `verb.like` · lubić | нравиться (о занятии); любить делать что-либо · `lubić · я lubię` · `Lubię czytać.` · A1 | PASS | Подсказка теперь называет именно предпочтение занятия; WSJP для `lubić` + инфинитив прямо даёт `lubić czytać`, а `lubię` — 1-е лицо настоящего. Отдельное «нравиться» имеет и другие польские выражения, но вторая часть подсказки задаёт конструкцию. [WSJP: lubić — podróżować](https://wsjp.pl/haslo/podglad/37948/lubic/4064835/podrozowac), [kochać — czytać](https://wsjp.pl/haslo/podglad/37947/kochac/4060073/czytac). |
| 25 | `verb.buy` · kupować | покупать · `kupować · я kupuję` · `Kupuję książkę.` · A1 | PASS | Несовершенный вид, настоящее `kupuję`; отличается от совершенного `kupić` № 30. [WSJP: kupować](https://wsjp.pl/haslo/podglad/7275/kupowac/2372345/mieszkanie). |
| 26 | `verb.read` · czytać | читать · `czytać · я czytam` · `Czytam książkę.` · A1 | PASS | Чтение книги и форма настоящего времени. [WSJP: czytać](https://wsjp.pl/haslo/podglad/278/czytac/4953760/ksiazke). |
| 27 | `verb.talk` · mówić | говорить · `mówić · я mówię` · `Mówię po polsku.` · A1 | PASS | Смысл «говорить на языке» задан `po polsku`; форма `mówię` корректна. [WSJP: po polsku — mówić](https://wsjp.pl/haslo/podglad/3030/po-polsku/2451652/mowic). |
| 28 | `verb.go` · iść | идти · `iść · я idę` · `Idę do domu.` · A1 | PASS | Направленное движение пешком; форма и конструкция `do domu` верны. [WSJP: iść — do domu](https://wsjp.pl/haslo/podglad/19012/isc/5066139/do-domu). |
| 29 | `verb.be` · być | быть · `być · я jestem` · `Jestem w domu.` · A1 | PASS | Нахождение в месте и 1-е лицо `jestem` соответствуют общему `być`; русский настоящий глагол-связка часто опускается, но обратный стимул «быть» всё равно понятен. [WSJP: być](https://wsjp.pl/haslo/podglad/31129/byc). |
| 30 | `verb.buyDone` · kupić | купить · `kupić · буд. я kupię · прош. я (м.) kupiłem` · `Kupiłem książkę.` · A2 | PASS | Совершенный вид; будущая и мужская прошедшая форма явно различены, пример демонстрирует последнюю. [WSJP: kupić](https://wsjp.pl/haslo/podglad/15714/kupic/4924090/mieszkanie). |
| 31 | `verb.do` · robić | делать · `robić · я robię` · `Robię obiad.` · A1 | PASS | Несовершенный вид и естественное приготовление еды. [WSJP: robić — kotlety](https://wsjp.pl/haslo/podglad/2963/robic/4920705/kotlety). |
| 32 | `verb.doDone` · zrobić | сделать · `zrobić · буд. я zrobię · прош. я (м.) zrobiłem` · `Zrobiłem obiad.` · A2 | PASS | Совершенный вид и оба времени названы; пример иллюстрирует мужское прошедшее. Сочетание `zrobić obiad` подтверждено словарём. [WSJP: zrobić — kotlety](https://wsjp.pl/haslo/podglad/15083/zrobic/4988368/kotlety). |

Итог языковой связки на исправленном срезе: **32 PASS, 0 NEEDS-CORRECTION, 0 UNVERIFIED**. Восемь адресных повторных проверок подтвердили значения, заявленные формы, согласование с примерами и пригодность подсказок для обоих направлений. Все 32 ID, lemma, rank, translation, form, example и level в текущем pack совпадают с записями `vocabulary-editorial.json` (сравнение полей и количества 32 ↔ 32). Текущий журнал имеет SHA-256 `0fc37f1a2288e2cd12e4121dba94b394df1e461517558bd6d0cd1037c223a510`: все 32 карточки `approved`, `reviewer: "Codex senior-tester (GPT-6 Sol)"`, `reviewedAt: "2026-09-24"`; 970 кандидатов остаются discovery (`needs-review`: 93, `deferred`: 877). Редакторская часть V2 оформлена в артефакте; runtime-gates проверяются отдельно.

Это независимая агентная проверка смысловой связки и заявленного авторства `project-authored`; она не является человеческой экспертной сертификацией CEFR и не доказывает происхождение предложений вне репозитория. Автоматический валидатор сам по себе не доказывает правильность польского текста.

## Повторно проверенные content diffs

Developer внёс восемь предложенных замен; ниже зафиксировано проверенное отличие от предыдущего среза. ID, rank, FSRS keys и локальные метки уровня в этих правках не менялись.

```diff
-noun.book.form: "książka · род. książki"
+noun.book.form: "książka · вин. książkę · род. książki"
-noun.friendM.example: "Mój kolega ma psa."
+noun.friendM.example: "Mój kolega z pracy ma psa."
-adjective.beautiful.translation: "красивый"
+adjective.beautiful.translation: "прекрасный, очень красивый"
-adjective.new.form: "nowy · ж. nowa"
+adjective.new.form: "nowy · ж. nowa · ж. вин. nową"
-adjective.nice.translation: "красивый"
+adjective.nice.translation: "симпатичный, приятный на вид"
-verb.like.translation: "любить"
+verb.like.translation: "нравиться (о занятии); любить делать что-либо"
-verb.buyDone.form: "kupić · я kupię"
+verb.buyDone.form: "kupić · буд. я kupię · прош. я (м.) kupiłem"
-verb.doDone.form: "zrobić · я zrobię"
+verb.doDone.form: "zrobić · буд. я zrobię · прош. я (м.) zrobiłem"
```

`Kolega z pracy` теперь выбирает то же значение «коллега по работе», что и русский перевод. Для пары `piękny`/`ładny` русские подсказки различают силу оценки, но синонимия остаётся: при самооценке разумный синоним следует считать понятным ответом. Строгое сравнение по единственной лемме для этих русских prompts дало бы ложно отрицательные оценки.

## Уровни и лицензия

Все 25 A1 и 7 A2 в шести полях просмотрены, но **каждая метка уровня остаётся UNVERIFIED как CEFR claim**: [план](VocabularyCatalogBlueprint.md) и [атрибуция курса](../../courses/pl-ru/ATTRIBUTION.md) называют их локальной редакторской группировкой. WSJP подтверждает смысл/морфологию, но не сертифицирует CEFR. Нет оснований переводить A2 в A1 или заполнять B1 по частотному рангу. Следующее редакторское решение об уровне надо фиксировать отдельно от языкового PASS, а в интерфейсе/документации не называть эти метки официальными.

[ATTRIBUTION.md](../../courses/pl-ru/ATTRIBUTION.md) прямо заявляет, что 32 польских предложения созданы для курса; изменённое предложение `Mój kolega z pracy ma psa.` внесено разработчиком в этом же проекте. [LICENSE](../../LICENSE) содержит MIT для проекта. По заявленной provenance `project-authored` для этих примеров **нет внешнего текста, лицензию которого нужно переносить**; словарные/corpus citations выше служат проверке, а не источником скопированных предложений. Независимого доказательства первоначального авторства каждого предложения из имеющегося репозитория нет; если позднее выявится заимствование, статус соответствующей записи нужно пересмотреть и установить разрешение/автора/URL. Внешние статьи WSJP и корпусные цитаты не лицензируют автоматическое копирование в pack.

Проверки: прочитаны исправленные восемь записей в `course.json` и журнале, соответствующие первичные статьи WSJP PAN, React prompt/reveal и атрибуция; SHA-256 зафиксирован выше. Сравнение всех текущих 32 pack/journal записей по ID, lemma, rank, translation, form, example и level: **PASS**. Приложение, браузеры, Kotlin, Android и iOS **NOT RUN** в этой редакторской задаче; их результаты не следуют из словарной сверки.
