package polski.core.engine

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UniversalCorePlan.md §1.3/§12 UC-07 acceptance ("no Polish literals in :core-engine"): a
 * source-level, ST-06-style genericity check — `:core-engine` realizes ANY language a pack
 * describes (§0: "ru→pl, ru→en, pl→en, en→ru…"), so its own `.kt` files must never contain a
 * Polish letter (a diacritic no other feature/case/copy-key identifier this module uses would
 * ever need) or Cyrillic text (pack-authored copy is always looked up through [PackCopy]/
 * [PackPattern] by an opaque key, never inlined). Scoped to `commonMain` only — `commonTest`
 * fixtures legitimately use realistic Polish example data to exercise the generic mechanism
 * meaningfully (`TableMorphologyTest`'s "żona"/"żony", same spirit as `EndingHighlightTest`'s own
 * ST-06 test), and this file's own diacritic/Cyrillic-range literals would trivially self-match.
 * JVM-only: scanning `.kt` source text is a build-time concern, not something JS/Wasm/Native
 * runtimes need to repeat.
 */
class NoLanguageLiteralsTest {
    private val polishLetters = ('Ą'..'ż').toSet() // Ą..ż — covers every Polish diacritic without spelling one out here
    private val cyrillic = ('Ѐ'..'ӿ')

    private fun sourceFiles(): List<File> {
        val moduleDir = generateSequence(File(".").canonicalFile) { it.parentFile }
            .firstOrNull { File(it, "core-engine/src/commonMain").isDirectory || (it.name == "core-engine" && File(it, "src/commonMain").isDirectory) }
            ?: error("NoLanguageLiteralsTest: could not locate the core-engine module from ${File(".").canonicalPath}")
        val root = if (moduleDir.name == "core-engine") moduleDir else File(moduleDir, "core-engine")
        return File(root, "src/commonMain").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    @Test fun commonMainContainsNoPolishLetterOrCyrillicText() {
        val files = sourceFiles()
        assertTrue(files.size >= 3, "expected to scan :core-engine's own commonMain .kt files, found ${files.size}")
        val offenders = files.mapNotNull { file ->
            val text = file.readText()
            val badChars = text.toSet().filter { it in polishLetters || it in cyrillic }
            if (badChars.isEmpty()) null else "${file.path}: ${badChars.joinToString()}"
        }
        assertTrue(offenders.isEmpty(), "Polish/Cyrillic literal(s) found in :core-engine source:\n${offenders.joinToString("\n")}")
    }

    /** Strips `/* ... */` and `// ...` so KDoc prose (which legitimately spells out example literals in backticks) never self-matches. */
    private fun codeOnly(text: String): String =
        Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(text, "").lineSequence().joinToString("\n") { it.substringBefore("//") }

    /**
     * UniversalCorePlan.md §1.3 principle #3 / UC-14 acceptance ("Ноль правок :core-*" for a second
     * language): a hardcoded lexeme id (which verb a tense skill drills, which possessive is the
     * unmarked default owner) or a hardcoded punctuation mark is exactly the kind of pack-specific
     * content decision that must come from `exercise-recipes.json`/`realization.json` through a
     * constructor port or recipe field ([ExerciseGenerator]'s `defaultOwnerLexeme`/`verbLexeme`,
     * [TextSpec.Prefixed]'s `punct`) — never a Kotlin string literal, even an ASCII one that passes
     * [commonMainContainsNoPolishLetterOrCyrillicText] above.
     */
    @Test fun commonMainContainsNoHardcodedLexemeOrPunctuationLiteral() {
        val bannedLiterals = listOf("\"go\"", "\"my\"", "\"!\"")
        val files = sourceFiles()
        val offenders = files.mapNotNull { file ->
            val code = codeOnly(file.readText())
            val hits = bannedLiterals.filter { it in code }
            if (hits.isEmpty()) null else "${file.path}: ${hits.joinToString()}"
        }
        assertTrue(
            offenders.isEmpty(),
            "Hardcoded lexeme-id/punctuation literal(s) found in :core-engine source (belongs in pack data instead):\n${offenders.joinToString("\n")}",
        )
    }
}
