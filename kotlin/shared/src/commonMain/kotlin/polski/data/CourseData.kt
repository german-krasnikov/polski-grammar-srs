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






/**
 * UC-03/EN-06: the single point where consumers reach a [CoursePack], keyed by [CoursePack.pairId].
 * [active] defaults to the first registered pack (pl-ru today, unchanged behavior) until
 * [select] switches it — EN-22 is the first production caller (host preferences sessions), driven
 * by `UserPreferencesV2.target`/`native` and gated by [usableCourseSelections].
 */
internal class PackRegistry(private val packs: List<CoursePackFields>) {
    init { require(packs.isNotEmpty() && packs.map { it.pairId }.distinct().size == packs.size) }

    var active: CoursePackFields = packs.first()
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
internal val packRegistry: PackRegistry by lazy {
    PackRegistry(buildList<CoursePackFields> {
        // NJ03 P7: pl-ru uses typed Biome literal source (PlRuTypedPack object).
        // Other packs read from JSON via CoursePack.
        // NJ03 P7: all packs are typed objects — zero JSON at runtime.
        add(PlRuTypedPack)
        add(EnRuTypedPack)
    })
}

/** EN-22: `(target, native)` for every pack this build actually embeds, pl-ru first — every pack
 * that exists, not necessarily one a picker may safely offer yet (see [usableCourseSelections]). */
val availableCourseSelections: List<Pair<String, String>> by lazy { packRegistry.options }

val usableCourseSelections: List<Pair<String, String>> by lazy {
    buildList {
        // NJ03 P7: all packs are typed — always usable.
        add(PlRuTypedPack.targetLanguage to PlRuTypedPack.nativeLanguage)
        add(EnRuTypedPack.targetLanguage to EnRuTypedPack.nativeLanguage)
    }
}

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
// NJ03: pl-ru metadata from typed source (pair.json still provides other fields).
private val plRuPairId = "pl-ru"
private val plRuTarget = "pl"
private val plRuNative = "ru"

val activeCoursePackId: String get() = packRegistry.active.pairId

/** The currently active pack's own [CoursePackOption] — a public target/native pair (`internal`
 *  [packRegistry] is not visible outside `:shared`) for a vocabulary direction picker or any other
 *  consumer that needs the active pack's languages without an `availableCoursePacks` lookup. */
val activeCoursePackOption: CoursePackOption get() =
    CoursePackOption(packRegistry.active.pairId, packRegistry.active.targetLanguage, packRegistry.active.nativeLanguage)

/** Switches the active pack; throws for a [pairId] not in [availableCoursePacks]. */
fun selectCoursePack(pairId: String) {
    require(availableCoursePacks.any { it.pairId == pairId }) { "Unusable pack pairId: $pairId" }
    packRegistry.select(pairId)
}

// EN-22: every val below was `by lazy` (frozen at first access) — read fresh now so a later
// selectCoursePack (host picker, this task) actually changes what these return; see Skills.kt's
// `skills` KDoc for the same fix's original writeup. `caseSentencePrefix`/`exerciseCopy` above were
// already plain `fun`s reading `packRegistry.active` fresh, so they needed no change.
// NJ03 B9: pl-ru reads from typed Biome literal source; other packs use JSON path.
val courseSentenceSeeds: List<SentenceSeed> get() =
packRegistry.active.sentenceSeeds
val courseStemAlternations: List<StemAlternation> get() =
packRegistry.active.stemAlternations

fun caseSentencePrefix(gramCase: GramCase, number: NumberGram): String {
    require(gramCase != GramCase.VOC)
    return caseSentencePrefix(gramCase.id, number)
}

/**
 * EnRuAcceptance-2026-09-28.md §7 item 1 (ADR-37 blocker 1): the active pack's own case-id
 * vocabulary is not necessarily [GramCase]'s pl-shaped closed set — en-ru's own
 * `caseSentencePrefixes` (`courses/pairs/en-ru/pair.json`) is keyed by its own case ids ("Subj",
 * not "nom"). [PackEngine]'s generic `casePrefix` port (`PlEngine.kt`) reads a pack's raw case id
 * straight from its own [ConstructionRealizer] bundle instead of forcing it through
 * [GramCase.fromId], which only recognizes pl's ids and throws `Unknown case` on any other pack's.
 * pl's own [caseSentencePrefix] overload above still routes through here unchanged, so its
 * `"nomSg"`/`"nomPl"` key split (pl's own convention, not a universal one) is preserved byte-for-byte.
 */
fun caseSentencePrefix(caseId: String, number: NumberGram): String {
    val key = if (caseId == GramCase.NOM.id) {
        if (number == NumberGram.SG) "nomSg" else "nomPl"
    } else caseId
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

// NJ03: pl-ru reads from typed Biome literal source for all reference fields.
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

internal fun JsonObject.string(key: String): String =
    (get(key) as? JsonPrimitive)?.content?.takeIf(String::isNotEmpty) ?: error("Missing course field $key")

internal fun JsonObject.optionalString(key: String): String? = (get(key) as? JsonPrimitive)?.content
internal fun JsonObject.obj(key: String): JsonObject = getValue(key).jsonObject
internal fun JsonObject.rows(key: String): List<JsonObject> = getValue(key).jsonArray.map { it.jsonObject }

/** [key] absent (a pack's `reference` block genuinely not covering this teaching topic, EN-22 fix)
 *  is null, not a parse error — see [CoursePack.reference]'s own KDoc for why. */
internal fun JsonObject.optObj(key: String): JsonObject? = (get(key) as? JsonObject)
internal fun JsonObject.optRows(key: String): List<JsonObject>? = (get(key) as? JsonArray)?.map { it.jsonObject }
internal fun JsonObject.caseForms(): Map<GramCase, String> = GramCase.entries.associateWith { string(it.id) }
internal fun JsonObject.strictCaseForms(): Map<GramCase, String> {
    require(keys == GramCase.entries.map(GramCase::id).toSet())
    return GramCase.entries.associateWith { formText(it.id) }
}
internal fun JsonObject.formText(key: String): String = string(key).also { require(it.isNotBlank()) }
internal fun JsonObject.methodPresentation(): MethodPresentation {
    fun required(field: String): String = string(field).also { value ->
        require(value.isNotBlank()) { "Invalid method stage: $field" }
    }
    return MethodPresentation(
        required("introduction"), required("promptLead"), required("introduce"),
        required("retrieve"), required("feedback"), required("review"),
    )
}
internal fun JsonObject.russianSupportTable(): RussianSupportTable =
    RussianSupportTable(string("construction"), string("check"))

internal fun JsonObject.russianSupportComparison(path: String): ContrastPair {
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
internal fun JsonObject.verbLabel(): VerbLabel = VerbLabel(string("full"), string("compact"))

/** [json] is the optional `skill.styleContent` value as it comes out of `root["styleContent"]` — absent/non-object means the all-derived default. */
internal fun parseSkillStyleContent(json: JsonElement?): SkillStyleContent =
    (json as? JsonObject)?.toSkillStyleContent() ?: SkillStyleContent()

/** [json] is the optional `stemAlternations` value as it comes out of `root["stemAlternations"]` — absent/non-array means no pack-declared alternation (UC S2). */
internal fun parseStemAlternations(json: JsonElement?): List<StemAlternation> =
    (json as? JsonArray)?.map { it.jsonObject.toStemAlternation() } ?: emptyList()

internal fun JsonObject.toStemAlternation(): StemAlternation {
    val a = string("a")
    val b = string("b")
    require(a.length == 1 && b.length == 1) { "stemAlternations: a/b must be single characters" }
    return StemAlternation(a[0], b[0])
}

internal fun JsonObject.toSkillStyleContent(): SkillStyleContent = SkillStyleContent(
    rule = optionalString("rule"),
    table = (get("table") as? JsonObject)?.rows("rows")?.map { it.toTableRow() },
    scene = optionalString("scene"),
    nativeParallel = (get("nativeParallel") as? JsonArray)?.map { it.jsonObject.toNativeParallelPair() } ?: emptyList(),
    examples = (get("examples") as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList(),
    why = optionalString("why"),
)

internal fun JsonObject.toTableRow(): TableRow = TableRow(string("label"), endingParts("before"), endingParts("after"))

/** [NativeParallelPair.targetParts] is filled in later, at compose time, from the skill's own
 *  explicit `focus` pair (EmphasisUXAudit E7/S4) — this raw course-data parse knows no exercise. */
internal fun JsonObject.toNativeParallelPair(): NativeParallelPair =
    NativeParallelPair(string("native"), string("target"), string("note"), getValue("matches").jsonPrimitive.boolean, targetParts = emptyList())

internal fun JsonObject.endingParts(key: String): List<EndingPart> = rows(key).map { part ->
    EndingPart(part.string("text"), part.getValue("isEnding").jsonPrimitive.boolean, part.getValue("isChanged").jsonPrimitive.boolean)
}
/** Uniqueness only — emptiness is a legitimate result for a pl-shaped table a pack's own grammar
 *  has nothing to contribute to (EnRuAcceptance §7 item 1: en's genderless [Noun]/[Adjective]/
 *  [Verb] rows are filtered out upstream, not force-parsed), so it is never asserted here. */
internal fun <T> List<T>.requireUniqueIds(id: (T) -> String): List<T> = also { items ->
    require(items.map(id).distinct().size == items.size) { "Duplicate course IDs" }
}
