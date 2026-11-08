package polski.textwire

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * PolskiTextCodec: generated round-trip tests from frozen .cases corpus tree definitions.
 * Oracle: tests/fixtures/textwire/positive.cases.
 * Do not edit between markers; run scripts/nj03/generate-textwire-tests.py.
 */
class PolskiTextCodecTest {
        // --- GENERATED START: do not edit, run scripts/nj03/generate-textwire-tests.py ---
    @Test fun allPositiveCases() {
        run { // P01 empty-root
            val tree = WireNode.WireMap(emptyList())
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P01_empty_root")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P01_empty_root: re-encode")
        }
        run { // P02 distinct-empty-null-zero-and-absence
            val tree = WireNode.WireMap(listOf("emptyText" to WireNode.WireString(""), "nothing" to WireNode.WireNull, "emptyList" to WireNode.WireList(emptyList()), "zero" to WireNode.WireI64(0L)))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P02_distinct_empty_null_zero_and_absence")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P02_distinct_empty_null_zero_and_absence: re-encode")
        }
        run { // P03 escaped-and-literal-keys
            val tree = WireNode.WireMap(listOf("-" to WireNode.WireString("dash"), "" to WireNode.WireString("empty"), "K:-" to WireNode.WireString("prefixed")))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P03_escaped_and_literal_keys")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P03_escaped_and_literal_keys: re-encode")
        }
        run { // P04 nested-order-and-unknown-extension
            val tree = WireNode.WireMap(listOf("z" to WireNode.WireList(emptyList()), "id" to WireNode.WireString("a"), "x-unknown" to WireNode.WireBool(true)))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P04_nested_order_and_unknown_extension")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P04_nested_order_and_unknown_extension: re-encode")
        }
        run { // P05 signed-i64-edges-and-numeric-text
            val tree = WireNode.WireMap(listOf("min" to WireNode.WireI64(Long.MIN_VALUE), "max" to WireNode.WireI64(9223372036854775807L), "numericText" to WireNode.WireString("001e+3"), "negativeZeroText" to WireNode.WireString("-0")))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P05_signed_i64_edges_and_numeric_text")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P05_signed_i64_edges_and_numeric_text: re-encode")
        }
        run { // P06 finite-binary64-bits
            val tree = WireNode.WireMap(listOf("positiveZero" to WireNode.WireString("f64bits 0000000000000000"), "negativeZero" to WireNode.WireString("f64bits 8000000000000000"), "one" to WireNode.WireString("f64bits 3ff0000000000000"), "smallestSubnormal" to WireNode.WireString("f64bits 0000000000000001"), "largestFinite" to WireNode.WireString("f64bits 7fefffffffffffff")))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P06_finite_binary64_bits")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P06_finite_binary64_bits: re-encode")
        }
        run { // P07 multilingual-controls-and-line-separators
            val tree = WireNode.WireMap(listOf("text" to WireNode.WireString("Zażółć gęślą jaźń / Привет / 漢字 / 😀\tline\nnext\\end\u0000\u001F\u007F\u0085\u2028\u2029")))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P07_multilingual_controls_and_line_separators")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P07_multilingual_controls_and_line_separators: re-encode")
        }
        run { // P08 literal-utf16-code-unit-order
            val tree = WireNode.WireMap(listOf("A" to WireNode.WireString("ASCII"), "\\uD800" to WireNode.WireString("lone high"), "\\uD800\\uDC00" to WireNode.WireString("U+10000"), "\\uDC00" to WireNode.WireString("lone low"), "\\uE000" to WireNode.WireString("BMP")))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P08_literal_utf16_code_unit_order")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P08_literal_utf16_code_unit_order: re-encode")
        }
        run { // P09 decoded-non-sorted-extension-order
            val tree = WireNode.WireMap(listOf("\\uE000" to WireNode.WireString("first"), "A" to WireNode.WireString("second"), "\\uD800\\uDC00" to WireNode.WireString("third")))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P09_decoded_non_sorted_extension_order")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P09_decoded_non_sorted_extension_order: re-encode")
        }
        // NOT RUN (JVM): P10 UTF16-lone-value-and-paired-value — lone surrogate round-trip not possible via UTF-8
        run { // P11 boolean-and-false
            val tree = WireNode.WireMap(listOf("yes" to WireNode.WireBool(true), "no" to WireNode.WireBool(false)))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P11_boolean_and_false")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P11_boolean_and_false: re-encode")
        }
        run { // P12 snapshot-framing-kind
            val tree = WireNode.WireMap(listOf("revision" to WireNode.WireI64(1L), "effectId" to WireNode.WireString("download-1")))
            val enc = PolskiTextCodec.encode(tree, "progress", 2)
            val dec = PolskiTextCodec.decode(enc.bytes)
            assertIs<DecodeResult.Ok>(dec)
            assertEquals(tree, dec.root, "P12_snapshot_framing_kind")
            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
            assertTrue(re.bytes.contentEquals(enc.bytes), "P12_snapshot_framing_kind: re-encode")
        }
    }
        // --- GENERATED END ---

    @Test fun sha256Vector() {
        // FIPS 180-4: SHA-256("abc")
        val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertTrue(PolskiTextCodec.sha256Hex("abc".toByteArray()) == expected)
    }


}
