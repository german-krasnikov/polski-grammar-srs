// Generated from dsl/course.data-contract
// source-sha256=7220bebcd199ae6efad76859f354f5efa10917e9625367ded8d6eb83611964b2
// Edit the source, then run biome generate.

package polski.data.rows.plru

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

private fun skillRowRowsPart0(): List<SkillRow> = listOf(
    SkillRow(
        id = "case.acc.n",
        title = "Biernik · средний род",
        group = "Przypadki",
        level = SkillRowLevel.A1,
        formula = "Средний род: Biernik = Mianownik",
        theory = "В единственном числе средний род сохраняет базовую форму в Bierniku: widzę moje małe dziecko.",
        hint = "moje nowe okno → widzę moje nowe okno",
        prerequisites = emptyList(),
        focus = FocusRow(before = "moje duże morze", after = "moje duże morze"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В единственном числе средний род сохраняет базовую форму в Bierniku: widzę moje małe dziecko.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь, совпадает ли базовая форма среднего рода с формой прямого дополнения.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты показываешь собеседнику то, о чём идёт речь. Назови это целой фразой: в этой группе форма остаётся прежней.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты сообщаешь собеседнику, что видишь названное. Как сказать об этом целой фразой?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Nom", to = "Acc"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(slot = "noun", where = WhereRow(gender = listOf("n"))),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Вижу моё старое окно.",
                    target = "Widzę moje stare okno.",
                    note = "Средний род в винительном падеже не меняется ни в русском, ни в польском: «окно» и okno остаются в исходной форме.",
                    matches = true,
                ),
            ),
            examples = listOf("Mam nowe okno.", "Widzę małe dziecko."),
            why = "Средний род — самый простой случай: винительный падеж внешне совпадает с именительным, потому что у среднего рода в обоих языках нет отдельного окончания для этой роли.",
        ),
    ),
    SkillRow(
        id = "case.acc.f",
        title = "Biernik · żeński",
        group = "Przypadki",
        level = SkillRowLevel.A1,
        formula = "-a → -ę; прилагательное -a → -ą",
        theory = "Прямой объект после widzę, mam, lubię, kupuję обычно стоит в Bierniku.",
        hint = "Widzę kogo? co?",
        prerequisites = emptyList(),
        focus = FocusRow(before = "moja młoda siostra", after = "moją młodą siostrę"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Прямой объект после widzę, mam, lubię, kupuję обычно стоит в Bierniku.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи род и падеж группы после действия, затем проверь согласование.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь собеседнику, кого или что видишь. Представь эту сцену, затем измени окончания всей группы.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты сообщаешь, кого или что видишь. Как назвать объект целой фразой?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Nom", to = "Acc"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(slot = "noun", where = WhereRow(gender = listOf("f"))),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Вижу мою хорошую жену.",
                    target = "Widzę moją dobrą żonę.",
                    note = "В обоих языках женский род на -a/-а получает окончание -ę/-у в винительном: żona→żonę, жена→жену; прилагательное меняется вместе с существительным.",
                    matches = true,
                ),
            ),
            examples = listOf("Mam nową książkę.", "Lubię moją siostrę."),
            why = "Прямой объект после глаголов действия (widzę, mam, lubię) обычно стоит в винительном; у женского рода на -a это всегда -ę, как и в русском -а→-у.",
        ),
        predictionQuestion = "Как думаешь: почему «moja siostra» в предложении «Widzę ...» превращается в «moją siostrę», а не остаётся как в именительном падеже?",
    ),
    SkillRow(
        id = "case.acc.m",
        title = "Biernik · męski",
        group = "Przypadki",
        level = SkillRowLevel.A2,
        formula = "на согласную: живое = Gen; неживое = Nom",
        theory = "Для мужских существительных на согласную: widzę męża/psa, но widzę dom/samochód. У существительных на -a: widzę kolegę, но nie widzę kolegi.",
        hint = "Сначала определи: żywotny или nieżywotny.",
        prerequisites = listOf("case.acc.f"),
        focus = FocusRow(before = "mój stary sąsiad", after = "mojego starego sąsiada"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Для мужских существительных на согласную: widzę męża/psa, но widzę dom/samochód. У существительных на -a: widzę kolegę, но nie widzę kolegi.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи, обозначает ли мужское слово лицо, живое существо или предмет.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты рассказываешь, кого или что видишь. Смысл «вижу кого/что?» помогает выбрать форму всей группы.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты рассказываешь, кого или что видишь. Что важно в значении названного?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Nom", to = "Acc"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(gender = listOf("m-personal", "m-animate", "m-inanimate")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Вижу мужа.",
                    target = "Widzę męża.",
                    note = "Одушевлённые существительные мужского рода в винительном совпадают с родительным — это работает одинаково в русском и польском.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Вижу дом.",
                    target = "Widzę dom.",
                    note = "Неодушевлённые существительные мужского рода в винительном совпадают с именительным — тоже как в русском.",
                    matches = true,
                ),
            ),
            examples = listOf("Widzę psa.", "Widzę samochód."),
            why = "Мужской род различает живое и неживое в винительном падеже — это не польская особенность, а общее славянское правило, знакомое из русского.",
        ),
        predictionQuestion = "Почему Widzę dom, но Widzę psa?",
    ),
    SkillRow(
        id = "case.gen.neg",
        title = "Dopełniacz · negacja",
        group = "Przypadki",
        level = SkillRowLevel.A2,
        formula = "Mam X → Nie mam + GEN",
        theory = "При отрицании прямого объекта Biernik очень часто превращается в Dopełniacz.",
        hint = "Не меняй только nie — поменяй весь noun phrase.",
        prerequisites = listOf("case.acc.f"),
        focus = FocusRow(before = "Widzę moją młodą siostrę.", after = "Nie widzę mojej młodej siostry."),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "При отрицании прямого объекта Biernik очень часто превращается в Dopełniacz.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь, как отрицание влияет на падеж прямого дополнения.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты отрицаешь действие с прямым объектом. В польском при отрицании меняется форма всей группы после глагола.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты отрицаешь действие, сохраняя то, о чём идёт речь. Как сказать это целым предложением?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.sentence.polarity",
        featureFocus = FeatureFocusRow(feature = "Polarity", from = "Pos", to = "Neg"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Не вижу жену.",
                    target = "Nie widzę żony.",
                    note = "В русском при отрицании падеж не меняется (живы оба варианта — «жену»/«жены»); в польском после nie обязателен Dopełniacz.",
                    matches = false,
                ),
                NativeParallelRow(
                    native = "Не вижу книгу.",
                    target = "Nie widzę książki.",
                    note = "Тот же сдвиг у неодушевлённого объекта — родительный обязателен без вариантов.",
                    matches = false,
                ),
            ),
            examples = listOf("Nie mam czasu.", "Nie widzę żadnego problemu."),
            why = "В польском отрицание всегда переводит прямое дополнение в родительный падеж; в русском это необязательно и звучит скорее книжно.",
        ),
        predictionQuestion = "Как думаешь: почему «Nie widzę żony», а не «Nie widzę żonę» — ведь слово то же самое?",
    ),
    SkillRow(
        id = "case.inst",
        title = "Narzędnik · z / być",
        group = "Przypadki",
        level = SkillRowLevel.A1,
        formula = "z + kim? czym? → INST",
        theory = "После z в значении «с кем/чем» используем Narzędnik. Также: Jestem programistą.",
        hint = "z moją żoną, z dobrym kolegą",
        prerequisites = emptyList(),
        focus = FocusRow(before = "moja żona", after = "z moją żoną"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "После z в значении «с кем/чем» используем Narzędnik. Также: Jestem programistą.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи падеж после предлога со значением совместного действия.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты рассказываешь, с кем идёшь. После z в значении «с кем?» меняется вся группа.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты говоришь, с кем идёшь. Как назвать спутника целой фразой?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Acc", to = "Inst"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(nounId = listOf("wife", "husband", "friendM", "son", "child")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Иду с женой.",
                    target = "Idę z żoną.",
                    note = "После «с»/z в значении совместности оба языка требуют творительного падежа.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Я программист.",
                    target = "Jestem programistą.",
                    note = "В польском профессия при być всегда в творительном; в разговорном русском связку «быть» обычно пропускают и говорят в именительном («Я программист»), творительный нужен только с «являюсь».",
                    matches = false,
                ),
            ),
            examples = listOf("Rozmawiam z kolegą.", "Jestem nauczycielem."),
            why = "Творительный после z/с — обычное дело в обоих языках; а вот профессия при глаголе «быть» — разные привычки: поляки всегда используют творительный, русские обычно говорят в именительном без связки.",
        ),
    ),
    SkillRow(
        id = "case.loc",
        title = "Miejscownik · o",
        group = "Przypadki",
        level = SkillRowLevel.A2,
        formula = "o + kim? czym? → LOC",
        theory = "После o (о ком/о чём) нужен Miejscownik.",
        hint = "mówię o żonie / domu",
        prerequisites = listOf("case.inst"),
        focus = FocusRow(before = "moja żona", after = "o mojej żonie"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "После o (о ком/о чём) нужен Miejscownik.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи падеж после предлога, который вводит тему разговора.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты рассказываешь о человеке или предмете из предложения. После o в значении «о ком/о чём?» нужна другая форма.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты рассказываешь о том, кто или что уже названо. Как обозначить тему разговора?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Acc", to = "Loc"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Говорю о жене.",
                    target = "Mówię o żonie.",
                    note = "После «о» оба языка используют падеж, обозначающий тему разговора (в русском предложный, в польском Miejscownik) — сама логика конструкции совпадает.",
                    matches = true,
                ),
            ),
            examples = listOf("Myślę o tobie.", "Czytam o historii."),
            why = "Предлог о/o вводит тему и требует особого падежа — в русском предложный, в польском Miejscownik; окончания разные, но роль в предложении одна и та же.",
        ),
    ),
    SkillRow(
        id = "case.dat",
        title = "Celownik · komu?",
        group = "Przypadki",
        level = SkillRowLevel.A2,
        formula = "daję/pomagam + DAT",
        theory = "Celownik отвечает «кому? чему?». Учим через короткие устойчивые конструкции.",
        hint = "Daję prezent komu?",
        prerequisites = emptyList(),
        focus = FocusRow(before = "moja żona", after = "mojej żonie"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Celownik отвечает «кому? чему?». Учим через короткие устойчивые конструкции.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи падеж получателя действия.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты передаёшь подарок человеку из предложения. Получатель действия отвечает на вопрос «кому?».",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты говоришь, кому передаёшь что-то. Как назвать получателя?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Acc", to = "Dat"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(nounId = listOf("wife", "husband", "friendM", "son", "child")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Даю жене подарок.",
                    target = "Daję żonie prezent.",
                    note = "Дательный падеж получателя действия — совпадает в обоих языках.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Помогаю жене.",
                    target = "Pomagam żonie.",
                    note = "Глагол «помогать»/pomagać управляет дательным падежом в обоих языках.",
                    matches = true,
                ),
            ),
            examples = listOf("Mówię ci prawdę.", "Dziękuję nauczycielowi."),
            why = "Celownik/дательный отвечает на «кому?» и работает одинаково с глаголами давания и помощи в обоих языках.",
        ),
    ),
    SkillRow(
        id = "agreement.my",
        title = "Мой, твой, его, наш, их",
        group = "Zgoda",
        level = SkillRowLevel.A2,
        formula = "Владелец + прилагательное + существительное",
        theory = "mój, twój, nasz, wasz согласуются с существительным. jego, jej, ich не склоняются.",
        hint = "moją piękną żonę → ich piękną żonę",
        prerequisites = listOf("case.acc.f", "case.gen.neg"),
        focus = FocusRow(before = "mojej pięknej żony", after = "ich pięknej żony"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "mój, twój, nasz, wasz согласуются с существительным. jego, jej, ich не склоняются.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Отдели владельца от падежа группы: какие слова должны согласоваться?",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Теперь названное принадлежит другому владельцу. Как сохранить смысл фразы?",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Теперь названное принадлежит другому владельцу. Как сохранить смысл фразы?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.agreement.possessive",
        featureFocus = null,
        fixed = listOf(
            FeatureValueRow(feature = "Case", value = "Acc"),
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Вижу его жену.",
                    target = "Widzę jego żonę.",
                    note = "«его» и jego не склоняются в обоих языках — это готовая форма для любого падежа и рода.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Вижу мою жену.",
                    target = "Widzę moją żonę.",
                    note = "«моя»/moja склоняется по роду и падежу существительного одинаково в обоих языках.",
                    matches = true,
                ),
            ),
            examples = listOf("To jest jej dom.", "Widzę ich dzieci."),
            why = "jego/jej/ich (его/её/их) не склоняются, потому что это застывшие формы местоимения — точно как в русском; а mój/twój/nasz/wasz согласуются с существительным, как «мой/твой/наш/ваш».",
        ),
    ),
    SkillRow(
        id = "verb.present",
        title = "Czas teraźniejszy",
        group = "Czasownik",
        level = SkillRowLevel.A1,
        formula = "лицо × число → окончание",
        theory = "Автоматизируем 6 лиц на высокочастотных глаголах.",
        hint = "ja/ty/on/my/wy/oni",
        prerequisites = emptyList(),
        focus = FocusRow(before = "robić", after = "robię"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Автоматизируем 6 лиц на высокочастотных глаголах.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи лицо и число действующего лица для настоящего времени.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты описываешь действие прямо сейчас. Сначала назови, кто действует, затем выбери форму глагола.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты говоришь, что кто-то делает сейчас. Как назвать действие этого участника?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.verb.tense",
        featureFocus = FeatureFocusRow(feature = "Tense", from = "Past", to = "Pres"),
        fixed = listOf(
            FeatureValueRow(feature = "Person", value = "3"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(nounId = listOf("wife", "husband", "friendM", "son", "child")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Он идёт.",
                    target = "On idzie.",
                    note = "В настоящем времени оба языка спрягают глагол по лицу и числу: 3-е лицо единственного числа — идёт/idzie, окончание показывает лицо одинаково в обоих языках.",
                    matches = true,
                ),
            ),
            examples = listOf("Idzie do szkoły.", "Idą do pracy."),
            why = "Формы настоящего времени различаются по лицу в обоих языках, но польский чаще опускает местоимение — окончание глагола само называет действующее лицо.",
        ),
    ),
    SkillRow(
        id = "verb.past",
        title = "Czas przeszły",
        group = "Czasownik",
        level = SkillRowLevel.A2,
        formula = "основа прошедшего + род + лицо",
        theory = "В прошедшем времени форма отражает род говорящего/подлежащего и лицо.",
        hint = "robił-em, robiła-m, robili-śmy",
        prerequisites = listOf("verb.present"),
        focus = FocusRow(before = "robię", after = "robiłem"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В прошедшем времени форма отражает род говорящего/подлежащего и лицо.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи лицо, число и род для прошедшего времени.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты рассказываешь о действии, которое уже произошло. Как назвать его?",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты рассказываешь о действии, которое уже произошло. Как назвать его?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.verb.tense",
        featureFocus = FeatureFocusRow(feature = "Tense", from = "Pres", to = "Past"),
        fixed = listOf(
            FeatureValueRow(feature = "Person", value = "3"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(nounId = listOf("wife", "husband", "friendM", "son", "child")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Она шла.",
                    target = "Szła.",
                    note = "Прошедшее время согласуется с родом подлежащего в обоих языках — «шла» и szła оба показывают женский род.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Он шёл.",
                    target = "Szedł.",
                    note = "Мужской род прошедшего времени тоже совпадает — «шёл» и szedł; лицо (он) в обоих языках выражено отдельным словом, сам глагол лица не называет.",
                    matches = true,
                ),
            ),
            examples = listOf("Szedł do pracy.", "Szła do szkoły."),
            why = "Прошедшее время в обоих языках зависит от рода — это общая славянская черта; а вот лицо (я/ты/он) в польском показывает сам глагол, а в русском — только отдельное местоимение.",
        ),
    ),
    SkillRow(
        id = "verb.future",
        title = "Czas przyszły",
        group = "Czasownik",
        level = SkillRowLevel.A2,
        formula = "będę + инфинитив (несов.) / форма настоящего (сов.)",
        theory = "Для несовершенного вида: będę robić. Совершенный вид имеет простое будущее: zrobię.",
        hint = "będę czytać / kupię",
        prerequisites = listOf("verb.present"),
        focus = FocusRow(before = "robię", after = "będę robić"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Для несовершенного вида: będę robić. Совершенный вид имеет простое будущее: zrobię.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи лицо и число для будущего времени действия.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь о действии, которое ещё произойдёт. Как назвать его для этого участника?",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты говоришь о действии, которое ещё произойдёт. Как назвать его для этого участника?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.verb.tense",
        featureFocus = FeatureFocusRow(feature = "Tense", from = "Pres", to = "Fut"),
        fixed = listOf(
            FeatureValueRow(feature = "Person", value = "3"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(nounId = listOf("wife", "husband", "friendM", "son", "child")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Буду идти.",
                    target = "Będę iść.",
                    note = "Общая модель будущего несовершенного вида: буду/będę + инфинитив — сохраняется независимо от лица.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Он будет идти.",
                    target = "On będzie iść.",
                    note = "Именно эту модель тренирует упражнение: 3-е лицо, будущее несовершенного вида — będzie + iść.",
                    matches = true,
                ),
            ),
            examples = listOf("Będziesz iść.", "Będziemy iść."),
            why = "У будущего времени в обоих языках два способа: составное для незавершённого действия (буду/będę + инфинитив) и простая форма для завершённого результата (сделаю/zrobię) — структура полностью совпадает.",
        ),
    ),
    SkillRow(
        id = "aspect",
        title = "Aspekt · robić/zrobić",
        group = "Czasownik",
        level = SkillRowLevel.B1,
        formula = "процесс/повторение ↔ завершённый результат",
        theory = "Недоконанный вид описывает процесс/повторяемость, dokonany — завершённый результат.",
        hint = "robiłem vs zrobiłem",
        prerequisites = listOf("verb.past", "verb.future"),
        focus = FocusRow(before = "robiłem", after = "zrobiłem"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Недоконанный вид описывает процесс/повторяемость, dokonany — завершённый результат.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Сравни значение процесса и завершённого результата.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты описываешь не сам процесс покупки, а готовый результат. Выбери завершённое действие.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты хочешь подчеркнуть, что действие завершилось. Как это передать?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.composite",
        featureFocus = null,
        fixed = listOf(
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я делал (не закончил).",
                    target = "Robiłem.",
                    note = "Несовершенный вид описывает процесс без гарантии завершения — одинаково в обоих языках.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Я сделал (готово).",
                    target = "Zrobiłem.",
                    note = "Совершенный вид подчёркивает завершённый результат — то же противопоставление, что в русской паре «сделал» против «делал».",
                    matches = true,
                ),
            ),
            examples = listOf("Czytałem książkę.", "Przeczytałem książkę."),
            why = "Пара несовершенный/совершенный вид (robić/zrobić) — это то же противопоставление процесса и результата, что в русской паре делать/сделать; понятие переносится почти без изменений.",
        ),
    ),
    SkillRow(
        id = "pronouns",
        title = "Zaimki osobowe",
        group = "Zaimki",
        level = SkillRowLevel.A2,
        formula = "ona → ją / jej / nią / niej",
        theory = "Тренируем местоимение сразу внутри конструкции, а не как отдельную таблицу.",
        hint = "widzę ją; mówię o niej; idę z nią",
        prerequisites = listOf("case.inst", "case.loc"),
        focus = FocusRow(before = "moja żona", after = "ją"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Тренируем местоимение сразу внутри конструкции, а не как отдельную таблицу.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи падеж короткого слова, заменяющего названную группу.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Собеседник уже знает, о ком речь. Замени длинное название человека коротким местоимением.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Собеседник уже знает, о ком или о чём речь. Как избежать повторения длинной группы?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = null,
        fixed = listOf(
            FeatureValueRow(feature = "Case", value = "Acc"),
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(nounId = listOf("wife", "husband", "friendM", "son", "child")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Знаю её.",
                    target = "Znam ją.",
                    note = "Личное местоимение склоняется по падежу в обоих языках, хотя конкретные формы разные.",
                    matches = true,
                ),
                NativeParallelRow(
                    native = "Говорю о ней.",
                    target = "Mówię o niej.",
                    note = "После предлога оба языка добавляют дополнительный звук к местоимению: русское «н-» (о ней, с ней) и польское «ni-» (o niej, z nią) появляются в одной и той же позиции.",
                    matches = true,
                ),
            ),
            examples = listOf("Idę z nią.", "Pomagam mu."),
            why = "Местоимения склоняются по падежу в обоих языках; в косвенных падежах после предлогов и русский, и польский вставляют один и тот же дополнительный звук (н-/ni-) — это не совпадение, а общее наследие.",
        ),
    ),
    SkillRow(
        id = "mixed",
        title = "Смешанные трансформации",
        group = "B1 Core",
        level = SkillRowLevel.B1,
        formula = "Лицо + время + отрицание + падеж",
        theory = "Смешанные карточки проверяют, можешь ли ты объединить правила в живом предложении.",
        hint = "Сначала структура, затем окончания.",
        prerequisites = listOf("agreement.my", "verb.past"),
        focus = FocusRow(before = "Widzę moją piękną żonę.", after = "Nie widziałem mojej pięknej żony."),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Смешанные карточки проверяют, можешь ли ты объединить правила в живом предложении.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь время, отрицание и падеж как три независимых условия.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты сообщаешь, что в прошлом не увидел названное. Как удержать все условия?",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты сообщаешь, что в прошлом не увидел названное. Как удержать все условия?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.composite",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Ты не видел мою жену.",
                    target = "Nie widziałeś mojej żony.",
                    note = "В русском отрицание не всегда меняет падеж — винительный «жену» тоже допустим (родительный «жены» звучит книжнее); в польском после nie родительный обязателен всегда, без вариантов.",
                    matches = false,
                ),
            ),
            examples = listOf("Nie robiłem tego wczoraj.", "Nie widziałem jej domu."),
            why = "Смешанное задание складывает несколько правил сразу: время, отрицание и падеж работают как отдельные, независимые условия — проверяй их по одному, а не все сразу.",
        ),
    ),
    SkillRow(
        id = "sentence.question",
        title = "Вопрос с Czy",
        group = "Предложение",
        level = SkillRowLevel.A1,
        formula = "Czy + утверждение → вопрос",
        theory = "В общем вопросе czy ставится перед предложением. Падежи при этом не меняются.",
        hint = "Widzisz… → Czy widzisz…?",
        prerequisites = emptyList(),
        focus = FocusRow(before = "Widzisz moją żonę.", after = "Czy widzisz moją żonę?"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В общем вопросе czy ставится перед предложением. Падежи при этом не меняются.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи, как превратить утверждение в общий вопрос без смены падежа.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты хочешь уточнить, видит ли собеседник названное. Как задать общий вопрос?",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты хочешь уточнить, видит ли собеседник названное. Как задать общий вопрос?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.sentence.mood",
        featureFocus = FeatureFocusRow(feature = "Mood", from = "Decl", to = "YesNoQ"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "2"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Ты видишь мою жену?",
                    target = "Czy widzisz moją żonę?",
                    note = "В русском общий вопрос обычно образуется только интонацией (падежи и порядок слов не меняются); в польском для этого есть отдельное слово czy в начале предложения.",
                    matches = false,
                ),
            ),
            examples = listOf("Czy masz czas?", "Czy on tu jest?"),
            why = "В русском вопрос без вопросительного слова опирается на интонацию (изредка — книжную частицу «ли» после глагола); в польском для этого есть отдельное слово czy, которое всегда ставится в начале предложения.",
        ),
    ),
    SkillRow(
        id = "sentence.plural",
        title = "Единственное → множественное",
        group = "Предложение",
        level = SkillRowLevel.A2,
        formula = "Местоимение + прилагательное + существительное → множественное число",
        theory = "Во множественном числе различай мужскую личную группу и все остальные.",
        hint = "mojego kolegę → moich kolegów; moją książkę → moje książki",
        prerequisites = listOf("case.acc.m", "agreement.my"),
        focus = FocusRow(before = "mojego kolegę", after = "moich kolegów"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Во множественном числе различай мужскую личную группу и все остальные.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Определи число и тип группы перед изменением формы.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Теперь речь не об одном человеке или предмете, а о нескольких. Измени всю группу.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Теперь речь о нескольких таких участниках или предметах. Как назвать всю группу?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном и проследи изменения в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.number",
        featureFocus = FeatureFocusRow(feature = "Number", from = "Sing", to = "Plur"),
        fixed = listOf(
            FeatureValueRow(feature = "Case", value = "Acc"),
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Мои коллеги. Мои книги.",
                    target = "Moi koledzy. Moje książki.",
                    note = "В русском множественное число прилагательного/местоимения одно для всех родов и групп («мои» и там, и там); в польском мужская личная группа (koledzy) получает особую форму (moi), отличную от остальных существительных (moje książki).",
                    matches = false,
                ),
            ),
            examples = listOf("Moi bracia.", "Moje siostry."),
            why = "Главная сложность множественного числа в польском — категория «мужская личная группа» (люди мужского пола), у которой особые формы прилагательных, местоимений и глаголов; в русском такого разделения во множественном числе нет.",
        ),
    ),
    SkillRow(
        id = "degree.comparison",
        title = "Степени сравнения прилагательных",
        group = "Przymiotniki",
        level = SkillRowLevel.B1,
        formula = "Форма сравнительной степени + согласование; при сравнении существительных od + Dopełniacz",
        theory = "Сравнительная степень бывает простой (ciemny → ciemniejszy), нерегулярной (zły → gorszy) и описательной (zajęty → bardziej zajęty). Прилагательное согласуется с существительным. Превосходная степень образуется с naj- от сравнительной: mały → mniejszy → najmniejszy.",
        hint = "Определи тип сравнения и род существительного; затем измени всё предложение.",
        prerequisites = listOf("agreement.my"),
        focus = FocusRow(before = "To drzewo jest wysokie.", after = "To drzewo jest wyższe."),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Определи, какая форма сравнения нужна, и проверь согласование с существительным.",
                promptLead = "Назови форму и собери целое предложение.",
                introduce = "Сначала найди прилагательное, затем реши, нужна простая, описательная или нерегулярная форма.",
                retrieve = "Произнеси или напиши целое предложение, не подсматривая в ответ.",
                feedback = "Сравни форму прилагательного, согласование и остальные слова предложения.",
                review = "Оцени своё воспроизведение; следующий пример потребует нового выбора формы.",
            ),
            situations = MethodRow(
                introduction = "Сравни предметы по качеству и сохрани естественное польское предложение.",
                promptLead = "Представь предмет и скажи целую фразу.",
                introduce = "Представь, как объясняешь другу различие между предметами.",
                retrieve = "Скажи новую фразу так, чтобы собеседник понял сравнение.",
                feedback = "Проверь, передал ли ты нужную степень и согласовал форму.",
                review = "Оцени ответ по смыслу и форме, затем переходи к другой ситуации.",
            ),
        ),
        construction = "core.sentence.degree",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "Для обычного сравнения используй сравнительную форму; для описательного — bardziej + прилагательное. Согласуй окончание с существительным.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Этот мост длиннее того моста.",
                    target = "Ten most jest dłuższy od tamtego mostu.",
                    note = "Польское od вводит второй предмет в родительном падеже: mostu.",
                    matches = true,
                ),
            ),
            examples = listOf("Ten most jest dłuższy od tamtego mostu.", "Ta ulica jest węższa."),
            why = "Простая форма меняет основу или окончание, а описательная добавляет bardziej. Второй предмет после od стоит в Dopełniaczu; при сравнении целых высказываний употребляется niż.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "verb.participles",
        title = "Причастия и деепричастия",
        group = "Czasowniki",
        level = SkillRowLevel.B2,
        formula = "czytający / napisany / czytając: форма зависит от роли и времени действия",
        theory = "Действительное причастие описывает того, кто действует; страдательное — предмет результата. Деепричастие на -ąc обозначает одновременное действие того же субъекта.",
        hint = "Найди общий субъект и назови требуемый тип формы.",
        prerequisites = listOf("verb.past", "aspect"),
        focus = FocusRow(before = "uczeń, który pisze", after = "uczeń piszący"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Определи роль действия: признак деятеля, признак объекта или одновременное действие.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Определи роль действия: признак деятеля, признак объекта или одновременное действие.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Кратко опиши человека или предмет, не теряя связи между действиями.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Кратко опиши человека или предмет, не теряя связи между действиями.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.participle",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "Для деятеля используй форму на -ący, для результата — страдательное причастие; при одном субъекте одновременность передаёт -ąc.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Женщина, читающая газету, ждёт поезд.",
                    target = "Kobieta czytająca gazetę czeka na pociąg.",
                    note = "Действительное причастие согласуется с kobieta; русское «читающая» помогает распознать роль.",
                    matches = true,
                ),
            ),
            examples = listOf("Uczeń piszący list siedzi przy oknie.", "Czytając list, Maria uśmiecha się."),
            why = "В польском причастие согласуется с существительным, а деепричастие не склоняется. Действие на -ąc должно относиться к субъекту главной части.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "voice.impersonal",
        title = "Безличные формы и пассив",
        group = "Czasowniki",
        level = SkillRowLevel.B2,
        formula = "zrobiono + Biernik; объект пассива → Mianownik",
        theory = "Форма на -no/-to сообщает о выполненном действии без названного деятеля и сохраняет дополнение в Bierniku. В пассиве объект становится подлежащим, а причастие согласуется с ним.",
        hint = "Реши, нужно скрыть деятеля или сделать объект подлежащим.",
        prerequisites = listOf("verb.past", "case.inst"),
        focus = FocusRow(before = "Ktoś naprawił drogę.", after = "Naprawiono drogę."),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Сначала выдели деятеля и объект; затем выбери безличную или пассивную конструкцию.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Сначала выдели деятеля и объект; затем выбери безличную или пассивную конструкцию.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Сообщи о выполненной работе, сохранив участников там, где они названы.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Сообщи о выполненной работе, сохранив участников там, где они названы.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.voice",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "В безличной конструкции на -no/-to объект остаётся дополнением. В пассиве он становится подлежащим и управляет согласованием.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Кто-то открыл дверь утром.",
                    target = "Otwarto drzwi rano.",
                    note = "Польская форма -to позволяет не называть исполнителя, как русская неопределённо-личная фраза.",
                    matches = true,
                ),
            ),
            examples = listOf("Otworzono okno o świcie.", "Obraz został namalowany przez Annę."),
            why = "Безличная форма не называет подлежащее; пассив меняет синтаксическую роль объекта. Поэтому падеж и согласование проверяются по-разному.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "wordform.nouns",
        title = "Образование существительных",
        group = "Słowotwórstwo",
        level = SkillRowLevel.B2,
        formula = "czytać → czytanie; czysty → czystość: производная форма меняет структуру фразы",
        theory = "От глагола образуется название действия на -nie/-cie, от прилагательного — название качества. При номинализации дополнение может перейти в родительный падеж.",
        hint = "Найди действие или качество; проверь зависимые слова после нового существительного.",
        prerequisites = listOf("verb.present", "agreement.my"),
        focus = FocusRow(before = "czytam → czynność", after = "czytanie"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Найди исходную часть речи, образуй существительное и перестрой зависимые слова.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Найди исходную часть речи, образуй существительное и перестрой зависимые слова.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Назови действие или свойство одним словом, сохранив смысл всего высказывания.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Назови действие или свойство одним словом, сохранив смысл всего высказывания.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.nominalization",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "Название действия: czytać → czytanie; название качества: czysty → czystość. После названия действия проверяй падеж объекта.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "То, что я читаю книги, радует меня.",
                    target = "Czytanie książek sprawia mi radość.",
                    note = "После польского czytanie объект книги становится książek в родительном падеже.",
                    matches = true,
                ),
            ),
            examples = listOf("Pisanie listów zajmuje czas.", "Czystość pokoju ma znaczenie."),
            why = "Образование существительного меняет не только основу: глагольное дополнение нередко переходит в Dopełniacz, а признак становится самостоятельным именем.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "numeral.agreement",
        title = "Числительные и согласование",
        group = "Liczebniki",
        level = SkillRowLevel.B2,
        formula = "2–4: две/три/четыре формы; 5+: Dopełniacz + сказуемое ср. ед.",
        theory = "Числительные меняют форму существительного и иногда форму сказуемого. В мужском личном множественном числе формы числительного отличаются от неодушевлённых.",
        hint = "Сначала проверь число, затем класс существительного и падеж всей группы.",
        prerequisites = listOf("sentence.plural", "case.gen.neg"),
        focus = FocusRow(before = "dwa + krzesło", after = "dwa krzesła"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Проверь число и тип группы: предметы, женщины или мужчины-люди.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Проверь число и тип группы: предметы, женщины или мужчины-люди.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Пересчитай участников или предметы и скажи полное предложение.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Пересчитай участников или предметы и скажи полное предложение.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.numeral",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "После pięć в подлежащем обычно родительный множественного и сказуемое среднего единственного; после dwa/dwie — другая модель.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "На полке стоят пять кружек.",
                    target = "Na półce stoi pięć kubków.",
                    note = "Русское «стоят» — множественное, польское stoi согласуется с числовой группой иначе.",
                    matches = true,
                ),
            ),
            examples = listOf("W ogrodzie rosną trzy drzewa.", "Przyszło siedmiu gości."),
            why = "Польское сказуемое при группе от пяти часто имеет форму среднего единственного числа, даже если русское сказуемое множественное.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "wordform.adjectives",
        title = "Образование прилагательных",
        group = "Słowotwórstwo",
        level = SkillRowLevel.B2,
        formula = "drewno → drewniany; szkło → szklany; tydzień → tygodniowy",
        theory = "Производное прилагательное называет материал, время или отношение к предмету. Суффикс зависит от основы и значения; новое прилагательное затем согласуется с существительным.",
        hint = "Определи отношение и проверь словарную форму производного прилагательного.",
        prerequisites = listOf("agreement.my"),
        focus = FocusRow(before = "drewno + stół", after = "drewniany stół"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Найди исходное существительное, образуй проверенное прилагательное и согласуй его.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Найди исходное существительное, образуй проверенное прилагательное и согласуй его.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Опиши предмет кратко по материалу, времени или назначению.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Опиши предмет кратко по материалу, времени или назначению.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.denominal",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "Материал и отношение часто передаются производным прилагательным, но суффикс выбирается по конкретному слову; затем проверь род и падеж.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Это деревянное окно.",
                    target = "To jest drewniane okno.",
                    note = "Название материала drewno даёт прилагательное drewniany; форма drewniane согласуется с okno.",
                    matches = true,
                ),
            ),
            examples = listOf("To jest szklany wazon.", "To jest miesięczny raport."),
            why = "Словообразование не сводится к одному механическому суффиксу: drewno → drewniany, szkło → szklany, tydzień → tygodniowy.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "sentence.connectives",
        title = "Союзы и смысловые связи",
        group = "Zdania",
        level = SkillRowLevel.B1,
        formula = "ponieważ — причина; ale — контраст; jeśli — условие; żeby — цель",
        theory = "Союз выбирается по логической связи между частями: причина, противопоставление, условие, цель, уступка или время.",
        hint = "Назови связь и используй указанный союз, сохрани время и смысл.",
        prerequisites = listOf("sentence.question", "mixed"),
        focus = FocusRow(before = "przyczyna + skutek", after = "skutek, ponieważ przyczyna"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Определи отношение частей и поставь подходящую связку.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Определи отношение частей и поставь подходящую связку.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Соедини две мысли так, чтобы собеседник понял причину, контраст, условие, цель, уступку или время.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Соедини две мысли так, чтобы собеседник понял причину, контраст, условие, цель, уступку или время.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.connective",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "Причину вводит ponieważ, условие — jeśli, цель — żeby. Уступка chociaż не равна простому ale.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я остаюсь дома, потому что идёт дождь.",
                    target = "Zostaję w domu, ponieważ pada deszcz.",
                    note = "Связка ponieważ называет причину, как русское «потому что».",
                    matches = true,
                ),
            ),
            examples = listOf("Jeśli będziesz miał czas, przyjdź.", "Chociaż jest późno, czytam dalej."),
            why = "От перестановки или замены союза меняется отношение событий; поэтому сначала нужно понять связь.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "sentence.rephrase",
        title = "Смысловое преобразование фраз",
        group = "Zdania",
        level = SkillRowLevel.B2,
        formula = "Сохрани утверждение, меняя форму и структуру предложения",
        theory = "Перефразирование требует сохранить участников, время, причинность и ограничение, хотя синтаксическая форма меняется.",
        hint = "Найди неизменный смысл; затем примени названную конструкцию.",
        prerequisites = listOf("sentence.connectives"),
        focus = FocusRow(before = "tylko jedna osoba", after = "nikt oprócz tej osoby"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Сверь, кто действует, когда и почему; перестрой фразу по указанию.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Сверь, кто действует, когда и почему; перестрой фразу по указанию.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Скажи ту же мысль иначе, не меняя события или ограничения.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Скажи ту же мысль иначе, не меняя события или ограничения.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.rephrase",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "При перестройке сохрани истинность утверждения: отрицание, причина, время и участники должны остаться теми же.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Только Пётр пришёл.",
                    target = "Nikt oprócz Piotra nie przyszedł.",
                    note = "Русское «только» можно выразить польским nikt oprócz с обязательным отрицанием.",
                    matches = true,
                ),
            ),
            examples = listOf("Mimo śniegu idziemy do szkoły.", "Kiedy przeczytał list, zadzwonił."),
            why = "Новая конструкция меняет грамматику, но не утверждаемое событие; особенно легко потерять ограничение только или временную последовательность.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "case.valency",
        title = "Управление польских глаголов",
        group = "Przypadki",
        level = SkillRowLevel.B2,
        formula = "czekać na + Biernik; słuchać + Dopełniacz; pomagać + Celownik",
        theory = "Каждый глагол задаёт свою рамку дополнения. Сходный русский глагол может требовать другой предлог или падеж.",
        hint = "Переведи мысль с указанным польским глаголом и проверь его рамку.",
        prerequisites = listOf("case.acc.m", "case.gen.neg", "case.dat", "case.inst", "case.loc"),
        focus = FocusRow(before = "ждать + объект", after = "czekać na + Biernik"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Определи польский глагол, затем его предлог и падеж дополнения.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Определи польский глагол, затем его предлог и падеж дополнения.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Скажи собеседнику ту же мысль по-польски, выбрав форму дополнения.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Скажи собеседнику ту же мысль по-польски, выбрав форму дополнения.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.valency",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "Проверяй глагол вместе с управлением: czekać na autobus, słuchać muzyki, pomagać bratu, korzystać z telefonu.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я жду поезд.",
                    target = "Czekam na pociąg.",
                    note = "Русское ждать без предлога; польское czekać na требует na + Biernik.",
                    matches = true,
                ),
            ),
            examples = listOf("Słucham radia.", "Pomagam siostrze."),
            why = "Знание падежа отдельно не подсказывает управление глагола; учи рамку вместе со словом.",
        ),
        caseReferenceAvailable = false,
    ),
    SkillRow(
        id = "verb.mood",
        title = "Условное и повелительное наклонение",
        group = "Czasowniki",
        level = SkillRowLevel.B2,
        formula = "pisałbym / pisałabym; otwórz / nie wychodź / chodźmy",
        theory = "Условное наклонение образуется с -by и личным окончанием на базе прошедшей формы. Повелительное выражает просьбу или побуждение; отрицательная команда часто требует несовершенного вида.",
        hint = "Назови говорящего, род, вид и требуемое намерение.",
        prerequisites = listOf("aspect", "verb.future"),
        focus = FocusRow(before = "piszę", after = "pisałbym / pisałabym"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Определи условность или команду; затем проверь лицо, род и вид.",
                promptLead = "Определи правило и преобразуй предложение.",
                introduce = "Определи условность или команду; затем проверь лицо, род и вид.",
                retrieve = "Скажи целое предложение самостоятельно.",
                feedback = "Сверь смысл, форму и изменённый фрагмент с ответом.",
                review = "Оцени воспроизведение и реши следующий пример заново.",
            ),
            situations = MethodRow(
                introduction = "Передай просьбу, предложение или гипотетическое действие целым предложением.",
                promptLead = "Представь ситуацию и произнеси целое предложение.",
                introduce = "Передай просьбу, предложение или гипотетическое действие целым предложением.",
                retrieve = "Передай мысль целым предложением самостоятельно.",
                feedback = "Сверь, сохранился ли нужный смысл и верная форма.",
                review = "Оцени ответ и переходи к новой ситуации.",
            ),
        ),
        construction = "core.sentence.mood",
        featureFocus = null,
        fixed = emptyList(),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            rule = "В условном: pisałbym (мужчина), pisałabym (женщина). В команде проверь лицо и вид: otwórz, nie wychodź, chodźmy.",
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я бы написал письмо.",
                    target = "Napisałbym list.",
                    note = "Польское -bym присоединяется к мужской форме прошедшего; русское «бы» стоит отдельно.",
                    matches = true,
                ),
            ),
            examples = listOf("Pisałabym list.", "Czy mogłabyś zamknąć okno?"),
            why = "Наклонение меняет отношение говорящего к событию; польская форма дополнительно показывает род и лицо.",
        ),
        caseReferenceAvailable = false,
    ),
)

val SKILL_ROW_ROWS: List<SkillRow> = skillRowRowsPart0()

private fun nounRowRowsPart0(): List<NounRow> = listOf(
    NounRow(
        id = "wife",
        lemma = "żona",
        meaning = "жена",
        gender = "f",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "żona",
                gen = "żony",
                dat = "żonie",
                acc = "żonę",
                inst = "żoną",
                loc = "żonie",
                voc = "żono",
            ),
            pl = CaseFormsRow(
                nom = "żony",
                gen = "żon",
                dat = "żonom",
                acc = "żony",
                inst = "żonami",
                loc = "żonach",
                voc = "żony",
            ),
        ),
    ),
    NounRow(
        id = "woman",
        lemma = "kobieta",
        meaning = "женщина",
        gender = "f",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "kobieta",
                gen = "kobiety",
                dat = "kobiecie",
                acc = "kobietę",
                inst = "kobietą",
                loc = "kobiecie",
                voc = "kobieto",
            ),
            pl = CaseFormsRow(
                nom = "kobiety",
                gen = "kobiet",
                dat = "kobietom",
                acc = "kobiety",
                inst = "kobietami",
                loc = "kobietach",
                voc = "kobiety",
            ),
        ),
    ),
    NounRow(
        id = "book",
        lemma = "książka",
        meaning = "книга",
        gender = "f",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "książka",
                gen = "książki",
                dat = "książce",
                acc = "książkę",
                inst = "książką",
                loc = "książce",
                voc = "książko",
            ),
            pl = CaseFormsRow(
                nom = "książki",
                gen = "książek",
                dat = "książkom",
                acc = "książki",
                inst = "książkami",
                loc = "książkach",
                voc = "książki",
            ),
        ),
    ),
    NounRow(
        id = "work",
        lemma = "praca",
        meaning = "работа",
        gender = "f",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "praca",
                gen = "pracy",
                dat = "pracy",
                acc = "pracę",
                inst = "pracą",
                loc = "pracy",
                voc = "praco",
            ),
            pl = CaseFormsRow(
                nom = "prace",
                gen = "prac",
                dat = "pracom",
                acc = "prace",
                inst = "pracami",
                loc = "pracach",
                voc = "prace",
            ),
        ),
    ),
    NounRow(
        id = "husband",
        lemma = "mąż",
        meaning = "муж",
        gender = "m-personal",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "mąż",
                gen = "męża",
                dat = "mężowi",
                acc = "męża",
                inst = "mężem",
                loc = "mężu",
                voc = "mężu",
            ),
            pl = CaseFormsRow(
                nom = "mężowie",
                gen = "mężów",
                dat = "mężom",
                acc = "mężów",
                inst = "mężami",
                loc = "mężach",
                voc = "mężowie",
            ),
        ),
    ),
    NounRow(
        id = "friendM",
        lemma = "kolega",
        meaning = "коллега",
        gender = "m-personal",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "kolega",
                gen = "kolegi",
                dat = "koledze",
                acc = "kolegę",
                inst = "kolegą",
                loc = "koledze",
                voc = "kolego",
            ),
            pl = CaseFormsRow(
                nom = "koledzy",
                gen = "kolegów",
                dat = "kolegom",
                acc = "kolegów",
                inst = "kolegami",
                loc = "kolegach",
                voc = "koledzy",
            ),
        ),
    ),
    NounRow(
        id = "son",
        lemma = "syn",
        meaning = "сын",
        gender = "m-personal",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "syn",
                gen = "syna",
                dat = "synowi",
                acc = "syna",
                inst = "synem",
                loc = "synu",
                voc = "synu",
            ),
            pl = CaseFormsRow(
                nom = "synowie",
                gen = "synów",
                dat = "synom",
                acc = "synów",
                inst = "synami",
                loc = "synach",
                voc = "synowie",
            ),
        ),
    ),
    NounRow(
        id = "dog",
        lemma = "pies",
        meaning = "собака",
        gender = "m-animate",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "pies",
                gen = "psa",
                dat = "psu",
                acc = "psa",
                inst = "psem",
                loc = "psie",
                voc = "psie",
            ),
            pl = CaseFormsRow(
                nom = "psy",
                gen = "psów",
                dat = "psom",
                acc = "psy",
                inst = "psami",
                loc = "psach",
                voc = "psy",
            ),
        ),
    ),
    NounRow(
        id = "cat",
        lemma = "kot",
        meaning = "кот",
        gender = "m-animate",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "kot",
                gen = "kota",
                dat = "kotu",
                acc = "kota",
                inst = "kotem",
                loc = "kocie",
                voc = "kocie",
            ),
            pl = CaseFormsRow(
                nom = "koty",
                gen = "kotów",
                dat = "kotom",
                acc = "koty",
                inst = "kotami",
                loc = "kotach",
                voc = "koty",
            ),
        ),
    ),
    NounRow(
        id = "car",
        lemma = "samochód",
        meaning = "машина",
        gender = "m-inanimate",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "samochód",
                gen = "samochodu",
                dat = "samochodowi",
                acc = "samochód",
                inst = "samochodem",
                loc = "samochodzie",
                voc = "samochodzie",
            ),
            pl = CaseFormsRow(
                nom = "samochody",
                gen = "samochodów",
                dat = "samochodom",
                acc = "samochody",
                inst = "samochodami",
                loc = "samochodach",
                voc = "samochody",
            ),
        ),
    ),
    NounRow(
        id = "house",
        lemma = "dom",
        meaning = "дом",
        gender = "m-inanimate",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "dom",
                gen = "domu",
                dat = "domowi",
                acc = "dom",
                inst = "domem",
                loc = "domu",
                voc = "domu",
            ),
            pl = CaseFormsRow(
                nom = "domy",
                gen = "domów",
                dat = "domom",
                acc = "domy",
                inst = "domami",
                loc = "domach",
                voc = "domy",
            ),
        ),
    ),
    NounRow(
        id = "phone",
        lemma = "telefon",
        meaning = "телефон",
        gender = "m-inanimate",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "telefon",
                gen = "telefonu",
                dat = "telefonowi",
                acc = "telefon",
                inst = "telefonem",
                loc = "telefonie",
                voc = "telefonie",
            ),
            pl = CaseFormsRow(
                nom = "telefony",
                gen = "telefonów",
                dat = "telefonom",
                acc = "telefony",
                inst = "telefonami",
                loc = "telefonach",
                voc = "telefony",
            ),
        ),
    ),
    NounRow(
        id = "child",
        lemma = "dziecko",
        meaning = "ребёнок",
        gender = "n",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "dziecko",
                gen = "dziecka",
                dat = "dziecku",
                acc = "dziecko",
                inst = "dzieckiem",
                loc = "dziecku",
                voc = "dziecko",
            ),
            pl = CaseFormsRow(
                nom = "dzieci",
                gen = "dzieci",
                dat = "dzieciom",
                acc = "dzieci",
                inst = "dziećmi",
                loc = "dzieciach",
                voc = "dzieci",
            ),
        ),
    ),
    NounRow(
        id = "window",
        lemma = "okno",
        meaning = "окно",
        gender = "n",
        forms = NounFormsRow(
            sg = CaseFormsRow(
                nom = "okno",
                gen = "okna",
                dat = "oknu",
                acc = "okno",
                inst = "oknem",
                loc = "oknie",
                voc = "okno",
            ),
            pl = CaseFormsRow(
                nom = "okna",
                gen = "okien",
                dat = "oknom",
                acc = "okna",
                inst = "oknami",
                loc = "oknach",
                voc = "okna",
            ),
        ),
    ),
)

val NOUN_ROW_ROWS: List<NounRow> = nounRowRowsPart0()

private fun adjectiveRowRowsPart0(): List<AdjectiveRow> = listOf(
    AdjectiveRow(
        id = "beautiful",
        lemma = "piękny",
        meaning = "красивый",
        forms = AdjectiveFormsRow(
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "piękny",
                    gen = "pięknego",
                    dat = "pięknemu",
                    acc = "pięknego",
                    inst = "pięknym",
                    loc = "pięknym",
                    voc = "piękny",
                ),
                m_animate = CaseFormsRow(
                    nom = "piękny",
                    gen = "pięknego",
                    dat = "pięknemu",
                    acc = "pięknego",
                    inst = "pięknym",
                    loc = "pięknym",
                    voc = "piękny",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "piękny",
                    gen = "pięknego",
                    dat = "pięknemu",
                    acc = "piękny",
                    inst = "pięknym",
                    loc = "pięknym",
                    voc = "piękny",
                ),
                f = CaseFormsRow(
                    nom = "piękna",
                    gen = "pięknej",
                    dat = "pięknej",
                    acc = "piękną",
                    inst = "piękną",
                    loc = "pięknej",
                    voc = "piękna",
                ),
                n = CaseFormsRow(
                    nom = "piękne",
                    gen = "pięknego",
                    dat = "pięknemu",
                    acc = "piękne",
                    inst = "pięknym",
                    loc = "pięknym",
                    voc = "piękne",
                ),
            ),
            pl = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "piękni",
                    gen = "pięknych",
                    dat = "pięknym",
                    acc = "pięknych",
                    inst = "pięknymi",
                    loc = "pięknych",
                    voc = "piękni",
                ),
                m_animate = CaseFormsRow(
                    nom = "piękne",
                    gen = "pięknych",
                    dat = "pięknym",
                    acc = "piękne",
                    inst = "pięknymi",
                    loc = "pięknych",
                    voc = "piękne",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "piękne",
                    gen = "pięknych",
                    dat = "pięknym",
                    acc = "piękne",
                    inst = "pięknymi",
                    loc = "pięknych",
                    voc = "piękne",
                ),
                f = CaseFormsRow(
                    nom = "piękne",
                    gen = "pięknych",
                    dat = "pięknym",
                    acc = "piękne",
                    inst = "pięknymi",
                    loc = "pięknych",
                    voc = "piękne",
                ),
                n = CaseFormsRow(
                    nom = "piękne",
                    gen = "pięknych",
                    dat = "pięknym",
                    acc = "piękne",
                    inst = "pięknymi",
                    loc = "pięknych",
                    voc = "piękne",
                ),
            ),
        ),
    ),
    AdjectiveRow(
        id = "new",
        lemma = "nowy",
        meaning = "новый",
        forms = AdjectiveFormsRow(
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "nowy",
                    gen = "nowego",
                    dat = "nowemu",
                    acc = "nowego",
                    inst = "nowym",
                    loc = "nowym",
                    voc = "nowy",
                ),
                m_animate = CaseFormsRow(
                    nom = "nowy",
                    gen = "nowego",
                    dat = "nowemu",
                    acc = "nowego",
                    inst = "nowym",
                    loc = "nowym",
                    voc = "nowy",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "nowy",
                    gen = "nowego",
                    dat = "nowemu",
                    acc = "nowy",
                    inst = "nowym",
                    loc = "nowym",
                    voc = "nowy",
                ),
                f = CaseFormsRow(
                    nom = "nowa",
                    gen = "nowej",
                    dat = "nowej",
                    acc = "nową",
                    inst = "nową",
                    loc = "nowej",
                    voc = "nowa",
                ),
                n = CaseFormsRow(
                    nom = "nowe",
                    gen = "nowego",
                    dat = "nowemu",
                    acc = "nowe",
                    inst = "nowym",
                    loc = "nowym",
                    voc = "nowe",
                ),
            ),
            pl = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "nowi",
                    gen = "nowych",
                    dat = "nowym",
                    acc = "nowych",
                    inst = "nowymi",
                    loc = "nowych",
                    voc = "nowi",
                ),
                m_animate = CaseFormsRow(
                    nom = "nowe",
                    gen = "nowych",
                    dat = "nowym",
                    acc = "nowe",
                    inst = "nowymi",
                    loc = "nowych",
                    voc = "nowe",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "nowe",
                    gen = "nowych",
                    dat = "nowym",
                    acc = "nowe",
                    inst = "nowymi",
                    loc = "nowych",
                    voc = "nowe",
                ),
                f = CaseFormsRow(
                    nom = "nowe",
                    gen = "nowych",
                    dat = "nowym",
                    acc = "nowe",
                    inst = "nowymi",
                    loc = "nowych",
                    voc = "nowe",
                ),
                n = CaseFormsRow(
                    nom = "nowe",
                    gen = "nowych",
                    dat = "nowym",
                    acc = "nowe",
                    inst = "nowymi",
                    loc = "nowych",
                    voc = "nowe",
                ),
            ),
        ),
    ),
    AdjectiveRow(
        id = "small",
        lemma = "mały",
        meaning = "маленький",
        forms = AdjectiveFormsRow(
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "mały",
                    gen = "małego",
                    dat = "małemu",
                    acc = "małego",
                    inst = "małym",
                    loc = "małym",
                    voc = "mały",
                ),
                m_animate = CaseFormsRow(
                    nom = "mały",
                    gen = "małego",
                    dat = "małemu",
                    acc = "małego",
                    inst = "małym",
                    loc = "małym",
                    voc = "mały",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "mały",
                    gen = "małego",
                    dat = "małemu",
                    acc = "mały",
                    inst = "małym",
                    loc = "małym",
                    voc = "mały",
                ),
                f = CaseFormsRow(
                    nom = "mała",
                    gen = "małej",
                    dat = "małej",
                    acc = "małą",
                    inst = "małą",
                    loc = "małej",
                    voc = "mała",
                ),
                n = CaseFormsRow(
                    nom = "małe",
                    gen = "małego",
                    dat = "małemu",
                    acc = "małe",
                    inst = "małym",
                    loc = "małym",
                    voc = "małe",
                ),
            ),
            pl = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "mali",
                    gen = "małych",
                    dat = "małym",
                    acc = "małych",
                    inst = "małymi",
                    loc = "małych",
                    voc = "mali",
                ),
                m_animate = CaseFormsRow(
                    nom = "małe",
                    gen = "małych",
                    dat = "małym",
                    acc = "małe",
                    inst = "małymi",
                    loc = "małych",
                    voc = "małe",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "małe",
                    gen = "małych",
                    dat = "małym",
                    acc = "małe",
                    inst = "małymi",
                    loc = "małych",
                    voc = "małe",
                ),
                f = CaseFormsRow(
                    nom = "małe",
                    gen = "małych",
                    dat = "małym",
                    acc = "małe",
                    inst = "małymi",
                    loc = "małych",
                    voc = "małe",
                ),
                n = CaseFormsRow(
                    nom = "małe",
                    gen = "małych",
                    dat = "małym",
                    acc = "małe",
                    inst = "małymi",
                    loc = "małych",
                    voc = "małe",
                ),
            ),
        ),
    ),
    AdjectiveRow(
        id = "nice",
        lemma = "ładny",
        meaning = "красивый",
        forms = AdjectiveFormsRow(
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "ładny",
                    gen = "ładnego",
                    dat = "ładnemu",
                    acc = "ładnego",
                    inst = "ładnym",
                    loc = "ładnym",
                    voc = "ładny",
                ),
                m_animate = CaseFormsRow(
                    nom = "ładny",
                    gen = "ładnego",
                    dat = "ładnemu",
                    acc = "ładnego",
                    inst = "ładnym",
                    loc = "ładnym",
                    voc = "ładny",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "ładny",
                    gen = "ładnego",
                    dat = "ładnemu",
                    acc = "ładny",
                    inst = "ładnym",
                    loc = "ładnym",
                    voc = "ładny",
                ),
                f = CaseFormsRow(
                    nom = "ładna",
                    gen = "ładnej",
                    dat = "ładnej",
                    acc = "ładną",
                    inst = "ładną",
                    loc = "ładnej",
                    voc = "ładna",
                ),
                n = CaseFormsRow(
                    nom = "ładne",
                    gen = "ładnego",
                    dat = "ładnemu",
                    acc = "ładne",
                    inst = "ładnym",
                    loc = "ładnym",
                    voc = "ładne",
                ),
            ),
            pl = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "ładni",
                    gen = "ładnych",
                    dat = "ładnym",
                    acc = "ładnych",
                    inst = "ładnymi",
                    loc = "ładnych",
                    voc = "ładni",
                ),
                m_animate = CaseFormsRow(
                    nom = "ładne",
                    gen = "ładnych",
                    dat = "ładnym",
                    acc = "ładne",
                    inst = "ładnymi",
                    loc = "ładnych",
                    voc = "ładne",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "ładne",
                    gen = "ładnych",
                    dat = "ładnym",
                    acc = "ładne",
                    inst = "ładnymi",
                    loc = "ładnych",
                    voc = "ładne",
                ),
                f = CaseFormsRow(
                    nom = "ładne",
                    gen = "ładnych",
                    dat = "ładnym",
                    acc = "ładne",
                    inst = "ładnymi",
                    loc = "ładnych",
                    voc = "ładne",
                ),
                n = CaseFormsRow(
                    nom = "ładne",
                    gen = "ładnych",
                    dat = "ładnym",
                    acc = "ładne",
                    inst = "ładnymi",
                    loc = "ładnych",
                    voc = "ładne",
                ),
            ),
        ),
    ),
    AdjectiveRow(
        id = "good",
        lemma = "dobry",
        meaning = "хороший",
        forms = AdjectiveFormsRow(
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "dobry",
                    gen = "dobrego",
                    dat = "dobremu",
                    acc = "dobrego",
                    inst = "dobrym",
                    loc = "dobrym",
                    voc = "dobry",
                ),
                m_animate = CaseFormsRow(
                    nom = "dobry",
                    gen = "dobrego",
                    dat = "dobremu",
                    acc = "dobrego",
                    inst = "dobrym",
                    loc = "dobrym",
                    voc = "dobry",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "dobry",
                    gen = "dobrego",
                    dat = "dobremu",
                    acc = "dobry",
                    inst = "dobrym",
                    loc = "dobrym",
                    voc = "dobry",
                ),
                f = CaseFormsRow(
                    nom = "dobra",
                    gen = "dobrej",
                    dat = "dobrej",
                    acc = "dobrą",
                    inst = "dobrą",
                    loc = "dobrej",
                    voc = "dobra",
                ),
                n = CaseFormsRow(
                    nom = "dobre",
                    gen = "dobrego",
                    dat = "dobremu",
                    acc = "dobre",
                    inst = "dobrym",
                    loc = "dobrym",
                    voc = "dobre",
                ),
            ),
            pl = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "dobrzy",
                    gen = "dobrych",
                    dat = "dobrym",
                    acc = "dobrych",
                    inst = "dobrymi",
                    loc = "dobrych",
                    voc = "dobrzy",
                ),
                m_animate = CaseFormsRow(
                    nom = "dobre",
                    gen = "dobrych",
                    dat = "dobrym",
                    acc = "dobre",
                    inst = "dobrymi",
                    loc = "dobrych",
                    voc = "dobre",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "dobre",
                    gen = "dobrych",
                    dat = "dobrym",
                    acc = "dobre",
                    inst = "dobrymi",
                    loc = "dobrych",
                    voc = "dobre",
                ),
                f = CaseFormsRow(
                    nom = "dobre",
                    gen = "dobrych",
                    dat = "dobrym",
                    acc = "dobre",
                    inst = "dobrymi",
                    loc = "dobrych",
                    voc = "dobre",
                ),
                n = CaseFormsRow(
                    nom = "dobre",
                    gen = "dobrych",
                    dat = "dobrym",
                    acc = "dobre",
                    inst = "dobrymi",
                    loc = "dobrych",
                    voc = "dobre",
                ),
            ),
        ),
    ),
    AdjectiveRow(
        id = "old",
        lemma = "stary",
        meaning = "старый",
        forms = AdjectiveFormsRow(
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "stary",
                    gen = "starego",
                    dat = "staremu",
                    acc = "starego",
                    inst = "starym",
                    loc = "starym",
                    voc = "stary",
                ),
                m_animate = CaseFormsRow(
                    nom = "stary",
                    gen = "starego",
                    dat = "staremu",
                    acc = "starego",
                    inst = "starym",
                    loc = "starym",
                    voc = "stary",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "stary",
                    gen = "starego",
                    dat = "staremu",
                    acc = "stary",
                    inst = "starym",
                    loc = "starym",
                    voc = "stary",
                ),
                f = CaseFormsRow(
                    nom = "stara",
                    gen = "starej",
                    dat = "starej",
                    acc = "starą",
                    inst = "starą",
                    loc = "starej",
                    voc = "stara",
                ),
                n = CaseFormsRow(
                    nom = "stare",
                    gen = "starego",
                    dat = "staremu",
                    acc = "stare",
                    inst = "starym",
                    loc = "starym",
                    voc = "stare",
                ),
            ),
            pl = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "starzy",
                    gen = "starych",
                    dat = "starym",
                    acc = "starych",
                    inst = "starymi",
                    loc = "starych",
                    voc = "starzy",
                ),
                m_animate = CaseFormsRow(
                    nom = "stare",
                    gen = "starych",
                    dat = "starym",
                    acc = "stare",
                    inst = "starymi",
                    loc = "starych",
                    voc = "stare",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "stare",
                    gen = "starych",
                    dat = "starym",
                    acc = "stare",
                    inst = "starymi",
                    loc = "starych",
                    voc = "stare",
                ),
                f = CaseFormsRow(
                    nom = "stare",
                    gen = "starych",
                    dat = "starym",
                    acc = "stare",
                    inst = "starymi",
                    loc = "starych",
                    voc = "stare",
                ),
                n = CaseFormsRow(
                    nom = "stare",
                    gen = "starych",
                    dat = "starym",
                    acc = "stare",
                    inst = "starymi",
                    loc = "starych",
                    voc = "stare",
                ),
            ),
        ),
    ),
    AdjectiveRow(
        id = "expensive",
        lemma = "drogi",
        meaning = "дорогой",
        forms = AdjectiveFormsRow(
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "drogi",
                    gen = "drogiego",
                    dat = "drogiemu",
                    acc = "drogiego",
                    inst = "drogim",
                    loc = "drogim",
                    voc = "drogi",
                ),
                m_animate = CaseFormsRow(
                    nom = "drogi",
                    gen = "drogiego",
                    dat = "drogiemu",
                    acc = "drogiego",
                    inst = "drogim",
                    loc = "drogim",
                    voc = "drogi",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "drogi",
                    gen = "drogiego",
                    dat = "drogiemu",
                    acc = "drogi",
                    inst = "drogim",
                    loc = "drogim",
                    voc = "drogi",
                ),
                f = CaseFormsRow(
                    nom = "droga",
                    gen = "drogiej",
                    dat = "drogiej",
                    acc = "drogą",
                    inst = "drogą",
                    loc = "drogiej",
                    voc = "droga",
                ),
                n = CaseFormsRow(
                    nom = "drogie",
                    gen = "drogiego",
                    dat = "drogiemu",
                    acc = "drogie",
                    inst = "drogim",
                    loc = "drogim",
                    voc = "drogie",
                ),
            ),
            pl = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "drodzy",
                    gen = "drogich",
                    dat = "drogim",
                    acc = "drogich",
                    inst = "drogimi",
                    loc = "drogich",
                    voc = "drodzy",
                ),
                m_animate = CaseFormsRow(
                    nom = "drogie",
                    gen = "drogich",
                    dat = "drogim",
                    acc = "drogie",
                    inst = "drogimi",
                    loc = "drogich",
                    voc = "drogie",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "drogie",
                    gen = "drogich",
                    dat = "drogim",
                    acc = "drogie",
                    inst = "drogimi",
                    loc = "drogich",
                    voc = "drogie",
                ),
                f = CaseFormsRow(
                    nom = "drogie",
                    gen = "drogich",
                    dat = "drogim",
                    acc = "drogie",
                    inst = "drogimi",
                    loc = "drogich",
                    voc = "drogie",
                ),
                n = CaseFormsRow(
                    nom = "drogie",
                    gen = "drogich",
                    dat = "drogim",
                    acc = "drogie",
                    inst = "drogimi",
                    loc = "drogich",
                    voc = "drogie",
                ),
            ),
        ),
    ),
)

val ADJECTIVE_ROW_ROWS: List<AdjectiveRow> = adjectiveRowRowsPart0()

private fun verbRowRowsPart0(): List<VerbRow> = listOf(
    VerbRow(
        id = "have",
        lemma = "mieć",
        meaning = "иметь",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "mam", p2 = "masz", p3 = "ma"),
            pl = PersonFormsRow(p1 = "mamy", p2 = "macie", p3 = "mają"),
        ),
        pastStem = PastStemRow(m = "miał", f = "miała", n = "miało", mp = "mieli", np = "miały"),
    ),
    VerbRow(
        id = "see",
        lemma = "widzieć",
        meaning = "видеть",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "widzę", p2 = "widzisz", p3 = "widzi"),
            pl = PersonFormsRow(p1 = "widzimy", p2 = "widzicie", p3 = "widzą"),
        ),
        pastStem = PastStemRow(m = "widział", f = "widziała", n = "widziało", mp = "widzieli", np = "widziały"),
    ),
    VerbRow(
        id = "like",
        lemma = "lubić",
        meaning = "любить",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "lubię", p2 = "lubisz", p3 = "lubi"),
            pl = PersonFormsRow(p1 = "lubimy", p2 = "lubicie", p3 = "lubią"),
        ),
        pastStem = PastStemRow(m = "lubił", f = "lubiła", n = "lubiło", mp = "lubili", np = "lubiły"),
    ),
    VerbRow(
        id = "buy",
        lemma = "kupować",
        meaning = "покупать",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        perfectivePair = "buyDone",
        present = PresentRow(
            sg = PersonFormsRow(p1 = "kupuję", p2 = "kupujesz", p3 = "kupuje"),
            pl = PersonFormsRow(p1 = "kupujemy", p2 = "kupujecie", p3 = "kupują"),
        ),
        pastStem = PastStemRow(m = "kupował", f = "kupowała", n = "kupowało", mp = "kupowali", np = "kupowały"),
    ),
    VerbRow(
        id = "read",
        lemma = "czytać",
        meaning = "читать",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "czytam", p2 = "czytasz", p3 = "czyta"),
            pl = PersonFormsRow(p1 = "czytamy", p2 = "czytacie", p3 = "czytają"),
        ),
        pastStem = PastStemRow(m = "czytał", f = "czytała", n = "czytało", mp = "czytali", np = "czytały"),
    ),
    VerbRow(
        id = "talk",
        lemma = "mówić",
        meaning = "говорить",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "mówię", p2 = "mówisz", p3 = "mówi"),
            pl = PersonFormsRow(p1 = "mówimy", p2 = "mówicie", p3 = "mówią"),
        ),
        pastStem = PastStemRow(m = "mówił", f = "mówiła", n = "mówiło", mp = "mówili", np = "mówiły"),
    ),
    VerbRow(
        id = "go",
        lemma = "iść",
        meaning = "идти",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "idę", p2 = "idziesz", p3 = "idzie"),
            pl = PersonFormsRow(p1 = "idziemy", p2 = "idziecie", p3 = "idą"),
        ),
        pastStem = PastStemRow(m = "szedł", f = "szła", n = "szło", mp = "szli", np = "szły"),
    ),
    VerbRow(
        id = "be",
        lemma = "być",
        meaning = "быть",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "jestem", p2 = "jesteś", p3 = "jest"),
            pl = PersonFormsRow(p1 = "jesteśmy", p2 = "jesteście", p3 = "są"),
        ),
        pastStem = PastStemRow(m = "był", f = "była", n = "było", mp = "byli", np = "były"),
    ),
    VerbRow(
        id = "buyDone",
        lemma = "kupić",
        meaning = "купить",
        aspect = VerbRowAspect.PERFECTIVE,
        futureType = VerbRowFutureType.PRESENT,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "kupię", p2 = "kupisz", p3 = "kupi"),
            pl = PersonFormsRow(p1 = "kupimy", p2 = "kupicie", p3 = "kupią"),
        ),
        pastStem = PastStemRow(m = "kupił", f = "kupiła", n = "kupiło", mp = "kupili", np = "kupiły"),
    ),
    VerbRow(
        id = "do",
        lemma = "robić",
        meaning = "делать",
        aspect = VerbRowAspect.IMPERFECTIVE,
        futureType = VerbRowFutureType.COMPOUND,
        perfectivePair = "doDone",
        present = PresentRow(
            sg = PersonFormsRow(p1 = "robię", p2 = "robisz", p3 = "robi"),
            pl = PersonFormsRow(p1 = "robimy", p2 = "robicie", p3 = "robią"),
        ),
        pastStem = PastStemRow(m = "robił", f = "robiła", n = "robiło", mp = "robili", np = "robiły"),
    ),
    VerbRow(
        id = "doDone",
        lemma = "zrobić",
        meaning = "сделать",
        aspect = VerbRowAspect.PERFECTIVE,
        futureType = VerbRowFutureType.PRESENT,
        present = PresentRow(
            sg = PersonFormsRow(p1 = "zrobię", p2 = "zrobisz", p3 = "zrobi"),
            pl = PersonFormsRow(p1 = "zrobimy", p2 = "zrobicie", p3 = "zrobią"),
        ),
        pastStem = PastStemRow(m = "zrobił", f = "zrobiła", n = "zrobiło", mp = "zrobili", np = "zrobiły"),
    ),
)

val VERB_ROW_ROWS: List<VerbRow> = verbRowRowsPart0()

private fun pronounRowRowsPart0(): List<PronounRow> = listOf(
    PronounRow(
        id = "ja",
        forms = CaseFormsRow(
            nom = "ja",
            gen = "mnie",
            dat = "mi",
            acc = "mnie",
            inst = "mną",
            loc = "mnie",
            voc = "ja",
        ),
    ),
    PronounRow(
        id = "ty",
        forms = CaseFormsRow(
            nom = "ty",
            gen = "ciebie",
            dat = "ci",
            acc = "ciebie",
            inst = "tobą",
            loc = "tobie",
            voc = "ty",
        ),
    ),
    PronounRow(
        id = "on",
        forms = CaseFormsRow(nom = "on", gen = "go", dat = "mu", acc = "go", inst = "nim", loc = "nim", voc = "on"),
    ),
    PronounRow(
        id = "ona",
        forms = CaseFormsRow(
            nom = "ona",
            gen = "jej",
            dat = "jej",
            acc = "ją",
            inst = "nią",
            loc = "niej",
            voc = "ona",
        ),
    ),
    PronounRow(
        id = "ono",
        forms = CaseFormsRow(nom = "ono", gen = "go", dat = "mu", acc = "je", inst = "nim", loc = "nim", voc = "ono"),
    ),
    PronounRow(
        id = "my",
        forms = CaseFormsRow(nom = "my", gen = "nas", dat = "nam", acc = "nas", inst = "nami", loc = "nas", voc = "my"),
    ),
    PronounRow(
        id = "wy",
        forms = CaseFormsRow(nom = "wy", gen = "was", dat = "wam", acc = "was", inst = "wami", loc = "was", voc = "wy"),
    ),
    PronounRow(
        id = "oni",
        forms = CaseFormsRow(
            nom = "oni",
            gen = "ich",
            dat = "im",
            acc = "ich",
            inst = "nimi",
            loc = "nich",
            voc = "oni",
        ),
    ),
    PronounRow(
        id = "one",
        forms = CaseFormsRow(
            nom = "one",
            gen = "ich",
            dat = "im",
            acc = "je",
            inst = "nimi",
            loc = "nich",
            voc = "one",
        ),
    ),
)

val PRONOUN_ROW_ROWS: List<PronounRow> = pronounRowRowsPart0()

private fun possessiveRowRowsPart0(): List<PossessiveRow> = listOf(
    PossessiveRow(
        id = "my",
        label = "mój — мой",
        forms = PossessiveFormsRow(
            kind = PossessiveFormsRowKind.DECLINED,
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "mój",
                    gen = "mojego",
                    dat = "mojemu",
                    acc = "mojego",
                    inst = "moim",
                    loc = "moim",
                    voc = "mój",
                ),
                m_animate = CaseFormsRow(
                    nom = "mój",
                    gen = "mojego",
                    dat = "mojemu",
                    acc = "mojego",
                    inst = "moim",
                    loc = "moim",
                    voc = "mój",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "mój",
                    gen = "mojego",
                    dat = "mojemu",
                    acc = "mój",
                    inst = "moim",
                    loc = "moim",
                    voc = "mój",
                ),
                f = CaseFormsRow(
                    nom = "moja",
                    gen = "mojej",
                    dat = "mojej",
                    acc = "moją",
                    inst = "moją",
                    loc = "mojej",
                    voc = "moja",
                ),
                n = CaseFormsRow(
                    nom = "moje",
                    gen = "mojego",
                    dat = "mojemu",
                    acc = "moje",
                    inst = "moim",
                    loc = "moim",
                    voc = "moje",
                ),
            ),
            pl = PossessivePluralRow(
                m_personal = CaseFormsRow(
                    nom = "moi",
                    gen = "moich",
                    dat = "moim",
                    acc = "moich",
                    inst = "moimi",
                    loc = "moich",
                    voc = "moi",
                ),
                other = CaseFormsRow(
                    nom = "moje",
                    gen = "moich",
                    dat = "moim",
                    acc = "moje",
                    inst = "moimi",
                    loc = "moich",
                    voc = "moje",
                ),
            ),
        ),
    ),
    PossessiveRow(
        id = "your",
        label = "twój — твой",
        forms = PossessiveFormsRow(
            kind = PossessiveFormsRowKind.DECLINED,
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "twój",
                    gen = "twojego",
                    dat = "twojemu",
                    acc = "twojego",
                    inst = "twoim",
                    loc = "twoim",
                    voc = "twój",
                ),
                m_animate = CaseFormsRow(
                    nom = "twój",
                    gen = "twojego",
                    dat = "twojemu",
                    acc = "twojego",
                    inst = "twoim",
                    loc = "twoim",
                    voc = "twój",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "twój",
                    gen = "twojego",
                    dat = "twojemu",
                    acc = "twój",
                    inst = "twoim",
                    loc = "twoim",
                    voc = "twój",
                ),
                f = CaseFormsRow(
                    nom = "twoja",
                    gen = "twojej",
                    dat = "twojej",
                    acc = "twoją",
                    inst = "twoją",
                    loc = "twojej",
                    voc = "twoja",
                ),
                n = CaseFormsRow(
                    nom = "twoje",
                    gen = "twojego",
                    dat = "twojemu",
                    acc = "twoje",
                    inst = "twoim",
                    loc = "twoim",
                    voc = "twoje",
                ),
            ),
            pl = PossessivePluralRow(
                m_personal = CaseFormsRow(
                    nom = "twoi",
                    gen = "twoich",
                    dat = "twoim",
                    acc = "twoich",
                    inst = "twoimi",
                    loc = "twoich",
                    voc = "twoi",
                ),
                other = CaseFormsRow(
                    nom = "twoje",
                    gen = "twoich",
                    dat = "twoim",
                    acc = "twoje",
                    inst = "twoimi",
                    loc = "twoich",
                    voc = "twoje",
                ),
            ),
        ),
    ),
    PossessiveRow(
        id = "his",
        label = "jego — его",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "jego"),
    ),
    PossessiveRow(
        id = "her",
        label = "jej — её",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "jej"),
    ),
    PossessiveRow(
        id = "our",
        label = "nasz — наш",
        forms = PossessiveFormsRow(
            kind = PossessiveFormsRowKind.DECLINED,
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "nasz",
                    gen = "naszego",
                    dat = "naszemu",
                    acc = "naszego",
                    inst = "naszym",
                    loc = "naszym",
                    voc = "nasz",
                ),
                m_animate = CaseFormsRow(
                    nom = "nasz",
                    gen = "naszego",
                    dat = "naszemu",
                    acc = "naszego",
                    inst = "naszym",
                    loc = "naszym",
                    voc = "nasz",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "nasz",
                    gen = "naszego",
                    dat = "naszemu",
                    acc = "nasz",
                    inst = "naszym",
                    loc = "naszym",
                    voc = "nasz",
                ),
                f = CaseFormsRow(
                    nom = "nasza",
                    gen = "naszej",
                    dat = "naszej",
                    acc = "naszą",
                    inst = "naszą",
                    loc = "naszej",
                    voc = "nasza",
                ),
                n = CaseFormsRow(
                    nom = "nasze",
                    gen = "naszego",
                    dat = "naszemu",
                    acc = "nasze",
                    inst = "naszym",
                    loc = "naszym",
                    voc = "nasze",
                ),
            ),
            pl = PossessivePluralRow(
                m_personal = CaseFormsRow(
                    nom = "nasi",
                    gen = "naszych",
                    dat = "naszym",
                    acc = "naszych",
                    inst = "naszymi",
                    loc = "naszych",
                    voc = "nasi",
                ),
                other = CaseFormsRow(
                    nom = "nasze",
                    gen = "naszych",
                    dat = "naszym",
                    acc = "nasze",
                    inst = "naszymi",
                    loc = "naszych",
                    voc = "nasze",
                ),
            ),
        ),
    ),
    PossessiveRow(
        id = "yourPlural",
        label = "wasz — ваш",
        forms = PossessiveFormsRow(
            kind = PossessiveFormsRowKind.DECLINED,
            sg = GenderFormsRow(
                m_personal = CaseFormsRow(
                    nom = "wasz",
                    gen = "waszego",
                    dat = "waszemu",
                    acc = "waszego",
                    inst = "waszym",
                    loc = "waszym",
                    voc = "wasz",
                ),
                m_animate = CaseFormsRow(
                    nom = "wasz",
                    gen = "waszego",
                    dat = "waszemu",
                    acc = "waszego",
                    inst = "waszym",
                    loc = "waszym",
                    voc = "wasz",
                ),
                m_inanimate = CaseFormsRow(
                    nom = "wasz",
                    gen = "waszego",
                    dat = "waszemu",
                    acc = "wasz",
                    inst = "waszym",
                    loc = "waszym",
                    voc = "wasz",
                ),
                f = CaseFormsRow(
                    nom = "wasza",
                    gen = "waszej",
                    dat = "waszej",
                    acc = "waszą",
                    inst = "waszą",
                    loc = "waszej",
                    voc = "wasza",
                ),
                n = CaseFormsRow(
                    nom = "wasze",
                    gen = "waszego",
                    dat = "waszemu",
                    acc = "wasze",
                    inst = "waszym",
                    loc = "waszym",
                    voc = "wasze",
                ),
            ),
            pl = PossessivePluralRow(
                m_personal = CaseFormsRow(
                    nom = "wasi",
                    gen = "waszych",
                    dat = "waszym",
                    acc = "waszych",
                    inst = "waszymi",
                    loc = "waszych",
                    voc = "wasi",
                ),
                other = CaseFormsRow(
                    nom = "wasze",
                    gen = "waszych",
                    dat = "waszym",
                    acc = "wasze",
                    inst = "waszymi",
                    loc = "waszych",
                    voc = "wasze",
                ),
            ),
        ),
    ),
    PossessiveRow(
        id = "their",
        label = "ich — их",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "ich"),
    ),
)

val POSSESSIVE_ROW_ROWS: List<PossessiveRow> = possessiveRowRowsPart0()

private fun seedRowRowsPart0(): List<SeedRow> = listOf(
    SeedRow(nounId = "wife", adjectiveId = "beautiful"),
    SeedRow(nounId = "husband", adjectiveId = "good"),
    SeedRow(nounId = "friendM", adjectiveId = "good"),
    SeedRow(nounId = "son", adjectiveId = "small"),
    SeedRow(nounId = "dog", adjectiveId = "good"),
    SeedRow(nounId = "cat", adjectiveId = "small"),
    SeedRow(nounId = "book", adjectiveId = "new"),
    SeedRow(nounId = "car", adjectiveId = "new"),
    SeedRow(nounId = "house", adjectiveId = "new"),
    SeedRow(nounId = "phone", adjectiveId = "new"),
    SeedRow(nounId = "child", adjectiveId = "small"),
    SeedRow(nounId = "window", adjectiveId = "new"),
)

val SEED_ROW_ROWS: List<SeedRow> = seedRowRowsPart0()

private fun copyEntryRowRowsPart0(): List<CopyEntryRow> = listOf(
    CopyEntryRow(key = "accPrompt", value = "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…»."),
    CopyEntryRow(
        key = "accExplanation",
        value = "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.",
    ),
    CopyEntryRow(key = "accReasonPersonalA", value = "kolega → kolegę; местоимение и прилагательное — mojego dobrego."),
    CopyEntryRow(
        key = "accReasonUnchanged",
        value = "Biernik совпадает с Mianownikiem: группа сохраняет базовую форму.",
    ),
    CopyEntryRow(key = "accReasonChanged", value = "Изменяется вся группа слов."),
    CopyEntryRow(key = "genNegPrompt", value = "Сделай всё предложение отрицательным."),
    CopyEntryRow(
        key = "genNegExplanation",
        value = "Отрицание widzę переводит прямой объект из Biernika в Dopełniacz.",
    ),
    CopyEntryRow(key = "genNegReason", value = "Biernik → Dopełniacz"),
    CopyEntryRow(key = "negationParticle", value = "Nie"),
    CopyEntryRow(key = "negationInsertionReason", value = "Добавь отрицание nie."),
    CopyEntryRow(key = "instStart", value = "Idę z"),
    CopyEntryRow(key = "instPrompt", value = "Скажи, что идёшь вместе с этим человеком. Начни с «Idę z…»."),
    CopyEntryRow(key = "instReason", value = "z kim? czym? — Narzędnik"),
    CopyEntryRow(key = "locStart", value = "Mówię o"),
    CopyEntryRow(key = "locPrompt", value = "Теперь скажи, что говоришь об этом. Начни с «Mówię o…»."),
    CopyEntryRow(key = "locReason", value = "o kim? czym? — Miejscownik"),
    CopyEntryRow(key = "datStart", value = "Daję prezent"),
    CopyEntryRow(key = "datPrompt", value = "Скажи, что даришь этому человеку подарок. Начни с «Daję prezent…»."),
    CopyEntryRow(key = "datReason", value = "komu? czemu? — Celownik"),
    CopyEntryRow(
        key = "caseDrillExplanation",
        value = "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.",
    ),
    CopyEntryRow(key = "ownerYour", value = "твой / твоя"),
    CopyEntryRow(key = "ownerHis", value = "его"),
    CopyEntryRow(key = "ownerHer", value = "её"),
    CopyEntryRow(key = "ownerOur", value = "наш / наша"),
    CopyEntryRow(key = "ownerYourPlural", value = "ваш / ваша"),
    CopyEntryRow(key = "ownerTheir", value = "их"),
    CopyEntryRow(key = "ownerPromptPrefix", value = "Замени «мой / моя» на «"),
    CopyEntryRow(key = "ownerPromptSuffix", value = "». Сохрани время и смысл предложения."),
    CopyEntryRow(
        key = "ownerExplanation",
        value = "mój, twój, nasz, wasz согласуются с предметом обладания. jego, jej, ich не склоняются.",
    ),
    CopyEntryRow(key = "ownerReason", value = "Меняем владельца, сохраняем Biernik."),
    CopyEntryRow(key = "verbPastPrompt", value = "Перенеси всё предложение в прошедшее время."),
    CopyEntryRow(
        key = "verbFuturePrompt",
        value = "Перенеси всё предложение в будущее время. Сохрани несовершенный вид.",
    ),
    CopyEntryRow(key = "verbPresentPrompt", value = "Перенеси всё предложение в настоящее время."),
    CopyEntryRow(
        key = "verbExplanation",
        value = "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.",
    ),
    CopyEntryRow(key = "verbReason", value = "Меняется время глагола, остальные слова сохраняются."),
    CopyEntryRow(key = "aspectSource", value = "Dzisiaj kupuję nową książkę."),
    CopyEntryRow(
        key = "aspectPrompt",
        value = "Скажи, что вчера уже купил эту книгу: завершённый результат. Начни с «Wczoraj…».",
    ),
    CopyEntryRow(key = "aspectExpected", value = "Wczoraj kupiłem nową książkę."),
    CopyEntryRow(key = "aspectAccepted", value = "Wczoraj kupiłam nową książkę."),
    CopyEntryRow(
        key = "aspectExplanation",
        value = "Процесс kupować → завершённый результат kupić. Форма kupiłam также правильная.",
    ),
    CopyEntryRow(key = "aspectFrom", value = "kupuję"),
    CopyEntryRow(key = "aspectTo", value = "kupiłem"),
    CopyEntryRow(key = "aspectReason", value = "Совершенный вид + прошедшее время."),
    CopyEntryRow(key = "pronounPrompt", value = "Замени всю группу после «Widzę» одним личным местоимением."),
    CopyEntryRow(key = "pronounExplanation", value = "Местоимение заменяет всю группу слов и остаётся в Bierniku."),
    CopyEntryRow(key = "pronounReason", value = "кого? что?"),
    CopyEntryRow(
        key = "questionPrompt",
        value = "Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Czy…».",
    ),
    CopyEntryRow(
        key = "questionExplanation",
        value = "Czy превращает утверждение в общий вопрос. Падеж объекта сохраняется.",
    ),
    CopyEntryRow(key = "questionReason", value = "Добавь вопросительное czy."),
    CopyEntryRow(key = "questionParticle", value = "Czy"),
    CopyEntryRow(key = "pluralPrompt", value = "Поставь всю группу после «Widzę» во множественное число."),
    CopyEntryRow(
        key = "pluralExplanation",
        value = "Во множественном числе мужские личные формы отличаются от остальных: moich dobrych kolegów, но moje nowe książki.",
    ),
    CopyEntryRow(key = "pluralReason", value = "Меняются число и согласование всей группы."),
    CopyEntryRow(key = "mixedPrompt", value = "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя»."),
    CopyEntryRow(
        key = "mixedExplanation",
        value = "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.",
    ),
    CopyEntryRow(key = "mixedVerbReason", value = "Лицо + время + отрицание"),
    CopyEntryRow(key = "mixedNounReason", value = "Biernik → Dopełniacz"),
    CopyEntryRow(key = "chainAccPrompt", value = "Скажи, что видишь это. Начни с «Widzę…»."),
    CopyEntryRow(key = "chainAccReason", value = "Widzę → Biernik."),
    CopyEntryRow(key = "chainPastPrompt", value = "Перенеси предложение в прошедшее время. Говори от мужского лица."),
    CopyEntryRow(key = "chainPastReason", value = "Меняется только время глагола."),
    CopyEntryRow(key = "chainNegPrompt", value = "Теперь сделай это предложение отрицательным."),
    CopyEntryRow(key = "chainNegReason", value = "Отрицание → Biernik меняется на Dopełniacz."),
    CopyEntryRow(key = "chainOwnerPrompt", value = "Замени «мой / моя» на «их»."),
    CopyEntryRow(
        key = "chainOwnerReason",
        value = "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.",
    ),
    CopyEntryRow(key = "chainLocPrompt", value = "Теперь скажи, что говоришь об этом. Начни с «Mówię o…»."),
    CopyEntryRow(key = "chainLocReason", value = "o → Miejscownik. Сохрани владельца «их»."),
    CopyEntryRow(key = "questionChangeFrom", value = "Widzisz"),
    CopyEntryRow(key = "questionChangeTo", value = "Czy widzisz"),
    CopyEntryRow(key = "mixedChangeFrom", value = "Widzę"),
    CopyEntryRow(key = "mixedChangeTo", value = "Nie widziałeś"),
    CopyEntryRow(key = "chainPastChangeFrom", value = "Widzę"),
    CopyEntryRow(key = "chainPastChangeTo", value = "Widziałem"),
    CopyEntryRow(key = "degreeOldSource", value = "Ten dom jest stary."),
    CopyEntryRow(key = "degreeOldExpected", value = "Ten dom jest starszy."),
    CopyEntryRow(
        key = "degreeOldPrompt",
        value = "Образуй простую сравнительную форму прилагательного; сохрани остальное предложение.",
    ),
    CopyEntryRow(key = "degreeOldExplanation", value = "Сравнительная степень от stary — starszy."),
    CopyEntryRow(key = "degreeOldFrom", value = "stary"),
    CopyEntryRow(key = "degreeOldTo", value = "starszy"),
    CopyEntryRow(key = "degreeOldReason", value = "Сравнительная форма прилагательного stary — starszy."),
    CopyEntryRow(key = "degreeEasySource", value = "To zadanie jest łatwe."),
    CopyEntryRow(key = "degreeEasyExpected", value = "To zadanie jest łatwiejsze."),
    CopyEntryRow(
        key = "degreeEasyPrompt",
        value = "Образуй простую сравнительную форму прилагательного; сохрани средний род.",
    ),
    CopyEntryRow(
        key = "degreeEasyExplanation",
        value = "Форма łatwiejsze согласуется с существительным среднего рода zadanie.",
    ),
    CopyEntryRow(key = "degreeEasyFrom", value = "łatwe"),
    CopyEntryRow(key = "degreeEasyTo", value = "łatwiejsze"),
    CopyEntryRow(
        key = "degreeEasyReason",
        value = "Сравнительная форма от łatwy — łatwiejszy; при zadanie она получает окончание -e.",
    ),
    CopyEntryRow(key = "degreeGoodSource", value = "Ta książka jest dobra."),
    CopyEntryRow(key = "degreeGoodExpected", value = "Ta książka jest lepsza."),
    CopyEntryRow(
        key = "degreeGoodPrompt",
        value = "Используй нерегулярную сравнительную форму прилагательного; сохрани женский род.",
    ),
    CopyEntryRow(
        key = "degreeGoodExplanation",
        value = "У dobry нерегулярная сравнительная форма lepszy; с książka нужна форма lepsza.",
    ),
    CopyEntryRow(key = "degreeGoodFrom", value = "dobra"),
    CopyEntryRow(key = "degreeGoodTo", value = "lepsza"),
    CopyEntryRow(
        key = "degreeGoodReason",
        value = "Dobry → lepszy — нерегулярная форма; lepsza согласуется с książka.",
    ),
    CopyEntryRow(key = "degreeAnalyticSource", value = "Ten film jest interesujący."),
    CopyEntryRow(key = "degreeAnalyticExpected", value = "Ten film jest bardziej interesujący."),
    CopyEntryRow(
        key = "degreeAnalyticPrompt",
        value = "Вырази сравнительную степень описательно, сохранив целое предложение.",
    ),
    CopyEntryRow(
        key = "degreeAnalyticExplanation",
        value = "Прилагательное interesujący образует сравнительную степень описательно с bardziej.",
    ),
    CopyEntryRow(key = "degreeAnalyticFrom", value = "interesujący"),
    CopyEntryRow(key = "degreeAnalyticTo", value = "bardziej interesujący"),
    CopyEntryRow(
        key = "degreeAnalyticReason",
        value = "Добавь bardziej перед interesujący; не образуй искусственную форму на -szy.",
    ),
    CopyEntryRow(key = "degreeSuperlativeSource", value = "To jest duży pokój."),
    CopyEntryRow(key = "degreeSuperlativeExpected", value = "To jest największy pokój."),
    CopyEntryRow(
        key = "degreeSuperlativePrompt",
        value = "Поставь характеристику комнаты в превосходную степень во всём предложении.",
    ),
    CopyEntryRow(
        key = "degreeSuperlativeExplanation",
        value = "У duży сравнительная форма większy, превосходная — największy.",
    ),
    CopyEntryRow(key = "degreeSuperlativeFrom", value = "duży"),
    CopyEntryRow(key = "degreeSuperlativeTo", value = "największy"),
    CopyEntryRow(
        key = "degreeSuperlativeReason",
        value = "Приставка naj- присоединяется к нерегулярной сравнительной форме większy.",
    ),
    CopyEntryRow(key = "degreeCompareSource", value = "Ten rower jest szybki."),
    CopyEntryRow(key = "degreeCompareExpected", value = "Ten rower jest szybszy od tamtego roweru."),
    CopyEntryRow(key = "degreeComparePrompt", value = "Сравни этот велосипед с тем, используя od и целое предложение."),
    CopyEntryRow(
        key = "degreeCompareExplanation",
        value = "После od второй предмет стоит в родительном падеже: tamtego roweru.",
    ),
    CopyEntryRow(key = "degreeCompareFrom", value = "szybki"),
    CopyEntryRow(key = "degreeCompareTo", value = "szybszy od tamtego roweru"),
    CopyEntryRow(
        key = "degreeCompareReason",
        value = "Szybki → szybszy; после od нужен родительный падеж: tamtego roweru.",
    ),
    CopyEntryRow(key = "participles1Source", value = "Kobieta, która czyta książkę, siedzi przy oknie."),
    CopyEntryRow(key = "participles1Expected", value = "Kobieta czytająca książkę siedzi przy oknie."),
    CopyEntryRow(
        key = "participles1Prompt",
        value = "Замени придаточное действительным причастием, сохрани всё предложение.",
    ),
    CopyEntryRow(
        key = "participles1Explanation",
        value = "Читающая женщина — субъект обоих действий; форма согласуется с kobieta.",
    ),
    CopyEntryRow(key = "participles1From", value = "która czyta"),
    CopyEntryRow(key = "participles1To", value = "czytająca"),
    CopyEntryRow(
        key = "participles1Reason",
        value = "Читающая женщина — субъект обоих действий; форма согласуется с kobieta.",
    ),
    CopyEntryRow(key = "participles2Source", value = "Mężczyzna, który pracuje w sklepie, zna mojego brata."),
    CopyEntryRow(key = "participles2Expected", value = "Mężczyzna pracujący w sklepie zna mojego brata."),
    CopyEntryRow(key = "participles2Prompt", value = "Сверни придаточное в действительное причастие."),
    CopyEntryRow(
        key = "participles2Explanation",
        value = "Действительное причастие мужского рода описывает mężczyzna.",
    ),
    CopyEntryRow(key = "participles2From", value = "który pracuje"),
    CopyEntryRow(key = "participles2To", value = "pracujący"),
    CopyEntryRow(key = "participles2Reason", value = "Действительное причастие мужского рода описывает mężczyzna."),
    CopyEntryRow(key = "participles3Source", value = "List, który został napisany wczoraj, leży na stole."),
    CopyEntryRow(key = "participles3Expected", value = "List napisany wczoraj leży na stole."),
    CopyEntryRow(
        key = "participles3Prompt",
        value = "Убери относительное придаточное, сохрани страдательное причастие.",
    ),
    CopyEntryRow(key = "participles3Explanation", value = "Результат написания остаётся признаком письма."),
    CopyEntryRow(key = "participles3From", value = "który został napisany"),
    CopyEntryRow(key = "participles3To", value = "napisany"),
    CopyEntryRow(key = "participles3Reason", value = "Результат написания остаётся признаком письма."),
    CopyEntryRow(key = "participles4Source", value = "Drzwi, które zostały zamknięte rano, prowadzą do piwnicy."),
    CopyEntryRow(key = "participles4Expected", value = "Drzwi zamknięte rano prowadzą do piwnicy."),
    CopyEntryRow(key = "participles4Prompt", value = "Сверни придаточное со страдательным причастием."),
    CopyEntryRow(key = "participles4Explanation", value = "Форма множественного числа согласуется с drzwi."),
    CopyEntryRow(key = "participles4From", value = "które zostały zamknięte"),
    CopyEntryRow(key = "participles4To", value = "zamknięte"),
    CopyEntryRow(key = "participles4Reason", value = "Форма множественного числа согласуется с drzwi."),
    CopyEntryRow(key = "participles5Source", value = "Maria czyta gazetę i pije kawę."),
    CopyEntryRow(key = "participles5Expected", value = "Czytając gazetę, Maria pije kawę."),
    CopyEntryRow(key = "participles5Prompt", value = "Сделай первое одновременное действие деепричастным."),
    CopyEntryRow(key = "participles5Explanation", value = "Оба действия выполняет Maria; -ąc не склоняется."),
    CopyEntryRow(key = "participles5From", value = "Maria czyta gazetę"),
    CopyEntryRow(key = "participles5To", value = "Czytając gazetę"),
    CopyEntryRow(key = "participles5Reason", value = "Оба действия выполняет Maria; -ąc не склоняется."),
    CopyEntryRow(key = "participles6Source", value = "Piotr słucha muzyki i sprząta pokój."),
    CopyEntryRow(key = "participles6Expected", value = "Słuchając muzyki, Piotr sprząta pokój."),
    CopyEntryRow(key = "participles6Prompt", value = "Передай первое одновременное действие деепричастием."),
    CopyEntryRow(key = "participles6Explanation", value = "Субъект Piotr остаётся общим для обоих действий."),
    CopyEntryRow(key = "participles6From", value = "Piotr słucha muzyki"),
    CopyEntryRow(key = "participles6To", value = "Słuchając muzyki"),
    CopyEntryRow(key = "participles6Reason", value = "Субъект Piotr остаётся общим для обоих действий."),
    CopyEntryRow(key = "voice1Source", value = "Ktoś zamknął sklep o szóstej."),
    CopyEntryRow(key = "voice1Expected", value = "Zamknięto sklep o szóstej."),
    CopyEntryRow(key = "voice1Prompt", value = "Замени неопределённого деятеля безличной формой на -to."),
    CopyEntryRow(key = "voice1Explanation", value = "В безличной форме объект sklep остаётся дополнением."),
    CopyEntryRow(key = "voice1From", value = "Ktoś zamknął"),
    CopyEntryRow(key = "voice1To", value = "Zamknięto"),
    CopyEntryRow(key = "voice1Reason", value = "В безличной форме объект sklep остаётся дополнением."),
    CopyEntryRow(key = "voice2Source", value = "Ktoś otworzył okno rano."),
    CopyEntryRow(key = "voice2Expected", value = "Otwarto okno rano."),
    CopyEntryRow(key = "voice2Prompt", value = "Передай действие безличной формой на -to."),
    CopyEntryRow(key = "voice2Explanation", value = "Форма otwarto не называет исполнителя; okno сохраняется."),
    CopyEntryRow(key = "voice2From", value = "Ktoś otworzył"),
    CopyEntryRow(key = "voice2To", value = "Otwarto"),
    CopyEntryRow(key = "voice2Reason", value = "Форма otwarto не называет исполнителя; okno сохраняется."),
    CopyEntryRow(key = "voice3Source", value = "Ktoś przeczytał list wczoraj."),
    CopyEntryRow(key = "voice3Expected", value = "Przeczytano list wczoraj."),
    CopyEntryRow(key = "voice3Prompt", value = "Убери неопределённого деятеля, употреби -no."),
    CopyEntryRow(key = "voice3Explanation", value = "Прочитанный объект list остаётся винительным дополнением."),
    CopyEntryRow(key = "voice3From", value = "Ktoś przeczytał"),
    CopyEntryRow(key = "voice3To", value = "Przeczytano"),
    CopyEntryRow(key = "voice3Reason", value = "Прочитанный объект list остаётся винительным дополнением."),
    CopyEntryRow(key = "voice4Source", value = "Robotnicy naprawili drogę."),
    CopyEntryRow(key = "voice4Expected", value = "Droga została naprawiona przez robotników."),
    CopyEntryRow(key = "voice4Prompt", value = "Сделай объект подлежащим пассивного предложения и сохрани деятеля."),
    CopyEntryRow(
        key = "voice4Explanation",
        value = "Droga — женское подлежащее; причастие naprawiona согласуется с ним.",
    ),
    CopyEntryRow(key = "voice4From", value = "Robotnicy naprawili drogę"),
    CopyEntryRow(key = "voice4To", value = "Droga została naprawiona przez robotników"),
    CopyEntryRow(key = "voice4Reason", value = "Droga — женское подлежащее; причастие naprawiona согласуется с ним."),
    CopyEntryRow(key = "voice5Source", value = "Policja zatrzymała kierowcę."),
    CopyEntryRow(key = "voice5Expected", value = "Kierowca został zatrzymany przez policję."),
    CopyEntryRow(key = "voice5Prompt", value = "Передай действие пассивом с названным деятелем."),
    CopyEntryRow(key = "voice5Explanation", value = "Kierowca становится подлежащим, zatrzymany — мужская форма."),
    CopyEntryRow(key = "voice5From", value = "Policja zatrzymała kierowcę"),
    CopyEntryRow(key = "voice5To", value = "Kierowca został zatrzymany przez policję"),
    CopyEntryRow(key = "voice5Reason", value = "Kierowca становится подлежащим, zatrzymany — мужская форма."),
    CopyEntryRow(key = "voice6Source", value = "Nauczyciel sprawdził testy."),
    CopyEntryRow(key = "voice6Expected", value = "Testy zostały sprawdzone przez nauczyciela."),
    CopyEntryRow(key = "voice6Prompt", value = "Сделай testy подлежащим пассивного предложения."),
    CopyEntryRow(key = "voice6Explanation", value = "Множественное testy требует zostały sprawdzone."),
    CopyEntryRow(key = "voice6From", value = "Nauczyciel sprawdził testy"),
    CopyEntryRow(key = "voice6To", value = "Testy zostały sprawdzone przez nauczyciela"),
    CopyEntryRow(key = "voice6Reason", value = "Множественное testy требует zostały sprawdzone."),
    CopyEntryRow(key = "nouns1Source", value = "To, że czytam książki, sprawia mi przyjemność."),
    CopyEntryRow(key = "nouns1Expected", value = "Czytanie książek sprawia mi przyjemność."),
    CopyEntryRow(key = "nouns1Prompt", value = "Замени придаточное названием действия, сохрани смысл."),
    CopyEntryRow(
        key = "nouns1Explanation",
        value = "После названия действия объект переходит в родительный множественного числа.",
    ),
    CopyEntryRow(key = "nouns1From", value = "czytam książki"),
    CopyEntryRow(key = "nouns1To", value = "Czytanie książek"),
    CopyEntryRow(
        key = "nouns1Reason",
        value = "После названия действия объект переходит в родительный множественного числа.",
    ),
    CopyEntryRow(key = "nouns2Source", value = "To, że piszę listy, zajmuje dużo czasu."),
    CopyEntryRow(key = "nouns2Expected", value = "Pisanie listów zajmuje dużo czasu."),
    CopyEntryRow(key = "nouns2Prompt", value = "Назови действие существительным и перестрой объект."),
    CopyEntryRow(key = "nouns2Explanation", value = "Pisanie управляет родительным listów."),
    CopyEntryRow(key = "nouns2From", value = "piszę listy"),
    CopyEntryRow(key = "nouns2To", value = "Pisanie listów"),
    CopyEntryRow(key = "nouns2Reason", value = "Pisanie управляет родительным listów."),
    CopyEntryRow(key = "nouns3Source", value = "To, że wspólnie gotujemy, jest dla nas ważne."),
    CopyEntryRow(key = "nouns3Expected", value = "Wspólne gotowanie jest dla nas ważne."),
    CopyEntryRow(key = "nouns3Prompt", value = "Замени придаточное названием действия."),
    CopyEntryRow(key = "nouns3Explanation", value = "Наречие совместности передано согласованным прилагательным."),
    CopyEntryRow(key = "nouns3From", value = "wspólnie gotujemy"),
    CopyEntryRow(key = "nouns3To", value = "Wspólne gotowanie"),
    CopyEntryRow(key = "nouns3Reason", value = "Наречие совместности передано согласованным прилагательным."),
    CopyEntryRow(key = "nouns4Source", value = "To, że ten pokój jest czysty, ma znaczenie."),
    CopyEntryRow(key = "nouns4Expected", value = "Czystość tego pokoju ma znaczenie."),
    CopyEntryRow(key = "nouns4Prompt", value = "Замени описание качества существительным."),
    CopyEntryRow(
        key = "nouns4Explanation",
        value = "Czystość называет качество; обладатель качества стоит в родительном.",
    ),
    CopyEntryRow(key = "nouns4From", value = "ten pokój jest czysty"),
    CopyEntryRow(key = "nouns4To", value = "Czystość tego pokoju"),
    CopyEntryRow(key = "nouns4Reason", value = "Czystość называет качество; обладатель качества стоит в родительном."),
    CopyEntryRow(key = "nouns5Source", value = "To, że ona jest młoda, nie przeszkadza jej w pracy."),
    CopyEntryRow(key = "nouns5Expected", value = "Jej młodość nie przeszkadza jej w pracy."),
    CopyEntryRow(key = "nouns5Prompt", value = "Назови качество существительным, сохрани субъект."),
    CopyEntryRow(key = "nouns5Explanation", value = "Młodość — название качества, jej сохраняет обладателя."),
    CopyEntryRow(key = "nouns5From", value = "ona jest młoda"),
    CopyEntryRow(key = "nouns5To", value = "Jej młodość"),
    CopyEntryRow(key = "nouns5Reason", value = "Młodość — название качества, jej сохраняет обладателя."),
    CopyEntryRow(key = "nouns6Source", value = "To, że on jest odważny, pomaga mu w pracy."),
    CopyEntryRow(key = "nouns6Expected", value = "Jego odwaga pomaga mu w pracy."),
    CopyEntryRow(key = "nouns6Prompt", value = "Назови качество существительным, сохрани смысл."),
    CopyEntryRow(
        key = "nouns6Explanation",
        value = "Odwaga — производное название качества; jego сохраняет обладателя.",
    ),
    CopyEntryRow(key = "nouns6From", value = "on jest odważny"),
    CopyEntryRow(key = "nouns6To", value = "Jego odwaga"),
    CopyEntryRow(key = "nouns6Reason", value = "Odwaga — производное название качества; jego сохраняет обладателя."),
    CopyEntryRow(key = "numerals1Source", value = "Na stole leży jedna książka."),
    CopyEntryRow(key = "numerals1Expected", value = "Na stole leżą dwie książki."),
    CopyEntryRow(key = "numerals1Prompt", value = "Измени количество на две книги и согласуй всё предложение."),
    CopyEntryRow(
        key = "numerals1Explanation",
        value = "После dwie существительное и сказуемое имеют формы множественного числа.",
    ),
    CopyEntryRow(key = "numerals1From", value = "jedna książka leży"),
    CopyEntryRow(key = "numerals1To", value = "dwie książki leżą"),
    CopyEntryRow(
        key = "numerals1Reason",
        value = "После dwie существительное и сказуемое имеют формы множественного числа.",
    ),
    CopyEntryRow(key = "numerals2Source", value = "W pokoju stoją dwa krzesła."),
    CopyEntryRow(key = "numerals2Expected", value = "W pokoju stoi pięć krzeseł."),
    CopyEntryRow(key = "numerals2Prompt", value = "Измени количество на пять стульев."),
    CopyEntryRow(
        key = "numerals2Explanation",
        value = "После pięć нужен родительный krzeseł и сказуемое среднего единственного.",
    ),
    CopyEntryRow(key = "numerals2From", value = "dwa krzesła stoją"),
    CopyEntryRow(key = "numerals2To", value = "pięć krzeseł stoi"),
    CopyEntryRow(
        key = "numerals2Reason",
        value = "После pięć нужен родительный krzeseł и сказуемое среднего единственного.",
    ),
    CopyEntryRow(key = "numerals3Source", value = "Dwaj studenci przyszli rano."),
    CopyEntryRow(key = "numerals3Expected", value = "Pięciu studentów przyszło rano."),
    CopyEntryRow(key = "numerals3Prompt", value = "Измени количество на пять студентов, сохрани время."),
    CopyEntryRow(
        key = "numerals3Explanation",
        value = "Мужской личный тип: pięciu studentów и пришедшее событие в среднем единственном.",
    ),
    CopyEntryRow(key = "numerals3From", value = "Dwaj studenci przyszli"),
    CopyEntryRow(key = "numerals3To", value = "Pięciu studentów przyszło"),
    CopyEntryRow(
        key = "numerals3Reason",
        value = "Мужской личный тип: pięciu studentów и пришедшее событие в среднем единственном.",
    ),
    CopyEntryRow(key = "numerals4Source", value = "Mam jednego kota."),
    CopyEntryRow(key = "numerals4Expected", value = "Mam trzy koty."),
    CopyEntryRow(key = "numerals4Prompt", value = "Измени количество на три кота."),
    CopyEntryRow(
        key = "numerals4Explanation",
        value = "В Mam trzy koty винительный множественного совпадает с именительным: koty. Коты одушевлённые, но не относятся к мужскому личному типу.",
    ),
    CopyEntryRow(key = "numerals4From", value = "jednego kota"),
    CopyEntryRow(key = "numerals4To", value = "trzy koty"),
    CopyEntryRow(
        key = "numerals4Reason",
        value = "В Mam trzy koty винительный множественного совпадает с именительным: koty. Коты одушевлённые, но не относятся к мужскому личному типу.",
    ),
    CopyEntryRow(key = "numerals5Source", value = "Widziałem dwie kobiety."),
    CopyEntryRow(key = "numerals5Expected", value = "Widziałem pięć kobiet."),
    CopyEntryRow(key = "numerals5Prompt", value = "Измени количество на пять женщин."),
    CopyEntryRow(key = "numerals5Explanation", value = "Pięć требует родительного множественного kobiet."),
    CopyEntryRow(key = "numerals5From", value = "dwie kobiety"),
    CopyEntryRow(key = "numerals5To", value = "pięć kobiet"),
    CopyEntryRow(key = "numerals5Reason", value = "Pięć требует родительного множественного kobiet."),
    CopyEntryRow(key = "numerals6Source", value = "Czekam na jednego kolegę."),
    CopyEntryRow(key = "numerals6Expected", value = "Czekam na dwóch kolegów."),
    CopyEntryRow(key = "numerals6Prompt", value = "Измени количество на двух коллег."),
    CopyEntryRow(key = "numerals6Explanation", value = "В винительном мужского личного типа: dwóch kolegów."),
    CopyEntryRow(key = "numerals6From", value = "jednego kolegę"),
    CopyEntryRow(key = "numerals6To", value = "dwóch kolegów"),
    CopyEntryRow(key = "numerals6Reason", value = "В винительном мужского личного типа: dwóch kolegów."),
    CopyEntryRow(key = "adjectives1Source", value = "To jest stół z drewna."),
    CopyEntryRow(key = "adjectives1Expected", value = "To jest drewniany stół."),
    CopyEntryRow(key = "adjectives1Prompt", value = "Замени указание материала производным прилагательным."),
    CopyEntryRow(key = "adjectives1Explanation", value = "Drewno даёт drewniany; форма согласуется со stół."),
    CopyEntryRow(key = "adjectives1From", value = "stół z drewna"),
    CopyEntryRow(key = "adjectives1To", value = "drewniany stół"),
    CopyEntryRow(key = "adjectives1Reason", value = "Drewno даёт drewniany; форма согласуется со stół."),
    CopyEntryRow(key = "adjectives2Source", value = "To jest butelka ze szkła."),
    CopyEntryRow(key = "adjectives2Expected", value = "To jest szklana butelka."),
    CopyEntryRow(key = "adjectives2Prompt", value = "Замени указание материала прилагательным."),
    CopyEntryRow(key = "adjectives2Explanation", value = "Szkło даёт szklany; женская форма szklana."),
    CopyEntryRow(key = "adjectives2From", value = "butelka ze szkła"),
    CopyEntryRow(key = "adjectives2To", value = "szklana butelka"),
    CopyEntryRow(key = "adjectives2Reason", value = "Szkło даёт szklany; женская форма szklana."),
    CopyEntryRow(key = "adjectives3Source", value = "To jest sweter z wełny."),
    CopyEntryRow(key = "adjectives3Expected", value = "To jest wełniany sweter."),
    CopyEntryRow(key = "adjectives3Prompt", value = "Передай материал прилагательным."),
    CopyEntryRow(key = "adjectives3Explanation", value = "Wełna даёт wełniany, согласованное со sweter."),
    CopyEntryRow(key = "adjectives3From", value = "sweter z wełny"),
    CopyEntryRow(key = "adjectives3To", value = "wełniany sweter"),
    CopyEntryRow(key = "adjectives3Reason", value = "Wełna даёт wełniany, согласованное со sweter."),
    CopyEntryRow(key = "adjectives4Source", value = "To jest plan na tydzień."),
    CopyEntryRow(key = "adjectives4Expected", value = "To jest tygodniowy plan."),
    CopyEntryRow(key = "adjectives4Prompt", value = "Замени указание длительности прилагательным."),
    CopyEntryRow(key = "adjectives4Explanation", value = "Tydzień даёт tygodniowy; здесь речь о плане на неделю."),
    CopyEntryRow(key = "adjectives4From", value = "plan na tydzień"),
    CopyEntryRow(key = "adjectives4To", value = "tygodniowy plan"),
    CopyEntryRow(key = "adjectives4Reason", value = "Tydzień даёт tygodniowy; здесь речь о плане на неделю."),
    CopyEntryRow(key = "adjectives5Source", value = "To jest podróż zimą."),
    CopyEntryRow(key = "adjectives5Expected", value = "To jest zimowa podróż."),
    CopyEntryRow(key = "adjectives5Prompt", value = "Передай время путешествия прилагательным."),
    CopyEntryRow(key = "adjectives5Explanation", value = "Zima даёт zimowy; женская форма zimowa."),
    CopyEntryRow(key = "adjectives5From", value = "podróż zimą"),
    CopyEntryRow(key = "adjectives5To", value = "zimowa podróż"),
    CopyEntryRow(key = "adjectives5Reason", value = "Zima даёт zimowy; женская форма zimowa."),
    CopyEntryRow(key = "adjectives6Source", value = "To jest obiad z rodziną."),
    CopyEntryRow(key = "adjectives6Expected", value = "To jest rodzinny obiad."),
    CopyEntryRow(key = "adjectives6Prompt", value = "Сверни указание на семью в прилагательное."),
    CopyEntryRow(
        key = "adjectives6Explanation",
        value = "Rodzina даёт rodzinny; контекст семейного обеда сохраняется.",
    ),
    CopyEntryRow(key = "adjectives6From", value = "obiad z rodziną"),
    CopyEntryRow(key = "adjectives6To", value = "rodzinny obiad"),
    CopyEntryRow(key = "adjectives6Reason", value = "Rodzina даёт rodzinny; контекст семейного обеда сохраняется."),
    CopyEntryRow(key = "connectives1Source", value = "Pada deszcz. Zostajemy w domu."),
    CopyEntryRow(key = "connectives1Expected", value = "Zostajemy w domu, ponieważ pada deszcz."),
    CopyEntryRow(key = "connectives1Prompt", value = "Соедини мысли союзом ponieważ, назвав причину."),
    CopyEntryRow(key = "connectives1Explanation", value = "Дождь объясняет решение остаться дома."),
    CopyEntryRow(key = "connectives1From", value = "Pada deszcz. Zostajemy w domu."),
    CopyEntryRow(key = "connectives1To", value = "Zostajemy w domu, ponieważ pada deszcz."),
    CopyEntryRow(key = "connectives1Reason", value = "Дождь объясняет решение остаться дома."),
    CopyEntryRow(key = "connectives2Source", value = "Pada deszcz. Idziemy na spacer."),
    CopyEntryRow(key = "connectives2Expected", value = "Pada deszcz, ale idziemy na spacer."),
    CopyEntryRow(key = "connectives2Prompt", value = "Соедини мысли союзом ale, сохрани противопоставление."),
    CopyEntryRow(key = "connectives2Explanation", value = "Прогулка происходит вопреки дождю."),
    CopyEntryRow(key = "connectives2From", value = "Pada deszcz. Idziemy na spacer."),
    CopyEntryRow(key = "connectives2To", value = "Pada deszcz, ale idziemy na spacer."),
    CopyEntryRow(key = "connectives2Reason", value = "Прогулка происходит вопреки дождю."),
    CopyEntryRow(key = "connectives3Source", value = "Będziesz miał czas. Przyjdź do mnie."),
    CopyEntryRow(key = "connectives3Expected", value = "Jeśli będziesz miał czas, przyjdź do mnie."),
    CopyEntryRow(key = "connectives3Prompt", value = "Соедини мысли союзом jeśli, обозначив условие."),
    CopyEntryRow(key = "connectives3Explanation", value = "Время — условие приглашения."),
    CopyEntryRow(key = "connectives3From", value = "Będziesz miał czas. Przyjdź do mnie."),
    CopyEntryRow(key = "connectives3To", value = "Jeśli będziesz miał czas, przyjdź do mnie."),
    CopyEntryRow(key = "connectives3Reason", value = "Время — условие приглашения."),
    CopyEntryRow(key = "connectives4Source", value = "Uczę się polskiego. Chcę rozmawiać z sąsiadami."),
    CopyEntryRow(key = "connectives4Expected", value = "Uczę się polskiego, żeby rozmawiać z sąsiadami."),
    CopyEntryRow(key = "connectives4Prompt", value = "Вырази цель учёбы с помощью żeby."),
    CopyEntryRow(key = "connectives4Explanation", value = "Желание разговаривать превращается в целевую часть."),
    CopyEntryRow(key = "connectives4From", value = "Chcę rozmawiać z sąsiadami"),
    CopyEntryRow(key = "connectives4To", value = "żeby rozmawiać z sąsiadami"),
    CopyEntryRow(key = "connectives4Reason", value = "Желание разговаривать превращается в целевую часть."),
    CopyEntryRow(key = "connectives5Source", value = "Jest zmęczona. Pracuje dalej."),
    CopyEntryRow(key = "connectives5Expected", value = "Chociaż jest zmęczona, pracuje dalej."),
    CopyEntryRow(key = "connectives5Prompt", value = "Соедини мысли союзом chociaż, назвав уступку."),
    CopyEntryRow(key = "connectives5Explanation", value = "Работа продолжается несмотря на усталость."),
    CopyEntryRow(key = "connectives5From", value = "Jest zmęczona. Pracuje dalej."),
    CopyEntryRow(key = "connectives5To", value = "Chociaż jest zmęczona, pracuje dalej."),
    CopyEntryRow(key = "connectives5Reason", value = "Работа продолжается несмотря на усталость."),
    CopyEntryRow(key = "connectives6Source", value = "Skończę pracę. Zadzwonię do ciebie."),
    CopyEntryRow(key = "connectives6Expected", value = "Kiedy skończę pracę, zadzwonię do ciebie."),
    CopyEntryRow(key = "connectives6Prompt", value = "Соедини мысли союзом kiedy, назвав время звонка."),
    CopyEntryRow(key = "connectives6Explanation", value = "Звонок следует после окончания работы."),
    CopyEntryRow(key = "connectives6From", value = "Skończę pracę. Zadzwonię do ciebie."),
    CopyEntryRow(key = "connectives6To", value = "Kiedy skończę pracę, zadzwonię do ciebie."),
    CopyEntryRow(key = "connectives6Reason", value = "Звонок следует после окончания работы."),
    CopyEntryRow(key = "rephrase1Source", value = "Piotr jest wyższy od Jana."),
    CopyEntryRow(key = "rephrase1Expected", value = "Jan jest niższy od Piotra."),
    CopyEntryRow(key = "rephrase1Prompt", value = "Сохрани сравнение, сделав Jan подлежащим и используя niższy."),
    CopyEntryRow(key = "rephrase1Explanation", value = "Обратное сравнение сохраняет порядок роста."),
    CopyEntryRow(key = "rephrase1From", value = "Piotr jest wyższy od Jana"),
    CopyEntryRow(key = "rephrase1To", value = "Jan jest niższy od Piotra"),
    CopyEntryRow(key = "rephrase1Reason", value = "Обратное сравнение сохраняет порядок роста."),
    CopyEntryRow(key = "rephrase2Source", value = "Tylko Anna zna odpowiedź."),
    CopyEntryRow(key = "rephrase2Expected", value = "Nikt oprócz Anny nie zna odpowiedzi."),
    CopyEntryRow(key = "rephrase2Prompt", value = "Передай ограничение через nikt oprócz."),
    CopyEntryRow(key = "rephrase2Explanation", value = "Отрицание и oprócz сохраняют исключительность Анны."),
    CopyEntryRow(key = "rephrase2From", value = "Tylko Anna zna odpowiedź"),
    CopyEntryRow(key = "rephrase2To", value = "Nikt oprócz Anny nie zna odpowiedzi"),
    CopyEntryRow(key = "rephrase2Reason", value = "Отрицание и oprócz сохраняют исключительность Анны."),
    CopyEntryRow(key = "rephrase3Source", value = "Chociaż pada deszcz, idziemy na spacer."),
    CopyEntryRow(key = "rephrase3Expected", value = "Mimo deszczu idziemy na spacer."),
    CopyEntryRow(key = "rephrase3Prompt", value = "Передай уступку сочетанием mimo + существительное."),
    CopyEntryRow(key = "rephrase3Explanation", value = "Уступительный смысл сохраняется; mimo требует родительного."),
    CopyEntryRow(key = "rephrase3From", value = "Chociaż pada deszcz"),
    CopyEntryRow(key = "rephrase3To", value = "Mimo deszczu"),
    CopyEntryRow(key = "rephrase3Reason", value = "Уступительный смысл сохраняется; mimo требует родительного."),
    CopyEntryRow(key = "rephrase4Source", value = "Nie mogę przyjść, bo jestem chory."),
    CopyEntryRow(key = "rephrase4Expected", value = "Z powodu choroby nie mogę przyjść."),
    CopyEntryRow(key = "rephrase4Prompt", value = "Передай причину сочетанием z powodu."),
    CopyEntryRow(key = "rephrase4Explanation", value = "Причина болезни остаётся той же."),
    CopyEntryRow(key = "rephrase4From", value = "bo jestem chory"),
    CopyEntryRow(key = "rephrase4To", value = "Z powodu choroby"),
    CopyEntryRow(key = "rephrase4Reason", value = "Причина болезни остаётся той же."),
    CopyEntryRow(key = "rephrase5Source", value = "Piotr skończył pracę, a potem wrócił do domu."),
    CopyEntryRow(key = "rephrase5Expected", value = "Kiedy Piotr skończył pracę, wrócił do domu."),
    CopyEntryRow(
        key = "rephrase5Prompt",
        value = "Передай последовательность придаточным с kiedy; оба действия выполняет Piotr.",
    ),
    CopyEntryRow(
        key = "rephrase5Explanation",
        value = "Piotr закончил работу и затем вернулся домой; после перестройки оба действия остаются его.",
    ),
    CopyEntryRow(key = "rephrase5From", value = "Piotr skończył pracę, a potem"),
    CopyEntryRow(key = "rephrase5To", value = "Kiedy Piotr skończył pracę"),
    CopyEntryRow(
        key = "rephrase5Reason",
        value = "Piotr закончил работу и затем вернулся домой; после перестройки оба действия остаются его.",
    ),
    CopyEntryRow(key = "rephrase6Source", value = "Maria napisała list."),
    CopyEntryRow(key = "rephrase6Expected", value = "List został napisany przez Marię."),
    CopyEntryRow(key = "rephrase6Prompt", value = "Перестрой предложение в пассив, сохрани деятеля."),
    CopyEntryRow(key = "rephrase6Explanation", value = "Пассив меняет подлежащее, но сохраняет автора письма."),
    CopyEntryRow(key = "rephrase6From", value = "Maria napisała list"),
    CopyEntryRow(key = "rephrase6To", value = "List został napisany przez Marię"),
    CopyEntryRow(key = "rephrase6Reason", value = "Пассив меняет подлежащее, но сохраняет автора письма."),
    CopyEntryRow(key = "valency1Source", value = "Я жду автобус."),
    CopyEntryRow(key = "valency1Expected", value = "Czekam na autobus."),
    CopyEntryRow(key = "valency1Prompt", value = "Переведи с глаголом czekać и его польским управлением."),
    CopyEntryRow(key = "valency1Explanation", value = "Czekać требует na + Biernik."),
    CopyEntryRow(key = "valency1From", value = "жду автобус"),
    CopyEntryRow(key = "valency1To", value = "czekam na autobus"),
    CopyEntryRow(key = "valency1Reason", value = "Czekać требует na + Biernik."),
    CopyEntryRow(key = "valency2Source", value = "Я слушаю музыку."),
    CopyEntryRow(key = "valency2Expected", value = "Słucham muzyki."),
    CopyEntryRow(key = "valency2Prompt", value = "Переведи с глаголом słuchać и его польским управлением."),
    CopyEntryRow(key = "valency2Explanation", value = "Słuchać требует Dopełniacz без предлога."),
    CopyEntryRow(key = "valency2From", value = "слушаю музыку"),
    CopyEntryRow(key = "valency2To", value = "słucham muzyki"),
    CopyEntryRow(key = "valency2Reason", value = "Słuchać требует Dopełniacz без предлога."),
    CopyEntryRow(key = "valency3Source", value = "Я помогаю брату."),
    CopyEntryRow(key = "valency3Expected", value = "Pomagam bratu."),
    CopyEntryRow(key = "valency3Prompt", value = "Переведи с глаголом pomagać и его польским управлением."),
    CopyEntryRow(key = "valency3Explanation", value = "Pomagać требует Celownik."),
    CopyEntryRow(key = "valency3From", value = "помогаю брату"),
    CopyEntryRow(key = "valency3To", value = "pomagam bratu"),
    CopyEntryRow(key = "valency3Reason", value = "Pomagać требует Celownik."),
    CopyEntryRow(key = "valency4Source", value = "Я пользуюсь телефоном."),
    CopyEntryRow(key = "valency4Expected", value = "Korzystam z telefonu."),
    CopyEntryRow(key = "valency4Prompt", value = "Переведи с глаголом korzystać и его польским управлением."),
    CopyEntryRow(key = "valency4Explanation", value = "Korzystać z требует Dopełniacz."),
    CopyEntryRow(key = "valency4From", value = "пользуюсь телефоном"),
    CopyEntryRow(key = "valency4To", value = "korzystam z telefonu"),
    CopyEntryRow(key = "valency4Reason", value = "Korzystać z требует Dopełniacz."),
    CopyEntryRow(key = "valency5Source", value = "Я интересуюсь историей."),
    CopyEntryRow(key = "valency5Expected", value = "Interesuję się historią."),
    CopyEntryRow(key = "valency5Prompt", value = "Переведи с глаголом interesować się и его польским управлением."),
    CopyEntryRow(key = "valency5Explanation", value = "Interesować się требует Narzędnik."),
    CopyEntryRow(key = "valency5From", value = "интересуюсь историей"),
    CopyEntryRow(key = "valency5To", value = "interesuję się historią"),
    CopyEntryRow(key = "valency5Reason", value = "Interesować się требует Narzędnik."),
    CopyEntryRow(key = "valency6Source", value = "Я ищу ключ."),
    CopyEntryRow(key = "valency6Expected", value = "Szukam klucza."),
    CopyEntryRow(key = "valency6Prompt", value = "Переведи с глаголом szukać и его польским управлением."),
    CopyEntryRow(
        key = "valency6Explanation",
        value = "Szukać требует Dopełniacz; русский винительный здесь не переносится.",
    ),
    CopyEntryRow(key = "valency6From", value = "ищу ключ"),
    CopyEntryRow(key = "valency6To", value = "szukam klucza"),
    CopyEntryRow(
        key = "valency6Reason",
        value = "Szukać требует Dopełniacz; русский винительный здесь не переносится.",
    ),
    CopyEntryRow(key = "mood1Source", value = "Piszę list."),
    CopyEntryRow(key = "mood1Expected", value = "Pisałbym list."),
    CopyEntryRow(key = "mood1Prompt", value = "Скажи гипотетически от лица мужчины; сохрани несовершенный вид."),
    CopyEntryRow(key = "mood1Explanation", value = "Мужская условная форма 1-го лица — pisałbym."),
    CopyEntryRow(key = "mood1From", value = "Piszę"),
    CopyEntryRow(key = "mood1To", value = "Pisałbym"),
    CopyEntryRow(key = "mood1Reason", value = "Мужская условная форма 1-го лица — pisałbym."),
    CopyEntryRow(key = "mood2Source", value = "Czytam tę książkę."),
    CopyEntryRow(key = "mood2Expected", value = "Czytałabym tę książkę."),
    CopyEntryRow(key = "mood2Prompt", value = "Скажи гипотетически от лица женщины; сохрани несовершенный вид."),
    CopyEntryRow(key = "mood2Explanation", value = "Женская условная форма 1-го лица — czytałabym."),
    CopyEntryRow(key = "mood2From", value = "Czytam"),
    CopyEntryRow(key = "mood2To", value = "Czytałabym"),
    CopyEntryRow(key = "mood2Reason", value = "Женская условная форма 1-го лица — czytałabym."),
    CopyEntryRow(key = "mood3Source", value = "Otwierasz okno."),
    CopyEntryRow(key = "mood3Expected", value = "Otwórz okno."),
    CopyEntryRow(key = "mood3Prompt", value = "Преврати сообщение в команду завершить действие."),
    CopyEntryRow(key = "mood3Explanation", value = "Совершенный вид otwórz просит завершить открытие."),
    CopyEntryRow(key = "mood3From", value = "Otwierasz"),
    CopyEntryRow(key = "mood3To", value = "Otwórz"),
    CopyEntryRow(key = "mood3Reason", value = "Совершенный вид otwórz просит завершить открытие."),
    CopyEntryRow(key = "mood4Source", value = "Wychodzisz teraz."),
    CopyEntryRow(key = "mood4Expected", value = "Nie wychodź teraz."),
    CopyEntryRow(key = "mood4Prompt", value = "Преврати сообщение в запрет на действие сейчас."),
    CopyEntryRow(key = "mood4Explanation", value = "Отрицательная команда использует несовершенный wychodź."),
    CopyEntryRow(key = "mood4From", value = "Wychodzisz"),
    CopyEntryRow(key = "mood4To", value = "Nie wychodź"),
    CopyEntryRow(key = "mood4Reason", value = "Отрицательная команда использует несовершенный wychodź."),
    CopyEntryRow(key = "mood5Source", value = "Idziemy do domu."),
    CopyEntryRow(key = "mood5Expected", value = "Chodźmy do domu."),
    CopyEntryRow(key = "mood5Prompt", value = "Преврати сообщение в совместное побуждение."),
    CopyEntryRow(key = "mood5Explanation", value = "Chodźmy приглашает собеседника идти вместе."),
    CopyEntryRow(key = "mood5From", value = "Idziemy"),
    CopyEntryRow(key = "mood5To", value = "Chodźmy"),
    CopyEntryRow(key = "mood5Reason", value = "Chodźmy приглашает собеседника идти вместе."),
    CopyEntryRow(key = "mood6Source", value = "Czy możesz mi pomóc?"),
    CopyEntryRow(key = "mood6Expected", value = "Czy mógłbyś mi pomóc?"),
    CopyEntryRow(key = "mood6Prompt", value = "Сделай просьбу вежливее, обращаясь к мужчине."),
    CopyEntryRow(key = "mood6Explanation", value = "Условная форма mógłbyś смягчает просьбу."),
    CopyEntryRow(key = "mood6From", value = "możesz"),
    CopyEntryRow(key = "mood6To", value = "mógłbyś"),
    CopyEntryRow(key = "mood6Reason", value = "Условная форма mógłbyś смягчает просьбу."),
)

val COPY_ENTRY_ROW_ROWS: List<CopyEntryRow> = copyEntryRowRowsPart0()

private fun patternEntryRowRowsPart0(): List<PatternEntryRow> = listOf(
    PatternEntryRow(key = "seenAcc", value = "Widzę {acc}."),
    PatternEntryRow(key = "seenGenNeg", value = "Nie widzę {gen}."),
    PatternEntryRow(key = "caseDrill", value = "{start} {target}."),
    PatternEntryRow(key = "verbSentence", value = "{nom} {verb} do domu."),
    PatternEntryRow(key = "verbFutureAccepted", value = "{nom} będzie {past} do domu."),
    PatternEntryRow(key = "questionSource", value = "Widzisz {acc}."),
    PatternEntryRow(key = "questionExpected", value = "Czy widzisz {acc}?"),
    PatternEntryRow(key = "mixedMale", value = "Nie widziałeś {gen}."),
    PatternEntryRow(key = "mixedFemale", value = "Nie widziałaś {gen}."),
    PatternEntryRow(key = "chainPast", value = "Widziałem {acc}."),
    PatternEntryRow(key = "chainNeg", value = "Nie widziałem {gen}."),
    PatternEntryRow(key = "chainOwner", value = "Nie widziałem {theirGen}."),
    PatternEntryRow(key = "chainLoc", value = "Mówię o {theirLoc}."),
)

val PATTERN_ENTRY_ROW_ROWS: List<PatternEntryRow> = patternEntryRowRowsPart0()

private fun prefixEntryRowRowsPart0(): List<PrefixEntryRow> = listOf(
    PrefixEntryRow(key = "nomSg", value = "To jest"),
    PrefixEntryRow(key = "nomPl", value = "To są"),
    PrefixEntryRow(key = "gen", value = "Nie widzę"),
    PrefixEntryRow(key = "dat", value = "Przyglądam się"),
    PrefixEntryRow(key = "acc", value = "Widzę"),
    PrefixEntryRow(key = "inst", value = "Zachwycam się"),
    PrefixEntryRow(key = "loc", value = "Mówię o"),
)

val PREFIX_ENTRY_ROW_ROWS: List<PrefixEntryRow> = prefixEntryRowRowsPart0()

private fun vocabularyRowRowsPart0(): List<VocabularyRow> = listOf(
    VocabularyRow(
        source = "curated from existing Polish grammar lexemes; examples authored for this course",
        items = listOf(
            VocabularyItemRow(
                id = "noun.wife",
                lemma = "żona",
                translation = "жена",
                form = "żona · род. żony",
                example = "Moja żona czyta książkę.",
                level = "A1",
                frequencyRank = 506,
            ),
            VocabularyItemRow(
                id = "noun.woman",
                lemma = "kobieta",
                translation = "женщина",
                form = "kobieta · род. kobiety",
                example = "Ta kobieta mówi po polsku.",
                level = "A1",
                frequencyRank = 162,
            ),
            VocabularyItemRow(
                id = "noun.book",
                lemma = "książka",
                translation = "книга",
                form = "książka · вин. książkę · род. książki",
                example = "Czytam nową książkę.",
                level = "A1",
                frequencyRank = 276,
            ),
            VocabularyItemRow(
                id = "noun.work",
                lemma = "praca",
                translation = "работа",
                form = "praca · род. pracy",
                example = "Mam dziś dużo pracy.",
                level = "A2",
                frequencyRank = 62,
            ),
            VocabularyItemRow(
                id = "noun.husband",
                lemma = "mąż",
                translation = "муж",
                form = "mąż · род. męża",
                example = "Jej mąż pracuje w domu.",
                level = "A2",
                frequencyRank = 658,
            ),
            VocabularyItemRow(
                id = "noun.friendM",
                lemma = "kolega",
                translation = "коллега",
                form = "kolega · род. kolegi",
                example = "Mój kolega z pracy ma psa.",
                level = "A2",
                frequencyRank = 445,
            ),
            VocabularyItemRow(
                id = "noun.son",
                lemma = "syn",
                translation = "сын",
                form = "syn · род. syna",
                example = "Mój syn lubi koty.",
                level = "A1",
                frequencyRank = 490,
            ),
            VocabularyItemRow(
                id = "noun.dog",
                lemma = "pies",
                translation = "собака",
                form = "pies · род. psa",
                example = "Ten pies jest mały.",
                level = "A1",
                frequencyRank = 637,
            ),
            VocabularyItemRow(
                id = "noun.cat",
                lemma = "kot",
                translation = "кот",
                form = "kot · род. kota",
                example = "Mój kot śpi na kanapie.",
                level = "A1",
                frequencyRank = 540,
            ),
            VocabularyItemRow(
                id = "noun.car",
                lemma = "samochód",
                translation = "машина",
                form = "samochód · род. samochodu",
                example = "Ten samochód jest nowy.",
                level = "A1",
                frequencyRank = 225,
            ),
            VocabularyItemRow(
                id = "noun.house",
                lemma = "dom",
                translation = "дом",
                form = "dom · род. domu",
                example = "Mój dom jest blisko.",
                level = "A1",
                frequencyRank = 104,
            ),
            VocabularyItemRow(
                id = "noun.phone",
                lemma = "telefon",
                translation = "телефон",
                form = "telefon · род. telefonu",
                example = "Gdzie jest mój telefon?",
                level = "A1",
                frequencyRank = 596,
            ),
            VocabularyItemRow(
                id = "noun.child",
                lemma = "dziecko",
                translation = "ребёнок",
                form = "dziecko · род. dziecka",
                example = "To dziecko lubi czytać.",
                level = "A1",
                frequencyRank = 86,
            ),
            VocabularyItemRow(
                id = "noun.window",
                lemma = "okno",
                translation = "окно",
                form = "okno · род. okna",
                example = "Otwórz okno, proszę.",
                level = "A1",
                frequencyRank = 554,
            ),
            VocabularyItemRow(
                id = "adjective.beautiful",
                lemma = "piękny",
                translation = "прекрасный, очень красивый",
                form = "piękny · ж. piękna",
                example = "To piękny dom.",
                level = "A2",
                frequencyRank = 488,
            ),
            VocabularyItemRow(
                id = "adjective.new",
                lemma = "nowy",
                translation = "новый",
                form = "nowy · ж. nowa · ж. вин. nową",
                example = "Mam nową książkę.",
                level = "A1",
                frequencyRank = 68,
            ),
            VocabularyItemRow(
                id = "adjective.small",
                lemma = "mały",
                translation = "маленький",
                form = "mały · ж. mała",
                example = "To mały pies.",
                level = "A1",
                frequencyRank = 155,
            ),
            VocabularyItemRow(
                id = "adjective.nice",
                lemma = "ładny",
                translation = "симпатичный, приятный на вид",
                form = "ładny · ж. ładna",
                example = "To ładny dom.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.good",
                lemma = "dobry",
                translation = "хороший",
                form = "dobry · ж. dobra",
                example = "To dobry pomysł.",
                level = "A1",
                frequencyRank = 81,
            ),
            VocabularyItemRow(
                id = "adjective.old",
                lemma = "stary",
                translation = "старый",
                form = "stary · ж. stara",
                example = "To stary samochód.",
                level = "A1",
                frequencyRank = 142,
            ),
            VocabularyItemRow(
                id = "adjective.expensive",
                lemma = "drogi",
                translation = "дорогой",
                form = "drogi · ж. droga",
                example = "Ten telefon jest drogi.",
                level = "A2",
                frequencyRank = 921,
            ),
            VocabularyItemRow(
                id = "verb.have",
                lemma = "mieć",
                translation = "иметь",
                form = "mieć · я mam",
                example = "Mam książkę.",
                level = "A1",
                frequencyRank = 14,
            ),
            VocabularyItemRow(
                id = "verb.see",
                lemma = "widzieć",
                translation = "видеть",
                form = "widzieć · я widzę",
                example = "Widzę dom.",
                level = "A1",
                frequencyRank = 117,
            ),
            VocabularyItemRow(
                id = "verb.like",
                lemma = "lubić",
                translation = "нравиться (о занятии); любить делать что-либо",
                form = "lubić · я lubię",
                example = "Lubię czytać.",
                level = "A1",
                frequencyRank = 370,
            ),
            VocabularyItemRow(
                id = "verb.buy",
                lemma = "kupować",
                translation = "покупать",
                form = "kupować · я kupuję",
                example = "Kupuję książkę.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.read",
                lemma = "czytać",
                translation = "читать",
                form = "czytać · я czytam",
                example = "Czytam książkę.",
                level = "A1",
                frequencyRank = 455,
            ),
            VocabularyItemRow(
                id = "verb.talk",
                lemma = "mówić",
                translation = "говорить",
                form = "mówić · я mówię",
                example = "Mówię po polsku.",
                level = "A1",
                frequencyRank = 41,
            ),
            VocabularyItemRow(
                id = "verb.go",
                lemma = "iść",
                translation = "идти",
                form = "iść · я idę",
                example = "Idę do domu.",
                level = "A1",
                frequencyRank = 176,
            ),
            VocabularyItemRow(
                id = "verb.be",
                lemma = "być",
                translation = "быть",
                form = "być · я jestem",
                example = "Jestem w domu.",
                level = "A1",
                frequencyRank = 2,
            ),
            VocabularyItemRow(
                id = "verb.buyDone",
                lemma = "kupić",
                translation = "купить",
                form = "kupić · буд. я kupię · прош. я (м.) kupiłem",
                example = "Kupiłem książkę.",
                level = "A2",
                frequencyRank = 449,
            ),
            VocabularyItemRow(
                id = "verb.do",
                lemma = "robić",
                translation = "делать",
                form = "robić · я robię",
                example = "Robię obiad.",
                level = "A1",
                frequencyRank = 133,
            ),
            VocabularyItemRow(
                id = "verb.doDone",
                lemma = "zrobić",
                translation = "сделать",
                form = "zrobić · буд. я zrobię · прош. я (м.) zrobiłem",
                example = "Zrobiłem obiad.",
                level = "A2",
                frequencyRank = 125,
            ),
        ),
        instructions = VocabularyInstructionsRow(
            react = "Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера видны в списке, но пока не добавляются в тренировки. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
            web = "Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера пока не добавляются в тренировки. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
            native = "Частотный ранг не равен уровню CEFR. Без проверенного перевода и примера слово пока недоступно. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
            ios = "Частотный ранг не равен уровню CEFR. Непроверенные слова пока недоступны. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
        ),
        unavailableLabel = "Перевод и пример ещё не проверены",
    ),
)

val VOCABULARY_ROW_ROWS: List<VocabularyRow> = vocabularyRowRowsPart0()

private fun trainingRowRowsPart0(): List<TrainingRow> = listOf(
    TrainingRow(
        chainPresentation = ChainPresentationRow(
            steps = listOf(
                ChainStepRow(id = "acc", label = "Вижу"),
                ChainStepRow(id = "past", label = "Прошлое"),
                ChainStepRow(id = "neg", label = "Отрицание"),
                ChainStepRow(id = "owner", label = "Владелец"),
                ChainStepRow(id = "loc", label = "Говорю о"),
            ),
            completion = ChainCompletionRow(
                title = "Цепочка завершена",
                reactEyebrow = "5 преобразований",
                reactBody = "Ты изменил время, отрицание, владельца и падеж, сохранив одну мысль. Оценки сохранены в расписании повторений.",
                webBody = "Пять преобразований завершены. Оценки сохранены в расписании повторений.",
            ),
        ),
    ),
)

val TRAINING_ROW_ROWS: List<TrainingRow> = trainingRowRowsPart0()

private fun stemAlternationRowRowsPart0(): List<StemAlternationRow> = listOf(
    StemAlternationRow(a = "ó", b = "o"),
)

val STEM_ALTERNATION_ROW_ROWS: List<StemAlternationRow> = stemAlternationRowRowsPart0()

private fun referenceRowRowsPart0(): List<ReferenceRow> = listOf(
    ReferenceRow(
        matrixIntroduction = "Один небольшой словарь. Видно, что меняется при каждой операции.",
        webCaseCompositionHeader = "Местоимение + прилагательное + существительное",
        maleAccIntro = "Для единственного числа сначала определи тип существительного.",
        comparisonNounIds = listOf("husband", "friendM", "dog", "house", "wife", "book", "child"),
        contextHelp = ReactCompactRow(
            react = "Красным показана старая часть, золотым — новая. Местоимение и прилагательное согласуются с существительным.",
            compact = "Местоимение и прилагательное согласуются с существительным. jego, jej, ich не изменяются.",
        ),
        aspectNoPresent = AspectNoPresentRow(
            compact = "Нет настоящего времени",
            ios = "Настоящее: нет настоящего времени",
        ),
        genderNames = GenderNamesRow(
            m_personal = "мужской · человек",
            m_animate = "мужской · животное",
            m_inanimate = "мужской · предмет",
            f = "женский",
            n = "средний",
        ),
        caseRows = listOf(
            RefCaseRow(id = "nom", pl = "Mianownik", ru = "Именительный", question = "kto? co?", trigger = "To jest…"),
            RefCaseRow(
                id = "gen",
                pl = "Dopełniacz",
                ru = "Родительный",
                question = "kogo? czego?",
                trigger = "nie widzę / nie mam / bez / do",
                skill = "case.gen.neg",
            ),
            RefCaseRow(
                id = "dat",
                pl = "Celownik",
                ru = "Дательный",
                question = "komu? czemu?",
                trigger = "daję / pomagam",
                skill = "case.dat",
            ),
            RefCaseRow(
                id = "acc",
                pl = "Biernik",
                ru = "Винительный",
                question = "kogo? co?",
                trigger = "widzę / mam / lubię",
                skill = "case.acc.f",
            ),
            RefCaseRow(
                id = "inst",
                pl = "Narzędnik",
                ru = "Творительный",
                question = "kim? czym?",
                trigger = "z (с) / jestem",
                skill = "case.inst",
            ),
            RefCaseRow(
                id = "loc",
                pl = "Miejscownik",
                ru = "Предложный",
                question = "o kim? o czym?",
                trigger = "o / w / na (место)",
                skill = "case.loc",
            ),
            RefCaseRow(
                id = "voc",
                pl = "Wołacz",
                ru = "Обращение",
                question = "обращаемся к кому-то",
                trigger = "Żono! Kolego!",
            ),
        ),
        chainRows = listOf(
            RefChainRow(
                label = "База",
                from = "To jest moja piękna żona.",
                to = "Widzę moją piękną żonę.",
                change = "Biernik всей группы",
            ),
            RefChainRow(
                label = "Прошедшее",
                from = "Widzę moją piękną żonę.",
                to = "Widziałem moją piękną żonę.",
                change = "widzę → widziałem",
            ),
            RefChainRow(
                label = "Отрицание",
                from = "Widziałem moją piękną żonę.",
                to = "Nie widziałem mojej pięknej żony.",
                change = "вся группа: Biernik → Dopełniacz",
            ),
            RefChainRow(
                label = "Их вместо моей",
                from = "Nie widziałem mojej pięknej żony.",
                to = "Nie widziałem ich pięknej żony.",
                change = "mojej → ich; ich не склоняется",
            ),
            RefChainRow(
                label = "Говорю о…",
                from = "Nie widziałem ich pięknej żony.",
                to = "Mówię o ich pięknej żonie.",
                change = "o → Miejscownik",
            ),
        ),
        tenseRows = listOf(
            RefTenseRow(
                label = "Сейчас",
                from = "Moja piękna żona idzie do domu.",
                to = "Moja piękna żona idzie do domu.",
            ),
            RefTenseRow(
                label = "Прошедшее",
                from = "Moja piękna żona idzie do domu.",
                to = "Moja piękna żona szła do domu.",
            ),
            RefTenseRow(
                label = "Будущее · процесс",
                from = "Moja piękna żona szła do domu.",
                to = "Moja piękna żona będzie szła do domu.",
            ),
            RefTenseRow(
                label = "Отрицание",
                from = "Moja piękna żona idzie do domu.",
                to = "Moja piękna żona nie idzie do domu.",
            ),
            RefTenseRow(
                label = "Вопрос",
                from = "Moja piękna żona idzie do domu.",
                to = "Czy moja piękna żona idzie do domu?",
            ),
        ),
        aspectRows = listOf(
            RefAspectRow(
                label = "robić · процесс",
                from = "robić",
                present = "robię",
                past = "robiłem / robiłam",
                future = "będę robić",
            ),
            RefAspectRow(
                label = "zrobić · результат",
                from = "zrobić",
                present = null,
                past = "zrobiłem / zrobiłam",
                future = "zrobię",
            ),
            RefAspectRow(
                label = "kupować · процесс",
                from = "kupować",
                present = "kupuję",
                past = "kupowałem / kupowałam",
                future = "będę kupować",
            ),
            RefAspectRow(
                label = "kupić · результат",
                from = "kupić",
                present = null,
                past = "kupiłem / kupiłam",
                future = "kupię",
            ),
        ),
        maleAccRows = listOf(
            RefMaleAccRow(
                id = "person",
                label = "Человек",
                title = "mąż / kolega",
                examples = listOf(
                    RefMaleAccExampleRow(
                        from = "mój dobry mąż",
                        to = "mojego dobrego męża",
                        sentence = "Widzę mojego dobrego męża.",
                    ),
                    RefMaleAccExampleRow(
                        from = "mój dobry kolega",
                        to = "mojego dobrego kolegę",
                        sentence = "Widzę mojego dobrego kolegę.",
                    ),
                ),
                rule = "На согласную: как Gen. На -a: -ę у существительного, но -ego у местоимения и прилагательного.",
            ),
            RefMaleAccRow(
                id = "animal",
                label = "Животное",
                title = "pies / kot",
                examples = listOf(
                    RefMaleAccExampleRow(
                        from = "mój dobry pies",
                        to = "mojego dobrego psa",
                        sentence = "Widzę mojego dobrego psa.",
                    ),
                ),
                rule = "Винительный как родительный: pies → psa.",
            ),
            RefMaleAccRow(
                id = "object",
                label = "Предмет",
                title = "dom / samochód",
                examples = listOf(
                    RefMaleAccExampleRow(
                        from = "mój nowy samochód",
                        to = "mój nowy samochód",
                        sentence = "Widzę mój nowy samochód.",
                    ),
                ),
                rule = "Винительный как именительный: группа сохраняет базовую форму.",
            ),
        ),
        systemCards = listOf(
            RefSystemCardRow(
                id = "verb.participles",
                title = "Причастия и деепричастия",
                explanation = "Различай действительное, страдательное и одновременное деепричастие; проверяй субъект и согласование.",
                example = "Kobieta czytająca gazetę czeka na pociąg.",
                steps = listOf("Найди деятеля и объект", "Выбери тип формы", "Проверь субъект и согласование"),
            ),
            RefSystemCardRow(
                id = "voice.impersonal",
                title = "Безличные формы и пассив",
                explanation = "Различай безличную форму -no/-to и пассив с согласованным причастием.",
                example = "Droga została naprawiona przez robotników.",
                steps = listOf(
                    "Определи деятеля и объект",
                    "Выбери безличность или пассив",
                    "Проверь падеж и согласование",
                ),
            ),
            RefSystemCardRow(
                id = "wordform.nouns",
                title = "Образование существительных",
                explanation = "Образуй название действия или качества и проверь зависимые слова.",
                example = "Czytanie książek sprawia mi radość.",
                steps = listOf("Определи глагол или признак", "Образуй существительное", "Проверь зависимый падеж"),
            ),
            RefSystemCardRow(
                id = "numeral.agreement",
                title = "Числительные и согласование",
                explanation = "Проверяй число, класс существительного, его падеж и форму сказуемого.",
                example = "W pokoju stoi pięć krzeseł.",
                steps = listOf("Выбери форму числительного", "Проверь существительное", "Проверь сказуемое"),
            ),
            RefSystemCardRow(
                id = "wordform.adjectives",
                title = "Образование прилагательных",
                explanation = "Выбери производное прилагательное по значению и согласуй с существительным.",
                example = "To jest drewniany stół.",
                steps = listOf("Определи отношение", "Проверь производную основу", "Согласуй окончание"),
            ),
            RefSystemCardRow(
                id = "sentence.connectives",
                title = "Союзы и смысловые связи",
                explanation = "Выбери союз по причинной, условной, целевой, уступительной или временной связи.",
                example = "Zostaję w domu, ponieważ pada deszcz.",
                steps = listOf("Назови смысл связи", "Вставь требуемый союз", "Проверь времена и пунктуацию"),
            ),
            RefSystemCardRow(
                id = "sentence.rephrase",
                title = "Смысловое преобразование фраз",
                explanation = "Меняй форму предложения, сохраняя участников, отрицание, причину и время.",
                example = "Nikt oprócz Anny nie zna odpowiedzi.",
                steps = listOf("Выдели утверждение", "Примени указанную конструкцию", "Сверь неизменный смысл"),
            ),
            RefSystemCardRow(
                id = "case.valency",
                title = "Управление польских глаголов",
                explanation = "Выбирай предлог и падеж по конкретному польскому глаголу.",
                example = "Czekam na autobus.",
                steps = listOf("Назови глагол", "Проверь рамку управления", "Поставь дополнение в нужную форму"),
            ),
            RefSystemCardRow(
                id = "verb.mood",
                title = "Условное и повелительное наклонение",
                explanation = "Различай условие, команду и вежливую просьбу; проверяй род, лицо и вид.",
                example = "Czy mógłbyś mi pomóc?",
                steps = listOf("Определи намерение", "Выбери лицо, род и вид", "Собери целое предложение"),
            ),
            RefSystemCardRow(
                id = "degree.comparison",
                title = "Степени сравнения",
                explanation = "Простая, нерегулярная и описательная сравнительная степень; согласование с существительным. После od — Dopełniacz.",
                example = "Ten most jest dłuższy od tamtego mostu.",
                steps = listOf("Выбери нужную степень", "Согласуй прилагательное", "После od проверь Dopełniacz"),
            ),
            RefSystemCardRow(
                id = "noun",
                title = "Существительное",
                explanation = "Род × число × падеж",
                example = "żona → żonę → żony",
                steps = listOf("żona", "żonę", "żony"),
            ),
            RefSystemCardRow(
                id = "agreement",
                title = "Прилагательное и владелец",
                explanation = "Копируют род, число и падеж",
                example = "moja piękna → moją piękną",
                steps = listOf("moja piękna", "moją piękną"),
            ),
            RefSystemCardRow(
                id = "verb",
                title = "Глагол",
                explanation = "Лицо × число × время × вид",
                example = "widzę → widziałem → będę widzieć",
                steps = listOf("widzę", "widziałem", "będę widzieć"),
            ),
            RefSystemCardRow(
                id = "modifiers",
                title = "Модификаторы",
                explanation = "Отрицание · вопрос · владелец",
                example = "Widzę… → Nie widzę… → Czy widzę…?",
                steps = listOf("Widzę…", "Nie widzę…", "Czy widzę…?"),
            ),
        ),
        pipeline = RefPipelineRow(
            title = "Сначала конструкция, затем формы",
            steps = listOf(
                RefPipelineStepRow(
                    id = "intent",
                    label = "01 · Смысл",
                    question = "Что хочу сказать?",
                    example = "Вижу / не вижу / говорю о…",
                ),
                RefPipelineStepRow(
                    id = "case",
                    label = "02 · Операция",
                    question = "Какой падеж нужен?",
                    example = "widzę → Biernik",
                ),
                RefPipelineStepRow(
                    id = "agreement",
                    label = "03 · Согласование",
                    question = "Меняю всю группу",
                    example = "moją + piękną + żonę",
                ),
            ),
            compactExample = "widzę → Biernik → moją + piękną + żonę",
        ),
        caseTeaching = CaseTeachingRow(
            caseNote = ReactCompactRow(
                react = "Wołacz показан как форма обращения; с неодушевлёнными словами обычно используется только стилистически. «Zachwycam się…» = «Восхищаюсь…» (Narzędnik), «Przyglądam się…» — Celownik.",
                compact = "Wołacz — форма обращения. «Zachwycam się…» требует Narzędnik; «Przyglądam się…» — Celownik.",
            ),
            comparisonReadingHint = "Читай по строке, чтобы сравнить типы. По столбцу — чтобы увидеть все формы одного слова.",
        ),
        verbTeaching = VerbTeachingRow(
            subjects = listOf(
                VerbSubjectRow(
                    id = "ja",
                    person = 1,
                    number = "sg",
                    genderMode = VerbSubjectRowGenderMode.SELECTED,
                    label = FullCompactRow(full = "ja — я", compact = "ja — я"),
                ),
                VerbSubjectRow(
                    id = "ty",
                    person = 2,
                    number = "sg",
                    genderMode = VerbSubjectRowGenderMode.SELECTED,
                    label = FullCompactRow(full = "ty — ты", compact = "ty — ты"),
                ),
                VerbSubjectRow(
                    id = "on",
                    person = 3,
                    number = "sg",
                    genderMode = VerbSubjectRowGenderMode.FIXED,
                    fixedGender = "m-personal",
                    label = FullCompactRow(full = "on — он", compact = "on — он"),
                ),
                VerbSubjectRow(
                    id = "ona",
                    person = 3,
                    number = "sg",
                    genderMode = VerbSubjectRowGenderMode.FIXED,
                    fixedGender = "f",
                    label = FullCompactRow(full = "ona — она", compact = "ona — она"),
                ),
                VerbSubjectRow(
                    id = "ono",
                    person = 3,
                    number = "sg",
                    genderMode = VerbSubjectRowGenderMode.FIXED,
                    fixedGender = "n",
                    label = FullCompactRow(full = "ono — оно", compact = "ono — оно"),
                ),
                VerbSubjectRow(
                    id = "my",
                    person = 1,
                    number = "pl",
                    genderMode = VerbSubjectRowGenderMode.SELECTED,
                    label = FullCompactRow(full = "my — мы", compact = "my — мы"),
                ),
                VerbSubjectRow(
                    id = "wy",
                    person = 2,
                    number = "pl",
                    genderMode = VerbSubjectRowGenderMode.SELECTED,
                    label = FullCompactRow(full = "wy — вы", compact = "wy — вы"),
                ),
                VerbSubjectRow(
                    id = "oni",
                    person = 3,
                    number = "pl",
                    genderMode = VerbSubjectRowGenderMode.FIXED,
                    fixedGender = "m-personal",
                    label = FullCompactRow(full = "oni — мужская личная группа", compact = "oni — мужская группа"),
                ),
                VerbSubjectRow(
                    id = "one",
                    person = 3,
                    number = "pl",
                    genderMode = VerbSubjectRowGenderMode.FIXED,
                    fixedGender = "f",
                    label = FullCompactRow(full = "one — остальные", compact = "one — остальные"),
                ),
            ),
            genderControlLabel = FullCompactRow(full = "Род для ja / ty / my / wy", compact = "Род"),
            genderOptions = listOf(
                VerbGenderOptionRow(
                    id = "m",
                    label = FullCompactRow(full = "Мужской / мужская личная группа", compact = "Мужской"),
                ),
                VerbGenderOptionRow(
                    id = "f",
                    label = FullCompactRow(full = "Женский / женская группа", compact = "Женский"),
                ),
            ),
            tenseLabels = TenseLabelsRow(
                present = FullCompactRow(full = "Teraz · сейчас", compact = "Teraz"),
                past = FullCompactRow(full = "Przeszłość · прошлое", compact = "Przeszłość"),
                future = FullCompactRow(full = "Przyszłość · будущее", compact = "Przyszłość"),
            ),
            futureExplanation = ReactCompactRow(
                react = "Составное будущее: będę + инфинитив (będę robić) или форма на -ł с родом и числом (będę robił / robiła). У być: będę, без второго глагола.",
                compact = "Составное будущее: będę + инфинитив или форма на -ł. У być: będę, без второго глагола.",
            ),
        ),
        pronounTeaching = PronounTeachingRow(
            personal = PersonalTeachingRow(
                title = "Личные местоимения в конструкциях",
                intro = ReactCompactRow(
                    react = "Вместо отдельной формы запоминай её место в предложении. После предлогов у местоимений третьего лица появляется n-.",
                    compact = "После предлогов у местоимений третьего лица появляется n-.",
                ),
                footer = FooterRow(
                    react = "Здесь обычные безударные формы. Для ударения или противопоставления: mnie, tobie, jego, jemu. После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.",
                    web = "Здесь обычные безударные формы. После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.",
                    native = "После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.",
                ),
                pronounIds = listOf("ja", "ty", "on", "ona", "ono", "my", "wy", "oni", "one"),
            ),
            contexts = listOf(
                PronounContextRow(
                    id = "gen",
                    caseId = "gen",
                    cue = CueRow(full = "Nie widzę…", compact = "Nie widzę…", ios = "Nie widzę"),
                    caseName = "Dopełniacz",
                ),
                PronounContextRow(
                    id = "dat",
                    caseId = "dat",
                    cue = CueRow(full = "Daję prezent…", compact = "Daję prezent…", ios = "Daję prezent"),
                    caseName = "Celownik",
                ),
                PronounContextRow(
                    id = "acc",
                    caseId = "acc",
                    cue = CueRow(full = "Widzę…", compact = "Widzę…", ios = "Widzę"),
                    caseName = "Biernik",
                ),
                PronounContextRow(
                    id = "inst",
                    caseId = "inst",
                    cue = CueRow(full = "Idę z…", compact = "Idę z…", ios = "Idę"),
                    caseName = "Narzędnik",
                    valuePrefix = "z ",
                    specialValues = listOf(SpecialValueRow(pronounId = "ja", value = "ze mną")),
                ),
                PronounContextRow(
                    id = "loc",
                    caseId = "loc",
                    cue = CueRow(full = "Mówię o…", compact = "Mówię o…", ios = "Mówię o"),
                    caseName = "Miejscownik",
                    valuePrefix = "o ",
                ),
            ),
            possessive = PossessiveTeachingRow(
                title = "Владелец меняется независимо от падежа",
                demo = PossessiveDemoRow(
                    nounId = "wife",
                    adjectiveId = "beautiful",
                    number = "sg",
                    cases = listOf(
                        DemoCaseRow(
                            id = "nom",
                            caption = CaptionRow(web = "Mianownik", desktop = "Mianownik", ios = "Mianownik"),
                        ),
                        DemoCaseRow(
                            id = "acc",
                            caption = CaptionRow(web = "Widzę… · Biernik", desktop = "Biernik", ios = "Biernik"),
                        ),
                        DemoCaseRow(
                            id = "gen",
                            caption = CaptionRow(
                                web = "Nie widzę… · Dopełniacz",
                                desktop = "Dopełniacz",
                                ios = "Dopełniacz",
                            ),
                        ),
                    ),
                    invariableOwnerIds = listOf("his", "her", "their"),
                    rule = DemoRuleRow(invariable = "Владелец не склоняется", variable = "Согласуется с żona"),
                ),
            ),
        ),
        russianSupport = RussianSupportSectionRow(
            title = FullCompactRow(
                full = "Опора на русский: что переносится, а что проверить",
                compact = "Опора на русский",
            ),
            columns = listOf("Русская опора", "Польская конструкция", "Проверка"),
            rows = listOf(
                SupportRow(
                    id = "accusative",
                    cue = "вижу кого? что?",
                    react = ConstructionCheckRow(
                        construction = "Widzę moją żonę.",
                        check = "Логика винительного знакома; польские окончания нужно менять во всей группе.",
                    ),
                    web = ConstructionCheckRow(
                        construction = "Widzę moją żonę.",
                        check = "Польские окончания меняются во всей группе.",
                    ),
                    desktop = ConstructionCheckRow(
                        construction = "Widzę moją żonę.",
                        check = "Окончания меняются во всей группе",
                    ),
                    mobileLine = "вижу кого? что? → Widzę moją żonę. Окончания меняются во всей группе.",
                    comparisons = listOf(
                        ContrastRow(
                            from = "moja żona",
                            to = "moją żonę",
                            beforeParts = listOf(
                                EndingPartRow(text = "moj", isChanged = false, isEnding = false),
                                EndingPartRow(text = "a", isChanged = true, isEnding = true),
                                EndingPartRow(text = " ", isChanged = false, isEnding = false),
                                EndingPartRow(text = "żon", isChanged = false, isEnding = false),
                                EndingPartRow(text = "a", isChanged = true, isEnding = true),
                            ),
                            afterParts = listOf(
                                EndingPartRow(text = "moj", isChanged = false, isEnding = false),
                                EndingPartRow(text = "ą", isChanged = true, isEnding = true),
                                EndingPartRow(text = " ", isChanged = false, isEnding = false),
                                EndingPartRow(text = "żon", isChanged = false, isEnding = false),
                                EndingPartRow(text = "ę", isChanged = true, isEnding = true),
                            ),
                        ),
                    ),
                ),
                SupportRow(
                    id = "instrumental",
                    cue = "с моей женой",
                    react = ConstructionCheckRow(
                        construction = "z moją żoną",
                        check = "Польское женское -ą соответствует здесь творительному; это же окончание есть у прилагательного в Bierniku.",
                    ),
                    web = ConstructionCheckRow(
                        construction = "z moją żoną",
                        check = "Женское -ą здесь соответствует творительному.",
                    ),
                    desktop = ConstructionCheckRow(construction = "z moją żoną", check = "Женское -ą — творительный"),
                    mobileLine = "с моей женой → z moją żoną. Женское -ą — творительный.",
                    comparisons = listOf(
                        ContrastRow(
                            from = "moja żona",
                            to = "moją żoną",
                            beforeParts = listOf(
                                EndingPartRow(text = "moj", isChanged = false, isEnding = false),
                                EndingPartRow(text = "a", isChanged = true, isEnding = true),
                                EndingPartRow(text = " ", isChanged = false, isEnding = false),
                                EndingPartRow(text = "żon", isChanged = false, isEnding = false),
                                EndingPartRow(text = "a", isChanged = true, isEnding = true),
                            ),
                            afterParts = listOf(
                                EndingPartRow(text = "moj", isChanged = false, isEnding = false),
                                EndingPartRow(text = "ą", isChanged = true, isEnding = true),
                                EndingPartRow(text = " ", isChanged = false, isEnding = false),
                                EndingPartRow(text = "żon", isChanged = false, isEnding = false),
                                EndingPartRow(text = "ą", isChanged = true, isEnding = true),
                            ),
                        ),
                    ),
                ),
                SupportRow(
                    id = "locative",
                    cue = "говорю о жене",
                    react = ConstructionCheckRow(
                        construction = "mówię o żonie",
                        check = "Местный падеж требует предлога; żona → żonie.",
                    ),
                    web = ConstructionCheckRow(
                        construction = "mówię o żonie",
                        check = "Местный падеж требует предлога; żona → żonie.",
                    ),
                    desktop = ConstructionCheckRow(construction = "mówię o żonie", check = "Местный требует предлога"),
                    mobileLine = "говорю о жене → mówię o żonie. Местный требует предлога.",
                    comparisons = listOf(
                        ContrastRow(
                            from = "żona",
                            to = "żonie",
                            beforeParts = listOf(
                                EndingPartRow(text = "żon", isChanged = false, isEnding = false),
                                EndingPartRow(text = "a", isChanged = true, isEnding = true),
                            ),
                            afterParts = listOf(
                                EndingPartRow(text = "żon", isChanged = false, isEnding = false),
                                EndingPartRow(text = "ie", isChanged = true, isEnding = true),
                            ),
                        ),
                    ),
                ),
                SupportRow(
                    id = "possessive",
                    cue = "мой / его / их",
                    react = ConstructionCheckRow(
                        construction = "moją żonę / jego żonę / ich żonę",
                        check = "jego, jej, ich не склоняются. Формы mojego и mojej зависят от предмета обладания.",
                    ),
                    web = ConstructionCheckRow(
                        construction = "moją żonę / jego żonę / ich żonę",
                        check = "jego, jej, ich не склоняются.",
                    ),
                    desktop = ConstructionCheckRow(
                        construction = "moją / jego / ich żonę",
                        check = "jego, jej, ich не склоняются",
                    ),
                    mobileLine = "мой / его / их → moją / jego / ich żonę. jego, jej, ich не склоняются.",
                    comparisons = listOf(
                        ContrastRow(
                            from = "moją żonę",
                            to = "jego żonę",
                            beforeParts = listOf(
                                EndingPartRow(text = "moją", isChanged = true, isEnding = false),
                                EndingPartRow(text = " żonę", isChanged = false, isEnding = false),
                            ),
                            afterParts = listOf(
                                EndingPartRow(text = "jego", isChanged = true, isEnding = false),
                                EndingPartRow(text = " żonę", isChanged = false, isEnding = false),
                            ),
                        ),
                        ContrastRow(
                            from = "moją żonę",
                            to = "ich żonę",
                            beforeParts = listOf(
                                EndingPartRow(text = "moją", isChanged = true, isEnding = false),
                                EndingPartRow(text = " żonę", isChanged = false, isEnding = false),
                            ),
                            afterParts = listOf(
                                EndingPartRow(text = "ich", isChanged = true, isEnding = false),
                                EndingPartRow(text = " żonę", isChanged = false, isEnding = false),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    ),
)

val REFERENCE_ROW_ROWS: List<ReferenceRow> = referenceRowRowsPart0()

private fun morphologyRowRowsPart0(): List<MorphologyRow> = listOf(
    MorphologyRow(
        futureAuxiliary = FutureAuxiliaryRow(
            verbId = "be",
            forms = PresentRow(
                sg = PersonFormsRow(p1 = "będę", p2 = "będziesz", p3 = "będzie"),
                pl = PersonFormsRow(p1 = "będziemy", p2 = "będziecie", p3 = "będą"),
            ),
        ),
    ),
)

val MORPHOLOGY_ROW_ROWS: List<MorphologyRow> = morphologyRowRowsPart0()

private fun constructionRowRowsPart0(): List<ConstructionRow> = listOf(
    ConstructionRow(
        id = "core.argument.case-role",
        slots = listOf(
            SlotRow(
                name = "owner",
                category = "possessive",
                optional = true,
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(
                name = "adjective",
                category = "adjective",
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(name = "noun", category = "noun", requiredFeatures = listOf("Number", "Case")),
        ),
    ),
    ConstructionRow(
        id = "core.agreement.possessive",
        slots = listOf(
            SlotRow(
                name = "owner",
                category = "possessive",
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(
                name = "adjective",
                category = "adjective",
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(name = "noun", category = "noun", requiredFeatures = listOf("Number", "Case")),
        ),
    ),
    ConstructionRow(
        id = "core.number",
        slots = listOf(
            SlotRow(
                name = "owner",
                category = "possessive",
                optional = true,
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(
                name = "adjective",
                category = "adjective",
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(name = "noun", category = "noun", requiredFeatures = listOf("Number", "Case")),
        ),
    ),
    ConstructionRow(
        id = "core.sentence.polarity",
        slots = listOf(
            SlotRow(
                name = "owner",
                category = "possessive",
                optional = true,
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(
                name = "adjective",
                category = "adjective",
                requiredFeatures = listOf("Number", "Gender", "Case"),
                agreementSource = "noun",
            ),
            SlotRow(name = "noun", category = "noun", requiredFeatures = listOf("Number", "Case")),
        ),
        govFeature = "Case",
        govDefault = "acc",
        govWhen = listOf(GovWhenRow(condition = ConditionRow(feature = "Polarity", value = "neg"), result = "gen")),
    ),
    ConstructionRow(
        id = "core.verb.tense",
        slots = listOf(
            SlotRow(
                name = "verb",
                category = "verb",
                requiredFeatures = listOf("Tense", "Person", "Number"),
                requiredFeaturesWhen = listOf(
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Tense", value = "past"),
                        features = listOf("Tense", "Person", "Number", "Gender"),
                    ),
                ),
                agreementSource = "noun",
            ),
        ),
    ),
)

val CONSTRUCTION_ROW_ROWS: List<ConstructionRow> = constructionRowRowsPart0()

private fun skillRecipeRowRowsPart0(): List<SkillRecipeRow> = listOf(
    SkillRecipeRow(
        skill = "case.acc.n",
        source = TextSpecRow(
            kind = TextSpecRowKind.PREFIXED,
            spec = PhraseSpecRow(
                construction = "core.argument.case-role",
                bundle = listOf(
                    FeatureValueRow(feature = "Number", value = "sg"),
                    FeatureValueRow(feature = "Case", value = "nom"),
                ),
            ),
            prefixCase = "nom",
            punct = ".",
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "accPrompt",
        explanationKey = "accExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "nom"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(
                    kind = ReasonSpecRowKind.CLASSIFIED,
                    rules = listOf(
                        ReasonRuleRow(whenUnchanged = true, key = "accReasonUnchanged"),
                        ReasonRuleRow(
                            whenGender = "m-personal",
                            whenNomSingularSuffix = "a",
                            key = "accReasonPersonalA",
                        ),
                        ReasonRuleRow(key = "accReasonChanged"),
                    ),
                ),
            ),
        ),
        tags = listOf("acc"),
        includeNounGenderTag = true,
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "case.acc.f",
        source = TextSpecRow(
            kind = TextSpecRowKind.PREFIXED,
            spec = PhraseSpecRow(
                construction = "core.argument.case-role",
                bundle = listOf(
                    FeatureValueRow(feature = "Number", value = "sg"),
                    FeatureValueRow(feature = "Case", value = "nom"),
                ),
            ),
            prefixCase = "nom",
            punct = ".",
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "accPrompt",
        explanationKey = "accExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "nom"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(
                    kind = ReasonSpecRowKind.CLASSIFIED,
                    rules = listOf(
                        ReasonRuleRow(whenUnchanged = true, key = "accReasonUnchanged"),
                        ReasonRuleRow(
                            whenGender = "m-personal",
                            whenNomSingularSuffix = "a",
                            key = "accReasonPersonalA",
                        ),
                        ReasonRuleRow(key = "accReasonChanged"),
                    ),
                ),
            ),
        ),
        tags = listOf("acc"),
        includeNounGenderTag = true,
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "case.acc.m",
        source = TextSpecRow(
            kind = TextSpecRowKind.PREFIXED,
            spec = PhraseSpecRow(
                construction = "core.argument.case-role",
                bundle = listOf(
                    FeatureValueRow(feature = "Number", value = "sg"),
                    FeatureValueRow(feature = "Case", value = "nom"),
                ),
            ),
            prefixCase = "nom",
            punct = ".",
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "accPrompt",
        explanationKey = "accExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "nom"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(
                    kind = ReasonSpecRowKind.CLASSIFIED,
                    rules = listOf(
                        ReasonRuleRow(whenUnchanged = true, key = "accReasonUnchanged"),
                        ReasonRuleRow(
                            whenGender = "m-personal",
                            whenNomSingularSuffix = "a",
                            key = "accReasonPersonalA",
                        ),
                        ReasonRuleRow(key = "accReasonChanged"),
                    ),
                ),
            ),
        ),
        tags = listOf("acc"),
        includeNounGenderTag = true,
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "case.gen.neg",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenGenNeg",
            values = listOf(
                PatternValueRow(
                    name = "gen",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "neg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "genNegPrompt",
        explanationKey = "genNegExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "neg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "genNegReason"),
            ),
        ),
        tags = listOf("gen", "negation"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "case.inst",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "caseDrill",
            values = listOf(
                PatternValueRow(name = "start", value = TextValueRow(kind = TextValueRowKind.COPY, key = "instStart")),
                PatternValueRow(
                    name = "target",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "inst"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "instPrompt",
        explanationKey = "caseDrillExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "inst"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "instReason"),
            ),
        ),
        tags = listOf("inst"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "case.loc",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "caseDrill",
            values = listOf(
                PatternValueRow(name = "start", value = TextValueRow(kind = TextValueRowKind.COPY, key = "locStart")),
                PatternValueRow(
                    name = "target",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "loc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "locPrompt",
        explanationKey = "caseDrillExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "loc"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "locReason"),
            ),
        ),
        tags = listOf("loc"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "case.dat",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "caseDrill",
            values = listOf(
                PatternValueRow(name = "start", value = TextValueRow(kind = TextValueRowKind.COPY, key = "datStart")),
                PatternValueRow(
                    name = "target",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "dat"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "datPrompt",
        explanationKey = "caseDrillExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "dat"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "datReason"),
            ),
        ),
        tags = listOf("dat"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "agreement.my",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                            ownerLexeme = "my",
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.agreement.possessive",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        explanationKey = "ownerExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                        ownerLexeme = "my",
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.agreement.possessive",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "ownerReason"),
            ),
        ),
        tags = listOf("agreement", "acc"),
        ownerDraw = OwnerDrawRow(
            owners = listOf(
                OwnerChoiceRow(lexeme = "your", labelKey = "ownerYour"),
                OwnerChoiceRow(lexeme = "his", labelKey = "ownerHis"),
                OwnerChoiceRow(lexeme = "her", labelKey = "ownerHer"),
                OwnerChoiceRow(lexeme = "our", labelKey = "ownerOur"),
                OwnerChoiceRow(lexeme = "yourPlural", labelKey = "ownerYourPlural"),
                OwnerChoiceRow(lexeme = "their", labelKey = "ownerTheir"),
            ),
            promptPrefixKey = "ownerPromptPrefix",
            promptSuffixKey = "ownerPromptSuffix",
        ),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "verb.present",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "nom",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "nom"),
                            ),
                        ),
                    ),
                ),
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.verb.tense",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "past"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "nom",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "nom"),
                            ),
                        ),
                    ),
                ),
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.verb.tense",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "present"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "verbPresentPrompt",
        explanationKey = "verbExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "past"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "present"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "verbReason"),
            ),
        ),
        tags = listOf("verb", "present", "nom"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "verb.past",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "nom",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "nom"),
                            ),
                        ),
                    ),
                ),
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.verb.tense",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "present"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "nom",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "nom"),
                            ),
                        ),
                    ),
                ),
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.verb.tense",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "past"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "verbPastPrompt",
        explanationKey = "verbExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "present"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "past"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "verbReason"),
            ),
        ),
        tags = listOf("verb", "past", "nom"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "verb.future",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "nom",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "nom"),
                            ),
                        ),
                    ),
                ),
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.verb.tense",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "present"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "nom",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "nom"),
                            ),
                        ),
                    ),
                ),
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.verb.tense",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "future"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "verbFuturePrompt",
        explanationKey = "verbExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "present"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "future"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "verbReason"),
            ),
        ),
        accepted = listOf(
            TextSpecRow(
                kind = TextSpecRowKind.PATTERN,
                key = "verbFutureAccepted",
                values = listOf(
                    PatternValueRow(
                        name = "nom",
                        value = TextValueRow(
                            kind = TextValueRowKind.PHRASE,
                            capitalized = true,
                            spec = PhraseSpecRow(
                                construction = "core.argument.case-role",
                                bundle = listOf(
                                    FeatureValueRow(feature = "Number", value = "sg"),
                                    FeatureValueRow(feature = "Case", value = "nom"),
                                ),
                            ),
                        ),
                    ),
                    PatternValueRow(
                        name = "past",
                        value = TextValueRow(
                            kind = TextValueRowKind.PHRASE,
                            spec = PhraseSpecRow(
                                construction = "core.verb.tense",
                                bundle = listOf(
                                    FeatureValueRow(feature = "Tense", value = "past"),
                                    FeatureValueRow(feature = "Person", value = "3"),
                                    FeatureValueRow(feature = "Number", value = "sg"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        tags = listOf("verb", "future", "nom"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "aspect",
        staticOverride = StaticExerciseRow(
            sourceKey = "aspectSource",
            promptKey = "aspectPrompt",
            expectedKey = "aspectExpected",
            explanationKey = "aspectExplanation",
            acceptedKeys = listOf("aspectAccepted"),
            changeFromKey = "aspectFrom",
            changeToKey = "aspectTo",
            changeReasonKey = "aspectReason",
            nounId = "book",
            adjectiveId = "new",
            tags = listOf("aspect", "past"),
        ),
    ),
    SkillRecipeRow(
        skill = "pronouns",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PRONOUN,
                        genderToKey = listOf(
                            GenderPronounRow(gender = "f", pronounId = "ona"),
                            GenderPronounRow(gender = "n", pronounId = "ono"),
                            GenderPronounRow(gender = "default", pronounId = "on"),
                        ),
                        caseId = "acc",
                    ),
                ),
            ),
        ),
        promptKey = "pronounPrompt",
        explanationKey = "pronounExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PRONOUN,
                    genderToKey = listOf(
                        GenderPronounRow(gender = "f", pronounId = "ona"),
                        GenderPronounRow(gender = "n", pronounId = "ono"),
                        GenderPronounRow(gender = "default", pronounId = "on"),
                    ),
                    caseId = "acc",
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "pronounReason"),
            ),
        ),
        tags = listOf("pronoun", "acc"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "sentence.question",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "questionSource",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "questionExpected",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "questionPrompt",
        explanationKey = "questionExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(kind = TextValueRowKind.COPY, key = "questionChangeFrom"),
                to = TextValueRow(kind = TextValueRowKind.COPY, key = "questionChangeTo"),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "questionReason"),
            ),
        ),
        tags = listOf("question", "acc"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "sentence.plural",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.number",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "pl"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "pluralPrompt",
        explanationKey = "pluralExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.number",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "pl"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "pluralReason"),
            ),
        ),
        tags = listOf("plural", "acc"),
        numberOverride = "pl",
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "mixed",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "mixedMale",
            values = listOf(
                PatternValueRow(
                    name = "gen",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "neg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "mixedPrompt",
        explanationKey = "mixedExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(kind = TextValueRowKind.COPY, key = "mixedChangeFrom"),
                to = TextValueRow(kind = TextValueRowKind.COPY, key = "mixedChangeTo"),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "mixedVerbReason"),
            ),
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "neg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "mixedNounReason"),
            ),
        ),
        accepted = listOf(
            TextSpecRow(
                kind = TextSpecRowKind.PATTERN,
                key = "mixedFemale",
                values = listOf(
                    PatternValueRow(
                        name = "gen",
                        value = TextValueRow(
                            kind = TextValueRowKind.PHRASE,
                            spec = PhraseSpecRow(
                                construction = "core.sentence.polarity",
                                bundle = listOf(
                                    FeatureValueRow(feature = "Number", value = "sg"),
                                    FeatureValueRow(feature = "Polarity", value = "neg"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        tags = listOf("mixed", "gen", "past"),
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    SkillRecipeRow(
        skill = "degree.comparison",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "degreeOldSource",
                promptKey = "degreeOldPrompt",
                expectedKey = "degreeOldExpected",
                explanationKey = "degreeOldExplanation",
                acceptedKeys = emptyList(),
                changeFromKey = "degreeOldFrom",
                changeToKey = "degreeOldTo",
                changeReasonKey = "degreeOldReason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("degree", "comparison"),
            ),
            StaticExerciseRow(
                sourceKey = "degreeEasySource",
                promptKey = "degreeEasyPrompt",
                expectedKey = "degreeEasyExpected",
                explanationKey = "degreeEasyExplanation",
                acceptedKeys = emptyList(),
                changeFromKey = "degreeEasyFrom",
                changeToKey = "degreeEasyTo",
                changeReasonKey = "degreeEasyReason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("degree", "comparison"),
            ),
            StaticExerciseRow(
                sourceKey = "degreeGoodSource",
                promptKey = "degreeGoodPrompt",
                expectedKey = "degreeGoodExpected",
                explanationKey = "degreeGoodExplanation",
                acceptedKeys = emptyList(),
                changeFromKey = "degreeGoodFrom",
                changeToKey = "degreeGoodTo",
                changeReasonKey = "degreeGoodReason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("degree", "comparison"),
            ),
            StaticExerciseRow(
                sourceKey = "degreeAnalyticSource",
                promptKey = "degreeAnalyticPrompt",
                expectedKey = "degreeAnalyticExpected",
                explanationKey = "degreeAnalyticExplanation",
                acceptedKeys = emptyList(),
                changeFromKey = "degreeAnalyticFrom",
                changeToKey = "degreeAnalyticTo",
                changeReasonKey = "degreeAnalyticReason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("degree", "comparison"),
            ),
            StaticExerciseRow(
                sourceKey = "degreeSuperlativeSource",
                promptKey = "degreeSuperlativePrompt",
                expectedKey = "degreeSuperlativeExpected",
                explanationKey = "degreeSuperlativeExplanation",
                acceptedKeys = emptyList(),
                changeFromKey = "degreeSuperlativeFrom",
                changeToKey = "degreeSuperlativeTo",
                changeReasonKey = "degreeSuperlativeReason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("degree", "comparison"),
            ),
            StaticExerciseRow(
                sourceKey = "degreeCompareSource",
                promptKey = "degreeComparePrompt",
                expectedKey = "degreeCompareExpected",
                explanationKey = "degreeCompareExplanation",
                acceptedKeys = emptyList(),
                changeFromKey = "degreeCompareFrom",
                changeToKey = "degreeCompareTo",
                changeReasonKey = "degreeCompareReason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("degree", "comparison"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "verb.participles",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "participles1Source",
                promptKey = "participles1Prompt",
                expectedKey = "participles1Expected",
                explanationKey = "participles1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "participles1From",
                changeToKey = "participles1To",
                changeReasonKey = "participles1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("participles"),
            ),
            StaticExerciseRow(
                sourceKey = "participles2Source",
                promptKey = "participles2Prompt",
                expectedKey = "participles2Expected",
                explanationKey = "participles2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "participles2From",
                changeToKey = "participles2To",
                changeReasonKey = "participles2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("participles"),
            ),
            StaticExerciseRow(
                sourceKey = "participles3Source",
                promptKey = "participles3Prompt",
                expectedKey = "participles3Expected",
                explanationKey = "participles3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "participles3From",
                changeToKey = "participles3To",
                changeReasonKey = "participles3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("participles"),
            ),
            StaticExerciseRow(
                sourceKey = "participles4Source",
                promptKey = "participles4Prompt",
                expectedKey = "participles4Expected",
                explanationKey = "participles4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "participles4From",
                changeToKey = "participles4To",
                changeReasonKey = "participles4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("participles"),
            ),
            StaticExerciseRow(
                sourceKey = "participles5Source",
                promptKey = "participles5Prompt",
                expectedKey = "participles5Expected",
                explanationKey = "participles5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "participles5From",
                changeToKey = "participles5To",
                changeReasonKey = "participles5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("participles"),
            ),
            StaticExerciseRow(
                sourceKey = "participles6Source",
                promptKey = "participles6Prompt",
                expectedKey = "participles6Expected",
                explanationKey = "participles6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "participles6From",
                changeToKey = "participles6To",
                changeReasonKey = "participles6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("participles"),
            ),
        ),
    ),
)

private fun skillRecipeRowRowsPart1(): List<SkillRecipeRow> = listOf(
    SkillRecipeRow(
        skill = "voice.impersonal",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "voice1Source",
                promptKey = "voice1Prompt",
                expectedKey = "voice1Expected",
                explanationKey = "voice1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "voice1From",
                changeToKey = "voice1To",
                changeReasonKey = "voice1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("voice"),
            ),
            StaticExerciseRow(
                sourceKey = "voice2Source",
                promptKey = "voice2Prompt",
                expectedKey = "voice2Expected",
                explanationKey = "voice2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "voice2From",
                changeToKey = "voice2To",
                changeReasonKey = "voice2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("voice"),
            ),
            StaticExerciseRow(
                sourceKey = "voice3Source",
                promptKey = "voice3Prompt",
                expectedKey = "voice3Expected",
                explanationKey = "voice3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "voice3From",
                changeToKey = "voice3To",
                changeReasonKey = "voice3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("voice"),
            ),
            StaticExerciseRow(
                sourceKey = "voice4Source",
                promptKey = "voice4Prompt",
                expectedKey = "voice4Expected",
                explanationKey = "voice4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "voice4From",
                changeToKey = "voice4To",
                changeReasonKey = "voice4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("voice"),
            ),
            StaticExerciseRow(
                sourceKey = "voice5Source",
                promptKey = "voice5Prompt",
                expectedKey = "voice5Expected",
                explanationKey = "voice5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "voice5From",
                changeToKey = "voice5To",
                changeReasonKey = "voice5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("voice"),
            ),
            StaticExerciseRow(
                sourceKey = "voice6Source",
                promptKey = "voice6Prompt",
                expectedKey = "voice6Expected",
                explanationKey = "voice6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "voice6From",
                changeToKey = "voice6To",
                changeReasonKey = "voice6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("voice"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "wordform.nouns",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "nouns1Source",
                promptKey = "nouns1Prompt",
                expectedKey = "nouns1Expected",
                explanationKey = "nouns1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "nouns1From",
                changeToKey = "nouns1To",
                changeReasonKey = "nouns1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("nouns"),
            ),
            StaticExerciseRow(
                sourceKey = "nouns2Source",
                promptKey = "nouns2Prompt",
                expectedKey = "nouns2Expected",
                explanationKey = "nouns2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "nouns2From",
                changeToKey = "nouns2To",
                changeReasonKey = "nouns2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("nouns"),
            ),
            StaticExerciseRow(
                sourceKey = "nouns3Source",
                promptKey = "nouns3Prompt",
                expectedKey = "nouns3Expected",
                explanationKey = "nouns3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "nouns3From",
                changeToKey = "nouns3To",
                changeReasonKey = "nouns3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("nouns"),
            ),
            StaticExerciseRow(
                sourceKey = "nouns4Source",
                promptKey = "nouns4Prompt",
                expectedKey = "nouns4Expected",
                explanationKey = "nouns4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "nouns4From",
                changeToKey = "nouns4To",
                changeReasonKey = "nouns4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("nouns"),
            ),
            StaticExerciseRow(
                sourceKey = "nouns5Source",
                promptKey = "nouns5Prompt",
                expectedKey = "nouns5Expected",
                explanationKey = "nouns5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "nouns5From",
                changeToKey = "nouns5To",
                changeReasonKey = "nouns5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("nouns"),
            ),
            StaticExerciseRow(
                sourceKey = "nouns6Source",
                promptKey = "nouns6Prompt",
                expectedKey = "nouns6Expected",
                explanationKey = "nouns6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "nouns6From",
                changeToKey = "nouns6To",
                changeReasonKey = "nouns6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("nouns"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "numeral.agreement",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "numerals1Source",
                promptKey = "numerals1Prompt",
                expectedKey = "numerals1Expected",
                explanationKey = "numerals1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "numerals1From",
                changeToKey = "numerals1To",
                changeReasonKey = "numerals1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("numerals"),
            ),
            StaticExerciseRow(
                sourceKey = "numerals2Source",
                promptKey = "numerals2Prompt",
                expectedKey = "numerals2Expected",
                explanationKey = "numerals2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "numerals2From",
                changeToKey = "numerals2To",
                changeReasonKey = "numerals2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("numerals"),
            ),
            StaticExerciseRow(
                sourceKey = "numerals3Source",
                promptKey = "numerals3Prompt",
                expectedKey = "numerals3Expected",
                explanationKey = "numerals3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "numerals3From",
                changeToKey = "numerals3To",
                changeReasonKey = "numerals3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("numerals"),
            ),
            StaticExerciseRow(
                sourceKey = "numerals4Source",
                promptKey = "numerals4Prompt",
                expectedKey = "numerals4Expected",
                explanationKey = "numerals4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "numerals4From",
                changeToKey = "numerals4To",
                changeReasonKey = "numerals4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("numerals"),
            ),
            StaticExerciseRow(
                sourceKey = "numerals5Source",
                promptKey = "numerals5Prompt",
                expectedKey = "numerals5Expected",
                explanationKey = "numerals5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "numerals5From",
                changeToKey = "numerals5To",
                changeReasonKey = "numerals5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("numerals"),
            ),
            StaticExerciseRow(
                sourceKey = "numerals6Source",
                promptKey = "numerals6Prompt",
                expectedKey = "numerals6Expected",
                explanationKey = "numerals6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "numerals6From",
                changeToKey = "numerals6To",
                changeReasonKey = "numerals6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("numerals"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "wordform.adjectives",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "adjectives1Source",
                promptKey = "adjectives1Prompt",
                expectedKey = "adjectives1Expected",
                explanationKey = "adjectives1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "adjectives1From",
                changeToKey = "adjectives1To",
                changeReasonKey = "adjectives1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("adjectives"),
            ),
            StaticExerciseRow(
                sourceKey = "adjectives2Source",
                promptKey = "adjectives2Prompt",
                expectedKey = "adjectives2Expected",
                explanationKey = "adjectives2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "adjectives2From",
                changeToKey = "adjectives2To",
                changeReasonKey = "adjectives2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("adjectives"),
            ),
            StaticExerciseRow(
                sourceKey = "adjectives3Source",
                promptKey = "adjectives3Prompt",
                expectedKey = "adjectives3Expected",
                explanationKey = "adjectives3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "adjectives3From",
                changeToKey = "adjectives3To",
                changeReasonKey = "adjectives3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("adjectives"),
            ),
            StaticExerciseRow(
                sourceKey = "adjectives4Source",
                promptKey = "adjectives4Prompt",
                expectedKey = "adjectives4Expected",
                explanationKey = "adjectives4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "adjectives4From",
                changeToKey = "adjectives4To",
                changeReasonKey = "adjectives4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("adjectives"),
            ),
            StaticExerciseRow(
                sourceKey = "adjectives5Source",
                promptKey = "adjectives5Prompt",
                expectedKey = "adjectives5Expected",
                explanationKey = "adjectives5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "adjectives5From",
                changeToKey = "adjectives5To",
                changeReasonKey = "adjectives5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("adjectives"),
            ),
            StaticExerciseRow(
                sourceKey = "adjectives6Source",
                promptKey = "adjectives6Prompt",
                expectedKey = "adjectives6Expected",
                explanationKey = "adjectives6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "adjectives6From",
                changeToKey = "adjectives6To",
                changeReasonKey = "adjectives6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("adjectives"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "sentence.connectives",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "connectives1Source",
                promptKey = "connectives1Prompt",
                expectedKey = "connectives1Expected",
                explanationKey = "connectives1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "connectives1From",
                changeToKey = "connectives1To",
                changeReasonKey = "connectives1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("connectives"),
            ),
            StaticExerciseRow(
                sourceKey = "connectives2Source",
                promptKey = "connectives2Prompt",
                expectedKey = "connectives2Expected",
                explanationKey = "connectives2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "connectives2From",
                changeToKey = "connectives2To",
                changeReasonKey = "connectives2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("connectives"),
            ),
            StaticExerciseRow(
                sourceKey = "connectives3Source",
                promptKey = "connectives3Prompt",
                expectedKey = "connectives3Expected",
                explanationKey = "connectives3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "connectives3From",
                changeToKey = "connectives3To",
                changeReasonKey = "connectives3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("connectives"),
            ),
            StaticExerciseRow(
                sourceKey = "connectives4Source",
                promptKey = "connectives4Prompt",
                expectedKey = "connectives4Expected",
                explanationKey = "connectives4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "connectives4From",
                changeToKey = "connectives4To",
                changeReasonKey = "connectives4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("connectives"),
            ),
            StaticExerciseRow(
                sourceKey = "connectives5Source",
                promptKey = "connectives5Prompt",
                expectedKey = "connectives5Expected",
                explanationKey = "connectives5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "connectives5From",
                changeToKey = "connectives5To",
                changeReasonKey = "connectives5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("connectives"),
            ),
            StaticExerciseRow(
                sourceKey = "connectives6Source",
                promptKey = "connectives6Prompt",
                expectedKey = "connectives6Expected",
                explanationKey = "connectives6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "connectives6From",
                changeToKey = "connectives6To",
                changeReasonKey = "connectives6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("connectives"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "sentence.rephrase",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "rephrase1Source",
                promptKey = "rephrase1Prompt",
                expectedKey = "rephrase1Expected",
                explanationKey = "rephrase1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "rephrase1From",
                changeToKey = "rephrase1To",
                changeReasonKey = "rephrase1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("rephrase"),
            ),
            StaticExerciseRow(
                sourceKey = "rephrase2Source",
                promptKey = "rephrase2Prompt",
                expectedKey = "rephrase2Expected",
                explanationKey = "rephrase2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "rephrase2From",
                changeToKey = "rephrase2To",
                changeReasonKey = "rephrase2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("rephrase"),
            ),
            StaticExerciseRow(
                sourceKey = "rephrase3Source",
                promptKey = "rephrase3Prompt",
                expectedKey = "rephrase3Expected",
                explanationKey = "rephrase3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "rephrase3From",
                changeToKey = "rephrase3To",
                changeReasonKey = "rephrase3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("rephrase"),
            ),
            StaticExerciseRow(
                sourceKey = "rephrase4Source",
                promptKey = "rephrase4Prompt",
                expectedKey = "rephrase4Expected",
                explanationKey = "rephrase4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "rephrase4From",
                changeToKey = "rephrase4To",
                changeReasonKey = "rephrase4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("rephrase"),
            ),
            StaticExerciseRow(
                sourceKey = "rephrase5Source",
                promptKey = "rephrase5Prompt",
                expectedKey = "rephrase5Expected",
                explanationKey = "rephrase5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "rephrase5From",
                changeToKey = "rephrase5To",
                changeReasonKey = "rephrase5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("rephrase"),
            ),
            StaticExerciseRow(
                sourceKey = "rephrase6Source",
                promptKey = "rephrase6Prompt",
                expectedKey = "rephrase6Expected",
                explanationKey = "rephrase6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "rephrase6From",
                changeToKey = "rephrase6To",
                changeReasonKey = "rephrase6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("rephrase"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "case.valency",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "valency1Source",
                promptKey = "valency1Prompt",
                expectedKey = "valency1Expected",
                explanationKey = "valency1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "valency1From",
                changeToKey = "valency1To",
                changeReasonKey = "valency1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("valency"),
            ),
            StaticExerciseRow(
                sourceKey = "valency2Source",
                promptKey = "valency2Prompt",
                expectedKey = "valency2Expected",
                explanationKey = "valency2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "valency2From",
                changeToKey = "valency2To",
                changeReasonKey = "valency2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("valency"),
            ),
            StaticExerciseRow(
                sourceKey = "valency3Source",
                promptKey = "valency3Prompt",
                expectedKey = "valency3Expected",
                explanationKey = "valency3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "valency3From",
                changeToKey = "valency3To",
                changeReasonKey = "valency3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("valency"),
            ),
            StaticExerciseRow(
                sourceKey = "valency4Source",
                promptKey = "valency4Prompt",
                expectedKey = "valency4Expected",
                explanationKey = "valency4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "valency4From",
                changeToKey = "valency4To",
                changeReasonKey = "valency4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("valency"),
            ),
            StaticExerciseRow(
                sourceKey = "valency5Source",
                promptKey = "valency5Prompt",
                expectedKey = "valency5Expected",
                explanationKey = "valency5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "valency5From",
                changeToKey = "valency5To",
                changeReasonKey = "valency5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("valency"),
            ),
            StaticExerciseRow(
                sourceKey = "valency6Source",
                promptKey = "valency6Prompt",
                expectedKey = "valency6Expected",
                explanationKey = "valency6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "valency6From",
                changeToKey = "valency6To",
                changeReasonKey = "valency6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("valency"),
            ),
        ),
    ),
    SkillRecipeRow(
        skill = "verb.mood",
        staticPool = listOf(
            StaticExerciseRow(
                sourceKey = "mood1Source",
                promptKey = "mood1Prompt",
                expectedKey = "mood1Expected",
                explanationKey = "mood1Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "mood1From",
                changeToKey = "mood1To",
                changeReasonKey = "mood1Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("mood"),
            ),
            StaticExerciseRow(
                sourceKey = "mood2Source",
                promptKey = "mood2Prompt",
                expectedKey = "mood2Expected",
                explanationKey = "mood2Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "mood2From",
                changeToKey = "mood2To",
                changeReasonKey = "mood2Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("mood"),
            ),
            StaticExerciseRow(
                sourceKey = "mood3Source",
                promptKey = "mood3Prompt",
                expectedKey = "mood3Expected",
                explanationKey = "mood3Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "mood3From",
                changeToKey = "mood3To",
                changeReasonKey = "mood3Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("mood"),
            ),
            StaticExerciseRow(
                sourceKey = "mood4Source",
                promptKey = "mood4Prompt",
                expectedKey = "mood4Expected",
                explanationKey = "mood4Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "mood4From",
                changeToKey = "mood4To",
                changeReasonKey = "mood4Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("mood"),
            ),
            StaticExerciseRow(
                sourceKey = "mood5Source",
                promptKey = "mood5Prompt",
                expectedKey = "mood5Expected",
                explanationKey = "mood5Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "mood5From",
                changeToKey = "mood5To",
                changeReasonKey = "mood5Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("mood"),
            ),
            StaticExerciseRow(
                sourceKey = "mood6Source",
                promptKey = "mood6Prompt",
                expectedKey = "mood6Expected",
                explanationKey = "mood6Explanation",
                acceptedKeys = emptyList(),
                changeFromKey = "mood6From",
                changeToKey = "mood6To",
                changeReasonKey = "mood6Reason",
                nounId = "book",
                adjectiveId = "new",
                tags = listOf("mood"),
            ),
        ),
    ),
)

val SKILL_RECIPE_ROW_ROWS: List<SkillRecipeRow> = skillRecipeRowRowsPart0() + skillRecipeRowRowsPart1()

private fun chainStepRecipeRowRowsPart0(): List<ChainStepRecipeRow> = listOf(
    ChainStepRecipeRow(
        source = TextSpecRow(
            kind = TextSpecRowKind.PREFIXED,
            spec = PhraseSpecRow(
                construction = "core.argument.case-role",
                bundle = listOf(
                    FeatureValueRow(feature = "Number", value = "sg"),
                    FeatureValueRow(feature = "Case", value = "nom"),
                ),
            ),
            prefixCase = "nom",
            punct = ".",
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "chainAccPrompt",
        explanationKey = "chainAccReason",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "nom"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainAccReason"),
            ),
        ),
        tags = listOf("acc"),
        ownerOut = "my",
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    ChainStepRecipeRow(
        skill = "verb.past",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenAcc",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainPast",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "chainPastPrompt",
        explanationKey = "chainPastReason",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(kind = TextValueRowKind.COPY, key = "chainPastChangeFrom"),
                to = TextValueRow(kind = TextValueRowKind.COPY, key = "chainPastChangeTo"),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainPastReason"),
            ),
        ),
        tags = listOf("past", "acc"),
        ownerOut = "my",
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    ChainStepRecipeRow(
        skill = "case.gen.neg",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainPast",
            values = listOf(
                PatternValueRow(
                    name = "acc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "acc"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainNeg",
            values = listOf(
                PatternValueRow(
                    name = "gen",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "neg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "chainNegPrompt",
        explanationKey = "chainNegReason",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "acc"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "neg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainNegReason"),
            ),
        ),
        tags = listOf("gen", "negation"),
        ownerOut = "my",
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    ChainStepRecipeRow(
        skill = "agreement.my",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainNeg",
            values = listOf(
                PatternValueRow(
                    name = "gen",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "neg"),
                            ),
                            ownerLexeme = "my",
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainOwner",
            values = listOf(
                PatternValueRow(
                    name = "theirGen",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.agreement.possessive",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "gen"),
                            ),
                            ownerLexeme = "their",
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "chainOwnerPrompt",
        explanationKey = "chainOwnerReason",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "neg"),
                        ),
                        ownerLexeme = "my",
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.agreement.possessive",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "gen"),
                        ),
                        ownerLexeme = "their",
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainOwnerReason"),
            ),
        ),
        tags = listOf("gen", "agreement"),
        ownerOut = "their",
        tileTargetSlots = listOf("adjective", "noun"),
    ),
    ChainStepRecipeRow(
        skill = "case.loc",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainOwner",
            values = listOf(
                PatternValueRow(
                    name = "theirGen",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.agreement.possessive",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "gen"),
                            ),
                            ownerLexeme = "their",
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainLoc",
            values = listOf(
                PatternValueRow(
                    name = "theirLoc",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "loc"),
                            ),
                            ownerLexeme = "their",
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "chainLocPrompt",
        explanationKey = "chainLocReason",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.agreement.possessive",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "gen"),
                        ),
                        ownerLexeme = "their",
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "loc"),
                        ),
                        ownerLexeme = "their",
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainLocReason"),
            ),
        ),
        tags = listOf("loc"),
        ownerOut = "their",
        tileTargetSlots = listOf("adjective", "noun"),
    ),
)

val CHAIN_STEP_RECIPE_ROW_ROWS: List<ChainStepRecipeRow> = chainStepRecipeRowRowsPart0()

private fun recipeDefaultsRowRowsPart0(): List<RecipeDefaultsRow> = listOf(
    RecipeDefaultsRow(
        defaultOwnerLexeme = "my",
        verbLexeme = "go",
        firstSkillByGender = listOf(
            GenderSkillRow(gender = "f", skill = "case.acc.f"),
            GenderSkillRow(gender = "n", skill = "case.acc.n"),
            GenderSkillRow(gender = "default", skill = "case.acc.m"),
        ),
    ),
)

val RECIPE_DEFAULTS_ROW_ROWS: List<RecipeDefaultsRow> = recipeDefaultsRowRowsPart0()

private fun lifehackRowRowsPart0(): List<LifehackRow> = listOf(
    LifehackRow(
        id = "pl-ru.case.gen.neg.obligatory",
        skillId = "case.gen.neg",
        text = "В русском родительный падеж при отрицании — это опциональный, стилистически окрашенный выбор: «Я не вижу дом» и «Я не вижу дома» оба грамматичны, второй вариант более книжный. В польском при отрицании прямого дополнения родительный падеж ОБЯЗАТЕЛЕН, а не факультативен: «Nie widzę domu» — единственный правильный вариант, «Nie widzę dom» (винительный, как в утверждении «Widzę dom») грамматически неверно. Русская интуиция «можно и так, и так» здесь не работает.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — обязательность родительного падежа прямого дополнения при отрицании в польском, в контрасте с факультативностью того же явления в русском.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.inst.no-copula-present",
        skillId = "case.inst",
        text = "В польском связка «być» в настоящем времени не пропускается, и предикативное имя после неё стоит в творительном падеже: «Jestem lekarzem» (буквально «есть врачом»). В русском настоящем времени связка «быть» вообще не произносится, а именная часть сказуемого стоит в именительном: «Я врач», не «Я врачом». Отсюда системная ошибка переноса — пропуск «jestem» и/или замена творительного на именительный: *«Ja lekarz» вместо «Jestem lekarzem».",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Bielec, D. (1998). Polish: An Essential Grammar. Routledge — обязательность связки «być» и творительного падежа предикативного имени в настоящем времени, в контрасте с нулевой связкой русского настоящего времени.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.agreement.my.masculine-personal-plural",
        skillId = "agreement.my",
        text = "«Mój/moja/moje» согласуются с обладаемым по роду и числу — на первый взгляд как русские «мой/моя/моё». Но во множественном числе польский дополнительно различает «мужской личный» род (обладаемое — люди мужского пола) и все прочие: «moi bracia» (мои братья, мужской личный), но «moje psy» (мои собаки, не личный, хотя «пёс» — тоже мужской род в единственном числе). В русском такого расщепления по одушевлённости-«личности» во множественном числе нет — «мои» одно на всё. Опора на русскую форму даёт неверный выбор именно в множественном числе.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — категория rodzaj męskoosobowy (мужской личный род) во множественном числе и её отсутствие как отдельной согласовательной категории в русском.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.aspect.boundary-mismatch",
        skillId = "aspect",
        text = "И в русском, и в польском есть совершенный/несовершенный вид, и большинство глаголов интуитивно совпадают по видовой паре («написать»/«napisać», «писать»/«pisać»). Но границы вида — не всегда те же самые: есть глаголы, где польский выбирает несовершенный вид там, где русский естественно ждёт совершенный, и наоборот (типологически ожидаемое несовпадение видовых границ между близкородственными славянскими языками, а не исключение из правила). Совпадение видовых пар «в большинстве случаев» — это не гарантия совпадения «всегда»: конкретный глагол в конкретном контексте нужно проверять, не выводить автоматически по русской модели.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Dickey, S. M. (2000). Parameters of Slavic Aspect: A Cognitive Approach. CSLI Publications — межславянское сопоставление аспектуальных систем, включая несовпадение границ вида между близкородственными языками (в частности, польским и русским).",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.mixed.interleaving",
        skillId = "mixed",
        text = "Упражнения, где вперемешку встречаются разные конструкции (mixed), сложнее блочной тренировки «одно и то же по одной теме подряд» — но именно эта сложность и даёт более прочное запоминание на дистанции, потому что каждый раз заново заставляет распознать, какое из уже выученных правил (падеж, вид, согласование) применить, а не действовать по инерции предыдущего задания.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Rohrer, D., & Taylor, K. (2007). The shuffling of mathematics problems improves learning. Instructional Science, 35(6), 481–498 — эффект интерливинга (чередование задач разных типов) для долговременного запоминания, методически применимый к чередованию грамматических категорий независимо от конкретной языковой пары.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.acc.n.nom-acc-syncretism",
        skillId = "case.acc.n",
        text = "В обоих языках у существительных среднего рода в единственном числе именительный и винительный падеж совпадают буквально: русское «окно» → «вижу окно» (форма не меняется), польское «okno» → «widzę okno» — та же неизменная форма. Это один из немногих случаев винительного падежа, где русская интуиция срабатывает без всяких поправок: если существительное среднего рода, окончание винительного падежа менять не нужно вовсе.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — таблицы склонения существительных среднего рода: совпадение именительного и винительного падежа в единственном числе, параллельно русскому.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.acc.n.um-indeclinable",
        skillId = "case.acc.n",
        text = "Существительные среднего рода на -um (muzeum, liceum, gimnazjum, akwarium) в единственном числе не изменяются вообще ни по одному падежу, включая винительный: «widzę to muzeum» и «idę do tego muzeum» — форма «muzeum» одна и та же везде. Механизм иной, чем у русских несклоняемых заимствований («кино», «такси»), но результат для винительного падежа тот же самый — форма не меняется, значит и здесь ничего специально подстраивать не нужно.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Bielec, D. (1998). Polish: An Essential Grammar. Routledge — неизменяемость существительных среднего рода на -um во всех падежах единственного числа.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.acc.f.nasal-ending",
        skillId = "case.acc.f",
        text = "Основная модель винительного падежа женского рода — окончание -a меняется на носовое -ę: «książka» → «widzę książkę». Русское соответствие -a → -у/-ю («книга» → «книгу») учит менять окончание, но не готовит к тому, что польское -ę — это отдельный носовой звук (пишется с «хвостиком», ogonek), который часто произносят как обычное «е» или как «эн», теряя носовое качество. Ошибка не в выборе падежной модели (она угадана верно по аналогии с русским), а в произношении и написании самого нового звука.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Sadowska, I. (2012). Polish: A Comprehensive Grammar. Routledge — раздел о носовых гласных ą/ę, их произношении и типичных ошибках произношения у изучающих.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.acc.f.consonant-stem-unchanged",
        skillId = "case.acc.f",
        text = "Не все существительные женского рода заканчиваются на -a: те, что заканчиваются на мягкий согласный (noc — ночь, rzecz — вещь, postać — персонаж, pomoc — помощь), в винительном падеже вообще не меняются — «widzę tę rzecz» так же, как «to jest ta rzecz». Это прямая параллель с русским третьим склонением: «вижу эту вещь» — форма «вещь» тоже не меняется. Правило «женский род → окончание -ę» действует только для типа на -a, а не для этого второго, более редкого подтипа на согласный.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — два типа склонения женского рода (на -a и на мягкий согласный) и их падежное поведение.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.acc.m.animacy-split-matches-russian",
        skillId = "case.acc.m",
        text = "Винительный падеж мужского рода в единственном числе расходится по одушевлённости так же, как в русском: у неодушевлённых существительных винительный совпадает с именительным («widzę dom» — вижу дом, форма не меняется), а у одушевлённых (люди и животные) винительный совпадает с родительным («widzę psa» — вижу собаку, «psa» — та же форма, что и в родительном). Это не новая логика, которую нужно выучивать заново — это тот же самый принцип, что и в русском («вижу дом» / «вижу кота»), перенос безопасен, нужно только выучить сами окончания.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — одушевлённость как фактор совпадения винительного падежа с именительным либо родительным в мужском роде единственного числа.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.acc.m.a-ending-masculine",
        skillId = "case.acc.m",
        text = "Некоторые существительные мужского рода оканчиваются на -a (mężczyzna — мужчина, kolega — коллега, artysta — артист) и в винительном падеже склоняются по образцу женского склонения: «widzę tego mężczyznę», а не «mężczyznego». Это не специфически польское исключение — то же самое происходит и в русском: «мужчина», «коллега» тоже склоняются по форме как существительные женского рода («вижу мужчину», «вижу коллегу»), сохраняя при этом мужское согласование прилагательных и местоимений («этот мужчина», не «эта мужчина»). Логика одна и та же в обоих языках, различается только конкретный материал (окончание).",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — класс существительных мужского рода на -a и их склонение по женской модели.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.gen.neg.pronoun-go-jej",
        skillId = "case.gen.neg",
        text = "Родительный падеж при отрицании касается не только существительных, но и местоимений, заменяющих прямое дополнение — и здесь ловушка незаметнее, потому что для мужского и среднего рода винительный и родительный падеж местоимения 3-го лица совпадают по форме («go»): «Widzę go» (вижу его) и «Nie widzę go» (не вижу его) выглядят одинаково, будто падеж вообще не меняется. Для женского рода формы разные: «Widzę ją» (винительный), но «Nie widzę jej» (родительный) — забыть переключиться легко именно потому, что мужской род никакого сигнала об изменении не подаёт.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — склонение личных местоимений 3-го лица, совпадение винительного и родительного падежа «go» для мужского/среднего рода в отличие от женского «ją»/«jej».",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.inst.transport-no-preposition",
        skillId = "case.inst",
        text = "Творительный падеж без предлога для орудия действия работает одинаково в обоих языках: «pisać ołówkiem» = «писать карандашом» — перенос безопасен. Но для транспортных средств русский обычно добавляет предлог «на» + предложный падеж («ехать на поезде», «на автобусе»), а польский использует чистый творительный без предлога: «jechać pociągiem», «autobusem» — не «na pociągu». Механический перенос русской модели «на + место» на транспорт даёт лишний, неправильный предлог там, где польский требует голого творительного падежа.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Bielec, D. (1998). Polish: An Essential Grammar. Routledge — беспредложный творительный падеж для орудия и средства передвижения.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.loc.dative-locative-syncretism",
        skillId = "case.loc",
        text = "У существительных женского рода дательный и местный (предложный) падеж в единственном числе совпадают по форме буквально: «matka» → «matce» и в дательном («daję matce» — даю матери), и в местном («o matce» — о матери) — одна и та же форма. В русском происходит ровно то же самое: «мама» → «маме» и в дательном («даю маме»), и в предложном («о маме»). Если форма дательного падежа для существительного женского рода уже выучена, местный падеж для того же слова достаётся бесплатно — учить отдельно не нужно.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Sadowska, I. (2012). Polish: A Comprehensive Grammar. Routledge — таблица склонения женского рода: совпадение дательного и местного падежа в единственном числе.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.loc.consonant-alternation",
        skillId = "case.loc",
        text = "Местный падеж почти всегда сопровождается чередованием согласного перед окончанием -e, которого русский предложный падеж не знает: «Polska» → «w Polsce» (k→c), «miasto» → «w mieście» (st→ście), «matka» → «o matce» (k→c), «Warszawa» → «w Warszawie» (w→wi). Русское «в Польше», «о городе», «о матери» меняет только окончание, сам корень остаётся тем же. Выучить окончание -e недостаточно — нужно отдельно запоминать (или проверять), какое чередование происходит с последним согласным основы: это отдельный список правил, не автоматика.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Rothstein, R. A. (1993). Polish. In B. Comrie & G. Corbett (Eds.), The Slavonic Languages. Routledge — чередования согласных (палатализация) перед падежными окончаниями на передний гласный в польском склонении, в контрасте с ограниченностью таких чередований в русском.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.dat.dative-locative-syncretism",
        skillId = "case.dat",
        text = "Та же параллель работает и в обратную сторону: для существительных женского рода дательный падеж совпадает с местным — если уже выучена форма местного падежа («o żonie» — о жене), дательный («daję żonie» — даю жене) окажется той же самой формой. Русский язык устроен точно так же («о жене» / «даю жене» — одна форма «жене»), так что это не польская странность, которую нужно выучивать отдельно, а структурное сходство, которое можно использовать как готовую подсказку.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Sadowska, I. (2012). Polish: A Comprehensive Grammar. Routledge — таблица склонения женского рода: совпадение дательного и местного падежа в единственном числе.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.dat.owi-u-split",
        skillId = "case.dat",
        text = "В отличие от русского, где дательный падеж мужского рода единственного числа всегда получает одно и то же окончание -у/-ю («брату», «отцу», «сыну», «мальчику»), в польском действуют два конкурирующих окончания: продуктивное -owi (большинство существительных: «chłopcowi», «Adamowi») и более старое -u для ограниченного, но частого списка слов («ojcu» — отцу, «bratu» — брату, «panu» — господину, «Bogu» — богу, «światu» — миру). Русская модель «всегда -у» не подсказывает, какое слово из закрытого списка исключений нужно проверять по памяти, а не выводить по общему правилу.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — конкуренция окончаний -owi/-u в дательном падеже мужского рода единственного числа и лексический список исключений на -u.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.agreement.my.swoj-obligatory",
        skillId = "agreement.my",
        text = "Возвратное притяжательное «swój» обязательно, когда обладатель — само подлежащее того же предложения: «On kocha swoją żonę» (он любит свою [собственную] жену). Если вместо этого сказать «On kocha jego żonę», получится не тот же смысл с более разговорным оттенком, а буквально другой смысл — «жену другого мужчины», не его собственную. В русском разграничение «свою»/«его» в этой позиции тоже существует, но на практике часто нейтрализуется в разговорной речи без потери смысла — в польском такой нейтрализации нет, замена «swój» на «jego/jej» всегда меняет референцию.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — обязательное употребление возвратного притяжательного «swój» при кореференции с подлежащим.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.present.iotation-parallel",
        skillId = "verb.present",
        text = "Настоящее время многих польских глаголов образуется с тем же историческим чередованием согласных перед личным окончанием, что и в русском (общеславянская йотация): «pisać» → «pisze» (s→sz) — как русское «писать» → «пишет» (с→ш); «móc» → «może» (g→ż) — как русское «могу» → «может» (г→ж). Если чередование знакомо по русскому глаголу-родственнику, есть хороший шанс, что польский когнат мутирует тем же способом — не гарантия, но рабочая подсказка, а не случайность.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Sussex, R., & Cubberley, P. (2006). The Slavic Languages. Cambridge University Press — общеславянская йотация согласных перед личными окончаниями настоящего времени, общая для польского и русского спряжения.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.present.conjugation-class-unpredictable",
        skillId = "verb.present",
        text = "В русском практически все глаголы на -ать спрягаются одинаково в 3-м лице (читает, играет, работает — окончание -ает почти без исключений), поэтому по инфинитиву легко предсказать личную форму. В польском инфинитив на -ać такой гарантии не даёт: «czytać» → «czyta» (окончание -a), но «pisać» → «pisze» (окончание -e, с чередованием s→sz) — два разных типа спряжения при внешне одинаковом инфинитиве. Окончание инфинитива в польском подсказывает спряжение значительно слабее, чем в русском — форму 3-го лица нужно запоминать по каждому глаголу отдельно, а не выводить по шаблону.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — глава о классах спряжения: несколько разных моделей настоящего времени для глаголов на -ać.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.past.gender-agreement-matches",
        skillId = "verb.past",
        text = "Прошедшее время в 3-м лице согласуется с подлежащим по роду одинаково в обоих языках, по той же трёхчленной модели: «żona robiła» (она делала, женский род), «mąż robił» (он делал, мужской род), «dziecko robiło» (оно делало, средний род) — та же логика, что в русском «делала/делал/делало», просто с другими окончаниями (-ła/-ł/-ło вместо -ла/-л/-ло). Выбор формы по роду подлежащего переносится из русского напрямую и работает без исключений для этой модели.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Sussex, R., & Cubberley, P. (2006). The Slavic Languages. Cambridge University Press — общеславянское происхождение прошедшего времени на основе l-причастия с согласованием по роду, общее для польского и русского.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.past.nasal-vowel-alternation",
        skillId = "verb.past",
        text = "У глаголов на -nąć (zacząć — начать, zamknąć — закрыть, wyciągnąć — вытянуть) корневой гласный меняется в зависимости от рода прошедшего времени: перед нулевым окончанием мужского рода — «ą» (zaczął), перед -a/-o женского/среднего рода — «ę» (zaczęła, zaczęło). Русские соответствия («начал/начала/начало», «закрыл/закрыла/закрыло») никакого чередования корневого гласного не показывают — легко забыть переключить «ą» на «ę» при переходе от мужского рода к женскому или среднему, механически перенося русскую неизменность корня.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — чередование ą/ę в корне глаголов на -nąć в прошедшем времени, обусловленное родовым окончанием.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.future.perfective-present-form",
        skillId = "verb.future",
        text = "Совершенные глаголы образуют будущее время, просто спрягаясь по образцу настоящего времени, без вспомогательного глагола — точно тот же механизм, что и в русском: польское «napisać» → «napisze» (напишет) устроено ровно так же, как русское «написать» → «напишет». Раз перенос уже знаком по родному языку, для совершенных глаголов польское будущее время учить отдельно как новую конструкцию не нужно.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Dickey, S. M. (2000). Parameters of Slavic Aspect: A Cognitive Approach. CSLI Publications — «настоящая» форма совершенного глагола как регулярный способ выражения будущего времени в славянских языках, включая польский и русский.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.future.periphrastic-two-forms",
        skillId = "verb.future",
        text = "Для несовершенных глаголов польский допускает два равнозначных варианта будущего времени: «będzie» + инфинитив («będzie pisać») или «będzie» + причастие на -ł с согласованием по роду («będzie pisał/pisała/pisało»). В русском способ только один — «будет» + инфинитив («будет писать»), без какого-либо согласования по роду. Наличие второго, родового варианта в польском может сбивать, будто в будущем времени тоже нужно менять форму по роду, как в прошедшем — на самом деле оба варианта равноправны, и вариант с инфинитивом (без родовой формы) — самый безопасный и точный перенос русской модели.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — два равнозначных перифрастических способа образования будущего времени несовершенных глаголов.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.aspect.suppletive-pairs",
        skillId = "aspect",
        text = "Оба языка допускают, что видовая пара образуется не приставкой или суффиксом, а вообще другим корнем (супплетивно): русское «говорить/сказать» и польское «mówić/powiedzieć» — тот же самый тип нерегулярности, только на разном материале. Если для какого-то польского глагола видовая пара образована неожиданно, другим корнем, а не приставкой, — это не польская особенность, которую нужно осваивать с нуля, а общеславянская черта, с которой русский носитель уже интуитивно знаком на примере своего собственного языка.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Dickey, S. M. (2000). Parameters of Slavic Aspect: A Cognitive Approach. CSLI Publications — супплетивные видовые пары как общеславянское, а не изолированное польское явление.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.mixed.testing-effect",
        skillId = "mixed",
        text = "Смешанные упражнения (mixed) заставляют не просто применить правило, а сначала определить, какое из нескольких выученных правил применимо в данном случае — это дополнительный шаг «поиска в памяти», которого нет в блочной тренировке одного правила подряд. Именно эта необходимость активно вспоминать и выбирать нужное правило, а не просто повторно его применять, сама по себе улучшает долговременное запоминание.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Roediger, H. L., & Karpicke, J. D. (2006). Test-enhanced learning: Taking memory tests improves long-term retention. Psychological Science, 17(3), 249–255 — эффект тестирования: активное извлечение из памяти при решении задачи улучшает долговременное запоминание сильнее, чем повторное узнавание уже известного правила.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.pronouns.neuter-je-vs-go",
        skillId = "pronouns",
        text = "В русском местоимение 3-го лица в винительном падеже для мужского и среднего рода совпадает: «его» — и «вижу его» (про мужчину), и «вижу его» (про предмет среднего рода, вроде «окно»). В польском средний род получает отдельную форму «je», отличную от мужской «go»: «widzę go» (про мужчину или предмет мужского рода) против «widzę je» (про «dziecko» — ребёнка, среднего рода, или «okno» — окно). Русская привычка «одна форма на оба рода» подсказывает неверно выбрать «go» там, где нужно «je».",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — склонение личных местоимений 3-го лица: отдельная форма «je» для среднего рода в винительном падеже, в отличие от совпадающей русской формы «его» для мужского и среднего рода.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.pronouns.clitic-position",
        skillId = "pronouns",
        text = "Безударные местоимения-клитики (go, ją, je, mu, jej) не могут стоять в самом начале предложения — это жёсткое синтаксическое правило, а не стилистическая рекомендация: «Widzę go» (вижу его) — нормально, но начать предложение с «Go widzę» невозможно вообще. В русском такого позиционного запрета для «его/её» нет — «Его я вижу» вполне грамматично при особом порядке слов для акцента. Строя польское предложение с местоимением-объектом, нельзя механически переносить свободный русский порядок слов — местоимение должно уйти внутрь предложения.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Franks, S., & King, T. H. (2000). A Handbook of Slavic Clitics. Oxford University Press — позиционные ограничения безударных местоименных клитик в польском синтаксисе.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.sentence.question.czy-particle",
        skillId = "sentence.question",
        text = "В русском вопрос без вопросительного слова оформляется практически только интонацией: «Ты работаешь?» и «Ты работаешь.» отличаются на письме только знаком, порядок слов не меняется, специальная частица не обязательна. В польском нейтральный, наиболее естественный способ задать общий вопрос — поставить частицу «czy» в самое начало предложения: «Czy pracujesz?», а не голое «Pracujesz?» (последнее тоже встречается, но воспринимается как более разговорный вариант, не нейтральный по умолчанию). Русская привычка обходиться совсем без маркера регулярно даёт грамматически возможное, но более разговорное польское предложение там, где нейтральный вариант ожидает «czy».",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — частица «czy» как нейтральный маркер общего вопроса в начале предложения.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.sentence.question.embedded-czy-vs-li",
        skillId = "sentence.question",
        text = "Та же частица «czy» используется и в косвенных вопросах («не знаю, работает ли он» → «Nie wiem, czy on pracuje»), но стоит в начале придаточного, а не после сказуемого. Русская частица «ли» ведёт себя противоположно — она клитика второй позиции, обязательно стоящая после того слова (обычно глагола), которое находится под вопросом: «работает ли он», а не «ли он работает». Механический перенос позиции русской «ли» на польское «czy» даёт неверный порядок слов.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Franks, S., & King, T. H. (2000). A Handbook of Slavic Clitics. Oxford University Press — контраст между клитикой второй позиции (русское «ли») и частицей в начале клаузы (польское «czy») в вопросительных конструкциях славянских языков.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.sentence.plural.masculine-personal-category",
        skillId = "sentence.plural",
        text = "В русском множественном числе никакой отдельной категории «мужской личный род» не существует: прилагательные и глаголы согласуются одинаково независимо от того, о людях или не-людях речь («красивые дома» и «красивые мужчины» — одно и то же окончание). В польском множественное число обязательно расщепляется на два согласовательных класса: «mężczyźni byli» (мужчины были — męskoosobowy, «мужской личный») против «kobiety/dzieci/psy były» (женщины/дети/собаки были — niemęskoosobowy, всё остальное, включая женщин, детей и животных). Разные формы получают и прилагательное, и глагол в прошедшем времени, и местоимение «они» (oni/one). Русская интуиция «множественное число одно на всех» здесь не работает вообще — это отдельная грамматическая категория, которую нужно выучить как факт языка, а не выводить логически.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Corbett, G. G. (1991). Gender. Cambridge University Press — противопоставление męskoosobowy/niemęskoosobowy в польском множественном числе как классический пример уникального согласовательного класса.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.sentence.plural.accusative-split-mismatch",
        skillId = "sentence.plural",
        text = "В русском множественном числе винительный падеж повторяет родительный для любого одушевлённого существительного независимо от рода: «вижу этих мужчин», «вижу этих женщин», «вижу этих собак» — везде родительный. В польском родительную модель винительного падежа во множественном числе получают только существительные класса mężczyźni (mężczyzn, chłopców, lekarzy) — а «kobiety» (женщины), «dzieci» (дети) и животные (psy, koty) в винительном падеже множественного числа выглядят как именительный, без всякого сходства с родительным: «widzę te kobiety», «widzę te dzieci», «widzę te psy». Русская логика «одушевлённое значит родительный» здесь систематически подводит для женщин, детей и животных — в польском винительном падеже множественного числа они ведут себя как неличные, что противоречит и русской, и бытовой интуиции.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. A. (2002). A Grammar of Contemporary Polish. Slavica Publishers — морфология винительного падежа множественного числа: родительная модель только для mężczyźni-класса, именительная для всех прочих, включая одушевлённых нелично.",
            url = "https://www.slavica.com/grammar-of-contemporary-polish.html",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.degree.comparison.od-genitive",
        skillId = "degree.comparison",
        text = "Если сравниваешь два названных предмета через od, поставь второй в Dopełniacz: Ten most jest dłuższy od tamtego mostu. Русское «чем тот мост» не подсказывает польский падеж после od. Для сравнения целых высказываний употребляй niż.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan, O. E. (2002). A Grammar of Contemporary Polish, §6, Common Expressions Using Comparatives and Superlatives, p. 151: comparison with od + genitive or niż; od + genitive favoured for nouns.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.participles.source-rule",
        skillId = "verb.participles",
        text = "У формы на -ąc субъект тот же, что в главной части: Czytając list, Maria uśmiecha się. Русская конструкция с другим субъектом здесь не подходит.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 12, Participles, pp. 298–307: adjectival participles agree; adverbial participles are uninflected and share the main-clause subject.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.voice.impersonal.source-rule",
        skillId = "voice.impersonal",
        text = "В форме naprawiono drogę слово drogę остаётся дополнением; в Droga została naprawiona оно уже подлежащее. Не переноси согласование одной конструкции в другую.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 12, Passive and Impersonal Forms, pp. 313–316: -no/-to impersonal past and passive subject/object mapping.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.wordform.nouns.source-rule",
        skillId = "wordform.nouns",
        text = "После czytanie прямое дополнение становится зависимым родительным: czytam książki → czytanie książek. Русская фраза «читаю книги» не подсказывает новую польскую форму объекта.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 12, Verbal Nouns, pp. 308–310: action nouns and the Genitive of an original Accusative object.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.numeral.agreement.source-rule",
        skillId = "numeral.agreement",
        text = "Русское «пять студентов пришли» подталкивает к множественному глаголу; польское Pięciu studentów przyszło требует форму среднего единственного.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 8, Syntax of Cardinal Numerals, pp. 195–202: 2–4 versus 5+ noun and verb agreement, including masculine-personal forms.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.wordform.adjectives.source-rule",
        skillId = "wordform.adjectives",
        text = "Не приклеивай один суффикс к каждому существительному: drewno → drewniany, но szkło → szklany. Сначала проверь производную основу, затем окончание.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 6, Relational Adjectives, pp. 139–143: denominal material and type adjectives, variable suffixes -n- and -ow-.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.sentence.connectives.source-rule",
        skillId = "sentence.connectives",
        text = "Чтобы назвать цель, польское żeby требует зависимую часть: Uczę się polskiego, żeby rozmawiać z sąsiadami. Русское «чтобы» здесь помогает: żeby называет цель («зачем?»), а ponieważ объясняет причину («почему?»).",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 14, Conjunctions, pp. 402–407: coordinating and subordinating conjunctions for cause, contrast, condition, purpose and time.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.sentence.rephrase.source-rule",
        skillId = "sentence.rephrase",
        text = "При замене tylko на nikt oprócz понадобится отрицание: Tylko Anna zna odpowiedź → Nikt oprócz Anny nie zna odpowiedzi. Второе dopełniacz связано и с oprócz, и с отрицанием.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 13, Uses of the Cases, pp. 333–340 (Genitive of negation); ch. 14, Conjunctions, pp. 402–407: negative and subordinate-clause constructions used for equivalent reformulation.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.case.valency.source-rule",
        skillId = "case.valency",
        text = "Русское «жду автобус» без предлога нельзя калькировать: Czekam na autobus. Польское czekać требует na с винительным.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 14, Simple Sentence Syntax, p. 394: genitive, dative, instrumental and prepositional complements (szukać, słuchać, pomagać, interesować się, czekać na); ch. 13, Uses of the Cases, pp. 337–373: korzystać z + genitive and case/preposition frames.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "pl-ru.verb.mood.source-rule",
        skillId = "verb.mood",
        text = "Русское «бы» пишется отдельно, польское -by соединяется с формой и личным окончанием: pisałbym, pisałabym. Род говорящего меняет форму даже в первом лице.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Swan (2002), A Grammar of Contemporary Polish, ch. 9, Present Tense and Imperative, pp. 239–243: commands and inclusive chodźmy; ch. 10, Conditional Mood, pp. 257–259: -by personal endings.",
            url = "https://lektorek.org/lektorek/grammar.pdf",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
)

val LIFEHACK_ROW_ROWS: List<LifehackRow> = lifehackRowRowsPart0()
