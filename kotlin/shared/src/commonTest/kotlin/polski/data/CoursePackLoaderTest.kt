package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * NJ03 B8: v1 course.json removed; pl-ru now always loads via v2 layers (pair.json + lexicon.json).
 * The v1 vs v2 comparison is no longer needed (no v1 source). This test verifies the v2 path
 * produces a valid CoursePack with the expected content from the Biome literal sources.
 */
class CoursePackLoaderTest {
    private val v2Source = CoursePackLoader.fromV2Layers(
        pairId = "pl-ru",
        lexiconJson = generatedLexiconJsonByLang.getValue("pl"),
        pairJson = generatedPairJsonByPairId.getValue("pl-ru"),
    )

    @Test fun v2PackHasExpectedContent() {
        val pack = CoursePack(v2Source)
        assertEquals("pl-ru", pack.pairId)
        assertEquals(14, pack.nouns.size)
        assertEquals(7, pack.adjectives.size)
        assertEquals(11, pack.verbs.size)
        assertEquals(16, pack.skills.size)
        assertEquals(12, pack.sentenceSeeds.size)
    }

    @Test fun v2PackSkillsMatchTypedBiomeSource() {
        val pack = CoursePack(v2Source)
        // CoursePack still parses from JSON; typed source (generatedPlRuSkills) should match
        assertEquals(generatedPlRuSkills.map { it.id }, pack.skills.map { it.id })
    }
}
