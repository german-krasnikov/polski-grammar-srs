// Generated from dsl/exam.data-contract
// source-sha256=c25663c7842effa836bb812b389b1c0384ddfb8c602fa15fdc03765737f8cfa6
// Edit the source, then run biome generate.

package polski.exam.data.p9rubric

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
        examId = "certyfikat-polski-C1-2022-11",
        provider = "certyfikat-polski",
        targetLanguage = "pl",
        level = "C1",
        year = 2022,
        sourceUrl = "https://certyfikatpolski.pl/wp-content/uploads/2026/01/2022.11.5-6_C1_Arkusz_egzaminacyjny.pdf",
        license = "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
    ),
)

val GRAMMAR_TEST_ROW_ROWS: List<GrammarTestRow> = listOf(
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-1",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 1,
        instruction = "I. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi słownymi formami liczb oraz wyrazów podanych w nawiasach (odmiana liczebników i rzeczowników). _____ / 5 p. (10 x 0,5 p.) Na początku lutego południowoafrykański lodołamacz z ekipą ekspedycyjną liczącą sześćdziesiąt trzy (63)PRZYKŁAD osoby (osoba)PRZYKŁAD opuścił Kapsztad i popłynął w kierunku Morza Weddella, by odnaleźć wrak słynnego statku. Wyprawa o nazwie Endurance22 wyruszyła na Antarktydę miesiąc po setnej (100.)PRZYKŁAD rocznicy (rocznica)PRZYKŁAD śmierci kapitana Ernesta Shackletona. Załogę tworzy ………………..…………………………………. 1 (kilkadziesiąt)1 ………………..…………………………………. 2 (poszukiwacz)2 przygód, bo to bardzo trudna podróż i ambitne zadanie — zmiażdżony przez lód w 1915 roku statek Endurance znajduje się prawdopodobnie na głębokości 3000 metrów. Ponadto w morzu szaleje wirujący prąd, który utrzymuje na powierzchni masę grubego lodu. Te niesprzyjające warunki przed ………………..…………………………………. 3 (kilka)3 ………………..…………………………………. 4 (rok)4 uniemożliwiły dokończenie poprzedniej misji poszukiwawczej, doprowadziły również do katastrofy Endurance. Dla znawców dawnych podroży morskich historia wyprawy z początku wieku stanowi ………………..…………………………………. 5 (2.)5 ………………..…………………………………. 6 (pozycja)6 listy rejsów dramatycznych – tuż za tragicznym kursem Titanica. Śmiałkowie z Endurance zdołali jednak przetrwać. Ostatecznie opuścili pokład po prawie ………………..…………………………………. (dziewięć)7 ………………..…………………………………. 8 (miesiąc)8 prób uwolnienia statku z lodowych kleszczy. Biwakując całymi tygodniami na lodzie morskim, obserwowali, jak ich statek ugina się pod naporem kry. Dopiero w kwietniu 1916 roku, gdy lód pękł, łodzie ratunkowe z niemal ………………..…………………………………. 9 (30)9 ………………..…………………………………. 10 (rozbitek)10 popłynęły na Wyspę Słoni. Shackleton uratował swoją załogę i wraz z nią przeszedł do historii wypraw morskich. Na podstawie: https://podroze.onet.pl/aktualnosci/gdzie-spoczywa-endurance-najwieksze-polowanie-na-wrak-w- historii/1ytv9xtr",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-2",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 2,
        instruction = "II. Proszę – zgodnie z przykładem – użyć odpowiedniej formy wyrazów podanych w nawiasach i przekształcić zdania tak, by nie zmieniły sensu (zamiana zdań złożonych na zdania pojedyncze). _____ / 5 p. (5 x 1 p.) PRZYKŁAD W nowej wersji survivalu chodzi o to, żeby poznać najskuteczniejsze sposoby wydostania się z trudnego położenia. (polegać) Nowa wersja survivalu polega na poznaniu najskuteczniejszych sposobów wydostania się z trudnego położenia.. 1. Agencje turystyczne obserwują, że wśród klientów spada zainteresowanie typowymi propozycjami. (odnotowywać) ……………………………………………………………………………………………………………………………………………… ……………………………………………………………………………………………………………………………………………… 2. Właściciele biur podróży mają świadomość, że koniecznie powinni rozszerzyć ofertę o nowe atrakcje. (rozumieć) ……………………………………………………………………………………………………………………………………………… ……………………………………………………………………………………………………………………………………………… 3. Nowa odmiana survivalu to rodzaj aktywności, której nie ograniczają ani finanse, ani wiek uczestników. (pozbawiony) ……………………………………………………………………………………………………………………………………………… …………………………………………………………………………………………………………………………….………………… 4. Uczestnicy chcą sprawdzić, czy będą umieli poradzić sobie i przetrwać w surowych warunkach. (przetestować) ……………………………………………………………………………………………………………………………………………… ……………………………………………………………………………………………………………………………………………… 5. Chętni mają świadomość, że mogą spędzić całą noc w lesie. (liczyć się ) ……………………………………………………………………………………………………………………………………………… …………………………………………………………………………………………………………….…………………………………",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-3",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 3,
        instruction = "III. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów utworzonych od słów podanych w nawiasach (tworzenie i odmiana rzeczowników odczasownikowych). _____ / 5 p. (10 x 0,5 p.) Rozmowa z psychologiem, który jest autorem książki na temat kłamstwa - Czy da się rozpoznać kłamstwo (kłamać) PRZYKŁAD bez użycia wariografu? - Badacze (badać)PRZYKŁAD twierdzą, że to do pewnego stopnia możliwe. Ważną ………………..…………………………………. 1 (wskazywać)1, którą możemy wyczytać z twarzy mówiącego, jest na przykład asymetryczność ekspresji. Radość jest symetryczna: „cieszy się” jednakowo prawa i lewa część twarzy. - Naprawdę kłamiemy nawet 200 razy dziennie? - Takie są ………………..…………………………………. 2 (ustalić)2 dokonane na podstawie ankiet, ale oczywiście to zależy od intensywności kontaktów z ludźmi. Większość naszych kłamstw wynika z grzeczności lub wygody. - Dlaczego tak się zachowujemy? - Powodem blisko jednej trzeciej kłamstw jest potrzeba ………………..…………………………………. (uniknąć)3 konfliktu, w szczególności kiedy zgadzamy się na to, by spełnić czyjąś ………………..…………………………………. 4 (prosić)4, choć wcale nie mamy na to ochoty. Często kłamstwo jest czymś w rodzaju szybkiej ………………..…………………………………. 5 (uciec)5 albo, jeśli wolimy tak to nazwać, dyplomatycznego wyjścia z sytuacji. - Kiedy zaczynamy kłamać? - Około trzeciego-czwartego roku życia. Dziecko na tym etapie ………………..…………………………………. 6 (rozwijać się)6 potrafi już odgadywać czyjeś prawdziwe ………………..…………………………………. 7 (pragnąć)7, intencje i ………………..…………………………………. (tęsknić)8 . - Skoro nasz mózg doskonalił tę zdolność w toku ………………..…………………………………. (ewoluować)9, to chyba już powinniśmy osiągnąć perfekcję. - Rzeczywiście, niektórzy z nas są doskonałymi ………………..…………………………………. (kłamać)10. Im bardziej złożona grupa społeczna, im więcej w niej zależności, tym więcej trzeba kłamstw, aby utrzymać swoją pozycję. Na podstawie: www.wysokieobcasy.pl/wysokie-obcasy/7,181615,21205724,jak-rozpoznac-klamstwo-nie-majac-wariografu- wysokie-obcasy.html",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-4",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 4,
        instruction = "IV. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów utworzonych od słów podanych w nawiasach (tworzenie i odmiana przymiotników). _____ / 5 p. (10 x 0,5 p.) Millennium Tower w San Francisco to luksusowy (luksus)PRZYKŁAD apartamentowiec, w którym mieszkania wykupiły m.in. gwiazdy sportu. Ten wielopiętrowy (wiele/piętro)PRZYKŁAD, liczący aż 58 kondygnacji budynek, który oddano do użytku w 2009 roku, niebezpiecznie się przechyla. Nie pomogły wzmocnienia fundamentów ani inne działania – niezwykle ………………..………………………….……………. 1 (koszt)1 dla inwestora. Obiekt, reklamowany jako jeden z najbardziej ………………..………………………….……………. (prestiż)2 budynków w USA, ma poważne wady ………………..………………………….……………. (konstrukcja)3 — nie tylko osiada w gruncie (ponad 40 cm od początku budowy), ale też odgina się od pionu o około 7,5 cm rocznie. Dziś całkowity przechył mierzony na szczycie wynosi 66 cm. Budynek, porównywany do Krzywej Wieży w Pizie, stał się już tematem licznych żartów. Jednak mieszkańcy wieży absolutnie nie są skłonni do ………………..………………………….……………. 4 (bez/troska)4 śmiechu z tych dowcipów i uważają, że jakiekolwiek ………………..………………………….……………. 5 (ironia)5 komentarze są wręcz nie na miejscu. Jeśli przechylanie utrzyma się w obecnym tempie, może dojść do katastrofy ………………..………………………….……………. 6 (budowla)6 . Właściciele mieszkań postanowili pozwać inwestora. - Ta sprawa jest absolutnie ………………..………………………….……………. 7 (bez/precedens)7 — powiedział zaangażowany w negocjacje przedstawiciel środowiska ………………..………………………….……………. 8 (prawnik)8 . Dlaczego dotąd nie zakończył się proces osiadania budynku? Według portalu ………………..………………………….……………. 9 (architektura)9 „Bryła” , przyczyna takiego stanu rzeczy jest ………………..………………………….……………. 10 (banał)10 – na wczesnym etapie budowy wieżowca zmniejszono głębokość jego posadowienia. Jednakże inwestor zaprzecza takim domniemaniom. Na podstawie: https://noizz.pl/design/luksusowy-wiezowiec-przechyla-sie-o-75-cm-rocznie-krzywa-wieza/3ffsg55",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-5",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 5,
        instruction = "V. Proszę – zgodnie z przykładem – uzupełnić tekst odpowiednimi wyrażeniami z ramki. Uwaga! Każdego wyrażenia można użyć tylko raz, a dwa wyrażenia są zbędne. ______ / 5 p. (10 x 0,5 p.) czyżby gdzie kiedy mianowicie natomiast przy tym pomimo że przecież więc skoro właśnie zaledwie choćby Kiedy PRZYKŁAD „Gazeta Wyborcza” wystawiła na licytację okładkę stołecznego wydania, by wesprzeć Wielką Orkiestrę Świątecznej Pomocy, nikt w Inspeerity długo się nie zastanawiał. - Bo to ………………..…………………………………. 1 nie jest zwykła reklama – mówią szefowie firmy, która zaoferowała największą kwotę na licytacji. - To świetna okazja, by się zaprezentować, a ………………..…………………………………. 2 wspomóc tę konkretną akcję charytatywną, ………………..…………………………………. 3 dlatego, że wielu pracowników co roku nosi serduszka WOŚP. Inspeerity jest w Warszawie od niedawna. Do wieżowca przy jednej ze stacji metra firma wprowadziła się ………………..…………………………………. 4 rok temu. Z biura roztacza się piękna panorama Warszawy – lśniące bryły nowych biurowców wokół Pałacu Kultury i dźwigi wciąż zmieniające ten krajobraz. Wkrótce firma przeniesie się do nowego lokum, być może ………………..…………………………………. 5 w jednym z tych nowych biurowców. Inspeerity chce utrzymać etos firmy przyjaznej, być miejscem, ………………..…………………………………. 6 ludzie się nawzajem szanują i chcą ze sobą pracować. Bo ………………..…………………………………. 7 w pracy spędzają połowę swojego życia, to ważne, by czuli się w niej dobrze. Przy naborze nowych pracowników liczy się ………………..…………………………………. 8 nie tylko kompetencja zawodowa, lecz także pozytywna energia. - Gdy ktoś ubiega się u nas o pracę, bierzemy pod uwagę „czynnik piwa” – ………………..…………………………………. 9 zastanawiamy się, czy mielibyśmy ochotę wyskoczyć z tą osobą na piwo. Braki wiedzy technicznej można uzupełnić, ………………..…………………………………. 10 ze zmianą osobowości bywa trudniej. Na podstawie: https://warszawa.wyborcza.pl/warszawa/7,54420,28147336,inspeerity-programisci-z-zasadami-na-podboj- swiata.html",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-6",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 6,
        instruction = "VI. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów podanych w nawiasach (tworzenie i odmiana imiesłowów). ______ / 5 p. (10 x 0,5 p.) Słynna powieść Jana Potockiego Rękopis znaleziony w Saragossie została napisana (napisać)PRZYKŁAD w języku francuskim. Zapoznawszy się (zapoznać się)PRZYKŁAD z nowym przekładem, Wojciech Orliński rekomenduje ją jako współczesny thriller. Czytając (czytać)PRZYKŁAD go, miał wrażenie, że hrabia Potocki, żyjący (żyć)PRZYKŁAD w XVIII wieku, był człowiekiem o bliskiej nam, współczesnej mentalności. Rękopis znaleziony w Saragossie jest przykładem opowieści szkatułkowej o ………………..…………………………………. 1 (rozwinąć)1 warstwie fabularnej. Jej walory już dawno zostały ………………..…………………………………. 2 (dostrzec)2 przez filmowców. Tekst mieści w sobie takie bogactwo wątków i historii, że dla niektórych czytelników może to być wręcz ………………..…………………………………. 3 (przytłaczać)3 . Potockiemu udało się zwiedzić Europę i część Afryki. ………………..…………………………………. 4 (Tworzyć)4 powieść, czerpał więc materiał z własnych podróży, przeżyć i oczywiście swojej niezwykłej wyobraźni. W 1847 roku dzieło zostało ………………..…………………………………. 5 (przełożyć)5 z francuskiego na polski. Okazało się jednak, że tłumacz podczas pracy kompilował dwie wersje powieści, dodatkowo ………………..…………………………………. 6 (dopisywać)6 do nich fragmenty własnego autorstwa. Anna Wasilewska – autorka nowego przekładu – ………………..…………………………………. (przeanalizować)7 dostępne manuskrypty, dokonała tłumaczenia, które pozwala na zapoznanie się z dziełem Potockiego w formie faktycznie ………………..…………………………………. (nadać)8 mu przez autora. Rękopis… to nie tylko skarb kultury, lecz także niezwykle współczesna powieść. Po jej lekturze można śmiało powiedzieć, że hrabia Potocki jest twórcą jak najbardziej ………………..…………………………………. 9 (pasować)9 do naszych czasów, zwłaszcza że – zdaniem historyków – w swojej epoce podobno często czuł się ………………..…………………………………. (zagubić)10 . Na podstawie: https://wyborcza.pl/12,82983,19161420.html",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-7",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 7,
        instruction = "VII. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów podanych w nawiasach (aspekt i różne formy czasowników). _____ / 5 p. (10 x 0,5 p.) Na przełomie lat 60. i 70. XX wieku rozpoczęto (rozpoczynać/rozpocząć)PRZYKŁAD proces odchodzenia od „pracujących” sobót. Obecnie rozważa się (rozważać/rozważyć)PRZYKŁAD pomysł stopniowego skracania tygodnia pracy do czterech dni roboczych. Z pewnością nie mówilibyśmy (mówić/powiedzieć)PRZYKŁAD dziś o tym tak wiele, gdyby nie było odpowiednich warunków społeczno-ekonomicznych – twierdzą specjaliści. Dodają, że różne kraje, każdy w swoim tempie, stopniowo będą skracać / będą skracały (skracać/skrócić)PRZYKŁAD tydzień pracy. Stan wywołany parę lat temu pandemią COVID-19 ………………..…………………………………. (przynosić/przynieść)1 ekstremalne wyzwania dla systemów opieki zdrowotnej, a także zmiany w gospodarce. Nowa rzeczywistość z dnia na dzień ………………..…………………………………. 2 (wpływać/wpłynąć)2 na nasze postrzeganie aktywności zawodowej. Zanim to wszystko się wydarzyło, grono wybitnych socjologów już wcześniej ………………..…………………………………. 3 (dochodzić/dojść)3 do wniosku, że wdrożony dekady temu system pięciodniowy z ośmiogodzinnym rytmem pracy jest anachroniczny i nie ………………..…………………………………. 4 (przystawać/przystać)4 do współczesnych wyzwań. Pracownicy chcą zwolnić tempo życia i w bardziej skupić się na sobie. Najnowsze badania sondażowe ………………..…………………………………. (dowodzić/dowieść)5, że zdaniem większości Amerykanów dodatkowy dzień wolny będzie pozytywnie wpływał na stan ich zdrowia psychicznego. Co ciekawe, ta intuicja jest słuszna. Dotychczasowe eksperymenty z ograniczeniem tygodnia pracy do czterech dni pozwalają przypuszczać, że po wprowadzeniu tej regulacji gwałtownie ………………..…………………………………. 6 (ulegać/ulec)6 zmianie dwa wskaźniki: wydajność pracy i kondycja pracowników. Przeprowadzone dotąd badania jednoznacznie wykazały, że w grupie eksperymentalnej ………………..…………………………………. 7 (odnotowywać/odnotować)7 niższy poziom stresu niż wśród przedstawicieli grupy kontrolnej. Równie ważny jest ponadto mechanizm, który polega na tym, że poprzez stopniowe wydłużanie czasu wolnego w końcu ………………..…………………………………. (doprowadzać/doprowadzić)8 do zmiany modelu gospodarczego. Im więcej mamy czasu wolnego, tym chętniej wydajemy pieniądze, żeby spędzić go zgodnie z naszymi preferencjami, a to napędza gospodarkę. Gdy zatem zdecydujemy się na dodatkowy dzień odpoczynku, to w krótkim czasie ………………..…………………………………. 9 (powstawać/powstać)9 nowe rynki zbytu, a w konsekwencji od razu ………………..…………………………………. (wzrastać/wzrosnąć)10 znaczenie handlu i sektora usług. Na podstawie: https://wyborcza.biz/biznes/7,179195,28057631,do-pracy-tylko-4-razy-w-tygodniu-pierwsze- kamyczki-w-lawinie.html#a=40&c=15",
    ),
    GrammarTestRow(
        testId = "certyfikat-polski-C1-2022-11-grammar-8",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 8,
        instruction = "VIII. Proszę – zgodnie z przykładami – uzupełnić tekst poprawnymi formami wyrazów podanych w nawiasach (odmiana rzeczowników, przymiotników, zaimków). _____ / 5 p. (10 x 0,5 p.) Dziennikarka miesięcznika „Pokolenia” rozmawia z Wojciechem Wałachowskim, który odnawia stare (stary)PRZYKŁAD powozy (powóz)PRZYKŁAD, dorożki i inne pojazdy konne. Chce przywrócić im (one)PRZYKŁAD dawny blask. - Jest pan prawnikiem, skąd więc u pana ta fascynacja ……………….………………………………. (koń)1, zaprzęgami, historią? - Jako młody chłopak jeździłem konno, potem działałem w klubach jeździeckich. W końcu zwróciłem uwagę na inne sprawy z tym związane. Zacząłem zbierać stare pojazdy, by ocalić ………………..……………………………. 2 (one)2 od zapomnienia. Szkoda, by kończyły swój żywot na złomowisku. Przecież to część naszej historii… - Znajomi się nie dziwili, widząc pana przy ………………..…………………………………. 3 (którykolwiek)3 z zajęć wykonywanych podczas renowacji? Prawnik majstrujący przy rozklekotanej bryczce to niecodzienny widok. - Moich znajomych to nie dziwi. Ale jako radca prawny uczestniczę w rozprawach i czasem wstydziłem się podać dokumenty ………………..………………………………. 4 (sędzia)4, bo miałem tak zniszczone i spracowane ręce. - Zna pan historie odnawianych pojazdów? - Niestety, nie wszystkich. Kiedyś trafił do mnie kompletnie zniszczony wizytowy „Milord” z drezdeńskiej fabryki Glazera, późniejszego producenta ………………..……………………………. (nadwozie – lm)5 dla Volkswagena. Pod koniec XIX wieku tego typu dorożką jeździła królowa Wiktoria. Może więc tym powozem poruszali się ………………..………………………. 6 (jakiś)6 ………………..…………………………. 7 (książę)7 z Polski lub Niemiec? Udało mi się także odrestaurować pojazd wyprodukowany w Sankt Petersburgu – ………………..……………………………. 8 (myśliwy)8 ………………..………………………………. 9 (wracający)9 z polowania używali go zwykle do przewozu ………………..……………………………. 10 (trofeum – lm)10. O mojej pasji mógłbym opowiadać godzinami, ale zdaję sobie sprawę, że jest to dziedzina, która nie wzbudza dziś szerszego zainteresowania. Na podstawie: H. Molka, Ocalić od zapomnienia, niech i dziś zachwycają, „Pokolenia. Warszawski Miesięcznik Seniorów”, XI 2021, s.15-17.",
    ),
)

val GRAMMAR_ITEM_ROW_ROWS: List<GrammarItemRow> = listOf(
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(kilkadziesiąt)",
        correctAnswer = "kilkudziesięciu",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(poszukiwacz)",
        correctAnswer = "poszukiwaczy",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(kilka)",
        correctAnswer = "kilku",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(rok)",
        correctAnswer = "laty",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(2.)",
        correctAnswer = "drugą",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q6",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(pozycja)",
        correctAnswer = "pozycję",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q7",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(dziewięć)",
        correctAnswer = "dziewięciu",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q8",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(miesiąc)",
        correctAnswer = "miesiącach",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q9",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(30)",
        correctAnswer = "trzydziestu",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g1-q10",
        testRef = "certyfikat-polski-C1-2022-11-grammar-1",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(rozbitek)",
        correctAnswer = "rozbitkami",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g2-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Agencje turystyczne obserwują, że wśród klientów spada zainteresowanie typowymi propozycjami. (odnotowywać)",
        correctAnswer = "Agencje turystyczne odnotowują spadek zainteresowania klientów typowymi propozycjami.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g2-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Właściciele biur podróży mają świadomość, że koniecznie powinni rozszerzyć ofertę o nowe atrakcje. (rozumieć)",
        correctAnswer = "Właściciele biur podróży rozumieją konieczność rozszerzenia oferty o nowe atrakcje.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g2-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Nowa odmiana survivalu to rodzaj aktywności, której nie ograniczają ani finanse, ani wiek uczestników. (pozbawiony)",
        correctAnswer = "Nowa odmiana survivalu to rodzaj aktywności pozbawionej ograniczeń finansowych i wiekowych.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g2-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Uczestnicy chcą sprawdzić, czy będą umieli poradzić sobie i przetrwać w surowych warunkach. (przetestować)",
        correctAnswer = "Uczestnicy chcą przetestować umiejętność poradzenia sobie i przetrwania w surowych warunkach.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g2-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-2",
        questionType = GrammarItemRowQuestionType.TRANSFORM,
        context = "Chętni mają świadomość, że mogą spędzić całą noc w lesie. (liczyć się)",
        correctAnswer = "Chętni liczą się ze spędzeniem całej nocy w lesie.",
        pointsQuarter = 4,
        gradingMode = GrammarItemRowGradingMode.OPEN_MANUAL,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(wskazywać)",
        correctAnswer = "wskazówką",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(ustalić)",
        correctAnswer = "ustalenia",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(uniknąć)",
        correctAnswer = "uniknięcia",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(prosić)",
        correctAnswer = "prośbę",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(uciec)",
        correctAnswer = "ucieczki",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q6",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(rozwijać się)",
        correctAnswer = "rozwoju",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q7",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(pragnąć)",
        correctAnswer = "pragnienia",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q8",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(tęsknić)",
        correctAnswer = "tęsknoty",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q9",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(ewoluować)",
        correctAnswer = "ewolucji",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g3-q10",
        testRef = "certyfikat-polski-C1-2022-11-grammar-3",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(kłamać)",
        correctAnswer = "kłamcami",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(koszt)",
        correctAnswer = "kosztowne",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(prestiż)",
        correctAnswer = "prestiżowych",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(konstrukcja)",
        correctAnswer = "konstrukcyjne",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(bez/troska)",
        correctAnswer = "beztroskiego",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(ironia)",
        correctAnswer = "ironiczne",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q6",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(budowla)",
        correctAnswer = "budowlanej",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q7",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(bez/precedens)",
        correctAnswer = "bezprecedensowa",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q8",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(prawnik)",
        correctAnswer = "prawniczego",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q9",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(architektura)",
        correctAnswer = "architektonicznego",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g4-q10",
        testRef = "certyfikat-polski-C1-2022-11-grammar-4",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(banał)",
        correctAnswer = "banalna",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 1 w tekście powyżej",
        correctAnswer = "przecież",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 2 w tekście powyżej",
        correctAnswer = "przy tym",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 3 w tekście powyżej",
        correctAnswer = "choćby",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 4 w tekście powyżej",
        correctAnswer = "zaledwie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 5 w tekście powyżej",
        correctAnswer = "właśnie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q6",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 6 w tekście powyżej",
        correctAnswer = "gdzie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q7",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 7 w tekście powyżej",
        correctAnswer = "skoro",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q8",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 8 w tekście powyżej",
        correctAnswer = "więc",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q9",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 9 w tekście powyżej",
        correctAnswer = "mianowicie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g5-q10",
        testRef = "certyfikat-polski-C1-2022-11-grammar-5",
        questionType = GrammarItemRowQuestionType.WORD_BANK,
        context = "Luka 10 w tekście powyżej",
        correctAnswer = "natomiast",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(rozwinąć)",
        correctAnswer = "rozwiniętej",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(dostrzec)",
        correctAnswer = "dostrzeżone",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(przytłaczać)",
        correctAnswer = "przytłaczające",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(Tworzyć)",
        correctAnswer = "Tworząc",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(przełożyć)",
        correctAnswer = "przełożone",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q6",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(dopisywać)",
        correctAnswer = "dopisując",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q7",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(przeanalizować)",
        correctAnswer = "przeanalizowawszy",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q8",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(nadać)",
        correctAnswer = "nadanej",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q9",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(pasować)",
        correctAnswer = "pasującym",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g6-q10",
        testRef = "certyfikat-polski-C1-2022-11-grammar-6",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(zagubić)",
        correctAnswer = "zagubiony",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(przynosić/przynieść)",
        correctAnswer = "przyniósł",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(wpływać/wpłynąć)",
        correctAnswer = "wpłynęła",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(dochodzić/dojść)",
        correctAnswer = "doszło",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(przystawać/przystać)",
        correctAnswer = "przystaje",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(dowodzić/dowieść)",
        correctAnswer = "dowodzą",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q6",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(ulegać/ulec)",
        correctAnswer = "ulegną",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q7",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(odnotowywać/odnotować)",
        correctAnswer = "odnotowano",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q8",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(doprowadzać/doprowadzić)",
        correctAnswer = "doprowadzi się",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q9",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(powstawać/powstać)",
        correctAnswer = "powstaną",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g7-q10",
        testRef = "certyfikat-polski-C1-2022-11-grammar-7",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(wzrastać/wzrosnąć)",
        correctAnswer = "wzrośnie",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q1",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(koń)",
        correctAnswer = "końmi",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q2",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(one)",
        correctAnswer = "je",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q3",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(którykolwiek)",
        correctAnswer = "którymkolwiek",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q4",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(sędzia)",
        correctAnswer = "sędziemu",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q5",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(nadwozie – lm)",
        correctAnswer = "nadwozi",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q6",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(jakiś)",
        correctAnswer = "jacyś",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q7",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(książę)",
        correctAnswer = "książęta",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q8",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(myśliwy)",
        correctAnswer = "myśliwi",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q9",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(wracający)",
        correctAnswer = "wracający",
        pointsQuarter = 2,
    ),
    GrammarItemRow(
        itemId = "certyfikat-polski-C1-2022-11-g8-q10",
        testRef = "certyfikat-polski-C1-2022-11-grammar-8",
        questionType = GrammarItemRowQuestionType.FILL_GAP,
        context = "(trofeum – lm)",
        correctAnswer = "trofeów",
        pointsQuarter = 2,
    ),
)

val GRAMMAR_RUBRIC_COMPONENT_ROW_ROWS: List<GrammarRubricComponentRow> = listOf(
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q1-c1",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q1",
        order = 1,
        creditQuarter = 1,
        criterionText = "odnotowują",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q1-c2",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q1",
        order = 2,
        creditQuarter = 1,
        criterionText = "spadek",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q1-c3",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q1",
        order = 3,
        creditQuarter = 1,
        criterionText = "zainteresowania",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q1-c4",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q1",
        order = 4,
        creditQuarter = 1,
        criterionText = "klientów",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q2-c1",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q2",
        order = 1,
        creditQuarter = 1,
        criterionText = "rozumieją",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q2-c2",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q2",
        order = 2,
        creditQuarter = 1,
        criterionText = "konieczność",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q2-c3",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q2",
        order = 3,
        creditQuarter = 1,
        criterionText = "rozszerzenia",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q2-c4",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q2",
        order = 4,
        creditQuarter = 1,
        criterionText = "oferty",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q3-c1",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q3",
        order = 1,
        creditQuarter = 1,
        criterionText = "pozbawionej/pozbawiony",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q3-c2",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q3",
        order = 2,
        creditQuarter = 1,
        criterionText = "ograniczeń",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q3-c3",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q3",
        order = 3,
        creditQuarter = 1,
        criterionText = "finansowych",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q3-c4",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q3",
        order = 4,
        creditQuarter = 1,
        criterionText = "i wiekowych",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q4-c1",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q4",
        order = 1,
        creditQuarter = 1,
        criterionText = "przetestować",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q4-c2",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q4",
        order = 2,
        creditQuarter = 1,
        criterionText = "umiejętność",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q4-c3",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q4",
        order = 3,
        creditQuarter = 1,
        criterionText = "poradzenia sobie",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q4-c4",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q4",
        order = 4,
        creditQuarter = 1,
        criterionText = "i przetrwania",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q5-c1",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q5",
        order = 1,
        creditQuarter = 1,
        criterionText = "liczą się ze / liczą się z",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q5-c2",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q5",
        order = 2,
        creditQuarter = 1,
        criterionText = "spędzeniem / możliwością",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q5-c3",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q5",
        order = 3,
        creditQuarter = 1,
        criterionText = "całej / spędzenia",
    ),
    GrammarRubricComponentRow(
        componentId = "certyfikat-polski-C1-2022-11-g2-q5-c4",
        itemRef = "certyfikat-polski-C1-2022-11-g2-q5",
        order = 4,
        creditQuarter = 1,
        criterionText = "nocy / całej nocy",
    ),
)
