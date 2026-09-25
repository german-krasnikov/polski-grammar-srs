package polski.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.runBlocking
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.LoadStatus
import polski.srs.FsrsScheduler
import polski.ui.screens.MatrixScreen
import polski.ui.screens.VocabularyScreen
import polski.vocabulary.VocabularyRepository
import polski.vocabulary.VocabularySession

class DesktopInventoryContentAcceptanceTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun desktopMatrixAndVocabularyRenderAuthoredHostCopy() = runComposeUiTest {
        setContent {
            MaterialTheme {
                MatrixScreen(AppUiState(loadStatus = LoadStatus.Ready, tab = AppTab.Matrix)) {}
            }
        }
        onNodeWithText("Один небольшой словарь. Видно, что меняется при каждой операции.").assertExists()

        val session = VocabularySession(InMemoryRepository(), FsrsScheduler(),
            { Instant.parse("2026-09-24T12:00:00Z") }, { "user.00000000-0000-4000-8000-000000000001" })
        runBlocking { session.start() }
        setContent { MaterialTheme { VocabularyScreen(session, {}, {}, {}) } }
        onNodeWithText("Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
            substring = true).assertExists()
    }

    private class InMemoryRepository : VocabularyRepository {
        override suspend fun loadRaw(): String? = null
        override suspend fun saveRaw(value: String, backupCurrent: Boolean) = Unit
    }
}
