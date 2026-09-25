package dev.polski.grammarmatrix

import android.content.Context
import android.util.AtomicFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import polski.progress.DecodeResult
import polski.progress.LoadResult
import polski.progress.MigrationResult
import polski.progress.ProgressCodec
import polski.progress.ProgressDocument
import polski.progress.ProgressRepository
import polski.progress.SaveResult
import polski.srs.Scheduler

sealed interface ImportResult {
    data object Imported : ImportResult
    data class Invalid(val reason: String) : ImportResult
    data class Unsupported(val version: Int) : ImportResult
    data class Failed(val cause: Throwable) : ImportResult
}

/** Private v1 document. AtomicFile keeps the last complete bytes across interrupted writes. */
class AndroidProgressRepository(context: Context, private val scheduler: Scheduler) : ProgressRepository {
    private val lock = Mutex()
    private val directory = context.applicationContext.filesDir
    private val document = AtomicFile(File(directory, "progress-v1.json"))

    override suspend fun load(): LoadResult = withContext(Dispatchers.IO) {
        lock.withLock { loadUnlocked() }
    }

    override suspend fun save(document: ProgressDocument): SaveResult = withContext(Dispatchers.IO) {
        lock.withLock {
            try {
                when (val current = loadUnlocked()) {
                    LoadResult.Missing, is LoadResult.Loaded -> Unit
                    is LoadResult.Unavailable -> return@withLock SaveResult.WriteFailed(current.cause)
                    else -> return@withLock SaveResult.WriteFailed(IllegalStateException("Progress requires recovery"))
                }
                val raw = ProgressCodec.encodeLegacyV1(document)
                write(this@AndroidProgressRepository.document, raw)
                check(read(this@AndroidProgressRepository.document) == raw) { "Progress read-back mismatch" }
                SaveResult.Saved
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                SaveResult.WriteFailed(error)
            }
        }
    }

    suspend fun importJson(raw: String): ImportResult = withContext(Dispatchers.IO) {
        lock.withLock {
            when (val decoded = ProgressCodec.decode(raw)) {
                is DecodeResult.Invalid -> return@withLock ImportResult.Invalid(decoded.reason)
                is DecodeResult.Unsupported -> return@withLock ImportResult.Unsupported(decoded.version)
                is DecodeResult.Valid -> Unit
            }
            try {
                val old = readOrNull(document)
                if (old != null) {
                    val backup = AtomicFile(File(directory, "backups/progress-${UUID.randomUUID()}.json"))
                    write(backup, old)
                    check(read(backup) == old) { "Import backup read-back mismatch" }
                }
                write(document, raw)
                check(read(document) == raw) { "Imported progress read-back mismatch" }
                ImportResult.Imported
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                ImportResult.Failed(error)
            }
        }
    }

    override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult =
        save(ProgressCodec.fresh(skillIds, at, localDay, scheduler))

    override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult =
        MigrationResult.RecoveryRequired("", "Import a compatible JSON file explicitly")

    private fun loadUnlocked(): LoadResult = try {
        val raw = readOrNull(document) ?: return LoadResult.Missing
        when (val decoded = ProgressCodec.decode(raw)) {
            is DecodeResult.Valid -> LoadResult.Loaded(decoded.document)
            is DecodeResult.Invalid -> LoadResult.Invalid(raw, decoded.reason)
            is DecodeResult.Unsupported -> LoadResult.Unsupported(raw, decoded.version)
        }
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        LoadResult.Unavailable(error)
    }

    private fun readOrNull(file: AtomicFile): String? = try {
        read(file)
    } catch (_: FileNotFoundException) {
        null
    }

    private fun read(file: AtomicFile): String = file.openRead().use { stream ->
        readUtf8Limited(stream)
    }

    private fun write(file: AtomicFile, raw: String) {
        file.baseFile.parentFile?.mkdirs()
        val stream = file.startWrite()
        try {
            stream.write(raw.toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (error: Exception) {
            file.failWrite(stream)
            throw error
        }
    }
}

internal fun readUtf8Limited(input: InputStream): String {
    val bytes = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        require(bytes.size() + count <= 10_000_000) { "Progress file is too large" }
        bytes.write(buffer, 0, count)
    }
    return bytes.toString(Charsets.UTF_8.name())
}
