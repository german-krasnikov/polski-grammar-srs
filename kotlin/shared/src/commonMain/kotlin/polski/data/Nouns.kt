package polski.data

import polski.model.Noun

/** EN-22: read fresh. pl-ru returns PlRuTypedPack fields (NJ03 P7). */
val nouns: List<Noun> get() = packRegistry.active.nouns

fun nounById(id: String): Noun = nouns.firstOrNull { it.id == id } ?: error("Unknown noun $id")

fun nounByIdOrNull(id: String): Noun? = nouns.firstOrNull { it.id == id }

fun nounLabel(id: String): String =
    packRegistry.active.nounLabels.getValue(id).let { (lemma, meaning) -> "$lemma — $meaning" }

fun nounLemma(id: String): String = packRegistry.active.nounLabels.getValue(id).first
