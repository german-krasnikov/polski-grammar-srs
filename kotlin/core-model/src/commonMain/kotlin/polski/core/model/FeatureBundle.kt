package polski.core.model

/**
 * An open catalog key for a grammatical/semantic feature axis — a UD `feat`/UniMorph schema name
 * such as `"Case"`, `"Tense"`, `"Polarity"` (UniversalCorePlan.md §1.1). `:core-model` never
 * hardcodes the set of keys that exist; a language declares the subset it uses.
 */
data class FeatureKey(val name: String)

/** An open catalog value for a [FeatureKey], e.g. `"Gen"`, `"Past"`, `"Neg"` (UniversalCorePlan.md §1.1). */
data class FeatureValue(val name: String)

/**
 * One "конструкция × набор признаков" cell (UniversalCorePlan.md §0/§1.1): the realized state of
 * a set of feature axes, before any language-specific realization decides how to express it.
 */
typealias FeatureBundle = Map<FeatureKey, FeatureValue>

/**
 * A language-independent unit of meaning (UniversalCorePlan.md §2) — e.g. `core.verb.tense`,
 * `core.sentence.polarity`. [axes] names the [FeatureKey]s this construction varies over.
 * [composesOf] names the other [Construction] ids a composite construction (`core.composite`)
 * applies in order to the same [FeatureBundle] — sequencing is the caller's responsibility;
 * conflict resolution across chained steps on the same axis is an open risk (UniversalCorePlan.md §2, §10.1).
 */
data class Construction(
    val id: String,
    val axes: Set<FeatureKey> = emptySet(),
    val composesOf: List<String> = emptyList(),
)

/**
 * Which [FeatureKey] a skill drills, and the [FeatureValue] it moves the learner from and to —
 * e.g. pl's `case.gen.neg` focuses `Polarity: Pos -> Neg` (UniversalCorePlan.md §3.2).
 */
data class FeatureFocus(val feature: FeatureKey, val from: FeatureValue, val to: FeatureValue)

/**
 * Restricts which lexical candidates a [SkillSpec] draws from — e.g. pl's `case.acc.f` restricts
 * the noun [slot] to a `gender` value, `case.inst` restricts it to a `nounId` set
 * (`ExerciseFactory.kt`'s per-skill `sentenceSeeds.filter { ... }`, UniversalCorePlan.md §3.2,
 * §12 UC-06). Each `where` entry names a candidate property and the values it must be one of;
 * `null` on [SkillSpec.lexicalFilter] means the skill draws from every candidate, unfiltered.
 */
data class LexicalFilter(val slot: String, val where: Map<String, List<String>>)

/**
 * A drillable skill: the [Construction] it exercises, the axis it [focus]es on (if any), the
 * other axes it holds [fixed] while doing so, and the candidates [lexicalFilter] restricts it to
 * (UniversalCorePlan.md §3.2, `lang/pl/curriculum.json`). [focus] is `null` for a skill whose
 * code has no single feature-value flip — a random pick among several targets, a lexical
 * substitution, or a composite of several changes at once.
 * [id] is stable across engine changes (UniversalCorePlan.md §1.7) — core code never renames it.
 */
data class SkillSpec(
    val id: String,
    val construction: String,
    val focus: FeatureFocus? = null,
    val fixed: FeatureBundle = emptyMap(),
    val lexicalFilter: LexicalFilter? = null,
    val level: String? = null,
    val prerequisites: List<String> = emptyList(),
)
