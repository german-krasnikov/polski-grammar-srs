package polski.data

import polski.model.Adjective

val adjectives: List<Adjective> by lazy { PolishCourseData.adjectives }

fun adjectiveById(id: String): Adjective = adjectives.firstOrNull { it.id == id } ?: error("Unknown adjective $id")
