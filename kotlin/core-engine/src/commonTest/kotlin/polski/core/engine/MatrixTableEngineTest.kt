package polski.core.engine

import kotlin.test.Test
import kotlin.test.assertEquals

/** UniversalCorePlan.md §5.3.3, UC-09: MatrixTableEngine only crosses a row axis with columns —
 *  it never resolves a lexeme or knows what a "case" is, so a plain `Int` row axis is enough to
 *  prove the shape. Real pack-data parity lives in `:shared`'s `MatrixTableViewModelTest`, since
 *  only that layer wires in an actual language's forms and ContrastPair highlighting. */
class MatrixTableEngineTest {
    @Test fun crossesTheRowAxisWithEveryColumnInOrder() {
        val table = MatrixTableEngine.build(
            rowAxis = listOf(1, 2, 3),
            rowHeaderLabel = "n",
            rowHeader = { "row$it" },
            columns = listOf(
                MatrixColumn<Int>("double", { r -> (r * 2).toString() }),
                MatrixColumn<Int>("square", { r -> (r * r).toString() }),
            ),
        )
        assertEquals(listOf("double", "square"), table.columnHeaders)
        assertEquals(3, table.rows.size)
        assertEquals(MatrixRow("row1", listOf(MatrixCell("2"), MatrixCell("1"))), table.rows[0])
        assertEquals(MatrixRow("row2", listOf(MatrixCell("4"), MatrixCell("4"))), table.rows[1])
        assertEquals(MatrixRow("row3", listOf(MatrixCell("6"), MatrixCell("9"))), table.rows[2])
    }

    @Test fun attachesAContrastBaseOnlyToColumnsThatDeclareOne() {
        val table = MatrixTableEngine.build(
            rowAxis = listOf("nom", "gen"),
            rowHeaderLabel = "case",
            rowHeader = { it },
            columns = listOf(
                MatrixColumn<String>("plain", { it }),
                MatrixColumn<String>("contrasted", { "$it-form" }, contrastFrom = { "nom-form" }),
            ),
        )
        assertEquals(null, table.rows[0].cells[0].contrastFrom)
        assertEquals("nom-form", table.rows[0].cells[1].contrastFrom)
        assertEquals("nom-form", table.rows[1].cells[1].contrastFrom)
    }

    @Test fun emptyRowAxisProducesNoRowsButKeepsHeaders() {
        val table = MatrixTableEngine.build<String>(emptyList(), "id", { it }, listOf(MatrixColumn("h", { it })))
        assertEquals(listOf("h"), table.columnHeaders)
        assertEquals(emptyList(), table.rows)
    }
}
