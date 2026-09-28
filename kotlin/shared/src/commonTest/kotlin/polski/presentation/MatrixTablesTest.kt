package polski.presentation

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * UC-09 part 2/2: [casesFullTable]/[comparisonTable]/[verbsTable]/[personalPronounsTable]/
 * [possessivesTable] are the exact tables desktop's `MatrixScreen.kt` (`CasesDesktop`/
 * `VerbsDesktop`/`PronounsDesktop`) and macOS's `matrixSnapshot` build by hand today — every
 * expected value/header below is computed the same way those call sites compute it, so a switch
 * to these shared builders cannot change a single cell a host already shows.
 */
class MatrixTablesTest {
    private val selection = MatrixSelection()

    @Test fun casesFullTableMatchesDesktopsHeadersAndPerCaseFormsAndSentences() {
        val number = NumberGram.fromId(selection.numberId)
        val owner = PossessiveId.fromId(selection.ownerId)
        val seed = SentenceSeed(selection.nounId, selection.adjectiveId)
        val table = casesFullTable(selection)

        assertEquals("Падеж · русская опора", table.rowHeaderLabel)
        assertEquals(listOf("Вопрос / конструкция", "Вся группа слов", "Целое предложение"), table.columnHeaders)
        assertEquals(caseRows.size, table.rows.size)
        caseRows.forEachIndexed { i, row ->
            val phrase = nounPhrase(selection.nounId, row.id, number, selection.adjectiveId, owner)
            val sentence = caseSentence(seed, row.id, owner, number)
            assertEquals("${row.pl} · ${row.ru}", table.rows[i].header)
            assertEquals("${row.question} · ${row.trigger}", table.rows[i].cells[0].value)
            assertNull(table.rows[i].cells[0].contrast)
            assertEquals(phrase, table.rows[i].cells[1].value)
            assertEquals(sentence, table.rows[i].cells[2].value)
            val nomPhrase = nounPhrase(selection.nounId, GramCase.NOM, number, selection.adjectiveId, owner)
            val nomSentence = caseSentence(seed, GramCase.NOM, owner, number)
            assertEquals(ContrastPair.generated(nomPhrase, phrase), table.rows[i].cells[1].contrast)
            assertEquals(ContrastPair.generated(nomSentence, sentence), table.rows[i].cells[2].contrast)
        }
    }

    @Test fun comparisonTableMatchesDesktopsLemmaHeadersAndEveryNounsFormPerCase() {
        val number = NumberGram.fromId(selection.numberId)
        val table = comparisonTable(selection)

        assertEquals(comparisonNounIds.map { nounById(it).lemma }, table.columnHeaders)
        caseRows.forEachIndexed { rowIndex, row ->
            comparisonNounIds.forEachIndexed { colIndex, id ->
                val noun = nounById(id)
                val expected = noun.forms.getValue(number).getValue(row.id)
                val base = noun.forms.getValue(number).getValue(GramCase.NOM)
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(base, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
        }
    }

    @Test fun verbsTableMatchesDesktopsCompactTenseLabelsAndEverySubject() {
        val teaching = referenceVerbTeaching
        val tenses = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE)
        val table = verbsTable(selection)
        val lemma = verbs.first { it.id == selection.verbId }.lemma

        assertEquals(tenses.map { teaching.tenseLabels.getValue(it).compact }, table.columnHeaders)
        teaching.subjects.forEachIndexed { rowIndex, subject ->
            assertEquals(subject.label.compact, table.rows[rowIndex].header)
            tenses.forEachIndexed { colIndex, tense ->
                val expected = verbForm(selection.verbId, tense, subject.person, subject.number, subject.gender(selection.feminineGroup))
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(lemma, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
        }
    }

    @Test fun personalPronounsTableMatchesDesktopsCompactCuesAndEveryPronoun() {
        val teaching = referencePronounTeaching
        val table = personalPronounsTable()

        assertEquals(teaching.contexts.map { it.cue.compact }, table.columnHeaders)
        teaching.pronounIds.forEachIndexed { rowIndex, id ->
            assertEquals(id, table.rows[rowIndex].header)
            teaching.contexts.forEachIndexed { colIndex, context ->
                val expected = context.value(id, personalPronouns.getValue(id))
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(id, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
        }
    }

    @Test fun possessivesTableMatchesDesktopsCaseColumnsPlusAnUnhighlightedRuleColumn() {
        val teaching = referencePronounTeaching
        val demo = teaching.demo
        val table = possessivesTable()

        assertEquals(demo.cases.map { it.desktop } + "Правило", table.columnHeaders)
        possessives.forEachIndexed { rowIndex, possessive ->
            assertEquals(possessive.label, table.rows[rowIndex].header)
            demo.cases.forEachIndexed { colIndex, case ->
                val expected = demo.phrase(possessive.id, case.id)
                val base = demo.phrase(possessive.id, GramCase.NOM)
                assertEquals(expected, table.rows[rowIndex].cells[colIndex].value)
                assertEquals(ContrastPair.generated(base, expected), table.rows[rowIndex].cells[colIndex].contrast)
            }
            val ruleCell = table.rows[rowIndex].cells[demo.cases.size]
            assertEquals(demo.rule(possessive.id), ruleCell.value)
            assertNull(ruleCell.contrast)
        }
    }

    @Test fun toJsonRoundTripsHeadersValuesAndContrastPairsLosslessly() {
        val json = casesFullTable(selection).toJson()
        assertEquals("Падеж · русская опора", json.jsonObject.getValue("rowHeaderLabel").jsonPrimitive.content)
        val firstRow = json.jsonObject.getValue("rows").jsonArray[1].jsonObject
        val cell = firstRow.getValue("cells").jsonArray[1].jsonObject
        val contrast = cell.getValue("contrast").jsonObject
        assertEquals(cell.getValue("value").jsonPrimitive.content, contrast.getValue("to").jsonPrimitive.content)
        assertEquals(contrast.getValue("from").jsonPrimitive.content,
            contrast.getValue("beforeParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })
        assertEquals(contrast.getValue("to").jsonPrimitive.content,
            contrast.getValue("afterParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })
    }
}
