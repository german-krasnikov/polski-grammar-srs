package polski.spike

enum class SpikeRating {
    AGAIN,
    HARD,
    GOOD,
    EASY,
}

/** Temporary state for the browser renderer experiment, not a learning-progress format. */
data class SpikeSession(
    val input: String = "",
    val revealed: Boolean = false,
    val rating: SpikeRating? = null,
) {
    fun type(value: String): SpikeSession = copy(input = value)

    fun reveal(): SpikeSession = copy(revealed = true)

    fun rate(value: SpikeRating): SpikeSession =
        if (revealed) copy(rating = value) else this

    fun toWire(): String = "${input.length}:$input:${if (revealed) 1 else 0}:${rating?.name.orEmpty()}"

    companion object {
        fun fromWire(raw: String?): SpikeSession? {
            if (raw == null) return null
            val separator = raw.indexOf(':')
            if (separator < 1) return null
            val inputLength = raw.substring(0, separator).toIntOrNull() ?: return null
            if (inputLength < 0 || separator + inputLength + 1 >= raw.length) return null
            val inputEnd = separator + 1 + inputLength
            if (raw.getOrNull(inputEnd) != ':') return null
            val input = raw.substring(separator + 1, inputEnd)
            val suffix = raw.substring(inputEnd + 1).split(':')
            if (suffix.size != 2) return null
            val revealed = when (suffix[0]) {
                "0" -> false
                "1" -> true
                else -> return null
            }
            val rating = if (suffix[1].isEmpty()) null else SpikeRating.entries.find { it.name == suffix[1] } ?: return null
            if (!revealed && rating != null) return null
            return SpikeSession(input, revealed, rating)
        }
    }
}
