// Generated from dsl/tile-reveal.data-contract, dsl/learning/tile-reveal-outcomes.core-data
// source-sha256=1f538df1381f30377b5f8abeb4b955abfe66e73107b1dc957e2d5554ae0bd04e
// Edit the source, then run biome generate.

package polski.core.engine.policy.data

data class TileRevealOutcomeRow(
    val disposition: String,
    val numerator: Int,
    val addend: Int,
    val divisor: Int,
)

val TILE_REVEAL_OUTCOME_ROW_ROWS: List<TileRevealOutcomeRow> = listOf(
    TileRevealOutcomeRow(disposition = "limited", numerator = 1, addend = 1, divisor = 3),
    TileRevealOutcomeRow(disposition = "open", numerator = 1, addend = 0, divisor = 1),
)
