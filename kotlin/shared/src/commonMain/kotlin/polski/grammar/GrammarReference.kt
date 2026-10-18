package polski.grammar

import polski.model.Gender
import polski.model.GramCase
import polski.data.generatedPlRuCaseReferenceRows
import polski.data.generatedPlRuReferenceGenderNames
import polski.data.packRegistry

/** Ordered table metadata used by the grammar reference screen. */
data class CaseRow(
    val id: GramCase,
    val pl: String,
    val ru: String,
    val question: String,
    val trigger: String,
    val skill: String? = null,
)

// EN-22 fix (EnRuAcceptance §7 item 1, corrected): read fresh (`get()`, not `by lazy` — the same
// "actually changes after a later selectCoursePack" fix CourseData.kt already applies) and fall
// back to empty/blank instead of `!!` — see CourseData.kt's reference-wrapper comment for why: a
// pack whose `reference` block has no case rows/gender names (en-ru) degrades to nothing taught
// here rather than crashing a host's reference screen once that pack is genuinely active.
// NJ03: pl-ru reads from typed Biome literal source.
val caseRows: List<CaseRow> get() = if (packRegistry.active.id == "pl-ru")
    generatedPlRuCaseReferenceRows.map { CaseRow(it.id, it.pl, it.ru, it.question, it.trigger, it.skill) }
    else (packRegistry.active.caseReferenceRows ?: emptyList()).map { CaseRow(it.id, it.pl, it.ru, it.question, it.trigger, it.skill) }

val genderNames: Map<Gender, String> get() {
    val names = if (packRegistry.active.id == "pl-ru") generatedPlRuReferenceGenderNames
        else (packRegistry.active.referenceGenderNames ?: emptyMap())
    return Gender.entries.associateWith { names[it.id] ?: "" }
}
