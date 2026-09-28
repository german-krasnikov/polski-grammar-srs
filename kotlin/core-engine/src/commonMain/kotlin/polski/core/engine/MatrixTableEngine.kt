package polski.core.engine

/**
 * UniversalCorePlan.md §4.1/§5.3.3, UC-09: one column of a [MatrixTable] — its header label, how
 * to resolve one row's value for it, and (optionally) the base value that row's cell is compared
 * against for before/after highlighting (ContrastHighlightPlan.md §4: every table row is labeled
 * with its own base value, and only the difference from that base is ever highlighted). [R] is
 * the row's own axis value (a case, a verb subject, a pronoun id, a chain step, ...) —
 * MatrixTableEngine never knows what it is.
 */
data class MatrixColumn<R>(val header: String, val cell: (R) -> String, val contrastFrom: ((R) -> String?)? = null)

/** One resolved cell: its text, plus the base text it should be highlighted against, if any. */
data class MatrixCell(val value: String, val contrastFrom: String? = null)

/** One resolved row: its own leading label (never itself part of any contrast) plus its cells, one per column. */
data class MatrixRow(val header: String, val cells: List<MatrixCell>)

/**
 * One resolved matrix/reference table: a header row plus data rows, already as plain strings —
 * independent of any language, pack or host. Every current matrix-page table (case rows, verb
 * conjugation, pronoun grids, chain/support tables) already has exactly this shape: a row axis
 * crossed with a fixed list of columns. [MatrixTableEngine] only assembles that shape; it never
 * resolves a lexeme, calls a language's morphology or decides what a "case" or "tense" is —
 * `rowHeader`/[MatrixColumn.cell]/[MatrixColumn.contrastFrom] are supplied by the caller and are
 * the only place pack data enters.
 */
data class MatrixTable(val rowHeaderLabel: String, val columnHeaders: List<String>, val rows: List<MatrixRow>)

object MatrixTableEngine {
    /** Crosses [rowAxis] with [columns], resolving every cell and its optional contrast base.
     *  [rowHeaderLabel] is the leading column's own header text (e.g. a case or subject label). */
    fun <R> build(rowAxis: List<R>, rowHeaderLabel: String, rowHeader: (R) -> String, columns: List<MatrixColumn<R>>): MatrixTable =
        MatrixTable(
            rowHeaderLabel,
            columnHeaders = columns.map { it.header },
            rows = rowAxis.map { row ->
                MatrixRow(rowHeader(row), columns.map { column -> MatrixCell(column.cell(row), column.contrastFrom?.invoke(row)) })
            },
        )
}
