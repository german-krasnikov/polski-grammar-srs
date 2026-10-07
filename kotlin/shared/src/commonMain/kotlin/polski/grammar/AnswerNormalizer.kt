package polski.grammar

/** NFC normalization is supplied by each platform; the remaining answer rules stay shared. */
expect fun normalizeNfc(value: String): String
expect fun polishLowercase(value: String): String
expect fun polishUppercase(value: String): String

/** Mirrors the React answer comparison: NFC, Polish lower case, trim, final punctuation, whitespace. */
fun normalize(value: String): String = normalizeNfc(value)
    .let(::polishLowercase)
    .let(::trimJsWhitespace)
    .replace(Regex("[.!?]+$"), "")
    .let(::collapseJsWhitespace)

/** ECMAScript WhiteSpace and LineTerminator characters, including BOM (U+FEFF). */
private fun Char.isJsWhitespace(): Boolean = this in '\u0009'..'\u000d' || this == ' ' ||
    this == '\u00a0' || this == '\u1680' || this in '\u2000'..'\u200a' ||
    this == '\u2028' || this == '\u2029' || this == '\u202f' ||
    this == '\u205f' || this == '\u3000' || this == '\ufeff'

/** Uses the same edge trimming as JavaScript String.trim(), without platform-dependent Unicode rules. */
fun trimJsWhitespace(value: String): String {
    var start = 0
    var end = value.length
    while (start < end && value[start].isJsWhitespace()) start++
    while (end > start && value[end - 1].isJsWhitespace()) end--
    return value.substring(start, end)
}

private fun collapseJsWhitespace(value: String): String = buildString(value.length) {
    var previousWhitespace = false
    value.forEach { char ->
        if (char.isJsWhitespace()) {
            if (!previousWhitespace) append(' ')
            previousWhitespace = true
        } else {
            append(char)
            previousWhitespace = false
        }
    }
}
