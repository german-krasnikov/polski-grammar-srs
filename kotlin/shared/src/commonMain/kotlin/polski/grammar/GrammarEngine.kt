package polski.grammar

import polski.data.adjectiveById
import polski.data.nounById
import polski.data.verbById
import polski.data.CoursePossessiveForms
import polski.data.packRegistry
import polski.data.caseSentencePrefix
import polski.model.*

fun nounForm(id: String, gramCase: GramCase, number: NumberGram = NumberGram.SG): String =
    nounById(id).forms.getValue(number).getValue(gramCase)

fun adjectiveForm(id: String, gender: Gender, gramCase: GramCase, number: NumberGram = NumberGram.SG): String =
    adjectiveById(id).forms[number]?.get(gender)?.get(gramCase)
        ?: error("No adjective form $id/${gender.id}")

/** Looks up a course-authored possessive; non-personal plural genders share the other paradigm. */
fun possessiveForm(id: PossessiveId, gender: Gender, number: NumberGram, gramCase: GramCase): String =
    when (val forms = packRegistry.active.possessiveForms.getValue(id)) {
        is CoursePossessiveForms.Invariant -> forms.value
        is CoursePossessiveForms.Declined -> if (number == NumberGram.SG) {
            forms.singular.getValue(gender).getValue(gramCase)
        } else {
            forms.plural.getValue(if (gender == Gender.M_PERSONAL) "m-personal" else "other").getValue(gramCase)
        }
    }

fun possessiveMy(gender: Gender, number: NumberGram, gramCase: GramCase): String =
    possessiveForm(PossessiveId.MY, gender, number, gramCase)

fun nounPhrase(
    nounId: String,
    gramCase: GramCase,
    number: NumberGram = NumberGram.SG,
    adjectiveId: String? = null,
    possessive: PossessiveId? = null,
): String {
    val noun = nounById(nounId)
    val parts = mutableListOf<String>()
    if (possessive != null) parts += possessiveForm(possessive, noun.gender, number, gramCase)
    if (adjectiveId != null) parts += adjectiveForm(adjectiveId, noun.gender, gramCase, number)
    parts += nounForm(nounId, gramCase, number)
    return parts.joinToString(" ")
}

fun verbForm(id: String, tense: Tense, person: Person, number: NumberGram, gender: Gender = Gender.M_PERSONAL): String {
    val verb = verbById(id)
    val present = verb.present?.get(number)?.get(person)
    if (tense == Tense.PRESENT) {
        if (verb.aspect == Aspect.PERFECTIVE) error("Perfective verbs have no present tense")
        return present ?: error("No present")
    }
    val auxiliary = packRegistry.active.futureAuxiliary
    val future = auxiliary.forms.getValue(number).getValue(person)
    if (tense == Tense.FUTURE) {
        if (id == auxiliary.verbId) return future
        if (verb.futureType == FutureType.PRESENT) return present ?: error("No future")
        return "$future ${verb.lemma}"
    }
    val shortSuffix = gender == Gender.F || gender == Gender.N
    val suffix = if (number == NumberGram.SG) {
        when (person) {
            Person.FIRST -> if (shortSuffix) "m" else "em"
            Person.SECOND -> if (shortSuffix) "ś" else "eś"
            Person.THIRD -> ""
        }
    } else {
        when (person) {
            Person.FIRST -> "śmy"
            Person.SECOND -> "ście"
            Person.THIRD -> ""
        }
    }
    val stem = if (number == NumberGram.PL) {
        if (gender == Gender.M_PERSONAL) verb.pastStem.mp else verb.pastStem.np
    } else {
        when (gender) {
            Gender.F -> verb.pastStem.f
            Gender.N -> verb.pastStem.n ?: verb.pastStem.m
            else -> verb.pastStem.m
        }
    }
    return stem + suffix
}

fun caseSentence(seed: SentenceSeed, gramCase: GramCase, owner: PossessiveId = PossessiveId.MY, number: NumberGram = NumberGram.SG): String {
    val phrase = nounPhrase(seed.nounId, gramCase, number, seed.adjectiveId, owner)
    if (gramCase == GramCase.VOC) return capitalize(phrase) + "!"
    val prefix = caseSentencePrefix(gramCase, number)
    return "$prefix $phrase."
}

fun capitalize(value: String): String = if (value.isEmpty()) value else polishUppercase(value.take(1)) + value.drop(1)
