package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.data.published.EXAM_MODULE_ROW_ROWS
import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.mergeGrammarTests
import polski.exam.listening.ListeningExamScorer
import polski.exam.listening.ListeningQuestion
import polski.exam.reading.ReadingExamScorer
import polski.exam.reading.ReadingQuestion

/** Source-key closure against the independently audited authored module covers. */
class ExamNewPaperCanonicalClosureTest {
    @Test fun b2February2022() = verify("b2-2022-02")
    @Test fun b2February2023() = verify("b2-2023-02")
    @Test fun b2June2023() = verify("b2-2023-06")
    @Test fun b2February2024() = verify("b2-2024-02")
    @Test fun b2June2024() = verify("b2-2024-06")
    @Test fun c1March2022() = verify("c1-2022-03")
    @Test fun c1November2022() = verify("c1-2022-11")
    @Test fun c1April2023() = verify("c1-2023-04")
    @Test fun c1November2023() = verify("c1-2023-11")
    @Test fun c1April2024() = verify("c1-2024-04")

    private fun verify(route: String) {
        val exam = requireNotNull(ExamCatalog.find(route))
        val covers = EXAM_MODULE_ROW_ROWS.filter { it.examRef == exam.sourceExamId }
            .associate { it.module.tag to it.maximumQuarter }
        assertEquals(setOf("listening", "reading", "grammar", "writing"), covers.keys, route)

        val listening = officialListeningData.getValue(route)
        val listeningQuestions = listening.tasks.flatMap { it.questions }
        val listeningAnswers = listeningQuestions.associate { it.id to canonical(it) }
        val listeningResult = ListeningExamScorer.scoreAttempt(listening, listeningAnswers)
        val listeningMisses = listeningQuestions.filter { question ->
            val grade = listeningResult.evaluations.getValue(question.id)
            grade.confirmedQuarter != question.pointsQuarter || grade.pendingQuarter != 0
        }.map { it.id }
        assertEquals(covers.getValue("listening"), listeningResult.possibleQuarter, "$route listening denominator")
        assertEquals(covers.getValue("listening"), listeningResult.confirmedQuarter,
            "$route listening canonical misses: $listeningMisses")
        assertEquals(0, listeningResult.manualPendingQuarter, "$route listening canonical pending: $listeningMisses")

        val reading = officialReadingData.getValue(route)
        val readingAnswers = reading.questions.associate { it.id to canonical(it) }
        val readingResult = ReadingExamScorer.scoreAttempt(reading, readingAnswers)
        val readingMisses = reading.questions.filter { question ->
            val grade = readingResult.evaluations.getValue(question.id)
            grade.confirmedQuarter != question.pointsQuarter || grade.pendingQuarter != 0
        }.map { it.id }
        assertEquals(covers.getValue("reading"), readingResult.possibleQuarter, "$route reading denominator")
        assertEquals(covers.getValue("reading"), readingResult.confirmedQuarter,
            "$route reading canonical misses: $readingMisses")
        assertEquals(0, readingResult.manualPendingQuarter, "$route reading canonical pending: $readingMisses")

        val grammar = mergeGrammarTests(route, officialGrammarData.getValue(route))
        val grammarAnswers = grammar.items.associate { it.itemId to it.correctAnswer }.toMutableMap()
        grammar.answerPairs.distinctBy { it.firstItemId to it.secondItemId }.forEach { pair ->
            grammarAnswers[pair.firstItemId] = pair.firstAnswer
            grammarAnswers[pair.secondItemId] = pair.secondAnswer
        }
        val grammarResult = GrammarExamScorer.scoreAttempt(grammar, grammarAnswers)
        val grammarMisses = grammar.items.filter { item ->
            val grade = GrammarExamScorer.gradeInAttempt(grammar, item, grammarAnswers)
            grade.confirmedQuarter != item.maximumQuarter || grade.pendingQuarter != 0
        }.map { it.itemId }
        assertEquals(covers.getValue("grammar"), grammarResult.possibleQuarterPoints, "$route grammar denominator")
        assertEquals(covers.getValue("grammar"), grammarResult.earnedQuarterPoints,
            "$route grammar canonical misses: $grammarMisses")
        assertEquals(0, grammarResult.manualPendingQuarterPoints, "$route grammar canonical pending: $grammarMisses")
    }
    private fun canonical(question: ListeningQuestion): String = canonicalSet(
        question.answerMode, question.requiredSelections, question.acceptedFacts, question.correctAnswer,
    )

    private fun canonical(question: ReadingQuestion): String = canonicalSet(
        question.answerMode, question.requiredSelections, question.acceptedFacts, question.correctAnswer,
    )

    private fun canonicalSet(
        mode: ExamAnswerMode, required: Int?, facts: List<ExamAcceptedFact>, correctAnswer: String,
    ): String {
        if (mode != ExamAnswerMode.UnorderedSet) return correctAnswer
        val count = required ?: return correctAnswer
        val candidates = if (facts.isNotEmpty()) facts.map { it.displayText } else correctAnswer.split("|")
        require(candidates.size >= count) { "Insufficient source canonical facts for $correctAnswer" }
        return candidates.take(count).joinToString("|")
    }
}
