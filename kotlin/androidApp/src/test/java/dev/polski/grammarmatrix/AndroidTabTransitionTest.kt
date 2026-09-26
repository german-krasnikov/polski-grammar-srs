package dev.polski.grammarmatrix

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.presentation.AppTab
import polski.ui.screens.AndroidTabContent
import polski.ui.screens.tabSlideDirection

/**
 * D4 (`Plans/Kotlin/Lane-android.md`): phone-like paging between the bottom-nav tabs. The pure
 * direction rule is exercised without Compose; the Compose tests below drive the real
 * `AnimatedContent` through Robolectric's main clock (state flips via a real `performClick`, like
 * `AndroidFlipCardGestureTimingTest`, so the mutation goes through Compose's own snapshot/
 * recomposition path rather than being poked from off the composition) to prove [AndroidTabContent]
 * actually settles on the tab it was asked to show — not the ambient one closed over by a naive
 * `content` lambda (see its own doc comment for why that distinction matters).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidTabTransitionTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun slidesForwardWhenMovingRightInNavOrder() {
        assertEquals(1, tabSlideDirection(AppTab.Training, AppTab.Matrix))
        assertEquals(1, tabSlideDirection(AppTab.Matrix, AppTab.Progress))
        assertEquals(1, tabSlideDirection(AppTab.Progress, AppTab.Vocabulary))
    }

    @Test fun slidesBackwardWhenMovingLeftInNavOrder() {
        assertEquals(-1, tabSlideDirection(AppTab.Vocabulary, AppTab.Progress))
        assertEquals(-1, tabSlideDirection(AppTab.Matrix, AppTab.Training))
    }

    @Test fun sameTabHasNoDirectionPreference() {
        assertEquals(1, tabSlideDirection(AppTab.Training, AppTab.Training))
    }

    @Test fun settlesOnTheTargetTabAfterTheSlideAnimation() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var tab by remember { mutableStateOf(AppTab.Training) }
            MaterialTheme {
                Button(onClick = { tab = AppTab.Matrix }) { Text("GO-MATRIX") }
                AndroidTabContent(tab = tab, reduceMotion = false) { branchTab ->
                    Text(if (branchTab == AppTab.Training) "TRAINING-CONTENT" else "MATRIX-CONTENT")
                }
            }
        }
        composeRule.onNodeWithText("TRAINING-CONTENT").assertExists()

        composeRule.onNodeWithText("GO-MATRIX").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithText("MATRIX-CONTENT").assertExists()
    }

    @Test fun eachBranchRendersItsOwnTabNotTheLatestAmbientOne() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var tab by remember { mutableStateOf(AppTab.Training) }
            MaterialTheme {
                Button(onClick = { tab = AppTab.Matrix }) { Text("GO-MATRIX") }
                AndroidTabContent(tab = tab, reduceMotion = false) { branchTab ->
                    Text("CONTENT-FOR-${branchTab.name}")
                }
            }
        }
        composeRule.onNodeWithText("GO-MATRIX").performClick()
        // Mid-animation both the outgoing (Training) and incoming (Matrix) branches are on
        // screen; the outgoing one must still read Training, not have flipped to Matrix just
        // because the ambient target state already changed.
        composeRule.mainClock.advanceTimeBy(50)
        composeRule.onNodeWithText("CONTENT-FOR-Training").assertExists()
        composeRule.onNodeWithText("CONTENT-FOR-Matrix").assertExists()
    }
}
