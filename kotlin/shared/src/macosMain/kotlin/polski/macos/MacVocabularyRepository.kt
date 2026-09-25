package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.rename
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyRepository

/** Separate native Mac vocabulary document, compatible with the JVM preview file. */
@OptIn(ExperimentalForeignApi::class)
class MacVocabularyRepository(private val directory: String) : VocabularyRepository {
    private val lock = Mutex()
    private val file = "$directory/vocabulary-v1.json"

    override suspend fun loadRaw(): String? = withContext(Dispatchers.Default) {
        lock.withLock { read(file) }
    }

    override suspend fun saveRaw(value: String, backupCurrent: Boolean) = withContext(Dispatchers.Default) {
        lock.withLock {
            require(value.encodeToByteArray().size <= 10_000_000) { "Словарь слишком большой" }
            VocabularyCodec.decode(value)
            val current = read(file)
            if (current != null) {
                if (backupCurrent) writeVerified("$directory/backups/vocabulary-${NSUUID().UUIDString}.json", current)
                else VocabularyCodec.decode(current)
            }
            writeVerified(file, value)
        }
    }

    private fun read(path: String): String? {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
            ?: error("Unreadable UTF-8 vocabulary file: $path")
    }

    private fun writeVerified(path: String, raw: String) {
        val parent = path.substringBeforeLast('/')
        check(NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null)) {
            "Cannot create vocabulary directory"
        }
        val temporary = "$path.tmp-${NSUUID().UUIDString}"
        try {
            check((raw as NSString).writeToFile(temporary, true, NSUTF8StringEncoding, null)) {
                "Cannot write vocabulary temporary file"
            }
            check(read(temporary) == raw) { "Vocabulary temporary read-back mismatch" }
            check(rename(temporary, path) == 0) { "Cannot replace vocabulary file" }
            check(read(path) == raw) { "Vocabulary read-back mismatch" }
        } finally { NSFileManager.defaultManager.removeItemAtPath(temporary, null) }
    }
}
