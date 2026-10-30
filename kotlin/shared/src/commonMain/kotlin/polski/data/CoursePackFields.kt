package polski.data

import polski.core.RecipeSet
import polski.core.engine.ConstructionTemplate
import polski.core.model.FeatureBundle
import polski.core.model.SkillSpec
import polski.model.Adjective
import polski.model.GramCase
import polski.model.Noun
import polski.model.PossessiveId
import polski.model.Skill
import polski.model.SentenceSeed
import polski.model.Verb
import polski.presentation.Lifehack
import polski.presentation.StemAlternation

/**
 * NJ03 P7: one typed contract for course pack fields.
 * PlRuTypedPack and EnRuTypedPack (generated typed vals) implement this.
 * PackRegistry and all consumers reference this interface — no second authority.
 */
internal interface CoursePackFields {
    val id: String
    val pairId: String
    val targetLanguage: String
    val nativeLanguage: String

    // Lexicon
    val nouns: List<Noun>
    val nounLabels: Map<String, Pair<String, String>>
    val adjectives: List<Adjective>
    val verbs: List<Verb>
    val skills: List<Skill>

    // The four inputs the exercise engine is built from (R1.13): PackEngine takes them from the pack, no file or JSON text
    val skillSpecs: List<SkillSpec>
    val recipes: RecipeSet
    val constructionTemplates: Map<String, ConstructionTemplate>
    val formTable: Map<String, Map<FeatureBundle, String>>

    // Sentence / exercise
    val sentenceSeeds: List<SentenceSeed>
    val stemAlternations: List<StemAlternation>
    val caseSentencePrefixes: Map<String, String>
    val exerciseCopy: Map<String, String>
    val exercisePatterns: Map<String, String>
    val presentations: Map<String, SkillPresentation>
    val styleContent: Map<String, SkillStyleContent>
    val vocabulary: List<VocabularyItem>
    val frequency: List<FrequencyItem>
    val lifehacks: List<Lifehack>

    // Reference (nullable — en-ru doesn't have them yet)
    val chainPresentation: ChainPresentation
    val caseReferenceRows: List<CaseReferenceRow>?
    val referenceGenderNames: Map<String, String>?
    val referenceChainRows: List<ReferenceChainRow>?
    val referenceSystemCards: List<ReferenceSystemCard>?
    val referencePipeline: ReferencePipeline?
    val referenceCaseTeaching: CaseTeaching?
    val referencePronounTeaching: PronounTeaching?
    val referenceVerbTeaching: VerbTeaching?
    val comparisonNounIds: List<String>?
    val referenceRussianSupport: RussianSupport?
    val referenceTenseRows: List<ReferenceTenseRow>?
    val referenceAspectRows: List<ReferenceAspectRow>?
    val maleAccRows: List<MaleAccRow>?

    // Simple content strings
    val matrixIntroduction: String?
    val webCaseCompositionHeader: String?
    val contextHelp: CourseContextHelp?
    val maleAccIntro: String?
    val aspectNoPresent: CourseAspectNoPresent?
    val vocabularyInstructions: CourseVocabularyInstructions
    val vocabularyUnavailableLabel: String

    // Pronouns / morphology
    val personalPronouns: Map<String, Map<GramCase, String>>
    val personalPronounRawForms: Map<String, Map<String, String>>
    val possessives: List<Possessive>
    val possessiveForms: Map<PossessiveId, CoursePossessiveForms>
    val futureAuxiliary: FutureAuxiliary?
}
