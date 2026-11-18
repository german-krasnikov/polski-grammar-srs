# Module design: Шаблоны писем (Letter Templates)

Дата: 2026-11-18. Статус: принят к реализации.

---

## Scope and consumers

**Capability:** contextual reference panel (structure + phrase bank + common mistakes + model letter) for
letter/email/application writing tasks in Certyfikat Polski B1/B2 exams.

**Access points:**
1. "Шаблон" collapsible panel inside the existing exam writing task view — shown only when
   `task.genre` matches a template (`letter`, `email`, `application`). No new nav tab.
2. Standalone sub-screen "Шаблоны" reachable from the exam home list — rendered as a new
   `ExamScreen.Templates` value within the existing `ExamWebController` state machine (web)
   and its equivalents on other hosts.

**Hosts:** web DOM (primary), Android Compose, iOS bridge + SwiftUI. Desktop frozen (ADR, AGENTS.md):
no new template UI, exhaustiveness arm only where enums require it.

**Practice element:** NO for MVP. Section ordering / register choice exercises require FSRS
integration and a new `ExerciseType` DSL extension. Deferred to a future iteration; the DSL
contract leaves room (a future `LetterPracticeRow` record adds no breaking changes — P14).

**En-ru pack:** documented here, not implemented. What en-ru would need:
- `courses-biome/en-ru/letter-templates.core-data` with English phrases, same record schema.
- Russian notes (`nativeNote`, `ruleNative`) adapted to English letter conventions
  (English conventions differ meaningfully from Polish: no Pan/Pani, different salutation norms).
- A model letter in English per template type.
- `EnRuLetterTemplateRows.kt` derived via `dsl/derive-enru-mappers.mjs` (same pattern as other
  pair mappers). Until authored, `LetterTemplateModel.forGenre(genre)` returns empty list for
  en-ru without error; "Шаблон" toggle is hidden.

---

## Exam genre survey — B1/B2 basis for MVP scope

Genres from actual B1/B2 `writing.core-data` files that have letter-like structure:

| Genre tag | Appearances | MVP template |
|---|---|---|
| `letter` | B1/2019, B1/2022-06, B1/2023-02, B1/2023-06, B1/2024-02, B1/sample, B2/2019, B2/2020, B2/2022-02, B2/2024-02 | 3 templates: complaint, request, inquiry |
| `email` | B2/2017, B2/2020, B2/2023-02, B2/2023-06, B2/2024-02, B2/2024-06 | 2 templates: formal, informal |
| `application` | B2/2019, B2/2023-02, B2/2024-02 | 1 template: podanie/list motywacyjny |
| `invitation` | B1/2017, B1/2019, B2/2019, B2/2024-02 | DEFERRED to expansion |

MVP: 6 templates total (3 `letter` + 2 `email` + 1 `application`).
The `invitation` template is a natural second wave — the structure is distinct and exam-confirmed.

---

## Public contract

### New DSL contract: `dsl/letter-template.data-contract` (NEW)

```
DSL 1
# Letter/email templates: section structure, phrase banks, mistakes and model letters.
# Target-language phrases and native-language notes are pair data (courses-biome/<pair>/).
# Task→template links are authored per real WritingTaskRow id (LetterTaskLinkRow).

# Top-level ENUMs generate a single shared enum class in Kotlin (e.g. `enum class LetterRegister`),
# reused across all records that reference it.  Record-inline `enum` fields generate a
# record-scoped class (e.g. `LetterTemplateRowLetterType`).

ENUM LetterRegister formal|semi_formal|informal
ENUM LetterVerificationStatus verified|needs_review|inferred

RECORD LetterTemplateRow KEY templateId COUNT 1..
  templateId: string
  letterType: enum complaint|request|inquiry|application|apology|invitation|formal_email|informal_letter
  register: LetterRegister
END

RECORD LetterSectionRow KEY sectionId COUNT 1..
  sectionId: string
  templateRef: ref LetterTemplateRow
  order: int
  sectionKey: enum salutation|opening|body|request|closing|signoff
  labelNative: string
  noteNative: string OPTIONAL
END

RECORD LetterPhraseRow KEY phraseId COUNT 1..
  phraseId: string
  sectionRef: ref LetterSectionRow
  targetText: string
  nativeNote: string OPTIONAL
  register: LetterRegister
  isPreferred: bool OPTIONAL
  mistakeNote: string OPTIONAL
  sourceNote: string OPTIONAL
  verificationStatus: LetterVerificationStatus
END

RECORD LetterMistakeRow KEY mistakeId COUNT 0..
  mistakeId: string
  templateRef: ref LetterTemplateRow
  wrongForm: string
  correctForm: string
  ruleNative: string
  sourceNote: string OPTIONAL
  verificationStatus: LetterVerificationStatus
END

RECORD ModelLetterRow KEY letterId COUNT 0..
  letterId: string
  templateRef: ref LetterTemplateRow
  titleNative: string
  fullText: string
  annotationNative: string OPTIONAL
  sourceNote: string OPTIONAL
  verificationStatus: LetterVerificationStatus
END

# Per-task links: one row per (WritingTaskRow id, LetterTemplateRow id) pair.
# taskRef is a plain string (not ref) to avoid a cross-contract dependency — same reasoning
# as genreRef was, but explicit and per-task accurate rather than genre-approximate.
RECORD LetterTaskLinkRow KEY linkId COUNT 0..
  linkId: string
  taskRef: string
  templateRef: ref LetterTemplateRow
END
```

**Notes on design choices:**
- `LetterRegister` and `LetterVerificationStatus` are top-level ENUMs so biome generate emits
  one shared `enum class LetterRegister` and one `enum class LetterVerificationStatus` in Kotlin.
  Using them in multiple records avoids duplicate inline enum classes (P7 / "один контракт →
  типы адаптера; ручной второй словарь типов запрещён" — ProjectMigratioPrincipals §Практика типов).
- `letterType` and `sectionKey` are inline enums (each used in exactly one record) — they generate
  `LetterTemplateRowLetterType` and `LetterSectionRowSectionKey`.
- `genreRef` is REMOVED (finding: `genre: letter` in B1 covers both informal letters to friends
  and formal complaints/requests in B2 — genre-based matching would suggest wrong templates).
  Replaced by `LetterTaskLinkRow` with `taskRef: string` (WritingTaskRow task id, plain string
  to avoid cross-contract dependency) + `templateRef: ref LetterTemplateRow`. The companion
  panel calls `model.forTask(taskId)` and shows only templates linked to that specific task.
  A task with no links shows no "Шаблон" toggle. The standalone browser lists `model.all()`
  grouped by `letterType` / `register`.
- `informal_email` renamed to `informal_letter` — the informal template covers both informal
  letters to friends and informal emails (the distinction is structural, not register-based;
  the template content notes cover both contexts).
- `formal_email` added to `letterType` — required by AC-3 and to distinguish formal email
  tasks (B2/2017, B2/2020, B2/2023-06, B2/2024-02, B2/2024-06) from formal letters.
- `LetterTemplateRow` has no explicit `targetLanguage`/`nativeLanguage` fields — pack-implicit
  (data in `courses-biome/pl-ru/`), same convention as `LifehackRow`.
- `verificationStatus` on every content row. The cross-field conditional "verified row must have
  non-empty sourceNote" is NOT expressible in data-contract (no cross-field predicate syntax).
  This check lives as a native Kotlin unit test — a **small-case measured-better native**
  remainder (P25/P8).

### New Kotlin domain types: `polski/exam/template/` (NEW, commonMain)

```kotlin
// LetterTemplate.kt — pure data, no IO; enums are GENERATED types, never redeclared.
//
// Generated by biome generate (never hand-edit):
//   enum class LetterRegister(val tag: String)         — top-level ENUM, shared
//   enum class LetterVerificationStatus(val tag: String) — top-level ENUM, shared
//   enum class LetterTemplateRowLetterType(val tag: String) — record-inline enum
//   enum class LetterSectionRowSectionKey(val tag: String) — record-inline enum
//
// Optional typealiases for ergonomic names (aliases, NOT redeclarations — P7 compliant):
typealias LetterType = LetterTemplateRowLetterType
typealias SectionKey = LetterSectionRowSectionKey
// LetterRegister and LetterVerificationStatus need no alias — names are already concise.

data class LetterPhrase(
    val id: String, val targetText: String, val nativeNote: String?,
    val register: LetterRegister, val isPreferred: Boolean,
    val mistakeNote: String?, val verificationStatus: LetterVerificationStatus,
)
data class LetterSection(
    val id: String, val order: Int, val sectionKey: SectionKey,
    val labelNative: String, val noteNative: String?,
    val phrases: List<LetterPhrase>,
)
data class LetterMistake(
    val id: String, val wrongForm: String, val correctForm: String,
    val ruleNative: String, val verificationStatus: LetterVerificationStatus,
)
data class ModelLetter(
    val id: String, val titleNative: String, val fullText: String,
    val annotationNative: String?, val verificationStatus: LetterVerificationStatus,
)
data class LetterTemplate(
    val id: String, val letterType: LetterType, val register: LetterRegister,
    val sections: List<LetterSection>,
    val mistakes: List<LetterMistake>,
    val modelLetter: ModelLetter?,
)
```

### LetterTemplateModel (NEW, commonMain, stateless)

```kotlin
// LetterTemplateModel.kt
class LetterTemplateModel(
    private val templates: List<LetterTemplate>,
    private val links: List<LetterTaskLink>,   // from LetterTaskLinkRow mapper
) {
    /** Templates explicitly linked to this writing task. Empty if none authored. */
    fun forTask(taskId: String): List<LetterTemplate> {
        val ids = links.filter { it.taskRef == taskId }.map { it.templateRef }
        return templates.filter { it.id in ids }
    }
    fun all(): List<LetterTemplate> = templates
}

// LetterTaskLink — domain wrapper for LetterTaskLinkRow
data class LetterTaskLink(val linkId: String, val taskRef: String, val templateRef: String)
```

No coroutines, no IO. Instantiated once at host startup alongside `officialWritingData`.
Companion panel calls `forTask(task.id)`. If the list is empty, no "Шаблон" toggle is shown.
Standalone browser calls `all()` and groups by `letterType` / `register`.

### LetterTemplateProvider (NEW, commonMain, interface)

```kotlin
interface LetterTemplateProvider {
    fun forPack(packId: String): LetterTemplateModel
}
```

`StaticLetterTemplateProvider` implements this using the generated rows, analogous to
`StaticPackLifehackProvider`. An empty `LetterTemplateModel(emptyList())` is returned for packs
with no authored templates — no null, no exception.

---

## Components and dependencies

### New files

| Path | Status | Responsibility |
|---|---|---|
| `dsl/letter-template.data-contract` | NEW | DSL schema (6 record types above) |
| `courses-biome/pl-ru/letter-templates/letter_template.core-data` | NEW | 6 MVP `LetterTemplateRow` records (complaint, request, inquiry, application, formal_email, informal_letter) |
| `courses-biome/pl-ru/letter-templates/letter_section.core-data` | NEW | `LetterSectionRow` records; each template has 4–6 sections ordered by `order` field |
| `courses-biome/pl-ru/letter-templates/letter_phrase.core-data` | NEW | `LetterPhraseRow` records; 4–6 phrases/section; all `verified` rows cite `sourceNote` |
| `courses-biome/pl-ru/letter-templates/letter_mistake.core-data` | NEW | `LetterMistakeRow` records; 3–5 per template |
| `courses-biome/pl-ru/letter-templates/model_letter.core-data` | NEW | `ModelLetterRow` records; 1 model letter per template |
| `courses-biome/pl-ru/letter-templates/letter_task_link.core-data` | NEW | `LetterTaskLinkRow` records; maps real B1/B2 task ids to template ids |
| `kotlin/shared/src/commonMain/kotlin/polski/exam/template/LetterTemplate.kt` | NEW | Domain types (enums + data classes above) |
| `kotlin/shared/src/commonMain/kotlin/polski/exam/template/LetterTemplateModel.kt` | NEW | Stateless query model |
| `kotlin/shared/src/commonMain/kotlin/polski/exam/template/LetterTemplateProvider.kt` | NEW | Interface + `StaticLetterTemplateProvider` |
| `kotlin/shared/src/commonMain/kotlin/polski/exam/template/LetterTemplateSourceData.kt` | NEW | `val plRuLetterTemplates: LetterTemplateModel by lazy { ... }` using generated rows, mirroring `ExamWritingSourceData.kt`'s pattern |
| `kotlin/shared/src/commonMain/kotlin/polski/data/PlRuLetterTemplateRows.kt` | NEW | pl-ru mapper (generated rows → domain `LetterTemplate` trees), follows `PlRu*Rows.kt` pattern |
| `kotlin/shared/src/commonMain/generated/polski/data/rows/plru/LetterTemplateRows.kt` | NEW (generated) | `biome generate` output — never hand-edited |
| `kotlin/shared/src/commonTest/kotlin/polski/exam/template/LetterTemplateModelTest.kt` | NEW | Unit tests for query model (see Verification) |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/LetterTemplateWeb.kt` | NEW | Web DOM renderer — template panel + standalone browse screen; phrase-to-draft insertion |
| `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidLetterTemplatePanel.kt` | NEW | Android Compose collapsible panel; phrase appends to draft |
| `kotlin/apple-bridge/src/iosMain/kotlin/polski/ios/IosLetterTemplateSession.kt` | NEW | On-demand snapshot query by genre, exposed to SwiftUI (follows `IosVocabularySession` pattern in same package) |
| `dsl/exam-web/writing-letter-template.playtest` | NEW | Companion panel playtest (profile `playmode`): openLetterTemplate / selectLetterTemplate / insertLetterPhrase / ASSERT writing.draft |
| `dsl/exam-web/writing-letter-templates-browse.playtest` | NEW | Standalone browse playtest: open exam home → "Шаблоны" → list / section render / model letter |

### Modified files

| Path | Change |
|---|---|
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/ExamWeb.kt` | Add `ExamScreen.Templates` to enum; wire "Шаблоны" link on exam list; render standalone template browser |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/ExamWritingWeb.kt` | Add "Шаблон" collapsible below guidance when `LetterTemplateModel.forTask(task.id).isNotEmpty()` |
| `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidExamWritingScreen.kt` | Integrate `AndroidLetterTemplatePanel` composable; phrase tap appends to draft textarea |
| `kotlin/apple-bridge/src/iosMain/kotlin/polski/ios/IosSnapshot.kt` | No change required; `IosLetterTemplateSession` is a separate on-demand session |
| `dsl/biome.project` | Register `letter-template.data-contract` and all 6 `.core-data` files in `courses-biome/pl-ru/letter-templates/` |
| `tests/browser/exam-playtest-host.ts` | Add 3 actions (`openLetterTemplate`, `selectLetterTemplate type=...`, `insertLetterPhrase phrase=...`) and 3 snapshot keys (`writing.templateVisible: bool`, `writing.templateSections: string` (comma-joined section keys), `writing.templateTypes: string` (comma-joined type labels)) |

### Dependency direction

```
courses-biome/pl-ru/letter-templates/{letter_template,letter_section,letter_phrase,
                                       letter_mistake,model_letter,letter_task_link}.core-data
    ↓ biome generate (--project dsl/biome.project)
commonMain/generated/.../LetterTemplateRows.kt    [generated, never edited]
    ↓ PlRuLetterTemplateRows.kt mapper
polski/exam/template/ {LetterTemplate, LetterTaskLink, LetterTemplateModel, LetterTemplateProvider}
    ↓ LetterTemplateSourceData.kt  (lazy val, no IO)
    ↓ used by exam hosts
webMain/LetterTemplateWeb.kt  ←  ExamWritingWeb.kt (companion panel: forTask(task.id))
                              ←  ExamWeb.kt         (standalone screen: all())
androidMain/AndroidLetterTemplatePanel.kt    ← AndroidExamWritingScreen.kt
apple-bridge/iosMain/IosLetterTemplateSession.kt ← SwiftUI host
```

The template module has zero dependency on `TrainingStore`, `AppUiState`, FSRS, progress or
`WritingExamSession`. It is a pure static reference provider. No circular dependencies.

---

## State and resources

`LetterTemplateModel` and all domain objects are immutable. `plRuLetterTemplates` is a lazy val
initialized once. No subscriptions, no coroutines, no cleanup needed.

The `IosLetterTemplateSession` (in `kotlin/apple-bridge/src/iosMain/kotlin/polski/ios/`)
follows the same on-demand snapshot pattern as `IosVocabularySession` in the same package:
a Kotlin method returns a JSON snapshot for a given `taskId` string, Swift decodes it.
This avoids embedding all template data in every `IosSnapshot` emission.

---

## Content accuracy requirements

The following Polish language rules must be sourced and marked `verified` in the data.
Every `verified` row's `sourceNote` must name a source the developer actually opened — the
direct URL of the PWN Poradnia answer or the exam PDF page reference — not a general citation.
A row whose source was not opened is `needs_review`.

**PWN norms (verify each at poradnia.pwn.pl / Wielki słownik ortograficzny PWN):**

| Rule | Correct forms | Typical learner mistake | Authoring note |
|---|---|---|---|
| Salutation punctuation | Both "Szanowny Panie!" and "Szanowny Panie," are correct. After "!" the body starts with CAPITAL; after "," the body starts with LOWERCASE. | Mixing: "Szanowny Panie," + "Chciałbym…" (comma + capital) OR "Szanowny Panie!" + "chciałbym…" (exclamation + lowercase) | Record as a `noteNative` on the salutation section, not as two separate `LetterMistakeRow` entries. |
| Formal closing punctuation | "Z poważaniem" (no comma preferred); signature on the next line | "Z poważaniem," (comma is an English calque; non-standard but widely used) | Convention, not a hard error. Mistake row: `wrongForm: "Z poważaniem,"` → `correctForm: "Z poważaniem"` with note that the comma form appears in practice but is non-standard per PWN. `verificationStatus: verified`, source cited. |
| Closing register — formal letter | "Z poważaniem" | Informal closing ("Buziaki", "Cześć") in a formal letter; also "Pozdrawiam serdecznie" (semi-formal only) | Mistake row on formal template |
| Closing register — informal letter | "Pozdrawiam serdecznie", "Ściskam", "Buziaki" | "Z poważaniem" in a letter to a friend (over-formal) | Mistake row on informal template |
| Salutation gender agreement | "Szanowny Panie" (m), "Szanowna Pani" (f), "Szanowni Państwo" (mixed/unknown) | Agreement errors: "Szanowny Pani", "Szanowna Panie" | Mistake rows with wrong/correct pairs |
| Spelling | "Z poważaniem" | "Z pozawaniem", "Z powazaniem" | Highest-priority mistake row (source: the user's motivating example) |
| Pan/Pani capitalization | Capitalizing Pan/Pani/Państwo in the letter body is a **courtesy option**, not mandatory. Both "Pan" and "pan" are acceptable. | — | Record as a `noteNative` tip ("Zaleca się pisać Pan/Pani wielką literą jako wyraz szacunku"), not as a `LetterMistakeRow`. |

All entries in `courses-biome/pl-ru/letter-templates/` with `verificationStatus: verified`
must have a non-empty `sourceNote` naming the actual source opened.
Content flagged `needs_review` is shown with a visual badge (⚠).
Content that is correct but was not independently sourced is marked `inferred` — shown without a badge (reviewed by main agent).
Content that needs a source check is marked `needs_review`.
Task A aims for all 6 MVP templates fully `verified` before merge.

---

## Decisions and tradeoffs

**D1: No practice element in MVP.** Register-choice or phrase-ordering exercises require new FSRS
item types and exam session integration. The reference value alone (structure + phrases + mistakes +
model letter) addresses the stated user need. Future `LetterPracticeRow` record adds to the
contract without breaking it (P14 additive).

**D2: No new nav tab.** Nine tabs already exist. The companion panel inside the writing task gives
contextual access at the right moment. The standalone "Шаблоны" link on the exam home screen gives
discovery. Both are within the existing `ExamScreen` state machine — no new routing infrastructure.

**D3: `LetterTaskLinkRow.taskRef` is a plain string, not a typed ref.** Avoids a cross-contract
dependency from `letter-template.data-contract` to `exam.data-contract`. The host passes
`task.id` (a plain string from `officialWritingData`) to `model.forTask(taskId)`. A
`ref WritingTaskRow` would create a build-order dependency; the runtime value is already a string.
Genre-based matching (`genreRef`) was rejected because `genre: "letter"` in B1 covers informal
letters to friends, while in B2 it covers formal complaints — suggesting the same templates for
both would be wrong. Per-task links are precise and always correct.

**D4: `letterType` distinguishes subtypes within a template set.** A writing task may be linked
to multiple `LetterTemplateRow` records with different `letterType` values (e.g. complaint and
request). The UI shows all templates linked to the task and lets the user pick the subtype.
`informal_letter` covers both informal letters to friends and informal emails — the distinction is
structural (greeting/signoff differ), not register-based; a single template handles both with
context notes per section.

**D5: Pair-layer data only in `courses-biome/pl-ru/`.** Section labels, notes and Russian
explanations are Russian-speaker concerns — they belong in the pair layer, not in a
language-agnostic shared location. This is consistent with how `lifehacks.core-data` stores
Russian-language explanations in `courses-biome/pl-ru/`. The DSL contract is language-agnostic;
the data files are pair-specific.

**D6: iOS uses a separate on-demand session, not snapshot embedding.** Template data with model
letters is substantially larger than the small `lifehackGroups` field that was added to
`IosSnapshot`. Embedding all templates in every snapshot would increase wire size and slow every
snapshot update. A purpose-built `IosLetterTemplateSession` queries by genre and returns only the
relevant template tree, following the precedent of `IosVocabularySession`.

---

## Verification

### AC-1 (data/shared — Tasks A1 + A2 gate)

**DSL-covered checks (run by `biome generate --check --project dsl/biome.project`):**
- All `LetterPhraseRow.sectionRef` refs resolve — checked by biome data ref validation.
- All `LetterMistakeRow.templateRef` and `ModelLetterRow.templateRef` refs resolve — same.
- All enum field values are from the declared domain — biome data type check.
- These checks require no native test code (P4/ARCHITECTURE §3: "поведение, приёмка → .examples,
  .sources, EditMode/PlayTest; обход, сортировка, IO → механизм Core или нативная утилита").

**Native Kotlin unit tests in `LetterTemplateModelTest` — classified remainders:**
- `verifiedRowHasNonEmptySourceNote` — asserts every row with `verificationStatus ==
  LetterVerificationStatus.VERIFIED` has a non-empty `sourceNote`. **Remainder class: small
  case** — data-contract supports type/ref/COUNT/OPTIONAL checks but not cross-field
  conditional predicates; native test is the measured-better option (P8/P25).
- `eachTemplateSectionsAreOrderedWithoutGaps` — asserts sections 1..N sequential. **Remainder
  class: ordering invariant on a set of records** — not expressible in data-contract syntax;
  native test (P8/P25).
- `forTaskReturnsLinkedTemplates` — loads pl-ru model; task with 2 linked templates → `forTask(id)` returns 2.
- `forTaskReturnsEmptyForUnlinkedTask` — task with no `LetterTaskLinkRow` rows → `forTask(id)` returns empty.
- `formalTemplateHasOnlyFormalPhrases` — formal template salutation section phrases all have
  `register == LetterRegister.FORMAL`. **Remainder class: register isolation** — a data
  authoring invariant not expressible in data-contract; native test (P8/P25).
- `modelLetterPresentForAllTemplates` — 6 of 6 MVP templates have a non-null `modelLetter`.

A1 gate: passes with 1-template seed data. A2 gate: all 6 templates, all native tests green.

### AC-2 (web companion — Task B gate, playtest)

Implemented as `dsl/exam-web/writing-letter-template.playtest` (profile `playmode`), driven by
`tests/browser/exam-playtest-host.ts`. Outline of the DSL test script:

```
TEST "Letter template companion shows in writing task and inserts phrase" PROFILE playmode
  open exam="B2 Certyfikat 2019" module="writing"
  # (select writing set whose task has LetterTaskLinkRow entries in letter_task_link.core-data)
  selectWritingSet set="I"
  # Task 1 must be linked to complaint template via letter_task_link.core-data
  ASSERT writing.templateVisible == true
  openLetterTemplate
  ASSERT writing.templateTypes contains "complaint"
  selectLetterTemplate type="complaint"
  ASSERT writing.templateSections == "salutation|opening|body|request|closing|signoff"
  insertLetterPhrase phrase="Szanowni Państwo"
  ASSERT writing.draft contains "Szanowni Państwo"
  writeDraft text="Szanowni Państwo\nChciałbym zgłosić reklamację."
  nextWriting
  submitWriting
  ASSERT writing.reviewCount == 2
END
```

(Exact phrase text is taken from the first `isPreferred: true` salutation phrase in the authored
data. `writing.templateVisible` is `false` for tasks with no `LetterTaskLinkRow` entries — no toggle
shown. The playtest file is finalized in Task B once the data is authored in Task A.)

### AC-3 (web standalone — Task B gate, playtest)

Implemented as `dsl/exam-web/writing-letter-templates-browse.playtest`:

```
TEST "Letter templates standalone browser lists templates with sections and model letter" PROFILE playmode
  open route="exam"
  openTemplatesBrowser
  ASSERT writing.templateTypes contains "complaint"
  selectLetterTemplate type="formal_email"
  ASSERT writing.templateSections == "salutation|opening|body|request|closing|signoff"
  ASSERT writing.templateModelLetter != ""
END
```

Both playtest files are run by `runExamPlaytest` in `exam-playtest-host.ts`. The 3 new actions
(`openLetterTemplate`, `selectLetterTemplate`, `insertLetterPhrase`) and 3 snapshot keys
(`writing.templateVisible`, `writing.templateSections`, `writing.templateTypes`) plus
`writing.templateModelLetter` are added to `exam-playtest-host.ts` as part of Task B.

### AC-4 (Android companion — Task C gate)

Open a B2 writing task linked to `formal_email` template (via `letter_task_link.core-data`) on
Android emulator → `AndroidLetterTemplatePanel` shown below guidance, collapsed by default.
Expand → email sections visible. Tap a phrase → phrase text appended to draft. Phrases match web
content (shared `LetterTemplateSourceData`). A task with no `LetterTaskLinkRow` rows shows no panel.

### AC-5 (iOS bridge — Task D gate)

`IosLetterTemplateSession.snapshot(genre: "letter")` returns non-null data. SwiftUI `LetterTemplateView`
renders at minimum the section list and phrase count per section for the complaint template.

### AC-6 (register isolation)

Formal complaint template: `Salutation` section phrases all have `register: formal`. Informal email
template: `Salutation` section phrases all have `register: informal`. No cross-register mixing in a
single template's phrase list (enforced by `LetterTemplateModelTest.formalTemplateHasOnlyFormalPhrases`
and an equivalent informal assertion).

### AC-7 (content accuracy — mistakes)

Formal letter template `LetterMistakeRow` entries include at minimum:
- closing punctuation: `wrongForm: "Z poważaniem,"` / `correctForm: "Z poważaniem"` — `verificationStatus: verified`, source URL cited in `sourceNote`
- spelling: `wrongForm: "Z pozawaniem"` / `correctForm: "Z poważaniem"` — `verificationStatus: verified`, source cited (direct motivating example)
- closing register: `wrongForm: "Buziaki" / "Cześć"` in formal letter / `correctForm: "Z poważaniem"` — `verificationStatus: verified`
- salutation agreement: `wrongForm: "Szanowny Pani"` / `correctForm: "Szanowna Pani"` — `verificationStatus: verified`, source cited
Salutation punctuation (comma vs "!") is a `noteNative` on the `salutation` section, not a mistake
row — both forms are correct; the mistake is mixing them, documented in the section note.
Pan/Pani capitalization is a `noteNative` tip ("Zaleca się pisać Pan/Pani wielką literą jako wyraz
szacunku"), not a `LetterMistakeRow`.

### AC-8 (E2E walk — web + Android, playtest + emulator)

**Web (playtest):** Covered by `writing-letter-template.playtest` above (AC-2). Additionally:
Exam → B2/2019 → Writing → Set I → Task 1 (formal letter) → open template → select "Prośba"
(request template) → navigate to `signoff` section → insert preferred phrase "Z wyrazami szacunku"
→ `writing.draft` contains "Z wyrazami szacunku" (phrase inserted without destroying existing draft
content). "Z wyrazami szacunku" is the preferred sign-off for request letters (more deferential than
"Z poważaniem").

**Android (emulator walk):** Exam → B1/sample → Writing → genre `letter` task → template panel
visible and expandable → tap salutation phrase → phrase text appears in draft → submit →
review shows draft including inserted phrase text. App does not crash; back navigation works.

### AC-9 (en-ru graceful degradation)

Switch active pack to en-ru (if available) → open any writing task → no "Шаблон" toggle shown
(en-ru `letter_task_link.core-data` has no rows) → no exception, no console error.
`LetterTemplateModel.forTask(anyTaskId)` returns empty list for en-ru.

---

## Developer task order

Each task is one feature × one concern. Sequential by dependency.

Each task is one developer, one file unit, ~100–150 lines (BestPractice §8).

| Task | Scope | Gates |
|---|---|---|
| **A1: Contract + infrastructure** | `letter-template.data-contract` with top-level ENUMs; register in `dsl/biome.project`; `biome generate` → `LetterTemplateRows.kt`; `LetterTemplate.kt` (domain types + typealiases, NO redeclared enums); `LetterTemplateModel.kt`; `LetterTemplateProvider.kt` + `StaticLetterTemplateProvider`; `PlRuLetterTemplateRows.kt` mapper; `LetterTemplateSourceData.kt`; `LetterTemplateModelTest.kt` with 1-template seed in `.core-data` to prove generate→mapper→model chain | `biome generate --check`, `:shared:desktopTest` with AC-1 partial (1 template, all native tests green on seed data) |
| **A2: Content authoring** | Remaining 5 templates + all task link rows across the 6 files in `courses-biome/pl-ru/letter-templates/`; all rows `verificationStatus: verified`; real B1/B2 task ids in `letter_task_link.core-data` | AC-1 full (6 templates), all 6 `LetterTemplateModelTest` assertions green |
| **B1: Web companion panel** | `LetterTemplateWeb.kt` (panel component only); `ExamWritingWeb.kt` toggle (shown when `model.forTask(task.id).isNotEmpty()`); phrase→draft insertion at caret; `exam-playtest-host.ts` new actions/snapshot keys; `writing-letter-template.playtest` | AC-2, AC-8 (web), AC-9 |
| **B2: Web standalone browse** | `ExamWeb.kt` standalone "Шаблоны" link + `ExamScreen.Templates`; browse renderer in `LetterTemplateWeb.kt`; `writing-letter-templates-browse.playtest` | AC-3 |
| **C: Android** | `AndroidLetterTemplatePanel.kt` (companion collapsible, phrase tap appends to draft); integration into `AndroidExamWritingScreen.kt` | AC-4, AC-8 (Android), `:androidApp:testDebugUnitTest` |
| **D: iOS** | `IosLetterTemplateSession.kt` in `kotlin/apple-bridge/src/iosMain/kotlin/polski/ios/`; SwiftUI `LetterTemplateView` | AC-5, simulator walk |

---

## Documentation and handoff

- **Content accuracy owner: user.** All authored phrases in `courses-biome/pl-ru/letter-templates/`
  must be verified against cited Polish sources before `verificationStatus: verified`.
  Content that is correct but not independently sourced is `inferred` (no badge in UI — reviewed
  by main agent). Content needing a source check ships as `needs_review` (badge shown).
- **Sources to cite:** Poradnia językowa PWN (poradnia.pwn.pl), official Certyfikat Polski
  exam sample arkusze and klucze (PDF), Certyfikat Polski marking rubrics.
- **Task A1 developer steps:**
  1. Add `letter-template.data-contract` to `dsl/` with top-level ENUMs (`LetterRegister`,
     `LetterVerificationStatus`) and 6 RECORD types (including `LetterTaskLinkRow`).
  2. Create `courses-biome/pl-ru/letter-templates/` directory. Register the contract and all 6
     `.core-data` file paths in `dsl/biome.project` (follow `exam.data-contract` pattern).
  3. Author a minimal 1-template seed across the 6 files: 1 complaint `LetterTemplateRow`,
     2 `LetterSectionRow`, 2 `LetterPhraseRow`, 1 `LetterMistakeRow`, 1 `ModelLetterRow`,
     1 `LetterTaskLinkRow` (a real B2 task id) — enough to prove the generate chain.
  4. Run `biome generate` to emit `LetterTemplateRows.kt`; verify generated enum naming
     (`LetterRegister`, `LetterVerificationStatus`, `LetterTemplateRowLetterType`,
     `LetterSectionRowSectionKey`). Also verify `LetterTaskLinkRow` rows.
  5. Write `PlRuLetterTemplateRows.kt` mapper (follow `PlRu*Rows.kt` pattern; include
     `LetterTaskLink` domain type; en-ru derive deferred — no `derive-enru-mappers.mjs` change
     until en-ru data exists).
  6. Write `LetterTemplate.kt` (domain data classes + `LetterTaskLink` + typealiases, NO
     redeclared enums), `LetterTemplateModel.kt`, `LetterTemplateProvider.kt`,
     `LetterTemplateSourceData.kt`.
  7. Write `LetterTemplateModelTest.kt`; run `:shared:desktopTest` — seed data sufficient
     for `forTaskReturnsLinkedTemplates` and `forTaskReturnsEmptyForUnlinkedTask`.
- **Task A2 developer steps:**
  1. For each of the remaining 5 templates: open the source URL (PWN Poradnia answer or exam
     PDF), author phrases/mistakes/model letter across the 5 files, set
     `verificationStatus: verified` with the URL.
  2. Author `LetterTaskLinkRow` rows for all relevant real B1/B2 task ids.
  3. Run `biome generate --check` and `:shared:desktopTest` after each template addition.
  4. Gate: all 6 `LetterTemplateModelTest` assertions green; `modelLetterPresentForAllTemplates`
     confirms all 6 have model letters.
- **Playtest spec files:** `dsl/exam-web/writing-letter-template.playtest` and
  `writing-letter-templates-browse.playtest` are driven by the existing `runExamPlaytest` harness
  in `tests/browser/exam-playtest-host.ts`. No new `.spec.ts` file; no `playwright.kotlin.config.ts`
  change required.
- **Desktop:** frozen. Add exhaustiveness arms for any new enum values only.
- **Display policy resolved:** `needs_review` rows show a badge (⚠). `inferred` rows show no
  badge (reviewed by main agent; content is correct but unsourced). `verified` rows show source
  on demand. Task A aims for all 6 MVP templates fully `verified`.

---

## Principles compliance

Sources: PRINCIPLES.md P1–P25; ARCHITECTURE §3 (layer ownership); BestPractice §4 (тесты на
DSL), §8 (проверка каждой правки); ProjectMigratioPrincipals §Куда помещать намерение,
§Практика типов, §Выбор: DSL, шаблон или утилита.

| Principle | Decision | Classification | Reference |
|---|---|---|---|
| **P4** (content in DSL, not Kotlin) | Phrases, sections, mistakes, model letters in `.core-data`; Kotlin holds only IO/rendering | ✓ | BestPractice §4; ProjectMigratioPrincipals §Куда помещать: "поведение, приёмка → .playtest" |
| **P7** (один факт — одно место) | Enum domains declared once: shared top-level ENUM for `LetterRegister`/`LetterVerificationStatus`; inline enum for single-record types. Domain data classes use generated types via typealias, not redeclared value sets | ✓ | ProjectMigratioPrincipals §Практика типов: "Тип, повторяющий имена реестра фактов — вторая копия; ручной второй словарь типов запрещён" |
| **P9** (слои) | generated rows → mapper → domain model → provider → host adapters; no reverse dep; LetterTemplateModel zero deps on TrainingStore/FSRS/AppUiState | ✓ | ARCHITECTURE §3 layer table |
| **P10** (правило в своём слое) | Section structure, phrase register, verification status are DECLARE-layer data; genre lookup and ordering checks are mechanical native operations; no PROGRAM in DSL | ✓ | ARCHITECTURE §3: "DECLARE — типы, словарь, грамматика; обход/сортировка/IO → Core или native" |
| **P11** (нет рукописного JSON) | All template data in Biome line DSL | ✓ | — |
| **P13** (словарь из продуктов) | 5 RECORDs + 2 ENUMs: 1 consumer (polski) → `experimental`. `stable` after en-ru provides second C1+ consumer | ✓ per rule | PATTERN_DICTIONARY §5 |
| **P14** (аддитивность) | `LetterPracticeRow`, `invitation` template, en-ru data are additive | ✓ | — |
| **P16** (генерат не правится) | Generated output in `commonMain/generated/` only from `biome generate`; mapper is hand-authored in separate file | ✓ | — |
| **P17** (доказательство — исполнение) | AC-1 to AC-9 are runnable; each host's NOT RUN stated per task | ✓ | — |
| **P22** (размер файлов) | A1 target: `LetterTemplate.kt` ≤ 60 lines, `LetterTemplateModel.kt` ≤ 25 lines; B1/B2 each ≤ 150 lines; one task = one file unit | ✓ | BestPractice §8: "one developer task = one file unit, ~100–150 lines" |
| **P25** (граница self-hosting) | Native remainders classified below; no rule or expectation hidden in helper | ✓ | ARCHITECTURE §3; ProjectMigratioPrincipals §Выбор |

### Native remainder classification (P25)

| Remainder | Location | Class | Reason |
|---|---|---|---|
| Task lookup (`forTask`) | `LetterTemplateModel.kt` | Mechanical / measured-better native | Pure list filter over task link records; S4 (1 consumer); native ≤ DSL PROGRAM by token count |
| Sections ordered 1..N check | `LetterTemplateModelTest` | Small case — ordering invariant on record set | Not expressible in data-contract; P8 "малый механический случай может быть нативным" |
| Verified row has sourceNote | `LetterTemplateModelTest` | Small case — cross-field conditional | data-contract has no cross-field predicate syntax; P8 |
| Register isolation check | `LetterTemplateModelTest` | Authoring-invariant test | Data quality invariant, not application logic; P8 |
| Phrase→draft insertion | `LetterTemplateWeb.kt`, `AndroidLetterTemplatePanel.kt` | Platform / DOM/Compose mechanics | Physical DOM caret / Compose text state — platform binding (ProjectMigratioPrincipals §Куда помещать: "физический clock/SDK") |
| JSON snapshot for iOS bridge | `IosLetterTemplateSession.kt` | Platform / transport | Apple-platform transport mechanics, not application logic |
