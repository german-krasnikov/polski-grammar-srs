package polski.ios

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
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID
import polski.data.VocabularyItem
import polski.data.frequencyItems
import polski.data.vocabularyItems
import polski.data.courseVocabularyInstructions
import polski.data.courseVocabularyUnavailableLabel
import polski.presentation.cardEffectFor
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.vocabulary.activeStudyDirections
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularySession
import polski.vocabulary.VocabularyUiState

/**
 * Scene-owned SwiftUI bridge. Kotlin alone owns review scheduling and durable writes.
 * [defaults] is injectable (mirrors [IosSession]) so a test can point it at an isolated suite
 * instead of the real device's `standardUserDefaults`.
 */
class IosVocabularySession(private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var session = newSession()
    // EnRuAcceptance-2026-09-28.md §7 item 2: mirrors `MacVocabularySession.sessionPackId` — see
    // its own KDoc for why this in-memory session needs an explicit restart on a pack switch.
    private var sessionPackId = polski.data.activeCoursePackId
    private var observer: Job? = null
    var onState: ((String) -> Unit)? = null
        set(value) {
            field = value
            rebuildIfCourseSwitched()
            value?.invoke(snapshot(session.state.value))
        }

    /**
     * Fires once per accepted rating with a [polski.presentation.CardEffect] name
     * (`Remembered`/`Again`), for the host's decorative Rive overlay on the vocabulary card
     * (`Plans/Kotlin/FlipCardRivePlan.md` D3) — mirrors [IosSession.onEffect] exactly, so
     * [cardEffectFor] stays the single source of truth for both cards.
     */
    var onEffect: ((String) -> Unit)? = null

    init { observeAndStart() }

    private fun newSession(): VocabularySession = VocabularySession(IosVocabularyRepository(defaults), FsrsScheduler(),
        { Clock.System.now() }, { "user.${NSUUID().UUIDString.lowercase()}" })

    private fun observeAndStart() {
        observer = scope.launch { session.state.collect { onState?.invoke(snapshot(it)) } }
        scope.launch { session.start() }
    }

    /** Unlike `IosSession.rebuildIfCourseSwitched`, [newSession] never eagerly generates content,
     *  so no failure fallback is needed. */
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
            // EnRuAcceptance-2026-09-28.md §7 item 4: was `builtInStudyDirections` — pl-ru's own 2
            // wires only, so an en-ru direction from the host's picker was silently dropped.
            "direction" -> activeStudyDirections.firstOrNull { it.wire == value }?.let(session::setDirection)
            "filter" -> if (value in listOf("A1", "A2", "B1", "100", "500", "1000", "mine")) session.setFilter(value)
            "typed" -> session.setTyped(value == "true")
            "draft" -> session.setDraft(value)
            "reveal" -> session.reveal()
            "refresh" -> session.refresh()
            "again" -> scope.launch { if (session.rate(Rating.Again)) onEffect?.invoke(cardEffectFor(Rating.Again).name) }
            "good" -> scope.launch { if (session.rate(Rating.Good)) onEffect?.invoke(cardEffectFor(Rating.Good).name) }
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

    fun close() { onState = null; onEffect = null; scope.cancel() }

    private fun snapshot(state: VocabularyUiState): String = buildJsonObject {
        put("instructions", courseVocabularyInstructions.ios)
        put("unavailableLabel", courseVocabularyUnavailableLabel)
        put("loadStatus", state.loadStatus.name)
        // EnRuAcceptance-2026-09-28.md §7 item 4: the pack this exact [state] belongs to — always
        // in sync with [state.direction]/[state.document] because [rebuildIfCourseSwitched] (called
        // right before every snapshot) only ever rebuilds [session] to match
        // [polski.data.activeCoursePackId] at that same instant. The Swift picker reads these
        // instead of `model.preferences`' own target/native, which can briefly lag behind (or
        // self-correct after) this bridge's own pack switch — see `VocabularyView`'s own comment.
        val pairId = polski.data.activeCoursePackId
        put("target", pairId.substringBefore('-'))
        put("native", pairId.substringAfter('-'))
        put("direction", state.direction.wire)
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
}
