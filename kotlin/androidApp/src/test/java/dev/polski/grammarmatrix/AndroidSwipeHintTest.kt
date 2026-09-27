package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.ui.screens.AndroidSwipeHint
import polski.ui.screens.SwipeNudgeGate

/**
 * A4 (`Plans/Kotlin/EmphasisUXAudit-2026-09-27.md` U2, ADR-8): phones stay swipe-only, no rating
 * buttons — this closes the discoverability gap with a persistent arrow-marked hint on the revealed
 * card plus a single brief nudge wiggle the first time a fresh [SwipeNudgeGate] is shown, gated on
 * the "Анимации" (`reduceMotion`) toggle.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidSwipeHintTest {
    @get:Rule val composeRule = createComposeRule()

    private fun labelLeft() = composeRule.onNodeWithText("Повторить", substring = true).fetchSemanticsNode().boundsInRoot.left

    @Test fun hintShowsBothArrowsAndLabels() {
        composeRule.setContent {
            MaterialTheme { AndroidSwipeHint(nudgeGate = SwipeNudgeGate(), reduceMotion = true) }
        }
        composeRule.onNodeWithText("←", substring = true).assertExists()
        composeRule.onNodeWithText("→", substring = true).assertExists()
        composeRule.onNodeWithText("Повторить", substring = true).assertExists()
        composeRule.onNodeWithText("Вспомнил", substring = true).assertExists()
    }

    @Test fun freshGateNudgesOnceWithABriefWiggle() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MaterialTheme { AndroidSwipeHint(nudgeGate = SwipeNudgeGate(), reduceMotion = false) }
        }
        val restLeft = labelLeft()
        var moved = false
        for (step in 1..40) {
            composeRule.mainClock.advanceTimeBy(16)
            if (labelLeft() != restLeft) moved = true
        }
        assertTrue("a fresh gate must wiggle the hint at least once", moved)
    }

    @Test fun alreadyConsumedGateNeverWiggles() {
        val gate = SwipeNudgeGate()
        gate.consumeFirstTime()
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MaterialTheme { AndroidSwipeHint(nudgeGate = gate, reduceMotion = false) }
        }
        val restLeft = labelLeft()
        for (step in 1..40) {
            composeRule.mainClock.advanceTimeBy(16)
            assertEquals(restLeft, labelLeft())
        }
    }

    @Test fun reducedMotionNeverWigglesEvenWithAFreshGate() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MaterialTheme { AndroidSwipeHint(nudgeGate = SwipeNudgeGate(), reduceMotion = true) }
        }
        val restLeft = labelLeft()
        for (step in 1..40) {
            composeRule.mainClock.advanceTimeBy(16)
            assertEquals(restLeft, labelLeft())
        }
    }
}
