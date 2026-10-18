package polski.textwire

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import polski.macos.MacPreferencesRepository
import polski.macos.MacProgressRepository
import polski.srs.FsrsScheduler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Rejected old input only: the host must not create a legacy JSON output file. */
@OptIn(ExperimentalForeignApi::class)
class LegacyHostSizeAdmissionTest {
    private val progressPrefix = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":""""
    private val preferencesPrefix = """{"schemaVersion":1"""

    @Test
    fun macProgressRejectsLimitPlusOneBeforeAnyWrite() = runBlocking {
        val directory = NSTemporaryDirectory() + "polski-nj05-progress-${NSUUID().UUIDString}"
        try {
            val raw = progressPrefix + "x".repeat(10_000_001 - progressPrefix.length - 2) + "\"}"
            assertEquals(10_000_001, raw.encodeToByteArray().size)
            assertEquals("Файл прогресса слишком большой", MacProgressRepository(directory, FsrsScheduler()).importJson(raw))
            assertFalse(NSFileManager.defaultManager.fileExistsAtPath("$directory/progress-v1.json"))
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
            assertEquals("Файл настроек слишком большой", MacPreferencesRepository(directory).importJson(raw))
            assertFalse(NSFileManager.defaultManager.fileExistsAtPath("$directory/preferences-v1.json"))
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }
}
