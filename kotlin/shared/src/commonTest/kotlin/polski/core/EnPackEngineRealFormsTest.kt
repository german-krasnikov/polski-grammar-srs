package polski.core

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.data.generatedCurriculumJsonByLang
import polski.data.selectActiveCoursePack
import polski.training.parseCurriculum

/**
 * EnRuAcceptance-2026-09-28.md §7 item 1 (ADR-37 blocker 1): unlike [EnExerciseGeneratorTest],
 * which proves the real `lang/en/realization.json`/`exercise-recipes.json` templates generate all
 * 16 skills against a HAND-AUTHORED morphology/pronoun/case-prefix fixture, this test selects
 * en-ru as the real, process-wide active pack (exactly what a host's Settings picker does) and
 * calls the exact production entrypoints ([plExerciseGenerator]/[plChainSteps]) every host's
 * `PlExerciseEngine` calls — so it exercises the real, checked-in `lang/en/forms.generated.json`,
 * the real `caseSentencePrefix`/`personalPronounForm` wiring, end to end, with nothing faked.
 * Before this task's fixes this crashed on the very first skill/chain step with `TableMorphology:
 * no form for lexeme="..."`, then `Unknown case Subj`, then a missing pronoun row — none of which
 * EnExerciseGeneratorTest's own hand-authored fixtures could ever catch.
 */
class EnPackEngineRealFormsTest {
    private val skillIds = parseCurriculum(generatedCurriculumJsonByLang.getValue("en")).map { it.id }

    @AfterTest
    fun restorePlRu() {
        selectActiveCoursePack("pl-ru")
    }

    @Test
    fun curriculumHas16Skills() {
        assertEquals(16, skillIds.size)
    }

    @Test
    fun everySkillGeneratesFromTheRealCheckedInFormsTableWithEnRuActive() {
        selectActiveCoursePack("en-ru")
        val generator = plExerciseGenerator(RandomSource { 0.4 }, ExerciseIdFactory { "real-forms-test" })
        skillIds.forEach { skillId ->
            val exercise = generator.generateForSkill(skillId)
            assertTrue(exercise.source.isNotBlank(), "$skillId: blank source")
            assertTrue(exercise.expected.isNotBlank(), "$skillId: blank expected")
            assertTrue(exercise.changes.isNotEmpty(), "$skillId: no changes")
        }
    }

    @Test
    fun theFiveStepChainGeneratesFromTheRealCheckedInFormsTableWithEnRuActive() {
        selectActiveCoursePack("en-ru")
        val generator = plExerciseGenerator(RandomSource { 0.4 }, ExerciseIdFactory { "real-forms-chain" })
        val chain = generator.generateChain(plChainSteps("house"), mapOf("noun" to "house", "adjective" to "beautiful"))
        assertEquals(5, chain.size)
        chain.forEach { step -> assertTrue(step.expected.isNotBlank()) }
    }
}
