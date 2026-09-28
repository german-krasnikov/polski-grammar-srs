# Журнал решений (ADR)

Новые сверху. Формат: решение → почему → где подробно.

## ADR-30 · 2026-09-28 · EN-22 (android): `packRegistry` реально встраивает en-ru + `usableCourseSelections` — пикер предлагает только реально парсящийся пакет, переключение — через перезапуск процесса

`Plans/Kotlin/EnRuPackPlan.md` §6 EN-22, android-часть. Два независимых, но связанных решения,
найденных по ходу задачи (не в самом плане):

**1. `packRegistry` (`CourseData.kt`) до этой задачи встраивал только v1-пакеты
(`courses/<id>/course.json`) — то есть ровно один pl-ru; en-ru (EN-04..EN-20, только v2-слои,
`lang/en/lexicon.json` + `pairs/en-ru/pair.json`) физически не мог быть выбран, `PackRegistry.select`
кидал бы `Unknown pack pairId`.** Публичная продакшн-`packRegistry` теперь = v1-пакеты + любой
`pairId` из `generatedPairJsonByPairId`, которого нет среди v1 (реконструирован
`CoursePackLoader.fromV2Layers`, EN-04) — pl-ru всегда первый/дефолтный, en-ru добавляется без
дублирования. Новые публичные точки для хостов: `availableCourseSelections` (каждый
встроенный пакет), `selectActiveCoursePack(pairId)` (переключает, no-op на неизвестный/уже активный).

**2. Обнаружен реальный core-гэп, не входящий в EN-22 (и ни в одну задачу плана): схема
`CoursePack` (v1, ~35 полей) написана только под pl-ru и требует рода/падежа почти everywhere
(`Noun.gender`, `possessiveForms`/`futureAuxiliary`/референс-таблицы с ключами по падежу/роду,
закрытый `PossessiveId` не знает `its`).** У английского нет ни рода, ни падежа
(`lang/en/lexicon.json` корректно их не содержит — это не недостающий факт контента, а другая
грамматика) — попытка `packRegistry.select("en-ru")` вживую ломает ~30 из ~35 полей
`CoursePack` (`IllegalStateException`/`NoSuchElementException` на `.nouns`, `.adjectives`,
`.verbs`, `.possessiveForms`, `.futureAuxiliary`, все `reference*`-таблицы — точный список см.
`EnRuPackSwitchTest`/`CourseData.kt`'s `parsesCompletely()`). Добавлять «факты» контенту, чтобы
эти поля не падали, значит изобретать несуществующую английскую грамматику — прямо запрещено
инструкцией задачи. Решение: `usableCourseSelections` — рантайм-проба одноразового `CoursePack`
(никогда не через сам `packRegistry`, чтобы сломанный пакет не попал в process-wide кэш) по всем
полям; `selectActiveCoursePack` переключает только то, что прошло пробу. Сегодня это только
pl-ru — пикер на Android (`AndroidCoursePicker.kt`) предложит en-ru сам, без правки хоста, в тот
день, когда `CoursePack`'ную схему обобщат под pl-агностичный пакет (отдельная, ещё не заведённая
задача).

Отдельно: переключение пакета — не in-place мутация состояния, а рестарт процесса
(`MainActivity.restartApp`), потому что каждый `by lazy { packRegistry.active.* }` в
`CourseData.kt`/`Nouns.kt`/`Adjectives.kt`/`Verbs.kt`/`Skills.kt` кэшируется на весь процесс при
первом чтении — переключение `packRegistry.active` после этого меняет только указатель, не то,
что уже показано. `AndroidSessionViewModel`'s ранний `init`-блок вызывает
`selectActiveCoursePack` из persisted `target`/`native` до первого чтения курсовых данных;
`UserPreferencesCodec.peekTargetNative`/`AndroidUserPreferencesStore.peekTargetNative` — сырое
чтение без валидации против текущего `packRegistry.active` (у `decode()` эта валидация есть и
иначе не даёт даже долистать до `select`).

Проверено: `:shared:desktopTest` (348/348, включая новый `EnRuPackSwitchTest` +
`UserPreferencesCodecTest`'s `peekTargetNative*`), `:shared:compileKotlin{Js,WasmJs,MacosArm64}`,
`:androidApp:testDebugUnitTest` (88/88), `:androidApp:assembleDebug` — зелёные; живой прогон на
`emulator-5554`: чистая установка грузится в pl-ru без изменений, «Настройки» показывают новый
блок «Курс» (только Польский/Русский — en-ru корректно скрыт), повторный выбор уже активного
курса — no-op без рестарта, вкладки Тренировка/Матрица/Слова не падают.

## ADR-29 · 2026-09-28 · EN-21 (android): `AndroidLifehackBlock` — тот же сворачиваемый блок «Лайфхак», что web (ADR-27), теперь на Android-хосте

`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidLifehackBlock.kt`: android-часть
EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.3/§6) поверх уже готового `LifehackProvider`-порта и
`StaticPackLifehackProvider` (ADR-27, `:shared`, без изменений) — здесь только UI-проводка, никакой
новой архитектуры. Как и на web: лайфхак не 10-й `BlockKind` (ADR-15 прямо запрещает это), поэтому
не идёт через `AndroidBlockList`/`StyleComposer` — `AndroidTrainingScreen.kt` вызывает
`AndroidLifehackBlock(skill.id, reduceMotion)` один раз, сразу после `AndroidBlockList(backBlocks,
reduceMotion)`, вне `if (state.phase == CardPhase.Revealed)`'s style-блоков, но внутри той же
Revealed-ветки — тот же порядок «после back-блоков стиля», что `LifehackWeb.kt`'s
`renderLifehackBlock`. Пусто (`StaticPackLifehackProvider.forSkill(skillId)` — пустой список) →
composable не рисует вообще ничего (`if (lifehacks.isEmpty()) return`), не пустую рамку.

Один `AndroidCollapsible` (уже существующий D4-компонент, `AndroidWidgets.kt`) на каждый
[`Lifehack`], свёрнут по умолчанию; переключатель — обычный `clickable` `Row` с `caption`
(`«Лайфхак · источник: editorial/community»`) как единственным видимым текстом строки и
`Modifier.semantics { stateDescription = "развёрнуто"/"свёрнуто" }` — тот же паттерн, что
`AndroidWhyOnDemandBlock` уже использует для своего toggle, так что TalkBack читает подпись
источника независимо от состояния разворота, как и требует приёмка EN-21. Текст лайфхака и
цитата/ссылка показываются только после разворота (`AndroidCollapsible`), ссылка — обычный
`Text(..., Modifier.clickable { LocalUriHandler.current.openUri(url) })`, подчёркнутый, на **своей
собственной строке** под цитатой: первая попытка (цитата и ссылка в одном `Row`) ломалась визуально
— длинная цитата уже переносится на несколько строк внутри `Text`, забирая себе всю доступную
ширину `Row`, и второму `Text` оставалось несколько `dp`, из-за чего «— источник» переносился по
одной букве на строку (найдено и исправлено живьём на эмуляторе, не только в юнит-тесте — Compose
UI-тест на JVM/Robolectric не ловит this конкретный wrap, т.к. использует ту же ширину, что и
устройство, но скриншот-осмотр — единственный способ реально увидеть перенос).

Почему не переиспользован web'овский `renderLifehackBlock` буквально: разные UI-тулкиты (DOM vs
Compose), тот же паттерн («после back-блоков, коллапс по умолчанию, подпись видна всегда, пусто →
ничего»), а не общий код — ровно то же соотношение, что уже у `AndroidBlockList`/`LifehackWeb.kt`'s
`renderCardBlocks` для остальных 9 `BlockKind`.

Проверено: `:androidApp:testDebugUnitTest` (новый `AndroidLifehackBlockComposeTest`, 3/3 — RED
подтверждён отдельно временной no-op заглушкой перед реализацией, затем GREEN; остальные существующие
android-тесты не регрессировали) зелёный; `:androidApp:assembleDebug` — `BUILD SUCCESSFUL`; живой
смоук на `emulator-5554` (Single-skill режим → `case.gen.neg`, у которого есть pl-ru's EN-20
авторский лайфхак) — блок появляется после «Что изменилось», свёрнут по умолчанию, разворачивается
по тапу, ссылка «Источник» реально открывает `https://www.slavica.com/grammar-of-contemporary-polish.html`
в системном WebView-тестере; скриншоты в `/private/tmp/claude-501/.../scratchpad/en21-android/`.
Не менялись `:core-*`/`:shared`/`:pack-format` — pl-ru byte-identical инвариант не затронут по
построению (ноль правок вне android-хоста). iOS/macOS/desktop — вне скоупа этого лейна (отдельные
worktree-лейны), техдолг остаётся явным, как и было до этой задачи.

Подробно: `Plans/Kotlin/EnRuPackPlan.md` §4.3/§6 (EN-21); ADR-27 (core+web часть той же задачи);
`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidLifehackBlock.kt`;
`kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidLifehackBlockComposeTest.kt`.

## ADR-28 · 2026-09-28 · EN-24 (web): UC-09 часть 2/2 минимум — English-таблица на `MatrixWeb.kt`, `forms.generated.json(en)`

Plans/Kotlin/EnRuPackPlan.md §5 гэп H / §6 EN-24: минимальный слайс — хотя бы одна живая английская
матрица через уже готовый `MatrixTableEngine`/`MatrixTableViewModel` (ADR-21/22), не новый ad hoc
код. Новый `scripts/build-pack-en.mjs` материализует `courses/lang/en/forms.generated.json` из
`lang/en/lexicon.json`'s флэт-полей verb (`present3sg`/`presentSg1`/`presentPl`/`past`/`pastPl`) —
английская морфология не нуждается в декларациях (гэп B, не гэп): person/number почти всегда
инвариантны, лексикон уже несёт весь нужный парадигматический материал; скрипт только раскладывает
его в `Tense×Person×Number` `FeatureBundle`, byte-по-смыслу тот же приём, что `build-pack.mjs`
делает для pl из другой формы источника. Модальный `will` не материализуется как отдельная лексема
(нет собственной парадигмы — используется как инвариантный маркер будущего в `futureForm`).

`kotlin/shared/build.gradle.kts`'s `generateFormsFixtureSource` сканирует `lang/*/forms.generated.json`
(та же схема сканирования, что уже применена к realization.json/exercise-recipes.json, EN-05) в
новый `generatedFormsGeneratedJsonByLang: Map<String,String>`; `polski.core.PackEngine.morphology`
теперь читает `generatedFormsGeneratedJsonByLang[langId] ?: generatedFormsFixtureJson` — pl не имеет
файла по этому пути (`lang/pl/forms.generated.json` не существует, легаси-файл остаётся на
`courses/pl-ru/forms.generated.json`), поэтому pl продолжает читать старую константу нетронуто
(byte-identical, `TrainingParityTest`/`GrammarParityTest` не менялись и остаются зелёными). Новый
`val enMorphology: TableMorphology` (`PlEngine.kt`) — второй `PackEngine("en")`, независимый от
`packRegistry.active` (переключение активного пакета — EN-22, не эта задача); новый
`polski/data/EnLexicon.kt` — типизированные `enVerbs`/`enPersonalPronouns` из уже встроенного
`generatedLexiconJsonByLang["en"]`, тоже не зависят от `active`.

`MatrixWeb.kt`: `renderVerbs` добавляет два новых `MatrixTableEngine`-построения после
существующих pl-таблиц — «English: лицо × время» (фиксированный пример-глагол «see», demonstрирует
неправильный глагол через ту же табличную морфологию, никакого 11-го оператора) и отдельная
«do-support: вопрос и отрицание» таблица (do/does present-раскол, invariant «did»; future-столбец —
факт, не выдумка: у будущего своего do-support нет, вопрос/отрицание строятся через «will» само по
себе). `HTMLElement.matrixTable`/`matrixContrast` получили опциональный `lang` параметр (default
`"pl"`, все существующие вызовы без него — bute-identical); английские ячейки теперь помечаются
`lang="en"`, а не хардкожным `"pl"` (реальный a11y-баг, если бы English-текст читался с польскими
правилами произношения screen reader'ом — исправлен как часть этой задачи, не отдельным поводом).

Новый Playwright `tests/browser/kotlin-en-matrix.spec.ts` (в `playwright.kotlin.config.ts`'s
`testMatch`): проверяет реальные he/she/it→`sees`, неправильное `see→saw`, `will see`, do/does-раскол
и отсутствие `lang="pl"` на английском тексте — против собранного `composeWebCompatibility`
дистрибутива, на js и wasm веток одинаково.

Проверено: `:shared:compileKotlinJs`/`compileKotlinDesktop`, `:composeApp:compileKotlinJs`/
`compileKotlinWasmJs` — `BUILD SUCCESSFUL`; `:shared:desktopTest`/`:core-engine:desktopTest` —
зелёные (`TrainingParityTest` 5/5, `GrammarParityTest` 20/20, `PackEngineTest` 2/2,
`MatrixTableEngineTest`); `npm test` (268/268), `npm run course:validate`, `node
scripts/build-pack-en.mjs --check`, `node scripts/build-pack.mjs --check` (pl golden, 1560 записей).
`composeCompatibilityBrowserDistribution` собран; Playwright `kotlin-parity-matrix.spec.ts`+
`kotlin-matrix-progress.spec.ts`+`kotlin-en-matrix.spec.ts` на js и wasm — по 14 passed / 2
pre-existing failures (идентичные ADR-22's документированному разрыву, не регрессия, проверено
живьём на этой же ветке). Не прогнано: Android/iOS/macOS/desktop — вне лейна этой задачи (web only,
4 хоста — явный техдолг из плана §6).

Почему: план явно ограничивает EN-24 web-минимумом («полное переключение всех 5 хостов —
отдельная последующая задача»); гэп H требует именно «хотя бы одна живая английская матрица», не
полный UI — реализовано буквально этим объёмом, без изобретения нового рендер-пути.

Подробно: `Plans/Kotlin/EnRuPackPlan.md` §5 гэп H, §6 EN-24; `scripts/build-pack-en.mjs`;
`kotlin/shared/src/commonMain/kotlin/polski/core/PlEngine.kt`;
`kotlin/shared/src/commonMain/kotlin/polski/data/EnLexicon.kt`;
`kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt`;
`tests/browser/kotlin-en-matrix.spec.ts`.

## ADR-27 · 2026-09-28 · EN-21 (core+web): `LifehackProvider`-порт + `StaticPackLifehackProvider` + web-блок «Лайфхак»

`Lifehack`/`LifehackSource`/`LifehackStatus` + `fun interface LifehackProvider { fun forSkill(skillId: String): List<Lifehack> }`
(`kotlin/shared/src/commonMain/kotlin/polski/presentation/Lifehack.kt`) — тот же зарезервированный
порт-паттерн, что `AudioProvider`/`ExplanationProvider` (UniversalCorePlan.md §5.4), но с реальной
реализацией сразу: `StaticPackLifehackProvider` (объект, не класс с захваченным `pairId` — читает
`polski.data.packRegistry.active.pairId` заново на каждый вызов, поэтому переживёт будущий EN-22
пикер без переподключения) парсит `pairs/<pairId>/lifehacks.json`, embedded тем же механизмом, что
`pair.json` (`generateCoursePackSource`, `kotlin/shared/build.gradle.kts` → новый
`generatedLifehacksJsonByPairId`). Веб (`kotlin/composeApp/src/webMain/kotlin/polski/ui/CardBlocksWeb.kt`,
`TrainingWebApp.kt`): свой, всегда одинаковый сворачиваемый блок после `back`-блоков стиля (не 10-й
`BlockKind` — ADR-15/EnRuPackPlan.md §4.3 прямо запрещает это), источник —
`StaticPackLifehackProvider.forSkill(skill.id)`; пусто → блок не рендерится вообще. Подпись
`«Лайфхак · источник: <editorial/community>»` видна сразу (не только при раскрытии) — то же
требование §4.3 «нецветовая опора обязательна», перенесённое на атрибуцию.

Почему `forSkill` фильтрует только по точному совпадению `skillId` (не строит отдельный путь для
кросс-скилловых `skillId: null`+`topic`): единственный вызывающий сегодня — карточка одного навыка;
кросс-скилловый рендер вне скоупа EN-21's web-задачи, ничего не обходит контракт схемы (запись с
`skillId: null` просто никогда не совпадёт ни с одним реальным id).

Пакет pl-ru получил веб-блок бесплатно: `packRegistry.active` сегодня всегда pl-ru (EN-22 пикер ещё
не подключён), а `courses/pairs/pl-ru/lifehacks.json` (EN-20, 5 записей) уже непустой для
`case.gen.neg`/`case.inst`/`agreement.my`/`aspect`/`mixed` — это ожидаемое, а не побочное поведение
плана (§4.4: pl-ru's первый набор существует именно чтобы блок было чем проверить уже сейчас), и не
нарушает HARD-инвариант «pl-ru byte-identical» — тот инвариант про генерацию упражнений/ответов
(`TrainingParityTest`/`GrammarParityTest`), не про отсутствие новых информационных блоков в UI.

Подробно: `Plans/Kotlin/EnRuPackPlan.md` §4.2/§4.3/§6 (EN-21); `kotlin/shared/src/commonTest/kotlin/polski/presentation/LifehackTest.kt`.

## ADR-26 · 2026-09-28 · EN-11: `lang/en/{lang,lexicon,prepositions}.json` + `validate-pack-v2.mjs` генерализован на `lang/*`

Первый реальный второй язык в `courses/lang/`. Три новых файла (`lang.json`/`lexicon.json`/новая
лексическая категория `prepositions.json`), новая `morphology-notes.md` (авторские заметки, не
читается кодом/валидатором — то же решение, что уже принято для pl: таблица форм в runtime, правило
только на этапе авторинга) — данные буквально по EnRuPackPlan.md §1.3, ноль правок кода ради
контента (план's критерий приёмки).

**Новые схемы:** `courses/schema/lexicon-v1.schema.json` (первая схема, которая проверяет
`lexicon.json`'s собственную форму независимо от pl-специфичной v1-реконструкции — `nouns`/
`adjectives`/`verbs`/`personalPronouns`/`possessives` обязательны, `forms`' внутренняя форма
намеренно не типизирована жёстко: у pl это глубокие Case×Number(×Gender) таблицы, у en — плоские
`invariant`/Number-only строки, оба валидны для `TableMorphology` без единой правки `:core-engine`,
гэп B плана буквально «это не гэп»); `courses/schema/prepositions-v1.schema.json` (новая категория,
`forms`-ключи — значения языка's `case`, не pl-стиль морфологический падеж).

**`scripts/validate-pack-v2.mjs` генерализован**, а не задокументирован как «останется pl-only»:
скрипт теперь сканирует `courses/lang/*` и для каждого найденного языка схема-проверяет `lang.json`
(уже был generic до этой задачи) + `lexicon.json`/`prepositions.json` (новые схемы) + кросс-чек
«таблица предлогов покрывает ровно `lang.json`'s `case`-значения»; §8.2 кросс-чеки
(`construction`/`focus`/`fixed`/`lexicalFilter` резолвятся) запускаются для языка только если у него
уже есть `curriculum.json` — сегодня только pl (EN-12 для en — следующая задача), это честная
граница, а не тихий no-op под видом «generalized». `checkConstructionsResolve`/`checkFeaturesResolve`/
`checkLexicalFiltersResolve` параметризованы `langCode` для путей в сообщениях об ошибках — для pl
даёт байтово тот же текст ошибки, что и раньше (`tests/pack-v2.test.ts`'s
`rejects a curriculum skill referencing an unregistered construction` не менялся и зелёный).

**pl-ru's v1-мост (реконструкция `lexicon+pair` → `course-pack-v1.schema.json` → `validateCoursePack`)
НЕ генерализован** — сознательно, задокументировано прямо в файле: `course-pack-v1.schema.json`'s
собственный словарь падежных меток (`nom`/`gen`/`dat`/`acc`/`inst`/`loc`/`voc` как ключи объекта, не
значения generic `Case`) и `personalPronouns`'s обязательные `ja`/`ty`/`on`/... — буквально
pl-специфичный legacy-формат (EnRuPackPlan.md §0 пункт 3: «только `curriculum.json` реально
language-agnostic сегодня»); притворяться, что en может пройти через ту же реконструкцию без
`pair.json`/`curriculum.json` (которых у en ещё нет, EN-12/EN-17) и без переписывания самой v1-схемы
— значило бы либо изобрести несуществующие поля, либо тихо не проверить en вообще. En идёт по
чистому v2-пути (EN-04/EN-05, гэп G) — этот ADR его не строит, только не блокирует данными.

**Тесты:** `tests/pack-v2.test.ts`, новый `describe('EN-11: ...')` (5 тестов: lang.json-значения,
`validatePackV2()` не падает на новых слоях, внутренняя консистентность lexicon.json, полное
покрытие ролей в prepositions.json, `validatePackV2()` реально падает на испорченном
`lang/en/lexicon.json` с путём `/lang/en/lexicon.json` в сообщении). RED подтверждён живым прогоном
до создания файлов (`Cannot find module '.../lang/en/lang.json'` — 4/13 упавших, не «не
скомпилировалось», отсутствующая доставка), GREEN после — 13/13.

Лингвист-ревью (носитель en/ru, см. агент-роль этой задачи): все 14 существительных/7
прилагательных/10 глаголов/7 личных местоимений/7 притяжательных проверены вручную —
`morphology-notes.md` фиксирует осознанные отклонения (`be`'s presentSg1/presentPl/pastPl,
`will`'s единственное поле, `your` без отдельного `yourPlural`-аналога, `child→children`/
`wife→wives`/`woman→women` как неправильные формы, не правило).

Проверено: `npm test` — 38/38 файлов, 226/226 тестов (было 221/9 до задачи — +5 EN-11-тестов зелёные,
0 регрессий); `npm run course:validate` — `PASS pack v2`; `npm run typecheck` — та же 1
предсуществующая ошибка (`style-recipe-validation.test.ts`, не эта задача, подтверждено `git stash`
на baseline `bc14bf3`), 0 новых ошибок типов.

Подробно: `Plans/Kotlin/EnRuPackPlan.md` §1.3, §5 (гэпы A/B/C), §6 (EN-11);
`courses/lang/en/{lang,lexicon,prepositions}.json`, `courses/lang/en/morphology-notes.md`,
`courses/schema/{lexicon-v1,prepositions-v1}.schema.json`, `scripts/validate-pack-v2.mjs`,
`tests/pack-v2.test.ts`.

## ADR-25 · 2026-09-28 · UC-09 (часть 2/2, lane macos): macOS + Compose Desktop переключены на `MatrixTableViewModel`
Пять `MatrixTableViewModel`-билдеров в новом `kotlin/shared/src/commonMain/kotlin/polski/presentation/MatrixTables.kt` (`casesFullTable`/`comparisonTable`/`verbsTable`/`personalPronounsTable`/`possessivesTable`) — буквальная транскрипция пяти таблиц, которые desktop's `MatrixScreen.kt` (`CasesDesktop`/`VerbsDesktop`/`PronounsDesktop`) и macOS's `matrixSnapshot` (`MacSnapshot.kt`) до этого строили каждый по-своему вручную поверх одних и тех же `caseRows`/`nounPhrase`/`caseSentence`/`verbForm`/`referenceVerbTeaching`/`referencePronounTeaching`/`possessives` — те же заголовки, те же строки, та же `contrastFrom`-база на столбец, теперь один источник для обоих хостов. Матрицы pipeline-карточек/male-acc grid/chain/русской опоры (ADR-21 boundary) не тронуты — они остаются на прежнем ad-hoc пути на обоих хостах.
Новый `kotlin/shared/src/commonMain/kotlin/polski/presentation/MatrixTableSnapshot.kt`: `MatrixTableViewModel.toJson()` — единый wire-формат (rowHeaderLabel/columnHeaders/rows[{header,cells[{value,contrast?}]}]), `contrastPairJson(ContrastPair)` вынесен из `MacSnapshot.kt`'s `pairSnapshot` (теперь однострочный делегат) как общая форма для *любой* «было → стало» пары на снапшоте, не только матричной.
**Compose Desktop** (`composeApp/src/desktopMain/.../MatrixScreen.kt`): новый приватный composable `MatrixTableView(MatrixTableViewModel)` (тот же layout/ширины столбцов, что у старого ad-hoc `MatrixTable(headers, rows, contrastFrom)`) заменяет пять мест построения таблиц; сам ad-hoc `MatrixTable` остаётся для pipeline/chain/male-acc/русской опоры — не удалён, не единственный путь ещё.
**macOS** (`MacSnapshot.kt`/`PolskiGrammarMacApp.swift`): `matrixSnapshot` кладёт пять сериализованных таблиц (`casesTable`/`comparisonTable`/`verbsTable`/`personalPronounsTable`/`possessivesTable`) вместо ранее вручную собираемого `cases`-массива (удалён, ничем не читался кроме собственного decode); Swift получил один generic `Table`/`Row`/`Cell` (`Decodable`) и один рендерер `matrixTableView(_:)`, использованный для всех пяти таблиц — макOS-приложение **впервые** показывает разделы «Глаголы»/«Местоимения» матрицы (раньше показывался только общий pipeline-фоллбек для любого раздела кроме «Падежи»); раздел «Падежи» переключён с ad-hoc `CaseRow`/`cases` на generic-таблицу.
Приёмка: `MatrixTablesTest` (`:shared` commonTest, 6 кейсов, включая JSON round-trip) сверяет каждый билдер с той же ручной формулой, что раньше писал каждый хост — паритет значений/заголовков/contrast-баз подтверждён кодом, не глазами. Живой прогон: временная (не закоммиченная) подмена дефолтов `AppUiState.tab`/`MatrixSelection.section` + `xcodebuild build` + `screencapture` показала все три раздела (Падежи/Глаголы/Местоимения) на реальном запущенном native macOS приложении с корректной было/стало-подсветкой; тот же приём для Compose Desktop (`:composeApp:run`) показал таблицу падежей. Дефолты возвращены (`git diff` на `AppUiState.kt` пуст) — скриншоты не входят в диф, это была только verification-техника (Accessibility-автоматизация мыши/клавиатуры недоступна в этом окружении — `cliclick`/`osascript` подтверждённо не могут двигать курсор без ручного разрешения в System Settings, которое нельзя выдать без интерактивной сессии). `:shared:desktopTest`/`:shared:macosArm64Test`/`:composeApp:desktopTest` зелёные, `xcodebuild ... build` для `PolskiGrammarMac` — **BUILD SUCCEEDED**.
Не сделано (следующий шаг, не в этой задаче): Android (`AndroidMatrixScreen.kt`), web (`MatrixWeb.kt`) и iOS (`IosSnapshot.kt`) остаются на ad-hoc пути — это отдельные lanes/задачи. chain/русская опора/pipeline/male-acc остаются вне `MatrixTableEngine` на всех хостах (ADR-21 boundary, не изменилось).

## ADR-24 · 2026-09-28 · UC-09 (часть 2/2, iOS): матрица iOS читает MatrixTableEngine/MatrixTableViewModel, wire-контракт не меняется
`kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt`'s `matrixSnapshot` больше не вычисляет ячейки cases/comparison/verbsRows/tenseRows/aspectRows/pronouns/possessives/chainRows вручную (прямые вызовы `nounPhrase`/`caseSentence`/`verbForm`/`changeHighlightParts` по месту) — каждая из этих 8 таблиц теперь строится через `MatrixTableEngine.build(rowAxis, rowHeaderLabel, rowHeader, columns).toViewModel()` (тот же `:core-engine`/`:shared` путь, который `MatrixTableViewModelTest` (ADR-21) уже доказал побайтно идентичным этим же функциям), и снапшот читает `value`/`contrast` с готовых `MatrixTableRow`/`MatrixTableCell`. Wire-JSON, который читает `MatrixView` (SwiftUI), **не изменён** — те же именованные поля на секцию (`phrase`/`phrasePair`, `${tense.id}Pair`, `${gramCase.name}pair`, …), а не общий grid — поэтому ни один Swift-файл не тронут и риск ограничен Kotlin-стороной; `russianSupport`/`systemCards`/`maleAccRows` остаются вне `MatrixTableEngine` (как и зафиксировано в ADR-21 — не таблицы или встроенные построчные сравнения) и не тронуты.
Один намеренный отход от буквальных builder'ов `MatrixTableViewModelTest`: столбец местоимений для контекста `LOC` вычисляет ячейку через ту же host-специфичную подмену (`forms.getValue(GramCase.LOC)` вместо `context.value(id, forms)` с предложным префиксом), которую уже применяют `AndroidMatrixScreen.kt`'s `androidLine`-паттерн и прежний iOS-код — это host-специфичное решение живёт в замыкании `MatrixColumn.cell`, а не как второй путь построения таблицы; общий commonTest-builder намеренно использует «книжный» `context.value` (то, что показывает web), и это расхождение — существовавшее до этой задачи различие вывода между хостами, не внесённое и не устранённое здесь.
RED подтверждён вживую: временная порча `contrastFrom` у столбца «целая группа слов» (NOM→GEN) уронила `IosMatrixSnapshotTest` (`polski.ios.IosMatrixSnapshotTest.generatedMatrixCellsCarryPolishBeforeAndAfter` и другие), откат вернул все 12 тестов зелёными — не «не скомпилировалось», реальный поведенческий RED→GREEN на текущем test suite, который уже проверяет байтовое содержимое `matrix.cases/comparison/verbsRows/pronouns/possessives`.
Три существующих XCUITest (`testNativeCasesShowCompactNoteAndOrderedComparisonNouns`, `testNativeVerbGenderControlChangesSelectedSubjectOnly`, `testNativePronounTeachingShowsCompactContextsAndOwnerDemo`) уже утверждают конкретные значения ячеек (`"Было: robić; Стало: robiłem"`, `"mąż"/"kolega"/"pies"/…`, `"Было: ja; Стало: mnie"`, `"Было: moja piękna żona; Стало: moją piękną żonę"`) — эти значения идут через новый движок и прошли без правок ассертов; к каждому из трёх добавлен один `XCTAttachment(screenshot:)` (`matrix-cases-comparison-uc09`/`matrix-verbs-tense-aspect-uc09`/`matrix-pronouns-possessives-uc09`, тот же паттерн, что уже применяют `testNativeAppearanceSettingsKeepsTrainingCard` и `testSystemMapCardsRenderDashedBeforeSolidAfterInLightAndDarkTheme`) — визуальная опора для скриншот-паритета §12 UC-09, а не новая логика.
Проверено: `:shared:iosSimulatorArm64Test` (полный прогон, все Kotlin/Native iOS-тесты зелёные, включая `IosMatrixSnapshotTest` — 12/12), `:shared:compileKotlinIosSimulatorArm64`/`compileKotlinIosArm64`, `:core-engine:desktopTest`/`:shared:desktopTest`/`:composeApp:desktopTest` (не тронуты этой задачей — подтверждают отсутствие побочных эффектов на другие таргеты) — все `BUILD SUCCESSFUL`. `xcodebuild test` на iPhone 17 Pro Simulator (iOS 26.0), `-only-testing` на восьми Matrix/системных UI-тестах (`testNativeCasesShowCompactNoteAndOrderedComparisonNouns`, `testNativePronounTeachingShowsCompactContextsAndOwnerDemo`, `testNativeVerbGenderControlChangesSelectedSubjectOnly`, `testNativeTrainingMatrixAndProgress`, `testInventoryAuthoredMatrixAndVocabularyCopyOnSimulator`, `testSystemMapCardsShowStepByStepContrastPairs`, `testNativeGeneratedCaseContrastKeepsFullWordsInSemantics`, `testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings`) — **`** TEST SUCCEEDED **`, 8/8, 0 failures** (`Test-PolskiGrammar-2026.09.28_03-34-46-+0200.xcresult`); повторный `xcodebuild test` после добавления screenshot-вложений на трёх из них подтвердил то же 3/3 зелёными со скриншотами `keepAlways`. Не прогнаны: Android/Web/Desktop/macOS (не тронуты этой задачей, iOS-only wiring), физический iPhone/iPad, VoiceOver/Dynamic Type регрессия на матрице (не входили в объём).
Почему: UniversalCorePlan.md §5.3.3/§12 UC-09 требует переключить рендер таблиц на `MatrixTableViewModel` хост за хостом (ADR-21 явно отложила это на «отдельный шаг с собственным риском»); эта задача — тот шаг для iOS. Сохранение прежнего wire-JSON контракта — осознанное сужение риска: задача просит «UI tests; simulator screenshots parity» (не редизайн), а SwiftUI-переписывание всех 8 секций на общий grid-рендерер — отдельная, самостоятельно рискованная работа с иной ценой/выгодой, не часть этого запроса.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §5.3.3, §12 UC-09; `Plans/Kotlin/ContrastHighlightPlan.md` §4; `kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt`; `kotlin/shared/src/iosTest/kotlin/polski/ios/IosMatrixSnapshotTest.kt`; `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift`.

## ADR-23 · 2026-09-28 · UC-09 (часть 2/2, Android): `AndroidCasesSection`/`AndroidVerbsSection`/`AndroidPronounsSection` рендерят `MatrixTableViewModel`
`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt` (UniversalCorePlan.md §5.3.3, задача "AndroidMatrixScreen renders MatrixTableViewModel"): все табличные карточки Android-экрана «Матрица» (падежи + сравнение типов склонения, времена/лица, личные местоимения + владельцы) больше не вызывают `nounPhrase`/`caseSentence`/`verbForm`/`ContrastPair.generated` вручную внутри `forEach`, а строят `MatrixTable` через `MatrixTableEngine.build(rowAxis, rowHeaderLabel, rowHeader, columns)` и читают готовый `.toViewModel()` (`MatrixTableRow.cells[i].value`/`.contrast`). rowAxis на каждую таблицу: `caseRows` (падежи), `comparisonNounIds` (сравнение), `referenceVerbTeaching.subjects` (времена, столбец — тензис), `teaching.pronounIds` (личные, столбец — контекст), `possessives` (владельцы, столбец — падеж демо-фразы) — та же форма row-axis×columns, что уже использует `MatrixWeb.kt`'s приватный `matrixTable`-хелпер (ADR-21), только Android продолжает карточный, не табличный layout (это решение экрана, не движка) — каждая строка остаётся своей `AndroidInfoCard`, столбцы — Text/ContrastPairText внутри неё, без визуальных изменений.
**Область.** Только `polski.ui.screens.AndroidMatrixScreen` (три `private fun` стали `fun`, как `AndroidCaseReference`/`AndroidSystemMapCard`, чтобы Compose-тест из `androidApp` мог их вызвать напрямую). `AndroidMapSection` (article-блоки/pipeline-карточки/male-acc grid) и отдельный `AndroidCaseReference` в `AndroidTrainingScreen.kt` (карточка "Таблица этого предложения" с anti-leak маскированием целевой строки, ContrastHighlightPlan.md §5) вне области — не таблицы либо другой контракт (сокрытие ответа), не относящийся к UC-09.
**Приёмка (screenshot parity — "те же значения в тех же ячейках").** Refactor-under-green: `androidApp/src/test/java/dev/polski/grammarmatrix/AndroidMatrixTableParityTest.kt` — три Compose/Robolectric теста, вычисляющие ожидаемые значения теми же функциями (`nounPhrase`/`caseSentence`/`verbForm`/`context.value`/`teaching.demo.phrase`), что и раньше вызывал экран, и ищущие их по `contentDescription`/тексту в дереве (допуская дубликаты — польский синкретизм, например Dat==Loc, законно повторяет одну и ту же пару, как уже документирует `AndroidCaseReferenceLeakTest`). Тест написан и прогнан ДО рефакторинга (GREEN на старой реализации — baseline), затем реализация переключена на `MatrixTableEngine`, тест прогнан снова (GREEN) — фактическое доказательство паритета, не переписанный руками ожидаемый вывод. Полный `:androidApp:testDebugUnitTest` (все существующие Compose/Robolectric-тесты экрана, включая anti-leak) остался зелёным; `:shared:allTests`/`:core-engine:allTests` не задеты (правка только в `composeApp/androidMain`). Дополнительно — визуальная проверка на эмуляторе `Polski_ARM35` (API 35 arm64): скриншоты Cases/Verbs/Pronouns после переключения совпадают по структуре и значениям с ожидаемой картой экрана (`Plans/Kotlin/artifacts/android/uc09-after-*.png`).
**Не сделано.** web/desktop/iOS/macOS ещё не переключены (часть 2/2 остаётся открытой для остальных хостов — параллельна по хостам, см. UniversalCorePlan.md UC-09 row).

## ADR-22 · 2026-09-28 · UC-09 (web, часть 2/2): `MatrixWeb.kt` переключён на `MatrixTableEngine`+`MatrixTableViewModel`
Все 9 таблиц страницы «Матрица» (`renderMap`: цепочка, русская опора; `renderCases`: основная и сравнение склонений; `renderVerbs`: спряжение, «время меняется», вид; `renderPronouns`: личные, притяжательные) больше не строят `headers`/`rows: List<List<String>>` вручную — каждая собирает `MatrixTableEngine.build(rowAxis, rowHeaderLabel, rowHeader, columns).toViewModel()` (те же самые `rowAxis`/`columns`/`contrastFrom`, что уже проверил `MatrixTableViewModelTest` из ADR-21, буквально скопированные в вызывающий код, не переизобретённые). Приватный `HTMLElement.matrixTable(headers, rows, tableClass, contrastFrom, renderCell)` заменён на `HTMLElement.matrixTable(vm: MatrixTableViewModel, tableClass, renderCell)` — он больше не вычисляет `ContrastPair` сам (`before/value` + `contrastFrom`-лямбда), а просто читает уже готовый `MatrixTableCell.value`/`.contrast` из вьюмодели и передаёт `renderCell` те же `(row, column)`-индексы, что раньше (для русской опоры индекс её единственного «конструкция»-столбца сдвинулся с 1 на 0, т.к. вьюмодель больше не включает заголовочный столбец в `cells`).
Вне области (не тронуто, как и в части 1/2): системные pipeline-карточки (`renderSystemCardSteps`) и дерево решений мужского Biernik (`maleAccRows`) — не табличные секции; `renderCaseReferenceWeb` (таблица текущего упражнения на карточке) — отдельная, маскирующая ответ до reveal логика (Emphasis contract §5), не входит в перечень 9 таблиц матрицы и не упомянута в плане UC-09 как объект замены.
**Паритет подтверждён без снапшот-диффинга.** Значения ячеек уже исчерпывающе проверены на уровне `:shared`/`:core-engine` (`MatrixTableViewModelTest` 9/9, `MatrixTableEngineTest`), поэтому доказывать нужно было только «`MatrixWeb.kt` реально вызывает эти же построения» — сделано построчным переносом того же кода column-функций/`contrastFrom` из теста в `MatrixWeb.kt`. Дополнительно: собран `wasmJsBrowserDistribution`/`jsBrowserDistribution`, прогнан `tests/browser/kotlin-parity-matrix.spec.ts` + `kotlin-matrix-progress.spec.ts` на обоих (12 passed на каждом); 2 теста (`tense comparison`/`aspect form`, оба про `.change-before`/`.change-after` в двух конкретных таблицах) падают **одинаково на HEAD-версии файла до этой правки** (проверено вживую: временно вернул `git show HEAD:...MatrixWeb.kt`, пересобрал, тот же прогон — те же 2 падения, тот же `error-context.md`) — предсуществующий, не внесённый и не исправленный этой задачей разрыв где-то в `ContrastPair.generated`/раскладке слов для этих двух конкретных пар предложений (`Moja piękna żona idzie do domu.`→`...szła do domu.` и `robić`-таблица), не в переключении на вьюмодель.
Проверено: `:composeApp:compileKotlinJs`/`compileKotlinWasmJs` — `BUILD SUCCESSFUL`; `:shared:desktopTest`/`:core-engine:desktopTest --tests "*MatrixTable*"` — зелёные (9+3); Playwright `kotlin-parity-matrix.spec.ts`+`kotlin-matrix-progress.spec.ts` на wasm и js веток — 12 passed / 2 pre-existing failures (идентично до и после правки, см. выше). Не прогнано: Desktop/Android/iOS/macOS — они не входили в область этой задачи («web files only»).
Почему: план §5.3.3/§12 UC-09 явно допускает «по хостам параллельно»; риск минимизирован переносом уже проверенных построений один в один, а не переписыванием логики заново в `MatrixWeb.kt`.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §5.3.3, §12 UC-09; `kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt`; ADR-21 (`MatrixTableViewModelTest`).

## ADR-21 · 2026-09-28 · UC-09 (часть 1/2): `MatrixTableEngine`+`MatrixTableViewModel` — генерик-движок таблиц, хосты не тронуты
Новый `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/MatrixTableEngine.kt`: `MatrixTableEngine.build(rowAxis, rowHeaderLabel, rowHeader, columns: List<MatrixColumn<R>>): MatrixTable` — чистый generic-«крест» строкового измерения с фиксированным списком столбцов (`MatrixColumn<R>(header, cell: (R)->String, contrastFrom: ((R)->String?)? = null)`), без единого языкового/пакетного факта; `NoLanguageLiteralsTest` (уже существующий guard `:core-engine`) прошёл без правок. Это буквальное обобщение приватного `HTMLElement.matrixTable(headers, rows, contrastFrom, renderCell)` из `MatrixWeb.kt` — почти все секции страницы «Матрица» (падежи, глаголы, местоимения, цепочка и русская опора в «карте системы») уже сегодня строятся именно через этот один приватный хелпер с ровно этой формой (row-axis × column-list, опциональная база для «было → стало»); карточки системного pipeline и дерево решений мужского Biernik — не таблицы (article-блоки), поэтому вне области `MatrixTableEngine`.
Новый `kotlin/shared/src/commonMain/kotlin/polski/presentation/MatrixTableViewModel.kt`: `MatrixTable.toViewModel()` навешивает `ContrastPair.generated(...)` (тот же путь, что и все остальные «было → стало» поверхности) на ячейки, чей столбец объявил `contrastFrom` — раскладка на «что движок знает» (`:core-engine`, только текст) и «что решает подсветку» (`:shared`/presentation, тот же `ContrastPair`, который `EndingHighlight.kt` уже применяет везде) сохранена буквально по контракту выделения. `:core-presentation` как отдельный Gradle-модуль не создан (план допускает «или shared по §4» — не было отдельной код/API-границы, оправдывающей 7-таргетный модуль под один файл; `:shared` уже зависит на `:core-engine` как `api`).
**Ни один хост не тронут** (задача явно просила «no host changes yet»): `MatrixWeb.kt`/`MatrixScreen.kt`/`AndroidMatrixScreen.kt`/iOS-macOS matrix-снапшоты не импортируют новые типы, `git diff --stat` — только 4 новых файла, ни одной правки существующего. Приёмка проверена тестом `MatrixTableViewModelTest` (`:shared` commonTest, 9 кейсов) — исчерпывающее сравнение (не мок, реальные `caseRows`/`nounPhrase`/`caseSentence`/`comparisonNounIds`/`referenceVerbTeaching`/`verbForm`/`referenceTenseRows`/`referenceAspectRows`/`referencePronounTeaching`/`possessives`/`referenceChainRows`/`referenceRussianSupport`) даёт движку ровно те же headers/rows/contrastFrom, что сегодня вручную строит `renderCases`/`renderVerbs`/`renderPronouns`/`renderMap` в `MatrixWeb.kt` (сам файл не читается тестом — он `webMain`-only и недоступен из `commonTest`, поэтому ожидаемые значения — те же вызовы тех же грамматических функций, переписанные в тесте, а не импорт приватного кода) — **кроме** русской опоры (`referenceRussianSupport`), где это верно только для двух текстовых столбцов (construction/check); построчные `RussianSupportRow.comparisons` (встроенные примеры «было → стало» внутри construction-ячейки, `MatrixWeb.kt`'s `renderCell`) вне области, как и pipeline-карточки/male-acc grid (см. следующий абзац) — `MatrixColumn`/`MatrixCell` несут не более одного текста + одной опциональной `contrastFrom`-базы на ячейку, не список готовых `ContrastPair`. RED подтверждён вживую: сломанная строчка (`MatrixCell(column.cell(row), null)` вместо `column.contrastFrom?.invoke(row)`) уронила 8 из 9 тестов, откат вернул зелёный — не «не скомпилировалось», настоящий поведенческий RED→GREEN.
Не сделано в этой задаче (часть 2/2, следующий шаг): рендер таблиц ни на одном хосте не переключён на `MatrixTableViewModel` — `MatrixWeb.kt`/desktop/Android/iOS/macOS продолжают строить DOM/Compose/SwiftUI-таблицы напрямую; системные pipeline-карточки и male-acc decision-grid (не табличные секции) вообще не входят в `MatrixTableEngine` — для них уже есть отдельная, специфичная разметка цепочки (`renderSystemCardSteps`, ContrastHighlightPlan.md §4 «стрелки карты системы: в данных цепочка шагов»), не связанная с этой задачей. Тем же образом (документированное исключение, не молчаливый пропуск) вне области — построчные `RussianSupportRow.comparisons` русской опоры: тест `russianSupportTableCoversOnlyThePlainConstructionAndCheckTextNotTheEmbeddedComparisons` (переименован после code review) явно фиксирует границу и проверяет `require(support.rows.all { it.comparisons.isNotEmpty() })`, чтобы исключение было привязано к реальным данным, а не забыто.
Коррекция (тот же день, до пуша): ревью нашло, что тест `aspectTableOmitsContrastOnlyForARowWithNoPresentTense` сравнивал с захардкоженным `"—"` вместо реального `courseAspectNoPresent.compact` (`"Нет настоящего времени"`, см. `CourseInventoryContentTest`) — тавтология, не паритет. Исправлено импортом реальной константы; RED-инъекция (вернуть `"—"` только в cell-функцию столбца, оставив реальную константу в assert) подтверждена вживую — тест падает, значит теперь действительно сверяет с паковыми данными.
Проверено: `./gradlew :core-engine:desktopTest :shared:desktopTest` (RED-инъекция и откат вживую, затем чистый прогон — все зелёные, `:shared` — 286/286); `:shared:compileKotlinJs`/`compileKotlinWasmJs`/`compileKotlinMacosArm64`, `:core-engine:compileKotlinJs`/`compileKotlinWasmJs` — все `BUILD SUCCESSFUL`. Не прогнаны: androidApp/iOS-sim/macOS-native тесты, Playwright, полный `xcodebuild` — не нужны, поскольку ни один хост-файл не изменён этой задачей.
Почему: план §4.1/§5.3.3 отдаёт `MatrixTableEngine` в `:core-engine`, `MatrixTableViewModel` — в будущий `:core-presentation`, но задача прямо разрешает «или shared», а создание Gradle-модуля под один файл без второй код/API-границы противоречило бы module-architecture («Gradle-модуль — только на реальной код/API-границе»). Переключение хостов — отдельный шаг с собственным риском (4 живых рендерера + iOS/macOS снапшоты), лучше отдельным PR по образцу UC-08's parity-gate.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §4.1, §5.3.3, §12 UC-09; `Plans/Kotlin/ContrastHighlightPlan.md` §4; `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/MatrixTableEngine.kt`; `kotlin/shared/src/commonMain/kotlin/polski/presentation/MatrixTableViewModel.kt`; `kotlin/shared/src/commonTest/kotlin/polski/presentation/MatrixTableViewModelTest.kt`.

## ADR-20 · 2026-09-28 · UC-12: schema v2 (core/lang/pair) — Node-слой, `course.json` не переезжает
Новые Node-скрипты `scripts/migrate-v1-to-v2.mjs`/`scripts/validate-pack-v2.mjs` + 6 новых схем (`courses/schema/core-{features,constructions,template-ops,exercise-kinds}-v1.schema.json`, `lang-pack-v2.schema.json`, `pair-pack-v1.schema.json`) и новые файлы `courses/core/{features,constructions,template-ops,exercise-kinds}.json`, `courses/lang/pl/lang.json`, `courses/lang/pl/lexicon.json`, `courses/pairs/pl-ru/pair.json`.
`migrate-v1-to-v2.mjs` разбивает `courses/pl-ru/course.json` ровно по `LEXICON_KEYS` (`nouns/adjectives/verbs/personalPronouns/possessives/morphology/stemAlternations` → `lang/pl/lexicon.json`; всё остальное → `pairs/pl-ru/pair.json`) — механическое разбиение без переименования/переинтерпретации полей; `reconstructCoursePack(lexicon, pair)` восстанавливает исходный `course.json` (проверено `deepStrictEqual`, `--check`, и тестом `tests/pack-v2.test.ts`). **`courses/pl-ru/course.json` НЕ переименован и НЕ удалён** (план §6) — он остаётся живым источником для `generateCoursePackSource` (Gradle) и `src/data/course.ts` (React); ни один путь, который сканирует Gradle (`coursesDirectory`/`langDirectory`/`stylesDirectory`, `kotlin/shared/build.gradle.kts:7-24`), не тронут, поэтому golden-фикстуры и весь Kotlin-матрикс — вне области риска этой задачи (подтверждено: все затронутые Gradle-таски `UP-TO-DATE`/зелёные без единой Kotlin-правки).
`courses/core/{constructions,template-ops,exercise-kinds}.json` — не производные `course.json` (там таких понятий нет), а формализация уже существующих фактов: 6 CoreStructure + `core.composite` из плана §2 буквально; операторы `order/when/agree/gov/optional` помечены `implemented:true` (реально применяются в `ConstructionRealizer.kt`), `periphrasis/prependAppend/rewrite/alt/punct` — `false` (зарезервированы §5.2, 11-й оператор без ADR не добавляется); `exercise-kinds.json` — `transform`/`chain` `true` (`ExerciseGenerator.generateForSkill`/`generateChain`), `fill-slot`/`translate-L1->L2`/`recognize` `false` — то же самое резервирование, на которое уже ссылается комментарий в `Recipe.kt`. `core/features.json`/`lang/pl/lang.json` описывают ровно тот словарь значений (`Case: Nom/Gen/Dat/Acc/Inst/Loc/Voc`, `Tense: Pres/Past/Fut`, …), который реально встречается в `courses/lang/pl/curriculum.json` — не словарь `kotlin/shared/.../model/Grammar.kt`'s `GramCase`/`Tense` (отдельный, более старый, lowercase-код-путь, не источник для этого файла).
`validate-pack-v2.mjs` — cross-checks §8.2: `Skill.construction` ∈ `core/constructions.json`; каждое значение `focus`/`fixed` ∈ `core/features.json` И ∈ `lang.json.usesFeatures`; `lexicalFilter.where` (`gender`/`nounId`) резолвится в реальный лексикон; плюс восстановленный `lexicon+pair` заново проверяется существующей `course-pack-v1.schema.json` и `validateCoursePack` (переиспользование, не дублирование инвариантов). `pair-pack-v1.schema.json` — лёгкая структурная схема (не копирует всю глубину `course-pack-v1`), потому что глубокую проверку содержимого делает именно этот повторный прогон `validateCoursePack` на реконструкции. Встроено в `npm run course:validate` (`&& node scripts/validate-pack-v2.mjs`); новые самостоятельные скрипты `pack:migrate:v2`/`pack:migrate:v2:check`/`pack:validate:v2`.
Осознанно не сделано в этой задаче: Kotlin-загрузчик `CoursePackLoader`, который читал бы v2-слои в рантайме — `kotlin/pack-format/.../CoursePackSource.kt`'s собственный комментарий прямо называет его «не построен пока» (план §6); это отдельная будущая задача, а не часть UC-12 (которая — «расщепление файла», отдельно от «переезда движка на loader», по тому же §6). Не перенесены (осознанно, чтобы не плодить риск): `courses/styles/*.json` (уже данные, план явно говорит «styles stay data» — не трогать) и `courses/pl-ru/vocabulary-editorial.json` (уже пар-скоуп по месту, перенос увеличил бы список задетых Node-скриптов без пользы для этой задачи).
Проверено: `npm run course:validate`/`pack:migrate:v2:check`/`pack:validate:v2` — зелёные; `npm test` (38 файлов/220 тестов, включая новый `tests/pack-v2.test.ts`) — зелёный; `npm run course:inventory:check`/`course:inventory:decisions:check`/`pack:build:check` — зелёные; `./gradlew :core-engine:desktopTest :core-model:desktopTest :shared:desktopTest --rerun` — `BUILD SUCCESSFUL`, все задачи `UP-TO-DATE`/выполнены без Kotlin-правок (ожидаемо, раз ни один Gradle-вход не менялся). `npm run typecheck` показывает одну ошибку (`assertNoDuplicateBlockKinds` не экспортируется) — воспроизведена на чистом checkout БЕЗ единой правки этой задачи, то есть предсуществующий баг, не внесённый и не исправленный здесь.
Не сделано в этой сессии (за пределами Node-слоя, вне риска для golden-фикстур, но не проверено вживую): полный нативный матрикс (JS/Wasm/iOS-sim/macOS Kotlin-тесты, `androidApp` test+assemble, `xcodebuild` iOS/macOS) и fast-forward lane worktrees (`android`/`ios`/`macos`/`content`) — не запущены, так как ни один файл, который читает Gradle/Xcode/Android, не изменён этой задачей; следующий шаг (тестер/DevOps) может прогнать их как формальность или доверять этому рассуждению.
Почему: план §6 явно требует не переносить/не удалять `course.json` до подтверждённого паритета движка на выходе, а §12 UC-12 — именно «расщепление файла», отдельно от переезда загрузчика; минимизация риска — ни одна Kotlin/Gradle/Xcode входная точка не тронута.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §3.1, §6, §8.2, §12 UC-12; `scripts/migrate-v1-to-v2.mjs`, `scripts/validate-pack-v2.mjs`; `courses/core/*`, `courses/lang/pl/{lang,lexicon}.json`, `courses/pairs/pl-ru/pair.json`; `tests/pack-v2.test.ts`.

## ADR-19 · 2026-09-28 · UC-08: `ExerciseGenerator`/`TableMorphology` — единственный движок; `ExerciseFactory`/`GrammarEngine` удалены
Переключение, не новая архитектура: `:shared`'s `commonMain` теперь зависит на `:core-engine` (`api`, было только `commonTest`); `forms.generated.json`/`realization.json`/`exercise-recipes.json` и парсер `RecipeLoader.kt` переехали из `commonTest` в `commonMain` (были «тест-only до UC-08» с самого UC-05/07). Новый `kotlin/shared/src/commonMain/kotlin/polski/core/PlEngine.kt` — единственное место, где pl-ru данные (`plCurriculum`, `courseSentenceSeeds`, `forms.generated.json` и т.д.) собираются в живой `ExerciseGenerator`/`ConstructionRealizer`/`TableMorphology` (`plExerciseGenerator(random, ids)`, `plChainSteps(nounId)`, `plMorphology`) — то же, что раньше делал тест-only `UniversalCoreHarness.kt`.
`polski/training/ExerciseFactory.kt` удалён; `polski/training/PlExerciseEngine.kt` — тонкая обёртка над `plExerciseGenerator`, с **той же публичной сигнатурой** (`generateForSkill(skillId, preferredSeed): Exercise`, `generateChain(seed): List<Exercise>`), поэтому `TrainingStore` и все 5 хостов (`AndroidSessionViewModel`, desktop `Main.kt`, web `TrainingWebApp.kt`, `IosSession`, `MacSession`) поменяли только имя типа/импорт конструктора (`ExerciseFactory(RandomSource{...}, ExerciseIdFactory{...})` → `PlExerciseEngine(...)`, порты теперь из `polski.core.engine`, не из `polski.training`) — ни один вызывающий код не изменил форму вызова.
`polski/grammar/GrammarEngine.kt` (94-строчный `verbForm` с ручными суффиксами `em/eś/m/ś/śmy/ście`, `będę+infinitive`) удалён; новый `polski/grammar/PackMorphology.kt` в том же пакете реализует те же публичные функции (`nounForm`/`adjectiveForm`/`possessiveForm`/`possessiveMy`/`nounPhrase`/`caseSentence`/`capitalize`) через `plMorphology.form(...)` — то есть matrix/reference-экраны (`MatrixWeb.kt`, `MatrixScreen.kt`, `AndroidMatrixScreen.kt` и снапшоты iOS/macOS), которые уже вызывали эти функции напрямую (не через класс `GrammarEngine`), не потребовали правок. `verbForm` сохраняет явную проверку `PRESENT+PERFECTIVE → error(...)` (языковой факт, не восстановим из отсутствующей строки таблицы без явной проверки) — единственная оставшаяся ручная логика, всё остальное — `plMorphology.form("verb:$id", bundle)`.
Удалены как исполнившие свою (переходную) роль: `UniversalCoreHarness.kt`/`UniversalCoreParityTest.kt`/`UniversalCoreExhaustiveTest.kt`/`generate_uc07_parity.py` — их работа была «живой `ExerciseFactory` против живого `ExerciseGenerator`» (ADR-18), которая теряет смысл без живого `ExerciseFactory`. Постоянные golden-guards на замену: `TrainingParityTest`/`GrammarParityTest` (буквальные `Exercise`/форма-ожидания из `tests/fixtures/kotlin-parity`, React-эталон) и `TableMorphologyParityTest`/`CourseMorphologyTest` (исчерпывающий паритет таблицы форм) — они не сравнивали два движка, они и раньше проверяли текущую реализацию против пиновых значений, поэтому просто остались (без изменений или с точечной заменой импорта портов) и теперь охраняют новую реализацию тем же способом.
Проверено: `:shared`/`:composeApp` desktopTest, `:shared` jsBrowserTest/wasmJsBrowserTest/macosArm64Test/iosSimulatorArm64Test, `:androidApp` testDebugUnitTest/assembleDebug, `npm test`/`npm run course:validate`, Playwright chromium (wasm+js) `kotlin-style-blocks`/`kotlin-flip-card`/`kotlin-ux4`/`kotlin-training`/`kotlin-matrix-progress` — все зелёные; `xcodebuild` iOS simulator + macOS app build — `BUILD SUCCEEDED`. Три Playwright-кейса (`kotlin-parity-chain.spec.ts` P02, `kotlin-parity-matrix.spec.ts` ×2) воспроизведены как уже красные на `git worktree` с HEAD **до** этой правки (детерминированно, не флейк) — известный, не внесённый этой задачей разрыв React/Kotlin по `case.gen.neg`/tense-matrix contrast-разметке, вне объёма UC-08.
Почему: план §5.3/§12 UC-08 — «переключение без изменения вывода»; сигнатурная совместимость (не переименование хостов) минимизирует площадь риска до 100% механических правок, проверяемых компилятором на каждом таргете, вместо содержательного рефакторинга каждого хоста. Matrix-таблицы хостов (UC-09, `MatrixTableEngine`) не переписаны — задача просила формы через `TableMorphology`, что уже верно (те же функции теперь табличные), рендер таблиц — отдельная задача.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §5.3, §12 UC-08; `kotlin/shared/src/commonMain/kotlin/polski/core/PlEngine.kt`, `polski/grammar/PackMorphology.kt`, `polski/training/PlExerciseEngine.kt`; `kotlin/shared/build.gradle.kts`.

## ADR-18 · 2026-09-28 · UC-07: `ConstructionRealizer`/`TemplateInterpreter`/`ExerciseGenerator` в `:core-engine`, данные-рецепты вместо `when(skillId)`
Три новых типа в `:core-engine` (`ConstructionRealizer.kt`, `Recipe.kt`, `ExerciseGenerator.kt`), все зависят только от `:core-model` + порты (`RandomSource`/`ExerciseIdFactory`/`PackCopy`/`PackPattern`/`CasePrefix`/`PronounForms`/`TextCase`/`LexemeFeatures`) — ни одного языкового слова в исходниках, проверено новым `NoLanguageLiteralsTest` (JVM-only, сканирует `:core-engine`'s `commonMain/**/*.kt` на диакритику/кириллицу).
`ConstructionRealizer` — `order`+`gov`+`agree` из §5.2 буквально: `ConstructionTemplate.slots` (упорядоченный список, `optional`, `requiredFeatures[When]`, `agreementSource`), `govFeature/govDefault/govWhen` (условие `"Key=value"`). Морфология уже полностью табличная (UC-05) — оператору `periphrasis`/`rewrite` в pl-данных сейчас применения не нашлось (будущее время уже материализовано как готовая строка в `forms.generated.json`), они зарезервированы, не реализуются.
`ExerciseGenerator` заменяет `when(skillId)` (`ExerciseFactory.kt:49-136`) одним интерпретатором `SkillRecipe` (`TextValue`/`TextSpec`/`ReasonSpec`/`ChangeSpec`/`OwnerDraw`/`StaticExercise` — общая форма для всех 16 skill; `aspect` — «авторский override целой фразой», §5.2's escape hatch; кандидаты фильтруются generically через `SkillSpec.lexicalFilter`, а не жёстко закодированным списком nounId на скилл). Данные — NEW `courses/lang/pl/{realization,exercise-recipes}.json` (не только `curriculum.json`, которого одного оказалось недостаточно — нужна ещё wiring-таблица prompt/pattern/reason-key на скилл, которую план резервирует как `core/exercise-kinds.json`, но не специфицирует формат; здесь — конкретная реализация для pl, схема не запечатана). Встроены в Kotlin тест-only тем же приёмом, что `forms.generated.json` (`generateFormsFixtureSource`, `shared/build.gradle.kts`); парсер — `kotlin/shared/src/commonTest/kotlin/polski/core/RecipeLoader.kt` (manual `JsonElement`, не `:core-engine` — пока тест-only).
**Parity-gate: побайтное сравнение живого `ExerciseFactory` с живым `ExerciseGenerator`, не литералы из фикстуры.** `UniversalCoreParityTest` (генератор `generate_uc07_parity.py`, 112 core-golden кейсов) и `UniversalCoreExhaustiveTest` (16 skills × 12 seeds × 8 draws + полная цепочка × 12 seeds × 3 draws, вручную) строят `legacyFactory(draws)`/`uc07Generator(draws)` с ОДНИМ и тем же `Draws` и сравнивают `Exercise` напрямую — оба зелёные на JVM/JS/Wasm/iOS-sim/macOS. Причина отхода от литерал-транскрипции: `tests/fixtures/core-golden/exercises.json` **устарел** для `case.gen.neg` — у него 1 `FormChange` там, где живой `ExerciseFactory`/`TrainingParityTest` (актуальный, зелёный) даёт 2 (нет записи о вставке частицы `nie`); подтверждено на всех 15 задетых кейсов (12 цепочек `C-*-03` + 3 `E-case.gen.neg-*`). Фикстура не тронута (заморожена по README до UC-08) — расхождение зафиксировано здесь как известный факт, не решено самостоятельно.
Почему: §5.3 требует именно параллельный побайтный прогон, не статичное сравнение с текстом; живая проверка не может «протухнуть» и вскрыла реальный баг устаревшей фикстуры, который литерал-транскрипция замаскировала бы (пришлось бы либо копировать чужую ошибку, либо тихо чинить фикстуру).
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §5.1-§5.3, §12 UC-07; `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/{ConstructionRealizer,Recipe,ExerciseGenerator}.kt`; `courses/lang/pl/{realization,exercise-recipes}.json`; `kotlin/shared/src/commonTest/kotlin/polski/core/*`.

**Правка (тот же день, code review):** ревью нашло два оставшихся Kotlin-литерала в `:core-engine` `commonMain` того же класса, что и принцип №3 запрещает — `ExerciseGenerator.lexicalSlotsFor` жёстко кодировал id глагола (`"go"`) и владельца по умолчанию (`"my"`) для *любого* skill, не только verb.*; `TextSpec.Prefixed` жёстко кодировал терминальную пунктуацию (`"!"`/`"."`) вместо зарезервированного §5.2 `punct`. Оба литерала — ASCII, поэтому проходили старый `NoLanguageLiteralsTest` (сканирует только диакритику/кириллицу) незамеченными. Исправление: `SkillRecipe`/`ChainStepRecipe` не меняются (владелец/глагол были и остаются вне их полей), но `exercise-recipes.json` получил два новых pack-level поля — `defaultOwnerLexeme`/`verbLexeme` — которые `RecipeSet`/`ExerciseGenerator` принимают как обычный порт-параметр конструктора (никакого Kotlin-default); `TextSpec.Prefixed` получил обязательное поле `punct` (без default), заполненное в JSON для всех 4 мест использования. `NoLanguageLiteralsTest` дополнен вторым тестом (`commonMainContainsNoHardcodedLexemeOrPunctuationLiteral`) — ищет по исходнику (без комментариев) буквальные `"go"`/`"my"`/`"!"`, чтобы регресс не прошёл незамеченным снова. `ChainStepRecipe.ownerOut` и `RecipeLoader`'s `toChainStep`/`toTextSpec` тоже избавлены от Kotlin-default `"my"` — теперь обязательное поле, раз в JSON оно и так всегда присутствует. Паритет (`UniversalCoreParityTest`/`UniversalCoreExhaustiveTest`) перепрогнан на JVM/JS/Wasm/iOS-sim/macOS — 0 отказов, побайтное совпадение не нарушено (значения те же, просто теперь из данных, а не из кода).

## ADR-17 · 2026-09-27 · UC-06: `lang/pl/curriculum.json` (16 SkillSpec) как проверяемая копия `ExerciseFactory`, не замена
`SkillSpec` (`:core-model`, UC-01) получил `lexicalFilter: LexicalFilter?` (`slot` + `where: Map<String, List<String>>`) — свойство кандидата и список допустимых значений, например `case.acc.f` → `{"gender": ["f"]}`, `case.inst` → `{"nounId": [...]}`. Новый `courses/lang/pl/curriculum.json` несёт все 16 skill ID с `focus`/`fixed`/`lexicalFilter`; `focus`/`lexicalFilter` для всех 16 сверены тестом с реальными параметрами/условиями веток `ExerciseFactory.kt:49-136`. `fixed` — тоже реальный параметр ветки (и проверяется как таковой) только для `verb.present`/`verb.past`/`verb.future` (`Person`/`Number` буквально передаются в `verbForm`), `sentence.plural` (`Case`, буквальный `GramCase.ACC` в `phrase`, `:127`) и `agreement.my`/`pronouns` (`Case`, тоже буквальный `GramCase.ACC` — в `phrase` `:93` и в `personalPronouns[...].getValue` `:119`); для оставшихся 10 (`case.acc.*`, `case.gen.neg`, `case.inst`/`case.loc`/`case.dat`, `agreement.my`, `pronouns`, `sentence.question`, `aspect`) ветки вызывают `nounPhrase`/`caseSentence`/`phrase`/`sentence`, которые вообще не принимают Tense/Person/Number — `Tense: Pres, Person: 1, Number: Sing` там снят с жёстко закодированного текста паттерна `"seenAcc"` ("Widzę {acc}."), у `sentence.question` то же самое, но `Person: 2` — со своего паттерна `"questionSource"` ("Widzisz {acc}."), а `aspect`'s — с его copy-строк; тест сверяет это со строками-источником, а не выдаёт за факт кода. `level`/`prerequisites` не задублированы — файл читается и сравнивается с `skillById(...)` из уже загруженного `course.json`. Файл встроен в Kotlin тем же приёмом, что `course.json`/`styles/*.json` (`generateCoursePackSource` в `kotlin/shared/build.gradle.kts` сканирует `courses/lang/*/curriculum.json`), читается `polski.training.plCurriculum`/`parseCurriculum` и покрыт `CurriculumReaderTest` (12 тестов) на JVM/JS/Wasm/iOS-sim/macOS. Схема — `courses/schema/curriculum-v1.schema.json`, валидатор — `scripts/validate-curriculum.mjs`, встроен в `npm run course:validate`.
`ExerciseFactory` остаётся единственным живым путём генерации упражнений — `plCurriculum` не читается runtime-кодом хостов, это данные для будущего `ExerciseGenerator` (UC-07). Для `agreement.my` (случайный владелец из 6), `pronouns` (замена именной группы местоимением), `aspect` и `mixed` (составные изменения ≥2 осей одновременно) `focus = null` — код не делает единственный flip одного признака, поэтому значение не выдумывается.
Почему: план требует данные, покрывающие все 16 ID «с тем же `focus`/`fixed`/`lexicalFilter`, что текущие ветки», без замены `ExerciseFactory` до параллельного parity-gate (UC-07). `GrammarEngine`/морфология не тронуты — UC-05 (ADR-16) слит параллельно.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §3.1-§3.2, §5.3, §12 UC-06; `kotlin/core-model/src/commonMain/kotlin/polski/core/model/FeatureBundle.kt`; `kotlin/shared/src/commonMain/kotlin/polski/training/Curriculum.kt`.

## ADR-16 · 2026-09-27 · UC-05: `:core-engine` (Morphology/TableMorphology) + `forms.generated.json`
Новый модуль `:core-engine` (через `polski.kmp-common`, зависит только от `:core-model`) вводит `Morphology`
(`fun form(lexeme, bundle): String`) и `TableMorphology` — чистый lookup, без правил в рантайме. `scripts/build-pack.mjs`
(NEW, Node, по образцу `validate-course.mjs`) материализует noun/adjective/verb/possessive формы из
`courses/pl-ru/course.json` в `courses/pl-ru/forms.generated.json` (лексема с префиксом категории, например
`noun:wife`/`verb:have`/`possessive:my`, → список `{bundle, form}`); `--check` пересчитывает и сверяет побайтно с
`tests/fixtures/core-golden/grammar.json`. `forms.generated.json` встроен в `:shared`'s `commonTest` (новая задача
`generateFormsFixtureSource`, тот же приём встраивания JSON-в-Kotlin-строку, что `generateCoursePackSource`) —
`TableMorphologyParityTest` строит `TableMorphology` из этого встроенного JSON и проверяет побайтное совпадение с
`GrammarEngine` на каждой лексеме × наборе признаков (носители — существующие `nouns`/`adjectives`/`verbs`/`possessives`).
`GrammarEngine`/`ExerciseFactory` остаются живым путём — переключение (UC-07/08) не входит в эту задачу;
`ExerciseFactory.kt` не тронут (параллельная задача UC-06).
Почему: приёмка `UniversalCorePlan.md` §12 UC-05 требует именно эту пару (таблица + конвертер) до
`ConstructionRealizer`/`ExerciseGenerator` (UC-07), и явно фиксирует риск §6 — golden-фикстуры зафиксированы на
коммите `a4b4aec`, поэтому «побайтное совпадение» проверяемо, а не плывущая цель.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §5.1, §5.3.1, §8, §12 UC-05; `scripts/build-pack.mjs`;
`kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/Morphology.kt`;
`kotlin/shared/src/commonTest/kotlin/polski/grammar/TableMorphologyParityTest.kt`.

## ADR-15 · 2026-09-27 · Второй пакет — английский для русскоязычных (en-ru) + «Лайфхаки» вне ядра
Проверка ядра — пакет `en-ru` (вместо рекомендованного в плане zh), использующий всё: 4 стиля, правила, карты слов, таблицы, подсветку, L1-сравнение с русским. Ядро должно поддерживать любую пару «родной → изучаемый» (ru→pl, ru→en, pl→en, en→ru …); найденные при этом упущения ядра дорабатываются в ядре, а не обходятся в пакете.
**Лайфхаки — вне ядра.** Это приёмы, которые облегчают понимание темы носителю конкретного родного языка при изучении конкретного целевого (у ru→en одни, у ru→pl другие, у pl→en третьи). Живут в слое пары (`pairs/<target>-<native>/lifehacks`), привязаны к навыку/теме, имеют источник и статус проверки (`editorial` — отобраны редакцией по исследованиям; `community` — проверены пользователями: «помогло / не помогло»). Сейчас — только статичные отобранные лайфхаки; приём и проверка лайфхаков от пользователей требуют сервера и идут отдельной задачей через зарезервированный порт (`LifehackProvider`), ядро от неё не зависит.
Почему: пользователь хочет проверить универсальность ядра на реальном курсе; en-ru типологически далёк от pl-ru (без падежей, аналитические времена, артикли, do-support) и сразу полезен; ценность лайфхаков — в опыте носителей, а не в грамматике.
Порядок: UC-05/06 → UC-07/08 (универсальный движок, побайтный паритет pl-ru) → UC-09 (таблицы как данные) + UC-12 (схема v2) → пакет en-ru с лайфхаками для ru→en (и добор для ru→pl). Критерий: ноль правок кода ядра ради en-ru.

## ADR-14 · 2026-09-27 · UC-01-correction: Android-пикер стилей остаётся закрытым, `persistStyle` — терпимым
`AndroidStylePicker` перечисляет `builtInStyleIds` (как и Web/Desktop-пикеры), а не все ключи `StyleRegistry` — `AndroidSessionViewModel.persistStyle` до сих пор пишет через закрытый 4-значный enum `PreferredStyle`, и `PreferredStyle.valueOf(styleId.value)` без обработки падал на любом id вне этих 4. `persistStyle` дополнительно сделан терпимым (`PreferredStyle.entries.firstOrNull { it.name == styleId.value } ?: return` — no-op вместо падения) на случай, если `AppUiState.styleId` получит внешний id не через этот пикер.
Почему: ADR-13 открыл `StyleId`/`StyleRegistry`, но `AndroidStylePicker` был расширен до `registry.keys.forEach` в том же коммите без сопоставления с `PreferredStyle` — 5-й `courses/styles/*.json` рецепт ронял приложение на первом же выборе стиля в Android-настройках. Автообнаружение новых стилей во всех хостах — отдельная задача.
Подробно: `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidStylePicker.kt`, `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/AndroidSessionViewModel.kt`.

## ADR-13 · 2026-09-27 · UC-01: первый `:core-*` модуль и открытый `StyleId`
`:core-model` (через новый convention-плагин `polski.kmp-common`, те же 7 KMP-таргетов, что `:shared`) вводит `FeatureKey`/`FeatureValue`/`FeatureBundle`/`Construction`/`SkillSpec`; польские enum (`model/Grammar.kt`) не переименованы — сверху добавлен тонкий адаптер (`toFeatureValue()`, `CaseFeature`/`NumberFeature`/…), декларирующий подмножество открытого каталога. `StyleId` (UC-10) заменён с закрытого enum на `data class StyleId(val value: String)` с 4 именованными константами того же wire-значения — `StyleRegistry` больше не отбрасывает нераспознанный `id` рецепта, любой JSON-файл в `courses/styles` становится стилем без правки Kotlin.
Почему: приёмка `UniversalCorePlan.md` §12 UC-01 требует пилотный `:core-*` модуль и демонстрацию открытого каталога признаков; закрытый `StyleId` был единственным местом, где «5-й стиль» требовал Kotlin-правки, противореча ADR-12.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §1, §4.1, §4.3; `Plans/Kotlin/StylesBlueprint.md` §2, §4.

## ADR-12 · 2026-09-26 · Универсальное ядро, гибридная архитектура
Общий костяк «конструкция × признаки» для всех языков; языки, L1-слои, пары и стили — данные; Gradle-модули только на границах кода (`:core-*`, `:pack-format`, `:morph-api`); опциональные плагины морфологии. Главный критерий — гибкая расширяемость.
Почему: цель — много языков и родных языков с минимумом кода. Гибрид набрал 44/45 против 34/45 у «только данные».
Подробно: `Plans/Kotlin/UniversalCorePlan.md`.

## ADR-11 · 2026-09-26 · Четыре стиля подачи вместо «гуманитарий/технарь»
Стили: через правило, через ситуацию, через сравнение с родным языком, минимум теории. Стиль — предпочтение, а не диагноз; задания и FSRS общие.
Почему: старые «Логика/Ситуации» отличались одной строкой подсказки; «типы мозга» — нейромиф.

## ADR-10 · 2026-09-26 · Сейчас только статичные данные и карточки слов
Озвучка, голосовой ввод и ИИ — позже, через зарезервированные интерфейсы.

## ADR-9 · 2026-09-26 · Настройка «Анимации»
Выключено → Rive не загружается вообще, движение мгновенное. Хранится в общей `UserPreferences.animationsEnabled`.

## ADR-8 · 2026-09-26 · Поведение карточек
Задания — раскрытие вниз без переворота. Слова — переворот всей панели, смена стороны на 90°, клик переворачивает сразу. Оценка свайпом (влево — повторить, вправо — вспомнил); телефон без кнопок; ПК с кнопками и ←/→. Вкладки — пейджер. Круговой эффект Rive убран.
Заменяет: переворот карточек заданий (коммиты af126a6, b22e758).

## ADR-7 · 2026-09-26 · Rive через официальные рантаймы
Нативный переворот/свайп, Rive — эффекты поверх. Rive-CMP не берём (подходит только Compose-хостам); форк Rive-CMP — запасной вариант при проблемах порта.
Подробно: `Plans/Kotlin/RiveResearch.md`, `FlipCardRivePlan.md`, `RiveCatalog.md`.

## ADR-6 · 2026-09-26 · Хранение iOS на UserDefaults до релиза
Перенос в файлы с миграцией (M5) отложен: приложение в разработке, пользователей нет.
Подробно: `Plans/Kotlin/IosFileStorageBlueprint.md`.

## ADR-5 · 2026-09-25 · Этап 11 (React ↔ Kotlin parity gate) снят
Переход на Kotlin выполнен; React — только исторический эталон.

## ADR-4 · 2026-09-25 · Нативный UI на каждом хосте
Web — семантический DOM/CSS, Android — Compose Material 3, iOS/macOS — SwiftUI; общее — только Kotlin-ядро. Кастомное «стекло» откатано.
Подробно: `Plans/Kotlin/NativeHostDecision.md`, `PreGlassRollback.md`.
