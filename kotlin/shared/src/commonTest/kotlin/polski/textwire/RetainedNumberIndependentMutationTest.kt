package polski.textwire

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Independent structural corruption controls for the test-only A/C reader. */
class RetainedNumberIndependentMutationTest {
    @Test
    fun recomputedDigestDoesNotHideInvalidTokenOrKeyPosition() {
        val tree = ProbeMap(listOf(
            "wide" to RetainedNumber("1e0"),
            "items" to ProbeList(listOf(RetainedNumber("-0"))),
        ))
        for (arm in ProbeArm.entries) {
            val original = RetainedNumberProbeWire.encode(tree, arm)
            assertEquals(tree, RetainedNumberProbeWire.decode(original, arm))
            val prefix = original.substringBefore("H\tsha256\t")
            val namedKey = when (arm) {
                ProbeArm.A -> "K:wide"
                ProbeArm.C -> "KU4:0077006900640065"
            }
            val namedRow = "R\t$namedKey\t1e0\n"
            val itemRow = "R\t-\t-0\n"
            assertEquals(1, prefix.windowed(namedRow.length).count { it == namedRow })
            assertEquals(1, prefix.windowed(itemRow.length).count { it == itemRow })

            assertFault(
                "RetainedNumberSyntax",
                sealed(prefix.replace(namedRow, "R\t$namedKey\t+1\n")),
                arm,
            )
            assertFault(
                "DigestMismatch",
                original.replace(namedRow, "R\t$namedKey\t2e0\n"),
                arm,
            )
            assertFault(
                "MapKey",
                sealed(prefix.replace(namedRow, "R\t-\t1e0\n")),
                arm,
            )
            assertFault(
                "ListItemKey",
                sealed(prefix.replace(itemRow, "R\t$namedKey\t-0\n")),
                arm,
            )
        }
    }

    private fun sealed(prefix: String): String =
        prefix + "H\tsha256\t" + legacyAdmissionSha256(prefix.encodeToByteArray()) + "\n"

    private fun assertFault(code: String, wire: String, arm: ProbeArm) {
        assertEquals(code, assertFailsWith<ProbeWireFault> {
            RetainedNumberProbeWire.decode(wire, arm)
        }.code)
    }
}
