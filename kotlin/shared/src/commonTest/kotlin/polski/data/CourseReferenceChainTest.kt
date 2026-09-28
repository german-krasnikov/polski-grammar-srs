package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource

class CourseReferenceChainTest {
    @Test
    fun matrixSentencesMatchFiveStepExercise() {
        val factory = PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "chain-test" })
        val generated = factory.generateChain(courseSentenceSeeds.first())
        assertEquals(referenceChainRows.map { it.from to it.to }, generated.map { it.source to it.expected })
    }
}
