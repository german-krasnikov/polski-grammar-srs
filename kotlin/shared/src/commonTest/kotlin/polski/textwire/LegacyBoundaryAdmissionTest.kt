package polski.textwire

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.preferences.PreferencesDecode
import polski.preferences.UserPreferencesCodec
import polski.progress.DecodeResult
import polski.progress.ProgressCodec
import polski.vocabulary.VocabularyCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
}
