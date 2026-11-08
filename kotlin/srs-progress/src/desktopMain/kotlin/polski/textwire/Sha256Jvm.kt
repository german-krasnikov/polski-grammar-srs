package polski.textwire

import java.security.MessageDigest

internal actual fun sha256Platform(data: ByteArray): String {
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(data).joinToString("") { "%02x".format(it) }
}
