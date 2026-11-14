package polski.progress

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import polski.srs.Scheduler

/**
 * Synchronous, platform-provided byte store for a single progress document. No coroutines, no
 * mutex — [BlobProgressRepository] owns those; a platform only has to move bytes.
 */
interface ProgressBlobStore {
    fun read(): String?
    /** Copies the current stored bytes to a backup location (no-op if nothing stored yet), then writes [raw]. */
    fun backupThenWrite(raw: String)
    /** Plain write with no backup (used by save(), which never needs one — only import does). */
    fun write(raw: String)
}

/** Outcome of [BlobProgressRepository.importRaw]. Replaces the former per-platform ImportResult/DesktopImportResult/String?. */
sealed interface ImportResult {
    data object Imported : ImportResult
    data class Invalid(val reason: String) : ImportResult
    data class Unsupported(val version: Int) : ImportResult
    data class Failed(val cause: Throwable) : ImportResult
}

/**
 * Shared load/save/import control flow (decode dispatch, verified write, mutex, cancellation
 * passthrough) for a single-blob [ProgressRepository]. A platform implements only [ProgressBlobStore]
 * plus the handful of hooks below that genuinely differ between hosts (recovery message/raw,
 * oversize rejection, a forced-failure UI-test seam) — everything else is written once here.
 */
open class BlobProgressRepository(
    private val store: ProgressBlobStore,
    private val scheduler: Scheduler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ProgressRepository {
    private val lock = Mutex()

    override suspend fun load(): LoadResult = withContext(dispatcher) { lock.withLock { loadUnlocked() } }

    override suspend fun save(document: ProgressDocument): SaveResult = withContext(dispatcher) {
        lock.withLock {
            forcedSaveFailure()?.let { return@withLock SaveResult.WriteFailed(it) }
            try {
                when (val current = loadUnlocked()) {
                    LoadResult.Missing, is LoadResult.Loaded -> Unit
                    is LoadResult.Unavailable -> return@withLock SaveResult.WriteFailed(current.cause)
                    else -> return@withLock SaveResult.WriteFailed(IllegalStateException("Progress requires migration or recovery before save"))
                }
                val raw = ProgressCodec.encodeLegacyV1(document)
                store.write(raw)
                check(store.read() == raw) { "Progress read-back mismatch" }
                SaveResult.Saved
            } catch (error: Throwable) {
                error.rethrowCancellation()
                SaveResult.WriteFailed(error)
            }
        }
    }

    override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult =
        save(ProgressCodec.fresh(skillIds, at, localDay, scheduler))

    override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult =
        MigrationResult.RecoveryRequired(recoveryRawForMigration(), recoveryMessage())

    /** Shared import control flow (decode-validate, backup, write, verified read-back), exactly once. */
    protected suspend fun importRaw(raw: String): ImportResult = withContext(dispatcher) {
        lock.withLock {
            validateImportSize(raw)?.let { return@withLock it }
            when (val decoded = ProgressCodec.decode(raw)) {
                is DecodeResult.Invalid -> return@withLock ImportResult.Invalid(decoded.reason)
                is DecodeResult.Unsupported -> return@withLock ImportResult.Unsupported(decoded.version)
                is DecodeResult.Valid -> Unit
            }
            try {
                store.backupThenWrite(raw)
                check(store.read() == raw) { "Imported progress read-back mismatch" }
                ImportResult.Imported
            } catch (error: Throwable) {
                error.rethrowCancellation()
                ImportResult.Failed(error)
            }
        }
    }

    /** iOS/macOS map the shared result back to their historical `String?` (null = imported) shape. */
    protected fun ImportResult.toLegacyMessage(): String? = when (this) {
        ImportResult.Imported -> null
        is ImportResult.Invalid -> reason
        is ImportResult.Unsupported -> "Неподдерживаемая версия $version"
        is ImportResult.Failed -> cause.message ?: "Не удалось импортировать прогресс"
    }

    /** Bytes currently stored, for a platform's own recovery/debug accessor (e.g. macOS's `recoveryRaw()`). */
    protected fun readStored(): String? = store.read()

    /** The localized prompt shown when [migrateLegacy] always declines (every platform imports explicitly). */
    protected open fun recoveryMessage(): String = "Import a compatible JSON file explicitly"

    /** Raw bytes offered alongside [recoveryMessage]; only macOS surfaces its on-disk file here. */
    protected open fun recoveryRawForMigration(): String = ""

    /** Rejects an oversized import before decoding; only iOS/macOS enforce this inline. */
    protected open fun validateImportSize(raw: String): ImportResult.Invalid? = null

    /** Rejects oversized bytes already on disk; only macOS's load() enforces this today. */
    protected open fun validateLoadedRaw(raw: String): LoadResult.Invalid? = null

    /** iOS's UI-test seam: force every save() to fail without touching decode/read paths. */
    protected open fun forcedSaveFailure(): Throwable? = null

    private fun loadUnlocked(): LoadResult = try {
        val raw = store.read() ?: return LoadResult.Missing
        validateLoadedRaw(raw)?.let { return it }
        when (val decoded = ProgressCodec.decode(raw)) {
            is DecodeResult.Valid -> LoadResult.Loaded(decoded.document)
            is DecodeResult.Invalid -> LoadResult.Invalid(raw, decoded.reason)
            is DecodeResult.Unsupported -> LoadResult.Unsupported(raw, decoded.version)
        }
    } catch (error: Throwable) {
        error.rethrowCancellation()
        LoadResult.Unavailable(error)
    }

    private fun Throwable.rethrowCancellation() {
        if (this is CancellationException) throw this
    }
}
