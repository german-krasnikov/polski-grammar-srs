package polski.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.core.engine.ChainStepRecipe
import polski.core.engine.CasePrefix
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
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.caseSentencePrefix
import polski.data.courseSentenceSeeds
import polski.data.exerciseCopy
import polski.data.generatedCurriculumJsonByLang
import polski.data.nounById
import polski.data.personalPronouns
import polski.data.renderCoursePattern
import polski.grammar.generatedFormsFixtureJson
import polski.model.GramCase
import polski.model.NumberGram
import polski.training.parseCurriculum

/**
 * UniversalCorePlan.md §5.1/§5.3/§12 UC-08: pl-ru's real pack data
 * (`courses/lang/pl/curriculum.json`, UC-06; `realization.json`/`exercise-recipes.json`, UC-07;
 * `forms.generated.json`, UC-05) wired to `:core-engine`'s generic `TableMorphology`/
 * `ConstructionRealizer`/`ExerciseGenerator` — the live path, replacing the deleted
 * `GrammarEngine`/`ExerciseFactory`'s hand-written rules. [polski.grammar]'s morphology functions
 * and [polski.training.PlExerciseEngine] both build on [plMorphology]/[plExerciseGenerator] here,
 * so the pack data is parsed and embedded exactly once.
 */
private fun parseForms(json: String): Map<String, Map<FeatureBundle, String>> =
    Json.parseToJsonElement(json).jsonObject["forms"]!!.jsonObject.mapValues { (_, entries) ->
        entries.jsonArray.associate { entry ->
            val bundle: FeatureBundle = entry.jsonObject["bundle"]!!.jsonObject
                .mapKeys { (k, _) -> FeatureKey(k) }
                .mapValues { (_, v) -> FeatureValue(v.jsonPrimitive.content) }
            bundle to entry.jsonObject["form"]!!.jsonPrimitive.content
        }
    }

/**
 * EN-06/EN-07 (gap G): the `pack → engine` factory — one instance owns a target-language id's
 * whole `:core-engine` wiring ([TableMorphology], [ConstructionRealizer], [ExerciseGenerator],
 * chain steps), keyed by [langId] (a `courses/lang/<code>/` directory name) instead of the
 * hardcoded `"pl"` literal this file used to read directly. `plMorphology`/`plExerciseGenerator`/
 * `plChainSteps` below are thin wrappers over the one `PackEngine("pl")` instance, so
 * [polski.grammar]'s morphology functions and [polski.training.PlExerciseEngine] — every caller
 * this task's own acceptance names — keep calling the exact same public names.
 */
internal class PackEngine(langId: String) {
    private val templates by lazy { parseConstructionTemplates(generatedRealizationJsonByLang.getValue(langId)) }
    private val recipeSet by lazy { parseRecipes(generatedExerciseRecipesJsonByLang.getValue(langId)) }
    private val skills by lazy { parseCurriculum(generatedCurriculumJsonByLang.getValue(langId)).associateBy { it.id } }
    private val seeds by lazy { courseSentenceSeeds.map { mapOf("noun" to it.nounId, "adjective" to it.adjectiveId) } }

    /** This pack's own [TableMorphology] — also the source of every matrix/reference form (UC-08). */
    val morphology by lazy { TableMorphology(parseForms(generatedFormsFixtureJson)) }

    /** A noun's inherent `Gender` (§5.1's [LexemeFeatures]) — the only cross-slot agreement fact this pack needs. */
    val lexemeFeatures = LexemeFeatures { nounId -> mapOf(FeatureKey("Gender") to FeatureValue(nounById(nounId).gender.id)) }
    private val realizer by lazy { ConstructionRealizer(templates, morphology, lexemeFeatures) }

    /** Builds the live [ExerciseGenerator] for one session's [random]/[ids] ports. */
    fun exerciseGenerator(random: RandomSource, ids: ExerciseIdFactory): ExerciseGenerator = ExerciseGenerator(
        realizer = realizer,
        morphology = morphology,
        lexemeFeatures = lexemeFeatures,
        skills = skills,
        recipes = recipeSet.skills,
        seeds = seeds,
        random = random,
        ids = ids,
        copy = PackCopy { key -> exerciseCopy(key) },
        pattern = PackPattern { key, values -> renderCoursePattern(key, values) },
        casePrefix = CasePrefix { caseId, numberId -> caseSentencePrefix(GramCase.fromId(caseId), NumberGram.fromId(numberId)) },
        pronouns = PronounForms { key, caseId -> personalPronouns.getValue(key).getValue(GramCase.fromId(caseId)) },
        textCase = TextCase { value -> polski.grammar.capitalize(value) },
        defaultOwnerLexeme = recipeSet.defaultOwnerLexeme,
        verbLexeme = recipeSet.verbLexeme,
        constantSlots = recipeSet.constantSlots,
    )

    /** The chain's 5 fixed steps with step 1's skill resolved for [nounId]'s own gender (UniversalCorePlan.md §5.3). */
    fun chainSteps(nounId: String): List<ChainStepRecipe> {
        val gender = nounById(nounId).gender.id
        val firstSkill = recipeSet.firstSkillByGender[gender] ?: recipeSet.firstSkillByGender.getValue("default")
        val steps = recipeSet.chain
        return listOf(steps.first().copy(skillId = firstSkill)) + steps.drop(1)
    }
}

private val plEngine by lazy { PackEngine("pl") }

/** The pack's own [TableMorphology] — also the source of every matrix/reference form (UC-08). */
val plMorphology: TableMorphology get() = plEngine.morphology

/** A noun's inherent `Gender` (§5.1's [LexemeFeatures]) — the only cross-slot agreement fact pl needs. */
val plLexemeFeatures: LexemeFeatures get() = plEngine.lexemeFeatures

/** Builds the live [ExerciseGenerator] for one session's [random]/[ids] ports. */
fun plExerciseGenerator(random: RandomSource, ids: ExerciseIdFactory): ExerciseGenerator = plEngine.exerciseGenerator(random, ids)

/** The chain's 5 fixed steps with step 1's skill resolved for [nounId]'s own gender (UniversalCorePlan.md §5.3). */
fun plChainSteps(nounId: String): List<ChainStepRecipe> = plEngine.chainSteps(nounId)
