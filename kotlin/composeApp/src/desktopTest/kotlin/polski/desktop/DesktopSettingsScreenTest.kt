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
            // EN-22 / EnRuAcceptance-2026-09-28.md §7 item 1: target/native pickers built from
            // `availableCoursePacks`, never a hardcoded "pl"/"en" case list — en-ru now parses
            // completely, so a second, real "Английский" target row is listed alongside "Польский"
            // (both packs' native is "ru", so that row stays a single button).
            onAllNodesWithText("Польский").assertCountEquals(1)
            onAllNodesWithText("Английский").assertCountEquals(1)
            onAllNodesWithText("Русский").assertCountEquals(1)
            onNodeWithText("Польский").performClick()
            assertEquals("pl", target)
            onNodeWithText("Английский").performClick()
            assertEquals("en", target)
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
