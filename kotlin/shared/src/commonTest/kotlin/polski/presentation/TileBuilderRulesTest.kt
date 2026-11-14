package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import polski.core.model.FeatureKey
import polski.core.model.TileSegment
import polski.core.model.TileSelection

class TileBuilderRulesTest {
    private val choice1 = TileSegment.Choice(position = 1, correctForm = "piękną", lexeme = "l1", targetFeature = FeatureKey("Case"), alternatives = emptyList())
    private val choice2 = TileSegment.Choice(position = 3, correctForm = "żonę", lexeme = "l2", targetFeature = FeatureKey("Case"), alternatives = emptyList())
    private val segments = listOf(TileSegment.Locked(position = 0, text = "Widzę"), choice1, choice2)

    @Test fun canCheckIsFalseWhenNoChoiceIsPlaced() {
        assertFalse(tileBuilderCanCheck(segments, emptyList()))
    }

    @Test fun canCheckIsFalseWhenOnlySomeChoicesArePlaced() {
        assertFalse(tileBuilderCanCheck(segments, listOf(TileSelection(1, "piękną"))))
    }

    @Test fun canCheckIsTrueWhenEveryChoiceIsPlaced() {
        assertTrue(tileBuilderCanCheck(segments, listOf(TileSelection(1, "piękną"), TileSelection(3, "żonę"))))
    }

    @Test fun canCheckIgnoresLockedSegments() {
        assertTrue(tileBuilderCanCheck(listOf(TileSegment.Locked(position = 0, text = "x")), emptyList()))
    }

    @Test fun togglePanelOpensWhenNothingIsOpen() {
        assertEquals(2, togglePanel<Int>(null, 2))
    }

    @Test fun togglePanelClosesTheSameTappedPanel() {
        assertNull(togglePanel(2, 2))
    }

    @Test fun togglePanelSwitchesToTheNewlyTappedPanel() {
        assertEquals(5, togglePanel(2, 5))
    }
}
