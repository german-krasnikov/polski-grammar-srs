# DSL migration plan: Phase 1 (единый источник правды)

Дата: 2026-11-08. Синтез блоков A-G из задания, заземлённый на `Plans/Architecture/00-05`,
`dsl/biome.project`, `dsl/course.data-contract`, `dsl/derive-forms.mjs`, `dsl/derive-enru-mappers.mjs`,
`dsl/swipe/*`, Recipe.kt/ExerciseGenerator.kt/CardEffect.kt и реальные Rive/UI файлы 4 хостов
(прочитаны целиком или через параллельных ридеров в этой же сессии: classify-scan, rive-effects-scan,
uicopy-scan — их находки включены и перепроверены grep/`ls`/`wc`/`python3` там, где это было дёшево).

Все пути от корня polski; Kotlin — от `kotlin/`. Ничего из предложенного ниже не реализовано;
это план для Developer. `biome generate`/Gradle/npm — **NOT RUN** в рамках этой задачи.

**Примечание о состоянии дерева.** Задание утверждает «Phase 0 выполнена». В этом конкретном worktree
`kotlin/shared/src/commonTest/kotlin/polski/grammar/generate_parity.py` и
`kotlin/shared/src/commonTest/kotlin/polski/training/generate_parity.py` **всё ещё существуют**
(проверено `find`), хотя `00-UNIFIED-PLAN.md` отмечает шаг 0.4 как DONE и прогресс-заметка там же говорит
«не закоммичено — в working tree» (другого чекаута). Разработчику: перед блоком F подтвердить в своём
дереве, что оба мёртвых генератора удалены (`git ls-files '*generate_parity.py'` должен вернуть только
`progress/` и `srs/`); если не удалены — это блокер вне данного плана (Phase 0, не Phase 1).

## 0. Порядок и зависимости

```
A (Rive effects) ──> C (.riv sync)      # C нужна таблица имён asset из A
B (RiveGate)      — независим, параллельно с A
D (UiCopy)        — независим
E (classify)      — независим
F (GrammarParityTest) — независим, самый крупный (делать отдельным агентом)
G (progress/srs derive) — независим, самый дешёвый — делать первым для разогрева
```

Рекомендуемая последовательность одного агента за раз (P8, ~100-150 строк правки на шаг,
`feedback_smaller_agent_tasks`): **G → E → B → A → C → D → F**. G и E — самые маленькие и
наименее рискованные (чистые функции/контракты без хостового кода); F — самый большой, делать
последним и отдельной задачей с собственным Tester-проходом.

## Блок A. Rive effects → единый line-DSL + derive

### Факт (подтверждён rive-effects-scan + `ls -la` на `.riv`)

| Хост | Файл | Таблица |
|---|---|---|
| Android | `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidRiveOverlay.kt:48,56-63` (`effectSpecs()`) | Remembered = confetti `Trigger explosion` + again `Check`; Again = again `Error` |
| Web | `kotlin/composeApp/src/webMain/resources/rive/rive-bridge.js:18-26` (`RATING_EFFECTS`) | то же |
| iOS | `kotlin/iosApp/PolskiGrammar/RiveEffectOverlay.swift:52-68` | то же |
| macOS | `kotlin/macosApp/PolskiGrammarMac/RiveEffectOverlay.swift:38-57` | то же (КОД не дрейфнул; дрейфнул АССЕТ — см. блок C) |

Chain-complete: Android `AndroidRiveOverlay.kt:109-120`, web `rive-bridge.js:102-106`, iOS
`RiveEffectOverlay.swift:83`, macOS `RiveEffectOverlay.swift:65` — все четыре называют
`chain-complete`/`Tada`/`Reveal` одинаково (macOS код ссылается на файл, которого на диске нет —
отдельная находка, см. блок C).

### Решение: данные как `.core-data`, не новая DSL-конструкция

Таблица — перечисление (lookup), не ветвящееся решение; она ложится на существующий механизм
data-contract + `data generate` **без нового синтаксиса biome-dsl** (P13/P25): надёжнее и короче,
чем рукописный парсер произвольного line-формата, и даёт валидацию полей бесплатно (V4).

NEW `dsl/effects.data-contract`:

```
DSL 1
# The Rive rating-effect table and the chain-complete cue (one row, shared by all hosts): which
# logical asset(s) a card effect plays, on which state machine, with which trigger — and the
# animation the chain-complete celebration autoplays. The logical asset name is the file stem the
# sync-rive-assets script (block C) keeps identical across hosts; it is not a host path.
RECORD EffectPlayRow
  asset: string
  stateMachine: string
  trigger: string
END

RECORD CardEffectRow KEY effect COUNT 1..
  effect: enum remembered|again
  plays: [EffectPlayRow]
END

RECORD ChainCompleteRow COUNT 1
  asset: string
  artboard: string
  animation: string
END

RECORD RiveCatalogRow COUNT 1
  effects: [CardEffectRow]
  chainComplete: ChainCompleteRow
END
```

NEW `dsl/effects/card-effects.core-data` (one `RIVE_CATALOG` row, mirrors today's 4 copies exactly —
no behavior change on first migration):

```
DSL 1
RIVE_CATALOG {
  effects: [
    {effect: remembered plays: [
      {asset: "confetti" stateMachine: "State Machine 1" trigger: "Trigger explosion"}
      {asset: "again" stateMachine: "State Machine 1" trigger: "Check"}]}
    {effect: again plays: [
      {asset: "again" stateMachine: "State Machine 1" trigger: "Error"}]}]
  chainComplete: {asset: "chain-complete" artboard: "Tada" animation: "Reveal"}
}
```

NEW `dsl/derive-rive-effects.mjs` — reads the one row via `biome read` (pattern identical to
`derive-forms.mjs`'s `derive()` helper) and exports three pure-text printers:

- `kotlin(context)` → Kotlin object for commonMain (consumed by Android today; any future commonMain
  consumer gets it for free).
- `swift(context)` → one Swift file, written to **two** `OUT_*` paths (iOS and macOS) so both Apple
  targets get a byte-identical, `--check`ed copy without touching the Xcode project's shared-file
  wiring (same reasoning as `derive-enru-mappers.mjs`'s "copy, not shared reference" — Kotlin
  packages need separate files too; here two Xcode targets do).
- `js(context)` → a small standalone JS module with `RATING_EFFECTS`/`CHAIN_COMPLETE` constants,
  **not** inlined into the hand-written `rive-bridge.js` (avoids a `GENERATED START/END` hybrid,
  which 05-DATA-TESTING.md flags as a drift risk already present elsewhere in this repo).

NEW `dsl/biome.project` steps (append):

```
GENERATE effects-validate data --contract effects --rows RiveCatalogRow {dir}/effects/card-effects.core-data
GENERATE effects-kotlin derive {dir}/derive-rive-effects.mjs kotlin OUT_KOTLIN {dir}/effects/card-effects.core-data --out {dir}/../kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/effects/CardEffectSpecs.kt
GENERATE effects-ios derive {dir}/derive-rive-effects.mjs swift OUT_IOS {dir}/effects/card-effects.core-data --out {dir}/../kotlin/iosApp/PolskiGrammar/Generated/RiveEffects.swift
GENERATE effects-macos derive {dir}/derive-rive-effects.mjs swift OUT_MACOS {dir}/effects/card-effects.core-data --out {dir}/../kotlin/macosApp/PolskiGrammarMac/Generated/RiveEffects.swift
GENERATE effects-web derive {dir}/derive-rive-effects.mjs js OUT_JS {dir}/effects/card-effects.core-data --out {dir}/../kotlin/composeApp/src/webMain/resources/rive/rive-effects.generated.js
```

`effects-validate` has no `--target`/`--out` (precedent: `course-enru-frequency` line 27 of today's
`dsl/biome.project` — "a step without --target writes nothing"); it exists purely for the `--check`
guarantee on the hand-authored `.core-data` row.

### MODIFY (consume generated tables, delete hand copies)

- `AndroidRiveOverlay.kt`: delete `effectSpecs()` (lines 56-63) and `private class RiveEffectSpec`
  (line 48); read `CardEffectSpecs.kt`'s generated table instead, mapping `R.raw.<asset>` by name
  (Android resource names can't be generated directly — keep a tiny hand-written
  `assetResId(name: String): Int = when(name) { "confetti" -> R.raw.confetti; ... }` lookup, 3 lines,
  the only hand-written residue, same shape as today's `R.raw.*` references).
- `rive-bridge.js`: replace the inline `RATING_EFFECTS` object literal (lines 18-26) and the
  chain-complete literal (inside `playChain()`, lines 102-106) with
  `import { RATING_EFFECTS, CHAIN_COMPLETE } from './rive-effects.generated.js'` (or the project's
  existing script-loading convention for this file — verify how `rive-bridge.js` is loaded before
  choosing `import` vs a global).
- `RiveEffectOverlay.swift` (iOS) / `RiveEffectOverlay.swift` (macOS): replace the `switch event.name`
  tables (iOS lines 52-68, macOS lines 38-57) with lookups into the generated `RiveEffects` type.

### Acceptance

- `node <biome> generate --check --project dsl/biome.project` passes after a clean generate, fails on
  any manual edit to `CardEffectSpecs.kt`/`RiveEffects.swift`/`rive-effects.generated.js`.
- `grep -n "Trigger explosion\|State Machine 1" kotlin/composeApp/src/androidMain kotlin/iosApp kotlin/macosApp kotlin/composeApp/src/webMain/resources/rive/rive-bridge.js` → 0 hits outside the generated files.
- Existing host tests unaffected: `AndroidFlipCardTest` (Android), iOS `FlipCorrectnessUITests`/
  `PolskiGrammarUITests`, no macOS test exists today (04-doc) — add one asserting macOS plays the
  same triggers as iOS (closes the D1 gap, 04§8 "iOS и macOS играют одинаковые триггеры").

## Блок B. `RiveGate.decide` — единая гейтинг-функция

### Факт (rive-effects-scan, перепроверено построчно)

| Хост | Сигнатура/место | Природа |
|---|---|---|
| Android | `cardEffectToPlay(rating: Rating, reduceMotion: Boolean, riveDisabledForMeasurement: Boolean): CardEffect?` — `AndroidFlipCard.kt:78-79` | чистая функция, тесты `AndroidFlipCardTest.kt:39,43,47,51` |
| Web | `riveGated(): Boolean = motionInstantActive() \|\| disabledForMeasurement()` — `webMain/.../RiveEffectOverlay.kt:22,25,28` | читает DOM/`location.search`, не параметризована |
| iOS | `guard let event, !reduceMotion else { return }` — `RiveEffectOverlay.swift:47`; measurement-флаг — `@AppStorage`+`#if DEBUG` на уровень выше, в `PolskiGrammarApp.swift` | инлайн, 2 независимых места |
| macOS | то же, `RiveEffectOverlay.swift:39`; доп. `.onChange(of: reduceMotion)` на `:33` освобождает VM (iOS этого не делает — асимметрия, не часть этого блока) | инлайн |

### NEW `kotlin/shared/src/commonMain/kotlin/polski/presentation/RiveGate.kt`

```kotlin
package polski.presentation

/** Pure decision: should a host skip playing its Rive cue? Reduced motion, the app-wide Animations
 *  toggle, and a debug-only measurement override are independent inputs (04-ANIMATIONS-RIVE.md D2) —
 *  this replaces 3 ad hoc combinations with one truth table. */
object RiveGate {
    fun skip(reduceMotion: Boolean, animationsEnabled: Boolean, measurementDisabled: Boolean): Boolean =
        reduceMotion || !animationsEnabled || measurementDisabled
}
```

### MODIFY call sites

- Android `AndroidFlipCard.kt:78-79`: `cardEffectToPlay` becomes a 1-line wrapper:
  `if (RiveGate.skip(reduceMotion, animationsEnabled, riveDisabledForMeasurement)) null else cardEffectFor(rating)`.
  Requires separating `animationsEnabled` from `reduceMotion` at the call site (today the doc says
  Android/iOS/macOS fold the Animations toggle into `reduceMotion` before calling — this block makes
  that folding explicit at the call site instead of inside the gate, which is the point: **one**
  place now decides the combination, callers only resolve their own 3 raw settings).
- Web `RiveEffectOverlay.kt:22-28`: `riveGated()` keeps reading DOM/`location.search` (platform
  concern, stays native) but its body becomes
  `RiveGate.skip(motionInstantActive(), !animationsDisabledActive(), disabledForMeasurement())`
  (verify exact boolean polarity against today's `motionInstantActive()`, which already ORs reduced
  motion and animations-off — may need splitting that helper into its two inputs first).
- iOS/macOS `RiveEffectOverlay.swift`: `guard !reduceMotion` → `guard !RiveEffectsKt.gateSkip(...)`
  (via the existing Kotlin/Native shared-framework bridge — same mechanism `IosSnapshot`/
  `MacSnapshot` already use to call into `shared`).

### Acceptance

- NEW `shared/src/commonTest/kotlin/polski/presentation/RiveGateTest.kt`: full 2×2×2 truth table (8
  cases), independent expectations (not derived from the implementation).
- `AndroidFlipCardTest.kt` unchanged pass/fail outcomes (same 4 existing cases, now routed through
  `RiveGate.skip`).
- No DOM/location test regresses: `tests/browser/kotlin-flip-card.spec.ts` stays green (P15 — web's
  own Playwright expectation is independent, per 04-doc §6 Step 2).

## Блок C. `.riv` sync — один источник, `--check`

### Факт (проверено `find ... -exec ls -la`, не из плана)

```
confetti.riv      4597 B   android/ios/macos/web — identical
again.riv         2249 B   android/ios/web  |  5428 B macOS (STALE — pre-Pick-A asset)
chain-complete.riv 59088 B android/ios/web  |  MISSING on macOS (code references it, file absent)
```

Это расходится с `04-ANIMATIONS-RIVE.md` только в деталях (документ был не уверен про macOS
chain-complete; теперь подтверждено: файла нет).

### NEW `assets/rive/{confetti,again,chain-complete}.riv`

Canonical copies = the already-correct android/ios/web versions (2249/4597/59088 B). This is the
**source**; host copies become generated artifacts of a sync step, same spirit as `course-biome`
being the source for generated Kotlin rows.

### NEW `scripts/sync-rive-assets.mjs`

Binary files don't fit `biome generate`'s text-only `derive` contract (every existing `derive-*.mjs`
returns a string) — keep this **outside** `dsl/biome.project`, a plain Node script with its own
`--check`:

```
node scripts/sync-rive-assets.mjs           # copies assets/rive/*.riv to all 4 host paths
node scripts/sync-rive-assets.mjs --check   # SHA-256 compare, exits 1 and names the stale file(s)
```

Host path map (hand-written table inside the script, Android's underscore filename is real):

```js
const TARGETS = {
  'confetti.riv': ['composeApp/src/androidMain/res/raw/confetti.riv', 'iosApp/PolskiGrammar/Rive/confetti.riv', 'macosApp/PolskiGrammarMac/Rive/confetti.riv', 'composeApp/src/webMain/resources/rive/confetti.riv'],
  'again.riv': ['composeApp/src/androidMain/res/raw/again.riv', 'iosApp/PolskiGrammar/Rive/again.riv', 'macosApp/PolskiGrammarMac/Rive/again.riv', 'composeApp/src/webMain/resources/rive/again.riv'],
  'chain-complete.riv': ['composeApp/src/androidMain/res/raw/chain_complete.riv', 'iosApp/PolskiGrammar/Rive/chain-complete.riv', 'macosApp/PolskiGrammarMac/Rive/chain-complete.riv', 'composeApp/src/webMain/resources/rive/chain-complete.riv'],
};
```

Running it once: fixes the stale macOS `again.riv` (5428→2249 B) and adds the missing macOS
`chain-complete.riv` (59088 B) — this is the real bug 04-doc called "macOS drift", now precisely
located and fixed by construction, not by hand-editing the macOS Xcode project.

### MODIFY

- `package.json` (or `AGENTS.md`'s check list): add `node scripts/sync-rive-assets.mjs --check` to
  whatever the project calls its local pre-commit/gate sequence for `dsl/**` (same tier as
  `biome generate --check`).
- `THIRD_PARTY/credits.md`: verify the CC BY 4.0 `confetti.riv` credit line still matches after the
  canonical copy exists (04-doc flagged this as unresolved, P17).

### Acceptance

- `node scripts/sync-rive-assets.mjs --check` RED before the fix (names `macosApp/.../again.riv` and
  the missing `chain-complete.riv`), GREEN after.
- macOS: new UI test (added in block A) plays the real chain-complete animation instead of silently
  failing to load a missing resource.

## Блок D. `UiCopy` каталог

### Факт (uicopy-scan, перепроверено частично)

16 strings confirmed at 4-5 hosts with exact locations (full list in the scan transcript); highest-
value examples: "Повторить"/"Вспомнил" (5 hosts), "Прогресс"/"Настройки"/"Экспорт JSON"/
"Сбросить прогресс"/"Показать ответ" (5 hosts each). `intervalLabel()` — **3** copies, not the
assumed 2: `TrainingWebApp.kt:1005`, desktop `TrainingScreen.kt:310`, macOS
`SwipeRating.swift:132` (no iOS/Android copy — they don't render this text).

Cross-host wording is **inconsistent** today (iOS "Тренировка" vs rest "Карточки"; macOS "Словарь"
vs rest "Слова"; etc.) — uicopy-scan flagged this explicitly. **Boundary**: this migration copies
the current majority wording verbatim; it does not silently fix inconsistencies (UITests search for
literal text — 02-UI-CONSOLIDATION.md §6 risk note). Reconciling wording is a product decision for
the owner, tracked separately, not bundled into this refactor.

### NEW `kotlin/shared/src/commonMain/kotlin/polski/presentation/UiCopy.kt`

```kotlin
package polski.presentation

/** Catalog of UI strings duplicated across >=4 hosts today (02-UI-CONSOLIDATION.md §2.3). Hosts
 *  reference these constants instead of their own literal; Swift hosts receive the value through
 *  the existing IosSnapshot/MacSnapshot `put(...)` bridge, not a generated Swift file (no new
 *  mechanism needed — that bridge already carries copy strings, e.g. chainCompletionTitle). */
object UiCopy {
    const val Again = "Повторить"
    const val Good = "Вспомнил"
    const val ProgressTab = "Прогресс"
    const val SettingsTab = "Настройки"
    const val ExportJson = "Экспорт JSON"
    const val ResetProgress = "Сбросить прогресс"
    const val ShowAnswer = "Показать ответ"
    // ... remaining of the 18 strings at >=4 hosts (uicopy-scan's list), then the 30 at >=3
}
```

### NEW `kotlin/shared/src/commonMain/kotlin/polski/presentation/IntervalLabel.kt`

```kotlin
package polski.presentation

import kotlin.time.Instant

/** "через 5 мин" / "через 3 ч" / "через 2 дн" — the one copy (was: web, desktop, macOS Swift). */
fun intervalLabel(due: Instant, now: Instant): String = /* body ported verbatim from
    TrainingWebApp.kt:1005 (confirm web/desktop bodies are byte-identical before merging;
    if they differ, that's itself a bug this block fixes) */ TODO()
```

### MODIFY (delete the copies, call through)

- `TrainingWebApp.kt:1005`, desktop `TrainingScreen.kt:310`: delete the local `intervalLabel`, call
  `polski.presentation.intervalLabel(due, now)`.
- macOS `SwipeRating.swift:132`: delete the Swift reimplementation, call into `shared` via the
  Kotlin/Native bridge (same pattern as block B's gate call).
- Each host's literal strings at the 16+ confirmed locations (uicopy-scan's path list) → `UiCopy.*`.
  Swift call sites get the value via the snapshot `put("again", UiCopy.Again)`-style bridging that
  `IosSnapshot.kt`/`MacSnapshot.kt` already use for `chainCompletionTitle`/`chainStepSummary`
  (`IosSnapshot.kt:93-94`) — **no new Swift generation mechanism**.

### Acceptance

- NEW grep test (vitest, same family as `tests/course-data-layout.test.ts`):
  `tests/ui-copy-no-duplicates.test.ts` — extracts every string literal from `UiCopy.kt`, greps all
  host source trees (excluding `UiCopy.kt` itself and the Swift snapshot bridge files, which
  legitimately hold the value once each) for that literal; fails if found.
- `grep -rn "intervalLabel" kotlin/composeApp kotlin/macosApp kotlin/iosApp` → exactly 3 call sites
  (web, desktop, macOS), 0 definitions outside `IntervalLabel.kt`.
- No UITest text search broken: run `PolskiGrammarUITests`, `FlipCorrectnessUITests`,
  `DesktopScreenTest.kt`, `AndroidCaseReferenceLeakTest.kt` (NOT RUN here — Developer/Tester's job).

## Блок E. `classify()` → data rows

### Факт (ExerciseGenerator.kt:161-165, Recipe.kt:49-58, course.data-contract:580-583,
recipes.core-data:52,110,168 — confirmed independently and by classify-scan)

```kotlin
// Recipe.kt (current)
data class ReasonRule(val whenClass: String, val key: String)
sealed interface ReasonSpec {
    data class Fixed(val key: String) : ReasonSpec
    data class Classified(val rules: List<ReasonRule>) : ReasonSpec
}

// ExerciseGenerator.kt:161-165 (current, the only Polish literal in this file: "m-personal")
private fun classify(fromText: String, toText: String, nounId: String): String = when {
    fromText == toText -> "unchanged"
    genderOf(nounId) == "m-personal" && nomSingularOf(nounId).endsWith("a") -> "personalA"
    else -> "changed"
}
```

Called once, in `resolveChange` (lines 170-182): computes `classification`, then
`reason.rules.firstOrNull { it.whenClass == classification }?.key ?: reason.rules.first { it.whenClass == "default" }.key`.

The 3 `personalA` rows live **only** in pl-ru: `courses-biome/pl-ru/recipes.core-data:52,110,168`
(`{whenClass: personalA key: accReasonPersonalA}`), each inside a 3-rule list
`[unchanged, personalA, default]`. No en-ru equivalent — en-ru's `genderOf` always returns `null`, so
the `personalA` branch never fires there today; that must stay true after the change.

### MODIFY `dsl/course.data-contract` (lines 580-583)

```
# Before
RECORD ReasonRuleRow
  whenClass: string
  key: ref CopyEntryRow
END

# After — explicit, nullable conditions; a rule with none set is the unconditional fallback
# (replaces the "default" magic string with "no conditions"). First matching rule, in list order,
# wins — same semantics as today's lookup-by-classification, computed inline instead of through a
# named classifier.
RECORD ReasonRuleRow
  whenUnchanged: bool OPTIONAL
  whenGender: string OPTIONAL
  whenNomSingularSuffix: string OPTIONAL
  key: ref CopyEntryRow
END
```

### MODIFY `courses-biome/pl-ru/recipes.core-data` (3 occurrences, lines 52/110/168)

```
# Before
{whenClass: unchanged key: accReasonUnchanged}
{whenClass: personalA key: accReasonPersonalA}
{whenClass: default key: accReasonChanged}

# After
{whenUnchanged: true key: accReasonUnchanged}
{whenGender: "m-personal" whenNomSingularSuffix: "a" key: accReasonPersonalA}
{key: accReasonChanged}
```

(en-ru's equivalent rows, if any exist with `whenClass: unchanged`/`default` only — verify via
`grep whenClass courses-biome/en-ru/recipes.core-data` before writing the mapper, since it must
accept the same no-gender shape.)

### MODIFY `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/Recipe.kt`

```kotlin
data class ReasonRule(
    val whenUnchanged: Boolean? = null,
    val whenGender: String? = null,
    val whenNomSingularSuffix: String? = null,
    val key: String,
)
```

### MODIFY `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt`

Delete `classify()` (lines 161-165). `genderOf`/`nomSingularOf` stay (genuinely generic helpers,
`genderOf` is used elsewhere too — classify-scan confirmed this). `resolveChange` (170-182) becomes:

```kotlin
is ReasonSpec.Classified -> {
    val nounId = lexicalSlots.getValue("noun")
    reason.rules.first { rule ->
        (rule.whenUnchanged != true || from == to) &&
        (rule.whenGender == null || rule.whenGender == genderOf(nounId)) &&
        (rule.whenNomSingularSuffix == null || nomSingularOf(nounId).endsWith(rule.whenNomSingularSuffix))
    }.key
}
```

Note the guard order matters only in that the **list order** (unchanged, personalA, default) is
still what picks the first match — a row with no conditions always matches, so it must stay last
(unchanged today; worth a contract-level comment, not an enforced invariant, matching this project's
existing "first condition that holds wins" convention already used for `GovWhenRow`/`OrderWhenRow`
in the same contract).

### Acceptance

- `TrainingParityTest.kt`: pl-ru `kolega`/other `personalA` nouns unchanged (frozen literals, not
  regenerated — this is the regression oracle per 00-doc's recommendation).
- en-ru equivalent test (`EnExerciseGeneratorTest`, name TBC — verify actual file) unchanged: no
  `whenGender` row ever matches since `genderOf` returns `null` for en-ru nouns.
- `node <biome> generate --check --project dsl/biome.project` after regenerating `CourseRows.kt` for
  both packs from the edited `.core-data`.
- `grep -n "m-personal" kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt` → 0 (P10 closed: the only Polish-specific literal in core-engine moves to pack data).

## Блок F. `GrammarParityTest` → `.core-data` + цикл

### Факт (verified: `wc -l` = 2069, `assertEquals` count = 1980, `grammar.json` cases = 1979)

8 case kinds, by id prefix (counts from `tests/fixtures/kotlin-parity/grammar.json`, schemaVersion/
sourceRevision pinned, independent of Kotlin — P15 already satisfied by the fixture, this block only
changes *where* the frozen expectations live):

| Prefix | Count | Input shape | Expected (pre-joined literal) |
|---|---|---|---|
| `G-NOUN-` | 196 | `nounId, number, gramCase` | `lemma\|meaning\|gender\|form` |
| `G-ADJ-` | 490 | `adjectiveId, number, gender, gramCase` | `lemma\|meaning\|form` |
| `G-VERB-` | 386 | `verbId, tense, person, number` (gender defaults m-personal) | `lemma\|meaning\|aspect\|futureType\|perfectivePair\|form` |
| `G-PRON-` | 9 | `pronounId` | 7-case pipe row |
| `G-POSS-` | 490 | `owner` | `label` |
| `G-PHRASE-` | 196 | `nounId, adjectiveId, possessive, number, gramCase` | plain string |
| `G-SENTENCE-` | 196 | `seed.nounId, seed.adjectiveId, owner, number, gramCase` | plain string |
| `S-` (skills) | 16 | `skillId` | `id\|title\|group\|level\|formula\|theory\|hint\|prerequisites,joined` |

(196+490+386+9+490+196+196+16 = 1979, matches the fixture; the 1980th `assertEquals` is one extra
non-table assertion — check for it when porting, likely an `assertFailsWith`-adjacent sanity check or
a stray duplicate; don't silently drop it.)

**This fixture is independent of this test file** (P15) — converting it to `.core-data` is purely a
storage-format change for an already-frozen oracle, not a re-derivation from Kotlin.

### NEW `dsl/grammar-oracle.data-contract`

```
DSL 1
# One pinned grammar-oracle case (R1-style migration of tests/fixtures/kotlin-parity/grammar.json,
# schemaVersion/sourceRevision carried in a header comment, not re-derived from Kotlin — P15). Every
# kind uses a subset of these fields; GrammarOracleRowsTest.kt refuses a row whose fields don't match
# its own kind (same "kind + OPTIONAL fields, refuse the mismatch" idiom as TextValueRow).
RECORD GrammarCaseRow KEY id COUNT 1..
  id: string
  kind: enum noun|adjective|verb|pronoun|possessive|phrase|sentence|skill
  nounId: string OPTIONAL
  adjectiveId: string OPTIONAL
  verbId: string OPTIONAL
  pronounId: string OPTIONAL
  skillId: string OPTIONAL
  owner: string OPTIONAL
  number: string OPTIONAL
  gramCase: string OPTIONAL
  gender: string OPTIONAL
  tense: string OPTIONAL
  person: int OPTIONAL
  expected: string
END
```

### NEW `dsl/derive-grammar-oracle.mjs`

One-off converter (run once, by the Developer, not part of the steady-state `GENERATE` step — same
status as the historical `scripts/build-pack.mjs`/`build-pack-en.mjs` that `derive-forms.mjs`'s
header comment references as retired): reads `tests/fixtures/kotlin-parity/grammar.json`, prints
1979 `GRAMMAR_CASE {...}` rows to `dsl/grammar-oracle/grammar.core-data` (NEW, committed, the
oracle's new home — `grammar.json` itself is then deleted, closing G7 from 05-DATA-TESTING.md: "two
formats of one oracle in two places"). This script is **not** wired into `dsl/biome.project` — it has
exactly one run, like a migration script, not a standing derive step.

### NEW `dsl/biome.project` step (steady state, after the one-off conversion)

```
GENERATE grammar-oracle data --contract grammar-oracle --rows GrammarCaseRow {dir}/grammar-oracle/grammar.core-data --target kt --kt-package polski.data.rows.oracle --out {dir}/../kotlin/shared/src/commonTest/generated/kotlin/polski/data/rows/oracle/GrammarCaseRows.kt
```

(NEW source set root `commonTest/generated` — verify Gradle already resolves generated test sources
from this path the way it does `commonMain/generated`; if not, a one-line `sourceSets.commonTest`
addition in the relevant `build.gradle.kts` is part of this step, not a new architectural layer.)

### MODIFY: replace `GrammarParityTest.kt` (2069 → ~60 lines)

```kotlin
package polski.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.data.*
import polski.data.rows.oracle.*
import polski.model.*

/** Loops polski.data.rows.oracle.grammarCaseRows (generated from the pinned fixture, see
 *  dsl/grammar-oracle/grammar.core-data) — was 1979 hand-expanded assertEquals (P5/P6, P22). */
class GrammarParityTest {
    @Test fun allCases() {
        for (row in grammarCaseRows) {
            val actual = when (row.kind) {
                GrammarCaseRowKind.NOUN -> nounById(row.nounId!!).let { n ->
                    listOf(n.lemma, n.meaning, n.gender.id, nounForm(n.id, GramCase.fromId(row.gramCase!!), NumberGram.fromId(row.number!!))).joinToString("|")
                }
                GrammarCaseRowKind.ADJECTIVE -> adjectiveById(row.adjectiveId!!).let { a ->
                    listOf(a.lemma, a.meaning, adjectiveForm(a.id, Gender.fromId(row.gender!!), GramCase.fromId(row.gramCase!!), NumberGram.fromId(row.number!!))).joinToString("|")
                }
                GrammarCaseRowKind.VERB -> verbById(row.verbId!!).let { v ->
                    listOf(v.lemma, v.meaning, v.aspect.id, v.futureType.id, v.perfectivePair.orEmpty(),
                        verbForm(v.id, Tense.fromId(row.tense!!), Person.fromId(row.person!!), NumberGram.fromId(row.number!!), Gender.fromId(row.gender ?: "m-personal"))).joinToString("|")
                }
                GrammarCaseRowKind.PRONOUN -> GramCase.entries.joinToString("|") { personalPronouns.getValue(row.pronounId!!).getValue(it) }
                GrammarCaseRowKind.POSSESSIVE -> possessives.first { it.id.id == row.owner }.label
                GrammarCaseRowKind.PHRASE -> nounPhrase(row.nounId!!, GramCase.fromId(row.gramCase!!), NumberGram.fromId(row.number!!), row.adjectiveId!!, PossessiveId.fromId(row.owner!!))
                GrammarCaseRowKind.SENTENCE -> caseSentence(SentenceSeed(row.nounId!!, row.adjectiveId!!), GramCase.fromId(row.gramCase!!), PossessiveId.fromId(row.owner!!), NumberGram.fromId(row.number!!))
                GrammarCaseRowKind.SKILL -> skillById(row.skillId!!).let { s ->
                    listOf(s.id, s.title, s.group, s.level, s.formula, s.theory, s.hint, s.prerequisites.joinToString(",")).joinToString("|")
                }
            }
            assertEquals(row.expected, actual, row.id)
        }
    }
}
```

(Exact field names of `GrammarCaseRow`'s Kotlin printer — `nounId`, `kind` as a `GrammarCaseRowKind`
enum — follow the same printer convention already visible in `CourseRows.kt`'s `ReasonSpecRowKind`;
confirm the generated enum name before finalizing this loop.)

### Acceptance

- `wc -l kotlin/shared/src/commonTest/kotlin/polski/grammar/GrammarParityTest.kt` < 200 (target from
  00-doc's KPI table).
- `node <biome> generate --check --project dsl/biome.project` fails on a hand-edit to
  `GrammarCaseRows.kt` or to `grammar.core-data` with a schema violation (wrong `kind`/missing
  required field for that kind).
- Same 1979 (or 1980 — resolve the extra assertion first) PASS/FAIL outcomes as before the refactor —
  run both versions side by side before deleting the original file.
- `git ls-files tests/fixtures/kotlin-parity/grammar.json` → absent after the one-off conversion is
  committed (closes G7's "two formats of one oracle").

## Блок G. `progress`/`srs` генераторы → derive-шаги

### Факт (verified: `wc -l` = 69/31 lines; both still write unconditionally, no `--check` mode)

- `kotlin/shared/src/commonTest/kotlin/polski/progress/generate_parity.py` (69 lines): reads
  `tests/fixtures/kotlin-parity/progress.json`, writes `ProgressFixtureParityTest.kt` (5 cases, per
  00-doc).
- `kotlin/shared/src/commonTest/kotlin/polski/srs/generate_parity.py` (31 lines): reads
  `tests/fixtures/kotlin-parity/scheduler.json`, writes `SchedulerOracleTest.kt` (21 `@Test`
  functions, one per oracle case).

Neither has a `--check` mode (confirmed: both scripts unconditionally `Path.write_text(...)`, no
`sys.argv` branch comparing before writing — unlike `grammar/generate_parity.py`, which does have
`--check`, is RED, and is Phase-0 scope, not this block).

### Decision: wrap, don't port (05-DATA-TESTING.md §7's explicit recommendation)

Porting the string-building to JS buys nothing extra here (same V2 win either way); wrapping the
existing, working Python keeps the diff tiny and the risk near zero.

### MODIFY both Python scripts: add a `--print` mode (writes to stdout, doesn't touch the `.kt` file)

```python
# both scripts, appended
import sys
if '--print' in sys.argv:
    sys.stdout.write('\n'.join(lines) + '\n')
else:
    out.write_text('\n'.join(lines) + '\n')
    print(f'Wrote {out} ...')
```

### NEW `dsl/derive-progress-oracle.mjs` and `dsl/derive-scheduler-oracle.mjs`

```js
// derive-progress-oracle.mjs — shells out to the existing, unmodified generation logic.
import { spawnSync } from 'node:child_process';
import { resolve } from 'node:path';
const root = resolve(import.meta.dirname, '..');
export const OUT = resolve(root, 'kotlin/shared/src/commonTest/kotlin/polski/progress/ProgressFixtureParityTest.kt');
export function progress() {
  const script = resolve(root, 'kotlin/shared/src/commonTest/kotlin/polski/progress/generate_parity.py');
  const run = spawnSync('python3', [script, '--print'], { encoding: 'utf8' });
  if (run.status !== 0) throw new Error(`generate_parity.py --print failed: ${run.stderr}`);
  return `// Generated from tests/fixtures/kotlin-parity/progress.json by generate_parity.py (via dsl/derive-progress-oracle.mjs).\n${run.stdout}`;
}
```

(`derive-scheduler-oracle.mjs` is the same shape, pointed at `srs/generate_parity.py` and
`SchedulerOracleTest.kt`.)

### MODIFY `dsl/biome.project` (append)

```
GENERATE progress-oracle derive {dir}/derive-progress-oracle.mjs progress OUT {dir}/../tests/fixtures/kotlin-parity/progress.json
GENERATE scheduler-oracle derive {dir}/derive-scheduler-oracle.mjs srs OUT {dir}/../tests/fixtures/kotlin-parity/scheduler.json
```

(The `{dir}/../tests/.../*.json` argument is just so the step's `generateArgs` records its real
input for `--check`'s dependency tracking — the derive function itself reads nothing from it
directly, since the Python script already knows its own fixture path. Verify biome's `--check`
actually uses `generateArgs` for staleness detection, or whether it only re-runs and diffs the
output — if the latter, this line is cosmetic documentation, not a functional dependency edge.)

### Acceptance

- `node <biome> generate --check --project dsl/biome.project` now covers `progress`/`srs`: a hand-edit
  to `ProgressFixtureParityTest.kt` or `SchedulerOracleTest.kt` outside what the script would produce
  fails `--check` (closes 05-doc's G2/G8 for these two files).
- `python3 .../progress/generate_parity.py --print` output byte-equal to the file `generate_parity.py`
  (no args) currently writes — run both before/after to confirm the `--print` addition changed
  nothing else.
- `grep -rln 'generate_parity.py' kotlin/shared/src/commonTest/kotlin/polski/{progress,srs}` still
  finds the scripts (they're wrapped, not deleted) — only `grammar/` and `training/` should
  eventually disappear (Phase 0 scope, see note at top of this plan).

## Что НЕ делать (границы)

- Не вводить `TEMPLATE`/`FLOW`/`PROGRAM`/новую DSL-конструкцию ни в одном блоке — все семь решены
  через существующий `data-contract`/`data generate`/`derive` механизм или чистые Kotlin-функции.
  Эффект-таблица (блок A) — данные (lookup), не `DECISION`: нет ветвления, значит `swipe.logic`-стиль
  не подходит и не нужен (P25 S3: линейные данные остаются данными).
- Не трогать `swipe.logic`/`SwipePolicy.kt` (W1/W2 из `03-DSL-COVERAGE.md`) — не входит в задание.
- Не трогать `TrainingStore.dispatch`/`accepts()` (03§3.4) — отдельный, пока не назревший кандидат.
- Не удалять `grammar/`/`training/generate_parity.py` в этом плане — Phase 0, уже отмечено владельцем
  как DONE (хотя в этом дереве файлы физически присутствуют — см. примечание в начале документа).
- Не переносить `derive-forms.mjs`'s Polish-specific declension rules — вне скоупа (03§3.1, уже
  признано native).
- Не чинить асимметрию iOS/macOS в освобождении Rive view models при переключении reduceMotion
  (найдена в блоке B) — зафиксировать как отдельную находку, не расширять блок B ею.
- Не менять формулировки UI-строк при консолидации в `UiCopy` (блок D) — только перенос текущего
  текста; расхождения формулировок — решение владельца, не эта задача.
- Не переписывать `progress`/`srs` генераторы на JS (блок G) — обёртка, не порт, по явной
  рекомендации `05-DATA-TESTING.md` §7.
- Не трогать `nj03-exercise-oracle` второй проект (его собственный `biome.project`) — он уже рабочий
  (`--check` OK), включение его в `AGENTS.md`/`package.json` — отдельный, более мелкий шаг (05§8 п.5),
  не входит в блоки A-G.

## Метрики до/после (из задания, с источником проверки)

| Метрика | Сейчас (подтверждено) | Цель | Проверка после |
|---|---|---|---|
| Мест правки на «изменить effect» | 4 (Android/web/iOS/macOS copies) | 1 (`dsl/effects/card-effects.core-data`) | `grep` таблицы вне `Generated*`/`*.generated.*` = 0 |
| Мест правки на «добавить строку UI» | до 5 | 1 (`UiCopy`) + рендереры | `tests/ui-copy-no-duplicates.test.ts` |
| Python-генераторов вне `--check` | 2 живых (`progress`, `srs`) — подтверждено, оба без `--check`-ветки | 0 | `biome generate --check` покрывает оба шага |
| `GrammarParityTest` строк | 2069 (подтверждено `wc -l`) | <200 | `wc -l` после замены |
| Копий `intervalLabel` | 3 (не 2 — уточнено uicopy-scan) | 1 | `grep -rn intervalLabel` |
| Копий effect-таблицы Rive | 4, код идентичен, актив (`.riv`) дрейфнул на macOS (подтверждено `ls -la`) | 1 + 2 синхронные `.riv`-дефекта исправлены | блок A/C acceptance |
| Польских литералов в `core-engine` | 1 (`"m-personal"`, `ExerciseGenerator.kt:163`) | 0 | `grep -n "m-personal"` |

## Приёмочные тесты — сводная таблица запуска (всё NOT RUN до реализации)

| Команда | Блоки |
|---|---|
| `node <biome> generate --check --project dsl/biome.project` | A, E, F, G |
| `node scripts/sync-rive-assets.mjs --check` | C |
| `./gradlew :shared:commonTest` (или эквивалент) — `RiveGateTest`, `TrainingParityTest`, en-ru parity test, `GrammarParityTest.allCases` | B, E, F |
| `npx vitest run tests/ui-copy-no-duplicates.test.ts` | D |
| `./gradlew :androidApp:testDebugUnitTest` — `AndroidFlipCardTest` | A, B |
| iOS/macOS UI tests (`PolskiGrammarUITests`, `FlipCorrectnessUITests`, new macOS chain-complete test) | A, C, D |
| `npx playwright test --config playwright.kotlin.config.ts` (flip/rive specs) | A, B |

## Документация и handoff

- `AGENTS.md`: добавить блоки A-G в список `GENERATE`-шагов (краткая строка на шаг, по образцу
  существующих комментариев в `dsl/biome.project`).
- `AI/decisions.md`: записать решение блока E (контракт условий вместо `whenClass`-строки) и блока A
  (данные, не `DECISION`, для effect-таблицы) — оба меняют публичный контракт `course.data-contract`/
  вводят новый `effects.data-contract`.
- Разработчик по каждому блоку: RED/GREEN перед/после, с реальным выводом `generate --check`, не
  предполагаемым.
