package polski.core

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
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.CoursePackFields
import polski.data.EnRuTypedPack
import polski.data.caseSentencePrefix
import polski.data.courseSentenceSeeds
import polski.data.exerciseCopy
import polski.data.nounByIdOrNull
import polski.data.packRegistry
import polski.data.personalPronounForm
import polski.data.renderCoursePattern
import polski.model.NumberGram

/**
 * EN-06/EN-07 (gap G), R1.13, UniversalCorePlan.md §5.1/§5.3/§12 UC-08: the `pack → engine` factory — one instance owns a pack's whole `:core-engine` wiring
 * ([TableMorphology], [ConstructionRealizer], [ExerciseGenerator], chain steps), built from the [pack]'s own typed inputs (its skill specs, recipes, construction
 * templates and form table) instead of a `courses/lang/<code>/` directory name and the JSON text embedded for it. `plMorphology`/`plExerciseGenerator`/
 * `plChainSteps` below are thin wrappers over the engine of the active pack, so [polski.grammar]'s morphology functions and [polski.training.PlExerciseEngine]
 * — every caller this task's own acceptance names — keep calling the exact same public names, and a pack's data is built exactly once. The ports that read a
 * pack's sentence text and pronouns (copy, pattern, prefix, pronouns, seeds, lexeme features) still read [packRegistry]'s active pack, as before.
 */
internal class PackEngine(private val pack: CoursePackFields) {
    private val templates by lazy { pack.constructionTemplates }
    private val recipeSet by lazy { pack.recipes }
    private val skills by lazy { pack.skillSpecs.associateBy { it.id } }
    private val seeds by lazy { courseSentenceSeeds.map { mapOf("noun" to it.nounId, "adjective" to it.adjectiveId) } }

    /** This pack's own [TableMorphology] — also the source of every matrix/reference form (UC-08): a lookup in the pack's form table, which holds every form
     *  under the bundle that asks for it (the rules were run when the table was built, R1.12 for pl-ru). */
    val morphology by lazy { TableMorphology(pack.formTable) }

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

/** One [PackEngine] per pack, built at most once each — [activeEngine] and [enMorphology] share it. */
private val engines = mutableMapOf<String, PackEngine>()
private fun engineFor(pack: CoursePackFields): PackEngine = engines.getOrPut(pack.pairId) { PackEngine(pack) }

/**
 * EN-22 (gap G): the `plX`-named functions below keep their pl-hardcoded names for host source
 * compatibility (EN-07's own promise), but now resolve to whichever pack [packRegistry.active] is
 * — pl-ru by default, unchanged — instead of always building the engine of pl. Once a second pack
 * is safely selectable (see `packRegistry`'s own KDoc for why en-ru isn't wired in yet), a host's
 * preferences bridge calling `selectCoursePack` makes every one of these return that pack's own
 * engine on the very next call, no restart needed.
 */
private val activeEngine: PackEngine get() = engineFor(packRegistry.active)

/** The active pack's own [TableMorphology] — also the source of every matrix/reference form (UC-08). */
val plMorphology: TableMorphology get() = activeEngine.morphology

/** A noun's inherent `Gender` (§5.1's [LexemeFeatures]) — the only cross-slot agreement fact this pack needs. */
val plLexemeFeatures: LexemeFeatures get() = activeEngine.lexemeFeatures

/** Builds the live [ExerciseGenerator] for one session's [random]/[ids] ports. */
fun plExerciseGenerator(random: RandomSource, ids: ExerciseIdFactory): ExerciseGenerator = activeEngine.exerciseGenerator(random, ids)

/** The chain's 5 fixed steps with step 1's skill resolved for [nounId]'s own gender (UniversalCorePlan.md §5.3). */
fun plChainSteps(nounId: String): List<ChainStepRecipe> = activeEngine.chainSteps(nounId)

/**
 * EN-24 (UC-09 part 2/2 minimum): en's own [TableMorphology], from en-ru's form table — independent of
 * [polski.data.packRegistry]'s active pack, so the web matrix's English table always has real forms to show
 * regardless of which pack is currently active. Shares [engines]' cache with [activeEngine], so selecting en-ru builds no second instance.
 */
val enMorphology: TableMorphology get() = engineFor(EnRuTypedPack).morphology
