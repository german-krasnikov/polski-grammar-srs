package polski.core.engine

/**
 * Pure: pick [count] distinct forms from [paradigm] for [lexemeKey] that differ from [correctForm].
 * Returns fewer than [count] when the paradigm has fewer candidates (syncretism / small paradigm).
 * Never returns [correctForm].
 */
fun pickDistractors(
    paradigm: MorphologyParadigm,
    lexemeKey: String,
    correctForm: String,
    count: Int,
    random: RandomSource,
): List<String> {
    val candidates = paradigm.formsOf(lexemeKey).values.distinct().filter { it != correctForm }
    if (candidates.size <= count) return candidates
    val pool = candidates.toMutableList()
    return List(count) {
        val index = (random.nextDouble() * pool.size).toInt().coerceIn(0, pool.size - 1)
        pool.removeAt(index)
    }
}
