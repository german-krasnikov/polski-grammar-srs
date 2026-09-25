package polski.data

import polski.model.Noun

val nouns: List<Noun> by lazy { PolishCourseData.nouns }

fun nounById(id: String): Noun = nouns.firstOrNull { it.id == id } ?: error("Unknown noun $id")
