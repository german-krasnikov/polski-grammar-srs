package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.listening.ListeningExam

fun interface AudioClozePool {
    fun draw(limit: Int, random: RandomSource): List<AudioClozeExercise>
}

class DefaultAudioClozePool(
    private val listeningExams: Map<String, ListeningExam>,
    private val generator: AudioClozeGenerator,
) : AudioClozePool {
    private val allCandidates: Map<String, List<AudioClozeExercise>> by lazy {
        listeningExams.mapValues { (routeId, exam) -> generator.fromExam(routeId, exam) }
            .filterValues { it.isNotEmpty() }
    }

    fun availableExams(): List<String> = allCandidates.keys.toList()

    override fun draw(limit: Int, random: RandomSource): List<AudioClozeExercise> = drawFrom(null, limit, random)

    fun drawFrom(examRouteId: String?, limit: Int, random: RandomSource): List<AudioClozeExercise> {
        val pool = if (examRouteId != null) allCandidates[examRouteId].orEmpty()
        else allCandidates.values.flatten()
        if (pool.isEmpty()) return emptyList()
        val mutable = pool.toMutableList()
        for (i in mutable.lastIndex downTo 1) {
            val j = (random.nextDouble() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = mutable[i]; mutable[i] = mutable[j]; mutable[j] = tmp
        }
        return mutable.distinctBy { it.id }.take(limit)
    }
}
