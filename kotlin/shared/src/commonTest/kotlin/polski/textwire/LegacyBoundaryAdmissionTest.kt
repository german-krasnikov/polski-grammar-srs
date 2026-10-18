package polski.textwire

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.test.runTest
import polski.preferences.PreferencesDecode
import polski.preferences.UserPreferencesCodec
import polski.progress.DecodeResult
import polski.progress.ProgressCodec
import polski.srs.FsrsScheduler
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyRepository
import polski.vocabulary.VocabularySession
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Input-only controls for the old decoders; these are not new-wire encoder oracles. */
class LegacyBoundaryAdmissionTest {
    private val progressPrefix = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":""""
    private val vocabularyPrefix = """{"version":1,"pair":"pl-ru","selectedIds":[],"custom":[],"cards":{},"padding":""""
    private val preferencesPrefix = """{"schemaVersion":1"""

    private fun filled(prefix: String, suffix: String, size: Int, value: Char): String {
        val count = size - prefix.length - suffix.length
        require(count >= 0)
        return prefix + value.toString().repeat(count) + suffix
    }

    @Test
    fun progressRetainsSingleLargeUnknownValueAtLegacyByteCeiling() {
        for (size in listOf(9_999_999, 10_000_000)) {
            val raw = filled(progressPrefix, "\"}", size, 'x')
            assertEquals(size, raw.encodeToByteArray().size)
            val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(raw)).document.source
            val value = source["unknown"]!!.jsonPrimitive.content
            assertEquals(size - progressPrefix.length - 2, value.length)
            assertEquals(true, value.all { it == 'x' })
        }
    }

    @Test
    fun progressRetainsNestedUnknownTreeAndOrderedRows() {
        val nested = progressPrefix.dropLast(1) + "[".repeat(128) + "0" + "]".repeat(128) + "}"
        val root = assertIs<DecodeResult.Valid>(ProgressCodec.decode(nested)).document.source
        var item = root["unknown"]!!
        repeat(128) { item = assertIs<JsonArray>(item).single() }
        assertEquals("0", item.jsonPrimitive.content)
        val rows = progressPrefix.dropLast(1) + "[" + (0 until 2048).joinToString(",") { "{\"i\":$it}" } + "]}"
        val rowTree = assertIs<DecodeResult.Valid>(ProgressCodec.decode(rows)).document.source
        val values = assertIs<JsonArray>(rowTree["unknown"])
        assertEquals(2048, values.size)
        values.forEachIndexed { ordinal, value ->
            assertEquals(ordinal.toString(), assertIs<JsonObject>(value)["i"]!!.jsonPrimitive.content)
        }
    }

    @Test
    fun vocabularyAndPreferencesDecodersAdmitLegacyByteCeilings() {
        for (size in listOf(9_999_999, 10_000_000)) {
            val raw = filled(vocabularyPrefix, "\"}", size, 'x')
            assertEquals(size, raw.encodeToByteArray().size)
            assertEquals(emptyList(), VocabularyCodec.decode(raw).custom)
        }
        for (size in listOf(999_999, 1_000_000)) {
            val raw = filled(preferencesPrefix, "}", size, ' ')
            assertEquals(size, raw.encodeToByteArray().size)
            assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(raw))
        }
    }

    @Test
    fun multilingualUnknownProgressAndMaximumCustomVocabularyTextDecode() {
        val progress = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":{"żółć":"😀\t\n"}}"""
        val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(progress)).document.source
        val unknown = assertIs<JsonObject>(source["unknown"])
        assertEquals("😀\t\n", unknown["żółć"]!!.jsonPrimitive.content)

        val text = "ż".repeat(240)
        val vocabulary = """{"version":1,"pair":"pl-ru","selectedIds":[],"custom":[{"id":"user.00000000-0000-0000-0000-000000000000","lemma":"$text","translation":"$text","form":"$text","example":"$text","level":"A1"}],"cards":{}}"""
        val custom = VocabularyCodec.decode(vocabulary).custom.single()
        assertEquals(240, custom.lemma.length)
        assertEquals(text, custom.example)
    }

    @Test
    fun legacyDecodersAcceptLimitPlusOneButVocabularyImportRejectsBeforeWriting() = runTest {
        val progress = filled(progressPrefix, "\"}", 10_000_001, 'x')
        val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(progress)).document.source
        assertEquals(9_999_896, source["unknown"]!!.jsonPrimitive.content.length)

        val vocabulary = filled(vocabularyPrefix, "\"}", 10_000_001, 'x')
        assertEquals(emptyList(), VocabularyCodec.decode(vocabulary).custom)
        val repository = object : VocabularyRepository {
            var writes = 0
            override suspend fun loadRaw(): String? = null
            override suspend fun saveRaw(value: String, backupCurrent: Boolean) { writes++ }
        }
        val session = VocabularySession(repository, FsrsScheduler(),
            { Instant.parse("2026-09-24T12:00:00Z") }, { "user.00000000-0000-4000-8000-000000000001" })
        session.start()
        assertFalse(session.importJson(vocabulary))
        assertEquals(0, repository.writes)
        assertTrue(session.state.value.error.orEmpty().contains("слишком большой"))

        val preferences = filled(preferencesPrefix, "}", 1_000_001, ' ')
        assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(preferences))
    }

    @Test
    fun progressRetainsLargerOrderedTreeAtExactLegacyByteCeiling() {
        val rows = (0 until 32_768).joinToString(",") { "{\"i\":$it}" }
        val prefix = progressPrefix.dropLast(1) + "[" + rows + "],\"padding\":\""
        val raw = filled(prefix, "\"}", 10_000_000, 'x')
        assertEquals(10_000_000, raw.encodeToByteArray().size)
        val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(raw)).document.source
        assertEquals(listOf("version", "cards", "stats", "reviewsToday", "lastDay", "totalReviews", "unknown", "padding"), source.keys.toList())
        val values = assertIs<JsonArray>(source["unknown"])
        assertEquals(32_768, values.size)
        values.forEachIndexed { ordinal, value ->
            assertEquals(ordinal.toString(), assertIs<JsonObject>(value)["i"]!!.jsonPrimitive.content)
        }
        assertEquals(9_617_777, source["padding"]!!.jsonPrimitive.content.length)
    }

    @Test
    fun progressRetainsDeeperAndUtf16SpecificUnknownValues() {
        val nested = progressPrefix.dropLast(1) + "[".repeat(256) + "0" + "]".repeat(256) + "}"
        val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(nested)).document.source
        var item = source["unknown"]!!
        repeat(256) { item = assertIs<JsonArray>(item).single() }
        assertEquals("0", item.jsonPrimitive.content)

        val unicode = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":{"żółć":"😀\t\n","escaped":"\ud83d\ude00","lone":"\ud800"}}"""
        val unknown = assertIs<JsonObject>(assertIs<DecodeResult.Valid>(ProgressCodec.decode(unicode)).document.source["unknown"])
        assertEquals("😀\t\n", unknown["żółć"]!!.jsonPrimitive.content)
        assertEquals("😀", unknown["escaped"]!!.jsonPrimitive.content)
        assertEquals(2, unknown["escaped"]!!.jsonPrimitive.content.length)
        assertEquals(1, unknown["lone"]!!.jsonPrimitive.content.length)
        assertEquals(0xd800, unknown["lone"]!!.jsonPrimitive.content[0].code)
    }
}
