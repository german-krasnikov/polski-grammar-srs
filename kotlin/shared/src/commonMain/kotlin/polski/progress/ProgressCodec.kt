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
