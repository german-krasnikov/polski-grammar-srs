package polski.exam.reading

import polski.exam.*

data class ReadingPassage(val id: String, val instruction: String, val text: String)
data class ReadingQuestion(
    val id: String, val passageId: String, val type: QuestionType, val questionText: String,
    val options: List<String>, val correctAnswer: String, val pointsQuarter: Int = 4,
    val gradingMode: ExamGradingMode = ExamGradingMode.Exact,
    val answerMode: ExamAnswerMode = ExamAnswerMode.Single,
    val creditPerCorrectQuarter: Int? = null,
    val scoredVariants: List<ExamAnswerVariant> = emptyList(),
    val requiredSelections: Int? = null,
    val acceptedFacts: List<ExamAcceptedFact> = emptyList(),
    val setOverselectionMode: ExamSetOverselectionMode? = null,
) {
    init {
        require(pointsQuarter > 0)
        require(acceptedFacts.isEmpty() || requiredSelections != null && answerMode == ExamAnswerMode.UnorderedSet) { "Source facts require explicit set slots" }
        require(type !in setOf(QuestionType.MultipleChoice, QuestionType.MatchHeading, QuestionType.Matching, QuestionType.GapFillFragment) || options.isNotEmpty()) { "Missing options for $passageId/$id ($type)" }
        if (answerMode == ExamAnswerMode.UnorderedSet) {
            if (acceptedFacts.isEmpty() && requiredSelections == null) evaluateUnorderedSetAnswer(correctAnswer, options, "", pointsQuarter, creditPerCorrectQuarter, gradingMode)
            else evaluateFactSetAnswer(sourceSetAnswerSpec(answerSlotCount, pointsQuarter, acceptedFacts, options, gradingMode, setOverselectionMode, correctAnswer, creditPerCorrectQuarter), "")
        } else {
            evaluateExamAnswer(gradingMode, correctAnswer, scoredVariants, "", pointsQuarter)
        }
    }
    val answerSlotCount: Int get() = if (answerMode == ExamAnswerMode.UnorderedSet) requiredSelections ?: correctAnswer.split("|").size else 1
}
data class ReadingExam(val passages: List<ReadingPassage>, val questions: List<ReadingQuestion>) {
    init {
        require(passages.map { it.id }.distinct().size == passages.size)
        require(questions.map { it.id }.distinct().size == questions.size)
        require(questions.all { q -> passages.any { it.id == q.passageId } })
    }
}
