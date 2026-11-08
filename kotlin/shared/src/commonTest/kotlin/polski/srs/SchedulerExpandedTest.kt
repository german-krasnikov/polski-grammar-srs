package polski.srs

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.srs.data.oracle.SCHEDULER_EXPANDED_CASE_ROW_ROWS
import polski.srs.data.oracle.SchedulerExpandedCaseRow

/** Pinned ts-fsrs 5.4.2 oracle; expected values live in the scheduler DSL rows. */
class SchedulerExpandedTest {
    private fun rating(wire: Int, id: String): Rating {
        require(wire in 1..Rating.entries.size) { "$id invalid rating: $wire" }
        return Rating.entries[wire - 1]
    }

    private fun state(wire: Int, id: String): CardState {
        require(wire in CardState.entries.indices) { "$id invalid card state: $wire" }
        return CardState.entries[wire]
    }

    private fun source(row: SchedulerExpandedCaseRow): StoredCard = StoredCard(
        "oracle",
        SrsCard(
            Instant.parse(row.srcDue),
            row.srcStability.toDouble(),
            row.srcDifficulty.toDouble(),
            row.srcElapsed,
            row.srcScheduled,
            row.srcReps,
            row.srcLapses,
            row.srcSteps,
            state(row.srcState, row.id),
            row.srcLastReview?.let(Instant::parse),
        ),
    )

    private fun expected(row: SchedulerExpandedCaseRow): SrsCard = SrsCard(
        Instant.parse(row.expDue),
        row.expStability.toDouble(),
        row.expDifficulty.toDouble(),
        row.expElapsed,
        row.expScheduled,
        row.expReps,
        row.expLapses,
        row.expSteps,
        state(row.expState, row.id),
        Instant.parse(row.expLastReview),
    )

    private fun check(expected: SrsCard, actual: SrsCard, id: String) {
        assertEquals(expected.due, actual.due, "$id due")
        assertTrue(abs(expected.stability - actual.stability) <= 1e-7, "$id stability: ${actual.stability}")
        assertTrue(abs(expected.difficulty - actual.difficulty) <= 1e-7, "$id difficulty: ${actual.difficulty}")
        assertEquals(expected.elapsedDays, actual.elapsedDays, "$id elapsed")
        assertEquals(expected.scheduledDays, actual.scheduledDays, "$id scheduled")
        assertEquals(expected.reps, actual.reps, "$id reps")
        assertEquals(expected.lapses, actual.lapses, "$id lapses")
        assertEquals(expected.learningSteps, actual.learningSteps, "$id steps")
        assertEquals(expected.state, actual.state, "$id state")
        assertEquals(expected.lastReview, actual.lastReview, "$id last review")
    }

    @Test fun allCases() {
        val rows = SCHEDULER_EXPANDED_CASE_ROW_ROWS
        assertEquals(70, rows.size, "scheduler oracle case count")
        assertEquals(70, rows.map { it.id }.toSet().size, "scheduler oracle unique IDs")
        for (row in rows) {
            val at = Instant.parse(row.at)
            val source = source(row)
            val before = source.copy(card = source.card.copy())
            val scheduler = FsrsScheduler(row.fuzzEnabled)
            val preview = scheduler.preview(source, at)
            assertEquals(Instant.parse(row.previewAgain), preview.again, "${row.id} preview again")
            assertEquals(Instant.parse(row.previewHard), preview.hard, "${row.id} preview hard")
            assertEquals(Instant.parse(row.previewGood), preview.good, "${row.id} preview good")
            assertEquals(Instant.parse(row.previewEasy), preview.easy, "${row.id} preview easy")
            val reviewed = scheduler.review(source, rating(row.rating, row.id), at)
            check(expected(row), reviewed.card, row.id)
            assertEquals(before, source, "${row.id} source unchanged")
            assertEquals(preview[rating(row.rating, row.id)], reviewed.card.due, "${row.id} reviewed due matches preview")
        }
    }
}
