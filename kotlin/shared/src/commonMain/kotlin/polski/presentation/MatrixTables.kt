package polski.presentation

import polski.core.engine.MatrixColumn
import polski.core.engine.MatrixTableEngine
import polski.data.Possessive
import polski.data.comparisonNounIds
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
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense

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
