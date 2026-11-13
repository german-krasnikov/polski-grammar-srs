package polski.progress

import kotlin.time.Instant
import polski.srs.Rating
import polski.srs.Scheduler

sealed interface ReviewResult {
    data class Reviewed(val document: ProgressDocument) : ReviewResult
    data class UnknownSkill(val skillId: String) : ReviewResult
}

/** SB-8 pilot thresholds — not derived from any spec, a placeholder the same way
 *  `ScaffoldingLevel.Partial` was a placeholder before this; tune during the pilot. */
data class TilePromotionThresholds(val toPartial: Int = 3, val toNone: Int = 5)

/** Pure review transition. The caller supplies one captured instant and its local calendar day. */
class ReviewReducer(
    private val scheduler: Scheduler,
    private val knownSkillIds: Set<String>,
    private val tileThresholds: TilePromotionThresholds = TilePromotionThresholds(),
) {
    fun recordReview(
        document: ProgressDocument,
        skillId: String,
        rating: Rating,
        typedCorrect: Boolean?,
        at: Instant,
        localDay: String,
        /** SB-8: non-null only when this review's `AnswerMode` was TileBuilder. `null` (Oral/Typed)
         *  leaves the scaffolding rung/streak untouched. Appended last (not inserted next to
         *  [typedCorrect] as the blueprint's own sketch shows) so every existing positional call
         *  site ending at `localDay` keeps compiling unchanged. */
        tileCorrect: Boolean? = null,
    ): ReviewResult {
        if (skillId !in knownSkillIds || document.progress.cards.count { it.skillId == skillId } != 1) {
            return ReviewResult.UnknownSkill(skillId)
        }
        val oldStats = document.progress.stats[skillId] ?: return ReviewResult.UnknownSkill(skillId)
        val reviewed = scheduler.review(document.progress.cards.first { it.skillId == skillId }, rating, at)
        val correct = typedCorrect ?: (rating != Rating.Again)
        val (nextRung, nextTileStreak) = nextTileState(oldStats.tileScaffoldingRung, oldStats.tileScaffoldingStreak, tileCorrect)
        val nextStats = oldStats.copy(
            reviews = oldStats.reviews + 1,
            correct = oldStats.correct + if (correct) 1 else 0,
            mistakes = oldStats.mistakes + if (correct) 0 else 1,
            streak = if (correct) oldStats.streak + 1 else 0,
            tileScaffoldingRung = nextRung,
            tileScaffoldingStreak = nextTileStreak,
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

    /** `null` [tileCorrect] = this review wasn't TileBuilder-mode: rung/streak pass through
     *  untouched. Wrong at rung [RUNG_NONE] rolls back to [RUNG_PARTIAL] (design doc §5: "ошибка на
     *  None → откат к Partial"). Wrong at Full/Partial only resets the streak (stay, try again) —
     *  the design doc is silent on this case; silence read as "no demotion below where you already
     *  were" is the conservative choice. */
    private fun nextTileState(rung: Int, streak: Int, tileCorrect: Boolean?): Pair<Int, Int> {
        if (tileCorrect == null) return rung to streak
        if (!tileCorrect) return (if (rung == RUNG_NONE) RUNG_PARTIAL else rung) to 0
        val nextStreak = streak + 1
        return when (rung) {
            RUNG_FULL -> if (nextStreak >= tileThresholds.toPartial) RUNG_PARTIAL to 0 else RUNG_FULL to nextStreak
            RUNG_PARTIAL -> if (nextStreak >= tileThresholds.toNone) RUNG_NONE to 0 else RUNG_PARTIAL to nextStreak
            else -> RUNG_NONE to nextStreak // already hardest; keep counting, nothing to promote to
        }
    }

    private companion object {
        const val RUNG_FULL = 0
        const val RUNG_PARTIAL = 1
        const val RUNG_NONE = 2
    }
}
