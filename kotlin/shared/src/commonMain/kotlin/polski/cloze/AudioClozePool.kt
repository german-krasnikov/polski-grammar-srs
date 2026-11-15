package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.listening.ListeningExam

fun interface AudioClozePool {
    fun draw(limit: Int, random: RandomSource): List<AudioClozeExercise>
}

class DefaultAudioClozePool(
    listeningExams: Map<String, ListeningExam>,
    generator: AudioClozeGenerator,
) : AudioClozePool {
    private val candidates: List<AudioClozeExercise> by lazy {
        listeningExams.flatMap { (routeId, exam) -> generator.fromExam(routeId, exam) }
            .distinctBy { it.id }
    }

    override fun draw(limit: Int, random: RandomSource): List<AudioClozeExercise> {
        if (candidates.isEmpty()) return emptyList()
        val pool = candidates.toMutableList()
        for (i in pool.lastIndex downTo 1) {
            val j = (random.nextDouble() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp
        }
        return pool.take(limit)
    }
}
