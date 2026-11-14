// Generated from dsl/course.data-contract, courses-biome/pl-ru/uiCopy.core-data
// source-sha256=7220bebcd199ae6efad76859f354f5efa10917e9625367ded8d6eb83611964b2
// Edit the source, then run biome generate.

package polski.data.rows.plru.uicopy

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
    val caseReferenceAvailable: Boolean? = null,
    val predictionQuestion: String? = null,
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
    val staticPool: List<StaticExerciseRow>? = null,
    val tileTargetSlots: List<String>? = null,
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
    val tileTargetSlots: List<String>? = null,
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
    UiCopyRow(key = "noDueTitle", text = "Новых повторений пока нет."),
    UiCopyRow(key = "compareAnswer", text = "Сравни свой ответ с эталоном"),
    UiCopyRow(key = "backSideReference", text = "Обратная сторона · эталон"),
    UiCopyRow(key = "noAnswerEntered", text = "Ответ не введён"),
    UiCopyRow(key = "matches", text = "Совпадает с правильным вариантом"),
    UiCopyRow(key = "differs", text = "Отличается"),
    UiCopyRow(key = "selectExam", text = "Выберите экзамен"),
    UiCopyRow(key = "start", text = "Начать"),
    UiCopyRow(key = "next", text = "Далее →"),
    UiCopyRow(key = "finish", text = "Завершить"),
    UiCopyRow(key = "tryAgain", text = "Пройти ещё раз"),
    UiCopyRow(key = "task", text = "Задание"),
    UiCopyRow(key = "grammar", text = "Грамматика"),
    UiCopyRow(key = "listening", text = "Аудирование"),
    UiCopyRow(key = "reading", text = "Чтение"),
    UiCopyRow(key = "writing", text = "Письмо"),
    UiCopyRow(key = "writingInstructionsAndCriteria", text = "Инструкция и критерии"),
    UiCopyRow(key = "selectWritingSet", text = "Выберите набор"),
    UiCopyRow(key = "changeWritingSet", text = "Сменить набор"),
    UiCopyRow(key = "writingSet", text = "Набор"),
    UiCopyRow(key = "results", text = "Результаты"),
    UiCopyRow(key = "comingSoon", text = "Скоро"),
    UiCopyRow(key = "exam", text = "Экзамен"),
    UiCopyRow(key = "missions", text = "Миссии"),
    UiCopyRow(key = "examConfirmedPoints", text = "Подтверждено"),
    UiCopyRow(key = "examPendingPoints", text = "Ожидает проверки"),
    UiCopyRow(key = "examManualReview", text = "Требуется ручная проверка"),
    UiCopyRow(key = "examOfficialExamples", text = "Образцы из ключа"),
    UiCopyRow(key = "cardsTab", text = "Карточки"),
    UiCopyRow(key = "vocabularyTab", text = "Слова"),
    UiCopyRow(key = "matrixTab", text = "Таблицы и схема"),
    UiCopyRow(key = "lang.pl", text = "Польский"),
    UiCopyRow(key = "lang.en", text = "Английский"),
    UiCopyRow(key = "lang.ru", text = "Русский"),
    UiCopyRow(key = "adverb.pl", text = "по-польски"),
    UiCopyRow(key = "adverb.en", text = "по-английски"),
    UiCopyRow(key = "adverb.ru", text = "по-русски"),
    UiCopyRow(key = "tense.present", text = "Настоящее"),
    UiCopyRow(key = "tense.past", text = "Прошедшее"),
    UiCopyRow(key = "tense.future", text = "Будущее"),
    UiCopyRow(key = "trainingTab", text = "Тренировка"),
    UiCopyRow(key = "matrixNavTitle", text = "Матрица"),
    UiCopyRow(key = "resetTitle", text = "Сбросить весь прогресс?"),
    UiCopyRow(key = "resetConfirm", text = "Сбросить"),
    UiCopyRow(key = "cancel", text = "Отмена"),
    UiCopyRow(key = "resetWarning", text = "Все оценки и расписание повторений будут удалены."),
    UiCopyRow(key = "noticeTitle", text = "Сообщение"),
    UiCopyRow(key = "ok", text = "ОК"),
    UiCopyRow(key = "recoverySection", text = "Восстановление"),
    UiCopyRow(key = "recoveryAdvice", text = "Экспортируйте исходные настройки перед импортом проверенной копии."),
    UiCopyRow(key = "courseSection", text = "Курс"),
    UiCopyRow(key = "targetLanguage", text = "Изучаемый язык"),
    UiCopyRow(key = "nativeLanguage", text = "Родной язык"),
    UiCopyRow(key = "learningSection", text = "Обучение"),
    UiCopyRow(key = "explanationStyle", text = "Стиль объяснений"),
    UiCopyRow(
        key = "nativeContrastFallback",
        text = "Для текущего навыка пока нет пары для сравнения с родным — карточка показывается как «через правило».",
    ),
    UiCopyRow(key = "appearanceSection", text = "Внешний вид"),
    UiCopyRow(key = "answerModeTitle", text = "Ответ"),
    UiCopyRow(key = "oralMode", text = "Вслух"),
    UiCopyRow(key = "oralOrSilentMode", text = "Вслух / про себя"),
    UiCopyRow(key = "typedMode", text = "Напечатать"),
    UiCopyRow(key = "themeTitle", text = "Тема"),
    UiCopyRow(key = "themeSystem", text = "Системная"),
    UiCopyRow(key = "themeLight", text = "Светлая"),
    UiCopyRow(key = "themeDark", text = "Тёмная"),
    UiCopyRow(key = "motionTitle", text = "Движение"),
    UiCopyRow(key = "motionSystem", text = "Системное"),
    UiCopyRow(key = "motionReduced", text = "Сокращённое"),
    UiCopyRow(key = "motionReducedStatus", text = "Система сокращает движение"),
    UiCopyRow(key = "motionActiveStatus", text = "Системное движение активно"),
    UiCopyRow(key = "animationsTitle", text = "Анимации"),
    UiCopyRow(key = "settingsDataSection", text = "Данные настроек"),
    UiCopyRow(key = "exportSettingsJson", text = "Экспортировать настройки JSON"),
    UiCopyRow(key = "importSettingsJson", text = "Импортировать настройки JSON"),
    UiCopyRow(key = "done", text = "Готово"),
    UiCopyRow(key = "heroSubtitle", text = "Говори целыми предложениями"),
    UiCopyRow(key = "dueStat", text = "К повторению"),
    UiCopyRow(key = "todayStat", text = "Сегодня"),
    UiCopyRow(key = "loading", text = "Загружаем…"),
    UiCopyRow(key = "needsProgressCopy", text = "Нужна копия прогресса"),
    UiCopyRow(key = "openProgressForTransfer", text = "Откройте «Прогресс» для импорта или экспорта JSON."),
    UiCopyRow(key = "modeTemplate", text = "Режим: {mode}"),
    UiCopyRow(key = "sentenceChain", text = "Цепочка предложений"),
    UiCopyRow(key = "scheduledReviews", text = "По расписанию"),
    UiCopyRow(key = "singleSkill", text = "Отдельный навык"),
    UiCopyRow(key = "skillTitle", text = "Навык"),
    UiCopyRow(key = "nextWordSet", text = "Следующий набор слов"),
    UiCopyRow(key = "backToReviews", text = "К повторениям"),
    UiCopyRow(key = "reviewsDone", text = "Повторения на сейчас завершены"),
    UiCopyRow(key = "practiceChain", text = "Потренировать цепочку"),
    UiCopyRow(key = "sourceSentence", text = "Исходное предложение"),
    UiCopyRow(key = "skillIntroduction", text = "Знакомство с навыком"),
    UiCopyRow(key = "goToTask", text = "Перейти к заданию"),
    UiCopyRow(key = "modelAnswer", text = "Эталон"),
    UiCopyRow(key = "alsoAcceptedTemplate", text = "Также: {answers}"),
    UiCopyRow(key = "swipeHint", text = "Свайп влево — повторить · вправо — вспомнил"),
    UiCopyRow(key = "polishAnswer", text = "Ответ по-польски"),
    UiCopyRow(key = "oralInstruction", text = "Произнеси целое предложение, затем покажи ответ."),
    UiCopyRow(key = "checkAnswer", text = "Проверить ответ"),
    UiCopyRow(key = "before", text = "Было"),
    UiCopyRow(key = "after", text = "Стало"),
    UiCopyRow(key = "matrixMap", text = "Карта системы"),
    UiCopyRow(key = "casesAndEndings", text = "Падежи и окончания"),
    UiCopyRow(key = "tensesAndPersons", text = "Времена и лица"),
    UiCopyRow(key = "pronouns", text = "Местоимения"),
    UiCopyRow(key = "summary", text = "Сводка"),
    UiCopyRow(key = "skills", text = "Навыки"),
    UiCopyRow(key = "dataSection", text = "Данные"),
    UiCopyRow(key = "reviewsCountTemplate", text = "Повторений: {reviews} · Точно: {correct} · Серия: {streak}"),
    UiCopyRow(key = "nextDueTemplate", text = "Следующее: {date}"),
    UiCopyRow(key = "noLifehacks", text = "Для этого набора лайфхаков пока нет."),
    UiCopyRow(key = "vocabularyData", text = "Данные словаря"),
    UiCopyRow(key = "importVocabularyJson", text = "Импортировать словарь JSON"),
    UiCopyRow(key = "exportVocabularyJson", text = "Экспортировать словарь JSON"),
    UiCopyRow(key = "statusSection", text = "Состояние"),
    UiCopyRow(key = "directionSection", text = "Направление"),
    UiCopyRow(key = "cardSection", text = "Карточка"),
    UiCopyRow(key = "selectWords", text = "Выберите слова для тренировки"),
    UiCopyRow(key = "allReviewed", text = "На сейчас всё повторено"),
    UiCopyRow(key = "myWords", text = "Мои слова"),
    UiCopyRow(key = "addOwnWord", text = "Добавить своё слово"),
    UiCopyRow(key = "edit", text = "Изменить"),
    UiCopyRow(key = "delete", text = "Удалить"),
    UiCopyRow(key = "deleteWord", text = "Удалить слово"),
    UiCopyRow(key = "save", text = "Сохранить"),
    UiCopyRow(key = "deleteWordWarning", text = "Слово исчезнет из каталога, история оценок сохранится в JSON."),
    UiCopyRow(key = "add", text = "Добавить"),
    UiCopyRow(key = "remove", text = "Убрать"),
    UiCopyRow(key = "deleteWordTitle", text = "Удалить своё слово?"),
    UiCopyRow(key = "newWord", text = "Новое слово"),
    UiCopyRow(key = "editWord", text = "Изменить слово"),
    UiCopyRow(key = "polishWord", text = "Польское слово"),
    UiCopyRow(key = "translation", text = "Перевод"),
    UiCopyRow(key = "form", text = "Форма"),
    UiCopyRow(key = "exampleSentence", text = "Пример в предложении"),
    UiCopyRow(key = "level", text = "Уровень"),
    UiCopyRow(key = "exportProgressJson", text = "Экспортировать JSON"),
    UiCopyRow(key = "importProgressJson", text = "Импортировать JSON"),
    UiCopyRow(key = "totalReviews", text = "Всего повторений"),
    UiCopyRow(key = "nextDueLabel", text = "Следующее"),
    UiCopyRow(key = "wordSet", text = "Набор слов"),
    UiCopyRow(key = "matrixSection", text = "Раздел"),
    UiCopyRow(
        key = "vocabularyRecoveryAdvice",
        text = "Экспортируйте исходный JSON и импортируйте исправленный словарь.",
    ),
    UiCopyRow(key = "vocabularyUnavailable", text = "Словарь пока недоступен"),
    UiCopyRow(key = "studyDirection", text = "Учить"),
    UiCopyRow(key = "vocabularyCard", text = "Карточка"),
    UiCopyRow(key = "myVocabularyTemplate", text = "Мой словарь · {count}"),
    UiCopyRow(key = "vocabularyFilter", text = "Подборка"),
    UiCopyRow(key = "remember", text = "ЗАПОМНИ"),
    UiCopyRow(key = "why", text = "Почему так?"),
    UiCopyRow(key = "expanded", text = "развёрнуто"),
    UiCopyRow(key = "collapsed", text = "свёрнуто"),
    UiCopyRow(key = "lifehackBadge", text = "💡 Есть лайфхак"),
    UiCopyRow(key = "lifehackAccessibility", text = "Есть лайфхак для этого навыка"),
    UiCopyRow(key = "lifehackSourceTemplate", text = "Лайфхак · источник: {status}"),
    UiCopyRow(key = "sourceLink", text = "источник"),
    UiCopyRow(key = "whatChanged", text = "Что изменилось"),
    UiCopyRow(key = "langLower.pl", text = "польский"),
    UiCopyRow(key = "langLower.en", text = "английский"),
    UiCopyRow(key = "langLower.ru", text = "русский"),
    UiCopyRow(key = "recallAdverb.pl", text = "по-польски"),
    UiCopyRow(key = "recallAdverb.en", text = "по-английски"),
    UiCopyRow(key = "recallAdverb.ru", text = "по-русски"),
    UiCopyRow(key = "recallInTemplate", text = "Вспомни {language}"),
    UiCopyRow(key = "typedAnswerToggle", text = "Напечатать ответ"),
    UiCopyRow(key = "yourAnswer", text = "Твой ответ"),
    UiCopyRow(key = "check", text = "Проверить"),
    UiCopyRow(key = "selfReviewTemplate", text = "Твой ответ: {answer}. Сравни сам и выбери оценку."),
    UiCopyRow(key = "matrixChooseWordGroup", text = "Выбери группу слов"),
    UiCopyRow(key = "matrixWord", text = "Слово"),
    UiCopyRow(key = "matrixAdjective", text = "Прилагательное"),
    UiCopyRow(key = "matrixOwner", text = "Владелец"),
    UiCopyRow(key = "matrixNumber", text = "Число"),
    UiCopyRow(key = "matrixTrainChain", text = "Тренировать эту цепочку"),
    UiCopyRow(key = "matrixTrainMasculine", text = "Тренировать мужской род"),
    UiCopyRow(key = "matrixTrainNegation", text = "Тренировать отрицание"),
    UiCopyRow(key = "matrixDeclensionComparison", text = "Сравнение типов склонения"),
    UiCopyRow(key = "matrixCaseComparison", text = "Падеж для сравнения"),
    UiCopyRow(key = "matrixVerb", text = "Глагол"),
    UiCopyRow(key = "matrixTrainTenses", text = "Тренировать времена"),
    UiCopyRow(key = "matrixTrainOwnerChange", text = "Тренировать смену владельца"),
    UiCopyRow(key = "vocabularyTop100", text = "Топ 100"),
    UiCopyRow(key = "vocabularyTop500", text = "Топ 500"),
    UiCopyRow(key = "vocabularyTop1000", text = "Топ 1000"),
    UiCopyRow(key = "importSettingsSizeLimit", text = "Файл должен быть UTF-8 JSON не больше 1 МБ"),
    UiCopyRow(key = "importDataSizeLimit", text = "Файл должен быть UTF-8 JSON не больше 10 МБ"),
    UiCopyRow(key = "settingsImported", text = "Настройки импортированы"),
    UiCopyRow(key = "vocabularyImported", text = "Словарь импортирован"),
    UiCopyRow(key = "vocabularyImportFailed", text = "Импорт словаря не удался"),
    UiCopyRow(key = "progressExported", text = "Прогресс экспортирован"),
    UiCopyRow(key = "progressImported", text = "Прогресс импортирован"),
    UiCopyRow(key = "examTimedStart", text = "Полный экзамен по времени"),
    UiCopyRow(key = "examTimedPart", text = "Часть"),
    UiCopyRow(key = "examTimedComplete", text = "Экзамен завершён"),
    UiCopyRow(key = "examBackToModules", text = "← К модулям"),
    UiCopyRow(key = "examPrevious", text = "← Назад"),
    UiCopyRow(key = "examAnswerPrompt", text = "Введи ответ"),
    UiCopyRow(key = "examNativeUnavailable", text = "Пока недоступно на iPad"),
    UiCopyRow(key = "examChooseModule", text = "Выберите часть"),
    UiCopyRow(key = "examAudioPlay", text = "Воспроизвести"),
    UiCopyRow(key = "examAudioPause", text = "Пауза"),
    UiCopyRow(key = "examAudioReplay", text = "С начала"),
    UiCopyRow(key = "examAudioSeek", text = "Перемотка записи"),
    UiCopyRow(key = "examAudioElapsed", text = "Прослушано"),
    UiCopyRow(key = "examAudioDuration", text = "Длительность"),
    UiCopyRow(key = "examAudioUnavailable", text = "Аудиозапись недоступна"),
    UiCopyRow(key = "examAudioInvalidSegment", text = "Фрагмент аудиозаписи недоступен"),
    UiCopyRow(key = "examAudioPlaybackFailed", text = "Не удалось воспроизвести аудиозапись"),
    UiCopyRow(key = "examImageUnavailable", text = "Изображение недоступно"),
    UiCopyRow(key = "examImmediateFeedbackSetting", text = "Показывать правильный ответ сразу"),
    UiCopyRow(key = "examYourAnswer", text = "Ваш ответ"),
    UiCopyRow(key = "examFeedbackCorrect", text = "Верно"),
    UiCopyRow(key = "examFeedbackWrong", text = "Неверно"),
    UiCopyRow(key = "examFeedbackPartial", text = "Частично верно"),
    UiCopyRow(key = "examAnswerLocked", text = "Ответ зафиксирован"),
)
