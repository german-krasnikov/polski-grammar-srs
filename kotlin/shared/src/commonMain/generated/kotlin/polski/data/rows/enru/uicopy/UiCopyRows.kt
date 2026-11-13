// Generated from dsl/course.data-contract, courses-biome/en-ru/uiCopy.core-data
// source-sha256=8d297a43f02dd884a8988b7e3cf6d9b2864cf15bf767faa2680ef50be58bff1f
// Edit the source, then run biome generate.

package polski.data.rows.enru.uicopy

enum class SkillRowLevel(val tag: String) {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
}

enum class VerbRowAspect(val tag: String) {
    IMPERFECTIVE("imperfective"),
    PERFECTIVE("perfective"),
}

enum class VerbRowFutureType(val tag: String) {
    COMPOUND("compound"),
    PRESENT("present"),
}

enum class PossessiveFormsRowKind(val tag: String) {
    DECLINED("declined"),
    INVARIANT("invariant"),
}

enum class VerbSubjectRowGenderMode(val tag: String) {
    SELECTED("selected"),
    FIXED("fixed"),
}

enum class TextValueRowKind(val tag: String) {
    COPY("copy"),
    LITERAL("literal"),
    PHRASE("phrase"),
    PRONOUN("pronoun"),
}

enum class TextSpecRowKind(val tag: String) {
    DIRECT("direct"),
    PATTERN("pattern"),
    CONCAT("concat"),
    PREFIXED("prefixed"),
}

enum class ReasonSpecRowKind(val tag: String) {
    FIXED("fixed"),
    CLASSIFIED("classified"),
}

enum class LifehackRowStatus(val tag: String) {
    EDITORIAL("editorial"),
    COMMUNITY("community"),
}

enum class LifehackSourceRowKind(val tag: String) {
    RESEARCH("research"),
    TEACHING_PRACTICE("teaching_practice"),
    PROJECT_AUTHORED("project_authored"),
}

data class FocusRow(
    val before: String,
    val after: String,
)

data class MethodRow(
    val introduction: String,
    val promptLead: String,
    val introduce: String,
    val retrieve: String,
    val feedback: String,
    val review: String,
)

data class MethodsRow(
    val logic: MethodRow,
    val situations: MethodRow,
)

data class FeatureFocusRow(
    val feature: String,
    val from: String,
    val to: String,
)

data class FeatureValueRow(
    val feature: String,
    val value: String,
)

data class WhereRow(
    val nounId: List<String>? = null,
    val enNounId: List<String>? = null,
    val gender: List<String>? = null,
)

data class LexicalFilterRow(
    val slot: String,
    val where: WhereRow,
)

data class NativeParallelRow(
    val native: String,
    val target: String,
    val note: String,
    val matches: Boolean,
)

data class StyleContentRow(
    val rule: String? = null,
    val nativeParallel: List<NativeParallelRow>? = null,
    val examples: List<String>? = null,
    val why: String? = null,
)

data class SkillRow(
    val id: String,
    val title: String,
    val group: String,
    val level: SkillRowLevel,
    val formula: String,
    val theory: String,
    val hint: String,
    val prerequisites: List<String>,
    val focus: FocusRow,
    val methods: MethodsRow,
    val construction: String,
    val featureFocus: FeatureFocusRow?,
    val fixed: List<FeatureValueRow>,
    val lexicalFilter: LexicalFilterRow?,
    val styleContent: StyleContentRow? = null,
)

data class CaseFormsRow(
    val nom: String,
    val gen: String,
    val dat: String,
    val acc: String,
    val inst: String,
    val loc: String,
    val voc: String,
)

data class NounFormsRow(
    val sg: CaseFormsRow,
    val pl: CaseFormsRow,
)

data class NounRow(
    val id: String,
    val lemma: String,
    val meaning: String,
    val gender: String,
    val forms: NounFormsRow,
)

data class GenderFormsRow(
    val m_personal: CaseFormsRow,
    val m_animate: CaseFormsRow,
    val m_inanimate: CaseFormsRow,
    val f: CaseFormsRow,
    val n: CaseFormsRow,
)

data class AdjectiveFormsRow(
    val sg: GenderFormsRow,
    val pl: GenderFormsRow,
)

data class AdjectiveRow(
    val id: String,
    val lemma: String,
    val meaning: String,
    val forms: AdjectiveFormsRow,
)

data class PersonFormsRow(
    val p1: String,
    val p2: String,
    val p3: String,
)

data class PresentRow(
    val sg: PersonFormsRow,
    val pl: PersonFormsRow,
)

data class PastStemRow(
    val m: String,
    val f: String,
    val n: String,
    val mp: String,
    val np: String,
)

data class VerbRow(
    val id: String,
    val lemma: String,
    val meaning: String,
    val aspect: VerbRowAspect,
    val futureType: VerbRowFutureType,
    val perfectivePair: String? = null,
    val present: PresentRow,
    val pastStem: PastStemRow,
)

data class PronounRow(
    val id: String,
    val forms: CaseFormsRow,
)

data class PossessivePluralRow(
    val m_personal: CaseFormsRow,
    val other: CaseFormsRow,
)

data class PossessiveFormsRow(
    val kind: PossessiveFormsRowKind,
    val value: String? = null,
    val sg: GenderFormsRow? = null,
    val pl: PossessivePluralRow? = null,
)

data class PossessiveRow(
    val id: String,
    val label: String,
    val forms: PossessiveFormsRow,
)

data class SeedRow(
    val nounId: String,
    val adjectiveId: String,
)

data class CopyEntryRow(
    val key: String,
    val value: String,
)

data class UiCopyRow(
    val key: String,
    val text: String,
)

data class UiCopyCaseRow(
    val key: String,
    val plRu: String,
    val enRu: String,
    val placeholders: String,
)

data class PatternEntryRow(
    val key: String,
    val value: String,
)

data class PrefixEntryRow(
    val key: String,
    val value: String,
)

data class VocabularyItemRow(
    val id: String,
    val lemma: String,
    val translation: String,
    val form: String,
    val example: String,
    val level: String,
    val frequencyRank: Int?,
)

data class VocabularyInstructionsRow(
    val react: String,
    val web: String,
    val native: String,
    val ios: String,
)

data class VocabularyRow(
    val source: String,
    val items: List<VocabularyItemRow>,
    val instructions: VocabularyInstructionsRow,
    val unavailableLabel: String,
)

data class ChainStepRow(
    val id: String,
    val label: String,
)

data class ChainCompletionRow(
    val title: String,
    val reactEyebrow: String,
    val reactBody: String,
    val webBody: String,
)

data class ChainPresentationRow(
    val steps: List<ChainStepRow>,
    val completion: ChainCompletionRow,
)

data class TrainingRow(
    val chainPresentation: ChainPresentationRow,
)

data class StemAlternationRow(
    val a: String,
    val b: String,
)

data class ReactCompactRow(
    val react: String,
    val compact: String,
)

data class AspectNoPresentRow(
    val compact: String,
    val ios: String,
)

data class GenderNamesRow(
    val m_personal: String,
    val m_animate: String,
    val m_inanimate: String,
    val f: String,
    val n: String,
)

data class RefCaseRow(
    val id: String,
    val pl: String,
    val ru: String,
    val question: String,
    val trigger: String,
    val skill: String? = null,
)

data class RefChainRow(
    val label: String,
    val from: String,
    val to: String,
    val change: String,
)

data class RefTenseRow(
    val label: String,
    val from: String,
    val to: String,
)

data class RefAspectRow(
    val label: String,
    val from: String,
    val present: String?,
    val past: String,
    val future: String,
)

data class RefMaleAccExampleRow(
    val from: String,
    val to: String,
    val sentence: String,
)

data class RefMaleAccRow(
    val id: String,
    val label: String,
    val title: String,
    val examples: List<RefMaleAccExampleRow>,
    val rule: String,
)

data class RefSystemCardRow(
    val id: String,
    val title: String,
    val explanation: String,
    val example: String,
    val steps: List<String>,
)

data class RefPipelineStepRow(
    val id: String,
    val label: String,
    val question: String,
    val example: String,
)

data class RefPipelineRow(
    val title: String,
    val steps: List<RefPipelineStepRow>,
    val compactExample: String,
)

data class CaseTeachingRow(
    val caseNote: ReactCompactRow,
    val comparisonReadingHint: String,
)

data class FullCompactRow(
    val full: String,
    val compact: String,
)

data class VerbSubjectRow(
    val id: String,
    val person: Int,
    val number: String,
    val genderMode: VerbSubjectRowGenderMode,
    val fixedGender: String? = null,
    val label: FullCompactRow,
)

data class VerbGenderOptionRow(
    val id: String,
    val label: FullCompactRow,
)

data class TenseLabelsRow(
    val present: FullCompactRow,
    val past: FullCompactRow,
    val future: FullCompactRow,
)

data class VerbTeachingRow(
    val subjects: List<VerbSubjectRow>,
    val genderControlLabel: FullCompactRow,
    val genderOptions: List<VerbGenderOptionRow>,
    val tenseLabels: TenseLabelsRow,
    val futureExplanation: ReactCompactRow,
)

data class FooterRow(
    val react: String,
    val web: String,
    val native: String,
)

data class PersonalTeachingRow(
    val title: String,
    val intro: ReactCompactRow,
    val footer: FooterRow,
    val pronounIds: List<String>,
)

data class CueRow(
    val full: String,
    val compact: String,
    val ios: String,
)

data class SpecialValueRow(
    val pronounId: String,
    val value: String,
)

data class PronounContextRow(
    val id: String,
    val caseId: String,
    val cue: CueRow,
    val caseName: String,
    val valuePrefix: String? = null,
    val specialValues: List<SpecialValueRow>? = null,
)

data class CaptionRow(
    val web: String,
    val desktop: String,
    val ios: String,
)

data class DemoCaseRow(
    val id: String,
    val caption: CaptionRow,
)

data class DemoRuleRow(
    val invariable: String,
    val variable: String,
)

data class PossessiveDemoRow(
    val nounId: String,
    val adjectiveId: String,
    val number: String,
    val cases: List<DemoCaseRow>,
    val invariableOwnerIds: List<String>,
    val rule: DemoRuleRow,
)

data class PossessiveTeachingRow(
    val title: String,
    val demo: PossessiveDemoRow,
)

data class PronounTeachingRow(
    val personal: PersonalTeachingRow,
    val contexts: List<PronounContextRow>,
    val possessive: PossessiveTeachingRow,
)

data class ConstructionCheckRow(
    val construction: String,
    val check: String,
)

data class EndingPartRow(
    val text: String,
    val isChanged: Boolean,
    val isEnding: Boolean,
)

data class ContrastRow(
    val from: String,
    val to: String,
    val beforeParts: List<EndingPartRow>,
    val afterParts: List<EndingPartRow>,
)

data class SupportRow(
    val id: String,
    val cue: String,
    val react: ConstructionCheckRow,
    val web: ConstructionCheckRow,
    val desktop: ConstructionCheckRow,
    val mobileLine: String,
    val comparisons: List<ContrastRow>,
)

data class RussianSupportSectionRow(
    val title: FullCompactRow,
    val columns: List<String>,
    val rows: List<SupportRow>,
)

data class ReferenceRow(
    val matrixIntroduction: String,
    val webCaseCompositionHeader: String,
    val maleAccIntro: String,
    val comparisonNounIds: List<String>,
    val contextHelp: ReactCompactRow,
    val aspectNoPresent: AspectNoPresentRow,
    val genderNames: GenderNamesRow,
    val caseRows: List<RefCaseRow>,
    val chainRows: List<RefChainRow>,
    val tenseRows: List<RefTenseRow>,
    val aspectRows: List<RefAspectRow>,
    val maleAccRows: List<RefMaleAccRow>,
    val systemCards: List<RefSystemCardRow>,
    val pipeline: RefPipelineRow,
    val caseTeaching: CaseTeachingRow,
    val verbTeaching: VerbTeachingRow,
    val pronounTeaching: PronounTeachingRow,
    val russianSupport: RussianSupportSectionRow,
)

data class FutureAuxiliaryRow(
    val verbId: String,
    val forms: PresentRow,
)

data class MorphologyRow(
    val futureAuxiliary: FutureAuxiliaryRow,
)

data class PhraseSpecRow(
    val construction: String,
    val bundle: List<FeatureValueRow>,
    val ownerLexeme: String? = null,
)

data class GenderPronounRow(
    val gender: String,
    val pronounId: String,
)

data class TextValueRow(
    val kind: TextValueRowKind,
    val capitalized: Boolean? = null,
    val key: String? = null,
    val value: String? = null,
    val spec: PhraseSpecRow? = null,
    val genderToKey: List<GenderPronounRow>? = null,
    val caseId: String? = null,
)

data class PatternValueRow(
    val name: String,
    val value: TextValueRow,
)

data class TextSpecRow(
    val kind: TextSpecRowKind,
    val value: TextValueRow? = null,
    val key: String? = null,
    val values: List<PatternValueRow>? = null,
    val parts: List<TextValueRow>? = null,
    val spec: PhraseSpecRow? = null,
    val prefixCase: String? = null,
    val punct: String? = null,
)

data class ReasonRuleRow(
    val whenUnchanged: Boolean? = null,
    val whenGender: String? = null,
    val whenNomSingularSuffix: String? = null,
    val key: String,
)

data class ReasonSpecRow(
    val kind: ReasonSpecRowKind,
    val key: String? = null,
    val rules: List<ReasonRuleRow>? = null,
)

data class ChangeSpecRow(
    val from: TextValueRow,
    val to: TextValueRow,
    val reason: ReasonSpecRow,
)

data class OwnerChoiceRow(
    val lexeme: String,
    val labelKey: String,
)

data class OwnerDrawRow(
    val owners: List<OwnerChoiceRow>,
    val promptPrefixKey: String,
    val promptSuffixKey: String,
)

data class StaticExerciseRow(
    val sourceKey: String,
    val promptKey: String,
    val expectedKey: String,
    val explanationKey: String,
    val acceptedKeys: List<String>,
    val changeFromKey: String,
    val changeToKey: String,
    val changeReasonKey: String,
    val nounId: String,
    val adjectiveId: String,
    val tags: List<String>,
)

data class SkillRecipeRow(
    val skill: String,
    val source: TextSpecRow? = null,
    val expected: TextSpecRow? = null,
    val promptKey: String? = null,
    val explanationKey: String? = null,
    val changes: List<ChangeSpecRow>? = null,
    val accepted: List<TextSpecRow>? = null,
    val tags: List<String>? = null,
    val includeNounGenderTag: Boolean? = null,
    val ownerDraw: OwnerDrawRow? = null,
    val numberOverride: String? = null,
    val staticOverride: StaticExerciseRow? = null,
)

data class ChainStepRecipeRow(
    val skill: String? = null,
    val source: TextSpecRow,
    val expected: TextSpecRow,
    val promptKey: String,
    val explanationKey: String,
    val changes: List<ChangeSpecRow>,
    val tags: List<String>,
    val ownerOut: String,
)

data class GenderSkillRow(
    val gender: String,
    val skill: String,
)

data class ConstantSlotRow(
    val slot: String,
    val lexeme: String,
    val whenFeature: String? = null,
    val whenValues: List<String>? = null,
)

data class RecipeDefaultsRow(
    val defaultOwnerLexeme: String,
    val verbLexeme: String,
    val firstSkillByGender: List<GenderSkillRow>,
    val constantSlots: List<ConstantSlotRow>? = null,
)

data class ConditionRow(
    val feature: String,
    val value: String,
)

data class RequiredWhenRow(
    val condition: ConditionRow,
    val features: List<String>,
)

data class SlotRow(
    val name: String,
    val category: String,
    val optional: Boolean? = null,
    val requiredFeatures: List<String>,
    val requiredFeaturesWhen: List<RequiredWhenRow>? = null,
    val agreementSource: String? = null,
)

data class GovWhenRow(
    val condition: ConditionRow,
    val result: String,
)

data class OrderWhenRow(
    val condition: ConditionRow,
    val slots: List<SlotRow>,
)

data class ConstructionRow(
    val id: String,
    val slots: List<SlotRow>,
    val govFeature: String? = null,
    val govDefault: String? = null,
    val govWhen: List<GovWhenRow>? = null,
    val orderWhen: List<OrderWhenRow>? = null,
)

data class EnNounFormsRow(
    val sg: String,
    val pl: String,
)

data class EnNounRow(
    val id: String,
    val lemma: String,
    val meaning: String,
    val forms: EnNounFormsRow,
)

data class EnAdjectiveFormsRow(
    val invariant: String,
)

data class EnAdjectiveRow(
    val id: String,
    val lemma: String,
    val meaning: String,
    val forms: EnAdjectiveFormsRow,
)

data class EnVerbRow(
    val id: String,
    val lemma: String,
    val meaning: String,
    val present3sg: String,
    val presentSg1: String? = null,
    val presentPl: String? = null,
    val past: String? = null,
    val pastPl: String? = null,
    val pastParticiple: String? = null,
    val ing: String? = null,
)

data class EnPronounFormsRow(
    val Subj: String,
    val Obj: String,
)

data class EnPronounRow(
    val id: String,
    val forms: EnPronounFormsRow,
)

data class RoleFormRow(
    val role: String,
    val form: String,
)

data class EnPrepositionRow(
    val id: String,
    val forms: List<RoleFormRow>,
)

data class EnSeedRow(
    val nounId: String,
    val adjectiveId: String,
)

data class LifehackSourceRow(
    val kind: LifehackSourceRowKind,
    val citation: String,
    val url: String? = null,
)

data class LifehackRow(
    val id: String,
    val skillId: String? = null,
    val topic: String? = null,
    val text: String,
    val source: LifehackSourceRow,
    val status: LifehackRowStatus,
)

val UI_COPY_ROW_ROWS: List<UiCopyRow> = listOf(
    UiCopyRow(key = "again", text = "Повторить"),
    UiCopyRow(key = "good", text = "Вспомнил"),
    UiCopyRow(key = "progressTab", text = "Прогресс"),
    UiCopyRow(key = "settingsTab", text = "Настройки"),
    UiCopyRow(key = "lifehacks", text = "Лайфхаки"),
    UiCopyRow(key = "showAnswer", text = "Показать ответ"),
    UiCopyRow(key = "checkAndShowAnswer", text = "Проверить и показать ответ"),
    UiCopyRow(key = "exportJson", text = "Экспорт JSON"),
    UiCopyRow(key = "resetProgress", text = "Сбросить прогресс"),
    UiCopyRow(key = "hideTable", text = "Скрыть таблицу"),
    UiCopyRow(key = "tableAtHand", text = "Таблица под рукой"),
    UiCopyRow(key = "allTablesAndSchema", text = "Все таблицы и схема"),
    UiCopyRow(key = "whenToReview", text = "Когда повторить?"),
    UiCopyRow(key = "noDueTitle", text = "No reviews due."),
    UiCopyRow(key = "compareAnswer", text = "Compare your answer with the model"),
    UiCopyRow(key = "backSideReference", text = "Back side · model answer"),
    UiCopyRow(key = "noAnswerEntered", text = "No answer entered"),
    UiCopyRow(key = "matches", text = "Matches"),
    UiCopyRow(key = "differs", text = "Differs"),
    UiCopyRow(key = "selectExam", text = "Select exam"),
    UiCopyRow(key = "start", text = "Start"),
    UiCopyRow(key = "next", text = "Next →"),
    UiCopyRow(key = "finish", text = "Finish"),
    UiCopyRow(key = "tryAgain", text = "Try again"),
    UiCopyRow(key = "task", text = "Task"),
    UiCopyRow(key = "grammar", text = "Grammar"),
    UiCopyRow(key = "listening", text = "Listening"),
    UiCopyRow(key = "reading", text = "Reading"),
    UiCopyRow(key = "writing", text = "Writing"),
    UiCopyRow(key = "writingInstructionsAndCriteria", text = "Instructions and criteria"),
    UiCopyRow(key = "selectWritingSet", text = "Select a set"),
    UiCopyRow(key = "changeWritingSet", text = "Change set"),
    UiCopyRow(key = "writingSet", text = "Set"),
    UiCopyRow(key = "results", text = "Results"),
    UiCopyRow(key = "comingSoon", text = "Coming soon"),
    UiCopyRow(key = "exam", text = "Exam"),
    UiCopyRow(key = "examConfirmedPoints", text = "Confirmed"),
    UiCopyRow(key = "examPendingPoints", text = "Awaiting review"),
    UiCopyRow(key = "examManualReview", text = "Needs manual review"),
    UiCopyRow(key = "examOfficialExamples", text = "Examples from the key"),
    UiCopyRow(key = "cardsTab", text = "Cards"),
    UiCopyRow(key = "vocabularyTab", text = "Vocabulary"),
    UiCopyRow(key = "matrixTab", text = "Tables & scheme"),
    UiCopyRow(key = "lang.pl", text = "Польский"),
    UiCopyRow(key = "lang.en", text = "Английский"),
    UiCopyRow(key = "lang.ru", text = "Русский"),
    UiCopyRow(key = "adverb.pl", text = "по-польски"),
    UiCopyRow(key = "adverb.en", text = "по-английски"),
    UiCopyRow(key = "adverb.ru", text = "по-русски"),
    UiCopyRow(key = "tense.present", text = "Present"),
    UiCopyRow(key = "tense.past", text = "Past"),
    UiCopyRow(key = "tense.future", text = "Future"),
    UiCopyRow(key = "trainingTab", text = "Training"),
    UiCopyRow(key = "matrixNavTitle", text = "Matrix"),
    UiCopyRow(key = "resetTitle", text = "Reset all progress?"),
    UiCopyRow(key = "resetConfirm", text = "Reset"),
    UiCopyRow(key = "cancel", text = "Cancel"),
    UiCopyRow(key = "resetWarning", text = "All ratings and review schedules will be deleted."),
    UiCopyRow(key = "noticeTitle", text = "Notice"),
    UiCopyRow(key = "ok", text = "OK"),
    UiCopyRow(key = "recoverySection", text = "Recovery"),
    UiCopyRow(key = "recoveryAdvice", text = "Export the original settings before importing a verified copy."),
    UiCopyRow(key = "courseSection", text = "Course"),
    UiCopyRow(key = "targetLanguage", text = "Target language"),
    UiCopyRow(key = "nativeLanguage", text = "Native language"),
    UiCopyRow(key = "learningSection", text = "Learning"),
    UiCopyRow(key = "explanationStyle", text = "Explanation style"),
    UiCopyRow(
        key = "nativeContrastFallback",
        text = "This skill has no native-language comparison yet; the card uses the rule-first style.",
    ),
    UiCopyRow(key = "appearanceSection", text = "Appearance"),
    UiCopyRow(key = "answerModeTitle", text = "Answer"),
    UiCopyRow(key = "oralMode", text = "Speak aloud"),
    UiCopyRow(key = "oralOrSilentMode", text = "Aloud / silently"),
    UiCopyRow(key = "typedMode", text = "Type"),
    UiCopyRow(key = "themeTitle", text = "Theme"),
    UiCopyRow(key = "themeSystem", text = "System"),
    UiCopyRow(key = "themeLight", text = "Light"),
    UiCopyRow(key = "themeDark", text = "Dark"),
    UiCopyRow(key = "motionTitle", text = "Motion"),
    UiCopyRow(key = "motionSystem", text = "System"),
    UiCopyRow(key = "motionReduced", text = "Reduced"),
    UiCopyRow(key = "motionReducedStatus", text = "System reduces motion"),
    UiCopyRow(key = "motionActiveStatus", text = "System motion is active"),
    UiCopyRow(key = "animationsTitle", text = "Animations"),
    UiCopyRow(key = "settingsDataSection", text = "Settings data"),
    UiCopyRow(key = "exportSettingsJson", text = "Export settings JSON"),
    UiCopyRow(key = "importSettingsJson", text = "Import settings JSON"),
    UiCopyRow(key = "done", text = "Done"),
    UiCopyRow(key = "heroSubtitle", text = "Speak in complete sentences"),
    UiCopyRow(key = "dueStat", text = "Due"),
    UiCopyRow(key = "todayStat", text = "Today"),
    UiCopyRow(key = "loading", text = "Loading…"),
    UiCopyRow(key = "needsProgressCopy", text = "A progress copy is needed"),
    UiCopyRow(key = "openProgressForTransfer", text = "Open Progress to import or export JSON."),
    UiCopyRow(key = "modeTemplate", text = "Mode: {mode}"),
    UiCopyRow(key = "sentenceChain", text = "Sentence chain"),
    UiCopyRow(key = "scheduledReviews", text = "Scheduled reviews"),
    UiCopyRow(key = "singleSkill", text = "Single skill"),
    UiCopyRow(key = "skillTitle", text = "Skill"),
    UiCopyRow(key = "nextWordSet", text = "Next word set"),
    UiCopyRow(key = "backToReviews", text = "Back to reviews"),
    UiCopyRow(key = "reviewsDone", text = "Reviews are done for now"),
    UiCopyRow(key = "practiceChain", text = "Practice a chain"),
    UiCopyRow(key = "sourceSentence", text = "Source sentence"),
    UiCopyRow(key = "skillIntroduction", text = "Meet the skill"),
    UiCopyRow(key = "goToTask", text = "Go to task"),
    UiCopyRow(key = "modelAnswer", text = "Model answer"),
    UiCopyRow(key = "alsoAcceptedTemplate", text = "Also accepted: {answers}"),
    UiCopyRow(key = "swipeHint", text = "Swipe left to repeat · right if remembered"),
    UiCopyRow(key = "polishAnswer", text = "Answer in Polish"),
    UiCopyRow(key = "oralInstruction", text = "Say the whole sentence, then show the answer."),
    UiCopyRow(key = "checkAnswer", text = "Check answer"),
    UiCopyRow(key = "before", text = "Before"),
    UiCopyRow(key = "after", text = "After"),
    UiCopyRow(key = "matrixMap", text = "System map"),
    UiCopyRow(key = "casesAndEndings", text = "Cases and endings"),
    UiCopyRow(key = "tensesAndPersons", text = "Tenses and persons"),
    UiCopyRow(key = "pronouns", text = "Pronouns"),
    UiCopyRow(key = "summary", text = "Summary"),
    UiCopyRow(key = "skills", text = "Skills"),
    UiCopyRow(key = "dataSection", text = "Data"),
    UiCopyRow(key = "reviewsCountTemplate", text = "Reviews: {reviews} · Correct: {correct} · Streak: {streak}"),
    UiCopyRow(key = "nextDueTemplate", text = "Next: {date}"),
    UiCopyRow(key = "noLifehacks", text = "No lifehacks for this set yet."),
    UiCopyRow(key = "vocabularyData", text = "Vocabulary data"),
    UiCopyRow(key = "importVocabularyJson", text = "Import vocabulary JSON"),
    UiCopyRow(key = "exportVocabularyJson", text = "Export vocabulary JSON"),
    UiCopyRow(key = "statusSection", text = "Status"),
    UiCopyRow(key = "directionSection", text = "Direction"),
    UiCopyRow(key = "cardSection", text = "Card"),
    UiCopyRow(key = "selectWords", text = "Select words to study"),
    UiCopyRow(key = "allReviewed", text = "All reviews are done for now"),
    UiCopyRow(key = "myWords", text = "My words"),
    UiCopyRow(key = "addOwnWord", text = "Add your own word"),
    UiCopyRow(key = "edit", text = "Edit"),
    UiCopyRow(key = "delete", text = "Delete"),
    UiCopyRow(key = "deleteWord", text = "Delete word"),
    UiCopyRow(key = "save", text = "Save"),
    UiCopyRow(key = "deleteWordWarning", text = "The word will leave the catalog; its rating history stays in JSON."),
    UiCopyRow(key = "add", text = "Add"),
    UiCopyRow(key = "remove", text = "Remove"),
    UiCopyRow(key = "deleteWordTitle", text = "Delete your word?"),
    UiCopyRow(key = "newWord", text = "New word"),
    UiCopyRow(key = "editWord", text = "Edit word"),
    UiCopyRow(key = "polishWord", text = "Polish word"),
    UiCopyRow(key = "translation", text = "Translation"),
    UiCopyRow(key = "form", text = "Form"),
    UiCopyRow(key = "exampleSentence", text = "Example sentence"),
    UiCopyRow(key = "level", text = "Level"),
    UiCopyRow(key = "exportProgressJson", text = "Export JSON"),
    UiCopyRow(key = "importProgressJson", text = "Import JSON"),
    UiCopyRow(key = "totalReviews", text = "Total reviews"),
    UiCopyRow(key = "nextDueLabel", text = "Next"),
    UiCopyRow(key = "wordSet", text = "Word set"),
    UiCopyRow(key = "matrixSection", text = "Section"),
    UiCopyRow(key = "vocabularyRecoveryAdvice", text = "Export the original JSON and import a corrected vocabulary."),
    UiCopyRow(key = "vocabularyUnavailable", text = "Vocabulary is unavailable for now"),
    UiCopyRow(key = "studyDirection", text = "Study"),
    UiCopyRow(key = "vocabularyCard", text = "Card"),
    UiCopyRow(key = "myVocabularyTemplate", text = "My vocabulary · {count}"),
    UiCopyRow(key = "vocabularyFilter", text = "Selection"),
    UiCopyRow(key = "remember", text = "REMEMBER"),
    UiCopyRow(key = "why", text = "Why?"),
    UiCopyRow(key = "expanded", text = "expanded"),
    UiCopyRow(key = "collapsed", text = "collapsed"),
    UiCopyRow(key = "lifehackBadge", text = "💡 Lifehack available"),
    UiCopyRow(key = "lifehackAccessibility", text = "There is a lifehack for this skill"),
    UiCopyRow(key = "lifehackSourceTemplate", text = "Lifehack · source: {status}"),
    UiCopyRow(key = "sourceLink", text = "source"),
    UiCopyRow(key = "whatChanged", text = "What changed"),
    UiCopyRow(key = "langLower.pl", text = "polish"),
    UiCopyRow(key = "langLower.en", text = "english"),
    UiCopyRow(key = "langLower.ru", text = "russian"),
    UiCopyRow(key = "recallAdverb.pl", text = "in Polish"),
    UiCopyRow(key = "recallAdverb.en", text = "in English"),
    UiCopyRow(key = "recallAdverb.ru", text = "in Russian"),
    UiCopyRow(key = "recallInTemplate", text = "Recall {language}"),
    UiCopyRow(key = "typedAnswerToggle", text = "Type the answer"),
    UiCopyRow(key = "yourAnswer", text = "Your answer"),
    UiCopyRow(key = "check", text = "Check"),
    UiCopyRow(key = "selfReviewTemplate", text = "Your answer: {answer}. Compare it and choose a rating."),
    UiCopyRow(key = "matrixChooseWordGroup", text = "Choose a word group"),
    UiCopyRow(key = "matrixWord", text = "Word"),
    UiCopyRow(key = "matrixAdjective", text = "Adjective"),
    UiCopyRow(key = "matrixOwner", text = "Owner"),
    UiCopyRow(key = "matrixNumber", text = "Number"),
    UiCopyRow(key = "matrixTrainChain", text = "Practice this chain"),
    UiCopyRow(key = "matrixTrainMasculine", text = "Practice masculine forms"),
    UiCopyRow(key = "matrixTrainNegation", text = "Practice negation"),
    UiCopyRow(key = "matrixDeclensionComparison", text = "Compare declension types"),
    UiCopyRow(key = "matrixCaseComparison", text = "Case to compare"),
    UiCopyRow(key = "matrixVerb", text = "Verb"),
    UiCopyRow(key = "matrixTrainTenses", text = "Practice tenses"),
    UiCopyRow(key = "matrixTrainOwnerChange", text = "Practice owner changes"),
    UiCopyRow(key = "vocabularyTop100", text = "Top 100"),
    UiCopyRow(key = "vocabularyTop500", text = "Top 500"),
    UiCopyRow(key = "vocabularyTop1000", text = "Top 1000"),
    UiCopyRow(key = "importSettingsSizeLimit", text = "The file must be UTF-8 JSON and at most 1 MB"),
    UiCopyRow(key = "importDataSizeLimit", text = "The file must be UTF-8 JSON and at most 10 MB"),
    UiCopyRow(key = "settingsImported", text = "Settings imported"),
    UiCopyRow(key = "vocabularyImported", text = "Vocabulary imported"),
    UiCopyRow(key = "vocabularyImportFailed", text = "Vocabulary import failed"),
    UiCopyRow(key = "progressExported", text = "Progress exported"),
    UiCopyRow(key = "progressImported", text = "Progress imported"),
    UiCopyRow(key = "examTimedStart", text = "Full timed exam"),
    UiCopyRow(key = "examTimedPart", text = "Part"),
    UiCopyRow(key = "examTimedComplete", text = "Exam completed"),
    UiCopyRow(key = "examBackToModules", text = "← Modules"),
    UiCopyRow(key = "examPrevious", text = "← Previous"),
    UiCopyRow(key = "examAnswerPrompt", text = "Enter an answer"),
    UiCopyRow(key = "examNativeUnavailable", text = "Not available on iPad yet"),
    UiCopyRow(key = "examChooseModule", text = "Choose a part"),
    UiCopyRow(key = "examAudioPlay", text = "Play"),
    UiCopyRow(key = "examAudioPause", text = "Pause"),
    UiCopyRow(key = "examAudioReplay", text = "Replay"),
    UiCopyRow(key = "examAudioSeek", text = "Seek recording"),
    UiCopyRow(key = "examAudioElapsed", text = "Elapsed"),
    UiCopyRow(key = "examAudioDuration", text = "Duration"),
    UiCopyRow(key = "examAudioUnavailable", text = "Offline audio is unavailable"),
    UiCopyRow(key = "examAudioInvalidSegment", text = "Audio segment is unavailable"),
    UiCopyRow(key = "examAudioPlaybackFailed", text = "Audio playback failed"),
    UiCopyRow(key = "examImageUnavailable", text = "Image is unavailable"),
    UiCopyRow(key = "examImmediateFeedbackSetting", text = "Show the correct answer immediately"),
    UiCopyRow(key = "examYourAnswer", text = "Your answer"),
    UiCopyRow(key = "examFeedbackCorrect", text = "Correct"),
    UiCopyRow(key = "examFeedbackWrong", text = "Incorrect"),
    UiCopyRow(key = "examFeedbackPartial", text = "Partly correct"),
    UiCopyRow(key = "examAnswerLocked", text = "Answer locked"),
)
