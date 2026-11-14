package polski.model

import polski.core.model.TilePhrase

data class SentenceSeed(val nounId: String, val adjectiveId: String)

data class FormChange(val from: String, val to: String, val reason: String)

/** Exercise content; the caller supplies a stable [id] and owns collection snapshots. */
data class Exercise(
    val id: String,
    val primarySkill: String,
    val source: String,
    val prompt: String,
    val expected: String,
    val accepted: List<String> = emptyList(),
    val explanation: String,
    val tags: List<String>,
    val nounId: String,
    val adjectiveId: String,
    val possessive: PossessiveId,
    val number: NumberGram,
    val changes: List<FormChange>,
    /** SB-4 blueprint §5: `:core-model`'s [CoreExercise][polski.core.model.CoreExercise] carries
     *  the same two fields — duplicated here (not read off `CoreExercise` directly) because
     *  `AppUiState.exercise` is this pl-specific adapter, not the generic core type. */
    val tileTargetSlots: List<String> = emptyList(),
    val tilePhrase: TilePhrase? = null,
)

data class Evaluation(
    val correct: Boolean,
    val normalized: String,
    val expected: String,
    val distance: Int,
)

/**
 * M4/M5/M2 blueprint: the presentation-side twin of `polski.core.engine.seedKey(Map<String, String>)` —
 * same `"$nounId:$adjectiveId"` string format, kept as a separate function (not a shared
 * dependency) because [Exercise] and the generator's candidate map are different shapes. Used to
 * compare an [Exercise]'s seed against `SkillStats.seedErrors` keys (M5) and against the seed of
 * the error that enqueued an M2 retry ticket.
 */
fun seedKey(exercise: Exercise): String = "${exercise.nounId}:${exercise.adjectiveId}"
