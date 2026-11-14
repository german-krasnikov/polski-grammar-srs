package dev.polski.grammarmatrix

import android.content.Context
import android.util.AtomicFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import polski.progress.BlobProgressRepository
import polski.progress.ImportResult
import polski.progress.ProgressBlobStore
import polski.srs.Scheduler

/** Private v1 document. AtomicFile keeps the last complete bytes across interrupted writes. */
class AndroidProgressRepository(context: Context, scheduler: Scheduler) :
    BlobProgressRepository(AndroidProgressBlobStore(context), scheduler, Dispatchers.IO) {
    suspend fun importJson(raw: String): ImportResult = importRaw(raw)
}

private class AndroidProgressBlobStore(context: Context) : ProgressBlobStore {
    private val directory = context.applicationContext.filesDir
    private val document = AtomicFile(File(directory, "progress-v1.json"))

    override fun read(): String? = readOrNull(document)

    override fun write(raw: String) = write(document, raw)

    override fun backupThenWrite(raw: String) {
        val old = readOrNull(document)
        if (old != null) {
            val backup = AtomicFile(File(directory, "backups/progress-${UUID.randomUUID()}.json"))
            write(backup, old)
            check(read(backup) == old) { "Import backup read-back mismatch" }
        }
        write(document, raw)
    }

    private fun readOrNull(file: AtomicFile): String? = try {
        read(file)
    } catch (_: FileNotFoundException) {
        null
    }

    private fun read(file: AtomicFile): String = file.openRead().use { stream -> readUtf8Limited(stream) }

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
