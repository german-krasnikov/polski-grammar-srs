package polski.srs

import kotlin.time.Instant

enum class Rating(val wire: Int) { Again(1), Hard(2), Good(3), Easy(4) }

data class SchedulePreview(val again: Instant, val hard: Instant, val good: Instant, val easy: Instant) {
    operator fun get(rating: Rating): Instant = when (rating) {
        Rating.Again -> again
        Rating.Hard -> hard
        Rating.Good -> good
        Rating.Easy -> easy
    }
}

/** Pure scheduler. Callers capture [at] once and reuse it for preview and review. */
interface Scheduler {
    fun newCard(skillId: String, at: Instant): StoredCard
    fun preview(card: StoredCard, at: Instant): SchedulePreview
    fun review(card: StoredCard, rating: Rating, at: Instant): StoredCard
    fun isDue(card: StoredCard, at: Instant): Boolean
}
