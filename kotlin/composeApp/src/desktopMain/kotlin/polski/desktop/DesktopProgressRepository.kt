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
import polski.progress.BlobProgressRepository
import polski.progress.ImportResult
import polski.progress.ProgressBlobStore
import polski.srs.Scheduler

/** One local document, isolated from browser LocalStorage. Existing invalid bytes are never replaced. */
class DesktopProgressRepository(directory: Path, scheduler: Scheduler) :
    BlobProgressRepository(DesktopProgressBlobStore(directory), scheduler, Dispatchers.IO) {
    override fun recoveryMessage() = "Use the desktop JSON import action"

    /** The desktop imports a chosen file, not the browser's private storage key. */
    suspend fun importJson(raw: String): ImportResult = importRaw(raw)
}

private class DesktopProgressBlobStore(private val directory: Path) : ProgressBlobStore {
    private val progressFile = directory.resolve("progress-v1.json")

    override fun read(): String? = if (Files.exists(progressFile)) Files.readString(progressFile) else null

    override fun write(raw: String) = writeDesktopJsonAtomically(progressFile, raw)

    override fun backupThenWrite(raw: String) {
        val current = read()
        if (current != null) {
            val backup = directory.resolve("backups").resolve("progress-${UUID.randomUUID()}.json")
            writeDesktopJsonAtomically(backup, current)
            check(Files.readString(backup) == current) { "Import backup read-back mismatch" }
        }
        write(raw)
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
