package polski.grammar

/** NFC normalization is supplied by each platform; the remaining answer rules stay shared. */
expect fun normalizeNfc(value: String): String
expect fun polishLowercase(value: String): String
expect fun polishUppercase(value: String): String

/** Mirrors the React answer comparison: NFC, Polish lower case, trim, final punctuation, whitespace. */
fun normalize(value: String): String = normalizeNfc(value)
    .let(::polishLowercase)
    .trim()
    .replace(Regex("[.!?]+$"), "")
    .replace(Regex("\\s+"), " ")
