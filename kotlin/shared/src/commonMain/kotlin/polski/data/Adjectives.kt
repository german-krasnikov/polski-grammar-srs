package polski.data

import polski.model.Adjective

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
// NJ03 B7: pl-ru reads from typed Biome literal source.
val adjectives: List<Adjective> get() = if (packRegistry.active.id == "pl-ru") generatedPlRuAdjectives
    else packRegistry.active.adjectives

fun adjectiveById(id: String): Adjective = adjectives.firstOrNull { it.id == id } ?: error("Unknown adjective $id")
