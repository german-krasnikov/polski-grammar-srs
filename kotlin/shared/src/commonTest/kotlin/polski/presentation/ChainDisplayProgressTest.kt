package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.training.ExerciseFactory
import polski.training.ExerciseIdFactory
import polski.training.RandomSource
import polski.training.sentenceSeeds

class ChainDisplayProgressTest {
    @Test fun finalRatingChangesDisplayCountWithoutChangingStoreIndex() {
        val chain = ExerciseFactory(RandomSource { 0.1 }, ExerciseIdFactory { "display-progress" })
            .generateChain(sentenceSeeds.first())
        val before = AppUiState(chain = chain, chainIndex = 4, phase = CardPhase.Question)
        assertEquals(4, before.chainDisplayCount)
        val complete = before.copy(phase = CardPhase.ChainComplete)
        assertEquals(5, complete.chainDisplayCount)
        assertEquals(4, complete.chainIndex)
    }
}
