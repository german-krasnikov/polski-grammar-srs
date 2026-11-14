package polski.training

import polski.grammar.normalize
import polski.model.Evaluation
import polski.model.Exercise

/** P1 blueprint §P1.4: which version of [evaluate]'s normalize/match rule judged a given attempt.
 *  Bumped only when the rule itself changes in a way that could change a verdict for existing
 *  accepted-variant data — not on every unrelated edit to this file. A single global int, not a
 *  per-skill table: the matching algorithm is shared across every skill (`evaluate()` takes no
 *  skill-specific rule set). */
const val RUBRIC_VERSION = 1

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
