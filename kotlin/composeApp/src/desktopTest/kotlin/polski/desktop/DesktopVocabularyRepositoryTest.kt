package polski.desktop

import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyDocument

class DesktopVocabularyRepositoryTest {
    @Test
    fun savesSeparatelyFromGrammarAndPreservesInvalidBytesForRecovery() = runBlocking {
        val directory = Files.createTempDirectory("polski-vocabulary-")
        try {
            val repository = DesktopVocabularyRepository(directory)
            assertNull(repository.loadRaw())
            val raw = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))
            repository.saveRaw(raw)
            assertEquals(raw, DesktopVocabularyRepository(directory).loadRaw())
            assertEquals(false, Files.exists(directory.resolve("progress-v1.json")))

            Files.writeString(directory.resolve("vocabulary-v1.json"), "{broken")
            assertFails { repository.saveRaw(raw) }
            assertEquals("{broken", repository.loadRaw())
            repository.saveRaw(raw, backupCurrent = true)
            assertEquals(raw, repository.loadRaw())
            val backups = Files.list(directory.resolve("backups")).use { it.toList() }
            assertEquals(listOf("{broken"), backups.map(Files::readString))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
