package polski.macos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.rename
import polski.progress.DecodeResult
import polski.progress.LoadResult
import polski.progress.MigrationResult
import polski.progress.ProgressCodec
import polski.progress.ProgressDocument
import polski.progress.ProgressRepository
import polski.progress.SaveResult
import polski.srs.Scheduler

/** Reads the JVM preview's versioned document in the same Application Support directory. */
@OptIn(ExperimentalForeignApi::class)
class MacProgressRepository(
    private val directory: String,
    private val scheduler: Scheduler,
) : ProgressRepository {
    private val lock = Mutex()
    private val file = "$directory/progress-v1.json"

    override suspend fun load(): LoadResult = withContext(Dispatchers.Default) {
        lock.withLock { loadUnlocked() }
    }

    override suspend fun save(document: ProgressDocument): SaveResult = withContext(Dispatchers.Default) {
        lock.withLock {
            try {
                when (val existing = loadUnlocked()) {
                    LoadResult.Missing, is LoadResult.Loaded -> Unit
                    is LoadResult.Unavailable -> return@withLock SaveResult.WriteFailed(existing.cause)
                    else -> return@withLock SaveResult.WriteFailed(IllegalStateException("Progress requires recovery"))
                }
                writeVerified(file, ProgressCodec.encodeLegacyV1(document))
                SaveResult.Saved
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                SaveResult.WriteFailed(error)
            }
        }
    }

    /** Explicit import validates first, preserves a byte-identical backup, then atomically replaces. */
    suspend fun importJson(raw: String): String? = withContext(Dispatchers.Default) {
        lock.withLock {
            if (raw.encodeToByteArray().size > 10_000_000) return@withLock "Файл прогресса слишком большой"
            when (val decoded = ProgressCodec.decode(raw)) {
                is DecodeResult.Invalid -> return@withLock decoded.reason
                is DecodeResult.Unsupported -> return@withLock "Неподдерживаемая версия ${decoded.version}"
                is DecodeResult.Valid -> Unit
            }
            try {
                val previous = read(file)
                if (previous != null) {
                    val backup = "$directory/backups/progress-${NSUUID().UUIDString}.json"
                    writeVerified(backup, previous)
                }
                writeVerified(file, raw)
                null
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                error.message ?: "Не удалось импортировать прогресс"
            }
        }
    }

    fun recoveryRaw(): String? = runCatching { read(file) }.getOrNull()

    override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult =
        save(ProgressCodec.fresh(skillIds, at, localDay, scheduler))

    override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult =
        MigrationResult.RecoveryRequired(recoveryRaw().orEmpty(), "Импортируйте совместимый JSON явно")

    private fun loadUnlocked(): LoadResult = try {
        val raw = read(file) ?: return LoadResult.Missing
        if (raw.encodeToByteArray().size > 10_000_000) return LoadResult.Invalid(raw, "Файл прогресса слишком большой")
        when (val decoded = ProgressCodec.decode(raw)) {
            is DecodeResult.Valid -> LoadResult.Loaded(decoded.document)
            is DecodeResult.Invalid -> LoadResult.Invalid(raw, decoded.reason)
            is DecodeResult.Unsupported -> LoadResult.Unsupported(raw, decoded.version)
        }
    } catch (error: Throwable) {
        if (error is CancellationException) throw error
        LoadResult.Unavailable(error)
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
