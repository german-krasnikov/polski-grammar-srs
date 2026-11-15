package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.cloze.AudioClozeExercise
import polski.cloze.AudioClozeSessionAction
import polski.cloze.AudioClozeSessionState
import polski.cloze.AudioClozeSource
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.ui.screens.AndroidAudioClozeScreen

/**
 * Verifies [AndroidAudioClozeScreen] behaviour:
 * 1. Null session triggers [AppAction.StartAudioCloze] via LaunchedEffect.
 * 2. Exercise renders sentence + options + progress indicator.
 * 3. Tapping an option dispatches [AudioClozeSessionAction.Choose] and reflects answered state.
 * 4. Navigation buttons (←, →, ↺) are present.
 * 5. Replay button enabled only after answering.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidAudioClozeScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private val exercise = AudioClozeExercise(
        id = "ex-1",
        displaySentence = "Idę do ___.",
        correctForm = "sklepu",
        options = listOf("sklepu", "sklep", "sklepie"),
        audioUrl = "file://test.mp3",
        startMs = 0L,
        endMs = 3000L,
        source = AudioClozeSource.ListeningTranscript(examRouteId = "r1", taskId = "t1"),
        gapLexemeKey = "sklep",
    )

    private fun sessionWith(answers: Map<String, String> = emptyMap()) = AudioClozeSessionState(
        exercises = listOf(exercise),
        currentIndex = 0,
        answers = answers,
        replayCount = emptyMap(),
    )

    @Test fun nullSessionDispatchesStartAudioCloze() {
        val dispatched = mutableListOf<AppAction>()
        composeRule.setContent {
            MaterialTheme {
                AndroidAudioClozeScreen(state = AppUiState(), dispatch = { dispatched += it })
            }
        }
        composeRule.waitForIdle()
        assertTrue(dispatched.any { it is AppAction.StartAudioCloze })
    }

    @Test fun exerciseRendersSentenceOptionsAndProgress() {
        composeRule.setContent {
            MaterialTheme {
                AndroidAudioClozeScreen(
                    state = AppUiState(audioCloze = sessionWith()),
                    dispatch = {},
                )
            }
        }
        composeRule.onNodeWithText("1 / 1").assertExists()
        composeRule.onNodeWithText("sklepu").assertExists()
        composeRule.onNodeWithText("sklep").assertExists()
        composeRule.onNodeWithText("sklepie").assertExists()
    }

    @Test fun tappingOptionDispatchesChooseAction() {
        val dispatched = mutableListOf<AppAction>()
        composeRule.setContent {
            var state by remember { mutableStateOf(AppUiState(audioCloze = sessionWith())) }
            MaterialTheme {
                AndroidAudioClozeScreen(
                    state = state,
                    dispatch = { action ->
                        dispatched += action
                        if (action is AppAction.AudioClozeAct &&
                            action.action is AudioClozeSessionAction.Choose
                        ) {
                            state = state.copy(
                                audioCloze = sessionWith(answers = mapOf(exercise.id to "sklepu"))
                            )
                        }
                    },
                )
            }
        }
        composeRule.onNodeWithText("sklepu").performClick()
        composeRule.waitForIdle()

        val choose = dispatched.filterIsInstance<AppAction.AudioClozeAct>()
            .firstOrNull { it.action is AudioClozeSessionAction.Choose }
        assertTrue(choose != null)
        composeRule.onNodeWithText("Правильно!").assertExists()
    }

    @Test fun navButtonsArePresent() {
        composeRule.setContent {
            MaterialTheme {
                AndroidAudioClozeScreen(
                    state = AppUiState(audioCloze = sessionWith()),
                    dispatch = {},
                )
            }
        }
        composeRule.onNodeWithText("←").assertExists()
        composeRule.onNodeWithText("→").assertExists()
        composeRule.onNodeWithText("↺").assertExists()
    }

    @Test fun replayButtonDisabledBeforeAnswer() {
        composeRule.setContent {
            MaterialTheme {
                AndroidAudioClozeScreen(
                    state = AppUiState(audioCloze = sessionWith()),
                    dispatch = {},
                )
            }
        }
        composeRule.onNodeWithText("↺ Переслушать").assertIsNotEnabled()
    }

    @Test fun replayButtonEnabledAfterAnswer() {
        composeRule.setContent {
            MaterialTheme {
                AndroidAudioClozeScreen(
                    state = AppUiState(audioCloze = sessionWith(answers = mapOf(exercise.id to "sklepu"))),
                    dispatch = {},
                )
            }
        }
        composeRule.onNodeWithText("↺ Переслушать").assertIsEnabled()
    }
}
