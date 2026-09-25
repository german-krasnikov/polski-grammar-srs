package polski.srs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class SeedAndFuzzTest {
    @Test fun jsNumberSeedFormatting() {
        assertEquals("0", jsNumberString(-0.0))
        assertEquals("1.3596195999999998", jsNumberString(0.212 * 6.4133))
        assertEquals("0.000001", jsNumberString(1e-6))
        assertEquals("1e-7", jsNumberString(1e-7))
        assertEquals("100000000000000000000", jsNumberString(1e20))
        assertEquals("1e+21", jsNumberString(1e21))
    }

    @Test fun previewAndRepeatDoNotConsumeSharedRandomState() {
        val scheduler = FsrsScheduler()
        val at = Instant.parse("2030-01-01T00:00:00.000Z")
        val card = StoredCard("seed", SrsCard(Instant.parse("2029-12-01T00:00:00.000Z"), 60.0, 5.5, 10, 30, 3, 0, 0, CardState.Review, Instant.parse("2029-12-01T00:00:00.000Z")))
        val first = scheduler.review(card, Rating.Good, at)
        scheduler.preview(card.copy(skillId = "other"), at)
        val second = scheduler.review(card, Rating.Good, at)
        assertEquals(first, second)
        assertEquals(first.card.due, scheduler.preview(card, at).good)
        assertTrue(first.card.scheduledDays > 0)
    }
}
