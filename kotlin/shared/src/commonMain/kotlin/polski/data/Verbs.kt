package polski.data

import polski.model.Verb

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
// NJ03 B7: pl-ru reads from typed Biome literal source.
val verbs: List<Verb> get() = if (packRegistry.active.id == "pl-ru") generatedPlRuVerbs
    else packRegistry.active.verbs

fun verbById(id: String): Verb = verbs.firstOrNull { it.id == id } ?: error("Unknown verb $id")
