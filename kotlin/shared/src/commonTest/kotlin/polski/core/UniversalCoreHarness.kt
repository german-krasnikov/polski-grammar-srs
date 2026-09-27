package polski.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
import polski.data.caseSentencePrefix
import polski.data.courseSentenceSeeds
import polski.data.exerciseCopy
import polski.data.nounById
import polski.data.personalPronouns
import polski.data.renderCoursePattern
import polski.grammar.capitalize
import polski.grammar.generatedFormsFixtureJson
import polski.model.Exercise
import polski.model.FormChange
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.training.ExerciseFactory
import polski.training.plCurriculum
import polski.training.ExerciseIdFactory as LegacyExerciseIdFactory
import polski.training.RandomSource as LegacyRandomSource

/**
 * UniversalCorePlan.md §5.1/§5.3/§12 UC-07: wires the generic `ConstructionRealizer`/
 * `ExerciseGenerator` to pl-ru's real pack data — `courses/lang/pl/curriculum.json` (already
 * embedded, UC-06), `realization.json`/`exercise-recipes.json` (this task) and
 * `forms.generated.json` (UC-05) — exactly the data the byte-parity gate (`UniversalCoreParityTest`,
 * generated from `tests/fixtures/core-golden/exercises.json`) needs to drive the new engine
 * side by side with `ExerciseFactory`. Test-only: `ExerciseFactory`/`GrammarEngine` stay the live
 * path until UC-08 switches the app over.
 */
private fun parseUc07Forms(json: String): Map<String, Map<FeatureBundle, String>> =
    Json.parseToJsonElement(json).jsonObject["forms"]!!.jsonObject.mapValues { (_, entries) ->
        entries.jsonArray.associate { entry ->
            val bundle: FeatureBundle = entry.jsonObject["bundle"]!!.jsonObject
                .mapKeys { (k, _) -> FeatureKey(k) }
                .mapValues { (_, v) -> FeatureValue(v.jsonPrimitive.content) }
            bundle to entry.jsonObject["form"]!!.jsonPrimitive.content
        }
    }

private val uc07Templates by lazy { parseConstructionTemplates(generatedRealizationJson) }
private val uc07RecipeSet by lazy { parseRecipes(generatedExerciseRecipesJson) }
private val uc07Morphology by lazy { TableMorphology(parseUc07Forms(generatedFormsFixtureJson)) }
private val uc07LexemeFeatures = LexemeFeatures { nounId -> mapOf(FeatureKey("Gender") to FeatureValue(nounById(nounId).gender.id)) }
private val uc07Realizer by lazy { ConstructionRealizer(uc07Templates, uc07Morphology, uc07LexemeFeatures) }
private val uc07Skills by lazy { plCurriculum.associateBy { it.id } }
private val uc07Seeds by lazy { courseSentenceSeeds.map { mapOf("noun" to it.nounId, "adjective" to it.adjectiveId) } }

/** A repeatable [RandomSource]: returns [draws] in order, then repeats the last value (matches `TrainingParityTest`'s `Draws`). */
private class Uc07Draws(private val draws: List<Double>) : RandomSource {
    private var index = 0
    override fun nextDouble(): Double = draws[(index++).coerceAtMost(draws.lastIndex)]
}

fun uc07Generator(draws: List<Double>): ExerciseGenerator = ExerciseGenerator(
    realizer = uc07Realizer,
    morphology = uc07Morphology,
    lexemeFeatures = uc07LexemeFeatures,
    skills = uc07Skills,
    recipes = uc07RecipeSet.skills,
    seeds = uc07Seeds,
    random = Uc07Draws(draws),
    ids = ExerciseIdFactory { "generated" },
    copy = PackCopy { key -> exerciseCopy(key) },
    pattern = PackPattern { key, values -> renderCoursePattern(key, values) },
    casePrefix = CasePrefix { caseId, numberId -> caseSentencePrefix(GramCase.fromId(caseId), NumberGram.fromId(numberId)) },
    pronouns = PronounForms { key, caseId -> personalPronouns.getValue(key).getValue(GramCase.fromId(caseId)) },
    textCase = TextCase { value -> capitalize(value) },
)

/** The chain's 5 fixed steps with step 1's skill resolved for [nounId]'s own gender (UniversalCorePlan.md §5.3). */
fun uc07ChainSteps(nounId: String): List<ChainStepRecipe> {
    val gender = nounById(nounId).gender.id
    val firstSkill = uc07RecipeSet.firstSkillByGender[gender] ?: uc07RecipeSet.firstSkillByGender.getValue("default")
    val steps = uc07RecipeSet.chain
    return listOf(steps.first().copy(skillId = firstSkill)) + steps.drop(1)
}

/** A repeatable `polski.training.RandomSource` — the same sequence-then-repeat contract as [Uc07Draws], for the *old* port type. */
private class LegacyDraws(private val draws: List<Double>) : LegacyRandomSource {
    private var index = 0
    override fun nextDouble(): Double = draws[(index++).coerceAtMost(draws.lastIndex)]
}

/** The live `ExerciseFactory` (UniversalCorePlan.md §5.3's "old path"), fed the same [draws] as [uc07Generator] so both can be compared on equal footing. */
fun legacyFactory(draws: List<Double>): ExerciseFactory = ExerciseFactory(LegacyDraws(draws), LegacyExerciseIdFactory { "generated" })

/** Maps the generic [CoreExercise] onto `polski.model.Exercise` for the parity comparison — the only place this task's new code touches pl's own `Exercise` type. */
fun CoreExercise.toLegacyExercise(): Exercise = Exercise(
    id = id, primarySkill = primarySkill, source = source, prompt = prompt, expected = expected,
    accepted = accepted, explanation = explanation, tags = tags,
    nounId = slots.getValue("noun"), adjectiveId = slots.getValue("adjective"),
    possessive = PossessiveId.fromId(slots.getValue("owner")), number = NumberGram.fromId(slots.getValue("number")),
    changes = changes.map { FormChange(it.from, it.to, it.reason) },
)
