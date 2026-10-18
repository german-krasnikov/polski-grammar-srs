package polski.textwire

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import polski.progress.DecodeResult
import polski.progress.ProgressCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Read-only inspection of a sealed old-user input; no candidate wire encoder is called. */
class LegacyRepresentabilityProbeTest {
    private val expectations: Map<Pair<String, String>, String> by lazy {
        legacyRepresentabilityCases.lineSequence()
            .map(String::trim)
            .filter { line ->
                val kind = line.substringBefore(" | ")
                line.contains(" | ") && kind.isNotEmpty() && kind.all { it in 'A'..'Z' || it == '_' }
            }
            .associate { line ->
                val parts = line.split(" | ")
                require(parts.size == 3) { "Invalid representability expectation: $line" }
                (parts[0] to parts[1]) to parts[2]
            }
    }

    private fun expected(kind: String, path: String): String =
        expectations.getValue(kind to path)

    @Test
    fun frozenOldInputRetainsOrderedUnknownValuesAndUtf16Units() {
        assertEquals(expected("INPUT_BYTES", "progress").toInt(), legacyRepresentabilityInput.encodeToByteArray().size)
        assertEquals(legacyRepresentabilityInputSha256, legacyAdmissionSha256(legacyRepresentabilityInput.encodeToByteArray()))
        val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(legacyRepresentabilityInput)).document.source
        assertEquals(expected("ROOT_KEYS", "source").split(','), source.keys.toList())
        val unknown = assertIs<JsonObject>(source["unknown"])
        val longKey = unknown.keys.single { it.startsWith("longKey-") }
        val expectedKeys = expected("UNKNOWN_KEYS", "source.unknown").split(',')
            .map { if (it == "longKey") longKey else it }
        assertEquals(expectedKeys, unknown.keys.toList())
        val nested = assertIs<JsonObject>(unknown["nested"])
        assertEquals(expected("NESTED_KEYS", "source.unknown.nested").split(','), nested.keys.toList())

        expectations.filterKeys { it.first == "NUMBER_TOKEN" }.forEach { (key, token) ->
            val name = key.second.substringAfterLast('.')
            val number = assertIs<JsonPrimitive>(unknown[name])
            assertFalse(number.isString, "Number $name was coerced to a string")
            assertEquals(token, number.content, "Number token $name changed")
        }
        expectations.filterKeys { it.first == "NULL" }.forEach { (key, presence) ->
            assertEquals("present", presence)
            assertTrue(key.second.substringAfterLast('.') in unknown)
            assertIs<JsonNull>(unknown[key.second.substringAfterLast('.')])
        }
        expectations.filterKeys { it.first == "EMPTY_STRING" }.forEach { (key, presence) ->
            assertEquals("present", presence)
            assertEquals("", assertIs<JsonPrimitive>(unknown[key.second.substringAfterLast('.')]).content)
        }
        expectations.filterKeys { it.first == "EMPTY_LIST" }.forEach { (key, presence) ->
            assertEquals("present", presence)
            assertTrue(assertIs<JsonArray>(unknown[key.second.substringAfterLast('.')]).isEmpty())
        }
        expectations.filterKeys { it.first == "EMPTY_RECORD" }.forEach { (key, presence) ->
            assertEquals("present", presence)
            assertTrue(assertIs<JsonObject>(unknown[key.second.substringAfterLast('.')]).isEmpty())
        }
        expectations.filterKeys { it.first == "ABSENT" }.forEach { (key, absent) ->
            assertEquals("true", absent)
            assertNull(unknown[key.second.substringAfterLast('.')])
            assertFalse(key.second.substringAfterLast('.') in unknown)
        }

        expectations.filterKeys { it.first == "UTF16_HEX" }.forEach { (key, units) ->
            val name = key.second.substringAfterLast('.')
            val value = assertIs<JsonPrimitive>(unknown[name])
            assertTrue(value.isString)
            assertEquals(units.split(','), value.content.map { it.code.toString(16).padStart(4, '0') })
        }
        val (keyUnits, value) = expected("LONG_KEY", "source.unknown.longKey").split(',')
        assertEquals(keyUnits.toInt(), longKey.length)
        assertEquals(value, assertIs<JsonPrimitive>(unknown[longKey]).content)
    }
}
