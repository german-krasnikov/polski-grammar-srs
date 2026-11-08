package polski.ui

import polski.exam.QuestionType

/** Reading exams from generated biome rows — single source of truth. */
internal val examReadingData: Map<String, ReadingExam> by lazy {
    mapOf("b1-2019" to mapB1Reading())
}

private fun mapB1Reading(): ReadingExam {
    val pRows = polski.exam.data.b1y2019.READING_PASSAGE_ROW_ROWS
    val qRows = polski.exam.data.b1y2019.READING_QUESTION_ROW_ROWS
    return ReadingExam(
        passages = pRows.map { ReadingPassage(it.passageId, it.instruction, it.text) },
        questions = qRows.map { ReadingQuestion(it.questionId, it.passageRef, QuestionType.fromTag(it.questionType.tag), it.questionText, it.options?.split("|") ?: emptyList(), it.correctAnswer) },
    )
}
