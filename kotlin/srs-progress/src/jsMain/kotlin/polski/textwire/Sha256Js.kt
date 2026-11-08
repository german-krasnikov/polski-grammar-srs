package polski.textwire
// JS: use a pure-Kotlin SHA-256 implementation (no WebCrypto dependency in sync context)
internal actual fun sha256Platform(data: ByteArray): String = sha256Pure(data)
