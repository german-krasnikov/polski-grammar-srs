package polski.core.model

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * SB-1 blueprint §2 AC1.2 / UniversalCorePlan.md §1.3 ("no Polish literals in :core-*"): mirrors
 * `:core-engine`'s `NoLanguageLiteralsTest`, scoped to `TileExercise.kt` only (this module's other
 * existing `commonMain` files, e.g. `FeatureBundle.kt`, already carry Russian KDoc prose
 * predating this blueprint — not this test's concern). The new sentence-builder shapes describe
 * universal structure only, never a Polish word or Cyrillic text. JVM-only: scanning `.kt` source
 * text is a build-time concern.
 */
class NoLanguageLiteralsTest {
    private val polishLetters = ('Ą'..'ż').toSet()
    private val cyrillic = ('Ѐ'..'ӿ')

    private fun tileExerciseFile(): File {
        val moduleDir = generateSequence(File(".").canonicalFile) { it.parentFile }
            .firstOrNull { File(it, "core-model/src/commonMain").isDirectory || (it.name == "core-model" && File(it, "src/commonMain").isDirectory) }
            ?: error("NoLanguageLiteralsTest: could not locate the core-model module from ${File(".").canonicalPath}")
        val root = if (moduleDir.name == "core-model") moduleDir else File(moduleDir, "core-model")
        return File(root, "src/commonMain/kotlin/polski/core/model/TileExercise.kt").also {
            require(it.isFile) { "NoLanguageLiteralsTest: expected to find $it" }
        }
    }

    @Test fun tileExerciseFileContainsNoPolishLetterOrCyrillicText() {
        val text = tileExerciseFile().readText()
        val badChars = text.toSet().filter { it in polishLetters || it in cyrillic }
        assertTrue(badChars.isEmpty(), "Polish/Cyrillic literal(s) found in TileExercise.kt: ${badChars.joinToString()}")
    }
}
