package polski.exam

import polski.exam.*
import polski.exam.data.published.*
import polski.exam.listening.*

/** Listening models from one published DSL row catalog, available to every host. */
val officialListeningData: Map<String, ListeningExam> by lazy {
    val tasks = LISTENING_TASK_ROW_ROWS.groupBy { it.examRef }
    val questions = LISTENING_QUESTION_ROW_ROWS.groupBy { it.taskRef }
    val variants = LISTENING_ANSWER_VARIANT_ROW_ROWS.groupBy { it.questionRef }
    val facts = LISTENING_ACCEPTED_FACT_ROW_ROWS.groupBy { it.questionRef }
    val aliases = LISTENING_ACCEPTED_FACT_ALIAS_ROW_ROWS.groupBy { it.factRef }
    EXAM_PUBLICATION_ROW_ROWS.associate { published ->
        val paperTasks = tasks[published.examRef].orEmpty()
        published.routeId to ListeningExam(
            paperTasks.map { task ->
                ListeningTask(
                    task.taskId, task.instruction,
                    questions[task.taskId].orEmpty().map { row ->
                        ListeningQuestion(
                            row.questionId, QuestionType.fromTag(row.questionType.tag), row.questionText,
                            row.options?.split("|") ?: emptyList(), row.correctAnswer,
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
                    },
                    task.segmentStartMs.toLong(), task.segmentEndMs.toLong(),
                    task.promptImageUrl, task.promptImageAlt,
                )
            },
            paperTasks.firstOrNull()?.audioUrl ?: "",
        )
    }
}
