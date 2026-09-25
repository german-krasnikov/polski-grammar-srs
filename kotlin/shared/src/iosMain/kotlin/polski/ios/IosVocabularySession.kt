package polski.ios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import polski.data.frequencyItems
import polski.data.vocabularyItems
import polski.data.courseVocabularyInstructions
import polski.data.courseVocabularyUnavailableLabel
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.vocabulary.StudyDirection
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularySession
import polski.vocabulary.VocabularyUiState

/** Scene-owned SwiftUI bridge. Kotlin alone owns review scheduling and durable writes. */
class IosVocabularySession {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val session = VocabularySession(IosVocabularyRepository(), FsrsScheduler(),
        { Clock.System.now() }, { "user.${NSUUID().UUIDString.lowercase()}" })
    var onState: ((String) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(snapshot(session.state.value))
        }

    init {
        scope.launch { session.state.collect { onState?.invoke(snapshot(it)) } }
        scope.launch { session.start() }
    }

    fun currentSnapshot(): String = snapshot(session.state.value)
    fun exportJson(): String? = session.exportJson()

    fun dispatch(command: String, value: String = "") {
        when (command) {
            "direction" -> StudyDirection.entries.firstOrNull { it.wire == value }?.let(session::setDirection)
            "filter" -> if (value in listOf("A1", "A2", "B1", "100", "500", "1000", "mine")) session.setFilter(value)
            "typed" -> session.setTyped(value == "true")
            "draft" -> session.setDraft(value)
            "reveal" -> session.reveal()
            "refresh" -> session.refresh()
            "again" -> scope.launch { session.rate(Rating.Again) }
            "good" -> scope.launch { session.rate(Rating.Good) }
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
