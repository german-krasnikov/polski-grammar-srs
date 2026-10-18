package polski.data

import polski.model.Verb

/** EN-22: read fresh. pl-ru returns PlRuTypedPack fields (NJ03 P7). */
val verbs: List<Verb> get() = packRegistry.active.verbs

fun verbById(id: String): Verb = verbs.firstOrNull { it.id == id } ?: error("Unknown verb $id")
