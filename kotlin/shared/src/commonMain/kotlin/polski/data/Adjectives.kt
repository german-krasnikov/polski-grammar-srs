package polski.data

import polski.model.Adjective

/** EN-22: read fresh. pl-ru returns PlRuTypedPack fields (NJ03 P7). */
val adjectives: List<Adjective> get() = packRegistry.active.adjectives

fun adjectiveById(id: String): Adjective = adjectives.firstOrNull { it.id == id } ?: error("Unknown adjective $id")
