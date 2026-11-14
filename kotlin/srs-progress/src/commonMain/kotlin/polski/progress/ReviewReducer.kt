package polski.progress

import kotlin.time.Instant
import polski.progress.policy.Policy
import polski.progress.policy.data.TILE_SCAFFOLDING_OUTCOME_ROW_ROWS
import polski.progress.policy.data.TileScaffoldingOutcomeRowFinalStreak
import polski.progress.policy.data.TileScaffoldingOutcomeRowIndependentSinceAction
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
        /** M5: the `seedKey` format (`polski.core.engine.seedKey`/`polski.model.seedKey`) of the
         *  lexical seed this review drilled. `null` (every pre-M5 call site) leaves [SkillStats.seedErrors]
         *  untouched. Appended last, same "positional call sites keep compiling" reasoning as
         *  [tileCorrect]. Only ever bumps the count on a wrong answer — never reset on a later
         *  correct one, since the point is "which forms have ever been hard." */
        seedKey: String? = null,
    ): ReviewResult {
        if (skillId !in knownSkillIds || document.progress.cards.count { it.skillId == skillId } != 1) {
            return ReviewResult.UnknownSkill(skillId)
        }
        val oldStats = document.progress.stats[skillId] ?: return ReviewResult.UnknownSkill(skillId)
        val reviewed = scheduler.review(document.progress.cards.first { it.skillId == skillId }, rating, at)
        val correct = typedCorrect ?: (rating != Rating.Again)
        val nextRungState = nextTileRungState(oldStats.tileScaffoldingRung, oldStats.tileScaffoldingStreak, tileCorrect, oldStats.independentSince, localDay)
        val nextStats = oldStats.copy(
            reviews = oldStats.reviews + 1,
            correct = oldStats.correct + if (correct) 1 else 0,
            mistakes = oldStats.mistakes + if (correct) 0 else 1,
            streak = if (correct) oldStats.streak + 1 else 0,
            tileScaffoldingRung = nextRungState.rung,
            tileScaffoldingStreak = nextRungState.streak,
            independentSince = nextRungState.independentSince,
            seedErrors = if (!correct && seedKey != null) oldStats.seedErrors + (seedKey to (oldStats.seedErrors[seedKey] ?: 0) + 1) else oldStats.seedErrors,
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

    /** Rung/streak/independentSince after one TileBuilder review. `null` [tileCorrect] = this
     *  review wasn't TileBuilder-mode: everything passes through untouched. Otherwise the ladder
     *  itself (promotion/demotion thresholds, M6's "independent since" stickiness) lives in
     *  `dsl/learning/tile-scaffolding.logic` + `tile-scaffolding-outcomes.core-data` — this
     *  function only maps the `Int` rung constants to/from the DSL's enum tags and applies the
     *  outcome row the Logic decision picks. */
    private fun nextTileRungState(rung: Int, streak: Int, tileCorrect: Boolean?, oldIndependentSince: String?, localDay: String): TileRungState {
        if (tileCorrect == null) return TileRungState(rung, streak, oldIndependentSince)
        val nextStreak = if (tileCorrect) streak + 1 else 0
        val disposition = Policy.disposition(Policy.DispositionInput(
            correct = tileCorrect, rung = rungTag(rung), nextStreak = nextStreak.toDouble(),
            toPartial = tileThresholds.toPartial.toDouble(), toNone = tileThresholds.toNone.toDouble(),
        ))
        val outcome = TILE_SCAFFOLDING_OUTCOME_ROW_ROWS.single { it.disposition == disposition }
        val newRung = rungOf(outcome.newRung.tag)
        val finalStreak = if (outcome.finalStreak == TileScaffoldingOutcomeRowFinalStreak.ZERO) 0 else nextStreak
        val independentSince = when (outcome.independentSinceAction) {
            TileScaffoldingOutcomeRowIndependentSinceAction.CLEAR -> null
            TileScaffoldingOutcomeRowIndependentSinceAction.KEEP -> oldIndependentSince
            TileScaffoldingOutcomeRowIndependentSinceAction.STAMP_FRESH -> localDay
        }
        return TileRungState(newRung, finalStreak, independentSince)
    }

    private data class TileRungState(val rung: Int, val streak: Int, val independentSince: String?)

    private fun rungTag(rung: Int): String = when (rung) {
        RUNG_FULL -> "full"
        RUNG_PARTIAL -> "partial"
        else -> "none"
    }

    private fun rungOf(tag: String): Int = when (tag) {
        "full" -> RUNG_FULL
        "partial" -> RUNG_PARTIAL
        else -> RUNG_NONE
    }

    private companion object {
        const val RUNG_FULL = 0
        const val RUNG_PARTIAL = 1
        const val RUNG_NONE = 2
    }
}
