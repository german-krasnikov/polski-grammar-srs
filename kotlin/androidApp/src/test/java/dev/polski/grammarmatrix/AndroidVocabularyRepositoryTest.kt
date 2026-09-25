package dev.polski.grammarmatrix

import android.content.Context
import android.content.ContextWrapper
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyDocument

@RunWith(RobolectricTestRunner::class)
class AndroidVocabularyRepositoryTest {
    @get:Rule val temp = TemporaryFolder()

    private fun repository(): AndroidVocabularyRepository {
        val base = RuntimeEnvironment.getApplication()
        return AndroidVocabularyRepository(object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = temp.root
        })
    }

    @Test fun writesVocabularyApartFromGrammarProgress() = runBlocking {
        val grammar = File(temp.root, "progress-v1.json").apply { writeText("grammar bytes") }
        val raw = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))
        val repository = repository()
        repository.saveRaw(raw)
        assertEquals(raw, repository.loadRaw())
        assertEquals("grammar bytes", grammar.readText())
    }

    @Test fun malformedExistingDocumentCannotBeSilentlyOverwritten() = runBlocking {
        val file = File(temp.root, "vocabulary-v1.json").apply { writeText("{broken") }
        val repository = repository()
        val raw = VocabularyCodec.encode(VocabularyDocument())
        assertEquals("{broken", repository.loadRaw())
        try { repository.saveRaw(raw); throw AssertionError("Expected recovery guard") }
        catch (_: Exception) { assertEquals("{broken", file.readText()) }
    }

    @Test fun importBacksUpExactOldBytes() = runBlocking {
        val repository = repository()
        val old = VocabularyCodec.encode(VocabularyDocument())
        val newer = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))
        repository.saveRaw(old)
        repository.saveRaw(newer, backupCurrent = true)
        assertEquals(newer, repository.loadRaw())
        val backups = File(temp.root, "backups").listFiles().orEmpty()
        assertEquals(1, backups.size)
        assertEquals(old, backups.single().readText())
    }
}
