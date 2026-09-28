package polski.presentation

import polski.core.engine.MatrixColumn
import polski.core.engine.MatrixTableEngine
import polski.data.adjectives
import polski.data.comparisonNounIds
import polski.data.nounById
import polski.data.nouns
import polski.data.personalPronouns
import polski.data.possessives
import polski.data.referenceAspectRows
import polski.data.referenceChainRows
import polski.data.referencePronounTeaching
import polski.data.referenceRussianSupport
import polski.data.referenceTenseRows
import polski.data.referenceVerbTeaching
import polski.data.verbs
import polski.grammar.caseRows
import polski.grammar.caseSentence
import polski.grammar.genderNames
import polski.grammar.nounPhrase
import polski.grammar.verbForm
import polski.model.Aspect
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * UniversalCorePlan.md §5.3.3/§12 UC-09 acceptance: MatrixTableEngine + [MatrixTableViewModel]
 * must produce the same cell values and highlight parts as the matrix page's own current, hand-
 * built tables (`MatrixWeb.kt`'s `renderCases`/`renderVerbs`/`renderPronouns`/`renderMap`, web-only
 * and so not importable from `commonTest`). Every expected value below is computed the exact same
 * way that file computes it today — same pack data, same grammar functions, same `contrastFrom`
 * base — so a change that breaks the generic engine's parity with those sections fails here first.
 * No host is touched; this only proves the engine can replace their per-section table-building.
 */
class MatrixTableViewModelTest {
    private val nounId = "wife"
    private val adjectiveId = "beautiful"
    private val number = NumberGram.SG
    private val owner = PossessiveId.MY
    private val seed = SentenceSeed(nounId, adjectiveId)

    // --- Cases: "Все семь падежей на одной группе слов" (renderCases, first matrixTable) ---

    @Test fun casesMainTableMatchesTheCurrentPerCaseFormsAndSentences() {
        val table = MatrixTableEngine.build(
            rowAxis = caseRows,
            rowHeaderLabel = "Падеж · русская опора",
            rowHeader = { "${it.pl} · ${it.ru}" },
            columns = listOf(
                MatrixColumn("Вопрос / конструкция", { "${it.question} · ${it.trigger}" }),
                MatrixColumn(
                    "Целая группа слов",
                    { nounPhrase(nounId, it.id, number, adjectiveId, owner) },
                    contrastFrom = { nounPhrase(nounId, GramCase.NOM, number, adjectiveId, owner) },
                ),
                MatrixColumn(
                    "Целое предложение",
                    { caseSentence(seed, it.id, owner, number) },
                    contrastFrom = { caseSentence(seed, GramCase.NOM, owner, number) },
                ),
            ),
        ).toViewModel()

        assertEquals(caseRows.size, table.rows.size)
        caseRows.forEachIndexed { i, row ->
            val expectedPhrase = nounPhrase(nounId, row.id, number, adjectiveId, owner)
            val expectedSentence = caseSentence(seed, row.id, owner, number)
            assertEquals("${row.pl} · ${row.ru}", table.rows[i].header)
            assertEquals("${row.question} · ${row.trigger}", table.rows[i].cells[0].value)
            assertEquals(expectedPhrase, table.rows[i].cells[1].value)
            assertEquals(expectedSentence, table.rows[i].cells[2].value)
            val nomPhrase = nounPhrase(nounId, GramCase.NOM, number, adjectiveId, owner)
            val nomSentence = caseSentence(seed, GramCase.NOM, owner, number)
            assertEquals(ContrastPair.generated(nomPhrase, expectedPhrase), table.rows[i].cells[1].contrast)
            assertEquals(ContrastPair.generated(nomSentence, expectedSentence), table.rows[i].cells[2].contrast)
        }
    }

    // --- Cases: "Сравнение типов склонения" (renderCases, second matrixTable) ---

    @Test fun casesComparisonTableMatchesEveryComparisonNounsFormPerCase() {
        val comparisonIds = comparisonNounIds
        val table = MatrixTableEngine.build(
            rowAxis = caseRows,
            rowHeaderLabel = "Падеж",
            rowHeader = { it.pl },
            columns = comparisonIds.map { id ->
                val noun = nounById(id)
                MatrixColumn(
                    "${noun.lemma} · ${genderNames.getValue(noun.gender)}",
                    { row -> nounById(id).forms.getValue(number).getValue(row.id) },
                    contrastFrom = { nounById(id).forms.getValue(number).getValue(GramCase.NOM) },
                )
            },
        ).toViewModel()

        assertEquals(comparisonIds.map { "${nounById(it).lemma} · ${genderNames.getValue(nounById(it).gender)}" }, table.columnHeaders)
        caseRows.forEachIndexed { rowIndex, row ->
            comparisonIds.forEachIndexed { colIndex, id ->
                val expected = nounById(id).forms.getValue(number).getValue(row.id)
                val base = nounById(id).forms.getValue(number).getValue(GramCase.NOM)
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(base, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
        }
    }

    // --- Verbs: "Лицо × число × время" (renderVerbs, conjugation matrixTable) ---

    @Test fun verbConjugationTableMatchesEverySubjectAcrossAllThreeTenses() {
        val verbId = verbs.first { it.aspect == Aspect.IMPERFECTIVE }.id
        val teaching = referenceVerbTeaching
        val feminineGroup = false
        val tenses = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE)
        val table = MatrixTableEngine.build(
            rowAxis = teaching.subjects,
            rowHeaderLabel = "Кто",
            rowHeader = { it.label.full },
            columns = tenses.map { tense ->
                MatrixColumn(
                    teaching.tenseLabels.getValue(tense).full,
                    { subject -> verbForm(verbId, tense, subject.person, subject.number, subject.gender(feminineGroup)) },
                    contrastFrom = { verbs.first { it.id == verbId }.lemma },
                )
            },
        ).toViewModel()

        assertEquals(teaching.subjects.size, table.rows.size)
        val lemma = verbs.first { it.id == verbId }.lemma
        teaching.subjects.forEachIndexed { rowIndex, subject ->
            tenses.forEachIndexed { colIndex, tense ->
                val expected = verbForm(verbId, tense, subject.person, subject.number, subject.gender(feminineGroup))
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(lemma, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
        }
    }

    // --- Verbs: "Время меняется, предложение остаётся целым" (renderVerbs, tense-sentence table) ---

    @Test fun tenseSentenceTableMatchesEveryRowsOwnFromToSentencePair() {
        val table = MatrixTableEngine.build(
            rowAxis = referenceTenseRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(MatrixColumn("Предложение", { it.to }, contrastFrom = { it.from })),
        ).toViewModel()

        referenceTenseRows.forEachIndexed { i, row ->
            assertEquals(row.label, table.rows[i].header)
            assertEquals(row.to, table.rows[i].cells[0].value)
            assertEquals(ContrastPair.generated(row.from, row.to), table.rows[i].cells[0].contrast)
        }
    }

    // --- Verbs: "Вид: процесс или результат" (renderVerbs, aspect table) ---

    @Test fun aspectTableOmitsContrastOnlyForARowWithNoPresentTense() {
        val courseAspectNoPresentCompact = "—"
        val table = MatrixTableEngine.build(
            rowAxis = referenceAspectRows,
            rowHeaderLabel = "Смысл",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn(
                    "Настоящее",
                    { it.present ?: courseAspectNoPresentCompact },
                    contrastFrom = { row -> if (row.present == null) null else row.from },
                ),
                MatrixColumn("Прошедшее", { it.past }, contrastFrom = { it.from }),
                MatrixColumn("Будущее", { it.future }, contrastFrom = { it.from }),
            ),
        ).toViewModel()

        referenceAspectRows.forEachIndexed { i, row ->
            val presentCell = table.rows[i].cells[0]
            if (row.present == null) {
                assertEquals(courseAspectNoPresentCompact, presentCell.value)
                assertEquals(null, presentCell.contrast)
            } else {
                assertEquals(row.present, presentCell.value)
                assertEquals(ContrastPair.generated(row.from, row.present), presentCell.contrast)
            }
            assertEquals(ContrastPair.generated(row.from, row.past), table.rows[i].cells[1].contrast)
            assertEquals(ContrastPair.generated(row.from, row.future), table.rows[i].cells[2].contrast)
        }
    }

    // --- Pronouns: personal pronoun table (renderPronouns, first matrixTable) ---

    @Test fun personalPronounTableMatchesEveryPronounAcrossAllContexts() {
        val teaching = referencePronounTeaching
        val table = MatrixTableEngine.build(
            rowAxis = teaching.pronounIds,
            rowHeaderLabel = "Кто",
            rowHeader = { it },
            columns = teaching.contexts.map { context ->
                MatrixColumn(
                    "${context.cue.full} · ${context.caseName}",
                    { id -> context.value(id, personalPronouns.getValue(id)) },
                    contrastFrom = { id -> id },
                )
            },
        ).toViewModel()

        teaching.pronounIds.forEachIndexed { rowIndex, id ->
            teaching.contexts.forEachIndexed { colIndex, context ->
                val expected = context.value(id, personalPronouns.getValue(id))
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(id, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
        }
    }

    // --- Pronouns: possessive table (renderPronouns, second matrixTable) ---

    @Test fun possessiveTableHighlightsOnlyTheThreeCaseColumnsNotTheRuleColumn() {
        val teaching = referencePronounTeaching
        val demo = teaching.demo
        val table = MatrixTableEngine.build(
            rowAxis = possessives,
            rowHeaderLabel = "Кому принадлежит",
            rowHeader = { it.label },
            columns = demo.cases.map { case ->
                MatrixColumn<polski.data.Possessive>(case.web, { p -> demo.phrase(p.id, case.id) }, contrastFrom = { p -> demo.phrase(p.id, GramCase.NOM) })
            } + MatrixColumn<polski.data.Possessive>("Правило", { p -> demo.rule(p.id) }),
        ).toViewModel()

        possessives.forEachIndexed { rowIndex, possessive ->
            demo.cases.forEachIndexed { colIndex, case ->
                val expected = demo.phrase(possessive.id, case.id)
                val base = demo.phrase(possessive.id, GramCase.NOM)
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(base, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
            val ruleCell = table.rows[rowIndex].cells[demo.cases.size]
            assertEquals(demo.rule(possessive.id), ruleCell.value)
            assertEquals(null, ruleCell.contrast)
        }
    }

    // --- Map: "Одна мысль, пять преобразований" (renderMap, chain matrixTable) ---

    @Test fun chainTableHighlightsOnlyTheWholeSentenceColumnAgainstItsOwnFrom() {
        val table = MatrixTableEngine.build(
            rowAxis = referenceChainRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn("Целое предложение", { it.to }, contrastFrom = { it.from }),
                MatrixColumn("Что изменилось", { it.change }),
            ),
        ).toViewModel()

        referenceChainRows.forEachIndexed { i, row ->
            assertEquals(row.label, table.rows[i].header)
            assertEquals(row.to, table.rows[i].cells[0].value)
            assertEquals(ContrastPair.generated(row.from, row.to), table.rows[i].cells[0].contrast)
            assertEquals(row.change, table.rows[i].cells[1].value)
            assertEquals(null, table.rows[i].cells[1].contrast)
        }
    }

    // --- Map: Russian-support table (renderMap, support matrixTable) — no contrast column ---

    @Test fun russianSupportTableCarriesThePairsOwnConstructionAndCheckTextWithNoContrast() {
        val support = referenceRussianSupport
        val table = MatrixTableEngine.build(
            rowAxis = support.rows,
            rowHeaderLabel = support.columns[0],
            rowHeader = { it.cue },
            columns = listOf(
                MatrixColumn(support.columns[1], { it.web.construction }),
                MatrixColumn(support.columns[2], { it.web.check }),
            ),
        ).toViewModel()

        support.rows.forEachIndexed { i, row ->
            assertEquals(row.cue, table.rows[i].header)
            assertEquals(row.web.construction, table.rows[i].cells[0].value)
            assertEquals(row.web.check, table.rows[i].cells[1].value)
            assertEquals(null, table.rows[i].cells[0].contrast)
            assertEquals(null, table.rows[i].cells[1].contrast)
        }
    }

    init {
        // Sanity: the fixtures used above are the real pl-ru pack data, not test doubles.
        require(nouns.any { it.id == nounId })
        require(adjectives.any { it.id == adjectiveId })
    }
}
