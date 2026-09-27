package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.ui.screens.AndroidAnswerReveal

/**
 * D1 (`Plans/Kotlin/FlipCardRivePlan.md` §12/§18, `Plans/Kotlin/Lane-android.md`): the training
 * card no longer flips — `AndroidAnswerReveal` expands its content downward below the question
 * instead. This replaces the old flip-midpoint compose test (there is no more 90-degree face swap
 * to prove): it drives the same real Compose animation machinery through Robolectric's main clock
 * to prove the answer content actually reaches the screen, both animated and under reduced motion.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidAnswerRevealComposeTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun answerContentIsShownOnceTheExpandAnimationSettles() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MaterialTheme {
                AndroidAnswerReveal(itemKey = "x", reduceMotion = false, onRate = {}) {
                    Text("ANSWER-CONTENT")
                }
            }
        }

        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithText("ANSWER-CONTENT").assertExists()
    }

    /**
     * Reviewer correction (`Plans/Kotlin/Lane-android.md`): `expandVertically` defaults to
     * `expandFrom = Alignment.Bottom` when the argument is omitted. Measured against the real
     * Compose layout/clip pipeline (Robolectric, not just asserting final existence), that default
     * shifts the content *up* by `(targetHeight - currentHeight)` and clips it to the growing box,
     * so on the way to full height the box's visible strip shows whatever sits at the *bottom* of
     * the content first — here that is `ANSWER-CONTENT` (the second item), while `TOP-MARKER` (the
     * heading, meant to unfold first per D1) stays fully clipped (reported height `0`) until the
     * box is almost fully grown. `expandFrom = Alignment.Top` fixes this: the content's top stays
     * pinned and height is revealed top-down, so the heading appears no later than the tail.
     */
    @Test fun answerContentRevealsTopDownNotBottomUp() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MaterialTheme {
                AndroidAnswerReveal(itemKey = "x", reduceMotion = false, onRate = {}) {
                    Text("TOP-MARKER")
                    Text("ANSWER-CONTENT")
                }
            }
        }

        var markerVisibleAtStep = -1
        var answerVisibleAtStep = -1
        for (step in 1..40) {
            composeRule.mainClock.advanceTimeBy(8)
            val markerHeight = composeRule.onNodeWithText("TOP-MARKER").fetchSemanticsNode().boundsInRoot.height
            val answerHeight = composeRule.onNodeWithText("ANSWER-CONTENT").fetchSemanticsNode().boundsInRoot.height
            if (markerVisibleAtStep < 0 && markerHeight > 1f) markerVisibleAtStep = step
            if (answerVisibleAtStep < 0 && answerHeight > 1f) answerVisibleAtStep = step
            if (markerVisibleAtStep >= 0 && answerVisibleAtStep >= 0) break
        }

        assertTrue(
            "TOP-MARKER (the heading) must become visible no later than ANSWER-CONTENT (the tail) " +
                "as the card expands downward, but marker appeared at step $markerVisibleAtStep " +
                "and answer at step $answerVisibleAtStep",
            markerVisibleAtStep in 1..answerVisibleAtStep,
        )
    }

    @Test fun reducedMotionShowsTheAnswerImmediatelyWithNoAnimation() {
        composeRule.setContent {
            MaterialTheme {
                AndroidAnswerReveal(itemKey = "x", reduceMotion = true, onRate = {}) {
                    Text("ANSWER-CONTENT")
                }
            }
        }

        composeRule.onNodeWithText("ANSWER-CONTENT").assertExists()
    }
}
