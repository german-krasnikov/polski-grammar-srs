package polski.exam.listening

import polski.exam.*

data class ListeningTask(
    val id: String, val instruction: String, val questions: List<ListeningQuestion>,
    val startMs: Long = 0, val endMs: Long = 0, val promptImageUrl: String? = null, val promptImageAlt: String? = null,
)
data class ListeningQuestion(
    val id: String, val type: QuestionType, val questionText: String, val options: List<String>,
    val correctAnswer: String, val pointsQuarter: Int = 4,
    val gradingMode: ExamGradingMode = ExamGradingMode.Exact,
    val answerMode: ExamAnswerMode = ExamAnswerMode.Single,
    val creditPerCorrectQuarter: Int? = null, val scoredVariants: List<ExamAnswerVariant> = emptyList(),
) {
    val answerSlotCount: Int get() = if (answerMode == ExamAnswerMode.UnorderedSet) correctAnswer.split("|").size else 1
    init {
        require(pointsQuarter > 0)
        require(type !in setOf(QuestionType.MultipleChoice, QuestionType.Match, QuestionType.Matching) || options.isNotEmpty()) { "Missing options for listening question $id ($type)" }
        if (answerMode == ExamAnswerMode.UnorderedSet) {
            evaluateUnorderedSetAnswer(correctAnswer, options, "", pointsQuarter, creditPerCorrectQuarter, gradingMode)
        } else {
            evaluateExamAnswer(gradingMode, correctAnswer, scoredVariants, "", pointsQuarter)
        }
    }
}
typealias ListeningQ = ListeningQuestion

data class ListeningExam(val tasks: List<ListeningTask>, val audioUrl: String) {
    init {
        require(tasks.map { it.id }.distinct().size == tasks.size)
        require(tasks.flatMap { it.questions }.map { it.id }.distinct().size == tasks.sumOf { it.questions.size })
    }
}
