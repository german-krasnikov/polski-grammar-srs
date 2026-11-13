package polski.exam

import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest
import polski.exam.reading.ReadingExam
import polski.exam.reading.ReadingExamScorer
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningExamScorer

/** A read-only review of a submitted part's frozen answer map. */
data class ExamReviewRow(
    val itemId: String,
    val chosenAnswer: String?,
    val officialExamples: List<String>,
    val evaluation: ExamAnswerEvaluation,
    val maximumQuarter: Int,
)

fun grammarReviewRows(test: GrammarTest, frozenAnswers: Map<String, String>): List<ExamReviewRow> =
    test.items.map { item ->
        ExamReviewRow(
            itemId = item.itemId,
            chosenAnswer = frozenAnswers[item.itemId]?.takeIf { it.isNotBlank() },
            officialExamples = officialGrammarExamples(item, test),
            evaluation = GrammarExamScorer.gradeInAttempt(test, item, frozenAnswers),
            maximumQuarter = item.maximumQuarter,
        )
    }

fun officialGrammarExamples(item: GrammarItem, test: GrammarTest? = null): List<String> {
    val paired = test?.answerPairs.orEmpty().flatMap { pair ->
        listOfNotNull(
            pair.firstAnswer.takeIf { pair.firstItemId == item.itemId },
            pair.secondAnswer.takeIf { pair.secondItemId == item.itemId },
        )
    }
    return (listOf(item.correctAnswer) + item.scoredVariants.map { it.answer } + paired).distinct()
}

fun readingReviewRows(exam: ReadingExam, frozenAnswers: Map<String, String>): List<ExamReviewRow> =
    exam.passages.flatMap { passage ->
        exam.questions.filter { it.passageId == passage.id }.map { question ->
            ExamReviewRow(question.id, frozenAnswers[question.id]?.takeIf { it.isNotBlank() },
                (listOf(question.correctAnswer) + question.scoredVariants.map { it.answer }).distinct(),
                ReadingExamScorer.grade(question, frozenAnswers[question.id].orEmpty()), question.pointsQuarter)
        }
    }

fun listeningReviewRows(exam: ListeningExam, frozenAnswers: Map<String, String>): List<ExamReviewRow> =
    exam.tasks.flatMap { task ->
        task.questions.map { question ->
            ExamReviewRow(question.id, frozenAnswers[question.id]?.takeIf { it.isNotBlank() },
                (listOf(question.correctAnswer) + question.scoredVariants.map { it.answer }).distinct(),
                ListeningExamScorer.grade(question, frozenAnswers[question.id].orEmpty()), question.pointsQuarter)
        }
    }
