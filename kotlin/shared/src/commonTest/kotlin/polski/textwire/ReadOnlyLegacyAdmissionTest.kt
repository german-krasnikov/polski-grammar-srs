package polski.textwire

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonPrimitive
import polski.preferences.PreferencesDecode
import polski.preferences.UserPreferencesCodec
import polski.progress.DecodeResult
import polski.progress.LoadResult
import polski.progress.ProgressCodec
import polski.progress.RawKeyValueStore
import polski.progress.RawProgressRepository
import polski.srs.FsrsScheduler
import polski.vocabulary.VocabularyCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Uses only byte-sealed input recipes and read-only legacy entrypoints. */
class ReadOnlyLegacyAdmissionTest {
    private val progressPrefix = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":""""
    private val vocabularyPrefix = """{"version":1,"pair":"pl-ru","selectedIds":[],"custom":[],"cards":{},"padding":""""
    private val preferencesPrefix = """{"schemaVersion":1"""

    private fun filled(prefix: String, suffix: String, size: Int, value: Char): String {
        require(size >= prefix.length + suffix.length)
        return prefix + value.toString().repeat(size - prefix.length - suffix.length) + suffix
    }

    @Test
    fun progressDecoderSeparatesOldReaderFromHostByteCeiling() {
        for (size in listOf(9_999_999, 10_000_000, 10_000_001)) {
            val raw = filled(progressPrefix, "\"}", size, 'x')
            assertTrue(legacyAdmissionMatches("PG-$size", raw))
            val before = legacyAdmissionSha256(raw.encodeToByteArray())
            val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(raw)).document.source
            assertEquals(size - 105, source["unknown"]!!.jsonPrimitive.content.length)
            assertEquals(before, legacyAdmissionSha256(raw.encodeToByteArray()))
        }
    }

    @Test
    fun vocabularyDecoderAdmitsButDiscardsSealedUnknownPadding() {
        for (size in listOf(9_999_999, 10_000_000, 10_000_001)) {
            val raw = filled(vocabularyPrefix, "\"}", size, 'x')
            assertTrue(legacyAdmissionMatches("VO-$size", raw))
            val before = legacyAdmissionSha256(raw.encodeToByteArray())
            val document = VocabularyCodec.decode(raw)
            assertTrue(document.custom.isEmpty())
            assertTrue(document.selectedIds.isEmpty())
            assertEquals(before, legacyAdmissionSha256(raw.encodeToByteArray()))
        }
    }

    @Test
    fun preferencesDecoderAndPeekPreserveOldInputWithoutWriting() {
        for (size in listOf(999_999, 1_000_000, 1_000_001)) {
            val raw = filled(preferencesPrefix, "}", size, ' ')
            assertTrue(legacyAdmissionMatches("PF-$size", raw))
            val before = legacyAdmissionSha256(raw.encodeToByteArray())
            assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(raw))
            assertNull(UserPreferencesCodec.peekTargetNative(raw)) // This v1 input omits coursePair.
            assertEquals(before, legacyAdmissionSha256(raw.encodeToByteArray()))
        }
    }

    @Test
    fun rawProgressLegacyLoadOnlyReturnsSealedBytes() = runTest {
        val raw = legacyRepresentabilityInput
        val before = legacyAdmissionSha256(raw.encodeToByteArray())
        val keys = mutableMapOf(RawProgressRepository.LEGACY_KEY to raw)
        val store = object : RawKeyValueStore {
            override fun get(key: String): String? = keys[key]
            override fun put(key: String, value: String): Unit = error("Read-only load attempted a write")
        }
        val result = RawProgressRepository(store, FsrsScheduler()).load()
        assertEquals(raw, assertIs<LoadResult.LegacyAvailable>(result).raw)
        assertEquals(setOf(RawProgressRepository.LEGACY_KEY), keys.keys)
        assertEquals(before, legacyAdmissionSha256(keys.getValue(RawProgressRepository.LEGACY_KEY).encodeToByteArray()))
    }
}
