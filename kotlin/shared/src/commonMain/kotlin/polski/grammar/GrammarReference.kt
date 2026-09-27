package polski.grammar

import polski.model.Gender
import polski.model.GramCase
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

val caseRows: List<CaseRow> by lazy {
    packRegistry.active.caseReferenceRows.map { CaseRow(it.id, it.pl, it.ru, it.question, it.trigger, it.skill) }
}

val genderNames: Map<Gender, String> by lazy {
    Gender.entries.associateWith { packRegistry.active.referenceGenderNames.getValue(it.id) }
}
