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
)
