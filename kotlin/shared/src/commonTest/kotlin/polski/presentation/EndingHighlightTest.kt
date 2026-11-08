package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.model.FormChange
import polski.presentation.data.oracle.HIGHLIGHT_CASE_ROW_ROWS
import polski.presentation.data.oracle.HIGHLIGHT_PART_ROW_ROWS
import polski.presentation.data.oracle.HighlightCaseRowOperation
import polski.presentation.data.oracle.HighlightCaseRowSide
import polski.presentation.data.oracle.HighlightPartRowChangeSide

class EndingHighlightTest {
    @Test
    fun literalOraclePreservesOrderedFragmentsAndRoles() {
        val partsByCase = HIGHLIGHT_PART_ROW_ROWS.groupBy { it.caseRef }
        assertEquals(HIGHLIGHT_CASE_ROW_ROWS.map { it.id }.toSet(), partsByCase.keys)
        for (case in HIGHLIGHT_CASE_ROW_ROWS) {
            val expectedRows = partsByCase.getValue(case.id).sortedBy { it.ordinal }
            assertEquals((1..expectedRows.size).toList(), expectedRows.map { it.ordinal }, case.id)
            val expected = expectedRows.map { row ->
                EndingPart(row.text, row.isEnding, row.isChanged, row.changeSide?.let {
                    if (it == HighlightPartRowChangeSide.BEFORE) ChangeSide.Before else ChangeSide.After
                })
            }
            val side = when (case.side) {
                HighlightCaseRowSide.BEFORE -> ChangeSide.Before
                HighlightCaseRowSide.AFTER -> ChangeSide.After
                null -> null
            }
            fun changedParts(): List<EndingPart> {
                val selectedSide = requireNotNull(side) { "Missing side: ${case.id}" }
                val alternations = if (case.noAlternations == true) emptyList() else {
                    val a = case.alternationA
                    val b = case.alternationB
                    if (a != null && b != null) listOf(StemAlternation(a.single(), b.single())) else null
                }
                return if (alternations == null) changeHighlightParts(case.before, case.after, selectedSide)
                    else changeHighlightParts(case.before, case.after, selectedSide, alternations)
            }
            val actual = when (case.operation) {
                HighlightCaseRowOperation.ENDING -> endingHighlightParts(case.before, case.after)
                HighlightCaseRowOperation.CHANGE -> changedParts()
                HighlightCaseRowOperation.SENTENCE -> sentenceHighlightParts(
                    requireNotNull(case.text), listOf(FormChange(case.before, case.after, "oracle")), requireNotNull(side))
                HighlightCaseRowOperation.STYLE -> styleTextHighlightParts(requireNotNull(case.text), case.before, case.after)
            }
            assertEquals(expected, actual, case.id)
            assertEquals(case.text ?: if (side == ChangeSide.Before) case.before else case.after,
                actual.joinToString("") { it.text }, "${case.id} lossless text")
        }
    }

    @Test
    fun invalidExplicitPartsKeepLiteralFormWithoutColour() {
        val pair = ContrastPair(
            "żona", "żonie",
            listOf(EndingPart("żona", false, true)),
            listOf(EndingPart("żony", false, true)),
        )
        assertEquals(listOf(EndingPart("żonie", false)), pair.parts(ChangeSide.After))
    }

    @Test
    fun theActivePackAlternationAppliesWithoutCallersPassingIt() {
        assertEquals(listOf("ego"), endingHighlightParts("mój", "mojego").filter(EndingPart::isEnding).map(EndingPart::text))
    }

    @Test
    fun aSingleInsertedWordSurvivesAlongsideATrailingPunctuationChangeElsewhere() {
        val before = changeHighlightParts("Moja piękna żona idzie do domu.", "Czy moja piękna żona idzie do domu?", ChangeSide.Before)
        val after = changeHighlightParts("Moja piękna żona idzie do domu.", "Czy moja piękna żona idzie do domu?", ChangeSide.After)
        assertEquals("Moja piękna żona idzie do domu.", before.joinToString("") { it.text })
        assertEquals("Czy moja piękna żona idzie do domu?", after.joinToString("") { it.text })
        assertEquals(emptyList(), before.filter(EndingPart::isChanged))
        assertEquals(listOf("Czy"), after.filter(EndingPart::isChanged).map(EndingPart::text))
    }

    @Test
    fun theFutureAuxiliaryInATenseComparisonRowIsAPureInsertion() {
        val before = changeHighlightParts("Moja piękna żona szła do domu.", "Moja piękna żona będzie szła do domu.", ChangeSide.Before)
        val after = changeHighlightParts("Moja piękna żona szła do domu.", "Moja piękna żona będzie szła do domu.", ChangeSide.After)
        assertEquals("Moja piękna żona szła do domu.", before.joinToString("") { it.text })
        assertEquals("Moja piękna żona będzie szła do domu.", after.joinToString("") { it.text })
        assertEquals(emptyList(), before.filter(EndingPart::isChanged))
        assertEquals(listOf("będzie"), after.filter(EndingPart::isChanged).map(EndingPart::text))
    }

    @Test
    fun styleTextWithAnUnchangedPairNeverHighlightsIt() {
        val parts = styleTextHighlightParts("widzę moje duże morze", "moje duże morze", "moje duże morze")
        assertEquals(emptyList(), parts.filter(EndingPart::isChanged))
    }
}
