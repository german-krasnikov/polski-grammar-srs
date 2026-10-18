package polski.data

import polski.model.GramCase
import polski.model.Gender
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.Tense
import polski.pack.CoursePackSource

/**
 * NJ03: typed pl-ru CoursePack — all fields read from generated Biome literal sources.
 * Eliminates all `if (packRegistry.active.id == "pl-ru") generatedPlRu*` checks scattered
 * across accessor files. One source → one override point (P7).
 *
 * pair.json is still passed to super() for parsesCompletely() compatibility check; production
 * code reads only from typed generated vals here.
 */
internal class PlRuTypedPack(source: CoursePackSource) : CoursePack(source) {
    override val id = "pl-ru"
    override val pairId = "pl-ru"
    override val targetLanguage = "pl"
    override val nativeLanguage = "ru"

    // Lexical
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

    // Exercise
    override val caseSentencePrefixes get() = generatedPlRuCaseSentencePrefixes
    override val exerciseCopy get() = generatedPlRuExerciseCopy
    override val exercisePatterns get() = generatedPlRuExercisePatterns
    override val presentations get() = generatedPlRuPresentations
    override val styleContent get() = generatedPlRuStyleContent
    override val vocabulary get() = generatedPlRuVocabularyItems

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

    // Teaching / reference complex
    override val referenceCaseTeaching get() = generatedPlRuCaseTeaching
    override val referenceVerbTeaching get() = generatedPlRuVerbTeaching
    override val referencePronounTeaching get() = generatedPlRuPronounTeaching
    override val referenceRussianSupport get() = generatedPlRuRussianSupport
    override val frequency get() = generatedPlRuFrequency
    override val futureAuxiliary get() = generatedPlRuFutureAuxiliary

    // Simple strings
    override val matrixIntroduction get() = generatedPlRuMatrixIntroduction
    override val webCaseCompositionHeader get() = generatedPlRuWebCaseCompositionHeader
    override val maleAccIntro get() = generatedPlRuMaleAccIntro
    override val contextHelp get() = generatedPlRuContextHelp
    override val aspectNoPresent get() = generatedPlRuAspectNoPresent
    override val vocabularyInstructions get() = generatedPlRuVocabularyInstructions
    override val vocabularyUnavailableLabel get() = generatedPlRuVocabularyUnavailableLabel
}
