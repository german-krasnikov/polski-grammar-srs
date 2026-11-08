package polski.ios

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import platform.Foundation.NSUserDefaults
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyDocument

class IosVocabularyRepositoryTest {
    @Test
    fun guardsInvalidBytesAndBacksUpBeforeImport() = runBlocking {
        val suite = "polski-vocabulary-test-${kotlin.random.Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val repository = IosVocabularyRepository(defaults)
            val empty = VocabularyCodec.encode(VocabularyDocument())
            val selected = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))
            repository.saveRaw(empty)
            assertEquals(empty, repository.loadRaw())

            defaults.setObject("{broken", forKey = "polski-vocabulary-pl-ru-v1")
            assertFails { repository.saveRaw(selected) }
            assertEquals("{broken", repository.loadRaw())

            repository.saveRaw(selected, backupCurrent = true)
            assertEquals(selected, repository.loadRaw())
            val backups = defaults.dictionaryRepresentation().keys.filter {
                it.toString().startsWith("polski-vocabulary-import-backup-")
            }
            assertEquals(1, backups.size)
            assertEquals("{broken", defaults.stringForKey(backups.single().toString()))
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun repeatedBackedUpImportsKeepOnlyTheLatestBackup() = runBlocking {
        val suite = "polski-vocabulary-test-${kotlin.random.Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val repository = IosVocabularyRepository(defaults)
            val first = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))
            val second = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.husband")))
            repository.saveRaw(first)

            repository.saveRaw(second, backupCurrent = true)
            val third = VocabularyCodec.encode(VocabularyDocument())
            repository.saveRaw(third, backupCurrent = true)

            val backups = defaults.dictionaryRepresentation().keys.filter {
                it.toString().startsWith("polski-vocabulary-import-backup-")
            }
            assertEquals(1, backups.size, "Only the most recent pre-import backup should be kept, not one per import")
            assertEquals(second, defaults.stringForKey(backups.single().toString()))
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }
}
