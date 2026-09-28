package polski.presentation

import polski.core.engine.MatrixColumn
import polski.core.engine.MatrixTableEngine
import polski.core.enMorphology
import polski.data.Possessive
import polski.data.comparisonNounIds
import polski.data.enPersonalPronouns
import polski.data.enVerbs
import polski.data.nounById
import polski.data.personalPronouns
import polski.data.possessives
import polski.data.referencePronounTeaching
import polski.data.referenceVerbTeaching
import polski.data.verbs
import polski.grammar.caseRows
import polski.grammar.caseSentence
import polski.grammar.nounPhrase
import polski.grammar.verbForm
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
 * UC-09 part 2/2 (UniversalCorePlan.md §5.3.3, ADR-21): one [MatrixTableViewModel] builder per
 * "Матрица" table, shared by every host. Each function is a literal transcription of the ad-hoc
 * table a host already built by hand (desktop's `CasesDesktop`/`VerbsDesktop`/`PronounsDesktop` in
 * `MatrixScreen.kt`, macOS's `matrixSnapshot` in `MacSnapshot.kt`, web's `MatrixWeb.kt`) — same row
 * axis, same columns, same "было → стало" base per column — so hosts stop duplicating this glue and
 * instead render whatever `MatrixTableEngine` + this layer's contrast attachment produced.
 */

fun casesFullTable(selection: MatrixSelection): MatrixTableViewModel {
    val number = NumberGram.fromId(selection.numberId)
    val owner = PossessiveId.fromId(selection.ownerId)
    val seed = SentenceSeed(selection.nounId, selection.adjectiveId)
    return MatrixTableEngine.build(
        rowAxis = caseRows,
        rowHeaderLabel = "Падеж · русская опора",
        rowHeader = { row -> "${row.pl} · ${row.ru}" },
        columns = listOf(
            MatrixColumn("Вопрос / конструкция", { row -> "${row.question} · ${row.trigger}" }),
            MatrixColumn(
                "Вся группа слов",
                { row -> nounPhrase(selection.nounId, row.id, number, selection.adjectiveId, owner) },
                contrastFrom = { nounPhrase(selection.nounId, GramCase.NOM, number, selection.adjectiveId, owner) },
            ),
            MatrixColumn(
                "Целое предложение",
                { row -> caseSentence(seed, row.id, owner, number) },
                contrastFrom = { caseSentence(seed, GramCase.NOM, owner, number) },
            ),
        ),
    ).toViewModel()
}

fun comparisonTable(selection: MatrixSelection): MatrixTableViewModel {
    val number = NumberGram.fromId(selection.numberId)
    return MatrixTableEngine.build(
        rowAxis = caseRows,
        rowHeaderLabel = "Падеж",
        rowHeader = { row -> row.pl },
        columns = comparisonNounIds.map { id ->
            val noun = nounById(id)
            MatrixColumn(
                noun.lemma,
                { row -> noun.forms.getValue(number).getValue(row.id) },
                contrastFrom = { noun.forms.getValue(number).getValue(GramCase.NOM) },
            )
        },
    ).toViewModel()
}

fun verbsTable(selection: MatrixSelection): MatrixTableViewModel {
    val teaching = referenceVerbTeaching
    val lemma = verbs.first { it.id == selection.verbId }.lemma
    return MatrixTableEngine.build(
        rowAxis = teaching.subjects,
        rowHeaderLabel = "Кто",
        rowHeader = { subject -> subject.label.compact },
        columns = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).map { tense ->
            MatrixColumn(
                teaching.tenseLabels.getValue(tense).compact,
                { subject -> verbForm(selection.verbId, tense, subject.person, subject.number, subject.gender(selection.feminineGroup)) },
                contrastFrom = { lemma },
            )
        },
    ).toViewModel()
}

fun personalPronounsTable(): MatrixTableViewModel {
    val teaching = referencePronounTeaching
    return MatrixTableEngine.build(
        rowAxis = teaching.pronounIds,
        rowHeaderLabel = "Кто",
        rowHeader = { id -> id },
        columns = teaching.contexts.map { context ->
            MatrixColumn(
                context.cue.compact,
                { id -> context.value(id, personalPronouns.getValue(id)) },
                contrastFrom = { id -> id },
            )
        },
    ).toViewModel()
}

private val enTenseLabel = mapOf(Tense.PRESENT to "Настоящее", Tense.PAST to "Прошедшее", Tense.FUTURE to "Будущее")

private val enPersonNumberByPronounId = mapOf(
    "I" to (Person.FIRST to NumberGram.SG),
    "you" to (Person.SECOND to NumberGram.SG),
    "he" to (Person.THIRD to NumberGram.SG),
    "she" to (Person.THIRD to NumberGram.SG),
    "it" to (Person.THIRD to NumberGram.SG),
    "we" to (Person.FIRST to NumberGram.PL),
    "they" to (Person.THIRD to NumberGram.PL),
)

private fun enVerbForm(verbId: String, tense: Tense, pronounId: String): String {
    val (person, number) = enPersonNumberByPronounId.getValue(pronounId)
    return enMorphology.form(
        "verb:$verbId",
        mapOf(TenseFeature to tense.toFeatureValue(), PersonFeature to person.toFeatureValue(), NumberFeature to number.toFeatureValue()),
    )
}

/**
 * EN-24 (UC-09 part 2/2 minimum, Plans/Kotlin/EnRuPackPlan.md §6): the one live English person ×
 * tense table — real, irregular `forms.generated.json`(en) values read through [enMorphology] and
 * the same [MatrixTableEngine]/[MatrixTableViewModel] every pl table above already uses. Shared so
 * every host (web's `MatrixWeb.kt`, macOS's `matrixSnapshot`, desktop's `VerbsDesktop`) renders the
 * exact same table instead of each hand-rolling its own copy of [enVerbForm].
 */
fun enVerbsTable(verbId: String = "see"): MatrixTableViewModel {
    val lemma = enVerbs.first { it.id == verbId }.lemma
    return MatrixTableEngine.build(
        rowAxis = enPersonalPronouns,
        rowHeaderLabel = "Кто",
        rowHeader = { pronoun -> pronoun.subject },
        columns = Tense.entries.map { tense ->
            MatrixColumn(enTenseLabel.getValue(tense), { pronoun -> enVerbForm(verbId, tense, pronoun.id) }, contrastFrom = { lemma })
        },
    ).toViewModel()
}

/** The do-support table next to [enVerbsTable]: present splits by person (do/does), past is
 *  invariant ("did"), and future has no do-support of its own (built on "will" alone). */
fun enDoSupportTable(): MatrixTableViewModel = MatrixTableEngine.build(
    rowAxis = enPersonalPronouns,
    rowHeaderLabel = "Кто",
    rowHeader = { pronoun -> pronoun.subject },
    columns = listOf(
        MatrixColumn(enTenseLabel.getValue(Tense.PRESENT), { pronoun -> enVerbForm("do", Tense.PRESENT, pronoun.id) }, contrastFrom = { "do" }),
        MatrixColumn(enTenseLabel.getValue(Tense.PAST), { pronoun -> enVerbForm("do", Tense.PAST, pronoun.id) }, contrastFrom = { "do" }),
        MatrixColumn(enTenseLabel.getValue(Tense.FUTURE), { "не нужен — только will" }),
    ),
).toViewModel()

fun possessivesTable(): MatrixTableViewModel {
    val teaching = referencePronounTeaching
    return MatrixTableEngine.build(
        rowAxis = possessives,
        rowHeaderLabel = "Кому принадлежит",
        rowHeader = { possessive -> possessive.label },
        columns = teaching.demo.cases.map { row ->
            MatrixColumn<Possessive>(
                row.desktop,
                { possessive -> teaching.demo.phrase(possessive.id, row.id) },
                contrastFrom = { possessive -> teaching.demo.phrase(possessive.id, GramCase.NOM) },
            )
        } + MatrixColumn<Possessive>("Правило", { possessive -> teaching.demo.rule(possessive.id) }),
    ).toViewModel()
}
