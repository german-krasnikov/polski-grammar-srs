package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.data.selectCoursePack
import polski.presentation.AppUiState
import polski.ui.screens.AndroidCasesSection

/**
 * EnRuAcceptance-2026-09-28.md §7 (Android lane): with en-ru active, `nouns`
 * (`packRegistry.active.nouns`) is honestly empty — en-ru is caseless/genderless, same reasoning
 * as `MatrixWeb.kt`'s `hasCaseSystem` guard and as the sibling fix for `AndroidVerbsSection`
 * (92326f5). Opening Matrix → «Падежи и окончания» used to crash with `IllegalStateException`
 * ("Unknown noun wife") from `nounPhrase` -> `nounById` (live repro on emulator-5554, logcat at
 * `AndroidMatrixScreen.kt:173`). This pins the same calm-placeholder contract on Android: no
 * crash, a placeholder note instead of the pl declension grid.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidMatrixCasesSectionEnRuTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun casesSectionShowsCalmPlaceholderInsteadOfCrashingForEnRu() {
        try {
            selectCoursePack("en-ru")
            val state = AppUiState()
            composeRule.setContent { MaterialTheme { AndroidCasesSection(state, dispatch = {}) } }

            composeRule.onNodeWithText(
                "Для текущего курса эта таблица недоступна: в этом языке нет падежей/рода. " +
                    "Открой «Времена и лица» — таблица глаголов и do-support работает для любого курса.",
            ).assertExists()
        } finally {
            selectCoursePack("pl-ru")
        }
    }
}
