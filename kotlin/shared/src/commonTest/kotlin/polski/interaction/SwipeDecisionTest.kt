package polski.interaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import polski.srs.Rating

class SwipeDecisionTest {
    @Test
    fun thresholdsAreInclusiveAndDirectionFollowsDx() {
        for (threshold in listOf(72.0, 75.0, 80.0, 70.0)) {
            assertNull(SwipeDecision.rating(threshold - 0.01, 0.0, threshold, 1.25, true))
            assertNull(SwipeDecision.rating(-threshold + 0.01, 0.0, threshold, 1.25, true))
            assertEquals(Rating.Good, SwipeDecision.rating(threshold, 0.0, threshold, 1.25, true))
            assertEquals(Rating.Again, SwipeDecision.rating(-threshold, 0.0, threshold, 1.25, true))
        }
    }

    @Test
    fun releaseAxisIsStrictExceptForAndroidAfterCapture() {
        assertNull(SwipeDecision.rating(75.0, 60.0, 75.0, 1.25, true)) // axis tie
        assertEquals(Rating.Good, SwipeDecision.rating(75.0, 59.0, 75.0, 1.25, true))
        assertNull(SwipeDecision.rating(-75.0, -60.0, 75.0, 1.25, true))
        assertEquals(Rating.Again, SwipeDecision.rating(-75.0, -59.0, 75.0, 1.25, true))
        assertEquals(Rating.Good, SwipeDecision.rating(72.0, 200.0, 72.0, 1.0, false))
        assertEquals(Rating.Again, SwipeDecision.rating(-72.0, -200.0, 72.0, 1.0, false))
    }
}
