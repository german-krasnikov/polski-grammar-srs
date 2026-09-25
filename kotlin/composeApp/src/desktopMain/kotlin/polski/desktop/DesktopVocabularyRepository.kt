package polski.desktop

import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyRepository

/** An independent, atomic document for vocabulary selections and both review directions. */
class DesktopVocabularyRepository(private val directory: Path) : VocabularyRepository {
    private val lock = Mutex()
    private val file = directory.resolve("vocabulary-v1.json")

    override suspend fun loadRaw(): String? = withContext(Dispatchers.IO) {
        lock.withLock { readUnlocked() }
    }

    override suspend fun saveRaw(value: String, backupCurrent: Boolean) = withContext(Dispatchers.IO) {
        lock.withLock {
            require(value.encodeToByteArray().size <= 10_000_000) { "Словарь слишком большой" }
            VocabularyCodec.decode(value)
            val current = readUnlocked()
            if (current != null) {
                if (backupCurrent) {
                    val backup = directory.resolve("backups").resolve("vocabulary-${UUID.randomUUID()}.json")
                    writeDesktopJsonAtomically(backup, current)
                    check(Files.readString(backup) == current) { "Резервная копия словаря не сохранилась" }
                } else VocabularyCodec.decode(current)
            }
            writeDesktopJsonAtomically(file, value)
            check(Files.readString(file) == value) { "Словарь не прошёл проверку записи" }
        }
    }

    private fun readUnlocked(): String? {
        if (!Files.exists(file)) return null
        require(Files.size(file) <= 10_000_000) { "Файл словаря слишком большой" }
        return Files.readString(file)
    }
}
