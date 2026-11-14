// Generated from dsl/tile-scaffolding.data-contract, dsl/learning/tile-scaffolding-outcomes.core-data
// source-sha256=b36b0762ce17b5b0256683754c879d09d703b153bcf8b936aa78d6fb0f640be9
// Edit the source, then run biome generate.

package polski.progress.policy.data

enum class TileScaffoldingOutcomeRowNewRung(val tag: String) {
    FULL("full"),
    PARTIAL("partial"),
    NONE("none"),
}

enum class TileScaffoldingOutcomeRowFinalStreak(val tag: String) {
    ZERO("zero"),
    NEXT("next"),
}

enum class TileScaffoldingOutcomeRowIndependentSinceAction(val tag: String) {
    CLEAR("clear"),
    KEEP("keep"),
    STAMP_FRESH("stamp_fresh"),
}

data class TileScaffoldingOutcomeRow(
    val disposition: String,
    val newRung: TileScaffoldingOutcomeRowNewRung,
    val finalStreak: TileScaffoldingOutcomeRowFinalStreak,
    val independentSinceAction: TileScaffoldingOutcomeRowIndependentSinceAction,
)

val TILE_SCAFFOLDING_OUTCOME_ROW_ROWS: List<TileScaffoldingOutcomeRow> = listOf(
    TileScaffoldingOutcomeRow(
        disposition = "wrong_at_none",
        newRung = TileScaffoldingOutcomeRowNewRung.PARTIAL,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.ZERO,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.CLEAR,
    ),
    TileScaffoldingOutcomeRow(
        disposition = "wrong_at_full",
        newRung = TileScaffoldingOutcomeRowNewRung.FULL,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.ZERO,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.CLEAR,
    ),
    TileScaffoldingOutcomeRow(
        disposition = "wrong_at_partial",
        newRung = TileScaffoldingOutcomeRowNewRung.PARTIAL,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.ZERO,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.CLEAR,
    ),
    TileScaffoldingOutcomeRow(
        disposition = "promote_full_to_partial",
        newRung = TileScaffoldingOutcomeRowNewRung.PARTIAL,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.ZERO,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.CLEAR,
    ),
    TileScaffoldingOutcomeRow(
        disposition = "stay_full",
        newRung = TileScaffoldingOutcomeRowNewRung.FULL,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.NEXT,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.CLEAR,
    ),
    TileScaffoldingOutcomeRow(
        disposition = "promote_partial_to_none",
        newRung = TileScaffoldingOutcomeRowNewRung.NONE,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.ZERO,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.STAMP_FRESH,
    ),
    TileScaffoldingOutcomeRow(
        disposition = "stay_partial",
        newRung = TileScaffoldingOutcomeRowNewRung.PARTIAL,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.NEXT,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.CLEAR,
    ),
    TileScaffoldingOutcomeRow(
        disposition = "already_hardest",
        newRung = TileScaffoldingOutcomeRowNewRung.NONE,
        finalStreak = TileScaffoldingOutcomeRowFinalStreak.NEXT,
        independentSinceAction = TileScaffoldingOutcomeRowIndependentSinceAction.KEEP,
    ),
)
