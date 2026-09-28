package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.ui.screens.AndroidLifehacksSection

/**
 * Item (2) of the Matrix "Лайфхаки" sub-section task: every lifehack of the active pack, grouped
 * by skill in curriculum order with real skill titles ([polski.presentation.LifehackProvider.listAll]),
 * one collapsible group per skill, collapsed by default — mirrors
 * [AndroidLifehackBlockComposeTest]'s collapsed-by-default/attribution contract for the per-skill
 * card block, one level up (group, not single tip).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidLifehacksSectionComposeTest {
    @get:Rule val composeRule = createComposeRule()

    // "case.gen.neg" (pl-ru, `courses/pl-ru/course.json`) is skillById-titled "Dopełniacz ·
    // negacja" — same fixture AndroidLifehackBlockComposeTest already relies on for a stable,
    // always-authored skill.
    private val skillTitle = "Dopełniacz · negacja"
    private val caption = "Лайфхак · источник: editorial"
    private val citationStart = "Swan, O. A. (2002)."

    @Test fun listsEveryAuthoredSkillGroupCollapsedByDefault() {
        composeRule.setContent {
            MaterialTheme { AndroidLifehacksSection(reduceMotion = true) }
        }
        composeRule.onAllNodesWithText(skillTitle).onFirst().assertExists()
        composeRule.onNodeWithText(citationStart, substring = true).assertDoesNotExist()
    }

    @Test fun expandingAGroupShowsItsLifehacksWithSourceAndStatus() {
        composeRule.setContent {
            MaterialTheme { AndroidLifehacksSection(reduceMotion = true) }
        }
        composeRule.onAllNodesWithText(skillTitle).onFirst().performClick()
        composeRule.onAllNodesWithText(caption).onFirst().assertExists()
        composeRule.onAllNodesWithText(caption).onFirst().performClick()
        composeRule.onNodeWithText(citationStart, substring = true).assertExists()
    }
}
