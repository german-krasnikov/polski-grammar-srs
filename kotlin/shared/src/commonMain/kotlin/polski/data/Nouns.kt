package polski.data

import polski.model.Noun

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val nouns: List<Noun> get() = packRegistry.active.nouns

fun nounById(id: String): Noun = nouns.firstOrNull { it.id == id } ?: error("Unknown noun $id")

/** Null, not a throw, when [id] isn't in [nouns] — true for every noun of a genderless/caseless
 *  active pack (EnRuAcceptance §7 item 1: [nouns] only ever holds pl's case-declining shape).
 *  [PlEngine.kt]'s gender-driven lookups use this to degrade gracefully instead of crashing. */
fun nounByIdOrNull(id: String): Noun? = nouns.firstOrNull { it.id == id }

/** "lemma — meaning" for any noun the active pack actually declares, independent of whether it
 *  has pl's case/gender shape (EnRuAcceptance §7 item 3/4) — see [polski.data.CoursePack.nounLabels]. */
fun nounLabel(id: String): String = packRegistry.active.nounLabels.getValue(id).let { (lemma, meaning) -> "$lemma — $meaning" }

/** Just the lemma half of [nounLabel] — every host's chain-seed picker only ever needs this one
 *  field, never [nounById]'s full case/gender-declining [polski.model.Noun]. */
fun nounLemma(id: String): String = packRegistry.active.nounLabels.getValue(id).first
