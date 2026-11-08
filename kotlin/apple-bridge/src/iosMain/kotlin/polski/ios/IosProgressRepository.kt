package polski.ios

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults
import polski.progress.DecodeResult
import polski.progress.LoadResult
import polski.progress.MigrationResult
import polski.progress.ProgressCodec
import polski.progress.ProgressDocument
import polski.progress.ProgressRepository
import polski.progress.SaveResult
import polski.srs.Scheduler

/** App-private v1 document. Invalid or newer bytes are never replaced by a fresh document. */
class IosProgressRepository(
    private val scheduler: Scheduler,
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : ProgressRepository {
    private val lock = Mutex()
    private val key = "polski-progress-v1"
    private val backupKey = "polski-progress-import-backup-latest"

    /** Opt-in UI-test seam: forces every [save] to fail without touching decode/read paths. */
    private val forceSaveFailure: Boolean =
        NSProcessInfo.processInfo.environment["POLSKI_UITEST_FORCE_SAVE_FAILURE"] as? String == "1"

    override suspend fun load(): LoadResult = withContext(Dispatchers.Default) {
        lock.withLock { loadUnlocked() }
    }

    override suspend fun save(document: ProgressDocument): SaveResult = withContext(Dispatchers.Default) {
        lock.withLock {
            if (forceSaveFailure) return@withLock SaveResult.WriteFailed(IllegalStateException("Forced failure for UI test"))
            try {
                when (val existing = loadUnlocked()) {
                    LoadResult.Missing, is LoadResult.Loaded -> Unit
                    is LoadResult.Unavailable -> return@withLock SaveResult.WriteFailed(existing.cause)
                    else -> return@withLock SaveResult.WriteFailed(IllegalStateException("Progress requires recovery"))
                }
                val raw = ProgressCodec.encodeLegacyV1(document)
                put(key, raw)
                check(defaults.stringForKey(key) == raw) { "Progress read-back mismatch" }
                SaveResult.Saved
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                SaveResult.WriteFailed(error)
            }
        }
    }

    /** Returns null on success; an error string leaves the previous document untouched. */
    suspend fun importJson(raw: String): String? = withContext(Dispatchers.Default) {
        lock.withLock {
            if (raw.encodeToByteArray().size > 10_000_000) return@withLock "Файл прогресса слишком большой"
            when (val decoded = ProgressCodec.decode(raw)) {
                is DecodeResult.Invalid -> return@withLock decoded.reason
                is DecodeResult.Unsupported -> return@withLock "Неподдерживаемая версия ${decoded.version}"
                is DecodeResult.Valid -> Unit
            }
            try {
                val old = defaults.stringForKey(key)
                if (old != null) {
                    // Only the most recent pre-import document is kept; earlier backups are
                    // overwritten rather than accumulating one NSUserDefaults entry per import.
                    put(backupKey, old)
                    check(defaults.stringForKey(backupKey) == old) { "Import backup read-back mismatch" }
                }
                put(key, raw)
                check(defaults.stringForKey(key) == raw) { "Imported progress read-back mismatch" }
                null
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                error.message ?: "Не удалось импортировать прогресс"
            }
        }
    }

    override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult =
        save(ProgressCodec.fresh(skillIds, at, localDay, scheduler))

    override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult =
        MigrationResult.RecoveryRequired("", "Импортируйте совместимый JSON явно")

    private fun loadUnlocked(): LoadResult = try {
        val raw = defaults.stringForKey(key) ?: return LoadResult.Missing
        when (val decoded = ProgressCodec.decode(raw)) {
            is DecodeResult.Valid -> LoadResult.Loaded(decoded.document)
            is DecodeResult.Invalid -> LoadResult.Invalid(raw, decoded.reason)
            is DecodeResult.Unsupported -> LoadResult.Unsupported(raw, decoded.version)
        }
    } catch (error: Throwable) {
        if (error is CancellationException) throw error
        LoadResult.Unavailable(error)
    }

    private fun put(key: String, raw: String) {
        defaults.setObject(raw, forKey = key)
        check(defaults.synchronize()) { "Progress write failed" }
    }
}

