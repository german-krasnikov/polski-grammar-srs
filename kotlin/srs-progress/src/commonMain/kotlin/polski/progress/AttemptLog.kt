package polski.progress

import kotlinx.serialization.json.JsonObject

/** P1 blueprint (`Plans/Architecture/ATTEMPT-JOURNAL-BLUEPRINT.md` §P1.1): one graded or
 *  self-assessed learner action. [attemptId] reuses the already-unique `Exercise.id`
 *  (`ExerciseIdFactory`) — no second ID minting needed. Append-only by construction: the only
 *  mutation any caller performs is `events + newEvent` (never edits/removes an existing entry) —
 *  this is what makes AC1 ("первая попытка записывается один раз; исправление не заменяет
 *  ошибку") true by construction rather than by a rule someone has to remember. A correction is a
 *  **new** event with [correctionOf] set, never an edit of the original. */
data class AttemptEvent(
    val schemaVersion: Int = 1,
    val attemptId: String,
    val sessionId: String,
    val skillId: String,
    val seedKey: String,
    val kind: AttemptKind,
    val at: String,
    val localDay: String,
    val activeMillis: Long,
    val answerMode: String,
    val helpShown: Boolean,
    val disclosed: Boolean,
    val correct: Boolean?,
    val correctnessBasis: CorrectnessBasis,
    val selfRating: String,
    val correctionOf: String? = null,
    val sessionKind: String,
    val sessionSource: String,
    val experimentVariant: String? = null,
    val rubricVersion: Int,
    val contentVersion: String? = null,
    val scheduledNextDue: String? = null,
)

enum class AttemptKind { Practice, Probe, Retry }
enum class CorrectnessBasis { Evaluated, SelfReported, Ambiguous }

/** Age-pruned, append-only collection of [AttemptEvent]s. */
data class AttemptLog(val version: Int = 1, val events: List<AttemptEvent> = emptyList())

/** Retains the parsed legacy tree so unknown JSON/wire fields survive round-trips, same shape as
 *  [ProgressDocument]'s own [JsonObject] passthrough. */
data class AttemptLogDocument(val log: AttemptLog, val source: JsonObject = JsonObject(emptyMap()))
