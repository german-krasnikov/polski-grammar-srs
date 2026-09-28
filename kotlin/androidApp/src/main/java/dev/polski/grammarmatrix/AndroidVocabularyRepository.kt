package dev.polski.grammarmatrix

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import polski.data.activeCoursePackId
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyRepository

/** Vocabulary lives beside, and never overwrites, the grammar progress document. */
class AndroidVocabularyRepository(context: Context) : VocabularyRepository {
    private val directory = context.applicationContext.filesDir
    // EnRuAcceptance-2026-09-28.md §7 item 2: pl-ru keeps this exact, already-shipped filename;
    // any other active pack gets its own pack-namespaced file — read fresh (this repository is
    // reconstructed on every cold restart, `AndroidSessionViewModel`'s own EN-22 switch
    // mechanism, but the active pack must still pick the right file on that very first read).
    private val document: AtomicFile get() = activeCoursePackId.let { pairId ->
        AtomicFile(File(directory, if (pairId == "pl-ru") "vocabulary-v1.json" else "vocabulary-$pairId-v1.json"))
    }
    private val lock = Mutex()

    override suspend fun loadRaw(): String? = withContext(Dispatchers.IO) {
        lock.withLock { readOrNull(document) }
    }

    override suspend fun saveRaw(value: String, backupCurrent: Boolean) = withContext(Dispatchers.IO) {
        lock.withLock {
            require(value.encodeToByteArray().size <= 10_000_000) { "Словарь слишком большой" }
            VocabularyCodec.decode(value)
            val current = readOrNull(document)
            if (current != null) {
                if (backupCurrent) {
                    val backup = AtomicFile(File(directory, "backups/vocabulary-${UUID.randomUUID()}.json"))
                    write(backup, current)
                    check(read(backup) == current) { "Резервная копия словаря не сохранилась" }
                } else VocabularyCodec.decode(current)
            }
            write(document, value)
            check(read(document) == value) { "Словарь не прошёл проверку записи" }
        }
    }

    private fun readOrNull(file: AtomicFile): String? = try { read(file) } catch (_: FileNotFoundException) { null }
    private fun read(file: AtomicFile): String = file.openRead().use(::readUtf8Limited)
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
