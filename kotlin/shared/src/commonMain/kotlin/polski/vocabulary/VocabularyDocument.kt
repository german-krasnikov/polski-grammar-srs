package polski.vocabulary

import polski.textwire.PolskiTextCodec
import polski.textwire.WireNode
import polski.textwire.DecodeResult as WireDecodeResult

import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import polski.data.VocabularyItem
import polski.data.packRegistry
import polski.data.vocabularyItems
import polski.grammar.trimJsWhitespace
import polski.srs.CardDecodeResult
import polski.srs.Rating
import polski.srs.Scheduler
import polski.srs.SchedulePreview
import polski.srs.SerializedCard
import polski.srs.SrsCard
import polski.srs.SrsCardWire
import polski.srs.StoredCard

/**
 * Which side of a pack's pair the learner recalls (native → target, or target → native).
 * EN-09 (gap F, `Plans/Kotlin/EnRuPackPlan.md` §5) opened this from a closed 2-value enum to a
 * string-backed id — the same pattern [polski.presentation.StyleId] used for UC-01 — so a second
 * pack's pair (`"en-ru"/"ru-en"`) is just a new [StudyDirection], not a Kotlin change here. [wire]
 * keeps the exact `"ru-pl"/"pl-ru"` bytes the old enum's [wire] produced, so every persisted card
 * key ([VocabularyCodec.cardKey]) and the wire format hosts already speak stay byte-identical.
 */
data class StudyDirection(val wire: String) {
    companion object {
        val RussianToPolish = StudyDirection("ru-pl")
        val PolishToRussian = StudyDirection("pl-ru")

        /** Generalizes [RussianToPolish]/[PolishToRussian] (themselves this exact pair for pl-ru's
         *  own native="ru"/target="pl") to any pack's own (target, native) codes —
         *  EnRuAcceptance-2026-09-28.md §7 item 4: a second pack's UI needs no new named
         *  [StudyDirection] constants of its own, just its [polski.data.CoursePack.targetLanguage]/
         *  [polski.data.CoursePack.nativeLanguage]. */
        fun nativeToTarget(target: String, native: String) = StudyDirection("$native-$target")
        fun targetToNative(target: String, native: String) = StudyDirection("$target-$native")
    }
}

/** The 2 built-in directions in the old enum's declaration order — replaces `StudyDirection.entries`
 *  for hosts that need to iterate or look one up by [StudyDirection.wire]. */
val builtInStudyDirections: List<StudyDirection> = listOf(StudyDirection.RussianToPolish, StudyDirection.PolishToRussian)

/**
 * The 2 [StudyDirection]s for whichever pack is active right now — native→target first,
 * target→native second, the same order [builtInStudyDirections] fixes for pl-ru (byte-identical
 * there), built from [StudyDirection.nativeToTarget]/[StudyDirection.targetToNative]
 * (EnRuAcceptance-2026-09-28.md §7 item 4; en-ru gets `["ru-en", "en-ru"]`, see
 * [VocabularyDocumentTest.opensToASecondPacksDirectionWithoutTouchingTheBuiltIns]). Read fresh,
 * not cached: the active pack can change mid-process (EN-22).
 */
val activeStudyDirections: List<StudyDirection> get() = packRegistry.active.let { pack ->
    listOf(StudyDirection.nativeToTarget(pack.targetLanguage, pack.nativeLanguage), StudyDirection.targetToNative(pack.targetLanguage, pack.nativeLanguage))
}

/** One selectable direction for a vocabulary review, paired with its own display label. */
data class StudyDirectionOption(val direction: StudyDirection, val label: String)

/** Human name for a target/native language code — the same small display-name map every host's
 *  own target/native course picker already keeps (e.g. `MacPreferencesSession`'s own
 *  `languageDisplayNames`); [studyDirectionOptions] needs the same names to label a direction for
 *  any pack, not just pl-ru's hardcoded two. */
private val languageDisplayNames = mapOf("pl" to "Польский", "en" to "Английский", "ru" to "Русский")

/**
 * EnRuAcceptance-2026-09-28.md §7 item 4: the 2 study directions of a pack's own [target]/[native]
 * pair, in the same (native→target, target→native) order [builtInStudyDirections] hardcodes for
 * pl-ru — `studyDirectionOptions("pl", "ru")` returns pl-ru's own byte-identical wires and labels
 * ("ru-pl" "Русский → польский", "pl-ru" "Польский → русский"); any other pack (e.g. `"en"`/`"ru"`)
 * gets its own pair the same way, so a direction picker built from this never needs a hardcoded
 * 2-case list again.
 */
fun studyDirectionOptions(target: String, native: String): List<StudyDirectionOption> {
    fun label(from: String, to: String) = "${languageDisplayNames[from] ?: from} → ${(languageDisplayNames[to] ?: to).lowercase()}"
    return listOf(
        StudyDirectionOption(StudyDirection.nativeToTarget(target, native), label(native, target)),
        StudyDirectionOption(StudyDirection.targetToNative(target, native), label(target, native)),
    )
}

/** "по-{language}" adverb form ("по-польски", "по-русски", "по-английски") for [recallCaption] —
 *  grounded only in the 3 language codes any pack in this build actually uses. */
private val languageAdverbs = mapOf("pl" to "польски", "ru" to "русски", "en" to "английски")

/**
 * EnRuAcceptance-2026-09-28.md §7 item 4: whether [this] direction asks the learner to see the
 * native word and recall/produce the target one — [VocabularyItem.lemma] is always the target
 * word and [VocabularyItem.translation] always the native one (every pack's own `course.json`/
 * `pair.json` `vocabulary.items` is authored that way), so this single boolean is everything a
 * card needs to pick which field is the prompt and which is the revealed answer, for any pack's
 * [target]/[native] — not the `wire == "ru-pl"` compare a pl-ru-only card would hardcode.
 */
fun StudyDirection.recallsTarget(target: String, native: String): Boolean = this == StudyDirection.nativeToTarget(target, native)

/** "Вспомни по-…" prompt caption for whichever language [this] direction asks the learner to
 *  produce. */
fun StudyDirection.recallCaption(target: String, native: String): String {
    val code = if (recallsTarget(target, native)) target else native
    return "Вспомни по-${languageAdverbs[code] ?: code}"
}

/** Nominative, lowercase name of whichever language [this] direction reveals as the answer —
 *  e.g. "польский" recalling target pl, "русский" recalling native ru. */
fun StudyDirection.answerLanguageLabel(target: String, native: String): String {
    val code = if (recallsTarget(target, native)) target else native
    return (languageDisplayNames[code] ?: code).lowercase()
}

/**
 * EnRuAcceptance-2026-09-28.md §7 item 4: the active pack's own native→target direction — pl-ru's
 * own byte-identical [StudyDirection.RussianToPolish] ("ru-pl") while pl-ru is active, e.g. en-ru's
 * own "ru-en" once en-ru is. The sensible default for a fresh [VocabularyUiState] or a just-loaded
 * document with no direction choice of its own yet; reads [packRegistry.active] live, the same
 * discipline [VocabularyCodec.key]/[VocabularyCodec.cardKey] already apply, so a session that
 * starts after a pack switch never opens on the *previous* pack's own hardcoded direction.
 */
fun defaultStudyDirection(): StudyDirection = activeStudyDirections.first()

data class VocabularyDocument(
    val selectedIds: List<String> = emptyList(),
    val custom: List<VocabularyItem> = emptyList(),
    val cards: Map<String, SrsCard> = emptyMap(),
)

object VocabularyCodec {
    /** UC-04: was the literal `"pl-ru"` — now the active pack's [polski.data.CoursePack.pairId].
     * EnRuAcceptance-2026-09-28.md §7 item 2: read fresh (was `by lazy`, which froze this to
     * whichever pack was active on first read for the rest of the process) so a host's vocabulary
     * repository keeps loading/saving the right pack's own document after
     * [polski.data.selectCoursePack] switches the active pack — same fix [polski.data.skills] and
     * the rest of `CourseData.kt`'s per-pack `val`s already applied. */
    val key: String get() = "polski-vocabulary-${packRegistry.active.pairId}-v1"

    fun cardKey(id: String, direction: StudyDirection): String = "${packRegistry.active.pairId}:vocabulary:${direction.wire}:$id"

    fun dueIds(document: VocabularyDocument, direction: StudyDirection, scheduler: Scheduler, at: Instant): List<String> =
        document.selectedIds.filter { id ->
            val key = cardKey(id, direction)
            scheduler.isDue(StoredCard(key, document.cards[key] ?: scheduler.newCard(key, at).card), at)
        }

    fun select(document: VocabularyDocument, id: String, checked: Boolean): VocabularyDocument {
        require(item(document, id) != null) { "Unknown vocabulary item" }
        return document.copy(selectedIds = if (checked) (document.selectedIds + id).distinct()
            else document.selectedIds.filterNot { it == id })
    }

    fun review(document: VocabularyDocument, id: String, direction: StudyDirection, rating: Rating,
               scheduler: Scheduler, at: Instant): VocabularyDocument {
        require(id in document.selectedIds && rating in listOf(Rating.Again, Rating.Good))
        val key = cardKey(id, direction)
        val old = StoredCard(key, document.cards[key] ?: scheduler.newCard(key, at).card)
        return document.copy(cards = document.cards + (key to scheduler.review(old, rating, at).card))
    }

    /** UX4-13: the four rating intervals a real [review] of [id] would produce, without writing
     *  anything — mirrors [dueIds]'s way of reading the stored (or not-yet-created) card, but
     *  routes it through [Scheduler.preview] instead of [Scheduler.review]. */
    fun preview(document: VocabularyDocument, id: String, direction: StudyDirection,
                scheduler: Scheduler, at: Instant): SchedulePreview {
        val key = cardKey(id, direction)
        val card = StoredCard(key, document.cards[key] ?: scheduler.newCard(key, at).card)
        return scheduler.preview(card, at)
    }

    fun item(document: VocabularyDocument, id: String): VocabularyItem? =
        vocabularyItems.firstOrNull { it.id == id } ?: document.custom.firstOrNull { it.id == id }

    fun merge(current: VocabularyDocument, other: VocabularyDocument): VocabularyDocument {
        val local = validate(current)
        val imported = validate(other)
        val incoming = imported.custom.filter { item -> local.custom.none { it.id == item.id } }
        val restoring = local.selectedIds.isEmpty() && local.custom.isEmpty() && local.cards.isEmpty()
        val existingLemmas = (if (restoring) emptyList() else vocabularyItems + local.custom)
            .map { trimJsWhitespace(it.lemma).lowercase() }.toMutableSet()
        incoming.forEach { item ->
            require(existingLemmas.add(trimJsWhitespace(item.lemma).lowercase())) { "Это польское слово уже есть в словаре" }
        }
        val custom = local.custom + incoming
        return validate(local.copy(custom = custom, selectedIds = (local.selectedIds + imported.selectedIds).distinct(),
            cards = imported.cards + local.cards))
    }

    fun validate(document: VocabularyDocument): VocabularyDocument {
        require(document.custom.map(VocabularyItem::id).distinct().size == document.custom.size)
        val custom = document.custom.map { value ->
            require(Regex("^user\\.[a-f0-9-]{36}$").matches(value.id) && value.frequencyRank == null && value.custom)
            val normalized = value.copy(lemma = trimJsWhitespace(value.lemma),
                translation = trimJsWhitespace(value.translation), form = trimJsWhitespace(value.form),
                example = trimJsWhitespace(value.example))
            require(listOf(normalized.lemma, normalized.translation, normalized.form, normalized.example)
                .all { it.isNotEmpty() && it.length <= 240 }) { "Заполни слово, перевод, форму и пример (до 240 символов)." }
            require(value.level in listOf("—", "A1", "A2", "B1", "B2", "C1", "C2"))
            normalized
        }
        require(document.selectedIds.distinct().size == document.selectedIds.size)
        require(document.selectedIds.all { item(document, it) != null })
        // EnRuAcceptance-2026-09-28.md §7 item 4: allowed wires follow the active pack, not pl-ru's literal pair.
        val wires = activeStudyDirections.map(StudyDirection::wire)
        require(document.cards.keys.all { key -> wires.any { key.startsWith("${packRegistry.active.pairId}:vocabulary:$it:") } })
        return document.copy(custom = custom)
    }

    fun decode(raw: String): VocabularyDocument {
        // NJ03: try text wire first, fall back to legacy JSON.
        if (raw.startsWith("POLSKI-TEXT")) return decodeText(raw.encodeToByteArray())
        val root = Json.parseToJsonElement(raw).jsonObject
        require(root.getValue("version").jsonPrimitive.int == 1 && root.getValue("pair").jsonPrimitive.content == packRegistry.active.pairId)
        val selected = root.getValue("selectedIds").jsonArray.map { it.jsonPrimitive.content }
        val custom = root.getValue("custom").jsonArray.map { element ->
            val value = element.jsonObject
            VocabularyItem(value.text("id"), value.text("lemma"), value.text("translation"), value.text("form"),
                value.text("example"), value.text("level"), null, true)
        }
        val cards = root.getValue("cards").jsonObject.mapValues { (_, element) ->
            val value = element.jsonObject
            val wire = SerializedCard(value.text("due"), value.number("stability"), value.number("difficulty"),
                value.integer("elapsed_days"), value.integer("scheduled_days"), value.integer("reps"),
                value.integer("lapses"), value.integer("learning_steps"), value.integer("state"),
                value["last_review"]?.jsonPrimitive?.content?.takeUnless { it == "null" })
            when (val decoded = SrsCardWire.decode(wire)) {
                is CardDecodeResult.Valid -> decoded.card
                is CardDecodeResult.Invalid -> error(decoded.reason)
            }
        }
        return validate(VocabularyDocument(selected, custom, cards))
    }

    /** NJ03 C6: text wire encoder replaces JSON encode for new saves. */
    fun decodeText(bytes: ByteArray): VocabularyDocument {
        val result = PolskiTextCodec.decode(bytes)
        require(result is WireDecodeResult.Ok) { "Invalid POLSKI-TEXT: ${(result as? WireDecodeResult.Err)?.message}" }
        require(result.kind == "vocabulary" && result.schemaVersion == 1)
        val root = result.root
        val pairId = (root.get("pair") as WireNode.WireString).value
        require(pairId == packRegistry.active.pairId)
        val selected = (root.get("selectedIds") as WireNode.WireList).items.map { (it as WireNode.WireString).value }
        val custom = (root.get("custom") as WireNode.WireList).items.map { node ->
            val m = node as WireNode.WireMap
            VocabularyItem(
                (m.get("id") as WireNode.WireString).value,
                (m.get("lemma") as WireNode.WireString).value,
                (m.get("translation") as WireNode.WireString).value,
                (m.get("form") as WireNode.WireString).value,
                (m.get("example") as WireNode.WireString).value,
                (m.get("level") as WireNode.WireString).value,
                null, true,
            )
        }
        val cards = (root.get("cards") as WireNode.WireList).items.associate { node ->
            val m = node as WireNode.WireMap
            val key = (m.get("key") as WireNode.WireString).value
            val wire = SerializedCard(
                (m.get("due") as WireNode.WireString).value,
                Double.fromBits((m.get("stability") as WireNode.WireF64).bits),
                Double.fromBits((m.get("difficulty") as WireNode.WireF64).bits),
                (m.get("elapsed_days") as WireNode.WireI64).value.toInt(),
                (m.get("scheduled_days") as WireNode.WireI64).value.toInt(),
                (m.get("reps") as WireNode.WireI64).value.toInt(),
                (m.get("lapses") as WireNode.WireI64).value.toInt(),
                (m.get("learning_steps") as WireNode.WireI64).value.toInt(),
                (m.get("state") as WireNode.WireI64).value.toInt(),
                (m.get("last_review") as? WireNode.WireString)?.value,
            )
            val card = when (val result = SrsCardWire.decode(wire)) {
                is CardDecodeResult.Valid -> result.card
                is CardDecodeResult.Invalid -> error(result.reason)
            }
            key to card
        }
        return validate(VocabularyDocument(selected, custom, cards))
    }

    fun encodeText(document: VocabularyDocument): ByteArray {
        val normalized = validate(document)
        val pairId = packRegistry.active.pairId
        val selectedList = WireNode.WireList(normalized.selectedIds.map { WireNode.WireString(it) })
        val customList = WireNode.WireList(normalized.custom.map { item ->
            WireNode.WireMap(listOf(
                "id" to WireNode.WireString(item.id),
                "lemma" to WireNode.WireString(item.lemma),
                "translation" to WireNode.WireString(item.translation),
                "form" to WireNode.WireString(item.form),
                "example" to WireNode.WireString(item.example),
                "level" to WireNode.WireString(item.level),
            ))
        })
        val cardsList = WireNode.WireList(normalized.cards.entries.map { (key, card) ->
            val wire = SrsCardWire.encode(card)
            WireNode.WireMap(listOfNotNull(
                "key" to WireNode.WireString(key),
                "due" to WireNode.WireString(wire.due),
                "stability" to WireNode.WireF64(wire.stability.toBits()),
                "difficulty" to WireNode.WireF64(wire.difficulty.toBits()),
                "elapsed_days" to WireNode.WireI64(wire.elapsedDays.toLong()),
                "scheduled_days" to WireNode.WireI64(wire.scheduledDays.toLong()),
                "reps" to WireNode.WireI64(wire.reps.toLong()),
                "lapses" to WireNode.WireI64(wire.lapses.toLong()),
                "learning_steps" to WireNode.WireI64(wire.learningSteps.toLong()),
                "state" to WireNode.WireI64(wire.state.toLong()),
                wire.lastReview?.let { "last_review" to WireNode.WireString(it) },
            ))
        })
        val root = WireNode.WireMap(listOf(
            "version" to WireNode.WireI64(1),
            "pair" to WireNode.WireString(pairId),
            "selectedIds" to selectedList,
            "custom" to customList,
            "cards" to cardsList,
        ))
        return PolskiTextCodec.encode(root, "vocabulary", 1).bytes
    }

    fun encode(document: VocabularyDocument): String = buildJsonObject {
        val normalized = validate(document)
        put("version", 1)
        put("pair", packRegistry.active.pairId)
        put("selectedIds", buildJsonArray { normalized.selectedIds.forEach { add(JsonPrimitive(it)) } })
        put("custom", buildJsonArray { normalized.custom.forEach { item -> add(buildJsonObject {
            put("id", item.id); put("lemma", item.lemma); put("translation", item.translation)
            put("form", item.form); put("example", item.example); put("level", item.level)
            put("frequencyRank", JsonPrimitive(null as String?)); put("custom", true)
        }) } })
        put("cards", buildJsonObject { normalized.cards.forEach { (key, card) ->
            val wire = SrsCardWire.encode(card)
            put(key, buildJsonObject {
                put("due", wire.due); put("stability", wire.stability); put("difficulty", wire.difficulty)
                put("elapsed_days", wire.elapsedDays); put("scheduled_days", wire.scheduledDays)
                put("reps", wire.reps); put("lapses", wire.lapses); put("learning_steps", wire.learningSteps)
                put("state", wire.state); wire.lastReview?.let { put("last_review", it) }
            })
        } })
    }.toString()
}

private fun JsonObject.text(key: String): String = getValue(key).jsonPrimitive.content
private fun JsonObject.integer(key: String): Int = getValue(key).jsonPrimitive.int
private fun JsonObject.number(key: String): Double = getValue(key).jsonPrimitive.double
