package dev.polski.grammarmatrix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import polski.presentation.CardEffect
import polski.presentation.CardPhase
import polski.srs.Rating
import polski.ui.screens.SingleRatingGate
import polski.ui.screens.cardEffectToPlay
import polski.ui.screens.flipOnTap
import polski.ui.screens.isFlipTap
import polski.ui.screens.ratingForDrag

/**
 * Behavior for the Android flash card's flip/rating/effect contract
 * (`Plans/Kotlin/FlipCardRivePlan.md` §0/FC-07/FC-10/FC-14/FC-20), exercised as plain unit tests
 * against the pure functions in `AndroidFlipCard.kt` — no Compose test rule needed.
 */
class AndroidFlipCardTest {
    @Test fun tapDoesNotFlipBeforeReveal() {
        assertEquals(false, flipOnTap(CardPhase.Question, false))
    }

    @Test fun tapFlipsToBackOnceRevealed() {
        assertEquals(true, flipOnTap(CardPhase.Revealed, false))
    }

    @Test fun secondTapFlipsBackToFront() {
        assertEquals(false, flipOnTap(CardPhase.Revealed, true))
    }

    @Test fun leftSwipePastThresholdSelectsAgain() {
        assertEquals(Rating.Again, ratingForDrag(-80f, 72f))
    }

    @Test fun rightSwipePastThresholdSelectsGood() {
        assertEquals(Rating.Good, ratingForDrag(80f, 72f))
    }

    @Test fun shortSwipeSelectsNoRating() {
        assertNull(ratingForDrag(20f, 72f))
    }

    @Test fun negligibleMovementIsAFlipTap() {
        assertTrue(isFlipTap(dx = 3f, dy = 2f, tapSlopPx = 12f))
    }

    @Test fun aShortButNoticeableSwipeIsNeitherATapNorARating() {
        // FC-07: a short/vertical swipe must not rate, and — since it clearly wasn't a tap either —
        // must not surprise the user by flipping the card.
        assertFalse(isFlipTap(dx = 40f, dy = 2f, tapSlopPx = 12f))
        assertNull(ratingForDrag(40f, thresholdPx = 72f))
    }

    @Test fun verticalScrollAttemptIsNotAFlipTap() {
        assertFalse(isFlipTap(dx = 2f, dy = 40f, tapSlopPx = 12f))
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
