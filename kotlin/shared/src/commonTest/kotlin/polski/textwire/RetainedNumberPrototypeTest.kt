package polski.textwire

import kotlinx.serialization.json.JsonObject
import polski.progress.DecodeResult
import polski.progress.ProgressCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/** Only the frozen R01 bytes are exact wire oracles; R02 freezes a tree, not whole wire bytes. */
class RetainedNumberPrototypeTest {
    private val tree = ProbeMap(listOf(
        "wide" to RetainedNumber("9223372036854775808"),
        "huge" to RetainedNumber("1e10000"),
        "decimal" to RetainedNumber("1.0"),
        "exponentOne" to RetainedNumber("1e0"),
        "negativeZero" to RetainedNumber("-0"),
        "zero" to ProbeInt(0),
        "negativeZeroBits" to ProbeDoubleBits("8000000000000000"),
        "numericText" to ProbeString("-0"),
        "nullValue" to ProbeNull,
        "listExample" to ProbeList(listOf(RetainedNumber("1e0"))),
    ))

    private val oracleA = """POLSKI-TEXT	1	progress	2
M	-	10
R	K:wide	9223372036854775808
R	K:huge	1e10000
R	K:decimal	1.0
R	K:exponentOne	1e0
R	K:negativeZero	-0
I	K:zero	0
D	K:negativeZeroBits	8000000000000000
S	K:numericText	-0
N	K:nullValue
L	K:listExample	1
R	-	1e0
H	sha256	a0fe2b8ee5dc422e73e2f9ee3e92d781cd7370541a00b2aa94909a0124f098ee
"""
    private val oracleC = """POLSKI-UNITS	1	progress	2
M	-	10
R	KU4:0077006900640065	9223372036854775808
R	KU4:0068007500670065	1e10000
R	KU7:0064006500630069006D0061006C	1.0
R	KU11:006500780070006F006E0065006E0074004F006E0065	1e0
R	KU12:006E0065006700610074006900760065005A00650072006F	-0
I	KU4:007A00650072006F	0
D	KU16:006E0065006700610074006900760065005A00650072006F0042006900740073	8000000000000000
S	KU11:006E0075006D00650072006900630054006500780074	U2:002D0030
N	KU9:006E0075006C006C00560061006C00750065
L	KU11:006C006900730074004500780061006D0070006C0065	1
R	-	1e0
H	sha256	ed734896b6450a4b18744d9b066ca7d3157bb432e9338c1a512ef65f03d75569
"""

    @Test
    fun rejectsFrozenInvalidPositiveSign() {
        assertFalse(RetainedNumber.isValid("+1"))
    }

    @Test
    fun r01HasExactIndependentBytesAndDisjointTypedRoundTrip() {
        for ((arm, oracle, size, sha) in listOf(
            Quad(ProbeArm.A, oracleA, 316, "430e231aa713e9b2f156b177996a2914e265490ec7632bd2ac7ceb75e57fad94"),
            Quad(ProbeArm.C, oracleC, 618, "c410aa1ec0354fa08722d22b4876e63279693bb0a3ba20e63abf8f5cc21c7658"),
        )) {
            val actual = RetainedNumberProbeWire.encode(tree, arm)
            assertEquals(size, actual.encodeToByteArray().size, "R01 $arm byte count")
            assertEquals(sha, legacyAdmissionSha256(actual.encodeToByteArray()), "R01 $arm full SHA")
            assertEquals(oracle, actual, "R01 $arm independently frozen literal")
            val decoded = RetainedNumberProbeWire.decode(oracle, arm)
            assertEquals(tree, decoded)
            assertEquals(oracle, RetainedNumberProbeWire.encode(decoded, arm))
            assertIs<RetainedNumber>(decoded.entries[4].second)
            assertIs<ProbeInt>(decoded.entries[5].second)
            assertIs<ProbeDoubleBits>(decoded.entries[6].second)
            assertIs<ProbeString>(decoded.entries[7].second)
            assertEquals(ProbeNull, decoded.entries[8].second)
        }
    }

    @Test
    fun r02OldReaderProjectsOrderedNumericNodesAndUtf16WithoutWritingOldInput() {
        val oldBytes = legacyRepresentabilityInput.encodeToByteArray()
        assertEquals(683, oldBytes.size)
        assertEquals(legacyRepresentabilityInputSha256, legacyAdmissionSha256(oldBytes))
        val source = assertIs<DecodeResult.Valid>(ProgressCodec.decode(legacyRepresentabilityInput)).document.source
        val unknown = assertIs<JsonObject>(source["unknown"])
        val projected = assertIs<ProbeMap>(RetainedNumberProbeWire.projectOldUnknown(unknown))
        val names = projected.entries.map { it.first }
        val longKey = names.single { it.startsWith("longKey-") }
        assertEquals(
            listOf("wide", "huge", "decimal", "exponentOne", "negativeZero", "nullValue",
                "emptyString", "emptyList", "emptyRecord", "nested", "literal", "escaped", "loneHigh", longKey),
            names,
        )
        val expected = listOf("9223372036854775808", "1e10000", "1.0", "1e0", "-0")
        assertEquals(expected, projected.entries.take(5).map { assertIs<RetainedNumber>(it.second).token })
        assertEquals(ProbeNull, projected.entries[5].second)
        assertEquals(ProbeString(""), projected.entries[6].second)
        assertEquals(ProbeList(emptyList()), projected.entries[7].second)
        assertEquals(ProbeMap(emptyList()), projected.entries[8].second)
        assertEquals(listOf("z", "a", "m"), assertIs<ProbeMap>(projected.entries[9].second).entries.map { it.first })
        assertEquals(listOf(0xd83d, 0xde00), assertIs<ProbeString>(projected.entries[10].second).value.map { it.code })
        assertEquals(listOf(0xd83d, 0xde00), assertIs<ProbeString>(projected.entries[11].second).value.map { it.code })
        assertEquals(listOf(0xd800), assertIs<ProbeString>(projected.entries[12].second).value.map { it.code })
        assertEquals(320, longKey.length)
        for (arm in ProbeArm.entries) {
            val wire = RetainedNumberProbeWire.encode(projected, arm)
            val decoded = RetainedNumberProbeWire.decode(wire, arm)
            assertEquals(projected, decoded, "R02 $arm tree; full bytes are not frozen")
            assertEquals(wire, RetainedNumberProbeWire.encode(decoded, arm))
        }
        assertEquals(legacyRepresentabilityInputSha256, legacyAdmissionSha256(oldBytes))
    }

    @Test
    fun n01ToN13AndN15RejectAtFrozenGenericBoundaries() {
        val invalid = listOf("+1", "01", ".5", "1.", "1e", "--1", "NaN", "Infinity", "\u0661")
        for ((arm, oracle) in listOf(ProbeArm.A to oracleA, ProbeArm.C to oracleC)) {
            val wideKey = if (arm == ProbeArm.A) "K:wide" else "KU4:0077006900640065"
            val row = "R\t$wideKey\t9223372036854775808\n"
            val prefix = oracle.substringBefore("H\tsha256\t")
            assertEquals(1, prefix.windowed(row.length).count { it == row })
            for (token in invalid) {
                assertFalse(RetainedNumber.isValid(token))
                val mutated = withDigest(prefix.replace(row, "R\t$wideKey\t$token\n"))
                assertFault("RetainedNumberSyntax", mutated, arm)
            }
            assertFault("RowArity", withDigest(prefix.replace(row, "R\t$wideKey\t\t\n")), arm)
            assertFault("RowArity", withDigest(prefix.replace(row, "R\t$wideKey\t1\n0\n")), arm)
            assertFault("MapKey", withDigest(prefix.replace(row, "R\t-\t9223372036854775808\n")), arm)
            assertFault("ListItemKey", withDigest(prefix.replace("R\t-\t1e0\n", "R\t$wideKey\t1e0\n")), arm)
            assertFault("DigestMismatch", oracle.replace("9223372036854775808", "8223372036854775808"), arm)
        }
        assertTrue(RetainedNumber.isValid("1E+00000"))
        assertTrue(RetainedNumber.isValid("-0"))
        assertTrue(RetainedNumber.isValid("1.0"))
        assertTrue(RetainedNumber.isValid("1e0"))
    }

    private fun withDigest(prefix: String): String =
        prefix + "H\tsha256\t" + legacyAdmissionSha256(prefix.encodeToByteArray()) + "\n"

    private fun assertFault(expected: String, wire: String, arm: ProbeArm) {
        assertEquals(expected, assertFailsWith<ProbeWireFault> { RetainedNumberProbeWire.decode(wire, arm) }.code)
    }

    private data class Quad(val arm: ProbeArm, val oracle: String, val size: Int, val sha: String)
}
