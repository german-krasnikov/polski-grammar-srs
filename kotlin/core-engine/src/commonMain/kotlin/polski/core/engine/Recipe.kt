package polski.core.engine

/**
 * UniversalCorePlan.md §3.1/§5.2/§12 UC-07: the data `ExerciseGenerator` reads in place of
 * `ExerciseFactory.kt`'s per-skill `when(skillId)` branches — the exercise-kind wiring §3.1
 * reserves as `core/exercise-kinds.json`, made concrete here for pl (`lang/pl/exercise-
 * recipes.json`, authored for this task). Every field is either a construction/feature/case
 * identifier already used by [ConstructionRealizer]/`:core-model`, or an opaque copy-key string
 * resolved through [PackCopy]/[PackPattern] — never a literal word in any language.
 */

/** Which construction+bundle+lexeme realizes one phrase/word ([ownerLexeme] overrides the "owner" slot only). */
data class PhraseSpec(val construction: String, val bundle: Map<String, String>, val ownerLexeme: String? = null)

sealed interface TextValue {
    /** A pack copy string, looked up verbatim. */
    data class Copy(val key: String) : TextValue

    /** A value fixed by the recipe itself (e.g. the empty string a negation particle is inserted before). */
    data class Literal(val value: String) : TextValue

    /** One [ConstructionRealizer] result. */
    data class Phrase(val spec: PhraseSpec) : TextValue

    /** The same value with its first character capitalized ([TextCase], never a hardcoded rule here). */
    data class Capitalized(val inner: TextValue) : TextValue

    /** A closed pronoun paradigm selected by the drilled noun's own gender, then looked up by case. */
    data class Pronoun(val genderToKey: Map<String, String>, val caseId: String) : TextValue
}

sealed interface TextSpec {
    data class Direct(val value: TextValue) : TextSpec

    /** [key] renders through [PackPattern] with each named placeholder resolved from [values]. */
    data class Pattern(val key: String, val values: Map<String, TextValue>) : TextSpec

    /** Concatenates [parts] with no separator (e.g. agreement.my's "Replace 'my' with «" + label + "»."). */
    data class Concat(val parts: List<TextValue>) : TextSpec

    /**
     * A [ConstructionTemplate]'s own realized case sentence: `prefix + " " + phrase + punct`, or
     * (for [prefixCase] `"voc"`) a capitalized phrase alone followed by [punct] — a pack's own
     * sentence-terminal mark (`"."`, `"!"`, ...), never a Kotlin literal.
     */
    data class Prefixed(val spec: PhraseSpec, val prefixCase: String, val punct: String) : TextSpec
}

data class ReasonRule(val whenClass: String, val key: String)

sealed interface ReasonSpec {
    data class Fixed(val key: String) : ReasonSpec

    /** Branches on a caller-supplied classifier name (e.g. `"unchanged"`/`"personalA"`/`"changed"`); [ReasonRule.whenClass] `"default"` is the fallback. */
    data class Classified(val rules: List<ReasonRule>) : ReasonSpec
}

data class ChangeSpec(val from: TextValue, val to: TextValue, val reason: ReasonSpec)

/** agreement.my's second random draw: a pool of owner lexemes plus the copy key labelling each in the prompt. */
data class OwnerDraw(val pool: List<String>, val labelKeys: Map<String, String>, val promptPrefixKey: String, val promptSuffixKey: String)

/** aspect: fully pre-authored content (UniversalCorePlan.md §5.2's "authored overrides" escape hatch) — no realization at all. */
data class StaticExercise(
    val sourceKey: String,
    val promptKey: String,
    val expectedKey: String,
    val explanationKey: String,
    val acceptedKeys: List<String>,
    val changeFromKey: String,
    val changeToKey: String,
    val changeReasonKey: String,
    val nounId: String,
    val adjectiveId: String,
    val tags: List<String>,
)

/** One skill's recipe — everything `ExerciseGenerator.generateForSkill` needs beyond the [polski.core.model.SkillSpec] it's paired with. */
data class SkillRecipe(
    val skillId: String,
    val source: TextSpec = TextSpec.Direct(TextValue.Literal("")),
    val expected: TextSpec = TextSpec.Direct(TextValue.Literal("")),
    val promptKey: String = "",
    val explanationKey: String = "",
    val changes: List<ChangeSpec> = emptyList(),
    val accepted: List<TextSpec> = emptyList(),
    val tags: List<String> = emptyList(),
    val includeNounGenderTag: Boolean = false,
    val ownerDraw: OwnerDraw? = null,
    val numberOverride: String? = null,
    val staticOverride: StaticExercise? = null,
)

/**
 * One `generateChain` step — always realized against the caller-given seed, never a candidate
 * draw. Every [TextValue.Phrase] names its own owner via [PhraseSpec.ownerLexeme] where it differs
 * from the pack's default owner lexeme (e.g. the chain's last two steps, after `agreement.my`
 * swaps the owner to `"their"`) — a step has no single owner of its own, since its `source` still
 * reflects the *previous* step's owner while its `expected` may introduce a new one.
 */
data class ChainStepRecipe(
    val skillId: String,
    val source: TextSpec,
    val expected: TextSpec,
    val promptKey: String,
    val explanationKey: String,
    val changes: List<ChangeSpec>,
    val tags: List<String> = emptyList(),
    /** The step's *resulting* owner (`CoreExercise.slots["owner"]`) — independent of any per-[TextValue.Phrase] owner override above; the pack recipe supplies it explicitly (no engine-side default). */
    val ownerOut: String,
)
