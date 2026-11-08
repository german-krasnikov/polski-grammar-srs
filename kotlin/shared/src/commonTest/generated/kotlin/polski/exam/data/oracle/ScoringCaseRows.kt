// Generated from dsl/exam.data-contract, dsl/exam-scoring/scoring.core-data
// source-sha256=dabbe47f952468480f5466b9db3e7730de7389028901785f44f1a4c28f4d0614
// Edit the source, then run biome generate.

package polski.exam.data.oracle

enum class ListeningQuestionRowQuestionType(val tag: String) {
    MULTIPLE_CHOICE("multiple_choice"),
    TRUE_FALSE("true_false"),
    FILL_GAP("fill_gap"),
    MATCH("match"),
}

enum class ReadingQuestionRowQuestionType(val tag: String) {
    MULTIPLE_CHOICE("multiple_choice"),
    TRUE_FALSE_NOT_GIVEN("true_false_not_given"),
    MATCH_HEADING("match_heading"),
    FILL_GAP("fill_gap"),
    SHORT_ANSWER("short_answer"),
}

enum class GrammarItemRowQuestionType(val tag: String) {
    CHOOSE_FORM("choose_form"),
    FILL_GAP("fill_gap"),
    TRANSFORM("transform"),
    WORD_FORMATION("word_formation"),
}

enum class WritingTaskRowGenre(val tag: String) {
    LETTER("letter"),
    DESCRIPTION("description"),
    ARGUMENT("argument"),
    ESSAY("essay"),
}

data class ExamRow(
    val examId: String,
    val provider: String,
    val targetLanguage: String,
    val level: String,
    val year: Int? = null,
    val sourceUrl: String? = null,
    val license: String? = null,
)

data class ListeningTaskRow(
    val taskId: String,
    val examRef: String,
    val taskNumber: Int,
    val audioUrl: String,
    val segmentStartMs: Int,
    val segmentEndMs: Int,
    val instruction: String,
    val transcript: String? = null,
)

data class ListeningQuestionRow(
    val questionId: String,
    val taskRef: String,
    val questionType: ListeningQuestionRowQuestionType,
    val questionText: String,
    val options: String? = null,
    val correctAnswer: String,
    val points: Int,
)

data class ReadingPassageRow(
    val passageId: String,
    val examRef: String,
    val taskNumber: Int,
    val title: String? = null,
    val source: String? = null,
    val instruction: String,
    val text: String,
)

data class ReadingQuestionRow(
    val questionId: String,
    val passageRef: String,
    val questionType: ReadingQuestionRowQuestionType,
    val questionText: String,
    val options: String? = null,
    val correctAnswer: String,
    val points: Int,
    val relevantParagraph: Int? = null,
)

data class GrammarTestRow(
    val testId: String,
    val examRef: String,
    val taskNumber: Int,
    val instruction: String,
)

data class GrammarItemRow(
    val itemId: String,
    val testRef: String,
    val questionType: GrammarItemRowQuestionType,
    val context: String,
    val gap: String? = null,
    val options: String? = null,
    val correctAnswer: String,
    val points: Int,
)

data class WritingTaskRow(
    val taskId: String,
    val examRef: String,
    val taskNumber: Int,
    val instruction: String,
    val promptText: String,
    val wordLimit: Int? = null,
    val genre: WritingTaskRowGenre,
)

data class ScoringCaseRow(
    val id: String,
    val answer: String,
    val expected: String,
    val correct: Boolean,
)

val SCORING_CASE_ROW_ROWS: List<ScoringCaseRow> = listOf(
    ScoringCaseRow(id = "exact-match", answer = "nas", expected = "nas", correct = true),
    ScoringCaseRow(id = "case-insensitive", answer = "Nas", expected = "nas", correct = true),
    ScoringCaseRow(id = "trimmed", answer = "  nas  ", expected = "nas", correct = true),
    ScoringCaseRow(id = "wrong-answer", answer = "nami", expected = "nas", correct = false),
    ScoringCaseRow(id = "empty-answer", answer = "", expected = "nas", correct = false),
    ScoringCaseRow(id = "diacritics-match", answer = "więc", expected = "więc", correct = true),
    ScoringCaseRow(id = "diacritics-case", answer = "Więc", expected = "więc", correct = true),
)
