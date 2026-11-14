package polski.macos

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import platform.Foundation.NSUUID
import polski.apple.AppleVocabularySession
import polski.data.activeCoursePackOption
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.vocabulary.StudyDirection
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularySession
import polski.vocabulary.VocabularyUiState
import polski.vocabulary.recallCaption
import polski.vocabulary.recallsTarget
import polski.vocabulary.studyDirectionOptions

/**
 * Scene-owned SwiftUI bridge. Shared load/save control flow and the dispatch/snapshot skeleton
 * live in [AppleVocabularySession] (`apple-bridge/src/appleMain`); this class supplies only
 * macOS's own storage/scheduler, direction lookup and the extra directionOptions/promptCaption/
 * recallTarget/interval-preview wire fields the Mac vocabulary card needs.
 */
class MacVocabularySession(private val directory: String) : AppleVocabularySession() {
    private val scheduler = FsrsScheduler()

    init { startSession() }

    override fun newSession(): VocabularySession = VocabularySession(MacVocabularyRepository(directory), scheduler,
        { Clock.System.now() }, { "user.${NSUUID().UUIDString.lowercase()}" })

    // EnRuAcceptance-2026-09-28.md §7 item 4: only the active pack's own 2 directions
    // (`studyDirectionOptions`) are ever accepted — was `builtInStudyDirections`, a hardcoded
    // pl-ru pair that silently dropped en-ru's own "en-ru"/"ru-en" wires.
    override fun directionFor(wire: String): StudyDirection? = activeCoursePackOption.let { pack ->
        studyDirectionOptions(pack.target, pack.native)
    }.map { it.direction }.firstOrNull { it.wire == wire }

    override fun putExtraSnapshotFields(builder: JsonObjectBuilder, state: VocabularyUiState) {
        // EnRuAcceptance-2026-09-28.md §7 item 4: the active pack's own 2 directions (never a
        // hardcoded pl-ru pair) plus the ready-made copy a card needs to render either one without
        // knowing any language code itself.
        val pack = activeCoursePackOption
        builder.put("directionOptions", JsonArray(studyDirectionOptions(pack.target, pack.native).map { option ->
            buildJsonObject { put("wire", option.direction.wire); put("label", option.label) }
        }))
        builder.put("promptCaption", state.direction.recallCaption(pack.target, pack.native))
        builder.put("recallTarget", state.direction.recallsTarget(pack.target, pack.native))
        // D3: the compact rating buttons' interval hint, mirroring training's own "intervals"
        // (`MacSnapshot.kt`) and the web reference's `VocabularyCodec.preview` usage
        // (`webMain/kotlin/polski/ui/VocabularyWeb.kt`) — only computed once actually revealed,
        // the only phase these buttons are shown in.
        val currentId = state.currentId
        if (state.revealed && currentId != null) {
            val at = Clock.System.now()
            val preview = VocabularyCodec.preview(state.document, currentId, state.direction, scheduler, at)
            builder.put("now", at.toEpochMilliseconds())
            builder.put("intervals", intervalsJson(preview))
        } else {
            builder.put("now", JsonNull)
            builder.put("intervals", JsonNull)
        }
    }

    private fun intervalsJson(preview: SchedulePreview) = buildJsonObject {
        put("Again", preview[Rating.Again].toEpochMilliseconds())
        put("Good", preview[Rating.Good].toEpochMilliseconds())
    }
}
