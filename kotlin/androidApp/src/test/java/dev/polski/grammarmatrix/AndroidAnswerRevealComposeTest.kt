package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
                AndroidAnswerReveal(reduceMotion = false, onRate = {}) {
                    Text("ANSWER-CONTENT")
                }
            }
        }

        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithText("ANSWER-CONTENT").assertExists()
    }

    @Test fun reducedMotionShowsTheAnswerImmediatelyWithNoAnimation() {
        composeRule.setContent {
            MaterialTheme {
                AndroidAnswerReveal(reduceMotion = true, onRate = {}) {
                    Text("ANSWER-CONTENT")
                }
            }
        }

        composeRule.onNodeWithText("ANSWER-CONTENT").assertExists()
    }
}
