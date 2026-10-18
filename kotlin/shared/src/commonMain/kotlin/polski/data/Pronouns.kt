package polski.data

import polski.model.GramCase
import polski.model.PossessiveId

data class Possessive(val id: PossessiveId, val label: String)

// NJ03: pl-ru reads from typed Biome literal source.
val personalPronouns: Map<String, Map<GramCase, String>> get() =
    if (packRegistry.active.id == "pl-ru") generatedPlRuPersonalPronouns
    else packRegistry.active.personalPronouns

val possessives: List<Possessive> get() =
    if (packRegistry.active.id == "pl-ru") generatedPlRuPossessives
    else packRegistry.active.possessives

internal val possessiveForms: Map<PossessiveId, CoursePossessiveForms> get() =
    if (packRegistry.active.id == "pl-ru") generatedPlRuPossessiveForms
    else packRegistry.active.possessiveForms

fun personalPronounForm(pronounId: String, caseId: String): String =
    if (packRegistry.active.id == "pl-ru") generatedPlRuPersonalPronounRawForms.getValue(pronounId).getValue(caseId)
    else packRegistry.active.personalPronounRawForms.getValue(pronounId).getValue(caseId)
