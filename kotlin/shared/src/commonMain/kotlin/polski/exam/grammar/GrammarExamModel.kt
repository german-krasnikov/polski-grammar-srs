package polski.exam.grammar

import polski.exam.QuestionType

data class GrammarTest(
    val testId: String,
    val instruction: String,
    val items: List<GrammarItem>,
)

data class GrammarItem(
    val itemId: String,
    val questionType: QuestionType,
    val context: String,
    val gap: String?,
    val options: List<String>,
    val correctAnswer: String,
    val points: Int,
)

data class GrammarAttempt(val itemId: String, val answer: String)
