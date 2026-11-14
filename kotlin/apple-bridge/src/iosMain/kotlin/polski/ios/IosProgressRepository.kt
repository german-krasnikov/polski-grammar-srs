package polski.ios

import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults
import polski.progress.BlobProgressRepository
import polski.progress.ImportResult
import polski.progress.ProgressBlobStore
import polski.srs.Scheduler

/** App-private v1 document. Invalid or newer bytes are never replaced by a fresh document. */
class IosProgressRepository(
    scheduler: Scheduler,
    defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : BlobProgressRepository(IosProgressBlobStore(defaults), scheduler, Dispatchers.Default) {
    /** Opt-in UI-test seam: forces every [save] to fail without touching decode/read paths. */
    private val forceSaveFailure: Boolean =
        NSProcessInfo.processInfo.environment["POLSKI_UITEST_FORCE_SAVE_FAILURE"] as? String == "1"

    override fun forcedSaveFailure(): Throwable? =
        if (forceSaveFailure) IllegalStateException("Forced failure for UI test") else null

    override fun recoveryMessage() = "Импортируйте совместимый JSON явно"

    override fun validateImportSize(raw: String): ImportResult.Invalid? =
        if (raw.encodeToByteArray().size > 10_000_000) ImportResult.Invalid("Файл прогресса слишком большой") else null

    /** Returns null on success; an error string leaves the previous document untouched. */
    suspend fun importJson(raw: String): String? = importRaw(raw).toLegacyMessage()
}

private class IosProgressBlobStore(private val defaults: NSUserDefaults) : ProgressBlobStore {
    private val key = "polski-progress-v1"
    private val backupKey = "polski-progress-import-backup-latest"

    override fun read(): String? = defaults.stringForKey(key)

    override fun write(raw: String) = put(key, raw)

    override fun backupThenWrite(raw: String) {
        val old = defaults.stringForKey(key)
        if (old != null) {
            // Only the most recent pre-import document is kept; earlier backups are
            // overwritten rather than accumulating one NSUserDefaults entry per import.
            put(backupKey, old)
            check(defaults.stringForKey(backupKey) == old) { "Import backup read-back mismatch" }
        }
        write(raw)
    }

    private fun put(key: String, raw: String) {
        defaults.setObject(raw, forKey = key)
        check(defaults.synchronize()) { "Progress write failed" }
    }
}
