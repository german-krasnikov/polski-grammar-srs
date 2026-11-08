package polski.textwire
import java.security.MessageDigest
internal actual fun sha256Platform(data: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(data).joinToString("") { "%02x".format(it) }
