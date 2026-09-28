package polski.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.LoadStatus
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.training.sentenceSeeds
import polski.ui.screens.TrainingScreen

class ChainCompletionProgressTest {
    @OptIn(ExperimentalTestApi::class)
    @Test fun completedNativeChainDisplaysAllFiveSteps() = runComposeUiTest {
        val chain = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "chain-progress" })
            .generateChain(sentenceSeeds.first())
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrainingScreen(
                        AppUiState(
                            loadStatus = LoadStatus.Ready,
                            tab = AppTab.Training,
                            chain = chain,
                            chainIndex = 4,
                            phase = CardPhase.ChainComplete,
                            exercise = null,
                        ),
                        {}, FocusRequester(), { "test-date" },
                    )
                }
            }
        }
        onNodeWithText("5 / 5 · Вижу → Прошлое → Отрицание → Владелец → Говорю о").assertExists()
    }
}
