package polski.textwire

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LegacyAdmissionDigestTest {
    @Test
    fun sha256MatchesIndependentPublicVectors() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            legacyAdmissionSha256(byteArrayOf()),
        )
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            legacyAdmissionSha256("abc".encodeToByteArray()),
        )
    }

    @Test
    fun mutatedInputOrLedgerDigestFailsBeforeOldDecoder() {
        val prefix = """{"version":1,"cards":[],"stats":{},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"unknown":""""
        val raw = prefix.dropLast(1) + "[".repeat(128) + "0" + "]".repeat(128) + "}"
        assertTrue(legacyAdmissionMatches("Progress nested recipe", raw))
        val oneByteChanged = raw.replace("0" + "]".repeat(128), "1" + "]".repeat(128))
        assertFalse(legacyAdmissionMatches("Progress nested recipe", oneByteChanged))
        val (_, expected) = legacyAdmissionLedger.getValue("Progress nested recipe")
        val oneHexChanged = (if (expected.first() == '0') "1" else "0") + expected.drop(1)
        assertFalse(legacyAdmissionSha256(raw.encodeToByteArray()) == oneHexChanged)
    }
}
