package polski.exam.reading

import polski.exam.*

object ReadingExamScorer {
    fun grade(question: ReadingQuestion, answer: String): ExamAnswerEvaluation =
        if (question.answerMode == ExamAnswerMode.UnorderedSet)
            evaluateUnorderedSetAnswer(question.correctAnswer, question.options, answer, question.pointsQuarter, question.creditPerCorrectQuarter, question.gradingMode)
        else evaluateExamAnswer(question.gradingMode, question.correctAnswer, question.scoredVariants, answer, question.pointsQuarter)

    fun scoreAttempt(exam: ReadingExam, answers: Map<String, String>): ExamPartScore {
        val evaluations = exam.questions.associate { it.id to grade(it, answers[it.id].orEmpty()) }
        return ExamPartScore(evaluations.values.sumOf { it.confirmedQuarter }, exam.questions.sumOf { it.pointsQuarter }, evaluations.values.sumOf { it.pendingQuarter }, evaluations)
    }
}
