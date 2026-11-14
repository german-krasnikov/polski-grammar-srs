# Dedup migration: DSL constructs, data-contracts, and where DSL is the wrong tool

Scope: `ProgressRepository` boilerplate (9 repeated patterns), iOS↔macOS twins (`VocabularySession`,
`MatrixSnapshot`), `UiCopy.kt` (59 boilerplate properties). For each target: measured verdict
(DSL/data-contract vs native Kotlin), generated output if DSL applies, LOC removed, acceptance cases.

Read: `/Users/german/Work/Python/biome-dsl/AI/ARCHITECTURE.md`, `BestPractice.md`, `PRINCIPLES.md`.
Project already runs biome-dsl: `dsl/biome.project`, `dsl/*.data-contract`, generated Kotlin committed
under `kotlin/shared/src/commonMain/generated/`. `UiCopyRow(key, text)` is **already** DSL-generated
per pack (`GENERATE course-plru-uicopy` / `course-enru-uicopy` in `dsl/biome.project:69-70`) — the
duplication in scope is the hand-written *accessor* object, not the data.

Per PRINCIPLES P4/P10/P25: DSL owns decisions, grammar, cross-language rules. CORE/utilities/physics
stay native. A transfer that is longer than the native alternative, or that has no multi-language/
multi-decision payoff, loses the measurement and stays native. Two of the three targets below are
native fixes; only one is a genuine DSL win. This is the honest result of applying P25, not a partial
answer — see each section's "Verdict" line.

---

## 1. UiCopy.kt — DSL win (DECLARE + TEMPLATE)

**Files:** `kotlin/shared/src/commonMain/kotlin/polski/presentation/UiCopy.kt` (80 lines, 59
`val Name: String get() = text("key")` properties + 3 parameterized functions + `all()`).
**Data already in DSL:** `dsl/course.data-contract:199-202` `RECORD UiCopyRow KEY key`, rows in
`courses-biome/{pl-ru,en-ru}/uiCopy.core-data`, printed to
`kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/{plru,enru}/uicopy/UiCopyRows.kt`.

**Verdict: DSL win.** The Kotlin property *names* (`ExamCheck` → key `"check"`, `ProgressTab` →
`"progressTab"`) are not a mechanical case transform of the key — several disambiguate (`ExamCheck`,
`ExamYourAnswer`) — so this is a genuine declared mapping, not something a codegen-free
capitalize-the-key script could produce. Per BestPractice §5 (CODE construct: `PATTERN CODE <name>
SEVERITY <s> DOC <d>` → TEMPLATE emits types+docs), this is exactly "grammar + template instead of a
second hand-written copy." The real payoff is not LOC (DSL source and generated Kotlin are near
1:1 in line count) — it's **P5 (guarantees, not volume)**: today a typo or renamed key in
`uiCopy.core-data` is invisible, because `text()` falls back to the key string itself
(`UiCopy.kt:80`); `generate --check` can instead fail the build.

### DSL construct (NEW: `dsl/ui-copy.construct` + `dsl/ui-copy.declare`)

```text
# dsl/ui-copy.construct
PATTERN UI_COPY <name:token(IDENT) UNIQUE> KEY <key:string>
DECLARE
PATTERN UI_COPY_PREFIX <name:token(IDENT) UNIQUE> PREFIX <prefix:string>
DECLARE
```

`UI_COPY` declares one fixed-key property (the 56 non-parameterized entries). `UI_COPY_PREFIX`
declares the 3 parameterized lookups (`languageName`, `languageAdverb`, `tenseLabel`) that index by a
runtime argument (`"lang.$code"`, `"adverb.$code"`, `"tense.$tense"`) — these stay generated as
functions, not enumerated, because the argument domain (language/tense codes) is open-ended data, not
a closed key set; enumerating it would duplicate `polski.model` enums for no guarantee (P25 S3).

```text
# dsl/ui-copy.declare — one line per UiCopy.kt property today, same order
UI_COPY Again KEY "again"
UI_COPY Good KEY "good"
...
UI_COPY ExamCheck KEY "check"
...
UI_COPY Missions KEY "missions" PARTIAL   # M9: pl-ru only; en-ru intentionally falls back to "missions"
UI_COPY_PREFIX languageName PREFIX "lang."
UI_COPY_PREFIX languageAdverb PREFIX "adverb."
UI_COPY_PREFIX tenseLabel PREFIX "tense."
```

`PARTIAL` is the one clause that is *not* boilerplate removal — it encodes the existing comment at
`UiCopy.kt:33-34` ("pl-ru pilot only — en-ru falls back to this key's own name") as a checked fact
instead of a comment nobody re-verifies.

### Generated output

`kotlin/shared/src/commonMain/generated/kotlin/polski/presentation/UiCopy.kt`:

```kotlin
// Generated from dsl/ui-copy.declare. Edit the source, then run biome generate.
package polski.presentation
import polski.data.packRegistry

object UiCopy {
    fun all(): Map<String, String> = packRegistry.active.uiCopy.toMap()
    val Again: String get() = text("again")
    val Good: String get() = text("good")
    // ... one line per UI_COPY fact, identical body shape to today's hand-written file
    val ExamCheck: String get() = text("check")
    val Missions: String get() = text("missions")
    fun languageName(code: String): String = text("lang.$code")
    fun languageAdverb(code: String): String = text("adverb.$code")
    fun tenseLabel(tense: String): String = text("tense.$tense")
}
private fun text(key: String): String = packRegistry.active.uiCopy[key] ?: key
```

Byte-identical to the current hand-written file modulo the header comment — this is intentionally a
**zero-behavior-change move**, not a reshaping.

### Check step (the actual win)

New `GENERATE ui-copy-coverage derive {dir}/ui-copy-coverage.mjs` step (or a `.sources` grid test) that:
1. Reads `UI_COPY`/`UI_COPY_PREFIX` facts from `ui-copy.declare`.
2. Reads the generated `UiCopyRows.kt` (or the `.core-data` rows directly) for each pack.
3. For every non-`PARTIAL` `UI_COPY key`, asserts the key exists in **every** pack's row set.
4. For `PARTIAL` keys, asserts the key is declared at least once (catches a pure typo) but does not
   require every pack.

Failure is a `generate --check` / `gate changed` red, not a silent runtime fallback.

### LOC removed

~80 hand-maintained lines move to `generated/` (removed from the manually-edited surface, same
convention as `UiCopyRows.kt` today). DSL source (`ui-copy.declare`) is ~62 lines — comparable size,
by design (P6: this is a guarantee swap, not a line-count win; reported honestly rather than inflated).

### Acceptance cases

- AC1: `biome generate --check --project dsl/biome.project` is green before and after; generated
  `UiCopy.kt` is byte-identical in observable behavior (same keys, same fallback-to-key-name).
- AC2: Renaming a key in `pl-ru/uiCopy.core-data` without updating `ui-copy.declare` (or vice versa)
  fails `generate --check`, not silently at runtime. New negative fixture under
  `dsl/ui-copy-coverage.sources` (or equivalent `.sources` grid) proves it (P15/P17: independent
  expectation, real failure, not NOT RUN).
- AC3: `Missions` stays `PARTIAL`; en-ru continues to render the literal string `"missions"` exactly as
  today (M9 pilot behavior unchanged) — existing UI/Playwright fixtures covering that string keep
  passing unmodified.
- AC4: Existing Kotlin consumers of `UiCopy.X` (compose screens, both web/desktop/android/apple hosts)
  compile unchanged — this is a pure codegen relocation, no call-site edits.

---

## 2. ProgressRepository boilerplate — native Kotlin win, DSL verdict: NO

**Files:** `kotlin/srs-progress/src/commonMain/kotlin/polski/progress/ProgressRepository.kt` (244,
already shared for Web via `RawProgressRepository`/`RawKeyValueStore`),
`androidApp/.../AndroidProgressRepository.kt` (136), `apple-bridge/iosMain/.../IosProgressRepository.kt`
(107), `apple-bridge/macosMain/.../MacProgressRepository.kt` (124),
`composeApp/desktopMain/.../DesktopProgressRepository.kt` (124). 763 lines total across 6 files for one
contract (`ProgressRepository`).

**Verdict: no DSL/data-contract.** This is control-flow and resource-lifecycle boilerplate (verified
write + backup + mutex + cancellation passthrough + decode dispatch), not a data shape and not a
cross-language decision table — PRINCIPLES P4 ("CORE, утилиты — нативные"), P10 ("Физика: SDK, FS,
транспорт → Нативная утилита за контрактом. Утилита не решает") and P25 S1 ("длиннее натива") all land
on native here: every one of the 9 patterns already has a canonical Kotlin expression
(`RawProgressRepository` for Web), and a DSL TEMPLATE reproducing suspend/Mutex/withContext glue would
be strictly longer and less debuggable than extending that existing class. **The fix is
module-architecture, not DSL**: push the 9 patterns down into one shared commonMain implementation,
and shrink each platform to a ~15–25 line synchronous storage adapter.

### The 9 repeated patterns (named, with current copies)

1. `loadUnlocked()`: decode → `LoadResult.Loaded/Invalid/Unsupported` dispatch — 4 copies (android,
   ios, macos, desktop; `ProgressRepository.kt:91-96` is the canonical shape).
2. `save()` precondition: current state must be `Missing`/`Loaded`, else passthrough `Unavailable` or
   fail — 4 copies.
3. Encode → write → `check(read-back == raw)` inside `save()` — 4 copies.
4. `if (error is CancellationException) throw error` rethrow — 8+ occurrences across the 4 files.
5. `Mutex().withLock { ... }` wrapping load/save — 4 copies (android/ios/macos/desktop each declare
   their own `Mutex`).
6. `resetConfirmed = save(ProgressCodec.fresh(...))` — byte-identical one-liner, 4 copies.
7. `migrateLegacy` stub returning `MigrationResult.RecoveryRequired("", "<localized message>")` — 4
   copies, structurally identical, only the string differs.
8. Import flow: decode-validate → backup previous (UUID-named) → write raw → read-back check — 4
   copies (`AndroidProgressRepository.importJson`, `IosProgressRepository.importJson`,
   `MacProgressRepository.importJson`, `DesktopProgressRepository.importJson`).
9. Per-platform result type for #8 instead of one shared contract: `ImportResult` (android, used by
   `AndroidSessionViewModel.kt` and its test), `DesktopImportResult` (desktop, same 4 cases,
   different name), bare `String?` (ios, macos) — same semantics, 3 different public shapes.

### Native design (NEW, in `srs-progress` commonMain)

```kotlin
/** Synchronous, platform-provided byte store. No coroutines, no mutex — the shared repository owns those. */
interface ProgressBlobStore {
    fun read(): String?
    /** Copies the current stored bytes to a backup location (no-op if nothing stored yet), then writes [raw]. */
    fun backupThenWrite(raw: String)
    /** Plain write with no backup (used by save(), which never needs one — only import does). */
    fun write(raw: String)
}

sealed interface ImportResult {               // replaces ImportResult + DesktopImportResult + String?
    data object Imported : ImportResult
    data class Invalid(val reason: String) : ImportResult
    data class Unsupported(val version: Int) : ImportResult
    data class Failed(val cause: Throwable) : ImportResult
}

/** Owns patterns 1–9. Platforms implement [ProgressBlobStore] only. */
open class BlobProgressRepository(
    private val store: ProgressBlobStore,
    private val scheduler: Scheduler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ProgressRepository {
    private val lock = Mutex()
    override suspend fun load(): LoadResult = withContext(dispatcher) { lock.withLock { loadUnlocked() } }
    override suspend fun save(document: ProgressDocument): SaveResult = withContext(dispatcher) {
        lock.withLock { /* pattern 2 + 3, exactly once */ }
    }
    override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult =
        save(ProgressCodec.fresh(skillIds, at, localDay, scheduler))               // pattern 6, exactly once
    override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult =
        MigrationResult.RecoveryRequired("", recoveryMessage())                     // pattern 7, message is the only override point
    protected open fun recoveryMessage(): String = "Import a compatible JSON file explicitly"
    suspend fun importJson(raw: String): ImportResult = withContext(dispatcher) {
        lock.withLock { /* pattern 8 + 9, exactly once, using store.backupThenWrite */ }
    }
    private fun loadUnlocked(): LoadResult = /* pattern 1, exactly once */
}
```

Each platform shrinks to:

```kotlin
class AndroidProgressRepository(context: Context, scheduler: Scheduler) :
    BlobProgressRepository(AndroidBlobStore(context), scheduler, Dispatchers.IO) {
    override fun recoveryMessage() = "Import a compatible JSON file explicitly"
}
private class AndroidBlobStore(context: Context) : ProgressBlobStore { /* AtomicFile read/write/backup, ~15 lines */ }
```

iOS/macOS keep their localized recovery strings and (macOS only) `recoveryRaw()`/exposing the backup
path — both become a 2–3 line override, not a re-implementation. `Dispatchers.IO` (android/desktop) vs
`Dispatchers.Default` (ios/macos, no JVM IO dispatcher on Kotlin/Native) stays an explicit constructor
argument — the one genuinely platform-shaped knob, not boilerplate.

### LOC removed

763 → roughly: `BlobProgressRepository` ~90 lines (once, shared) + `ProgressRepository.kt`'s existing
244 (unchanged, `RawProgressRepository`/Web path untouched) + 4 platform files at ~20–30 lines each
(≈100 total) ≈ **434 lines, down from 763** (~43% reduction), concentrated entirely in the 4 platform
files (136+107+124+124 = 491 → ~100). The `Web` path (`WebProgressRepository`, 28 lines) is unchanged;
it could also be rebased onto `BlobProgressRepository` later for full consistency, but its current
`RawProgressRepository`-by-delegation (migration/backup semantics tied to browser legacy keys) is a
real behavioral difference (patterns 1/2/3/6 only, no import/#8/#9), not pure duplication — left as a
follow-up, not folded into this LOC count.

### Acceptance cases

- AC1: Existing platform tests (`AndroidProgressRepositoryTest`, `IosProgressRepositoryTest`,
  `MacProgressRepositoryTest`, `DesktopProgressRepositoryTest`, `ProgressRepositoryTest`) pass unchanged
  against the refactored classes — behavior preservation, not a rewrite (module-architecture skill:
  "Preserve public Kotlin APIs... across adapters").
- AC2: `AndroidSessionViewModel.kt`'s use of `ImportResult` compiles against the shared sealed type;
  grep confirms no remaining reference to a platform-local `ImportResult`/`DesktopImportResult`.
  (`grep -rl "ImportResult" kotlin --include="*.kt"` currently returns the 2 Android files + 2 desktop
  files — both collapse onto `polski.progress.ImportResult`.)
- AC3: Cancellation during `load`/`save`/`importJson` on every platform still propagates
  `CancellationException` (single rethrow site in `BlobProgressRepository`, tested once, not 4 times).
- AC4: `recoveryMessage()` override preserves today's exact localized strings
  ("Импортируйте совместимый JSON явно" ios/macos, "Import a compatible JSON file explicitly"
  android/desktop) — fixture per platform asserting the literal string, so a refactor slip is caught.
- AC5: Backup-before-import still happens exactly once per import, byte-verified read-back, on every
  platform (existing `*.importJson` tests already assert this; they must not need behavior changes).

---

## 3. iOS↔macOS twins — native Kotlin win (`appleMain`), DSL verdict: NO

**Files:** `apple-bridge/src/iosMain/.../IosVocabularySession.kt` (161) ↔
`apple-bridge/src/macosMain/.../MacVocabularySession.kt` (191); `IosMatrixSnapshot.kt` (274) ↔
`MacMatrixSnapshot.kt` (209).

**Verdict: no DSL.** DSL's entire payoff for a transfer is multi-language output (TS/C#/Python/Kotlin)
or a cross-language decision table (PRINCIPLES P2, P25 V1–V5). Both twins compile to the **same**
target language (Kotlin/Native) for the **same** paradigm (a SwiftUI scene bridge). There is no
multi-language axis to abstract over, so introducing a DSL layer here adds a codegen indirection with
no guarantee payoff — pure P6 loss ("новое слово убирает больше, чем добавляет") and P25 stop-signal S1
(a TEMPLATE reproducing this Kotlin would be longer than the Kotlin it replaces). The actual tool is
already configured and unused: `apple-bridge/build.gradle.kts:22` calls `applyDefaultHierarchyTemplate()`
with targets `iosArm64`, `iosSimulatorArm64`, `macosArm64` — Kotlin's default hierarchy template
**already creates an `appleMain`/`appleTest` intermediate source set** shared by exactly `iosMain` and
`macosMain`. Nobody has put a file in `src/appleMain/kotlin/` yet.

### Native design

**`AppleVocabularySession` (NEW, `apple-bridge/src/appleMain/kotlin/polski/apple/AppleVocabularySession.kt`)**
— `abstract class` owning the ~58 shared lines: scope/session lifecycle (`observeAndStart`,
`rebuildIfCourseSwitched`, `currentSnapshot`, `exportJson`, `close`), the dispatch skeleton's identical
branches (`filter`/`typed`/`draft`/`reveal`/`refresh`/`select`/`deselect`/`delete`), `saveCustom`,
`importJson`, and the snapshot's shared header + `entries`/`coverage` construction + `itemJson`. Platform
subclasses provide only what actually differs:

```kotlin
abstract class AppleVocabularySession {
    protected abstract fun newSession(): VocabularySession
    protected abstract fun directionFor(wire: String): StudyDirection?      // Ios: activeStudyDirections; Mac: studyDirectionOptions(pack)
    protected open fun onRated(rating: Rating) { onEffect?.invoke(cardEffectFor(rating).name) }  // Mac's rate() becomes the same default
    protected open fun putExtraSnapshotFields(builder: JsonObjectBuilder, state: VocabularyUiState) {}  // Mac-only: directionOptions/promptCaption/recallTarget/intervals
    // dispatch(), currentSnapshot(), exportJson(), saveCustom(), importJson(), close(), snapshot() — written once, call the 4 hooks above
}

class IosVocabularySession(private val defaults: NSUserDefaults = ...) : AppleVocabularySession() {
    override fun newSession() = VocabularySession(IosVocabularyRepository(defaults), FsrsScheduler(), ...)
    override fun directionFor(wire: String) = activeStudyDirections.firstOrNull { it.wire == wire }
}
class MacVocabularySession(private val directory: String) : AppleVocabularySession() {
    override fun newSession() = VocabularySession(MacVocabularyRepository(directory), scheduler, ...)
    override fun directionFor(wire: String) = studyDirectionOptions(pack.target, pack.native).map { it.direction }.firstOrNull { it.wire == wire }
    override fun putExtraSnapshotFields(builder, state) { /* directionOptions/promptCaption/recallTarget/intervals, unchanged */ }
}
```

**`AppleMatrixSnapshot` common fields (NEW, same `appleMain` source set)** — a shared
`fun JsonObjectBuilder.putCommonMatrixFields(state: AppUiState)` covering the ~34 identical lines:
`section`/`matrixIntroduction`/`aspectNoPresent`/selection ids, `nouns`/`adjectives`/`owners`/`numbers`/
`verbs` choice arrays, `pipelineTitle/Summary/Example`, `supportTitle/Lines/Rows`, `caseNote`,
`systemCards`, `maleAccRows`, `comparisonCases`, `verbGenderControlLabel/Options/FutureExplanation/
TenseLabels`, `pronounIntro/Footer/Contexts`, `possessiveTitle/Cases` — every field that is
byte-identical between `IosMatrixSnapshot.kt` and `MacMatrixSnapshot.kt` today. `cases`/`comparison`/
`verbsRows`/`pronouns`/`possessives`/`chainRows`/`tenseRows`/`aspectRows`/English-matrix fields stay
platform-specific functions in `iosMain`/`macosMain` **as-is** — they are a real, intentional wire-shape
divergence (iOS's own code comment: Swift binds to "named per-section fields, not a generic grid";
macOS mixes the newer `MatrixTableViewModel.toJson()` shared-builder path with legacy hand-built fields
for back-compat). Converging those shapes is a separate, larger SwiftUI-side decision (out of scope for
a dedup pass) — not something this plan should fold in as "duplication."

### LOC removed

- VocabularySession: 161 + 191 = 352 → `AppleVocabularySession` ~70 (shared, includes the hooks) +
  `IosVocabularySession` ~45 + `MacVocabularySession` ~75 (keeps its extra snapshot fields) ≈ 190,
  **down ~162 lines** (one copy of the 58 shared lines plus the surrounding method-declaration ceremony
  that duplicated along with it).
- MatrixSnapshot: 274 + 209 = 483 → `putCommonMatrixFields` ~45 (shared) + `IosMatrixSnapshot` ~230 +
  `MacMatrixSnapshot` ~165 ≈ 440, **down ~43 lines** (the 34 shared lines plus their `put(...)` call
  ceremony; most of each file's bulk is the genuinely divergent per-table JSON, which stays).

### Acceptance cases

- AC1: `IosVocabularySessionTest`, `MacVocabularySessionTest`, `IosMatrixSnapshotTest` (existing, under
  `apple-bridge/src/{ios,macos}Test`) pass unchanged against the refactored classes — wire JSON shape
  byte-identical field-for-field, verified by the existing fixtures (no new fixtures needed if this is
  pure extraction).
  **Known gap:** no `MacMatrixSnapshotTest` currently exists in the file listing — flag before merge;
  add one asserting the shared-field block if it is genuinely missing, since `appleMain` sharing without
  a macOS-side regression test for those fields would be an unverified assumption, not evidence (P17).
- AC2: Changing a shared field in `AppleVocabularySession`/`putCommonMatrixFields` (e.g. `instructions`,
  `loadStatus`) changes both hosts' snapshot identically with a one-line edit — demonstrated by a
  deliberate single-line change + both test suites' JSON assertions updating together in one commit
  (this is the actual "one fact, one place" proof, not just a code-reading claim).
  Open decision for the developer step: whether the Mac-only `recoveryRaw()`-style extras and iOS's
  `forceSaveFailure`-equivalent UI-test seams (actually on `IosProgressRepository`, not
  `IosVocabularySession` — no seam exists here) need any hook; current reading found none needed.
- AC3: Real device/simulator build for both `iosSimulatorArm64Test` and `macosArm64Test` targets stays
  green (per `AI/decisions.md` and `Plans/Kotlin/iOS.md` conventions: native evidence, not
  common-only compile).

---

## Summary table

| Duplication | Verdict | Mechanism | LOC before → after |
|---|---|---|---|
| UiCopy.kt (59 properties) | **DSL win** | `ui-copy.construct`/`.declare` + TEMPLATE + per-pack key-coverage check | ~80 hand-written → generated; real new guarantee (typo/rename caught at `generate --check`) |
| ProgressRepository (9 patterns, 5 platforms) | Native (no DSL) | `BlobProgressRepository` + `ProgressBlobStore` port in `srs-progress` commonMain | 763 → ~434 (platform files 491 → ~100) |
| iOS↔macOS twins (`VocabularySession`, `MatrixSnapshot`) | Native (no DSL) | `appleMain` intermediate source set (already available via `applyDefaultHierarchyTemplate()`, unused) | 352 → ~190; 483 → ~440 |

## Documentation and handoff

- Owner for `dsl/ui-copy.construct`/`.declare` + check step: whoever runs the next `dsl-migration-architect`/
  `dsl-feature-architect` slice — this is the only item needing biome-dsl tooling changes (a new
  construct + a coverage check script/`.sources` grid), everything else is plain Kotlin.
- `AI/decisions.md` should gain one entry once implemented: "UiCopy accessors generated from
  `ui-copy.declare`, cross-checked against both packs' `UiCopyRow` tables" — matching the existing
  convention for other `GENERATE` entries (e.g. the ADR-55 / course-data entries already there).
  Do **not** add a decisions.md entry claiming "ProgressRepository/twins moved to DSL" — they did not;
  record them as module-architecture refactors if the project's changelog wants them noted at all.
- Each of the 3 sections' AC lists is the developer/tester handoff; no further design decision is
  pending except the two explicitly flagged open items (AC3 under twins: missing `MacMatrixSnapshotTest`;
  §3 Web `ProgressRepository` follow-up noted as out of scope, not silently dropped).
