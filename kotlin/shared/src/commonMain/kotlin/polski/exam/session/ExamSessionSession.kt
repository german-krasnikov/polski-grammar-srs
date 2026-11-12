package polski.exam.session

import polski.exam.*
import polski.exam.grammar.*
import polski.exam.listening.*
import polski.exam.reading.*
import polski.exam.writing.*
import polski.exam.sessionpolicy.data.SessionPolicyOutcomeRowEffect

/** Reducer snapshot. The host supplies wall time for every event. */
data class ExamSessionState(
    val config: ExamSessionConfig, val currentModuleIndex: Int = 0,
    val listening: ListeningExamState = ListeningExamState(config.listening),
    val reading: ReadingExamState = ReadingExamState(config.reading),
    val grammar: GrammarExamState = GrammarExamState(mergeGrammarTests(config.examId, config.grammar)),
    val writing: WritingExamState = WritingExamState(config.writing),
    val deadlineEpochMs: Long? = null, val frozenResults: Map<ExamModule, ExamPartScore> = emptyMap(),
    val completed: Boolean = false,
)
sealed interface ExamSessionAction {
    data class Start(val nowEpochMs: Long) : ExamSessionAction
    data class PartAction(val module: ExamModule, val action: ExamPartAction, val nowEpochMs: Long) : ExamSessionAction
    data class FinishPart(val module: ExamModule, val nowEpochMs: Long) : ExamSessionAction
    data class Tick(val nowEpochMs: Long) : ExamSessionAction
}
sealed interface ExamPartAction {
    data class Listening(val action: ListeningExamAction) : ExamPartAction
    data class Reading(val action: ReadingExamAction) : ExamPartAction
    data class Grammar(val action: GrammarExamAction) : ExamPartAction
    data class Writing(val action: WritingExamAction) : ExamPartAction
}
/** Applies one event; a stale part identity is ignored before its deadline is checked. */
fun reduceExamSession(state: ExamSessionState, action: ExamSessionAction): ExamSessionState {
    val module = state.config.modules.getOrNull(state.currentModuleIndex)?.module
    val event = when (action) {
        is ExamSessionAction.Start -> "start"
        is ExamSessionAction.PartAction -> "part_action"
        is ExamSessionAction.FinishPart -> "finish_part"
        is ExamSessionAction.Tick -> "tick"
    }
    val now = when (action) {
        is ExamSessionAction.Start -> action.nowEpochMs
        is ExamSessionAction.PartAction -> action.nowEpochMs
        is ExamSessionAction.FinishPart -> action.nowEpochMs
        is ExamSessionAction.Tick -> action.nowEpochMs
    }
    val matches = when (action) {
        is ExamSessionAction.PartAction -> action.module == module
        is ExamSessionAction.FinishPart -> action.module == module
        else -> true
    }
    val disposition = examSessionEventDisposition(
        event = event, completed = state.completed, started = state.deadlineEpochMs != null,
        moduleMatchesCurrent = matches,
        deadlineReached = state.deadlineEpochMs?.let { now >= it } ?: false,
    )
    return when (disposition) {
        SessionPolicyOutcomeRowEffect.IGNORE -> state
        SessionPolicyOutcomeRowEffect.SET_DEADLINE -> state.copy(deadlineEpochMs = deadline(now, state.config.modules[0].durationSeconds))
        SessionPolicyOutcomeRowEffect.FREEZE -> finishCurrent(state, now)
        SessionPolicyOutcomeRowEffect.DISPATCH_PART_ACTION -> {
            action as ExamSessionAction.PartAction
            when (module) {
                ExamModule.Listening -> if (action.action is ExamPartAction.Listening) state.copy(listening = reduceListeningExam(state.listening, action.action.action)) else state
                ExamModule.Reading -> if (action.action is ExamPartAction.Reading) state.copy(reading = reduceReadingExam(state.reading, action.action.action)) else state
                ExamModule.Writing -> if (action.action is ExamPartAction.Writing) state.copy(writing = reduceWritingExam(state.writing, action.action.action)) else state
                ExamModule.Grammar -> if (action.action is ExamPartAction.Grammar) state.copy(grammar = reduceGrammarExam(state.grammar, action.action.action)) else state
                null -> error("Cannot dispatch a completed sitting")
            }
        }
    }
}
private fun finishCurrent(state: ExamSessionState, now: Long): ExamSessionState {
    val module = state.config.modules[state.currentModuleIndex].module
    val listening = if (module == ExamModule.Listening) reduceListeningExam(state.listening, ListeningExamAction.Submit) else state.listening
    val reading = if (module == ExamModule.Reading) reduceReadingExam(state.reading, ReadingExamAction.Submit) else state.reading
    val grammar = if (module == ExamModule.Grammar) reduceGrammarExam(state.grammar, GrammarExamAction.Submit) else state.grammar
    val writing = if (module == ExamModule.Writing) reduceWritingExam(state.writing, WritingExamAction.Submit) else state.writing
    val result = when (module) {
        ExamModule.Listening -> requireNotNull(listening.result)
        ExamModule.Reading -> requireNotNull(reading.result)
        ExamModule.Writing -> requireNotNull(writing.result)
        ExamModule.Grammar -> requireNotNull(grammar.result).let { ExamPartScore(it.earnedQuarterPoints, it.possibleQuarterPoints, it.manualPendingQuarterPoints) }
    }
    val next = state.currentModuleIndex + 1
    return state.copy(listening = listening, reading = reading, grammar = grammar, writing = writing,
        frozenResults = state.frozenResults.toMap() + (module to result), currentModuleIndex = next,
        deadlineEpochMs = if (next < 4) deadline(now, state.config.modules[next].durationSeconds) else null,
        completed = next == 4)
}
private fun deadline(now: Long, durationSeconds: Int): Long {
    require(durationSeconds > 0)
    val millis = durationSeconds.toLong() * 1000L
    require(now <= Long.MAX_VALUE - millis) { "Exam deadline overflow" }
    return now + millis
}
