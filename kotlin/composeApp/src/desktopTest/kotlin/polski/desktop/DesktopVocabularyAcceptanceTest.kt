package polski.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlinx.coroutines.runBlocking
import polski.srs.FsrsScheduler
import polski.data.VocabularyItem
import polski.data.activeCoursePackId
import polski.data.selectCoursePack
import polski.ui.screens.VocabularyScreen
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyDocument
import polski.vocabulary.VocabularyRepository
import polski.vocabulary.VocabularySession
import kotlin.time.Instant

class DesktopVocabularyAcceptanceTest {
    @Test
    fun oversizedWriteCannotReplaceVocabularyOrGrammarFiles() = runBlocking {
        val directory = Files.createTempDirectory("polski-vocabulary-limit-")
        try {
            val grammar = directory.resolve("progress-v1.json")
            Files.writeString(grammar, "grammar-sentinel")
            val repository = DesktopVocabularyRepository(directory)
            val original = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))
            repository.saveRaw(original)

            assertFails { repository.saveRaw("x".repeat(10_000_001)) }

            assertEquals(original, repository.loadRaw())
            assertEquals("grammar-sentinel", Files.readString(grammar))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ratingsAppearOnlyAfterRevealAndDirectionSwitchRestoresFront() = runComposeUiTest {
        val repository = InMemoryRepository()
        val session = VocabularySession(repository, FsrsScheduler(),
            { Instant.parse("2026-09-24T12:00:00Z") }, { "user.00000000-0000-4000-8000-000000000001" })
        runBlocking {
            session.start()
            session.select("noun.wife", true)
        }
        setContent { MaterialTheme { VocabularyScreen(session, {}, {}, {}) } }

        onNodeWithText("Вспомни по-польски").assertExists()
        onNodeWithText("Вспомнил").assertDoesNotExist()
        onNodeWithText("Показать ответ").performClick()
        waitForIdle()
        onNodeWithText("Вспомнил").assertExists()
        onNodeWithText("Повторить").assertExists()

        onNodeWithText("Польский → русский").performClick()
        waitForIdle()
        onNodeWithText("Вспомни по-русски").assertExists()
        onNodeWithText("Вспомнил").assertDoesNotExist()
        onNodeWithText("Показать ответ").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun frequencyCoverageKeepsUnreviewedWordsUnavailable() = runComposeUiTest {
        val repository = InMemoryRepository()
        val customId = "user.00000000-0000-4000-8000-000000000091"
        val custom = VocabularyItem(customId, "w", "в (собственное)", "w", "Jestem w domu.", "—", null, true)
        runBlocking { repository.saveRaw(VocabularyCodec.encode(VocabularyDocument(listOf(customId), listOf(custom)))) }
        val session = VocabularySession(repository, FsrsScheduler(),
            { Instant.parse("2026-09-24T12:00:00Z") }, { "user.00000000-0000-4000-8000-000000000001" })
        runBlocking { session.start() }
        session.setFilter("100")
        setContent { MaterialTheme { VocabularyScreen(session, {}, {}, {}) } }

        onNodeWithText("Готово 7/100 · недоступно 93").assertExists()
        onNodeWithContentDescription("w: карточка ещё не готова").assertIsNotEnabled()
        session.setFilter("mine")
        waitForIdle()
        onNodeWithContentDescription("w: в (собственное)").assertIsEnabled()
    }

    /** EnRuAcceptance-2026-09-28.md §7 item 4: the vocabulary screen must offer en-ru's own real
     *  directions/copy — was hardcoded to pl-ru's "Русский → польский"/"Польский → русский" pair,
     *  so switching the active pack to en-ru never changed what this screen showed. */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun enRuPackOffersItsOwnDirectionsAndFlipContent() = runComposeUiTest {
        val goodPackId = activeCoursePackId
        selectCoursePack("en-ru")
        try {
            val repository = InMemoryRepository()
            val session = VocabularySession(repository, FsrsScheduler(),
                { Instant.parse("2026-09-24T12:00:00Z") }, { "user.00000000-0000-4000-8000-000000000002" })
            runBlocking {
                session.start()
                session.select("noun.wife", true)
            }
            setContent { MaterialTheme { VocabularyScreen(session, {}, {}, {}) } }

            onNodeWithText("Русский → английский").assertExists()
            onNodeWithText("Английский → русский").assertExists()
            onNodeWithText("Вспомни по-английски").assertExists()
            onNodeWithText("Показать ответ").performClick()
            waitForIdle()
            onNodeWithText("Эталон · английский").assertExists()

            onNodeWithText("Английский → русский").performClick()
            waitForIdle()
            onNodeWithText("Вспомни по-русски").assertExists()
        } finally { selectCoursePack(goodPackId) }
    }

    private class InMemoryRepository : VocabularyRepository {
        private var raw: String? = null
        override suspend fun loadRaw(): String? = raw
        override suspend fun saveRaw(value: String, backupCurrent: Boolean) { raw = value }
    }
}
