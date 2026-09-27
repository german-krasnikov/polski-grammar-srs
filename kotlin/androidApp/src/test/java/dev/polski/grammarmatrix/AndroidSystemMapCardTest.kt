package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.data.ReferenceSystemCard
import polski.ui.screens.AndroidSystemMapCard

/**
 * E6 (EmphasisUXAudit-2026-09-27.md, C2 steps): the map-overview card used to flatten
 * `card.example` into one prose string ("żona → żonę → żony" glued together with the title and
 * explanation) with no per-step markup. [AndroidSystemMapCard] instead renders each of
 * [ReferenceSystemCard.steps] as its own text node with a visible arrow between them, and exposes
 * the whole chain as one spoken unit for screen readers (same pattern as `ContrastPairText`).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidSystemMapCardTest {
    @get:Rule val composeRule = createComposeRule()

    private val card = ReferenceSystemCard(
        id = "noun", title = "Существительное", explanation = "падеж меняет окончание",
        example = "żona → żonę → żony", steps = listOf("żona", "żonę", "żony"),
    )

    // Each step is its own node in the unmerged tree — the visual structure this fixes (E6). The
    // merged (a11y/TalkBack) tree collapses them into one spoken unit; see [exposesChainAsOneSpokenUnit].
    @Test fun rendersEachStepAsItsOwnNode() {
        composeRule.setContent { MaterialTheme { AndroidSystemMapCard(card) } }
        composeRule.onNodeWithText("żona", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("żonę", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("żony", useUnmergedTree = true).assertExists()
        // Not one flattened "title: explanation · example" string (the old E6 shape).
        composeRule.onNodeWithText("${card.title}: ${card.explanation} · ${card.example}", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun exposesChainAsOneSpokenUnit() {
        composeRule.setContent { MaterialTheme { AndroidSystemMapCard(card) } }
        composeRule.onNodeWithContentDescription("żona → żonę → żony").assertExists()
    }
}
