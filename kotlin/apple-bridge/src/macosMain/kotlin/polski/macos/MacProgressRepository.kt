package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.rename
import kotlinx.coroutines.Dispatchers
import polski.progress.BlobProgressRepository
import polski.progress.ImportResult
import polski.progress.LoadResult
import polski.progress.ProgressBlobStore
import polski.srs.Scheduler

/** Reads the JVM preview's versioned document in the same Application Support directory. */
@OptIn(ExperimentalForeignApi::class)
class MacProgressRepository(
    directory: String,
    scheduler: Scheduler,
) : BlobProgressRepository(MacProgressBlobStore(directory), scheduler, Dispatchers.Default) {
    override fun recoveryMessage() = "Импортируйте совместимый JSON явно"

    override fun recoveryRawForMigration(): String = recoveryRaw().orEmpty()

    override fun validateImportSize(raw: String): ImportResult.Invalid? =
        if (raw.encodeToByteArray().size > 10_000_000) ImportResult.Invalid("Файл прогресса слишком большой") else null

    override fun validateLoadedRaw(raw: String): LoadResult.Invalid? =
        if (raw.encodeToByteArray().size > 10_000_000) LoadResult.Invalid(raw, "Файл прогресса слишком большой") else null

    fun recoveryRaw(): String? = readStored()

    /** Explicit import validates first, preserves a byte-identical backup, then atomically replaces. */
    suspend fun importJson(raw: String): String? = importRaw(raw).toLegacyMessage()
}

@OptIn(ExperimentalForeignApi::class)
private class MacProgressBlobStore(private val directory: String) : ProgressBlobStore {
    private val file = "$directory/progress-v1.json"

    override fun read(): String? = read(file)

    override fun write(raw: String) = writeVerified(file, raw)

    override fun backupThenWrite(raw: String) {
        val previous = read(file)
        if (previous != null) {
            val backup = "$directory/backups/progress-${NSUUID().UUIDString}.json"
            writeVerified(backup, previous)
        }
        write(raw)
    }

    private fun read(path: String): String? {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
            ?: error("Unreadable UTF-8 progress file: $path")
    }

    private fun writeVerified(path: String, raw: String) {
        val parent = path.substringBeforeLast('/')
        check(NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null)) {
            "Cannot create progress directory: $parent"
        }
        val temp = "$path.tmp-${NSUUID().UUIDString}"
        try {
            check((raw as NSString).writeToFile(temp, true, NSUTF8StringEncoding, null)) {
                "Cannot write progress temporary file"
            }
            check(read(temp) == raw) { "Progress temporary read-back mismatch" }
            check(rename(temp, path) == 0) { "Cannot replace progress file" }
            check(read(path) == raw) { "Progress read-back mismatch" }
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(temp, null)
        }
    }
}
