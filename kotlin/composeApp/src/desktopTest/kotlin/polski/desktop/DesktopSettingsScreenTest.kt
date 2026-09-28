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
import polski.presentation.StyleId
import polski.ui.screens.DesktopSettingsScreen

class DesktopSettingsScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun settingsExposeWorkingChoicesAndUnavailableReminders() = runComposeUiTest {
        val directory = Files.createTempDirectory("polski-settings-ui-")
        try {
            val controller = DesktopPreferencesController(DesktopPreferencesRepository(directory, { null }))
            var selected: StyleId? = null
            var target: String? = null
            var native: String? = null
            setContent {
                MaterialTheme {
                    DesktopSettingsScreen(controller, MacSystemStatus.Available(MacSystemAppearance(false, false, false)),
                        onClose = {}, onTarget = { target = it }, onNative = { native = it }, onStyle = { selected = it },
                        onAnswerMode = {}, onAppearance = {}, onMotion = {},
                        onProgressImport = {}, onProgressExport = {},
                        onVocabularyImport = {}, onVocabularyExport = {},
                        onPreferencesImport = {}, onPreferencesExport = {})
                }
            }
            // EN-22: target/native pickers next to the style picker, built from
            // `availableCoursePacks` — only pl-ru is registered today, so one real, already-selected
            // "Польский"/"Русский" row each (see `polski.data.packRegistry`'s own KDoc for why).
            onAllNodesWithText("Польский").assertCountEquals(1)
            onAllNodesWithText("Русский").assertCountEquals(1)
            onNodeWithText("Польский").performClick()
            assertEquals("pl", target)
            onNodeWithText("Русский").performClick()
            assertEquals("ru", native)
            onAllNodesWithText("Недоступно на Mac в этой сборке").assertCountEquals(1)
            onNodeWithText("Через сравнение с родным").performClick()
            assertEquals(StyleId.NativeContrast, selected)
            onNodeWithText("Минимум теории").performClick()
            assertEquals(StyleId.MinimalTheory, selected)
        } finally { directory.toFile().deleteRecursively() }
    }
}
