package polski.progress

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class AttemptLogCodecTest {

    private fun event(
        attemptId: String = "ex-1",
        sessionId: String = "session-1",
        at: String = "2026-03-28T23:30:00Z",
        correct: Boolean? = true,
        correctionOf: String? = null,
    ) = AttemptEvent(
        attemptId = attemptId, sessionId = sessionId, skillId = "case.acc.f", seedKey = "wife:beautiful",
        kind = AttemptKind.Practice, at = at, localDay = "2026-03-28", activeMillis = 4200,
        answerMode = "Typed", helpShown = false, disclosed = true, correct = correct,
        correctnessBasis = CorrectnessBasis.Evaluated, selfRating = "Good", correctionOf = correctionOf,
        sessionKind = "Chain", sessionSource = "Chain", rubricVersion = 1,
    )

    @Test
    fun roundTripsAllFieldsIncludingNullableOnes() {
        val log = AttemptLog(1, listOf(event(), event(attemptId = "ex-2", correct = null, correctionOf = "ex-1")))
        val encoded = AttemptLogCodec.encodeText(AttemptLogDocument(log))
        val decoded = assertIs<AttemptLogDecodeResult.Valid>(AttemptLogCodec.decodeText(encoded))
        assertEquals(log, decoded.document.log)
    }

    @Test
    fun emptyLogRoundTrips() {
        val encoded = AttemptLogCodec.encodeText(AttemptLogDocument(AttemptLog()))
        val decoded = assertIs<AttemptLogDecodeResult.Valid>(AttemptLogCodec.decodeText(encoded))
        assertEquals(emptyList(), decoded.document.log.events)
    }

    @Test
    fun wrongKindIsInvalid() {
        val bogus = polski.textwire.PolskiTextCodec.encode(
            polski.textwire.WireNode.WireMap(listOf("version" to polski.textwire.WireNode.WireI64(1))),
            "progress", 1,
        ).bytes
        assertIs<AttemptLogDecodeResult.Invalid>(AttemptLogCodec.decodeText(bogus))
    }

    @Test
    fun unsupportedSchemaVersionIsReported() {
        val root = polski.textwire.WireNode.WireMap(listOf(
            "version" to polski.textwire.WireNode.WireI64(1),
            "events" to polski.textwire.WireNode.WireList(emptyList()),
        ))
        val bytes = polski.textwire.PolskiTextCodec.encode(root, "attempts", 99).bytes
        assertIs<AttemptLogDecodeResult.Unsupported>(AttemptLogCodec.decodeText(bytes))
    }

    // --- Repository: missing journal is empty, not an error (AC5/backward compat) ---

    @Test
    fun missingJournalLoadsAsEmptyNotError() {
        val store = FakeStore()
        val repository = RawAttemptLogRepository(store) { Instant.parse("2026-03-28T23:30:00Z") }
        assertIs<AttemptLogLoadResult.Missing>(immediate { repository.load() })
    }

    @Test
    fun appendOnMissingJournalCreatesItWithOneEvent() {
        val store = FakeStore()
        val repository = RawAttemptLogRepository(store) { Instant.parse("2026-03-28T23:30:00Z") }
        assertIs<AttemptLogSaveResult.Saved>(immediate { repository.append(event()) })
        val loaded = assertIs<AttemptLogLoadResult.Loaded>(immediate { repository.load() })
        assertEquals(listOf(event()), loaded.document.log.events)
    }

    @Test
    fun appendIsAppendOnlyAndNeverEditsAnExistingEntry() {
        val store = FakeStore()
        val repository = RawAttemptLogRepository(store) { Instant.parse("2026-03-28T23:30:00Z") }
        immediate { repository.append(event(attemptId = "ex-1", correct = false)) }
        immediate { repository.append(event(attemptId = "ex-2", correct = true, correctionOf = "ex-1")) }
        val loaded = assertIs<AttemptLogLoadResult.Loaded>(immediate { repository.load() })
        assertEquals(listOf("ex-1", "ex-2"), loaded.document.log.events.map { it.attemptId })
        assertEquals(false, loaded.document.log.events[0].correct, "the original error must never be overwritten")
        assertEquals("ex-1", loaded.document.log.events[1].correctionOf)
    }

    @Test
    fun appendPrunesEventsOlderThanRetentionWindow() {
        val store = FakeStore()
        var clock = Instant.parse("2026-01-01T00:00:00Z")
        val repository = RawAttemptLogRepository(store) { clock }
        immediate { repository.append(event(attemptId = "old", at = "2026-01-01T00:00:00Z")) }
        clock = Instant.parse("2026-01-01T00:00:00Z") + kotlin.time.Duration.parse("40d")
        immediate { repository.append(event(attemptId = "new", at = clock.toString())) }
        val loaded = assertIs<AttemptLogLoadResult.Loaded>(immediate { repository.load() })
        assertEquals(listOf("new"), loaded.document.log.events.map { it.attemptId })
    }

    @Test
    fun appendKeepsEventsWithinRetentionWindow() {
        val store = FakeStore()
        var clock = Instant.parse("2026-01-01T00:00:00Z")
        val repository = RawAttemptLogRepository(store) { clock }
        immediate { repository.append(event(attemptId = "recent", at = "2026-01-01T00:00:00Z")) }
        clock = Instant.parse("2026-01-01T00:00:00Z") + kotlin.time.Duration.parse("10d")
        immediate { repository.append(event(attemptId = "new", at = clock.toString())) }
        val loaded = assertIs<AttemptLogLoadResult.Loaded>(immediate { repository.load() })
        assertEquals(listOf("recent", "new"), loaded.document.log.events.map { it.attemptId })
    }

    @Test
    fun writeFailureIsReportedAsFailSoftNotAnException() {
        val store = FakeStore().apply { failPut = RawAttemptLogRepository.ATTEMPT_LOG_KEY }
        val repository = RawAttemptLogRepository(store) { Instant.parse("2026-03-28T23:30:00Z") }
        val result = assertIs<AttemptLogSaveResult.WriteFailed>(immediate { repository.append(event()) })
        assertTrue(result.cause.message?.contains("Write blocked") == true)
    }

    @Test
    fun readFailureDuringLoadIsUnavailableNotAThrow() {
        val store = FakeStore().apply { failGet = RawAttemptLogRepository.ATTEMPT_LOG_KEY }
        val repository = RawAttemptLogRepository(store) { Instant.parse("2026-03-28T23:30:00Z") }
        assertIs<AttemptLogLoadResult.Unavailable>(immediate { repository.load() })
    }

    @Test
    fun corruptStoredJournalIsReportedInvalidNotThrown() {
        val store = FakeStore().apply { values[RawAttemptLogRepository.ATTEMPT_LOG_KEY] = "not a valid wire document" }
        val repository = RawAttemptLogRepository(store) { Instant.parse("2026-03-28T23:30:00Z") }
        assertIs<AttemptLogLoadResult.Invalid>(immediate { repository.load() })
    }

    private class FakeStore : RawKeyValueStore {
        val values = mutableMapOf<String, String>()
        var failGet: String? = null
        var failPut: String? = null
        override fun get(key: String): String? {
            if (key == failGet) error("Read blocked")
            return values[key]
        }
        override fun put(key: String, value: String) {
            if (key == failPut) error("Write blocked")
            values[key] = value
        }
    }

    private fun <T> immediate(block: suspend () -> T): T {
        var completion: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { completion = result }
        })
        assertTrue(completion != null, "Repository unexpectedly suspended")
        return requireNotNull(completion).getOrThrow()
    }
}
