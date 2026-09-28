package polski.model

import polski.core.model.FeatureKey
import polski.core.model.FeatureValue

enum class GramCase(val id: String) {
    NOM("nom"), GEN("gen"), DAT("dat"), ACC("acc"), INST("inst"), LOC("loc"), VOC("voc");
    companion object { fun fromId(id: String): GramCase = entries.firstOrNull { it.id == id } ?: error("Unknown case $id") }
}

enum class NumberGram(val id: String) {
    SG("sg"), PL("pl");
    companion object { fun fromId(id: String): NumberGram = entries.firstOrNull { it.id == id } ?: error("Unknown number $id") }
}

enum class Gender(val id: String) {
    M_PERSONAL("m-personal"), M_ANIMATE("m-animate"), M_INANIMATE("m-inanimate"), F("f"), N("n");
    companion object { fun fromId(id: String): Gender = entries.firstOrNull { it.id == id } ?: error("Unknown gender $id") }
}

enum class Person(val id: Int) {
    FIRST(1), SECOND(2), THIRD(3);
    companion object { fun fromId(id: Int): Person = entries.firstOrNull { it.id == id } ?: error("Unknown person $id") }
}

enum class Tense(val id: String) {
    PRESENT("present"), PAST("past"), FUTURE("future");
    companion object { fun fromId(id: String): Tense = entries.firstOrNull { it.id == id } ?: error("Unknown tense $id") }
}

/** [ITS] is an en-ru-only value (English's third-person-singular-neuter possessive, EnRuAcceptance
 *  §7 item 1) — pl has no equivalent, so pl's own possessive rows never declare it. */
enum class PossessiveId(val id: String) {
    MY("my"), YOUR("your"), HIS("his"), HER("her"), OUR("our"), YOUR_PLURAL("yourPlural"), THEIR("their"), ITS("its");
    companion object { fun fromId(id: String): PossessiveId = entries.firstOrNull { it.id == id } ?: error("Unknown possessive $id") }
}

enum class Aspect(val id: String) { IMPERFECTIVE("imperfective"), PERFECTIVE("perfective") }
enum class FutureType(val id: String) { COMPOUND("compound"), PRESENT("present") }

data class Noun(val id: String, val lemma: String, val meaning: String, val gender: Gender, val forms: Map<NumberGram, Map<GramCase, String>>)
data class Adjective(val id: String, val lemma: String, val meaning: String, val forms: Map<NumberGram, Map<Gender, Map<GramCase, String>>>)
data class PastStem(val m: String, val f: String, val n: String?, val mp: String, val np: String)
data class Verb(val id: String, val lemma: String, val meaning: String, val aspect: Aspect, val present: Map<NumberGram, Map<Person, String>>?, val pastStem: PastStem, val futureType: FutureType, val perfectivePair: String?)
data class Skill(val id: String, val title: String, val group: String, val level: String, val formula: String, val theory: String, val hint: String, val prerequisites: List<String>)

/**
 * UniversalCorePlan.md §1/§4.1 (UC-01): pl's closed enums stay the engine's actual grammar model
 * (`fromId`, `.id` unchanged, still the only types `polski.grammar` (`PackMorphology.kt`)/
 * [polski.training.PlExerciseEngine] use) — these are a thin adapter declaring the subset of the
 * universal core's open [FeatureKey]/[FeatureValue] catalog pl uses, not a second grammar model.
 */
val CaseFeature = FeatureKey("Case")
val NumberFeature = FeatureKey("Number")
val GenderFeature = FeatureKey("Gender")
val PersonFeature = FeatureKey("Person")
val TenseFeature = FeatureKey("Tense")

fun GramCase.toFeatureValue(): FeatureValue = FeatureValue(id)
fun NumberGram.toFeatureValue(): FeatureValue = FeatureValue(id)
fun Gender.toFeatureValue(): FeatureValue = FeatureValue(id)
fun Tense.toFeatureValue(): FeatureValue = FeatureValue(id)
fun Person.toFeatureValue(): FeatureValue = FeatureValue(id.toString())
