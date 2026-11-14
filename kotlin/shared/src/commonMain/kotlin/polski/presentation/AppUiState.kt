package polski.presentation

import kotlin.time.Instant
import polski.cloze.ClozeSessionState
import polski.model.Evaluation
import polski.model.Exercise
import polski.data.skillById
import polski.progress.Progress
import polski.srs.SchedulePreview

enum class TrainingMode { Chain, Schedule, Focused, Daily }
enum class AppTab { Training, Vocabulary, Matrix, Progress, Cloze }
// SB-4 blueprint §5: TileBuilder is additive — every existing `state.answerMode == AnswerMode.Typed`/
// `== AnswerMode.Oral` check in TrainingCardModel.kt/TrainingStore.kt (no exhaustive `when` over
// this enum exists today) leaves TileBuilder to fall through exactly like Oral (no draft input,
// "Show answer" label) until SB-6+ wires its own UI — a deliberate, documented fallback, not an
// oversight (blueprint AC4.3).
enum class AnswerMode { Oral, Typed, TileBuilder }
enum class CardPhase { Question, Revealed, ChainComplete, NoDue, DailyComplete }
// `Lifehacks` (EnRuPackPlan.md §4.3): a Matrix sub-section like the other four, so it shares the
// reset-on-tab-entry/highlight/deep-link machinery below.
enum class MatrixSection { Map, Cases, Verbs, Pronouns, Lifehacks }
enum class LoadStatus { Loading, Ready, MigrationAvailable, RecoveryRequired, Unavailable }

/** M2 blueprint: a pending retry nudge after an error on [skillId] — [remaining] counts elapsed
 *  tasks (any mode, any outcome), decremented on every `rate()` call; the ticket matures and gets
 *  substituted in once it reaches 0. Session-only, same lifetime class as [DailySessionState] —
 *  never persisted, reset on every app load.
 *
 *  [originAttemptId] (P1 blueprint §P1.5): the erroring attempt's own `Exercise.id`, set at the
 *  point `rate()` enqueues this ticket. Carried through to the matured substitution's own
 *  `AttemptEvent.correctionOf` so a correction links back to the exact error it corrects. */
data class RetryTicket(val skillId: String, val remaining: Int, val originAttemptId: String)

/** P1 blueprint §P1.5: which action started the current session — a static classification of
 *  *which action* started it, minted once per session alongside [AppUiState.sessionId], never
 *  inferred later. */
enum class SessionSource { ManualPick, DailyButton, ScheduleDue, Chain, Reset, ExamMistake }

/** M10 blueprint: carried on [AppUiState.examRetryOrigin] from the moment a learner follows a
 *  "Потренировать навык" affordance after an exam mistake, until the targeted [skillId] is rated
 *  Good once — same transient, session-only lifetime class as [RetryTicket]/[DailySessionState].
 *  [verificationItemId] is `null` exactly when the exam host could not resolve a second linked
 *  item for [skillId] at the moment this origin was set (an under-authored skill; AC4's degrade
 *  case) — `TrainingStore` never computes it itself (it must not depend on `polski.exam`). */
data class ExamRetryOrigin(val examRouteId: String, val itemId: String, val skillId: String, val verificationItemId: String?)

/** Selection is reset on each entry to the matrix tab. */
data class MatrixSelection(
    val section: MatrixSection = MatrixSection.Map,
    val nounId: String = "wife",
    val adjectiveId: String = "beautiful",
    val ownerId: String = "my",
    val numberId: String = "sg",
    val verbId: String = "do",
    val feminineGroup: Boolean = false,
)

/** Immutable renderer snapshot. [now] is an absolute instant; [localDay] belongs to the host zone. */
data class AppUiState(
    val loadStatus: LoadStatus = LoadStatus.Loading,
    val tab: AppTab = AppTab.Training,
    val mode: TrainingMode = TrainingMode.Chain,
    val chain: List<Exercise> = emptyList(),
    val chainIndex: Int = 0,
    val seedIndex: Int = 0,
    val focusedSkillId: String? = null,
    val exercise: Exercise? = null,
    val phase: CardPhase = CardPhase.Question,
    val introPending: Boolean = false,
    /** M7 blueprint: true exactly while this skill's own first exposure should show its authored
     *  [polski.model.Skill.predictionQuestion] before [introPending]'s own `MethodIntro` — the
     *  same "gated once per skill" moment [introPending] already owns, one step earlier. `false`
     *  for every skill without a [polski.model.Skill.predictionQuestion] (the default: inactive). */
    val predictionPending: Boolean = false,
    val answerMode: AnswerMode = AnswerMode.Oral,
    /** UC-10: a display preference, not a diagnosis — switching it never creates a review. */
    val styleId: StyleId = StyleId.RuleFirst,
    val draft: String = "",
    val frozenAnswer: String? = null,
    val evaluation: Evaluation? = null,
    val showReference: Boolean = false,
    val showSkillPicker: Boolean = false,
    val matrixSelection: MatrixSelection = MatrixSelection(),
    val progress: Progress? = null,
    val dueCount: Int = 0,
    val todayCount: Int = 0,
    val nextDue: Instant? = null,
    val intervals: SchedulePreview? = null,
    val now: Instant? = null,
    val localDay: String? = null,
    val revision: Long = 0,
    val savedRevision: Long = 0,
    val error: String? = null,
    val pendingEffects: List<UiEffect> = emptyList(),
    /** SB-6 blueprint §1.1: derived from [exercise] alone, recomputed at every site that assigns a
     *  new [exercise] (never carried over from the previous card). `null` exactly when [exercise]
     *  doesn't support tiles ([tileBuilderStateFor]'s own documented fallback) — the UI treats
     *  `tileBuilder != null` as its single "is TileBuilder available" check. */
    val tileBuilder: TileBuilderState? = null,
    /** M3 blueprint: session-only, `null` outside [TrainingMode.Daily] — same documented-fallback
     *  pattern as [tileBuilder]. */
    val daily: DailySessionState? = null,
    /** M2 blueprint: pending retry nudges, oldest first. Never persisted — see [RetryTicket]. */
    val retryQueue: List<RetryTicket> = emptyList(),
    /** M2 blueprint: set exactly when the *current* [exercise] was served because a ticket
     *  matured; cleared on the next `rate()`. Drives the "Ещё раз: {skill}" badge. */
    val activeRetrySkillId: String? = null,
    /** P1 blueprint §P1.5: the [RetryTicket.originAttemptId] of the error this (matured) exercise
     *  corrects — `null` whenever [activeRetrySkillId] is `null`. Read by `rate()` to stamp the
     *  next `AttemptEvent.correctionOf`. */
    val activeRetryOriginAttemptId: String? = null,
    /** P1 blueprint §P1.5: minted once per session at each "mode start" action (`StartChain`,
     *  `StartSchedule`, `StartDaily`, `ChooseSkill`, the post-`ResetDecision` restart, and the
     *  first load) — `null` only before the very first of those has run. */
    val sessionId: String? = null,
    /** P1 blueprint §P1.5: which action started [sessionId]'s session — set once, alongside it. */
    val sessionSource: SessionSource? = null,
    /** M10 blueprint: non-null exactly while the current Focused session was started to correct
     *  an exam mistake — cleared once the targeted skill is rated Good, or on any other
     *  mode-starting action. See [ExamRetryOrigin]. */
    val examRetryOrigin: ExamRetryOrigin? = null,
    /** Cloze tab session state; null outside the Cloze tab or before first StartCloze. */
    val cloze: ClozeSessionState? = null,
) {
    val exerciseId: String? get() = exercise?.id
    val caseReferenceAvailable: Boolean get() = exercise?.let { skillById(it.primarySkill).caseReferenceAvailable } ?: true
    val chainComplete: Boolean get() = phase == CardPhase.ChainComplete
}
