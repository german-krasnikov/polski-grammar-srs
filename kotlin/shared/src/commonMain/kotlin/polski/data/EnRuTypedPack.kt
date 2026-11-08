package polski.data

import polski.model.GramCase
import polski.model.PossessiveId
import polski.presentation.StemAlternation

/**
 * NJ03 P7: typed en-ru course pack — implements CoursePackFields directly.
 * en-ru has no Polish-shaped case/gender forms: nouns/adjectives/verbs return emptyList().
 * Reference fields are null/empty (en-ru reference not authored yet).
 */
internal object EnRuTypedPack : CoursePackFields {
    override val id = "en-ru"
    override val pairId = "en-ru"
    override val targetLanguage = "en"
    override val nativeLanguage = "ru"

    // Lexicon (en has no pl-shaped forms — empty typed lists)
    override val nouns get() = generatedEnRuNouns
    override val nounLabels get() = generatedEnRuNounLabels
    override val adjectives get() = generatedEnRuAdjectives
    override val verbs get() = generatedEnRuVerbs
    override val skills get() = generatedEnRuSkills
    override val sentenceSeeds get() = generatedEnRuSentenceSeeds
    override val stemAlternations: List<StemAlternation> get() = emptyList()
    override val possessives get() = generatedEnRuPossessives
    override val possessiveForms get() = emptyMap<PossessiveId, CoursePossessiveForms>()
    override val personalPronouns get() = generatedEnRuPersonalPronouns
    override val personalPronounRawForms get() = generatedEnRuPersonalPronounRawForms

    // The inputs of the exercise engine (R1.15, R1.16)
    override val skillSpecs get() = generatedEnRuSkillSpecs
    override val recipes get() = generatedEnRuRecipes
    override val constructionTemplates get() = generatedEnRuConstructionTemplates
    override val formTable get() = generatedEnRuFormTable

    // Exercise
    override val caseSentencePrefixes get() = generatedEnRuCaseSentencePrefixes
    override val exerciseCopy get() = generatedEnRuExerciseCopy
    override val exercisePatterns get() = generatedEnRuExercisePatterns
    override val presentations get() = generatedEnRuPresentations
    override val styleContent get() = generatedEnRuStyleContent
    override val vocabulary get() = generatedEnRuVocabularyItems
    override val frequency get() = generatedEnRuFrequency
    override val chainPresentation get() = generatedEnRuChainPresentation

    // Reference (en-ru has empty reference)
    override val caseReferenceRows: List<CaseReferenceRow>? get() = null
    override val referenceGenderNames: Map<String, String>? get() = null
    override val referenceChainRows: List<ReferenceChainRow>? get() = null
    override val referenceSystemCards: List<ReferenceSystemCard>? get() = null
    override val referencePipeline: ReferencePipeline? get() = null
    override val referenceTenseRows: List<ReferenceTenseRow>? get() = null
    override val referenceAspectRows: List<ReferenceAspectRow>? get() = null
    override val maleAccRows: List<MaleAccRow>? get() = null
    override val comparisonNounIds: List<String>? get() = null
    override val referenceCaseTeaching: CaseTeaching? get() = null
    override val referenceVerbTeaching: VerbTeaching? get() = null
    override val referencePronounTeaching: PronounTeaching? get() = null
    override val referenceRussianSupport: RussianSupport? get() = null
    override val futureAuxiliary: FutureAuxiliary? get() = null

    // Content strings
    override val matrixIntroduction: String? get() = null
    override val webCaseCompositionHeader: String? get() = null
    override val contextHelp: CourseContextHelp? get() = null
    override val maleAccIntro: String? get() = null
    override val aspectNoPresent: CourseAspectNoPresent? get() = null
    override val vocabularyInstructions get() = generatedEnRuVocabularyInstructions
    override val vocabularyUnavailableLabel get() = generatedEnRuVocabularyUnavailableLabel
    override val lifehacks get() = generatedEnRuLifehacks

    override val uiCopy = mapOf(
        "again" to "Повторить", "good" to "Вспомнил",
        "progressTab" to "Прогресс", "settingsTab" to "Настройки", "lifehacks" to "Лайфхаки",
        "showAnswer" to "Показать ответ", "checkAndShowAnswer" to "Проверить и показать ответ",
        "exportJson" to "Экспорт JSON", "resetProgress" to "Сбросить прогресс",
        "hideTable" to "Скрыть таблицу", "tableAtHand" to "Таблица под рукой",
        "allTablesAndSchema" to "Все таблицы и схема", "whenToReview" to "Когда повторить?",
        "noDueTitle" to "No reviews due.", "compareAnswer" to "Compare your answer with the model",
        "backSideReference" to "Back side · model answer", "noAnswerEntered" to "No answer entered",
        "matches" to "Matches", "differs" to "Differs",
        "selectExam" to "Select exam", "start" to "Start", "next" to "Next →",
        "finish" to "Finish", "tryAgain" to "Try again", "task" to "Task",
        "grammar" to "Grammar", "listening" to "Listening", "reading" to "Reading",
        "writing" to "Writing", "results" to "Results", "comingSoon" to "Coming soon", "exam" to "Exam",
        "cardsTab" to "Cards", "vocabularyTab" to "Vocabulary", "matrixTab" to "Tables & scheme",
        "lang.pl" to "Польский", "lang.en" to "Английский", "lang.ru" to "Русский",
        "adverb.pl" to "по-польски", "adverb.en" to "по-английски", "adverb.ru" to "по-русски",
        "tense.present" to "Present", "tense.past" to "Past", "tense.future" to "Future",
    )
}
