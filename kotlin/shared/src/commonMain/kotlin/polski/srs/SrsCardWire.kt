package polski.srs

import kotlin.time.Instant

/** Typed legacy JSON fields. The progress codec maps these names to snake_case keys. */
data class SerializedCard(
    val due: String,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val scheduledDays: Int,
    val reps: Int,
    val lapses: Int,
    val learningSteps: Int,
    val state: Int,
    val lastReview: String? = null,
)

sealed interface CardDecodeResult {
    data class Valid(val card: SrsCard) : CardDecodeResult
    data class Invalid(val reason: String) : CardDecodeResult
}

/** Validates known legacy fields without silently replacing malformed progress. */
object SrsCardWire {
    fun decode(value: SerializedCard): CardDecodeResult {
        val due = try { Instant.parse(value.due) } catch (_: IllegalArgumentException) { return CardDecodeResult.Invalid("Invalid due") }
        val last = try { value.lastReview?.let(Instant::parse) } catch (_: IllegalArgumentException) { return CardDecodeResult.Invalid("Invalid last_review") }
        val state = CardState.entries.firstOrNull { it.wire == value.state } ?: return CardDecodeResult.Invalid("Invalid state")
        if (!value.stability.isFinite() || !value.difficulty.isFinite() || value.stability < 0 || value.difficulty < 0 ||
            value.elapsedDays < 0 || value.scheduledDays < 0 || value.reps < 0 || value.lapses < 0 || value.learningSteps < 0)
            return CardDecodeResult.Invalid("Invalid FSRS number")
        val memoryIsInitial = value.stability == 0.0 && value.difficulty == 0.0
        val memoryIsLearned = value.stability >= 0.001 && value.difficulty >= 1.0
        if ((!memoryIsInitial && !memoryIsLearned) || (memoryIsInitial && state != CardState.New) ||
            (state == CardState.New && !memoryIsInitial)) {
            return CardDecodeResult.Invalid("Invalid FSRS memory state")
        }
        return CardDecodeResult.Valid(SrsCard(due, value.stability, value.difficulty, value.elapsedDays, value.scheduledDays, value.reps, value.lapses, value.learningSteps, state, last))
    }

    fun encode(card: SrsCard): SerializedCard = SerializedCard(
        isoMilliseconds(card.due), card.stability, card.difficulty, card.elapsedDays, card.scheduledDays,
        card.reps, card.lapses, card.learningSteps, card.state.wire, card.lastReview?.let(::isoMilliseconds),
    )

    private fun isoMilliseconds(value: Instant): String {
        val rounded = Instant.fromEpochMilliseconds(value.toEpochMilliseconds()).toString()
        if (!rounded.endsWith('Z')) return rounded
        val body = rounded.dropLast(1)
        val dot = body.lastIndexOf('.')
        return if (dot < 0) "$body.000Z" else body.substring(0, dot) + "." + body.substring(dot + 1).padEnd(3, '0').take(3) + "Z"
    }
}
