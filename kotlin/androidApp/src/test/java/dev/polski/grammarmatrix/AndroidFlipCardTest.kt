package dev.polski.grammarmatrix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import polski.presentation.CardEffect
import polski.srs.Rating
import polski.ui.screens.SingleRatingGate
import polski.ui.screens.cardEffectToPlay
import polski.ui.screens.ratingForDrag

/**
 * Behavior for the Android training card's rating/effect contract
 * (`Plans/Kotlin/FlipCardRivePlan.md` §0/FC-07/FC-14/FC-20), exercised as plain unit tests against
 * the pure functions in `AndroidFlipCard.kt` — no Compose test rule needed. D1 removed the flip
 * itself (see `AndroidAnswerRevealComposeTest` for the expand-reveal it replaced), so the old
 * `flipOnTap`/`isFlipTap` tests are gone with it; the swipe-rating contract below is unchanged.
 */
class AndroidFlipCardTest {
    @Test fun leftSwipePastThresholdSelectsAgain() {
        assertEquals(Rating.Again, ratingForDrag(-80f, 72f))
    }

    @Test fun rightSwipePastThresholdSelectsGood() {
        assertEquals(Rating.Good, ratingForDrag(80f, 72f))
    }

    @Test fun shortSwipeSelectsNoRating() {
        assertNull(ratingForDrag(20f, 72f))
    }

    @Test fun goodEffectIsRememberedWhenMotionIsNotReduced() {
        assertEquals(CardEffect.Remembered, cardEffectToPlay(Rating.Good, reduceMotion = false, riveDisabledForMeasurement = false))
    }

    @Test fun againEffectIsAgainWhenMotionIsNotReduced() {
        assertEquals(CardEffect.Again, cardEffectToPlay(Rating.Again, reduceMotion = false, riveDisabledForMeasurement = false))
    }

    @Test fun reducedMotionSuppressesTheEffect() {
        assertNull(cardEffectToPlay(Rating.Good, reduceMotion = true, riveDisabledForMeasurement = false))
    }

    @Test fun measurementVariantBSuppressesTheEffectIndependentlyOfMotion() {
        assertNull(cardEffectToPlay(Rating.Good, reduceMotion = false, riveDisabledForMeasurement = true))
    }

    @Test fun singleRatingGateDispatchesExactlyOnceRegardlessOfHowManyGesturesRace() {
        var dispatchCount = 0
        var lastRating: Rating? = null
        val gate = SingleRatingGate()

        val firstEffect = gate.rate(Rating.Again, reduceMotion = false, riveDisabledForMeasurement = false) {
            dispatchCount++
            lastRating = it
        }
        // A second, later gesture on the same card (e.g. a button tap right after a swipe) must not rate again.
        val secondEffect = gate.rate(Rating.Good, reduceMotion = false, riveDisabledForMeasurement = false) {
            dispatchCount++
            lastRating = it
        }

        assertEquals(1, dispatchCount)
        assertEquals(Rating.Again, lastRating)
        assertEquals(CardEffect.Again, firstEffect)
        assertNull(secondEffect)
    }

    @Test fun freshGatePerCardAllowsARatingAgain() {
        val first = SingleRatingGate()
        assertTrue(first.rate(Rating.Good, reduceMotion = false, riveDisabledForMeasurement = false) {} != null)
        val secondCard = SingleRatingGate()
        assertTrue(secondCard.rate(Rating.Good, reduceMotion = false, riveDisabledForMeasurement = false) {} != null)
    }
}
