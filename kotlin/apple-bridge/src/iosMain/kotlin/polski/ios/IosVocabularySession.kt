package polski.ios

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import kotlin.time.Clock
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID
import polski.apple.AppleVocabularySession
import polski.data.activeCoursePackId
import polski.srs.FsrsScheduler
import polski.vocabulary.StudyDirection
import polski.vocabulary.VocabularySession
import polski.vocabulary.VocabularyUiState
import polski.vocabulary.activeStudyDirections

/**
 * Scene-owned SwiftUI bridge. [defaults] is injectable (mirrors [IosSession]) so a test can point
 * it at an isolated suite instead of the real device's `standardUserDefaults`. Shared load/save
 * control flow and the dispatch/snapshot skeleton live in [AppleVocabularySession]
 * (`apple-bridge/src/appleMain`); this class supplies only iOS's own storage and wire fields.
 */
class IosVocabularySession(private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults) : AppleVocabularySession() {
    init { startSession() }

    override fun newSession(): VocabularySession = VocabularySession(IosVocabularyRepository(defaults), FsrsScheduler(),
        { Clock.System.now() }, { "user.${NSUUID().UUIDString.lowercase()}" })

    // EnRuAcceptance-2026-09-28.md §7 item 4: pl-ru's own 2 wires only, so an en-ru direction from
    // the host's picker is never silently dropped — was `builtInStudyDirections`.
    override fun directionFor(wire: String): StudyDirection? = activeStudyDirections.firstOrNull { it.wire == wire }

    override fun putExtraSnapshotFields(builder: JsonObjectBuilder, state: VocabularyUiState) {
        // EnRuAcceptance-2026-09-28.md §7 item 4: the pack this exact [state] belongs to — always
        // in sync with [state.direction]/[state.document] because [AppleVocabularySession]'s own
        // rebuild-on-switch (called right before every snapshot) only ever rebuilds its session to
        // match [polski.data.activeCoursePackId] at that same instant. The Swift picker reads these
        // instead of `model.preferences`' own target/native, which can briefly lag behind (or
        // self-correct after) this bridge's own pack switch — see `VocabularyView`'s own comment.
        val pairId = activeCoursePackId
        builder.put("target", pairId.substringBefore('-'))
        builder.put("native", pairId.substringAfter('-'))
    }

    override fun close() { super.close(); onEffect = null }
}
