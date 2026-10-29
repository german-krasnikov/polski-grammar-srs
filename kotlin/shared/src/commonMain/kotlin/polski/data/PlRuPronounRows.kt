package polski.data

import polski.data.rows.plru.*
import polski.model.*

/**
 * R1.6: the pl-ru personal pronouns and possessives from the generated rows. The raw forms are the same texts under the case ids.
 * A possessive is declined or invariable, never both and never neither: the contract cannot say "one or the other", so this does.
 */
internal val generatedPlRuPersonalPronouns: Map<String, Map<GramCase, String>> = PRONOUN_ROW_ROWS.associate { it.id to it.forms.toCases() }
internal val generatedPlRuPersonalPronounRawForms: Map<String, Map<String, String>> =
    generatedPlRuPersonalPronouns.mapValues { (_, forms) -> forms.mapKeys { it.key.id } }
internal val generatedPlRuPossessives: List<Possessive> = POSSESSIVE_ROW_ROWS.map { Possessive(PossessiveId.fromId(it.id), it.label) }
internal val generatedPlRuPossessiveForms: Map<PossessiveId, CoursePossessiveForms> =
    POSSESSIVE_ROW_ROWS.associate { PossessiveId.fromId(it.id) to it.toForms() }

internal fun PossessiveRow.toForms(): CoursePossessiveForms = with(forms) {
    when (kind) {
        PossessiveFormsRowKind.INVARIANT -> {
            require(sg == null && pl == null) { "$id: invariable, but declined forms are written" }
            CoursePossessiveForms.Invariant(requireNotNull(value) { "$id: invariable, but no value is written" })
        }
        PossessiveFormsRowKind.DECLINED -> {
            require(value == null) { "$id: declined, but a value is written" }
            val plural = requireNotNull(pl) { "$id: declined, but no plural is written" }
            CoursePossessiveForms.Declined(
                requireNotNull(sg) { "$id: declined, but no singular is written" }.toGenders(),
                mapOf(Gender.M_PERSONAL.id to plural.m_personal.toCases(), "other" to plural.other.toCases()),
            )
        }
    }
}
