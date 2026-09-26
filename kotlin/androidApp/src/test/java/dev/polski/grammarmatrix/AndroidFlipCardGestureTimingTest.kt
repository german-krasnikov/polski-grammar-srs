package dev.polski.grammarmatrix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.srs.Rating
import polski.ui.screens.AndroidFlipCard

/**
 * Reviewer correction (`Plans/Kotlin/Lane-android.md`, commit 61539db): the vocabulary card's
 * revealed-face gesture must not attach the instant `revealed` turns true — `front()` (with its
 * own always-on tap-to-reveal control) keeps rendering for the ~250ms `tween(500)` takes to cross
 * 90°, so gating on `revealed` let a swipe thrown right after the reveal tap dispatch a rating
 * before the answer was ever shown, and let a double-tap in that window race `front`'s own reveal
 * against the ancestor's `toggleFlip`, reversing the in-flight animation. `AndroidFlipCard` now
 * gates on `showingBack` instead; these drive the real Compose animation clock through Robolectric
 * (not just the pure `isFlipTap`/`ratingForDrag` functions in `AndroidFlipCardTest`) to prove it.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidFlipCardGestureTimingTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun swipeDuringTheFlipAnimationDoesNotDispatchARatingBeforeTheAnswerIsShown() {
        val ratings = mutableListOf<Rating>()
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var revealed by remember { mutableStateOf(false) }
            MaterialTheme {
                Column {
                    Button(onClick = { revealed = true }) { Text("REVEAL") }
                    Box(Modifier.testTag("card").fillMaxWidth()) {
                        AndroidFlipCard(
                            itemId = "x",
                            revealed = revealed,
                            reduceMotion = false,
                            onRate = { ratings += it },
                            front = { Text("FRONT", Modifier.fillMaxWidth()) },
                            back = { Text("BACK") },
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("REVEAL").performClick()
        composeRule.mainClock.advanceTimeBy(100) // well under the ~250ms needed to cross 90°

        composeRule.onNodeWithTag("card").performTouchInput { swipeLeft() }
        composeRule.mainClock.advanceTimeBy(1_000) // let the flip (and any wrongly-dispatched rating) settle

        assertEquals("a swipe thrown mid-flip must never dispatch a rating", emptyList<Rating>(), ratings)
        composeRule.onNodeWithText("BACK").assertExists()
    }

    @Test fun tapDuringTheFlipAnimationDoesNotReverseIt() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var revealed by remember { mutableStateOf(false) }
            MaterialTheme {
                Column {
                    Button(onClick = { revealed = true }) { Text("REVEAL") }
                    Box(Modifier.testTag("card").fillMaxWidth()) {
                        AndroidFlipCard(
                            itemId = "x",
                            revealed = revealed,
                            reduceMotion = false,
                            // no gesture surface is composed at all before `showingBack` (the branch the reviewer flagged)
                            onRate = {},
                            front = { Text("FRONT", Modifier.fillMaxWidth()) },
                            back = { Text("BACK") },
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("REVEAL").performClick()
        composeRule.mainClock.advanceTimeBy(100) // well under the ~250ms needed to cross 90°

        composeRule.onNodeWithTag("card").performClick() // a tap landing on the card mid-flip
        composeRule.mainClock.advanceTimeBy(1_000) // let the flip settle

        composeRule.onNodeWithText("BACK").assertExists()
        composeRule.onNodeWithText("FRONT").assertDoesNotExist()
    }
}
