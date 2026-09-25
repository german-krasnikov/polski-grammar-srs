package polski.presentation

/** Number of chain steps shown to the learner; completion includes the final rated step. */
val AppUiState.chainDisplayCount: Int
    get() = if (phase == CardPhase.ChainComplete) chain.size else chainIndex.coerceIn(0, chain.size)
