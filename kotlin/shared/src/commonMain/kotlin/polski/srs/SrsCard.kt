package polski.srs

import kotlin.time.Instant

/** Legacy numeric FSRS state values. */
enum class CardState(val wire: Int) { New(0), Learning(1), Review(2), Relearning(3) }

/** Immutable scheduling fields; day counts use UTC calendar days, due uses an absolute instant. */
data class SrsCard(
    val due: Instant,
    val stability: Double = 0.0,
    val difficulty: Double = 0.0,
    val elapsedDays: Int = 0,
    val scheduledDays: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val learningSteps: Int = 0,
    val state: CardState = CardState.New,
    val lastReview: Instant? = null,
)

data class StoredCard(val skillId: String, val card: SrsCard)
