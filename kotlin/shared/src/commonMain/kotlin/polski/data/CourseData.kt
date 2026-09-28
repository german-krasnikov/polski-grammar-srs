package polski.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.model.*
import polski.pack.CoursePackSource
import polski.presentation.ContrastPair
import polski.presentation.EndingPart
import polski.presentation.NativeParallelPair
import polski.presentation.StemAlternation
import polski.presentation.TableRow

data class MethodPresentation(
    val introduction: String,
    val promptLead: String,
    val introduce: String,
    val retrieve: String,
    val feedback: String,
    val review: String,
)
data class SkillPresentation(
    val focusBefore: String,
    val focusAfter: String,
    val logic: MethodPresentation,
    val situations: MethodPresentation,
)
/**
 * Per-skill override content for the 4 presentation styles (UC-10), additive and all-optional:
 * a missing field means the [StyleComposer] block derives from existing skill/exercise data
 * instead (see the field-by-field derivation table in `Plans/Kotlin/StylesBlueprint.md`§3).
 * Only [nativeParallel] has no generic derivation and needs real per-skill authoring.
 */
data class SkillStyleContent(
    val rule: String? = null,
    val table: List<TableRow>? = null,
    val scene: String? = null,
    val nativeParallel: List<NativeParallelPair> = emptyList(),
    val examples: List<String> = emptyList(),
    val why: String? = null,
)
data class VocabularyItem(
    val id: String,
    val lemma: String,
    val translation: String,
    val form: String,
    val example: String,
    val level: String,
    val frequencyRank: Int?,
    val custom: Boolean = false,
)
data class FrequencyItem(val rank: Int, val lemma: String, val count: Int)
data class CaseReferenceRow(val id: GramCase, val pl: String, val ru: String, val question: String, val trigger: String, val skill: String?)
data class ReferenceChainRow(val label: String, val from: String, val to: String, val change: String)
data class ChainStep(val id: String, val label: String)
data class ChainCompletion(val title: String, val reactEyebrow: String, val reactBody: String, val webBody: String)
data class ChainPresentation(val steps: List<ChainStep>, val completion: ChainCompletion) {
    val summary: String get() = steps.joinToString(" → ") { it.label }
}
/** [steps] is the same arrow chain as [example] (`steps.joinToString(" → ") == example`), kept
 * structured so a host can highlight each consecutive pair with [polski.presentation.ContrastPair]
 * instead of parsing the prose arrow (EmphasisUXAudit E6). */
data class ReferenceSystemCard(val id: String, val title: String, val explanation: String, val example: String, val steps: List<String>)
data class ReferencePipelineStep(val id: String, val label: String, val question: String, val example: String)
data class ReferencePipeline(val title: String, val steps: List<ReferencePipelineStep>, val compactExample: String) {
    val compactSummary: String get() = steps.joinToString(" → ") { it.question }
}
data class CaseTeaching(val reactNote: String, val compactNote: String, val comparisonReadingHint: String)
data class VerbLabel(val full: String, val compact: String)
data class VerbGenderOption(val id: String, val label: VerbLabel)
data class VerbSubject(
    val id: String,
    val person: Person,
    val number: NumberGram,
    val selectedGender: Boolean,
    val fixedGender: Gender?,
    val label: VerbLabel,
) {
    fun gender(feminineGroup: Boolean): Gender =
        if (selectedGender) if (feminineGroup) Gender.F else Gender.M_PERSONAL else requireNotNull(fixedGender)
}
data class VerbTeaching(
    val subjects: List<VerbSubject>,
    val genderControlLabel: VerbLabel,
    val genderOptions: List<VerbGenderOption>,
    val tenseLabels: Map<Tense, VerbLabel>,
    val reactFutureExplanation: String,
    val compactFutureExplanation: String,
)
data class PronounCue(val full: String, val compact: String, val ios: String)
data class PronounContext(
    val id: GramCase,
    val cue: PronounCue,
    val caseName: String,
    val valuePrefix: String,
    val specialValues: Map<String, String>,
) {
    fun value(pronounId: String, forms: Map<GramCase, String>): String =
        specialValues[pronounId] ?: "$valuePrefix${forms.getValue(id)}"
    fun androidLine(pronounId: String, forms: Map<GramCase, String>): String =
        "${cue.compact} ${if (id == GramCase.LOC) forms.getValue(id) else value(pronounId, forms)}"
}
data class PossessiveDemoCase(val id: GramCase, val web: String, val desktop: String, val ios: String)
data class PossessiveDemo(
    val nounId: String,
    val adjectiveId: String,
    val number: NumberGram,
    val cases: List<PossessiveDemoCase>,
    val invariableOwnerIds: Set<PossessiveId>,
    val invariableRule: String,
    val variableRule: String,
) {
    fun phrase(owner: PossessiveId, gramCase: GramCase): String =
        polski.grammar.nounPhrase(nounId, gramCase, number, adjectiveId, owner)
    fun rule(owner: PossessiveId): String = if (owner in invariableOwnerIds) invariableRule else variableRule
}
data class PronounTeaching(
    val personalTitle: String,
    val reactIntro: String,
    val compactIntro: String,
    val reactFooter: String,
    val webFooter: String,
    val nativeFooter: String,
    val pronounIds: List<String>,
    val contexts: List<PronounContext>,
    val possessiveTitle: String,
    val demo: PossessiveDemo,
)
data class RussianSupportTable(val construction: String, val check: String)
data class RussianSupportRow(
    val id: String,
    val cue: String,
    val react: RussianSupportTable,
    val web: RussianSupportTable,
    val desktop: RussianSupportTable,
    val mobileLine: String,
    val comparisons: List<ContrastPair>,
)
data class RussianSupport(
    val fullTitle: String,
    val compactTitle: String,
    val columns: List<String>,
    val rows: List<RussianSupportRow>,
)
data class ReferenceTenseRow(val label: String, val from: String, val to: String)
data class ReferenceAspectRow(val label: String, val from: String, val present: String?, val past: String, val future: String)
data class MaleAccExample(val from: String, val to: String, val sentence: String)
data class MaleAccRow(val id: String, val label: String, val title: String, val examples: List<MaleAccExample>, val rule: String)
data class CourseContextHelp(val react: String, val compact: String)
data class CourseAspectNoPresent(val compact: String, val ios: String)
data class CourseVocabularyInstructions(val react: String, val web: String, val native: String, val ios: String)

internal sealed interface CoursePossessiveForms {
    data class Invariant(val value: String) : CoursePossessiveForms
    data class Declined(
        val singular: Map<Gender, Map<GramCase, String>>,
        val plural: Map<String, Map<GramCase, String>>,
    ) : CoursePossessiveForms
}

internal data class FutureAuxiliary(val verbId: String, val forms: Map<NumberGram, Map<Person, String>>)

/**
 * One course pack's parsed content, read from [source] as schema v1 (a `course.json` under
 * `courses`, UC-02's [CoursePackSource]). Polish/Russian is the only pack today, so every field below is
 * still pl-ru-shaped; a second pack would need its own schema before this class stops being
 * pl-ru-specific.
 */
internal class CoursePack(private val source: CoursePackSource) {
    private val root: JsonObject by lazy {
        Json.parseToJsonElement(source.load()).jsonObject.also { pack ->
            require(pack.getValue("schemaVersion").jsonPrimitive.int == 1)
            require(pack.string("id") == source.id)
            // EN-06 (gap G): no hardcoded target/native literal here — pl-ru is still the only
            // real pack, so its pl/ru strings keep flowing through unchanged, but the check no
            // longer refuses a second pack's own languages.
            pack.string("targetLanguage")
            pack.string("nativeLanguage")
        }
    }

    val id: String by lazy { root.string("id") }

    /** UC-04: vocabulary's pair scope (`${target}-${native}`) — v1 packs have `id == pairId`
     * (one pack per pair), but the two stay conceptually distinct for a future schema where a
     * pack id could version content without changing the pair. */
    val pairId: String by lazy { "${root.string("targetLanguage")}-${root.string("nativeLanguage")}" }

    /** EN-08 (gap G): the two halves of [pairId] as their own accessors, for preferences'
     * `CourseSelection(target, native, style)` and future per-language pickers (EN-22). */
    val targetLanguage: String by lazy { root.string("targetLanguage") }
    val nativeLanguage: String by lazy { root.string("nativeLanguage") }

    /**
     * EN-22 fix (EnRuAcceptance §7 item 1): a case-declining, gendered noun table — this is pl's
     * own grammar shape, not a universal fact every pack has (English nouns have neither). A row
     * without a "gender" field (en's `lexicon.json`) simply isn't this shape and is skipped rather
     * than force-parsed into false gender/case data — [nouns] is honestly empty for such a pack,
     * not fabricated. pl's rows all declare "gender", so pl's list is unchanged.
     */
    val nouns: List<Noun> by lazy { root.rows("nouns").mapNotNull { value ->
        val gender = value.optionalString("gender")?.let(Gender::fromId) ?: return@mapNotNull null
        Noun(value.string("id"), value.string("lemma"), value.string("meaning"), gender,
            NumberGram.entries.associateWith { number -> value.obj("forms").obj(number.id).caseForms() })
    }.requireUniqueIds(Noun::id) }

    /** Same reasoning as [nouns]: a row whose "forms" isn't number×gender×case-shaped (en's flat
     *  `{"invariant": "..."}`) is skipped, not force-parsed. */
    val adjectives: List<Adjective> by lazy { root.rows("adjectives").mapNotNull { value ->
        val forms = value.obj("forms")
        if (!NumberGram.entries.all { forms.containsKey(it.id) }) return@mapNotNull null
        Adjective(value.string("id"), value.string("lemma"), value.string("meaning"),
            NumberGram.entries.associateWith { number ->
                Gender.entries.associateWith { gender -> forms.obj(number.id).obj(gender.id).caseForms() }
            })
    }.requireUniqueIds(Adjective::id) }

    /** Same reasoning as [nouns]: a row without an "aspect" field (en's verbs have no pl-shaped
     *  `pastStem`/`futureType`) is skipped, not force-parsed. */
    val verbs: List<Verb> by lazy { root.rows("verbs").mapNotNull { value ->
        val aspectId = value.optionalString("aspect") ?: return@mapNotNull null
        val past = value.obj("pastStem")
        Verb(value.string("id"), value.string("lemma"), value.string("meaning"),
            Aspect.entries.first { it.id == aspectId },
            value["present"]?.jsonObject?.let { present ->
                NumberGram.entries.associateWith { number ->
                    val forms = present.obj(number.id)
                    Person.entries.associateWith { person -> forms.string(person.id.toString()) }
                }
            },
            PastStem(past.string("m"), past.string("f"), past.optionalString("n"), past.string("mp"), past.string("np")),
            FutureType.entries.first { it.id == value.string("futureType") }, value.optionalString("perfectivePair"))
    }.requireUniqueIds(Verb::id) }

    val skills: List<Skill> by lazy { root.rows("skills").map { value ->
        Skill(value.string("id"), value.string("title"), value.string("group"), value.string("level"),
            value.string("formula"), value.string("theory"), value.string("hint"),
            value.getValue("prerequisites").jsonArray.map { it.jsonPrimitive.content })
    }.requireUniqueIds(Skill::id) }

    /** Validated against the pack's own raw noun/adjective ids (every pack declares these,
     *  whichever shape they end up in) rather than [nouns]/[adjectives] — those two are narrowed
     *  to pl's gendered/case-declining shape and would wrongly reject a caseless pack's seeds. */
    val sentenceSeeds: List<SentenceSeed> by lazy {
        val nounIds = root.rows("nouns").map { it.string("id") }.toSet()
        val adjectiveIds = root.rows("adjectives").map { it.string("id") }.toSet()
        root.rows("sentenceSeeds").map { value -> SentenceSeed(value.string("nounId"), value.string("adjectiveId")) }
            .also { seeds ->
                require(seeds.isNotEmpty() && seeds.distinct().size == seeds.size)
                require(seeds.all { seed -> seed.nounId in nounIds && seed.adjectiveId in adjectiveIds })
            }
    }

    /** UC S2: regular stem alternations this pack declares (e.g. Polish `ó~o`), consumed only by
     * `EndingHighlight.kt`'s reliability check — optional, defaults to none for a pack without any. */
    val stemAlternations: List<StemAlternation> by lazy { parseStemAlternations(root["stemAlternations"]) }

    val caseSentencePrefixes: Map<String, String> by lazy {
        root.obj("caseSentencePrefixes").mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    val exerciseCopy: Map<String, String> by lazy {
        root.obj("exerciseCopy").mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    val exercisePatterns: Map<String, String> by lazy {
        root.obj("exercisePatterns").mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    val chainPresentation: ChainPresentation by lazy {
        val value = root.obj("training").obj("chainPresentation")
        val completion = value.obj("completion")
        ChainPresentation(
            value.rows("steps").map { ChainStep(it.string("id"), it.string("label")) }.requireUniqueIds(ChainStep::id),
            ChainCompletion(
                completion.string("title"), completion.string("reactEyebrow"),
                completion.string("reactBody"), completion.string("webBody"),
            ),
        )
    }

    /**
     * EN-22 fix (EnRuAcceptance §7 item 1): every property below through [aspectNoPresent] reads
     * pl's own `reference` block — hand-authored teaching copy for pl's specific case/gender/verb
     * system (Polish declension tables, gender-driven agreement rules…), not a structure every pack
     * necessarily has. en-ru's own `reference` is genuinely empty (its lang has neither case nor
     * gender to teach this way) — each property is therefore nullable, returning null when its own
     * top-level key is absent instead of force-parsing nothing into it. pl's `reference` still has
     * every one of these keys, so pl's parse is byte-identical to before.
     */
    private val reference: JsonObject by lazy { root.obj("reference") }

    val caseReferenceRows: List<CaseReferenceRow>? by lazy {
        val rows = reference.optRows("caseRows") ?: return@lazy null
        rows.map { value ->
            CaseReferenceRow(GramCase.fromId(value.string("id")), value.string("pl"), value.string("ru"),
                value.string("question"), value.string("trigger"), value.optionalString("skill"))
        }.also { parsed -> require(parsed.map { it.id } == listOf(GramCase.NOM, GramCase.GEN, GramCase.DAT, GramCase.ACC, GramCase.INST, GramCase.LOC, GramCase.VOC)) }
    }

    val referenceGenderNames: Map<String, String>? by lazy {
        val names = reference.optObj("genderNames") ?: return@lazy null
        names.mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    val referenceChainRows: List<ReferenceChainRow>? by lazy {
        val rows = reference.optRows("chainRows") ?: return@lazy null
        rows.map { value ->
            ReferenceChainRow(value.string("label"), value.string("from"), value.string("to"), value.string("change"))
        }.also { parsed ->
            require(parsed.size == 5 && (1 until parsed.size).all { parsed[it].from == parsed[it - 1].to })
        }
    }

    val referenceSystemCards: List<ReferenceSystemCard>? by lazy {
        val rows = reference.optRows("systemCards") ?: return@lazy null
        rows.mapIndexed { index, value ->
            val steps = value.getValue("steps").jsonArray.map { it.jsonPrimitive.content }
            val example = value.string("example")
            require(steps.size >= 2 && steps.joinToString(" → ") == example) {
                "/reference/systemCards/$index/steps: must join with ' → ' into example"
            }
            ReferenceSystemCard(value.string("id"), value.string("title"), value.string("explanation"), example, steps)
        }.also { cards ->
            require(cards.map { it.id } == listOf("noun", "agreement", "verb", "modifiers"))
        }
    }

    val referencePipeline: ReferencePipeline? by lazy {
        val pipeline = reference.optObj("pipeline") ?: return@lazy null
        ReferencePipeline(
            pipeline.string("title"),
            pipeline.rows("steps").map { step ->
                ReferencePipelineStep(step.string("id"), step.string("label"), step.string("question"), step.string("example"))
            },
            pipeline.string("compactExample"),
        ).also { value ->
            require(value.steps.map { it.id } == listOf("intent", "case", "agreement"))
        }
    }

    val referenceCaseTeaching: CaseTeaching? by lazy {
        val teaching = reference.optObj("caseTeaching") ?: return@lazy null
        val note = teaching.obj("caseNote")
        CaseTeaching(note.string("react"), note.string("compact"), teaching.string("comparisonReadingHint"))
    }

    val referencePronounTeaching: PronounTeaching? by lazy {
        val teaching = reference.optObj("pronounTeaching") ?: return@lazy null
        val personal = teaching.obj("personal")
        val intro = personal.obj("intro")
        val footer = personal.obj("footer")
        val possessive = teaching.obj("possessive")
        val demo = possessive.obj("demo")
        PronounTeaching(
            personal.string("title"), intro.string("react"), intro.string("compact"),
            footer.string("react"), footer.string("web"), footer.string("native"),
            personal.getValue("pronounIds").jsonArray.map { it.jsonPrimitive.content },
            teaching.rows("contexts").map { row ->
                val id = GramCase.fromId(row.string("id"))
                require(GramCase.fromId(row.string("caseId")) == id)
                val cue = row.obj("cue")
                PronounContext(id, PronounCue(cue.string("full"), cue.string("compact"), cue.string("ios")),
                    row.string("caseName"), row.optionalString("valuePrefix") ?: "",
                    row["specialValues"]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content } ?: emptyMap())
            },
            possessive.string("title"),
            PossessiveDemo(
                demo.string("nounId"), demo.string("adjectiveId"), NumberGram.fromId(demo.string("number")),
                demo.rows("cases").map { row ->
                    val caption = row.obj("caption")
                    PossessiveDemoCase(GramCase.fromId(row.string("id")), caption.string("web"),
                        caption.string("desktop"), caption.string("ios"))
                },
                demo.getValue("invariableOwnerIds").jsonArray.map { PossessiveId.fromId(it.jsonPrimitive.content) }.toSet(),
                demo.obj("rule").string("invariable"), demo.obj("rule").string("variable"),
            ),
        ).also { value ->
            require(value.pronounIds == listOf("ja", "ty", "on", "ona", "ono", "my", "wy", "oni", "one"))
            require(value.contexts.map { it.id } == listOf(GramCase.GEN, GramCase.DAT, GramCase.ACC, GramCase.INST, GramCase.LOC))
            require(value.demo.cases.map { it.id } == listOf(GramCase.NOM, GramCase.ACC, GramCase.GEN))
        }
    }

    val referenceVerbTeaching: VerbTeaching? by lazy {
        val teaching = reference.optObj("verbTeaching") ?: return@lazy null
        VerbTeaching(
            teaching.rows("subjects").map { subject ->
                VerbSubject(subject.string("id"), Person.fromId(subject.getValue("person").jsonPrimitive.int),
                    NumberGram.fromId(subject.string("number")), subject.string("genderMode") == "selected",
                    subject.optionalString("fixedGender")?.let(Gender::fromId), subject.obj("label").verbLabel())
            },
            teaching.obj("genderControlLabel").verbLabel(),
            teaching.rows("genderOptions").map { VerbGenderOption(it.string("id"), it.obj("label").verbLabel()) },
            Tense.entries.associateWith { teaching.obj("tenseLabels").obj(it.id).verbLabel() },
            teaching.obj("futureExplanation").string("react"),
            teaching.obj("futureExplanation").string("compact"),
        )
    }

    val comparisonNounIds: List<String>? by lazy {
        val ids = (reference["comparisonNounIds"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: return@lazy null
        ids.also {
            require(it.size == 7 && it.distinct().size == it.size)
            require(it.all { id -> nouns.any { noun -> noun.id == id } })
        }
    }

    val referenceRussianSupport: RussianSupport? by lazy {
        val support = reference.optObj("russianSupport") ?: return@lazy null
        val title = support.obj("title")
        RussianSupport(
            title.string("full"), title.string("compact"),
            support.getValue("columns").jsonArray.map { it.jsonPrimitive.content },
            support.rows("rows").mapIndexed { index, row ->
                require(row["comparisons"] != null) {
                    "/reference/russianSupport/rows/$index/comparisons: missing comparisons"
                }
                RussianSupportRow(
                    row.string("id"), row.string("cue"),
                    row.obj("react").russianSupportTable(),
                    row.obj("web").russianSupportTable(),
                    row.obj("desktop").russianSupportTable(),
                    row.string("mobileLine"),
                    row.rows("comparisons").mapIndexed { pairIndex, pair ->
                        pair.russianSupportComparison("/reference/russianSupport/rows/$index/comparisons/$pairIndex")
                    }.also { require(it.isNotEmpty()) { "/reference/russianSupport/rows/$index/comparisons: empty comparisons" } },
                )
            },
        ).also { value ->
            require(value.columns.size == 3 && value.columns.all(String::isNotBlank))
            require(value.rows.map { it.id } == listOf("accusative", "instrumental", "locative", "possessive"))
            require(value.rows.all { it.mobileLine.startsWith("${it.cue} → ") })
        }
    }

    val referenceTenseRows: List<ReferenceTenseRow>? by lazy {
        val rows = reference.optRows("tenseRows") ?: return@lazy null
        rows.map { value ->
            ReferenceTenseRow(value.string("label"), value.string("from"), value.string("to"))
        }.also { parsed ->
            require(parsed.size == 5 && parsed.indices.all { index ->
                parsed[index].from == if (index == 2) parsed[1].to else parsed[0].to
            })
        }
    }

    val referenceAspectRows: List<ReferenceAspectRow>? by lazy {
        val rows = reference.optRows("aspectRows") ?: return@lazy null
        rows.map { value ->
            ReferenceAspectRow(value.string("label"), value.string("from"),
                value.optionalString("present")?.takeUnless { it == "null" }, value.string("past"), value.string("future"))
        }.also { parsed ->
            require(parsed.size == 4 && parsed.all { row ->
                verbs.any { verb -> verb.lemma == row.from && (row.present == null) == (verb.aspect == Aspect.PERFECTIVE) }
            })
        }
    }

    val maleAccRows: List<MaleAccRow>? by lazy {
        val rows = reference.optRows("maleAccRows") ?: return@lazy null
        rows.map { value ->
            MaleAccRow(value.string("id"), value.string("label"), value.string("title"),
                value.rows("examples").map { example ->
                    MaleAccExample(example.string("from"), example.string("to"), example.string("sentence"))
                }, value.string("rule"))
        }.also { parsed ->
            require(parsed.map { it.id } == listOf("person", "animal", "object"))
            require(parsed.all { row -> row.examples.isNotEmpty() && row.examples.all { it.to in it.sentence } })
        }
    }

    val presentations: Map<String, SkillPresentation> by lazy {
        root.rows("skills").associate { value ->
            val focus = value.obj("focus")
            val methods = value.obj("methods")
            value.string("id") to SkillPresentation(
                focus.string("before"), focus.string("after"),
                methods.obj("logic").methodPresentation(), methods.obj("situations").methodPresentation(),
            )
        }
    }

    /** Absent on a skill (true for every skill today) means [SkillStyleContent]'s all-derived defaults. */
    val styleContent: Map<String, SkillStyleContent> by lazy {
        root.rows("skills").associate { value -> value.string("id") to parseSkillStyleContent(value["styleContent"]) }
    }

    val vocabulary: List<VocabularyItem> by lazy {
        root.obj("vocabulary").rows("items").map { value ->
            VocabularyItem(value.string("id"), value.string("lemma"), value.string("translation"),
                value.string("form"), value.string("example"), value.string("level"),
                value["frequencyRank"]?.jsonPrimitive?.let { primitive ->
                    if (primitive.content == "null") null else primitive.int
                })
        }.requireUniqueIds(VocabularyItem::id)
    }

    val matrixIntroduction: String? by lazy { reference.optionalString("matrixIntroduction") }
    val webCaseCompositionHeader: String? by lazy { reference.optionalString("webCaseCompositionHeader") }
    val contextHelp: CourseContextHelp? by lazy {
        reference.optObj("contextHelp")?.let { CourseContextHelp(it.string("react"), it.string("compact")) }
    }
    val maleAccIntro: String? by lazy { reference.optionalString("maleAccIntro") }
    val aspectNoPresent: CourseAspectNoPresent? by lazy {
        reference.optObj("aspectNoPresent")?.let { CourseAspectNoPresent(it.string("compact"), it.string("ios")) }
    }
    val vocabularyInstructions: CourseVocabularyInstructions by lazy {
        root.obj("vocabulary").obj("instructions").let {
            CourseVocabularyInstructions(it.string("react"), it.string("web"), it.string("native"), it.string("ios"))
        }
    }
    val vocabularyUnavailableLabel: String by lazy { root.obj("vocabulary").string("unavailableLabel") }

    val frequency: List<FrequencyItem> by lazy {
        Json.parseToJsonElement(generatedFrequencyJson).jsonObject.rows("items").mapIndexed { index, value ->
            FrequencyItem(value.getValue("rank").jsonPrimitive.int, value.string("lemma"),
                value.getValue("count").jsonPrimitive.int).also { require(it.rank == index + 1) }
        }.also { require(it.size == 1000) }
    }

    /** A pronoun row without pl's full 7-case paradigm (en's `{subject, object}`) isn't this
     *  case-declining shape and is skipped — same reasoning as [nouns]. */
    val personalPronouns: Map<String, Map<GramCase, String>> by lazy {
        root.obj("personalPronouns").mapNotNull { (id, value) ->
            val forms = value.jsonObject
            if (!GramCase.entries.all { forms.containsKey(it.id) }) return@mapNotNull null
            id to forms.caseForms()
        }.toMap()
    }

    val possessives: List<Possessive> by lazy {
        root.rows("possessives").map { Possessive(PossessiveId.fromId(it.string("id")), it.string("label")) }
    }

    /**
     * EN-22 fix (EnRuAcceptance §7 item 1): which possessive ids are invariant vs. declined by
     * case/gender is a per-pack grammatical fact declared in the pack's own `forms.kind`, not the
     * pl-specific "only his/her/their are invariant" rule this used to hardcode (English's whole
     * paradigm is invariant) — dropped in favor of the structural shape check already below.
     * Likewise, requiring every [PossessiveId] to be present is pl's own completeness rule, not a
     * universal one; a pack simply declares whichever ids it has (en has no `yourPlural`).
     */
    val possessiveForms: Map<PossessiveId, CoursePossessiveForms> by lazy {
        root.rows("possessives").associate { row ->
            val id = PossessiveId.fromId(row.string("id"))
            val forms = row.obj("forms")
            val value = when (forms.string("kind")) {
                "invariant" -> {
                    require(forms.keys == setOf("kind", "value"))
                    CoursePossessiveForms.Invariant(forms.formText("value"))
                }
                "declined" -> {
                    require(forms.keys == setOf("kind", "sg", "pl"))
                    val singular = forms.obj("sg")
                    val plural = forms.obj("pl")
                    require(singular.keys == Gender.entries.map(Gender::id).toSet())
                    require(plural.keys == setOf("m-personal", "other"))
                    CoursePossessiveForms.Declined(
                        Gender.entries.associateWith { gender -> singular.obj(gender.id).strictCaseForms() },
                        plural.keys.associateWith { group -> plural.obj(group).strictCaseForms() },
                    )
                }
                else -> error("Unknown possessive kind for ${id.id}")
            }
            id to value
        }
    }

    /** Absent entirely for a pack with no compound future tense (en's `will` is a single
     *  invariant particle, not a conjugated auxiliary verb) — null, not force-parsed. */
    val futureAuxiliary: FutureAuxiliary? by lazy {
        val morphology = root["morphology"]?.jsonObject ?: return@lazy null
        require(morphology.keys == setOf("futureAuxiliary"))
        val auxiliary = morphology.obj("futureAuxiliary")
        require(auxiliary.keys == setOf("verbId", "forms"))
        val verbId = auxiliary.string("verbId")
        require(verbs.any { it.id == verbId && it.aspect == Aspect.IMPERFECTIVE })
        val forms = auxiliary.obj("forms")
        require(forms.keys == NumberGram.entries.map(NumberGram::id).toSet())
        FutureAuxiliary(verbId, NumberGram.entries.associateWith { number ->
            forms.obj(number.id).let { numbers ->
                require(numbers.keys == Person.entries.map { it.id.toString() }.toSet())
                Person.entries.associateWith { person -> numbers.formText(person.id.toString()) }
            }
        })
    }
}

/**
 * UC-03/EN-06: the single point where consumers reach a [CoursePack], keyed by [CoursePack.pairId].
 * [active] defaults to the first registered pack (pl-ru today, unchanged behavior) until
 * [select] switches it — EN-22 is the first production caller (host preferences sessions), driven
 * by `UserPreferencesV2.target`/`native` and gated by [usableCourseSelections].
 */
internal class PackRegistry(private val packs: List<CoursePack>) {
    init { require(packs.isNotEmpty() && packs.map { it.pairId }.distinct().size == packs.size) }

    var active: CoursePack = packs.first()
        private set

    /** Switches [active] to the registered pack whose [CoursePack.pairId] is [pairId]. */
    fun select(pairId: String) {
        active = requireNotNull(packs.firstOrNull { it.pairId == pairId }) { "Unknown pack pairId: $pairId" }
    }

    /** Whether [pairId] names a registered pack — a non-throwing check before [select] (EN-22). */
    fun contains(pairId: String): Boolean = packs.any { it.pairId == pairId }

    /** Every registered pack's own (targetLanguage, nativeLanguage), in registration order (pl-ru
     *  first) — the real option list for a target/native picker (EN-22), never a hardcoded pair. */
    val options: List<Pair<String, String>> by lazy { packs.map { it.targetLanguage to it.nativeLanguage } }
}

/** EN-04/EN-22: v1 packs (a real `courses/<id>/course.json`) first, then any pair this build has
 * v2 layers for (`lang/<target>/lexicon.json` + `pairs/<pairId>/pair.json`) but no v1 file of its
 * own — reconstructed by [CoursePackLoader], the same content [CoursePackLoaderTest] proves is
 * byte-identical to a hand-written v1 pack. pl-ru ships as v1 today, so it is never duplicated
 * here; en-ru (EN-11..EN-20) has no `courses/en-ru/course.json` and so is only ever reached this
 * way — without this, [packRegistry] would embed pl-ru alone and en-ru could never be selected. */
private val v2OnlyCoursePackSources: List<CoursePackSource> by lazy {
    generatedPairJsonByPairId.keys.filter { pairId -> embeddedCoursePackSources.none { it.id == pairId } }
        .sorted()
        .map { pairId ->
            CoursePackLoader.fromV2Layers(pairId, generatedLexiconJsonByLang.getValue(pairId.substringBefore("-")),
                generatedPairJsonByPairId.getValue(pairId))
        }
}

internal val packRegistry: PackRegistry by lazy {
    PackRegistry((embeddedCoursePackSources + v2OnlyCoursePackSources).map(::CoursePack))
}

/** EN-22: `(target, native)` for every pack this build actually embeds, pl-ru first — every pack
 * that exists, not necessarily one a picker may safely offer yet (see [usableCourseSelections]). */
val availableCourseSelections: List<Pair<String, String>> by lazy { packRegistry.options }

/**
 * EN-22 (EnRuAcceptance §7 item 1): the (possibly smaller) subset of [availableCourseSelections]
 * whose full [CoursePack] content actually parses today — a runtime probe of a throwaway instance
 * (never touching [packRegistry]'s own singleton, so a broken pack is never read through the real,
 * process-wide cached path [selectActiveCoursePack] warns about), covering every field a host's
 * Training/Vocabulary/Matrix/reference screens read.
 *
 * en-ru now passes this probe: [CoursePack]'s grammar-shaped properties (`Noun.gender`, case-keyed
 * `possessiveForms`/`futureAuxiliary`/the `reference` block's pl-specific teaching rows) are each
 * derived from what the pack's own JSON actually declares — a genuinely caseless/genderless
 * language contributes an honest empty/null result there instead of a fabricated pl-shaped one, so
 * it no longer throws. pl's own JSON declares every one of these fields exactly as before, so its
 * parse (and every value it produces) is unchanged.
 */
val usableCourseSelections: List<Pair<String, String>> by lazy {
    (embeddedCoursePackSources + v2OnlyCoursePackSources).mapNotNull { source ->
        val pack = CoursePack(source)
        pack.takeIf { it.parsesCompletely() }?.let { it.targetLanguage to it.nativeLanguage }
    }
}

private fun CoursePack.parsesCompletely(): Boolean = runCatching {
    nouns; adjectives; verbs; skills; sentenceSeeds; stemAlternations; caseSentencePrefixes
    exerciseCopy; exercisePatterns; chainPresentation; caseReferenceRows; referenceGenderNames
    referenceChainRows; referenceSystemCards; referencePipeline; referenceCaseTeaching
    referencePronounTeaching; referenceVerbTeaching; comparisonNounIds; referenceRussianSupport
    referenceTenseRows; referenceAspectRows; maleAccRows; presentations; styleContent; vocabulary
    matrixIntroduction; webCaseCompositionHeader; contextHelp; maleAccIntro; aspectNoPresent
    vocabularyInstructions; vocabularyUnavailableLabel; frequency; personalPronouns; possessives
    possessiveForms; futureAuxiliary
}.isSuccess

/**
 * EN-22 host entrypoint: switches the active pack to [pairId] (`"target-native"`) when this build
 * embeds it *and* [usableCourseSelections] finds it safe to read; any other [pairId] — unknown,
 * or embedded but not yet parseable — is a no-op, never a crash. Pack-derived globals read
 * [packRegistry.active] live, but in-flight session state does not re-derive, so Android applies a
 * persisted switch on its next cold start (see `AndroidSessionViewModel`'s early `init` block).
 */
fun selectActiveCoursePack(pairId: String) {
    if (packRegistry.active.pairId == pairId) return
    if (usableCourseSelections.none { (target, native) -> "$target-$native" == pairId }) return
    runCatching { packRegistry.select(pairId) }
}

/** EN-22: one registered pack's id + languages, for a host's target/native pickers to list. */
data class CoursePackOption(val pairId: String, val target: String, val native: String)

/** Every pack [selectCoursePack] can switch to ([usableCourseSelections]), pl-ru first. */
val availableCoursePacks: List<CoursePackOption> get() =
    usableCourseSelections.map { (target, native) -> CoursePackOption("$target-$native", target, native) }

/** [CoursePack.pairId] of the pack currently serving content. */
val activeCoursePackId: String get() = packRegistry.active.pairId

/** Switches the active pack; throws for a [pairId] not in [availableCoursePacks]. */
fun selectCoursePack(pairId: String) {
    require(availableCoursePacks.any { it.pairId == pairId }) { "Unusable pack pairId: $pairId" }
    packRegistry.select(pairId)
}

// EN-22: every val below was `by lazy` (frozen at first access) — read fresh now so a later
// selectCoursePack (host picker, this task) actually changes what these return; see Skills.kt's
// `skills` KDoc for the same fix's original writeup. `caseSentencePrefix`/`exerciseCopy` above were
// already plain `fun`s reading `packRegistry.active` fresh, so they needed no change.
val courseSentenceSeeds: List<SentenceSeed> get() = packRegistry.active.sentenceSeeds
val courseStemAlternations: List<StemAlternation> get() = packRegistry.active.stemAlternations

fun caseSentencePrefix(gramCase: GramCase, number: NumberGram): String {
    require(gramCase != GramCase.VOC)
    val key = if (gramCase == GramCase.NOM) {
        if (number == NumberGram.SG) "nomSg" else "nomPl"
    } else gramCase.id
    return packRegistry.active.caseSentencePrefixes.getValue(key)
}

fun exerciseCopy(key: String): String = packRegistry.active.exerciseCopy.getValue(key)

// EN-22 fix (EnRuAcceptance §7 item 1, corrected): each [CoursePack] property below is nullable (a
// pack whose `reference` block doesn't cover this pl-specific teaching topic — en-ru today). This
// same fix's [selectActiveCoursePack]/[selectCoursePack] now really flip the process-wide
// [packRegistry.active] to en-ru (Settings/[PreferencesSession] included), so a host's existing
// Matrix/reference screen can run with en-ru genuinely active — `!!` would crash there. Each
// wrapper below falls back to an empty/blank instance of its own unchanged non-null return type:
// "this pack has nothing to teach here", not a fabricated pl-shaped fact, and not a crash. Wiring a
// *rich* en-ru-specific reference/matrix screen is report item 2's separate, unstarted work; this
// only guarantees today's pl-shaped screens degrade to blank instead of throwing.
private val emptyReferencePipeline = ReferencePipeline(title = "", steps = emptyList(), compactExample = "")
private val emptyCaseTeaching = CaseTeaching(reactNote = "", compactNote = "", comparisonReadingHint = "")
private val emptyVerbTeaching = VerbTeaching(
    subjects = emptyList(),
    genderControlLabel = VerbLabel("", ""),
    genderOptions = emptyList(),
    // every Tense key must resolve — hosts read `tenseLabels.getValue(tense)` unconditionally.
    tenseLabels = Tense.entries.associateWith { VerbLabel("", "") },
    reactFutureExplanation = "",
    compactFutureExplanation = "",
)
private val emptyPossessiveDemo = PossessiveDemo(
    nounId = "", adjectiveId = "", number = NumberGram.SG, cases = emptyList(),
    invariableOwnerIds = emptySet(), invariableRule = "", variableRule = "",
)
private val emptyPronounTeaching = PronounTeaching(
    personalTitle = "", reactIntro = "", compactIntro = "", reactFooter = "", webFooter = "", nativeFooter = "",
    pronounIds = emptyList(), contexts = emptyList(), possessiveTitle = "", demo = emptyPossessiveDemo,
)
private val emptyRussianSupport = RussianSupport(fullTitle = "", compactTitle = "", columns = emptyList(), rows = emptyList())
private val emptyContextHelp = CourseContextHelp(react = "", compact = "")
private val emptyAspectNoPresent = CourseAspectNoPresent(compact = "", ios = "")

val referenceChainRows: List<ReferenceChainRow> get() = packRegistry.active.referenceChainRows ?: emptyList()
val courseChainPresentation: ChainPresentation get() = packRegistry.active.chainPresentation
val referenceSystemCards: List<ReferenceSystemCard> get() = packRegistry.active.referenceSystemCards ?: emptyList()
val referencePipeline: ReferencePipeline get() = packRegistry.active.referencePipeline ?: emptyReferencePipeline
val referenceCaseTeaching: CaseTeaching get() = packRegistry.active.referenceCaseTeaching ?: emptyCaseTeaching
val referenceVerbTeaching: VerbTeaching get() = packRegistry.active.referenceVerbTeaching ?: emptyVerbTeaching
val referencePronounTeaching: PronounTeaching get() = packRegistry.active.referencePronounTeaching ?: emptyPronounTeaching
val comparisonNounIds: List<String> get() = packRegistry.active.comparisonNounIds ?: emptyList()
val referenceRussianSupport: RussianSupport get() = packRegistry.active.referenceRussianSupport ?: emptyRussianSupport
val referenceTenseRows: List<ReferenceTenseRow> get() = packRegistry.active.referenceTenseRows ?: emptyList()
val referenceAspectRows: List<ReferenceAspectRow> get() = packRegistry.active.referenceAspectRows ?: emptyList()
val maleAccRows: List<MaleAccRow> get() = packRegistry.active.maleAccRows ?: emptyList()
val courseMatrixIntroduction: String get() = packRegistry.active.matrixIntroduction ?: ""
val courseWebCaseCompositionHeader: String get() = packRegistry.active.webCaseCompositionHeader ?: ""
val courseContextHelp: CourseContextHelp get() = packRegistry.active.contextHelp ?: emptyContextHelp
val courseMaleAccIntro: String get() = packRegistry.active.maleAccIntro ?: ""
val courseAspectNoPresent: CourseAspectNoPresent get() = packRegistry.active.aspectNoPresent ?: emptyAspectNoPresent
val courseVocabularyInstructions: CourseVocabularyInstructions get() = packRegistry.active.vocabularyInstructions
val courseVocabularyUnavailableLabel: String get() = packRegistry.active.vocabularyUnavailableLabel

private val coursePlaceholder = Regex("\\{([A-Za-z][A-Za-z0-9]*)\\}")

fun renderCoursePattern(key: String, values: Map<String, String>): String =
    coursePlaceholder.replace(packRegistry.active.exercisePatterns.getValue(key)) { match ->
        val name = match.groupValues[1]
        values[name]?.takeIf { it.isNotEmpty() } ?: error("Missing $name for $key")
    }

private fun JsonObject.string(key: String): String =
    (get(key) as? JsonPrimitive)?.content?.takeIf(String::isNotEmpty) ?: error("Missing course field $key")

private fun JsonObject.optionalString(key: String): String? = (get(key) as? JsonPrimitive)?.content
private fun JsonObject.obj(key: String): JsonObject = getValue(key).jsonObject
private fun JsonObject.rows(key: String): List<JsonObject> = getValue(key).jsonArray.map { it.jsonObject }

/** [key] absent (a pack's `reference` block genuinely not covering this teaching topic, EN-22 fix)
 *  is null, not a parse error — see [CoursePack.reference]'s own KDoc for why. */
private fun JsonObject.optObj(key: String): JsonObject? = (get(key) as? JsonObject)
private fun JsonObject.optRows(key: String): List<JsonObject>? = (get(key) as? JsonArray)?.map { it.jsonObject }
private fun JsonObject.caseForms(): Map<GramCase, String> = GramCase.entries.associateWith { string(it.id) }
private fun JsonObject.strictCaseForms(): Map<GramCase, String> {
    require(keys == GramCase.entries.map(GramCase::id).toSet())
    return GramCase.entries.associateWith { formText(it.id) }
}
private fun JsonObject.formText(key: String): String = string(key).also { require(it.isNotBlank()) }
private fun JsonObject.methodPresentation(): MethodPresentation {
    fun required(field: String): String = string(field).also { value ->
        require(value.isNotBlank()) { "Invalid method stage: $field" }
    }
    return MethodPresentation(
        required("introduction"), required("promptLead"), required("introduce"),
        required("retrieve"), required("feedback"), required("review"),
    )
}
private fun JsonObject.russianSupportTable(): RussianSupportTable =
    RussianSupportTable(string("construction"), string("check"))

private fun JsonObject.russianSupportComparison(path: String): ContrastPair {
    val from = string("from")
    val to = string("to")
    fun parts(key: String, form: String): List<EndingPart> {
        val pieces = rows(key).mapIndexed { index, part ->
            EndingPart(
                part.string("text"),
                part.getValue("isEnding").jsonPrimitive.boolean,
                part.getValue("isChanged").jsonPrimitive.boolean,
            ).also { piece ->
                require(piece.text.isNotEmpty()) { "$path/$key/$index/text: empty segment" }
            }
        }
        require(pieces.isNotEmpty() && pieces.joinToString("") { it.text } == form) {
            "$path/$key: segments must reassemble exactly"
        }
        var cursor = 0
        pieces.forEachIndexed { index, piece ->
            val next = cursor + piece.text.length
            val before = form.getOrNull(cursor - 1)
            val after = form.getOrNull(next)
            if (piece.isEnding) {
                require(piece.isChanged && piece.text.length <= 3 && piece.text.all(Char::isLetter) &&
                    before?.isLetterOrDigit() == true && after?.isLetterOrDigit() != true) {
                    "$path/$key/$index/isEnding: invalid ending span"
                }
            } else if (piece.isChanged) {
                require(before?.isLetterOrDigit() != true && after?.isLetterOrDigit() != true) {
                    "$path/$key/$index/isChanged: replacement must mark a whole word"
                }
            }
            cursor = next
        }
        return pieces
    }
    val before = parts("beforeParts", from)
    val after = parts("afterParts", to)
    require(from == to || (before.any { it.isChanged } && after.any { it.isChanged })) {
        "$path: both changed forms need marked spans"
    }
    require(before.filterNot { it.isChanged }.joinToString("") { it.text } ==
        after.filterNot { it.isChanged }.joinToString("") { it.text } &&
        (from != to || (before.none { it.isChanged } && after.none { it.isChanged }))) {
        "$path: unchanged fragments must agree on both sides"
    }
    return ContrastPair(from, to, before, after)
}
private fun JsonObject.verbLabel(): VerbLabel = VerbLabel(string("full"), string("compact"))

/** [json] is the optional `skill.styleContent` value as it comes out of `root["styleContent"]` — absent/non-object means the all-derived default. */
internal fun parseSkillStyleContent(json: JsonElement?): SkillStyleContent =
    (json as? JsonObject)?.toSkillStyleContent() ?: SkillStyleContent()

/** [json] is the optional `stemAlternations` value as it comes out of `root["stemAlternations"]` — absent/non-array means no pack-declared alternation (UC S2). */
internal fun parseStemAlternations(json: JsonElement?): List<StemAlternation> =
    (json as? JsonArray)?.map { it.jsonObject.toStemAlternation() } ?: emptyList()

private fun JsonObject.toStemAlternation(): StemAlternation {
    val a = string("a")
    val b = string("b")
    require(a.length == 1 && b.length == 1) { "stemAlternations: a/b must be single characters" }
    return StemAlternation(a[0], b[0])
}

private fun JsonObject.toSkillStyleContent(): SkillStyleContent = SkillStyleContent(
    rule = optionalString("rule"),
    table = (get("table") as? JsonObject)?.rows("rows")?.map { it.toTableRow() },
    scene = optionalString("scene"),
    nativeParallel = (get("nativeParallel") as? JsonArray)?.map { it.jsonObject.toNativeParallelPair() } ?: emptyList(),
    examples = (get("examples") as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList(),
    why = optionalString("why"),
)

private fun JsonObject.toTableRow(): TableRow = TableRow(string("label"), endingParts("before"), endingParts("after"))

/** [NativeParallelPair.targetParts] is filled in later, at compose time, from the skill's own
 *  explicit `focus` pair (EmphasisUXAudit E7/S4) — this raw course-data parse knows no exercise. */
private fun JsonObject.toNativeParallelPair(): NativeParallelPair =
    NativeParallelPair(string("native"), string("target"), string("note"), getValue("matches").jsonPrimitive.boolean, targetParts = emptyList())

private fun JsonObject.endingParts(key: String): List<EndingPart> = rows(key).map { part ->
    EndingPart(part.string("text"), part.getValue("isEnding").jsonPrimitive.boolean, part.getValue("isChanged").jsonPrimitive.boolean)
}
/** Uniqueness only — emptiness is a legitimate result for a pl-shaped table a pack's own grammar
 *  has nothing to contribute to (EnRuAcceptance §7 item 1: en's genderless [Noun]/[Adjective]/
 *  [Verb] rows are filtered out upstream, not force-parsed), so it is never asserted here. */
private fun <T> List<T>.requireUniqueIds(id: (T) -> String): List<T> = also { items ->
    require(items.map(id).distinct().size == items.size) { "Duplicate course IDs" }
}
