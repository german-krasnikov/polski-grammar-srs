# Exam scoring implementation evidence

Date: 2026-11-08. Plan: [EXAM-OPEN-ANSWER-ADDENDUM.md](EXAM-OPEN-ANSWER-ADDENDUM.md) and [EXAM-COMPLETION-ADDENDUM.md](EXAM-COMPLETION-ADDENDUM.md). Source of expected outcomes: `dsl/exam-scoring/answer-evaluation.core-data`, `answer-attempt.core-data`, and `unordered-set.core-data`; Kotlin tests consume the generated oracle rows.

## Official answer rules represented

- B1/2019 grammar paper pp. 16–20/key pp. 6–7: 8 tasks, 55 items, 30 points. Task V has two quarter-point alternatives and four equivalent full phrasings for V/4. Task VI's key explicitly accepts other correct transformations; five VI rows are `open_manual`. Printed answers are confirmed automatically, unlisted nonempty answers remain pending manual review. Writing paper p. 22 has three sets of two prompts, with limits 30+170, 40+160, 30+170 words.
- B1/2020 listening paper p. 6/key p. 3: grouped IV/B and IV/C are unordered; each printed blank is one point. Both correct tokens earn 8 quarter units, one correct token earns 4, and one correct plus one wrong earns 4. The earlier oracle comment that no per-choice partial credit was documented was incorrect and has been replaced.
- B2/2017 listening IV's three-token E answer has three one-point slots: 12 quarter units maximum. B2/2020 listening V/6 has two free-text slots worth 2 quarter units each, with order-independent evaluation. Duplicate selections do not receive repeated credit. `open_manual` mixed known/unknown set evaluation can preserve confirmed credit and a pending upper bound; no finite-key row is marked open without key evidence.

## Observed implementation checks

| Check | Result | Command (working directory) |
|---|---|---|
| Open-answer RED | **FAIL as intended**: `open-unknown expected pending_manual but was wrong`; production compiled | `./gradlew :shared:desktopTest --tests 'polski.exam.ExamAnswerEvaluationTest' --console=plain` (`kotlin/`) |
| Open-answer GREEN | **PASS** after common evaluator implementation | same focused command (`kotlin/`) |
| Unordered-set RED | **FAIL as intended**: `b1-2020-reversed expected confirmed_full but was wrong`; production compiled | same focused command (`kotlin/`) |
| Shared scorer/attempt tests | **PASS** including full, partial, wrong, empty, unknown pending, Submit idempotence and upper bound | `./gradlew :shared:desktopTest --tests 'polski.exam.ExamAnswerEvaluationTest' --tests 'polski.exam.grammar.GrammarExamScorerTest' --console=plain` (`kotlin/`) |
| DSL generation and validation | **PASS**, including B1/2019 164 rows, B2/2017 180 rows, B2/2020 196 rows and scoring oracle | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs generate --project dsl/biome.project` and `generate --check --project dsl/biome.project` (repository root) |
| Browser compile and distribution | **PASS**; six audio asset hashes verified | `./gradlew :composeApp:compileKotlinWasmJs --console=plain`; `./gradlew :composeApp:wasmJsBrowserDistribution --console=plain` (`kotlin/`) |

The first open-answer checkpoint had `composeApp.js` SHA-256 `2296ab776bf5405ab8d94837fda6d20e0f30b35ea1aefadd94952b9e103765b3`. After correcting the per-slot oracle and incorporating the independently measured 2019 audio boundaries, the new distribution built successfully with six verified audio assets. Its `composeApp.js` SHA-256 is `17771dc4311aaaf01019b7d7fb9ad9524678b2f54fdd01d510ac4e742f3490a8`. Focused shared tests and `biome generate --check --project dsl/biome.project` pass against this source. Browser verification against this updated distribution is **NOT RUN** here; independent acceptance is recorded separately by the tester.

## Reviewer correction: open groups in hidden exams

The reviewer found two official open-answer groups missing `open_manual`: B1/2020 Grammar VI (five transformations; key p. 6) and B2/2017 Listening V (nine answer slots; key p. 5 permits other transcript-consistent answers). Source rows now mark all 5 and 9 explicitly. B2/2017 Listening V's six documented alternate forms for its first two half-point slots live in `listening-answer-variants.core-data` and are bound to the typed B2/2017 generation step.

The independent `dsl/exam-scoring/open-item-groups.core-data` records require those exact group sizes, unknown nonempty answers to remain pending manual review, and blank answers to be wrong. `OpenExamSourceRowsTest` maps the actual generated B1/2020 grammar rows through `ExamDataMapper`; it evaluates the actual generated B2/2017 listening rows with their typed answer variants. This checks the source flags through behavior rather than asserting their text.

- **PASS** — `./gradlew :shared:desktopTest --tests 'polski.exam.OpenExamSourceRowsTest' --console=plain` from `kotlin/`: 1 focused test, build successful.
- **PASS** — full `biome generate --project dsl/biome.project` before unrelated B2/2019 source editing resumed: scoring oracle 70 rows, B1/2020 155, B2/2017 186, zero problems.
- **PASS** — targeted `biome data check` with the exact `scoring-oracle`, `exam-b1-2020`, and `exam-b2-2017` project bindings, including `--target kt --out` freshness checks: 70/155/186 rows, zero problems and no stale generated files. The later full project check is **NOT RUN** because in-progress B2/2019 writing adds the as-yet unbound official `application` genre.
- **NOT RUN** — new browser build for this correction; both corrected exams remain hidden from the catalog.

## Isolated staged-source freshness, coordinator check

The coordinator copied the staged candidate into a detached temporary worktree
(`/tmp/polski-exam-candidate-check`) so ongoing B2/2019 restoration and the next
B2/2020 audio changes could not enter this checkpoint accidentally. After the
open-item group contract and the two source corrections, Biome regenerated all
seven affected Kotlin outputs there; those tool-generated blobs were indexed
without overwriting the active worktree. `biome generate --check --project
dsl/biome.project` then passed (exit 0), and the temporary worktree had no
unstaged generated drift. Logs: `/tmp/polski-staged-exam-v2-generate.log` and
`/tmp/polski-staged-exam-v2-check.log`. This checks reproducibility of the staged
sources; it does not certify the hidden exams or replace browser acceptance.
