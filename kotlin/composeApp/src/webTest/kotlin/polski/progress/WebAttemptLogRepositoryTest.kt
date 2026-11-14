package polski.progress

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.browser.window
import polski.platform.WebAttemptLogRepository

/** Runs against the browser's actual LocalStorage in both JS and Wasm test bundles — P1
 *  blueprint §P1.1: its own key, entirely separate from [RawProgressRepository]'s own keys, so
 *  writing/clearing one never touches the other (export/import compatibility). */
class WebAttemptLogRepositoryTest {
    // Real current time, not a fixed past date: RawAttemptLogRepository prunes by age against the
    // real system clock, so a stale fixture date would be silently pruned on the second append.
    private val at = kotlin.time.Clock.System.now().toString()

    private fun event(attemptId: String) = AttemptEvent(
        attemptId = attemptId, sessionId = "session-1", skillId = "case.acc.f", seedKey = "wife:beautiful",
        kind = AttemptKind.Practice, at = at, localDay = "2026-03-29", activeMillis = 1000,
        answerMode = "Typed", helpShown = false, disclosed = true, correct = true,
        correctnessBasis = CorrectnessBasis.Evaluated, selfRating = "Good",
        sessionKind = "Focused", sessionSource = "ManualPick", rubricVersion = 1,
    )

    @Test
    fun browserStorageAppendsAndReloadsIndependentlyOfProgressKeys() {
        val original = window.localStorage.getItem(RawAttemptLogRepository.ATTEMPT_LOG_KEY)
        val progressOriginal = window.localStorage.getItem(RawProgressRepository.PREVIEW_KEY)
        try {
            window.localStorage.removeItem(RawAttemptLogRepository.ATTEMPT_LOG_KEY)
            window.localStorage.removeItem(RawProgressRepository.PREVIEW_KEY)
            val repository = WebAttemptLogRepository()

            assertIs<AttemptLogLoadResult.Missing>(immediate { repository.load() })
            assertEquals(AttemptLogSaveResult.Saved, immediate { repository.append(event("ex-1")) })
            assertEquals(AttemptLogSaveResult.Saved, immediate { repository.append(event("ex-2")) })

            val loaded = assertIs<AttemptLogLoadResult.Loaded>(immediate { WebAttemptLogRepository().load() })
            assertEquals(listOf("ex-1", "ex-2"), loaded.document.log.events.map { it.attemptId })
            // Never touches the Progress key.
            assertEquals(null, window.localStorage.getItem(RawProgressRepository.PREVIEW_KEY))
        } finally {
            if (original == null) window.localStorage.removeItem(RawAttemptLogRepository.ATTEMPT_LOG_KEY)
            else window.localStorage.setItem(RawAttemptLogRepository.ATTEMPT_LOG_KEY, original)
            if (progressOriginal == null) window.localStorage.removeItem(RawProgressRepository.PREVIEW_KEY)
            else window.localStorage.setItem(RawProgressRepository.PREVIEW_KEY, progressOriginal)
        }
    }

    private fun <T> immediate(block: suspend () -> T): T {
        var completion: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { completion = result }
        })
        return requireNotNull(completion) { "Repository unexpectedly suspended" }.getOrThrow()
    }
}
