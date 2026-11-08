package polski.textwire

/**
 * POLSKI-TEXT arm A codec. Strict UTF-8/LF framing, canonical escaping, trailing SHA-256.
 * Mechanical native utility (P4). No document policy, FSRS or domain logic here.
 * Spec: Plans/Biome/TextWireDesign.md.
 */
object PolskiTextCodec {

    private const val HEADER = "POLSKI-TEXT"
    private const val FRAMING_VERSION = "1"
    private const val TAB = '\t'
    private const val LF = '\n'

    // ---------------------------------------------------------------------------
    // Encode
    // ---------------------------------------------------------------------------

    fun encode(root: WireNode.WireMap, kind: String, schemaVersion: Int): EncodeResult {
        require(kind.isNotEmpty()) { "kind must not be empty" }
        val sb = StringBuilder()
        sb.append(HEADER).append(TAB).append(FRAMING_VERSION).append(TAB)
            .append(kind).append(TAB).append(schemaVersion).append(LF)
        encodeNode(sb, "-", root)
        val bodyBytes = sb.toString().encodeToByteArray()
        val digest = sha256Hex(bodyBytes)
        val full = bodyBytes + "H\tsha256\t$digest\n".encodeToByteArray()
        return EncodeResult(full)
    }

    private fun encodeNode(sb: StringBuilder, key: String, node: WireNode) {
        when (node) {
            is WireNode.WireMap -> {
                sb.append('M').append(TAB).append(key).append(TAB).append(node.children.size).append(LF)
                for ((k, v) in node.children) encodeNode(sb, encodeKey(k), v)
            }
            is WireNode.WireList -> {
                sb.append('L').append(TAB).append(key).append(TAB).append(node.items.size).append(LF)
                for (item in node.items) encodeNode(sb, "-", item)
            }
            is WireNode.WireString -> {
                sb.append('S').append(TAB).append(key).append(TAB).append(escapeText(node.value)).append(LF)
            }
            is WireNode.WireI64 -> {
                sb.append('I').append(TAB).append(key).append(TAB).append(node.value).append(LF)
            }
            is WireNode.WireF64 -> {
                val d = Double.fromBits(node.bits)
                require(d.isFinite()) { "NaN/Infinity not allowed in wire" }
                sb.append('D').append(TAB).append(key).append(TAB)
                    .append(node.bits.toULong().toString(16).padStart(16, '0')).append(LF)
            }
            is WireNode.WireBool -> {
                sb.append('B').append(TAB).append(key).append(TAB)
                    .append(if (node.value) '1' else '0').append(LF)
            }
            WireNode.WireNull -> {
                sb.append('N').append(TAB).append(key).append(LF)
            }
        }
    }

    /** Named child key encoding: prepend "K:" and escape the key text. */
    private fun encodeKey(key: String): String = "K:" + escapeText(key)

    /** Escape: raw Unicode scalars except backslash and C0/C1/DEL → \uXXXX; \t \n \r \\ shortcuts. */
    fun escapeText(s: String): String {
        val sb = StringBuilder(s.length)
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c == '\\' -> sb.append("\\\\")
                c == '\t' -> sb.append("\\t")
                c == '\n' -> sb.append("\\n")
                c == '\r' -> sb.append("\\r")
                c.code in 0x00..0x1F || c.code in 0x7F..0x9F
                    || c.code == 0x2028 || c.code == 0x2029 -> {
                    sb.append("\\u").append(c.code.toString(16).uppercase().padStart(4, '0'))
                }
                else -> sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    // ---------------------------------------------------------------------------
    // Decode
    // ---------------------------------------------------------------------------

    fun decode(bytes: ByteArray): DecodeResult {
        // Validate UTF-8
        val text = try {
            bytes.decodeToString()
        } catch (e: Exception) {
            return DecodeResult.Err("invalid UTF-8")
        }
        if (text.contains('\r')) return DecodeResult.Err("CRLF not allowed")
        if (text.contains('\u0000')) return DecodeResult.Err("BOM or NUL not allowed")
        if (text.startsWith("\uFEFF")) return DecodeResult.Err("BOM not allowed")

        val lines = text.split(LF)
        // Last element after final LF should be empty
        if (lines.isEmpty() || lines.last() != "") return DecodeResult.Err("file must end with LF")
        val nonEmpty = lines.dropLast(1) // remove trailing empty

        if (nonEmpty.size < 3) return DecodeResult.Err("too few lines")

        // Parse header
        val header = nonEmpty[0].split(TAB)
        if (header.size != 4) return DecodeResult.Err("malformed header")
        if (header[0] != HEADER) return DecodeResult.Err("wrong header: ${header[0]}")
        if (header[1] != FRAMING_VERSION) return DecodeResult.Err("unsupported framing version: ${header[1]}")
        val kind = header[2]
        val schemaVersion = header[3].toIntOrNull() ?: return DecodeResult.Err("bad schemaVersion")

        // Last line: digest
        val digestLine = nonEmpty.last().split(TAB)
        if (digestLine.size != 3 || digestLine[0] != "H" || digestLine[1] != "sha256")
            return DecodeResult.Err("missing or malformed digest line")
        val expectedDigest = digestLine[2]
        if (!expectedDigest.matches(Regex("[0-9a-f]{64}")))
            return DecodeResult.Err("malformed digest hex")

        // Compute digest over body (everything before the H line including its preceding LF)
        val hLineStart = text.lastIndexOf("\nH\t")
        if (hLineStart < 0) return DecodeResult.Err("missing digest line")
        // Convert char index to byte index for multi-byte UTF-8 strings
        val bodyByteLen = text.substring(0, hLineStart + 1).encodeToByteArray().size
        val bodyBytes = bytes.copyOf(bodyByteLen)
        val actualDigest = sha256Hex(bodyBytes)
        if (actualDigest != expectedDigest) return DecodeResult.Err("digest mismatch")

        // Parse body rows (between header and digest line)
        val rows = nonEmpty.subList(1, nonEmpty.size - 1)
        val ctx = ParseContext(rows, 0)
        val rootNode = parseNode(ctx, "-") as? WireNode.WireMap
            ?: return DecodeResult.Err("root must be a map")
        if (ctx.pos != rows.size) return DecodeResult.Err("trailing rows after root")

        return DecodeResult.Ok(rootNode, kind, schemaVersion)
    }

    private class ParseContext(val rows: List<String>, var pos: Int)

    private fun parseNode(ctx: ParseContext, expectedKey: String): WireNode? {
        if (ctx.pos >= ctx.rows.size) return null
        val row = ctx.rows[ctx.pos]
        val cols = row.split(TAB)
        if (cols.isEmpty()) return null
        val tag = cols[0]
        val key = cols.getOrNull(1) ?: return null

        // Validate key matches expected position
        val decodedKey = decodeKey(key) ?: return null
        if (decodedKey != expectedKey) return null
        ctx.pos++

        return when (tag) {
            "M" -> {
                val count = cols.getOrNull(2)?.toIntOrNull() ?: return null
                if (count < 0) return null
                val children = mutableListOf<Pair<String, WireNode>>()
                val seenKeys = mutableSetOf<String>()
                repeat(count) {
                    val childRow = if (ctx.pos < ctx.rows.size) ctx.rows[ctx.pos] else return null
                    val childCols = childRow.split(TAB)
                    val childRawKey = childCols.getOrNull(1) ?: return null
                    if (!childRawKey.startsWith("K:")) return null // named child must have K: prefix
                    val childKey = unescapeText(childRawKey.removePrefix("K:")) ?: return null
                    if (!seenKeys.add(childKey)) return null // duplicate key
                    val child = parseNode(ctx, childKey) ?: return null
                    children.add(childKey to child)
                }
                WireNode.WireMap(children)
            }
            "L" -> {
                val count = cols.getOrNull(2)?.toIntOrNull() ?: return null
                if (count < 0) return null
                val items = mutableListOf<WireNode>()
                repeat(count) {
                    val item = parseNode(ctx, "-") ?: return null
                    items.add(item)
                }
                WireNode.WireList(items)
            }
            "S" -> {
                val escaped = cols.getOrNull(2) ?: "" // empty string = absent third col = empty value
                // Handle terminal HT (the test uses <HT> notation; actual file has raw tab then LF)
                val rawEscaped = if (cols.size > 2) cols.subList(2, cols.size).joinToString("\t") else ""
                WireNode.WireString(unescapeText(rawEscaped) ?: return null)
            }
            "I" -> {
                val raw = cols.getOrNull(2) ?: return null
                val v = raw.toLongOrNull() ?: return null
                // Reject noncanonical: leading zeros, -0
                if (raw != v.toString()) return null
                WireNode.WireI64(v)
            }
            "D" -> {
                val raw = cols.getOrNull(2) ?: return null
                if (!raw.matches(Regex("[0-9a-f]{16}"))) return null
                val bits = raw.toULong(16).toLong()
                val d = Double.fromBits(bits)
                if (!d.isFinite()) return null
                WireNode.WireF64(bits)
            }
            "B" -> {
                when (cols.getOrNull(2)) {
                    "1" -> WireNode.WireBool(true)
                    "0" -> WireNode.WireBool(false)
                    else -> null
                }
            }
            "N" -> WireNode.WireNull
            else -> null
        }
    }

    private fun decodeKey(raw: String): String? = when {
        raw == "-" -> "-"
        raw.startsWith("K:") -> unescapeText(raw.removePrefix("K:"))
        else -> null
    }

    fun unescapeText(s: String): String? {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\') {
                if (i + 1 >= s.length) return null
                when (s[i + 1]) {
                    '\\' -> { sb.append('\\'); i += 2 }
                    't'  -> { sb.append('\t'); i += 2 }
                    'n'  -> { sb.append('\n'); i += 2 }
                    'r'  -> { sb.append('\r'); i += 2 }
                    'u'  -> {
                        if (i + 5 >= s.length) return null
                        val hex = s.substring(i + 2, i + 6)
                        if (!hex.matches(Regex("[0-9A-F]{4}"))) return null // must be uppercase
                        val cp = hex.toInt(16)
                        // Reject canonical shortcuts: \t \n \r \\ must use short form
                        if (cp in setOf(0x09, 0x0A, 0x0D, 0x5C)) return null
                        sb.append(cp.toChar())
                        i += 6
                    }
                    else -> return null // unknown escape
                }
            } else {
                val c = s[i]
                // Raw C0/C1/DEL must be escaped
                if (c.code in 0x00..0x1F || c.code in 0x7F..0x9F) return null
                sb.append(c); i++
            }
        }
        return sb.toString()
    }

    // ---------------------------------------------------------------------------
    // SHA-256 (platform-provided via expect/actual or pure Kotlin)
    // ---------------------------------------------------------------------------

    internal fun sha256Hex(data: ByteArray): String = sha256Platform(data)
}

/** Platform-specific SHA-256 implementation. */
internal expect fun sha256Platform(data: ByteArray): String
