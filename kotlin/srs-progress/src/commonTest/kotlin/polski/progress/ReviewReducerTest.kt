package polski.progress

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.srs.Scheduler
import polski.srs.StoredCard
import polski.srs.SrsCard

class ReviewReducerTest {
    private val at = Instant.parse("2026-03-29T01:30:00Z")

    @Test
    fun typedCorrectnessIsIndependentOfRatingAndDayRollsOnce() {
        val scheduler = CountingScheduler()
        val original = ProgressDocument(
            progress = Progress(
                cards = listOf(StoredCard("known", SrsCard(at))),
                stats = mapOf("known" to SkillStats(reviews = 2, correct = 1, streak = 1, mistakes = 1)),
                reviewsToday = 3,
                lastDay = "2026-03-28",
                totalReviews = 2,
            ),
        )
        val reducer = ReviewReducer(scheduler, setOf("known"))

        val result = assertIs<ReviewResult.Reviewed>(
            reducer.recordReview(original, "known", Rating.Good, typedCorrect = false, at, "2026-03-29"),
        ).document.progress

        assertEquals(1, scheduler.reviewCalls)
        assertEquals(1, result.reviewsToday)
        assertEquals(3, result.totalReviews)
        assertEquals(SkillStats(3, 1, 0, 2), result.stats.getValue("known"))
        assertEquals(3, original.progress.reviewsToday)
        assertEquals(2, original.progress.stats.getValue("known").reviews)
    }

    @Test
    fun unknownSkillDoesNotCallScheduler() {
        val scheduler = CountingScheduler()
        val document = ProgressDocument(Progress(cards = emptyList(), stats = emptyMap(), lastDay = "2026-03-29"))
        assertIs<ReviewResult.UnknownSkill>(
            ReviewReducer(scheduler, setOf("known")).recordReview(document, "missing", Rating.Good, null, at, "2026-03-29"),
        )
        assertEquals(0, scheduler.reviewCalls)
    }

    @Test
    fun ownerRt05TypedCorrectWinsOverAgainWithoutChangingOtherHistory() {
        val now = Instant.parse("2026-02-03T12:00:00Z")
        val scheduler = CountingScheduler()
        val original = ProgressDocument(Progress(
            cards = listOf(StoredCard("a", SrsCard(now)), StoredCard("b", SrsCard(now).copy(reps = 3))),
            stats = mapOf("a" to SkillStats(4, 3, 2, 1), "b" to SkillStats(1, 1, 1, 0)),
            reviewsToday = 5, lastDay = "2026-02-03", totalReviews = 20,
        ))
        val result = assertIs<ReviewResult.Reviewed>(ReviewReducer(scheduler, setOf("a", "b"))
            .recordReview(original, "a", Rating.Again, true, now, "2026-02-03")).document.progress
        assertEquals(SkillStats(5, 4, 3, 1), result.stats.getValue("a"))
        assertEquals(SkillStats(1, 1, 1, 0), result.stats.getValue("b"))
        assertEquals(6, result.reviewsToday)
        assertEquals("2026-02-03", result.lastDay)
        assertEquals(21, result.totalReviews)
        assertEquals(mapOf("a" to 1, "b" to 3), result.cards.associate { it.skillId to it.card.reps })
        assertEquals(1, scheduler.reviewCalls)
        assertEquals(original.progress.stats.getValue("a"), SkillStats(4, 3, 2, 1))
    }

    // SB-8: a null tileCorrect (Oral/Typed review) never moves the scaffolding rung/streak.
    @Test
    fun nonTileReviewLeavesScaffoldingRungAndStreakUntouched() {
        val scheduler = CountingScheduler()
        val document = ProgressDocument(Progress(
            cards = listOf(StoredCard("known", SrsCard(at))),
            stats = mapOf("known" to SkillStats(reviews = 1, correct = 1, streak = 1, mistakes = 0, tileScaffoldingRung = 1, tileScaffoldingStreak = 2)),
            lastDay = "2026-03-29",
        ))
        val result = assertIs<ReviewResult.Reviewed>(
            ReviewReducer(scheduler, setOf("known")).recordReview(document, "known", Rating.Good, typedCorrect = true, at, "2026-03-29"),
        ).document.progress
        val stats = result.stats.getValue("known")
        assertEquals(1, stats.tileScaffoldingRung)
        assertEquals(2, stats.tileScaffoldingStreak)
    }

    // SB-8: correct TileBuilder reviews promote Full -> Partial at exactly toPartial, and
    // Partial -> None at exactly toNone, resetting the streak on each promotion.
    @Test
    fun correctTileReviewsPromoteAtExactThresholds() {
        val scheduler = CountingScheduler()
        var document = ProgressDocument(Progress(
            cards = listOf(StoredCard("known", SrsCard(at))),
            stats = mapOf("known" to SkillStats()),
            lastDay = "2026-03-29",
        ))
        val reducer = ReviewReducer(scheduler, setOf("known"), TilePromotionThresholds(toPartial = 3, toNone = 5))

        fun reviewTile(correct: Boolean) {
            document = (reducer.recordReview(document, "known", Rating.Good, null, at, "2026-03-29", tileCorrect = correct) as ReviewResult.Reviewed).document
        }

        reviewTile(true); reviewTile(true)
        assertEquals(0, document.progress.stats.getValue("known").tileScaffoldingRung) // still Full, streak 2 < 3
        assertEquals(2, document.progress.stats.getValue("known").tileScaffoldingStreak)
        reviewTile(true) // 3rd correct -> promotes to Partial, streak resets
        assertEquals(1, document.progress.stats.getValue("known").tileScaffoldingRung)
        assertEquals(0, document.progress.stats.getValue("known").tileScaffoldingStreak)

        reviewTile(true); reviewTile(true); reviewTile(true); reviewTile(true)
        assertEquals(1, document.progress.stats.getValue("known").tileScaffoldingRung) // streak 4 < 5
        reviewTile(true) // 5th correct at Partial -> promotes to None
        assertEquals(2, document.progress.stats.getValue("known").tileScaffoldingRung)
        assertEquals(0, document.progress.stats.getValue("known").tileScaffoldingStreak)
    }

    // SB-8: a wrong TileBuilder review at rung None rolls back to Partial; at Full/Partial it
    // only resets the streak (no demotion below where the learner already was).
    @Test
    fun wrongTileReviewDemotesOnlyFromNoneAndOtherwiseResetsStreak() {
        val scheduler = CountingScheduler()
        val reducer = ReviewReducer(scheduler, setOf("known"))

        val atFull = ProgressDocument(Progress(
            cards = listOf(StoredCard("known", SrsCard(at))),
            stats = mapOf("known" to SkillStats(tileScaffoldingRung = 0, tileScaffoldingStreak = 2)),
            lastDay = "2026-03-29",
        ))
        val resultFull = (reducer.recordReview(atFull, "known", Rating.Again, null, at, "2026-03-29", tileCorrect = false) as ReviewResult.Reviewed).document
        assertEquals(0, resultFull.progress.stats.getValue("known").tileScaffoldingRung)
        assertEquals(0, resultFull.progress.stats.getValue("known").tileScaffoldingStreak)

        val atNone = ProgressDocument(Progress(
            cards = listOf(StoredCard("known", SrsCard(at))),
            stats = mapOf("known" to SkillStats(tileScaffoldingRung = 2, tileScaffoldingStreak = 3)),
            lastDay = "2026-03-29",
        ))
        val resultNone = (reducer.recordReview(atNone, "known", Rating.Again, null, at, "2026-03-29", tileCorrect = false) as ReviewResult.Reviewed).document
        assertEquals(1, resultNone.progress.stats.getValue("known").tileScaffoldingRung)
        assertEquals(0, resultNone.progress.stats.getValue("known").tileScaffoldingStreak)
    }

    private class CountingScheduler : Scheduler {
        var reviewCalls = 0
        override fun newCard(skillId: String, at: Instant) = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant) = SchedulePreview(at, at, at, at)
        override fun review(card: StoredCard, rating: Rating, at: Instant): StoredCard {
            reviewCalls++
            return card.copy(card = card.card.copy(reps = card.card.reps + 1))
        }
        override fun isDue(card: StoredCard, at: Instant) = false
    }
}
