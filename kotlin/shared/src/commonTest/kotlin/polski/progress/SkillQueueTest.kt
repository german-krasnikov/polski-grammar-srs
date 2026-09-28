package polski.progress

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import polski.srs.Rating
import polski.srs.Scheduler
import polski.srs.SchedulePreview
import polski.srs.SrsCard
import polski.srs.StoredCard

/**
 * EN-10 (Plans/Kotlin/EnRuPackPlan.md §6): a synthetic `${target}:${skillId}` id (the namespace
 * any pack after pl-ru brings for its own skill ids, UniversalCorePlan.md §6) sitting alongside a
 * bare pl-ru id in the same stored list must never throw inside [SkillQueue] and must never leak
 * into the active pack's due/next results -- whichever id shape [SkillQueue]'s activePackFilter
 * itself recognizes as its own pack.
 */
class SkillQueueTest {
    private val at = Instant.parse("2026-03-29T01:30:00Z")
    private val future = Instant.parse("2026-04-01T00:00:00Z")

    private val alwaysDue = object : Scheduler {
        override fun newCard(skillId: String, at: Instant) = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant) = SchedulePreview(at, at, at, at)
        override fun review(card: StoredCard, rating: Rating, at: Instant) = card
        override fun isDue(card: StoredCard, at: Instant) = true
    }

    private fun card(skillId: String, dueAt: Instant) = StoredCard(skillId, SrsCard(dueAt))

    @Test
    fun aForeignNamespacedIdIsFilteredOutWithoutThrowingWhilePlRuStaysUnaffected() {
        val plRuIds = setOf("case.acc.f", "case.acc.n")
        val queue = SkillQueue { it in plRuIds }
        val cards = listOf(card("case.acc.f", at), card("en:role.object", at))

        assertEquals(listOf("case.acc.f"), queue.due(cards, alwaysDue, at).map { it.skillId })
        assertEquals("case.acc.f", queue.next(cards, alwaysDue, at)?.skillId)
    }

    @Test
    fun aNamespacedIdIsAcceptedWhenItsOwnPackIsActiveWithNoSpecialCasingByIdShape() {
        val enRuIds = setOf("en:role.object", "en:verb.presentSimple")
        val queue = SkillQueue { it in enRuIds }
        val cards = listOf(card("en:role.object", at), card("case.acc.f", at))

        assertEquals(listOf("en:role.object"), queue.due(cards, alwaysDue, at).map { it.skillId })
    }

    @Test
    fun nextDueAtNeverThrowsOnAForeignNamespacedId() {
        val plRuIds = setOf("case.acc.f")
        val queue = SkillQueue { it in plRuIds }
        val cards = listOf(card("case.acc.f", future), card("en:role.object", future))

        assertEquals(future, queue.nextDueAt(cards, at))
    }
}
