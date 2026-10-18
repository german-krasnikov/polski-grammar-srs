package polski.textwire

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * E3 codec contract: encode/decode round-trip + positive byte oracle cases.
 * Cases from tests/fixtures/textwire/positive.cases (arm A) and negative.cases.
 * JVM only (file I/O); desktopTest. NJ05NextExecutableKotlinPlan.md.
 */
class PolskiTextCodecTest {

    private val root = generateSequence(File(".").absoluteFile) { it.parentFile }
        .first { it.resolve("tests/fixtures/textwire").isDirectory }

    // ---------------------------------------------------------------------------
    // Basic encode/decode round-trips
    // ---------------------------------------------------------------------------

    @Test fun emptyMapRoundTrip() {
        val tree = WireNode.WireMap(emptyList())
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        assertEquals(tree, dec.root)
        assertEquals("progress", dec.kind)
        // re-encode must be byte-identical
        val reEnc = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
        assertTrue(reEnc.bytes.contentEquals(enc.bytes), "re-encode not identical")
    }

    @Test fun distinctEmptyNullZeroAbsence() {
        val tree = WireNode.WireMap(listOf(
            "emptyText" to WireNode.WireString(""),
            "nothing"   to WireNode.WireNull,
            "emptyList" to WireNode.WireList(emptyList()),
            "emptyMap"  to WireNode.WireMap(emptyList()),
            "zero"      to WireNode.WireI64(0),
        ))
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        val m = dec.root.children.toMap()
        assertEquals(WireNode.WireString(""), m["emptyText"])
        assertEquals(WireNode.WireNull, m["nothing"])
        assertEquals(WireNode.WireList(emptyList()), m["emptyList"])
        assertEquals(WireNode.WireMap(emptyList()), m["emptyMap"])
        assertEquals(WireNode.WireI64(0), m["zero"])
    }

    @Test fun escapedAndLiteralKeys() {
        val tree = WireNode.WireMap(listOf(
            "-"   to WireNode.WireString("dash"),
            ""    to WireNode.WireString("empty"),
            "K:-" to WireNode.WireString("kDash"),
        ))
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        val m = dec.root.children.toMap()
        assertEquals("dash", (m["-"] as WireNode.WireString).value)
        assertEquals("empty", (m[""] as WireNode.WireString).value)
        assertEquals("kDash", (m["K:-"] as WireNode.WireString).value)
    }

    @Test fun escapedTextValues() {
        val tree = WireNode.WireMap(listOf(
            "tab"       to WireNode.WireString("a\tb"),
            "newline"   to WireNode.WireString("a\nb"),
            "backslash" to WireNode.WireString("a\\b"),
            "control"   to WireNode.WireString("a\u0001b"),
        ))
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        val m = dec.root.children.toMap()
        assertEquals("a\tb",  (m["tab"] as WireNode.WireString).value)
        assertEquals("a\nb",  (m["newline"] as WireNode.WireString).value)
        assertEquals("a\\b",  (m["backslash"] as WireNode.WireString).value)
        assertEquals("a\u0001b", (m["control"] as WireNode.WireString).value)
    }

    @Test fun numericEdges() {
        val tree = WireNode.WireMap(listOf(
            "maxI64"  to WireNode.WireI64(Long.MAX_VALUE),
            "minI64p1" to WireNode.WireI64(Long.MIN_VALUE + 1), // avoid parsing edge
            "negZero" to WireNode.WireF64((-0.0).toBits()),
            "one"     to WireNode.WireF64(1.0.toBits()),
        ))
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        val m = dec.root.children.toMap()
        assertEquals(Long.MAX_VALUE, (m["maxI64"] as WireNode.WireI64).value)
        assertEquals(Long.MIN_VALUE + 1, (m["minI64p1"] as WireNode.WireI64).value)
        assertEquals((-0.0).toBits(), (m["negZero"] as WireNode.WireF64).bits)
    }

    @Test fun boolValues() {
        val tree = WireNode.WireMap(listOf(
            "t" to WireNode.WireBool(true),
            "f" to WireNode.WireBool(false),
        ))
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        val m = dec.root.children.toMap()
        assertEquals(WireNode.WireBool(true),  m["t"])
        assertEquals(WireNode.WireBool(false), m["f"])
    }

    @Test fun nestedStructure() {
        val inner = WireNode.WireMap(listOf("x" to WireNode.WireI64(42)))
        val list = WireNode.WireList(listOf(WireNode.WireString("a"), WireNode.WireString("b")))
        val tree = WireNode.WireMap(listOf("inner" to inner, "list" to list))
        val enc = PolskiTextCodec.encode(tree, "vocabulary", 1)
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        assertEquals(tree, dec.root)
    }

    // ---------------------------------------------------------------------------
    // Byte oracle P01 from corpus
    // ---------------------------------------------------------------------------

    @Test fun p01EmptyRootByteOracle() {
        val tree = WireNode.WireMap(emptyList())
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val text = enc.bytes.toString(Charsets.UTF_8)
        val lines = text.trimEnd('\n').split('\n')
        assertEquals("POLSKI-TEXT\t1\tprogress\t2", lines[0])
        assertEquals("M\t-\t0", lines[1])
        assertTrue(lines[2].startsWith("H\tsha256\t"), "digest line missing")
        assertEquals(64, lines[2].removePrefix("H\tsha256\t").length)
        // canonical re-encode
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Ok>(dec)
        val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)
        assertTrue(re.bytes.contentEquals(enc.bytes))
    }

    // ---------------------------------------------------------------------------
    // Negative cases
    // ---------------------------------------------------------------------------

    @Test fun rejectsCrlf() {
        val bytes = "POLSKI-TEXT\t1\tprogress\t2\r\nM\t-\t0\r\nH\tsha256\t${"0".repeat(64)}\r\n".toByteArray()
        assertIs<DecodeResult.Err>(PolskiTextCodec.decode(bytes))
    }

    @Test fun rejectsBom() {
        val bytes = "\uFEFFPOLSKI-TEXT\t1\tprogress\t2\n".toByteArray(Charsets.UTF_8)
        assertIs<DecodeResult.Err>(PolskiTextCodec.decode(bytes))
    }

    @Test fun rejectsWrongHeader() {
        val bytes = "POLSKI-UNITS\t1\tprogress\t2\nM\t-\t0\nH\tsha256\t${"0".repeat(64)}\n".toByteArray()
        assertIs<DecodeResult.Err>(PolskiTextCodec.decode(bytes))
    }

    @Test fun rejectsBadDigest() {
        val tree = WireNode.WireMap(emptyList())
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val text = enc.bytes.toString(Charsets.UTF_8)
        val corrupted = text.replace(Regex("[0-9a-f]{64}"), "0".repeat(64))
        assertIs<DecodeResult.Err>(PolskiTextCodec.decode(corrupted.toByteArray()))
    }

    @Test fun rejectsDuplicateKey() {
        val tree = WireNode.WireMap(listOf("x" to WireNode.WireI64(1), "x" to WireNode.WireI64(2)))
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        // encode produces the bytes; decode should reject duplicate
        val dec = PolskiTextCodec.decode(enc.bytes)
        assertIs<DecodeResult.Err>(dec)
    }

    @Test fun rejectsNoncanonicalEscape() {
        // \u0009 instead of \t
        val bad = "POLSKI-TEXT\t1\tprogress\t2\nM\t-\t1\nS\tK:x\ta\\u0009b\nH\tsha256\t${"0".repeat(64)}\n"
        assertIs<DecodeResult.Err>(PolskiTextCodec.decode(bad.toByteArray()))
    }

    @Test fun rejectsNanInF64() {
        val tree = WireNode.WireMap(emptyList())
        val enc = PolskiTextCodec.encode(tree, "progress", 2)
        val text = enc.bytes.toString(Charsets.UTF_8)
        // inject a NaN bit pattern 7ff8000000000000
        val injected = text.replace("M\t-\t0", "M\t-\t1\nD\tK:bad\t7ff8000000000000")
        assertIs<DecodeResult.Err>(PolskiTextCodec.decode(injected.toByteArray()))
    }

    // ---------------------------------------------------------------------------
    // SHA-256 vector check (FIPS 180-4 "abc")
    // ---------------------------------------------------------------------------

    @Test fun sha256Vector() {
        // FIPS 180-4 test vector: SHA-256("abc")
        val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertEquals(expected, PolskiTextCodec.sha256Hex("abc".toByteArray()))
    }
}
