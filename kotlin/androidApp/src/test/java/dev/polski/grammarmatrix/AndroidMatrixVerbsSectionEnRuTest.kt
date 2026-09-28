package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.data.enVerbs
import polski.data.selectCoursePack
import polski.presentation.AppUiState
import polski.ui.screens.AndroidVerbsSection

/**
 * EnRuAcceptance-2026-09-28.md §7 (Android lane): with en-ru active, `verbs`
 * (`packRegistry.active.verbs`) is honestly empty — en-ru is aspect-less, same reasoning as
 * `MatrixWeb.kt`'s `renderVerbs` guard (`verbs.isEmpty() -> renderNoCaseSystemNotice`). Opening
 * Matrix → «Времена и лица» used to crash with `NoSuchElementException` from
 * `verbs.first { it.id == selected.verbId }` (live repro on emulator-5554, logcat at
 * `AndroidMatrixScreen.kt`). This pins the same calm-placeholder contract on Android: no crash,
 * a placeholder note instead of the pl conjugation grid, and EN-24's own pack-independent English
 * verb matrix still renders regardless (it reads `lang/en/forms.generated.json` directly, not
 * `verbs`).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidMatrixVerbsSectionEnRuTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun verbsSectionShowsCalmPlaceholderInsteadOfCrashingForEnRu() {
        try {
            selectCoursePack("en-ru")
            val state = AppUiState()
            composeRule.setContent { MaterialTheme { AndroidVerbsSection(state, dispatch = {}) } }

            composeRule.onNodeWithText(
                "Для текущего курса эта таблица недоступна: в этом языке нет падежей/рода. " +
                    "Открой «Времена и лица» — таблица глаголов и do-support работает для любого курса.",
            ).assertExists()

            val exampleLemma = enVerbs.first { it.id == "see" }.lemma
            composeRule.onNodeWithText("English: лицо × время (\"$exampleLemma\")").assertExists()
        } finally {
            selectCoursePack("pl-ru")
        }
    }
}
