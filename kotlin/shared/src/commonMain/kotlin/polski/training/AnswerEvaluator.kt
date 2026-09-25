package polski.training

import polski.grammar.normalize
import polski.model.Evaluation
import polski.model.Exercise

/** Compares normalized primary and accepted answers; distance uses UTF-16 code units. */
fun evaluate(answer: String, exercise: Exercise): Evaluation {
    val normalized = normalize(answer)
    val variants = (listOf(exercise.expected) + exercise.accepted).map(::normalize)
    return Evaluation(
        correct = normalized in variants,
        normalized = normalized,
        expected = exercise.expected,
        distance = variants.minOf { distance(normalized, it) },
    )
}

private fun distance(left: String, right: String): Int {
    var previous = IntArray(right.length + 1) { it }
    for (i in left.indices) {
        val current = IntArray(right.length + 1)
        current[0] = i + 1
        for (j in right.indices) {
            current[j + 1] = minOf(
                previous[j + 1] + 1,
                current[j] + 1,
                previous[j] + if (left[i] == right[j]) 0 else 1,
            )
        }
        previous = current
    }
    return previous[right.length]
}
