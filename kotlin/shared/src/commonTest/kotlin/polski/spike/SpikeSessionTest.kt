package polski.spike

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpikeSessionTest {
    @Test
    fun answerIsHiddenUntilRevealAndRatingRequiresReveal() {
        val initial = SpikeSession()
        assertFalse(initial.revealed)
        assertEquals(null, initial.rate(SpikeRating.GOOD).rating)

        val answered = initial.type("Mówię po polsku").reveal().rate(SpikeRating.GOOD)
        assertEquals("Mówię po polsku", answered.input)
        assertTrue(answered.revealed)
        assertEquals(SpikeRating.GOOD, answered.rating)
    }

    @Test
    fun previewStateRoundTripsPolishUnicodeAndRejectsInvalidInput() {
        val session = SpikeSession("Mówię: żółć\n", revealed = true, rating = SpikeRating.EASY)
        assertEquals(session, SpikeSession.fromWire(session.toWire()))
        assertEquals(null, SpikeSession.fromWire("4:abc:1:EASY"))
        assertEquals(null, SpikeSession.fromWire("0::0:EASY"))
    }
}
