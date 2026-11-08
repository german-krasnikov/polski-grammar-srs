package polski.exam.grammar

import polski.grammar.normalize

data class GrammarExamResult(val correct: Int, val total: Int, val percentage: Double)

object GrammarExamScorer {
    fun score(item: GrammarItem, answer: String): Boolean =
        normalize(answer) == normalize(item.correctAnswer)

    fun scoreAttempt(test: GrammarTest, answers: Map<String, String>): GrammarExamResult {
        val correct = test.items.count { item ->
            val answer = answers[item.itemId]
            answer != null && score(item, answer)
        }
        val total = test.items.sumOf { it.points }
        val earned = test.items.sumOf { item ->
            val answer = answers[item.itemId]
            if (answer != null && score(item, answer)) item.points else 0
        }
        return GrammarExamResult(correct = earned, total = total, percentage = if (total > 0) earned.toDouble() / total * 100.0 else 0.0)
    }
}
