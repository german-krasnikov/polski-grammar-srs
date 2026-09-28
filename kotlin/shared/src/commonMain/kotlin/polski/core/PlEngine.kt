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
import polski.data.nounByIdOrNull
import polski.data.packRegistry
import polski.data.personalPronounForm
import polski.data.renderCoursePattern
import polski.grammar.generatedFormsFixtureJson
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

    /**
     * This pack's own [TableMorphology] — also the source of every matrix/reference form (UC-08).
     * EN-24: pl keeps reading the single legacy [generatedFormsFixtureJson] fixture byte-identical
     * to before (no `lang/pl/forms.generated.json` file exists, so the per-lang map below never
     * has a "pl" entry); a language with a real `lang/<code>/forms.generated.json` — en, EN-24 —
     * reads it from there instead, the same scanning pattern EN-05 already uses for
     * realization.json/exercise-recipes.json.
     */
    val morphology by lazy { TableMorphology(parseForms(generatedFormsGeneratedJsonByLang[langId] ?: generatedFormsFixtureJson)) }

    /** A noun's inherent `Gender` (§5.1's [LexemeFeatures]) — the only cross-slot agreement fact
     *  this pack needs. Empty for a noun this pack has no gendered/case-declining entry for
     *  (EnRuAcceptance §7 item 1: a genderless language's [nounByIdOrNull] lookup finds nothing) rather
     *  than throwing — pl's own nouns are always found, so pl's result is unchanged. */
    val lexemeFeatures = LexemeFeatures { nounId ->
        nounByIdOrNull(nounId)?.let { noun -> mapOf(FeatureKey("Gender") to FeatureValue(noun.gender.id)) } ?: emptyMap()
    }
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
        casePrefix = CasePrefix { caseId, numberId -> caseSentencePrefix(caseId, NumberGram.fromId(numberId)) },
        pronouns = PronounForms { key, caseId -> personalPronounForm(key, caseId) },
        textCase = TextCase { value -> polski.grammar.capitalize(value) },
        defaultOwnerLexeme = recipeSet.defaultOwnerLexeme,
        verbLexeme = recipeSet.verbLexeme,
        constantSlots = recipeSet.constantSlots,
    )

    /** The chain's 5 fixed steps with step 1's skill resolved for [nounId]'s own gender
     *  (UniversalCorePlan.md §5.3) — falls back to `"default"` for a genderless language's noun,
     *  same as an unrecognized gender id always has (EnRuAcceptance §7 item 1; en's own
     *  `exercise-recipes.json` already declares only `"default"` for this exact reason). */
    fun chainSteps(nounId: String): List<ChainStepRecipe> {
        val gender = nounByIdOrNull(nounId)?.gender?.id
        val firstSkill = gender?.let { recipeSet.firstSkillByGender[it] } ?: recipeSet.firstSkillByGender.getValue("default")
        val steps = recipeSet.chain
        return listOf(steps.first().copy(skillId = firstSkill)) + steps.drop(1)
    }
}

/** One [PackEngine] per `langId`, built at most once each — [activeEngine] and [enMorphology] share it. */
private val engines = mutableMapOf<String, PackEngine>()
private fun engineFor(langId: String): PackEngine = engines.getOrPut(langId) { PackEngine(langId) }

/**
 * EN-22 (gap G): the `plX`-named functions below keep their pl-hardcoded names for host source
 * compatibility (EN-07's own promise), but now resolve to whichever pack [packRegistry.active] is
 * — pl-ru by default, unchanged — instead of always building `PackEngine("pl")`. Once a second pack
 * is safely selectable (see `packRegistry`'s own KDoc for why en-ru isn't wired in yet), a host's
 * preferences bridge calling `selectCoursePack` makes every one of these return that pack's own
 * engine on the very next call, no restart needed.
 */
private val activeEngine: PackEngine get() = engineFor(packRegistry.active.targetLanguage)

/** The active pack's own [TableMorphology] — also the source of every matrix/reference form (UC-08). */
val plMorphology: TableMorphology get() = activeEngine.morphology

/** A noun's inherent `Gender` (§5.1's [LexemeFeatures]) — the only cross-slot agreement fact this pack needs. */
val plLexemeFeatures: LexemeFeatures get() = activeEngine.lexemeFeatures

/** Builds the live [ExerciseGenerator] for one session's [random]/[ids] ports. */
fun plExerciseGenerator(random: RandomSource, ids: ExerciseIdFactory): ExerciseGenerator = activeEngine.exerciseGenerator(random, ids)

/** The chain's 5 fixed steps with step 1's skill resolved for [nounId]'s own gender (UniversalCorePlan.md §5.3). */
fun plChainSteps(nounId: String): List<ChainStepRecipe> = activeEngine.chainSteps(nounId)

/**
 * EN-24 (UC-09 part 2/2 minimum): en's own [TableMorphology], sourced from
 * `lang/en/forms.generated.json` — independent of [polski.data.packRegistry]'s active pack, so the
 * web matrix's English table always has real forms to show regardless of which pack is currently
 * active. Shares [engines]' cache with [activeEngine], so selecting en-ru builds no second instance.
 */
val enMorphology: TableMorphology get() = engineFor("en").morphology
