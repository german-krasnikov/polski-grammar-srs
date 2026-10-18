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
}
