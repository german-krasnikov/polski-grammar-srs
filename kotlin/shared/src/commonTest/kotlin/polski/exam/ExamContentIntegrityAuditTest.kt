package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.exam.data.published.EXAM_PUBLICATION_ROW_ROWS
import polski.exam.data.published.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.data.published.LISTENING_QUESTION_ROW_ROWS
import polski.exam.data.published.READING_QUESTION_ROW_ROWS

/** Guards the authored-row to shared-model boundary for every published sitting. */
class ExamContentIntegrityAuditTest {
    @Test
    fun everyPublishedQuestionHasAVisiblePromptControlAndKey() {
        val routes = EXAM_PUBLICATION_ROW_ROWS.map { it.routeId }
        assertEquals(16, routes.size)
        assertEquals(routes.toSet(), officialGrammarData.keys)
        assertEquals(routes.toSet(), officialListeningData.keys)
        assertEquals(routes.toSet(), officialReadingData.keys)
        assertEquals(routes.toSet(), officialWritingData.keys)

        var grammarCount = 0
        var listeningCount = 0
        var readingCount = 0
        var writingCount = 0
        for (route in routes) {
            val grammar = officialGrammarData.getValue(route)
            assertTrue(grammar.isNotEmpty(), "$route grammar tasks")
            grammar.forEach { test ->
                assertTrue(test.instruction.isNotBlank(), "$route/${test.testId} instruction")
                assertTrue(test.items.isNotEmpty(), "$route/${test.testId} items")
                test.items.forEach { item ->
                    val location = "$route/${test.testId}/${item.itemId}"
                    assertTrue(item.context.isNotBlank(), "$location context")
                    assertTrue(item.correctAnswer.isNotBlank(), "$location key")
                    assertTrue(item.options.all { it.isNotBlank() }, "$location options")
                    grammarCount++
                }
            }

            val listening = officialListeningData.getValue(route)
            assertTrue(listening.audioUrl.isNotBlank(), "$route audio")
            assertTrue(listening.tasks.isNotEmpty(), "$route listening tasks")
            listening.tasks.forEach { task ->
                assertTrue(task.instruction.isNotBlank(), "$route/${task.id} instruction")
                assertTrue(task.questions.isNotEmpty(), "$route/${task.id} questions")
                task.questions.forEach { question ->
                    val location = "$route/${task.id}/${question.id}"
                    assertTrue(question.questionText.isNotBlank(), "$location prompt")
                    assertTrue(question.correctAnswer.isNotBlank(), "$location key")
                    assertTrue(question.options.all { it.isNotBlank() }, "$location options")
                    listeningCount++
                }
            }

            val reading = officialReadingData.getValue(route)
            assertTrue(reading.passages.isNotEmpty(), "$route reading passages")
            reading.passages.forEach { passage ->
                assertTrue(passage.instruction.isNotBlank(), "$route/${passage.id} instruction")
                assertTrue(passage.text.isNotBlank(), "$route/${passage.id} passage")
            }
            reading.questions.forEach { question ->
                val location = "$route/${question.passageId}/${question.id}"
                assertTrue(question.questionText.isNotBlank(), "$location prompt")
                assertTrue(question.correctAnswer.isNotBlank(), "$location key")
                assertTrue(question.options.all { it.isNotBlank() }, "$location options")
                readingCount++
            }

            val writing = officialWritingData.getValue(route)
            assertTrue(writing.tasks.isNotEmpty(), "$route writing tasks")
            writing.tasks.forEach { task ->
                assertTrue(task.instruction.isNotBlank(), "$route/${task.id} instruction")
                assertTrue(task.promptText.isNotBlank(), "$route/${task.id} prompt")
                assertTrue(task.promptImageUrl == null || !task.promptImageAlt.isNullOrBlank(), "$route/${task.id} image alt")
                writingCount++
            }
        }
        assertEquals(GRAMMAR_ITEM_ROW_ROWS.size, grammarCount)
        assertEquals(LISTENING_QUESTION_ROW_ROWS.size, listeningCount)
        assertEquals(READING_QUESTION_ROW_ROWS.size, readingCount)
        assertEquals(1125, grammarCount, "published grammar items")
        assertEquals(604, listeningCount, "published listening questions")
        assertEquals(691, readingCount, "published reading questions")
        assertEquals(91, writingCount, "published writing prompts")
    }
}
