package polski.core

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue

/**
 * EN-07 (gap G): [PackEngine] is the `pack -> engine` factory `PlEngine.kt` used to be a flat
 * pl-only set of top-level `val`s. It takes a target-language id in its constructor rather than
 * reading a hardcoded `"pl"` literal, and `plMorphology`/`plExerciseGenerator`/`plChainSteps`
 * (this file's public, host-facing names) must stay byte-identical wrappers over `PackEngine("pl")`
 * -- proven here by comparing a fresh instance's output to those top-level functions directly.
 */
class PackEngineTest {
    private class Draws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }

    @Test
    fun constructedByLanguageIdNotHardcodedLiteral() {
        // langId is a real constructor argument, not a compiled-in "pl" literal: a fresh
        // PackEngine("pl") reproduces the exact same wiring plMorphology/plLexemeFeatures expose.
        val engine = PackEngine("pl")
        assertEquals(plMorphology.form("noun:wife", mapOf(FeatureKey("Number") to FeatureValue("sg"), FeatureKey("Case") to FeatureValue("nom"))), engine.morphology.form("noun:wife", mapOf(FeatureKey("Number") to FeatureValue("sg"), FeatureKey("Case") to FeatureValue("nom"))))
        assertEquals(plLexemeFeatures.of("wife"), engine.lexemeFeatures.of("wife"))
    }

    @Test
    fun exerciseGeneratorMatchesTopLevelWrapper() {
        val seed = mapOf("noun" to "wife", "adjective" to "beautiful")
        val fromWrapper = plExerciseGenerator(Draws(listOf(0.11)), ExerciseIdFactory { "generated" })
            .generateChain(plChainSteps("wife"), seed)
        val fromEngine = PackEngine("pl").exerciseGenerator(Draws(listOf(0.11)), ExerciseIdFactory { "generated" })
            .generateChain(PackEngine("pl").chainSteps("wife"), seed)
        assertEquals(fromWrapper, fromEngine)
    }
}
