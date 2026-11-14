package polski.cloze

import polski.core.engine.RandomSource
import polski.core.model.CoreExercise
import polski.exam.grammar.GrammarTest

fun interface ClozePool {
    fun draw(limit: Int, random: RandomSource): List<ClozeExercise>
}

/** Computes all candidates once at construction; [draw] shuffles and limits on each call. */
class DefaultClozePool(
    grammarTests: List<GrammarTest>,
    courseExercises: List<CoreExercise>,
    generator: ClozeGenerator,
) : ClozePool {
    private val candidates: List<ClozeExercise> by lazy {
        val examItems = grammarTests.flatMap { it.items }.mapNotNull { generator.fromExamItem(it) }
        val courseItems = courseExercises.mapNotNull { generator.fromCoreExercise(it) }
        examItems + courseItems
    }

    override fun draw(limit: Int, random: RandomSource): List<ClozeExercise> {
        if (candidates.isEmpty()) return emptyList()
        val pool = candidates.toMutableList()
        for (i in pool.lastIndex downTo 1) {
            val j = (random.nextDouble() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp
        }
        return pool.take(limit)
    }
}
