package polski.srs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant
import polski.progress.DecodeResult
import polski.progress.ProgressCodec

class SrsCardWireTest {
    private val empty = SerializedCard("2026-02-03T12:00:00.000Z", 0.0, 0.0, 0, 0, 0, 0, 0, 0)

    @Test fun newCardAndMillisecondsRoundTrip() {
        val at = Instant.parse(empty.due)
        val new = FsrsScheduler().newCard("case.acc.f", at)
        assertEquals("case.acc.f", new.skillId)
        assertEquals(empty, SrsCardWire.encode(new.card))
        assertEquals(new.card, assertIs<CardDecodeResult.Valid>(SrsCardWire.decode(empty)).card)
        assertEquals(false, FsrsScheduler().isDue(new, Instant.fromEpochMilliseconds(at.toEpochMilliseconds() - 1)))
        assertEquals(true, FsrsScheduler().isDue(new, at))
        assertEquals(true, FsrsScheduler().isDue(new, Instant.fromEpochMilliseconds(at.toEpochMilliseconds() + 1)))
    }

    @Test fun offsetIsCanonicalizedToUtcMilliseconds() {
        val offset = empty.copy(due = "2026-02-03T13:00:00+01:00", lastReview = "2026-02-03T13:00:00.123+01:00")
        val parsed = assertIs<CardDecodeResult.Valid>(SrsCardWire.decode(offset)).card
        assertEquals("2026-02-03T12:00:00.000Z", SrsCardWire.encode(parsed).due)
        assertEquals("2026-02-03T12:00:00.123Z", SrsCardWire.encode(parsed).lastReview)
    }

    @Test fun malformedFieldsReturnInvalid() {
        assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(due = "bad")))
        assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(state = 99)))
        assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(stability = Double.NaN)))
        assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(difficulty = Double.POSITIVE_INFINITY)))
        assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(reps = -1)))
        assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(lastReview = "invalid")))
        assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(state = CardState.Review.wire, stability = 0.0, difficulty = 0.5)))
    }

    @Test fun nonNewCardsWithoutMemoryAreInvalid() {
        for (state in listOf(CardState.Learning, CardState.Review, CardState.Relearning)) {
            assertIs<CardDecodeResult.Invalid>(SrsCardWire.decode(empty.copy(state = state.wire)))
        }
    }

    @Test fun malformedReviewMemoryIsRejectedByProgressImport() {
        val raw = """{"version":1,"cards":[{"skillId":"case.acc.f","card":{"due":"2026-02-03T12:00:00.000Z","stability":0,"difficulty":0,"elapsed_days":0,"scheduled_days":0,"reps":1,"lapses":0,"learning_steps":0,"state":2,"last_review":"2026-02-02T12:00:00.000Z"}}],"stats":{"case.acc.f":{"reviews":1,"correct":1,"streak":1,"mistakes":0}},"reviewsToday":1,"lastDay":"2026-02-03","totalReviews":1}"""
        assertIs<DecodeResult.Invalid>(ProgressCodec.decode(raw))
    }
}
