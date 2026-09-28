package polski.core.engine

import polski.core.model.CoreExercise
import polski.core.model.CoreFormChange
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.LexicalFilter
import polski.core.model.SkillSpec

/** Values must be in [0, 1); a caller may use any repeatable or platform source (existing port shape, `polski.training.RandomSource`). */
fun interface RandomSource {
    fun nextDouble(): Double
}

/** IDs are independent of content randomness and must be nonempty (existing port shape, `polski.training.ExerciseIdFactory`). */
fun interface ExerciseIdFactory {
    fun newId(): String
}

/** One pack's `exerciseCopy` lookup (`polski.data.exerciseCopy`) — opaque keys in, pack-authored text out. */
fun interface PackCopy {
    fun text(key: String): String
}

/** One pack's `{placeholder}` sentence templates (`polski.data.renderCoursePattern`). */
fun interface PackPattern {
    fun render(key: String, values: Map<String, String>): String
}

/** One pack's fixed case-sentence lead-in (`polski.data.caseSentencePrefix`, e.g. pl's present-tense "I see"/"this is" lead-ins). */
fun interface CasePrefix {
    fun of(caseId: String, numberId: String): String
}

/** One pack's closed pronoun paradigm (`polski.data.personalPronouns`). */
fun interface PronounForms {
    fun form(pronounKey: String, caseId: String): String
}

/** Locale-safe "uppercase the first character" — never assumed generic (Turkish-`i`-class bugs are real), always caller-supplied. */
fun interface TextCase {
    fun capitalizeFirst(value: String): String
}

/**
 * EnRuPackPlan.md §5 gap A: a lexical slot [ExerciseGenerator] fills from a pack-level constant
 * lexeme rather than from the drilled seed (`prep:role`, `aux:do`, `neg:not`, ...) — present in
 * every phrase when [whenFeature] is `null` (the existing `owner`/`verb` shape, generalized), or
 * only in the phrases whose own resolved [PhraseSpec.bundle] carries [whenFeature] as one of
 * [whenValues] (e.g. English's `neg` slot only when `Polarity=Neg`). Presence is therefore decided
 * per phrase, not once per exercise — two phrases in the same exercise (a chain step's `source`
 * and `expected`, say) can resolve different bundles and so disagree on whether the slot exists.
 */
data class ConstantSlot(
    val slot: String,
    val lexeme: String,
    val whenFeature: String? = null,
    val whenValues: List<String> = emptyList(),
)

/**
 * UniversalCorePlan.md §5.1/§5.3/§12 UC-07: the generic replacement for `ExerciseFactory.kt`'s
 * `when(skillId)` branches. [skills] is a language's `curriculum.json` (`SkillSpec`s, UC-06);
 * [recipes] is its `exercise-recipes.json` wiring (this task, [SkillRecipe]); [seeds] is its
 * sentence-seed pool. [defaultOwnerLexeme]/[verbLexeme] are that same file's pack-level defaults
 * (which possessive is the unmarked default owner; which verb a tense skill drills) — a
 * pack-specific content choice, so it is read from data rather than assumed here.
 * [constantSlots] (EnRuPackPlan.md §5 gap A) adds further pack-constant slots beyond `owner`/
 * `verb`, each present unconditionally or conditionally per [ConstantSlot]; pl needs none, so the
 * default is empty and pl's behavior is unchanged. Every other dependency is a port — no lexeme
 * text, no copy string and no language-specific rule is ever hardcoded here.
 */
class ExerciseGenerator(
    private val realizer: ConstructionRealizer,
    private val morphology: Morphology,
    private val lexemeFeatures: LexemeFeatures,
    private val skills: Map<String, SkillSpec>,
    private val recipes: Map<String, SkillRecipe>,
    private val seeds: List<Map<String, String>>,
    private val random: RandomSource,
    private val ids: ExerciseIdFactory,
    private val copy: PackCopy,
    private val pattern: PackPattern,
    private val casePrefix: CasePrefix,
    private val pronouns: PronounForms,
    private val textCase: TextCase,
    private val defaultOwnerLexeme: String,
    private val verbLexeme: String,
    private val constantSlots: List<ConstantSlot> = emptyList(),
) {
    private fun <T> pick(items: List<T>): T {
        val draw = random.nextDouble()
        require(draw >= 0.0 && draw < 1.0) { "RandomSource must return [0, 1)" }
        return items[(draw * items.size).toInt()]
    }

    private fun newId(): String = ids.newId().also { require(it.isNotEmpty()) { "Exercise ID must be nonempty" } }

    /**
     * A noun's inherent `Gender`, or `null` for a pack (e.g. en, EnRuPackPlan.md §1.1) whose
     * [LexemeFeatures] carries no such feature at all — English has no grammatical gender, so
     * [pronounKeyFor] treats a missing feature exactly like an unmapped value: it falls back to
     * [TextValue.Pronoun.genderToKey]'s own `"default"` entry, never a hardcoded assumption here.
     */
    private fun genderOf(nounId: String): String? = lexemeFeatures.of(nounId)[FeatureKey("Gender")]?.name

    private fun matchesFilter(candidate: Map<String, String>, filter: LexicalFilter): Boolean = filter.where.all { (property, values) ->
        val idProperty = "${filter.slot}Id"
        if (property == idProperty) {
            candidate.getValue(filter.slot) in values
        } else {
            val featureKey = FeatureKey(property.replaceFirstChar { it.uppercaseChar() })
            lexemeFeatures.of(candidate.getValue(filter.slot))[featureKey]?.name in values
        }
    }

    private fun candidatesFor(filter: LexicalFilter?): List<Map<String, String>> =
        if (filter == null) seeds else seeds.filter { matchesFilter(it, filter) }

    private fun bundleOf(features: Map<String, String>): Map<FeatureKey, FeatureValue> =
        features.entries.associate { (key, value) -> FeatureKey(key) to FeatureValue(value) }

    private fun constantSlotsFor(bundle: Map<String, String>): Map<String, String> = constantSlots
        .filter { it.whenFeature == null || bundle[it.whenFeature] in it.whenValues }
        .associate { it.slot to it.lexeme }

    private fun resolvePhrase(spec: PhraseSpec, lexicalSlots: Map<String, String>): String {
        val withOwner = if (spec.ownerLexeme == null) lexicalSlots else lexicalSlots + ("owner" to spec.ownerLexeme)
        val slots = withOwner + constantSlotsFor(spec.bundle)
        return realizer.realize(spec.construction, bundleOf(spec.bundle), slots).text
    }

    private fun resolveValue(value: TextValue, lexicalSlots: Map<String, String>): String = when (value) {
        is TextValue.Copy -> copy.text(value.key)
        is TextValue.Literal -> value.value
        is TextValue.Phrase -> resolvePhrase(value.spec, lexicalSlots)
        is TextValue.Capitalized -> textCase.capitalizeFirst(resolveValue(value.inner, lexicalSlots))
        is TextValue.Pronoun -> {
            val key = pronounKeyFor(value.genderToKey, lexicalSlots)
            pronouns.form(key, value.caseId)
        }
    }

    private fun pronounKeyFor(genderToKey: Map<String, String>, lexicalSlots: Map<String, String>): String {
        val nounId = lexicalSlots.getValue("noun")
        val gender = genderOf(nounId)
        return (gender?.let(genderToKey::get)) ?: genderToKey.getValue("default")
    }

    private fun resolveSpec(spec: TextSpec, lexicalSlots: Map<String, String>): String = when (spec) {
        is TextSpec.Direct -> resolveValue(spec.value, lexicalSlots)
        is TextSpec.Pattern -> pattern.render(spec.key, spec.values.mapValues { (_, v) -> resolveValue(v, lexicalSlots) })
        is TextSpec.Concat -> spec.parts.joinToString("") { resolveValue(it, lexicalSlots) }
        is TextSpec.Prefixed -> {
            val phraseText = resolvePhrase(spec.spec, lexicalSlots)
            val numberId = spec.spec.bundle["Number"] ?: "sg"
            if (spec.prefixCase == "voc") "${textCase.capitalizeFirst(phraseText)}${spec.punct}" else "${casePrefix.of(spec.prefixCase, numberId)} $phraseText${spec.punct}"
        }
    }

    private fun classify(fromText: String, toText: String, nounId: String): String = when {
        fromText == toText -> "unchanged"
        genderOf(nounId) == "m-personal" && nomSingularOf(nounId).endsWith("a") -> "personalA"
        else -> "changed"
    }

    private fun nomSingularOf(nounId: String): String =
        morphology.form("noun:$nounId", mapOf(FeatureKey("Number") to FeatureValue("sg"), FeatureKey("Case") to FeatureValue("nom")))

    private fun resolveChange(change: ChangeSpec, lexicalSlots: Map<String, String>): CoreFormChange {
        val from = resolveValue(change.from, lexicalSlots)
        val to = resolveValue(change.to, lexicalSlots)
        val reasonKey = when (val reason = change.reason) {
            is ReasonSpec.Fixed -> reason.key
            is ReasonSpec.Classified -> {
                val classification = classify(from, to, lexicalSlots.getValue("noun"))
                reason.rules.firstOrNull { it.whenClass == classification }?.key
                    ?: reason.rules.first { it.whenClass == "default" }.key
            }
        }
        return CoreFormChange(from, to, copy.text(reasonKey))
    }

    private fun lexicalSlotsFor(nounId: String, adjectiveId: String, owner: String = defaultOwnerLexeme): Map<String, String> =
        mapOf("noun" to nounId, "adjective" to adjectiveId, "owner" to owner, "verb" to verbLexeme)

    private fun staticExercise(skillId: String, s: StaticExercise): CoreExercise = CoreExercise(
        id = newId(), primarySkill = skillId, source = copy.text(s.sourceKey), prompt = copy.text(s.promptKey),
        expected = copy.text(s.expectedKey), accepted = s.acceptedKeys.map(copy::text), explanation = copy.text(s.explanationKey),
        tags = s.tags, changes = listOf(CoreFormChange(copy.text(s.changeFromKey), copy.text(s.changeToKey), copy.text(s.changeReasonKey))),
        slots = mapOf("noun" to s.nounId, "adjective" to s.adjectiveId, "owner" to defaultOwnerLexeme, "number" to "sg"),
    )

    fun generateForSkill(skillId: String, preferredSeed: Map<String, String>? = null): CoreExercise {
        skills[skillId] ?: error("Unknown skill $skillId")
        val recipe = recipes[skillId] ?: error("No recipe for skill $skillId")
        val candidates = candidatesFor(skills.getValue(skillId).lexicalFilter)
        val seed = preferredSeed?.takeIf { preferred -> candidates.any { it["noun"] == preferred["noun"] } } ?: pick(candidates)

        val staticOverride = recipe.staticOverride
        if (staticOverride != null) return staticExercise(skillId, staticOverride)

        var owner = defaultOwnerLexeme
        val prompt: String
        val draw = recipe.ownerDraw
        if (draw != null) {
            owner = pick(draw.pool)
            prompt = copy.text(draw.promptPrefixKey) + copy.text(draw.labelKeys.getValue(owner)) + copy.text(draw.promptSuffixKey)
        } else {
            prompt = copy.text(recipe.promptKey)
        }
        val lexicalSlots = lexicalSlotsFor(seed.getValue("noun"), seed.getValue("adjective"), owner)

        val tags = recipe.tags + listOfNotNull(if (recipe.includeNounGenderTag) genderOf(seed.getValue("noun")) else null)
        return CoreExercise(
            id = newId(), primarySkill = skillId,
            source = resolveSpec(recipe.source, lexicalSlots), prompt = prompt, expected = resolveSpec(recipe.expected, lexicalSlots),
            accepted = recipe.accepted.map { resolveSpec(it, lexicalSlots) }, explanation = copy.text(recipe.explanationKey),
            tags = tags, changes = recipe.changes.map { resolveChange(it, lexicalSlots) },
            slots = mapOf(
                "noun" to seed.getValue("noun"), "adjective" to seed.getValue("adjective"),
                "owner" to owner, "number" to (recipe.numberOverride ?: "sg"),
            ),
        )
    }

    fun generateChain(steps: List<ChainStepRecipe>, seed: Map<String, String>): List<CoreExercise> {
        require(steps.isNotEmpty()) { "generateChain needs at least one step" }
        val lexicalSlots = lexicalSlotsFor(seed.getValue("noun"), seed.getValue("adjective"))
        return steps.map { step ->
            CoreExercise(
                id = newId(), primarySkill = step.skillId,
                source = resolveSpec(step.source, lexicalSlots), prompt = copy.text(step.promptKey),
                expected = resolveSpec(step.expected, lexicalSlots), explanation = copy.text(step.explanationKey),
                tags = step.tags, changes = step.changes.map { resolveChange(it, lexicalSlots) },
                slots = mapOf(
                    "noun" to seed.getValue("noun"), "adjective" to seed.getValue("adjective"),
                    "owner" to step.ownerOut, "number" to "sg",
                ),
            )
        }
    }
}
