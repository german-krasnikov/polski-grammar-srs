package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.srs.Rating

class CardEffectTest {
    @Test fun goodMapsToRemembered() {
        assertEquals(CardEffect.Remembered, cardEffectFor(Rating.Good))
    }

    @Test fun againMapsToAgain() {
        assertEquals(CardEffect.Again, cardEffectFor(Rating.Again))
    }
}
