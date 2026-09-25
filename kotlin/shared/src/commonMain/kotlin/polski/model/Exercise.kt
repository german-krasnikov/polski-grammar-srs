package polski.model

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
)

data class Evaluation(
    val correct: Boolean,
    val normalized: String,
    val expected: String,
    val distance: Int,
)
