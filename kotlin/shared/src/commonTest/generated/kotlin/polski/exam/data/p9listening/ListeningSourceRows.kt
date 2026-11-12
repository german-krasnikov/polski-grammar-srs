// Generated from dsl/exam.data-contract
// source-sha256=c25663c7842effa836bb812b389b1c0384ddfb8c602fa15fdc03765737f8cfa6
// Edit the source, then run biome generate.

package polski.exam.data.p9listening

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

val LISTENING_TASK_ROW_ROWS: List<ListeningTaskRow> = listOf(
    ListeningTaskRow(
        taskId = "certyfikat-polski-C1-2022-11-listen-1",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 1,
        audioUrl = "exam-audio/C1-2022-11.mp3",
        segmentStartMs = 110000,
        segmentEndMs = 444000,
        instruction = "I. Proszę uważnie słuchać tego nagrania i wykonywać zadanie zgodnie z podanym przykładem. Uwaga! Nagranie zostanie odtworzone tylko jeden raz. _____ / 6 p. (6 x 1 p.) Proszę wysłuchać kilku krótkich nagrań i zaznaczyć właściwe odpowiedzi. PRZYKŁAD Z tego nagrania wynika, że a) sąd nakazał mężczyźnie zapłacenie wysokiego odszkodowania. b) mężczyzna został niesprawiedliwie pozbawiony wolności. c) dzięki odszkodowaniu mężczyzna zapomni o wyrządzonej mu krzywdzie. Na podstawie: https://audycje.tokfm.pl/podcast/101760 [W przykładzie zaznaczono: b).]",
        transcript = "Przykład: Tomasz Komenda ma dostać prawie 13 milionów złotych od państwa, które niesłusznie wsadziło go do więzienia na 18 lat. W reakcji na wyrok mówi: „Żadne pieniądze tego nie wynagrodzą”. Pyta, kto jest gotów zamknąć się na 18 lat za 13 milionów. 1. Policjanci z Prochowic ruszyli na nietypową interwencję: przy jednej z dróg uczynny mieszkaniec zauważył bowiem dwa dorosłe i siedem młodych łabędzi. Konieczna była pomoc. - Ptaki te stwarzały zagrożenie nie tylko dla samych siebie, ale również dla ruchu drogowego, w związku z czym policjanci chwilowo wstrzymali ruch i pozwolili bezpiecznie przejść łabędziom przez drogę. Jak relacjonują stróże prawa, łabędzie bezpiecznie doszły do pobliskiego zbiornika wodnego, by cieszyć się resztą dnia. 2. Koparko-ładowarka o wartości około 150 tysięcy złotych zniknęła z jednej z firm na terenie powiatu jaworskiego kilka dni temu. - Jaworscy kryminalni prowadzą intensywne działania wykrywcze. Zabezpieczyli między innymi monitoring przemysłowy. Na monitoringu nagrał się nie tylko moment kradzieży, ale i wizerunek sprawcy. Sprawcą, złodziejem okazał się 31-letni mieszkaniec naszego powiatu. Jak się okazało, był wcześniej zatrudniony na terenie zakładu, z którego dokonał kradzieży koparko-ładowarki. 3. Ciekawe, często zaskakujące aranżacje, znani artyści i połączenie różnych stylów muzycznych. Hala regionalnego centrum sportowego, w której odbył się drugi Charytatywny Koncert z Narodową, pękała w szwach. Obok muzycznego wymiaru koncertu był również cel charytatywny. Za najwyższą kwotę 11,5 tysiąca wylicytowano kolację z dyrektorem Narodowej Orkiestry Dętej w Lubinie. Wciąż trwają kolejne licytacje na grupie facebookowej Klucze do ludzkich serc. 4. Sportowa Natura tak nazywa się nowy projekt, który jest skierowany do klubów sportowych oraz stowarzyszeń mających w swoim statucie krzewienie aktywnego stylu życia. Celem akcji jest promowanie lasów i spędzania w nich wolnego czasu. Przedstawiciele klubów sportowych oraz stowarzyszeń, aby wziąć udział w konkursie, muszą zaproponować akcję, która połączy sam las i zdrowy tryb życia. Ambasadorem akcji został Marcin Możdżonek, złoty medalista mistrzostw świata z 2014 roku, który bardzo często sam uprawia sport na świeżym powietrzu. 5. Starostwo powiatowe w Lubinie podpisało umowę z firmą, która kompleksowo przebuduje i wyremontuje budynek, *dostosując go do potrzeb osób z niepełnosprawnościami. Koszt to siedem i pół miliona złotych. Prawie dwa miliony złotych pochodzą z dofinansowania z Unii Europejskiej. Reszta to środki pochodzące z Rządowego Funduszu Inwestycji Lokalnych. To drugi etap remontu budynku. Wcześniej dokonano przebudowy sieci ciepłowniczych i elektroenergetycznych. Został również zmieniony sposób zasilania budynku prądem. Rozebrany został także stary budynek sali sportowej i basenu. 6. W obecności licznych oficjeli Witelonka otworzyła Centrum Nowoczesnych Technologii, a w nim między innymi pracownię wirtualnej rzeczywistości. Ćwicząc w pracowni wirtualnej rzeczywistości, studenci przenoszą się do konkretnych miejsc w Legnicy, gdzie kolizje i wypadki zdarzają się najczęściej. Nowoczesne technologie mają oczywiście ułatwić i uatrakcyjnić przyswajanie wiedzy, ale też dać studentom przewagę na rynku pracy. W ostatnich latach zapotrzebowanie na specjalistów z zakresu bezpieczeństwa drastycznie przecież wzrosło.",
    ),
    ListeningTaskRow(
        taskId = "certyfikat-polski-C1-2022-11-listen-2",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 2,
        audioUrl = "exam-audio/C1-2022-11.mp3",
        segmentStartMs = 444000,
        segmentEndMs = 853000,
        instruction = "II. Proszę uważnie słuchać tego nagrania i wykonywać zadanie zgodnie z podanym przykładem. Uwaga! Nagranie zostanie odtworzone dwukrotnie. _____ / 10 p. (10 x 1 p.) Proszę wysłuchać felietonu na temat ksiażki o pszczołach i zaznaczyć: TAK – gdy zdanie jest zgodne z nagraniem; NIE – gdy nie jest zgodne z nagraniem. TAK NIE PRZYKŁAD Arystoteles wskazywał różnice między ludźmi a zwierzętami. X [W przykładzie zaznaczono: TAK.]",
        transcript = "Arystoteles twierdził, że człowiek to zoon politikon, czyli zwierzę społeczne. Cechy umożliwiające współżycie w grupie, budowanie wspólnoty, obywatelskość miały odróżniać nasz gatunek od pozostałych stworzeń. Wspólnota, altruizm, społeczeństwo obywatelskie... wygląda na to, że to raczej nie o nas. Wygląda na to, że to już raczej o pszczołach. Przypomina o tym książka „Pszczoły”. Piotr Socha, jej autor, to jeden z najbardziej rozpoznawalnych grafików i najpopularniejszych ilustratorów prasowych. Jego ilustracje są bardzo charakterystyczne: epickie, pełne zabawnych detali, których odkrywanie – często ze szkłem powiększającym – bywa większą frajdą intelektualną niż czytanie tekstu, które mają za zadanie ilustrować. Nie miałem więc wątpliwości, że książka jego autorstwa będzie lekturą wielopoziomową. Owszem, mamy w niej tekst, popularnonaukowy, przystępny, opracowany przez Wojciecha Grajkowskiego, dyskretnie błąkający się u dołu każdej strony. Tekst, który jest właściwie tylko ilustracją do oszałamiających ilustracji. Pszczoły to sprawa poważna, ale Socha doskonale wie, że na sprawy poważne należy spoglądać z czułością, ale i okiem lekko przymrużonym, żeby przypadkiem do ich opisu nie wkradł się patos. Bez patosu mamy więc opowiedzianą historię, a nawet prehistorię pszczół, ich typologię, zwyczaje, prospołeczne zachowania, zasady porozumiewania się. Mamy też pszczoły w kulturach, mitologiach i religiach i mamy informacje dotyczące pszczelarstwa na różnych kontynentach. I mamy rysunki uli figuralnych, których nie da się opisać, trzeba je po prostu zobaczyć. Wreszcie mamy mnóstwo ciekawych i ważnych informacji oraz wskazówek, jak można wspólnie pomóc pszczołom w cywilizowanym świecie, który wcale nie jest im przyjazny. Staram się nie nadużywać słowa arcydzieło, ale ta książka o arcydzieło z pewnością się ociera i chyba też przywraca, chociaż na chwilę, wiarę w sensowność definicji Arystotelesa.",
    ),
    ListeningTaskRow(
        taskId = "certyfikat-polski-C1-2022-11-listen-3",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 3,
        audioUrl = "exam-audio/C1-2022-11.mp3",
        segmentStartMs = 853000,
        segmentEndMs = 1257000,
        instruction = "III. Proszę uważnie słuchać tego nagrania i wykonywać zadanie zgodnie z podanym przykładem. Uwaga! Nagranie zostanie odtworzone dwukrotnie. _____ / 5 p. (5 x 1 p.) Proszę wysłuchać rozmowy na temat gospodarstwa rolnego prowadzonego w Polsce i wybrać z podanych zdań 5 prawdziwych (zgodnych z treścią nagrania). Odpowiedzi proszę wpisać do tabeli. PRZYKŁAD 0. W gospodarstwie prowadzona jest nietypowa hodowla. 1. Wybór zwierząt przeznaczonych do hodowania w tym gospodarstwie był przypadkowy. 2. Zawód wykonywany przez pana Zbigniewa nie wiąże się z jego pracą w gospodarstwie. 3. Ślimaki trafiły do hodowli późną jesienią, co było uzależnione od ich cyklu życiowego. 4. Ślimaki hodowlane najlepiej rozmnażają się na pastwisku. 5. Wiosną ślimaki są wypuszczane na pastwisko wraz z innymi zwierzętami z gospodarstwa. 6. Ślimaki źle znoszą wiosenne spadki temperatury poniżej zera. 7. Karma dla ślimaków z Pątnowa sprowadzana jest z Afryki. 8. Właściciel hodowli dostrzega możliwość współpracy z branżami innymi niż spożywcza. 9. Śluz ślimaka może powodować oparzenia i zmiany alergiczne na skórze. 10. Kobieta wypowiada się na temat wartości odżywczych mięsa ślimaków. PRZYKŁAD Zdania prawdziwe 0. Na podstawie: https://www.youtube.com/watch?v=FPr6sw6-dgg [W przykładzie zaznaczono: 0.]",
        transcript = "Gospodarstwo rolne w Pątnowie pod Legnicą. Nic by w nim szczególnego nie było, wszak takich wiele jest w okolicy, gdyby nie fakt, co tutaj się hoduje. Ani owce, ani krowy, a... ślimaki, i to w tonach. - Kupiłem 2 lata temu teren rolny, z którym coś musiałem zrobić i tak po różnych analizach wyszło, że hodowla ślimaka będzie dobrą opcją. I tak pan Zbigniew, który na co dzień jest konserwatorem zabytków, stał się również hodowcą, tymczasem po hodowli oprowadza nas współpracownik jej właściciela. Przyglądać możemy się procesowi segregacji zwierząt, które już niebawem trafią na rynek. To już właściwie finisz, ale nim do niego doszło, trzeba było wykonać szereg innych procedur. - Zakup był w listopadzie. W listopadzie ślimaki żeśmy przywieźli. Są w specjalnym pomieszczeniu, to niby chłodnia, coś takiego. Wtedy zwierzęta są w okresie hibernacji. Kilka miesięcy później wybudzone rozmnażają się w innym, specjalnie do tego przygotowanym miejscu, a następnie, gdy pogoda sprzyja, robi się z nimi to, co właściwie ze wszystkimi zwierzętami hodowlanymi znanymi z polskiej wsi – trafiają na pastwisko. - Wtenczas przechodzą na pole pod namiot najpierw. Pod namiot najpierw, bo jest taka pora, która... Nie mogą zmarznąć. A czasami w maju, do 15 maja, są przymrozki. A gdy już pogoda jest satysfakcjonująca, ślimaki pasą się w najlepsze, karmione specjalną, warzywną paszą. W Pątnowie hoduje się konkretny rodzaj ślimaka. To gatunek prosto z Afryki. - Generalnie zawsze ślimaki były pozyskiwane na mięso. Tak jest do tej pory, natomiast gatunek, który my hodujemy, ma jeszcze tą zaletę, że wyciąga się z niego śluz. Śluz jest obecnie bardzo poszukiwanym surowcem w kosmetyce, w branży kosmetycznej, jak również w branży medycznej. Bo okazuje się, że na jego bazie można stworzyć na przykład żel na oparzenia. My jednak ślimaki kojarzymy głównie z francuskich stołów, choć w dobie globalizacji zajadać można się nimi już właściwie wszędzie. Czy mogą być ciekawym dodatkiem do diety? - Z całą pewnością mogę powiedzieć, że można je jeść na diecie, ponieważ są niskokaloryczne, w 100 gramach ślimaka mamy około 90 kalorii, w 100 gramach surowych ślimaków, oraz są wysokobiałkowe, czyli generalnie będą bezpieczne dla osób, które nie mają problemów z metabolizmem białka.",
    ),
    ListeningTaskRow(
        taskId = "certyfikat-polski-C1-2022-11-listen-4",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 4,
        audioUrl = "exam-audio/C1-2022-11.mp3",
        segmentStartMs = 1257000,
        segmentEndMs = 1733000,
        instruction = "IV. Proszę uważnie słuchać tego nagrania i wykonywać zadanie zgodnie z podanym przykładem. Uwaga! Nagranie zostanie odtworzone dwukrotnie. ______ / 10 p. (10 x 1 p.) Proszę wysłuchać reportażu na temat polskiego przemysłu i dopasować nazwy branż (1-5) do ich opisów (A-J). Uwaga! Do każdego stwierdzenia można dopasować tylko jeden opis. 1. PRZEMYSŁ STOCZNIOWY 2. PRZEMYSŁ TRANSPORTU 3. PRZEMYSŁ BUDOWLANY 4. PRZEMYSŁ GÓRNICZY I ENERGETYCZNY 5. PRZEMYSŁ BUDOWY MASZYN ROLNICZYCH Branża: PRZYKŁAD Ta branża współpracuje z europejskimi partnerami. [___5___] [W przykładzie zaznaczono: 5.]",
        transcript = "Polski przemysł. Branża maszyn i urządzeń. 1. Przemysł stoczniowy. Polska w przemyśle wysoko wyspecjalizowanym stanowi kluczowy sektor rozwoju gospodarki morskiej. Solidną pozycję w przemyśle OFFSHOR udowadniamy doświadczeniem i mistrzowską produkcją statków oraz platform wiertniczych na morzach i oceanach. Światowi armatorzy stawiają na polskie stocznie, dlatego inwestycja w port zewnętrzny zostanie zwieńczona terminalem dla ogromnych statków pasażerskich, tym samym znajdziemy się w ekstralidze, tworząc port głębokowodny, który obsłuży największe statki świata. 2. Przemysł transportu. Nadążamy za szybkimi zmianami, opracowując indywidualne rozwiązania w zakresie obsługi wielkogabarytowych platform jezdnych. Z kapitałem siedmiu dekad doświadczeń tworzymy branżę szynowego transportu w myśl ekspansji biznesu. Europa Środkowa i Wschodnia to centrum inwestycji infrastrukturalnych, dlatego polski przemysł kolejowy rozwija rynek transportu szynowego. 3. Przemysł budowlany. Projektujemy oraz tworzymy maszyny i urządzenia budowlane odpowiadające wizji współczesnych inżynierów. Tutaj, gdzie dźwigi, żurawie i suwnice stanowią element krajobrazu, Polska rośnie w siłę, tworząc międzynarodową myśl techniczną. 4. Przemysł górniczy, energetyczny. Branża wydobywcza odgrywa kluczową rolę z uwagi na wzrost zapotrzebowania na główne surowce. Nasza strategia wpływa na zwiększenie efektywności i bezpieczeństwa pracy odbiorców naszych rozwiązań. Działamy w coraz bardziej odległych geograficznie regionach. Ze starannością tworzymy wysoko wydajne i niezawodne maszyny oraz urządzenia, przede wszystkim dla górnictwa, ale także transportu, przeładunku i energetyki. „Zielony biznes” pod postacią farm wiatrowych to rozwiązanie, które pozwoli prowadzić zrównoważony i ekonomiczny rozwój. Nasza największa produkcja w Polsce i siódma w Europie składa się z sześćdziesięciu wiatraków o łącznej mocy stu dwudziestu megawatów. 5. Przemysł budowy maszyn rolniczych. Polska obsługuje partnerów europejskich w zakresie produkcji i dostaw maszyn rolniczych, takich jak kombajny i przenośniki zgrzebłowe. Polska wyróżnia się w produkcji wyrobów w niedużych seriach wymagających ogromnego zaangażowania myśli technicznej.",
    ),
    ListeningTaskRow(
        taskId = "certyfikat-polski-C1-2022-11-listen-5",
        examRef = "certyfikat-polski-C1-2022-11",
        taskNumber = 5,
        audioUrl = "exam-audio/C1-2022-11.mp3",
        segmentStartMs = 1733000,
        segmentEndMs = 2174000,
        instruction = "V. Proszę uważnie słuchać tego nagrania i wykonywać zadanie zgodnie z podanym przykładem. Uwaga! Nagranie zostanie odtworzone dwukrotnie. ______ / 9 p. Proszę wysłuchać reportażu na temat projektu radiowego „Osada Wirakocza” i odpowiedzieć na pytania. Uwaga! Forma gramatyczna odpowiedzi powinna być dopasowana do pytania. PRZYKŁAD Gdzie znajduje się osada Wirakocza? Nad Zalewem Czorsztyńskim.",
        transcript = "Osada Wirakocza. To jest takie niezwykłe miejsce nad zalewem, nad Jeziorem Czorsztyńskim, byłem tam, nawiasem mówiąc, kilka tygodni temu w czasie któregoś z wiosennych weekendów i nic się tam prawie nie zmieniło od czasu, kiedy ten program, ten projekt tam realizowaliśmy. Pomysł, bodaj jak większość chyba naszych pomysłów, zrodził się w głowie Staszka Tyczyńskiego, który tam kliknął, już wtedy był internet, kiedy to wymyśliliśmy, i znalazł stronę osady turystycznej w internecie, i mówi: „No to zrobimy tam fajny program, bo to jest miejsce, które się wiąże z legendą o Inkach, o skarbie, o czymś tam jeszcze” . Zbudowaliśmy oczywiście całą nadbudowę merytoryczną związaną właśnie z tą legendą zamku niedzickiego i też powiem, że niezwykłe to było wyzwanie od strony zarówno logistycznej, jak i dziennikarskiej, bo nie wiedzieliśmy wtedy, że tak można robić radio. Że można na przykład zaplanować sobie cały dzień, robiąc rozpiskę w Excelu. Staszek Tyczyński nam wręczał rozpisane wszystkie wejścia, gdzie, kto po kim wchodzi, rzuciliśmy wtedy wszystkie nasze siły i środki i postawiliśmy tam jakąś ogromną antenę do transmisji reporterskich w tym miejscu, chyba trwało to 4 miesiące, weekend w weekend. Ściągaliśmy wtedy ludzi z całej Polski, którzy byli bohaterami gry terenowej, jaka tam się toczyła i przyznaję, że chyba byliśmy wtedy, nawet nie zdawaliśmy sobie sprawy, że robimy coś, co dopiero nieco później robiła telewizja w programach typu Agent czy jakieś tam Bezludne wyspy czy coś w tym stylu, a myśmy to wtedy robili w radiu i to było niesamowite, do dzisiaj to pamiętam. Mam mnóstwo niezwykłych wspomnień z tamtym czasem związanych. Powiem bardzo osobistą refleksję związaną z tym miejscem. Jedną z osób, którą wtedy zakwalifikowałem, bo ja kwalifikowałem te osoby, które tam przyjeżdżały, była pewna lekarka ze Śląska. Zaciekawiło mnie, dlaczego lekarka chce wziąć udział w takiej zabawie i dlatego ją poprosiłem, żeby przyjechała. I ona przyjechała i zobaczyła któregoś dnia, jak stoję przy wozie reporterskim, naszym wozie satelitarnym, a wokół mnie jest taki stos po prostu petów, przepraszam, że tak powiem brzydko, bo paliłem wtedy trzy paczki dziennie. I ta lekarka powiedziała wtedy: „Wiesz co, Paweł, pomogę ci rzucić palenie”. I to się stało. I ja do dzisiaj tego nie zapomnę i to był taki anioł, który stanął na mojej drodze, w ramach obowiązków służbowych załatwiłem sobie rzucenie palenia. Super historia, jak dla mnie. osada_wirakocza&title=Osada%20Wirakocza&podpis=Pawe%C5%82%20Pawlik%3A",
    ),
)

val LISTENING_QUESTION_ROW_ROWS: List<ListeningQuestionRow> = listOf(
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l1-q1",
        taskRef = "certyfikat-polski-C1-2022-11-listen-1",
        questionType = ListeningQuestionRowQuestionType.MULTIPLE_CHOICE,
        questionText = "Z tego nagrania wynika, że",
        options = "a) ptaki zachowywały się agresywnie wobec policjantów.|b) łabędzie chciały dostać się na drugą stronę ulicy.|c) po interwencji utrudnienia w ruchu utrzymały się do końca dnia.",
        correctAnswer = "b) łabędzie chciały dostać się na drugą stronę ulicy.",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l1-q2",
        taskRef = "certyfikat-polski-C1-2022-11-listen-1",
        questionType = ListeningQuestionRowQuestionType.MULTIPLE_CHOICE,
        questionText = "Z tego nagrania wynika, że",
        options = "a) kradzieży koparki dokonał dawny pracownik firmy.|b) koparka została sprzedana za kwotę 150 tysięcy złotych.|c) sprawca już wcześniej okradał swojego pracodawcę.",
        correctAnswer = "a) kradzieży koparki dokonał dawny pracownik firmy.",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l1-q3",
        taskRef = "certyfikat-polski-C1-2022-11-listen-1",
        questionType = ListeningQuestionRowQuestionType.MULTIPLE_CHOICE,
        questionText = "Z tego nagrania wynika, że",
        options = "a) dyrektor orkiestry zaoferował najwyższą kwotę podczas licytacji.|b) koncert był transmitowany na żywo na portalu społecznościowym.|c) koncert przyciągnął rzesze słuchaczy.",
        correctAnswer = "c) koncert przyciągnął rzesze słuchaczy.",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l1-q4",
        taskRef = "certyfikat-polski-C1-2022-11-listen-1",
        questionType = ListeningQuestionRowQuestionType.MULTIPLE_CHOICE,
        questionText = "Z tego nagrania wynika, że",
        options = "a) projekt został zainicjowany przez prezesów klubów sportowych.|b) w konkursie mogą wziąć udział zarówno organizacje, jak i osoby prywatne.|c) twarzą projektu jest uznany na świecie polski sportowiec.",
        correctAnswer = "c) twarzą projektu jest uznany na świecie polski sportowiec.",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l1-q5",
        taskRef = "certyfikat-polski-C1-2022-11-listen-1",
        questionType = ListeningQuestionRowQuestionType.MULTIPLE_CHOICE,
        questionText = "Z tego nagrania wynika, że",
        options = "a) całkowite koszty remontu ponosi Unia Europejska.|b) instalacja elektryczna w budynku wymagała modernizacji.|c) planowana jest budowa nowego obiektu sportowego w pobliżu.",
        correctAnswer = "b) instalacja elektryczna w budynku wymagała modernizacji.",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l1-q6",
        taskRef = "certyfikat-polski-C1-2022-11-listen-1",
        questionType = ListeningQuestionRowQuestionType.MULTIPLE_CHOICE,
        questionText = "Z tego nagrania wynika, że",
        options = "a) studenci mogą poznać topografię Legnicy bez opuszczania sali lekcyjnej.|b) w pracowni są prowadzone specjalne szkolenia dla kierowców.|c) w ostatnich latach znacząco wzrosła liczba wypadków drogowych.",
        correctAnswer = "a) studenci mogą poznać topografię Legnicy bez opuszczania sali lekcyjnej.",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q1",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Zdaniem narratora pszczoły są bardziej skłonne do poświęceń niż ludzie.",
        options = "TAK|NIE",
        correctAnswer = "TAK",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q2",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Prace plastyczne Piotra Sochy wyróżnia koncentracja na szczegółach.",
        options = "TAK|NIE",
        correctAnswer = "TAK",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q3",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Strona graficzna „Pszczół” dostarcza silniejszych doznań intelektualnych niż sam tekst.",
        options = "TAK|NIE",
        correctAnswer = "TAK",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q4",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Narrator spodziewał się, że książka Sochy będzie bardziej jednoznaczna.",
        options = "TAK|NIE",
        correctAnswer = "NIE",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q5",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Narratora razi nadmiernie naukowy charakter opisów.",
        options = "TAK|NIE",
        correctAnswer = "NIE",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q6",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Sukcesem ilustratora jest umiejętność lekkiego ujęcia istotnego tematu.",
        options = "TAK|NIE",
        correctAnswer = "TAK",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q7",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Książka stanowi kompendium wiedzy na temat pszczół.",
        options = "TAK|NIE",
        correctAnswer = "TAK",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q8",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Według narratora najlepszym sposobem na przedstawienie uli figuralnych jest ich wizualizacja.",
        options = "TAK|NIE",
        correctAnswer = "TAK",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q9",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Publikacja zawiera pomysły na wspieranie pszczół w niesprzyjającym im środowisku.",
        options = "TAK|NIE",
        correctAnswer = "TAK",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l2-q10",
        taskRef = "certyfikat-polski-C1-2022-11-listen-2",
        questionType = ListeningQuestionRowQuestionType.TRUE_FALSE,
        questionText = "Narratorowi nie podoba się, że „Pszczoły” z taką łatwością są klasyfikowane jako arcydzieło.",
        options = "TAK|NIE",
        correctAnswer = "NIE",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l3-q1",
        taskRef = "certyfikat-polski-C1-2022-11-listen-3",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "Proszę wybrać pięć zdań prawdziwych (zgodnych z treścią nagrania).",
        options = "1|2|3|4|5|6|7|8|9|10",
        correctAnswer = "2|3|6|8|10",
        pointsQuarter = 20,
        answerMode = ListeningQuestionRowAnswerMode.UNORDERED_SET,
        creditPerCorrectQuarter = 4,
        requiredSelections = 5,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q1",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "A. Technologie dostarczane przez tę branżę podnoszą poziom bezpieczeństwa pracy.",
        options = "1|2|3|4|5",
        correctAnswer = "4",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q2",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "B. Urządzenia z tej branży odpowiadają bieżącym koncepcjom inżynierskim.",
        options = "1|2|3|4|5",
        correctAnswer = "3",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q3",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "C. W tej branży planowana jest obecnie duża inwestycja.",
        options = "1|2|3|4|5",
        correctAnswer = "1",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q4",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "D. Ta gałąź wspiera rozwiązania proekologiczne.",
        options = "1|2|3|4|5",
        correctAnswer = "4",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q5",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "E. Niebawem Polska znajdzie się w gronie najlepszych państw w tej branży.",
        options = "1|2|3|4|5",
        correctAnswer = "1",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q6",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "F. Polski projekt znajduje się w pierwszej dziesiątce tego rodzaju inwestycji w Europie.",
        options = "1|2|3|4|5",
        correctAnswer = "4",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q7",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "G. Przemysł ten rozwija się w Polsce od siedemdziesięciu lat.",
        options = "1|2|3|4|5",
        correctAnswer = "2",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q8",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "H. Zasięg terytorialny tej branży stopniowo się poszerza.",
        options = "1|2|3|4|5",
        correctAnswer = "4",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q9",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "I. Produkcja w tej gałęzi nie jest masowa, ale bardzo wyspecjalizowana.",
        options = "1|2|3|4|5",
        correctAnswer = "5",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l4-q10",
        taskRef = "certyfikat-polski-C1-2022-11-listen-4",
        questionType = ListeningQuestionRowQuestionType.MATCHING,
        questionText = "J. Ta branża wspomaga rozwój sieci infrastrukturalnych w Europie.",
        options = "1|2|3|4|5",
        correctAnswer = "2",
        pointsQuarter = 4,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q1",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Kiedy narrator był ostatni raz w Wirakoczy?",
        correctAnswer = "Kilka tygodni temu.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q2",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Jaka była rola Staszka Tyczyńskiego w projekcie?",
        correctAnswer = "Był jego pomysłodawcą.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q3",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Czego dotyczyły lokalne legendy?",
        correctAnswer = "Inków|skarbu",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
        answerMode = ListeningQuestionRowAnswerMode.UNORDERED_SET,
        creditPerCorrectQuarter = 2,
        requiredSelections = 2,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q4",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Czym zajmował się Staszek Tyczyński w tym projekcie?",
        correctAnswer = "Organizacją pracy.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q5",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Co dziennikarze zamontowali w osadzie?",
        correctAnswer = "Antenę do transmisji.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q6",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "W czym uczestniczyli zaproszeni goście?",
        correctAnswer = "W grze terenowej.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q7",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Dlaczego program, który tworzyli radiowcy, był przełomowy?",
        correctAnswer = "Ponieważ jeszcze nie było podobnych programów w telewizji.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q8",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Jaka była rola narratora w organizacji programu?",
        correctAnswer = "Kwalifikował uczestników.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
    ListeningQuestionRow(
        questionId = "certyfikat-polski-C1-2022-11-l5-q9",
        taskRef = "certyfikat-polski-C1-2022-11-listen-5",
        questionType = ListeningQuestionRowQuestionType.FILL_GAP,
        questionText = "Co zmieniło się w życiu narratora podczas realizacji programu?",
        correctAnswer = "Rzucił palenie.",
        pointsQuarter = 4,
        gradingMode = ListeningQuestionRowGradingMode.OPEN_MANUAL,
    ),
)

val LISTENING_ACCEPTED_FACT_ROW_ROWS: List<ListeningAcceptedFactRow> = listOf(
    ListeningAcceptedFactRow(
        factId = "certyfikat-polski-C1-2022-11-l5-q3-fact-inkow",
        questionRef = "certyfikat-polski-C1-2022-11-l5-q3",
        creditQuarter = 2,
        displayText = "Inków",
    ),
    ListeningAcceptedFactRow(
        factId = "certyfikat-polski-C1-2022-11-l5-q3-fact-skarbu",
        questionRef = "certyfikat-polski-C1-2022-11-l5-q3",
        creditQuarter = 2,
        displayText = "skarbu",
    ),
)

val LISTENING_ACCEPTED_FACT_ALIAS_ROW_ROWS: List<ListeningAcceptedFactAliasRow> = listOf(
    ListeningAcceptedFactAliasRow(
        aliasId = "certyfikat-polski-C1-2022-11-l5-q3-alias-zamek-niedzicki",
        factRef = "certyfikat-polski-C1-2022-11-l5-q3-fact-skarbu",
        answerText = "zamku niedzickiego",
    ),
    ListeningAcceptedFactAliasRow(
        aliasId = "certyfikat-polski-C1-2022-11-l5-q3-alias-zamek-w-niedzicy",
        factRef = "certyfikat-polski-C1-2022-11-l5-q3-fact-skarbu",
        answerText = "zamku w Niedzicy",
    ),
)
