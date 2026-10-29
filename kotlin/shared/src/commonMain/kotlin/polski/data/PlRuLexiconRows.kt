package polski.data

import polski.data.rows.plru.*
import polski.model.*

/**
 * R1.5: the pl-ru nouns, adjectives and verbs from the generated rows. The fields of a form record are the keys of the data
 * (m_personal, p1): this file pairs each with the id the model uses, and a missing form is a data error, not a null here.
 * A gender stays the id text of the data and is resolved by the model.
 */
internal val generatedPlRuNouns: List<Noun> = NOUN_ROW_ROWS.map { it.toNoun() }
internal val generatedPlRuAdjectives: List<Adjective> = ADJECTIVE_ROW_ROWS.map { it.toAdjective() }
internal val generatedPlRuVerbs: List<Verb> = VERB_ROW_ROWS.map { it.toVerb() }

internal fun NounRow.toNoun() =
    Noun(id, lemma, meaning, Gender.fromId(gender), mapOf(NumberGram.SG to forms.sg.toCases(), NumberGram.PL to forms.pl.toCases()))

internal fun AdjectiveRow.toAdjective() =
    Adjective(id, lemma, meaning, mapOf(NumberGram.SG to forms.sg.toGenders(), NumberGram.PL to forms.pl.toGenders()))

internal fun VerbRow.toVerb() = Verb(
    id, lemma, meaning, aspect.toModel(), present.toPersons(), PastStem(pastStem.m, pastStem.f, pastStem.n, pastStem.mp, pastStem.np),
    futureType.toModel(), perfectivePair,
)

internal fun CaseFormsRow.toCases() = mapOf(
    GramCase.NOM to nom, GramCase.GEN to gen, GramCase.DAT to dat, GramCase.ACC to acc, GramCase.INST to inst, GramCase.LOC to loc, GramCase.VOC to voc,
)

internal fun GenderFormsRow.toGenders() = mapOf(
    Gender.M_PERSONAL to m_personal.toCases(), Gender.M_ANIMATE to m_animate.toCases(), Gender.M_INANIMATE to m_inanimate.toCases(),
    Gender.F to f.toCases(), Gender.N to n.toCases(),
)

internal fun PresentRow.toPersons() = mapOf(NumberGram.SG to sg.toPersons(), NumberGram.PL to pl.toPersons())

private fun PersonFormsRow.toPersons() = mapOf(Person.FIRST to p1, Person.SECOND to p2, Person.THIRD to p3)

private fun VerbRowAspect.toModel() = when (this) {
    VerbRowAspect.IMPERFECTIVE -> Aspect.IMPERFECTIVE
    VerbRowAspect.PERFECTIVE -> Aspect.PERFECTIVE
}

private fun VerbRowFutureType.toModel() = when (this) {
    VerbRowFutureType.COMPOUND -> FutureType.COMPOUND
    VerbRowFutureType.PRESENT -> FutureType.PRESENT
}
