package polski.data

import polski.model.Noun

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
// NJ03 B7: pl-ru reads from typed Biome literal source.
val nouns: List<Noun> get() = if (packRegistry.active.id == "pl-ru") generatedPlRuNouns
    else packRegistry.active.nouns

fun nounById(id: String): Noun = nouns.firstOrNull { it.id == id } ?: error("Unknown noun $id")

/** Null, not a throw, when [id] isn't in [nouns] — true for every noun of a genderless/caseless
 *  active pack (EnRuAcceptance §7 item 1: [nouns] only ever holds pl's case-declining shape).
 *  [PlEngine.kt]'s gender-driven lookups use this to degrade gracefully instead of crashing. */
fun nounByIdOrNull(id: String): Noun? = nouns.firstOrNull { it.id == id }

/** "lemma — meaning" for any noun the active pack actually declares, independent of whether it
 *  has pl's case/gender shape (EnRuAcceptance §7 item 3/4) — see [polski.data.CoursePack.nounLabels]. */
private val plRuNounLabels: Map<String, Pair<String, String>> by lazy {
    generatedPlRuNouns.associate { it.id to (it.lemma to it.meaning) }
}
private val nounLabels: Map<String, Pair<String, String>>
    get() = if (packRegistry.active.id == "pl-ru") plRuNounLabels else packRegistry.active.nounLabels

fun nounLabel(id: String): String = nounLabels.getValue(id).let { (lemma, meaning) -> "$lemma — $meaning" }

fun nounLemma(id: String): String = nounLabels.getValue(id).first
