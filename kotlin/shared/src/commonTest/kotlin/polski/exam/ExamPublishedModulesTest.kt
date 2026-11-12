package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import polski.exam.data.published.EXAM_PUBLICATION_ROW_ROWS
import polski.exam.data.published.LISTENING_QUESTION_ROW_ROWS
import polski.exam.data.published.LISTENING_TASK_ROW_ROWS
import polski.exam.data.published.READING_PASSAGE_ROW_ROWS
import polski.exam.data.published.READING_QUESTION_ROW_ROWS
import polski.exam.data.published.WRITING_TASK_ROW_ROWS

class ExamPublishedModulesTest {
    @Test
    fun everyHostReceivesTheSixPublishedPapersAndNoSample() {
        val routes = EXAM_PUBLICATION_ROW_ROWS.map { it.routeId }
        assertEquals(listOf("b1-2017", "b1-2019", "b1-2020", "b2-2017", "b2-2019", "b2-2020"), routes)
        assertEquals(routes.toSet(), officialListeningData.keys)
        assertEquals(routes.toSet(), officialReadingData.keys)
        assertEquals(routes.toSet(), officialWritingData.keys)
        assertNull(officialListeningData["b1-sample"])
        assertNull(officialReadingData["b1-sample"])
        assertNull(officialWritingData["b1-sample"])
    }

    @Test
    fun listeningAndReadingPreserveSourceOrderIdentityCreditAndMedia() {
        for (published in EXAM_PUBLICATION_ROW_ROWS) {
            val listeningRows = LISTENING_TASK_ROW_ROWS.filter { it.examRef == published.examRef }
            val listening = officialListeningData.getValue(published.routeId)
            assertEquals(listeningRows.map { it.taskId }, listening.tasks.map { it.id })
            assertEquals(listeningRows.first().audioUrl, listening.audioUrl)
            for ((source, task) in listeningRows.zip(listening.tasks)) {
                assertEquals(source.segmentStartMs.toLong() to source.segmentEndMs.toLong(), task.startMs to task.endMs)
                assertEquals(source.promptImageUrl to source.promptImageAlt, task.promptImageUrl to task.promptImageAlt)
                val questions = LISTENING_QUESTION_ROW_ROWS.filter { it.taskRef == source.taskId }
                assertEquals(questions.map { it.questionId }, task.questions.map { it.id })
                assertEquals(questions.map { it.pointsQuarter ?: it.points!! * 4 }, task.questions.map { it.pointsQuarter })
                assertEquals(questions.map { it.requiredSelections }, task.questions.map { it.requiredSelections })
                assertEquals(questions.map { it.setOverselectionMode?.tag }, task.questions.map { it.setOverselectionMode?.tag })
            }

            val passageRows = READING_PASSAGE_ROW_ROWS.filter { it.examRef == published.examRef }
            val reading = officialReadingData.getValue(published.routeId)
            assertEquals(passageRows.map { it.passageId to it.text }, reading.passages.map { it.id to it.text })
            val questions = passageRows.flatMap { passage -> READING_QUESTION_ROW_ROWS.filter { it.passageRef == passage.passageId } }
            assertEquals(questions.map { it.questionId to it.correctAnswer }, reading.questions.map { it.id to it.correctAnswer })
            assertEquals(questions.map { it.pointsQuarter ?: it.points!! * 4 }, reading.questions.map { it.pointsQuarter })
            assertEquals(questions.map { it.requiredSelections }, reading.questions.map { it.requiredSelections })
            assertEquals(questions.map { it.setOverselectionMode?.tag }, reading.questions.map { it.setOverselectionMode?.tag })
        }
    }

    @Test
    fun writingPreservesSourceSetsPromptsAndCertifiedMaximum() {
        for (published in EXAM_PUBLICATION_ROW_ROWS) {
            val source = WRITING_TASK_ROW_ROWS.filter { it.examRef == published.examRef }
            val writing = officialWritingData.getValue(published.routeId)
            assertEquals(source.map { it.taskId to it.promptText }, writing.tasks.map { it.id to it.promptText })
            assertEquals(source.map { it.setId to it.wordLimit }, writing.tasks.map { it.setId to it.wordLimit })
            assertEquals(source.map { it.promptImageUrl to it.promptImageAlt }, writing.tasks.map { it.promptImageUrl to it.promptImageAlt })
            assertEquals(source.map { it.genre.tag }, writing.tasks.map { it.genre })
            assertEquals(ExamCatalog.find(published.routeId)!!.modules.single { it.module == ExamModule.Writing }.maximumQuarter,
                writing.maximumQuarter)
        }
    }
}
