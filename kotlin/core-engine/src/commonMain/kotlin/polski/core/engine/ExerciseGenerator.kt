package polski.core.engine

import polski.core.model.CoreExercise
import polski.core.model.CoreFormChange
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.LexicalFilter
import polski.core.model.RealizedSentence
import polski.core.model.SkillSpec
import polski.core.model.TilePhrase

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
 * M4/M5/M2 blueprint: one shared "which lexical seed" string, built from a generator-side
 * candidate map (`noun`/`adjective` keys — every [ExerciseGenerator.seeds] entry has both). Used
 * for M4's anti-repeat, M5's weak-form bias lookup, and M2's retry substitution — all three reuse
 * this exact format rather than inventing their own. `polski.model.seedKey(Exercise)` is the
 * presentation-side twin (same string shape, different input shape — see its own KDoc).
 */
fun seedKey(candidate: Map<String, String>): String = "${candidate.getValue("noun")}:${candidate.getValue("adjective")}"

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
    private val remainingStaticIndexes = mutableMapOf<String, MutableList<Int>>()
    private val lastStaticIndex = mutableMapOf<String, Int>()
    /** M4: the last dynamic-seed [seedKey] served for this skill — same per-skill "last thing
     *  shown" shape as [lastStaticIndex], but for the `pick(candidates)` path, which had no
     *  anti-repeat before. */
    private val lastSeedKey = mutableMapOf<String, String>()

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

    /** P1 blueprint §P1.3 (AC6): every *normal* practice/schedule/daily pick excludes held-out
     *  probe seeds before the existing M4 anti-repeat filter — a probe seed must never surface as
     *  an ordinary exercise. Falls back to the unfiltered pool when excluding probe seeds would
     *  leave nothing to pick from (same fail-soft shape as [pickSeed]'s own anti-repeat fallback;
     *  needed for a skill whose entire candidate pool happens to hash into a probe bucket). */
    private fun trainingCandidatesFor(skillId: String, filter: LexicalFilter?): List<Map<String, String>> {
        val all = candidatesFor(filter)
        return all.filter { poolOf(skillId, it) == ContentPool.Training }.ifEmpty { all }
    }

    private fun bundleOf(features: Map<String, String>): Map<FeatureKey, FeatureValue> =
        features.entries.associate { (key, value) -> FeatureKey(key) to FeatureValue(value) }

    private fun constantSlotsFor(bundle: Map<String, String>): Map<String, String> = constantSlots
        .filter { it.whenFeature == null || bundle[it.whenFeature] in it.whenValues }
        .associate { it.slot to it.lexeme }

    /** SB-5 blueprint §6: returns the full [RealizedSentence] (slot spans included), unlike
     *  [resolvePhrase]'s `.text`-only shortcut — [generateForSkill] needs the spans to build
     *  [CoreExercise.tilePhrase] without re-running [ConstructionRealizer.realize]. */
    private fun realizePhrase(spec: PhraseSpec, lexicalSlots: Map<String, String>): RealizedSentence {
        val withOwner = if (spec.ownerLexeme == null) lexicalSlots else lexicalSlots + ("owner" to spec.ownerLexeme)
        val slots = withOwner + constantSlotsFor(spec.bundle)
        return realizer.realize(spec.construction, bundleOf(spec.bundle), slots)
    }

    private fun resolvePhrase(spec: PhraseSpec, lexicalSlots: Map<String, String>): String = realizePhrase(spec, lexicalSlots).text

    /**
     * SB-5 blueprint §6 eligibility constraint: the sole embedded [TextValue.Phrase] inside
     * [spec], when [spec] has exactly one — a `Direct(Phrase)`, the sole `Phrase`-typed entry of
     * a `Pattern`'s `values`, the sole `Phrase` among a `Concat`'s `parts`, or `Prefixed` with
     * `prefixCase != "voc"` (whose capitalization would otherwise break verbatim substring
     * matching against `expected`). `null` (fail-soft, not an error) for every other shape —
     * content authors find out at tile-UI-time (empty tile picker), not at `biome generate` time.
     */
    private fun singlePhraseSpec(spec: TextSpec): PhraseSpec? = when (spec) {
        is TextSpec.Direct -> (spec.value as? TextValue.Phrase)?.spec
        is TextSpec.Pattern -> spec.values.values.filterIsInstance<TextValue.Phrase>().singleOrNull()?.spec
        is TextSpec.Concat -> spec.parts.filterIsInstance<TextValue.Phrase>().singleOrNull()?.spec
        is TextSpec.Prefixed -> if (spec.prefixCase == "voc") null else spec.spec
    }

    /** SB-5 blueprint §6: [CoreExercise.tilePhrase] for a constructed recipe whose
     *  [SkillRecipe.tileTargetSlots] opts in, or `null` (fail-soft) when [singlePhraseSpec] can't
     *  isolate exactly one embedded phrase in [recipe]'s `expected`. */
    private fun tilePhraseFor(recipe: SkillRecipe, lexicalSlots: Map<String, String>): TilePhrase? {
        if (recipe.tileTargetSlots.isEmpty()) return null
        val phraseSpec = singlePhraseSpec(recipe.expected) ?: return null
        val realized = realizePhrase(phraseSpec, lexicalSlots)
        return TilePhrase(phraseSpec.construction, phraseSpec.bundle, realized.text, realized.slotSpans)
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

    private fun nomSingularOf(nounId: String): String =
        morphology.form("noun:$nounId", mapOf(FeatureKey("Number") to FeatureValue("sg"), FeatureKey("Case") to FeatureValue("nom")))

    private fun resolveChange(change: ChangeSpec, lexicalSlots: Map<String, String>): CoreFormChange {
        val from = resolveValue(change.from, lexicalSlots)
        val to = resolveValue(change.to, lexicalSlots)
        val reasonKey = when (val reason = change.reason) {
            is ReasonSpec.Fixed -> reason.key
            is ReasonSpec.Classified -> {
                val nounId = lexicalSlots.getValue("noun")
                reason.rules.first { rule ->
                    (rule.whenUnchanged != true || from == to) &&
                        (rule.whenGender == null || rule.whenGender == genderOf(nounId)) &&
                        (rule.whenNomSingularSuffix == null || nomSingularOf(nounId).endsWith(rule.whenNomSingularSuffix))
                }.key
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

    /**
     * M4: when no [preferredSeed] is given and the candidate pool has more than one lexical
     * choice, excludes whichever candidate matches [lastSeedKey] for this skill before drawing —
     * so two consecutive calls never repeat the same noun+adjective pair. Falls back to the full
     * pool when that exclusion would leave nothing to pick from (a skill with exactly one lexical
     * candidate must still generate every time).
     *
     * M5: when [weakSeedCount] is non-null, draws one extra decision after that anti-repeat pool
     * is built: `< 0.5` restricts the pick to the pool's candidates with a positive weak-seed
     * count (falling back to the full pool when none have one), weighted toward the maximum count
     * — the single highest-count candidate wins outright, ties broken by a further [pick] draw
     * over just the tied subset. `>= 0.5` (or [weakSeedCount] `null`) falls through to the
     * existing uniform [pick] over the whole pool — one [RandomSource] draw per decision, same
     * contract every other call in this class already follows.
     */
    private fun pickSeed(skillId: String, candidates: List<Map<String, String>>, weakSeedCount: ((String) -> Int)?): Map<String, String> {
        val pool = if (candidates.size > 1) {
            candidates.filter { seedKey(it) != lastSeedKey[skillId] }.ifEmpty { candidates }
        } else candidates
        if (weakSeedCount == null || random.nextDouble() >= 0.5) return pick(pool)
        val weak = pool.filter { weakSeedCount(seedKey(it)) > 0 }.ifEmpty { return pick(pool) }
        val maxCount = weak.maxOf { weakSeedCount(seedKey(it)) }
        return pick(weak.filter { weakSeedCount(seedKey(it)) == maxCount })
    }

    fun generateForSkill(
        skillId: String,
        preferredSeed: Map<String, String>? = null,
        /** M5 blueprint: `null` (every pre-M5 call site) reproduces today's uniform behavior
         *  exactly — no extra draw, no bias. A host opts in by passing the closure. */
        weakSeedCount: ((candidateSeedKey: String) -> Int)? = null,
        /** P1 blueprint §P1.3 (AC6): `false` (every pre-P1 call site, including
         *  `TrainingParityTest`'s own pinned React-oracle fixture) reproduces today's exact
         *  candidate pool and is therefore a no-op for byte-parity — a host opts in by passing
         *  `true` for every *normal* (non-probe) pick, same opt-in shape as [weakSeedCount]. */
        excludeProbePool: Boolean = false,
    ): CoreExercise {
        skills[skillId] ?: error("Unknown skill $skillId")
        val recipe = recipes[skillId] ?: error("No recipe for skill $skillId")
        val filter = skills.getValue(skillId).lexicalFilter
        val candidates = if (excludeProbePool) trainingCandidatesFor(skillId, filter) else candidatesFor(filter)
        val seed = preferredSeed?.takeIf { preferred -> candidates.any { it["noun"] == preferred["noun"] } } ?: pickSeed(skillId, candidates, weakSeedCount)
        lastSeedKey[skillId] = seedKey(seed)
        return buildExercise(skillId, recipe, seed)
    }

    /** P1 blueprint §P1.3: same realization pipeline as [generateForSkill], but picks uniformly
     *  (no M4 anti-repeat, no M5 weak-form bias — a probe is not supposed to be adapted) from the
     *  held-out [window] pool instead of [ContentPool.Training]. Scoped to the dynamic-seed path
     *  only: a skill whose recipe is `staticOverride`/`staticPool`-driven has no seed pool for
     *  [poolOf] to partition, so it is not a probe candidate under this blueprint. */
    fun generateProbeExercise(skillId: String, window: ContentPool): CoreExercise {
        require(window != ContentPool.Training) { "generateProbeExercise needs a probe window, not Training" }
        skills[skillId] ?: error("Unknown skill $skillId")
        val recipe = recipes[skillId] ?: error("No recipe for skill $skillId")
        require(recipe.staticOverride == null && recipe.staticPool.isEmpty()) {
            "Skill $skillId has no seed-based pool to probe"
        }
        val candidates = candidatesFor(skills.getValue(skillId).lexicalFilter).filter { poolOf(skillId, it) == window }
        require(candidates.isNotEmpty()) { "No $window candidates for skill $skillId" }
        val seed = pick(candidates)
        return buildExercise(skillId, recipe, seed)
    }

    private fun buildExercise(skillId: String, recipe: SkillRecipe, seed: Map<String, String>): CoreExercise {
        val staticOverride = recipe.staticOverride
        if (staticOverride != null) return staticExercise(skillId, staticOverride)
        if (recipe.staticPool.isNotEmpty()) {
            val remaining = remainingStaticIndexes.getOrPut(skillId) { mutableListOf() }
            val startingCycle = remaining.isEmpty()
            if (remaining.isEmpty()) {
                remaining += recipe.staticPool.indices
            }
            val choices = if (startingCycle && remaining.size > 1) remaining.filter { it != lastStaticIndex[skillId] } else remaining
            val index = pick(choices)
            remaining.remove(index)
            lastStaticIndex[skillId] = index
            return staticExercise(skillId, recipe.staticPool[index])
        }

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
            tileTargetSlots = recipe.tileTargetSlots,
            tilePhrase = tilePhraseFor(recipe, lexicalSlots),
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
