# R1.0 — база и печати (R1Receipt)

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
