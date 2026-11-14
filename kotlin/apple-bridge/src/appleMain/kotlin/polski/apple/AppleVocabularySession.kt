package polski.apple

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.VocabularyItem
import polski.data.activeCoursePackId
import polski.data.courseVocabularyInstructions
import polski.data.courseVocabularyUnavailableLabel
import polski.data.frequencyItems
import polski.data.vocabularyItems
import polski.presentation.cardEffectFor
import polski.srs.Rating
import polski.vocabulary.StudyDirection
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularySession
import polski.vocabulary.VocabularyUiState

/**
 * Scene-owned SwiftUI bridge shared by `IosVocabularySession`/`MacVocabularySession` (Kotlin's
 * default hierarchy template's `appleMain`, shared by exactly `iosMain`+`macosMain` — see
 * `Plans/DSL-DEDUP-MIGRATION.md` §3: both twins target the same language/paradigm, so this is a
 * native extraction, not a DSL transfer). Kotlin alone owns review scheduling and durable writes;
 * a platform differs only in [newSession] (storage/scheduler), [directionFor] (lookup domain) and
 * [putExtraSnapshotFields] (platform-only wire fields).
 */
abstract class AppleVocabularySession {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    // [newSession] is only safe to call once a subclass's own constructor properties (storage
    // directory/defaults, scheduler) are set — i.e. never from this base class's own constructor.
    // A subclass's `init { startSession() }` runs after those are set and starts this lazily.
    protected lateinit var session: VocabularySession
    // EnRuAcceptance-2026-09-28.md §7 item 2: this in-memory session needs an explicit restart on
    // a pack switch — see [rebuildIfCourseSwitched].
    private var sessionPackId = activeCoursePackId
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
     * (`Plans/Kotlin/FlipCardRivePlan.md` D3).
     */
    var onEffect: ((String) -> Unit)? = null

    protected abstract fun newSession(): VocabularySession

    /** Each subclass calls this once from its own `init` block, after its storage properties are set. */
    protected fun startSession() {
        session = newSession()
        observeAndStart()
    }

    /** The active pack's own study-direction lookup; an unknown wire is a silent no-op. */
    protected abstract fun directionFor(wire: String): StudyDirection?

    /** Platform-only wire fields (iOS: target/native; macOS: directionOptions/promptCaption/recallTarget/now/intervals). */
    protected open fun putExtraSnapshotFields(builder: JsonObjectBuilder, state: VocabularyUiState) {}

    private fun observeAndStart() {
        observer = scope.launch { session.state.collect { onState?.invoke(snapshot(it)) } }
        scope.launch { session.start() }
    }

    /** [newSession] never eagerly generates content, so no failure fallback is needed — the new
     *  pack's own document simply loads (or starts empty) the next time [VocabularySession.start] runs. */
    private fun rebuildIfCourseSwitched() {
        val current = activeCoursePackId
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
            "direction" -> directionFor(value)?.let(session::setDirection)
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

    /** iOS also clears [onEffect]; macOS historically does not — each subclass's own override. */
    open fun close() { onState = null; scope.cancel() }

    private fun snapshot(state: VocabularyUiState): String = buildJsonObject {
        put("instructions", courseVocabularyInstructions.ios)
        put("unavailableLabel", courseVocabularyUnavailableLabel)
        put("loadStatus", state.loadStatus.name)
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
        putExtraSnapshotFields(this, state)
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
