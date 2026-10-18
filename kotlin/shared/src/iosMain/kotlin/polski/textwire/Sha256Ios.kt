package polski.textwire
import kotlinx.cinterop.*
import platform.CoreCrypto.*
@OptIn(ExperimentalForeignApi::class)
internal actual fun sha256Platform(data: ByteArray): String {
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    data.usePinned { pinned ->
        digest.usePinned { out ->
            CC_SHA256(pinned.addressOf(0), data.size.toUInt(), out.addressOf(0))
        }
    }
    return digest.joinToString("") { it.toString(16).padStart(2, '0') }
}
