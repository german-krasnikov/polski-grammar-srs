package polski.textwire

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import polski.macos.MacPreferencesRepository
import polski.macos.MacProgressRepository
import polski.macos.MacVocabularyRepository
import polski.srs.FsrsScheduler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Historical host admission only; accepted old files live solely in isolated, deleted test storage. */
@OptIn(ExperimentalForeignApi::class)
class LegacyHostSizeAdmissionTest {
    private val progressPrefix = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":""""
    private val vocabularyPrefix = """{"version":1,"pair":"pl-ru","selectedIds":[],"custom":[],"cards":{},"padding":""""
    private val preferencesPrefix = """{"schemaVersion":1"""

    private fun filled(prefix: String, suffix: String, size: Int, value: Char): String =
        prefix + value.toString().repeat(size - prefix.length - suffix.length) + suffix

    @Test
    fun macProgressImportsLedgerPinnedNearAndExactLimitInputs() = runBlocking {
        for (size in listOf(9_999_999, 10_000_000)) {
            val directory = NSTemporaryDirectory() + "polski-nj05-progress-$size-${NSUUID().UUIDString}"
            try {
                val raw = filled(progressPrefix, "\"}", size, 'x')
                assertTrue(legacyAdmissionMatches("PG-$size", raw))
                val repository = MacProgressRepository(directory, FsrsScheduler())
                assertNull(repository.importJson(raw))
                assertEquals(raw, repository.recoveryRaw())
            } finally {
                NSFileManager.defaultManager.removeItemAtPath(directory, null)
            }
        }
    }

    @Test
    fun macVocabularyRepositorySavesLedgerPinnedNearAndExactLimitInputs() = runBlocking {
        for (size in listOf(9_999_999, 10_000_000)) {
            val directory = NSTemporaryDirectory() + "polski-nj05-vocabulary-$size-${NSUUID().UUIDString}"
            try {
                val raw = filled(vocabularyPrefix, "\"}", size, 'x')
                assertTrue(legacyAdmissionMatches("VO-$size", raw))
                val repository = MacVocabularyRepository(directory)
                repository.saveRaw(raw, false)
                assertEquals(raw, repository.loadRaw())
            } finally {
                NSFileManager.defaultManager.removeItemAtPath(directory, null)
            }
        }
    }

    @Test
    fun macVocabularyRepositoryRejectsLedgerPinnedLimitPlusOneBeforeAnyWrite() = runBlocking {
        val directory = NSTemporaryDirectory() + "polski-nj05-vocabulary-${NSUUID().UUIDString}"
        try {
            val raw = filled(vocabularyPrefix, "\"}", 10_000_001, 'x')
            assertTrue(legacyAdmissionMatches("VO-10000001", raw))
            assertFailsWith<IllegalArgumentException> { MacVocabularyRepository(directory).saveRaw(raw, false) }
            assertFalse(NSFileManager.defaultManager.fileExistsAtPath(directory))
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }

    @Test
    fun macPreferencesImportsLedgerPinnedNearAndExactLimitInputs() {
        for (size in listOf(999_999, 1_000_000)) {
            val directory = NSTemporaryDirectory() + "polski-nj05-preferences-$size-${NSUUID().UUIDString}"
            try {
                val raw = filled(preferencesPrefix, "}", size, ' ')
                assertTrue(legacyAdmissionMatches("PF-$size", raw))
                val repository = MacPreferencesRepository(directory)
                assertNull(repository.importJson(raw))
                assertEquals(raw, repository.raw())
            } finally {
                NSFileManager.defaultManager.removeItemAtPath(directory, null)
            }
        }
    }

    @Test
    fun macProgressRejectsLimitPlusOneBeforeAnyWrite() = runBlocking {
        val directory = NSTemporaryDirectory() + "polski-nj05-progress-${NSUUID().UUIDString}"
        try {
            val raw = progressPrefix + "x".repeat(10_000_001 - progressPrefix.length - 2) + "\"}"
            assertEquals(10_000_001, raw.encodeToByteArray().size)
            assertTrue(legacyAdmissionMatches("PG-10000001", raw))
            assertEquals("Файл прогресса слишком большой", MacProgressRepository(directory, FsrsScheduler()).importJson(raw))
            assertFalse(NSFileManager.defaultManager.fileExistsAtPath("$directory/progress-v1.json"))
            assertFalse(NSFileManager.defaultManager.fileExistsAtPath(directory))
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }

    @Test
    fun macPreferencesRejectsLimitPlusOneBeforeAnyWrite() {
        val directory = NSTemporaryDirectory() + "polski-nj05-preferences-${NSUUID().UUIDString}"
        try {
            val raw = preferencesPrefix + " ".repeat(1_000_001 - preferencesPrefix.length - 1) + "}"
            assertEquals(1_000_001, raw.encodeToByteArray().size)
            assertTrue(legacyAdmissionMatches("PF-1000001", raw))
            assertEquals("Файл настроек слишком большой", MacPreferencesRepository(directory).importJson(raw))
            assertFalse(NSFileManager.defaultManager.fileExistsAtPath("$directory/preferences-v1.json"))
            assertFalse(NSFileManager.defaultManager.fileExistsAtPath(directory))
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }
}
