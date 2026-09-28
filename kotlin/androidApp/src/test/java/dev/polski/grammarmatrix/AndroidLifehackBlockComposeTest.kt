package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.ui.screens.AndroidLifehackBlock

/**
 * EN-21 android (`Plans/Kotlin/EnRuPackPlan.md` §4.3, §6): the lifehack block renders after a
 * style's Back blocks (wired in `AndroidTrainingScreen.kt`), one collapsible entry per authored
 * [polski.presentation.Lifehack], collapsed by default, absent entirely when the skill has none —
 * the same "empty -> no frame at all" contract `AndroidLifehackBlock`'s KDoc documents and
 * `LifehackWeb.kt`'s Playwright coverage already proves for the web host.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidLifehackBlockComposeTest {
    @get:Rule val composeRule = createComposeRule()

    private val caption = "Лайфхак · источник: editorial"
    private val citationStart = "Swan, O. A. (2002)."

    @Test fun composesNothingForASkillWithNoAuthoredLifehack() {
        composeRule.setContent {
            MaterialTheme { AndroidLifehackBlock(skillId = "no.such.skill.exists", reduceMotion = true) }
        }
        composeRule.onNodeWithText(caption).assertDoesNotExist()
    }

    @Test fun showsTheAttributionCaptionCollapsedByDefaultForAnAuthoredSkill() {
        composeRule.setContent {
            MaterialTheme { AndroidLifehackBlock(skillId = "case.gen.neg", reduceMotion = true) }
        }
        composeRule.onAllNodesWithText(caption).onFirst().assertExists()
        composeRule.onNodeWithText(citationStart, substring = true).assertDoesNotExist()
    }

    @Test fun tappingTheCaptionRevealsTheTextAndCitation() {
        composeRule.setContent {
            MaterialTheme { AndroidLifehackBlock(skillId = "case.gen.neg", reduceMotion = true) }
        }
        composeRule.onAllNodesWithText(caption).onFirst().performClick()
        composeRule.onNodeWithText(citationStart, substring = true).assertExists()
    }
}
