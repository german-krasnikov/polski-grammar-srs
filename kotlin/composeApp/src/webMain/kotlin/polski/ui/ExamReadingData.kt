package polski.ui

import polski.exam.QuestionType

/** Reading exams from generated biome rows — single source of truth. */
internal val examReadingData: Map<String, ReadingExam> by lazy {
    mapOf(
        "b1-2019" to mapB1Reading(), "b1-2017" to mapB1y2017Reading(),
        "b1-2020" to mapB1y2020Reading(), "b2-2017" to mapB2y2017Reading(),
        "b2-2019" to mapB2y2019Reading(), "b2-2020" to mapB2y2020Reading(),
    )
}

private fun mapB1Reading(): ReadingExam {
    val pRows = polski.exam.data.b1y2019.READING_PASSAGE_ROW_ROWS
    val qRows = polski.exam.data.b1y2019.READING_QUESTION_ROW_ROWS
    return ReadingExam(
        passages = pRows.map { ReadingPassage(it.passageId, it.instruction, it.text) },
        questions = qRows.map { ReadingQuestion(it.questionId, it.passageRef, QuestionType.fromTag(it.questionType.tag), it.questionText, it.options?.split("|") ?: emptyList(), it.correctAnswer, it.pointsQuarter ?: requireNotNull(it.points) { "Missing score for ${it.questionId}" } * 4) },
    )
}

private fun mapB1y2017Reading(): ReadingExam {
    val pRows = polski.exam.data.b1y2017.READING_PASSAGE_ROW_ROWS
    val qRows = polski.exam.data.b1y2017.READING_QUESTION_ROW_ROWS
    return ReadingExam(
        passages = pRows.map { ReadingPassage(it.passageId, it.instruction, it.text) },
        questions = qRows.map { ReadingQuestion(it.questionId, it.passageRef, QuestionType.fromTag(it.questionType.tag), it.questionText, it.options?.split("|") ?: emptyList(), it.correctAnswer, it.pointsQuarter ?: requireNotNull(it.points) { "Missing score for ${it.questionId}" } * 4) },
    )
}

private fun mapB1y2020Reading(): ReadingExam {
    val passages = polski.exam.data.b1y2020.READING_PASSAGE_ROW_ROWS
    val questions = polski.exam.data.b1y2020.READING_QUESTION_ROW_ROWS
    return ReadingExam(
        passages.map { ReadingPassage(it.passageId, it.instruction, it.text) },
        questions.map { ReadingQuestion(it.questionId, it.passageRef, QuestionType.fromTag(it.questionType.tag), it.questionText, it.options?.split("|") ?: emptyList(), it.correctAnswer, it.pointsQuarter ?: requireNotNull(it.points) { "Missing score for ${it.questionId}" } * 4) },
    )
}

private fun mapB2y2017Reading(): ReadingExam {
    val passages = polski.exam.data.b2y2017.READING_PASSAGE_ROW_ROWS
    val questions = polski.exam.data.b2y2017.READING_QUESTION_ROW_ROWS
    return ReadingExam(
        passages.map { ReadingPassage(it.passageId, it.instruction, it.text) },
        questions.map { ReadingQuestion(it.questionId, it.passageRef, QuestionType.fromTag(it.questionType.tag), it.questionText, it.options?.split("|") ?: emptyList(), it.correctAnswer, it.pointsQuarter ?: requireNotNull(it.points) { "Missing score for ${it.questionId}" } * 4) },
    )
}

private fun mapB2y2019Reading(): ReadingExam {
    val passages = polski.exam.data.b2y2019.READING_PASSAGE_ROW_ROWS
    val questions = polski.exam.data.b2y2019.READING_QUESTION_ROW_ROWS
    return ReadingExam(
        passages.map { ReadingPassage(it.passageId, it.instruction, it.text) },
        questions.map { ReadingQuestion(it.questionId, it.passageRef, QuestionType.fromTag(it.questionType.tag), it.questionText, it.options?.split("|") ?: emptyList(), it.correctAnswer, it.pointsQuarter ?: requireNotNull(it.points) { "Missing score for ${it.questionId}" } * 4) },
    )
}

private fun mapB2y2020Reading(): ReadingExam {
    val passages = polski.exam.data.b2y2020.READING_PASSAGE_ROW_ROWS
    val questions = polski.exam.data.b2y2020.READING_QUESTION_ROW_ROWS
    return ReadingExam(
        passages.map { ReadingPassage(it.passageId, it.instruction, it.text) },
        questions.map { ReadingQuestion(it.questionId, it.passageRef, QuestionType.fromTag(it.questionType.tag), it.questionText, it.options?.split("|") ?: emptyList(), it.correctAnswer, it.pointsQuarter ?: requireNotNull(it.points) { "Missing score for ${it.questionId}" } * 4) },
    )
}
