package polski.presentation

import polski.core.engine.MatrixTable

/** One resolved cell, ready for any host to render: [value] plus the highlight parts computed
 *  against its column's declared base ([MatrixTable]'s `contrastFrom`), if any. `contrast == null`
 *  means the cell renders as plain text — no "было → стало" pair applies. */
data class MatrixTableCell(val value: String, val contrast: ContrastPair?)

data class MatrixTableRow(val header: String, val cells: List<MatrixTableCell>)

/** A host-independent matrix/reference table, one per section (system map, cases, verbs,
 *  pronouns, ...) — the single source every host renderer (web DOM, Compose, SwiftUI) reads
 *  instead of hand-building its own headers/rows/contrast, per UniversalCorePlan.md §5.3.3
 *  (UC-09) and ContrastHighlightPlan.md §4. */
data class MatrixTableViewModel(val rowHeaderLabel: String, val columnHeaders: List<String>, val rows: List<MatrixTableRow>)

/**
 * Attaches this layer's own "было → стало" highlight semantics ([ContrastPair.generated], the
 * same function every other contrast surface uses — ContrastHighlightPlan.md §4) to a language-
 * neutral [MatrixTable] built by `:core-engine`'s `MatrixTableEngine`. `:core-engine` resolves
 * *what* each cell says and *which* base value it compares against; only this presentation layer
 * decides *how* that comparison is highlighted, so every matrix table shares one highlighting path
 * with the rest of the app instead of a second, table-only one.
 */
fun MatrixTable.toViewModel(): MatrixTableViewModel = MatrixTableViewModel(
    rowHeaderLabel = rowHeaderLabel,
    columnHeaders = columnHeaders,
    rows = rows.map { row ->
        MatrixTableRow(
            row.header,
            row.cells.map { cell -> MatrixTableCell(cell.value, cell.contrastFrom?.let { ContrastPair.generated(it, cell.value) }) },
        )
    },
)
