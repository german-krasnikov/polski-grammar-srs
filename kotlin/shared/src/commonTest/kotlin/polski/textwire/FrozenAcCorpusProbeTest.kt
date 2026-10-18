package polski.textwire

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Only a test-source A/C feasibility comparison; the corpus owns the expected bytes. */
class FrozenAcCorpusProbeTest {
    private data class Example(
        val id: String,
        val tree: ProbeMap,
        val frame: ProbeFrame = ProbeFrame.Progress,
    )

    private val high = 0xd800.toChar().toString()
    private val low = 0xdc00.toChar().toString()
    private val pair = high + low
    private val privateUse = 0xe000.toChar().toString()

    private val examples = listOf(
        Example("P01", map()),
        Example("P02", map(
            "emptyText" to string(""), "nothing" to ProbeNull,
            "emptyList" to list(), "emptyMap" to map(), "zero" to int(0),
        )),
        Example("P03", map(
            "-" to string("dash"), "" to string("empty"), "K:-" to string("prefixed"),
        )),
        Example("P04", map(
            "z" to list(
                map("id" to string("a"), "x-unknown" to ProbeBool(true)),
                map("id" to string("b")),
            ),
            "a" to map("inner" to list(ProbeNull, string("1"))),
        )),
        Example("P05", map(
            "min" to int(Long.MIN_VALUE), "max" to int(Long.MAX_VALUE),
            "numericText" to string("001e+3"), "negativeZeroText" to string("-0"),
        )),
        Example("P06", map(
            "positiveZero" to bits("0000000000000000"),
            "negativeZero" to bits("8000000000000000"),
            "one" to bits("3ff0000000000000"),
            "smallestSubnormal" to bits("0000000000000001"),
            "largestFinite" to bits("7fefffffffffffff"),
        )),
        Example("P07", map(
            "text" to string("Zażółć gęślą jaźń / Привет / 漢字 / 😀\tline\nnext\\end\u0000\u001F\u007F\u0085\u2028\u2029"),
        )),
        Example("P08", map(
            "A" to string("ASCII"), high to string("lone high"),
            pair to string("U+10000"), low to string("lone low"),
            privateUse to string("BMP"),
        )),
        Example("P09", map(
            privateUse to string("first"), "A" to string("second"), pair to string("third"),
        )),
        Example("P10", map("value" to string("$high|$low|😀"))),
        Example("P11", map("yes" to ProbeBool(true), "no" to ProbeBool(false))),
        Example("P12", map(
            "revision" to int(1), "effectId" to string("download-1"),
        ), ProbeFrame.Snapshot),
    )

    @Test
    fun p01MatchesFrozenAAndCBytesAndOrderedTree() = compareCase("P01")

    @Test
    fun p02MatchesFrozenAAndCBytesAndOrderedTree() = compareCase("P02")

    @Test
    fun p03MatchesFrozenAAndCBytesAndOrderedTree() = compareCase("P03")

    @Test
    fun p04MatchesFrozenAAndCBytesAndOrderedTree() = compareCase("P04")

    @Test
    fun p05MatchesFrozenAAndCBytesAndOrderedTree() = compareCase("P05")

    @Test
    fun p06MatchesFrozenAAndCBytesAndOrderedTree() = compareCase("P06")

    @Test
    fun p07ToP12MatchFrozenAAndCBytesAndOrderedTrees() = compare(examples.drop(6))

    @Test
    fun n16RejectsFrozenNamedListItemInBothArms() {
        for (arm in ProbeArm.entries) {
            val wire = frozenInput(frozenAcNegativeCorpus, "N16", arm)
            assertEquals("ListItemKey", assertFailsWith<ProbeWireFault> {
                RetainedNumberProbeWire.decode(wire, arm)
            }.code, "N16 $arm")
        }
    }

    private fun compareCase(id: String) = compare(listOf(examples.single { it.id == id }))

    private fun compare(selected: List<Example>) {
        assertEquals(12, examples.map { it.id }.toSet().size)
        for (example in selected) for (arm in ProbeArm.entries) {
            val frozen = frozenInput(frozenAcPositiveCorpus, example.id, arm)
            val encoded = RetainedNumberProbeWire.encode(example.tree, arm, example.frame)
            assertEquals(frozen, encoded, "${example.id} $arm exact frozen bytes")
            val decoded = RetainedNumberProbeWire.decode(frozen, arm, example.frame)
            assertEquals(example.tree, decoded, "${example.id} $arm ordered typed tree")
            assertEquals(frozen, RetainedNumberProbeWire.encode(decoded, arm, example.frame))
            if (example.frame == ProbeFrame.Snapshot) {
                assertEquals("Header", assertFailsWith<ProbeWireFault> {
                    RetainedNumberProbeWire.decode(frozen, arm, ProbeFrame.Progress)
                }.code, "${example.id} $arm wrong frame")
            }
        }
    }

    private fun frozenInput(corpus: String, id: String, arm: ProbeArm): String {
        val lines = corpus.split('\n')
        val starts = lines.indices.filter { lines[it].startsWith("CASE $id ") }
        require(starts.size == 1) { "Frozen case $id missing or duplicated" }
        val end = (starts.single() + 1 until lines.size).firstOrNull { lines[it] == "END" }
            ?: error("Frozen case $id has no END")
        val section = lines.subList(starts.single() + 1, end)
        val markers = section.indices.filter { section[it].startsWith("input ${arm.name} utf8-lines") }
        require(markers.size == 1) { "Frozen $id $arm input missing or duplicated" }
        val begin = markers.single()
        val next = (begin + 1 until section.size).firstOrNull {
            section[it].startsWith("input ") || section[it].startsWith("expect ")
        } ?: error("Frozen $id $arm input lacks terminator")
        val rows = section.subList(begin + 1, next)
        require(rows.isNotEmpty())
        val terminalHt = section[begin] == "input A utf8-lines-terminal-ht"
        if (terminalHt) require(id == "P02" && arm == ProbeArm.A)
        val marked = rows.count { it.endsWith("<HT>") }
        require(marked == if (terminalHt) 1 else 0) { "Frozen $id $arm HT marker changed" }
        return rows.joinToString("\n", postfix = "\n") { row ->
            if (row.endsWith("<HT>")) row.removeSuffix("<HT>") + '\t' else row
        }
    }

    private companion object {
        fun map(vararg entries: Pair<String, ProbeValue>) = ProbeMap(entries.toList())
        fun list(vararg items: ProbeValue) = ProbeList(items.toList())
        fun string(value: String) = ProbeString(value)
        fun int(value: Long) = ProbeInt(value)
        fun bits(hex: String) = ProbeDoubleBits(hex)
    }
}
