package polski.exam.listening

import polski.exam.*

object ListeningExamScorer {
    fun grade(question: ListeningQuestion, answer: String): ExamAnswerEvaluation =
        if (question.answerMode == ExamAnswerMode.UnorderedSet)
            if (question.acceptedFacts.isEmpty() && question.requiredSelections == null) evaluateUnorderedSetAnswer(question.correctAnswer, question.options, answer, question.pointsQuarter, question.creditPerCorrectQuarter, question.gradingMode)
            else evaluateFactSetAnswer(sourceSetAnswerSpec(question.answerSlotCount, question.pointsQuarter, question.acceptedFacts, question.options, question.gradingMode, question.setOverselectionMode, question.correctAnswer, question.creditPerCorrectQuarter), answer)
        else evaluateExamAnswer(question.gradingMode, question.correctAnswer, question.scoredVariants, answer, question.pointsQuarter)

    fun scoreAttempt(exam: ListeningExam, answers: Map<String, String>): ExamPartScore {
        val questions = exam.tasks.flatMap { it.questions }
        val evaluations = questions.associate { it.id to grade(it, answers[it.id].orEmpty()) }
        return ExamPartScore(evaluations.values.sumOf { it.confirmedQuarter }, questions.sumOf { it.pointsQuarter }, evaluations.values.sumOf { it.pendingQuarter }, evaluations)
    }
}
