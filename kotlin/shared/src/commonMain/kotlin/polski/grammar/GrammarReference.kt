package polski.grammar

import polski.model.Gender
import polski.model.GramCase
import polski.data.packRegistry

data class CaseRow(
    val id: GramCase, val pl: String, val ru: String,
    val question: String, val trigger: String, val skill: String? = null,
)

/** EN-22: read fresh. pl-ru returns PlRuTypedPack fields (NJ03 P7). */
val caseRows: List<CaseRow> get() =
    (packRegistry.active.caseReferenceRows ?: emptyList())
        .map { CaseRow(it.id, it.pl, it.ru, it.question, it.trigger, it.skill) }

val genderNames: Map<Gender, String> get() {
    val names = packRegistry.active.referenceGenderNames ?: emptyMap()
    return Gender.entries.associateWith { names[it.id] ?: "" }
}
