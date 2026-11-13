package polski.core.model

/**
 * UniversalCorePlan.md §5.1: the realized surface text of one construction (a noun phrase, a
 * verb form, ...) plus where each lexical slot landed in [text] — the language-neutral basis for
 * diff/highlight (`EndingHighlight.ContrastPair`'s eventual generic replacement). [core-engine]'s
 * `ConstructionRealizer` is the only producer; nothing here names a language.
 */
data class RealizedSentence(val text: String, val slotSpans: Map<String, IntRange> = emptyMap())

/** A single surface-form change to highlight — the generic counterpart of `polski.model.FormChange`. */
data class CoreFormChange(val from: String, val to: String, val reason: String)

/** UniversalCorePlan.md-style: the realized "answer phrase" inside one CoreExercise.expected that
 *  TileGenerator decomposes into tiles — the RealizedSentence ExerciseGenerator would otherwise
 *  discard (today it keeps only `.text`). [construction]/[bundle] are the same PhraseSpec fields
 *  recipe.expected's embedded Phrase was realized with — TileGenerator needs them to ask
 *  ConstructionRealizer.slotFacts for each slot's category + exact required FeatureBundle. */
data class TilePhrase(
    val construction: String,
    val bundle: Map<String, String>,
    val text: String,
    val slotSpans: Map<String, IntRange>,
)

/**
 * UniversalCorePlan.md §5.1/§12 UC-07: the generic output of `ExerciseGenerator` — the same shape
 * `polski.model.Exercise` carries today, but with pl's hardcoded `nounId/adjectiveId/possessive/
 * number` fields folded into an open [slots] map (`"noun" -> "wife"`, `"owner" -> "my"`, `"number"
 * -> "sg"`, ...) so a language without those exact slots isn't forced to declare them. A :shared
 * adapter maps this 1:1 onto `polski.model.Exercise` for the parity gate against `ExerciseFactory`
 * (§5.3) — `:core-engine` never imports that type, keeping the dependency direction `:shared ->
 * :core-engine`, never the reverse.
 */
data class CoreExercise(
    val id: String,
    val primarySkill: String,
    val source: String,
    val prompt: String,
    val expected: String,
    val accepted: List<String> = emptyList(),
    val explanation: String,
    val tags: List<String>,
    val changes: List<CoreFormChange>,
    val slots: Map<String, String> = emptyMap(),
    /** SB-5: which [slots] keys the recipe allows a sentence-builder UI to turn into a Choice tile.
     *  Empty = this skill has no sentence-builder support (today's default — every existing recipe). */
    val tileTargetSlots: List<String> = emptyList(),
    /** Null when [tileTargetSlots] is empty, OR when ExerciseGenerator could not isolate exactly
     *  one embedded Phrase inside recipe.expected (e.g. a `Prefixed` expected with `prefixCase ==
     *  "voc"`, whose capitalization breaks verbatim substring matching against [expected]) —
     *  TileGenerator must treat null as "no tiles for this instance", never throw. */
    val tilePhrase: TilePhrase? = null,
)
