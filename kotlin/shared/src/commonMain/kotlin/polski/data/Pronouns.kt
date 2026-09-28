package polski.data

import polski.model.GramCase
import polski.model.PossessiveId

data class Possessive(val id: PossessiveId, val label: String)

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val personalPronouns: Map<String, Map<GramCase, String>> get() = packRegistry.active.personalPronouns
val possessives: List<Possessive> get() = packRegistry.active.possessives

/** EnRuAcceptance-2026-09-28.md §7 item 1: the active pack's raw case-id → form map for one
 *  pronoun — see [polski.data.CoursePack.personalPronounRawForms]'s KDoc for why this exists
 *  alongside the pl-shaped, [GramCase]-keyed [personalPronouns]. */
fun personalPronounForm(pronounId: String, caseId: String): String =
    packRegistry.active.personalPronounRawForms.getValue(pronounId).getValue(caseId)
