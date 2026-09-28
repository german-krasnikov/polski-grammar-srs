package dev.polski.grammarmatrix

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.data.selectCoursePack
import polski.srs.FsrsScheduler
import polski.ui.screens.AndroidVocabularyScreen
import polski.vocabulary.VocabularyRepository
import polski.vocabulary.VocabularySession

/**
 * EnRuAcceptance-2026-09-28.md §7 item 4: [polski.vocabulary.StudyDirection] was already
 * open-typed (EN-09), but [AndroidVocabularyScreen] itself still hardcoded pl-ru's
 * `RussianToPolish`/`PolishToRussian` — so switching the active pack to en-ru left the direction
 * picker, the recall prompt and the reference face all showing Polish/Russian text regardless.
 * This drives the screen directly off a real en-ru [VocabularySession] (`selectCoursePack`, the
 * same production entrypoint a host's Settings screen calls) — the screen itself never needs
 * Android's own restart-gated pack switch (`AndroidSessionViewModel`'s cold-start probe), so this
 * is a true test of the screen's own wiring, not of that separate, already-covered gate.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidVocabularyScreenEnRuDirectionTest {
    @get:Rule val composeRule = createComposeRule()

    private class MemoryRepository : VocabularyRepository {
        var raw: String? = null
        override suspend fun loadRaw(): String? = raw
        override suspend fun saveRaw(value: String, backupCurrent: Boolean) { raw = value }
    }

    @Test fun showsEnRusOwnDirectionsAndRecallLanguage() {
        try {
            selectCoursePack("en-ru")
            val session = VocabularySession(
                MemoryRepository(), FsrsScheduler(), { Instant.parse("2026-09-28T12:00:00Z") }, { "user.x" },
            )
            runBlocking {
                session.start()
                session.select("noun.wife", true)
            }
            composeRule.setContent {
                AndroidVocabularyScreen(
                    session = session, onImport = {}, onExport = {}, launchMutation = { block -> runBlocking { block() } },
                    enableSwipeRating = true, reduceMotion = true,
                )
            }

            // Direction picker: en-ru's own pair, not pl-ru's. The button shows the current
            // selection; opening it reveals the other option as its own menu item.
            composeRule.onNodeWithText("Направление: Русский → английский").assertExists()
            composeRule.onNodeWithText("Направление: Русский → английский").performClick()
            composeRule.onNodeWithText("Английский → русский").assertExists()
            composeRule.onNodeWithText("Английский → русский").performClick()
            composeRule.onNodeWithText("Направление: Английский → русский").assertExists()
            composeRule.onNodeWithText("Направление: Английский → русский").performClick()
            composeRule.onNodeWithText("Русский → английский").performClick()

            // Default direction recalls English (native Russian shown, target English asked back).
            composeRule.onNodeWithText("Вспомни по-английски").assertExists()

            composeRule.onNodeWithText("Вспомни по-английски").performClick()
            composeRule.onNodeWithText("Эталон · английский").assertExists()
        } finally {
            selectCoursePack("pl-ru")
        }
    }
}
