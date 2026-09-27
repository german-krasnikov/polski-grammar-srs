package dev.polski.grammarmatrix

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import kotlin.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.srs.FsrsScheduler
import polski.ui.screens.AndroidVocabularyScreen
import polski.vocabulary.VocabularyRepository
import polski.vocabulary.VocabularySession

/**
 * PostMergeTest-2026-09-27.md regression 2: `VocabularySession.reveal()` writes the same
 * `revealed = true` back on a repeat call, and `StateFlow` never re-emits an equal value, so
 * `AndroidFlipCard`'s `LaunchedEffect(revealed, itemId)` never fires again once the card has
 * already been revealed once. After a full front->back->front cycle the front face's own
 * "Показать ответ" tap calls `reveal()` again (a no-op) and the card silently stays on the front.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidVocabularyScreenFlipTest {
    @get:Rule val composeRule = createComposeRule()

    private class MemoryRepository : VocabularyRepository {
        var raw: String? = null
        override suspend fun loadRaw(): String? = raw
        override suspend fun saveRaw(value: String, backupCurrent: Boolean) { raw = value }
    }

    @Test fun tapSequenceFrontBackFrontBackKeepsFlippingEachTime() {
        val session = VocabularySession(
            MemoryRepository(), FsrsScheduler(), { Instant.parse("2026-09-27T12:00:00Z") }, { "user.x" },
        )
        runBlocking {
            session.start()
            session.select("noun.wife", true)
        }
        composeRule.setContent {
            AndroidVocabularyScreen(
                session = session,
                onImport = {},
                onExport = {},
                launchMutation = { block -> runBlocking { block() } },
                enableSwipeRating = true,
                reduceMotion = true, // skips the native Rive overlay mount, irrelevant to this bug
            )
        }

        // Tap 1: front -> back (the initial reveal).
        composeRule.onNodeWithText("Вспомни по-польски").performClick()
        composeRule.onNodeWithText("Эталон · польский").assertExists()

        // Tap 2: back -> front (a plain tap-to-flip on the answer face, not a rating swipe).
        composeRule.onNodeWithContentDescription("Оценка карточки").performTouchInput { click() }
        composeRule.onNodeWithText("Вспомни по-польски").assertExists()

        // Tap 3: front -> back again. `revealed` is already true, so this must flip locally.
        composeRule.onNodeWithText("Вспомни по-польски").performClick()
        composeRule.onNodeWithText("Эталон · польский").assertExists()

        // Tap 4: back -> front again, proving the cycle keeps working, not just once more.
        composeRule.onNodeWithContentDescription("Оценка карточки").performTouchInput { click() }
        composeRule.onNodeWithText("Вспомни по-польски").assertExists()
    }
}
