package polski.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * EN-24 (UC-09 part 2/2 minimum): typed, read-only views of `lang/en/lexicon.json`'s verbs and
 * personal pronouns, for the web matrix's English table. Deliberately independent of
 * [packRegistry]'s active pack (unlike [nouns]/[verbs]/[personalPronouns] above, which are pl-ru's
 * data until pack-switching hosts is wired, EN-22) — en's own reference table must show real en
 * data regardless of which pack is currently active.
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
