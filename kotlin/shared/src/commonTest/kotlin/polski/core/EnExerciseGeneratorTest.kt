package polski.core

import polski.core.engine.CasePrefix
import polski.core.engine.ChainStepRecipe
import polski.core.engine.ConstructionRealizer
import polski.core.engine.ExerciseGenerator
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.LexemeFeatures
import polski.core.engine.PackCopy
import polski.core.engine.PackPattern
import polski.core.engine.PronounForms
import polski.core.engine.RandomSource
import polski.core.engine.TableMorphology
import polski.core.engine.TextCase
import polski.core.model.CoreExercise
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.generatedCurriculumJsonByLang
import polski.training.parseCurriculum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * EnRuPackPlan.md task EN-14: the live run its own acceptance names — `ExerciseGenerator
 * .generateForSkill` over every one of `lang/en/curriculum.json`'s 16 real skill ids, reading the
 * real, embedded `lang/en/exercise-recipes.json` (this task) through the same [parseRecipes]
 * `PlEngine.kt` uses for pl. No `forms.generated.json` exists for en yet (EnRealizationTest's own
 * documented gap, still open) so — exactly like that test — a hand-authored [TableMorphology]
 * fixture stands in for en's lexicon/prepositions.json values; every entry is transcribed from the
 * real `lang/en/lexicon.json`/`prepositions.json` content, not invented. [seeds] is a small,
 * controlled pool (not the full 14-noun lexicon) so this fixture stays finite while still covering
 * every construction/slot combination the 16 recipes actually exercise — `pick()` is only ever
 * reached by `en:possessive.my`/`en:mixed`'s owner draw (every other call passes `preferredSeed`).
 */
class EnExerciseGeneratorTest {
    private class FixedDraws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }

    private fun bundle(vararg pairs: Pair<String, String>): FeatureBundle = pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }
    private val empty: FeatureBundle = emptyMap()

    private val seeds = listOf(
        mapOf("noun" to "house", "adjective" to "beautiful"),
        mapOf("noun" to "book", "adjective" to "new"),
        mapOf("noun" to "car", "adjective" to "good"),
    )

    // Transcribed from lang/en/lexicon.json + prepositions.json (EnRuPackPlan.md §1.3/§5 gap A) —
    // no forms.generated.json exists for en yet, matching EnRealizationTest's own documented gap.
    private val morphology = TableMorphology(
        mapOf(
            "noun:house" to mapOf(bundle("Number" to "sg") to "house", bundle("Number" to "pl") to "houses"),
            "noun:book" to mapOf(bundle("Number" to "sg") to "book", bundle("Number" to "pl") to "books"),
            "noun:car" to mapOf(bundle("Number" to "sg") to "car", bundle("Number" to "pl") to "cars"),
            "adjective:beautiful" to mapOf(empty to "beautiful"),
            "adjective:new" to mapOf(empty to "new"),
            "adjective:good" to mapOf(empty to "good"),
            "possessive:my" to mapOf(empty to "my"),
            "possessive:your" to mapOf(empty to "your"),
            "possessive:his" to mapOf(empty to "his"),
            "possessive:her" to mapOf(empty to "her"),
            "possessive:its" to mapOf(empty to "its"),
            "possessive:our" to mapOf(empty to "our"),
            "possessive:their" to mapOf(empty to "their"),
            "pronoun:you" to mapOf(empty to "you"),
            "neg:not" to mapOf(empty to "not"),
            "prep:role" to mapOf(
                bundle("Case" to "Subj") to "", bundle("Case" to "Obj") to "",
                bundle("Case" to "In") to "in", bundle("Case" to "With") to "with", bundle("Case" to "To") to "to",
            ),
            "verb:walk" to mapOf(
                empty to "walk",
                bundle("Tense" to "Past") to "walked",
                bundle("Tense" to "Fut") to "walk",
                bundle("Aspect" to "Continuous") to "walking",
                bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg") to "walks",
                bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg") to "walk",
            ),
            "aux:do" to mapOf(
                bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg") to "do",
                bundle("Tense" to "Past") to "did",
                bundle("Tense" to "Pres", "Person" to "2", "Number" to "sg") to "do",
            ),
            "aux:will" to mapOf(bundle("Tense" to "Fut", "Person" to "3", "Number" to "sg") to "will"),
            "aux:be" to mapOf(bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg") to "is"),
        ),
    )

    // EnRuPackPlan.md §1.3: en's lexicon deliberately carries no grammatical-gender feature — the
    // "en:pronouns" recipe's `genderToKey` is `{"default": "it"}` and its own curriculum
    // `lexicalFilter` restricts the draw to inanimate nouns, so this absence is never a crash.
    private val lexemeFeatures = LexemeFeatures { emptyMap() }

    private val templates = parseConstructionTemplates(generatedRealizationJsonByLang.getValue("en"))
    private val recipeSet = parseRecipes(generatedExerciseRecipesJsonByLang.getValue("en"))
    private val skills = parseCurriculum(generatedCurriculumJsonByLang.getValue("en")).associateBy { it.id }
    private val realizer = ConstructionRealizer(templates, morphology, lexemeFeatures)

    private fun generator(draws: List<Double> = listOf(0.4)): ExerciseGenerator = ExerciseGenerator(
        realizer = realizer,
        morphology = morphology,
        lexemeFeatures = lexemeFeatures,
        skills = skills,
        recipes = recipeSet.skills,
        seeds = seeds,
        random = FixedDraws(draws),
        ids = ExerciseIdFactory { "test-id" },
        copy = PackCopy { key -> "[$key]" },
        pattern = PackPattern { key, values -> "<$key: " + values.entries.joinToString(", ") { (k, v) -> "$k=$v" } + ">" },
        casePrefix = CasePrefix { caseId, numberId ->
            when (caseId) {
                "Subj" -> if (numberId == "pl") "Here are" else "Here is"
                else -> error("EnExerciseGeneratorTest fixture: no case prefix for \"$caseId\"")
            }
        },
        pronouns = PronounForms { key, caseId ->
            if (key == "it" && caseId == "Obj") "it" else error("EnExerciseGeneratorTest fixture: no pronoun form for \"$key\"/\"$caseId\"")
        },
        textCase = TextCase { value -> value.replaceFirstChar { it.uppercaseChar() } },
        defaultOwnerLexeme = recipeSet.defaultOwnerLexeme,
        verbLexeme = recipeSet.verbLexeme,
        constantSlots = recipeSet.constantSlots,
    )

    private val enSkillIds = listOf(
        "en:role.object", "en:verb.presentSimple", "en:possessive.my", "en:mood.question",
        "en:role.location", "en:role.instrument", "en:polarity.present", "en:verb.pastSimple",
        "en:role.recipient", "en:number.plural", "en:polarity.past", "en:verb.futureSimple",
        "en:pronouns", "en:verb.presentContinuous", "en:tense.contrast", "en:mixed",
    )

    @Test
    fun curriculumHasExactlyThe16PlannedSkillIds() {
        assertEquals(enSkillIds.toSet(), skills.keys)
    }

    @Test
    fun everyOneOf16EnSkillsGeneratesANonEmptyExerciseWithoutErrors() {
        val results: Map<String, CoreExercise> = enSkillIds.associateWith { skillId ->
            generator().generateForSkill(skillId, preferredSeed = seeds.first())
        }
        assertEquals(16, results.size)
        results.forEach { (skillId, exercise) ->
            assertTrue(exercise.source.isNotBlank(), "$skillId: blank source")
            assertTrue(exercise.expected.isNotBlank(), "$skillId: blank expected")
            assertTrue(exercise.prompt.isNotBlank(), "$skillId: blank prompt")
            assertTrue(exercise.explanation.isNotBlank(), "$skillId: blank explanation")
            assertTrue(exercise.changes.isNotEmpty(), "$skillId: no changes")
        }
        // EN-14 evidence: sample output for every one of the 16 skills.
        println("=== EN-14 live ExerciseGenerator run (lang/en, all 16 skills) ===")
        enSkillIds.forEach { id ->
            val e = results.getValue(id)
            println("$id\n  source:      ${e.source}\n  expected:    ${e.expected}\n  prompt:      ${e.prompt}\n  explanation: ${e.explanation}\n  changes:     ${e.changes}\n  tags:        ${e.tags}")
        }
    }

    @Test
    fun realizesGrammaticallyCorrectDoSupportAndPeriphrasticTenses() {
        // A native-level spot check beyond "non-empty": exact surface text for the gap A/D cases.
        val polarityNeg = generator().generateForSkill("en:polarity.present", preferredSeed = seeds.first())
        assertEquals("<subjectVerb: verb=do not walk>", polarityNeg.expected)

        val future = generator().generateForSkill("en:verb.futureSimple", preferredSeed = seeds.first())
        assertEquals("<verbSentence: subj=My beautiful house, verb=will walk>", future.expected)

        val question = generator().generateForSkill("en:mood.question", preferredSeed = seeds.first())
        assertEquals("You walk.", question.source)
        assertEquals("Do you walk?", question.expected)

        val role = generator().generateForSkill("en:role.location", preferredSeed = seeds.first())
        assertEquals("<roleDrill: start=[roleLocationStart], target=in my beautiful house>", role.expected)
    }

    @Test
    fun generatesTheFiveStepChainWithoutErrors() {
        val firstSkill = recipeSet.firstSkillByGender.getValue("default")
        val steps: List<ChainStepRecipe> = listOf(recipeSet.chain.first().copy(skillId = firstSkill)) + recipeSet.chain.drop(1)
        val chain = generator().generateChain(steps, seeds.first())
        assertEquals(5, chain.size)
        chain.forEach { step ->
            assertTrue(step.source.isNotBlank())
            assertTrue(step.expected.isNotBlank())
        }
        println("=== EN-14 evidence: 5-step chain ===")
        chain.forEach { println("${it.primarySkill}: ${it.source} -> ${it.expected}") }
    }
}
