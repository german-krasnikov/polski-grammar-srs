package polski.exam.session

import polski.exam.ExamModule
import polski.exam.grammar.GrammarExamCard
import polski.exam.grammar.grammarExamCardModel
import polski.exam.listening.ListeningExamCardModel
import polski.exam.listening.listeningExamCardModel
import polski.exam.reading.ReadingExamCardModel
import polski.exam.reading.readingExamCardModel
import polski.exam.writing.WritingExamCardModel
import polski.exam.writing.writingExamCardModel

sealed interface ExamSessionPartCard {
    data class Listening(val card: ListeningExamCardModel) : ExamSessionPartCard
    data class Reading(val card: ReadingExamCardModel) : ExamSessionPartCard
    data class Grammar(val card: GrammarExamCard) : ExamSessionPartCard
    data class Writing(val card: WritingExamCardModel) : ExamSessionPartCard
}

data class ExamSessionCardModel(
    val currentModule: ExamModule?, val remainingMs: Long, val position: Int,
    val confirmedQuarter: Int, val manualPendingQuarter: Int, val possibleQuarter: Int,
    val isFinal: Boolean, val finalPercent: Int?, val currentPartCard: ExamSessionPartCard?,
) {
    val upperBoundQuarter: Int get() = confirmedQuarter + manualPendingQuarter
}
fun examSessionCardModel(state: ExamSessionState, nowEpochMs: Long): ExamSessionCardModel {
    val confirmed = state.frozenResults.values.sumOf { it.confirmedQuarter }
    val pending = state.frozenResults.values.sumOf { it.manualPendingQuarter }
    val possible = state.config.modules.sumOf { it.maximumQuarter }
    val final = state.completed && pending == 0
    return ExamSessionCardModel(
        state.config.modules.getOrNull(state.currentModuleIndex)?.module,
        state.deadlineEpochMs?.let { (it - nowEpochMs).coerceAtLeast(0) } ?: 0,
        state.currentModuleIndex + 1, confirmed, pending, possible, final,
        if (final) (confirmed.toDouble() / possible * 100).toInt() else null,
        when (state.config.modules.getOrNull(state.currentModuleIndex)?.module) {
            ExamModule.Listening -> ExamSessionPartCard.Listening(listeningExamCardModel(state.listening))
            ExamModule.Reading -> ExamSessionPartCard.Reading(readingExamCardModel(state.reading))
            ExamModule.Grammar -> ExamSessionPartCard.Grammar(grammarExamCardModel(state.grammar))
            ExamModule.Writing -> ExamSessionPartCard.Writing(writingExamCardModel(state.writing))
            null -> null
        },
    )
}
