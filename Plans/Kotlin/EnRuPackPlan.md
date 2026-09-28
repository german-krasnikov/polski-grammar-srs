# Пакет en-ru: план (ADR-15 — проверка универсальности ядра)

Дата: 2026-09-28. Статус: архитектурный план, репозиторий не менялся при подготовке (read-only разведка). Автор: senior-architect + native-level линговист en/ru.

Источники, сверенные напрямую: [AI/vision.md](../../AI/vision.md), [AI/architecture.md](../../AI/architecture.md), [AI/decisions.md](../../AI/decisions.md) (ADR-12..20), [UniversalCorePlan.md](UniversalCorePlan.md) (§1-§12), [ContrastHighlightPlan.md](ContrastHighlightPlan.md) (emphasis contract); код: `courses/core/{features,constructions,template-ops,exercise-kinds}.json`, `courses/lang/pl/{lang,lexicon,curriculum,realization,exercise-recipes}.json`, `courses/pairs/pl-ru/pair.json`, `courses/pl-ru/vocabulary-editorial.json`, `courses/schema/{lang-pack-v2,pair-pack-v1,style-recipe-v1,vocabulary-editorial-v1}.schema.json`, `scripts/validate-pack-v2.mjs`, `kotlin/shared/src/commonMain/kotlin/polski/core/PlEngine.kt`, `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/{ConstructionRealizer,ExerciseGenerator,Recipe}.kt`, `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt`, `kotlin/shared/src/commonMain/kotlin/polski/presentation/StyleRecipe.kt`, `kotlin/shared/src/commonMain/kotlin/polski/grammar/AnswerNormalizer.kt` (+ 5 platform actuals), `kotlin/shared/src/commonMain/kotlin/polski/vocabulary/VocabularyDocument.kt`, `kotlin/shared/src/commonMain/kotlin/polski/preferences/UserPreferences.kt`, `kotlin/shared/build.gradle.kts`.

Критерий приёмки всего плана (ADR-15, UC-14 буквально): **ноль правок кода ради контента en-ru**; правки `:core-*`/`:pack-format`, которые действительно понадобились, зафиксированы здесь как явные задачи и чинятся в ядре, а не обходятся в пакете.

---

## 0. Итог разведки: где ядро реально упирается в pl

Прежде чем проектировать en-ru данные, зафиксирую факты, которые определяют весь план:

1. **`PackRegistry.active = packs.first()`** (`CourseData.kt:538-539`) — единственный активный пакет захардкожен позиционно. Второй пакет физически не может быть выбран, даже если данные существуют.
2. **`PlEngine.kt` — не движок, а pl-специфичная сборка** движка: `plMorphology`/`plExerciseGenerator`/`plChainSteps` читают `polski.data.*` (глобалы `CourseData.kt`, которые сами читают только `packRegistry.active`). Нет параметра «какой пакет» — есть только «пакет pl-ru».
3. **v2-схема (core/lang/pair, UC-12) не имеет Kotlin-загрузчика.** `generateCoursePackSource` встраивает только файлы `courses/*/course.json` (v1, один документ на пакет) — так же для `realization.json`/`exercise-recipes.json` (`shared/build.gradle.kts`: `realizationFixtureFile`/`exerciseRecipesFixtureFile` — **одна захардкоженная пара путей на `lang/pl`**, не сканирование каталога, в отличие от `curriculum.json`, которое уже сканируется по `lang/*`). Только `lang/<code>/curriculum.json` действительно language-agnostic сегодня.
4. **`ExerciseGenerator.lexicalSlotsFor`** (`core-engine/.../ExerciseGenerator.kt:155-156`) — приватная функция с ровно 4 захардкоженными именами слотов (`noun/adjective/owner/verb`), безусловно. Английские конструкции требуют дополнительных позиций (предлог для падежа-роли, вспомогательный глагол и `not` для отрицания) и **условного** заполнения (предлога нет для Subj/Obj; `do`/`not` нет при Polarity=Pos). Это единственная реальная точка в `:core-engine`, которую нужно менять кодом.
5. **`AnswerNormalizer`** — 5 платформенных `actual` реализаций жёстко берут `Locale.forLanguageTag("pl-PL")` (`AnswerNormalizerJvm.kt` и аналоги iOS/macOS/Android/JS/Wasm) независимо от активного пакета — открытый каталог языков заканчивается на сравнении ответа.
6. **`StudyDirection`** (`VocabularyDocument.kt:28`) — закрытый enum с ровно 2 wire-значениями `"pl-ru"/"ru-pl"`. Второй пакет не сможет объявить `"en-ru"/"ru-en"`.
7. **UC-09 (`MatrixTableEngine`) — только часть 1/2.** ADR-21 (2026-09-28, landed во время подготовки этого плана) добавил генерик `MatrixTableEngine`+`MatrixTableViewModel` в `:core-engine`/`:shared`, но **ни один хост не переключён** — `MatrixWeb.kt`/`MatrixScreen.kt`/`AndroidMatrixScreen.kt` и iOS/macOS-снапшоты (~1600 строк) всё ещё читают pl-литералы напрямую (часть 2/2, явно вынесена как отдельная задача в самом ADR-21). ADR-15 называет порядок «UC-09 → пакет en-ru» — часть 1/2 закрыта, часть 2/2 (реальное подключение хоста к `MatrixTableViewModel`) остаётся предусловием минимум для одного хоста (см. §5 гэп H, EN-24).
8. **`core/features.json`** сегодня — не универсальный словарь UD, а ровно то подмножество, которое использует pl (`Case.values` = 7 польских падежей). Открытость каталога (принцип №1 плана) впервые проверяется реальной второй записью.
9. **Три пикера (target/native/style)** из §7 плана не существуют — есть только один пикер стиля; `UserPreferencesV2.coursePair: String` — одна строка, не структура.

Разделы 1-4 ниже — данные en-ru пакета. Раздел 5 — гэпы (детально, со ссылками на файлы/строки). Раздел 6 — задачи EN-01..EN-nn.

---

## 1. Skeleton mapping: core-структуры → английская curriculum

### 1.1 Как английские явления укладываются в 6+1 core-структур

| CoreStructure | Английское явление | Реализация (данные, не код) |
|---|---|---|
| `core.argument.case-role` | Роль аргумента через предлог/порядок слов, падежа нет | Case-значения re-use'ают тот же `FeatureKey`, но со своим словарём: `Subj, Obj, To, With, In, Of, About` вместо pl-семёрки. Слот `prep` — новая лексическая категория с таблицей `Case → предлог`, пустой строкой для `Subj/Obj` (см. §5, гэп A). |
| `core.verb.tense` | Present/Past/Future Simple + Continuous/Perfect | Simple — как pl (лицо/число меняют суффикс `-s`/`-ed`). Continuous/Perfect — новый feature `Aspect` (`Simple/Continuous/Perfect`), ортогональный `Tense`; составной глагол = 2 слота (`aux`+`verb`), а не 1, как в pl. |
| `core.sentence.polarity` | `not`/`don't`/`doesn't`/`didn't` (do-support) | `aux`-слот (`do`, спрягается по Tense/Person/Number) + `neg`-слот (`not`, инвариант), оба присутствуют только при `Polarity=Neg`. Русский язык вставляет одну частицу «не» перед глаголом — do-support у него нет вообще (переносимый риск для L1-контраста, не для реализации). |
| `core.sentence.mood` | Инверсия вспомогательного глагола (`Do you see…?`) | **Порядок слов конструкции меняется по `Mood`**, не только присутствие слота (см. §5, гэп D) — `aux` встаёт перед подлежащим. |
| `core.agreement.possessive` | `my/your/his/her/its/our/their` | Проще, чем pl: владелец инвариантен (не склоняется), прилагательное инвариантно — `agree`-оператор для en просто не нужен внутри этой конструкции (борт agreementSource не задействуется). |
| `core.number` | `-s/-es/-ies` + нерегулярные (`child→children`) | Таблица форм существительного (TableMorphology), как в pl — orthography-правила — данные build-скрипта, не runtime-код (см. §1.3). |
| `core.composite` | Chain (5 шагов) + `mixed` + Simple/Continuous/Perfect-контраст | Тот же механизм `exercise-recipes.json`'s `chain`, что у pl; контрастный skill (аналог pl `aspect`) через recipe-override, не новый оператор. |

### 1.2 16 skills — `courses/lang/en/curriculum.json` (NEW)

Namespace — по целевому языку (`UniversalCorePlan.md §3.3/§6`): id вида `en:<shape>`. Размер и разброс уровней сопоставим с pl (16 skills, A1→B1, 5 A1, 8 A2, 3 B1).

| id | construction | focus | fixed | level | prerequisites |
|---|---|---|---|---|---|
| `en:role.object` | `core.argument.case-role` | `Case: Subj→Obj` | `Tense:Pres,Person:1,Number:Sing` | A1 | — |
| `en:verb.presentSimple` | `core.verb.tense` | `Tense: Past→Pres` | `Person:3,Number:Sing,Aspect:Simple` | A1 | — |
| `en:possessive.my` | `core.agreement.possessive` | null | `Case:Obj,Tense:Pres,Person:1,Number:Sing` | A1 | `role.object` |
| `en:mood.question` | `core.sentence.mood` | `Mood: Decl→YesNoQ` | `Tense:Pres,Person:2,Number:Sing,Aspect:Simple` | A1 | `verb.presentSimple` |
| `en:role.location` | `core.argument.case-role` | `Case: Obj→In` | `Tense:Pres,Person:1,Number:Sing` | A1 | `role.object` |
| `en:role.instrument` | `core.argument.case-role` | `Case: Obj→With` | `Tense:Pres,Person:1,Number:Sing` | A2 | `role.object` |
| `en:polarity.present` | `core.sentence.polarity` | `Polarity: Pos→Neg` | `Tense:Pres,Person:1,Number:Sing,Aspect:Simple` | A2 | `role.object` |
| `en:verb.pastSimple` | `core.verb.tense` | `Tense: Pres→Past` | `Person:3,Number:Sing,Aspect:Simple` | A2 | `verb.presentSimple` |
| `en:role.recipient` | `core.argument.case-role` | `Case: Obj→To` | `Tense:Pres,Person:1,Number:Sing` | A2 | `role.instrument` |
| `en:number.plural` | `core.number` | `Number: Sing→Plur` | `Case:Obj,Tense:Pres,Person:1` | A2 | `role.object`, `possessive.my` |
| `en:polarity.past` | `core.sentence.polarity` | `Polarity: Pos→Neg` | `Tense:Past,Person:1,Number:Sing,Aspect:Simple` | A2 | `polarity.present`, `verb.pastSimple` |
| `en:verb.futureSimple` | `core.verb.tense` | `Tense: Pres→Fut` | `Person:3,Number:Sing,Aspect:Simple` | A2 | `verb.pastSimple` |
| `en:pronouns` | `core.argument.case-role` | null | `Case:Obj,Tense:Pres,Person:1,Number:Sing` | A2 | `role.instrument`, `role.location` |
| `en:verb.presentContinuous` | `core.verb.tense` | `Aspect: Simple→Continuous` | `Tense:Pres,Person:3,Number:Sing` | B1 | `verb.presentSimple` |
| `en:tense.contrast` | `core.composite` | null | `Person:1,Number:Sing` | B1 | `verb.futureSimple`, `verb.presentContinuous` |
| `en:mixed` | `core.composite` | null | `{}` | B1 | `possessive.my`, `verb.pastSimple` |

Почему именно так (отклонения от «зеркалим pl 1:1» — осознанные):

- **Нет `case.acc.f/m/n`-аналога (родовой разбивки).** У pl падежное окончание объекта зависит от рода — 3 отдельных skill на первом шаге. У английского объект вообще не помечен (позиция после глагола, инвариант) — родовой развилки нет физически, поэтому `role.object` — один skill, а освободившееся «место» отдано отдельным ролям (`location`/`instrument`/`recipient`), которых у pl нет как отдельных skill (они у pl слиты в `case.inst`/`case.loc`/`case.dat` тоже по одному — тут пропорция сохраняется 1:1 по ролям, не по родам).
- **`aspect` (pl, B1, лексический фокус Imperfective→Perfective) не имеет буквального аналога** — у английского вид не лексический (разные глаголы), а грамматический (Aspect-feature). Аналог — `tense.contrast` (Simple vs Continuous vs Perfect для одного события) занимает то же место в графе prerequisites (последний B1 перед `mixed`).
- **Present Perfect сознательно не включён** в 16 (сокращает эквивалентный объём v1; естественное B2-расширение). Отмечено в §5 как контент-задача, не гэп ядра — Perfect укладывается в тот же `aux+verb`-механизм, что Continuous (`have`+past participle вместо `be`+`-ing`).

### 1.3 Файлы данных (что физически появляется)

```
courses/
  lang/en/
    lang.json            # usesFeatures: [Case,Number,Person,Tense,Polarity,Mood,Aspect]; case: [Subj,Obj,In,With,To,About,Of] (роли, не падежи)
    lexicon.json          # nouns[] (Number-only formsForms; неправильные множественные — child→children — как отдельная форма, не правило),
                           # adjectives[] (formsFormsInvariant — 1 значение на лексему),
                           # verbs[] (present3sg/past/pastParticiple/ing — 4 формы; irregular flag не нужен,
                           #          неправильная форма — это просто другая строка в той же таблице, ровно как pl),
                           # possessives[] (kind:"invariant" ×7 — форма уже поддержана `CoursePossessiveForms.Invariant`, CourseData.kt:161),
                           # personalPronouns (subject+object формы: I/me, he/him, she/her, …),
                           # auxiliaries.json-раздел НЕ отдельный файл — do/be/have/will как обычные verbs[] записи (id "do"/"be"/"have"/"will"),
                           #   их формы (do/does/did, am/is/are/was/were, has/have/had) — просто дополнительные строки той же verb-таблицы
    prepositions.json      # NEW лексическая категория: {id:"role", forms: {Case: предлог}} — единственная запись, см. §5 гэп A
    curriculum.json         # 16 SkillSpec (§1.2)
    realization.json        # construction → slots (order/gov/agree), см. §1.1 таблицу + §5 гэп D (orderWhen)
    exercise-recipes.json    # skillId → source/expected/changes; defaultOwnerLexeme:"my", verbLexeme:"walk" (нейтральный, не "go" — избежать неправильного прошедшего на первом уроке)
    morphology-notes.md      # авторинг: -s/-es/-ies, -ed/-d, дублирование согласной (stop→stopped), y→i (study→studied) — правила ЗДЕСЬ как заметка автору, материализуются вручную построчно в lexicon.json (то же решение, что и pl: таблица во время исполнения, правило — только при авторинге/сборке, §1.5 принцип 5 плана)
  pairs/en-ru/
    pair.json              # тот же pair-pack-v1.schema.json — 16 skills[] (formula/theory/hint/focus/methods/styleContent), sentenceSeeds, exerciseCopy/exercisePatterns, vocabulary
    lifehacks.json         # NEW, см. §4
  schema/
    lang-pack-v2.schema.json (существующий, без изменений) + возможное добавление "Aspect" в core/features.json (аддитивно)
```

Ирландский/неправильный глагол — не отдельный оператор шаблона: как и в pl (`GrammarEngine`'s pastStem — отдельная строка на лексему), неправильная форма — это просто другая строка в `forms.generated.json`, произведённая тем же `scripts/build-pack.mjs`-подобным конвертером для `lang/en`. Регулярные `-s/-ed/-ing` тоже материализуются build-скриптом (правило применяется один раз при сборке, не в runtime) — тем самым «таблицы данных» покрывают и словоизменение, и орфографию одним и тем же механизмом, без 11-го оператора.

---

## 2. Четыре стиля для en-ru + emphasis contract

### 2.1 Стили — без изменений формата (`courses/styles/*.json` уже язык-независимы)

Все 4 существующих рецепта (`rule-first`/`situation-first`/`native-contrast`/`minimal-theory`) применяются к en-ru без единой правки: `StyleComposer` уже читает `SkillStyleContent` (per-skill `rule/table/scene/nativeParallel/examples/why`, все опциональны кроме `nativeParallel`, `CourseData.kt:41-48`). en-ru просто заполняет `pairs/en-ru/pair.json`'s `skills[].styleContent` тем же способом, что pl-ru.

### 2.2 `nativeParallel` (стиль 3) — где русский помогает/сбивает

`SkillStyleContent.nativeParallel: List<NativeParallelPair>` (`{native, target, note, matches}`) — заполняется по-разному для каждого из 4 явлений, названных в задаче:

| Skill | native (ru) | target (en) | matches | note |
|---|---|---|---|---|
| `en:role.object` | Вижу дом. | I see a house. | false | В русском нет артикля вообще — «дом» без «a»/«the» звучит нормально; в английском голое существительное почти всегда требует артикль. Это первое расхождение, которое собьёт русскоговорящего чаще всего. |
| `en:polarity.present` | Я не вижу дом. | I don't see a house. | false | В русском отрицание — одна частица «не» перед глаголом, без вспомогательного слова. В английском нужен `do`/`does` + `not`, и именно `do`, а не глагол, несёт отрицание — русский не подсказывает, что нужно вставить лишнее слово. |
| `en:verb.presentContinuous` | Я иду домой (сейчас). | I am going home. | false | Русский вид (иду — процесс vs пойду — начало) не совпадает по границам с английским Simple/Continuous: один и тот же русский несовершенный «иду» может быть и Present Simple («I go» — привычка), и Present Continuous («I am going» — сейчас) в зависимости от контекста, который в русском не маркирован грамматически вообще. |
| `en:mood.question` | Ты видишь дом? | Do you see a house? | false | В русском вопрос — только интонация/порядок, без изменений в глаголе. В английском обязательна инверсия (аналог pl, но там это do-support, не порядок подлежащее-глагол): нужен вспомогательный `do`, вынесенный перед подлежащим. |
| `en:role.location` | Я говорю о доме. | I talk about the house. | false | Русский родительный/предложный падеж не подсказывает, КАКОЙ предлог нужен в английском (about, не *of/on*) — предлог полностью лексический, падеж не выбирает его автоматически. |

`matches: true`-случай для контраста (нужен хотя бы один, чтобы стиль не выглядел «всегда всё разное» — методически честно показывать и совпадения): `en:possessive.my` → «Моя книга» / «My book» — порядок владелец-перед-существительным совпадает в обоих языках, притяжательное не согласуется по числу/роду ни там, ни там на уровне «мой/my» (в отличие от pl, где мой/moja/moje меняется по роду) — единственный skill из 16, где английское явление проще русского, не только польского.

### 2.3 Emphasis contract для английского (расширение `ContrastHighlightPlan.md`)

Правила §1 контракта («что выделяется») переносятся без изменения таксономии (`ending`/`alternation`/`whole`/`insertion`/`deletion`/`moved`) — только словарь единиц меняется:

| Явление | Вид (`ContrastHighlightPlan.md` §1) | Пример |
|---|---|---|
| `-s`/`-es`/`-ies` (3 л. ед., мн. число) | `ending` | `work → works`, `study → studies` |
| `-ed`/`-d` (прошедшее регулярное) | `ending` | `walk → walked` |
| Удвоение согласной перед `-ed`/`-ing` (`stop→stopped`) | `alternation` (вторичная точечная метка на удвоенной букве, как pl `ó~o`) | `stop → stopped` |
| Неправильное прошедшее (`go→went`) | `whole` (суппletив, как pl `robić→zrobić`) | `go → went` |
| Вставка `do`/`does`/`did` (вопрос/отрицание) | `insertion`, целым словом на стороне «стало» | `You see… → Do you see…?` |
| Вставка `not`/`n't` | `insertion` | `I see… → I do not see…` |
| Инверсия `aux`+подлежащее (вопрос) | `moved`, целым словом на обеих сторонах | `You see → Do you` (переставленные позиции) |
| Предлог, зависящий от роли (`with`/`to`/`about`) | `whole` (неоднозначное разбиение — предлог не окончание существительного) | `a pen → with a pen` |
| Артикль `a`/`the` (если решим вводить отдельным skill позже) | `insertion` | вне 16 skills сейчас — зарезервировано |
| Стяжения `don't`/`doesn't`/`didn't` как typed-alternative | **не подсвечиваются вообще** — accepted-вариант, не `FormChange` | правило §1 контракта: «альтернативы никогда через `to`, только `accepted`» |

Финальная пунктуация (`?` для вопроса) — не через новый оператор `punct` ядра: как и у pl (`ADR-18` правка), это `TextSpec.Prefixed.punct` — обычное поле данных `exercise-recipes.json`, уже обязательное после кода-ревью UC-07. En-ru просто ставит `"punct": "?"` вместо `"."` там, где нужно — **готовый механизм, без изменений `:core-engine`**.

---

## 3. Словарные карточки en-ru

### 3.1 Источник частотного списка

Тот же паттерн, что `courses/pl-ru/frequency-top1000.json` (Leksjo/NKJP, CC BY 4.0, выбран конкретно из-за отсутствия ShareAlike). Для английского два защитимых кандидата без ShareAlike-риска:

1. **Peter Norvig's `count_1w.txt`** (Google Books Ngram-derived, публично распространяется для реиспользования, без ShareAlike) — простой юнигр­аммный список, придётся вручную отсеивать не-леммы (грамматика: `the`, `of`…), т.к. список не лемматизирован.
2. **hermitdave/FrequencyWords** (`en/en_50k.txt`, OpenSubtitles-derived) — уже лемматизирован ближе к разговорной речи (хорошо подходит под A1-A2 словарь курса), но точная лицензия исходных субтитров не проверена этим планом.

**Решение — не принимается здесь.** Задача EN-13 (ниже) явно требует: зафиксировать URL/revision/SHA-256/лицензию в `frequencySource`-блоке (тот же формат, что `courses/pl-ru/frequency-top1000.json`), с проверкой лицензии перед копированием любых строк в репозиторий — то же правило, что уже применено к pl (`ATTRIBUTION.md`, §8.6 плана: «предпочтение источников без ShareAlike для коммерческого продукта»). Не фиксирую сейчас конкретный revision/SHA, чтобы не задокументировать непроверенный факт.

### 3.2 Редакторский конвейер — без изменений схемы

`courses/schema/vocabulary-editorial-v1.schema.json` уже язык-agnostic (`card.provenance` различает `project-authored` vs `external` с `url/revision/license/attribution`) — en-ru использует тот же файл-формат под новым путём `courses/en-ru/vocabulary-editorial.json` (или `courses/pairs/en-ru/vocabulary-editorial.json` — решение места см. EN-14, т.к. ADR-20 сознательно НЕ перенёс `vocabulary-editorial.json` pl в pair-слой, «уже пар-скоуп по месту» — для нового пакета естественное место сразу `pairs/en-ru/`, без миграции). Перевод/пример — `project-authored` (редакция), форма — если из словаря-источника — `external` с конкретной ссылкой (Merriam-Webster/Oxford API — оба требуют коммерческую лицензию для API-доступа, не для отдельных проверенных вручную слов — редакционная проверка, не автоматический дамп, как и у pl).

Объём — тот же принцип, что pl (`frequency-top1000` → отбор editorial карточек с `status: approved|needs-review`), сайзинг не заявляется здесь числом (задача EN-15 сама считает по факту отбора, ориентир — сопоставимый с pl-ru объём текущих карточек, не строгое совпадение).

---

## 4. Лайфхаки (ADR-15)

### 4.1 Модель данных — pair-слой, вне ядра

NEW `courses/schema/lifehacks-v1.schema.json` + `courses/pairs/<target>-<native>/lifehacks.json`:

```json
{
  "schemaVersion": 1,
  "pairId": "en-ru",
  "lifehacks": [
    {
      "id": "en-ru.role.object.article",
      "skillId": "en:role.object",
      "text": "В русском нет артиклей вообще — при переводе на английский почти любое единственное исчисляемое существительное без «this/my/the» требует «a»: «дом» → «a house», не «house».",
      "source": { "kind": "research", "citation": "Master, P. (1997). The English article system: acquisition, function, and pedagogy.", "url": "https://doi.org/10.1016/S0346-251X(96)00063-8" },
      "status": "editorial",
      "votes": { "helpful": 0, "notHelpful": 0 }
    },
    {
      "id": "en-ru.polarity.present.do",
      "skillId": "en:polarity.present",
      "text": "Отрицание в английском несёт вспомогательный «do», а не смысловой глагол — если ловишь себя на «I not see», проверь: смысловой глагол в отрицании и вопросе ВСЕГДА в базовой форме, «do/does/did» берёт на себя время и лицо.",
      "source": { "kind": "teaching-practice", "citation": "Language Transfer, English course, unit on negation — систематическая ошибка русскоговорящих (перенос «не + глагол»), устная практика.", "url": "https://www.languagetransfer.org/courses" },
      "status": "editorial",
      "votes": { "helpful": 0, "notHelpful": 0 }
    }
  ]
}
```

Поля: `id` (уникален глобально, префикс pairId), `skillId` (nullable — `null` + `topic: string` для кросс-скилловых советов, напр. про артикли в целом), `text` (одна русскоязычная строка, style-neutral — не дублируется на 4 стиля), `source.kind` (`research|teaching-practice|project-authored`), `status` (`editorial|community`), `votes` (placeholder-объект, не используется до серверной задачи). **Не изобретать** `"verified": true` или похожие поля — ADR-15 прямо запрещает придуманные «проверено пользователями» claims: сейчас `status` всегда `"editorial"`, `"community"` — зарезервировано на будущее.

### 4.2 `LifehackProvider` — зарезервированный порт

Тот же паттерн §5.4 `UniversalCorePlan.md` (`ExplanationProvider`/`AudioProvider`): `:core-presentation`, без сервера сейчас.

```kotlin
data class Lifehack(val id: String, val text: String, val source: LifehackSource, val status: LifehackStatus)
fun interface LifehackProvider { fun forSkill(skillId: String): List<Lifehack> }
// default = StaticPackLifehackProvider(pack) — читает pairs/<pairId>/lifehacks.json, текущее статичное поведение
```

Приём голосов (`helpful/notHelpful`) — отдельная будущая задача через сервер, ядро от неё не зависит (буквально повторяет решение ADR-15 по формулировке).

### 4.3 UI-размещение — отдельно от стилей, не через `BlockKind`

**Не добавляется** как 10-й `BlockKind` в `StyleRecipe`/`StyleComposer` — лайфхак не относится ни к одному стилю персонально (ADR-15: «доступен во всех стилях»), а `BlockKind`/`requires`/`fallback` — механизм именно про то, что́ *отличается* между стилями. Вместо этого: карточка на каждом хосте рисует лайфхак как **отдельный, всегда одинаковый, сворачиваемый блок** после блоков стиля (после `back`-фазы), источник — `LifehackProvider.forSkill(skill.id)`, пусто → блок не рисуется вообще (не пустая рамка). Подпись обязательна: `«Лайфхак · источник: <editorial/community>»` + короткая citation при раскрытии — тот же принцип §3 `ContrastHighlightPlan.md` («нецветовая опора обязательна»), перенесённый на атрибуцию, не на цвет.

Почему не блок стиля: если сделать `BlockKind.Lifehack` и добавить его в `blocks.front/back` каждого из 4 стилей, придётся держать инвариант «во всех 4 рецептах одинаково» руками — источник ошибки (аналог ADR-14: забыли синхронизировать closed enum с открытым списком). Разместив вне `StyleRecipe`, инвариант «показан всегда» становится структурным, а не договорным.

### 4.4 Начальный отобранный набор

- **en-ru:** ~2 на каждый из 16 skills (см. §4.1 для 2 примеров) — задача EN-16 производит оставшиеся ~30, каждый с `source.kind=research|teaching-practice` и настоящей ссылкой (никогда `project-authored` без реального обоснования методики — «project-authored» здесь означает «редакция сама сформулировала на основе типологического сравнения», не «выдумано без опоры»).
- **pl-ru — первый набор (не полное покрытие):** по 1 на самые сложные/типологически неожиданные 5 skills, где перенос из русского систематически сбивает — `case.gen.neg` (родительный при отрицании — в русском варьируется, в польском обязателен), `case.inst` (творительный без предлога после «być»/статичных глаголов — в русском часто с предлогом «с»), `agreement.my` (согласование по роду — русское «мой/моя/моё» лексически похоже, но правило согласования разное для одушевлённых мужского рода), `aspect` (совершенный/несовершенный вид — оба языка его имеют, но границы не совпадают 1:1), `mixed` (когнитивная нагрузка комбинирования — методический, не грамматический совет). Полное покрытие всех 16 pl-ru skills — отдельная задача после en-ru (не в объёме этого плана).

---

## 5. Гэпы ядра, которые нужно закрыть (не обойти в пакете)

Пронумерованы по возрастанию сложности; A и D — единственные, требующие правки `:core-engine`-кода (Kotlin), остальные — `:pack-format`/данные/UI.

### Гэп A — `ExerciseGenerator.lexicalSlotsFor` захардкожен на 4 позиции (БЛОКИРУЕТ role/polarity skills)

`core-engine/.../ExerciseGenerator.kt:155-156`: `mapOf("noun" to nounId, "adjective" to adjectiveId, "owner" to owner, "verb" to verbLexeme)` — фиксированная форма, без условности. Английскому нужно: `prep` (лексема-константа `"role"`, её РЕАЛЬНАЯ форма зависит от `Case` через обычный `TableMorphology`-lookup слота — сам слот нужен только когда `Case ∉ {Subj, Obj}`), `aux`+`neg` (лексемы-константы `"do"`/`"not"`, нужны только при `Polarity=Neg`). **Не новый оператор** — это обобщение того, что уже есть (`defaultOwnerLexeme`/`verbLexeme` — пакетные константы, `optional` — условное отсутствие слота): нужно, чтобы (1) слот мог быть пакетной константой, а не только «носитель/глагол», (2) присутствие слота в `lexicalSlots` могло зависеть от уже резолвленного `FeatureBundle` (не только от seed). Проверка регресса pl — существующие golden guards `TrainingParityTest`/`GrammarParityTest` (`ADR-19`) — pl's 4 позиции становятся частным случаем того же обобщённого правила, byte-identical.

### Гэп B — verb/prep/aux формы — чистые данные, не гэп (для полноты)

Расширение таблицы форм глагола (`-ing`/`pastParticiple`) и новая категория `prep:role` — целиком `TableMorphology`, зафиксировано отдельно только чтобы явно сказать: **это НЕ гэп**, `:core-engine` уже поддерживает произвольные лексические категории по префиксу (`"${slot.category}:$lexeme"`, `ConstructionRealizer.kt:70`) — новая категория `prep` не требует ни строчки Kotlin.

### Гэп C — `core/features.json` — первая реальная проверка «открытого каталога»

`Case.values` сегодня — буквально 7 польских падежей; `Polarity`/`Mood`/`Tense`/`Person`/`Number` уже достаточно общие для английского без изменений. Нужно добавить (аддитивно, не трогая pl'ские значения): `Case.values` += `["Subj","Obj","In","With","To","About","Of"]`, и новый feature-key `Aspect: ["Simple","Continuous","Perfect"]`. `scripts/validate-pack-v2.mjs`'s `checkFeaturesResolve` (`scripts/validate-pack-v2.mjs:41-50`) уже проверяет ПРОИЗВОЛЬНОЕ множество значений — само это изменение не требует правки валидатора, только данных. Приёмка: `pl-ru` продолжает валидироваться (значения не удалены, только добавлены) — прямая проверка тезиса ADR-12 «открытый каталог».

### Гэп D — `ConstructionTemplate.order` не может меняться по условию (БЛОКИРУЕТ `mood.question`)

`ConstructionRealizer.kt:42-48,61-77`: `slots: List<SlotTemplate>` — один фиксированный список на конструкцию. Для `Do you see…?` вспомогательный `aux` физически стоит ПЕРЕД подлежащим, а в утверждении `You see…` — после (точнее, слитно с глаголом). Это не «слот условно присутствует» (гэп A), а «сам порядок слотов меняется по значению `Mood`». Нужен `orderWhen: Map<String, List<SlotTemplate>>` (тот же однословный синтаксис условия `"Key=value"`, что уже есть у `govWhen`/`requiredFeaturesWhen`) — обобщение уже существующего паттерна «default + условные варианты», не новый класс механизма. Приёмка pl — `govWhen`/`requiredFeaturesWhen` не переименовываются, `order` (без `orderWhen`) продолжает работать как единственный список для конструкций, где он не задан.

### Гэп E — `AnswerNormalizer` жёстко на `pl-PL`-локали (не блокирует en, но нарушает открытость)

5 файлов (`AnswerNormalizerJvm.kt:6`, аналоги Android/iOS/macOS/JS/Wasm) — `Locale.forLanguageTag("pl-PL")` независимо от активного пакета. Для английского ASCII-текста результат `lowercase(pl-PL)` совпадает с `lowercase(en)` — **функционально не сломает en-ru**, поэтому не блокирует пакет, но это открытый языковой литерал в общем коде вопреки принципу №1 (`UniversalCorePlan.md §1`). Правильное решение — параметризовать локаль через порт (`fun interface AnswerLocale { fun tag(): String }`, поставляется активным пакетом через `lang.json`), не 5 разных `expect/actual`-хардкодов. Не блокирует EN-релиз, но должно закрыться до третьего пакета (иначе к третьему языку с реальными кириллическими/турецкими casing-особенностями это стало бы реальным багом, не только архитектурным нарушением).

### Гэп F — `StudyDirection` — закрытый enum на 2 значения (БЛОКИРУЕТ словарь en-ru)

`VocabularyDocument.kt:28`: `enum class StudyDirection(val wire: String) { RussianToPolish("ru-pl"), PolishToRussian("pl-ru") }`. Тот же класс проблемы, что у `PreferredStyle` до ADR-13 — закрытый enum там, где пара языков должна быть данными. Решение — тот же паттерн, что `StyleId` (ADR-13): `data class StudyDirection(val nativeToTarget: String, val targetToNative: String)`, производные от активного `pack.pairId`, с сохранением текущих строк `"ru-pl"/"pl-ru"` как констант для pl-ru (совместимость сохранена побайтно). `VocabularyWeb.kt:123` (`if (directionSelect.value == "pl-ru")`) — единственный вызывающий код, который увидит смену типа.

### Гэп G — `PackRegistry`/`PlEngine` — единственный пакет вместо реестра (САМЫЙ ДОРОГОЙ гэп)

`PackRegistry.active = packs.first()` + `PlEngine.kt`'s pl-специфичные `val`ы (не функции от id пакета) — второй пакет физически не запускается, даже если весь остальной контент готов. Это не один гэп, а связка (детализирована как отдельные задачи EN-01..EN-06 в §6): загрузчик v2-слоёв, обобщённое встраивание `realization.json`/`exercise-recipes.json` по каталогу (не по 2 захардкоженным путям), выбираемый `PackRegistry`, генерализация `PlEngine.kt` в фабрику «пакет → движок», три пикера (`target/native/style`) вместо одной строки `coursePair`, namespacing прогресса (`${target}:${skillId}`) и его фильтр (`activePackFilter`, сегодня определён но нигде не подключён к реальному предикату — только объявление класса).

### Гэп H — UC-09 часть 2/2 (хосты не подключены к `MatrixTableViewModel`) — явное предусловие ADR-15, не закрыто

ADR-15 фиксирует порядок «UC-09 (таблицы как данные) → пакет en-ru». ADR-21 закрыл часть 1/2 (генерик-движок, `:core-engine`/`:shared`, без языка/пакета в коде, `NoLanguageLiteralsTest` зелёный) — но ни один хост не читает через него; все 5 хостов остаются на pl-литералах. En-ru не обязан немедленно иметь полностью параллельный UI матрицы на всех 5 хостах (это отдельная, большая, механическая задача по хостам — собственно часть 2/2 ADR-21), но **пакет не может считаться завершённым без хотя бы одной живой английской матрицы через `MatrixTableViewModel`** — иначе критерий ADR-15 «пакет использует всё, включая таблицы» не выполнен. Включено как задача с явно ограниченным скоупом (см. EN-24, теперь строит НА готовом `MatrixTableViewModel`, а не с нуля).

---

## 6. Задачи EN-01..EN-24 (лейны, зависимости, параллельность)

Лейны: **core** (`:core-engine`/`:core-model`/`:pack-format`/`:shared` некоммерческий код), **content** (`courses/*` данные + Node-скрипты/схемы), **web/android/ios/macos** (хост-код). Порядок: 01-08 разблокируют пакет (без них en-ru не запустится), 09-16 — контент пакета (частично параллельны 01-08 — авторинг данных не ждёт движка), 17-24 — UI/хосты и полировка.

| ID | Задача | Лейн | Приёмка | Зависит от | Параллельно с |
|---|---|---|---|---|---|
| EN-01 | `core/features.json`: добавить `Case`-роли (en) + `Aspect` — аддитивно | content | `validate-pack-v2.mjs` зелёный на pl-ru (регрессия) + новый фикстур-тест на en-значения | — | EN-02..EN-16 |
| EN-02 | `ConstructionRealizer`: обобщить `lexicalSlotsFor`-паттерн — слот = пакетная константа ИЛИ условно присутствует по резолвленному `FeatureBundle` (гэп A) | core | `TrainingParityTest`/`GrammarParityTest` (pl) зелёные без изменения ожиданий; новый unit-тест `:core-engine` с синтетической (не en, не pl) конструкцией, где слот то есть, то нет | — | EN-03 |
| EN-03 | `ConstructionTemplate.orderWhen` — условный порядок слотов (гэп D) | core | Существующие `:core-engine` тесты (order/gov/agree) зелёные без изменений; новый unit-тест — 2 конструкции с разным order по условию дают разные `RealizedSentence.slotSpans` | — | EN-02 |
| EN-04 | v2-слой → Kotlin: `CoursePackLoader` читает `core/lang/pair` (не только v1 `course.json`) в тот же `CoursePack`-контракт | core | pl-ru, реконструированный из v2-слоёв (уже проверено Node-стороной, ADR-20), даёт тот же Kotlin `CoursePack`, что текущий v1-путь — фикстурное сравнение | EN-01 | EN-05 |
| EN-05 | `shared/build.gradle.kts`: `realization.json`/`exercise-recipes.json` — сканирование `lang/*/` вместо 2 захардкоженных путей (как уже сделано для `curriculum.json`) | core | Gradle-таск даёт тот же встроенный pl-контент; новый файл под `lang/en/` подхватывается без правки `.gradle.kts` | — | EN-04 |
| EN-06 | `PackRegistry`: реестр по id пары, выбираемый активный пакет вместо `packs.first()` | core | pl-ru продолжает быть дефолтным активным (текущее поведение без выбора); юнит-тест переключает активный пакет на второй фикстур-пакет и видит другой `pairId` | EN-04 | EN-07 |
| EN-07 | `PlEngine.kt` → generic `PackEngine`-фабрика (пакет → `TableMorphology`+`ConstructionRealizer`+`ExerciseGenerator`), без переименования публичных `plX`-функций (совместимость хостов) | core | Все 5 хостов компилируются без изменения вызовов; `TrainingParityTest` зелёный | EN-02, EN-03, EN-06 | — |
| EN-08 | `UserPreferencesV2.coursePair: String` → `CourseSelection(target, native, style)` + tolerant codec v3→v4 (`"pl-ru"→(pl,ru)`) | core | Существующий сохранённый v3 JSON (React-экспорт и KMP) загружается неизменным; golden JSON совпадает | EN-06 | EN-09..EN-16 |
| EN-09 | `StudyDirection`: закрытый enum → открытая пара строк (гэп F), `"pl-ru"/"ru-pl"` как сохранённые константы | core | `VocabularyWeb.kt` компилируется без изменения текущего поведения pl-ru; новый юнит-тест строит en-ru направление | — | EN-01..EN-08 |
| EN-10 | Progress namespacing: `${target}:${skillId}` для нового пакета, `activePackFilter` реально подключён (сегодня объявлен, не вызван предикатом реального пакета) | core | pl-ru прогресс не намespace'ится (обратная совместимость буквально сохранена); синтетический en-namespaced skillId проходит `SkillQueue` без краша | EN-06, EN-07 | EN-11..EN-16 |
| EN-11 | `lang/en/lang.json`+`lexicon.json` (nouns/adjectives/verbs/possessives/personalPronouns) + `prepositions.json` | content | `validate-pack-v2.mjs` (обобщённый на `lang/en`) зелёный; ручная сверка форм носителем (лингвист-ревью) | EN-01 | EN-12 |
| EN-12 | `lang/en/curriculum.json` — 16 `SkillSpec` (§1.2 таблица) | content | Кросс-чек §8.2 (construction∈constructions.json, focus/fixed∈features.json+usesFeatures) зелёный | EN-01, EN-11 | EN-11 |
| EN-13 | `lang/en/realization.json` — 7 construction-шаблонов, включая `orderWhen` для `core.sentence.mood` | content | Ручной прогон 16 skills через `ConstructionRealizer`+тест-фикстуру даёт грамматически верные предложения (лингвист-ревью — не автоматизируется полностью) | EN-02, EN-03, EN-11 | — |
| EN-14 | `lang/en/exercise-recipes.json` — wiring всех 16 skills (source/expected/changes/chain/mixed) | content | Живой прогон `ExerciseGenerator.generateForSkill` на каждый из 16 id даёт непустой `CoreExercise` без ошибок; ручная лингвистическая проверка примеров | EN-13 | — |
| EN-15 | Частотный список en: выбрать источник, зафиксировать `frequencySource`(url/revision/SHA-256/лицензия) — см. §3.1 | content | `courses/en-ru/frequency-topN.json` проходит ту же схему-проверку, что pl; лицензия документирована в новом `ATTRIBUTION.md` | — | EN-01..EN-14 |
| EN-16 | Редакторские словарные карточки en-ru (`vocabulary-editorial.json`) — перевод/пример/форма, review-цикл как у pl | content | Схема `vocabulary-editorial-v1` валидна; каждая карточка `status=approved` имеет `reviewer`/`reviewedAt`/`reviewSources` | EN-15 | — |
| EN-17 | `pairs/en-ru/pair.json` — 16 skills[] (formula/theory/hint/focus/methods/styleContent), sentenceSeeds, exerciseCopy/exercisePatterns | content | `pair-pack-v1.schema.json` валиден; каждый из 16 skill id совпадает с `lang/en/curriculum.json` | EN-12, EN-14 | — |
| EN-18 | `nativeParallel`-контент для всех 16 skills (не только 5 примеров §2.2) | content | `StyleComposer`-рендер `native-contrast`-стиля непустой на каждом из 16; лингвист-ревью на «не сбивает, а объясняет» | EN-17 | EN-19 |
| EN-19 | `pairs/en-ru/lifehacks.json` — ~30 записей (§4.4) + схема `lifehacks-v1.schema.json` | content | Схема валидна; каждая запись имеет реальный `source.citation`, не пустой/не выдуманный | EN-17 | EN-18 |
| EN-20 | `pairs/pl-ru/lifehacks.json` — первый набор (5 записей, §4.4) | content | Та же схема; не блокирует en-ru, может идти в любой момент после EN-19 | EN-19 | — |
| EN-21 | `LifehackProvider`-порт + `StaticPackLifehackProvider` + UI-блок «Лайфхак» на всех 5 хостах (сворачиваемый, с атрибуцией) | core+web+android+ios+macos | Блок рендерится на каждом хосте при непустом списке, отсутствует при пустом; VoiceOver/screen-reader читает подпись источника | EN-19 | EN-22 |
| EN-22 | Настройки: пикеры target/native (новые) рядом с существующим style-пикером на 5 хостах | web/android/ios/macos/desktop | Выбор `en`+`ru` реально переключает активный пакет (использует EN-06/EN-08); существующий pl-ru выбор — поведение не изменилось | EN-08, EN-10 | EN-23 |
| EN-23 | `AnswerEvaluator`/emphasis-контракт: ручная проверка всех 16 en-ru кейсов (do-support вставка, `n't` как accepted, неправильные глаголы как `whole`) на `EndingHighlightTest`-аналоге | core+content | Тесты по образцу `EndingHighlightTest` (see `ContrastHighlightPlan.md` §7) зелёные для нового набора кейсов; `do`/`don't` never rendered as diff, только как `accepted` | EN-14, EN-02 | EN-24 |
| EN-24 | UC-09 часть 2/2 минимум: подключить web-хост (`MatrixWeb.kt`) к уже готовому `MatrixTableViewModel` (ADR-21) для en — Present/Past/Future × Person, do-support-строка; полное переключение всех 5 хостов на `MatrixTableViewModel` — отдельная последующая задача, не блокирует релиз en-ru | web | Таблица показывает реальные формы из `forms.generated.json`(en) через `MatrixTableViewModel`, не через новый ad hoc код; остальные 4 хоста — задокументированный, явный техдолг, не тихое упущение | EN-11..EN-14 | EN-22 |

Параллельность по волнам: **волна 1** (EN-01 → EN-02/EN-03/EN-05 параллельно) → **волна 2** (EN-04, EN-06 → EN-07/EN-08 параллельно; EN-09 независим и параллелен всей волне 1-2) → **волна 3** контент (EN-11 → EN-12 → EN-13 → EN-14, строго последовательно внутри лейна content, но параллельно всей core-волне 2 — авторинг данных не ждёт движка, только финальный «живой прогон» EN-14 ждёт EN-02/EN-03) → **волна 4** (EN-15/EN-16 параллельны волне 3; EN-17 после EN-12+EN-14; EN-18/EN-19/EN-20 параллельны друг другу после EN-17) → **волна 5** (EN-21/EN-22/EN-23/EN-24 — хостовые и полировочные, все параллельны друг другу, зависят только от соответствующих волна-3/4 задач).

---

## 7. Что явно НЕ входит в этот план

- Полное покрытие UC-09 по всем 5 хостам (только EN-24 minimum slice).
- Present Perfect / Present Perfect Continuous как отдельные skills (естественное B2-расширение, не часть 16).
- Серверная часть `LifehackProvider` (голоса/community-статус) — порт зарезервирован, реализация — отдельная будущая задача (ADR-15 буквально).
- ~~Полное покрытие лайфхаками всех 16 pl-ru skills (только первые 5, §4.4).~~ Закрыто отдельной
  задачей после EN-20: `courses/pairs/pl-ru/lifehacks.json` теперь содержит по 2 записи на каждый
  из 16 skills (32 записи), каждая с реальной проверяемой ссылкой (research-грамматики: Swan 2002,
  Bielec 1998, Sadowska 2012, Rothstein 1993, Sussex & Cubberley 2006, Corbett 1991, Franks & King
  2000, Dickey 2000; методические — Rohrer & Taylor 2007, Roediger & Karpicke 2006), status
  `editorial` (ADR-15: без выдуманных `verified`-заявлений). Тесты обновлены на контракт «≥2 на
  каждый skill» (`tests/lifehacks-pl-ru.test.ts`), затронутые host-тесты, зависевшие от старого
  5-записного/пустого содержимого (`tests/browser/kotlin-lifehack-block.spec.ts`,
  `MacSnapshotStyleBlocksTest.kt`, `DesktopScreenTest.kt`), обновлены на реальное новое содержимое,
  не на моки.
- Выбор конкретного revision/SHA частотного списка (EN-15 сама фиксирует, не предрешено здесь, чтобы не задокументировать непроверенный факт).
