package polski.textwire

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import polski.macos.MacPreferencesRepository
import polski.macos.MacProgressRepository
import polski.macos.MacVocabularyRepository
import polski.preferences.PreferencesDecode
import polski.progress.LoadResult
import polski.srs.FsrsScheduler
import polski.vocabulary.VocabularyCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Only test-fixture injection writes old bytes; all application entrypoints called here read. */
@OptIn(ExperimentalForeignApi::class)
class ReadOnlyLegacyHostAdmissionTest {
    private val progressPrefix = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":""""
    private val vocabularyPrefix = """{"version":1,"pair":"pl-ru","selectedIds":[],"custom":[],"cards":{},"padding":""""
    private val preferencesPrefix = """{"schemaVersion":1"""

    private fun filled(prefix: String, suffix: String, size: Int, value: Char): String =
        prefix + value.toString().repeat(size - prefix.length - suffix.length) + suffix

    private suspend fun withSeededInput(fileName: String, raw: String, block: suspend (String) -> Unit) {
        val directory = NSTemporaryDirectory() + "polski-nj05-readonly-${NSUUID().UUIDString}"
        val manager = NSFileManager.defaultManager
        val before = legacyAdmissionSha256(raw.encodeToByteArray())
        try {
            assertTrue(manager.createDirectoryAtPath(directory, true, null, null))
            val file = "$directory/$fileName"
            assertTrue((raw as NSString).writeToFile(file, true, NSUTF8StringEncoding, null))
            assertEquals(before, legacyAdmissionSha256(checkNotNull(NSString.stringWithContentsOfFile(file, NSUTF8StringEncoding, null)).encodeToByteArray()))
            block(directory)
            assertEquals(before, legacyAdmissionSha256(checkNotNull(NSString.stringWithContentsOfFile(file, NSUTF8StringEncoding, null)).encodeToByteArray()))
            assertEquals(1, manager.contentsOfDirectoryAtPath(directory, null).orEmpty().size)
        } finally {
            manager.removeItemAtPath(directory, null)
        }
    }

    @Test
    fun macProgressLoadRespectsItsReadCapAndKeepsEveryOldByte() = runBlocking {
        for (size in listOf(9_999_999, 10_000_000, 10_000_001)) {
            val raw = filled(progressPrefix, "\"}", size, 'x')
            assertTrue(legacyAdmissionMatches("PG-$size", raw))
            withSeededInput("progress-v1.json", raw) { directory ->
                val result = MacProgressRepository(directory, FsrsScheduler()).load()
                if (size <= 10_000_000) assertIs<LoadResult.Loaded>(result)
                else assertIs<LoadResult.Invalid>(result)
            }
        }
        assertEquals(legacyRepresentabilityInputSha256,
            legacyAdmissionSha256(legacyRepresentabilityInput.encodeToByteArray()))
        withSeededInput("progress-v1.json", legacyRepresentabilityInput) { directory ->
            assertIs<LoadResult.Loaded>(MacProgressRepository(directory, FsrsScheduler()).load())
        }
    }

    @Test
    fun macVocabularyRawReaderKeepsSealedPlusOneInput() = runBlocking {
        val size = 10_000_001
        val raw = filled(vocabularyPrefix, "\"}", size, 'x')
        assertTrue(legacyAdmissionMatches("VO-$size", raw))
        withSeededInput("vocabulary-v1.json", raw) { directory ->
            val read = checkNotNull(MacVocabularyRepository(directory).loadRaw())
            assertEquals(legacyAdmissionSha256(raw.encodeToByteArray()), legacyAdmissionSha256(read.encodeToByteArray()))
            assertTrue(VocabularyCodec.decode(read).custom.isEmpty())
        }
    }

    @Test
    fun macPreferencesReadIsIndependentOfOneMegabyteImportCap() = runBlocking {
        for (size in listOf(999_999, 1_000_000, 1_000_001)) {
            val raw = filled(preferencesPrefix, "}", size, ' ')
            assertTrue(legacyAdmissionMatches("PF-$size", raw))
            withSeededInput("preferences-v1.json", raw) { directory ->
                assertIs<PreferencesDecode.Loaded>(MacPreferencesRepository(directory).load())
            }
        }
    }
}
