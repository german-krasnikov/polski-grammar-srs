package dev.polski.grammarmatrix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.srs.Rating
import polski.ui.screens.AndroidRatingDragSurface

/**
 * D3 (`Plans/Kotlin/FlipCardRivePlan.md` §17.3/UX4-08..15): the whole-card drag-to-rate surface
 * shared by the training answer panel and the vocabulary back face, replacing the old
 * commit-or-nothing `detectSwipeRating`/`detectFlipOrSwipe`. No visible rating buttons on Android —
 * these drive the real Compose gesture/animation clock through Robolectric to prove a completed
 * drag past the threshold rates exactly once, a short tap (when [AndroidRatingDragSurface] is given
 * one) does not rate, and the two ratings are always reachable as TalkBack custom actions.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidRatingDragSurfaceTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun swipeLeftPastThresholdRatesAgainExactlyOnce() {
        val ratings = mutableListOf<Rating>()
        composeRule.setContent {
            MaterialTheme {
                Box(Modifier.testTag("card").fillMaxWidth()) {
                    AndroidRatingDragSurface(itemKey = "x", reduceMotion = true, onRate = { ratings += it }) {
                        Text("CONTENT")
                    }
                }
            }
        }

        composeRule.onNodeWithTag("card").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        assertEquals(listOf(Rating.Again), ratings)
    }

    @Test fun swipeRightPastThresholdRatesGoodExactlyOnce() {
        val ratings = mutableListOf<Rating>()
        composeRule.setContent {
            MaterialTheme {
                Box(Modifier.testTag("card").fillMaxWidth()) {
                    AndroidRatingDragSurface(itemKey = "x", reduceMotion = true, onRate = { ratings += it }) {
                        Text("CONTENT")
                    }
                }
            }
        }

        composeRule.onNodeWithTag("card").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        assertEquals(listOf(Rating.Good), ratings)
    }

    @Test fun aShortTapFiresOnTapNotARating() {
        val ratings = mutableListOf<Rating>()
        var tapped = false
        composeRule.setContent {
            MaterialTheme {
                Box(Modifier.testTag("card").fillMaxWidth()) {
                    AndroidRatingDragSurface(itemKey = "x", reduceMotion = true, onRate = { ratings += it }, onTap = { tapped = true }) {
                        Text("CONTENT")
                    }
                }
            }
        }

        composeRule.onNodeWithTag("card").performTouchInput { click() }
        composeRule.waitForIdle()

        assertTrue(tapped)
        assertTrue(ratings.isEmpty())
    }

    @Test fun bothRatingsAreAlwaysExposedAsAccessibilityCustomActions() {
        val ratings = mutableListOf<Rating>()
        composeRule.setContent {
            MaterialTheme {
                Box(Modifier.testTag("card").fillMaxWidth()) {
                    AndroidRatingDragSurface(itemKey = "x", reduceMotion = true, onRate = { ratings += it }) {
                        Text("CONTENT")
                    }
                }
            }
        }

        val actions = composeRule.onNodeWithContentDescription("Оценка карточки").fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals(listOf("Повторить", "Вспомнил"), actions.map { it.label })

        actions.first { it.label == "Вспомнил" }.action()
        composeRule.waitForIdle()

        assertEquals(listOf(Rating.Good), ratings)
    }
}
