package polski.srs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SchedulerParityTest {
    @Test fun newAgainMatchesInstalledOracle() {
        val at = Instant.parse("2026-02-03T12:00:00.000Z")
        val scheduler: Scheduler = FsrsScheduler()
        val original = scheduler.newCard("case.acc.f", at)
        val result = scheduler.review(original, Rating.Again, at)
        assertEquals(Instant.parse("2026-02-03T12:01:00.000Z"), result.card.due)
        assertEquals(0.212, result.card.stability)
        assertEquals(CardState.Learning, result.card.state)
        assertEquals(CardState.New, original.card.state)
    }
}
