package polski.data

import kotlinx.serialization.json.*
import polski.model.*
import polski.presentation.StemAlternation
import polski.pack.CoursePackSource

/**
 * JSON-based course pack reader. Used by tests for synthetic pack fixtures.
 * Production packs use PlRuTypedPack / EnRuTypedPack (typed objects, zero JSON).
 */

internal class CoursePack(private val source: CoursePackSource) : CoursePackFields {
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

    override val id: String by lazy { root.string("id") }

    /** UC-04: vocabulary's pair scope (`${target}-${native}`) — v1 packs have `id == pairId`
     * (one pack per pair), but the two stay conceptually distinct for a future schema where a
     * pack id could version content without changing the pair. */
    override val pairId: String by lazy { "${root.string("targetLanguage")}-${root.string("nativeLanguage")}" }

    /** EN-08 (gap G): the two halves of [pairId] as their own accessors, for preferences'
     * `CourseSelection(target, native, style)` and future per-language pickers (EN-22). */
    override val targetLanguage: String by lazy { root.string("targetLanguage") }
    override val nativeLanguage: String by lazy { root.string("nativeLanguage") }

    /**
     * EN-22 fix (EnRuAcceptance §7 item 1): a case-declining, gendered noun table — this is pl's
     * own grammar shape, not a universal fact every pack has (English nouns have neither). A row
     * without a "gender" field (en's `lexicon.json`) simply isn't this shape and is skipped rather
     * than force-parsed into false gender/case data — [nouns] is honestly empty for such a pack,
     * not fabricated. pl's rows all declare "gender", so pl's list is unchanged.
     */
    override val nouns: List<Noun> by lazy { root.rows("nouns").mapNotNull { value ->
        val gender = value.optionalString("gender")?.let(Gender::fromId) ?: return@mapNotNull null
        Noun(value.string("id"), value.string("lemma"), value.string("meaning"), gender,
            NumberGram.entries.associateWith { number -> value.obj("forms").obj(number.id).caseForms() })
    }.requireUniqueIds(Noun::id) }

    /**
     * EnRuAcceptance-2026-09-28.md §7 item 3/4: every noun row's own `(lemma, meaning)`, regardless
     * of whether it has pl's case/gender shape — [nouns] above skips a genderless pack's rows
     * entirely, but a host's chain-seed picker (`TrainingWebApp.kt`) only ever needs a label for
     * whichever noun the active pack's own [sentenceSeeds] actually name, never pl's declension.
     */
    override val nounLabels: Map<String, Pair<String, String>> by lazy {
        root.rows("nouns").associate { value -> value.string("id") to (value.string("lemma") to value.string("meaning")) }
    }

    /** Same reasoning as [nouns]: a row whose "forms" isn't number×gender×case-shaped (en's flat
     *  `{"invariant": "..."}`) is skipped, not force-parsed. */
    override val adjectives: List<Adjective> by lazy { root.rows("adjectives").mapNotNull { value ->
        val forms = value.obj("forms")
        if (!NumberGram.entries.all { forms.containsKey(it.id) }) return@mapNotNull null
        Adjective(value.string("id"), value.string("lemma"), value.string("meaning"),
            NumberGram.entries.associateWith { number ->
                Gender.entries.associateWith { gender -> forms.obj(number.id).obj(gender.id).caseForms() }
            })
    }.requireUniqueIds(Adjective::id) }

    /** Same reasoning as [nouns]: a row without an "aspect" field (en's verbs have no pl-shaped
     *  `pastStem`/`futureType`) is skipped, not force-parsed. */
    override val verbs: List<Verb> by lazy { root.rows("verbs").mapNotNull { value ->
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

    override val skills: List<Skill> by lazy { root.rows("skills").map { value ->
        Skill(value.string("id"), value.string("title"), value.string("group"), value.string("level"),
            value.string("formula"), value.string("theory"), value.string("hint"),
            value.getValue("prerequisites").jsonArray.map { it.jsonPrimitive.content })
    }.requireUniqueIds(Skill::id) }

    /** Validated against the pack's own raw noun/adjective ids (every pack declares these,
     *  whichever shape they end up in) rather than [nouns]/[adjectives] — those two are narrowed
     *  to pl's gendered/case-declining shape and would wrongly reject a caseless pack's seeds. */
    override val sentenceSeeds: List<SentenceSeed> by lazy {
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
    override val stemAlternations: List<StemAlternation> by lazy { parseStemAlternations(root["stemAlternations"]) }

    override val caseSentencePrefixes: Map<String, String> by lazy {
        root.obj("caseSentencePrefixes").mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    override val exerciseCopy: Map<String, String> by lazy {
        root.obj("exerciseCopy").mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    override val exercisePatterns: Map<String, String> by lazy {
        root.obj("exercisePatterns").mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    override val chainPresentation: ChainPresentation by lazy {
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

    override val caseReferenceRows: List<CaseReferenceRow>? by lazy {
        val rows = reference.optRows("caseRows") ?: return@lazy null
        rows.map { value ->
            CaseReferenceRow(GramCase.fromId(value.string("id")), value.string("pl"), value.string("ru"),
                value.string("question"), value.string("trigger"), value.optionalString("skill"))
        }.also { parsed -> require(parsed.map { it.id } == listOf(GramCase.NOM, GramCase.GEN, GramCase.DAT, GramCase.ACC, GramCase.INST, GramCase.LOC, GramCase.VOC)) }
    }

    override val referenceGenderNames: Map<String, String>? by lazy {
        val names = reference.optObj("genderNames") ?: return@lazy null
        names.mapValues { (_, value) -> value.jsonPrimitive.content }
    }

    override val referenceChainRows: List<ReferenceChainRow>? by lazy {
        val rows = reference.optRows("chainRows") ?: return@lazy null
        rows.map { value ->
            ReferenceChainRow(value.string("label"), value.string("from"), value.string("to"), value.string("change"))
        }.also { parsed ->
            require(parsed.size == 5 && (1 until parsed.size).all { parsed[it].from == parsed[it - 1].to })
        }
    }

    override val referenceSystemCards: List<ReferenceSystemCard>? by lazy {
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

    override val referencePipeline: ReferencePipeline? by lazy {
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

    override val referenceCaseTeaching: CaseTeaching? by lazy {
        val teaching = reference.optObj("caseTeaching") ?: return@lazy null
        val note = teaching.obj("caseNote")
        CaseTeaching(note.string("react"), note.string("compact"), teaching.string("comparisonReadingHint"))
    }

    override val referencePronounTeaching: PronounTeaching? by lazy {
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

    override val referenceVerbTeaching: VerbTeaching? by lazy {
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

    override val comparisonNounIds: List<String>? by lazy {
        val ids = (reference["comparisonNounIds"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: return@lazy null
        ids.also {
            require(it.size == 7 && it.distinct().size == it.size)
            require(it.all { id -> nouns.any { noun -> noun.id == id } })
        }
    }

    override val referenceRussianSupport: RussianSupport? by lazy {
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

    override val referenceTenseRows: List<ReferenceTenseRow>? by lazy {
        val rows = reference.optRows("tenseRows") ?: return@lazy null
        rows.map { value ->
            ReferenceTenseRow(value.string("label"), value.string("from"), value.string("to"))
        }.also { parsed ->
            require(parsed.size == 5 && parsed.indices.all { index ->
                parsed[index].from == if (index == 2) parsed[1].to else parsed[0].to
            })
        }
    }

    override val referenceAspectRows: List<ReferenceAspectRow>? by lazy {
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

    override val maleAccRows: List<MaleAccRow>? by lazy {
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

    override val presentations: Map<String, SkillPresentation> by lazy {
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
    override val styleContent: Map<String, SkillStyleContent> by lazy {
        root.rows("skills").associate { value -> value.string("id") to parseSkillStyleContent(value["styleContent"]) }
    }

    override val vocabulary: List<VocabularyItem> by lazy {
        root.obj("vocabulary").rows("items").map { value ->
            VocabularyItem(value.string("id"), value.string("lemma"), value.string("translation"),
                value.string("form"), value.string("example"), value.string("level"),
                value["frequencyRank"]?.jsonPrimitive?.let { primitive ->
                    if (primitive.content == "null") null else primitive.int
                })
        }.requireUniqueIds(VocabularyItem::id)
    }

    override val matrixIntroduction: String? by lazy { reference.optionalString("matrixIntroduction") }
    override val webCaseCompositionHeader: String? by lazy { reference.optionalString("webCaseCompositionHeader") }
    override val contextHelp: CourseContextHelp? by lazy {
        reference.optObj("contextHelp")?.let { CourseContextHelp(it.string("react"), it.string("compact")) }
    }
    override val maleAccIntro: String? by lazy { reference.optionalString("maleAccIntro") }
    override val aspectNoPresent: CourseAspectNoPresent? by lazy {
        reference.optObj("aspectNoPresent")?.let { CourseAspectNoPresent(it.string("compact"), it.string("ios")) }
    }
    override val vocabularyInstructions: CourseVocabularyInstructions by lazy {
        root.obj("vocabulary").obj("instructions").let {
            CourseVocabularyInstructions(it.string("react"), it.string("web"), it.string("native"), it.string("ios"))
        }
    }
    override val vocabularyUnavailableLabel: String by lazy { root.obj("vocabulary").string("unavailableLabel") }

    override val frequency: List<FrequencyItem> by lazy {
        Json.parseToJsonElement(generatedFrequencyJson).jsonObject.rows("items").mapIndexed { index, value ->
            FrequencyItem(value.getValue("rank").jsonPrimitive.int, value.string("lemma"),
                value.getValue("count").jsonPrimitive.int).also { require(it.rank == index + 1) }
        }.also { require(it.size == 1000) }
    }

    /** A pronoun row without pl's full 7-case paradigm (en's `{subject, object}`) isn't this
     *  case-declining shape and is skipped — same reasoning as [nouns]. */
    override val personalPronouns: Map<String, Map<GramCase, String>> by lazy {
        root.obj("personalPronouns").mapNotNull { (id, value) ->
            val forms = value.jsonObject
            if (!GramCase.entries.all { forms.containsKey(it.id) }) return@mapNotNull null
            id to forms.caseForms()
        }.toMap()
    }

    /**
     * EnRuAcceptance-2026-09-28.md §7 item 1 (ADR-37 blocker 1): [personalPronouns] above skips
     * any pronoun row that isn't pl's full 7-case paradigm — en's own `{subject, object}` rows
     * (`lang/en/lexicon.json`) never pass that filter, so [PackEngine]'s generic `pronouns` port
     * (`PlEngine.kt`) needs every pack's raw, un-filtered case-id → form map instead, keyed by
     * whatever case-id strings the pack's own JSON actually declares — never forced through
     * [GramCase]'s pl-only closed set.
     */
    override val personalPronounRawForms: Map<String, Map<String, String>> by lazy {
        root.obj("personalPronouns").mapValues { (_, value) ->
            value.jsonObject.mapValues { (_, form) -> form.jsonPrimitive.content }
        }
    }

    override val possessives: List<Possessive> by lazy {
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
    override val possessiveForms: Map<PossessiveId, CoursePossessiveForms> by lazy {
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
    override val futureAuxiliary: FutureAuxiliary? by lazy {
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
