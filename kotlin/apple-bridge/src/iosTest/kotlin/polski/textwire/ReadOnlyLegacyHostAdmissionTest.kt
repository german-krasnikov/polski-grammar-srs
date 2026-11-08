package polski.textwire

import kotlinx.coroutines.runBlocking
import platform.Foundation.NSUserDefaults
import polski.ios.IosProgressRepository
import polski.ios.IosVocabularyRepository
import polski.progress.LoadResult
import polski.srs.FsrsScheduler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** NSUserDefaults is seeded only with sealed old-user input; repositories only read. */
class ReadOnlyLegacyHostAdmissionTest {
    private val progressPrefix = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":""""
    private val vocabularyPrefix = """{"version":1,"pair":"pl-ru","selectedIds":[],"custom":[],"cards":{},"padding":""""

    private suspend fun withSeededInput(key: String, raw: String, block: suspend (NSUserDefaults) -> Unit) {
        val suite = "polski-nj05-readonly-${kotlin.random.Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        val before = legacyAdmissionSha256(raw.encodeToByteArray())
        try {
            defaults.setObject(raw, forKey = key)
            assertTrue(defaults.synchronize())
            assertEquals(before, legacyAdmissionSha256(checkNotNull(defaults.stringForKey(key)).encodeToByteArray()))
            val keysBefore = defaults.dictionaryRepresentation().keys.map { it.toString() }.toSet()
            block(defaults)
            assertEquals(before, legacyAdmissionSha256(checkNotNull(defaults.stringForKey(key)).encodeToByteArray()))
            assertEquals(keysBefore, defaults.dictionaryRepresentation().keys.map { it.toString() }.toSet())
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun iosProgressLoadRetainsFrozenUnknownValuesAndReaderAdmitsImportLimitPlusOne() = runBlocking {
        assertEquals(legacyRepresentabilityInputSha256,
            legacyAdmissionSha256(legacyRepresentabilityInput.encodeToByteArray()))
        withSeededInput("polski-progress-v1", legacyRepresentabilityInput) { defaults ->
            assertIs<LoadResult.Loaded>(IosProgressRepository(FsrsScheduler(), defaults).load())
        }
        val size = 10_000_001
        val raw = progressPrefix + "x".repeat(size - progressPrefix.length - 2) + "\"}"
        assertTrue(legacyAdmissionMatches("PG-$size", raw))
        withSeededInput("polski-progress-v1", raw) { defaults ->
            assertIs<LoadResult.Loaded>(IosProgressRepository(FsrsScheduler(), defaults).load())
        }
    }

    @Test
    fun iosVocabularyRawLoadKeepsSealedPlusOneInput() = runBlocking {
        val size = 10_000_001
        val raw = vocabularyPrefix + "x".repeat(size - vocabularyPrefix.length - 2) + "\"}"
        assertTrue(legacyAdmissionMatches("VO-$size", raw))
        withSeededInput("polski-vocabulary-pl-ru-v1", raw) { defaults ->
            val loaded = assertNotNull(IosVocabularyRepository(defaults).loadRaw())
            assertEquals(legacyAdmissionSha256(raw.encodeToByteArray()), legacyAdmissionSha256(loaded.encodeToByteArray()))
        }
    }
}
