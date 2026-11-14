package polski.mission

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import polski.grammar.normalize
import polski.mission.data.MISSION_CHOICE_ROW_ROWS
import polski.mission.data.MISSION_NODE_ROW_ROWS
import polski.mission.data.MISSION_ROW_ROWS
import polski.mission.data.MissionNodeRow
import polski.mission.data.MissionNodeRowOutcome

/** M9 blueprint AC3: [meaningCorrect] and [formCorrect] are reported independently of each other
 *  — a grammatically correct but goal-irrelevant reply ([meaningCorrect] false, [formCorrect]
 *  true) is distinguishable from a goal-correct but misspelled one ([meaningCorrect] true,
 *  [formCorrect] false); never merged into one pass/fail. [reachedGoal] is true exactly when
 *  this choice ended the mission on its `goal_reached` terminal node — false both mid-mission and
 *  on a `dead_end` ending. */
data class MissionOutcome(val meaningCorrect: Boolean, val formCorrect: Boolean, val reachedGoal: Boolean)

/** The native-language gloss shown on a choice button — [polski.mission.data.MissionChoiceRow]'s
 *  own `expectedForm`/`acceptedVariant` stay hidden until [MissionSession.choose] grades them. */
data class MissionChoiceView(val choiceId: String, val label: String)

data class MissionUiState(
    val missionId: String? = null,
    val nodeId: String? = null,
    val situation: String = "",
    val prompt: String = "",
    val choices: List<MissionChoiceView> = emptyList(),
    /** True exactly on the current (terminal) node — no further choices, [outcome] is set. */
    val completed: Boolean = false,
    val outcome: MissionNodeRowOutcome? = null,
    /** The grading of whichever choice just led to this node — `null` only at the mission's very
     *  first node, before any [MissionSession.choose] call. */
    val lastResult: MissionOutcome? = null,
    val error: String? = null,
)

/** Same shape as `polski.vocabulary.VocabularyRepository` — the host owns atomic persistence. */
interface MissionRepository {
    suspend fun loadRaw(): String?
    suspend fun saveRaw(value: String)
}

/**
 * M9 blueprint MVP: a hand-authored branching dialogue session, one mission at a time — no FSRS
 * scheduling, no attempt-journal integration (both named follow-ups, see the blueprint's own
 * Decisions section: mission outcomes stay host-local in [MissionDocument] for now). Grammar
 * grading reuses the exact normalize-and-compare rule `polski.training.evaluate`/
 * `polski.exam.grammar.GrammarExamScorer` already use — a third copy of the same one-liner, not
 * new text-matching logic.
 */
class MissionSession(private val repository: MissionRepository, private val now: () -> Instant) {
    private val mutableState = MutableStateFlow(MissionUiState())
    val state: StateFlow<MissionUiState> = mutableState.asStateFlow()
    private var document = MissionDocument()

    /** Loads the persisted [MissionDocument] (a missing/corrupt one fails soft to an empty one —
     *  mission progress is a low-stakes, additive fact, never a blocker for playing) and opens
     *  [missionId] on its own first authored node (the node no choice in this mission ever names
     *  as its `nextNodeRef` — in practice the first [polski.mission.data.MissionNodeRow] authored
     *  for it). */
    suspend fun start(missionId: String) {
        val raw = runCatching { repository.loadRaw() }.getOrNull()
        document = raw?.let { runCatching { MissionCodec.decode(it) }.getOrNull() } ?: MissionDocument()
        val mission = MISSION_ROW_ROWS.firstOrNull { it.missionId == missionId }
        val startNode = MISSION_NODE_ROW_ROWS.firstOrNull { it.missionRef == missionId }
        if (mission == null || startNode == null) {
            mutableState.value = MissionUiState(error = "Миссия не найдена: $missionId")
            return
        }
        mutableState.value = stateFor(missionId, startNode, lastResult = null)
    }

    private fun stateFor(missionId: String, node: MissionNodeRow, lastResult: MissionOutcome?): MissionUiState {
        val choices = MISSION_CHOICE_ROW_ROWS.filter { it.nodeRef == node.nodeId }
            .map { MissionChoiceView(it.choiceId, it.label) }
        return MissionUiState(
            missionId = missionId, nodeId = node.nodeId, situation = node.situation, prompt = node.prompt,
            choices = choices, completed = node.isTerminal, outcome = node.outcome.takeIf { node.isTerminal },
            lastResult = lastResult,
        )
    }

    fun choose(choiceId: String, typedForm: String): MissionOutcome {
        val current = state.value
        val choice = MISSION_CHOICE_ROW_ROWS.firstOrNull { it.choiceId == choiceId && it.nodeRef == current.nodeId }
            ?: return MissionOutcome(meaningCorrect = false, formCorrect = false, reachedGoal = false)
        val accepted = listOfNotNull(choice.expectedForm, choice.acceptedVariant).map(::normalize)
        val formCorrect = normalize(typedForm) in accepted
        val nextNode = choice.nextNodeRef?.let { ref -> MISSION_NODE_ROW_ROWS.firstOrNull { it.nodeId == ref } }
        val reachedGoal = nextNode?.isTerminal == true && nextNode.outcome == MissionNodeRowOutcome.GOAL_REACHED
        val result = MissionOutcome(meaningCorrect = choice.meaningAdvancesGoal, formCorrect = formCorrect, reachedGoal = reachedGoal)
        val missionId = current.missionId
        if (nextNode != null && missionId != null) {
            mutableState.value = stateFor(missionId, nextNode, result)
        }
        return result
    }

    /** Persists `missionId -> last outcome/date` once [MissionUiState.completed] is true — same
     *  load/save-and-verify discipline `VocabularySession`/`TrainingStore` already use (write,
     *  then read back and compare) so a failed write surfaces as [state]'s own [MissionUiState.error]
     *  instead of a silently lost completion. A no-op (returns `false`) before the mission reaches
     *  a terminal node. */
    suspend fun complete(): Boolean {
        val current = state.value
        val missionId = current.missionId
        if (missionId == null || !current.completed) return false
        val outcomeTag = if (current.outcome == MissionNodeRowOutcome.GOAL_REACHED) "goal_reached" else "dead_end"
        val next = document.copy(progress = document.progress + (missionId to MissionProgressEntry(outcomeTag, now().toString())))
        return try {
            val serialized = MissionCodec.encode(next)
            repository.saveRaw(serialized)
            check(repository.loadRaw() == serialized) { "Прогресс миссии не прошёл проверку записи" }
            document = next
            true
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            mutableState.update { it.copy(error = error.message ?: "Не удалось сохранить прогресс миссии") }
            false
        }
    }
}
