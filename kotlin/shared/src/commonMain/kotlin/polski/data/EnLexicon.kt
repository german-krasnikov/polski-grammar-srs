package polski.data

import polski.core.enMorphology
import polski.model.NumberFeature
import polski.model.NumberGram
import polski.model.Person
import polski.model.PersonFeature
import polski.model.Tense
import polski.model.TenseFeature
import polski.model.toFeatureValue

/**
 * NJ03: typed en-specific data for matrix tables.
 * Reads from generated typed vals (GeneratedEnRuPack.kt), no JSON.
 */
data class EnVerb(val id: String, val lemma: String, val meaning: String)
data class EnPronoun(val id: String, val subject: String)

val enVerbs: List<EnVerb> get() = generatedEnVerbs
val enPersonalPronouns: List<EnPronoun> get() = generatedEnPronouns

private val enPersonNumberByPronounId = mapOf(
    "I" to (Person.FIRST to NumberGram.SG),
    "you" to (Person.SECOND to NumberGram.SG),
    "he" to (Person.THIRD to NumberGram.SG),
    "she" to (Person.THIRD to NumberGram.SG),
    "it" to (Person.THIRD to NumberGram.SG),
    "we" to (Person.FIRST to NumberGram.PL),
    "they" to (Person.THIRD to NumberGram.PL),
)

fun enVerbForm(verbId: String, tense: Tense, pronounId: String): String {
    val (person, number) = enPersonNumberByPronounId.getValue(pronounId)
    return enMorphology.form(
        "verb:$verbId",
        mapOf(TenseFeature to tense.toFeatureValue(), PersonFeature to person.toFeatureValue(), NumberFeature to number.toFeatureValue()),
    )
}
