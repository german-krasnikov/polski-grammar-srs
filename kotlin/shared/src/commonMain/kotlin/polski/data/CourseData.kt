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
data class ReferenceSystemCard(val id: String, val title: String, val explanation: String, val example: String)
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
 * The Polish/Russian material is authored in courses/pl-ru/course.json, not in Kotlin source.
 * UC-02: reached through [CoursePackSource] (`:pack-format`) rather than a fixed generated
 * property — `generateCoursePackSource` (`kotlin/shared/build.gradle.kts`) discovers pl-ru by
 * scanning the `courses` directory for a `course.json`, but it is still read here as schema v1.
 */
internal object PolishCourseData {
    private val root: JsonObject by lazy {
        val source: CoursePackSource = embeddedCoursePackSources.first { it.id == "pl-ru" }
        Json.parseToJsonElement(source.load()).jsonObject.also { pack ->
            require(pack.getValue("schemaVersion").jsonPrimitive.int == 1)
            require(pack.string("id") == "pl-ru")
            require(pack.string("targetLanguage") == "pl" && pack.string("nativeLanguage") == "ru")
        }
    }

    val nouns: List<Noun> by lazy { root.rows("nouns").map { value ->
        Noun(value.string("id"), value.string("lemma"), value.string("meaning"), Gender.fromId(value.string("gender")),
            NumberGram.entries.associateWith { number -> value.obj("forms").obj(number.id).caseForms() })
    }.requireUniqueIds(Noun::id) }

    val adjectives: List<Adjective> by lazy { root.rows("adjectives").map { value ->
        Adjective(value.string("id"), value.string("lemma"), value.string("meaning"),
            NumberGram.entries.associateWith { number ->
                Gender.entries.associateWith { gender -> value.obj("forms").obj(number.id).obj(gender.id).caseForms() }
            })
    }.requireUniqueIds(Adjective::id) }

    val verbs: List<Verb> by lazy { root.rows("verbs").map { value ->
        val past = value.obj("pastStem")
        Verb(value.string("id"), value.string("lemma"), value.string("meaning"),
            Aspect.entries.first { it.id == value.string("aspect") },
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

    val sentenceSeeds: List<SentenceSeed> by lazy { root.rows("sentenceSeeds").map { value ->
        SentenceSeed(value.string("nounId"), value.string("adjectiveId"))
    }.also { seeds ->
        require(seeds.isNotEmpty() && seeds.distinct().size == seeds.size)
        require(seeds.all { seed -> nouns.any { it.id == seed.nounId } && adjectives.any { it.id == seed.adjectiveId } })
    } }

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

    val caseReferenceRows: List<CaseReferenceRow> by lazy {
        root.obj("reference").rows("caseRows").map { value ->
            CaseReferenceRow(GramCase.fromId(value.string("id")), value.string("pl"), value.string("ru"),
                value.string("question"), value.string("trigger"), value.optionalString("skill"))
        }.also { rows -> require(rows.map { it.id } == listOf(GramCase.NOM, GramCase.GEN, GramCase.DAT, GramCase.ACC, GramCase.INST, GramCase.LOC, GramCase.VOC)) }
    }

    val referenceGenderNames: Map<String, String> by lazy {
        root.obj("reference").obj("genderNames").mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    val referenceChainRows: List<ReferenceChainRow> by lazy {
        root.obj("reference").rows("chainRows").map { value ->
            ReferenceChainRow(value.string("label"), value.string("from"), value.string("to"), value.string("change"))
        }.also { rows ->
            require(rows.size == 5 && (1 until rows.size).all { rows[it].from == rows[it - 1].to })
        }
    }

    val referenceSystemCards: List<ReferenceSystemCard> by lazy {
        root.obj("reference").rows("systemCards").map { value ->
            ReferenceSystemCard(value.string("id"), value.string("title"), value.string("explanation"), value.string("example"))
        }.also { cards ->
            require(cards.map { it.id } == listOf("noun", "agreement", "verb", "modifiers"))
        }
    }

    val referencePipeline: ReferencePipeline by lazy {
        val pipeline = root.obj("reference").obj("pipeline")
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

    val referenceCaseTeaching: CaseTeaching by lazy {
        val teaching = root.obj("reference").obj("caseTeaching")
        val note = teaching.obj("caseNote")
        CaseTeaching(note.string("react"), note.string("compact"), teaching.string("comparisonReadingHint"))
    }

    val referencePronounTeaching: PronounTeaching by lazy {
        val teaching = root.obj("reference").obj("pronounTeaching")
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

    val referenceVerbTeaching: VerbTeaching by lazy {
        val teaching = root.obj("reference").obj("verbTeaching")
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

    val comparisonNounIds: List<String> by lazy {
        root.obj("reference").getValue("comparisonNounIds").jsonArray.map { it.jsonPrimitive.content }.also { ids ->
            require(ids.size == 7 && ids.distinct().size == ids.size)
            require(ids.all { id -> nouns.any { it.id == id } })
        }
    }

    val referenceRussianSupport: RussianSupport by lazy {
        val support = root.obj("reference").obj("russianSupport")
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

    val referenceTenseRows: List<ReferenceTenseRow> by lazy {
        root.obj("reference").rows("tenseRows").map { value ->
            ReferenceTenseRow(value.string("label"), value.string("from"), value.string("to"))
        }.also { rows ->
            require(rows.size == 5 && rows.indices.all { index ->
                rows[index].from == if (index == 2) rows[1].to else rows[0].to
            })
        }
    }

    val referenceAspectRows: List<ReferenceAspectRow> by lazy {
        root.obj("reference").rows("aspectRows").map { value ->
            ReferenceAspectRow(value.string("label"), value.string("from"),
                value.optionalString("present")?.takeUnless { it == "null" }, value.string("past"), value.string("future"))
        }.also { rows ->
            require(rows.size == 4 && rows.all { row ->
                verbs.any { verb -> verb.lemma == row.from && (row.present == null) == (verb.aspect == Aspect.PERFECTIVE) }
            })
        }
    }

    val maleAccRows: List<MaleAccRow> by lazy {
        root.obj("reference").rows("maleAccRows").map { value ->
            MaleAccRow(value.string("id"), value.string("label"), value.string("title"),
                value.rows("examples").map { example ->
                    MaleAccExample(example.string("from"), example.string("to"), example.string("sentence"))
                }, value.string("rule"))
        }.also { rows ->
            require(rows.map { it.id } == listOf("person", "animal", "object"))
            require(rows.all { row -> row.examples.isNotEmpty() && row.examples.all { it.to in it.sentence } })
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

    val matrixIntroduction: String by lazy { root.obj("reference").string("matrixIntroduction") }
    val webCaseCompositionHeader: String by lazy { root.obj("reference").string("webCaseCompositionHeader") }
    val contextHelp: CourseContextHelp by lazy {
        root.obj("reference").obj("contextHelp").let { CourseContextHelp(it.string("react"), it.string("compact")) }
    }
    val maleAccIntro: String by lazy { root.obj("reference").string("maleAccIntro") }
    val aspectNoPresent: CourseAspectNoPresent by lazy {
        root.obj("reference").obj("aspectNoPresent").let { CourseAspectNoPresent(it.string("compact"), it.string("ios")) }
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

    val personalPronouns: Map<String, Map<GramCase, String>> by lazy {
        root.obj("personalPronouns").mapValues { (_, value) -> value.jsonObject.caseForms() }
    }

    val possessives: List<Possessive> by lazy {
        root.rows("possessives").map { Possessive(PossessiveId.fromId(it.string("id")), it.string("label")) }
    }

    val possessiveForms: Map<PossessiveId, CoursePossessiveForms> by lazy {
        root.rows("possessives").associate { row ->
            val id = PossessiveId.fromId(row.string("id"))
            val forms = row.obj("forms")
            val value = when (forms.string("kind")) {
                "invariant" -> {
                    require(id in setOf(PossessiveId.HIS, PossessiveId.HER, PossessiveId.THEIR))
                    require(forms.keys == setOf("kind", "value"))
                    CoursePossessiveForms.Invariant(forms.formText("value"))
                }
                "declined" -> {
                    require(id !in setOf(PossessiveId.HIS, PossessiveId.HER, PossessiveId.THEIR))
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
        }.also { forms -> require(forms.keys == PossessiveId.entries.toSet()) }
    }

    val futureAuxiliary: FutureAuxiliary by lazy {
        val morphology = root.obj("morphology")
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

val courseSentenceSeeds: List<SentenceSeed> by lazy { PolishCourseData.sentenceSeeds }

fun caseSentencePrefix(gramCase: GramCase, number: NumberGram): String {
    require(gramCase != GramCase.VOC)
    val key = if (gramCase == GramCase.NOM) {
        if (number == NumberGram.SG) "nomSg" else "nomPl"
    } else gramCase.id
    return PolishCourseData.caseSentencePrefixes.getValue(key)
}

fun exerciseCopy(key: String): String = PolishCourseData.exerciseCopy.getValue(key)
val referenceChainRows: List<ReferenceChainRow> by lazy { PolishCourseData.referenceChainRows }
val courseChainPresentation: ChainPresentation by lazy { PolishCourseData.chainPresentation }
val referenceSystemCards: List<ReferenceSystemCard> by lazy { PolishCourseData.referenceSystemCards }
val referencePipeline: ReferencePipeline by lazy { PolishCourseData.referencePipeline }
val referenceCaseTeaching: CaseTeaching by lazy { PolishCourseData.referenceCaseTeaching }
val referenceVerbTeaching: VerbTeaching by lazy { PolishCourseData.referenceVerbTeaching }
val referencePronounTeaching: PronounTeaching by lazy { PolishCourseData.referencePronounTeaching }
val comparisonNounIds: List<String> by lazy { PolishCourseData.comparisonNounIds }
val referenceRussianSupport: RussianSupport by lazy { PolishCourseData.referenceRussianSupport }
val referenceTenseRows: List<ReferenceTenseRow> by lazy { PolishCourseData.referenceTenseRows }
val referenceAspectRows: List<ReferenceAspectRow> by lazy { PolishCourseData.referenceAspectRows }
val maleAccRows: List<MaleAccRow> by lazy { PolishCourseData.maleAccRows }
val courseMatrixIntroduction: String by lazy { PolishCourseData.matrixIntroduction }
val courseWebCaseCompositionHeader: String by lazy { PolishCourseData.webCaseCompositionHeader }
val courseContextHelp: CourseContextHelp by lazy { PolishCourseData.contextHelp }
val courseMaleAccIntro: String by lazy { PolishCourseData.maleAccIntro }
val courseAspectNoPresent: CourseAspectNoPresent by lazy { PolishCourseData.aspectNoPresent }
val courseVocabularyInstructions: CourseVocabularyInstructions by lazy { PolishCourseData.vocabularyInstructions }
val courseVocabularyUnavailableLabel: String by lazy { PolishCourseData.vocabularyUnavailableLabel }

private val coursePlaceholder = Regex("\\{([A-Za-z][A-Za-z0-9]*)\\}")

fun renderCoursePattern(key: String, values: Map<String, String>): String =
    coursePlaceholder.replace(PolishCourseData.exercisePatterns.getValue(key)) { match ->
        val name = match.groupValues[1]
        values[name]?.takeIf { it.isNotEmpty() } ?: error("Missing $name for $key")
    }

private fun JsonObject.string(key: String): String =
    (get(key) as? JsonPrimitive)?.content?.takeIf(String::isNotEmpty) ?: error("Missing course field $key")

private fun JsonObject.optionalString(key: String): String? = (get(key) as? JsonPrimitive)?.content
private fun JsonObject.obj(key: String): JsonObject = getValue(key).jsonObject
private fun JsonObject.rows(key: String): List<JsonObject> = getValue(key).jsonArray.map { it.jsonObject }
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

private fun JsonObject.toSkillStyleContent(): SkillStyleContent = SkillStyleContent(
    rule = optionalString("rule"),
    table = (get("table") as? JsonObject)?.rows("rows")?.map { it.toTableRow() },
    scene = optionalString("scene"),
    nativeParallel = (get("nativeParallel") as? JsonArray)?.map { it.jsonObject.toNativeParallelPair() } ?: emptyList(),
    examples = (get("examples") as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList(),
    why = optionalString("why"),
)

private fun JsonObject.toTableRow(): TableRow = TableRow(string("label"), endingParts("before"), endingParts("after"))

private fun JsonObject.toNativeParallelPair(): NativeParallelPair =
    NativeParallelPair(string("native"), string("target"), string("note"), getValue("matches").jsonPrimitive.boolean)

private fun JsonObject.endingParts(key: String): List<EndingPart> = rows(key).map { part ->
    EndingPart(part.string("text"), part.getValue("isEnding").jsonPrimitive.boolean, part.getValue("isChanged").jsonPrimitive.boolean)
}
private fun <T> List<T>.requireUniqueIds(id: (T) -> String): List<T> = also { items ->
    require(items.isNotEmpty() && items.map(id).distinct().size == items.size) { "Duplicate or missing course IDs" }
}
