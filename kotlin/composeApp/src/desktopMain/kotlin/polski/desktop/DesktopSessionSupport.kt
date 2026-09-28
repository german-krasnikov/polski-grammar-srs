package polski.desktop

import polski.data.selectCoursePack
import polski.presentation.TrainingStore

/**
 * EnRuAcceptance-2026-09-28.md §7 item 2 parity (ADR-37): mirrors `MacSession.rebuildIfCourseSwitched`'s
 * own runCatching-rollback for the JVM Compose Desktop preview, which has no session bridge of its
 * own — `Main.kt`'s `DesktopSession` builds [TrainingStore] directly. `polski.data.availableCoursePacks`
 * (what [DesktopPreferencesController.setTarget]/[setNative] check before accepting a switch) only
 * probes that a pack's JSON *parses*; it does not probe that [build] can actually produce a session
 * ([TrainingStore]'s own `exerciseEngine.generateChain()` init property can still throw — e.g. en-ru's
 * still-verb-only `forms.generated.json`, EnRuAcceptance §7/ADR-37 blocker 1, a content-lane gap this
 * function does not and must not paper over with invented pack data).
 *
 * Tries [build] once; on failure, rolls the shared `polski.data.packRegistry` active pack back to
 * [lastGoodPackId] (a pack already known to build, normally the previous store's own pack) and
 * retries once. A [lastGoodPackId] that cannot build either is a genuine, unrecoverable bug — its
 * exception is allowed to propagate rather than loop or hide it.
 */
internal fun buildTrainingStoreOrRollback(lastGoodPackId: String, build: () -> TrainingStore): TrainingStore =
    runCatching(build).getOrElse {
        selectCoursePack(lastGoodPackId)
        build()
    }
