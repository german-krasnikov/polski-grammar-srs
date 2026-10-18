package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * NJ03 B11: pair-lifetime oracle. Verifies that:
 * 1. pl-ru skills come from typed Biome literal source (generatedPlRuSkills)
 * 2. After switching to en-ru, active skills change (JSON path for en-ru)
 * 3. Switching back to pl-ru restores Biome literal source
 * Independent expectations authored against oracle + pair data (NJ03PragmaticCourseCutoverDecision §2).
 */
class NJ03PairLifetimeTest {

    @Test
    fun plRuSkillsAreFromTypedBiomeLiteralSource() {
        selectCoursePack("pl-ru")
        val plSkills = skills
        // Exact count and identity match generatedPlRuSkills
        assertEquals(16, plSkills.size)
        assertEquals(generatedPlRuSkills, plSkills, "pl-ru skills must equal generatedPlRuSkills typed source")
        assertEquals("case.acc.n", plSkills[0].id)
        assertEquals("sentence.plural", plSkills.last().id)
    }

    @Test
    fun afterSwitchToEnRuActiveSkillsChange() {
        selectCoursePack("pl-ru")
        val plIds = skills.map { it.id }.toSet()

        selectCoursePack("en-ru")
        val enSkills = skills
        assertEquals(16, enSkills.size)
        // en-ru skills use "en:" prefix namespace
        assertEquals(true, enSkills.all { it.id.startsWith("en:") }, "en-ru skills must have en: prefix")
        assertEquals(false, enSkills.map { it.id }.toSet().intersect(plIds).isNotEmpty(),
            "en-ru and pl-ru skill ids must be disjoint")

        selectCoursePack("pl-ru") // restore
    }

    @Test
    fun switchBackToPlRuRestoresTypedSource() {
        selectCoursePack("pl-ru")
        val before = skills.map { it.id }

        selectCoursePack("en-ru")
        selectCoursePack("pl-ru")

        val after = skills.map { it.id }
        assertEquals(before, after, "pl-ru skills must be identical after round-trip switch")
        assertEquals(generatedPlRuSkills, skills, "must match typed Biome source after round-trip")
    }

    @Test
    fun sentenceSeedsFromTypedSourceForPlRu() {
        selectCoursePack("pl-ru")
        assertEquals(12, courseSentenceSeeds.size)
        assertEquals(generatedPlRuSentenceSeeds, courseSentenceSeeds,
            "pl-ru sentenceSeeds must equal typed Biome source")
    }
}
