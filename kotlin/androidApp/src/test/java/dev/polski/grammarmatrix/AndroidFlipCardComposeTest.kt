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
import polski.presentation.CardPhase
import polski.ui.screens.AndroidFlipCard

/**
 * FC2-04's one real gap (`Plans/Kotlin/FlipCardRivePlan.md` §12.1/R1): `AndroidFlipCard` already
 * swaps faces exactly at the 90° midpoint of the rotation (`angle >= 90f` gates which slot is
 * composed, not just which is visible) — this had no regression test because `androidApp`/
 * `composeApp` didn't pull in a Compose UI test runner. Driving `MainTestClock` through the actual
 * 500ms tween and sampling well below/above the midpoint proves it directly against the real
 * composable, not a restated assumption.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidFlipCardComposeTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun backFaceIsNotComposedBefore90DegreesAndIsAfter() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MaterialTheme {
                AndroidFlipCard(
                    exerciseId = "ex-1",
                    phase = CardPhase.Revealed, // triggers the auto-flip-to-back on reveal
                    reduceMotion = false,
                    onRate = {},
                    front = { Text("FRONT-FACE") },
                    back = { Text("BACK-FACE") },
                )
            }
        }

        // 20% into the 500ms tween: even with FastOutSlowIn easing this is nowhere near the 90°
        // midpoint — the front face must still be the only one composed.
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithText("FRONT-FACE").assertExists()
        composeRule.onNodeWithText("BACK-FACE").assertDoesNotExist()

        // 80% into the tween: well past the midpoint either way — the back face must now be the
        // only one composed, and the front face must already be gone (R1: no early/no late swap).
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.onNodeWithText("BACK-FACE").assertExists()
        composeRule.onNodeWithText("FRONT-FACE").assertDoesNotExist()

        composeRule.mainClock.advanceTimeBy(200) // let the animation settle before teardown
    }
}
