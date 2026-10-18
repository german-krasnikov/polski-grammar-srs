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
