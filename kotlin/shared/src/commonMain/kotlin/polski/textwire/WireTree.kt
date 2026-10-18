package polski.textwire

/** Ordered typed tree node — the language-neutral data model for POLSKI-TEXT wire format. */
sealed class WireNode {
    data class WireMap(val children: List<Pair<String, WireNode>>) : WireNode() {
        fun get(key: String): WireNode? = children.firstOrNull { it.first == key }?.second
    }
    data class WireList(val items: List<WireNode>) : WireNode()
    data class WireString(val value: String) : WireNode()
    data class WireI64(val value: Long) : WireNode()
    data class WireF64(val bits: Long) : WireNode() // IEEE-754 binary64 bit pattern
    data class WireBool(val value: Boolean) : WireNode()
    object WireNull : WireNode()
}

/** Result of a decode operation. */
sealed class DecodeResult {
    data class Ok(val root: WireNode.WireMap, val kind: String, val schemaVersion: Int) : DecodeResult()
    data class Err(val message: String) : DecodeResult()
}

/** Result of an encode operation. */
data class EncodeResult(val bytes: ByteArray) {
    override fun equals(other: Any?) = other is EncodeResult && bytes.contentEquals(other.bytes)
    override fun hashCode() = bytes.contentHashCode()
}
