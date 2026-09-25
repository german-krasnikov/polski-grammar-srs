package polski.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import polski.presentation.ExplanationMethod
import polski.ui.screens.DesktopSettingsScreen

class DesktopSettingsScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun settingsExposeWorkingChoicesAndUnavailableReminders() = runComposeUiTest {
        val directory = Files.createTempDirectory("polski-settings-ui-")
        try {
            val controller = DesktopPreferencesController(DesktopPreferencesRepository(directory, { null }))
            var selected: ExplanationMethod? = null
            setContent {
                MaterialTheme {
                    DesktopSettingsScreen(controller, MacSystemStatus.Available(MacSystemAppearance(false, false, false)),
                        onClose = {}, onMethod = { selected = it },
                        onAnswerMode = {}, onAppearance = {}, onMotion = {},
                        onProgressImport = {}, onProgressExport = {},
                        onVocabularyImport = {}, onVocabularyExport = {},
                        onPreferencesImport = {}, onPreferencesExport = {})
                }
            }
            onAllNodesWithText("Польский ↔ русский · активный курс").assertCountEquals(1)
            onAllNodesWithText("Недоступно на Mac в этой сборке").assertCountEquals(1)
            onNodeWithText("Живые ситуации").performClick()
            assertEquals(ExplanationMethod.Situations, selected)
        } finally { directory.toFile().deleteRecursively() }
    }
}
