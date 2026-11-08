// Generated from dsl/exam.data-contract
// source-sha256=38312e40b51866266241f0d457f0b1f2091a4dc0f9c70d53551cb502cdd032fe
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
    INVITATION("invitation"),
    WISHES("wishes"),
    STORY("story"),
    REPORT("report"),
    SMS("sms"),
    OPINION("opinion"),
}

enum class ExamInventoryCaseRowSection(val tag: String) {
    LISTENING("listening"),
    READING("reading"),
    GRAMMAR("grammar"),
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
    val promptImageUrl: String? = null,
    val promptImageAlt: String? = null,
)

data class ListeningQuestionRow(
    val questionId: String,
    val taskRef: String,
    val questionType: ListeningQuestionRowQuestionType,
    val questionText: String,
    val options: String? = null,
    val correctAnswer: String,
    val points: Int? = null,
    val pointsQuarter: Int? = null,
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
    val points: Int? = null,
    val pointsQuarter: Int? = null,
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
    val points: Int? = null,
    val pointsQuarter: Int? = null,
)

data class GrammarAnswerVariantRow(
    val variantId: String,
    val itemRef: String,
    val answer: String,
    val earnedQuarter: Int,
)

data class WritingTaskRow(
    val taskId: String,
    val examRef: String,
    val taskNumber: Int,
    val setId: String? = null,
    val instruction: String,
    val promptText: String,
    val promptImageUrl: String? = null,
    val promptImageAlt: String? = null,
    val wordLimit: Int? = null,
    val genre: WritingTaskRowGenre,
)

data class ScoringCaseRow(
    val id: String,
    val answer: String,
    val expected: String,
    val correct: Boolean,
)

data class QuarterScoringCaseRow(
    val id: String,
    val fullAnswer: String,
    val partialAnswer: String,
    val partialQuarter: Int,
    val maximumQuarter: Int,
    val answer: String,
    val expectedQuarter: Int,
)

data class WeightedScoringCaseRow(
    val id: String,
    val answer: String,
    val expected: String,
    val maximumQuarter: Int,
    val earnedQuarter: Int,
)

data class ExamInventoryCaseRow(
    val id: String,
    val section: ExamInventoryCaseRowSection,
    val taskRef: String,
    val itemCount: Int,
    val pointsQuarter: Int,
    val variantCount: Int? = null,
    val segmentStartMs: Int? = null,
    val segmentEndMs: Int? = null,
)

data class WritingPromptCaseRow(
    val id: String,
    val setId: String,
    val taskNumber: Int,
    val wordLimit: Int,
    val promptImageUrl: String? = null,
)

data class QuarterFormatCaseRow(
    val id: String,
    val pointsQuarter: Int,
    val expectedText: String,
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

val QUARTER_SCORING_CASE_ROW_ROWS: List<QuarterScoringCaseRow> = listOf(
    QuarterScoringCaseRow(
        id = "full",
        fullAnswer = "z którym",
        partialAnswer = "z kim",
        partialQuarter = 1,
        maximumQuarter = 2,
        answer = "z którym",
        expectedQuarter = 2,
    ),
    QuarterScoringCaseRow(
        id = "partial",
        fullAnswer = "z którym",
        partialAnswer = "z kim",
        partialQuarter = 1,
        maximumQuarter = 2,
        answer = "z kim",
        expectedQuarter = 1,
    ),
    QuarterScoringCaseRow(
        id = "wrong",
        fullAnswer = "z którym",
        partialAnswer = "z kim",
        partialQuarter = 1,
        maximumQuarter = 2,
        answer = "z czego",
        expectedQuarter = 0,
    ),
    QuarterScoringCaseRow(
        id = "empty",
        fullAnswer = "z którym",
        partialAnswer = "z kim",
        partialQuarter = 1,
        maximumQuarter = 2,
        answer = "",
        expectedQuarter = 0,
    ),
)

val WEIGHTED_SCORING_CASE_ROW_ROWS: List<WeightedScoringCaseRow> = listOf(
    WeightedScoringCaseRow(
        id = "reading-half-full",
        answer = "zna",
        expected = "zna",
        maximumQuarter = 2,
        earnedQuarter = 2,
    ),
    WeightedScoringCaseRow(
        id = "reading-half-zero",
        answer = "",
        expected = "zna",
        maximumQuarter = 2,
        earnedQuarter = 0,
    ),
    WeightedScoringCaseRow(
        id = "reading-whole-full",
        answer = "kościołów",
        expected = "kościołów",
        maximumQuarter = 4,
        earnedQuarter = 4,
    ),
)

val EXAM_INVENTORY_CASE_ROW_ROWS: List<ExamInventoryCaseRow> = listOf(
    ExamInventoryCaseRow(
        id = "b1-2017-listen-1",
        section = ExamInventoryCaseRowSection.LISTENING,
        taskRef = "certyfikat-polski-B1-2017-listen-1",
        itemCount = 14,
        pointsQuarter = 28,
        segmentStartMs = 115000,
        segmentEndMs = 369000,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-listen-2",
        section = ExamInventoryCaseRowSection.LISTENING,
        taskRef = "certyfikat-polski-B1-2017-listen-2",
        itemCount = 7,
        pointsQuarter = 28,
        segmentStartMs = 369000,
        segmentEndMs = 599000,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-listen-3",
        section = ExamInventoryCaseRowSection.LISTENING,
        taskRef = "certyfikat-polski-B1-2017-listen-3",
        itemCount = 5,
        pointsQuarter = 20,
        segmentStartMs = 599000,
        segmentEndMs = 877000,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-listen-4",
        section = ExamInventoryCaseRowSection.LISTENING,
        taskRef = "certyfikat-polski-B1-2017-listen-4",
        itemCount = 6,
        pointsQuarter = 24,
        segmentStartMs = 877000,
        segmentEndMs = 1116000,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-listen-5",
        section = ExamInventoryCaseRowSection.LISTENING,
        taskRef = "certyfikat-polski-B1-2017-listen-5",
        itemCount = 5,
        pointsQuarter = 20,
        segmentStartMs = 1116000,
        segmentEndMs = 1395000,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-read-1",
        section = ExamInventoryCaseRowSection.READING,
        taskRef = "certyfikat-polski-B1-2017-read-1",
        itemCount = 5,
        pointsQuarter = 20,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-read-2",
        section = ExamInventoryCaseRowSection.READING,
        taskRef = "certyfikat-polski-B1-2017-read-2",
        itemCount = 7,
        pointsQuarter = 28,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-read-3",
        section = ExamInventoryCaseRowSection.READING,
        taskRef = "certyfikat-polski-B1-2017-read-3",
        itemCount = 7,
        pointsQuarter = 28,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-read-4",
        section = ExamInventoryCaseRowSection.READING,
        taskRef = "certyfikat-polski-B1-2017-read-4",
        itemCount = 7,
        pointsQuarter = 28,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-read-5",
        section = ExamInventoryCaseRowSection.READING,
        taskRef = "certyfikat-polski-B1-2017-read-5",
        itemCount = 8,
        pointsQuarter = 16,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-grammar-1",
        section = ExamInventoryCaseRowSection.GRAMMAR,
        taskRef = "certyfikat-polski-B1-2017-grammar-1",
        itemCount = 10,
        pointsQuarter = 20,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-grammar-2",
        section = ExamInventoryCaseRowSection.GRAMMAR,
        taskRef = "certyfikat-polski-B1-2017-grammar-2",
        itemCount = 5,
        pointsQuarter = 10,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-grammar-3",
        section = ExamInventoryCaseRowSection.GRAMMAR,
        taskRef = "certyfikat-polski-B1-2017-grammar-3",
        itemCount = 10,
        pointsQuarter = 20,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-grammar-4",
        section = ExamInventoryCaseRowSection.GRAMMAR,
        taskRef = "certyfikat-polski-B1-2017-grammar-4",
        itemCount = 5,
        pointsQuarter = 10,
        variantCount = 2,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-grammar-5",
        section = ExamInventoryCaseRowSection.GRAMMAR,
        taskRef = "certyfikat-polski-B1-2017-grammar-5",
        itemCount = 10,
        pointsQuarter = 30,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-grammar-6",
        section = ExamInventoryCaseRowSection.GRAMMAR,
        taskRef = "certyfikat-polski-B1-2017-grammar-6",
        itemCount = 5,
        pointsQuarter = 10,
    ),
    ExamInventoryCaseRow(
        id = "b1-2017-grammar-7",
        section = ExamInventoryCaseRowSection.GRAMMAR,
        taskRef = "certyfikat-polski-B1-2017-grammar-7",
        itemCount = 5,
        pointsQuarter = 20,
    ),
)

val WRITING_PROMPT_CASE_ROW_ROWS: List<WritingPromptCaseRow> = listOf(
    WritingPromptCaseRow(id = "b1-2017-writing-I-1", setId = "I", taskNumber = 1, wordLimit = 20),
    WritingPromptCaseRow(id = "b1-2017-writing-I-2", setId = "I", taskNumber = 2, wordLimit = 180),
    WritingPromptCaseRow(id = "b1-2017-writing-II-1", setId = "II", taskNumber = 1, wordLimit = 25),
    WritingPromptCaseRow(
        id = "b1-2017-writing-II-2",
        setId = "II",
        taskNumber = 2,
        wordLimit = 175,
        promptImageUrl = "exam-images/B1-2017-writing-II-2.jpeg",
    ),
    WritingPromptCaseRow(id = "b1-2017-writing-III-1", setId = "III", taskNumber = 1, wordLimit = 60),
    WritingPromptCaseRow(id = "b1-2017-writing-III-2", setId = "III", taskNumber = 2, wordLimit = 140),
)

val QUARTER_FORMAT_CASE_ROW_ROWS: List<QuarterFormatCaseRow> = listOf(
    QuarterFormatCaseRow(id = "quarter-one", pointsQuarter = 1, expectedText = "0,25"),
    QuarterFormatCaseRow(id = "quarter-two", pointsQuarter = 2, expectedText = "0,5"),
    QuarterFormatCaseRow(id = "quarter-three", pointsQuarter = 3, expectedText = "0,75"),
    QuarterFormatCaseRow(id = "quarter-120", pointsQuarter = 120, expectedText = "30"),
)
