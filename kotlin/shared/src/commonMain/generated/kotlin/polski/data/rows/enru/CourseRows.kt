// Generated from dsl/course.data-contract
// source-sha256=1c990107b99132709babf88837c6c708ca52eeaa1d020e201b2433dc94d38fe6
// Edit the source, then run biome generate.

package polski.data.rows.enru

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
    val whenClass: String,
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

private fun enNounRowRowsPart0(): List<EnNounRow> = listOf(
    EnNounRow(id = "wife", lemma = "wife", meaning = "жена", forms = EnNounFormsRow(sg = "wife", pl = "wives")),
    EnNounRow(id = "woman", lemma = "woman", meaning = "женщина", forms = EnNounFormsRow(sg = "woman", pl = "women")),
    EnNounRow(id = "book", lemma = "book", meaning = "книга", forms = EnNounFormsRow(sg = "book", pl = "books")),
    EnNounRow(id = "work", lemma = "job", meaning = "работа", forms = EnNounFormsRow(sg = "job", pl = "jobs")),
    EnNounRow(
        id = "husband",
        lemma = "husband",
        meaning = "муж",
        forms = EnNounFormsRow(sg = "husband", pl = "husbands"),
    ),
    EnNounRow(id = "friend", lemma = "friend", meaning = "друг", forms = EnNounFormsRow(sg = "friend", pl = "friends")),
    EnNounRow(id = "son", lemma = "son", meaning = "сын", forms = EnNounFormsRow(sg = "son", pl = "sons")),
    EnNounRow(id = "dog", lemma = "dog", meaning = "собака", forms = EnNounFormsRow(sg = "dog", pl = "dogs")),
    EnNounRow(id = "cat", lemma = "cat", meaning = "кот", forms = EnNounFormsRow(sg = "cat", pl = "cats")),
    EnNounRow(id = "car", lemma = "car", meaning = "машина", forms = EnNounFormsRow(sg = "car", pl = "cars")),
    EnNounRow(id = "house", lemma = "house", meaning = "дом", forms = EnNounFormsRow(sg = "house", pl = "houses")),
    EnNounRow(id = "phone", lemma = "phone", meaning = "телефон", forms = EnNounFormsRow(sg = "phone", pl = "phones")),
    EnNounRow(
        id = "child",
        lemma = "child",
        meaning = "ребёнок",
        forms = EnNounFormsRow(sg = "child", pl = "children"),
    ),
    EnNounRow(id = "window", lemma = "window", meaning = "окно", forms = EnNounFormsRow(sg = "window", pl = "windows")),
)

val EN_NOUN_ROW_ROWS: List<EnNounRow> = enNounRowRowsPart0()

private fun enAdjectiveRowRowsPart0(): List<EnAdjectiveRow> = listOf(
    EnAdjectiveRow(
        id = "beautiful",
        lemma = "beautiful",
        meaning = "красивый",
        forms = EnAdjectiveFormsRow(invariant = "beautiful"),
    ),
    EnAdjectiveRow(id = "new", lemma = "new", meaning = "новый", forms = EnAdjectiveFormsRow(invariant = "new")),
    EnAdjectiveRow(
        id = "small",
        lemma = "small",
        meaning = "маленький",
        forms = EnAdjectiveFormsRow(invariant = "small"),
    ),
    EnAdjectiveRow(id = "nice", lemma = "nice", meaning = "приятный", forms = EnAdjectiveFormsRow(invariant = "nice")),
    EnAdjectiveRow(id = "good", lemma = "good", meaning = "хороший", forms = EnAdjectiveFormsRow(invariant = "good")),
    EnAdjectiveRow(id = "old", lemma = "old", meaning = "старый", forms = EnAdjectiveFormsRow(invariant = "old")),
    EnAdjectiveRow(
        id = "expensive",
        lemma = "expensive",
        meaning = "дорогой",
        forms = EnAdjectiveFormsRow(invariant = "expensive"),
    ),
)

val EN_ADJECTIVE_ROW_ROWS: List<EnAdjectiveRow> = enAdjectiveRowRowsPart0()

private fun enVerbRowRowsPart0(): List<EnVerbRow> = listOf(
    EnVerbRow(
        id = "walk",
        lemma = "walk",
        meaning = "ходить",
        present3sg = "walks",
        past = "walked",
        pastParticiple = "walked",
        ing = "walking",
    ),
    EnVerbRow(
        id = "see",
        lemma = "see",
        meaning = "видеть",
        present3sg = "sees",
        past = "saw",
        pastParticiple = "seen",
        ing = "seeing",
    ),
    EnVerbRow(
        id = "talk",
        lemma = "talk",
        meaning = "говорить",
        present3sg = "talks",
        past = "talked",
        pastParticiple = "talked",
        ing = "talking",
    ),
    EnVerbRow(
        id = "go",
        lemma = "go",
        meaning = "идти",
        present3sg = "goes",
        past = "went",
        pastParticiple = "gone",
        ing = "going",
    ),
    EnVerbRow(
        id = "like",
        lemma = "like",
        meaning = "любить",
        present3sg = "likes",
        past = "liked",
        pastParticiple = "liked",
        ing = "liking",
    ),
    EnVerbRow(
        id = "read",
        lemma = "read",
        meaning = "читать",
        present3sg = "reads",
        past = "read",
        pastParticiple = "read",
        ing = "reading",
    ),
    EnVerbRow(
        id = "have",
        lemma = "have",
        meaning = "иметь",
        present3sg = "has",
        past = "had",
        pastParticiple = "had",
        ing = "having",
    ),
    EnVerbRow(
        id = "do",
        lemma = "do",
        meaning = "делать",
        present3sg = "does",
        past = "did",
        pastParticiple = "done",
        ing = "doing",
    ),
    EnVerbRow(
        id = "be",
        lemma = "be",
        meaning = "быть",
        present3sg = "is",
        presentSg1 = "am",
        presentPl = "are",
        past = "was",
        pastPl = "were",
        pastParticiple = "been",
        ing = "being",
    ),
    EnVerbRow(id = "will", lemma = "will", meaning = "вспомогательный глагол будущего времени", present3sg = "will"),
)

val EN_VERB_ROW_ROWS: List<EnVerbRow> = enVerbRowRowsPart0()

private fun enPronounRowRowsPart0(): List<EnPronounRow> = listOf(
    EnPronounRow(id = "I", forms = EnPronounFormsRow(Subj = "I", Obj = "me")),
    EnPronounRow(id = "you", forms = EnPronounFormsRow(Subj = "you", Obj = "you")),
    EnPronounRow(id = "he", forms = EnPronounFormsRow(Subj = "he", Obj = "him")),
    EnPronounRow(id = "she", forms = EnPronounFormsRow(Subj = "she", Obj = "her")),
    EnPronounRow(id = "it", forms = EnPronounFormsRow(Subj = "it", Obj = "it")),
    EnPronounRow(id = "we", forms = EnPronounFormsRow(Subj = "we", Obj = "us")),
    EnPronounRow(id = "they", forms = EnPronounFormsRow(Subj = "they", Obj = "them")),
)

val EN_PRONOUN_ROW_ROWS: List<EnPronounRow> = enPronounRowRowsPart0()

private fun possessiveRowRowsPart0(): List<PossessiveRow> = listOf(
    PossessiveRow(
        id = "my",
        label = "my — мой",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "my"),
    ),
    PossessiveRow(
        id = "your",
        label = "your — твой/ваш",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "your"),
    ),
    PossessiveRow(
        id = "his",
        label = "his — его",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "his"),
    ),
    PossessiveRow(
        id = "her",
        label = "her — её",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "her"),
    ),
    PossessiveRow(
        id = "its",
        label = "its — его/её (о предмете)",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "its"),
    ),
    PossessiveRow(
        id = "our",
        label = "our — наш",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "our"),
    ),
    PossessiveRow(
        id = "their",
        label = "their — их",
        forms = PossessiveFormsRow(kind = PossessiveFormsRowKind.INVARIANT, value = "their"),
    ),
)

val POSSESSIVE_ROW_ROWS: List<PossessiveRow> = possessiveRowRowsPart0()

private fun enPrepositionRowRowsPart0(): List<EnPrepositionRow> = listOf(
    EnPrepositionRow(
        id = "role",
        forms = listOf(
            RoleFormRow(role = "Subj", form = ""),
            RoleFormRow(role = "Obj", form = ""),
            RoleFormRow(role = "In", form = "in"),
            RoleFormRow(role = "With", form = "with"),
            RoleFormRow(role = "To", form = "to"),
            RoleFormRow(role = "About", form = "about"),
            RoleFormRow(role = "Of", form = "of"),
        ),
    ),
)

val EN_PREPOSITION_ROW_ROWS: List<EnPrepositionRow> = enPrepositionRowRowsPart0()

private fun skillRowRowsPart0(): List<SkillRow> = listOf(
    SkillRow(
        id = "en:role.object",
        title = "Object · без падежа",
        group = "Roles",
        level = SkillRowLevel.A1,
        formula = "Подлежащее → дополнение: падежа нет, меняется только порядок слов (Subject-Verb-Object).",
        theory = "В английском у существительных нет падежных окончаний: форма слова одна и та же и для подлежащего, и для дополнения. Роль слова показывает порядок: I see my house — «house» дополнение просто потому, что стоит после глагола.",
        hint = "my beautiful house → I see my beautiful house.",
        prerequisites = emptyList(),
        focus = FocusRow(before = "my beautiful house", after = "my beautiful house"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В английском у существительных нет падежных окончаний: форма слова одна и та же и для подлежащего, и для дополнения. Роль слова показывает порядок: I see my house — «house» дополнение просто потому, что стоит после глагола.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: меняется ли форма существительного при переходе из подлежащего в дополнение?",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты показываешь собеседнику предмет, о котором говоришь. В английском не нужно менять форму слова — только сказать её после «I see».",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Ты сообщаешь собеседнику, что видишь названное. Как сказать об этом целой фразой?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни, что хотел сказать, с эталоном: форма слова не меняется, меняется только его место в предложении.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Subj", to = "Obj"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Вижу дом.",
                    target = "I see a house.",
                    note = "В русском нет артикля вообще — «дом» без «a»/«the» звучит нормально; в английском голое существительное почти всегда требует артикль. Это первое расхождение, которое собьёт русскоговорящего чаще всего.",
                    matches = false,
                ),
            ),
            examples = listOf("I see a book.", "I see a dog."),
            why = "Английский обозначает роль слова местом в предложении, а не окончанием — поэтому дополнение выглядит точно так же, как подлежащее, просто стоит после глагола.",
        ),
    ),
    SkillRow(
        id = "en:verb.presentSimple",
        title = "Present Simple",
        group = "Verb",
        level = SkillRowLevel.A1,
        formula = "3-е лицо ед.ч.: добавляем -s/-es к базовой форме глагола (Past → Pres).",
        theory = "В настоящем простом времени для he/she/it к глаголу добавляется -s (walk → walks). Это единственное изменение формы глагола по лицу для этого времени.",
        hint = "walked → walks",
        prerequisites = emptyList(),
        focus = FocusRow(before = "walked", after = "walks"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В настоящем простом времени для he/she/it к глаголу добавляется -s (walk → walks). Это единственное изменение формы глагола по лицу для этого времени.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: это 3-е лицо единственного числа? Тогда добавь -s к базовой форме.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты рассказываешь о привычном действии — том, что происходит регулярно, не именно сейчас.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что это происходит обычно, а не сейчас?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни свой ответ с эталоном: обрати внимание на окончание -s.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.verb.tense",
        featureFocus = FeatureFocusRow(feature = "Tense", from = "Past", to = "Pres"),
        fixed = listOf(
            FeatureValueRow(feature = "Person", value = "3"),
            FeatureValueRow(feature = "Number", value = "Sing"),
            FeatureValueRow(feature = "Aspect", value = "Simple"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Он читает книгу каждый день.",
                    target = "He reads a book every day.",
                    note = "В русском лицо глагола различается на каждой форме (читаю/читаешь/читает/читаем/читаете/читают) — окончание меняется всегда. В английском наоборот: единственный маркер лица во всём наборе — окончание -s, и только для he/she/it; I/you/we/they вообще без окончания. Из-за этого русскоговорящие либо забывают -s там, где оно обязательно, либо по инерции добавляют его туда, где оно не нужно.",
                    matches = false,
                ),
            ),
            examples = listOf("The dog walks.", "She reads a book."),
            why = "Present Simple обозначает привычные, регулярные действия и факты; -s в 3-м лице — единственный маркер лица в этом времени.",
        ),
    ),
    SkillRow(
        id = "en:possessive.my",
        title = "My, his, her, our...",
        group = "Agreement",
        level = SkillRowLevel.A1,
        formula = "Притяжательное местоимение не согласуется ни с родом, ни с числом предмета: my/your/his/her/its/our/their — всегда одна форма.",
        theory = "В отличие от русского («мой/моя/моё»), английское притяжательное местоимение не изменяется по роду или числу владения: my dog, my house, my houses — форма my одна на все случаи.",
        hint = "my beautiful house → her beautiful house",
        prerequisites = listOf("en:role.object"),
        focus = FocusRow(before = "my beautiful house", after = "her beautiful house"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В отличие от русского («мой/моя/моё»), английское притяжательное местоимение не изменяется по роду или числу владения: my dog, my house, my houses — форма my одна на все случаи.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: меняется ли форма прилагательного или существительного при замене владельца? Нет — меняется только само местоимение.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь о предмете, который принадлежит кому-то другому — не тебе.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что этот предмет принадлежит другому человеку?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: остальная фраза не меняется, меняется только владелец.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.agreement.possessive",
        featureFocus = null,
        fixed = listOf(
            FeatureValueRow(feature = "Case", value = "Obj"),
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Моя книга.",
                    target = "My book.",
                    note = "Порядок владелец-перед-существительным совпадает в обоих языках, притяжательное не согласуется по числу/роду ни там, ни там на уровне «мой/my» — единственный skill из 16, где английское явление проще русского, не только польского.",
                    matches = true,
                ),
            ),
            examples = listOf("This is her book.", "This is their car."),
            why = "Английское притяжательное — закрытый список неизменяемых слов; в отличие от польского и русского, ему не нужно согласование с родом или числом существительного.",
        ),
    ),
    SkillRow(
        id = "en:mood.question",
        title = "Yes/No Question · do-support",
        group = "Sentence",
        level = SkillRowLevel.A1,
        formula = "Вопрос образуется через вынесенный вперёд вспомогательный do/does, а не порядком слов.",
        theory = "В отличие от русского, где вопрос — это только интонация, английский вопрос требует вспомогательный глагол do/does, поставленный перед подлежащим: You walk. → Do you walk?",
        hint = "You walk. → Do you walk?",
        prerequisites = listOf("en:verb.presentSimple"),
        focus = FocusRow(before = "You walk.", after = "Do you walk?"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В отличие от русского, где вопрос — это только интонация, английский вопрос требует вспомогательный глагол do/does, поставленный перед подлежащим: You walk. → Do you walk?",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: есть ли в предложении вспомогательный глагол do? Если нет — его нужно добавить и поставить в начало.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты хочешь спросить собеседника о чём-то — получить ответ «да» или «нет».",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как задать вопрос, на который можно ответить да/нет?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни свой вопрос с эталоном: do вынесено перед подлежащим.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.sentence.mood",
        featureFocus = FeatureFocusRow(feature = "Mood", from = "Decl", to = "YesNoQ"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "2"),
            FeatureValueRow(feature = "Number", value = "Sing"),
            FeatureValueRow(feature = "Aspect", value = "Simple"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Ты видишь дом?",
                    target = "Do you see a house?",
                    note = "В русском вопрос — только интонация/порядок, без изменений в глаголе. В английском обязательна инверсия (аналог pl, но там это do-support, не порядок подлежащее-глагол): нужен вспомогательный do, вынесенный перед подлежащим.",
                    matches = false,
                ),
            ),
            examples = listOf("Do you see a house?", "Does she read?"),
            why = "У английского нет свободной интонационной инверсии, как в русском — вопрос всегда маркируется грамматически, через вынесенный вперёд do/does (do-support).",
        ),
    ),
    SkillRow(
        id = "en:role.location",
        title = "In · место",
        group = "Roles",
        level = SkillRowLevel.A1,
        formula = "Роль места вводится предлогом in: Case Obj → In добавляет in перед дополнением.",
        theory = "Место действия в английском выражается предлогом in, а не изменением формы существительного: I see my house. → I live in my house.",
        hint = "my beautiful house → in my beautiful house",
        prerequisites = listOf("en:role.object"),
        focus = FocusRow(before = "my beautiful house", after = "in my beautiful house"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Место действия в английском выражается предлогом in, а не изменением формы существительного: I see my house. → I live in my house.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: нужно ли добавить предлог in перед дополнением?",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь, где что-то происходит — внутри чего-то.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что находишься внутри этого места?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: сама фраза не меняется, добавляется только in.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Obj", to = "In"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я живу в доме.",
                    target = "I live in a house.",
                    note = "Здесь предлоги почти буквально совпадают: русское «в» и английское in оба обозначают нахождение внутри — редкий случай, когда предложная логика почти не расходится. Это исключение: другие роли (with/to/about) выбирают предлог лексически, без опоры на русский падеж (см. другие skills).",
                    matches = true,
                ),
            ),
            examples = listOf("I live in a house.", "I read in a car."),
            why = "Английские предлоги роли — отдельные, лексически закреплённые слова, а не падежные окончания; in выбирается по роли «место», а не по форме существительного.",
        ),
    ),
    SkillRow(
        id = "en:role.instrument",
        title = "With · инструмент/спутник",
        group = "Roles",
        level = SkillRowLevel.A2,
        formula = "Роль инструмента/спутника — предлог with: Case Obj → With.",
        theory = "Совместность или инструмент выражается предлогом with, который не связан с падежом русского творительного — это отдельное лексическое слово: I see my dog. → I go with my dog.",
        hint = "my good dog → with my good dog",
        prerequisites = listOf("en:role.object"),
        focus = FocusRow(before = "my good dog", after = "with my good dog"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Совместность или инструмент выражается предлогом with, который не связан с падежом русского творительного — это отдельное лексическое слово: I see my dog. → I go with my dog.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: нужно ли добавить with перед дополнением для смысла «вместе с» или «с помощью»?",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь, с кем или с помощью чего что-то делаешь.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что делаешь что-то вместе с этим?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: добавляется только with.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Obj", to = "With"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я пишу ручкой.",
                    target = "I write with a pen.",
                    note = "Русский творительный падеж выражает роль «инструмент» одним окончанием, без предлога вообще — «ручкой» уже значит «с помощью ручки». В английском падежа нет, поэтому эта роль не может обойтись без отдельного слова with — предлог здесь не необязательное дополнение, а единственный способ показать роль.",
                    matches = false,
                ),
            ),
            examples = listOf("I go with my friend.", "I write with a phone."),
            why = "With не связан с формой существительного — как и in/to/about, это чисто лексический выбор предлога по роли, а не морфология.",
        ),
    ),
    SkillRow(
        id = "en:polarity.present",
        title = "Do not · отрицание",
        group = "Sentence",
        level = SkillRowLevel.A2,
        formula = "Отрицание несёт вспомогательный do: I do not walk. — сам глагол остаётся в базовой форме.",
        theory = "В отличие от русского «не» перед глаголом, английское отрицание требует вспомогательный глагол do/does/did + not; смысловой глагол сохраняет базовую форму: I walk. → I do not walk.",
        hint = "I walk. → I do not walk.",
        prerequisites = listOf("en:role.object"),
        focus = FocusRow(before = "I walk.", after = "I do not walk."),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В отличие от русского «не» перед глаголом, английское отрицание требует вспомогательный глагол do/does/did + not; смысловой глагол сохраняет базовую форму: I walk. → I do not walk.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: смысловой глагол остаётся в базовой форме — время и лицо несёт do.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь, что что-то НЕ делаешь.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что ты этого не делаешь?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: добавлено do not, глагол в базовой форме.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.sentence.polarity",
        featureFocus = FeatureFocusRow(feature = "Polarity", from = "Pos", to = "Neg"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
            FeatureValueRow(feature = "Aspect", value = "Simple"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я не вижу дом.",
                    target = "I don't see a house.",
                    note = "В русском отрицание — одна частица «не» перед глаголом, без вспомогательного слова. В английском нужен do/does + not, и именно do, а не глагол, несёт отрицание — русский не подсказывает, что нужно вставить лишнее слово.",
                    matches = false,
                ),
            ),
            examples = listOf("I do not see a car.", "I do not read."),
            why = "Do-support — обязательный механизм английского отрицания и вопроса: смысловой глагол сам никогда не меняется для этого, всю грамматическую нагрузку берёт вспомогательный do.",
        ),
    ),
    SkillRow(
        id = "en:verb.pastSimple",
        title = "Past Simple",
        group = "Verb",
        level = SkillRowLevel.A2,
        formula = "Правильные глаголы: -ed к базовой форме (Pres → Past); неправильные — отдельная форма (go → went).",
        theory = "Большинство глаголов образуют прошедшее время добавлением -ed (walk → walked); часть глаголов — неправильные, их форма прошедшего времени не выводится по правилу и запоминается отдельно (go → went).",
        hint = "walks → walked",
        prerequisites = listOf("en:verb.presentSimple"),
        focus = FocusRow(before = "walks", after = "walked"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Большинство глаголов образуют прошедшее время добавлением -ed (walk → walked); часть глаголов — неправильные, их форма прошедшего времени не выводится по правилу и запоминается отдельно (go → went).",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: -ed добавляется к базовой форме, или нужна отдельная неправильная форма?",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты рассказываешь о том, что уже произошло.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что это уже произошло?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: обрати внимание на форму прошедшего времени.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.verb.tense",
        featureFocus = FeatureFocusRow(feature = "Tense", from = "Pres", to = "Past"),
        fixed = listOf(
            FeatureValueRow(feature = "Person", value = "3"),
            FeatureValueRow(feature = "Number", value = "Sing"),
            FeatureValueRow(feature = "Aspect", value = "Simple"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Она гуляла вчера.",
                    target = "She walked yesterday.",
                    note = "Русское прошедшее время согласуется с подлежащим по роду: гулял (он) / гуляла (она) / гуляли (они) — форма глагола меняется в зависимости от того, кто действует. Английское прошедшее время (walked) не различает ни род, ни число, ни лицо вообще — одна форма на всех. Здесь английская грамматика проще, но именно поэтому русскоговорящий по привычке ищет несуществующее различие.",
                    matches = false,
                ),
            ),
            examples = listOf("She walked to school.", "He went home."),
            why = "Прошедшее время в английском не зависит от лица и числа (в отличие от русского рода): walked одинаково для I/you/he/she/we/they — меняется только между настоящим и прошедшим.",
        ),
    ),
    SkillRow(
        id = "en:role.recipient",
        title = "To · получатель",
        group = "Roles",
        level = SkillRowLevel.A2,
        formula = "Роль получателя — предлог to: Case Obj → To.",
        theory = "Получатель действия вводится предлогом to: I give a present to my friend. — форма существительного не меняется, добавляется только to.",
        hint = "my good friend → to my good friend",
        prerequisites = listOf("en:role.instrument"),
        focus = FocusRow(before = "my good friend", after = "to my good friend"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Получатель действия вводится предлогом to: I give a present to my friend. — форма существительного не меняется, добавляется только to.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: нужно ли добавить to перед дополнением-получателем?",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь, кому что-то даёшь или посылаешь.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, кому это адресовано?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: добавляется только to.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = FeatureFocusRow(feature = "Case", from = "Obj", to = "To"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(enNounId = listOf("wife", "husband", "friend", "son", "dog", "cat", "child")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я дал книгу своему другу.",
                    target = "I gave the book to my friend.",
                    note = "Русский дательный падеж («другу») сам маркирует получателя окончанием — отдельного слова не нужно. В английском нет падежных окончаний, поэтому роль «получатель» держится только на предлоге to; убрать to — потерять саму роль, а не просто изменить стиль.",
                    matches = false,
                ),
            ),
            examples = listOf("I give a present to my son.", "I talk to my friend."),
            why = "To выбирается по роли «получатель», а не по падежу; в русском дательный падеж меняет форму существительного, в английском форма не меняется вообще, меняется только предлог.",
        ),
    ),
    SkillRow(
        id = "en:number.plural",
        title = "Plural · -s/-es + неправильные",
        group = "Number",
        level = SkillRowLevel.A2,
        formula = "Множественное число: -s/-es к базовой форме; часть существительных — неправильные (child → children).",
        theory = "Регулярное множественное число добавляет -s (house → houses) или -es после шипящих; неправильные существительные (child → children) меняют форму не по правилу и запоминаются отдельно.",
        hint = "my beautiful house → my beautiful houses",
        prerequisites = listOf("en:role.object", "en:possessive.my"),
        focus = FocusRow(before = "my beautiful house", after = "my beautiful houses"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Регулярное множественное число добавляет -s (house → houses) или -es после шипящих; неправильные существительные (child → children) меняют форму не по правилу и запоминаются отдельно.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: добавляется -s/-es, или нужна отдельная неправильная форма?",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь не об одном предмете, а о нескольких.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что их несколько?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: обрати внимание на форму множественного числа.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.number",
        featureFocus = FeatureFocusRow(feature = "Number", from = "Sing", to = "Plur"),
        fixed = listOf(
            FeatureValueRow(feature = "Case", value = "Obj"),
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Один ребёнок, но много детей.",
                    target = "One child, but many children.",
                    note = "Редкое настоящее совпадение: оба языка образуют множественное число именно этого слова не регулярным окончанием, а отдельной (супплетивной) формой — ребёнок→дети, child→children, а не «childs». Здесь само исключение переносится напрямую, а не только общее правило.",
                    matches = true,
                ),
            ),
            examples = listOf("I see two houses.", "I see three children."),
            why = "Притяжательное my и прилагательное не меняются по числу — согласование с числом происходит только у самого существительного, в отличие от русского, где меняются оба слова.",
        ),
    ),
    SkillRow(
        id = "en:polarity.past",
        title = "Did not · отрицание в прошедшем",
        group = "Sentence",
        level = SkillRowLevel.A2,
        formula = "Отрицание в прошедшем времени: did not + базовая форма глагола (время несёт did, не сам глагол).",
        theory = "В прошедшем времени отрицание строится так же, как в настоящем, но с did: I walked. → I did not walk. Смысловой глагол снова возвращается в базовую форму.",
        hint = "I walked. → I did not walk.",
        prerequisites = listOf("en:polarity.present", "en:verb.pastSimple"),
        focus = FocusRow(before = "I walked.", after = "I did not walk."),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "В прошедшем времени отрицание строится так же, как в настоящем, но с did: I walked. → I did not walk. Смысловой глагол снова возвращается в базовую форму.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: -ed или неправильная форма прошедшего времени убирается, а время «переезжает» на did.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь, что чего-то не делал в прошлом.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что этого не произошло?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: did not + базовая форма.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.sentence.polarity",
        featureFocus = FeatureFocusRow(feature = "Polarity", from = "Pos", to = "Neg"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Past"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
            FeatureValueRow(feature = "Aspect", value = "Simple"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я не видел ту машину.",
                    target = "I did not see that car.",
                    note = "Русское «не» — одна и та же частица в любом времени; само время несёт на себе глагол (видел — уже прошедшее). В английском отрицание в прошедшем времени, наоборот, целиком перекладывает время на вспомогательный did, а смысловой глагол (see) возвращается в базовую форму — время «переезжает» на служебное слово, а не остаётся на смысловом глаголе, как интуитивно ожидал бы русскоговорящий.",
                    matches = false,
                ),
            ),
            examples = listOf("I did not see him.", "She did not read the book."),
            why = "Did — та же механика do-support, но в прошедшем времени; смысловой глагол в отрицании прошедшего времени никогда не сохраняет -ed.",
        ),
    ),
    SkillRow(
        id = "en:verb.futureSimple",
        title = "Future Simple",
        group = "Verb",
        level = SkillRowLevel.A2,
        formula = "Будущее простое: will + базовая форма глагола, неизменная для всех лиц.",
        theory = "Future Simple строится одинаково для всех лиц: will + базовая форма (walk → will walk). В отличие от настоящего и прошедшего, здесь нет изменения окончания самого глагола.",
        hint = "walks → will walk",
        prerequisites = listOf("en:verb.pastSimple"),
        focus = FocusRow(before = "walks", after = "will walk"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Future Simple строится одинаково для всех лиц: will + базовая форма (walk → will walk). В отличие от настоящего и прошедшего, здесь нет изменения окончания самого глагола.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: will не меняется по лицу, и глагол после него всегда в базовой форме.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь о том, что произойдёт позже.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что это произойдёт позже?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: will + базовая форма глагола.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.verb.tense",
        featureFocus = FeatureFocusRow(feature = "Tense", from = "Pres", to = "Fut"),
        fixed = listOf(
            FeatureValueRow(feature = "Person", value = "3"),
            FeatureValueRow(feature = "Number", value = "Sing"),
            FeatureValueRow(feature = "Aspect", value = "Simple"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я буду читать.",
                    target = "I will read.",
                    note = "Настоящее совпадение структуры: для несовершенного вида русский тоже строит будущее время аналитически — буду + инфинитив, почти как will + базовая форма в английском. Разница — в русском эта модель работает только для несовершенного вида (у совершенного будущее однословное: прочитаю), а в английском will + базовая форма — единственный способ говорить о будущем вообще, без такого разделения.",
                    matches = true,
                ),
            ),
            examples = listOf("I will walk home.", "She will read a book."),
            why = "Will — неизменяемый вспомогательный элемент будущего времени; в отличие от настоящего/прошедшего, лицо и число вообще не влияют на форму глагола.",
        ),
    ),
    SkillRow(
        id = "en:pronouns",
        title = "It · личное местоимение",
        group = "Pronouns",
        level = SkillRowLevel.A2,
        formula = "Личное местоимение заменяет всю группу слов дополнения: it — для неодушевлённых предметов.",
        theory = "Местоимение it заменяет целую группу «притяжательное + прилагательное + существительное», когда речь идёт о предмете, а не о человеке: my beautiful house → it.",
        hint = "my beautiful house → it",
        prerequisites = listOf("en:role.instrument", "en:role.location"),
        focus = FocusRow(before = "my beautiful house", after = "it"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Местоимение it заменяет целую группу «притяжательное + прилагательное + существительное», когда речь идёт о предмете, а не о человеке: my beautiful house → it.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: вся группа слов заменяется одним словом it.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты уже назвал предмет и не хочешь повторять его полное название.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать это одним словом вместо целой фразы?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: вместо целой фразы — одно слово it.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.argument.case-role",
        featureFocus = null,
        fixed = listOf(
            FeatureValueRow(feature = "Case", value = "Obj"),
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "1"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = LexicalFilterRow(
            slot = "noun",
            where = WhereRow(enNounId = listOf("book", "work", "dog", "cat", "car", "house", "phone", "window")),
        ),
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Где моя книга? — Вот она.",
                    target = "Where is my book? — Here it is.",
                    note = "Выбор русского местоимения (он/она/оно) зависит от грамматического рода существительного — «книга» женского рода, поэтому «она», хотя книга явно неодушевлённый предмет. Это условность, которую нужно помнить отдельно для каждого слова. В английском для всех неодушевлённых предметов — всегда it, независимо от рода существительного в русском переводе.",
                    matches = false,
                ),
            ),
            examples = listOf("I see it.", "I read it."),
            why = "It — единственная форма для неодушевлённых предметов независимо от рода в русском переводе слова; у английских личных местоимений нет такого явного падежного различия subject/object, как у he/him, she/her — it неизменно в обеих ролях.",
        ),
    ),
    SkillRow(
        id = "en:verb.presentContinuous",
        title = "Present Continuous",
        group = "Verb",
        level = SkillRowLevel.B1,
        formula = "Aspect Simple → Continuous: am/is/are + глагол-ing.",
        theory = "Present Continuous описывает процесс, происходящий сейчас: be (am/is/are) + глагол с -ing. В отличие от Present Simple (привычка/факт), Continuous всегда подчёркивает, что действие идёт в момент речи.",
        hint = "walks → is walking",
        prerequisites = listOf("en:verb.presentSimple"),
        focus = FocusRow(before = "walks", after = "is walking"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Present Continuous описывает процесс, происходящий сейчас: be (am/is/are) + глагол с -ing. В отличие от Present Simple (привычка/факт), Continuous всегда подчёркивает, что действие идёт в момент речи.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: be согласуется с подлежащим, глагол получает -ing.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты говоришь о том, что происходит прямо сейчас, в эту минуту.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что это происходит именно сейчас, а не всегда?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: be + глагол-ing.",
                review = "Оцени, удалось ли вспомнить ответ самостоятельно; следующую ситуацию реши заново.",
            ),
        ),
        construction = "core.verb.tense",
        featureFocus = FeatureFocusRow(feature = "Aspect", from = "Simple", to = "Continuous"),
        fixed = listOf(
            FeatureValueRow(feature = "Tense", value = "Pres"),
            FeatureValueRow(feature = "Person", value = "3"),
            FeatureValueRow(feature = "Number", value = "Sing"),
        ),
        lexicalFilter = null,
        styleContent = StyleContentRow(
            nativeParallel = listOf(
                NativeParallelRow(
                    native = "Я иду домой (сейчас).",
                    target = "I am going home.",
                    note = "Русский вид (иду — процесс vs пойду — начало) не совпадает по границам с английским Simple/Continuous: один и тот же русский несовершенный «иду» может быть и Present Simple («I go» — привычка), и Present Continuous («I am going» — сейчас) в зависимости от контекста, который в русском не маркирован грамматически вообще.",
                    matches = false,
                ),
            ),
            examples = listOf("She is reading a book.", "The dog is walking."),
            why = "Continuous — грамматический аспект (форма be+ing), не лексический выбор глагола, как русский совершенный/несовершенный вид; один и тот же глагол walk работает и в Simple, и в Continuous.",
        ),
    ),
    SkillRow(
        id = "en:tense.contrast",
        title = "Simple vs Continuous vs Perfect · одно событие",
        group = "B1 Core",
        level = SkillRowLevel.B1,
        formula = "Одно и то же событие: I read (привычка) — I am reading (процесс) — I have read (результат, значим сейчас).",
        theory = "Simple, Continuous и Perfect не про «когда», а про то, что говорящий подчёркивает: привычку/факт, процесс в моменте или готовый результат. I have read a new book сообщает о результате, а не о моменте времени.",
        hint = "am reading → have read",
        prerequisites = listOf("en:verb.futureSimple", "en:verb.presentContinuous"),
        focus = FocusRow(before = "am reading", after = "have read"),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "Simple, Continuous и Perfect не про «когда», а про то, что говорящий подчёркивает: привычку/факт, процесс в моменте или готовый результат. I have read a new book сообщает о результате, а не о моменте времени.",
                promptLead = "Сначала определи правило и форму.",
                introduce = "Проверь: have + причастие показывает завершённость и её значимость сейчас, а не сам момент действия.",
                retrieve = "Определи признаки и преобразуй исходное предложение самостоятельно.",
                feedback = "Сравни исходную и новую форму, затем проверь каждое изменение и правило.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты хочешь показать, что действие уже завершилось и результат важен сейчас, а не что оно происходит в данный момент.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать, что ты уже закончил, а не что делаешь это сейчас?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном: have + причастие вместо be + -ing.",
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
                    native = "Я прочитал книгу.",
                    target = "I have read the book.",
                    note = "Русский вид маркирует завершённость одним глаголом — «прочитал» (от читать→прочитать) сразу говорит «уже сделано, результат есть». Английский для той же идеи «результат сейчас значим» не меняет сам глагол на другой, а добавляет вспомогательный have к тому же самому read, которым только что была выражена и привычка (I read), и процесс (I am reading) — одна лексема, три разных грамматических костюма.",
                    matches = false,
                ),
            ),
            examples = listOf("I read a new book.", "I am reading a new book.", "I have read a new book."),
            why = "Три формы одного глагола показывают три разных взгляда на событие — привычку, процесс, результат — а не три разных момента времени; различие грамматическое (aspect), а не лексическое, как русский вид.",
        ),
    ),
    SkillRow(
        id = "en:mixed",
        title = "Смешанные трансформации",
        group = "B1 Core",
        level = SkillRowLevel.B1,
        formula = "Комбинация: время (Present → Past) + владелец (my → другое притяжательное) в одном предложении.",
        theory = "На уровне B1 нужно менять несколько вещей одновременно: и время глагола, и притяжательное местоимение, без пошаговых подсказок, как на A1-A2.",
        hint = "I see my beautiful house. → I saw his beautiful house.",
        prerequisites = listOf("en:possessive.my", "en:verb.pastSimple"),
        focus = FocusRow(before = "I see my beautiful house.", after = "I saw his beautiful house."),
        methods = MethodsRow(
            logic = MethodRow(
                introduction = "На уровне B1 нужно менять несколько вещей одновременно: и время глагола, и притяжательное местоимение, без пошаговых подсказок, как на A1-A2.",
                promptLead = "Сначала определи, что меняется: время, владелец — или оба.",
                introduce = "Проверь оба изменения по отдельности, затем собери результат.",
                retrieve = "Построй итоговое предложение самостоятельно, без пошаговых подсказок.",
                feedback = "Сравни ответ с эталоном по каждому изменению отдельно.",
                review = "Выбери «Повторить» или «Вспомнил» по своей попытке; затем реши новое задание без подсказки.",
            ),
            situations = MethodRow(
                introduction = "Ты пересказываешь историю с новыми деталями: другое время, другой владелец.",
                promptLead = "Представь ситуацию и скажи целое предложение.",
                introduce = "Как сказать это с учётом обеих деталей сразу?",
                retrieve = "Представь ситуацию и скажи целое предложение самостоятельно.",
                feedback = "Сравни ответ с эталоном по каждой детали.",
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
                    native = "Она увидела твой дом.",
                    target = "She saw your house.",
                    note = "В русском прошедшем времени глагол согласуется по роду с подлежащим («увидела», не «увидел» — потому что говорит она), а не с владельцем предмета («твой дом» остаётся «твой» независимо от рода того, кто увидел). В английском saw вообще не согласуется ни с кем — одна форма для всех; типичная ошибка — решить, что раз меняется владелец, должен «согласоваться» и глагол, хотя в обоих языках эти два изменения независимы.",
                    matches = false,
                ),
            ),
            examples = listOf("She walked her dog.", "He read their book."),
            why = "B1 объединяет уже отдельно выученные навыки (время, притяжательные) в одном предложении без пошаговых подсказок — тренирует именно комбинирование, а не новое правило.",
        ),
    ),
)

val SKILL_ROW_ROWS: List<SkillRow> = skillRowRowsPart0()

private fun enSeedRowRowsPart0(): List<EnSeedRow> = listOf(
    EnSeedRow(nounId = "wife", adjectiveId = "beautiful"),
    EnSeedRow(nounId = "husband", adjectiveId = "good"),
    EnSeedRow(nounId = "friend", adjectiveId = "good"),
    EnSeedRow(nounId = "son", adjectiveId = "small"),
    EnSeedRow(nounId = "dog", adjectiveId = "good"),
    EnSeedRow(nounId = "cat", adjectiveId = "small"),
    EnSeedRow(nounId = "book", adjectiveId = "new"),
    EnSeedRow(nounId = "car", adjectiveId = "new"),
    EnSeedRow(nounId = "house", adjectiveId = "new"),
    EnSeedRow(nounId = "phone", adjectiveId = "new"),
    EnSeedRow(nounId = "child", adjectiveId = "small"),
    EnSeedRow(nounId = "window", adjectiveId = "new"),
)

val EN_SEED_ROW_ROWS: List<EnSeedRow> = enSeedRowRowsPart0()

private fun copyEntryRowRowsPart0(): List<CopyEntryRow> = listOf(
    CopyEntryRow(key = "roleObjPrompt", value = "Скажи, что видишь названное. Начни с «I see…»."),
    CopyEntryRow(
        key = "roleObjExplanation",
        value = "I see требует дополнения после глагола; падежа нет — форма слова не меняется, меняется только его место в предложении.",
    ),
    CopyEntryRow(
        key = "roleObjReason",
        value = "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.",
    ),
    CopyEntryRow(key = "verbPresentPrompt", value = "Перенеси предложение в настоящее простое время."),
    CopyEntryRow(
        key = "verbTenseExplanation",
        value = "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.",
    ),
    CopyEntryRow(key = "verbTenseReason", value = "Меняется форма глагола, остальные слова сохраняются."),
    CopyEntryRow(
        key = "ownerExplanation",
        value = "my/your/his/her/its/our/their не согласуются ни с родом, ни с числом предмета — это закрытый список неизменяемых слов.",
    ),
    CopyEntryRow(key = "ownerReason", value = "Меняем владельца, остальная фраза не меняется."),
    CopyEntryRow(
        key = "moodQuestionPrompt",
        value = "Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Do…».",
    ),
    CopyEntryRow(
        key = "moodQuestionExplanation",
        value = "Do выносится перед подлежащим; смысловой глагол остаётся в базовой форме.",
    ),
    CopyEntryRow(
        key = "moodQuestionReason",
        value = "Do-support: вспомогательный глагол выносится вперёд, смысловой глагол теряет -s.",
    ),
    CopyEntryRow(key = "roleLocationPrompt", value = "Скажи, что находишься внутри этого. Начни с «I live in…»."),
    CopyEntryRow(
        key = "roleDrillExplanation",
        value = "Роль вводится отдельным предлогом; форма существительного не меняется — меняется только предлог перед ним.",
    ),
    CopyEntryRow(key = "roleLocationReason", value = "Добавлен предлог in по роли «место»."),
    CopyEntryRow(key = "roleLocationStart", value = "I live"),
    CopyEntryRow(
        key = "roleInstrumentPrompt",
        value = "Скажи, что делаешь это вместе с этим/с помощью этого. Начни с «I go with…».",
    ),
    CopyEntryRow(key = "roleInstrumentReason", value = "Добавлен предлог with по роли «инструмент/спутник»."),
    CopyEntryRow(key = "roleInstrumentStart", value = "I go"),
    CopyEntryRow(key = "polarityPresentPrompt", value = "Сделай предложение отрицательным."),
    CopyEntryRow(
        key = "polarityExplanation",
        value = "Do/does/did несёт отрицание; смысловой глагол остаётся в базовой форме.",
    ),
    CopyEntryRow(key = "polarityReason", value = "Добавлен do/does/did + not, смысловой глагол — в базовой форме."),
    CopyEntryRow(key = "verbPastPrompt", value = "Перенеси предложение в прошедшее простое время."),
    CopyEntryRow(key = "roleRecipientPrompt", value = "Скажи, кому это адресовано. Начни с «I give a present to…»."),
    CopyEntryRow(key = "roleRecipientReason", value = "Добавлен предлог to по роли «получатель»."),
    CopyEntryRow(key = "roleRecipientStart", value = "I give a present"),
    CopyEntryRow(key = "pluralPrompt", value = "Скажи, что таких предметов несколько."),
    CopyEntryRow(
        key = "pluralExplanation",
        value = "Существительное получает -s/-es или неправильную форму множественного числа; притяжательное и прилагательное не меняются.",
    ),
    CopyEntryRow(
        key = "pluralReason",
        value = "Существительное — во множественном числе, остальные слова — без изменений.",
    ),
    CopyEntryRow(key = "polarityPastPrompt", value = "Сделай предложение в прошедшем времени отрицательным."),
    CopyEntryRow(key = "verbFuturePrompt", value = "Перенеси предложение в будущее простое время."),
    CopyEntryRow(key = "pronounPrompt", value = "Замени группу дополнения одним личным местоимением."),
    CopyEntryRow(key = "pronounExplanation", value = "It заменяет целую группу слов, обозначающую предмет."),
    CopyEntryRow(key = "pronounReason", value = "Вся группа слов заменена местоимением it."),
    CopyEntryRow(key = "verbContinuousPrompt", value = "Скажи, что это происходит именно сейчас."),
    CopyEntryRow(key = "tenseContrastSource", value = "I am reading a new book."),
    CopyEntryRow(
        key = "tenseContrastPrompt",
        value = "Скажи, что ты уже прочитал эту книгу — результат сейчас есть, а не процесс. Начни с «I have…».",
    ),
    CopyEntryRow(key = "tenseContrastExpected", value = "I have read a new book."),
    CopyEntryRow(key = "tenseContrastAccepted", value = "I've read a new book."),
    CopyEntryRow(
        key = "tenseContrastExplanation",
        value = "I read a new book (Simple — привычка/факт вообще) → I am reading a new book (Continuous — процесс, идёт сейчас) → I have read a new book (Perfect — результат уже есть, важен для настоящего момента). Одно и то же событие можно описать тремя разными «временами»: разница не в моменте времени, а в том, что говорящий хочет подчеркнуть.",
    ),
    CopyEntryRow(key = "tenseContrastFrom", value = "am reading"),
    CopyEntryRow(key = "tenseContrastTo", value = "have read"),
    CopyEntryRow(
        key = "tenseContrastReason",
        value = "Continuous (процесс) → Perfect (результат, значимый сейчас): подчёркиваем не «что происходит», а «что уже сделано».",
    ),
    CopyEntryRow(key = "mixedPrompt", value = "Перенеси предложение в прошедшее время и замени владельца."),
    CopyEntryRow(
        key = "mixedExplanation",
        value = "Здесь два изменения сразу: время глагола и притяжательное местоимение; каждое меняется по своему собственному правилу, независимо от другого.",
    ),
    CopyEntryRow(key = "mixedVerbFrom", value = "see"),
    CopyEntryRow(key = "mixedVerbTo", value = "saw"),
    CopyEntryRow(key = "mixedVerbReason", value = "Меняется время глагола: настоящее → прошедшее."),
    CopyEntryRow(
        key = "mixedOwnerReason",
        value = "Меняется владелец; остальная форма существительного и прилагательного не меняется.",
    ),
    CopyEntryRow(key = "mixedOwnerPromptPrefix", value = "Замени «мой» на «"),
    CopyEntryRow(key = "mixedOwnerPromptSuffix", value = "». Сохрани прошедшее время предложения."),
    CopyEntryRow(key = "ownerYour", value = "твой / твоя"),
    CopyEntryRow(key = "ownerHis", value = "его"),
    CopyEntryRow(key = "ownerHer", value = "её"),
    CopyEntryRow(key = "ownerIts", value = "его / её (у предмета)"),
    CopyEntryRow(key = "ownerOur", value = "наш / наша"),
    CopyEntryRow(key = "ownerTheir", value = "их"),
    CopyEntryRow(key = "ownerPromptPrefix", value = "Замени «мой» на «"),
    CopyEntryRow(key = "ownerPromptSuffix", value = "». Сохрани время и смысл предложения."),
    CopyEntryRow(key = "chainObjPrompt", value = "Скажи, что видишь этот предмет. Начни с «I see…»."),
    CopyEntryRow(
        key = "chainObjReason",
        value = "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.",
    ),
    CopyEntryRow(key = "chainPastPrompt", value = "Перенеси предложение в прошедшее время."),
    CopyEntryRow(
        key = "chainPastReason",
        value = "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.",
    ),
    CopyEntryRow(key = "chainPastChangeFrom", value = "see"),
    CopyEntryRow(key = "chainPastChangeTo", value = "saw"),
    CopyEntryRow(key = "chainNegPrompt", value = "Сделай предложение отрицательным, сохранив прошедшее время."),
    CopyEntryRow(key = "chainNegReason", value = "Добавлен did not перед базовой формой глагола."),
    CopyEntryRow(key = "chainNegChangeFrom", value = "saw"),
    CopyEntryRow(key = "chainNegChangeTo", value = "did not see"),
    CopyEntryRow(key = "chainOwnerPrompt", value = "Замени владельца на «her». Сохрани отрицание и прошедшее время."),
    CopyEntryRow(
        key = "chainOwnerReason",
        value = "Меняется владелец; остальная фраза, включая отрицание, не меняется.",
    ),
    CopyEntryRow(key = "chainLocPrompt", value = "Скажи, что находишься внутри этого. Начни с «I stayed in…»."),
    CopyEntryRow(
        key = "chainLocReason",
        value = "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.",
    ),
    CopyEntryRow(key = "chainLocStart", value = "I stayed"),
)

val COPY_ENTRY_ROW_ROWS: List<CopyEntryRow> = copyEntryRowRowsPart0()

private fun patternEntryRowRowsPart0(): List<PatternEntryRow> = listOf(
    PatternEntryRow(key = "seenObj", value = "I see {obj}."),
    PatternEntryRow(key = "verbSentence", value = "{subj} {verb}."),
    PatternEntryRow(key = "subjectVerb", value = "I {verb}."),
    PatternEntryRow(key = "roleDrill", value = "{start} {target}."),
    PatternEntryRow(key = "chainPast", value = "I saw {obj}."),
    PatternEntryRow(key = "chainNeg", value = "I did not see {obj}."),
)

val PATTERN_ENTRY_ROW_ROWS: List<PatternEntryRow> = patternEntryRowRowsPart0()

private fun prefixEntryRowRowsPart0(): List<PrefixEntryRow> = listOf(
    PrefixEntryRow(key = "Subj", value = "Here is"),
)

val PREFIX_ENTRY_ROW_ROWS: List<PrefixEntryRow> = prefixEntryRowRowsPart0()

private fun vocabularyRowRowsPart0(): List<VocabularyRow> = listOf(
    VocabularyRow(
        source = "curated from courses/lang/en/lexicon.json's genuinely-authored lexemes; editorial cards from courses/pairs/en-ru/vocabulary-editorial.json (EN-15/EN-16), examples authored for this course",
        items = listOf(
            VocabularyItemRow(
                id = "noun.wife",
                lemma = "wife",
                translation = "жена",
                form = "wife · pl. wives (irregular)",
                example = "This is my wife.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.woman",
                lemma = "woman",
                translation = "женщина",
                form = "woman · pl. women (irregular)",
                example = "That woman is my friend.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.book",
                lemma = "book",
                translation = "книга",
                form = "book · pl. books (regular -s)",
                example = "I read a book every day.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.work",
                lemma = "job",
                translation = "работа",
                form = "job · pl. jobs (regular -s)",
                example = "I like my job.",
                level = "A2",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.husband",
                lemma = "husband",
                translation = "муж",
                form = "husband · pl. husbands (regular -s)",
                example = "Her husband works at home.",
                level = "A2",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.friend",
                lemma = "friend",
                translation = "друг",
                form = "friend · pl. friends (regular -s)",
                example = "My friend has a dog.",
                level = "A2",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.son",
                lemma = "son",
                translation = "сын",
                form = "son · pl. sons (regular -s)",
                example = "My son likes cats.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.dog",
                lemma = "dog",
                translation = "собака",
                form = "dog · pl. dogs (regular -s)",
                example = "This dog is small.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.cat",
                lemma = "cat",
                translation = "кот",
                form = "cat · pl. cats (regular -s)",
                example = "My cat sleeps on the sofa.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.car",
                lemma = "car",
                translation = "машина",
                form = "car · pl. cars (regular -s)",
                example = "This car is new.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.house",
                lemma = "house",
                translation = "дом",
                form = "house · pl. houses (regular -s)",
                example = "My house is near the school.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.phone",
                lemma = "phone",
                translation = "телефон",
                form = "phone · pl. phones (regular -s)",
                example = "Where is my phone?",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.child",
                lemma = "child",
                translation = "ребёнок",
                form = "child · pl. children (irregular)",
                example = "This child likes to read.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "noun.window",
                lemma = "window",
                translation = "окно",
                form = "window · pl. windows (regular -s)",
                example = "Open the window, please.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.beautiful",
                lemma = "beautiful",
                translation = "прекрасный, очень красивый",
                form = "beautiful · invariant (no gender/number agreement)",
                example = "This is a beautiful house.",
                level = "A2",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.new",
                lemma = "new",
                translation = "новый",
                form = "new · invariant (no gender/number agreement)",
                example = "I have a new book.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.small",
                lemma = "small",
                translation = "маленький",
                form = "small · invariant (no gender/number agreement)",
                example = "This is a small dog.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.nice",
                lemma = "nice",
                translation = "симпатичный, приятный",
                form = "nice · invariant (no gender/number agreement)",
                example = "This is a nice house.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.good",
                lemma = "good",
                translation = "хороший",
                form = "good · invariant (no gender/number agreement)",
                example = "This is a good idea.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.old",
                lemma = "old",
                translation = "старый",
                form = "old · invariant (no gender/number agreement)",
                example = "This is an old car.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "adjective.expensive",
                lemma = "expensive",
                translation = "дорогой",
                form = "expensive · invariant (no gender/number agreement)",
                example = "This phone is expensive.",
                level = "A2",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.walk",
                lemma = "walk",
                translation = "ходить",
                form = "walk · he walks · past walked (regular -ed)",
                example = "I walk to work.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.see",
                lemma = "see",
                translation = "видеть",
                form = "see · he sees · past saw · past participle seen (irregular)",
                example = "I see a house.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.talk",
                lemma = "talk",
                translation = "говорить",
                form = "talk · he talks · past talked (regular -ed)",
                example = "I talk to my friend.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.go",
                lemma = "go",
                translation = "идти",
                form = "go · he goes · past went · past participle gone (irregular)",
                example = "I go home.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.like",
                lemma = "like",
                translation = "нравиться, любить",
                form = "like · he likes · past liked (regular -ed)",
                example = "I like my job.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.read",
                lemma = "read",
                translation = "читать",
                form = "read · he reads · past read (same spelling as base, irregular pronunciation)",
                example = "I read a book.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.have",
                lemma = "have",
                translation = "иметь",
                form = "have · he has · past had (irregular)",
                example = "I have a car.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.do",
                lemma = "do",
                translation = "делать",
                form = "do · he does · past did · past participle done (irregular)",
                example = "I do my work.",
                level = "A1",
                frequencyRank = null,
            ),
            VocabularyItemRow(
                id = "verb.be",
                lemma = "be",
                translation = "быть",
                form = "be · I am · he is · we are · past was/were · past participle been (highly irregular)",
                example = "I am at home.",
                level = "A1",
                frequencyRank = null,
            ),
        ),
        instructions = VocabularyInstructionsRow(
            react = "Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера видны в списке, но пока не добавляются в тренировки. Исходные 30 карточек прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
            web = "Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера пока не добавляются в тренировки. Исходные 30 карточек прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
            native = "Частотный ранг не равен уровню CEFR. Без проверенного перевода и примера слово пока недоступно. Исходные 30 карточек прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
            ios = "Частотный ранг не равен уровню CEFR. Непроверенные слова пока недоступны. Исходные 30 карточек прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.",
        ),
        unavailableLabel = "Перевод и пример ещё не проверены",
    ),
)

val VOCABULARY_ROW_ROWS: List<VocabularyRow> = vocabularyRowRowsPart0()

private fun trainingRowRowsPart0(): List<TrainingRow> = listOf(
    TrainingRow(
        chainPresentation = ChainPresentationRow(
            steps = listOf(
                ChainStepRow(id = "obj", label = "Вижу"),
                ChainStepRow(id = "past", label = "Прошлое"),
                ChainStepRow(id = "neg", label = "Отрицание"),
                ChainStepRow(id = "owner", label = "Владелец"),
                ChainStepRow(id = "loc", label = "Место"),
            ),
            completion = ChainCompletionRow(
                title = "Цепочка завершена",
                reactEyebrow = "5 преобразований",
                reactBody = "Ты изменил время, отрицание, владельца и роль дополнения, сохранив одну мысль. Оценки сохранены в расписании повторений.",
                webBody = "Пять преобразований завершены. Оценки сохранены в расписании повторений.",
            ),
        ),
    ),
)

val TRAINING_ROW_ROWS: List<TrainingRow> = trainingRowRowsPart0()

private fun constructionRowRowsPart0(): List<ConstructionRow> = listOf(
    ConstructionRow(
        id = "core.argument.case-role",
        slots = listOf(
            SlotRow(name = "prep", category = "prep", optional = true, requiredFeatures = listOf("Case")),
            SlotRow(name = "owner", category = "possessive", optional = true, requiredFeatures = emptyList()),
            SlotRow(name = "adjective", category = "adjective", requiredFeatures = emptyList()),
            SlotRow(name = "noun", category = "noun", requiredFeatures = listOf("Number")),
        ),
    ),
    ConstructionRow(
        id = "core.agreement.possessive",
        slots = listOf(
            SlotRow(name = "owner", category = "possessive", requiredFeatures = emptyList()),
            SlotRow(name = "adjective", category = "adjective", requiredFeatures = emptyList()),
            SlotRow(name = "noun", category = "noun", requiredFeatures = listOf("Number")),
        ),
    ),
    ConstructionRow(
        id = "core.number",
        slots = listOf(
            SlotRow(name = "prep", category = "prep", optional = true, requiredFeatures = listOf("Case")),
            SlotRow(name = "owner", category = "possessive", optional = true, requiredFeatures = emptyList()),
            SlotRow(name = "adjective", category = "adjective", requiredFeatures = emptyList()),
            SlotRow(name = "noun", category = "noun", requiredFeatures = listOf("Number")),
        ),
    ),
    ConstructionRow(
        id = "core.verb.tense",
        slots = listOf(
            SlotRow(
                name = "aux",
                category = "aux",
                optional = true,
                requiredFeatures = listOf("Tense", "Person", "Number"),
            ),
            SlotRow(
                name = "verb",
                category = "verb",
                requiredFeatures = listOf("Tense", "Person", "Number"),
                requiredFeaturesWhen = listOf(
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Tense", value = "Past"),
                        features = listOf("Tense"),
                    ),
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Tense", value = "Fut"),
                        features = listOf("Tense"),
                    ),
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Aspect", value = "Continuous"),
                        features = listOf("Aspect"),
                    ),
                ),
            ),
        ),
    ),
    ConstructionRow(
        id = "core.sentence.polarity",
        slots = listOf(
            SlotRow(
                name = "aux",
                category = "aux",
                optional = true,
                requiredFeatures = listOf("Tense", "Person", "Number"),
                requiredFeaturesWhen = listOf(
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Tense", value = "Past"),
                        features = listOf("Tense"),
                    ),
                ),
            ),
            SlotRow(name = "neg", category = "neg", optional = true, requiredFeatures = emptyList()),
            SlotRow(
                name = "verb",
                category = "verb",
                requiredFeatures = listOf("Tense", "Person", "Number"),
                requiredFeaturesWhen = listOf(
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Polarity", value = "Neg"),
                        features = emptyList(),
                    ),
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Tense", value = "Past"),
                        features = listOf("Tense"),
                    ),
                ),
            ),
        ),
    ),
    ConstructionRow(
        id = "core.sentence.mood",
        slots = listOf(
            SlotRow(name = "subject", category = "pronoun", requiredFeatures = emptyList()),
            SlotRow(
                name = "aux",
                category = "aux",
                optional = true,
                requiredFeatures = listOf("Tense", "Person", "Number"),
                requiredFeaturesWhen = listOf(
                    RequiredWhenRow(
                        condition = ConditionRow(feature = "Tense", value = "Past"),
                        features = listOf("Tense"),
                    ),
                ),
            ),
            SlotRow(name = "verb", category = "verb", requiredFeatures = emptyList()),
        ),
        orderWhen = listOf(
            OrderWhenRow(
                condition = ConditionRow(feature = "Mood", value = "YesNoQ"),
                slots = listOf(
                    SlotRow(
                        name = "aux",
                        category = "aux",
                        optional = true,
                        requiredFeatures = listOf("Tense", "Person", "Number"),
                        requiredFeaturesWhen = listOf(
                            RequiredWhenRow(
                                condition = ConditionRow(feature = "Tense", value = "Past"),
                                features = listOf("Tense"),
                            ),
                        ),
                    ),
                    SlotRow(name = "subject", category = "pronoun", requiredFeatures = emptyList()),
                    SlotRow(name = "verb", category = "verb", requiredFeatures = emptyList()),
                ),
            ),
        ),
    ),
)

val CONSTRUCTION_ROW_ROWS: List<ConstructionRow> = constructionRowRowsPart0()

private fun skillRecipeRowRowsPart0(): List<SkillRecipeRow> = listOf(
    SkillRecipeRow(
        skill = "en:role.object",
        source = TextSpecRow(
            kind = TextSpecRowKind.PREFIXED,
            spec = PhraseSpecRow(
                construction = "core.argument.case-role",
                bundle = listOf(
                    FeatureValueRow(feature = "Number", value = "sg"),
                    FeatureValueRow(feature = "Case", value = "Subj"),
                ),
            ),
            prefixCase = "Subj",
            punct = ".",
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "roleObjPrompt",
        explanationKey = "roleObjExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Subj"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "roleObjReason"),
            ),
        ),
        tags = listOf("obj"),
    ),
    SkillRecipeRow(
        skill = "en:verb.presentSimple",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Past"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Simple"),
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
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Pres"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Simple"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "verbPresentPrompt",
        explanationKey = "verbTenseExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Past"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Simple"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Simple"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "verbTenseReason"),
            ),
        ),
        tags = listOf("verb", "present", "subj"),
    ),
    SkillRecipeRow(
        skill = "en:possessive.my",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                            ownerLexeme = "my",
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.agreement.possessive",
                            bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
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
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                        ownerLexeme = "my",
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.agreement.possessive",
                        bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "ownerReason"),
            ),
        ),
        tags = listOf("agreement", "obj"),
        ownerDraw = OwnerDrawRow(
            owners = listOf(
                OwnerChoiceRow(lexeme = "your", labelKey = "ownerYour"),
                OwnerChoiceRow(lexeme = "his", labelKey = "ownerHis"),
                OwnerChoiceRow(lexeme = "her", labelKey = "ownerHer"),
                OwnerChoiceRow(lexeme = "its", labelKey = "ownerIts"),
                OwnerChoiceRow(lexeme = "our", labelKey = "ownerOur"),
                OwnerChoiceRow(lexeme = "their", labelKey = "ownerTheir"),
            ),
            promptPrefixKey = "ownerPromptPrefix",
            promptSuffixKey = "ownerPromptSuffix",
        ),
    ),
    SkillRecipeRow(
        skill = "en:mood.question",
        source = TextSpecRow(
            kind = TextSpecRowKind.CONCAT,
            parts = listOf(
                TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    capitalized = true,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.mood",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "2"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Mood", value = "Decl"),
                        ),
                    ),
                ),
                TextValueRow(kind = TextValueRowKind.LITERAL, value = "."),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.CONCAT,
            parts = listOf(
                TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    capitalized = true,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.mood",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "2"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Mood", value = "YesNoQ"),
                        ),
                    ),
                ),
                TextValueRow(kind = TextValueRowKind.LITERAL, value = "?"),
            ),
        ),
        promptKey = "moodQuestionPrompt",
        explanationKey = "moodQuestionExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    capitalized = true,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.mood",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "2"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Mood", value = "Decl"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    capitalized = true,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.mood",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "2"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Mood", value = "YesNoQ"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "moodQuestionReason"),
            ),
        ),
        tags = listOf("mood", "question"),
    ),
    SkillRecipeRow(
        skill = "en:role.location",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "roleDrill",
            values = listOf(
                PatternValueRow(
                    name = "start",
                    value = TextValueRow(kind = TextValueRowKind.COPY, key = "roleLocationStart"),
                ),
                PatternValueRow(
                    name = "target",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "In"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "roleLocationPrompt",
        explanationKey = "roleDrillExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "In"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "roleLocationReason"),
            ),
        ),
        tags = listOf("role", "in"),
    ),
    SkillRecipeRow(
        skill = "en:role.instrument",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "roleDrill",
            values = listOf(
                PatternValueRow(
                    name = "start",
                    value = TextValueRow(kind = TextValueRowKind.COPY, key = "roleInstrumentStart"),
                ),
                PatternValueRow(
                    name = "target",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "With"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "roleInstrumentPrompt",
        explanationKey = "roleDrillExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "With"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "roleInstrumentReason"),
            ),
        ),
        tags = listOf("role", "with"),
    ),
    SkillRecipeRow(
        skill = "en:polarity.present",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "subjectVerb",
            values = listOf(
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "Pres"),
                                FeatureValueRow(feature = "Person", value = "1"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "Pos"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "subjectVerb",
            values = listOf(
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "Pres"),
                                FeatureValueRow(feature = "Person", value = "1"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "Neg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "polarityPresentPrompt",
        explanationKey = "polarityExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "1"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "Pos"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "1"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "Neg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "polarityReason"),
            ),
        ),
        accepted = listOf(
            TextSpecRow(
                kind = TextSpecRowKind.PATTERN,
                key = "subjectVerb",
                values = listOf(
                    PatternValueRow(
                        name = "verb",
                        value = TextValueRow(kind = TextValueRowKind.LITERAL, value = "don't walk"),
                    ),
                ),
            ),
        ),
        tags = listOf("polarity", "neg"),
    ),
    SkillRecipeRow(
        skill = "en:verb.pastSimple",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Pres"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Simple"),
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
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Past"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Simple"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "verbPastPrompt",
        explanationKey = "verbTenseExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Simple"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Past"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Simple"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "verbTenseReason"),
            ),
        ),
        tags = listOf("verb", "past", "subj"),
    ),
    SkillRecipeRow(
        skill = "en:role.recipient",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "roleDrill",
            values = listOf(
                PatternValueRow(
                    name = "start",
                    value = TextValueRow(kind = TextValueRowKind.COPY, key = "roleRecipientStart"),
                ),
                PatternValueRow(
                    name = "target",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "To"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "roleRecipientPrompt",
        explanationKey = "roleDrillExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "To"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "roleRecipientReason"),
            ),
        ),
        tags = listOf("role", "to"),
    ),
    SkillRecipeRow(
        skill = "en:number.plural",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.number",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "pl"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
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
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.number",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "pl"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "pluralReason"),
            ),
        ),
        tags = listOf("plural", "obj"),
        numberOverride = "pl",
    ),
    SkillRecipeRow(
        skill = "en:polarity.past",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "subjectVerb",
            values = listOf(
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "Past"),
                                FeatureValueRow(feature = "Person", value = "1"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "Pos"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "subjectVerb",
            values = listOf(
                PatternValueRow(
                    name = "verb",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.sentence.polarity",
                            bundle = listOf(
                                FeatureValueRow(feature = "Tense", value = "Past"),
                                FeatureValueRow(feature = "Person", value = "1"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Polarity", value = "Neg"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "polarityPastPrompt",
        explanationKey = "polarityExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Past"),
                            FeatureValueRow(feature = "Person", value = "1"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "Pos"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.sentence.polarity",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Past"),
                            FeatureValueRow(feature = "Person", value = "1"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Polarity", value = "Neg"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "polarityReason"),
            ),
        ),
        accepted = listOf(
            TextSpecRow(
                kind = TextSpecRowKind.PATTERN,
                key = "subjectVerb",
                values = listOf(
                    PatternValueRow(
                        name = "verb",
                        value = TextValueRow(kind = TextValueRowKind.LITERAL, value = "didn't walk"),
                    ),
                ),
            ),
        ),
        tags = listOf("polarity", "neg", "past"),
    ),
    SkillRecipeRow(
        skill = "en:verb.futureSimple",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Pres"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Simple"),
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
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Fut"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Simple"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "verbFuturePrompt",
        explanationKey = "verbTenseExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Simple"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Fut"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Simple"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "verbTenseReason"),
            ),
        ),
        tags = listOf("verb", "future", "subj"),
    ),
    SkillRecipeRow(
        skill = "en:pronouns",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PRONOUN,
                        genderToKey = listOf(GenderPronounRow(gender = "default", pronounId = "it")),
                        caseId = "Obj",
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
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PRONOUN,
                    genderToKey = listOf(GenderPronounRow(gender = "default", pronounId = "it")),
                    caseId = "Obj",
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "pronounReason"),
            ),
        ),
        tags = listOf("pronoun", "obj"),
    ),
    SkillRecipeRow(
        skill = "en:verb.presentContinuous",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "verbSentence",
            values = listOf(
                PatternValueRow(
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Pres"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Simple"),
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
                    name = "subj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        capitalized = true,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Subj"),
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
                                FeatureValueRow(feature = "Tense", value = "Pres"),
                                FeatureValueRow(feature = "Person", value = "3"),
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Aspect", value = "Continuous"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "verbContinuousPrompt",
        explanationKey = "verbTenseExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Simple"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.verb.tense",
                        bundle = listOf(
                            FeatureValueRow(feature = "Tense", value = "Pres"),
                            FeatureValueRow(feature = "Person", value = "3"),
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Aspect", value = "Continuous"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "verbTenseReason"),
            ),
        ),
        tags = listOf("verb", "continuous", "subj"),
    ),
    SkillRecipeRow(
        skill = "en:tense.contrast",
        staticOverride = StaticExerciseRow(
            sourceKey = "tenseContrastSource",
            promptKey = "tenseContrastPrompt",
            expectedKey = "tenseContrastExpected",
            explanationKey = "tenseContrastExplanation",
            acceptedKeys = listOf("tenseContrastAccepted"),
            changeFromKey = "tenseContrastFrom",
            changeToKey = "tenseContrastTo",
            changeReasonKey = "tenseContrastReason",
            nounId = "house",
            adjectiveId = "new",
            tags = listOf("tense", "contrast"),
        ),
    ),
    SkillRecipeRow(
        skill = "en:mixed",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                            ownerLexeme = "my",
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
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.agreement.possessive",
                            bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "mixedPrompt",
        explanationKey = "mixedExplanation",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(kind = TextValueRowKind.COPY, key = "mixedVerbFrom"),
                to = TextValueRow(kind = TextValueRowKind.COPY, key = "mixedVerbTo"),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "mixedVerbReason"),
            ),
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                        ownerLexeme = "my",
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.agreement.possessive",
                        bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "mixedOwnerReason"),
            ),
        ),
        tags = listOf("mixed", "past", "agreement"),
        ownerDraw = OwnerDrawRow(
            owners = listOf(
                OwnerChoiceRow(lexeme = "your", labelKey = "ownerYour"),
                OwnerChoiceRow(lexeme = "his", labelKey = "ownerHis"),
                OwnerChoiceRow(lexeme = "her", labelKey = "ownerHer"),
                OwnerChoiceRow(lexeme = "its", labelKey = "ownerIts"),
                OwnerChoiceRow(lexeme = "our", labelKey = "ownerOur"),
                OwnerChoiceRow(lexeme = "their", labelKey = "ownerTheir"),
            ),
            promptPrefixKey = "mixedOwnerPromptPrefix",
            promptSuffixKey = "mixedOwnerPromptSuffix",
        ),
    ),
)

val SKILL_RECIPE_ROW_ROWS: List<SkillRecipeRow> = skillRecipeRowRowsPart0()

private fun chainStepRecipeRowRowsPart0(): List<ChainStepRecipeRow> = listOf(
    ChainStepRecipeRow(
        source = TextSpecRow(
            kind = TextSpecRowKind.PREFIXED,
            spec = PhraseSpecRow(
                construction = "core.argument.case-role",
                bundle = listOf(
                    FeatureValueRow(feature = "Number", value = "sg"),
                    FeatureValueRow(feature = "Case", value = "Subj"),
                ),
            ),
            prefixCase = "Subj",
            punct = ".",
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        promptKey = "chainObjPrompt",
        explanationKey = "chainObjReason",
        changes = listOf(
            ChangeSpecRow(
                from = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Subj"),
                        ),
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainObjReason"),
            ),
        ),
        tags = listOf("obj"),
        ownerOut = "my",
    ),
    ChainStepRecipeRow(
        skill = "en:verb.pastSimple",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "seenObj",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
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
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
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
        tags = listOf("past"),
        ownerOut = "my",
    ),
    ChainStepRecipeRow(
        skill = "en:polarity.past",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainPast",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
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
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
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
                from = TextValueRow(kind = TextValueRowKind.COPY, key = "chainNegChangeFrom"),
                to = TextValueRow(kind = TextValueRowKind.COPY, key = "chainNegChangeTo"),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainNegReason"),
            ),
        ),
        tags = listOf("neg", "past"),
        ownerOut = "my",
    ),
    ChainStepRecipeRow(
        skill = "en:possessive.my",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainNeg",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "Obj"),
                            ),
                            ownerLexeme = "my",
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
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.agreement.possessive",
                            bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
                            ownerLexeme = "her",
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
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "Obj"),
                        ),
                        ownerLexeme = "my",
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.agreement.possessive",
                        bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
                        ownerLexeme = "her",
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainOwnerReason"),
            ),
        ),
        tags = listOf("agreement"),
        ownerOut = "her",
    ),
    ChainStepRecipeRow(
        skill = "en:role.location",
        source = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "chainNeg",
            values = listOf(
                PatternValueRow(
                    name = "obj",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.agreement.possessive",
                            bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
                            ownerLexeme = "her",
                        ),
                    ),
                ),
            ),
        ),
        expected = TextSpecRow(
            kind = TextSpecRowKind.PATTERN,
            key = "roleDrill",
            values = listOf(
                PatternValueRow(
                    name = "start",
                    value = TextValueRow(kind = TextValueRowKind.COPY, key = "chainLocStart"),
                ),
                PatternValueRow(
                    name = "target",
                    value = TextValueRow(
                        kind = TextValueRowKind.PHRASE,
                        spec = PhraseSpecRow(
                            construction = "core.argument.case-role",
                            bundle = listOf(
                                FeatureValueRow(feature = "Number", value = "sg"),
                                FeatureValueRow(feature = "Case", value = "In"),
                            ),
                            ownerLexeme = "her",
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
                        bundle = listOf(FeatureValueRow(feature = "Number", value = "sg")),
                        ownerLexeme = "her",
                    ),
                ),
                to = TextValueRow(
                    kind = TextValueRowKind.PHRASE,
                    spec = PhraseSpecRow(
                        construction = "core.argument.case-role",
                        bundle = listOf(
                            FeatureValueRow(feature = "Number", value = "sg"),
                            FeatureValueRow(feature = "Case", value = "In"),
                        ),
                        ownerLexeme = "her",
                    ),
                ),
                reason = ReasonSpecRow(kind = ReasonSpecRowKind.FIXED, key = "chainLocReason"),
            ),
        ),
        tags = listOf("role", "in"),
        ownerOut = "her",
    ),
)

val CHAIN_STEP_RECIPE_ROW_ROWS: List<ChainStepRecipeRow> = chainStepRecipeRowRowsPart0()

private fun recipeDefaultsRowRowsPart0(): List<RecipeDefaultsRow> = listOf(
    RecipeDefaultsRow(
        defaultOwnerLexeme = "my",
        verbLexeme = "walk",
        firstSkillByGender = listOf(GenderSkillRow(gender = "default", skill = "en:role.object")),
        constantSlots = listOf(
            ConstantSlotRow(
                slot = "prep",
                lexeme = "role",
                whenFeature = "Case",
                whenValues = listOf("In", "With", "To", "About", "Of"),
            ),
            ConstantSlotRow(slot = "aux", lexeme = "do", whenFeature = "Polarity", whenValues = listOf("Neg")),
            ConstantSlotRow(slot = "neg", lexeme = "not", whenFeature = "Polarity", whenValues = listOf("Neg")),
            ConstantSlotRow(slot = "aux", lexeme = "will", whenFeature = "Tense", whenValues = listOf("Fut")),
            ConstantSlotRow(slot = "aux", lexeme = "be", whenFeature = "Aspect", whenValues = listOf("Continuous")),
            ConstantSlotRow(slot = "aux", lexeme = "do", whenFeature = "Mood", whenValues = listOf("YesNoQ")),
            ConstantSlotRow(slot = "subject", lexeme = "you"),
        ),
    ),
)

val RECIPE_DEFAULTS_ROW_ROWS: List<RecipeDefaultsRow> = recipeDefaultsRowRowsPart0()

private fun lifehackRowRowsPart0(): List<LifehackRow> = listOf(
    LifehackRow(
        id = "en-ru.role.object.article",
        skillId = "en:role.object",
        text = "В русском нет артиклей вообще — при переводе на английский почти любое единственное исчисляемое существительное без «this/my/the» требует «a»: «дом» → «a house», не «house».",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Master, P. (1997). The English article system: acquisition, function, and pedagogy. System, 25(2), 215–232.",
            url = "https://doi.org/10.1016/S0346-251X(96)00063-8",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.role.object.wordorder",
        skillId = "en:role.object",
        text = "В русском грамматическая роль (кто субъект, кто объект) видна по падежному окончанию, а порядок слов свободный: «Мальчик видит собаку» и «Собаку видит мальчик» грамматичны и означают одно и то же. В английском падежных окончаний у существительных нет — роль определяется только порядком слов (SVO), поэтому «The dog sees the boy» и «The boy sees the dog» — два разных, противоположных по смыслу предложения.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Comrie, B. (1981). Language Universals and Linguistic Typology (2nd ed.). University of Chicago Press — типология кодирования грамматических отношений порядком слов вместо падежа.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.presentsimple.3rd-s",
        skillId = "en:verb.presentSimple",
        text = "В русском настоящее время последовательно изменяется по всем лицам (я вижу / ты видишь / он видит) — окончание никогда не «выпадает». В английском реально маркируется только 3-е лицо единственного числа (-s: he sees), а I/you/we/they see остаются без окончания. Отсюда систематическое или пропуск -s (по аналогии с «немаркированными» формами), или, наоборот, его добавление не туда.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Prévost, P., & White, L. (2000). Missing surface inflection or impairment in second language acquisition? Evidence from tense and agreement. Second Language Research, 16(2), 103–133 — общая для L2 английского тенденция непоследовательной маркировки согласования (Missing Surface Inflection), к которой типологически относится и пропуск -s носителями языков с иной моделью согласования.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.presentsimple.habitual",
        skillId = "en:verb.presentSimple",
        text = "Present Simple — это специально «немаркированное по аспекту» время для привычных и постоянных действий (I work here). Русское настоящее время само по себе не различает разовость и длительность: «работаю» покрывает и привычку, и процесс прямо сейчас. Отсюда риск выбрать Present Simple там, где по-английски обязателен Present Continuous.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Comrie, B. (1976). Aspect: An Introduction to the Study of Verbal Aspect and Related Problems. Cambridge University Press.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.possessive.my.invariant",
        skillId = "en:possessive.my",
        text = "Русское «мой/моя/моё/мои» согласуется с обладаемым по роду, числу и падежу («мой дом», «моя книга», «моё окно»). Английское «my» — одна неизменяемая форма для всего: «my house», «my book», «my window». Согласовывать «my» с существительным не нужно вообще — это частый источник избыточных попыток «просклонять» притяжательное местоимение.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Timberlake, A. (2004). A Reference Grammar of Russian. Cambridge University Press — парадигма согласования притяжательного «мой» по роду/числу/падежу обладаемого.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.possessive.my.gender-transfer",
        skillId = "en:possessive.my",
        text = "«His/her» в английском согласуются с родом ВЛАДЕЛЬЦА, не предмета — как и русские «его/её». Расхождения тут нет, но привычка искать согласование «с чем-нибудь» может дать сбой: если предмет по-русски грамматически «противоположного» рода (ручка — женский род), учащийся иногда ошибочно тянет род предмета на местоимение (*her pen про ручку мужчины), хотя должен ориентироваться только на пол владельца.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Corbett, G. G. (1991). Gender. Cambridge University Press — согласовательные класс и перенос привычки грамматического согласования по роду.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.mood.question.do-inversion",
        skillId = "en:mood.question",
        text = "В русском вопрос образуется интонацией или частицей «ли», порядок слов может не меняться: «Ты видишь дом» → «Ты видишь дом?». В английском вопрос требует вспомогательного глагола, которого нет в утверждении, и его выноса перед подлежащим: «You see the house» → «Do you see the house?». Учащиеся часто пытаются вопрос через интонацию, оставляя порядок слов утверждения.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "White, L. (2003). Second Language Acquisition and Universal Grammar. Cambridge University Press — усвоение функциональной категории do-support при отсутствии прямого аналога в родном языке.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.mood.question.wh-inversion",
        skillId = "en:mood.question",
        text = "После вопросительного слова в английском тоже нужна инверсия вспомогательного глагола: «What do you see?», а не «What you see?». В русском вопросительное слово просто ставится в начало, порядок остального предложения не трогается: «Что ты видишь?».",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Huddleston, R., & Pullum, G. K. (2002). The Cambridge Grammar of the English Language. Cambridge University Press — subject-auxiliary inversion в wh-вопросах.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.role.location.in-at-on",
        skillId = "en:role.location",
        text = "Русские предлоги места «в»/«на» не совпадают 1:1 с английскими in/at/on: «на почте» (буквально «на») — «at the post office» (не «on»), «в школе» — «at school» (не «in», если речь о посещении как деятельности, не о здании). Прямой перевод предлога систематически даёт неверный результат — предлог места нужно запоминать пословно с существительным, не выводить по русской модели.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Jarvis, S., & Pavlenko, A. (2008). Crosslinguistic Influence in Language and Cognition. Routledge — перенос пространственных категорий и их предложного оформления между языками.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.role.location.article",
        skillId = "en:role.location",
        text = "«At the theater» требует артикля, у русского «в театре» артикля нет вообще. Это тот же article-гэп, что и у role.object (см. соответствующий совет), но именно с локативными существительными его легко упустить, потому что внимание уходит на выбор предлога, а не артикля.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Ionin, T., Ko, H., & Wexler, K. (2004). Article Semantics in L2 Acquisition: The Role of Specificity. Language Acquisition, 12(1), 3–69 — усвоение системы артиклей носителями языков без артиклей, включая русский.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.role.instrument.with",
        skillId = "en:role.instrument",
        text = "Русский творительный падеж выражает орудие действия без предлога: «резать ножОМ». Английский творительной падежной формы не имеет — та же роль всегда выражается предлогом «with»: «cut it with a knife». Без «with» предложение по-английски грамматически неполно, хотя по-русски беспредложная форма — норма.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Timberlake, A. (2004). A Reference Grammar of Russian. Cambridge University Press — беспредложное инструментальное значение творительного падежа.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.role.instrument.omission",
        skillId = "en:role.instrument",
        text = "Из-за привычки к беспредложному творительному падежу учащиеся систематически пробуют пропустить «with» вообще: «I cut it a knife» вместо «I cut it with a knife». Это одна из самых частых и хорошо документированных ошибок переноса из безартиклевых, падежных языков в практике преподавания английского как иностранного.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.TEACHING_PRACTICE,
            citation = "Систематически фиксируется в практике преподавания английского как иностранного (TEFL/TESOL) как ошибка переноса из языков с беспредложным выражением инструмента падежом — документируется в материалах по типичным ошибкам учащихся с учётом родного языка (Cambridge Assessment English, TKT: Teaching Knowledge Test).",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.polarity.present.do-support",
        skillId = "en:polarity.present",
        text = "Отрицание в английском несёт вспомогательный «do/does», а не сам смысловой глагол: «I don't see», а не «I not see». Смысловой глагол в отрицании всегда в базовой форме — «do/does» берёт на себя время и лицо. В русском отрицательная частица «не» просто ставится перед смысловым глаголом без вспомогательного слова, поэтому первая интуиция — перенести именно эту модель.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Lightbown, P. M., & Spada, N. (2013). How Languages Are Learned (4th ed.). Oxford University Press — do-support как типичная точка ошибки при отсутствии аналогичной вспомогательной конструкции в родном языке.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.polarity.present.contraction",
        skillId = "en:polarity.present",
        text = "«Don't/doesn't» — это одна фонетическая и орфографическая единица, а не «do» плюс отдельное слово «not» с паузой между ними. Русское «не» всегда отдельное, самостоятельно произносимое слово — учащиеся иногда произносят «do not» медленно и раздельно там, где естественная английская речь почти всегда даёт слитную форму.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Odlin, T. (1989). Language Transfer: Cross-Linguistic Influence in Language Learning. Cambridge University Press — перенос границ морфем и словоформ между языками.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.pastsimple.irregular",
        skillId = "en:verb.pastSimple",
        text = "Правильные глаголы получают -ed (walk → walked), но частотные неправильные глаголы (go → went, see → saw) меняют форму непредсказуемо и должны запоминаться как отдельный факт словаря, а не выводиться правилом — в отличие от русского прошедшего времени, где показатель -л- работает почти без исключений для всех глаголов.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Huddleston, R., & Pullum, G. K. (2002). The Cambridge Grammar of the English Language. Cambridge University Press — морфология правильных и неправильных форм прошедшего времени.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.pastsimple.no-person",
        skillId = "en:verb.pastSimple",
        text = "Здесь английский проще русского: Past Simple не меняется по лицу и числу (I/he/we/they walked — одна форма), тогда как русское прошедшее время меняется по роду и числу (читал/читала/читали). Учащиеся иногда по инерции ищут «свою» форму глагола для каждого лица и напрасно усложняют себе задачу — достаточно выучить одну форму на глагол.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Comrie, B. (1981). Language Universals and Linguistic Typology (2nd ed.). University of Chicago Press — типология согласования глагола по лицу/роду/числу.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.role.recipient.to",
        skillId = "en:role.recipient",
        text = "Русский дательный падеж выражает получателя без предлога через окончание: «дать книгу другУ». В английском той же роли соответствует только предлог «to»: «give the book to a friend» — падежного окончания, которое взяло бы эту роль на себя, у английского существительного нет.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Timberlake, A. (2004). A Reference Grammar of Russian. Cambridge University Press — дательный падеж как беспредложный маркер получателя.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.role.recipient.word-order",
        skillId = "en:role.recipient",
        text = "В «give a friend the book» (без «to») получатель и предмет различаются только порядком слов — получатель всегда идёт первым. В русском такой альтернативы вообще не нужно: дательный падеж однозначно маркирует получателя независимо от порядка слов, поэтому порядок в этой английской конструкции нельзя менять свободно, как в русском.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Huddleston, R., & Pullum, G. K. (2002). The Cambridge Grammar of the English Language. Cambridge University Press — двойное дополнение (double object construction) и порядок слов.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.number.plural.irregular",
        skillId = "en:number.plural",
        text = "Большинство существительных получают -s/-es (book → books), но частотные слова меняют форму непредсказуемо (child → children, man → men, mouse → mice) — это факты словаря, которые нужно выучить отдельно, правило -s на них не работает, как и в русском множественное число не для всех существительных образуется одинаково регулярным способом.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Huddleston, R., & Pullum, G. K. (2002). The Cambridge Grammar of the English Language. Cambridge University Press — регулярная и нерегулярная морфология множественного числа.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.number.plural.countability",
        skillId = "en:number.plural",
        text = "Исчисляемость слова не всегда совпадает между языками: «news» в английском — неисчисляемое и грамматически единственное («The news is good»), а русское «новости» — множественное. «Advice» — неисчисляемое единственное, а русское «советы» обычно множественное. Множественное число нельзя переносить по русской интуиции слово в слово — категорию исчисляемости нужно проверять для каждого конкретного слова.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Jarvis, S., & Pavlenko, A. (2008). Crosslinguistic Influence in Language and Cognition. Routledge — перенос категорий исчисляемости и числа между языками.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.polarity.past.bare-verb",
        skillId = "en:polarity.past",
        text = "В отрицании прошедшего времени время маркирует только вспомогательный «did», а смысловой глагол возвращается в базовую форму: «I didn't go», а не «I didn't went». Двойная маркировка прошедшего времени (и на «did», и на смысловом глаголе) — одна из самых частых ошибок именно потому, что она выглядит «более правильной» по интуиции.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Prévost, P., & White, L. (2000). Missing surface inflection or impairment in second language acquisition? Second Language Research, 16(2), 103–133 — избыточная и непоследовательная маркировка времени в межъязыковых системах учащихся.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.polarity.past.n-t",
        skillId = "en:polarity.past",
        text = "«Didn't» — вспомогательный глагол «did» плюс отрицательная частица «not», слитые в одну форму. В устной практике «didn't» почти всегда произносится как единая слитная форма — как и в present-отрицании, ошибочно проговаривать «did not» раздельно и медленно там, где естественнее слитная форма.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.TEACHING_PRACTICE,
            citation = "Систематически фиксируется в практике преподавания английского как иностранного (TEFL/TESOL) как перенос интонационной модели отдельно стоящей отрицательной частицы из родного языка — документируется в материалах по типичным ошибкам учащихся (Cambridge Assessment English, TKT: Teaching Knowledge Test).",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.futuresimple.will-invariant",
        skillId = "en:verb.futureSimple",
        text = "«Will» не изменяется по лицам и числам — одна форма для всех: I/you/he/we/they will go. Русское будущее время несовершенного вида спрягается полностью: буду/будешь/будет/будем/будете/будут. Учащиеся иногда по привычке ищут для «will» соответствующую форму под каждое лицо — искать не нужно, форма одна.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Timberlake, A. (2004). A Reference Grammar of Russian. Cambridge University Press — парадигма спряжения будущего времени несовершенного вида (буду + инфинитив).",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.futuresimple.aspect-gap",
        skillId = "en:verb.futureSimple",
        text = "Русское будущее время различает совершенный и несовершенный вид (сделаю vs буду делать) — два разных грамматических способа говорить о будущем действии с разным значением завершённости. Английское «will» такого различия сам по себе не делает — оно покрывает оба случая одной формой, поэтому пытаться найти для «will» аналог видовой пары бессмысленно.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Comrie, B. (1976). Aspect: An Introduction to the Study of Verbal Aspect and Related Problems. Cambridge University Press — вид как категория, не совпадающая по границам между языками.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.pronouns.you-collapse",
        skillId = "en:pronouns",
        text = "Английское «you» — одна форма и для «ты», и для «вы» (и вежливого, и множественного числа). Различие ты/вы, которое в русском обязательно нужно выбрать, в английском местоимении никак не выражается — оно снимается контекстом или обращением, а не отдельной формой местоимения.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Brown, R., & Gilman, A. (1960). The pronouns of power and solidarity. In T. A. Sebeok (Ed.), Style in Language. MIT Press — классическое описание T/V-различия местоимений и языков, где оно отсутствует.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.pronouns.it-not-grammatical-gender",
        skillId = "en:pronouns",
        text = "В русском предмет заменяется местоимением по грамматическому роду существительного: «стол» (муж. р.) → «он», «ручка» (жен. р.) → «она». В английском все неживые предметы, независимо от рода русского слова-эквивалента, заменяются одним «it» — грамматического рода у английских существительных нет вообще, и переносить род русского слова на выбор местоимения не нужно.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Corbett, G. G. (1991). Gender. Cambridge University Press — согласовательный класс «род» и его отсутствие в системе местоимений английского для неодушевлённых предметов.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.presentcontinuous.stative",
        skillId = "en:verb.presentContinuous",
        text = "Глаголы состояния (know, like, love, want, believe) в английском обычно не берут -ing, даже когда речь о текущем моменте: «I know», не «I am knowing». Русский язык такого лексического ограничения не делает — «-ing»-форму приходится проверять по конкретному глаголу, не выводить из одной общей грамматической логики.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Comrie, B. (1976). Aspect: An Introduction to the Study of Verbal Aspect and Related Problems. Cambridge University Press — лексический класс стативных глаголов и их несовместимость с прогрессивом.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.verb.presentcontinuous.overgeneralize",
        skillId = "en:verb.presentContinuous",
        text = "Поскольку русское настоящее время не различает привычное действие и действие в процессе, есть и обратный риск: перенести это несовпадение на английский и начать применять -ing к привычным действиям («I am working here every day» вместо «I work here every day»). -ing маркирует именно процесс, ограниченный текущим отрезком времени, а не привычку.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Odlin, T. (1989). Language Transfer: Cross-Linguistic Influence in Language Learning. Cambridge University Press — перенос аспектуальных категорий родного языка на грамматику второго языка.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.tense.contrast.aspect-not-tense",
        skillId = "en:tense.contrast",
        text = "Simple/Continuous в английском — это ось Aspect, отдельная от оси Tense (Present/Past/Future); одно и то же время может быть и Simple, и Continuous. Русский совершенный/несовершенный вид — тоже отдельная от времени категория, но границы между «завершённость» (русский вид) и «процесс в моменте» (английский прогрессив) не совпадают 1:1 — прямое отождествление двух систем даёт систематические ошибки в обе стороны.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Comrie, B. (1976). Aspect: An Introduction to the Study of Verbal Aspect and Related Problems. Cambridge University Press — несовпадение границ аспектуальных систем между языками.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.tense.contrast.two-axes",
        skillId = "en:tense.contrast",
        text = "Полезно держать в голове две независимые оси одновременно: «когда» (Present/Past/Future) и «как» (Simple/Continuous), а не искать для каждой русской видо-временной формы одну готовую английскую. Такой перенос «один русский ярлык → один английский ярлык» — систематический источник ошибок именно потому, что оси в двух языках не совпадают по количеству и границам значений.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Odlin, T. (1989). Language Transfer: Cross-Linguistic Influence in Language Learning. Cambridge University Press — межъязыковой перенос грамматических категорий по принципу прямого соответствия.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.mixed.interleaving",
        skillId = "en:mixed",
        text = "Упражнения, где вперемешку встречаются разные конструкции (mixed), сложнее блочной тренировки «одно и то же по одной теме подряд» — но именно эта сложность и даёт более прочное запоминание на дистанции, потому что заставляет каждый раз заново распознавать, какое правило применить, а не действовать по инерции предыдущего задания.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Rohrer, D., & Taylor, K. (2007). The shuffling of mathematics problems improves learning. Instructional Science, 35(6), 481–498 — эффект интерливинга (chередование задач разных типов) для долговременного запоминания.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
    LifehackRow(
        id = "en-ru.mixed.cognitive-load",
        skillId = "en:mixed",
        text = "Когда в одном предложении нужно одновременно решить и время, и отрицание, и порядок слов (вопрос), нагрузка распределяется — полезно сначала мысленно зафиксировать время и полярность отдельно от порядка слов, а затем собирать предложение по частям, а не пытаться удержать все решения одновременно.",
        source = LifehackSourceRow(
            kind = LifehackSourceRowKind.RESEARCH,
            citation = "Sweller, J. (1988). Cognitive load during problem solving: Effects on learning. Cognitive Science, 12(2), 257–285 — управление когнитивной нагрузкой при одновременном применении нескольких правил.",
        ),
        status = LifehackRowStatus.EDITORIAL,
    ),
)

val LIFEHACK_ROW_ROWS: List<LifehackRow> = lifehackRowRowsPart0()
