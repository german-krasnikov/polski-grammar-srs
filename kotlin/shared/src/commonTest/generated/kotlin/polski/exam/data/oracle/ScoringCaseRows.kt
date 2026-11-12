// Generated from dsl/exam.data-contract
// source-sha256=9f2773168dc0b459612d773f233da80748fbf9916ca5bae13c27401bf0105706
// Edit the source, then run biome generate.

package polski.exam.data.oracle

enum class ExamModuleRowModule(val tag: String) {
    LISTENING("listening"),
    READING("reading"),
    GRAMMAR("grammar"),
    WRITING("writing"),
}

enum class ListeningQuestionRowQuestionType(val tag: String) {
    MULTIPLE_CHOICE("multiple_choice"),
    TRUE_FALSE("true_false"),
    FILL_GAP("fill_gap"),
    MATCH("match"),
    MATCHING("matching"),
}

enum class ListeningQuestionRowGradingMode(val tag: String) {
    EXACT("exact"),
    OPEN_MANUAL("open_manual"),
}

enum class ListeningQuestionRowAnswerMode(val tag: String) {
    SINGLE("single"),
    UNORDERED_SET("unordered_set"),
}

enum class ListeningQuestionRowSetOverselectionMode(val tag: String) {
    LEGACY_WRONG("legacy_wrong"),
    MANUAL_REVIEW("manual_review"),
}

enum class ReadingQuestionRowQuestionType(val tag: String) {
    MULTIPLE_CHOICE("multiple_choice"),
    TRUE_FALSE_NOT_GIVEN("true_false_not_given"),
    MATCH_HEADING("match_heading"),
    FILL_GAP("fill_gap"),
    SHORT_ANSWER("short_answer"),
    GAP_FILL_FRAGMENT("gap_fill_fragment"),
    MATCHING("matching"),
}

enum class ReadingQuestionRowGradingMode(val tag: String) {
    EXACT("exact"),
    OPEN_MANUAL("open_manual"),
}

enum class ReadingQuestionRowAnswerMode(val tag: String) {
    SINGLE("single"),
    UNORDERED_SET("unordered_set"),
}

enum class ReadingQuestionRowSetOverselectionMode(val tag: String) {
    LEGACY_WRONG("legacy_wrong"),
    MANUAL_REVIEW("manual_review"),
}

enum class GrammarItemRowQuestionType(val tag: String) {
    CHOOSE_FORM("choose_form"),
    FILL_GAP("fill_gap"),
    TRANSFORM("transform"),
    WORD_FORMATION("word_formation"),
    WORD_BANK("word_bank"),
    QUESTION_FORMATION("question_formation"),
}

enum class GrammarItemRowGradingMode(val tag: String) {
    EXACT("exact"),
    OPEN_MANUAL("open_manual"),
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
    ANNOUNCEMENT("announcement"),
    EMAIL("email"),
    REVIEW("review"),
    ADVERTISEMENT("advertisement"),
    APPLICATION("application"),
    SPEECH("speech"),
    CHARACTERIZATION("characterization"),
}

enum class ExamInventoryCaseRowSection(val tag: String) {
    LISTENING("listening"),
    READING("reading"),
    GRAMMAR("grammar"),
}

enum class AnswerEvaluationCaseRowConsumer(val tag: String) {
    GRAMMAR("grammar"),
    LISTENING("listening"),
}

enum class AnswerEvaluationCaseRowMode(val tag: String) {
    EXACT("exact"),
    OPEN_MANUAL("open_manual"),
}

enum class AnswerEvaluationCaseRowExpectedStatus(val tag: String) {
    CONFIRMED_FULL("confirmed_full"),
    CONFIRMED_PARTIAL("confirmed_partial"),
    WRONG("wrong"),
    PENDING_MANUAL("pending_manual"),
}

enum class UnorderedSetCaseRowMode(val tag: String) {
    EXACT("exact"),
    OPEN_MANUAL("open_manual"),
}

enum class UnorderedSetCaseRowExpectedStatus(val tag: String) {
    CONFIRMED_FULL("confirmed_full"),
    CONFIRMED_PARTIAL("confirmed_partial"),
    WRONG("wrong"),
    PENDING_MANUAL("pending_manual"),
}

enum class ExamModuleCaseRowModule(val tag: String) {
    LISTENING("listening"),
    READING("reading"),
    GRAMMAR("grammar"),
    WRITING("writing"),
}

enum class OpenItemGroupCaseRowSection(val tag: String) {
    GRAMMAR("grammar"),
    LISTENING("listening"),
}

enum class OpenItemGroupCaseRowExpectedUnknownStatus(val tag: String) {
    PENDING_MANUAL("pending_manual"),
}

enum class OpenItemGroupCaseRowExpectedBlankStatus(val tag: String) {
    WRONG("wrong"),
}

enum class QuestionControlCaseRowExpectedKind(val tag: String) {
    SINGLE_OPTION("single_option"),
    TEXT("text"),
    UNORDERED_SET("unordered_set"),
    BANK_OPTION("bank_option"),
}

enum class QuestionControlCaseRowExpectedStatus(val tag: String) {
    CONFIRMED_FULL("confirmed_full"),
    CONFIRMED_PARTIAL("confirmed_partial"),
    WRONG("wrong"),
    PENDING_MANUAL("pending_manual"),
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

data class ExamPublicationRow(
    val routeId: String,
    val examRef: String,
    val displayTitle: String,
    val sourceDir: String,
)

data class ExamModuleRow(
    val moduleId: String,
    val examRef: String,
    val module: ExamModuleRowModule,
    val order: Int,
    val durationSeconds: Int,
    val maximumQuarter: Int,
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
    val gradingMode: ListeningQuestionRowGradingMode? = null,
    val answerMode: ListeningQuestionRowAnswerMode? = null,
    val creditPerCorrectQuarter: Int? = null,
    val requiredSelections: Int? = null,
    val setOverselectionMode: ListeningQuestionRowSetOverselectionMode? = null,
)

data class ListeningAnswerVariantRow(
    val variantId: String,
    val questionRef: String,
    val answer: String,
    val earnedQuarter: Int,
)

data class ListeningAcceptedFactRow(
    val factId: String,
    val questionRef: String,
    val creditQuarter: Int,
    val displayText: String,
)

data class ListeningAcceptedFactAliasRow(
    val aliasId: String,
    val factRef: String,
    val answerText: String,
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
    val gradingMode: ReadingQuestionRowGradingMode? = null,
    val answerMode: ReadingQuestionRowAnswerMode? = null,
    val creditPerCorrectQuarter: Int? = null,
    val requiredSelections: Int? = null,
    val setOverselectionMode: ReadingQuestionRowSetOverselectionMode? = null,
    val relevantParagraph: Int? = null,
)

data class ReadingAnswerVariantRow(
    val variantId: String,
    val questionRef: String,
    val answer: String,
    val earnedQuarter: Int,
)

data class ReadingAcceptedFactRow(
    val factId: String,
    val questionRef: String,
    val creditQuarter: Int,
    val displayText: String,
)

data class ReadingAcceptedFactAliasRow(
    val aliasId: String,
    val factRef: String,
    val answerText: String,
)

data class GrammarTestRow(
    val testId: String,
    val examRef: String,
    val taskNumber: Int,
    val instruction: String,
)

data class GrammarBankTokenRow(
    val tokenId: String,
    val testRef: String,
    val position: Int,
    val text: String,
)

data class GrammarItemRow(
    val itemId: String,
    val testRef: String,
    val questionType: GrammarItemRowQuestionType,
    val context: String,
    val questionTarget: String? = null,
    val gap: String? = null,
    val options: String? = null,
    val correctAnswer: String,
    val points: Int? = null,
    val pointsQuarter: Int? = null,
    val gradingMode: GrammarItemRowGradingMode? = null,
)

data class GrammarAnswerVariantRow(
    val variantId: String,
    val itemRef: String,
    val answer: String,
    val earnedQuarter: Int,
    val onlyIfNotUsedBy: String? = null,
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

data class AnswerEvaluationCaseRow(
    val id: String,
    val consumer: AnswerEvaluationCaseRowConsumer,
    val mode: AnswerEvaluationCaseRowMode,
    val fullAnswer: String,
    val variantAnswer: String? = null,
    val variantQuarter: Int? = null,
    val maximumQuarter: Int,
    val answer: String,
    val expectedStatus: AnswerEvaluationCaseRowExpectedStatus,
    val expectedConfirmedQuarter: Int,
    val expectedPendingQuarter: Int,
)

data class AnswerAttemptCaseRow(
    val id: String,
    val exactAnswer: String,
    val openAnswer: String,
    val exactResponse: String,
    val openResponse: String,
    val exactMaximumQuarter: Int,
    val openMaximumQuarter: Int,
    val expectedConfirmedQuarter: Int,
    val expectedPendingQuarter: Int,
    val expectedPossibleQuarter: Int,
    val expectedFinal: Boolean,
)

data class UnorderedSetCaseRow(
    val id: String,
    val correctAnswer: String,
    val options: String,
    val answer: String,
    val maximumQuarter: Int,
    val creditPerCorrectQuarter: Int? = null,
    val mode: UnorderedSetCaseRowMode? = null,
    val expectedStatus: UnorderedSetCaseRowExpectedStatus,
    val expectedConfirmedQuarter: Int,
    val expectedPendingQuarter: Int? = null,
)

data class ExamModuleCaseRow(
    val id: String,
    val examRef: String,
    val module: ExamModuleCaseRowModule,
    val order: Int,
    val durationSeconds: Int,
    val maximumQuarter: Int,
)

data class ReadingSetCaseRow(
    val id: String,
    val correctAnswer: String,
    val options: String,
    val answer: String,
    val maximumQuarter: Int,
    val creditPerCorrectQuarter: Int,
    val expectedConfirmedQuarter: Int,
)

data class OpenItemGroupCaseRow(
    val id: String,
    val section: OpenItemGroupCaseRowSection,
    val groupRef: String,
    val expectedItemCount: Int,
    val unknownAnswer: String,
    val expectedUnknownStatus: OpenItemGroupCaseRowExpectedUnknownStatus,
    val expectedBlankStatus: OpenItemGroupCaseRowExpectedBlankStatus,
)

data class GrammarConstraintCaseRow(
    val id: String,
    val answer6: String,
    val answer8: String,
    val expected6Quarter: Int,
    val expected8Quarter: Int,
)

data class BankTokenCaseRow(
    val id: String,
    val testId: String,
    val expectedOrderedTokenText: String,
    val expectedDuplicateCount: Int,
)

data class QuestionControlCaseRow(
    val id: String,
    val sourceItemId: String,
    val expectedTag: String,
    val expectedKind: QuestionControlCaseRowExpectedKind,
    val expectedOptionCount: Int,
    val expectedRawAnswer: String,
    val expectedQuestionTarget: String? = null,
    val expectedQuarter: Int,
    val expectedStatus: QuestionControlCaseRowExpectedStatus,
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

val ANSWER_EVALUATION_CASE_ROW_ROWS: List<AnswerEvaluationCaseRow> = listOf(
    AnswerEvaluationCaseRow(
        id = "exact-unknown",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.EXACT,
        fullAnswer = "Komu",
        maximumQuarter = 2,
        answer = "Z kim",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
        expectedPendingQuarter = 0,
    ),
    AnswerEvaluationCaseRow(
        id = "open-known-full",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "Bardzo brakuje mi mamy.",
        maximumQuarter = 4,
        answer = "Bardzo brakuje mi mamy.",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.CONFIRMED_FULL,
        expectedConfirmedQuarter = 4,
        expectedPendingQuarter = 0,
    ),
    AnswerEvaluationCaseRow(
        id = "open-known-partial",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "Z czego",
        variantAnswer = "Z jakiej kapusty",
        variantQuarter = 1,
        maximumQuarter = 2,
        answer = "Z jakiej kapusty",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.CONFIRMED_PARTIAL,
        expectedConfirmedQuarter = 1,
        expectedPendingQuarter = 0,
    ),
    AnswerEvaluationCaseRow(
        id = "open-empty",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "Bardzo brakuje mi mamy.",
        maximumQuarter = 4,
        answer = "",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
        expectedPendingQuarter = 0,
    ),
    AnswerEvaluationCaseRow(
        id = "open-spaces",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "Bardzo brakuje mi mamy.",
        maximumQuarter = 4,
        answer = "   ",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
        expectedPendingQuarter = 0,
    ),
    AnswerEvaluationCaseRow(
        id = "open-unknown",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "Bardzo brakuje mi mamy.",
        maximumQuarter = 4,
        answer = "Brakuje mi bardzo mamy.",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.PENDING_MANUAL,
        expectedConfirmedQuarter = 0,
        expectedPendingQuarter = 4,
    ),
    AnswerEvaluationCaseRow(
        id = "listening-open-unknown",
        consumer = AnswerEvaluationCaseRowConsumer.LISTENING,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "młodą osobą",
        maximumQuarter = 2,
        answer = "młodą dziewczyną",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.PENDING_MANUAL,
        expectedConfirmedQuarter = 0,
        expectedPendingQuarter = 2,
    ),
    AnswerEvaluationCaseRow(
        id = "listening-open-known-partial",
        consumer = AnswerEvaluationCaseRowConsumer.LISTENING,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "z dzisiejszej Etiopii",
        variantAnswer = "z Afryki",
        variantQuarter = 2,
        maximumQuarter = 4,
        answer = "z Afryki",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.CONFIRMED_PARTIAL,
        expectedConfirmedQuarter = 2,
        expectedPendingQuarter = 0,
    ),
    AnswerEvaluationCaseRow(
        id = "policy-public-canonical-over-variant",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.OPEN_MANUAL,
        fullAnswer = "tak",
        variantAnswer = "tak",
        variantQuarter = 1,
        maximumQuarter = 4,
        answer = "tak",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.CONFIRMED_FULL,
        expectedConfirmedQuarter = 4,
        expectedPendingQuarter = 0,
    ),
    AnswerEvaluationCaseRow(
        id = "policy-public-full-variant",
        consumer = AnswerEvaluationCaseRowConsumer.GRAMMAR,
        mode = AnswerEvaluationCaseRowMode.EXACT,
        fullAnswer = "tak",
        variantAnswer = "owszem",
        variantQuarter = 4,
        maximumQuarter = 4,
        answer = "owszem",
        expectedStatus = AnswerEvaluationCaseRowExpectedStatus.CONFIRMED_FULL,
        expectedConfirmedQuarter = 4,
        expectedPendingQuarter = 0,
    ),
)

val ANSWER_ATTEMPT_CASE_ROW_ROWS: List<AnswerAttemptCaseRow> = listOf(
    AnswerAttemptCaseRow(
        id = "known-plus-pending",
        exactAnswer = "w",
        openAnswer = "Bardzo brakuje mi mamy.",
        exactResponse = "w",
        openResponse = "Brakuje mi bardzo mamy.",
        exactMaximumQuarter = 4,
        openMaximumQuarter = 4,
        expectedConfirmedQuarter = 4,
        expectedPendingQuarter = 4,
        expectedPossibleQuarter = 8,
        expectedFinal = false,
    ),
    AnswerAttemptCaseRow(
        id = "wrong-plus-known",
        exactAnswer = "w",
        openAnswer = "Bardzo brakuje mi mamy.",
        exactResponse = "z",
        openResponse = "Bardzo brakuje mi mamy.",
        exactMaximumQuarter = 4,
        openMaximumQuarter = 4,
        expectedConfirmedQuarter = 4,
        expectedPendingQuarter = 0,
        expectedPossibleQuarter = 8,
        expectedFinal = true,
    ),
)

val UNORDERED_SET_CASE_ROW_ROWS: List<UnorderedSetCaseRow> = listOf(
    UnorderedSetCaseRow(
        id = "b1-2020-forward",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "1|4",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_FULL,
        expectedConfirmedQuarter = 8,
    ),
    UnorderedSetCaseRow(
        id = "b1-2020-reversed",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "4|1",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_FULL,
        expectedConfirmedQuarter = 8,
    ),
    UnorderedSetCaseRow(
        id = "b1-2020-single",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "1",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_PARTIAL,
        expectedConfirmedQuarter = 4,
    ),
    UnorderedSetCaseRow(
        id = "b1-2020-one-correct-one-wrong",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "1|3",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_PARTIAL,
        expectedConfirmedQuarter = 4,
    ),
    UnorderedSetCaseRow(
        id = "b1-2020-extra",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "1|4|3",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
    ),
    UnorderedSetCaseRow(
        id = "b1-2020-duplicate",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "1|1",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
    ),
    UnorderedSetCaseRow(
        id = "b1-2020-empty",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
    ),
    UnorderedSetCaseRow(
        id = "three-token-reversed",
        correctAnswer = "1|2|4",
        options = "1|2|3|4",
        answer = "4|2|1",
        maximumQuarter = 12,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_FULL,
        expectedConfirmedQuarter = 12,
    ),
    UnorderedSetCaseRow(
        id = "b2-2017-duplicate",
        correctAnswer = "1|2|4",
        options = "1|2|3|4",
        answer = "2|3|3",
        maximumQuarter = 12,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
    ),
    UnorderedSetCaseRow(
        id = "b2-2017-one-correct-one-wrong",
        correctAnswer = "1|2|4",
        options = "1|2|3|4",
        answer = "2|3",
        maximumQuarter = 12,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_PARTIAL,
        expectedConfirmedQuarter = 4,
    ),
    UnorderedSetCaseRow(
        id = "b1-2020-all-wrong",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "2|3",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
    ),
    UnorderedSetCaseRow(
        id = "generic-exact-single",
        correctAnswer = "1|4",
        options = "1|2|3|4",
        answer = "1",
        maximumQuarter = 8,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
    ),
    UnorderedSetCaseRow(
        id = "typed-pair-reversed",
        correctAnswer = "czajnik|filiżanka",
        options = "",
        answer = "filiżanka|czajnik",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 2,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_FULL,
        expectedConfirmedQuarter = 4,
    ),
    UnorderedSetCaseRow(
        id = "typed-one-known",
        correctAnswer = "czajnik|filiżanka",
        options = "",
        answer = "czajnik",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 2,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_PARTIAL,
        expectedConfirmedQuarter = 2,
    ),
    UnorderedSetCaseRow(
        id = "typed-one-known-one-wrong",
        correctAnswer = "czajnik|filiżanka",
        options = "",
        answer = "czajnik|obce",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 2,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.CONFIRMED_PARTIAL,
        expectedConfirmedQuarter = 2,
    ),
    UnorderedSetCaseRow(
        id = "typed-duplicate",
        correctAnswer = "czajnik|filiżanka",
        options = "",
        answer = "czajnik|czajnik",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 2,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.WRONG,
        expectedConfirmedQuarter = 0,
    ),
    UnorderedSetCaseRow(
        id = "typed-open-mixed",
        correctAnswer = "czajnik|filiżanka",
        options = "",
        answer = "czajnik|inne poprawne słowo",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 2,
        mode = UnorderedSetCaseRowMode.OPEN_MANUAL,
        expectedStatus = UnorderedSetCaseRowExpectedStatus.PENDING_MANUAL,
        expectedConfirmedQuarter = 2,
        expectedPendingQuarter = 2,
    ),
)

val OPEN_ITEM_GROUP_CASE_ROW_ROWS: List<OpenItemGroupCaseRow> = listOf(
    OpenItemGroupCaseRow(
        id = "b1-2020-grammar-vi",
        section = OpenItemGroupCaseRowSection.GRAMMAR,
        groupRef = "certyfikat-polski-B1-2020-grammar-6",
        expectedItemCount = 5,
        unknownAnswer = "inna odpowiedź spoza drukowanego klucza",
        expectedUnknownStatus = OpenItemGroupCaseRowExpectedUnknownStatus.PENDING_MANUAL,
        expectedBlankStatus = OpenItemGroupCaseRowExpectedBlankStatus.WRONG,
    ),
    OpenItemGroupCaseRow(
        id = "b2-2017-listening-v",
        section = OpenItemGroupCaseRowSection.LISTENING,
        groupRef = "certyfikat-polski-B2-2017-listen-5",
        expectedItemCount = 9,
        unknownAnswer = "inna odpowiedź spoza drukowanego klucza",
        expectedUnknownStatus = OpenItemGroupCaseRowExpectedUnknownStatus.PENDING_MANUAL,
        expectedBlankStatus = OpenItemGroupCaseRowExpectedBlankStatus.WRONG,
    ),
)

val GRAMMAR_CONSTRAINT_CASE_ROW_ROWS: List<GrammarConstraintCaseRow> = listOf(
    GrammarConstraintCaseRow(
        id = "canonical-pair",
        answer6 = "w",
        answer8 = "przy",
        expected6Quarter = 2,
        expected8Quarter = 2,
    ),
    GrammarConstraintCaseRow(
        id = "repeated-w",
        answer6 = "w",
        answer8 = "w",
        expected6Quarter = 2,
        expected8Quarter = 0,
    ),
    GrammarConstraintCaseRow(id = "blank-six", answer6 = "", answer8 = "w", expected6Quarter = 0, expected8Quarter = 2),
    GrammarConstraintCaseRow(
        id = "wrong-six",
        answer6 = "inne",
        answer8 = "w",
        expected6Quarter = 0,
        expected8Quarter = 2,
    ),
    GrammarConstraintCaseRow(
        id = "normalized-repeat",
        answer6 = "W",
        answer8 = "W",
        expected6Quarter = 2,
        expected8Quarter = 0,
    ),
    GrammarConstraintCaseRow(
        id = "blank-eight",
        answer6 = "w",
        answer8 = "",
        expected6Quarter = 2,
        expected8Quarter = 0,
    ),
)

val BANK_TOKEN_CASE_ROW_ROWS: List<BankTokenCaseRow> = listOf(
    BankTokenCaseRow(
        id = "b1-2017-6",
        testId = "certyfikat-polski-B1-2017-grammar-6",
        expectedOrderedTokenText = "a|ale|chociaż|czy|gdyby|i|że",
        expectedDuplicateCount = 0,
    ),
    BankTokenCaseRow(
        id = "b1-2019-2",
        testId = "B1-2019-grammar-2",
        expectedOrderedTokenText = "ponieważ|jeśli|czy|żeby|którzy|że|lecz",
        expectedDuplicateCount = 0,
    ),
    BankTokenCaseRow(
        id = "b1-2019-8",
        testId = "B1-2019-grammar-8",
        expectedOrderedTokenText = "bez|między|za|przez|w|około|oprócz",
        expectedDuplicateCount = 0,
    ),
    BankTokenCaseRow(
        id = "b1-2020-2",
        testId = "certyfikat-polski-B1-2020-grammar-2",
        expectedOrderedTokenText = "że|oraz|ale|chociaż|czyli|ani|więc",
        expectedDuplicateCount = 0,
    ),
    BankTokenCaseRow(
        id = "b1-2020-8",
        testId = "certyfikat-polski-B1-2020-grammar-8",
        expectedOrderedTokenText = "do|w|nad|przed|z|na|w",
        expectedDuplicateCount = 1,
    ),
    BankTokenCaseRow(
        id = "b2-2017-2",
        testId = "certyfikat-polski-B2-2017-grammar-2",
        expectedOrderedTokenText = "z|oprócz|przy|o|około|spośród|do|po|od|na|w|ponad",
        expectedDuplicateCount = 0,
    ),
    BankTokenCaseRow(
        id = "b2-2019-2",
        testId = "certyfikat-polski-B2-2019-grammar-2",
        expectedOrderedTokenText = "ponad|przy|wbrew|na|między|nad|na|po|o|do|według|z|dzięki",
        expectedDuplicateCount = 1,
    ),
    BankTokenCaseRow(
        id = "b2-2020-2",
        testId = "certyfikat-polski-B2-2020-grammar-2",
        expectedOrderedTokenText = "dla|dzięki|na|na|o|od|do|przy|u|w|wokół|wbrew|za",
        expectedDuplicateCount = 1,
    ),
)

val QUESTION_CONTROL_CASE_ROW_ROWS: List<QuestionControlCaseRow> = listOf(
    QuestionControlCaseRow(
        id = "b2-2020-bank-full",
        sourceItemId = "certyfikat-polski-B2-2020-grammar-2-1",
        expectedTag = "word_bank",
        expectedKind = QuestionControlCaseRowExpectedKind.BANK_OPTION,
        expectedOptionCount = 13,
        expectedRawAnswer = "na",
        expectedQuarter = 2,
        expectedStatus = QuestionControlCaseRowExpectedStatus.CONFIRMED_FULL,
    ),
    QuestionControlCaseRow(
        id = "b1-2020-question-formation",
        sourceItemId = "certyfikat-polski-B1-2020-grammar-5-1",
        expectedTag = "question_formation",
        expectedKind = QuestionControlCaseRowExpectedKind.TEXT,
        expectedOptionCount = 0,
        expectedRawAnswer = "Czym",
        expectedQuestionTarget = "widelcem",
        expectedQuarter = 2,
        expectedStatus = QuestionControlCaseRowExpectedStatus.CONFIRMED_FULL,
    ),
    QuestionControlCaseRow(
        id = "b1-2017-underlined-target",
        sourceItemId = "B1-2017-g4-2",
        expectedTag = "question_formation",
        expectedKind = QuestionControlCaseRowExpectedKind.TEXT,
        expectedOptionCount = 0,
        expectedRawAnswer = "z którym",
        expectedQuestionTarget = "z tym nowym",
        expectedQuarter = 2,
        expectedStatus = QuestionControlCaseRowExpectedStatus.CONFIRMED_FULL,
    ),
    QuestionControlCaseRow(
        id = "b1-2019-underlined-target",
        sourceItemId = "B1-2019-g47",
        expectedTag = "question_formation",
        expectedKind = QuestionControlCaseRowExpectedKind.TEXT,
        expectedOptionCount = 0,
        expectedRawAnswer = "Komu",
        expectedQuestionTarget = "Ewie",
        expectedQuarter = 2,
        expectedStatus = QuestionControlCaseRowExpectedStatus.CONFIRMED_FULL,
    ),
    QuestionControlCaseRow(
        id = "b2-2017-derived-word",
        sourceItemId = "certyfikat-polski-B2-2017-grammar-6-1",
        expectedTag = "word_formation",
        expectedKind = QuestionControlCaseRowExpectedKind.TEXT,
        expectedOptionCount = 0,
        expectedRawAnswer = "smutek",
        expectedQuarter = 4,
        expectedStatus = QuestionControlCaseRowExpectedStatus.CONFIRMED_FULL,
    ),
    QuestionControlCaseRow(
        id = "b2-2020-fragment",
        sourceItemId = "certyfikat-polski-B2-2020-reading-2-1",
        expectedTag = "gap_fill_fragment",
        expectedKind = QuestionControlCaseRowExpectedKind.SINGLE_OPTION,
        expectedOptionCount = 10,
        expectedRawAnswer = "E",
        expectedQuarter = 4,
        expectedStatus = QuestionControlCaseRowExpectedStatus.CONFIRMED_FULL,
    ),
    QuestionControlCaseRow(
        id = "b2-2017-matching-partial",
        sourceItemId = "certyfikat-polski-B2-2017-listening-4-1",
        expectedTag = "matching",
        expectedKind = QuestionControlCaseRowExpectedKind.UNORDERED_SET,
        expectedOptionCount = 4,
        expectedRawAnswer = "1",
        expectedQuarter = 4,
        expectedStatus = QuestionControlCaseRowExpectedStatus.CONFIRMED_PARTIAL,
    ),
    QuestionControlCaseRow(
        id = "b2-2020-open-pending",
        sourceItemId = "certyfikat-polski-B2-2020-listening-5-1",
        expectedTag = "fill_gap",
        expectedKind = QuestionControlCaseRowExpectedKind.TEXT,
        expectedOptionCount = 0,
        expectedRawAnswer = "nieznany",
        expectedQuarter = 0,
        expectedStatus = QuestionControlCaseRowExpectedStatus.PENDING_MANUAL,
    ),
    QuestionControlCaseRow(
        id = "b2-2020-bank-wrong",
        sourceItemId = "certyfikat-polski-B2-2020-grammar-2-1",
        expectedTag = "word_bank",
        expectedKind = QuestionControlCaseRowExpectedKind.BANK_OPTION,
        expectedOptionCount = 13,
        expectedRawAnswer = "wbrew",
        expectedQuarter = 0,
        expectedStatus = QuestionControlCaseRowExpectedStatus.WRONG,
    ),
)

val EXAM_MODULE_CASE_ROW_ROWS: List<ExamModuleCaseRow> = listOf(
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2017-listening",
        examRef = "certyfikat-polski-B1-2017",
        module = ExamModuleCaseRowModule.LISTENING,
        order = 1,
        durationSeconds = 1500,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2017-reading",
        examRef = "certyfikat-polski-B1-2017",
        module = ExamModuleCaseRowModule.READING,
        order = 2,
        durationSeconds = 2700,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2017-grammar",
        examRef = "certyfikat-polski-B1-2017",
        module = ExamModuleCaseRowModule.GRAMMAR,
        order = 3,
        durationSeconds = 2700,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2017-writing",
        examRef = "certyfikat-polski-B1-2017",
        module = ExamModuleCaseRowModule.WRITING,
        order = 4,
        durationSeconds = 4500,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2019-listening",
        examRef = "certyfikat-polski-B1-2019",
        module = ExamModuleCaseRowModule.LISTENING,
        order = 1,
        durationSeconds = 1500,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2019-reading",
        examRef = "certyfikat-polski-B1-2019",
        module = ExamModuleCaseRowModule.READING,
        order = 2,
        durationSeconds = 2700,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2019-grammar",
        examRef = "certyfikat-polski-B1-2019",
        module = ExamModuleCaseRowModule.GRAMMAR,
        order = 3,
        durationSeconds = 2700,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2019-writing",
        examRef = "certyfikat-polski-B1-2019",
        module = ExamModuleCaseRowModule.WRITING,
        order = 4,
        durationSeconds = 4500,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2020-listening",
        examRef = "certyfikat-polski-B1-2020",
        module = ExamModuleCaseRowModule.LISTENING,
        order = 1,
        durationSeconds = 1800,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2020-reading",
        examRef = "certyfikat-polski-B1-2020",
        module = ExamModuleCaseRowModule.READING,
        order = 2,
        durationSeconds = 2400,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2020-grammar",
        examRef = "certyfikat-polski-B1-2020",
        module = ExamModuleCaseRowModule.GRAMMAR,
        order = 3,
        durationSeconds = 2700,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B1-2020-writing",
        examRef = "certyfikat-polski-B1-2020",
        module = ExamModuleCaseRowModule.WRITING,
        order = 4,
        durationSeconds = 4500,
        maximumQuarter = 120,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2017-listening",
        examRef = "certyfikat-polski-B2-2017",
        module = ExamModuleCaseRowModule.LISTENING,
        order = 1,
        durationSeconds = 1800,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2017-reading",
        examRef = "certyfikat-polski-B2-2017",
        module = ExamModuleCaseRowModule.READING,
        order = 2,
        durationSeconds = 3600,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2017-grammar",
        examRef = "certyfikat-polski-B2-2017",
        module = ExamModuleCaseRowModule.GRAMMAR,
        order = 3,
        durationSeconds = 3600,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2017-writing",
        examRef = "certyfikat-polski-B2-2017",
        module = ExamModuleCaseRowModule.WRITING,
        order = 4,
        durationSeconds = 5400,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2019-listening",
        examRef = "certyfikat-polski-B2-2019",
        module = ExamModuleCaseRowModule.LISTENING,
        order = 1,
        durationSeconds = 1800,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2019-reading",
        examRef = "certyfikat-polski-B2-2019",
        module = ExamModuleCaseRowModule.READING,
        order = 2,
        durationSeconds = 3600,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2019-grammar",
        examRef = "certyfikat-polski-B2-2019",
        module = ExamModuleCaseRowModule.GRAMMAR,
        order = 3,
        durationSeconds = 3600,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2019-writing",
        examRef = "certyfikat-polski-B2-2019",
        module = ExamModuleCaseRowModule.WRITING,
        order = 4,
        durationSeconds = 5400,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2020-listening",
        examRef = "certyfikat-polski-B2-2020",
        module = ExamModuleCaseRowModule.LISTENING,
        order = 1,
        durationSeconds = 2400,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2020-reading",
        examRef = "certyfikat-polski-B2-2020",
        module = ExamModuleCaseRowModule.READING,
        order = 2,
        durationSeconds = 3600,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2020-grammar",
        examRef = "certyfikat-polski-B2-2020",
        module = ExamModuleCaseRowModule.GRAMMAR,
        order = 3,
        durationSeconds = 3600,
        maximumQuarter = 160,
    ),
    ExamModuleCaseRow(
        id = "certyfikat-polski-B2-2020-writing",
        examRef = "certyfikat-polski-B2-2020",
        module = ExamModuleCaseRowModule.WRITING,
        order = 4,
        durationSeconds = 5400,
        maximumQuarter = 160,
    ),
)

val READING_SET_CASE_ROW_ROWS: List<ReadingSetCaseRow> = listOf(
    ReadingSetCaseRow(
        id = "b2-2024-02-v-1",
        correctAnswer = "D",
        options = "A|B|C|D|E",
        answer = "D",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 4,
        expectedConfirmedQuarter = 4,
    ),
    ReadingSetCaseRow(
        id = "b2-2024-02-v-2",
        correctAnswer = "C|D|E",
        options = "A|B|C|D|E",
        answer = "E|C|D",
        maximumQuarter = 12,
        creditPerCorrectQuarter = 4,
        expectedConfirmedQuarter = 12,
    ),
    ReadingSetCaseRow(
        id = "b2-2024-02-v-3",
        correctAnswer = "C",
        options = "A|B|C|D|E",
        answer = "C",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 4,
        expectedConfirmedQuarter = 4,
    ),
    ReadingSetCaseRow(
        id = "b2-2024-02-v-4",
        correctAnswer = "A|C",
        options = "A|B|C|D|E",
        answer = "C|A",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedConfirmedQuarter = 8,
    ),
    ReadingSetCaseRow(
        id = "b2-2024-02-v-5",
        correctAnswer = "B",
        options = "A|B|C|D|E",
        answer = "B",
        maximumQuarter = 4,
        creditPerCorrectQuarter = 4,
        expectedConfirmedQuarter = 4,
    ),
    ReadingSetCaseRow(
        id = "b2-2024-02-v-6",
        correctAnswer = "A|E",
        options = "A|B|C|D|E",
        answer = "E|A",
        maximumQuarter = 8,
        creditPerCorrectQuarter = 4,
        expectedConfirmedQuarter = 8,
    ),
)
