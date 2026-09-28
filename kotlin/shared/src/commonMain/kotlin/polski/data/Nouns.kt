package polski.data

import polski.model.Noun

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val nouns: List<Noun> get() = packRegistry.active.nouns

fun nounById(id: String): Noun = nouns.firstOrNull { it.id == id } ?: error("Unknown noun $id")

/** Null, not a throw, when [id] isn't in [nouns] — true for every noun of a genderless/caseless
 *  active pack (EnRuAcceptance §7 item 1: [nouns] only ever holds pl's case-declining shape).
 *  [PlEngine.kt]'s gender-driven lookups use this to degrade gracefully instead of crashing. */
fun nounByIdOrNull(id: String): Noun? = nouns.firstOrNull { it.id == id }
