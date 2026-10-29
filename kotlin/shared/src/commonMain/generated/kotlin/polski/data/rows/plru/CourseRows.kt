// Generated from dsl/course.data-contract
// source-sha256=8d177f9bb4865d3fc7369ca236cefea9f7520711e60dab029ccddd67c64addc4
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

data class FixedRow(
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
    val fixed: List<FixedRow>,
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Case", value = "Acc"),
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
        fixed = listOf(FixedRow(feature = "Person", value = "3"), FixedRow(feature = "Number", value = "Sing")),
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
        fixed = listOf(FixedRow(feature = "Person", value = "3"), FixedRow(feature = "Number", value = "Sing")),
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
        fixed = listOf(FixedRow(feature = "Person", value = "3"), FixedRow(feature = "Number", value = "Sing")),
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
        fixed = listOf(FixedRow(feature = "Person", value = "1"), FixedRow(feature = "Number", value = "Sing")),
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
            FixedRow(feature = "Case", value = "Acc"),
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "2"),
            FixedRow(feature = "Number", value = "Sing"),
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
            FixedRow(feature = "Case", value = "Acc"),
            FixedRow(feature = "Tense", value = "Pres"),
            FixedRow(feature = "Person", value = "1"),
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
