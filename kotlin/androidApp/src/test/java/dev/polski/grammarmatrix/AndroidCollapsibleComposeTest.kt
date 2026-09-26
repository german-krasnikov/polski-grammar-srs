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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.ui.screens.AndroidCollapsible

/**
 * D4 (`Plans/Kotlin/Lane-android.md`): panel show/hide (the training screen's "Таблица под
 * рукой" reference table and the skill picker) animates height/opacity instead of popping, and
 * stays out of the accessibility tree while hidden — the same real-clock proof style as
 * `AndroidAnswerRevealComposeTest` uses for D1's expand-reveal. The toggle flips via a real
 * `performClick` (like `AndroidFlipCardGestureTimingTest`) so the state mutation goes through
 * Compose's own snapshot/recomposition path.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidCollapsibleComposeTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun contentAppearsOnceTheExpandAnimationSettles() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var visible by remember { mutableStateOf(false) }
            MaterialTheme {
                Button(onClick = { visible = true }) { Text("SHOW") }
                AndroidCollapsible(visible = visible, reduceMotion = false) { Text("PANEL-CONTENT") }
            }
        }
        composeRule.onNodeWithText("PANEL-CONTENT").assertDoesNotExist()

        composeRule.onNodeWithText("SHOW").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithText("PANEL-CONTENT").assertExists()
    }

    @Test fun contentLeavesTheTreeOnceTheCollapseAnimationSettles() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var visible by remember { mutableStateOf(true) }
            MaterialTheme {
                Button(onClick = { visible = false }) { Text("HIDE") }
                AndroidCollapsible(visible = visible, reduceMotion = false) { Text("PANEL-CONTENT") }
            }
        }
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithText("PANEL-CONTENT").assertExists()

        composeRule.onNodeWithText("HIDE").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.onNodeWithText("PANEL-CONTENT").assertDoesNotExist()
    }

    @Test fun reducedMotionShowsContentImmediatelyWithoutWaitingForAnimation() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var visible by remember { mutableStateOf(false) }
            MaterialTheme {
                Button(onClick = { visible = true }) { Text("SHOW") }
                AndroidCollapsible(visible = visible, reduceMotion = true) { Text("PANEL-CONTENT") }
            }
        }
        composeRule.onNodeWithText("SHOW").performClick()
        composeRule.mainClock.advanceTimeBy(32) // a frame or two, nowhere near the ~220ms animated path
        composeRule.onNodeWithText("PANEL-CONTENT").assertExists()
    }
}
