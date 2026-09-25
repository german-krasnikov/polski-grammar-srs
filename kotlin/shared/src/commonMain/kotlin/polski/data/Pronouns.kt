package polski.data

import polski.model.GramCase
import polski.model.PossessiveId

data class Possessive(val id: PossessiveId, val label: String)

val personalPronouns: Map<String, Map<GramCase, String>> by lazy { PolishCourseData.personalPronouns }
val possessives: List<Possessive> by lazy { PolishCourseData.possessives }
