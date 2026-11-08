package polski.data

import polski.model.GramCase
import polski.model.PossessiveId

/**
 * NJ03 P7: typed pl-ru course pack — implements CoursePackFields directly from
 * generated Biome literal sources. No JSON parsing.
 * One source → one typed contract → one implementation.
 */
internal object PlRuTypedPack : CoursePackFields {
    override val id = "pl-ru"
    override val pairId = "pl-ru"
    override val targetLanguage = "pl"
    override val nativeLanguage = "ru"

    // Lexicon
    override val skills get() = generatedPlRuSkills
    override val nouns get() = generatedPlRuNouns
    override val nounLabels by lazy { generatedPlRuNouns.associate { it.id to (it.lemma to it.meaning) } }
    override val adjectives get() = generatedPlRuAdjectives
    override val verbs get() = generatedPlRuVerbs
    override val sentenceSeeds get() = generatedPlRuSentenceSeeds
    override val stemAlternations get() = generatedPlRuStemAlternations
    override val possessives get() = generatedPlRuPossessives
    override val possessiveForms get() = generatedPlRuPossessiveForms
    override val personalPronouns get() = generatedPlRuPersonalPronouns
    override val personalPronounRawForms get() = generatedPlRuPersonalPronounRawForms

    // The inputs of the exercise engine
    override val skillSpecs get() = generatedPlRuSkillSpecs
    override val recipes get() = generatedPlRuRecipes
    override val constructionTemplates get() = generatedPlRuConstructionTemplates
    override val formTable get() = generatedPlRuFormTable

    // Exercise
    override val caseSentencePrefixes get() = generatedPlRuCaseSentencePrefixes
    override val exerciseCopy get() = generatedPlRuExerciseCopy
    override val exercisePatterns get() = generatedPlRuExercisePatterns
    override val presentations get() = generatedPlRuPresentations
    override val styleContent get() = generatedPlRuStyleContent
    override val vocabulary get() = generatedPlRuVocabularyItems
    override val frequency get() = generatedPlRuFrequency

    // Reference
    override val chainPresentation get() = generatedPlRuChainPresentation
    override val caseReferenceRows get() = generatedPlRuCaseReferenceRows
    override val referenceGenderNames get() = generatedPlRuReferenceGenderNames
    override val referenceChainRows get() = generatedPlRuReferenceChainRows
    override val referenceSystemCards get() = generatedPlRuReferenceSystemCards
    override val referencePipeline get() = generatedPlRuReferencePipeline
    override val referenceTenseRows get() = generatedPlRuReferenceTenseRows
    override val referenceAspectRows get() = generatedPlRuReferenceAspectRows
    override val maleAccRows get() = generatedPlRuMaleAccRows
    override val comparisonNounIds get() = generatedPlRuComparisonNounIds
    override val referenceCaseTeaching get() = generatedPlRuCaseTeaching
    override val referenceVerbTeaching get() = generatedPlRuVerbTeaching
    override val referencePronounTeaching get() = generatedPlRuPronounTeaching
    override val referenceRussianSupport get() = generatedPlRuRussianSupport
    override val futureAuxiliary get() = generatedPlRuFutureAuxiliary

    // Simple strings
    override val matrixIntroduction get() = generatedPlRuMatrixIntroduction
    override val webCaseCompositionHeader get() = generatedPlRuWebCaseCompositionHeader
    override val maleAccIntro get() = generatedPlRuMaleAccIntro
    override val contextHelp get() = generatedPlRuContextHelp
    override val aspectNoPresent get() = generatedPlRuAspectNoPresent
    override val vocabularyInstructions get() = generatedPlRuVocabularyInstructions
    override val vocabularyUnavailableLabel get() = generatedPlRuVocabularyUnavailableLabel
    override val lifehacks get() = generatedPlRuLifehacks

    override val uiCopy = mapOf(
        "again" to "Повторить", "good" to "Вспомнил",
        "progressTab" to "Прогресс", "settingsTab" to "Настройки", "lifehacks" to "Лайфхаки",
        "showAnswer" to "Показать ответ", "checkAndShowAnswer" to "Проверить и показать ответ",
        "exportJson" to "Экспорт JSON", "resetProgress" to "Сбросить прогресс",
        "hideTable" to "Скрыть таблицу", "tableAtHand" to "Таблица под рукой",
        "allTablesAndSchema" to "Все таблицы и схема", "whenToReview" to "Когда повторить?",
        "noDueTitle" to "Новых повторений пока нет.", "compareAnswer" to "Сравни свой ответ с эталоном",
        "backSideReference" to "Обратная сторона · эталон", "noAnswerEntered" to "Ответ не введён",
        "matches" to "Совпадает с правильным вариантом", "differs" to "Отличается",
        "selectExam" to "Выберите экзамен", "start" to "Начать", "next" to "Далее →",
        "finish" to "Завершить", "tryAgain" to "Пройти ещё раз", "task" to "Задание",
        "grammar" to "Грамматика", "listening" to "Аудирование", "reading" to "Чтение",
        "writing" to "Письмо", "selectWritingSet" to "Выберите набор", "changeWritingSet" to "Сменить набор", "writingSet" to "Набор",
        "results" to "Результаты", "comingSoon" to "Скоро", "exam" to "Экзамен",
        "examConfirmedPoints" to "Подтверждено", "examPendingPoints" to "Ожидает проверки",
        "examManualReview" to "Требуется ручная проверка", "examOfficialExamples" to "Образцы из ключа",
        "cardsTab" to "Карточки", "vocabularyTab" to "Слова", "matrixTab" to "Таблицы и схема",
        "lang.pl" to "Польский", "lang.en" to "Английский", "lang.ru" to "Русский",
        "adverb.pl" to "по-польски", "adverb.en" to "по-английски", "adverb.ru" to "по-русски",
        "tense.present" to "Настоящее", "tense.past" to "Прошедшее", "tense.future" to "Будущее",
    )
}
