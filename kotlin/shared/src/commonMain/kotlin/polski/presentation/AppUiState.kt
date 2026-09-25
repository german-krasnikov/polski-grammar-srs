package polski.presentation

import kotlin.time.Instant
import polski.model.Evaluation
import polski.model.Exercise
import polski.progress.Progress
import polski.srs.SchedulePreview

enum class TrainingMode { Chain, Schedule, Focused }
enum class AppTab { Training, Vocabulary, Matrix, Progress }
enum class AnswerMode { Oral, Typed }
enum class ExplanationMethod { Logic, Situations }
enum class CardPhase { Question, Revealed, ChainComplete, NoDue }
enum class MatrixSection { Map, Cases, Verbs, Pronouns }
enum class LoadStatus { Loading, Ready, MigrationAvailable, RecoveryRequired, Unavailable }

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
    val answerMode: AnswerMode = AnswerMode.Oral,
    val explanationMethod: ExplanationMethod = ExplanationMethod.Logic,
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
) {
    val exerciseId: String? get() = exercise?.id
    val chainComplete: Boolean get() = phase == CardPhase.ChainComplete
}
