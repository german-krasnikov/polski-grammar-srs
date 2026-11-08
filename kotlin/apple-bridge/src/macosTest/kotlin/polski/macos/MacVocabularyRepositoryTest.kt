package polski.macos

import kotlinx.coroutines.runBlocking
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import polski.data.selectCoursePack
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyDocument

/**
 * EnRuAcceptance-2026-09-28.md §7 item 2: [MacVocabularyRepository] used one fixed
 * `vocabulary-v1.json` for every pack — switching packs would read/write that same file, and
 * [VocabularyCodec.decode]'s own `pair == packRegistry.active.pairId` check would then reject
 * whichever pack's document wasn't the one most recently saved there. Each pack now gets its own
 * file, [MacVocabularyRepository]'s own KDoc has the exact rule (pl-ru keeps its already-shipped
 * name), so switching back and forth keeps both packs' own progress intact.
 */
@OptIn(ExperimentalForeignApi::class)
class MacVocabularyRepositoryTest {
    @Test
    fun eachPackKeepsItsOwnVocabularyFileAndPlRuKeepsItsShippedName() = runBlocking {
        val directory = NSTemporaryDirectory() + "polski-mac-vocabulary-${NSUUID().UUIDString}"
        try {
            val repository = MacVocabularyRepository(directory)
            val plDocument = VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))
            repository.saveRaw(plDocument)
            assertEquals(plDocument, repository.loadRaw())

            try {
                selectCoursePack("en-ru")
                assertEquals(null, repository.loadRaw(), "a freshly-switched pack must start with no document of its own")
                val enDocument = VocabularyCodec.encode(VocabularyDocument())
                repository.saveRaw(enDocument)
                assertEquals(enDocument, repository.loadRaw())
            } finally {
                selectCoursePack("pl-ru")
            }

            assertEquals(plDocument, repository.loadRaw(), "switching back to pl-ru must see its own, untouched document again")
            assertTrue(NSFileManager.defaultManager.fileExistsAtPath("$directory/vocabulary-v1.json"),
                "pl-ru must keep the exact, already-shipped filename")
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }
}
