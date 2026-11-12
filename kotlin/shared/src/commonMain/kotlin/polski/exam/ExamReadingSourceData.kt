package polski.exam

import polski.exam.*
import polski.exam.data.published.*
import polski.exam.reading.*

/** Reading models from one published DSL row catalog, available to every host. */
val officialReadingData: Map<String, ReadingExam> by lazy {
    val passages = READING_PASSAGE_ROW_ROWS.groupBy { it.examRef }
    val questions = READING_QUESTION_ROW_ROWS.groupBy { it.passageRef }
    val variants = READING_ANSWER_VARIANT_ROW_ROWS.groupBy { it.questionRef }
    val facts = READING_ACCEPTED_FACT_ROW_ROWS.groupBy { it.questionRef }
    val aliases = READING_ACCEPTED_FACT_ALIAS_ROW_ROWS.groupBy { it.factRef }
    EXAM_PUBLICATION_ROW_ROWS.associate { published ->
        val paperPassages = passages[published.examRef].orEmpty()
        published.routeId to ReadingExam(
            paperPassages.map { ReadingPassage(it.passageId, it.instruction, it.text) },
            paperPassages.flatMap { passage ->
                questions[passage.passageId].orEmpty().map { row ->
                    ReadingQuestion(
                        row.questionId, row.passageRef, QuestionType.fromTag(row.questionType.tag),
                        row.questionText, row.options?.split("|") ?: emptyList(), row.correctAnswer,
                        row.pointsQuarter ?: requireNotNull(row.points) { "Missing score for ${row.questionId}" } * 4,
                        row.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                        row.answerMode?.tag?.let(ExamAnswerMode::fromTag) ?: ExamAnswerMode.Single,
                        row.creditPerCorrectQuarter,
                        variants[row.questionId].orEmpty().map { ExamAnswerVariant(it.answer, it.earnedQuarter) },
                        row.requiredSelections,
                        facts[row.questionId].orEmpty().map { fact ->
                            ExamAcceptedFact(fact.factId, fact.displayText, fact.creditQuarter,
                                aliases[fact.factId].orEmpty().map { it.answerText })
                        },
                        row.setOverselectionMode?.tag?.let(ExamSetOverselectionMode::fromTag),
                    )
                }
            },
        )
    }
}
