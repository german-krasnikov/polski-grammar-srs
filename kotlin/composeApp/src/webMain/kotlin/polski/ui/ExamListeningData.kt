package polski.ui

import polski.exam.QuestionType
import polski.exam.ExamAnswerMode
import polski.exam.ExamGradingMode
import polski.exam.ExamAnswerVariant

/** Listening exams from generated biome rows — single source of truth. */
internal val examListeningData: Map<String, ListeningExam> by lazy {
    mapOf(
        "b1-2019" to mapB1Listening(), "b1-2017" to mapB1y2017Listening(),
        "b1-2020" to mapB1y2020Listening(), "b2-2017" to mapB2y2017Listening(),
        "b2-2019" to mapB2Listening(), "b2-2020" to mapB2y2020Listening(),
    )
}

private fun mapB1Listening(): ListeningExam {
    val tasks = polski.exam.data.b1y2019.LISTENING_TASK_ROW_ROWS
    val qs = polski.exam.data.b1y2019.LISTENING_QUESTION_ROW_ROWS
    return ListeningExam(
        tasks.map { t ->
            ListeningTask(t.taskId, t.instruction,
                qs.filter { it.taskRef == t.taskId }.map { q ->
                    ListeningQ(q.questionId, QuestionType.fromTag(q.questionType.tag), q.questionText, q.options?.split("|") ?: emptyList(), q.correctAnswer, q.pointsQuarter ?: requireNotNull(q.points) { "Missing score for ${q.questionId}" } * 4,
                        q.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                        q.answerMode?.tag?.let(ExamAnswerMode::fromTag) ?: ExamAnswerMode.Single,
                        q.creditPerCorrectQuarter)
                }, t.segmentStartMs.toLong(), t.segmentEndMs.toLong(), t.promptImageUrl, t.promptImageAlt)
        }, tasks.firstOrNull()?.audioUrl ?: "")
}

private fun mapB2Listening(): ListeningExam {
    val tasks = polski.exam.data.b2y2019.LISTENING_TASK_ROW_ROWS
    val qs = polski.exam.data.b2y2019.LISTENING_QUESTION_ROW_ROWS
    val variants = polski.exam.data.b2y2019.LISTENING_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.questionRef }, valueTransform = { ExamAnswerVariant(it.answer, it.earnedQuarter) },
    )
    return ListeningExam(
        tasks.map { t ->
            ListeningTask(t.taskId, t.instruction,
                qs.filter { it.taskRef == t.taskId }.map { q ->
                    ListeningQ(q.questionId, QuestionType.fromTag(q.questionType.tag), q.questionText, q.options?.split("|") ?: emptyList(), q.correctAnswer, q.pointsQuarter ?: requireNotNull(q.points) { "Missing score for ${q.questionId}" } * 4,
                        q.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                        q.answerMode?.tag?.let(ExamAnswerMode::fromTag) ?: ExamAnswerMode.Single,
                        q.creditPerCorrectQuarter, variants[q.questionId].orEmpty())
                }, t.segmentStartMs.toLong(), t.segmentEndMs.toLong(), t.promptImageUrl, t.promptImageAlt)
        }, tasks.firstOrNull()?.audioUrl ?: "")
}

private fun mapB1y2020Listening(): ListeningExam {
    val tasks = polski.exam.data.b1y2020.LISTENING_TASK_ROW_ROWS
    val questions = polski.exam.data.b1y2020.LISTENING_QUESTION_ROW_ROWS
    return ListeningExam(tasks.map { task ->
        ListeningTask(task.taskId, task.instruction,
            questions.filter { it.taskRef == task.taskId }.map { q ->
                ListeningQ(q.questionId, QuestionType.fromTag(q.questionType.tag), q.questionText,
                    q.options?.split("|") ?: emptyList(), q.correctAnswer,
                    q.pointsQuarter ?: requireNotNull(q.points) { "Missing score for ${q.questionId}" } * 4,
                    q.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                    q.answerMode?.tag?.let(ExamAnswerMode::fromTag) ?: ExamAnswerMode.Single,
                    q.creditPerCorrectQuarter)
            }, task.segmentStartMs.toLong(), task.segmentEndMs.toLong(), task.promptImageUrl, task.promptImageAlt)
    }, tasks.firstOrNull()?.audioUrl ?: "")
}

private fun mapB2y2017Listening(): ListeningExam {
    val tasks = polski.exam.data.b2y2017.LISTENING_TASK_ROW_ROWS
    val questions = polski.exam.data.b2y2017.LISTENING_QUESTION_ROW_ROWS
    val variants = polski.exam.data.b2y2017.LISTENING_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.questionRef }, valueTransform = { ExamAnswerVariant(it.answer, it.earnedQuarter) },
    )
    return ListeningExam(tasks.map { task ->
        ListeningTask(task.taskId, task.instruction,
            questions.filter { it.taskRef == task.taskId }.map { q ->
                ListeningQ(q.questionId, QuestionType.fromTag(q.questionType.tag), q.questionText,
                    q.options?.split("|") ?: emptyList(), q.correctAnswer,
                    q.pointsQuarter ?: requireNotNull(q.points) { "Missing score for ${q.questionId}" } * 4,
                    q.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                    q.answerMode?.tag?.let(ExamAnswerMode::fromTag) ?: ExamAnswerMode.Single,
                    q.creditPerCorrectQuarter, variants[q.questionId].orEmpty())
            }, task.segmentStartMs.toLong(), task.segmentEndMs.toLong(), task.promptImageUrl, task.promptImageAlt)
    }, tasks.firstOrNull()?.audioUrl ?: "")
}

private fun mapB2y2020Listening(): ListeningExam {
    val tasks = polski.exam.data.b2y2020.LISTENING_TASK_ROW_ROWS
    val questions = polski.exam.data.b2y2020.LISTENING_QUESTION_ROW_ROWS
    val variants = polski.exam.data.b2y2020.LISTENING_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.questionRef }, valueTransform = { ExamAnswerVariant(it.answer, it.earnedQuarter) },
    )
    return ListeningExam(tasks.map { task ->
        ListeningTask(task.taskId, task.instruction,
            questions.filter { it.taskRef == task.taskId }.map { q ->
                ListeningQ(q.questionId, QuestionType.fromTag(q.questionType.tag), q.questionText,
                    q.options?.split("|") ?: emptyList(), q.correctAnswer,
                    q.pointsQuarter ?: requireNotNull(q.points) { "Missing score for ${q.questionId}" } * 4,
                    q.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                    q.answerMode?.tag?.let(ExamAnswerMode::fromTag) ?: ExamAnswerMode.Single,
                    q.creditPerCorrectQuarter, variants[q.questionId].orEmpty())
            }, task.segmentStartMs.toLong(), task.segmentEndMs.toLong(), task.promptImageUrl, task.promptImageAlt)
    }, tasks.firstOrNull()?.audioUrl ?: "")
}

private fun mapB1y2017Listening(): ListeningExam {
    val tasks = polski.exam.data.b1y2017.LISTENING_TASK_ROW_ROWS
    val questions = polski.exam.data.b1y2017.LISTENING_QUESTION_ROW_ROWS
    return ListeningExam(
        tasks.map { task ->
            ListeningTask(
                task.taskId,
                task.instruction,
                questions.filter { it.taskRef == task.taskId }.map { question ->
                    ListeningQ(
                        question.questionId,
                        QuestionType.fromTag(question.questionType.tag),
                        question.questionText,
                        question.options?.split("|") ?: emptyList(),
                        question.correctAnswer,
                        question.pointsQuarter ?: requireNotNull(question.points) { "Missing score for ${question.questionId}" } * 4,
                        question.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                        question.answerMode?.tag?.let(ExamAnswerMode::fromTag) ?: ExamAnswerMode.Single,
                        question.creditPerCorrectQuarter,
                    )
                },
                task.segmentStartMs.toLong(),
                task.segmentEndMs.toLong(),
                task.promptImageUrl,
                task.promptImageAlt,
            )
        },
        tasks.firstOrNull()?.audioUrl ?: "",
    )
}
