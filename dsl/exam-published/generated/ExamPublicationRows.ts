// Generated from dsl/exam.data-contract
// source-sha256=d042fe012299c6328ebcf0e4cdbdb40b49b95886364fbfd07ef55064fbd4e415
// Edit the source, then run biome generate.

export type ExamModuleRowModule = "listening" | "reading" | "grammar" | "writing";

export type ListeningQuestionRowQuestionType = "multiple_choice" | "true_false" | "fill_gap" | "match" | "matching";

export type ListeningQuestionRowGradingMode = "exact" | "open_manual";

export type ListeningQuestionRowAnswerMode = "single" | "unordered_set";

export type ListeningQuestionRowSetOverselectionMode = "legacy_wrong" | "manual_review";

export type ReadingQuestionRowQuestionType = "multiple_choice" | "true_false_not_given" | "match_heading" | "fill_gap" | "short_answer" | "gap_fill_fragment" | "matching" | "word_bank";

export type ReadingQuestionRowGradingMode = "exact" | "open_manual";

export type ReadingQuestionRowAnswerMode = "single" | "unordered_set";

export type ReadingQuestionRowSetOverselectionMode = "legacy_wrong" | "manual_review";

export type GrammarItemRowQuestionType = "choose_form" | "fill_gap" | "transform" | "word_formation" | "word_bank" | "question_formation";

export type GrammarItemRowGradingMode = "exact" | "open_manual";

export type WritingTaskRowGenre = "letter" | "description" | "argument" | "essay" | "invitation" | "wishes" | "story" | "report" | "sms" | "opinion" | "announcement" | "email" | "review" | "advertisement" | "application" | "speech" | "characterization";

export type ExamInventoryCaseRowSection = "listening" | "reading" | "grammar";

export type AnswerEvaluationCaseRowConsumer = "grammar" | "listening";

export type AnswerEvaluationCaseRowMode = "exact" | "open_manual";

export type AnswerEvaluationCaseRowExpectedStatus = "confirmed_full" | "confirmed_partial" | "wrong" | "pending_manual";

export type GrammarPairCaseRowStatus8 = "confirmed_full" | "wrong" | "pending_manual";

export type GrammarPairCaseRowStatus9 = "confirmed_full" | "wrong" | "pending_manual";

export type NovemberListeningCaseRowExpectedStatus = "confirmed_full" | "confirmed_partial" | "wrong" | "pending_manual";

export type UnorderedSetCaseRowMode = "exact" | "open_manual";

export type UnorderedSetCaseRowExpectedStatus = "confirmed_full" | "confirmed_partial" | "wrong" | "pending_manual";

export type ExamModuleCaseRowModule = "listening" | "reading" | "grammar" | "writing";

export type OpenItemGroupCaseRowSection = "grammar" | "listening";

export type OpenItemGroupCaseRowExpectedUnknownStatus = "pending_manual";

export type OpenItemGroupCaseRowExpectedBlankStatus = "wrong";

export type QuestionControlCaseRowExpectedKind = "single_option" | "text" | "unordered_set" | "bank_option";

export type QuestionControlCaseRowExpectedStatus = "confirmed_full" | "confirmed_partial" | "wrong" | "pending_manual";

export interface ExamRow {
  readonly examId: string;
  readonly provider: string;
  readonly targetLanguage: string;
  readonly level: string;
  readonly year?: number;
  readonly sourceUrl?: string;
  readonly license?: string;
}

export interface ExamPublicationRow {
  readonly routeId: string;
  readonly examRef: string;
  readonly displayTitle: string;
  readonly sourceDir: string;
}

export interface ExamModuleRow {
  readonly moduleId: string;
  readonly examRef: string;
  readonly module: ExamModuleRowModule;
  readonly order: number;
  readonly durationSeconds: number;
  readonly maximumQuarter: number;
  readonly writingGuidance?: string;
}

export interface ListeningTaskRow {
  readonly taskId: string;
  readonly examRef: string;
  readonly taskNumber: number;
  readonly audioUrl: string;
  readonly segmentStartMs: number;
  readonly segmentEndMs: number;
  readonly instruction: string;
  readonly transcript?: string;
  readonly promptImageUrl?: string;
  readonly promptImageAlt?: string;
}

export interface ListeningQuestionRow {
  readonly questionId: string;
  readonly taskRef: string;
  readonly questionType: ListeningQuestionRowQuestionType;
  readonly questionText: string;
  readonly options?: string;
  readonly correctAnswer: string;
  readonly points?: number;
  readonly pointsQuarter?: number;
  readonly gradingMode?: ListeningQuestionRowGradingMode;
  readonly answerMode?: ListeningQuestionRowAnswerMode;
  readonly creditPerCorrectQuarter?: number;
  readonly requiredSelections?: number;
  readonly setOverselectionMode?: ListeningQuestionRowSetOverselectionMode;
}

export interface ListeningAnswerVariantRow {
  readonly variantId: string;
  readonly questionRef: string;
  readonly answer: string;
  readonly earnedQuarter: number;
}

export interface ListeningAcceptedFactRow {
  readonly factId: string;
  readonly questionRef: string;
  readonly creditQuarter: number;
  readonly displayText: string;
}

export interface ListeningAcceptedFactAliasRow {
  readonly aliasId: string;
  readonly factRef: string;
  readonly answerText: string;
}

export interface ReadingPassageRow {
  readonly passageId: string;
  readonly examRef: string;
  readonly taskNumber: number;
  readonly title?: string;
  readonly source?: string;
  readonly instruction: string;
  readonly text: string;
}

export interface ReadingQuestionRow {
  readonly questionId: string;
  readonly passageRef: string;
  readonly questionType: ReadingQuestionRowQuestionType;
  readonly questionText: string;
  readonly options?: string;
  readonly correctAnswer: string;
  readonly points?: number;
  readonly pointsQuarter?: number;
  readonly gradingMode?: ReadingQuestionRowGradingMode;
  readonly answerMode?: ReadingQuestionRowAnswerMode;
  readonly creditPerCorrectQuarter?: number;
  readonly requiredSelections?: number;
  readonly setOverselectionMode?: ReadingQuestionRowSetOverselectionMode;
  readonly relevantParagraph?: number;
}

export interface ReadingAnswerVariantRow {
  readonly variantId: string;
  readonly questionRef: string;
  readonly answer: string;
  readonly earnedQuarter: number;
}

export interface ReadingAcceptedFactRow {
  readonly factId: string;
  readonly questionRef: string;
  readonly creditQuarter: number;
  readonly displayText: string;
}

export interface ReadingAcceptedFactAliasRow {
  readonly aliasId: string;
  readonly factRef: string;
  readonly answerText: string;
}

export interface GrammarTestRow {
  readonly testId: string;
  readonly examRef: string;
  readonly taskNumber: number;
  readonly instruction: string;
}

export interface GrammarBankTokenRow {
  readonly tokenId: string;
  readonly testRef: string;
  readonly position: number;
  readonly text: string;
}

export interface GrammarItemRow {
  readonly itemId: string;
  readonly testRef: string;
  readonly questionType: GrammarItemRowQuestionType;
  readonly context: string;
  readonly questionTarget?: string;
  readonly gap?: string;
  readonly options?: string;
  readonly correctAnswer: string;
  readonly points?: number;
  readonly pointsQuarter?: number;
  readonly gradingMode?: GrammarItemRowGradingMode;
}

export interface GrammarAnswerVariantRow {
  readonly variantId: string;
  readonly itemRef: string;
  readonly answer: string;
  readonly earnedQuarter: number;
  readonly onlyIfNotUsedBy?: string;
}

export interface GrammarRubricComponentRow {
  readonly componentId: string;
  readonly itemRef: string;
  readonly order: number;
  readonly creditQuarter: number;
  readonly criterionText: string;
}

export interface GrammarAnswerPairRow {
  readonly pairId: string;
  readonly firstItemRef: string;
  readonly firstAnswer: string;
  readonly secondItemRef: string;
  readonly secondAnswer: string;
  readonly firstEarnedQuarter: number;
  readonly secondEarnedQuarter: number;
}

export interface WritingTaskRow {
  readonly taskId: string;
  readonly examRef: string;
  readonly taskNumber: number;
  readonly setId?: string;
  readonly instruction: string;
  readonly promptText: string;
  readonly promptImageUrl?: string;
  readonly promptImageAlt?: string;
  readonly wordLimit?: number;
  readonly genre: WritingTaskRowGenre;
}

export interface ScoringCaseRow {
  readonly id: string;
  readonly answer: string;
  readonly expected: string;
  readonly correct: boolean;
}

export interface QuarterScoringCaseRow {
  readonly id: string;
  readonly fullAnswer: string;
  readonly partialAnswer: string;
  readonly partialQuarter: number;
  readonly maximumQuarter: number;
  readonly answer: string;
  readonly expectedQuarter: number;
}

export interface WeightedScoringCaseRow {
  readonly id: string;
  readonly answer: string;
  readonly expected: string;
  readonly maximumQuarter: number;
  readonly earnedQuarter: number;
}

export interface ExamInventoryCaseRow {
  readonly id: string;
  readonly section: ExamInventoryCaseRowSection;
  readonly taskRef: string;
  readonly itemCount: number;
  readonly pointsQuarter: number;
  readonly variantCount?: number;
  readonly segmentStartMs?: number;
  readonly segmentEndMs?: number;
}

export interface WritingPromptCaseRow {
  readonly id: string;
  readonly setId: string;
  readonly taskNumber: number;
  readonly wordLimit: number;
  readonly promptImageUrl?: string;
}

export interface QuarterFormatCaseRow {
  readonly id: string;
  readonly pointsQuarter: number;
  readonly expectedText: string;
}

export interface AnswerEvaluationCaseRow {
  readonly id: string;
  readonly consumer: AnswerEvaluationCaseRowConsumer;
  readonly mode: AnswerEvaluationCaseRowMode;
  readonly fullAnswer: string;
  readonly variantAnswer?: string;
  readonly variantQuarter?: number;
  readonly maximumQuarter: number;
  readonly answer: string;
  readonly expectedStatus: AnswerEvaluationCaseRowExpectedStatus;
  readonly expectedConfirmedQuarter: number;
  readonly expectedPendingQuarter: number;
}

export interface AnswerAttemptCaseRow {
  readonly id: string;
  readonly exactAnswer: string;
  readonly openAnswer: string;
  readonly exactResponse: string;
  readonly openResponse: string;
  readonly exactMaximumQuarter: number;
  readonly openMaximumQuarter: number;
  readonly expectedConfirmedQuarter: number;
  readonly expectedPendingQuarter: number;
  readonly expectedPossibleQuarter: number;
  readonly expectedFinal: boolean;
}

export interface GrammarPairCaseRow {
  readonly id: string;
  readonly answer8: string;
  readonly answer9: string;
  readonly status8: GrammarPairCaseRowStatus8;
  readonly status9: GrammarPairCaseRowStatus9;
  readonly confirmed8: number;
  readonly confirmed9: number;
  readonly pending8: number;
  readonly pending9: number;
}

export interface NovemberListeningCaseRow {
  readonly id: string;
  readonly answer: string;
  readonly expectedStatus: NovemberListeningCaseRowExpectedStatus;
  readonly confirmedQuarter: number;
  readonly pendingQuarter: number;
}

export interface GrammarRubricCaseRow {
  readonly id: string;
  readonly itemRef: string;
  readonly componentCount: number;
  readonly maximumQuarter: number;
  readonly unknownAnswer: string;
  readonly unknownPendingQuarter: number;
}

export interface UnorderedSetCaseRow {
  readonly id: string;
  readonly correctAnswer: string;
  readonly options: string;
  readonly answer: string;
  readonly maximumQuarter: number;
  readonly creditPerCorrectQuarter?: number;
  readonly mode?: UnorderedSetCaseRowMode;
  readonly expectedStatus: UnorderedSetCaseRowExpectedStatus;
  readonly expectedConfirmedQuarter: number;
  readonly expectedPendingQuarter?: number;
}

export interface ExamModuleCaseRow {
  readonly id: string;
  readonly examRef: string;
  readonly module: ExamModuleCaseRowModule;
  readonly order: number;
  readonly durationSeconds: number;
  readonly maximumQuarter: number;
}

export interface ReadingSetCaseRow {
  readonly id: string;
  readonly correctAnswer: string;
  readonly options: string;
  readonly answer: string;
  readonly maximumQuarter: number;
  readonly creditPerCorrectQuarter: number;
  readonly expectedConfirmedQuarter: number;
}

export interface OpenItemGroupCaseRow {
  readonly id: string;
  readonly section: OpenItemGroupCaseRowSection;
  readonly groupRef: string;
  readonly expectedItemCount: number;
  readonly unknownAnswer: string;
  readonly expectedUnknownStatus: OpenItemGroupCaseRowExpectedUnknownStatus;
  readonly expectedBlankStatus: OpenItemGroupCaseRowExpectedBlankStatus;
}

export interface GrammarConstraintCaseRow {
  readonly id: string;
  readonly answer6: string;
  readonly answer8: string;
  readonly expected6Quarter: number;
  readonly expected8Quarter: number;
}

export interface BankTokenCaseRow {
  readonly id: string;
  readonly testId: string;
  readonly expectedOrderedTokenText: string;
  readonly expectedDuplicateCount: number;
}

export interface QuestionControlCaseRow {
  readonly id: string;
  readonly sourceItemId: string;
  readonly expectedTag: string;
  readonly expectedKind: QuestionControlCaseRowExpectedKind;
  readonly expectedOptionCount: number;
  readonly expectedRawAnswer: string;
  readonly expectedQuestionTarget?: string;
  readonly expectedQuarter: number;
  readonly expectedStatus: QuestionControlCaseRowExpectedStatus;
}

export const EXAM_PUBLICATION_ROW_ROWS: readonly ExamPublicationRow[] = [
  {
    routeId: "b1-2017",
    examRef: "certyfikat-polski-B1-2017",
    displayTitle: "B1 Certyfikat 2017",
    sourceDir: "exams/pl/certyfikat-polski/B1/2017",
  },
  {
    routeId: "b1-2019",
    examRef: "certyfikat-polski-B1-2019",
    displayTitle: "B1 Certyfikat 2019",
    sourceDir: "exams/pl/certyfikat-polski/B1/2019",
  },
  {
    routeId: "b1-2020",
    examRef: "certyfikat-polski-B1-2020",
    displayTitle: "B1 Certyfikat 2020",
    sourceDir: "exams/pl/certyfikat-polski/B1/2020",
  },
  {
    routeId: "b2-2017",
    examRef: "certyfikat-polski-B2-2017",
    displayTitle: "B2 Certyfikat 2017",
    sourceDir: "exams/pl/certyfikat-polski/B2/2017",
  },
  {
    routeId: "b2-2019",
    examRef: "certyfikat-polski-B2-2019",
    displayTitle: "B2 Certyfikat 2019",
    sourceDir: "exams/pl/certyfikat-polski/B2/2019",
  },
  {
    routeId: "b2-2020",
    examRef: "certyfikat-polski-B2-2020",
    displayTitle: "B2 Certyfikat 2020",
    sourceDir: "exams/pl/certyfikat-polski/B2/2020",
  },
  {
    routeId: "b2-2022-02",
    examRef: "certyfikat-polski-B2-2022-02",
    displayTitle: "B2 Certyfikat 2022-02",
    sourceDir: "exams/pl/certyfikat-polski/B2/2022-02",
  },
  {
    routeId: "b2-2023-02",
    examRef: "certyfikat-polski-B2-2023-02",
    displayTitle: "B2 Certyfikat 2023-02",
    sourceDir: "exams/pl/certyfikat-polski/B2/2023-02",
  },
  {
    routeId: "b2-2023-06",
    examRef: "certyfikat-polski-B2-2023-06",
    displayTitle: "B2 Certyfikat 2023-06",
    sourceDir: "exams/pl/certyfikat-polski/B2/2023-06",
  },
  {
    routeId: "b2-2024-02",
    examRef: "certyfikat-polski-B2-2024-02",
    displayTitle: "B2 Certyfikat 2024-02",
    sourceDir: "exams/pl/certyfikat-polski/B2/2024-02",
  },
  {
    routeId: "b2-2024-06",
    examRef: "certyfikat-polski-B2-2024-06",
    displayTitle: "B2 Certyfikat 2024-06",
    sourceDir: "exams/pl/certyfikat-polski/B2/2024-06",
  },
  {
    routeId: "c1-2022-03",
    examRef: "certyfikat-polski-C1-2022-03",
    displayTitle: "C1 Certyfikat 2022-03",
    sourceDir: "exams/pl/certyfikat-polski/C1/2022-03",
  },
  {
    routeId: "c1-2022-11",
    examRef: "certyfikat-polski-C1-2022-11",
    displayTitle: "C1 Certyfikat 2022-11",
    sourceDir: "exams/pl/certyfikat-polski/C1/2022-11",
  },
  {
    routeId: "c1-2023-04",
    examRef: "certyfikat-polski-C1-2023-04",
    displayTitle: "C1 Certyfikat 2023-04",
    sourceDir: "exams/pl/certyfikat-polski/C1/2023-04",
  },
  {
    routeId: "c1-2023-11",
    examRef: "certyfikat-polski-C1-2023-11",
    displayTitle: "C1 Certyfikat 2023-11",
    sourceDir: "exams/pl/certyfikat-polski/C1/2023-11",
  },
  {
    routeId: "c1-2024-04",
    examRef: "certyfikat-polski-C1-2024-04",
    displayTitle: "C1 Certyfikat 2024-04",
    sourceDir: "exams/pl/certyfikat-polski/C1/2024-04",
  },
];

export const EXAM_ROW_ROWS: readonly ExamRow[] = [
  {
    examId: "certyfikat-polski-B1-2017",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B1",
    year: 2017,
    sourceUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/",
  },
  {
    examId: "certyfikat-polski-B1-2019",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B1",
    year: 2019,
    sourceUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/",
  },
  {
    examId: "certyfikat-polski-B1-2020",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B1",
    year: 2020,
    sourceUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/",
  },
  {
    examId: "certyfikat-polski-B2-2017",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2017,
    sourceUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/",
  },
  {
    examId: "certyfikat-polski-B2-2019",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2019,
    sourceUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/",
  },
  {
    examId: "certyfikat-polski-B2-2020",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2020,
    sourceUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/",
  },
  {
    examId: "certyfikat-polski-B2-2022-02",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2022,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2026/01/2022.02.6-7_B2_Arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-B2-2023-02",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2023,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_02_5_6_B2_arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-B2-2023-06",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2023,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_06_24_25_B2_arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-B2-2024-02",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2024,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/02/4-5.02.2024-B2_arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-B2-2024-06",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "B2",
    year: 2024,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/02/22-23.06.2024-B2_arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-C1-2022-03",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "C1",
    year: 2022,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2026/01/2022.03.26-27_C1_Arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-C1-2022-11",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "C1",
    year: 2022,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2026/01/2022.11.5-6_C1_Arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-C1-2023-04",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "C1",
    year: 2023,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_04_15_16_C1_arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-C1-2023-11",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "C1",
    year: 2023,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_11_18_19_C1_arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
  {
    examId: "certyfikat-polski-C1-2024-04",
    provider: "certyfikat-polski",
    targetLanguage: "pl",
    level: "C1",
    year: 2024,
    sourceUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/02/20-21.04.2024-C1_arkusz_egzaminacyjny.pdf",
    license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego",
  },
];
