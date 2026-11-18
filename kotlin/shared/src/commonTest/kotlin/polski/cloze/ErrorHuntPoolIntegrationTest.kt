package polski.cloze

import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.core.plMorphology
import polski.grammar.normalize
import polski.training.PlExerciseEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ErrorHuntPoolIntegrationTest {
    private var idSeq = 0
    private val engine = PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "g${idSeq++}" })
    private val generator = ErrorHuntGenerator(plMorphology, RandomSource { 0.0 })
    private val courseExercises = engine.coreExercisesForErrorHuntPool()
    private val pool = DefaultErrorHuntPool(courseExercises, generator)
    private val exerciseById = courseExercises.associateBy { it.id }

    @Test fun pool_nonEmpty() {
        val drawn = pool.draw(1000, RandomSource { 0.5 })
        println("ErrorHuntPool size over real pl-ru data: ${drawn.size}")
        assertTrue(drawn.isNotEmpty(), "Pool must not be empty")
    }

    @Test fun pool_allCandidatesValid() {
        val drawn = pool.draw(200, RandomSource { 0.5 })
        for (ex in drawn) {
            assertTrue(ex.tokens[ex.errorTokenIndex].contains(ex.errorForm),
                "token[errorTokenIndex] must contain errorForm for ${ex.id}")
            assertTrue(ex.correctToken.contains(ex.correctForm),
                "correctToken must contain correctForm for ${ex.id}")
            assertNotEquals(ex.errorForm, ex.correctForm, "errorForm must differ from correctForm for ${ex.id}")
            assertEquals(3, ex.options.size, "options must have 3 elements for ${ex.id}")
            assertEquals(3, ex.options.distinct().size, "all options distinct for ${ex.id}")
            assertTrue(ex.correctForm in ex.options, "correctForm must be in options for ${ex.id}")

            val original = exerciseById[ex.sourceExerciseId] ?: continue
            val errorSentence = ex.tokens.joinToString(" ")
            val normalError = normalize(errorSentence)
            val validSentences = (listOf(original.expected) + original.accepted).map { normalize(it) }
            assertTrue(normalError !in validSentences,
                "Error sentence should not be an accepted sentence for ${ex.id}")
        }
    }
}
