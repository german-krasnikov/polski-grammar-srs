package polski.ui

import polski.exam.writing.*

/** Writing exams from generated biome rows — single source of truth. */
internal val examWritingData: Map<String, WritingExam> by lazy {
    mapOf(
        "b1-2019" to mapB1Writing(), "b1-2017" to mapB1y2017Writing(),
        "b1-2020" to mapB1y2020Writing(), "b2-2017" to mapB2y2017Writing(),
        "b2-2019" to mapB2Writing(), "b2-2020" to mapB2y2020Writing(),
    )
}

private fun mapB1Writing(): WritingExam {
    return WritingExam(polski.exam.data.b1y2019.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    }, polski.exam.data.b1y2019.EXAM_MODULE_ROW_ROWS.first { it.module.tag == "writing" }.maximumQuarter)
}

private fun mapB2Writing(): WritingExam {
    return WritingExam(polski.exam.data.b2y2019.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    }, polski.exam.data.b2y2019.EXAM_MODULE_ROW_ROWS.first { it.module.tag == "writing" }.maximumQuarter)
}

private fun mapB1y2017Writing(): WritingExam = WritingExam(
    polski.exam.data.b1y2017.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    },
    polski.exam.data.b1y2017.EXAM_MODULE_ROW_ROWS.first { it.module.tag == "writing" }.maximumQuarter,
)

private fun mapB1y2020Writing(): WritingExam = WritingExam(
    polski.exam.data.b1y2020.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    },
    polski.exam.data.b1y2020.EXAM_MODULE_ROW_ROWS.first { it.module.tag == "writing" }.maximumQuarter,
)

private fun mapB2y2017Writing(): WritingExam = WritingExam(
    polski.exam.data.b2y2017.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    },
    polski.exam.data.b2y2017.EXAM_MODULE_ROW_ROWS.first { it.module.tag == "writing" }.maximumQuarter,
)

private fun mapB2y2020Writing(): WritingExam = WritingExam(
    polski.exam.data.b2y2020.WRITING_TASK_ROW_ROWS.map {
        WritingTask(it.taskId, it.instruction, it.promptText, it.wordLimit, it.setId, it.genre.tag, it.promptImageUrl, it.promptImageAlt)
    },
    polski.exam.data.b2y2020.EXAM_MODULE_ROW_ROWS.first { it.module.tag == "writing" }.maximumQuarter,
)
