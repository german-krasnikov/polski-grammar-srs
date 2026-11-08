package polski.ui

/** Writing exams from generated biome rows — single source of truth. */
internal val examWritingData: Map<String, WritingExam> by lazy {
    mapOf("b1-2019" to mapB1Writing(), "b2-2019" to mapB2Writing(), "b1-2017" to mapB1y2017Writing())
}

private fun mapB1Writing(): WritingExam {
    return WritingExam(polski.exam.data.b1y2019.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    })
}

private fun mapB2Writing(): WritingExam {
    return WritingExam(polski.exam.data.b2y2019.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    })
}

private fun mapB1y2017Writing(): WritingExam = WritingExam(
    polski.exam.data.b1y2017.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    },
)
