package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.training.PlExerciseEngine
import polski.presentation.StaticPackLifehackProvider
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.StyleRegistry

/** P18 product acceptance: one stable skill, six authored transformations and four presentations per family. */
class PlRuExpansionCoverageTest {
    private val ids = listOf(
        "degree.comparison", "verb.participles", "voice.impersonal", "wordform.nouns",
        "numeral.agreement", "wordform.adjectives", "sentence.connectives",
        "sentence.rephrase", "case.valency", "verb.mood",
    )

    @Test fun tenNewSkillsHaveCompleteAuthoringAndKeepTheHistoricalPrefix() {
        val oldIds = listOf(
            "case.acc.n", "case.acc.f", "case.acc.m", "case.gen.neg", "case.inst", "case.loc", "case.dat",
            "agreement.my", "verb.present", "verb.past", "verb.future", "aspect", "pronouns", "mixed",
            "sentence.question", "sentence.plural",
        )
        assertEquals(oldIds + ids, generatedPlRuSkills.map { it.id })
        for (id in ids) {
            val skill = generatedPlRuSkills.single { it.id == id }
            val recipe = generatedPlRuRecipes.skills.getValue(id)
            assertFalse(skill.caseReferenceAvailable, id)
            assertEquals(6, recipe.staticPool.size, "$id needs six distinct tasks")
            assertEquals(6, recipe.staticPool.map { it.sourceKey }.toSet().size, "$id needs distinct sources")
            assertEquals(6, recipe.staticPool.map { it.expectedKey }.toSet().size, "$id needs distinct targets")
            assertTrue(StaticPackLifehackProvider.forSkill(id).isNotEmpty(), "$id needs a cited tip")
            assertTrue(referenceSystemCards.any { it.id == id }, "$id needs a Matrix summary")
            val content = styleContentBySkillId(id)
            assertTrue(content.nativeParallel.isNotEmpty(), "$id needs authored native parallel")
            assertTrue(content.examples.isNotEmpty(), "$id needs authored examples")
            for (style in listOf(StyleId.RuleFirst, StyleId.SituationFirst, StyleId.NativeContrast, StyleId.MinimalTheory)) {
                val recipeForStyle = StyleRegistry.recipes.getValue(style)
                assertEquals(style, StyleComposer.resolveEffectiveStyle(recipeForStyle, content, StyleRegistry.recipes), "$id style $style")
            }
        }
    }

    @Test fun allNewPoolsYieldSixWholeSentencesBeforeRepeating() {
        val samples = mapOf(
            "verb.participles" to "Kobieta czytająca książkę siedzi przy oknie.",
            "voice.impersonal" to "Zamknięto sklep o szóstej.",
            "wordform.nouns" to "Czytanie książek sprawia mi przyjemność.",
            "numeral.agreement" to "W pokoju stoi pięć krzeseł.",
            "wordform.adjectives" to "To jest szklana butelka.",
            "sentence.connectives" to "Jeśli będziesz miał czas, przyjdź do mnie.",
            "sentence.rephrase" to "Nikt oprócz Anny nie zna odpowiedzi.",
            "case.valency" to "Korzystam z telefonu.",
            "verb.mood" to "Czy mógłbyś mi pomóc?",
        )
        for ((id, sample) in samples) {
            val engine = PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "p18" })
            val cycle = (1..6).map { engine.generateForSkill(id) }
            assertEquals(6, cycle.map { it.source }.toSet().size, id)
            assertEquals(6, cycle.map { it.expected }.toSet().size, id)
            assertTrue(cycle.any { it.expected == sample }, "$id: missing source-backed answer $sample")
            assertTrue(cycle.all { it.source.isNotBlank() && it.prompt.isNotBlank() && it.expected.last() in ".?" && it.changes.size == 1 }, id)
            val front = styleContentBySkillId(id)
            assertTrue(cycle.none { exercise -> front.nativeParallel.any { it.target == exercise.expected } }, "$id: native parallel reveals an answer")
            assertTrue(cycle.none { exercise -> front.examples.contains(exercise.expected) }, "$id: examples reveal an answer")
            if (id == "numeral.agreement") {
                val cats = cycle.single { it.source == "Mam jednego kota." }
                assertTrue("не относятся к мужскому личному типу" in cats.explanation)
                assertFalse("неодушевлённо" in cats.explanation)
            }
            if (id == "sentence.rephrase") {
                assertTrue(cycle.any {
                    it.source == "Piotr skończył pracę, a potem wrócił do domu." &&
                        it.expected == "Kiedy Piotr skończył pracę, wrócił do domu."
                })
            }
            assertNotEquals(cycle.last().source, engine.generateForSkill(id).source, "$id: cycle boundary")
        }
    }
}
