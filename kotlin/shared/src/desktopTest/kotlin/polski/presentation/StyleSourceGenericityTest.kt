package polski.presentation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * CORE (`presentation/Block.kt`, `StyleRecipe.kt`, `StyleComposer.kt`) must name no natural
 * language: it is shared across every future target x native pack, and pair-specific text
 * belongs to `SkillStyleContent`/the `courses` content tree, never a Kotlin literal here. JVM-only (needs file
 * I/O the browser/native test runners don't have) — a static-source companion to
 * [StyleComposerTest.composerIsGenericAcrossLanguagePairs]'s behavioral check.
 */
class StyleSourceGenericityTest {
    private val filesToScan = listOf("Block.kt", "StyleRecipe.kt", "StyleComposer.kt")

    // Cyrillic (Russian) or Polish-diacritic letters in a shared-module source file.
    private val nonGenericLetters = Regex("[Ѐ-ӿĀ-ſ]")

    @Test fun coreStyleFilesContainNoRussianOrPolishLiterals() {
        val sourceDir = presentationSourceDir()
        for (name in filesToScan) {
            val file = File(sourceDir, name)
            assertTrue(file.isFile, "expected to find $file")
            val offendingLines = file.readLines().withIndex()
                .filter { (_, line) -> nonGenericLetters.containsMatchIn(line) }
            assertTrue(offendingLines.isEmpty(), "$name must name no language, found: $offendingLines")
        }
    }

    private fun presentationSourceDir(): File {
        val relative = "src/commonMain/kotlin/polski/presentation"
        val candidates = listOf(File(relative), File("shared", relative))
        return candidates.firstOrNull { it.isDirectory }
            ?: error("could not locate $relative from ${File(".").absolutePath}")
    }
}
