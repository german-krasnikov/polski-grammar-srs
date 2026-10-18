package polski.data

import polski.model.GramCase
import polski.model.PossessiveId

data class Possessive(val id: PossessiveId, val label: String)

/** EN-22: read fresh. pl-ru returns PlRuTypedPack fields (NJ03 P7). */
val personalPronouns: Map<String, Map<GramCase, String>> get() = packRegistry.active.personalPronouns
val possessives: List<Possessive> get() = packRegistry.active.possessives

internal val possessiveForms: Map<PossessiveId, CoursePossessiveForms> get() =
    packRegistry.active.possessiveForms

fun personalPronounForm(pronounId: String, caseId: String): String =
    packRegistry.active.personalPronounRawForms.getValue(pronounId).getValue(caseId)
