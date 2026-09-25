package dev.polski.grammarmatrix

import android.content.Context
import android.content.ContextWrapper
import java.io.ByteArrayInputStream
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
import kotlin.time.Instant
import polski.progress.LoadResult
import polski.progress.ProgressCodec
import polski.progress.SaveResult
import polski.srs.FsrsScheduler

@RunWith(RobolectricTestRunner::class)
class AndroidProgressRepositoryTest {
    @get:Rule val temp = TemporaryFolder()
    private val scheduler = FsrsScheduler()

    private fun repository(): AndroidProgressRepository {
        val base = RuntimeEnvironment.getApplication()
        val isolated = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = temp.root
        }
        return AndroidProgressRepository(isolated, scheduler)
    }

    @Test fun savesAndReloadsVersionedProgress() = runBlocking {
        val repository = repository()
        val document = ProgressCodec.fresh(
            listOf("case.acc.f"), Instant.parse("2026-02-03T12:00:00Z"), "2026-02-03", scheduler,
        )
        assertEquals(SaveResult.Saved, repository.save(document))
        assertEquals(document.progress, (repository.load() as LoadResult.Loaded).document.progress)
    }

    @Test fun malformedExistingBytesAreNeverOverwritten() = runBlocking {
        val file = File(temp.root, "progress-v1.json")
        file.writeText("{broken")
        val repository = repository()
        val document = ProgressCodec.fresh(
            listOf("case.acc.f"), Instant.parse("2026-02-03T12:00:00Z"), "2026-02-03", scheduler,
        )
        assertTrue(repository.load() is LoadResult.Invalid)
        assertTrue(repository.save(document) is SaveResult.WriteFailed)
        assertEquals("{broken", file.readText())
    }

    @Test fun importsReactV1AndBacksUpCurrentDocument() = runBlocking {
        val repository = repository()
        val initial = ProgressCodec.fresh(
            listOf("case.acc.f"), Instant.parse("2026-02-03T12:00:00Z"), "2026-02-03", scheduler,
        )
        assertEquals(SaveResult.Saved, repository.save(initial))
        val oldRaw = File(temp.root, "progress-v1.json").readText()
        assertTrue(repository.importJson("{") is ImportResult.Invalid)
        assertEquals(oldRaw, File(temp.root, "progress-v1.json").readText())
        val fixture = checkNotNull(javaClass.getResource("/react-progress-v1.json")).readText()
        assertEquals(ImportResult.Imported, repository.importJson(fixture))
        assertTrue(repository.load() is LoadResult.Loaded)
        assertEquals(fixture, File(temp.root, "progress-v1.json").readText())
        val backups = File(temp.root, "backups").listFiles().orEmpty()
        assertEquals(1, backups.size)
        assertEquals(oldRaw, backups.single().readText())
    }

    @Test fun unsupportedImportLeavesExistingProgressUntouched() = runBlocking {
        val repository = repository()
        val initial = ProgressCodec.fresh(
            listOf("case.acc.f"), Instant.parse("2026-02-03T12:00:00Z"), "2026-02-03", scheduler,
        )
        assertEquals(SaveResult.Saved, repository.save(initial))
        val oldRaw = File(temp.root, "progress-v1.json").readText()

        assertEquals(ImportResult.Unsupported(2), repository.importJson("{\"version\":2}"))
        assertEquals(oldRaw, File(temp.root, "progress-v1.json").readText())
        assertTrue(File(temp.root, "backups").listFiles().isNullOrEmpty())
    }

    @Test fun oversizedStreamIsRejected() {
        val input = ByteArrayInputStream(ByteArray(10_000_001))
        try {
            readUtf8Limited(input)
            throw AssertionError("Expected file size rejection")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("too large"))
        }
    }
}
