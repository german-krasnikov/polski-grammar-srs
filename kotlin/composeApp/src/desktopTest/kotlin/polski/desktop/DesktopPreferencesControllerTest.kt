package polski.desktop

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import polski.preferences.Appearance
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredMethod
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.CardPhase
import polski.presentation.ExplanationMethod

class DesktopPreferencesControllerTest {
    @Test
    fun manualAppearancePersistsAcrossControllerRestart() {
        val directory = Files.createTempDirectory("polski-controller-")
        try {
            val first = DesktopPreferencesController(DesktopPreferencesRepository(directory, { null }))
            first.setAppearance(Appearance.Light)
            assertIs<DesktopPreferencesStatus.Loaded>(first.status)

            val reopened = DesktopPreferencesController(DesktopPreferencesRepository(directory, { null }))
            assertEquals(Appearance.Light, reopened.value.appearance)
            reopened.setAppearance(Appearance.Dark)
            val restarted = DesktopPreferencesController(DesktopPreferencesRepository(directory, { null }))
            assertEquals(Appearance.Dark, restarted.value.appearance)
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun oneWriterUpdatesCardMethodAndDefersAnswerModeUntilNextQuestion() {
        val directory = Files.createTempDirectory("polski-controller-")
        try {
            val controller = DesktopPreferencesController(DesktopPreferencesRepository(directory, { null }))
            val actions = mutableListOf<AppAction>()
            controller.setMethod(ExplanationMethod.Situations, actions::add)
            controller.setAnswerMode(AnswerMode.Typed, CardPhase.Revealed, actions::add)
            assertEquals(listOf<AppAction>(AppAction.SetExplanationMethod(ExplanationMethod.Situations)), actions)
            assertEquals(PreferredMethod.Situations, controller.value.explanationMethod)
            assertEquals(PreferredAnswerMode.Typed, controller.value.answerMode)
            controller.applyPendingAnswerMode(CardPhase.Question, AnswerMode.Oral, actions::add)
            assertEquals(AppAction.SetAnswerMode(AnswerMode.Typed), actions.last())
            assertIs<DesktopPreferencesStatus.Loaded>(controller.status)
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun failedWriteLeavesExportableChoiceAndHonestStatus() {
        val directory = Files.createTempDirectory("polski-controller-")
        try {
            val controller = DesktopPreferencesController(DesktopPreferencesRepository(directory, { null }) { _, _ -> })
            controller.setMethod(ExplanationMethod.Situations) { }
            assertIs<DesktopPreferencesStatus.WriteFailed>(controller.status)
            assertEquals(PreferredMethod.Situations, controller.value.explanationMethod)
            assertEquals(PreferredMethod.Situations,
                (polski.preferences.UserPreferencesCodec.decode(controller.exportRaw()) as polski.preferences.PreferencesDecode.Loaded).value.explanationMethod)
        } finally { directory.toFile().deleteRecursively() }
    }
}
