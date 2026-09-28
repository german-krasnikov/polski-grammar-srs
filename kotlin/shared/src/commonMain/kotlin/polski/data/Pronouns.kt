package polski.data

import polski.model.GramCase
import polski.model.PossessiveId

data class Possessive(val id: PossessiveId, val label: String)

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val personalPronouns: Map<String, Map<GramCase, String>> get() = packRegistry.active.personalPronouns
val possessives: List<Possessive> get() = packRegistry.active.possessives
