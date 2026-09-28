package polski.data

import polski.model.Noun

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val nouns: List<Noun> get() = packRegistry.active.nouns

fun nounById(id: String): Noun = nouns.firstOrNull { it.id == id } ?: error("Unknown noun $id")
