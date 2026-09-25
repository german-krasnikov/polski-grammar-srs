package polski.desktop

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.channels.FileChannel
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import polski.progress.DecodeResult
import polski.progress.LoadResult
import polski.progress.MigrationResult
import polski.progress.ProgressCodec
import polski.progress.ProgressDocument
import polski.progress.ProgressRepository
import polski.progress.SaveResult
import polski.srs.Scheduler

sealed interface DesktopImportResult {
    data object Imported : DesktopImportResult
    data class Invalid(val reason: String) : DesktopImportResult
    data class Unsupported(val version: Int) : DesktopImportResult
    data class Failed(val cause: Throwable) : DesktopImportResult
}

/** One local document, isolated from browser LocalStorage. Existing invalid bytes are never replaced. */
class DesktopProgressRepository(
    private val directory: Path,
    private val scheduler: Scheduler,
) : ProgressRepository {
    private val mutex = Mutex()
    private val progressFile = directory.resolve("progress-v1.json")

    override suspend fun load(): LoadResult = withContext(Dispatchers.IO) {
        mutex.withLock { loadUnlocked() }
    }

    override suspend fun save(document: ProgressDocument): SaveResult = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val current = loadUnlocked()
                if (current !is LoadResult.Missing && current !is LoadResult.Loaded) {
                    return@withLock SaveResult.WriteFailed(IllegalStateException("Existing desktop progress needs recovery"))
                }
                val raw = ProgressCodec.encodeLegacyV1(document)
                writeDesktopJsonAtomically(progressFile, raw)
                check(Files.readString(progressFile) == raw) { "Desktop progress read-back mismatch" }
                SaveResult.Saved
            } catch (error: Exception) {
                SaveResult.WriteFailed(error)
            }
        }
    }

    /** The desktop imports a chosen file, not the browser's private storage key. */
    suspend fun importJson(raw: String): DesktopImportResult = withContext(Dispatchers.IO) {
        mutex.withLock {
            when (val decoded = ProgressCodec.decode(raw)) {
                is DecodeResult.Invalid -> return@withLock DesktopImportResult.Invalid(decoded.reason)
                is DecodeResult.Unsupported -> return@withLock DesktopImportResult.Unsupported(decoded.version)
                is DecodeResult.Valid -> Unit
            }
            try {
                val current = if (Files.exists(progressFile)) Files.readString(progressFile) else null
                if (current != null) {
                    val backup = directory.resolve("backups").resolve("progress-${UUID.randomUUID()}.json")
                    writeDesktopJsonAtomically(backup, current)
                    check(Files.readString(backup) == current) { "Import backup read-back mismatch" }
                }
                writeDesktopJsonAtomically(progressFile, raw)
                check(Files.readString(progressFile) == raw) { "Imported progress read-back mismatch" }
                DesktopImportResult.Imported
            } catch (error: Exception) {
                DesktopImportResult.Failed(error)
            }
        }
    }

    override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult =
        MigrationResult.RecoveryRequired("", "Use the desktop JSON import action")

    override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult =
        save(ProgressCodec.fresh(skillIds, at, localDay, scheduler))

    private fun loadUnlocked(): LoadResult = try {
        if (!Files.exists(progressFile)) return LoadResult.Missing
        val raw = Files.readString(progressFile)
        when (val decoded = ProgressCodec.decode(raw)) {
            is DecodeResult.Valid -> LoadResult.Loaded(decoded.document)
            is DecodeResult.Invalid -> LoadResult.Invalid(raw, decoded.reason)
            is DecodeResult.Unsupported -> LoadResult.Unsupported(raw, decoded.version)
        }
    } catch (error: Exception) {
        LoadResult.Unavailable(error)
    }

}

/** Durable replacement on the same filesystem; preserve the old target if writing the temp file fails. */
internal fun writeDesktopJsonAtomically(target: Path, raw: String) {
    Files.createDirectories(target.parent)
    val temporary = Files.createTempFile(target.parent, ".polski-", ".tmp")
    try {
        FileChannel.open(temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use { channel ->
            val bytes = ByteBuffer.wrap(raw.toByteArray(StandardCharsets.UTF_8))
            while (bytes.hasRemaining()) channel.write(bytes)
            channel.force(true)
        }
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        Files.deleteIfExists(temporary)
    }
}
