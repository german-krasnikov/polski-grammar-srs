# R1.0 — база и печати (R1Receipt)

> Разделы 1–6 — факты чистой базы R1.0. Разделы 7–15 (после черты) добавлены итоговым прогоном R1.20 на дереве `65b8890`.

Только факты на **чистой** базе: код и данные продукта этим срезом не менялись (`git status` перед коммитом — один этот файл и неотслеживаемые `R1Design.md`, `ResumePlan.md`).
Каждая проверка названа с хостом. «Исполнено» = запущено на этом хосте и дало результат; всё остальное — NOT RUN. Один зелёный хост не доказывает другой: `desktopTest` — только JVM desktop.

| | |
|---|---|
| Продукт | `9d867a6f91e2a83ce52b4529c9e742e3ae2bb13c`, ветка `biome-resume`, worktree `wt-polski63` |
| Компилятор | `61cc0c34fc4ed872a24ca87d320d6b369b0eb1e1`; `TOOLCHAIN` в `tests/fixtures/nj03-exercise-oracle/biome.project` тот же; дерево компилятора грязное (`Plans/MIGRATION_GAP_PLAN.md`), пин не fetchable (R6) |
| Среда | macOS 26.6 arm64, Xcode 27.1, Node v24.1.0, Python 3.12.3 (`PYTHONDONTWRITEBYTECODE=1`: отслеживаемый `scripts/nj03/__pycache__/biome_value.cpython-312.pyc` иначе перезаписывается любым запуском генератора), Gradle 9.3.1, Kotlin 2.4.20, JVM тестов Temurin 21.0.12.1 aarch64 (daemon: Oracle JDK 23), браузер для JS/Wasm — Chrome (Karma headless), `ANDROID_HOME=$HOME/Library/Android/sdk` только для проб Android |
| Дата | 2026-10-29 |

## 1. Лимиты P8/P23 — объявлены здесь, до любого замера кандидата

Замеры §4 — это замеры **базы**, не кандидата. Лимиты ниже не поднимаются ради прохода (P23); срез, который их нарушает, получает NO-GO по G1/R1.20.

| Что | Как мерить | Лимит | Проход (запас ≥ 2×) |
|---|---|---|---|
| Целый пак, kotlinc | сравнение с §4, те же команды, чередование база/кандидат; порог владельца 2026-10-29 | ×1.6 к базе по метрике дизайна (wall команды) | wall кандидата ≤ 1.6 × база; **вторая метрика — время задачи в Gradle profile — тоже печатается**, она строже (в базе ≈ 3 с из ≈ 12 с wall) |
| `biome data generate`, N = 10/100/1000 `SkillRow` | 3 чередующихся прогона | N=1000: ≤ 3.0 с wall | ≤ 1.5 с; t(1000) − t(10) ≤ 12 · (t(100) − t(10)) |
| kotlinc `:shared:compileKotlinDesktop`, N = 10/100/1000 | как §4 (холодный, `--offline --no-daemon --profile`) | N=1000: wall ≤ 30 с, задача ≤ 10 с | wall ≤ 15 с, задача ≤ 5 с |
| JS/Wasm kotlinc, N = 1000 | как §4 | wall ≤ 25 с каждый | wall ≤ 12.5 с |
| Первый доступ `generatedPlRuSkills` (N = 1000), JVM / браузер JS и Wasm | чередующиеся CPU-замеры, собственный тест | JVM ≤ 200 мс, браузер ≤ 1000 мс | JVM ≤ 100 мс, браузер ≤ 500 мс |
| Логические шаги (`requireAcyclic`, R1.4b) | счётчик шагов в тесте | N=1000: ≤ 20 · N | ≤ 10 · N |
| P8, три правки на DSL-руке и на нативном компараторе | поле текста навыка; новое существительное (14 форм); новый навык с рецептом и copy-ключами (+ поле модели как 4-я) | места правки DSL ≤ нативного; существительное: цель 2 файла / 1 место авторства (база 6.0: 4 / 2) | метрики: места правки, авторские/генерируемые строки, места ревью, пойманные мутанты |

## 2. Исполнено на базе

| Проверка | Команда, cwd | Хост | Результат |
|---|---|---|---|
| `:shared:desktopTest` | `cd kotlin && ./gradlew :shared:desktopTest :composeApp:desktopTest --console=plain` | JVM desktop (Temurin 21) | **PASS** 72 файла / 420 тестов selected = executed, 0 fail / 0 error / 0 skipped; перечень имён — печать в §5 |
| `:composeApp:desktopTest` | та же команда | JVM desktop | **FAIL до исполнения тестов**: `:composeApp:compileKotlinDesktop` — `composeApp/src/desktopMain/kotlin/polski/desktop/Main.kt:216:13` NO_ELSE_IN_WHEN, нет ветки `UiEffect.DownloadText`; тесты composeApp **NOT RUN** |
| `:shared:jsBrowserTest` | `cd kotlin && ./gradlew :shared:jsBrowserTest` | браузер (JS) | **FAIL до исполнения**: `:shared:compileTestKotlinJs` — `commonTest/kotlin/polski/vocabulary/VocabularySessionAcceptanceTest.kt:36:95,104` (`toString(Charsets.UTF_8)` — только JVM; добавлено `2c2fc28`); тесты **NOT RUN** |
| `:shared:wasmJsBrowserTest` | `cd kotlin && ./gradlew :shared:wasmJsBrowserTest` | браузер (Wasm) | **FAIL до исполнения**: `:shared:compileTestKotlinWasmJs`, та же причина; **NOT RUN** |
| `:shared:macosArm64Test` | `cd kotlin && ./gradlew :shared:macosArm64Test` | Kotlin/Native macOS arm64 | **FAIL до исполнения**: `:shared:compileTestKotlinMacosArm64`, та же причина; **NOT RUN** |
| `:androidApp:testDebugUnitTest` | `cd kotlin && ./gradlew :androidApp:testDebugUnitTest` | Android JVM unit | без `ANDROID_HOME`: «SDK location not found»; с `ANDROID_HOME=$HOME/Library/Android/sdk`: **FAIL до исполнения**, `:androidApp:compileDebugKotlin` — `androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt:209:13` NO_ELSE_IN_WHEN (`UiEffect.DownloadText`); **NOT RUN** |
| iOS simulator, `:composeApp` JS/Wasm browser, Playwright, приложения iOS/macOS | — | — | **NOT RUN** (в списке W дизайна их нет / не запускались) |
| `biome generate --check --project tests/fixtures/nj03-exercise-oracle` | из корня продукта | node (CLI `61cc0c34`) | **PASS**, exit 0, 15 шагов, ≈ 1.4 с |
| проба подмены, 15 выходов | дописать `// probe` в выход, `generate --check`, вернуть через git | node | 13 цельных выходов: exit 1, байты на месте (не «починены»). 2 гибрида с областью `GENERATED START/END` (`TrainingParityTest.kt`, `PolskiTextCodecTest.kt`): дописанная **вне** области строка **не** ловится (exit 0, рукописная обвязка); правка **внутри** области — exit 1, байты на месте |
| `vitest run` (все 42 файла) | из корня, `BIOME_BIN=…/packages/biome/src/bin.mjs npx vitest run` | node/vitest | **10 файлов PASS (67 тестов), 32 файла FAIL на импорте** `courses/pl-ru/course.json` (28 — «Cannot find module», 4 — «Failed to resolve import … src/data/course.ts»); 0 упавших тестов внутри загруженных файлов |
| `check-nj03-old-curriculum-facts.mjs` | `node scripts/…` | node | PASS 2 файла, 32/32 строк |
| `scripts/nj03/check-preposition-freeze.py` | `python3 …` | python | PASS (7 old/typed facts, 7 lookup, 2 realizer, 9 source negatives, 5 tamper); NOT RUN сам перечисляет: Core parser, свежесть Kotlin, Gradle, хосты, P23 |
| `check-nj03-old-seeds-chains.mjs` | `node scripts/…` | node | **FAIL** ENOENT `courses/pl-ru/course.json` |
| `check-nj03-skill-copy-freeze.py` | `python3 scripts/…` | python | **FAIL** ENOENT `courses/pl-ru/course.json` |
| `check-no-app-json-tracked.py --summary` | `python3 scripts/…` | python | paths=108 tool=10 legacy-input=0 **violations=98** (в `courses/` 45: core 4, en-ru 1, lang 11, pairs 4, pl-ru 6, schema 15, styles 4), exit 1 |

## 3. Красное заморожено по именам (не PASS, R1 его не чинит; новый красный не допускается)

Vitest, 32 файла (все падают на импорте удалённого `course.json`; R1 их не меняет):

- `tests/app.test.tsx`
- `tests/course-chain-presentation-acceptance.test.ts`
- `tests/course-chain-presentation.test.ts`
- `tests/course-inventory-content.test.ts`
- `tests/course-morphology-acceptance.test.ts`
- `tests/course-morphology.test.ts`
- `tests/course-pattern.test.ts`
- `tests/course-reference-cases.test.ts`
- `tests/course-reference-chain.test.ts`
- `tests/course-reference-male-acc.test.ts`
- `tests/course-reference-pipeline.test.ts`
- `tests/course-reference-pronouns.test.ts`
- `tests/course-reference-russian-support.test.ts`
- `tests/course-reference-system-cards.test.ts`
- `tests/course-reference-verbs.test.ts`
- `tests/course-russian-support-contrast.test.ts`
- `tests/course-style-leak.test.ts`
- `tests/course-system-cards.test.ts`
- `tests/course-validation.test.ts`
- `tests/grammar.test.ts`
- `tests/method-cycle-audit.test.ts`
- `tests/method-cycle-content.test.ts`
- `tests/method-cycle-editorial-acceptance.test.ts`
- `tests/method-cycle-ui.test.tsx`
- `tests/pack-v2.test.ts`
- `tests/parity-acceptance.test.ts`
- `tests/parity-fixtures.test.ts`
- `tests/regressions.test.ts`
- `tests/sentences.test.ts`
- `tests/vocabulary-editorial.test.ts`
- `tests/vocabulary-view.test.tsx`
- `tests/vocabulary.test.ts`

Vitest, 10 зелёных (тесты в скобках):

- `tests/course-inventory-acceptance.test.ts` (5)
- `tests/course-inventory-checker.test.ts` (4)
- `tests/course-inventory-generator-acceptance.test.ts` (5)
- `tests/course-russian-support-contrast.test.tsx` (2)
- `tests/ending-highlight.test.ts` (7)
- `tests/lifehacks-en-ru.test.ts` (12)
- `tests/lifehacks-pl-ru.test.ts` (12)
- `tests/style-recipe-validation.test.ts` (5)
- `tests/swipe-generation.test.ts` (7)
- `tests/vocabulary-editorial-en-ru.test.ts` (8)

Остальное красное: `:composeApp:compileKotlinDesktop` (`Main.kt:216`), `:androidApp:compileDebugKotlin` (`MainActivity.kt:209`) — оба из-за `UiEffect.DownloadText` без ветки в `when`; `VocabularySessionAcceptanceTest.kt:36` в `commonTest` (JS/Wasm/K/N); чекеры `check-nj03-old-seeds-chains.mjs`, `check-nj03-skill-copy-freeze.py`; NJ01 (98).

## 4. Замеры базы (kotlinc холодный)

Команда: `cd kotlin && rm -rf shared/build/kotlin/compileKotlin<T> shared/build/classes/kotlin/<t>/main && ./gradlew :shared:compileKotlin<T> --offline --no-daemon --profile --console=plain`, 3 раунда, цели чередуются; «задача» — `Task Execution` из Gradle profile. Фон: на машине идут другие сборки (первый запуск: «1 busy Daemon could not be reused»), разброс раунд-в-раунд ≈ 0.5 с.

| Цель | wall команды, с | медиана | задача, с | медиана |
|---|---|---|---|---|
| Desktop | 12.6, 12.1, 11.6 | 12.1 | 3.07, 3.11, 2.81 | 3.07 |
| Js | 10.8, 10.2, 10.3 | 10.3 | 2.04, 1.74, 1.75 | 1.75 |
| WasmJs | 10.3, 10.3, 10.6 | 10.3 | 1.82, 1.75, 1.71 | 1.75 |

Число дизайна (12.7 с «фон» на desktop) по величине совпадает с wall команды (здесь 12.1 с), а не с временем задачи (3.1 с); метрика в дизайне не названа — это предположение. Пол wall ≈ 9 с — запуск Gradle без daemon, сама компиляция `:shared` ≈ 3 с. Если порог владельца ×1.6 принимался по wall, он допускает рост задачи kotlinc больше чем в 1.6 раза: обе метрики печатаются в каждом срезе, владельцу решать, какая из них его порог.

## 5. Печати sha256 (sha256, байт, путь)

Перечень имён 420 тестов `:shared:desktopTest` (отсортированные `класс#тест статус`, один файл): `3d5af25261d8da69531efb5cacbaef4eb5de284136773a505dc035de69c0d2ad`.

18 JSON-источников R1 (удаляются к R1.19):

```text
4f98cb4e0a5bc5b5f21890120fe25e077cedc2ba5e65852bff4b1e252b4234b7     5067  courses/lang/pl/curriculum.json
8ef622c556d34e4cdf29cc99fe33c5f3ad848df373c437a845874e4e8d34b5a4    24150  courses/lang/pl/exercise-recipes.json
2dd50f80ae326d27a54e899e56aafa7049763105c3ed4b71cd5a1ac940f8347b     2762  courses/lang/pl/realization.json
4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6     5010  courses/lang/en/curriculum.json
05757efb0e6966dfd25144f62ab34b49fd66453dd9a4dc077bbdeaed28a6c326    28283  courses/lang/en/exercise-recipes.json
50f846ca393f14c0fa2b969cfb2dddbed40566c404d2c29194eaa8d2dd490e03     7557  courses/lang/en/realization.json
a0796ddfbb8eddebc7860ec0dd84cffee757223aa755b747c98289ef6b9edd3d    44644  courses/lang/en/forms.generated.json
f96da7fc14e569d2e061a9f0b39a6d97ead26eb7f21d6521d9ed457a8d0abd80     5469  courses/lang/en/lexicon.json
140e900f2b98b6233d00b0bb83df360f93875f75bf83a66f83b85ea1d0a6fcc2      592  courses/lang/en/prepositions.json
55f79438cf1511b1061d389f4d74117112c910092e7515c73c57aa0d3af61862    86219  courses/pairs/en-ru/pair.json
4cc8eb175cef2189b30493308e03d6f75cbbc6f19b2e899309394143bf6369c0    35687  courses/pairs/en-ru/lifehacks.json
6b851b198f19e2a594175050439b0ee4392bc670f8ed0207fe00949b295cbc61    49406  courses/pairs/pl-ru/lifehacks.json
97864959a9d00be33f8cab0de4250f1cfa8cde64d1bf995c5e8182333fb656c7   242240  courses/pl-ru/forms.generated.json
4549d27b90ec4bb3e1ca9db55fcdd2256b91e28a25f0474cbd55f6a94e2a9a70    76965  courses/pl-ru/frequency-top1000.json
3dc89ab8a1549bd3ffa44de0b6e49c93eb0d371291221733e14171e800190375      366  courses/styles/minimal-theory.json
e62dd012c32f470b818a154cbac09dfe91f89d1ec5428955a9b52418e0869e99      514  courses/styles/native-contrast.json
2b34a01937f8e80f77fa40d47bb99e5416179aba47c2e234a2d53663beefd305      396  courses/styles/rule-first.json
f29d62285b12357685970490fa2f80b4f3a4e39814cd0145ec6610a6423dba2f      368  courses/styles/situation-first.json
```

12 `Generated*.kt`:

```text
7b9d2b859f359f31320eb11018975c581a564445a7ad44a5bfeeb153133bf8e4    40056  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseFrequency.kt
b4651ab868644fc405f97f3ca5a7c495595a991b9543efbeef096ced8781f4fe     8675  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseFull.kt
14ee8ee37a283002fc4e03b948d44e20396abd9b92bbb629ef7d1d29e73eae06    36073  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseLexicon.kt
a7b91786f8af48c4f3c5efa47570e7f9cbcfa58b12301b9b294347e8f1c7c8ec    42606  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCoursePresentations.kt
f15980ef6c19c46082727bc3bb44fbcc352971d49afc7cd80e5621fcd5d4f490    13163  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCoursePronouns.kt
e2049753fd3b2f17943471356b4b26d2c3a0749bd6d8d47445d98c3a8474154f     1653  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseReference.kt
a07bede4e19ca75840f800073cdb4a4782667852b060ae2fb01f4fdf221c1654     8257  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseSections.kt
6c7fc7f7d877e5ac0e6068f28541f70e7eddaba027f55491a3b320f0414d3f2e     9644  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseTeaching.kt
05e4c9c6bca59da333e438fb2a54c762c0ee60ed17199c3a1d8c45188099603c     5972  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseValueSource.kt
87d325d5e3b68ef3ea65fc766bb147f8f75851938d2fc35a03e566a6cb405be0     4384  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseVocabulary.kt
e8831a522810fbd813912d2dc4b73fda12526d5f6d67ec257061ceccd0f7d4ba    74809  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedEnRuPack.kt
a8e906939ea4dd7bd9a371adbc2e86f1bd58f77f0f277c20cac2fc4e642f69eb    78353  kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedLifehacks.kt
```

`courses-biome/**` (15):

```text
a9097635fb4574da0998169d381f0137108a9cce0724c5286783b9750c0e54ce     8485  courses-biome/pl-ru/adjectives.core-data
5475e2f178a751f0dc97235f7ba03c38048915fa82423544b16ee55d9324c831      247  courses-biome/pl-ru/caseSentencePrefixes.core-data
451ddab15acac682fce5f7f0923956d6d1de40267eaa0dc077b97a7f4e9a53fa     6142  courses-biome/pl-ru/exerciseCopy.core-data
b16b470d896d43d176cc411195f5ff4707a586530adaf08aeda29fd2711a4e7a      575  courses-biome/pl-ru/exercisePatterns.core-data
116da4e67137319eed8cc020fc2f63afd086d4ef9dec0ea762f133ef0738eb14      238  courses-biome/pl-ru/morphology.core-data
db662af417e3b6e7769d3c295f8a7771bff37e697bfb6810ff1f5258c7240f34     4271  courses-biome/pl-ru/nouns.core-data
e39d3b632d4a71bfb9e89230b9c257a5bfd0665073825a5979702214b35afd2f      873  courses-biome/pl-ru/personalPronouns.core-data
95131de5ee024a0307c4b9777b5f69310524b6da5d82abae389e7205f3e78181     3673  courses-biome/pl-ru/possessives.core-data
3306d03cefeff36a9fc193c675be3624f5305ac741e6315067b395b217433dff    16000  courses-biome/pl-ru/reference.core-data
9d00e7bb7aaaece751eaa2a5e72b253e3283bd5798fc762c755a40811ea4d08e      609  courses-biome/pl-ru/sentenceSeeds.core-data
87c142d054b803c5982f1022c5de479086a431f88f24cfe977ade435ea8eafd4    48466  courses-biome/pl-ru/skills.core-data
735770fe9cb99d84da3402a16d7473479468396978958b417752ae9ca3a02f8a      127  courses-biome/pl-ru/stemAlternations.core-data
272a339fa2848923bd06659defaed673b5dbb5d9391a691070c98cf8cb4b6fcd      776  courses-biome/pl-ru/training.core-data
908826874f24affa55c76476db4c60acf21d0785ca2175b30eccb0ef5220dc11     3185  courses-biome/pl-ru/verbs.core-data
78a8d1ab4b1bceb12910e11d904a075a8b7ec9ce034d10f629babc2b1099efe8     7092  courses-biome/pl-ru/vocabulary.core-data
```

Независимые опоры (не меняются без отдельной записи требования, P15):

```text
ef4d5949d19e8a30f2f71604ab47db3a8bcd5a0fba9ace05869b0d17e2cdf29c    87057  tests/fixtures/nj03-exercise-oracle/requirements.core-data
f704738d523421e2975aa79796b0a44c7465cafc1cfa3c74a1f586cbfbe7571f    97624  kotlin/shared/src/commonTest/kotlin/polski/training/TrainingParityTest.kt
6d5d4e135453328c441f5459beefa69466e5066b0304114b8ab4303df1660c37     5351  Plans/Biome/NJ03CurriculumIndependent.cases
1e7933b61b666397fa23d40b80a9e197674bfd7148465cc92b2219c6589dea82   233422  Plans/Biome/NJ03SkillCopyIndependent.cases
c7888ad050df4f1c96e74c06fd8fe4122cbd0568b118c8adea5deb8d176b1eb1     5809  Plans/Biome/NJ03PrepositionIndependent.cases
929b5c7b8d8798b8efcb3638d879dbdaefc622859f43280509d5d5593c784c69    10314  Plans/Biome/NJ03SeedsNounsChains.cases
85c64ba7763411aefafe798f0e1c9f07b4c6a941b22aa429e2e3fb7f661a5998   533108  kotlin/shared/src/commonTest/kotlin/polski/grammar/GrammarParityTest.kt
d5b187c68b5b3fc46cdc967d38197a9a8fa1fd8ee0b52841c4dadbd93de58361    13289  kotlin/shared/src/commonTest/kotlin/polski/training/CurriculumReaderTest.kt
90024794948af8261cc2f4f6bcd530da450dd8e5db3c372214fe84acb45193d0     8106  kotlin/shared/src/commonTest/kotlin/polski/data/CoursePackFixtureTest.kt
```


## 6. W после правки commonTest (добавлено вторым коммитом, вне плана R1)

Единственная причина красного JS/Wasm/K/N на базе — `VocabularySessionAcceptanceTest.kt:36`. Правка в одну строку (`toString(Charsets.UTF_8)` → `decodeToString()`, те же байты UTF-8 → тот же текст) отдельным коммитом, не частью срезов R1; без неё W не исполняется вовсе.
Прогон на дереве `75e2324` + эта правка, каждая задача отдельным `./gradlew <задача> --rerun --console=plain` из `kotlin/` (XML свежие):

| Задача | Хост | files / selected = executed | fail / error / skipped | exit |
|---|---|---|---|---|
| `:shared:desktopTest` | JVM desktop (Temurin 21) | 72 / 420 | 0 / 0 / 0 | 0 |
| `:shared:jsBrowserTest` | браузер (JS, Chrome/Karma) | 68 / 410 | 0 / 0 / 0 | 0 |
| `:shared:wasmJsBrowserTest` | браузер (Wasm, Chrome/Karma) | 68 / 410 | 0 / 0 / 0 | 0 |
| `:shared:macosArm64Test` | Kotlin/Native macOS arm64 | 76 / 447 | 0 / 0 / 0 | 0 |

Расхождение 420 (desktop) / 410 (JS, Wasm) / 447 (macOS) проверено по именам: наборы JS и Wasm совпадают; 10 тестов только desktop — классы `NJ03CourseValuePilotTest`, `NJ03PairLifetimeTest`, `StyleSourceGenericityTest`, `PolskiTextCodecTest` (source set `desktopTest`); macOS — те же 410 плюс 37 из `macosTest`.
По-прежнему NOT RUN: `:composeApp:desktopTest` (`Main.kt:216`), `:androidApp:testDebugUnitTest` (`MainActivity.kt:209`) — нужна ветка `UiEffect.DownloadText` в обоих `when` (поведение хоста, решение вне R1); iOS simulator; Playwright; приложения iOS/macOS.

---

# R1.20 — итоговый прогон (§5 дизайна), дерево `65b8890`

Дата 2026-10-30. Ветка `biome-resume`, база R1.0 — `9d867a6` (§1–6 выше), начало R1.11–R1.19 — `1962c38`. Дерево чистое, кроме неотслеживаемых `R1Design.md` и `ResumePlan.md`. Компилятор — соседний checkout (HEAD `1a062dfb`); пин `61cc0c34` даёт предупреждение `TOOLCHAIN_PIN_STALE` (файлы всё равно сравниваются побайтно), как на базе. Каждая проверка названа с хостом; «исполнено» = запущено на этом хосте и дало результат. Решение GO/NO-GO принимает владелец; ниже — доказательства и открытые пункты.

## 7. Что сделано: 36 коммитов R1.11–R1.19

| Срез | Коммиты (feat / test…) | Итог |
|---|---|---|
| R1.11a, R1.11b-1, R1.11b-2 | `78d4a09` `efe0aee` `0884312` `d02e058` `228ead7` `1b66dbf` | pl: выражения рецептов, шаблоны конструкций, рецепты, цепочка, значения по умолчанию — типизированные строки |
| R1.12 | `760eb0f` `d673d3a` `f7d38e6` | pl: таблица форм строится при сборке (`dsl/derive-forms.mjs`), 39 лексем / 1560 форм |
| R1.13-0, R1.13 | `6b085f8` `6c022f5` `23a85fd` | чекеры читают историю `git show` + SHA; `PackEngine` строится из пака, pl-JSON и две задачи Gradle ушли |
| R1.14a, R1.14b | `7c9911d` `527cf02` `134ca47` `66631a2` | en: лексикон, пара (навыки, тексты, словарь, цепочка), копии мапперов делает `dsl/derive-enru-mappers.mjs` |
| R1.15, R1.16 | `cee8989` `a52d50c` `f4a2424` `d517768` `e6b8393` | en: SkillSpec, рецепты, конструкции, таблица форм (49 лексем / 329 форм) |
| R1.17 | `ec2c588` `c39d2cc` | en-флип: пак берёт вход движка из строк; мост, парсеры и 7 en-JSON удалены |
| R1.18a, R1.18b | `cb4a9b6` `211e3eb` `a905cbd` · `10076ee` `c8d09fd` `306e723` `505cb41` | стили (4 строки, `StyleRegistry`) и лайфхаки (64 строки, `CoursePackFields.lifehacks`); последняя задача встраивания удалена |
| R1.19a, b, c | `d485a75` `bcb69ae` · `1bf704d` `a9ccde8` · `0e24de3` `65b8890` | частоты en-ru как данные без потребителя; канонический вид `biome fmt` и шаги `fmt`; guard «нет читателя JSON курса»; ADR-54 |

С базы удалено: 19 файлов `courses/**.json` (694 934 байта; ещё три — `course.json`, `pairs/pl-ru/pair.json`, `lang/pl/lexicon.json` — убрал NJ03 раньше), 12 `Generated*.kt` (323 645 байт), 13 Python-генераторов, 3 `build-pack*.mjs`/`extract-course-morphology.mjs`, 12 обёрток `derive-*.mjs`, обе Gradle-задачи встраивания, `EnRuJsonBridge.kt`, `parseCurriculum`, `parseRecipes`, `parseConstructionTemplates`, `parseStyleRecipesJson`, `validate-lifehacks.mjs`.

## 8. Исполнено на итоговом дереве

Каждая задача — `./gradlew <задача> --rerun --console=plain` из `kotlin/` (XML свежие). selected = executed: число `@Test` в исходниках равно числу выполненных (commonTest 517, desktopTest +7, macosTest +37; `@Ignore` нет).

| Задача | Хост | files / tests | fail / error / skipped | База (§6) |
|---|---|---|---|---|
| `:shared:desktopTest` | JVM desktop (Temurin 21) | 92 / 524 | 0 / 0 / 0 | 72 / 420 |
| `:shared:jsBrowserTest` (`-Pkotlin.incremental.js=false -Pkotlin.incremental.js.ir=false`) | Chrome headless (JS) | 89 / 517 | 0 / 0 / 0 | 68 / 410 |
| `:shared:wasmJsBrowserTest` | Chrome headless (Wasm) | 89 / 517 | 0 / 0 / 0 | 68 / 410 |
| `:shared:macosArm64Test` | Kotlin/Native macOS arm64 | 97 / 554 | 0 / 0 / 0 | 76 / 447 |

Только компиляция (без запуска тестов): `:shared:compileKotlinIosArm64`, `:shared:compileKotlinIosSimulatorArm64`, `:shared:compileTestKotlinIosSimulatorArm64`, `:composeApp:compileKotlinJs`, `:composeApp:compileKotlinWasmJs`, `:composeApp:compileTestDevelopmentExecutableKotlinJs` — BUILD SUCCESSFUL: ни один хост не ссылается на удалённый API.

**NOT RUN:** `:composeApp:desktopTest` и `:androidApp:testDebugUnitTest` — те же две ошибки компиляции, что на базе, в тех же строках и единственные: `Main.kt:216` и `MainActivity.kt:209`, `'when' expression must be exhaustive … 'is DownloadText'`. Ветка для `UiEffect.DownloadText` — продуктовое решение (что делать с эффектом), не правка в одну строку, поэтому её нет. Также NOT RUN: iOS simulator (тесты и приложение), приложения Swift (iOS, macOS), Playwright (`tests/browser/*`), `gate:all` компилятора, устройства.

Vitest, весь набор (`BIOME_BIN` задан): **339 тестов PASS, 0 FAIL; 32 набора не запускаются — ровно те 32 файла по именам, что заморожены в §3** (падают на импорте удалённого JSON; R1 их не менял). Из 10 зелёных файлов базы три удалены вместе со своим JSON после карты на Kotlin/vitest (`lifehacks-en-ru`, `lifehacks-pl-ru`, `style-recipe-validation`: карта в сообщениях `306e723`, `211e3eb`), `vocabulary-editorial-en-ru` (8) переподключён к `git show` и зелёный, остальные шесть не менялись. `npx tsc -b`: 97 ошибок (88 после R1.13; +9 — импорты удалённых файлов в `tests/pack-v2.test.ts`, красном до R1).

## 9. Оракулы и печати (§5.2)

| Оракул | Что | Результат (tests / fail / skipped) на desktop, JS, Wasm, K/N |
|---|---|---|
| EX112 | `TrainingParityTest` | 5 / 0 / 0 на каждом из четырёх хостов |
| EN-OR | `TrainingParityEnTest` (92 случая, R1.1) | 2 / 0 / 0 на каждом |
| GP | `GrammarParityTest` | 20 / 0 / 0 на каждом |
| CUR | `CurriculumReaderTest` + vitest `course-curriculum` (16 строк pl и 16 en против `NJ03CurriculumIndependent.cases`) | 11 / 0 / 0 на каждом; vitest 4 / 0 |
| SC | `python3 scripts/check-nj03-skill-copy-freeze.py` | PASS: 5 печатей, 32 навыка, 1296 узлов, 7 отрицательных мутаций |
| PREP | `python3 scripts/nj03/check-preposition-freeze.py` | PASS (читает JSON из `git show 7358aeb…` + SHA) |
| — | `check-nj03-old-curriculum-facts.mjs`, `check-nj03-old-seeds-chains.mjs` | PASS, PASS (были FAIL на базе: ENOENT) |

Независимые опоры не менялись: `requirements.core-data` (ef4d5949…), `TrainingParityTest.kt` (f704738d…), `GrammarParityTest.kt` (85c64ba7…), `NJ03CurriculumIndependent.cases`, `NJ03SkillCopyIndependent.cases`, `NJ03PrepositionIndependent.cases`, `NJ03SeedsNounsChains.cases` — sha256 равны базе (§5). Изменены только `CurriculumReaderTest.kt` (вход — строки; удалён один тест, ставший тождеством: `level`/`prerequisites` теперь одна строка) и `CoursePackFixtureTest.kt` (замороженный JSON-текст стал типизированными значениями поле в поле, R1.3). Новая опора: `requirements-en.core-data` (46975490…, R1.1). Ожидания не подгонялись под вывод.

Проба флипа en (R1.17), одни и те же четыре правки строк en-ru до и после: до флипа **0** тестов видят правку (движок читал JSON), после — `TrainingParityEnTest.allExerciseCases` видит все четыре; правка конструкции валит ещё 4 теста движка (`EnExerciseGeneratorTest` ×2, `EnRealizationTest` ×2), правка владельца — ещё 3 (`TrainingParityEnTest.metadataAndChainLinks`, `EnExerciseGeneratorTest` ×2).

## 10. Один авторитет (§5.1)

- JSON-сканер `check-no-app-json-tracked.py --summary`: **violations 98 → 79** (цель дизайна 80; лишний — частоты en-ru по решению владельца 3).
- Guard `tests/retired-json-path.test.ts` (8 тестов): 22 файла `courses/**.json` отсутствуют; в 20 файлах Gradle/`.properties`/`.toml` нет `courses/`; в 356 `.kt` нет `val generated…Json…`; в 113 `.kt` пакетов `polski.{data,core,training}` нет `kotlinx.serialization`; Python- и NJ03-скрипты, называющие JSON курса, читают его через `git show` на 40-hex ревизии. Пять пробы-нарушений убиты 5 из 5 (каждое — одним тестом).
- Реестр сгенерированного (`tests/generated-registry.test.ts`): каждый `.kt` с баннером — выход ровно одного шага; git помечает его generated.
- Проба подмены для **каждого** выхода реестра (дописать строку в выход, `generate --check`, вернуть): **17 выходов** — 14 целых файлов: exit 1 с именем файла; 3 гибрида (`TrainingParityTest.kt`, `TrainingParityEnTest.kt`, `PolskiTextCodecTest.kt`): правка **внутри** области `GENERATED START/END` — exit 1, вне неё — exit 0 (рукописная обвязка, как на базе §2). Байты после проб на месте, `git status` чист.
- Каждый факт — в одном `.core-data`: `KEY`/`ref` проверяет `data check` (`generate --check` исполняет 5 `data`-шагов, 9 `derive`-шагов, 3 `fmt`-шага); дубли записей двух паков (`rows.plru` / `rows.enru`) собираются из одного рукописного маппера pl-ru (копию делает `dsl/derive-enru-mappers.mjs`, `generate --check` ловит расхождение).

## 11. D-набор (§5.2) — какой тест что исполняет

| Случай | Тесты |
|---|---|
| пропавший id | vitest: 44 теста `DATA_REF` в 10 файлах (`course-recipes` 18, `course-enru-pair` 9, `course-pronoun-teaching` 5, `course-lexicon` 3, `course-data` «a prerequisite that is no skill»…) — каждый проверяет позицию `file:line:col` |
| чужой тип | `course-enru-pair`: навык/ключ copy/конструкция/владелец паков друг друга — `DATA_REF` в шаге другого пака; `course-lifehacks`: skillId не из SkillRow |
| цикл | Kotlin `PlRuSkillSpecsTest.aSelfLoopATwoCycleAndACycleBehindAnAcyclicTailAreRefused` + `aSharedPrerequisiteIsNotACycle` на всех четырёх хостах; **остаточный риск**: цикл ловится при первом доступе, позиции в файле нет (`data check` чист) |
| optional / null / empty / порядок | `PlRuRowsTest` (`anAbsentStyleContentIsTheDomainDefault`, `anEmptyTextIsDataNotAbsence`, `focusFixedAndFilterBecomeTheCoreTypesInTheOrderWritten`, `aFilterPropertyThatIsNotWrittenIsNotInTheFilter`), vitest `DATA_REQUIRED` («null is written»), `toString`-паритеты порядка в Δ |
| Unicode | `PlRuRowsTest.textsPassThroughUnchanged` (астральный символ, `$`, `${y}`, `\`, `"`, перенос, табуляция, пробелы по краям) на всех четырёх хостах; vitest `course-data`: печатный Kotlin несёт такой текст точно |
| позиция в UTF-16 | vitest `course-data` «columns count UTF-16 units» (ожидаемая колонка считается `indexOf` в JS, не берётся из вывода инструмента) |

## 12. Равные данные и API (§5.3)

Δ=0 по каждому семейству отдельно для pl и en (одноразовые сравнения со старым значением — в сообщениях коммитов; постоянные — тесты, читающие закрытый источник из истории):

| Семейство | Сравнение | Постоянный тест |
|---|---|---|
| pl рецепты, конструкции | `==` с `parseRecipes`/`parseConstructionTemplates` (R1.11b), затем EX112/GP зелёные | `PlRuRecipeSetRowsTest`, EX112 |
| pl формы | 39 лексем / 1560 форм == `forms.generated.json` и 1560 golden-случаев | vitest `course-forms` (golden), `FormTablesTest` |
| pl навыки (SkillSpec) | 16 строк == замороженные случаи | vitest `course-curriculum`, `CurriculumReaderTest` |
| en лексикон, пара | `==` старому сгенерированному значению (16 навыков, 12 затравок, 73 copy, 6 шаблонов, 1 префикс, цепочка, 30 карточек…) и `toString` | `EnRu*RowsTest`, vitest `course-enru-pair` |
| en SkillSpec, рецепты, конструкции | паритет с JSON-парсерами на всех четырёх хостах (R1.15–R1.16), `toString` | vitest `course-curriculum` (en), `EnRuEngineInputsTest`, EN-OR |
| en формы | 49 лексем / 329 форм == замороженный JSON **побайтно** и в том же порядке | vitest `course-forms-enru` (`git show` + SHA), `EnRuFormTablesTest` |
| стили | 4 рецепта `==` `parseStyleRecipesJson` (toString 1272 знака) | vitest `course-styles` (`git show` + SHA) |
| лайфхаки | 32 + 32 `==` сгенерированному Kotlin (29 526 + 22 107 знаков) | vitest `course-lifehacks` (`git show` + SHA) |
| частоты en | 1000 строк == `items` закрытого файла | vitest `course-frequency` |

Снимок публичных деклараций верхнего уровня `polski.{data,core,training,presentation}` (commonMain; одноразовый скрипт вне репозитория, разбирает исходники двух ревизий, члены классов не отслеживаются): **164 на базе → 446 сейчас; удалены 4** — `parseConstructionTemplates`, `parseRecipes` (`polski.core`), `parseCurriculum`, `plCurriculum` (`polski.training`), то есть ровно JSON-входы белого списка; **добавлены 286** — все в новых пакетах `polski.data.rows.{plru,enru,plru.forms,enru.forms,plru.frequency,styles}` (типы и константы строк). Остальные 160 деклараций не менялись.

## 13. P8 / P23 (§1 и §5.4)

Холодный kotlinc целого пака, база `9d867a6` против `65b8890`, 3 чередующихся раунда, `--offline --no-daemon --profile`, медианы (wall команды / время задачи Gradle):

| Цель | база, с | кандидат, с | отношение | лимит |
|---|---|---|---|---|
| Desktop wall / задача | 11.70 / 2.52 | 11.94 / 3.22 | ×1.02 / ×1.28 | wall ≤ ×1.6 ✓ (задача печатается, строже) |
| Js | 10.02 / 1.58 | 10.46 / 1.94 | ×1.04 / ×1.22 | ✓ |
| WasmJs | 10.10 / 1.57 | 10.23 / 1.88 | ×1.01 / ×1.20 | ✓ |

Серия N = 10 / 100 / 1000 синтетических `SkillRow` (полная строка первого навыка pl-ru, 2.3 КБ данных, `prerequisites: []`; Kotlin 57 756 / 427 616 / 4 126 064 байт), 3 раунда:

| Что | N = 10 | N = 100 | N = 1000 | Лимит / проход |
|---|---|---|---|---|
| `biome data generate`, wall, с (медиана) | 0.50 | 0.66 | **1.495** (6 прогонов: 1.46–1.58; первые три — 1.51) | ≤ 3.0 / проход ≤ 1.5: лимит ✓ с запасом ×2.0, **проход на грани**; t(1000) − t(10) = 0.99 ≤ 12 · (t(100) − t(10)) = 1.86 ✓ |
| kotlinc Desktop, wall / задача | 12.50 / 3.43 | 12.25 / 3.49 | **13.40 / 4.54** | wall ≤ 30 ✓ (проход ≤ 15 ✓), задача ≤ 10 ✓ (проход ≤ 5 ✓) |
| kotlinc Js, wall / задача | 10.14 / 2.01 | — | **10.96 / 2.83** | wall ≤ 25 ✓ (проход ≤ 12.5 ✓) |
| kotlinc WasmJs, wall / задача | 10.05 / 1.96 | — | **10.89 / 2.79** | wall ≤ 25 ✓ (проход ≤ 12.5 ✓) |
| первый доступ `generatedScaleSkills`, N = 1000 (3 свежих запуска) | — | — | JVM 21.1 / 24.1 / 21.3 мс; Chrome JS 26.4 / 26.1 / 25.6 мс; Chrome Wasm 26.0 / 26.3 / 26.1 мс; K/N macOS 13.6 / 4.5 / 4.4 мс | JVM ≤ 200 ✓ (проход ≤ 100 ✓), браузер ≤ 1000 ✓ (проход ≤ 500 ✓) |
| логические шаги `requireAcyclic` | 6.30 | 6.93 | 6.99 на навык | ≤ 20 · N ✓ (проход ≤ 10 · N ✓), тест `PlRuSkillSpecsTest.workGrowsLinearlyWithTheNumberOfSkills` на четырёх хостах |

Метод серии: рабочая копия итогового дерева, синтетические строки делает одноразовый скрипт (не в репозитории), копия маппера `PlRuSkillRows.kt` с заменой имён в пакет `polski.data.rows.scale`; первый доступ — одноразовый тест в копии (не в репозитории). Холодный старт K/N, нагрузка нескольких паков и реальные устройства не измерялись.

ObjC-заголовок статического framework macOS (`linkDebugFrameworkMacosArm64`): **535 272 → 834 525 байт (+55.9%), 7 786 → 11 088 строк, 327 → 565 интерфейсов** (база R1.0 → R1.19; каждый срез в своём коммите). Решение владельца — «отметить, мер не принимать» было принято при +16.4%; сейчас рост больше. Источники роста: контракт печатается целиком в пакет каждого шага (`rows.plru`, `rows.enru`, `rows.*.forms`, `rows.styles`), записи дублируются. Время линковки, размер самого framework и холодная линковка базы против кандидата в итоговом прогоне **не измерялись**.

P8, три правки на DSL-руке, выполнены на итоговом дереве (правка → `biome fmt` → `biome generate` → полный `desktopTest` и vitest-тесты курса → откат); на базе — те же правки там, где конвейер на базе работает:

| Правка | DSL-рука (итог) | Нативная рука (база `9d867a6`) |
|---|---|---|
| поле текста навыка (`hint` у `case.acc.n`) | 1 авторский файл / 1 строка; сгенерирован 1 файл (`rows/plru/CourseRows.kt`, 1 строка); падают 2 закреплённых ожидания: `CoursePackFixtureTest`, `GrammarParityTest.corpusPart20` (нужны только при смене требования) | выполнено: 1 авторский файл (`skills.core-data`); сгенерированы 2 файла (`GeneratedCoursePresentations.kt`, `GeneratedCourseValueSource.kt`) |
| новое существительное (14 форм: `sister`) | **1 авторский файл** (`nouns.core-data`, 26 строк, формы пишутся один раз); сгенерированы 2 файла (`CourseRows.kt` +26, `forms/FormRows.kt` +103: выведены 14 форм); падают 3 закреплённых счётчика (`FormTablesTest` 39 лексем; vitest `course-data`, `course-forms`) | `nouns.core-data` (1 строка) + `GeneratedCourseLexicon.kt` (+5 строк) + **ещё одно место авторства**: `forms.generated.json` (100 строк на существительное, пишутся руками: `scripts/build-pack.mjs` читает `course.json`, которого нет, и не запускается); база дизайна: 4 файла / 2 места авторства. Цель дизайна 2 файла / 1 место: **1 место достигнуто**, файлов 3 (1 авторский + 2 сгенерированных), на один больше цели — `FormRows.kt` дизайн не считал |
| новый навык с рецептом и ключами copy (`case.dat2`: строка навыка, рецепт, 3 ключа) | **3 авторских файла**: `skills.core-data` +55, `recipes.core-data` +58, `exerciseCopy.core-data` +6 (119 строк); сгенерирован 1 файл (+131); `data check` сразу показывает пропавший ключ copy (до дописывания ключей: 3 × `DATA_REF` с `file:line:col`); падают 16 закреплённых «16 навыков»: 11 Kotlin (счётчики, порядок, независимые `TrainingParityTest.metadataAndChainLinks`, `CoursePackFixtureTest`, и **покрытие лайфхаками: у каждого навыка ≥ 2 совета** — для нового навыка нужны ещё 2 строки лайфхаков) и 5 vitest | перечислено, не выполнено: строка в `skills.core-data` (+ `level`/`prerequisites`) **и** запись в `lang/pl/curriculum.json` (те же `level`/`prerequisites` второй раз), запись в `exercise-recipes.json`, ключи в `exerciseCopy.core-data` — **4 авторских файла** |
| поле модели (4-я) | перечислено, не выполнено: 4 места (данные, контракт, маппер, модель) | 4 места, как в R1.4b |

Пойманные ошибки (единая методика «тот же дефект на обеих руках», подробные таблицы в сообщениях коммитов): навыки pl 9 из 11 статически против 6 из 11 (R1.4b); пара en-ru 6 из 6 против 2 из 6; таблица форм en 6 из 6 против 1 из 6; стили 5 из 5 против 0 из 5; лайфхаки 7 из 7 (старый валидатор ловил те же 7, но отдельной командой вне сборки; генератор не отказал ни разу); рецепты en 6 из 7 статически (7-й — лексема константного слота, вне контракта; его ловит первое использование) против 0 из 7 статически на старом пути: 2 при разборе JSON, 5 при первом использовании движка.

## 14. Отклонения, остаток и открытые пункты

- **Не исполнено (NOT RUN)**: см. §8. Ничто из этого не затронуто изменениями R1, кроме ссылок, которые проверены компиляцией.
- **Новых красных тестов нет** (339 тестов vitest PASS, 0 FAIL, те же 32 набора; 4 хоста 0 fail; `tsc -b` +9 ошибок в красном `pack-v2.test.ts`). **Сломано командами вне R1-области (требует решения владельца)**: шаги CI `npm run course:validate`, `course:inventory:check`, `course:inventory:decisions:check` (`.github/workflows/kotlin-check.yml`) на этой ветке не проходят — они читают файлы, которые R1 удалил (инвентарные падали и на базе); `validate-pack-v2.mjs` импортирует удалённый `validate-lifehacks.mjs`. §6 дизайна оставляет TS-инструменты pack v2 за R2–R6. Не меняю CI-гейт без владельца.
- **Видимая пользователю фраза** «Формы читаются из forms.generated.json(en)…» на экранах матрицы iOS, web и Android называет удалённый файл; она закреплена `courses/pl-ru/source-inventory.json`, правка требует хостов, которых нет.
- **Рост ObjC-заголовка** +55.9% против +16.4% при решении владельца (§13).
- `courses/schema/lifehacks-v1.schema.json` осталась (15 схем — за R5/R6); читать её больше некому.
- Цикл в навыках ловится при первом доступе, без позиции в файле (остаточный риск D-набора).
- Реализация формы: таблица форм en-ru строится в `derive-forms.mjs` портом правил `build-pack-en.mjs`; `neg:not` — литерал, как в старом построителе.
- Нумерация срезов: в дизайне R1.18 — стили и лайфхаки, R1.19 — Retire; частоты en-ru (решение владельца 3) — отдельный срез R1.19a.

## 15. Вердикт по §5 (чтение доказательств, не решение владельца)

1. **Один авторитет — выполнено**: guard (8 тестов, 5 из 5 нарушений ловятся), 17 из 17 выходов реестра отвергают подмену, JSON-сканер 98 → 79.
2. **Исполнено — выполнено для `:shared` на четырёх хостах** (selected = executed, skipped = 0) и всех оракулов; **не выполнено** `:composeApp` и Android unit-тесты (две ошибки компиляции базы, не R1). D-набор исполняется, цикл — с остаточным риском без позиции.
3. **Равные данные и API — выполнено**: Δ=0 по каждому семейству, публичные декларации отличаются только удалёнными JSON-входами и новыми пакетами строк.
4. **P8/P23 — в пределах объявленных лимитов**: целый пак ×1.01–×1.04 по wall (лимит ×1.6), серия N = 1000 укладывается в проходные отметки (запас ≥ 2× до лимитов), `data generate` — на грани проходной отметки 1.5 с (медиана шести прогонов 1.495 с, трёх прогонов серии Desktop 1.51 с; лимит 3.0 с с запасом ×2.0); нативное сравнение P8 выполнено полностью для правки текста, для нового существительного и навыка — частично (DSL-рука выполнена, нативная — по структуре базы).

Если открытые пункты §14 (CI-гейты, composeApp/Android, ObjC) принимаются владельцем, оснований для NO-GO по §5 в собранных доказательствах нет; пока они не решены, слияние — решение владельца (единица слияния — вся R1.3–R1.19).
