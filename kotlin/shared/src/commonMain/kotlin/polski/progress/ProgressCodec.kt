package polski.progress

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Instant
import polski.textwire.DecodeResult as WireDecodeResult
import polski.textwire.PolskiTextCodec
import polski.textwire.WireNode
import polski.srs.CardDecodeResult
import polski.srs.Scheduler
import polski.srs.SerializedCard
import polski.srs.SrsCardWire
import polski.srs.StoredCard

sealed interface DecodeResult {
    data class Valid(val document: ProgressDocument) : DecodeResult
    data class Invalid(val reason: String) : DecodeResult
    data class Unsupported(val version: Int) : DecodeResult
}

/** Strict v1 reader and React-compatible writer. No clock or storage is read here. */
object ProgressCodec {
    private val json = Json { isLenient = false }
    private const val JS_DATE_LIMIT_MILLIS = 8_640_000_000_000_000L

    fun decode(raw: String): DecodeResult {
        val root = try { json.parseToJsonElement(raw) as? JsonObject }
        catch (_: Exception) { null } ?: return DecodeResult.Invalid("Expected JSON object")
        val version = root.int("version") ?: return DecodeResult.Invalid("Invalid version")
        if (version != 1) return DecodeResult.Unsupported(version)
        val cardsJson = root["cards"] as? JsonArray ?: return DecodeResult.Invalid("Invalid cards")
        val statsJson = root["stats"] as? JsonObject ?: return DecodeResult.Invalid("Invalid stats")
        val cards = ArrayList<StoredCard>(cardsJson.size)
        val seen = HashSet<String>()
        for ((index, element) in cardsJson.withIndex()) {
            val wrapper = element as? JsonObject ?: return DecodeResult.Invalid("Invalid card at $index")
            val skillId = wrapper.string("skillId")?.takeIf { it.isNotBlank() }
                ?: return DecodeResult.Invalid("Invalid skillId at $index")
            if (!seen.add(skillId)) return DecodeResult.Invalid("Duplicate skillId: $skillId")
            val value = wrapper["card"] as? JsonObject ?: return DecodeResult.Invalid("Invalid card for $skillId")
            val serialized = serializedCard(value) ?: return DecodeResult.Invalid("Invalid FSRS card for $skillId")
            when (val decoded = SrsCardWire.decode(serialized)) {
                is CardDecodeResult.Valid -> cards += StoredCard(skillId, decoded.card)
                is CardDecodeResult.Invalid -> return DecodeResult.Invalid("$skillId: ${decoded.reason}")
            }
        }
        val stats = linkedMapOf<String, SkillStats>()
        for ((skillId, element) in statsJson) {
            if (skillId.isBlank()) return DecodeResult.Invalid("Invalid stats skillId")
            val value = element as? JsonObject ?: return DecodeResult.Invalid("Invalid stats for $skillId")
            stats[skillId] = SkillStats(
                reviews = value.nonnegativeInt("reviews") ?: return DecodeResult.Invalid("Invalid reviews for $skillId"),
                correct = value.nonnegativeInt("correct") ?: return DecodeResult.Invalid("Invalid correct for $skillId"),
                streak = value.nonnegativeInt("streak") ?: return DecodeResult.Invalid("Invalid streak for $skillId"),
                mistakes = value.nonnegativeInt("mistakes") ?: return DecodeResult.Invalid("Invalid mistakes for $skillId"),
            )
        }
        val reviewsToday = root.nonnegativeInt("reviewsToday") ?: return DecodeResult.Invalid("Invalid reviewsToday")
        val totalReviews = root.nonnegativeInt("totalReviews") ?: return DecodeResult.Invalid("Invalid totalReviews")
        val lastDay = root.string("lastDay")?.takeIf(::validDay) ?: return DecodeResult.Invalid("Invalid lastDay")
        return DecodeResult.Valid(
            ProgressDocument(Progress(1, cards.toList(), stats.toMap(), reviewsToday, lastDay, totalReviews), root),
        )
    }

    fun encodeLegacyV1(document: ProgressDocument): String {
        val originalCards = (document.source["cards"] as? JsonArray).orEmpty()
            .mapNotNull { it as? JsonObject }
            .associateBy { it.string("skillId") }
        val originalStats = document.source["stats"] as? JsonObject ?: JsonObject(emptyMap())
        val cards = document.progress.cards.map { stored ->
            val original = originalCards[stored.skillId] ?: JsonObject(emptyMap())
            val originalCard = original["card"] as? JsonObject ?: JsonObject(emptyMap())
            val encoded = SrsCardWire.encode(stored.card)
            val cardFields = originalCard.toMutableMap()
            cardFields["due"] = JsonPrimitive(encoded.due)
            cardFields["stability"] = JsonPrimitive(encoded.stability)
            cardFields["difficulty"] = JsonPrimitive(encoded.difficulty)
            cardFields["elapsed_days"] = JsonPrimitive(encoded.elapsedDays)
            cardFields["scheduled_days"] = JsonPrimitive(encoded.scheduledDays)
            cardFields["reps"] = JsonPrimitive(encoded.reps)
            cardFields["lapses"] = JsonPrimitive(encoded.lapses)
            cardFields["learning_steps"] = JsonPrimitive(encoded.learningSteps)
            cardFields["state"] = JsonPrimitive(encoded.state)
            if (encoded.lastReview == null) cardFields.remove("last_review")
            else cardFields["last_review"] = JsonPrimitive(encoded.lastReview)
            JsonObject(original + mapOf("skillId" to JsonPrimitive(stored.skillId), "card" to JsonObject(cardFields)))
        }
        val stats = document.progress.stats.mapValues { (skillId, value) ->
            val original = originalStats[skillId] as? JsonObject ?: JsonObject(emptyMap())
            JsonObject(original + mapOf(
                "reviews" to JsonPrimitive(value.reviews),
                "correct" to JsonPrimitive(value.correct),
                "streak" to JsonPrimitive(value.streak),
                "mistakes" to JsonPrimitive(value.mistakes),
            ))
        }
        val root = JsonObject(document.source + mapOf(
            "version" to JsonPrimitive(1),
            "cards" to JsonArray(cards),
            "stats" to JsonObject(stats),
            "reviewsToday" to JsonPrimitive(document.progress.reviewsToday),
            "lastDay" to JsonPrimitive(document.progress.lastDay),
            "totalReviews" to JsonPrimitive(document.progress.totalReviews),
        ))
        return json.encodeToString(JsonElement.serializer(), root)
    }

    // ---------------------------------------------------------------------------
    // Text wire encode/decode (POLSKI-TEXT arm A, NJ03 C2)
    // ---------------------------------------------------------------------------

    fun encodeText(document: ProgressDocument): ByteArray {
        val p = document.progress
        val cards = WireNode.WireList(p.cards.map { stored ->
            val enc = SrsCardWire.encode(stored.card)
            WireNode.WireMap(listOfNotNull(
                "skillId" to WireNode.WireString(stored.skillId),
                "due"            to WireNode.WireString(enc.due),
                "stability"      to WireNode.WireF64(enc.stability.toBits()),
                "difficulty"     to WireNode.WireF64(enc.difficulty.toBits()),
                "elapsed_days"   to WireNode.WireI64(enc.elapsedDays.toLong()),
                "scheduled_days" to WireNode.WireI64(enc.scheduledDays.toLong()),
                "reps"           to WireNode.WireI64(enc.reps.toLong()),
                "lapses"         to WireNode.WireI64(enc.lapses.toLong()),
                "learning_steps" to WireNode.WireI64(enc.learningSteps.toLong()),
                "state"          to WireNode.WireI64(enc.state.toLong()),
                enc.lastReview?.let { "last_review" to WireNode.WireString(it) },
            ))
        })
        val stats = WireNode.WireList(p.stats.entries.map { (skillId, s) ->
            WireNode.WireMap(listOf(
                "skillId"  to WireNode.WireString(skillId),
                "reviews"  to WireNode.WireI64(s.reviews.toLong()),
                "correct"  to WireNode.WireI64(s.correct.toLong()),
                "streak"   to WireNode.WireI64(s.streak.toLong()),
                "mistakes" to WireNode.WireI64(s.mistakes.toLong()),
            ))
        })
        val root = WireNode.WireMap(listOf(
            "version"      to WireNode.WireI64(p.version.toLong()),
            "cards"        to cards,
            "stats"        to stats,
            "reviewsToday" to WireNode.WireI64(p.reviewsToday.toLong()),
            "lastDay"      to WireNode.WireString(p.lastDay),
            "totalReviews" to WireNode.WireI64(p.totalReviews.toLong()),
        ))
        return PolskiTextCodec.encode(root, "progress", 2).bytes
    }

    fun decodeText(bytes: ByteArray): DecodeResult {
        val wire = PolskiTextCodec.decode(bytes)
        if (wire is WireDecodeResult.Err) return DecodeResult.Invalid(wire.message)
        val ok = wire as WireDecodeResult.Ok
        if (ok.kind != "progress") return DecodeResult.Invalid("wrong kind: ${ok.kind}")
        if (ok.schemaVersion != 2) return DecodeResult.Unsupported(ok.schemaVersion)
        val m = ok.root.children.toMap()
        val version = (m["version"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing version")
        val cardsNode = m["cards"] as? WireNode.WireList ?: return DecodeResult.Invalid("missing cards")
        val statsNode = m["stats"] as? WireNode.WireList ?: return DecodeResult.Invalid("missing stats")
        val reviewsToday = (m["reviewsToday"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing reviewsToday")
        val lastDay = (m["lastDay"] as? WireNode.WireString)?.value?.takeIf(::validDay) ?: return DecodeResult.Invalid("invalid lastDay")
        val totalReviews = (m["totalReviews"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing totalReviews")
        val cards = ArrayList<StoredCard>(cardsNode.items.size)
        val seen = HashSet<String>()
        for ((index, item) in cardsNode.items.withIndex()) {
            val cm = (item as? WireNode.WireMap)?.children?.toMap() ?: return DecodeResult.Invalid("invalid card at $index")
            val skillId = (cm["skillId"] as? WireNode.WireString)?.value?.takeIf { it.isNotBlank() } ?: return DecodeResult.Invalid("missing skillId at $index")
            if (!seen.add(skillId)) return DecodeResult.Invalid("duplicate skillId: $skillId")
            val serialized = SerializedCard(
                due            = (cm["due"] as? WireNode.WireString)?.value ?: return DecodeResult.Invalid("missing due for $skillId"),
                stability      = Double.fromBits((cm["stability"] as? WireNode.WireF64)?.bits ?: return DecodeResult.Invalid("missing stability")),
                difficulty     = Double.fromBits((cm["difficulty"] as? WireNode.WireF64)?.bits ?: return DecodeResult.Invalid("missing difficulty")),
                elapsedDays    = (cm["elapsed_days"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing elapsed_days"),
                scheduledDays  = (cm["scheduled_days"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing scheduled_days"),
                reps           = (cm["reps"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing reps"),
                lapses         = (cm["lapses"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing lapses"),
                learningSteps  = (cm["learning_steps"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing learning_steps"),
                state          = (cm["state"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing state"),
                lastReview     = (cm["last_review"] as? WireNode.WireString)?.value,
            )
            when (val decoded = SrsCardWire.decode(serialized)) {
                is CardDecodeResult.Valid -> cards += StoredCard(skillId, decoded.card)
                is CardDecodeResult.Invalid -> return DecodeResult.Invalid("$skillId: ${decoded.reason}")
            }
        }
        val stats = linkedMapOf<String, SkillStats>()
        for ((idx, item) in statsNode.items.withIndex()) {
            val sm = (item as? WireNode.WireMap)?.children?.toMap() ?: return DecodeResult.Invalid("invalid stats at $idx")
            val skillId = (sm["skillId"] as? WireNode.WireString)?.value?.takeIf { it.isNotBlank() } ?: return DecodeResult.Invalid("missing stats skillId at $idx")
            stats[skillId] = SkillStats(
                reviews  = (sm["reviews"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing reviews for $skillId"),
                correct  = (sm["correct"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing correct for $skillId"),
                streak   = (sm["streak"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing streak for $skillId"),
                mistakes = (sm["mistakes"] as? WireNode.WireI64)?.value?.toInt() ?: return DecodeResult.Invalid("missing mistakes for $skillId"),
            )
        }
        return DecodeResult.Valid(
            ProgressDocument(Progress(version, cards.toList(), stats.toMap(), reviewsToday, lastDay, totalReviews))
        )
    }

    private fun SerializedCard(due: String, stability: Double, difficulty: Double,
        elapsedDays: Int, scheduledDays: Int, reps: Int, lapses: Int, learningSteps: Int,
        state: Int, lastReview: String?): SerializedCard =
        polski.srs.SerializedCard(due, stability, difficulty, elapsedDays, scheduledDays, reps, lapses, learningSteps, state, lastReview)

    fun fresh(skillIds: List<String>, at: Instant, localDay: String, scheduler: Scheduler): ProgressDocument {
        require(validDay(localDay)) { "Invalid local day" }
        require(skillIds.all { it.isNotBlank() } && skillIds.distinct().size == skillIds.size) { "Invalid skill IDs" }
        return ProgressDocument(Progress(
            cards = skillIds.map { scheduler.newCard(it, at) },
            stats = skillIds.associateWith { SkillStats() },
            lastDay = localDay,
        ))
    }

    fun completeKnownSkills(document: ProgressDocument, skillIds: List<String>, at: Instant, scheduler: Scheduler): ProgressDocument {
        require(skillIds.all { it.isNotBlank() } && skillIds.distinct().size == skillIds.size) { "Invalid skill IDs" }
        val existing = document.progress.cards.mapTo(HashSet()) { it.skillId }
        val cards = document.progress.cards.toMutableList()
        for (skillId in skillIds) if (existing.add(skillId)) cards += scheduler.newCard(skillId, at)
        val stats = document.progress.stats.toMutableMap()
        for (skillId in skillIds) if (skillId !in stats) stats[skillId] = SkillStats()
        return document.copy(progress = document.progress.copy(cards = cards.toList(), stats = stats.toMap()))
    }

    fun normalizeDay(document: ProgressDocument, localDay: String): ProgressDocument {
        require(validDay(localDay)) { "Invalid local day" }
        if (document.progress.lastDay == localDay) return document
        return document.copy(progress = document.progress.copy(reviewsToday = 0, lastDay = localDay))
    }

    private fun serializedCard(value: JsonObject): SerializedCard? {
        val due = value.string("due")?.takeIf(::validInstant) ?: return null
        val last = when (val item = value["last_review"]) {
            null, JsonNull -> null
            else -> (item as? JsonPrimitive)?.content?.takeIf(::validInstant) ?: return null
        }
        return SerializedCard(
            due = due,
            stability = value.finiteNonnegativeDouble("stability") ?: return null,
            difficulty = value.finiteNonnegativeDouble("difficulty") ?: return null,
            elapsedDays = value.nonnegativeInt("elapsed_days") ?: return null,
            scheduledDays = value.nonnegativeInt("scheduled_days") ?: return null,
            reps = value.nonnegativeInt("reps") ?: return null,
            lapses = value.nonnegativeInt("lapses") ?: return null,
            learningSteps = value.nonnegativeInt("learning_steps") ?: return null,
            state = value.int("state") ?: return null,
            lastReview = last,
        )
    }

    private fun validInstant(value: String): Boolean {
        val instant = try { Instant.parse(value) } catch (_: IllegalArgumentException) { return false }
        return instant.toEpochMilliseconds() in -JS_DATE_LIMIT_MILLIS..JS_DATE_LIMIT_MILLIS
    }

    internal fun validDay(value: String): Boolean {
        if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(value)) return false
        val year = value.substring(0, 4).toInt()
        val month = value.substring(5, 7).toInt()
        val day = value.substring(8, 10).toInt()
        if (year == 0 || month !in 1..12) return false
        val leap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        val days = when (month) { 2 -> if (leap) 29 else 28; 4, 6, 9, 11 -> 30; else -> 31 }
        return day in 1..days
    }

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)
        ?.takeIf { it.isString }?.content

    private fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)
        ?.takeUnless { it.isString }?.intOrNull

    private fun JsonObject.nonnegativeInt(key: String): Int? = int(key)?.takeIf { it >= 0 }

    private fun JsonObject.finiteNonnegativeDouble(key: String): Double? = (this[key] as? JsonPrimitive)
        ?.takeUnless { it.isString }?.doubleOrNull?.takeIf { it.isFinite() && it >= 0 }
}
