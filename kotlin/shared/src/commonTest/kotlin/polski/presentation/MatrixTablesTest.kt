package polski.presentation

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.data.comparisonNounIds
import polski.data.enPersonalPronouns
import polski.data.nounById
import polski.data.personalPronouns
import polski.data.possessives
import polski.data.referencePronounTeaching
import polski.data.referenceVerbTeaching
import polski.grammar.caseRows
import polski.grammar.caseSentence
import polski.grammar.nounPhrase
import polski.grammar.verbForm
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import polski.presentation.data.oracle.MATRIX_CELL_CASE_ROW_ROWS
import polski.presentation.data.oracle.MATRIX_HEADER_ROW_ROWS
import polski.presentation.data.oracle.MATRIX_TABLE_CASE_ROW_ROWS
import polski.presentation.data.oracle.MatrixTableCaseRowPack
import polski.presentation.data.oracle.MatrixTableCaseRowTableKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * UC-09 part 2/2: [casesFullTable]/[comparisonTable]/[verbsTable]/[personalPronounsTable]/
 * [possessivesTable] are the exact tables desktop's `MatrixScreen.kt` (`CasesDesktop`/
 * `VerbsDesktop`/`PronounsDesktop`) and macOS's `matrixSnapshot` build by hand today — every
 * DSL rows pin observed headers and selected cells independently of the builders. The source-wide
 * property checks below cover every row and contrast, including cells outside the literal oracle.
 */
class MatrixTablesTest {
    private val selection = MatrixSelection()

    @Test fun literalMatrixOraclePreservesHeadersCellsAndContrastRoles() {
        val headers = MATRIX_HEADER_ROW_ROWS.groupBy { it.caseRef }
        val cells = MATRIX_CELL_CASE_ROW_ROWS.groupBy { it.caseRef }
        assertEquals(MATRIX_TABLE_CASE_ROW_ROWS.map { it.id }.toSet(), headers.keys)
        assertEquals(MATRIX_TABLE_CASE_ROW_ROWS.map { it.id }.toSet(), cells.keys)
        MATRIX_TABLE_CASE_ROW_ROWS.forEach { case ->
            val caseSelection = selection.copy(
                nounId = case.nounId ?: selection.nounId,
                adjectiveId = case.adjectiveId ?: selection.adjectiveId,
                ownerId = case.ownerId ?: selection.ownerId,
                numberId = case.numberId ?: selection.numberId,
                verbId = case.verbId ?: selection.verbId,
                feminineGroup = case.feminineGroup ?: selection.feminineGroup,
            )
            val table = when (case.tableKind) {
                MatrixTableCaseRowTableKind.CASES -> casesFullTable(caseSelection)
                MatrixTableCaseRowTableKind.COMPARISON -> comparisonTable(caseSelection)
                MatrixTableCaseRowTableKind.VERBS -> verbsTable(caseSelection)
                MatrixTableCaseRowTableKind.PRONOUNS -> personalPronounsTable()
                MatrixTableCaseRowTableKind.POSSESSIVES -> possessivesTable()
                MatrixTableCaseRowTableKind.ENVERBS -> enVerbsTable(requireNotNull(case.verbId))
                MatrixTableCaseRowTableKind.ENDOSUPPORT -> enDoSupportTable()
            }
            val expectedPack = when (case.tableKind) {
                MatrixTableCaseRowTableKind.ENVERBS, MatrixTableCaseRowTableKind.ENDOSUPPORT -> MatrixTableCaseRowPack.EN
                else -> MatrixTableCaseRowPack.PL
            }
            assertEquals(expectedPack, case.pack, "${case.id} pack")
            assertEquals(case.rowHeaderLabel, table.rowHeaderLabel, case.id)
            assertEquals(case.rowCount, table.rows.size, "${case.id} row count")
            val expectedHeaders = headers.getValue(case.id).sortedBy { it.ordinal }
            assertEquals(table.columnHeaders.indices.toList(), expectedHeaders.map { it.ordinal }, "${case.id} header order")
            assertEquals(expectedHeaders.map { it.label }, table.columnHeaders, case.id)
            cells.getValue(case.id).forEach { row ->
                val tableRow = table.rows[row.rowIndex]
                val cell = tableRow.cells[row.columnIndex]
                assertEquals(row.rowHeader, tableRow.header, row.cellId)
                assertEquals(row.value, cell.value, row.cellId)
                if (row.expectNoContrast == true) {
                    assertNull(cell.contrast, row.cellId)
                } else {
                    assertTrue(row.contrastFrom != null && row.contrastTo != null, "${row.cellId} incomplete oracle")
                    assertEquals(row.contrastFrom, cell.contrast?.from, "${row.cellId} contrast from")
                    assertEquals(row.contrastTo, cell.contrast?.to, "${row.cellId} contrast to")
                }
            }
        }
    }

    private fun assertSourceCell(cell: MatrixTableCell, expectedValue: String, contrastFrom: String? = null) {
        assertEquals(expectedValue, cell.value)
        assertEquals(contrastFrom?.let { ContrastPair.generated(it, expectedValue) }, cell.contrast)
    }

    @Test fun plTablesCoverEverySourceRowAndSubject() {
        val number = NumberGram.fromId(selection.numberId)
        val owner = PossessiveId.fromId(selection.ownerId)
        val seed = SentenceSeed(selection.nounId, selection.adjectiveId)
        val cases = casesFullTable(selection)
        val nomPhrase = nounPhrase(selection.nounId, GramCase.NOM, number, selection.adjectiveId, owner)
        val nomSentence = caseSentence(seed, GramCase.NOM, owner, number)
        assertEquals(caseRows.size, cases.rows.size)
        caseRows.forEachIndexed { index, row ->
            val cells = cases.rows[index].cells
            assertEquals("${row.pl} · ${row.ru}", cases.rows[index].header)
            assertSourceCell(cells[0], "${row.question} · ${row.trigger}")
            assertSourceCell(cells[1], nounPhrase(selection.nounId, row.id, number, selection.adjectiveId, owner), nomPhrase)
            assertSourceCell(cells[2], caseSentence(seed, row.id, owner, number), nomSentence)
        }

        val comparison = comparisonTable(selection)
        assertEquals(comparisonNounIds.size, comparison.columnHeaders.size)
        caseRows.forEachIndexed { rowIndex, row ->
            comparisonNounIds.forEachIndexed { columnIndex, id ->
                val forms = nounById(id).forms.getValue(number)
                assertSourceCell(comparison.rows[rowIndex].cells[columnIndex], forms.getValue(row.id), forms.getValue(GramCase.NOM))
            }
        }

        val teaching = referenceVerbTeaching
        val tenses = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE)
        val verbs = verbsTable(selection)
        val lemma = polski.data.verbs.first { it.id == selection.verbId }.lemma
        assertEquals(teaching.subjects.size, verbs.rows.size)
        teaching.subjects.forEachIndexed { rowIndex, subject ->
            assertEquals(subject.label.compact, verbs.rows[rowIndex].header)
            tenses.forEachIndexed { columnIndex, tense ->
                val form = verbForm(selection.verbId, tense, subject.person, subject.number, subject.gender(selection.feminineGroup))
                assertSourceCell(verbs.rows[rowIndex].cells[columnIndex], form, lemma)
            }
        }

        val pronouns = personalPronounsTable()
        val pronounTeaching = referencePronounTeaching
        pronounTeaching.pronounIds.forEachIndexed { rowIndex, id ->
            assertEquals(id, pronouns.rows[rowIndex].header)
            pronounTeaching.contexts.forEachIndexed { columnIndex, context ->
                assertSourceCell(pronouns.rows[rowIndex].cells[columnIndex], context.value(id, personalPronouns.getValue(id)), id)
            }
        }

        val possessiveTable = possessivesTable()
        val demo = pronounTeaching.demo
        possessives.forEachIndexed { rowIndex, possessive ->
            assertEquals(possessive.label, possessiveTable.rows[rowIndex].header)
            demo.cases.forEachIndexed { columnIndex, case ->
                assertSourceCell(possessiveTable.rows[rowIndex].cells[columnIndex], demo.phrase(possessive.id, case.id),
                    demo.phrase(possessive.id, GramCase.NOM))
            }
            assertSourceCell(possessiveTable.rows[rowIndex].cells[demo.cases.size], demo.rule(possessive.id))
        }
    }

    @Test fun englishTablesCoverEverySubjectAndFutureHasNoDoSupport() {
        val verbs = enVerbsTable("see")
        val support = enDoSupportTable()
        val subjects = enPersonalPronouns.map { it.subject }
        assertEquals(subjects, verbs.rows.map { it.header })
        assertEquals(subjects, support.rows.map { it.header })
        support.rows.forEach { row ->
            assertNull(Regex("\\bdid\\b|\\bdoes?\\b").find(row.cells[2].value))
            assertNull(row.cells[2].contrast)
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
