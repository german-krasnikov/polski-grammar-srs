package polski.ui

import polski.exam.QuestionType

/** Listening exams from generated biome rows — single source of truth. */
internal val examListeningData: Map<String, ListeningExam> by lazy {
    mapOf("b1-2019" to mapB1Listening(), "b2-2019" to mapB2Listening())
}

private fun mapB1Listening(): ListeningExam {
    val tasks = polski.exam.data.b1y2019.LISTENING_TASK_ROW_ROWS
    val qs = polski.exam.data.b1y2019.LISTENING_QUESTION_ROW_ROWS
    return ListeningExam(
        tasks.map { t ->
            ListeningTask(t.taskId, t.instruction,
                qs.filter { it.taskRef == t.taskId }.map { q ->
                    ListeningQ(q.questionId, QuestionType.fromTag(q.questionType.tag), q.questionText, q.options?.split("|") ?: emptyList(), q.correctAnswer)
                }, t.segmentStartMs.toLong(), t.segmentEndMs.toLong())
        }, tasks.firstOrNull()?.audioUrl ?: "")
}

private fun mapB2Listening(): ListeningExam {
    val tasks = polski.exam.data.b2y2019.LISTENING_TASK_ROW_ROWS
    val qs = polski.exam.data.b2y2019.LISTENING_QUESTION_ROW_ROWS
    return ListeningExam(
        tasks.map { t ->
            ListeningTask(t.taskId, t.instruction,
                qs.filter { it.taskRef == t.taskId }.map { q ->
                    ListeningQ(q.questionId, QuestionType.fromTag(q.questionType.tag), q.questionText, q.options?.split("|") ?: emptyList(), q.correctAnswer)
                }, t.segmentStartMs.toLong(), t.segmentEndMs.toLong())
        }, tasks.firstOrNull()?.audioUrl ?: "")
}
