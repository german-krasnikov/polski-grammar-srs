package polski.data

import polski.model.Adjective

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val adjectives: List<Adjective> get() = packRegistry.active.adjectives

fun adjectiveById(id: String): Adjective = adjectives.firstOrNull { it.id == id } ?: error("Unknown adjective $id")
