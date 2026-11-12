package polski.exam

import polski.exam.data.published.EXAM_MODULE_ROW_ROWS
import polski.exam.data.published.EXAM_PUBLICATION_ROW_ROWS
import polski.exam.data.published.WRITING_TASK_ROW_ROWS
import polski.exam.writing.WritingExam
import polski.exam.writing.WritingTask

/** Writing models from one published DSL row catalog, available to every host. */
val officialWritingData: Map<String, WritingExam> by lazy {
    val tasks = WRITING_TASK_ROW_ROWS.groupBy { it.examRef }
    val covers = EXAM_MODULE_ROW_ROWS.filter { it.module.tag == "writing" }.associateBy { it.examRef }
    EXAM_PUBLICATION_ROW_ROWS.associate { published ->
        published.routeId to WritingExam(
            tasks[published.examRef].orEmpty().map { task ->
                WritingTask(task.taskId, task.instruction, task.promptText, task.wordLimit,
                    task.setId, task.genre.tag, task.promptImageUrl, task.promptImageAlt)
            },
            requireNotNull(covers[published.examRef]) { "Missing writing cover for ${published.routeId}" }.maximumQuarter,
        )
    }
}
