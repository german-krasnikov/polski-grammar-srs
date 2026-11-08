package polski.srs

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Instant

/** Pure ts-fsrs 5.4.2 BasicScheduler port with fixed product settings and seeded fuzz. */
class FsrsScheduler internal constructor(private val fuzzEnabled: Boolean) : Scheduler {
    constructor() : this(true)

    override fun newCard(skillId: String, at: Instant): StoredCard = StoredCard(skillId, SrsCard(at))

    override fun preview(card: StoredCard, at: Instant): SchedulePreview = SchedulePreview(
        review(card, Rating.Again, at).card.due,
        review(card, Rating.Hard, at).card.due,
        review(card, Rating.Good, at).card.due,
        review(card, Rating.Easy, at).card.due,
    )

    override fun review(card: StoredCard, rating: Rating, at: Instant): StoredCard {
        val old = card.card
        val elapsed = if (old.state == CardState.New || old.lastReview == null) {
            0
        } else {
            utcDay(at) - utcDay(old.lastReview)
        }
        require(elapsed >= 0) { "Review precedes last UTC review day" }

        // BasicScheduler increments reps before constructing this seed, using the old memory state.
        val seed = "${at.toEpochMilliseconds()}_${old.reps + 1}_${jsNumberString(old.difficulty * old.stability)}"

        fun next(grade: Rating): SrsCard {
            val (difficulty, stability) = FsrsMath.nextState(old.difficulty, old.stability, elapsed, grade)
            return old.copy(
                due = at,
                difficulty = difficulty,
                stability = stability,
                elapsedDays = elapsed,
                reps = old.reps + 1,
                lastReview = at,
            )
        }

        fun days(base: SrsCard, count: Int): SrsCard = base.copy(
            due = Instant.fromEpochMilliseconds(at.toEpochMilliseconds() + count.toLong() * MILLIS_PER_DAY),
            scheduledDays = count,
            state = CardState.Review,
            learningSteps = 0,
        )

        fun step(base: SrsCard, grade: Rating, target: CardState): SrsCard {
            val steps = if (old.state == CardState.Review || old.state == CardState.Relearning) {
                intArrayOf(10)
            } else {
                intArrayOf(1, 10)
            }
            val current = old.learningSteps
            if (current >= steps.size) {
                return days(base, FsrsMath.interval(base.stability, elapsed, seed, fuzzEnabled))
            }
            val minutes = when (grade) {
                Rating.Again -> steps.first()
                Rating.Hard -> if (steps.size == 1) 15 else 6
                Rating.Good -> steps.getOrNull(current + 1) ?: 0
                Rating.Easy -> 0
            }
            val nextStep = when (grade) {
                Rating.Again -> 0
                Rating.Hard -> current
                Rating.Good -> current + 1
                Rating.Easy -> 0
            }
            return if (minutes in 1..1439) {
                base.copy(
                    due = Instant.fromEpochMilliseconds(at.toEpochMilliseconds() + minutes * 60000L),
                    scheduledDays = 0,
                    learningSteps = nextStep,
                    state = target,
                )
            } else {
                days(base, FsrsMath.interval(base.stability, elapsed, seed, fuzzEnabled))
            }
        }

        val result = when (old.state) {
            CardState.New, CardState.Learning, CardState.Relearning -> {
                val nextState = if (old.state == CardState.New) CardState.Learning else old.state
                step(next(rating), rating, nextState)
            }
            CardState.Review -> if (rating == Rating.Again) {
                step(next(rating), rating, CardState.Relearning).copy(lapses = old.lapses + 1)
            } else {
                val hard = next(Rating.Hard)
                val good = next(Rating.Good)
                val easy = next(Rating.Easy)
                val hardDays = min(
                    FsrsMath.interval(hard.stability, elapsed, seed, fuzzEnabled),
                    FsrsMath.interval(good.stability, elapsed, seed, fuzzEnabled),
                )
                val goodDays = max(FsrsMath.interval(good.stability, elapsed, seed, fuzzEnabled), hardDays + 1)
                val easyDays = max(FsrsMath.interval(easy.stability, elapsed, seed, fuzzEnabled), goodDays + 1)
                when (rating) {
                    Rating.Hard -> days(hard, hardDays)
                    Rating.Good -> days(good, goodDays)
                    Rating.Easy -> days(easy, easyDays)
                    Rating.Again -> error("unreachable")
                }
            }
        }
        return StoredCard(card.skillId, result)
    }

    override fun isDue(card: StoredCard, at: Instant): Boolean = card.card.due <= at

    private fun utcDay(instant: Instant): Int = floor(instant.toEpochMilliseconds() / MILLIS_PER_DAY.toDouble()).toInt()

    private companion object {
        const val MILLIS_PER_DAY = 86400000L
    }
}
