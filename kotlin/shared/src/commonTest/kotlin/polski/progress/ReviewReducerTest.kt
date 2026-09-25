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
