package polski.data

import polski.model.Verb

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val verbs: List<Verb> get() = packRegistry.active.verbs

fun verbById(id: String): Verb = verbs.firstOrNull { it.id == id } ?: error("Unknown verb $id")
