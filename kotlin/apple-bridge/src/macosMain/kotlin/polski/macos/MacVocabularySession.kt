package polski.macos

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import platform.Foundation.NSUUID
import polski.data.VocabularyItem
import polski.data.activeCoursePackOption
import polski.data.frequencyItems
import polski.data.vocabularyItems
import polski.data.courseVocabularyInstructions
import polski.data.courseVocabularyUnavailableLabel
import polski.presentation.cardEffectFor
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularySession
import polski.vocabulary.VocabularyUiState
import polski.vocabulary.recallCaption
import polski.vocabulary.recallsTarget
import polski.vocabulary.studyDirectionOptions

/** Scene-owned SwiftUI bridge. Kotlin alone owns review scheduling and durable writes. */
class MacVocabularySession(private val directory: String) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val scheduler = FsrsScheduler()
    private var session = newSession()
    // EnRuAcceptance-2026-09-28.md §7 item 2: mirrors `MacSession.storePackId`/
    // `rebuildIfCourseSwitched` — [MacVocabularyRepository]'s own storage path is now
    // pack-namespaced (fresh `VocabularyCodec.key`/file per active pack), but this bridge's
    // in-memory [session] still needs an explicit restart to actually load the newly active
    // pack's own document instead of continuing to serve the previous pack's one.
    private var sessionPackId = polski.data.activeCoursePackId
    private var observer: Job? = null
    var onState: ((String) -> Unit)? = null
        set(value) {
            field = value
            rebuildIfCourseSwitched()
            value?.invoke(snapshot(session.state.value))
        }

    /** D3: mirrors [MacSession.onEffect] for the vocabulary card's decorative Rive rating overlay. */
    var onEffect: ((String) -> Unit)? = null

    init { observeAndStart() }

    private fun newSession(): VocabularySession = VocabularySession(MacVocabularyRepository(directory), scheduler,
        { Clock.System.now() }, { "user.${NSUUID().UUIDString.lowercase()}" })

    private fun observeAndStart() {
        observer = scope.launch { session.state.collect { onState?.invoke(snapshot(it)) } }
        scope.launch { session.start() }
    }

    /** Unlike `MacSession.rebuildIfCourseSwitched`, [newSession] never eagerly generates content,
     *  so no failure fallback is needed — the new pack's own document simply loads (or starts
     *  empty) the next time [VocabularySession.start] runs. */
    private fun rebuildIfCourseSwitched() {
        val current = polski.data.activeCoursePackId
        if (current == sessionPackId) return
        sessionPackId = current
        observer?.cancel()
        session = newSession()
        observeAndStart()
    }

    fun currentSnapshot(): String { rebuildIfCourseSwitched(); return snapshot(session.state.value) }
    fun exportJson(): String? = session.exportJson()

    fun dispatch(command: String, value: String = "") {
        rebuildIfCourseSwitched()
        when (command) {
            // EnRuAcceptance-2026-09-28.md §7 item 4: only the active pack's own 2 directions
            // (`studyDirectionOptions`) are ever accepted — was `builtInStudyDirections`, a
            // hardcoded pl-ru pair that silently dropped en-ru's own "en-ru"/"ru-en" wires.
            "direction" -> activeCoursePackOption.let { pack -> studyDirectionOptions(pack.target, pack.native) }
                .map { it.direction }.firstOrNull { it.wire == value }?.let(session::setDirection)
            "filter" -> if (value in listOf("A1", "A2", "B1", "100", "500", "1000", "mine")) session.setFilter(value)
            "typed" -> session.setTyped(value == "true")
            "draft" -> session.setDraft(value)
            "reveal" -> session.reveal()
            "refresh" -> session.refresh()
            "again" -> scope.launch { rate(Rating.Again) }
            "good" -> scope.launch { rate(Rating.Good) }
            "select" -> scope.launch { session.select(value, true) }
            "deselect" -> scope.launch { session.select(value, false) }
            "delete" -> scope.launch { session.deleteCustom(value) }
        }
    }

    fun saveCustom(lemma: String, translation: String, form: String, example: String,
                   level: String, editingId: String?, completion: (Boolean) -> Unit) {
        scope.launch { completion(session.saveCustom(lemma, translation, form, example, level, editingId)) }
    }

    fun importJson(raw: String, completion: (Boolean) -> Unit) {
        scope.launch { completion(session.importJson(raw)) }
    }

    fun close() { onState = null; scope.cancel() }

    /**
     * D3 (`Plans/Kotlin/FlipCardRivePlan.md` FC-01/FC-17/FC-20): fires [onEffect] once per
     * accepted rating with [cardEffectFor]'s Again/Remembered mapping, mirroring
     * [MacSession.dispatch]'s own rate handling — never for a rate [VocabularySession.rate]
     * itself rejects (no revealed current card yet), so a stale tap never fires a spurious cue.
     */
    private suspend fun rate(rating: Rating) {
        if (session.rate(rating)) onEffect?.invoke(cardEffectFor(rating).name)
    }

    private fun snapshot(state: VocabularyUiState): String = buildJsonObject {
        put("instructions", courseVocabularyInstructions.ios)
        put("unavailableLabel", courseVocabularyUnavailableLabel)
        put("loadStatus", state.loadStatus.name)
        put("direction", state.direction.wire)
        // EnRuAcceptance-2026-09-28.md §7 item 4: the active pack's own 2 directions (never a
        // hardcoded pl-ru pair) plus the ready-made copy a card needs to render either one
        // without knowing any language code itself.
        val pack = activeCoursePackOption
        put("directionOptions", JsonArray(studyDirectionOptions(pack.target, pack.native).map { option ->
            buildJsonObject { put("wire", option.direction.wire); put("label", option.label) }
        }))
        put("promptCaption", state.direction.recallCaption(pack.target, pack.native))
        put("recallTarget", state.direction.recallsTarget(pack.target, pack.native))
        put("filter", state.filter)
        put("currentId", state.currentId?.let(::JsonPrimitive) ?: JsonNull)
        put("revealed", state.revealed)
        put("typed", state.typed)
        put("draft", state.draft)
        put("busy", state.busy)
        put("error", state.error?.let(::JsonPrimitive) ?: JsonNull)
        put("selectedCount", state.document.selectedIds.size)
        val item = state.currentId?.let { VocabularyCodec.item(state.document, it) }
        put("current", item?.let(::itemJson) ?: JsonNull)
        // D3: the compact rating buttons' interval hint, mirroring training's own "intervals"
        // (`MacSnapshot.kt`) and the web reference's `VocabularyCodec.preview` usage
        // (`webMain/kotlin/polski/ui/VocabularyWeb.kt`) — only computed once actually revealed,
        // the only phase these buttons are shown in.
        if (state.revealed && state.currentId != null) {
            val at = Clock.System.now()
            val preview = VocabularyCodec.preview(state.document, state.currentId, state.direction, scheduler, at)
            put("now", at.toEpochMilliseconds())
            put("intervals", intervalsJson(preview))
        } else {
            put("now", JsonNull)
            put("intervals", JsonNull)
        }
        val all = vocabularyItems + state.document.custom
        val byLemma = vocabularyItems.associateBy(VocabularyItem::lemma)
        val entries = when (state.filter) {
            "100", "500", "1000" -> frequencyItems.take(state.filter.toInt()).map { it.rank to (byLemma[it.lemma] ?: VocabularyItem(
                "", it.lemma, "", "", "", "—", it.rank, false)) }
            "mine" -> state.document.custom.map { null to it }
            else -> all.filter { it.level == state.filter }.map { it.frequencyRank to it }
        }
        if (state.filter in listOf("100", "500", "1000")) {
            val ready = entries.count { (_, entry) -> entry.id.isNotEmpty() && !entry.custom }
            put("coverage", "Готово $ready/${entries.size} · недоступно ${entries.size - ready}")
        } else put("coverage", "")
        put("entries", JsonArray(entries.map { (rank, entry) -> buildJsonObject {
            put("id", entry.id); put("lemma", entry.lemma); put("translation", entry.translation)
            put("form", entry.form); put("example", entry.example); put("level", entry.level)
            put("rank", rank?.let(::JsonPrimitive) ?: JsonNull)
            put("custom", entry.custom); put("selected", entry.id in state.document.selectedIds)
            put("available", entry.id.isNotEmpty())
        } }))
    }.toString()

    private fun itemJson(item: VocabularyItem) = buildJsonObject {
        put("id", item.id); put("lemma", item.lemma); put("translation", item.translation)
        put("form", item.form); put("example", item.example); put("level", item.level)
    }

    private fun intervalsJson(preview: SchedulePreview) = buildJsonObject {
        put("Again", preview[Rating.Again].toEpochMilliseconds())
        put("Good", preview[Rating.Good].toEpochMilliseconds())
    }
}
