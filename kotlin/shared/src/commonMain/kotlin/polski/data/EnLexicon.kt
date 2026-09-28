package polski.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.core.enMorphology
import polski.model.NumberFeature
import polski.model.NumberGram
import polski.model.Person
import polski.model.PersonFeature
import polski.model.Tense
import polski.model.TenseFeature
import polski.model.toFeatureValue

/**
 * EN-24 (UC-09 part 2/2 minimum, then the ios lane): typed, read-only views of `lang/en/
 * lexicon.json`'s verbs and personal pronouns, for the web and iOS matrix hosts' English tables.
 * Deliberately independent of [packRegistry]'s active pack (unlike [nouns]/[verbs]/
 * [personalPronouns] above, which are pl-ru's data until pack-switching hosts is wired, EN-22) —
 * en's own reference table must show real en data regardless of which pack is currently active.
 */
data class EnVerb(val id: String, val lemma: String, val meaning: String)

/** [subject] is the pronoun's own subject-case spelling ("I", "he", "they", ...), in `lexicon.json`'s own declared order. */
data class EnPronoun(val id: String, val subject: String)

private val enLexiconRoot by lazy { Json.parseToJsonElement(generatedLexiconJsonByLang.getValue("en")).jsonObject }

val enVerbs: List<EnVerb> by lazy {
    enLexiconRoot.getValue("verbs").jsonArray.map { entry ->
        val o = entry.jsonObject
        EnVerb(o.getValue("id").jsonPrimitive.content, o.getValue("lemma").jsonPrimitive.content, o.getValue("meaning").jsonPrimitive.content)
    }
}

/** In `lexicon.json`'s own declared pronoun order (I, you, he, she, it, we, they). */
val enPersonalPronouns: List<EnPronoun> by lazy {
    enLexiconRoot.getValue("personalPronouns").jsonObject.map { (id, forms) ->
        EnPronoun(id, forms.jsonObject.getValue("subject").jsonPrimitive.content)
    }
}

/** EN-24 (UC-09 part 2/2, ios lane): a pronoun id's grammatical person/number, for [enVerbForm].
 *  Promoted here from what was a webMain-only private map (`MatrixWeb.kt`) once the iOS matrix
 *  host needed the exact same lookup — one source, not a second hand-copied mapping. */
private val enPersonNumberByPronounId = mapOf(
    "I" to (Person.FIRST to NumberGram.SG),
    "you" to (Person.SECOND to NumberGram.SG),
    "he" to (Person.THIRD to NumberGram.SG),
    "she" to (Person.THIRD to NumberGram.SG),
    "it" to (Person.THIRD to NumberGram.SG),
    "we" to (Person.FIRST to NumberGram.PL),
    "they" to (Person.THIRD to NumberGram.PL),
)

/** [enMorphology] form for [verbId]/[tense] at [pronounId]'s person/number — the one lookup both
 *  the web and iOS English matrix tables use, so their forms cannot drift apart. */
fun enVerbForm(verbId: String, tense: Tense, pronounId: String): String {
    val (person, number) = enPersonNumberByPronounId.getValue(pronounId)
    return enMorphology.form(
        "verb:$verbId",
        mapOf(TenseFeature to tense.toFeatureValue(), PersonFeature to person.toFeatureValue(), NumberFeature to number.toFeatureValue()),
    )
}
