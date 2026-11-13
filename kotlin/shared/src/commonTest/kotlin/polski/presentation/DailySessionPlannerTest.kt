package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import polski.model.Skill
import polski.progress.Progress
import polski.progress.SkillQueue
import polski.progress.SkillStats
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.srs.Scheduler
import polski.srs.SrsCard
import polski.srs.StoredCard

/**
 * M3 blueprint (`Plans/Architecture/DAILY-SESSION-BLUEPRINT.md` §Verification): pure fixtures,
 * same style as `kotlin/srs-progress/src/commonTest/kotlin/polski/progress/SkillQueueTest.kt` —
 * no [TrainingStore]/Compose runtime needed.
 */
class DailySessionPlannerTest {
    private val at = Instant.parse("2026-11-13T12:00:00Z")
    private val past = Instant.parse("2026-11-13T00:00:00Z")
    private val future = Instant.parse("2026-11-20T00:00:00Z")

    private val dueByInstant = object : Scheduler {
        override fun newCard(skillId: String, at: Instant) = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant) = SchedulePreview(at, at, at, at)
        override fun review(card: StoredCard, rating: Rating, at: Instant) = card
        override fun isDue(card: StoredCard, at: Instant) = card.card.due <= at
    }

    private fun skill(id: String, prerequisites: List<String> = emptyList()) =
        Skill(id, id, "group", "A1", "formula", "theory", "hint", prerequisites)

    private fun allActive() = SkillQueue { true }

    private fun progress(cards: List<StoredCard>, stats: Map<String, SkillStats>) =
        Progress(cards = cards, stats = stats, lastDay = "2026-11-13")

    @Test
    fun noReviewsAtAllReturnsNewStepForASkillWithEmptyPrerequisites() {
        val skills = listOf(skill("a"), skill("b", prerequisites = listOf("a")))
        val step = DailySessionPlanner.nextStep(
            progress(emptyList(), emptyMap()), dueByInstant, allActive(), skills, at, completed = emptyList(),
        )
        assertEquals(DailyStep(DailyStepKind.New, "a"), step)
    }

    @Test
    fun aReviewedDueSkillIsReviewNeverConflatedWithNew() {
        val skills = listOf(skill("a"))
        val cards = listOf(StoredCard("a", SrsCard(past)))
        val stats = mapOf("a" to SkillStats(reviews = 3, correct = 2))
        val step = DailySessionPlanner.nextStep(
            progress(cards, stats), dueByInstant, allActive(), skills, at, completed = emptyList(),
        )
        assertEquals(DailyStep(DailyStepKind.Review, "a"), step)
    }

    @Test
    fun unreviewedSkillWithUnmetPrerequisitesFallsThroughToPracticeOrNull() {
        // "b" is reviews==0 but its prerequisite "a" is also reviews==0 -> not New-eligible, and
        // there is no due card for Review either. With a reviewed-but-unrelated "c" skill present,
        // the only remaining candidate is Practice.
        val withPractice = DailySessionPlanner.nextStep(
            progress(emptyList(), mapOf("c" to SkillStats(reviews = 2, correct = 1))),
            dueByInstant, allActive(), listOf(skill("b", prerequisites = listOf("a")), skill("c")), at,
            completed = emptyList(),
        )
        assertEquals(DailyStep(DailyStepKind.Practice, "c"), withPractice)

        // Without any reviewed skill at all, every kind is empty -> null.
        val none = DailySessionPlanner.nextStep(
            progress(emptyList(), emptyMap()), dueByInstant, allActive(), listOf(skill("b", prerequisites = listOf("a"))), at,
            completed = emptyList(),
        )
        assertNull(none)
    }

    @Test
    fun capBoundaryBackfillsExactlyWhenHitNeverEarlier() {
        // 5 reviewed+due skills (more than the default reviews cap of 4), 1 new-eligible skill,
        // 1 practice-eligible skill distinct from the review pool.
        val reviewSkills = (1..5).map { skill("r$it") }
        val cards = reviewSkills.map { StoredCard(it.id, SrsCard(past)) }
        val reviewStats = reviewSkills.associate { it.id to SkillStats(reviews = 1, correct = 1) }
        val newSkill = skill("new1")
        val practiceSkill = skill("practice1")
        val practiceStats = mapOf(practiceSkill.id to SkillStats(reviews = 5, correct = 1)) // low accuracy
        val skills = reviewSkills + newSkill + practiceSkill
        val stats = reviewStats + practiceStats
        val doc = progress(cards, stats)

        // Below cap (3 of 4 reviews done): still Review, not an early backfill.
        val belowCap = DailySessionPlanner.nextStep(doc, dueByInstant, allActive(), skills, at, completed = List(3) { DailyStepKind.Review })
        assertEquals(DailyStepKind.Review, belowCap?.kind)

        // Review cap (4) hit -> backfills to New.
        val reviewCapHit = DailySessionPlanner.nextStep(doc, dueByInstant, allActive(), skills, at, completed = List(4) { DailyStepKind.Review })
        assertEquals(DailyStep(DailyStepKind.New, "new1"), reviewCapHit)

        // Review + New caps both hit -> backfills to Practice.
        val newCapHit = DailySessionPlanner.nextStep(
            doc, dueByInstant, allActive(), skills, at,
            completed = List(4) { DailyStepKind.Review } + DailyStepKind.New,
        )
        assertEquals(DailyStep(DailyStepKind.Practice, "practice1"), newCapHit)

        // All three caps hit -> null, even though candidates for every kind still exist.
        val allCapsHit = DailySessionPlanner.nextStep(
            doc, dueByInstant, allActive(), skills, at,
            completed = List(4) { DailyStepKind.Review } + DailyStepKind.New + DailyStepKind.Practice,
        )
        assertNull(allCapsHit)
    }

    @Test
    fun everyKindEmptyReturnsNullNotAnError() {
        val step = DailySessionPlanner.nextStep(
            progress(emptyList(), emptyMap()), dueByInstant, allActive(), emptyList(), at, completed = emptyList(),
        )
        assertNull(step)
    }

    @Test
    fun aFutureDueCardIsNotOfferedAsReviewButStillEligibleForPractice() {
        val skills = listOf(skill("a"))
        val cards = listOf(StoredCard("a", SrsCard(future)))
        val stats = mapOf("a" to SkillStats(reviews = 3, correct = 2))
        val step = DailySessionPlanner.nextStep(
            progress(cards, stats), dueByInstant, allActive(), skills, at, completed = emptyList(),
        )
        // Not due -> no Review; "a" is also the only skill, so New (reviews != 0) can't pick it
        // either. Practice has no due requirement (it is the lowest-accuracy-pick scaffold, not a
        // memory test), so "a" is still offered there.
        assertEquals(DailyStep(DailyStepKind.Practice, "a"), step)
    }

    @Test
    fun practicePicksLowestAccuracySkillAmongAlreadyReviewed() {
        val skills = listOf(skill("high"), skill("low"))
        val stats = mapOf(
            "high" to SkillStats(reviews = 10, correct = 9),
            "low" to SkillStats(reviews = 10, correct = 2),
        )
        val step = DailySessionPlanner.nextStep(
            progress(emptyList(), stats), dueByInstant, allActive(), skills, at, completed = emptyList(),
        )
        assertEquals(DailyStep(DailyStepKind.Practice, "low"), step)
    }

    @Test
    fun classifyDailyOutcomeMapsAgainToErroredAndCorrectnessToAssistedOrIndependent() {
        val typedWrong = AppUiState(
            answerMode = AnswerMode.Typed,
            evaluation = polski.model.Evaluation(correct = false, normalized = "x", expected = "y", distance = 1),
        )
        assertEquals(DailyOutcome.Assisted, classifyDailyOutcome(typedWrong, Rating.Good))
        val typedRight = AppUiState(
            answerMode = AnswerMode.Typed,
            evaluation = polski.model.Evaluation(correct = true, normalized = "x", expected = "x", distance = 0),
        )
        assertEquals(DailyOutcome.Independent, classifyDailyOutcome(typedRight, Rating.Good))
        assertEquals(DailyOutcome.Errored, classifyDailyOutcome(typedRight, Rating.Again))
        val oral = AppUiState(answerMode = AnswerMode.Oral)
        assertEquals(DailyOutcome.Independent, classifyDailyOutcome(oral, Rating.Good))
    }
}
