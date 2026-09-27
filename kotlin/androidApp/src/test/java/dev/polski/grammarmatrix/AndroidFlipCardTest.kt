package dev.polski.grammarmatrix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import polski.presentation.CardEffect
import polski.srs.Rating
import polski.ui.screens.SingleRatingGate
import polski.ui.screens.SwipeNudgeGate
import polski.ui.screens.cardEffectToPlay
import polski.ui.screens.dragProgress
import polski.ui.screens.dragRotationDegrees
import polski.ui.screens.isFlipTap
import polski.ui.screens.ratingForDrag

/**
 * Behavior for the Android training card's rating/effect contract
 * (`Plans/Kotlin/FlipCardRivePlan.md` §0/FC-07/FC-14/FC-20), exercised as plain unit tests against
 * the pure functions in `AndroidFlipCard.kt` — no Compose test rule needed. D1 removed the training
 * card's flip (see `AndroidAnswerRevealComposeTest` for the expand-reveal it replaced); D2 (§16.0-B)
 * brings a whole-panel flip back for the vocabulary card, sharing `isFlipTap` below with its
 * revealed-face gesture detector (`AndroidVocabularyScreen.kt`).
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

    @Test fun aShortStillTapIsAFlipTap() {
        assertTrue(isFlipTap(dx = 2f, dy = 1f, tapSlopPx = 12f))
    }

    @Test fun aDragPastSlopIsNotAFlipTap() {
        assertTrue(!isFlipTap(dx = 40f, dy = 1f, tapSlopPx = 12f))
    }

    // D3 (`Plans/Kotlin/FlipCardRivePlan.md` §17.3/UX4-08..10): the whole-card drag affordance's
    // tint/label growth and tilt, ported from the web host's `--swipe-progress`/`rotate()` language.
    @Test fun dragProgressIsZeroAtRest() {
        assertEquals(0f, dragProgress(0f, thresholdPx = 72f))
    }

    @Test fun dragProgressIsHalfwayAtHalfTheThreshold() {
        assertEquals(0.5f, dragProgress(36f, thresholdPx = 72f))
    }

    @Test fun dragProgressClampsPastTheThreshold() {
        assertEquals(1f, dragProgress(200f, thresholdPx = 72f))
        assertEquals(-1f, dragProgress(-200f, thresholdPx = 72f))
    }

    @Test fun dragRotationDegreesGrowsWithDistanceThenClamps() {
        assertEquals(0f, dragRotationDegrees(0f, pxPerDegree = 22f))
        assertEquals(2f, dragRotationDegrees(44f, pxPerDegree = 22f))
        assertEquals(8f, dragRotationDegrees(1000f, pxPerDegree = 22f, maxDegrees = 8f))
        assertEquals(-8f, dragRotationDegrees(-1000f, pxPerDegree = 22f, maxDegrees = 8f))
    }

    // A4 (`Plans/Kotlin/EmphasisUXAudit-2026-09-27.md` U2): the swipe hint's nudge wiggle plays once.
    @Test fun swipeNudgeGateConsumesFirstTimeExactlyOnce() {
        val gate = SwipeNudgeGate()
        assertTrue(gate.consumeFirstTime())
        assertTrue(!gate.consumeFirstTime())
        assertTrue(!gate.consumeFirstTime())
    }

    @Test fun freshSwipeNudgeGatePerScreenAllowsANudgeAgain() {
        val first = SwipeNudgeGate()
        assertTrue(first.consumeFirstTime())
        val secondScreenVisit = SwipeNudgeGate()
        assertTrue(secondScreenVisit.consumeFirstTime())
    }
}
