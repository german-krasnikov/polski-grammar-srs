// Generated from dsl/exam.data-contract
// source-sha256=c25663c7842effa836bb812b389b1c0384ddfb8c602fa15fdc03765737f8cfa6
// Edit the source, then run biome generate.

package polski.exam.data.p9oracle

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
    WORD_BANK("word_bank"),
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

enum class GrammarPairCaseRowStatus8(val tag: String) {
    CONFIRMED_FULL("confirmed_full"),
    WRONG("wrong"),
    PENDING_MANUAL("pending_manual"),
}

enum class GrammarPairCaseRowStatus9(val tag: String) {
    CONFIRMED_FULL("confirmed_full"),
    WRONG("wrong"),
    PENDING_MANUAL("pending_manual"),
}

enum class NovemberListeningCaseRowExpectedStatus(val tag: String) {
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

data class GrammarRubricComponentRow(
    val componentId: String,
    val itemRef: String,
    val order: Int,
    val creditQuarter: Int,
    val criterionText: String,
)

data class GrammarAnswerPairRow(
    val pairId: String,
    val firstItemRef: String,
    val firstAnswer: String,
    val secondItemRef: String,
    val secondAnswer: String,
    val firstEarnedQuarter: Int,
    val secondEarnedQuarter: Int,
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

data class GrammarPairCaseRow(
    val id: String,
    val answer8: String,
    val answer9: String,
    val status8: GrammarPairCaseRowStatus8,
    val status9: GrammarPairCaseRowStatus9,
    val confirmed8: Int,
    val confirmed9: Int,
    val pending8: Int,
    val pending9: Int,
)

data class NovemberListeningCaseRow(
    val id: String,
    val answer: String,
    val expectedStatus: NovemberListeningCaseRowExpectedStatus,
    val confirmedQuarter: Int,
    val pendingQuarter: Int,
)

data class GrammarRubricCaseRow(
    val id: String,
    val itemRef: String,
    val componentCount: Int,
    val maximumQuarter: Int,
    val unknownAnswer: String,
    val unknownPendingQuarter: Int,
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

val EXAM_ROW_ROWS: List<ExamRow> = listOf(
    ExamRow(
        examId = "certyfikat-polski-C1-2024-04",
        provider = "certyfikat-polski",
        targetLanguage = "pl",
        level = "C1",
        year = 2024,
        sourceUrl = "https://certyfikatpolski.pl/wp-content/uploads/2025/02/20-21.04.2024-C1_arkusz_egzaminacyjny.pdf",
        license = "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
    ),
)

val GRAMMAR_TEST_ROW_ROWS: List<GrammarTestRow> = listOf(
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-1",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 1,
        instruction = "POPRAWNOŚĆ GRAMATYCZNA 8 zadań, 60 minut Liczba punktów: 40 I. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi słownymi formami liczb oraz wyrazów podanych w nawiasach (odmiana liczebników i rzeczowników). _____ / 5 p. (10 x 0,5 p.) Przed dziesięciu (10)PRZYKŁAD laty (rok)PRZYKŁAD na Przeglądzie Piosenki Aktorskiej we Wrocławiu Karolina Czarnecka wykonała utwór, który natychmiast podbił internet. By nagrać do niego teledysk, potrzebna była kwota ………………..…………………………………. 1 (9)1 ………………..…………………………………. 2 (tysiąc)2 ………………..…………………………………. 3 (złoty)3 . Wokalistka zwróciła się do portalu crowdfundingowego PolakPotrafi.pl i po upływie niespełna ………………..…………………………………. 4 (2)4 ………………..…………………………………. 5 (doba)5 udało się jej zebrać całą tę sumę. Crowdfunding to w dosłownym tłumaczeniu ‘finansowanie przez tłum’. W Polsce taka forma zbierania pieniędzy nosi nazwę „finansowanie społecznościowe”. Projekt, na który gromadzone są środki, może dotyczyć właściwie wszystkiego – na przykład wydania gry planszowej przygotowanej przez ………………..…………………………………. 6 (parę)6 ………………..…………………………………. 7 (zapaleniec)7 czy podróży busem do Australii z ………………..…………………………………. 8 (kilka)8 ………………..…………………………………. 9 (przyjaciel)9 . Jedynym ograniczeniem jest prawo i zdrowy rozsądek. W Polsce od 2011 roku dzięki wpłatom internautów sfinansowano ponad 4000 pomysłów, choć w pierwszym roku zbiórek doliczono się jedynie ………………..…………………………………. 10 (15)10 takich, które zyskały uznanie wpłacających. Na podstawie: https://wyborcza.pl/7,75398,19435981,zbieranie-na-swoje-marzenia-crowdfunding-coraz- popularniejszy.html",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-2",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 2,
        instruction = "II. Proszę – zgodnie z przykładem – użyć odpowiedniej formy wyrazów podanych w nawiasach i przekształcić zdania tak, by nie zmieniły sensu (zamiana zdań złożonych na zdania pojedyncze). _____ / 5 p. (5 x 1 p.) PRZYKŁAD Przewagę aplikacji Trade Edge AI stanowi to, że wykorzystuje ona zaawansowany algorytm sztucznej inteligencji. (dawać) Aplikacji Trade Edge AI przewagę daje wykorzystywanie przez nią zaawansowanego algorytmu sztucznej inteligencji. 1. Programiści sugerowali, że będzie można wykorzystać technologię AI do zarabiania pieniędzy. (przewidywać) …………………………………………………………………………………………………………………………………………………………… ………………………………………………………………………………………………………………………………………..………………… 2. Zastosowanie sztucznej inteligencji spowodowało, że użytkownik nie musi posiadać specjalistycznej wiedzy na temat giełdy. (zwolnić) …………………………………………………………………………………………………………………………………………………………… ………………………………………………………………………………………………………………………………………..………………… 3. Wbudowany algorytm pozwala aplikacji, by samodzielnie podjęła decyzję. (umożliwiać) …………………………………………………………………………………………………………………………………………………………… ………………………………………………………………………………………………………………………………………..………………… 4. Wirtualny asystent sprawia, że konfiguracja konta przebiega sprawnie. (zapewniać) …………………………………………………………………………………………………………………………………………………………… ………………………………………………………………………………………………………………………………………..………………… 5. Użytkowanie tego typu aplikacji zwykle jest możliwe, gdy niezwłocznie uiścimy opłatę. (wiązać się) …………………………………………………………………………………………………………………………………………………………… ………………………………………………………………………………………………………………………………………..…………………",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-3",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 3,
        instruction = "III. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów utworzonych od słów podanych w nawiasach (tworzenie i odmiana rzeczowników odczasownikowych). _____ / 5 p. (10 x 0,5 p.) Do powstania (powstać)PRZYKŁAD Chłopów – filmu, który miał być kandydatem (kandydować)PRZYKŁAD do Oscara, przyczyniło się Muzeum Okręgowe w Bydgoszczy. Twórcy animacji wykorzystali bowiem dzieła malarskie z bydgoskiej kolekcji (kolekcjonować)PRZYKŁAD . Ten film w ………………..…………………………………. 1 (reżyserować)1 Doroty Kobieli- Welchman i Hugh Welchmana to malarska ………………..…………………………………. 2 (adaptować)2 noblowskiej powieści Władysława Reymonta. Z informacji opublikowanych przez firmę NEXT FILM – ………………..…………………………………. 3 (dystrybuować)3 Chłopów – wynika, że film zebrał wiele ………………..…………………………………. 4 (pochwalić)4 od widzów. W Chłopach wykorzystano cztery młodopolskie dzieła znajdujące się w bogatych ………………..…………………………………. 5 (zbierać)5 bydgoskiego muzeum. Pierwszy z obrazów pędzla Leona Wyczółkowskiego nosi tytuł „Kopanie buraków”. Artysta, zainspirowany francuskim impresjonizmem, postawił sobie za cel ………………..…………………………………. (wprowadzić)6 światła i koloru do polskiego malarstwa. Autorem drugiego jest Michał Wywiórski, który słynął z tego, że z niezwykłym malarskim ………………..…………………………………. 7 (wyczuć)7 ukazywał świat przyrody. Pozostałe dwa to dzieła Ferdynanda Ruszczyca oraz Józefa Chełmońskiego. Obecnie można je zobaczyć w muzeum w Radomiu na ………………..…………………………………. 8 (wystawić)8 Rok polski – przyroda, kultura, praca, święta i obyczaje. - Cieszymy się, że obrazy z naszej kolekcji zainspirowały ………………..…………………………………. 9 (tworzyć)9 Chłopów i dostarczyły im materiałów do filmowych kadrów – powiedział szef działu marketingu muzeum. – Świadczy to o ogromnej wartości artystycznej tych dzieł. Bardzo zachęcamy do ich ………………..…………………………………. 10 (obejrzeć)10! Na podstawie: Obrazy z bydgoskiego muzeum były inspiracją do filmu, który ma szansę zdobyć Oscara (wyborcza.pl)",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-4",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 4,
        instruction = "IV. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów utworzonych od słów podanych w nawiasach (tworzenie i odmiana przymiotników). _____ / 5 p. (10 x 0,5 p.) Oratorio secondo Johanna Daniela Pucklitza (1705-1774) to nowo odkryte dzieło tego kompozytora. W 2021 roku zostało nagrane przez gdański (Gdańsk)PRZYKŁAD zespół Goldberg Baroque Ensemble, który powstał dla dokumentowania i promowania naszego dziedzictwa muzycznego (muzyka)PRZYKŁAD . Płyta z nagraniem była nominowana w kategorii Najlepsze ………………..………………………….……………. 1 (Innowacja)1 Opracowanie Dźwięku do jednej z najważniejszych nagród muzycznych w Europie – Opus Klassik 2022. Tym sposobem Oratorio secondo zdobyło rozgłos. - Kompozycja została uznana za dzieło wzbogacające mozaikę muzyki ………………..………………………….……………. 2 (osiemnasty wiek)2 – nie ukrywa dumy Andrzej Szadejko, dyrygent Goldberg Baroque Ensemble. – Ze wspaniałym tekstem ………………..………………………….……………. 3 (anonim)3 gdańskiego poety współgra niezwykła harmonia oraz bogata obsada ………………..………………………….……………. 4 (instrument)4 . Wydawałoby się, że w muzyce epoki baroku nic nie dorówna ………………..………………………….……………. 5 (mistrz)5 dziełom Bacha lub Haendla, tymczasem utwór Pucklitza może śmiało iść w zawody z najlepszymi kompozycjami swoich czasów. Poza tym ………………..………………………….……………. 6 (Trójmiasto)6 melomani cenią go zapewne za związek ………………..………………………….……………. 7 (tematyka)7 ze słynnym Sądem ostatecznym, obrazem Hansa Memlinga eksponowanym w ………………..………………………….……………. 8 (tutaj)8 oddziale Muzeum Narodowego. Okazja, by usłyszeć oratorium na żywo, nadarzyła się w drugiej połowie listopada 2023 roku w Gdańskim Teatrze Szekspirowskim. To ………………..………………………….……………. (monument)9 dzieło, trwające blisko dwie godziny, zabrzmiało w ………………..………………………….……………. 10 (wirtuoz)10 wykonaniu uświetnionym przez artystów, którzy brali udział w nagraniu płyty. Na podstawie: https://www.gdansk.pl/wiadomosci/Po-raz-pierwszy-w-XXI-wieku-na-zywo- Swiatowa-premiera-Oratorio-Secondo-Pucklitza-w-Gdansku,a,253178 ",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-5",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 5,
        instruction = "V. Proszę – zgodnie z przykładem – uzupełnić tekst odpowiednimi wyrażeniami z ramki. Uwaga! Każdego słowa lub wyrażenia można użyć tylko raz, a dwa z nich są zbędne. ______ / 5 p. (10 x 0,5 p.) lecz także chociażby nie tylko dopiero a gdzie jeszcze którego mimo wszystko bo przecież właśnie wprawdzie zresztą Paweł Orłowski, którego PRZYKŁAD wspaniałe rzeźby zachwycają Polskę i świat, dzieli się w rozmowie z dziennikarzem swoimi artystycznymi przemyśleniami. Tomasz Kobylański: ………………..…………………………………. 1 przez pewien czas na wzgórzach można oglądać pana rzeźbę – interpretację wawelskiej „Kobiety w czepcu”. Skąd pomysł, aby ………………..…………………………………. 2 w tym miejscu pojawiła się rzeźba nowoczesna, ale tak mocno nawiązująca do historii? Paweł Orłowski: Propozycja wyszła od Zamku Królewskiego na Wawelu. Jego zespół ………………..…………………………………. 3 pielęgnuje i promuje historyczne dziedzictwo, ………………..…………………………………. 4 podejmuje inicjatywy mające przyciągnąć uwagę osób zwiedzających Kraków i Wawel, zwłaszcza młodych odbiorców. Sądzę, że ………………..…………………………………. 5 trudno by było zainteresować przypadkowego odbiorcę renesansowym dziełem, jakim jest „Kobieta w czepcu”. A dzięki tak współczesnej realizacji mam wrażenie, że zbudowaliśmy twórczy pomost pomiędzy epoką Jagiellonów ………………..…………………………………. 6 naszymi czasami. Mam nadzieję, że Jagiellonowie byliby zadowoleni z tego, że dziś Wzgórze Wawelskie jest miejscem prezentowania prac współczesnych twórców, ………………..…………………………………. 7 byli rodem bardzo otwartym, zapraszali na Wawel znamienitych artystów z całej Europy. Wśród nich znaleźli się ………………..…………………………………. 8 Santi Gucci czy Francesco Fiorentino. ………………..…………………………………. 9 nie jest tajemnicą, że od dobrych kilku lat staram się interpretować dzieła wielkich mistrzów sztuki i odnosić je do czasów współczesnych. Zapraszam wszystkich do gmachu głównego Akademii Sztuk Pięknych w Krakowie przy placu Jana Matejki, ………………..…………………………………. 10 można zobaczyć moje reinterpretacje dzieł Michała Anioła. Na podstawie: https://www.well.pl/art_culture/142/well_talk_po_sasiedzku_tworzyli_z_nim_georg_baselitz_i_tony_cragg_polski_rzezbia rz_pawel_orlowski_o_swoim_artystycznym_rzemiosle,13242.htm",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-6",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 6,
        instruction = "VI. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów podanych w nawiasach (tworzenie i odmiana imiesłowów). ______ / 5 p. (10 x 0,5 p.) Banksy zdemaskowany (zdemaskować)PRZYKŁAD? Być może stało się to za sprawą rozgłośni BBC, która odnalazłszy (odnaleźć)PRZYKŁAD archiwalne nagranie, opublikowała wywiad z artystą od lat ukrywającym (ukrywać)PRZYKŁAD swoje dane personalne. Odpowiadając (odpowiadać)PRZYKŁAD na pytanie dziennikarza, Banksy ujawnia w nim swoje imię. Tożsamość artysty ulicznego, który kryje się pod tym pseudonimem, jest dobrze ………………..…………………………………. 1 (strzec)1 tajemnicą. Wiadomo jedynie, że jest ………………..…………………………………. 2 (urodzić się)2 w Bristolu Brytyjczykiem, a jego dzieła są ………………..…………………………………. 3 (sprzedawać)3 za rekordowe kwoty. Temat tożsamości Banksy'ego podejmuje BBC, ………………..…………………………………. (nagłaśniać)4 rozmowę przeprowadzoną z nim w 2003 roku. Udzielił wtedy wywiadu dziennikarzowi ………………..…………………………………. 5 (zajmować się)5 sztuką. Artysta, wówczas dwudziestokilkuletni, ………………..…………………………………. 6 (zagadnąć)6 przez dziennikarza, czy nazywa się Robert Banks, odpowiedział: „Jestem Robbie”. To niewiele, ale jednak całkiem sporo, ………………..…………………………………. 7 (brać)7 pod uwagę, że dziennikarze dysponują już nagraniami jego głosu oraz części twarzy. Z tych danych z pewnością będą próbowali poskładać tropy. Banksy zaczął budować swoją markę pod koniec lat dziewięćdziesiątych XX wieku, ………………..…………………………………. 8 (przeprowadzić się)8 najpierw z Bristolu do Londynu. Na początku XXI w. był najbardziej rozpoznawalnym artystą graffiti w Wielkiej Brytanii. Obecnie tworzy w Nowym Jorku. Namalował między innymi ulicznego bojówkarza ………………..…………………………………. 9 (rzucać)9 bukietem kwiatów, jakby to była butelka z benzyną, oraz szczura-turystę z plecakiem i okularami słonecznymi. Do tej pory jednak tożsamość Banksy’ego nie została ………………..…………………………………. 10 (potwierdzić)10. Niektórzy podejrzewają, że to nie jeden człowiek, ale cały zespół ludzi. Na podstawie: Banksy zdemaskowany? Zwrot w sprawie światowej tajemnicy - Kultura (onet.pl) ",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-7",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 7,
        instruction = "VII. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów podanych w nawiasach (aspekt i różne formy czasowników). _____ / 5 p. (10 x 0,5 p.) Przez lata mieszkańcy Bydgoszczy uznawali (uznawać/uznać)PRZYKŁAD Młyny Rothera za powód do wstydu – i zapewne taka sytuacja utrzymywałaby się (utrzymywać się/utrzymać się)PRZYKŁAD jeszcze długo, gdyby nie działania władz miasta. Przypomnijmy (my – przypominać/przypomnieć)PRZYKŁAD, że historia młynów sięga (sięgać/sięgnąć)PRZYKŁAD 1842 roku, kiedy na wyspie w centrum miasta wybudowano (budować/wybudować)PRZYKŁAD pierwszy nowoczesny młyn. Niszczejący obiekt, nieczynny od zakończenia wojny, dopiero w 1992 roku został przeznaczony na hotel z dużym parkiem rozrywki i oddany w prywatne ręce. Ambitny plan inwestora nie ………………..…………………………………. 1 (wychodzić/wyjść)1 niestety poza papier i jeszcze do niedawna można było tylko obserwować, jak zabytkowe budynki powoli ………………..…………………………………. 2 (niszczeć/zniszczeć)2. Gdyby po 171 latach od powstania młynów nie ………………..…………………………………. 3 (podejmować/podjąć)3 żadnej przełomowej decyzji, miejsce w końcu ………………..…………………………………. 4 (popadać/popaść)4 w całkowitą ruinę. Po odkupieniu obiektu od prywatnego inwestora włodarze miasta ………………..…………………………………. 5 (rozpoczynać/rozpocząć)5 renowację. Z jakim skutkiem – każdy z łatwością ………………..…………………………………. (odpowiadać/odpowiedzieć)6 sobie sam, gdy przespaceruje się po okolicy. Dziś odnowione mury, idealna hybryda zabytkowości i nowoczesności, to siedziba Centrum Nauki i Kultury Młyny Rothera. Co ………………..…………………………………. 7 (my – znajdować/znaleźć)7 w środku? Wspaniałą przestrzeń, w której obecnie ………………..…………………………………. (odbywać się/odbyć się)8 wiele wystaw, targów i spotkań, zachęcających do integracji i kreatywnego spędzania czasu. Warto również dodać, że Młyny Rothera ………………..…………………………………. (uzyskiwać/uzyskać)9 tytuł najlepszego obiektu turystycznego. Mieszkańcy mają więc powód do dumy, a turyści – powód do przyjazdu. Będąc w Bydgoszczy, ………………..…………………………………. 10 (wy – wpadać/wpaść)10 tu chociaż na chwilę! Na podstawie: To miejsce niszczało latami, dziś jest najlepszą atrakcją w Polsce. Jedź tam, jeśli nie masz planów na weekend (gazeta.pl)]",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2024-04-grammar-8",
        examRef = "certyfikat-polski-C1-2024-04",
        taskNumber = 8,
        instruction = "VIII. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów podanych w nawiasach (odmiana rzeczowników, przymiotników, zaimków). _____ / 5 p. (10 x 0,5 p.) Typowa rodzina słowiańska mniej więcej co dziesięć lat porzucała swoje domostwo i wznosiła nowe – czasem tylko o rzut kamieniem od starego miejsca (stare miejsce)PRZYKŁAD . Tymczasowość siedlisk Słowian miała jednak cechy dobrze odpowiadające warunkom, w jakich (jaki)PRZYKŁAD rozwijała się ich kultura. O pierwszych Słowianach pisze się często, że – inaczej niż ……………….……………………………. 1 (koczowniczy)1 ……………….……………………………. 2 (Węgier – lm)2 – byli ludem osiadłym. Tymczasem mieszkańcy Słowiańszczyzny odznaczali się dużo większą mobilnością, niż można by sobie wyobrażać, a ich osady okazywały się nietrwałe. Co ciekawe, przenoszono ……………….………………………………. 3 (one)3 na stosunkowo niewielką odległość. Najbardziej typowym domostwem wczesnych Słowian była prosta ziemianka przykryta szałasowym dachem. Takich siedzib nie budowano na pokolenia, ponieważ prowizoryczna konstrukcja była w stanie się utrzymać najwyżej przez kilkanaście lat. W ……………….………………………………. 4 (strzecha)4, która dotykała ziemi, wilgoć wyrządzała szkody nie do naprawienia. ……………….………………………………. 5 (Nasz)5 ……………….………………………………. 6 (przodek)6 musieli więc stawiać kolejne szałasy. Istnieją także dowody na to, że na słowiańskich……………….………………………………. (wieś)7 trwały nieustanne przebudowy. W ……………….………………………………. 8 (obrąb)8 wioski jedne chaty rozbierano, a inne akurat stawiano. Życie w tych domostwach było wprawdzie uciążliwe i ascetyczne, ale zapewniało łatwość reakcji na ……………….………………………………. (niespodziewana)9 ……………….………………………………. 10 (trudność – lm)10. Było to ważne w niepewnych czasach przełomu starożytności oraz średniowiecza. Na podstawie: Wędrujące wioski wczesnych Słowian. Zapomniany obraz egzystencji naszych średniowiecznych przodków – WielkaHistoria",
    ),
)

val GRAMMAR_ITEM_ROW_ROWS: List<GrammarItemRow> = listOf(
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(9)",
        correctAnswer = "dziewięciu",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(tysiąc)",
        correctAnswer = "tysięcy",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(złoty)",
        correctAnswer = "złotych",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(2)",
        correctAnswer = "dwóch",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(doba)",
        correctAnswer = "dób",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q6",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(parę)",
        correctAnswer = "paru",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q7",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(zapaleniec)",
        correctAnswer = "zapaleńców",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q8",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(kilka)",
        correctAnswer = "kilku",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q9",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(przyjaciel)",
        correctAnswer = "przyjaciółmi",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g1-q10",
        testRef = "certyfikat-polski-C1-2024-04-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(15)",
        correctAnswer = "piętnastu",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g2-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Programiści sugerowali, że będzie można wykorzystać technologię AI do zarabiania pieniędzy. (przewidywać)",
        correctAnswer = "Programiści przewidywali możliwość wykorzystania technologii AI do zarabiania pieniędzy.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g2-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Zastosowanie sztucznej inteligencji spowodowało, że użytkownik nie musi posiadać specjalistycznej wiedzy na temat giełdy. (zwolnić)",
        correctAnswer = "Zastosowanie sztucznej inteligencji zwolniło użytkownika z konieczności posiadania specjalistycznej wiedzy na temat giełdy.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g2-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Wbudowany algorytm pozwala aplikacji, by samodzielnie podjęła decyzję. (umożliwiać)",
        correctAnswer = "Wbudowany algorytm umożliwia aplikacji samodzielne podjęcie decyzji.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g2-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Wirtualny asystent sprawia, że konfiguracja konta przebiega sprawnie. (zapewniać)",
        correctAnswer = "Wirtualny asystent zapewnia sprawny przebieg konfiguracji konta.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g2-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Użytkowanie tego typu aplikacji zwykle jest możliwe, gdy niezwłocznie uiścimy opłatę. (wiązać się)",
        correctAnswer = "Użytkowanie tego typu aplikacji zwykle wiąże się z niezwłocznym uiszczeniem opłaty.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(reżyserować)",
        correctAnswer = "reżyserii",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(adaptować)",
        correctAnswer = "adaptacja",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(dystrybuować)",
        correctAnswer = "dystrybutora",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(pochwalić)",
        correctAnswer = "pochwał",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(zbierać)",
        correctAnswer = "zbiorach",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q6",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(wprowadzić)",
        correctAnswer = "wprowadzenie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q7",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(wyczuć)",
        correctAnswer = "wyczuciem",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q8",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(wystawić)",
        correctAnswer = "wystawie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q9",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(tworzyć)",
        correctAnswer = "twórców",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g3-q10",
        testRef = "certyfikat-polski-C1-2024-04-grammar-3",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(obejrzeć)",
        correctAnswer = "obejrzenia",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(Innowacja)",
        correctAnswer = "Innowacyjne",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(osiemnasty wiek)",
        correctAnswer = "osiemnastowiecznej",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(anonim)",
        correctAnswer = "anonimowego",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(instrument)",
        correctAnswer = "instrumentalna",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(mistrz)",
        correctAnswer = "mistrzowskim",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q6",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(Trójmiasto)",
        correctAnswer = "trójmiejscy",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q7",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(tematyka)",
        correctAnswer = "tematyczny",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q8",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(tutaj)",
        correctAnswer = "tutejszym",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q9",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(monument)",
        correctAnswer = "monumentalne",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g4-q10",
        testRef = "certyfikat-polski-C1-2024-04-grammar-4",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(wirtuoz)",
        correctAnswer = "wirtuozerskim",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 1 w tekście powyżej",
        correctAnswer = "Jeszcze",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 2 w tekście powyżej",
        correctAnswer = "właśnie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 3 w tekście powyżej",
        correctAnswer = "nie tylko",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 4 w tekście powyżej",
        correctAnswer = "lecz także",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 5 w tekście powyżej",
        correctAnswer = "mimo wszystko",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q6",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 6 w tekście powyżej",
        correctAnswer = "a",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q7",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 7 w tekście powyżej",
        correctAnswer = "bo przecież",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q8",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 8 w tekście powyżej",
        correctAnswer = "chociażby",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q9",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 9 w tekście powyżej",
        correctAnswer = "Zresztą",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g5-q10",
        testRef = "certyfikat-polski-C1-2024-04-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 10 w tekście powyżej",
        correctAnswer = "gdzie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(strzec)",
        correctAnswer = "strzeżoną",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(urodzić się)",
        correctAnswer = "urodzonym",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(sprzedawać)",
        correctAnswer = "sprzedawane",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(nagłaśniać)",
        correctAnswer = "nagłaśniając",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(zajmować się)",
        correctAnswer = "zajmującemu się",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q6",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(zagadnąć)",
        correctAnswer = "zagadnięty",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q7",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(brać)",
        correctAnswer = "biorąc",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q8",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(przeprowadzić się)",
        correctAnswer = "przeprowadziwszy się",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q9",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(rzucać)",
        correctAnswer = "rzucającego",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g6-q10",
        testRef = "certyfikat-polski-C1-2024-04-grammar-6",
        questionType = GrammarItemRowQuestionType.WORD_FORMATION,
        context = "(potwierdzić)",
        correctAnswer = "potwierdzona",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(wychodzić/wyjść)",
        correctAnswer = "wyszedł",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(niszczeć/zniszczeć)",
        correctAnswer = "niszczeją",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(podejmować/podjąć)",
        correctAnswer = "podjęto",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(popadać/popaść)",
        correctAnswer = "popadłoby",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(rozpoczynać/rozpocząć)",
        correctAnswer = "rozpoczęli",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q6",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(odpowiadać/odpowiedzieć)",
        correctAnswer = "odpowie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q7",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(my – znajdować/znaleźć)",
        correctAnswer = "znajdziemy",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q8",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(odbywać się/odbyć się)",
        correctAnswer = "odbywa się",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q9",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(uzyskiwać/uzyskać)",
        correctAnswer = "uzyskały",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g7-q10",
        testRef = "certyfikat-polski-C1-2024-04-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(wy – wpadać/wpaść)",
        correctAnswer = "wpadnijcie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q1",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(koczowniczy)",
        correctAnswer = "koczowniczy",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q2",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(Węgier – lm)",
        correctAnswer = "Węgrzy",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q3",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(one)",
        correctAnswer = "je",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q4",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(strzecha)",
        correctAnswer = "strzesze",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q5",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(Nasz)",
        correctAnswer = "Nasi",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q6",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(przodek)",
        correctAnswer = "przodkowie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q7",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(wieś)",
        correctAnswer = "wsiach",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q8",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(obrąb)",
        correctAnswer = "obrębie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q9",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(niespodziewana)",
        correctAnswer = "niespodziewane",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2024-04-g8-q10",
        testRef = "certyfikat-polski-C1-2024-04-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(trudność – lm)",
        correctAnswer = "trudności",
        pointsQuarter = 2,
    ),
)

val GRAMMAR_ANSWER_PAIR_ROW_ROWS: List<GrammarAnswerPairRow> = listOf(
    GrammarAnswerPairRow(
        pairId = "certyfikat-polski-C1-2024-04-g1-pair-1",
        firstItemRef = "certyfikat-polski-C1-2024-04-g1-q8",
        firstAnswer = "kilku",
        secondItemRef = "certyfikat-polski-C1-2024-04-g1-q9",
        secondAnswer = "przyjaciółmi",
        firstEarnedQuarter = 2,
        secondEarnedQuarter = 2,
    ),
    GrammarAnswerPairRow(
        pairId = "certyfikat-polski-C1-2024-04-g1-pair-2",
        firstItemRef = "certyfikat-polski-C1-2024-04-g1-q8",
        firstAnswer = "kilkoma",
        secondItemRef = "certyfikat-polski-C1-2024-04-g1-q9",
        secondAnswer = "przyjaciółmi",
        firstEarnedQuarter = 2,
        secondEarnedQuarter = 2,
    ),
    GrammarAnswerPairRow(
        pairId = "certyfikat-polski-C1-2024-04-g1-pair-3",
        firstItemRef = "certyfikat-polski-C1-2024-04-g1-q8",
        firstAnswer = "kilkorgiem",
        secondItemRef = "certyfikat-polski-C1-2024-04-g1-q9",
        secondAnswer = "przyjaciół",
        firstEarnedQuarter = 2,
        secondEarnedQuarter = 2,
    ),
)
