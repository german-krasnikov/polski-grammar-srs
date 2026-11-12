# P13: опубликованные экзамены и пустые ответы

2026-11-12. Область: только 16 строк `dsl/exam-published.core-data`. Вне каталога остаются sample, черновики и отозванный B2/2022-06. Этот журнал фиксирует проверку полноты данных и отображения; семантическая сверка **каждого** ответа с официальным PDF ещё не завершена.

## Источники и адреса вопросов

В каждой строке ниже `sourceDir` равен `exams/pl/certyfikat-polski/<уровень>/<сессия>`, а PDF, ключ и аудио лежат в `ExamBin/pl/<уровень>/<сессия>/{arkusz.pdf,klucz.pdf,audio.mp3}`. Все 48 локальных файлов обнаружены в этой проверке. Числа обозначают `grammar tests/items · listening tasks/questions · reading passages/questions · writing prompts`. Полный индекс идентификаторов заданий и вопросов находится в соответствующих `grammar-tests/items`, `listening-tasks/questions`, `reading-passages/questions` и `writing.core-data`; генерация сохраняет идентификаторы в `dsl/exam-published/generated/`, а `ExamContentIntegrityAuditTest` обходит все опубликованные shared models. Это адресуемая матрица, не подтверждение каждой страницы ключа.

Итого в опубликованном наборе: 127 grammar tests / 1125 items, 80 listening tasks / 604 questions, 80 reading passages / 691 questions, 91 writing prompts. Browser traversal сверяет фактический порядок и количество каждого задания/вопроса с соответствующей авторской строкой, в том числе все наборы письма.

| routeId | Авторские строки и локальный receipt | Задания и вопросы |
| --- | --- | --- |
| `b1-2017` | `B1/2017`; [source audit](B1-2017-DATA-EVIDENCE.md) | 7/50 · 5/37 · 5/34 · 6 |
| `b1-2019` | `B1/2019`; [source audit](EXAM-SCORING-EVIDENCE.md) | 8/55 · 5/42 · 5/35 · 6 |
| `b1-2020` | `B1/2020`; [source audit](B1-2020-DATA-EVIDENCE.md) | 8/55 · 5/34 · 5/34 · 6 |
| `b2-2017` | `B2/2017`; [source audit](B2-2017-DATA-EVIDENCE.md) | 8/65 · 5/43 · 5/45 · 6 |
| `b2-2019` | `B2/2019`; [source audit](B2-2019-DATA-EVIDENCE.md) | 8/75 · 5/36 · 5/45 · 6 |
| `b2-2020` | `B2/2020`; [source audit](B2-2020-DATA-EVIDENCE.md) | 8/75 · 5/38 · 5/45 · 6 |
| `b2-2022-02` | `B2/2022-02`; [source receipt](P9-B2-2023-06-2022-02-SOURCE-EVIDENCE.md) | 8/75 · 5/38 · 5/40 · 6 |
| `b2-2023-02` | `B2/2023-02`; [source receipt](P9-NEXT-TWO-SOURCE-EVIDENCE.md) | 8/75 · 5/36 · 5/45 · 6 |
| `b2-2023-06` | `B2/2023-06`; [source receipt](P9-B2-2023-06-2022-02-SOURCE-EVIDENCE.md) | 8/75 · 5/39 · 5/45 · 6 |
| `b2-2024-02` | `B2/2024-02`; [source receipt](P9-FIRST-TWO-SOURCE-EVIDENCE.md) | 8/75 · 5/38 · 5/41 · 6 |
| `b2-2024-06` | `B2/2024-06`; [source receipt](P9-NEXT-TWO-SOURCE-EVIDENCE.md) | 8/75 · 5/37 · 5/45 · 6 |
| `c1-2022-03` | `C1/2022-03`; [source receipt](P9-2022-REMAINING-SOURCE-EVIDENCE.md) | 8/75 · 5/40 · 5/41 · 5 |
| `c1-2022-11` | `C1/2022-11`; [source receipt](P9-NEXT-TWO-SOURCE-EVIDENCE.md) | 8/75 · 5/36 · 5/50 · 5 |
| `c1-2023-04` | `C1/2023-04`; [source receipt](P9-C1-2023-SOURCE-EVIDENCE.md) | 8/75 · 5/37 · 5/50 · 5 |
| `c1-2023-11` | `C1/2023-11`; [source receipt](P9-C1-2023-SOURCE-EVIDENCE.md) | 8/75 · 5/35 · 5/43 · 5 |
| `c1-2024-04` | `C1/2024-04`; [source receipt](P9-FIRST-TWO-SOURCE-EVIDENCE.md) | 8/75 · 5/38 · 5/53 · 5 |

For the 10 dated papers, [P9 source provenance](P9-SOURCE-PROVENANCE.md) gives the official URLs, local byte counts and recorded SHA-256. The six earlier papers have individual/source scoring receipts above. The current P13 pass did not recompare all 2,420 keyed questions against `klucz.pdf`; any proposed key correction still requires a page-level independent check before editing `.core-data`.

## Проверенная цепочка

1. **Авторские строки → generated:** исходный read-only Biome audit parsed 1,125 grammar, 604 listening and 691 reading rows and found zero blank `correctAnswer`, blank option tokens or missing required choice options. Receipt: `../../../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/initial-row-audit.{mjs,json}`. This counts source-derived rows; it is not an independent key oracle.
2. **Generated → shared model:** `ExamContentIntegrityAuditTest` traverses all 16 routes and every question, including IDs, nonempty prompt/context, answer key and options; it also checks nonempty task instructions, reading passages, writing prompts, image descriptions and audio URL. A blank user answer is allowed before submission; writing has no automatic correct answer. `open_manual` may legitimately remain pending for an unknown nonempty answer.
3. **Shared model → real DOM:** `kotlin-exam-content-audit.spec.ts` visits every authored task and every writing set in all four independent parts of every published paper on the real compatibility distribution. It matches the ordered grammar context, reading/listening question text and printed choices to the authored IDs/rows, checks input hints and button text, submits an empty attempt, and checks the exact review item count. It additionally checks a source-backed B2/2024-06 reading choice via `published-content-audit.playtest`. A wrong selected first answer remains visible in review; a separate unanswered item displays `[—]`.

No reproducible empty published answer, label or option was found in this bounded pass. No answer key, option, score or production renderer was changed. This does not resolve the owner's reported empty answer in other states (timed session, a different host, imported snapshot, or a particular interaction); collect an exam route, module, task/question and action sequence for a source-backed RED before changing data or policy. P15's explicit “Ваш ответ/Правильный ответ” report design remains separate work.

**PDF spot-check correction:** `ExamBin/pl/B2/2024-06/arkusz.pdf` (SHA-256 `6c22f42ad9216c6990c1a95f071d3bfd162fc674a3f7fa07d05cd44b2c697866`), PDF page 9, Reading I/1 prints `1. Z tekstu dowiadujemy się, że`; the prior authored row `certyfikat-polski-B2-2024-06-r1-q1` added `o blokadzie drogi do lotniska`. The source owner approved restoring the printed wording. The authored `questionText` now omits the numeral because the existing web renderer adds `1.`. The three options still match the paper. `ExamBin/pl/B2/2024-06/klucz.pdf` (SHA-256 `2b244b375866a6e23744509b6f988f1ff2312e877e870528382d6e03c5de4574`), PDF page 6, keys I/1 as `c`, matching the unchanged `correctAnswer`. Only the authored question text and its two generated copies changed.

## Evidence and remaining gate

- **RED → GREEN printed prompt:** After replacing the PlayTest expectation with the PDF page 9 wording, the old distribution failed on the `reading.firstQuestion` assertion. Receipt: `../../../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p13/reading-prompt-red.log`. After the single authored row correction, `biome fmt` reported 0 formatting changes, `biome generate` updated only the aggregate row and generated Kotlin row, and a fresh `:composeApp:composeCompatibilityBrowserDistribution` passed with all 16 exam audio assets. The focused PlayTest passed on the fresh distribution; receipt: `../../../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p13/reading-prompt-green.log`.
- **PASS shared test:** `./gradlew :shared:desktopTest --tests polski.exam.ExamContentIntegrityAuditTest --max-workers=2` from `kotlin/` (1 test) after regeneration. The first attempted `:shared:jvmTest` was a task-name setup failure; this project uses `desktopTest`.
- **PASS real browser:** Chromium, Wasm compatibility branch, Vite preview port 4202, zero retries: 19/19 source selection + full task/set traversal + focused DSL and wrong/blank review cases on the fresh distribution. Its `originWasmComposeApp.js` SHA-256 is `4794c61e6b4883ddb7921957afeb565539c77baa286f3c9768901cb6ebc223c0` and app Wasm `c1be4bef1795bb48e023.wasm` SHA-256 is `f71554a0c9e07141869b7945a189c16a12f8965ba7111e01a79d56cbd3efbb41`. The test did not enter a correct/wrong answer for all 2,420 questions; the selected-wrong check is one representative case. Full log: `../../../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p13/chromium-content-audit-after-source-fix.log`.
- **PASS DSL generation check:** `node …/biome/src/bin.mjs generate --check --project dsl/biome.project` from repository root after source correction; generated files compared byte-for-byte. The toolchain pin reported a stale warning, not a failed check.
- **PASS browser TypeScript compilation:** `npx tsc -p tsconfig.browser.json --pretty false` from repository root.
- **NOT RUN here:** independent per-question PDF/key page verification; JS fallback, Firefox, WebKit, iOS/Android devices. No native host result is inferred from Chromium or `desktopTest`.

Next P13 step: independent tester samples each exam's PDF page, answer key, control and review with IDs from this ledger, then investigates any reported blank on the exact route/state. Correct only source-backed mismatches through `.core-data` → `biome fmt` → `biome generate` → `biome generate --check`.
