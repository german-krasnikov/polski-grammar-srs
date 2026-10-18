package polski.textwire

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Test-only candidate tree; no production document, storage API or wire selection. */
internal sealed interface ProbeValue
internal data class ProbeMap(val entries: List<Pair<String, ProbeValue>>) : ProbeValue
internal data class ProbeList(val items: List<ProbeValue>) : ProbeValue
internal data class ProbeString(val value: String) : ProbeValue
internal data class ProbeInt(val value: Long) : ProbeValue
internal data class ProbeDoubleBits(val hex: String) : ProbeValue
internal data class ProbeBool(val value: Boolean) : ProbeValue
internal data object ProbeNull : ProbeValue

/** Exact old numeric spelling, disjoint from string, signed i64 and binary64. */
internal data class RetainedNumber(val token: String) : ProbeValue {
    init { require(isValid(token)) { "RetainedNumberSyntax" } }

    companion object {
        /** Whole-scalar linear ASCII scan; no numeric conversion or exponent expansion. */
        fun isValid(token: String): Boolean {
            if (token.isEmpty()) return false
            var cursor = 0
            if (token[cursor] == '-') cursor++
            if (cursor == token.length) return false
            when (token[cursor]) {
                '0' -> cursor++
                in '1'..'9' -> {
                    cursor++
                    while (cursor < token.length && token[cursor] in '0'..'9') cursor++
                }
                else -> return false
            }
            if (cursor < token.length && token[cursor] == '.') {
                cursor++
                val first = cursor
                while (cursor < token.length && token[cursor] in '0'..'9') cursor++
                if (cursor == first) return false
            }
            if (cursor < token.length && token[cursor] in "eE") {
                cursor++
                if (cursor < token.length && token[cursor] in "+-") cursor++
                val first = cursor
                while (cursor < token.length && token[cursor] in '0'..'9') cursor++
                if (cursor == first) return false
            }
            return cursor == token.length
        }
    }
}

internal enum class ProbeArm(val header: String) {
    A("POLSKI-TEXT\t1\tprogress\t2"),
    C("POLSKI-UNITS\t1\tprogress\t2"),
}
internal class ProbeWireFault(val code: String) : IllegalArgumentException(code)

/** Mechanical A/C feasibility printer/parser; schema admission and storage are absent. */
internal object RetainedNumberProbeWire {
    fun encode(root: ProbeMap, arm: ProbeArm): String {
        val prefix = buildString {
            append(arm.header).append('\n')
            write(root, "-", arm)
        }
        return prefix + "H\tsha256\t" + legacyAdmissionSha256(prefix.encodeToByteArray()) + "\n"
    }

    fun decode(wire: String, arm: ProbeArm): ProbeMap {
        if (!wire.endsWith('\n') || '\r' in wire || wire.startsWith('\uFEFF')) fail("Framing")
        val lines = wire.split('\n').dropLast(1)
        if (lines.size < 3 || lines.first() != arm.header) fail("Header")
        val digest = lines.last().split('\t')
        if (digest.size != 3 || digest[0] != "H" || digest[1] != "sha256" ||
            digest[2].length != 64 || digest[2].any { it !in '0'..'9' && it !in 'a'..'f' }
        ) fail("DigestRow")
        val prefix = lines.dropLast(1).joinToString("\n", postfix = "\n")
        if (legacyAdmissionSha256(prefix.encodeToByteArray()) != digest[2]) fail("DigestMismatch")
        val parser = Parser(lines.subList(1, lines.lastIndex), arm)
        val root = parser.read(Position.ROOT)
        if (parser.hasMore()) fail("TrailingRow")
        if (root !is ProbeMap) fail("RootMap")
        if (encode(root, arm) != wire) fail("NonCanonical")
        return root
    }

    /** Read only from the old decoder's source tree; never invokes the old writer. */
    fun projectOldUnknown(element: JsonElement): ProbeValue = when (element) {
        is JsonObject -> ProbeMap(element.entries.map { (key, value) -> key to projectOldUnknown(value) })
        is JsonArray -> ProbeList(element.map(::projectOldUnknown))
        JsonNull -> ProbeNull
        is JsonPrimitive -> when {
            element.isString -> ProbeString(element.content)
            element.content == "true" -> ProbeBool(true)
            element.content == "false" -> ProbeBool(false)
            else -> RetainedNumber(element.content)
        }
    }

    private fun StringBuilder.write(value: ProbeValue, key: String, arm: ProbeArm) {
        when (value) {
            is ProbeMap -> {
                append("M\t").append(key).append('\t').append(value.entries.size).append('\n')
                value.entries.forEach { (name, child) -> write(child, encodeKey(name, arm), arm) }
            }
            is ProbeList -> {
                append("L\t").append(key).append('\t').append(value.items.size).append('\n')
                value.items.forEach { write(it, "-", arm) }
            }
            is ProbeString -> append("S\t").append(key).append('\t').append(encodeText(value.value, arm)).append('\n')
            is ProbeInt -> append("I\t").append(key).append('\t').append(value.value).append('\n')
            is ProbeDoubleBits -> append("D\t").append(key).append('\t').append(value.hex).append('\n')
            is ProbeBool -> append("B\t").append(key).append('\t').append(if (value.value) '1' else '0').append('\n')
            ProbeNull -> append("N\t").append(key).append('\n')
            is RetainedNumber -> append("R\t").append(key).append('\t').append(value.token).append('\n')
        }
    }

    private fun encodeKey(key: String, arm: ProbeArm): String = when (arm) {
        ProbeArm.A -> "K:" + escape(key)
        ProbeArm.C -> "K" + units(key)
    }
    private fun encodeText(value: String, arm: ProbeArm): String = when (arm) {
        ProbeArm.A -> escape(value)
        ProbeArm.C -> units(value)
    }
    private fun units(value: String): String = buildString {
        append('U').append(value.length).append(':')
        value.forEach { append(it.code.toString(16).padStart(4, '0').uppercase()) }
    }
    private fun escape(value: String): String = buildString {
        for (index in value.indices) {
            val unit = value[index]
            when {
                unit == '\\' -> append("\\\\")
                unit == '\t' -> append("\\t")
                unit == '\n' -> append("\\n")
                unit == '\r' -> append("\\r")
                unit.code in 0..0x1f || unit.code in 0x7f..0x9f ||
                    (unit.isHighSurrogate() && (index + 1 == value.length || !value[index + 1].isLowSurrogate())) ||
                    (unit.isLowSurrogate() && (index == 0 || !value[index - 1].isHighSurrogate())) ->
                    append("\\u").append(unit.code.toString(16).padStart(4, '0').uppercase())
                else -> append(unit)
            }
        }
    }

    private enum class Position { ROOT, MAP_CHILD, LIST_ITEM }
    private class Parser(private val lines: List<String>, private val arm: ProbeArm) {
        private var cursor = 0
        fun hasMore(): Boolean = cursor != lines.size

        fun read(position: Position): ProbeValue {
            if (cursor == lines.size) fail("Truncated")
            val columns = lines[cursor++].split('\t')
            if (columns.size < 2) fail("RowArity")
            val tag = columns[0]
            val key = columns[1]
            when (position) {
                Position.ROOT -> if (key != "-") fail("RootKey")
                Position.LIST_ITEM -> if (key != "-") fail("ListItemKey")
                Position.MAP_CHILD -> {
                    if (key == "-") fail("MapKey")
                    val name = decodeKey(key, arm)
                    if (encodeKey(name, arm) != key) fail("NonCanonicalKey")
                }
            }
            if (columns.size != (if (tag == "N") 2 else 3)) fail("RowArity")
            val payload = columns.getOrNull(2)
            return when (tag) {
                "M" -> {
                    val count = count(payload!!)
                    val entries = ArrayList<Pair<String, ProbeValue>>(minOf(count, lines.size - cursor))
                    val seen = HashSet<String>()
                    repeat(count) {
                        val line = lines.getOrNull(cursor) ?: fail("Truncated")
                        val keyColumn = line.split('\t').getOrNull(1) ?: fail("RowArity")
                        val name = decodeKey(keyColumn, arm)
                        if (!seen.add(name)) fail("DuplicateKey")
                        entries += name to read(Position.MAP_CHILD)
                    }
                    ProbeMap(entries)
                }
                "L" -> ProbeList(List(count(payload!!)) { read(Position.LIST_ITEM) })
                "S" -> ProbeString(decodeText(payload!!, arm))
                "I" -> ProbeInt(payload!!.toLongOrNull()?.takeIf { it.toString() == payload } ?: fail("IntSyntax"))
                "D" -> {
                    if (payload!!.length != 16 || payload.any { it !in '0'..'9' && it !in 'a'..'f' } ||
                        !Double.fromBits(payload.toULong(16).toLong()).isFinite()) fail("DoubleBits")
                    ProbeDoubleBits(payload)
                }
                "B" -> ProbeBool(when (payload) { "0" -> false; "1" -> true; else -> fail("BoolSyntax") })
                "N" -> ProbeNull
                "R" -> {
                    if (!RetainedNumber.isValid(payload!!)) fail("RetainedNumberSyntax")
                    RetainedNumber(payload)
                }
                else -> fail("UnknownTag")
            }
        }

        private fun count(payload: String): Int {
            val count = payload.toIntOrNull()?.takeIf { it >= 0 && it.toString() == payload } ?: fail("CountSyntax")
            if (count > lines.size - cursor) fail("Truncated")
            return count
        }
    }

    private fun decodeKey(value: String, arm: ProbeArm): String = when (arm) {
        ProbeArm.A -> if (value.startsWith("K:")) unescape(value.drop(2)) else fail("MapKey")
        ProbeArm.C -> if (value.startsWith("KU")) decodeUnits(value.drop(1)) else fail("MapKey")
    }
    private fun decodeText(value: String, arm: ProbeArm): String = when (arm) {
        ProbeArm.A -> unescape(value)
        ProbeArm.C -> decodeUnits(value)
    }
    private fun decodeUnits(value: String): String {
        if (!value.startsWith('U')) fail("Units")
        val delimiter = value.indexOf(':')
        if (delimiter < 2) fail("Units")
        val count = value.substring(1, delimiter).toIntOrNull() ?: fail("Units")
        val hex = value.substring(delimiter + 1)
        if (count < 0 || count > hex.length / 4 || hex.length != count * 4 ||
            hex.any { it !in '0'..'9' && it !in 'A'..'F' }) fail("Units")
        return buildString(count) {
            for (index in 0 until count) append(hex.substring(index * 4, index * 4 + 4).toInt(16).toChar())
        }
    }
    private fun unescape(value: String): String = buildString {
        var cursor = 0
        while (cursor < value.length) {
            val unit = value[cursor++]
            if (unit != '\\') { append(unit); continue }
            if (cursor == value.length) fail("Escape")
            when (value[cursor++]) {
                '\\' -> append('\\')
                't' -> append('\t')
                'n' -> append('\n')
                'r' -> append('\r')
                'u' -> {
                    if (value.length - cursor < 4) fail("Escape")
                    val hex = value.substring(cursor, cursor + 4)
                    if (hex.any { it !in '0'..'9' && it !in 'A'..'F' }) fail("Escape")
                    append(hex.toInt(16).toChar())
                    cursor += 4
                }
                else -> fail("Escape")
            }
        }
    }
    private fun fail(code: String): Nothing = throw ProbeWireFault(code)
}
