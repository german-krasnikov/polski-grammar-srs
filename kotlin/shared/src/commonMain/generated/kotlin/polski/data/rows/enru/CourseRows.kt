// Generated from dsl/course.data-contract
// source-sha256=ad63a057e2973d0916a2de0ff23081414305075a27a2cc08f74c9721a9bd26f3
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

val EN_NOUN_ROW_ROWS: List<EnNounRow> = listOf(
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

val EN_ADJECTIVE_ROW_ROWS: List<EnAdjectiveRow> = listOf(
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

val EN_VERB_ROW_ROWS: List<EnVerbRow> = listOf(
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

val EN_PRONOUN_ROW_ROWS: List<EnPronounRow> = listOf(
    EnPronounRow(id = "I", forms = EnPronounFormsRow(Subj = "I", Obj = "me")),
    EnPronounRow(id = "you", forms = EnPronounFormsRow(Subj = "you", Obj = "you")),
    EnPronounRow(id = "he", forms = EnPronounFormsRow(Subj = "he", Obj = "him")),
    EnPronounRow(id = "she", forms = EnPronounFormsRow(Subj = "she", Obj = "her")),
    EnPronounRow(id = "it", forms = EnPronounFormsRow(Subj = "it", Obj = "it")),
    EnPronounRow(id = "we", forms = EnPronounFormsRow(Subj = "we", Obj = "us")),
    EnPronounRow(id = "they", forms = EnPronounFormsRow(Subj = "they", Obj = "them")),
)

val POSSESSIVE_ROW_ROWS: List<PossessiveRow> = listOf(
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

val EN_PREPOSITION_ROW_ROWS: List<EnPrepositionRow> = listOf(
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
