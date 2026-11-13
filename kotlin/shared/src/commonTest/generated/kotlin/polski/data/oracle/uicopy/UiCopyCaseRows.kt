// Generated from dsl/course.data-contract, dsl/ui-copy/uicopy-oracle.core-data
// source-sha256=8d297a43f02dd884a8988b7e3cf6d9b2864cf15bf767faa2680ef50be58bff1f
// Edit the source, then run biome generate.

package polski.data.oracle.uicopy

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

val UI_COPY_CASE_ROW_ROWS: List<UiCopyCaseRow> = listOf(
    UiCopyCaseRow(key = "again", plRu = "Повторить", enRu = "Повторить", placeholders = ""),
    UiCopyCaseRow(key = "good", plRu = "Вспомнил", enRu = "Вспомнил", placeholders = ""),
    UiCopyCaseRow(key = "progressTab", plRu = "Прогресс", enRu = "Прогресс", placeholders = ""),
    UiCopyCaseRow(key = "settingsTab", plRu = "Настройки", enRu = "Настройки", placeholders = ""),
    UiCopyCaseRow(key = "lifehacks", plRu = "Лайфхаки", enRu = "Лайфхаки", placeholders = ""),
    UiCopyCaseRow(key = "showAnswer", plRu = "Показать ответ", enRu = "Показать ответ", placeholders = ""),
    UiCopyCaseRow(
        key = "checkAndShowAnswer",
        plRu = "Проверить и показать ответ",
        enRu = "Проверить и показать ответ",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "exportJson", plRu = "Экспорт JSON", enRu = "Экспорт JSON", placeholders = ""),
    UiCopyCaseRow(key = "resetProgress", plRu = "Сбросить прогресс", enRu = "Сбросить прогресс", placeholders = ""),
    UiCopyCaseRow(key = "hideTable", plRu = "Скрыть таблицу", enRu = "Скрыть таблицу", placeholders = ""),
    UiCopyCaseRow(key = "tableAtHand", plRu = "Таблица под рукой", enRu = "Таблица под рукой", placeholders = ""),
    UiCopyCaseRow(
        key = "allTablesAndSchema",
        plRu = "Все таблицы и схема",
        enRu = "Все таблицы и схема",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "whenToReview", plRu = "Когда повторить?", enRu = "Когда повторить?", placeholders = ""),
    UiCopyCaseRow(key = "noDueTitle", plRu = "Новых повторений пока нет.", enRu = "No reviews due.", placeholders = ""),
    UiCopyCaseRow(
        key = "compareAnswer",
        plRu = "Сравни свой ответ с эталоном",
        enRu = "Compare your answer with the model",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "backSideReference",
        plRu = "Обратная сторона · эталон",
        enRu = "Back side · model answer",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "noAnswerEntered", plRu = "Ответ не введён", enRu = "No answer entered", placeholders = ""),
    UiCopyCaseRow(key = "matches", plRu = "Совпадает с правильным вариантом", enRu = "Matches", placeholders = ""),
    UiCopyCaseRow(key = "differs", plRu = "Отличается", enRu = "Differs", placeholders = ""),
    UiCopyCaseRow(key = "selectExam", plRu = "Выберите экзамен", enRu = "Select exam", placeholders = ""),
    UiCopyCaseRow(key = "start", plRu = "Начать", enRu = "Start", placeholders = ""),
    UiCopyCaseRow(key = "next", plRu = "Далее →", enRu = "Next →", placeholders = ""),
    UiCopyCaseRow(key = "finish", plRu = "Завершить", enRu = "Finish", placeholders = ""),
    UiCopyCaseRow(key = "tryAgain", plRu = "Пройти ещё раз", enRu = "Try again", placeholders = ""),
    UiCopyCaseRow(key = "task", plRu = "Задание", enRu = "Task", placeholders = ""),
    UiCopyCaseRow(key = "grammar", plRu = "Грамматика", enRu = "Grammar", placeholders = ""),
    UiCopyCaseRow(key = "listening", plRu = "Аудирование", enRu = "Listening", placeholders = ""),
    UiCopyCaseRow(key = "reading", plRu = "Чтение", enRu = "Reading", placeholders = ""),
    UiCopyCaseRow(key = "writing", plRu = "Письмо", enRu = "Writing", placeholders = ""),
    UiCopyCaseRow(key = "selectWritingSet", plRu = "Выберите набор", enRu = "Select a set", placeholders = ""),
    UiCopyCaseRow(key = "changeWritingSet", plRu = "Сменить набор", enRu = "Change set", placeholders = ""),
    UiCopyCaseRow(key = "writingSet", plRu = "Набор", enRu = "Set", placeholders = ""),
    UiCopyCaseRow(key = "results", plRu = "Результаты", enRu = "Results", placeholders = ""),
    UiCopyCaseRow(key = "comingSoon", plRu = "Скоро", enRu = "Coming soon", placeholders = ""),
    UiCopyCaseRow(key = "exam", plRu = "Экзамен", enRu = "Exam", placeholders = ""),
    UiCopyCaseRow(key = "examConfirmedPoints", plRu = "Подтверждено", enRu = "Confirmed", placeholders = ""),
    UiCopyCaseRow(key = "examPendingPoints", plRu = "Ожидает проверки", enRu = "Awaiting review", placeholders = ""),
    UiCopyCaseRow(
        key = "examManualReview",
        plRu = "Требуется ручная проверка",
        enRu = "Needs manual review",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "examOfficialExamples",
        plRu = "Образцы из ключа",
        enRu = "Examples from the key",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "cardsTab", plRu = "Карточки", enRu = "Cards", placeholders = ""),
    UiCopyCaseRow(key = "vocabularyTab", plRu = "Слова", enRu = "Vocabulary", placeholders = ""),
    UiCopyCaseRow(key = "matrixTab", plRu = "Таблицы и схема", enRu = "Tables & scheme", placeholders = ""),
    UiCopyCaseRow(key = "lang.pl", plRu = "Польский", enRu = "Польский", placeholders = ""),
    UiCopyCaseRow(key = "lang.en", plRu = "Английский", enRu = "Английский", placeholders = ""),
    UiCopyCaseRow(key = "lang.ru", plRu = "Русский", enRu = "Русский", placeholders = ""),
    UiCopyCaseRow(key = "adverb.pl", plRu = "по-польски", enRu = "по-польски", placeholders = ""),
    UiCopyCaseRow(key = "adverb.en", plRu = "по-английски", enRu = "по-английски", placeholders = ""),
    UiCopyCaseRow(key = "adverb.ru", plRu = "по-русски", enRu = "по-русски", placeholders = ""),
    UiCopyCaseRow(key = "tense.present", plRu = "Настоящее", enRu = "Present", placeholders = ""),
    UiCopyCaseRow(key = "tense.past", plRu = "Прошедшее", enRu = "Past", placeholders = ""),
    UiCopyCaseRow(key = "tense.future", plRu = "Будущее", enRu = "Future", placeholders = ""),
    UiCopyCaseRow(key = "trainingTab", plRu = "Тренировка", enRu = "Training", placeholders = ""),
    UiCopyCaseRow(
        key = "resetTitle",
        plRu = "Сбросить весь прогресс?",
        enRu = "Reset all progress?",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "answerModeTitle", plRu = "Ответ", enRu = "Answer", placeholders = ""),
    UiCopyCaseRow(key = "modelAnswer", plRu = "Эталон", enRu = "Model answer", placeholders = ""),
    UiCopyCaseRow(key = "themeTitle", plRu = "Тема", enRu = "Theme", placeholders = ""),
    UiCopyCaseRow(key = "matrixMap", plRu = "Карта системы", enRu = "System map", placeholders = ""),
    UiCopyCaseRow(key = "nextDueTemplate", plRu = "Следующее: {date}", enRu = "Next: {date}", placeholders = "date"),
    UiCopyCaseRow(
        key = "reviewsCountTemplate",
        plRu = "Повторений: {reviews} · Точно: {correct} · Серия: {streak}",
        enRu = "Reviews: {reviews} · Correct: {correct} · Streak: {streak}",
        placeholders = "reviews|correct|streak",
    ),
    UiCopyCaseRow(key = "addOwnWord", plRu = "Добавить своё слово", enRu = "Add your own word", placeholders = ""),
    UiCopyCaseRow(key = "settingsDataSection", plRu = "Данные настроек", enRu = "Settings data", placeholders = ""),
    UiCopyCaseRow(key = "vocabularyData", plRu = "Данные словаря", enRu = "Vocabulary data", placeholders = ""),
    UiCopyCaseRow(
        key = "myVocabularyTemplate",
        plRu = "Мой словарь · {count}",
        enRu = "My vocabulary · {count}",
        placeholders = "count",
    ),
    UiCopyCaseRow(
        key = "lifehackSourceTemplate",
        plRu = "Лайфхак · источник: {status}",
        enRu = "Lifehack · source: {status}",
        placeholders = "status",
    ),
    UiCopyCaseRow(key = "langLower.pl", plRu = "польский", enRu = "polish", placeholders = ""),
    UiCopyCaseRow(key = "langLower.en", plRu = "английский", enRu = "english", placeholders = ""),
    UiCopyCaseRow(key = "langLower.ru", plRu = "русский", enRu = "russian", placeholders = ""),
    UiCopyCaseRow(key = "recallAdverb.pl", plRu = "по-польски", enRu = "in Polish", placeholders = ""),
    UiCopyCaseRow(key = "recallAdverb.en", plRu = "по-английски", enRu = "in English", placeholders = ""),
    UiCopyCaseRow(key = "recallAdverb.ru", plRu = "по-русски", enRu = "in Russian", placeholders = ""),
    UiCopyCaseRow(
        key = "recallInTemplate",
        plRu = "Вспомни {language}",
        enRu = "Recall {language}",
        placeholders = "language",
    ),
    UiCopyCaseRow(
        key = "matrixChooseWordGroup",
        plRu = "Выбери группу слов",
        enRu = "Choose a word group",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "matrixWord", plRu = "Слово", enRu = "Word", placeholders = ""),
    UiCopyCaseRow(key = "matrixAdjective", plRu = "Прилагательное", enRu = "Adjective", placeholders = ""),
    UiCopyCaseRow(key = "matrixOwner", plRu = "Владелец", enRu = "Owner", placeholders = ""),
    UiCopyCaseRow(key = "matrixNumber", plRu = "Число", enRu = "Number", placeholders = ""),
    UiCopyCaseRow(
        key = "matrixTrainChain",
        plRu = "Тренировать эту цепочку",
        enRu = "Practice this chain",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "matrixTrainMasculine",
        plRu = "Тренировать мужской род",
        enRu = "Practice masculine forms",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "matrixTrainNegation",
        plRu = "Тренировать отрицание",
        enRu = "Practice negation",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "matrixDeclensionComparison",
        plRu = "Сравнение типов склонения",
        enRu = "Compare declension types",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "matrixCaseComparison",
        plRu = "Падеж для сравнения",
        enRu = "Case to compare",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "matrixVerb", plRu = "Глагол", enRu = "Verb", placeholders = ""),
    UiCopyCaseRow(key = "matrixTrainTenses", plRu = "Тренировать времена", enRu = "Practice tenses", placeholders = ""),
    UiCopyCaseRow(
        key = "matrixTrainOwnerChange",
        plRu = "Тренировать смену владельца",
        enRu = "Practice owner changes",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "vocabularyTop100", plRu = "Топ 100", enRu = "Top 100", placeholders = ""),
    UiCopyCaseRow(key = "vocabularyTop500", plRu = "Топ 500", enRu = "Top 500", placeholders = ""),
    UiCopyCaseRow(key = "vocabularyTop1000", plRu = "Топ 1000", enRu = "Top 1000", placeholders = ""),
    UiCopyCaseRow(
        key = "importSettingsSizeLimit",
        plRu = "Файл должен быть UTF-8 JSON не больше 1 МБ",
        enRu = "The file must be UTF-8 JSON and at most 1 MB",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "importDataSizeLimit",
        plRu = "Файл должен быть UTF-8 JSON не больше 10 МБ",
        enRu = "The file must be UTF-8 JSON and at most 10 MB",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "settingsImported",
        plRu = "Настройки импортированы",
        enRu = "Settings imported",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "vocabularyImported",
        plRu = "Словарь импортирован",
        enRu = "Vocabulary imported",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "vocabularyImportFailed",
        plRu = "Импорт словаря не удался",
        enRu = "Vocabulary import failed",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "progressExported",
        plRu = "Прогресс экспортирован",
        enRu = "Progress exported",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "progressImported",
        plRu = "Прогресс импортирован",
        enRu = "Progress imported",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "examTimedStart",
        plRu = "Полный экзамен по времени",
        enRu = "Full timed exam",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "examTimedPart", plRu = "Часть", enRu = "Part", placeholders = ""),
    UiCopyCaseRow(key = "examTimedComplete", plRu = "Экзамен завершён", enRu = "Exam completed", placeholders = ""),
    UiCopyCaseRow(key = "examBackToModules", plRu = "← К модулям", enRu = "← Modules", placeholders = ""),
    UiCopyCaseRow(key = "examPrevious", plRu = "← Назад", enRu = "← Previous", placeholders = ""),
    UiCopyCaseRow(key = "examAnswerPrompt", plRu = "Введи ответ", enRu = "Enter an answer", placeholders = ""),
    UiCopyCaseRow(
        key = "examNativeUnavailable",
        plRu = "Пока недоступно на iPad",
        enRu = "Not available on iPad yet",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "examChooseModule", plRu = "Выберите часть", enRu = "Choose a part", placeholders = ""),
    UiCopyCaseRow(key = "examAudioPlay", plRu = "Воспроизвести", enRu = "Play", placeholders = ""),
    UiCopyCaseRow(key = "examAudioPause", plRu = "Пауза", enRu = "Pause", placeholders = ""),
    UiCopyCaseRow(key = "examAudioReplay", plRu = "С начала", enRu = "Replay", placeholders = ""),
    UiCopyCaseRow(key = "examAudioSeek", plRu = "Перемотка записи", enRu = "Seek recording", placeholders = ""),
    UiCopyCaseRow(key = "examAudioElapsed", plRu = "Прослушано", enRu = "Elapsed", placeholders = ""),
    UiCopyCaseRow(key = "examAudioDuration", plRu = "Длительность", enRu = "Duration", placeholders = ""),
    UiCopyCaseRow(
        key = "examAudioUnavailable",
        plRu = "Аудиозапись недоступна",
        enRu = "Offline audio is unavailable",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "examAudioInvalidSegment",
        plRu = "Фрагмент аудиозаписи недоступен",
        enRu = "Audio segment is unavailable",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "examAudioPlaybackFailed",
        plRu = "Не удалось воспроизвести аудиозапись",
        enRu = "Audio playback failed",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "examImageUnavailable",
        plRu = "Изображение недоступно",
        enRu = "Image is unavailable",
        placeholders = "",
    ),
    UiCopyCaseRow(
        key = "examImmediateFeedbackSetting",
        plRu = "Показывать правильный ответ сразу",
        enRu = "Show the correct answer immediately",
        placeholders = "",
    ),
    UiCopyCaseRow(key = "examYourAnswer", plRu = "Ваш ответ", enRu = "Your answer", placeholders = ""),
    UiCopyCaseRow(key = "examFeedbackCorrect", plRu = "Верно", enRu = "Correct", placeholders = ""),
    UiCopyCaseRow(key = "examFeedbackWrong", plRu = "Неверно", enRu = "Incorrect", placeholders = ""),
    UiCopyCaseRow(key = "examFeedbackPartial", plRu = "Частично верно", enRu = "Partly correct", placeholders = ""),
)
