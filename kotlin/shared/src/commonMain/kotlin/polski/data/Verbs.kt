package polski.data

import polski.model.Verb

val verbs: List<Verb> by lazy { packRegistry.active.verbs }

fun verbById(id: String): Verb = verbs.firstOrNull { it.id == id } ?: error("Unknown verb $id")
