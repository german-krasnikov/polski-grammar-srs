package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.data.skills
import polski.grammar.caseRows
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.ui.screens.AndroidCaseReference

/**
 * EmphasisUXAudit E8 / ContrastHighlightPlan.md §5 ("Запрет утечки ответа"), mirroring the web
 * fix in 804f6c6: "Таблица под рукой"/"Таблица этого предложения" is reference material for the
 * whole case system, so every OTHER row may show its form freely — but the row matching this
 * exercise's own target case is the answer, and before reveal that row's "Стало" form must never
 * reach the screen, for every skill (not just the ones whose exercises happen to be case drills).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidCaseReferenceLeakTest {
    @get:Rule val composeRule = createComposeRule()

    private val factory = PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "leak-fixture" })

    @Test fun noSkillLeaksItsTargetCaseAnswerBeforeReveal() {
        var state by mutableStateOf(AppUiState(exercise = factory.generateForSkill(skills.first().id), phase = CardPhase.Question))
        composeRule.setContent { MaterialTheme { AndroidCaseReference(state, dispatch = {}) } }

        skills.forEach { skill ->
            val exercise = factory.generateForSkill(skill.id)
            val target = caseRows.firstOrNull { it.id.id in exercise.tags }

            // Before reveal: the target row is masked. (Checking for the placeholder, not the
            // absence of the exact answer text, matches kotlin-reference-panel-leak.spec.ts —
            // Polish's own syncretism, e.g. masculine personal Gen==Acc, otherwise means an
            // unrelated OTHER row legitimately shows the identical inflected form as its own,
            // different, answer — the same "Было" nominative baseline every row displays too.)
            state = state.copy(exercise = exercise, phase = CardPhase.Question)
            composeRule.waitForIdle()
            if (target != null) composeRule.onNodeWithText("Стало: ?", useUnmergedTree = true).assertExists()

            // After reveal: the placeholder is gone — the target row now shows its real answer.
            state = state.copy(phase = CardPhase.Revealed)
            composeRule.waitForIdle()
            if (target != null) composeRule.onNodeWithText("Стало: ?", useUnmergedTree = true).assertDoesNotExist()
        }
    }
}
