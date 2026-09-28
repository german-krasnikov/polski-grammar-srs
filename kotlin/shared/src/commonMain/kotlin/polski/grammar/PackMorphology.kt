package polski.grammar

import polski.core.plMorphology
import polski.data.caseSentencePrefix
import polski.data.nounById
import polski.data.verbById
import polski.model.Aspect
import polski.model.CaseFeature
import polski.model.Gender
import polski.model.GenderFeature
import polski.model.GramCase
import polski.model.NumberFeature
import polski.model.NumberGram
import polski.model.Person
import polski.model.PersonFeature
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import polski.model.TenseFeature
import polski.model.toFeatureValue

/**
 * UniversalCorePlan.md §5.1/§5.3/§12 UC-08: pl's morphology (noun/adjective/possessive/verb forms
 * — used by matrix/reference screens as well as [nounPhrase]/[caseSentence] below) now reads
 * [plMorphology] (`:core-engine`'s `TableMorphology`, built from `forms.generated.json`) instead
 * of the deleted `GrammarEngine`'s hand-written declension/suffix rules. Exhaustively proven
 * byte-identical to the old rules for every lexeme × feature combination by
 * `TableMorphologyParityTest`/`GrammarParityTest` before this switch.
 */
fun nounForm(id: String, gramCase: GramCase, number: NumberGram = NumberGram.SG): String =
    plMorphology.form("noun:$id", mapOf(NumberFeature to number.toFeatureValue(), CaseFeature to gramCase.toFeatureValue()))

fun adjectiveForm(id: String, gender: Gender, gramCase: GramCase, number: NumberGram = NumberGram.SG): String =
    plMorphology.form(
        "adjective:$id",
        mapOf(NumberFeature to number.toFeatureValue(), GenderFeature to gender.toFeatureValue(), CaseFeature to gramCase.toFeatureValue()),
    )

/** Looks up a course-authored possessive; non-personal plural genders share the other paradigm (folded into the table already). */
fun possessiveForm(id: PossessiveId, gender: Gender, number: NumberGram, gramCase: GramCase): String =
    plMorphology.form(
        "possessive:${id.id}",
        mapOf(NumberFeature to number.toFeatureValue(), GenderFeature to gender.toFeatureValue(), CaseFeature to gramCase.toFeatureValue()),
    )

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

/** Verb id validity and the present/perfective contract (Polish has no such tense) stay explicit; every actual form is a table lookup. */
fun verbForm(id: String, tense: Tense, person: Person, number: NumberGram, gender: Gender = Gender.M_PERSONAL): String {
    val verb = verbById(id)
    if (tense == Tense.PRESENT && verb.aspect == Aspect.PERFECTIVE) error("Perfective verbs have no present tense")
    val bundle = buildMap {
        put(TenseFeature, tense.toFeatureValue())
        put(PersonFeature, person.toFeatureValue())
        put(NumberFeature, number.toFeatureValue())
        if (tense == Tense.PAST) put(GenderFeature, gender.toFeatureValue())
    }
    return plMorphology.form("verb:$id", bundle)
}

fun caseSentence(seed: SentenceSeed, gramCase: GramCase, owner: PossessiveId = PossessiveId.MY, number: NumberGram = NumberGram.SG): String {
    val phrase = nounPhrase(seed.nounId, gramCase, number, seed.adjectiveId, owner)
    if (gramCase == GramCase.VOC) return capitalize(phrase) + "!"
    val prefix = caseSentencePrefix(gramCase, number)
    return "$prefix $phrase."
}

fun capitalize(value: String): String = if (value.isEmpty()) value else polishUppercase(value.take(1)) + value.drop(1)
