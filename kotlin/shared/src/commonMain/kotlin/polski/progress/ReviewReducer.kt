package polski.progress

import kotlin.time.Instant
import polski.srs.Rating
import polski.srs.Scheduler

sealed interface ReviewResult {
    data class Reviewed(val document: ProgressDocument) : ReviewResult
    data class UnknownSkill(val skillId: String) : ReviewResult
}

/** Pure review transition. The caller supplies one captured instant and its local calendar day. */
class ReviewReducer(private val scheduler: Scheduler, private val knownSkillIds: Set<String>) {
    fun recordReview(
        document: ProgressDocument,
        skillId: String,
        rating: Rating,
        typedCorrect: Boolean?,
        at: Instant,
        localDay: String,
    ): ReviewResult {
        if (skillId !in knownSkillIds || document.progress.cards.count { it.skillId == skillId } != 1) {
            return ReviewResult.UnknownSkill(skillId)
        }
        val oldStats = document.progress.stats[skillId] ?: return ReviewResult.UnknownSkill(skillId)
        val reviewed = scheduler.review(document.progress.cards.first { it.skillId == skillId }, rating, at)
        val correct = typedCorrect ?: (rating != Rating.Again)
        val nextStats = oldStats.copy(
            reviews = oldStats.reviews + 1,
            correct = oldStats.correct + if (correct) 1 else 0,
            mistakes = oldStats.mistakes + if (correct) 0 else 1,
            streak = if (correct) oldStats.streak + 1 else 0,
        )
        val next = document.progress.copy(
            cards = document.progress.cards.map { if (it.skillId == skillId) reviewed else it },
            stats = document.progress.stats + (skillId to nextStats),
            reviewsToday = (if (document.progress.lastDay == localDay) document.progress.reviewsToday else 0) + 1,
            lastDay = localDay,
            totalReviews = document.progress.totalReviews + 1,
        )
        return ReviewResult.Reviewed(document.copy(progress = next))
    }
}
